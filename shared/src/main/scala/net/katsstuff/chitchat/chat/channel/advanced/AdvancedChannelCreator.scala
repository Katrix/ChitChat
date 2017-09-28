package net.katsstuff.chitchat.chat.channel.advanced

import java.util.{Optional, UUID}

import scala.annotation.tailrec
import scala.reflect.ClassTag
import scala.util.Try

import org.spongepowered.api.text.Text
import org.spongepowered.api.text.channel.{MessageChannel, MessageReceiver}
import org.spongepowered.api.text.serializer.TextSerializers

import fastparse.core.{ParseError, Parsed}
import net.katsstuff.chitchat.chat.channel.advanced.ProcessedTerm._
import net.katsstuff.chitchat.chat.channel.advanced.{AdvancedChannelAST => AST}
import scala.collection.JavaConverters._

import org.spongepowered.api.Sponge
import org.spongepowered.api.text.channel.impl.DelegateMessageChannel
import org.spongepowered.api.text.chat.ChatType

import io.github.katrix.katlib.helper.Implicits._
import net.katsstuff.chitchat.chat.{DifferenceMessageChannel, IntersectionMessageChannel}
import net.katsstuff.chitchat.chat.channel.advanced.AdvancedChannelCreator.termToMessageChannel
import shapeless.Typeable

object AdvancedChannelCreator {

  private val channelTermTypeable: Typeable[ChannelTerm] = Typeable[ChannelTerm]

  def sequenceEither[A, B](seq: Seq[Either[A, B]]): Either[Seq[A], Seq[B]] = {
    if (seq.forall(_.isRight)) {
      Right(seq.map(_.right.get))
    } else {
      Left(seq.collect {
        case Left(e) => e
      })
    }
  }

  def getText(args: Map[String, String], index: Int): Either[(String, Int), Text] = {
    args
      .get("jsontext")
      .map(s => Try(TextSerializers.JSON.deserialize(s)).toEither)
      .orElse(args.get("text").map(s => Try(TextSerializers.FORMATTING_CODE.deserialize(s)).toEither))
      .map(_.left.map(e => (e.getMessage, index)))
      .toRight(("No text specified", index))
      .flatMap(identity)
  }

  val constantChannels: Map[String, ConstantChannel] =
    Map("inChannel" -> InChannel, "console" -> Console, "all" -> All, "players" -> AllPlayers, "none" -> NoReceivers)

  val applyChannels: Map[String, (Map[String, String], Int) => Either[(String, Int), ApplyChannel]] = Map(
    "permission" -> ((args, idx) => args.get("perm").map(Permission).toRight(("perm arg not specified", idx)))
  )

  val varArgsChannels: Map[String, (Seq[String], Int) => Either[(String, Int), ApplyVarArgsChannel]] = Map(
    "players" -> { (args, idx) =>
      sequenceEither(args.map(s => Try(UUID.fromString(s)).toEither.left.map(_.getMessage))).left
        .map(errors => (errors.mkString(", "), idx))
        .map(Players)
    }
  )

  val constantTransformations: Map[String, ConstantTransformation] = Map.empty
  val applyTransformations: Map[String, (Map[String, String], Int) => Either[(String, Int), ApplyTransformation]] = Map(
    "prefix" -> ((args, idx) => getText(args, idx).map(Prefix)),
    "suffix" -> ((args, idx) => getText(args, idx).map(Suffix))
  )
  val varArgsTransformations: Map[String, (Seq[String], Int) => Either[(String, Int), ApplyVarArgsTransformation]] =
    Map.empty

  def createChannel(str: String): Either[(String, Int), Set[MessageReceiver] => MessageChannel] = {
    AdvancedChannelParser.ast.parse(str) match {
      case failure @ Parsed.Failure(_, idx, _) => Left((ParseError(failure).getMessage, idx))
      case Parsed.Success(ast, _) =>
        processTerm(ast).map(optimizeTerm).map(optimized => termToMessageChannel(_, optimized))
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

            Optional.ofNullable(res.orNull)
          }
        }
    }
  }

  private def processTerm(term: AST.Term): Either[(String, Int), ProcessedTerm] = {
    term match {
      case expr: AST.Expression => exprToChannel(expr)
      case AST.Combine(method, first, second, _) =>
        for {
          firstTerm         <- processTerm(first)
          firstChannelTerm  <- channelTermTypeable.cast(firstTerm).toRight(("Illegal term type", first.idx))
          secondTerm        <- processTerm(second)
          secondChannelTerm <- channelTermTypeable.cast(secondTerm).toRight(("Illegal term type", second.idx))
        } yield {
          method match {
            case AST.Union      => Union(Seq(firstChannelTerm, secondChannelTerm))
            case AST.Intersect  => Intersect(Seq(firstChannelTerm, secondChannelTerm))
            case AST.Difference => Difference(Seq(firstChannelTerm, secondChannelTerm))
          }
        }
      case AST.TransformedTerm(innerTerm, transformation, _) =>
        for {
          processedTerm        <- processTerm(innerTerm)
          processedChannelTerm <- channelTermTypeable.cast(processedTerm).toRight(("Illegal term type", innerTerm.idx))
          gatheredTransformations <- gatherTransformations(transformation).left
            .map(errors => (errors.mkString(", "), innerTerm.idx))
        } yield TransformedTerm(processedChannelTerm, gatheredTransformations)
    }
  }

  private def optimizeTerm[A <: ProcessedTerm](term: A): A = {
    term match {
      case Union(terms) =>
        if (terms.exists(_.isInstanceOf[Union])) combineCombines(terms, Union).asInstanceOf[A]
        else Union(terms.map(optimizeTerm)).asInstanceOf[A]
      case Intersect(terms) =>
        if (terms.exists(_.isInstanceOf[Intersect])) combineCombines(terms, Intersect).asInstanceOf[A]
        else Intersect(terms.map(optimizeTerm)).asInstanceOf[A]
      case Difference(terms) =>
        if (terms.exists(_.isInstanceOf[Difference])) combineCombines(terms, Difference).asInstanceOf[A]
        else Difference(terms.map(optimizeTerm)).asInstanceOf[A]
      case TransformedTerm(innerTerm, transformations) =>
        @tailrec
        def unwrapTransforms(
            term: ChannelTerm,
            acc: Seq[TransformationTerm]
        ): (ChannelTerm, Seq[TransformationTerm]) = {
          term match {
            case TransformedTerm(innerInnerTerm, innerTransformations) =>
              unwrapTransforms(innerInnerTerm, acc ++ innerTransformations)
            case _ => (term, acc)
          }
        }

        val (newInnerTerm, newTransformations) = unwrapTransforms(innerTerm, transformations)
        TransformedTerm(newInnerTerm, newTransformations).asInstanceOf[A]
      case _ => term
    }
  }

  private def combineCombines[A <: CombineTerm](terms: Seq[ChannelTerm], create: Seq[ChannelTerm] => A)(
      implicit classTag: ClassTag[A]
  ): A = {
    val clazz                       = classTag.runtimeClass
    val (intersects: Seq[A], other) = terms.partition(clazz.isInstance)
    val innerOther                  = intersects.flatMap(_.inner)
    create((other ++ innerOther).map(optimizeTerm))
  }

  private def gatherTransformations(
      transformation: AST.Transformation
  ): Either[Seq[(String, Int)], Seq[TransformationTerm]] = {
    @tailrec
    def inner(
        acc: List[TransformationTerm],
        errors: List[(String, Int)],
        transformation: AST.Transformation
    ): (List[TransformationTerm], List[(String, Int)]) = {
      val term = exprToTransformation(transformation.expression)

      transformation.next match {
        case Some(next) =>
          term match {
            case Left(e)  => inner(acc, e :: errors, next)
            case Right(t) => inner(t :: acc, errors, next)
          }
        case None =>
          term match {
            case Left(e)  => (acc, e :: errors)
            case Right(t) => (t :: acc, errors)
          }
      }
    }

    val (res, errors) = inner(Nil, Nil, transformation)
    if (errors.nonEmpty) Left(errors)
    else Right(res)
  }

  private def exprToTerm[A](
      constants: Map[String, A with ConstantTerm],
      applys: Map[String, (Map[String, String], Int) => Either[(String, Int), A with ApplyTerm]],
      varArgs: Map[String, (Seq[String], Int) => Either[(String, Int), A with VarArgsTerm]],
      typeName: String
  )(expr: AdvancedChannelAST.Expression): Either[(String, Int), A] = {
    expr match {
      case AST.Constant(name, idx) => constants.get(name).toRight((s"$name not a valid constant $typeName", idx))
      case AST.Apply(name, args, idx) =>
        applys.get(name).toRight((s"$name not a valid apply $typeName", idx)).flatMap(f => f(args))
      case AST.ApplyVarArgs(name, args, idx) =>
        varArgs.get(name).toRight((s"$name not a valid varargs $typeName", idx)).flatMap(f => f(args))
    }
  }

  private def exprToChannel(expr: AdvancedChannelAST.Expression): Either[(String, Int), ChannelTerm] =
    exprToTerm(constantChannels, applyChannels, varArgsChannels, "channel")(expr)

  private def exprToTransformation(expr: AdvancedChannelAST.Expression): Either[(String, Int), TransformationTerm] =
    exprToTerm(constantTransformations, applyTransformations, varArgsTransformations, "transformation")(expr)
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

  case class Permission(perm: String) extends ApplyChannel {
    override def messageChannel(members: Set[MessageReceiver]): MessageChannel = MessageChannel.permission(perm)
  }

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
      MessageChannel.combined(inner.map(termToMessageChannel(members, _)).asJava)
  }
  case class Intersect(inner: Seq[ChannelTerm]) extends CombineTerm {
    override def messageChannel(members: Set[MessageReceiver]): MessageChannel =
      new IntersectionMessageChannel(inner.map(termToMessageChannel(members, _)))
  }
  case class Difference(inner: Seq[ChannelTerm]) extends CombineTerm {
    override def messageChannel(members: Set[MessageReceiver]): MessageChannel =
      new DifferenceMessageChannel(
        termToMessageChannel(members, inner.head),
        inner.tail.map(termToMessageChannel(members, _))
      )
  }

  case class TransformedTerm(term: ChannelTerm, transformations: Seq[TransformationTerm]) extends ProcessedTerm
}
