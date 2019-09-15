package scala4neo.util.future

import scala.concurrent.{Await, Awaitable, Future}

trait AwaitHelper {

  protected def await[T](awaitable: Awaitable[T]): T = {
    import scala.concurrent.duration._
    Await.result(awaitable, 10.second)
  }

  implicit class FutureHelper[T](future: Future[T]) {
    def await: T = {
      import scala.concurrent.duration._
      Await.result(future, 10.second)
    }
  }

}