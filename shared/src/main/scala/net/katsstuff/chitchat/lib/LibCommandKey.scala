package net.katsstuff.chitchat.lib

import io.github.katrix.katlib.helper.Implicits._
import net.katsstuff.chitchat.chat.channel.Channel

object LibCommandKey {

  val Message:            CommandKey[String]  = cmdKey[String](t"message")
  val Channel:            CommandKey[Channel] = cmdKey[Channel](t"channel")
  val ChannelName:        CommandKey[String]  = cmdKey[String](t"channel name")
  val ChannelPrefix:      CommandKey[String]  = cmdKey[String](t"prefix")
  val ChannelDescription: CommandKey[String]  = cmdKey[String](t"description")
  val ChannelType:        CommandKey[String]  = cmdKey[String](t"type")
  val ChannelExtra:       CommandKey[String]  = cmdKey[String](t"extra")

}
