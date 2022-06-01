package spark_example

import com.ziprecruiter.config.ZRConfig

case class Config(
  x: String,
)

object App {
  def main(args: Array[String]): Unit = {
    val c = ZRConfig[Config]()
    println(c)
  }
}
