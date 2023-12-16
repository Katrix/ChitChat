package net.katsstuff.chitchat.chat.channel.advanced

import fastparse.all._
import net.katsstuff.chitchat.chat.channel.advanced.AdvancedChannelAST._

//noinspection ForwardReference
object AdvancedChannelParser {

  val comma:        Parser[Unit]   = P(",".~/)
  val whitespace:   Parser[Unit]   = P(CharsWhileIn(" \t\n"))
  val name:         Parser[String] = P(CharsWhileIn(('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9'))).!.opaque("alphanumeric")
  val string:       Parser[String] = P(CharsWhile(c => !" \t\n".contains(c)).!)

  val keyValuePair: Parser[(String, String)] = P(name ~ "=" ~/ string)
  val keyValueApply: Parser[(String, Map[String, String])] = P(
    (name ~ "[" ~ keyValuePair.rep(1, sep = comma) ~ "]").map {
      case (applyName, args) => applyName -> args.toMap
    }
  )
  val varArgsApply: Parser[(String, Seq[String])] = P(name ~ "[" ~ string.rep(1, sep = comma) ~ "]")

  val constant: Parser[Constant] = P((name ~ Index).map { case (n, idx) => Constant(n, idx) })

  val apply: Parser[Apply] = P((keyValueApply ~ Index).map { case (funName, args, idx) => Apply(funName, args, idx) })

  val applyVarArgs: Parser[ApplyVarArgs] = P((varArgsApply ~ Index).map {
    case (funName, args, idx) => ApplyVarArgs(funName, args, idx)
  })

  val function: Parser[Function] = P(apply | applyVarArgs | constant)

  val unionMethod:      Parser[AdvancedChannelAST.Union.type]      = P("union").map(_ => Union)
  val intersectMethod:  Parser[AdvancedChannelAST.Intersect.type]  = P("intersect").map(_ => Intersect)
  val differenceMethod: Parser[AdvancedChannelAST.Difference.type] = P("difference" | "diff").map(_ => Difference)
  val combineMethod:    Parser[CombineMethod]                      = P(unionMethod | intersectMethod | differenceMethod)

  val combinedTerm: Parser[Combine] = P(
    ("(" ~ term ~ whitespace ~ combineMethod ~ whitespace ~ term ~ ")" ~ Index).map {
      case (firstTerm, method, secondTerm, idx) => Combine(method, firstTerm, secondTerm, idx)
    }
  )

  val nextTransform: Parser[Option[Transformation]] = P((whitespace ~ "->" ~/ whitespace ~ transformation).?)

  val transformation: Parser[Transformation] = P(function ~ nextTransform ~ Index).map {
    case (expr, next, idx) => Transformation(expr, next, idx)
  }

  val transformedTerm: Parser[TransformedTerm] = P(("<" ~ term ~ whitespace ~ transformation ~ ">" ~ Index).map {
    case (trm, transform, idx) => TransformedTerm(trm, transform, idx)
  })

  val term: Parser[Term] = P(combinedTerm | transformedTerm | function)

  val ast: Parser[Term] = P(term ~ End)
}
