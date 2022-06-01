package com.ziprecruiter.config

import com.fasterxml.jackson.core.JsonParseException
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfterAll, FunSuite}
import org.junit.Assert.assertEquals

@RunWith(classOf[JUnitRunner])
class ZRConfigTest extends FunSuite with BeforeAndAfterAll {

  override protected def beforeAll(): Unit = {
    scala.util.Properties.setProp("ZR_APP_CONFIG", "src/test/resources/test.json")
  }

  case class SimpleConfig(tier: String)

  test("config loaded") {
    val config = ZRConfig[SimpleConfig]
    assertEquals("dev", config.tier)
  }

  case class ConfigWithBool(tier: String, boolExample: Boolean)

  test("parsing boolean and camel case") {
    val config = ZRConfig[ConfigWithBool]
    assert(config.boolExample)
  }

  case class ConfigWithOptional(tier: String, optionalField: Option[String])

  test("optional field") {
    val config = ZRConfig[ConfigWithOptional]
    assertEquals("default", config.optionalField.getOrElse("default"))
  }

  case class InnerConfig(executors: Int)
  case class NestedConfig(tier: String, spark: InnerConfig)

  test("nested config") {
    val jsonString = "{ \"tier\": \"dev\", \"spark\" : { \"executors\": 5 }}"
    val config = ZRConfig[NestedConfig](jsonString)
    assertEquals(5, config.spark.executors)
  }

  test("malformed json config") {
    assertThrows[JsonParseException] {
      ZRConfig[SimpleConfig]("{")
    }
  }

  case class MissingParamConfig(nonExistentParam: String)

  test("missing config param") {
    val caught =
      intercept[RuntimeException] {
        ZRConfig[MissingParamConfig]
      }
    assertEquals(
      """Failed to read json config file: No usable value for nonExistentParam
        |Did not find value which can be converted into java.lang.String""".stripMargin,
      caught.getMessage())
  }

  case class CommandLineConfig(tier:String)

  case class CommandLineConfigNested(tier:String,spark: InnerConfig)

  case class CommandLineConfigBoolean(tier:String,boolExampleNew: Boolean)

  test ("command line arguments without overriding"){
    val commandLineArgs = Array("--newArg=a")
    val config = ZRConfig[CommandLineConfig](commandLineArgs)
    assertEquals("dev",config.tier)
  }

  test ("command line arguments overriding"){
    val commandLineArgs = Array("--tier=prod")
    val config = ZRConfig[CommandLineConfig](commandLineArgs)
    assertEquals("prod",config.tier)
  }

  test ("command line arguments bool"){
    val commandLineArgs = Array("--boolExampleNew=true")
    val config = ZRConfig[CommandLineConfigBoolean](commandLineArgs)
    assertEquals(true,config.boolExampleNew)
  }

  test ("command line arguments set nested"){
    val commandLineArgs = Array("--tier=prod","--spark.executors=3")
    val config = ZRConfig[CommandLineConfigNested](commandLineArgs)
    assertEquals(3,config.spark.executors)
  }

  case class ConfigWithArray(tier: String, arrValues: List[String])

  test("array in config"){
    val jsonString =
      """{
        |"tier": "dev",
        |"arr_values": ["val1", "val2"]
        |}""".stripMargin
    val config = ZRConfig[ConfigWithArray](jsonString)
    assertEquals(2, config.arrValues.length)
    assertEquals("val2", config.arrValues(1))
  }
}