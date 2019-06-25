package extractor.atlas

case class Args(jsonFilename: String, excelFilename: String, configFilename: String)

class ArgsParser {
  private val optionsList = List(
    ("--json", "jsonFilename", "input JSON file"),
    ("--excel", "excelFilename", "output Excel file"),
    ("--config", "configFilename", "file with Entities fields to extract"))

  private val optionsMax1 = optionsList.map(_._1.length).max
  private val optionsMax2 = optionsList.map(_._2.length).max
  private val optionsSet = optionsList.map(_._1).toSet
  private var optionsMap = optionsList.flatMap { o => Map(o._1 -> "") }.toMap

  def parse(argsList: List[String]): Args = {
    // Indexes for keys in options list
    val keyIndexes = 0 until argsList.length by 2
    // Set of keys from args
    val argsSet = keyIndexes.map(argsList(_)).toSet
    // Check if we have all the keys in the args
    if (argsSet.equals(optionsSet)) {
      keyIndexes.foreach { a => optionsMap += argsList(a) -> argsList(a + 1) }
      Args(optionsMap("--json"), optionsMap("--excel"), optionsMap("--config"))
    } else {
      print("Error: incorrect command-line arguments.\nNo keys: ")
      println(optionsSet.diff(argsSet).mkString(", "))
      println("\nUsage:")
      optionsList.foreach { o =>
        print(String.format("%1$" + -1 * optionsMax1 + "s  ", o._1))
        print(String.format("%1$" + -1 * optionsMax2 + "s -- ", o._2))
        println(s"${o._3}")
      }
      sys.exit(-1)
    }
  }
}

object ArgsParser {
  def apply(argsList: List[String]): Args = new ArgsParser().parse(argsList)
}
