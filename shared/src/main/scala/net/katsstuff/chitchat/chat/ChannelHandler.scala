package net.katsstuff.chitchat.chat

import scala.collection.mutable

import org.spongepowered.api.data.DataHolder
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.channel.MessageReceiver

import io.github.katrix.katlib.helper.Implicits._
import net.katsstuff.chitchat.ChitChatPlugin
import net.katsstuff.chitchat.chat.channel.{Channel, ChannelRegistry, SimpleChannel}
import net.katsstuff.chitchat.persistant.StorageLoader

class ChannelHandler(storage: StorageLoader)(implicit plugin: ChitChatPlugin) {

  private implicit object HandlerOnly extends HandlerOnly

  private var channels: mutable.HashMap[String, Channel] = _
  val registry = new ChannelRegistry
  reloadChannels()

  def reloadChannels(): Unit = {
    val map = new mutable.HashMap[String, Channel]
    map ++= storage.loadData

    if (!map.contains("global")) {
      map.put("global", SimpleChannel("global", t"G", t"The global channel", Set()))
    }

    channels = map
  }

  def saveChannels(): Unit = storage.saveData(channels.toMap)

  def globalChannel: Channel              = channels("global")
  def allChannels:   Map[String, Channel] = channels.toMap
  def getChannel(name: String): Option[Channel] = channels.get(name)

  def addChannel(channel: Channel): Boolean =
    if (channels.contains(channel.name) && channel.name != "global") {
      false
    } else {
      channels.put(channel.name, channel)
      saveChannels()
      true
    }

  def removeChannel(channel: Channel): Boolean =
    if (channel.name != "global") {
      channels.remove(channel.name)
      saveChannels()
      true
    } else false

  def renameChannel(channel: Channel, newName: String): Boolean =
    if (channel.name != "global") {
      channels.remove(channel.name)
      channels.put(newName, channel.name = newName)
      saveChannels()
      true
    } else false

  def setChannelPrefix(channel: Channel, newPrefix: Text): Unit = {
    channels.put(channel.name, channel.prefix = newPrefix)
    saveChannels()
  }

  def setChannelDescription(channel: Channel, newDescription: Text): Unit = {
    channels.put(channel.name, channel.description = newDescription)
    saveChannels()
  }

  def setExtraData(channel: Channel, extra: String): Option[Text] = {
    val handled = channel.handleExtraData(extra)

    handled.fold(Some(_), newChannel => {
      channels.put(newChannel.name, newChannel)
      saveChannels()
      None
    })
  }

  def setReceiverChannel(receiver: MessageReceiver, channel: Channel): Unit = {
    val channel = getChannelForReceiver(receiver)

    receiver match {
      case holder: DataHolder =>
        val result = holder.offer(plugin.versionHelper.ChannelKey, channel.name)

        if (result.isSuccessful) {
          val newOld     = channel.removeMember(receiver)
          val newChannel = channel.addMember(receiver)

          channels.put(newOld.name, newOld)
          channels.put(newChannel.name, channel)
        }

      case _ =>
        val newOld     = channel.removeMember(receiver)
        val newChannel = channel.addMember(receiver)

        channels.put(newOld.name, newOld)
        channels.put(newChannel.name, channel)
    }
  }

  def getChannelForReceiver(receiver: MessageReceiver): Channel =
    receiver match {
      case holder: DataHolder => channels.getOrElse(holder.getOrElse(plugin.versionHelper.ChannelKey, "global"), globalChannel)
      case _ =>
        channels
          .collectFirst {
            case (_, c) if c.members.exists(_.get.contains(receiver)) => c
          }
          .getOrElse(globalChannel)
    }
}

sealed trait HandlerOnly
