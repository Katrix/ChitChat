package net.katsstuff.chitchat.chat.channel

import scala.collection.mutable

import org.spongepowered.api.text.Text
import org.spongepowered.api.text.format.TextColors._

import io.github.katrix.katlib.helper.Implicits._

class ChannelRegistry {

  private val channels = new mutable.HashMap[String, (String, Text, Text, String) => Either[Text, Channel]]

  def registerChannelType(name: String, constructor: (String, Text, Text, String) => Either[Text, Channel]): Unit = {
    channels.put(name, constructor)
  }

  def createChannel(channelType: String, name: String, prefix: Text, description: Text, extra: String): Either[Text, Channel] = {
    channels.get(channelType).toRight(t"${RED}No channel type by that name").flatMap(f => f(name, prefix, description, extra))
  }
}
