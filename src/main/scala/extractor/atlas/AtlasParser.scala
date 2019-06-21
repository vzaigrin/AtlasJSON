package extractor.atlas

import java.util.Date
import io.circe.{Decoder, HCursor, parser}

case class Attribute(owner: Option[String],
                     createTime: Option[String],
                     qualifiedName: Option[String],
                     name: Option[String],
                     description: Option[String]
                    )
case class Entity(typeName: Option[String],
                  attributes: Attribute,
                  guid: Option[String],
                  status: Option[String],
                  displayText: Option[String],
                  classificationNames: List[Option[String]]
                 )
case class SearchParameters(typeName: Option[String],
                            excludeDeletedEntities: Boolean,
                            includeClassificationAttributes: Boolean,
                            limit: Int,
                            offset: Int
                           )
case class AtlasReport(queryType: String, searchParameters: SearchParameters, entities: List[Entity])

object AtlasReport {
  def apply(input: String): AtlasReport = AtlasParser().decode(input)
}

class AtlasParser {
  implicit val attributeDecoder: Decoder[Attribute] =
    (hCursor: HCursor) => {
      for {
        owner <- hCursor.get[Option[String]]("owner")
        createTime <- hCursor.get[Option[Long]]("createTime")
        qualifiedName <- hCursor.get[Option[String]]("qualifiedName")
        name <- hCursor.get[Option[String]]("name")
        description <- hCursor.get[Option[String]]("description")
      } yield Attribute(owner, getDate(createTime), qualifiedName, name, description)
    }

  implicit val entityDecoder: Decoder[Entity] =
    (hCursor: HCursor) => {
      for {
        typeName <- hCursor.get[Option[String]]("typeName")
        attributes <- hCursor.get[Attribute]("attributes")
        guid <- hCursor.get[Option[String]]("guid")
        status <- hCursor.get[Option[String]]("status")
        displayText <- hCursor.get[Option[String]]("displayText")
        classificationNames <- hCursor.get[List[Option[String]]]("classificationNames")
      } yield Entity(typeName, attributes, guid, status, displayText, classificationNames)
    }

  implicit val searchParameters: Decoder[SearchParameters] =
    (hCursor: HCursor) => {
      for {
        typeName <- hCursor.get[Option[String]]("typeName")
        excludeDeletedEntities <- hCursor.get[Boolean]("excludeDeletedEntities")
        includeClassificationAttributes <- hCursor.get[Boolean]("includeClassificationAttributes")
        limit <- hCursor.get[Int]("limit")
        offset <- hCursor.get[Int]("offset")
      } yield SearchParameters(typeName, excludeDeletedEntities, includeClassificationAttributes, limit, offset)
    }

  implicit val atlasDecoder: Decoder[AtlasReport] =
    (hCursor: HCursor) => {
      for {
        queryType <- hCursor.get[String]("queryType")
        searchParameters <- hCursor.get[SearchParameters]("searchParameters")
        entities <- hCursor.get[List[Entity]]("entities")
      } yield AtlasReport(queryType, searchParameters, entities)
    }

  def getDate(dt: Option[Long]): Option[String] = {
    dt match {
      case Some(ts) => Some(new Date(ts).toString)
      case None => None
    }
  }

  def decode(input: String): AtlasReport = {
    parser.decode[AtlasReport](input) match {
      case Left(error) => throw new Exception(error.getMessage)
      case Right(value) => value
    }
  }
}

object AtlasParser {
  def apply() = new AtlasParser()
}
