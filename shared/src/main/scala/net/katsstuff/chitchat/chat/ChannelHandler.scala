package net.katsstuff.chitchat.chat

import scala.collection.JavaConverters._
import scala.collection.mutable

import org.spongepowered.api.Sponge
import org.spongepowered.api.data.DataHolder
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.channel.MessageReceiver
import org.spongepowered.api.text.format.TextColors._

import io.github.katrix.katlib.helper.Implicits._
import net.katsstuff.chitchat.ChitChatPlugin
import net.katsstuff.chitchat.chat.channel.{Channel, ChannelRegistry, SimpleChannel}
import net.katsstuff.chitchat.chat.data.ChannelData
import net.katsstuff.chitchat.persistant.StorageLoader

class ChannelHandler(storage: StorageLoader)(implicit plugin: ChitChatPlugin) {

  /**
    * The name of the global channel. This channel should always be present.
    */
  final val GlobalName = "Global"

  private implicit object HandlerOnly extends HandlerOnly

  private val channels: mutable.HashMap[String, Channel] = mutable.HashMap.empty
  val registry = new ChannelRegistry
  reloadChannels()

  /**
    * Reloads the channels and logs in all players again.
    */
  def reloadChannels(): Unit = {
    storage.reload()

    channels.clear()
    channels ++= storage.loadData

    if (!channels.contains(GlobalName)) {
      channels.put(GlobalName, SimpleChannel(GlobalName, t"${GOLD}G", t"The global channel", Set()))
    }

    Sponge.getServer.getOnlinePlayers.asScala.foreach(loginPlayer)
  }

  /**
    * Saves the channels using the [[StorageLoader]].
    */
  def saveChannels(): Unit = storage.saveData(channels.toMap)

  /**
    * The global channel. This channel should always be present.
    */
  def globalChannel:            Channel              = channels(GlobalName)
  def allChannels:              Map[String, Channel] = channels.toMap
  def getChannel(name: String): Option[Channel]      = channels.get(name)

  /**
    * Adds a new channel and saves all the channels.
    * The channel name may not be the same as [[GlobalName]]
    */
  def addChannel(channel: Channel): Boolean =
    if (channels.contains(channel.name) && channel.name != GlobalName) {
      false
    } else {
      channels.put(channel.name, channel)
      saveChannels()
      true
    }

  /**
    * Removes a channel, moves all of it's members to the global channel and
    * saves all the channels. The channel name may not be the
    * same as [[GlobalName]].
    */
  def removeChannel(channel: Channel): Boolean =
    if (channel.name != GlobalName) {
      channel.messageChannel.send(t"${YELLOW}The channel you are in is being deleted. You are being moved to the main channel")
      val global    = globalChannel
      val newGlobal = global.members = global.members ++ channel.members
      channels.put(newGlobal.name, newGlobal)

      channels.remove(channel.name)
      saveChannels()
      true
    } else false

  /**
    * Renames a channel and saves all the channels.
    * The new name may not be the same as [[GlobalName]]
    */
  def renameChannel(channel: Channel, newName: String): Boolean =
    if (channel.name != GlobalName) {
      channels.remove(channel.name)
      channels.put(newName, channel.name = newName)
      saveChannels()
      true
    } else false

  /**
    * Sets the prefix for a channel and saves all the channels.
    */
  def setChannelPrefix(channel: Channel, newPrefix: Text): Unit = {
    channels.put(channel.name, channel.prefix = newPrefix)
    saveChannels()
  }

  /**
    * Sets the prefix for a channel and saves all the channels.
    */
  def setChannelDescription(channel: Channel, newDescription: Text): Unit = {
    channels.put(channel.name, channel.description = newDescription)
    saveChannels()
  }

  /**
    * Sets extra data for a channel and saves all the channels.
    */
  def setExtraData(channel: Channel, extra: String): Option[Text] = {
    val handled = channel.handleExtraData(extra)

    handled.fold(Some(_), newChannel => {
      channels.put(newChannel.name, newChannel)
      saveChannels()
      None
    })
  }

  /**
    * Adds a player to the channel they are currently in.
    */
  def loginPlayer(player: Player): Unit = {
    val channel = getChannelForReceiver(player)
    channels.put(channel.name, channel.addMember(player))
  }

  /**
    * Sets the channel for a [[MessageReceiver]], while removing the receiver
    * from the old channel.
    * @return If setting the channel was successful.
    */
  def setReceiverChannel(receiver: MessageReceiver, channel: Channel): Boolean = {
    val oldChannel = getChannelForReceiver(receiver)

    receiver match {
      case holder: DataHolder =>
        val data   = new ChannelData(channel.name)
        val result = holder.offer(data)

        if (result.isSuccessful) {
          val newOld     = oldChannel.removeMember(receiver)
          val newChannel = channel.addMember(receiver)

          channels.put(newOld.name, newOld)
          channels.put(newChannel.name, newChannel)
        }

        result.isSuccessful
      case _ =>
        val newOld     = oldChannel.removeMember(receiver)
        val newChannel = channel.addMember(receiver)

        channels.put(newOld.name, newOld)
        channels.put(newChannel.name, newChannel)

        true
    }
  }

  /**
    * Tries to find the channel for a [[MessageReceiver]].
    * @return The channel the receiver is currently in. If the channel is
    * found, [[globalChannel]] is returned.
    */
  def getChannelForReceiver(receiver: MessageReceiver): Channel =
    receiver match {
      case holder: DataHolder => channels.getOrElse(holder.getOrElse(plugin.versionHelper.ChannelKey, GlobalName), globalChannel)
      case _ =>
        channels
          .collectFirst {
            case (_, c) if c.members.exists(_.get.contains(receiver)) => c
          }
          .getOrElse(globalChannel)
    }
}

/**
  * Represents that if this type is defined as an implicit parameter on a
  * method, that method may only be called from within the [[ChannelHandler]].
  */
sealed trait HandlerOnly
