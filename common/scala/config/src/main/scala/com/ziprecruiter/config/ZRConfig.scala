package com.ziprecruiter.config

import org.json4s.JsonAST.{JString}
import org.json4s.{CustomSerializer, JInt,JBool, JValue}
import org.json4s.jackson.JsonMethods
import org.json4s.JValue
import profig._

class ZRConfig {
  def getConfig[T:Manifest] : T = {
    try {
        loadConfig()
        parseJson[T](Profig.json.toString())
    }
    catch {
      case e: Exception =>
        throw new RuntimeException(s"Failed to read json config file: ${e.getMessage}", e)
    }
  }
  def mergeCommandLineArgs[T:Manifest](commandLineArgs:Seq[String]) : T = {
    try {
      loadConfig()
      Profig.merge(commandLineArgs)
      parseJson[T](Profig.json.toString())
    }
    catch {
      case e: Exception =>
        throw new RuntimeException(s"Failed to read json config file: ${e.getMessage}", e)
    }
  }
  def loadConfig(): Unit ={
    val configPath = scala.util.Properties.propOrElse("ZR_APP_CONFIG",
      scala.util.Properties.envOrElse("ZR_APP_CONFIG", null))
     Profig.load(ProfigLookupPath(configPath,FileType.Json,LoadType.Merge))
  }

  def parseJson[T:Manifest](jsonString: String): T = {
    implicit val formats = org.json4s.DefaultFormats + new NumberSerializer() + new BooleanSerializer()
    val json: JValue = JsonMethods.parse(jsonString)
    json.camelizeKeys.extract[T]
  }

  class BooleanSerializer extends CustomSerializer[Boolean](_ =>({
    case JString(x) => x.toBoolean
    case JBool(x) => x
  },
    {
      case x: Boolean => JBool(x)
    }
  ))

  class NumberSerializer extends CustomSerializer[Int](_ =>({
      case JInt(x) => x.toInt
      case JString(x) => x.toInt
    },
    {
      case x: Int => JInt(x)
    }
  ))
}


object ZRConfig {
  def apply[T: Manifest]() : T = {
    val config = new ZRConfig
    config.getConfig[T]
  }
  def apply[T: Manifest](jsonString: String) : T = {
    val config = new ZRConfig
    config.parseJson[T](jsonString)
  }
  def apply[T: Manifest](args:Array[String]) : T = {
    val config = new ZRConfig
    config.mergeCommandLineArgs[T](args)
  }
}
