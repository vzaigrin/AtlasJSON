package extractor.atlas

import scala.io.{BufferedSource, Source}
import java.io.{File, FileInputStream, FileOutputStream}
import java.text.SimpleDateFormat
import java.util.Calendar
import org.apache.poi.xssf.usermodel.{XSSFRow, XSSFSheet, XSSFWorkbook}

object AtlasJSON extends App {

  // Parse command line arguments
  val a: Args = try Args(args.toList)
  catch {
    case e: Exception =>
      Args.usage(e.getMessage)
      sys.exit(-1)
    }

  // Open inout JSON file
  val jsonFile: BufferedSource = Source.fromFile(a.jsonFilename)
  val lines: String = try jsonFile.getLines.mkString
  catch {
    case e: Exception =>
      println(s"Error reading JSON file ${a.jsonFilename}: ${e.getMessage}")
      sys.exit(-1)
  }
  finally {
    jsonFile.close()
  }

  // Parse input JSON file
  val report: AtlasReport = try AtlasReport(lines)
  catch {
    case e: Exception =>
      println(s"Error processing JSON file ${a.jsonFilename}: ${e.getMessage}")
      sys.exit(-1)
  }

  // Open or create Excel file
  val workbook: XSSFWorkbook =
    if (a.add)
      try new XSSFWorkbook(new FileInputStream(new File(a.excelFilename)))
      catch {
        case _: java.io.FileNotFoundException => new XSSFWorkbook()
        case e: Exception =>
          println(s"Error creating excel file ${a.excelFilename}: ${e.getMessage}")
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
      println(s"Error creating sheet $typeName in Excel file ${a.excelFilename}: ${e.getMessage}")
      sys.exit(-1)
  }

  // Create a header for new sheet
  val row0 = sheet.createRow(0)
  row0.createCell(0).setCellValue("typeName")
  row0.createCell(1).setCellValue("owner")
  row0.createCell(2).setCellValue("name")
  row0.createCell(3).setCellValue("qualifiedName")
  row0.createCell(4).setCellValue("displayText")
  row0.createCell(5).setCellValue("status")

  // Put all entities on the new sheet
  var r: Int = 1
  report.entities.foreach { e =>
    val row: XSSFRow = sheet.createRow(r)
    row.createCell(0).setCellValue(e.typeName.getOrElse(""))
    row.createCell(1).setCellValue(e.attributes.owner.getOrElse(""))
    row.createCell(2).setCellValue(e.attributes.name.getOrElse(""))
    row.createCell(3).setCellValue(e.attributes.qualifiedName.getOrElse(""))
    row.createCell(4).setCellValue(e.displayText.getOrElse(""))
    row.createCell(5).setCellValue(e.status.getOrElse(""))
    r += 1
  }

  // Write and close Excel file
  try workbook.write(new FileOutputStream(a.excelFilename))
  catch {
    case e: Exception =>
      println(s"Error writing Excel file ${a.excelFilename}: ${e.getMessage}")
      sys.exit(-1)
  }
  finally {
    workbook.close()
  }
}
