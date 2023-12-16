package net.katsstuff.chitchat.chat

import scala.collection.JavaConverters._
import scala.collection.mutable

import org.spongepowered.api.Sponge
import org.spongepowered.api.data.DataHolder
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.channel.MessageReceiver
import org.spongepowered.api.text.format.TextColors._

import net.katsstuff.katlib.helper.Implicits._
import net.katsstuff.katlib.i18n.Resource
import net.katsstuff.chitchat.{CCResource, ChitChatPlugin, Storage}
import net.katsstuff.chitchat.chat.channel.{Channel, ChannelRegistry, SimpleChannel}
import net.katsstuff.chitchat.chat.data.{ChannelData, ImmutableChannelData}

class ChannelHandler(registry: ChannelRegistry)(implicit plugin: ChitChatPlugin) {

  /**
    * The name of the global channel. This channel should always be present.
    */
  final val GlobalName = "Global"

  private val channels = mutable.HashMap.empty[String, Channel]

  /**
    * Reloads the channels and logs in all players again.
    */
  def reloadChannels(storage: Map[String, Channel]): Unit = {
    channels.clear()
    channels ++= storage

    if (!channels.contains(GlobalName)) {
      channels.put(GlobalName, SimpleChannel(GlobalName, t"${GOLD}G", t"The global channel", Set.empty))
    }

    Sponge.getServer.getOnlinePlayers.asScala.foreach(loginPlayer)
  }

  /**
    * Saves the channels using the [[net.katsstuff.chitchat.Storage]].
    */
  private def saveChannels(): Unit =
    Storage.save(plugin.storagePath, channels.toMap).unsafeRunSync() //TODO: Handle exceptions

  /**
    * The global channel. This channel should always be present.
    */
  def globalChannel: Channel = channels(GlobalName)

  /**
    * All the channels currently active.
    */
  def allChannels: Map[String, Channel] = channels.toMap

  /**
    * Gets a specific channel by name.
    */
  def getChannel(name: String): Option[Channel] = channels.get(name)

  /**
    * Adds a new channel and saves all the channels.
    * The channel name may not be the same as [[GlobalName]]
    */
  def addChannel(channel: Channel): Boolean =
    if (!channels.contains(channel.name) && channel.name != GlobalName) {
      modifyChannelSave(channel)
      true
    } else false

  private def modifyChannelSave(channel: Channel): Unit = {
    modifyChannel(channel)
    saveChannels()
  }

  private def modifyChannel(channel: Channel): Unit =
    channels.put(channel.name, channel)

  /**
    * Removes a channel, moves all of it's members to the global channel and
    * saves all the channels. The channel name may not be the
    * same as [[GlobalName]].
    */
  def removeChannel(channel: Channel): Boolean =
    if (channel.name != GlobalName) {
      implicit val resource: Resource = CCResource
      channel.messageChannel.sendLocalized("channel.deleteMoved", t"$YELLOW")

      val data = new ImmutableChannelData(GlobalName)
      channel.members.flatMap(_.get).foreach {
        case holder: DataHolder => holder.offer(data.asMutable())
        case _                  => //Do nothing
      }
      val global    = globalChannel
      val newGlobal = global.members = global.members ++ channel.members

      channels.remove(channel.name)
      modifyChannelSave(newGlobal)
      true
    } else false

  /**
    * Renames a channel and saves all the channels.
    * The new name may not be the same as [[GlobalName]]
    */
  def renameChannel(channel: Channel, newName: String): Boolean =
    if (channel.name != GlobalName && newName != GlobalName) {
      channels.remove(channel.name)
      modifyChannelSave(channel.name = newName)
      true
    } else false

  /**
    * Sets the prefix for a channel and saves all the channels.
    */
  def setChannelPrefix(channel: Channel, newPrefix: Text): Unit =
    modifyChannelSave(channel.prefix = newPrefix)

  /**
    * Sets the prefix for a channel and saves all the channels.
    */
  def setChannelDescription(channel: Channel, newDescription: Text): Unit =
    modifyChannelSave(channel.description = newDescription)

  /**
    * Sets extra data for a channel and saves all the channels.
    */
  def setExtraData(channel: Channel, extra: String): Option[Text] = {
    val handled = channel.handleExtraData(extra)

    handled.fold(Some.apply, newChannel => {
      modifyChannelSave(newChannel)
      None
    })
  }

  /**
    * Adds a player to the channel they are currently in.
    */
  def loginPlayer(player: Player): Unit = {
    val channel = getChannelForReceiver(player)
    modifyChannel(channel.addMember(player))
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

          modifyChannel(newOld)
          modifyChannel(newChannel)
        }

        result.isSuccessful
      case _ =>
        val newOld     = oldChannel.removeMember(receiver)
        val newChannel = channel.addMember(receiver)

        modifyChannel(newOld)
        modifyChannel(newChannel)
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
      case holder: DataHolder =>
        channels.getOrElse(holder.getOrElse(plugin.versionHelper.ChannelKey, GlobalName), globalChannel)
      case _ =>
        channels
          .collectFirst {
            case (_, c) if c.members.exists(_.get.contains(receiver)) => c
          }
          .getOrElse(globalChannel)
    }
}
