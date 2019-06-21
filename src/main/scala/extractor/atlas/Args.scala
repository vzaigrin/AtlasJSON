package extractor.atlas

case class Args(jsonFilename: String, excelFilename: String, add: Boolean)

object Args {
  type ArgsMap = Map[String, String]

  def parseArgs(args: List[String], options: ArgsMap): ArgsMap = {
    args match {
      case "--json" :: head :: tail => parseArgs(tail, options + ("jsonFilename" -> head))
      case "--excel" :: head :: tail => parseArgs(tail, options + ("excelFilename" -> head))
      case "-a" :: tail => parseArgs(tail, options + ("add" -> "true"))
      case _ => options
    }
  }

  def usage(e: String): Unit = {
    println(e)
    println("\nArguments:")
    println(" --json jsonFilename   -- input JSON file")
    println(" --excel excelFilename -- output Excel file")
    println(" -a                    -- flag to add Sheet to existing Excel file, optional, default = false")
  }

  def apply(args: List[String]): Args = {
    val oMap: ArgsMap = parseArgs(args, Map())
    val aFlag: Boolean = if (oMap.contains("add")) true else false
    if (!oMap.contains("jsonFilename")) throw new Exception("jsonFilename is not defined")
    else if (!oMap.contains("excelFilename")) throw new Exception("excelFilename is not defined")
    else Args(oMap("jsonFilename"), oMap("excelFilename"), add = aFlag)
  }
}
