package net.katsstuff.chitchat

import java.nio.file.Path
import java.util.UUID

import org.slf4j.Logger
import org.spongepowered.api.Sponge
import org.spongepowered.api.command.CommandSource
import org.spongepowered.api.config.ConfigDir
import org.spongepowered.api.data.DataQuery
import org.spongepowered.api.data.key.{Key, KeyFactory}
import org.spongepowered.api.data.value.mutable.Value
import org.spongepowered.api.effect.sound.{SoundType, SoundTypes}
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.cause.{Cause, NamedCause}
import org.spongepowered.api.event.game.GameReloadEvent
import org.spongepowered.api.event.game.state.{GameConstructionEvent, GameInitializationEvent}
import org.spongepowered.api.plugin.{Dependency, Plugin, PluginContainer}
import org.spongepowered.api.service.permission.Subject
import org.spongepowered.api.text.format.TextColors._

import com.google.inject.Inject

import cats.Traverse
import cats.data.{NonEmptyList, Validated}
import net.katsstuff.katlib.helper.Implicits._
import net.katsstuff.katlib.ImplKatPlugin
import net.katsstuff.chitchat.chat.{ChannelHandler, ChatListener, SendToConsole}
import net.katsstuff.chitchat.chat.channel.{Channel, SimpleChannel}
import net.katsstuff.chitchat.chat.data.{ChannelData, ChannelDataBuilder, ImmutableChannelData}
import net.katsstuff.chitchat.command.{CmdAnnounce, CmdInChannel, CmdJoinChannel, CmdListChannel, CmdMe, CmdModChannel, CmdPm, CmdReply, CmdShout}
import net.katsstuff.chitchat.lib.LibPlugin
import ninja.leaping.configurate.objectmapping.serialize.{TypeSerializer, TypeSerializers}

object ChitChat {
  final val ConstantVersion = "5.0.0-2.0.1"
}

@Plugin(
  id = LibPlugin.Id,
  name = LibPlugin.Name,
  description = LibPlugin.Description,
  authors = Array("Katrix"),
  version = ChitChat.ConstantVersion
)
class ChitChat @Inject()(logger: Logger, @ConfigDir(sharedRoot = false) cfgDir: Path, spongeContainer: PluginContainer)
    extends ImplKatPlugin(logger, cfgDir, spongeContainer)
    with ChitChatPlugin {

  implicit private val plugin: ChitChatPlugin = this

  private lazy val configLoader            = new ChitChatConfigLoader(configDir)
  private lazy val storageLoader           = new StorageLoader(configDir)
  private lazy implicit val channelHandler = new ChannelHandler(storageLoader)

  private var _config: ChitChatConfig = _
  override def config: ChitChatConfig = _config

  import cats.data._
  import cats.implicits._

  val nelType: List[ValidatedNel[Throwable, UUID]] = ???
  private val sequence = Traverse[List].sequence(nelType)

  @Listener
  def init(event: GameInitializationEvent): Unit = {
    _config = configLoader.loadData
    Channel.registerChannelType[SimpleChannel]("simple")
    TypeSerializers.getDefaultSerializers.registerType(typeToken[Channel], implicitly[TypeSerializer[Channel]])

    channelHandler.registry.registerChannelType("simple", {
      case (name, prefix, description, none) if none.trim.isEmpty =>
        Right(new SimpleChannel(name, prefix, description, Set()))
      case _ => Left(t"${RED}This channel does not take extra arguments")
    })
    Sponge.getDataManager.register(classOf[ChannelData], classOf[ImmutableChannelData], new ChannelDataBuilder)

    Sponge.getEventManager.registerListeners(this, new ChatListener)

    registerCommand(pluginCmd)
    registerCommand(new CmdModChannel)
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

  @Listener
  def reload(event: GameReloadEvent): Unit = {
    configLoader.reload()
    _config = configLoader.loadData
    storageLoader.reload()
    channelHandler.reloadChannels()
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
    override def createChatCause(src: CommandSource, sendToConsole: Boolean): Cause = {
      val causeBuilder = Cause
        .builder()
        .suggestNamed("Plugin", plugin)
        .named(NamedCause.owner(src))

      if(sendToConsole) causeBuilder.named("SendToConsole", SendToConsole).build()
      else causeBuilder.build()
    }
  }
}
