package extractor.atlas

case class Args(jsonFilename: String, excelFilename: String, configFilename: String)

object Args {
  type ArgsMap = Map[String, String]

  def parseArgs(args: List[String], options: ArgsMap): ArgsMap = {
    args match {
      case "--json" :: head :: tail => parseArgs(tail, options + ("jsonFilename" -> head))
      case "--excel" :: head :: tail => parseArgs(tail, options + ("excelFilename" -> head))
      case "--config" :: head :: tail => parseArgs(tail, options + ("configFilename" -> head))
      case _ => options
    }
  }

  def usage(e: String): Unit = {
    println(e)
    println("\nArguments:")
    println(" --json jsonFilename     -- input JSON file")
    println(" --excel excelFilename   -- output Excel file")
    println(" --config configFilename -- file with Entities fields to extract")
  }

  def apply(args: List[String]): Args = {
    val oMap: ArgsMap = parseArgs(args, Map())
    if (!oMap.contains("jsonFilename")) throw new Exception("jsonFilename is not defined")
    else if (!oMap.contains("excelFilename")) throw new Exception("excelFilename is not defined")
    else if (!oMap.contains("configFilename")) throw new Exception("configFilename is not defined")
    else Args(oMap("jsonFilename"), oMap("excelFilename"), oMap("configFilename"))
  }
}
