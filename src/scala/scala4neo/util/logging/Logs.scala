package scala4neo.util.logging

import org.slf4j.{Logger, LoggerFactory}

trait Logs {

  protected val logger: Logger = {
    val name = getClass.getName
    val index = name.indexOf('$')
    if (index > 0) {
      LoggerFactory.getLogger(name.substring(0, index))
    } else {
      LoggerFactory.getLogger(name)
    }
  }

}
