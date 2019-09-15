package scala4neo.query

import scala4neo.query.pagination.{PageRequest, SortOrder}
import scala.collection.JavaConverters._

trait CypherQuery {
  type R

  val query: String
  val paramMap: Map[String, Any]
  val pageRequest: Option[PageRequest] = None

  def statement: String = {
    require(
      CypherQuery.isValid(query, paramMap),
      "All parameters from query must be provided and all provided parameters must be used"
    )
    pageRequest match {
      case Some(pr) => s"$query ${orderBy(pr.sort)} SKIP {offset} LIMIT {limit}"
      case None => query
    }
  }

  def parameters: java.util.Map[String, AnyRef] = {
    pageRequest.map { pr =>
      paramMap ++ Map("offset" -> pr.offset, "limit" -> pr.querySize)
    }.getOrElse(paramMap).asInstanceOf[Map[String, AnyRef]].asJava
  }

  private def orderBy(fields: Seq[SortOrder]): String = {
    if (fields.isEmpty) ""
    else fields.map { order =>
      s"${order.field} ${order.direction}"
    }.mkString("ORDER BY ", ", ", "")
  }

}

object CypherQuery {
  trait NoReturn extends CypherQuery { type R = Unit }
  // TODO find a way to make it type instead of trait
  trait Aux[R1] extends CypherQuery { type R = R1 }

  private val paramPattern = "\\{[A-z0-9_]+\\}".r

  private def isValid(query: String, paramMap: Map[String, Any]): Boolean = {
    val declared = paramPattern.findAllIn(query).map(_.drop(1).dropRight(1)).toSet
    val provided = paramMap.keySet
    declared == provided
  }
}
