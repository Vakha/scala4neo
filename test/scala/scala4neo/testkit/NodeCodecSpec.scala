package scala4neo.testkit

import monix.eval.Task
import monix.execution.Scheduler
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}
import scala4neo.codec.ValueCodec
import scala4neo.model.Node
import scala4neo.repository.GenericRepository
import scala4neo.util.future.AwaitHelper

import scala.collection.immutable.Range.Inclusive
import scala.collection.mutable
import scala.reflect.runtime.universe.TypeTag

abstract class NodeCodecSpec[N <: Node : ValueCodec : TypeTag] extends
  FlatSpec with
  BeforeAndAfter with
  AwaitHelper {

  protected implicit def scheduler: Scheduler

  protected val genericRepo: GenericRepository

  protected val nodes: mutable.Buffer[N] = mutable.Buffer.empty[N]

  protected val repetitionCount: Int = 10

  protected val runs: Inclusive = 1 to repetitionCount

  def randomEntity: N

  def updateEntity(entityToUpdate: N, update: N): N

  protected def cleanup(): Unit = {
    Task.traverse(nodes)(genericRepo.deleteNode[N]).runToFuture.await
    nodes.clear()
  }

  protected def insertCheck(before: N => N = identity): Unit = {
    it should "insert new node and find it by it's id" in {
      runs foreach { _ =>
        val entity = randomEntity
        val saved = save(before(entity))
        nodes += saved
        assert(find(saved).contains(saved))
      }
    }
  }

  protected def updateCheck(
    before: N => N = identity,
    beforeUpdate: N => N = (_) => randomEntity
  ): Unit = {
    it should "update node and get updated value back" in {
      runs foreach { _ =>
        val saved = save(before(randomEntity))
        nodes += saved
        val toUpdate = updateEntity(saved, beforeUpdate(saved))
        val updated = save(toUpdate)
        val fromDB = find(saved)
        assert(fromDB.contains(updated))
      }
    }
  }

  protected def removeCheck(before: N => N = identity): Unit = {
    it should "remove node by it's id" in {
      runs foreach { _ =>
        val saved = save(before(randomEntity))
        nodes += saved
        delete(saved)
        assert(find(saved).isEmpty)
      }
    }
  }

  protected def correctNodeCodec(
    beforeInsert: N => N = identity,
    beforeUpdate: N => N = _ => randomEntity
  ): Unit = {
    insertCheck(beforeInsert)
    updateCheck(beforeInsert, beforeUpdate)
    removeCheck(beforeInsert)
  }

  protected def save(node: N): N = {
    genericRepo.saveOrUpdateNode(node).runToFuture.await
    node
  }

  protected def find(node: N): Option[N] = genericRepo.findNodeById[N](node.id).runToFuture.await

  protected def delete(node: N): Unit = genericRepo.deleteNode(node).runToFuture.await

  after(cleanup())
}
