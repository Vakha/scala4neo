package scala4neo.database

import org.neo4j.driver.v1.{Transaction => DriverTransaction}

sealed trait Transaction {
  val isAutoCommit: Boolean
}

object ReadAutoCommitTransaction extends Transaction {
  override val isAutoCommit: Boolean = true
}

sealed trait WriteTransaction extends Transaction

object WriteAutoCommitTransaction extends WriteTransaction {
  override val isAutoCommit: Boolean = true
}

case class WriteNonAutoCommitTransaction(
  value: DriverTransaction
) extends WriteTransaction {
  override val isAutoCommit: Boolean = false
}
