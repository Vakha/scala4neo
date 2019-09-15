package scala4neo.util

import com.typesafe.config.Config

package object config {

  implicit class RichConfig(val config: Config) extends AnyVal {

    def getStringOpt(path: String): Option[String] =
      if (config.hasPath(path)) Some(config.getString(path))
      else None

  }

}
