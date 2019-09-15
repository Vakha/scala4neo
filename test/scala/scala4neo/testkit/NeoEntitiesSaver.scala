package scala4neo.testkit

import monix.execution.Scheduler
import org.scalatest.{BeforeAndAfterAll, Suite}
import scala4neo.codec.ValueCodec
import scala4neo.model.{Node, Relation}
import scala4neo.repository.GenericRepository
import scala4neo.util.future.AwaitHelper
import scala4neo.util.logging.Logs

import scala.collection.mutable
import scala.reflect.runtime.universe.TypeTag

trait NeoEntitiesSaver extends
  Logs with
  BeforeAndAfterAll with
  AwaitHelper { self: Suite =>

  val genericRepo: GenericRepository

  protected implicit def scheduler: Scheduler

  protected val removeFromNeoFunctions: mutable.Buffer[() => Unit] = mutable.Buffer.empty[() => Unit]

  implicit class PersistableNode[N <: Node : TypeTag](node: N) {
    def save(implicit codec: ValueCodec[N]): N = {
      logger.debug(s"Saving $node for ${self.getClass.getSimpleName}")
      genericRepo.saveOrUpdateNode(node).runToFuture.await
      val removeFunction = () => {
        logger.debug(s"Deleting $node for ${self.getClass.getSimpleName}")
        genericRepo.deleteNode(node).runToFuture.await
        ()
      }
      removeFromNeoFunctions.prepend(removeFunction)
      node
    }
  }

  implicit class PersistableRelation[
    S <: Node : ValueCodec,
    T <: Node : ValueCodec
  ](sourceAndTarget: (S, T)) {

    def connectBy[R <: Relation : ValueCodec : TypeTag](relation: R): R = {
      val (source, target) = sourceAndTarget
      logger.debug(s"Saving relation ($relation) between ($source) and ($target) for ${self.getClass.getSimpleName}")
      genericRepo.saveRelation(source, relation, target).runToFuture.await
      val removeFunction = () => {
        logger.debug(s"Deleting relation ($relation) between ($source) and ($target) for ${self.getClass.getSimpleName}")
        genericRepo.deleteRelation(relation).runToFuture.await
        ()
      }
      removeFromNeoFunctions.prepend(removeFunction)
      relation
    }

  }

  abstract override protected def afterAll(): Unit = {
    removeFromNeoFunctions.foreach(_ ())
    removeFromNeoFunctions.clear()
    super.afterAll()
  }

}
