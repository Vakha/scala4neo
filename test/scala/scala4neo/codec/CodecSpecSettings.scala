package scala4neo.codec

import com.typesafe.config.ConfigFactory
import monix.execution.Scheduler
import scala4neo.database.Database
import scala4neo.databaseimpl.NeoDatabase
import scala4neo.repository.GenericRepository

trait CodecSpecSettings {

  implicit protected def scheduler: Scheduler = Scheduler.global
  private val config = ConfigFactory.load()
  private val neo4jDB: Database = new NeoDatabase(config)
  protected val genericRepo: GenericRepository = new GenericRepository(neo4jDB)

}
