package extractor.atlas

import org.scalatest.FunSuite

class ArgsTest extends FunSuite {

  test("Parse command line arguments") {
    val args: Array[String] = Array("--json", "json_file", "--excel", "excel_file", "--config", "config_file")
    assert(Args(args.toList) === Args("json_file", "excel_file", "config_file"))
  }

}
