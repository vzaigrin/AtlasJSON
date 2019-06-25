package extractor.atlas

import scala.io.{BufferedSource, Source}
import java.io.{File, FileInputStream, FileOutputStream}
import java.util
import org.apache.poi.ss.usermodel.{HorizontalAlignment, VerticalAlignment}
import org.apache.poi.xssf.usermodel.{XSSFCell, XSSFCellStyle, XSSFFont, XSSFRow, XSSFSheet, XSSFWorkbook}
import org.snakeyaml.engine.v1.api.{Load, LoadSettings, LoadSettingsBuilder}
import scala.jdk.CollectionConverters._

object AtlasJSON extends App {

  // Function to parse config file and check fields to extract
  def parseConfig(filename: String): List[(String, String)] = {
    def isExist(field: String): Boolean = 
      List("typeName", "attribute.owner", "attribute.createTime", "attribute.qualifiedName",
        "attribute.name", "attribute.description", "guid", "status", "displayText",
        "classificationNames").contains(field)

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
  val arg: Args = ArgsParser(args.toList)

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

  // Create or open Sheet with name searchParameters.typeName
  val typeName: String = report.searchParameters.typeName.getOrElse("")
  val sheet: XSSFSheet = try {
    val getSheet = workbook.getSheet(typeName)
    if (getSheet != null) getSheet
    else workbook.createSheet(typeName)
  } catch {
    case e: Exception =>
      println(s"Error creating sheet $typeName in Excel file ${arg.excelFilename}: ${e.getMessage}")
      sys.exit(-1)
  }

  // Create a header for new sheet
  val font: XSSFFont = workbook.createFont()
  font.setBold(true)
  font.setItalic(false)

  val style: XSSFCellStyle = workbook.createCellStyle
  style.setAlignment(HorizontalAlignment.CENTER)
  style.setVerticalAlignment(VerticalAlignment.CENTER)
  style.setFont(font)
  
  val row0: XSSFRow = sheet.createRow(0)
  row0.setRowStyle(style)

  fields.zipWithIndex.foreach { f =>
    val cell: XSSFCell = row0.createCell(f._2)
    cell.setCellValue(f._1._2)
    cell.setCellStyle(style)
  }

  // Put all entities on the new sheet
  report.entities.zipWithIndex.foreach { e =>
    val row: XSSFRow = sheet.createRow(e._2 + 1)
    fields.zipWithIndex.foreach { f =>
      row.createCell(f._2).setCellValue(e._1.get(f._1._1))
    }
  }

  // Auto size columns
  (0 until fields.length).foreach(sheet.autoSizeColumn)

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
