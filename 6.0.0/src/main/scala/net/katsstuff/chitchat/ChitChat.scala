package net.katsstuff.chitchat

import java.nio.file.Path

import org.slf4j.Logger
import org.spongepowered.api.Sponge
import org.spongepowered.api.config.ConfigDir
import org.spongepowered.api.data.DataQuery
import org.spongepowered.api.data.key.{Key, KeyFactory}
import org.spongepowered.api.data.value.mutable.Value
import org.spongepowered.api.effect.sound.{SoundType, SoundTypes}
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.game.state.{GameConstructionEvent, GameInitializationEvent}
import org.spongepowered.api.plugin.{Dependency, Plugin, PluginContainer}
import org.spongepowered.api.service.permission.Subject
import org.spongepowered.api.text.format.TextColors._

import com.google.inject.Inject

import io.github.katrix.katlib.command.CommandBase
import io.github.katrix.katlib.helper.Implicits._
import io.github.katrix.katlib.lib.LibKatLibPlugin
import io.github.katrix.katlib.serializer.TypeSerializerImpl._
import io.github.katrix.katlib.{ImplKatPlugin, KatLib}
import net.katsstuff.chitchat.chat.{ChannelHandler, ChatListener}
import net.katsstuff.chitchat.chat.channel.{Channel, SimpleChannel}
import net.katsstuff.chitchat.chat.data.{ChannelData, ChannelDataBuilder, ImmutableChannelData}
import net.katsstuff.chitchat.command.{CmdAdminChannel, CmdAnnounce, CmdInChannel, CmdJoinChannel, CmdListChannel, CmdMe, CmdPm, CmdReply, CmdShout}
import net.katsstuff.chitchat.lib.LibPlugin
import net.katsstuff.chitchat.persistant.{ChitChatConfig, ChitChatConfigLoader, StorageLoader}
import ninja.leaping.configurate.objectmapping.serialize.{TypeSerializer, TypeSerializers}

object ChitChat {

  final val Version         = s"${KatLib.CompiledAgainst}-2.0.0"
  final val ConstantVersion = "6.0.0-2.0.0"
  assert(Version == ConstantVersion)

  private var _plugin: ChitChat = _
  implicit def plugin: ChitChat = _plugin
}

@Plugin(
  id = LibPlugin.Id,
  name = LibPlugin.Name,
  description = LibPlugin.Description,
  authors = Array("Katrix"),
  version = ChitChat.ConstantVersion,
  dependencies = Array(new Dependency(id = LibKatLibPlugin.Id, version = KatLib.ConstantVersion))
)
class ChitChat @Inject()(logger: Logger, @ConfigDir(sharedRoot = false) cfgDir: Path, spongeContainer: PluginContainer)
    extends ImplKatPlugin(logger, cfgDir, spongeContainer)
    with ChitChatPlugin {

  implicit private val plugin: ChitChatPlugin = this

  private lazy val configLoader = new ChitChatConfigLoader(configDir)
  lazy val storageLoader        = new StorageLoader(configDir)

  private var _config: ChitChatConfig = _
  override def config: ChitChatConfig = _config

  @Listener
  def gameConstruct(event: GameConstructionEvent): Unit = ChitChat._plugin = this

  @Listener
  def init(event: GameInitializationEvent): Unit = {
    _config = configLoader.loadData
    Channel.registerChannelType[SimpleChannel]("simple")
    TypeSerializers.getDefaultSerializers.registerType(typeToken[Channel], implicitly[TypeSerializer[Channel]])

    implicit val channelHandler = new ChannelHandler(storageLoader)
    channelHandler.registry.registerChannelType("simple", {
      case (name, prefix, description, none) if none.trim.isEmpty => Right(new SimpleChannel(name, prefix, description, Set()))
      case _ => Left(t"${RED}This channel does not take extra arguments")
    })
    Sponge.getDataManager.register(classOf[ChannelData], classOf[ImmutableChannelData], new ChannelDataBuilder)

    Sponge.getEventManager.registerListeners(this, new ChatListener)

    registerCommand(pluginCmd)
    registerCommand(new CmdAdminChannel)
    registerCommand(new CmdAnnounce)
    registerCommand(new CmdInChannel)
    registerCommand(new CmdJoinChannel)
    registerCommand(new CmdListChannel)
    registerCommand(new CmdMe)
    registerCommand(new CmdShout)

    val pmCmd = new CmdPm
    registerCommand(pmCmd)
    registerCommand(new CmdReply(pmCmd))
  }

  private def registerCommand(cmd: CommandBase): Unit =
    Sponge.getCommandManager.register(this, cmd.commandSpec, cmd.aliases: _*)

  override val versionHelper: VersionHelper = new VersionHelper {
    override val levelUpSound: SoundType = SoundTypes.ENTITY_PLAYER_LEVELUP
    override val ChannelKey: Key[Value[String]] = KeyFactory.makeSingleKey(
      typeToken[String],
      typeToken[Value[String]],
      DataQuery.of(LibPlugin.Id, "channel", "currentChannel"),
      s"${LibPlugin.Id}:currentChannel",
      "ChitChat Current Channel"
    )
    override def getSubjectOption(subject: Subject, option: String): Option[String] = subject.getOption(option).toOption
  }
}
