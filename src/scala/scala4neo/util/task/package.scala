package scala4neo.util

import java.util.concurrent.CompletionStage
import monix.eval.Task

package object task {
  import monix.java8.eval._

  implicit class PimpCompletionStage[R](val completionStage: CompletionStage[R]) extends AnyVal {
    def toTask: Task[R] = {
      Task.fromCompletableFuture(completionStage.toCompletableFuture)
    }
  }
}
