package net.katsstuff.chitchat.chat

import scala.collection.mutable

import org.spongepowered.api.entity.living.player.Player

import net.katsstuff.chitchat.ChitChatPlugin
import net.katsstuff.chitchat.chat.channel.{Channel, SimpleChannel}

import io.github.katrix.katlib.helper.Implicits._

class ChannelHandler(implicit plugin: ChitChatPlugin) {

  protected object RenamePermission extends RenamePermission

  private val channels = new mutable.HashMap[String, Channel]
  addChannel(SimpleChannel("global", t"G", t"The global channel", Set()))

  def globalChannel: Channel = channels("global")

  def getChannel(name: String): Option[Channel] = channels.get(name)

  def addChannel(channel: Channel): Either[String, Unit] =
    if (channels.contains(channel.name)) {
      Left("A channel by that name already exists")
    } else {
      channels.put(channel.name, channel)
      Right(Unit)
    }

  def removeChannel(channel: Channel): Unit =
    channels.remove(channel.name)

  def renameChannel(channel: Channel, newName: String)(implicit permission: RenamePermission): Unit =
    if (channel.name != "global") {
      channels.remove(channel.name)
      channels.put(newName, channel.rename(newName))
    }

  def setPlayerChannel(player: Player, channel: Channel): Unit = {
    val old    = getChannelForPlayer(player)
    val result = player.offer(plugin.versionHelper.ChannelKey, channel.name)

    if (result.isSuccessful) {
      val newOld     = old.removeMember(player)
      val newChannel = channel.addMember(player)

      channels.put(newOld.name, newOld)
      channels.put(newChannel.name, channel)
    }
  }

  def getChannelForPlayer(player: Player): Channel = channels.getOrElse(player.getOrElse(plugin.versionHelper.ChannelKey, "global"), globalChannel)
}

sealed trait RenamePermission