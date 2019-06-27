package extractor.atlas

import org.scalatest.FunSuite

class AtlasTest extends FunSuite {

  test("Parse Atlas JSON decode Basic search") {
    val lines: String = """{"queryType":"BASIC","searchParameters":{"typeName":"hive_db","excludeDeletedEntities":false,"includeClassificationAttributes":false,"limit":10000,"offset":0},"entities":[{"typeName":"hive_db","attributes":{"owner":"user1","createTime":null,"qualifiedName":"001@hadoop","name":"001","description":null},"guid":"11111111-1111-1111-1111-111111111111","status":"ACTIVE","displayText":"001","classificationNames":[]},{"typeName":"hive_db","attributes":{"owner":"user2","createTime":null,"qualifiedName":"002@hadoop","name":"002","description":null},"guid":"22222222-2222-2222-2222-222222222222","status":"ACTIVE","displayText":"002","classificationNames":[]},{"typeName":"hive_db","attributes":{"owner":"user3","createTime":null,"qualifiedName":"003@hadoop","name":"003","description":null},"guid":"33333333-3333-3333-3333-333333333333","status":"ACTIVE","displayText":"003","classificationNames":[]}]}"""
    assert(AtlasParser(lines) === AtlasReport("BASIC", SearchParameters("hive_db", excludeDeletedEntities = false, includeClassificationAttributes = false, 10000, 0),
      Some(List(
        Entity(Some("hive_db"), Attribute(Some("user1"), None, Some("001@hadoop"), Some("001"), None),
          Some("11111111-1111-1111-1111-111111111111"), Some("ACTIVE"), Some("001"), List()),
        Entity(Some("hive_db"), Attribute(Some("user2"), None, Some("002@hadoop"), Some("002"), None),
          Some("22222222-2222-2222-2222-222222222222"), Some("ACTIVE"), Some("002"), List()),
        Entity(Some("hive_db"), Attribute(Some("user3"), None, Some("003@hadoop"), Some("003"), None),
          Some("33333333-3333-3333-3333-333333333333"), Some("ACTIVE"), Some("003"), List())))))
  }

  test("Parse Atlas JSON decode Basic search, no Entities") {
    val lines = """{"queryType":"BASIC","searchParameters":{"typeName":"hive_db","excludeDeletedEntities":false,"includeClassificationAttributes":false,"limit":10000,"offset":10000}}"""
    assert(AtlasParser(lines) === AtlasReport("BASIC", SearchParameters("hive_db", excludeDeletedEntities = false, includeClassificationAttributes = false, 10000, 10000), None))
  }
}