package net.katsstuff.chitchat

import java.nio.file.Path

import org.spongepowered.api.command.CommandSource
import org.spongepowered.api.data.key.Key
import org.spongepowered.api.data.value.mutable.Value
import org.spongepowered.api.effect.sound.SoundType
import org.spongepowered.api.event.cause.Cause

import net.katsstuff.chitchat.chat.ChannelHandler
import net.katsstuff.katlib.KatPlugin

trait ChitChatPlugin extends KatPlugin { plugin =>

  def configPath:  Path
  def storagePath: Path

  def config: ChatConfig.RootConfig

  def channelHandler: ChannelHandler

  def versionHelper: VersionHelper

  trait VersionHelper {

    def createChatCause(src: CommandSource, sendToConsole: Boolean = false): Cause

    def levelUpSound: SoundType

    def ChannelKey: Key[Value[String]]
  }
}
