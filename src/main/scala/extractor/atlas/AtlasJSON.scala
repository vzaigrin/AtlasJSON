package extractor.atlas

import scala.io.{BufferedSource, Source}
import java.io.{File, FileInputStream, FileOutputStream}
import java.text.SimpleDateFormat
import java.util
import java.util.Calendar

import org.apache.poi.ss.usermodel.{HorizontalAlignment, VerticalAlignment}
import org.apache.poi.xssf.usermodel.{XSSFCell, XSSFCellStyle, XSSFRow, XSSFSheet, XSSFWorkbook}
import org.snakeyaml.engine.v1.api.{Load, LoadSettings, LoadSettingsBuilder}

import scala.jdk.CollectionConverters._

object AtlasJSON extends App {

  // Function to parse config file and check fields to extract
  def parseConfig(filename: String): List[(String, String)] = {
    def isExist(field: String): Boolean = {
      field match {
        case "typeName" => true
        case "attribute.owner" => true
        case "attribute.createTime" => true
        case "attribute.qualifiedName" => true
        case "attribute.name" => true
        case "attribute.description" => true
        case "guid" => true
        case "status" => true
        case "displayText" => true
        case "classificationNames" => true
        case _ => false
      }
    }

    val settings: LoadSettings = new LoadSettingsBuilder().build()
    val load: Load = new Load(settings)

    val fields: List[(String, String)] = try
      load
        .loadFromInputStream(new FileInputStream(new File(filename)))
        .asInstanceOf[util.LinkedHashMap[String, String]]
        .asScala
        .toList
    catch {
      case e: Exception =>
        println(s"Error processing configuration file $filename: ${e.getMessage}")
        sys.exit(-1)
    }

    fields.filter(f => !isExist(f._1)).foreach(f => println(s"WARNING: no such field $f"))
    fields.filter(f => isExist(f._1))
  }

  // Parse command line arguments
  val arg: Args = try Args(args.toList)
  catch {
    case e: Exception =>
      Args.usage(e.getMessage)
      sys.exit(-1)
    }

  // Get fields to extract from Config file
  val fields: List[(String, String)] = parseConfig(arg.configFilename)

  // Open inout JSON file
  val jsonFile: BufferedSource = Source.fromFile(arg.jsonFilename)
  val lines: String = try jsonFile.getLines.mkString
  catch {
    case e: Exception =>
      println(s"Error reading JSON file ${arg.jsonFilename}: ${e.getMessage}")
      sys.exit(-1)
  }
  finally {
    jsonFile.close()
  }

  // Parse input JSON file
  val report: AtlasReport = try AtlasReport(lines)
  catch {
    case e: Exception =>
      println(s"Error processing JSON file ${arg.jsonFilename}: ${e.getMessage}")
      sys.exit(-1)
  }

  // Open or create Excel file
  val workbook: XSSFWorkbook =
    if (new File(arg.excelFilename).exists())
      try new XSSFWorkbook(new FileInputStream(new File(arg.excelFilename)))
      catch {
        case e: Exception =>
          println(s"Error creating excel file ${arg.excelFilename}: ${e.getMessage}")
          sys.exit(-1)
      }
    else new XSSFWorkbook()

  // Add Sheet with name searchParametrs.typeName
  val typeName = report.searchParameters.typeName.getOrElse("")
  val sheet: XSSFSheet = try workbook.createSheet(typeName)
  catch {
    case _: java.lang.IllegalArgumentException =>
      val today: String = new SimpleDateFormat("_yyyy_MM_dd_H_mm").format(Calendar.getInstance().getTime)
      workbook.createSheet(typeName + today)
    case e: Exception =>
      println(e)
      println(s"Error creating sheet $typeName in Excel file ${arg.excelFilename}: ${e.getMessage}")
      sys.exit(-1)
  }

  // Create a header for new sheet
  val row0: XSSFRow = sheet.createRow(0)

  val font = workbook.createFont()
  font.setBold(true)
  font.setItalic(false)

  val style = row0.getRowStyle
  /*
  style.setAlignment(HorizontalAlignment.CENTER)
  style.setVerticalAlignment(VerticalAlignment.CENTER)
  style.setFont(font)
  */

  var c: Int = 0
  fields.foreach { f =>
    row0.createCell(c).setCellValue(f._2)
    c = c + 1
  }

  // Put all entities on the new sheet
  var r: Int = 1
  report.entities.foreach { e =>
    val row: XSSFRow = sheet.createRow(r)
    c = 0
    fields.foreach { f =>
      row.createCell(c).setCellValue(e.get(f._1))
      c = c + 1
    }
    r += 1
  }

  // Write and close Excel file
  try workbook.write(new FileOutputStream(arg.excelFilename))
  catch {
    case e: Exception =>
      println(s"Error writing Excel file ${arg.excelFilename}: ${e.getMessage}")
      sys.exit(-1)
  }
  finally {
    workbook.close()
  }
}
