package net.katsstuff.chitchat

import org.spongepowered.api.data.key.Key
import org.spongepowered.api.data.value.mutable.Value
import org.spongepowered.api.effect.sound.SoundType
import org.spongepowered.api.service.permission.Subject

import io.github.katrix.katlib.KatPlugin
import net.katsstuff.chitchat.persistant.ChitChatConfig

trait ChitChatPlugin extends KatPlugin {

  def config: ChitChatConfig

  def versionHelper: VersionHelper

  trait VersionHelper {

    def getSubjectOption(subject: Subject, option: String): Option[String]
    def experiencePling: SoundType

    def ChannelKey: Key[Value[String]]
  }
}
