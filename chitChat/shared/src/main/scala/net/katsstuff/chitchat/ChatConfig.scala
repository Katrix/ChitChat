package net.katsstuff.chitchat

import java.nio.file.{Files, Path}

import org.spongepowered.api.Sponge
import org.spongepowered.api.event.message.MessageEvent
import org.spongepowered.api.text.format.TextColors._
import org.spongepowered.api.text.{Text, TextTemplate}

import cats.effect.IO
import cats.syntax.either._
import metaconfig.annotation.{Description, ExtraName}
import metaconfig.generic.Surface
import metaconfig.typesafeconfig.TypesafeConfig2Class
import metaconfig.{generic, Conf, ConfDecoder, ConfError, Configured}
import net.katsstuff.chitchat.lib.LibPlugin
import net.katsstuff.katlib.helper.Implicits._

object ChatConfig {
  case class RootConfig(
      @Description("Should players be plinged if they are mentioned") mentionPling: Boolean,
      formatting: Formatting
  )
  case class Formatting(chat: ChatFormatter, join: ChatFormatter, disconnect: ChatFormatter, commands: Commands)
  case class Commands(
      @Description("The template for announcements") announceTemplate: TextTemplate,
      @Description("The template for the me cmd") meTemplate: TextTemplate,
      @Description("The template used for PMs") pmTemplate: TextTemplate,
      @Description("The template used when shouting to a channel") shoutTemplate: TextTemplate
  )

  case class ChatFormatter(
      @Description("The formatter options to apply to the header") header: FormatterOptions = FormatterOptions(),
      @Description("The formatter options to apply to the body") body: FormatterOptions = FormatterOptions(),
      @Description("The formatter options to apply to the footer") footer: FormatterOptions = FormatterOptions()
  )

  case class FormatterOptions(
      @Description(
        "The template to override for this section. Overrides if the section has a parameter with a specific name"
      )
      overrides: Map[String, TextTemplate] = Map.empty,
      @ExtraName("new")
      @Description("A list of new templates to add to this section")
      newTemplates: Seq[TextTemplate] = Seq.empty,
      @Description("A set of template parameters to apply, and their option counterparts")
      parameters: Map[String, ChatOption] = Map.empty
  )

  case class ChatOption(
      @Description("The name of this option as permission option") name: String,
      @Description("If this option should be parsed as text") isText: Boolean,
      @Description("The default value if nothing is found") default: Text
  )

  final val TemplatePrefix = s"${LibPlugin.Id}.prefix"
  final val TemplateSuffix = s"${LibPlugin.Id}.suffix"

  final val TemplateHeader = MessageEvent.PARAM_MESSAGE_HEADER

  final val PlayerName = s"${LibPlugin.Id}.playerName"
  final val Message    = s"${LibPlugin.Id}.message"

  final val Sender   = s"${LibPlugin.Id}.sender"
  final val Receiver = s"${LibPlugin.Id}.receiver"

  implicit val surface: Surface[RootConfig] = generic.deriveSurface[RootConfig]
  implicit val decoder: ConfDecoder[RootConfig] =
    generic.deriveDecoder(
      RootConfig(
        mentionPling = true,
        formatting = Formatting(
          chat = ChatFormatter(
            header = FormatterOptions(
              overrides = Map("header"  -> tt"$TemplatePrefix$TemplateHeader: "),
              parameters = Map("prefix" -> ChatOption(name = TemplatePrefix, isText = true, default = Text.EMPTY))
            ),
            footer = FormatterOptions(
              overrides = Map("footer"  -> tt"$TemplateSuffix"),
              parameters = Map("suffix" -> ChatOption(name = TemplateSuffix, isText = true, default = Text.EMPTY))
            )
          ),
          join = ChatFormatter(
            body = FormatterOptions(
              overrides = Map(
                "body" -> TextTemplate
                  .of(t"${GREEN}The player ", TextTemplate.arg("body").color(AQUA), t"$GREEN has joined the server")
              ),
            )
          ),
          disconnect = ChatFormatter(
            body = FormatterOptions(
              newTemplates = Seq(
                TextTemplate.of(t"${RED}The player ", TextTemplate.arg("body").color(AQUA), t"$RED has left the server")
              )
            )
          ),
          commands = Commands(
            announceTemplate = tt"$BLUE[Announcement] $TemplateHeader: ",
            meTemplate = tt"$GOLD* $TemplateHeader ",
            pmTemplate = TextTemplate.of(
              t"${DARK_PURPLE}From ",
              TextTemplate.arg(Sender).color(DARK_AQUA),
              t" ${DARK_PURPLE}to ",
              TextTemplate.arg(Receiver).color(DARK_AQUA),
              t"$DARK_PURPLE: "
            ),
            shoutTemplate = tt"$YELLOW$TemplateHeader shouts: "
          )
        )
      )
    )

  def createAndReadConfig(file: Path)(implicit plugin: ChitChatPlugin): IO[Configured[Conf]] = {
    for {
      optAsset <- IO(Sponge.getAssetManager.getAsset(plugin, "reference.conf").toOption)
      conf <- optAsset match {
        case Some(asset) =>
          for {
            _    <- IO(asset.copyToFile(file))
            conf <- IO(TypesafeConfig2Class.gimmeConfFromFile(file.toFile))
          } yield conf
        case None => IO.pure(Configured.notOk(ConfError.fileDoesNotExist(file)))
      }
    } yield conf
  }

  def load(file: Path)(implicit plugin: ChitChatPlugin): IO[RootConfig] =
    for {
      _            <- IO(Files.createDirectories(file.getParent))
      fileNotExist <- IO(Files.notExists(file))
      configured <- if (fileNotExist) createAndReadConfig(file)
      else IO(TypesafeConfig2Class.gimmeConfFromFile(file.toFile))
      configuredConfig = configured.andThen(_.as[RootConfig])
      config <- IO.fromEither(configuredConfig.toEither.leftMap(e => e.cause.getOrElse(new Exception(e.msg))))
    } yield config
}
