package net.katsstuff.chitchat

import org.spongepowered.api.command.CommandSource
import org.spongepowered.api.data.key.Key
import org.spongepowered.api.data.value.mutable.Value
import org.spongepowered.api.effect.sound.SoundType
import org.spongepowered.api.event.cause.Cause
import org.spongepowered.api.service.permission.Subject

import io.github.katrix.katlib.KatPlugin
import net.katsstuff.chitchat.persistant.ChitChatConfig

trait ChitChatPlugin extends KatPlugin { plugin =>

  def config: ChitChatConfig

  def versionHelper: VersionHelper

  trait VersionHelper {

    def createChatCause(src: CommandSource, sendToConsole: Boolean = false): Cause

    def getSubjectOption(subject: Subject, option: String): Option[String]
    def levelUpSound:                                       SoundType

    def ChannelKey: Key[Value[String]]
  }
}
