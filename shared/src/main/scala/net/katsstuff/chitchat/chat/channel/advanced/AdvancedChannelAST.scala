package net.katsstuff.chitchat.chat.channel.advanced

object AdvancedChannelAST {

  sealed trait ListenerTerm

  sealed trait NamedChannel extends ListenerTerm {
    def name: String
  }

  final case class ChannelApply(name: String, arguments: Map[String, String]) extends NamedChannel
  final case class ChannelConstant(name: String)                              extends NamedChannel

  final case class TransformingTerm(transformation: Transformation)                          extends ListenerTerm
  final case class Combine(method: CombineMethod, first: ListenerTerm, second: ListenerTerm) extends ListenerTerm

  sealed trait CombineMethod
  case object Union     extends CombineMethod
  case object Intersect extends CombineMethod

  sealed trait Transformation {
    def name: String
    def next: Option[Transformation]
  }

  final case class TransformationConstant(name: String, next: Option[Transformation])                              extends Transformation
  final case class TransformationApply(name: String, arguments: Map[String, String], next: Option[Transformation]) extends Transformation

}
