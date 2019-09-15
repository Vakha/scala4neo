package scala4neo.query

import java.util.UUID
import scala.collection.JavaConverters._

object GenericQueries {

  case class NodeById[N](label: String,
                         id: UUID) extends CypherNQuery {
    type V = N
    override val query: String = s"MATCH (n: $label {id: {id} }) RETURN n"
    override val paramMap: Map[String, Any] = Map("id" -> id.toString)
  }

  case class RelationById[N](label: String,
                             id: UUID) extends CypherNQuery {
    type V = N
    override val query = s"MATCH ()-[n: $label {id: {id} }]-() RETURN n"
    override val paramMap: Map[String, Any] = Map("id" -> id.toString)
  }

  case class SaveOrUpdateNode[N](label: String,
                                 id: UUID,
                                 json: String) extends CypherQuery {
    override type R = Unit
    override val query: String = s"MERGE (n: $label { id: {id} }) " +
      s"ON CREATE SET n = $json " +
      s"ON MATCH SET n += $json"
    override val paramMap: Map[String, Any] = Map("id" -> id.toString)
  }

  case class SaveRelation[R](sourceLabel: String,
                             targetLabel: String,
                             sourceNodeId: UUID,
                             targetNodeId: UUID,
                             jsonWithLabel: String) extends CypherQuery.NoReturn {

    override val query: String = s"MATCH (source: $sourceLabel {id: {sourceNodeId} }) " +
      s"MATCH (target: $targetLabel {id: {targetNodeId} }) " +
      s"CREATE (source)-[:$jsonWithLabel]->(target)"

    override val paramMap: Map[String, Any] = Map(
      "sourceNodeId" -> sourceNodeId.toString,
      "targetNodeId" -> targetNodeId.toString
    )

  }

  case class UpdateRelation[R](label: String,
                               relationId: UUID,
                               json: String) extends CypherQuery.NoReturn {
    override val query: String = s"MATCH ()-[n:$label { id: {relationId} }]-() SET n += $json"
    override val paramMap: Map[String, Any] = Map(
      "relationId" -> relationId.toString
    )
  }

  case class DeleteNode[N](label: String,
                           nodeId: UUID) extends CypherQuery.NoReturn {
    override val query: String = s"MATCH (n: $label { id: {nodeId} } ) DETACH DELETE n"
    override val paramMap: Map[String, Any] = Map(
      "nodeId" -> nodeId.toString
    )
  }

  case class DeleteRelation[N](label: String,
                               relationId: UUID) extends CypherQuery.NoReturn {
    override val query: String = s"MATCH ()-[n: $label { id: {relationId} }]-() DETACH DELETE n"
    override val paramMap: Map[String, Any] = Map(
      "relationId" -> relationId.toString
    )
  }

  case class DeleteRelations[N](label: String,
                                ids: Seq[UUID]) extends CypherQuery.NoReturn {
    override val query: String = s"MATCH ()-[n: $label]-() " +
      s"WHERE n.id in {relationIds} " +
      s"DETACH DELETE n"
    override val paramMap: Map[String, Any] = Map(
      "relationIds" -> ids.map(_.toString).asJava
    )
  }

}
