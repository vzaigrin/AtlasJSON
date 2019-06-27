package extractor.atlas

import java.util.Date
import io.circe.parser.parse
import io.circe.{Decoder, HCursor}

case class Attribute(owner: Option[String],
                     createTime: Option[String],
                     qualifiedName: Option[String],
                     name: Option[String],
                     description: Option[String]
                    ) {
  def get(field: String): String = {
    field match {
      case "owner" => owner.getOrElse("")
      case "createTime" => createTime.getOrElse("")
      case "qualifiedName" => qualifiedName.getOrElse("")
      case "name" => name.getOrElse("")
      case "description" => description.getOrElse("")
      case f => s"no such variable attribute.$f"
    }
  }
}

case class Entity(typeName: Option[String],
                  attributes: Attribute,
                  guid: Option[String],
                  status: Option[String],
                  displayText: Option[String],
                  classificationNames: List[Option[String]]
                 ) {
  def get(field: String): String = {
    field.split('.').toList match {
      case "attribute" :: n :: Nil => attributes.get(n)
      case n :: Nil => n match {
        case "typeName" => typeName.getOrElse("")
        case "guid" => guid.getOrElse("")
        case "status" => status.getOrElse("")
        case "displayText" => displayText.getOrElse("")
        case "classificationNames" => classificationNames.map(_.getOrElse("")).mkString(", ")
        case f => s"no such variable $f"
      }
      case f => s"no such variable $f"
    }
  }
}

case class SearchParameters(typeName: String,
                            excludeDeletedEntities: Boolean,
                            includeClassificationAttributes: Boolean,
                            limit: Int,
                            offset: Int
                           )

object SearchParameters {
  implicit val searchParameters: Decoder[SearchParameters] =
    (hCursor: HCursor) => {
      for {
        typeName <- hCursor.get[String]("typeName")
        excludeDeletedEntities <- hCursor.get[Boolean]("excludeDeletedEntities")
        includeClassificationAttributes <- hCursor.get[Boolean]("includeClassificationAttributes")
        limit <- hCursor.get[Int]("limit")
        offset <- hCursor.get[Int]("offset")
      } yield SearchParameters(typeName, excludeDeletedEntities, includeClassificationAttributes, limit, offset)
    }
}

case class AtlasReport(queryType: String, searchParameters: SearchParameters, entities: Option[List[Entity]])

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


  def getDate(dt: Option[Long]): Option[String] = {
    dt match {
      case Some(ts) => Some(new Date(ts).toString)
      case None => None
    }
  }

  def decode(input: String): AtlasReport = {
    parse(input) match {
      case Left(_) => throw new Exception("Error: can't parse JSON")
      case Right(value) =>
        val cursor: HCursor = value.hcursor

        val queryType: String = cursor.downField("queryType").as[String].getOrElse("ERROR")
        if (queryType == "ERROR") throw new Exception("Error: can't parse JSON. No 'queryType'")

        val searchParameters: SearchParameters =
          cursor.get[SearchParameters]("searchParameters") match {
            case Left(_) => throw new Exception("Error: can't parse JSON. No 'searchParameters'")
            case Right(value) => value
          }

        val entities: Option[List[Entity]] =
          cursor.get[List[Entity]]("entities") match {
            case Left(_) => None
            case Right(value) => Some(value)
          }

        AtlasReport(queryType, searchParameters, entities)
    }
  }
}

object AtlasParser {
  def apply(input: String): AtlasReport = new AtlasParser().decode(input)
}
