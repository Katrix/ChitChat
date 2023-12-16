package net.katsstuff.chitchat.chat.channel.advanced

import java.util.{Optional, UUID}

import scala.collection.JavaConverters._

import org.spongepowered.api.Sponge
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.channel.impl.DelegateMessageChannel
import org.spongepowered.api.text.channel.{MessageChannel, MessageReceiver}
import org.spongepowered.api.text.chat.ChatType
import org.spongepowered.api.text.serializer.{TextSerializer, TextSerializers}

import cats._
import cats.data._
import cats.implicits._
import fastparse.core.{ParseError, Parsed}
import net.katsstuff.chitchat.chat.channel.advanced.ProcessedTerm._
import net.katsstuff.chitchat.chat.channel.advanced.{AdvancedChannelAST => AST}
import net.katsstuff.chitchat.chat.{DifferenceMessageChannel, IntersectionMessageChannel}
import net.katsstuff.katlib.helper.Implicits._
import shapeless.Typeable

object AdvancedChannelCreator {

  private val channelTermTypeable: Typeable[ChannelTerm] = Typeable[ChannelTerm]

  def getText(args: Map[String, String], index: Int): ValidatedNel[(String, Int), Text] = {
    def getTextArg(name: String, serializer: TextSerializer) =
      args
        .get(name)
        .map(s => Validated.catchNonFatal(serializer.deserialize(s)).leftMap(e => (e.getMessage, index)))

    getTextArg("jsontext", TextSerializers.JSON)
      .orElse(getTextArg("text", TextSerializers.FORMATTING_CODE))
      .getOrElse(("No text specified", index).invalid)
      .toValidatedNel
  }

  type ArgsParser[A, B] = (A, Int) => ValidatedNel[(String, Int), B]
  type ApplyParser[A]   = ArgsParser[Map[String, String], A]
  type VarargsParser[A] = ArgsParser[Seq[String], A]

  type ApplyChannelParser   = ApplyParser[ApplyChannel]
  type VarargsChannelParser = VarargsParser[ApplyVarArgsChannel]

  val constantChannels: Map[String, ConstantChannel] =
    Map("inChannel" -> InChannel, "console" -> Console, "all" -> All, "players" -> AllPlayers, "none" -> NoReceivers)

  val applyChannels: Map[String, ApplyChannelParser] = Map.empty

  val varArgsChannels: Map[String, VarargsChannelParser] = Map("players" -> { (args, _) =>
    val validated = args.map(s => Validated.catchNonFatal(UUID.fromString(s)).toValidatedNel)
    Traverse[List].sequence(validated.toList).bimap(nel => nel.map(_.getMessage), Players.apply)
  }, "permission" -> { (args, _) =>
    Permission(args).validNel
  })

  type ApplyTransformationParser   = ApplyParser[ApplyTransformation]
  type VarargsTransformationParser = VarargsParser[ApplyVarArgsTransformation]

  val constantTransformations: Map[String, ConstantTransformation] = Map.empty
  val applyTransformations: Map[String, ApplyTransformationParser] = Map(
    "prefix" -> ((args, idx) => getText(args, idx).map(Prefix)),
    "suffix" -> ((args, idx) => getText(args, idx).map(Suffix))
  )
  val varArgsTransformations: Map[String, VarargsTransformationParser] =
    Map.empty

  def createChannel(str: String): ValidatedNel[(String, Int), Set[MessageReceiver] => MessageChannel] = {
    AdvancedChannelParser.ast.parse(str) match {
      case failure @ Parsed.Failure(_, idx, _) => (ParseError(failure).getMessage, idx).invalidNel
      case Parsed.Success(ast, _) =>
        processTerm(ast).map(processed => termToMessageChannel(_, processed))
    }
  }

  def termToMessageChannel(members: Set[MessageReceiver], term: ProcessedTerm): MessageChannel = {
    term match {
      case channelTerm: ChannelTerm => channelTerm.messageChannel(members)
      case TransformedTerm(channelTerm, transformations) =>
        val channel = channelTerm.messageChannel(members)
        new DelegateMessageChannel(channel) {
          override def transformMessage(
              sender: scala.Any,
              recipient: MessageReceiver,
              original: Text,
              tpe: ChatType
          ): Optional[Text] = {
            val res = transformations.foldLeft(Option(original)) {
              case (Some(msg), transform) => transform.transformMessage(Option(sender), recipient, msg, tpe)
              case (None, _)              => None
            }

            res.toOptional
          }
        }
    }
  }

  private def processTerm(term: AST.Term): ValidatedNel[(String, Int), ProcessedTerm] = {
    def illegalType(idx: Int) = ("Illegal term type", idx)
    def processAndCast(term: AST.Term) =
      processTerm(term).andThen(channelTermTypeable.cast(_).toValidNel(illegalType(term.idx)))

    term match {
      case expr: AST.Function => functionToChannel(expr)
      case AST.Combine(method, first, second, _) =>
        processAndCast(first).product(processAndCast(second)).map {
          case (firstChannel, secondChannel) =>
            method match {
              case AST.Union      => Union(Seq(firstChannel, secondChannel))
              case AST.Intersect  => Intersect(Seq(firstChannel, secondChannel))
              case AST.Difference => Difference(Seq(firstChannel, secondChannel))
            }
        }

      case AST.TransformedTerm(innerTerm, transformation, _) =>
        processAndCast(innerTerm).product(gatherTransformations(transformation)).map {
          case (channel, transformations) =>
            TransformedTerm(channel, transformations)
        }
    }
  }

  private def functionToChannel(expr: AdvancedChannelAST.Function): ValidatedNel[(String, Int), ChannelTerm] =
    functionToTerm(constantChannels, applyChannels, varArgsChannels, "channel")(expr)

  private def functionToTransformation(
      expr: AdvancedChannelAST.Function
  ): ValidatedNel[(String, Int), TransformationTerm] =
    functionToTerm(constantTransformations, applyTransformations, varArgsTransformations, "transformation")(expr)

  private def functionToTerm[A](
      constants: Map[String, A with ConstantTerm],
      applys: Map[String, ApplyParser[A with ApplyTerm]],
      varArgs: Map[String, VarargsParser[A with VarArgsTerm]],
      typeName: String
  )(expr: AdvancedChannelAST.Function): ValidatedNel[(String, Int), A] = {
    def error(funType: String) = (s"${expr.name} is not a valid $funType $typeName", expr.idx)

    expr match {
      case AST.Constant(name, _)           => constants.get(name).toValidNel(error("constant"))
      case AST.Apply(name, args, _)        => applys.get(name).toValidNel(error("apply")).andThen(f => f(args))
      case AST.ApplyVarArgs(name, args, _) => varArgs.get(name).toValidNel(error("varargs")).andThen(f => f(args))
    }
  }

  private def gatherTransformations(
      transformation: AST.Transformation
  ): ValidatedNel[(String, Int), List[TransformationTerm]] = {
    def transformationToList(acc: List[AST.Function], transformation: AST.Transformation): List[AST.Function] = {
      transformation.next match {
        case Some(next) => transformationToList(transformation.function :: acc, next)
        case None => transformation.function :: acc
      }
    }

    val transformations = transformationToList(Nil, transformation).map(functionToTransformation)
    Traverse[List].sequence(transformations)
  }
}

trait ProcessedTerm
object ProcessedTerm {
  trait ConstantTerm extends ProcessedTerm
  trait ApplyTerm    extends ProcessedTerm
  trait VarArgsTerm  extends ProcessedTerm
  trait ChannelTerm extends ProcessedTerm {
    def messageChannel(members: Set[MessageReceiver]): MessageChannel
  }
  trait CombineTerm extends ChannelTerm {
    def inner: Seq[ProcessedTerm]
  }

  trait ConstantChannel     extends ChannelTerm with ConstantTerm
  trait ApplyChannel        extends ChannelTerm with ApplyTerm
  trait ApplyVarArgsChannel extends ChannelTerm with VarArgsTerm

  trait TransformationTerm extends ProcessedTerm {
    def transformMessage(sender: Option[Any], recipient: MessageReceiver, original: Text, tpe: ChatType): Option[Text]
  }
  trait ConstantTransformation     extends TransformationTerm with ConstantTerm
  trait ApplyTransformation        extends TransformationTerm with ApplyTerm
  trait ApplyVarArgsTransformation extends TransformationTerm with VarArgsTerm

  case object InChannel extends ConstantChannel {
    override def messageChannel(members: Set[MessageReceiver]): MessageChannel = MessageChannel.fixed(members.asJava)
  }
  case object Console extends ConstantChannel {
    override def messageChannel(members: Set[MessageReceiver]): MessageChannel = MessageChannel.TO_CONSOLE
  }
  case object All extends ConstantChannel {
    override def messageChannel(members: Set[MessageReceiver]): MessageChannel = MessageChannel.TO_ALL
  }
  case object AllPlayers extends ConstantChannel {
    override def messageChannel(members: Set[MessageReceiver]): MessageChannel = MessageChannel.TO_PLAYERS
  }
  case object NoReceivers extends ConstantChannel {
    override def messageChannel(members: Set[MessageReceiver]): MessageChannel = MessageChannel.TO_NONE
  }

  case class Permission(perms: Seq[String]) extends ApplyVarArgsChannel {
    override def messageChannel(members: Set[MessageReceiver]): MessageChannel =
      new IntersectionMessageChannel(perms.map(MessageChannel.permission))
  }

  //TODO: Find ways to make it work for offline players
  case class Players(uuids: Seq[UUID]) extends ApplyVarArgsChannel {
    override def messageChannel(members: Set[MessageReceiver]): MessageChannel = {
      MessageChannel.fixed(
        uuids
          .map(Sponge.getServer.getPlayer(_).toOption)
          .collect {
            case Some(p) => p
          }
          .asJava
      )
    }
  }

  case class Prefix(prefix: Text) extends ApplyTransformation {
    override def transformMessage(
        sender: Option[Any],
        recipient: MessageReceiver,
        original: Text,
        tpe: ChatType
    ): Option[Text] = Some(prefix.concat(original))
  }
  case class Suffix(suffix: Text) extends ApplyTransformation {
    override def transformMessage(
        sender: Option[Any],
        recipient: MessageReceiver,
        original: Text,
        tpe: ChatType
    ): Option[Text] = Some(original.concat(suffix))
  }

  case class Union(inner: Seq[ChannelTerm]) extends CombineTerm {
    override def messageChannel(members: Set[MessageReceiver]): MessageChannel =
      MessageChannel.combined(inner.map(AdvancedChannelCreator.termToMessageChannel(members, _)).asJava)
  }
  case class Intersect(inner: Seq[ChannelTerm]) extends CombineTerm {
    override def messageChannel(members: Set[MessageReceiver]): MessageChannel =
      new IntersectionMessageChannel(inner.map(AdvancedChannelCreator.termToMessageChannel(members, _)))
  }
  case class Difference(inner: Seq[ChannelTerm]) extends CombineTerm {
    override def messageChannel(members: Set[MessageReceiver]): MessageChannel = {
      val channels = inner.map(AdvancedChannelCreator.termToMessageChannel(members, _))
      new DifferenceMessageChannel(channels.head, channels.tail)
    }
  }

  case class TransformedTerm(term: ChannelTerm, transformations: Seq[TransformationTerm]) extends ProcessedTerm
}
