package net.katsstuff.chitchat.persistant

import java.nio.file.Path
import java.util.{Map => JMap}

import scala.collection.JavaConverters._

import io.github.katrix.katlib.KatPlugin
import io.github.katrix.katlib.helper.Implicits._
import io.github.katrix.katlib.helper.LogHelper
import io.github.katrix.katlib.persistant.ConfigurateBase
import net.katsstuff.chitchat.chat.channel.Channel
import ninja.leaping.configurate.ConfigurationNode
import ninja.leaping.configurate.gson.GsonConfigurationLoader

class StorageLoader(dir: Path)(implicit plugin: KatPlugin)
    extends ConfigurateBase[Map[String, Channel], ConfigurationNode, GsonConfigurationLoader](
      dir,
      "storage.json",
      path => GsonConfigurationLoader.builder().setPath(path).build()
    ) {

  private val mapTypeToken = typeToken[JMap[String, Channel]]

  override def loadData: Map[String, Channel] = {
    val node = channelNode
    versionNode.getString("1") match {
      case "1" =>
        Option(node.getValue(mapTypeToken)).fold {
          LogHelper.error("Could not load channels from storage.")
          Map[String, Channel]()
        } (map => Map(map.asScala.toSeq: _*))

      case unknown =>
        LogHelper.error(s"Unknown version for config $unknown. Using default")
        Map()
    }
  }

  override def saveData(data: Map[String, Channel]): Unit = {
    versionNode.setValue("1")

    channelNode.setValue(mapTypeToken, data.asJava)

    saveFile()
  }

  def channelNode: ConfigurationNode = cfgRoot.getNode("channel")
  def versionNode: ConfigurationNode = cfgRoot.getNode("version")
}
