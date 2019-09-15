package scala4neo.testkit

import java.util.UUID

import io.circe.Encoder
import monix.eval.Task
import monix.execution.Scheduler
import org.neo4j.driver.v1.Value
import org.scalatest.{BeforeAndAfter, FlatSpec}
import scala4neo.codec.{NodeCodec, ValueCodec}
import scala4neo.model.{Node, Relation}
import scala4neo.repository.GenericRepository
import scala4neo.util.future.AwaitHelper

import scala.collection.immutable.Range.Inclusive
import scala.collection.mutable
import scala.reflect.runtime.universe.TypeTag

abstract class RelationCodecSpec[R <: Relation : ValueCodec : TypeTag] extends
  FlatSpec with
  BeforeAndAfter with
  AwaitHelper {

  import io.circe.generic.semiauto._

  case class DummyNode(
    id: UUID
  ) extends Node

  private implicit val dummyNodeValueEncoder: Encoder[DummyNode] = deriveEncoder[DummyNode]

  private implicit val dummyNodeValueCodec: ValueCodec[DummyNode] =
    new NodeCodec[DummyNode] {
      override def convert(record: Value): DummyNode =
        DummyNode(
          id = UUID.fromString(record.get("id").asString)
        )
    }

  private def rndDummy: DummyNode = DummyNode(UUID.randomUUID)

  protected implicit def scheduler: Scheduler

  protected val genericRepo: GenericRepository

  protected val relations: mutable.Buffer[R] = mutable.Buffer.empty[R]
  protected val nodes: mutable.Buffer[DummyNode] = mutable.Buffer.empty[DummyNode]

  protected val repetitionCount: Int = 10

  protected val runs: Inclusive = 1 to 10

  def randomEntity: R

  def updateEntity(entityToUpdate: R, update: R): R

  protected def cleanup(): Unit = {
    Task.traverse(relations)(genericRepo.deleteRelation[R]).runToFuture.await
    Task.traverse(nodes)(genericRepo.deleteNode[DummyNode]).runToFuture.await
    relations.clear()
    nodes.clear()
  }

  protected def insertCheck(before: R => R = identity): Unit = {
    it should "insert new relation and find it by it's id" in {
      runs foreach { _ =>
        val entity = randomEntity
        val saved = save(before(entity))
        relations += saved
        assert(find(saved).contains(saved))
      }
    }
  }

  protected def updateCheck(
    before: R => R = identity,
    beforeUpdate: R => R = _ => randomEntity
  ): Unit = {
    it should "update relation and get updated value back" in {
      runs foreach { _ =>
        val saved = save(before(randomEntity))
        relations += saved
        val toUpdate = updateEntity(saved, beforeUpdate(saved))
        val updated = update(toUpdate)
        val fromDB = find(saved)
        assert(fromDB.contains(updated))
      }
    }
  }

  protected def removeCheck(before: R => R = identity): Unit = {
    it should "remove relation by it's id" in {
      runs foreach { _ =>
        val saved = save(before(randomEntity))
        relations += saved
        delete(saved)
        assert(find(saved).isEmpty)
      }
    }
  }

  protected def correctRelationCodec(
    beforeInsert: R => R = identity,
    beforeUpdate: R => R = _ => randomEntity
  ): Unit = {
    insertCheck(beforeInsert)
    updateCheck(beforeInsert, beforeUpdate)
    removeCheck(beforeInsert)
  }

  protected def save(relation: R): R = {
    val a = rndDummy
    val b = rndDummy
    genericRepo.saveOrUpdateNode(a).runToFuture.await
    genericRepo.saveOrUpdateNode(b).runToFuture.await
    genericRepo.saveRelation(a, relation, b).runToFuture.await
    nodes += a += b
    relation
  }

  protected def update(relation: R): R = {
    genericRepo.updateRelation(relation).runToFuture.await
    relation
  }

  protected def find(relation: R): Option[R] = genericRepo.findRelationById[R](relation.id).runToFuture.await

  protected def delete(relation: R): Unit = genericRepo.deleteRelation(relation).runToFuture.await

  after(cleanup())
}

