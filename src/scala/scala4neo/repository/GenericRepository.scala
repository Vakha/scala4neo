package scala4neo.repository

import java.util.UUID

import monix.eval.Task
import scala4neo.codec.{RecordCodec, ValueCodec}
import scala4neo.database._
import scala4neo.model.{NRecord, Node, Relation}
import scala4neo.query.{CypherCountQuery, CypherNQuery, CypherQuery, GenericQueries}

import scala.reflect.runtime.universe.TypeTag

class GenericRepository(val db: Database) {

  import scala4neo.codec.implicits._

  def findNodeById[N <: Node : ValueCodec : TypeTag]
                  (id: UUID)
                  (implicit tx: Transaction = ReadAutoCommitTransaction): Task[Option[N]] = {
    val label = ValueCodec[N].label
    val query = GenericQueries.NodeById[N](label, id)
    db.read(query).map(_.headOption.map(_.convertTo[NRecord[N]].n))
  }

  def findRelationById[R <: Relation : ValueCodec : TypeTag]
                      (id: UUID)
                      (implicit tx: Transaction = ReadAutoCommitTransaction): Task[Option[R]] = {
    val label = ValueCodec[R].label
    val query = GenericQueries.RelationById[R](label, id)
    db.read(query).map(_.headOption.map(_.convertTo[NRecord[R]].n))
  }

  /**
    * Special case of [[findCustomAllBy]] where returned value should have n alias.*/
  def findAllBy[V, Q <: CypherNQuery : TypeTag]
               (query: Q)
               (implicit codec: RecordCodec[NRecord[query.V]],
                tx: Transaction = ReadAutoCommitTransaction): Task[Seq[query.V]] =
    findCustomAllBy(query).map(_.map(_.n))

  def findCustomAllBy[R, Q <: CypherQuery : TypeTag]
                     (query: Q)
                     (implicit codec: RecordCodec[query.R],
                      tx: Transaction = ReadAutoCommitTransaction): Task[Seq[query.R]] = {
    db.read[Q](query).map(_.map(codec.convert(_)))
  }

  /**
    * Find one Node or Relation by provided query.
    * The query must return result with name n (exactly),
    * e.g. RETURN n */
  def findOneBy[V, Q <: CypherNQuery : TypeTag]
               (query: Q)
               (implicit codec: RecordCodec[NRecord[query.V]],
                tx: Transaction = ReadAutoCommitTransaction): Task[Option[query.V]] = {
    findCustomOneBy(query).map(_.map(_.n))
  }

  def findCustomOneBy[Q <: CypherQuery : TypeTag]
                     (query: Q)
                     (implicit codec: RecordCodec[query.R],
                      tx: Transaction = ReadAutoCommitTransaction): Task[Option[query.R]] = {
    db.read(query).flatMap { res =>
      if (res.size <= 1)
        Task.eval(res.headOption.map(codec.convert(_)))
      else
        Task.raiseError(TooManyRowsException(1, res.size))
    }
  }

  def existsBy[T, Q <: CypherQuery : TypeTag]
              (query: Q)
              (implicit tx: Transaction = ReadAutoCommitTransaction): Task[Boolean] = {
    db.read(query).map(_.nonEmpty)
  }

  def countBy[Q <: CypherCountQuery : TypeTag]
             (query: Q)
             (implicit tx: Transaction = ReadAutoCommitTransaction): Task[Long] = {
    findCustomOneBy(query).map(_.map(_.count).getOrElse(0))
  }

  def saveOrUpdateNode[N <: Node : TypeTag]
                      (node: N)
                      (implicit codec: ValueCodec[N],
                       tx: WriteTransaction = WriteAutoCommitTransaction): Task[Unit] = {
    val label = codec.label
    val json = codec.toJson(node)
    val query = GenericQueries.SaveOrUpdateNode[N](label, node.id, json)
    db.write(query).flatMap {
      case true => Task.unit
      case false => Task.raiseError(NodeNotSavedException(codec.toJsonWithLabel(node)))
    }
  }

  def saveRelation[S <: Node : ValueCodec,
                   R <: Relation : TypeTag,
                   T <: Node : ValueCodec]
                  (sourceNode: S,
                   relation: R,
                   targetNode: T)
                  (implicit codecR: ValueCodec[R],
                   tx: WriteTransaction = WriteAutoCommitTransaction): Task[Unit] = {
    val query = GenericQueries.SaveRelation[R](
      sourceLabel = ValueCodec[S].label,
      targetLabel = ValueCodec[T].label,
      sourceNodeId = sourceNode.id,
      targetNodeId = targetNode.id,
      jsonWithLabel = codecR.toJsonWithLabel(relation)
    )
    db.write(query).flatMap {
      case true => Task.unit
      case false => Task.raiseError(
        RelationNotSavedException(
          relationJson = codecR.toJsonWithLabel(relation),
          sourceJson = ValueCodec[S].toJsonWithLabel(sourceNode),
          targetJson = ValueCodec[T].toJsonWithLabel(targetNode)
        )
      )
    }
  }

  def updateRelation[R <: Relation : TypeTag]
                    (relation: R)
                    (implicit codec: ValueCodec[R],
                     tx: WriteTransaction = WriteAutoCommitTransaction): Task[Unit] = {
    val label = codec.label
    val json = codec.toJson(relation)
    val query = GenericQueries.UpdateRelation[R](label, relation.id, json)
    db.write(query).flatMap {
      case true => Task.unit
      case false => Task.raiseError(RelationNotUpdatedException(codec.toJsonWithLabel(relation)))
    }
  }

  def deleteNode[N <: Node : ValueCodec : TypeTag]
                (node: N)
                (implicit tx: WriteTransaction = WriteAutoCommitTransaction): Task[Unit] = {
    val query = GenericQueries.DeleteNode[N](ValueCodec[N].label, node.id)
    db.write(query).void
  }

  def deleteRelation[R <: Relation : ValueCodec : TypeTag]
                    (relation: R)
                    (implicit tx: WriteTransaction = WriteAutoCommitTransaction): Task[Unit] = {
    val query = GenericQueries.DeleteRelation[R](ValueCodec[R].label, relation.id)
    db.write(query).void
  }

  def deleteRelations[R <: Relation : ValueCodec : TypeTag]
                     (relations: Seq[R])
                     (implicit tx: WriteTransaction = WriteAutoCommitTransaction): Task[Unit] = {
    val query = GenericQueries.DeleteRelations[R](ValueCodec[R].label, relations.map(_.id))
    db.write(query).void
  }

}