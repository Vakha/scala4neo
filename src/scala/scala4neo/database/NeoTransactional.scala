package scala4neo.database

import monix.eval.Task

trait NeoTransactional {

  val database: Database

  def withNeoTransaction[T](block: WriteTransaction => Task[T]): Task[T] = {
    database.withWriteTransaction(block)
  }

}
