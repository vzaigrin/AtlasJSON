package extractor.atlas

import org.scalatest.FunSuite

class ArgsTest extends FunSuite {

  test("Parse command line argumens") {
    val args: Array[String] = Array("--json", "json_file", "--excel", "excel_file", "-c")
    assert(Args(args.toList) === Args("json_file", "excel_file", add = false))
  }

}
