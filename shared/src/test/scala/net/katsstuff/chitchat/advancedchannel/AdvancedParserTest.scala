package net.katsstuff.chitchat.advancedchannel

import org.scalactic.Equality
import org.scalatest.enablers.Containing
import org.scalatest.matchers.{BeMatcher, MatchResult}
import org.scalatest.{FunSuite, Matchers}

import fastparse.all.Parsed

import net.katsstuff.chitchat.chat.channel.advanced.AdvancedChannelAST._
import net.katsstuff.chitchat.chat.channel.advanced.AdvancedChannelParser._

class AdvancedParserTest extends FunSuite with Matchers {

  val successful: BeMatcher[Parsed[_]] =
    BeMatcher[Parsed[_]](
      left =>
        MatchResult(left match {
          case Parsed.Success(_, _)    => true
          case Parsed.Failure(_, _, _) => false
        }, "Parser was not successful", "Parser was successful")
    )

  implicit def parseResultContainer[A: Equality]: Containing[Parsed[A]] =
    new Containing[Parsed[A]] {
      override def contains(container: Parsed[A], element: Any): Boolean =
        container match {
          case Parsed.Failure(_, _, _) => false
          case Parsed.Success(parsedVal, _) =>
            implicitly[Equality[A]].areEqual(parsedVal, element)
        }
      override def containsOneOf(container: Parsed[A], elements: Seq[Any]): Boolean = {
        val equal = implicitly[Equality[A]]
        container match {
          case Parsed.Failure(_, _, _) => false
          case Parsed.Success(parsedVal, _) =>
            elements.exists(equal.areEqual(parsedVal, _))
        }
      }
      override def containsNoneOf(container: Parsed[A], elements: Seq[Any]): Boolean = {
        val equal = implicitly[Equality[A]]
        container match {
          case Parsed.Failure(_, _, _) => false
          case Parsed.Success(parsedVal, _) =>
            elements.forall(!equal.areEqual(parsedVal, _))
        }
      }
    }

  test("Name should parse any alphanumeric constant") {
    val str = """inChannel"""
    name.parse(str) should contain(str)
  }

  test("Name should fail on non alphanumeric constants") {
    val str = """inChannel.test"""
    name.parse(str) shouldNot contain(str)
  }

  test("String should match anything with quotes at the start and end") {
    val str       = "foo"
    val withQutes = s""""$str""""
    string.parse(withQutes) should contain(str)
  }

  test("String should fail if quotes are missing at the end") {
    val str       = "foo"
    val withQutes = s""""$str"""
    string.parse(withQutes) shouldNot contain(str)
  }

  test("String should fail if quotes are missing at the start") {
    val str       = "foo"
    val withQutes = s"""$str""""
    string.parse(withQutes) shouldNot contain(str)
  }

  test("apply shoud match any name with an key value block attached") {
    val str = """foo[arg="val1"]"""
    apply.parse(str) should contain(Apply("foo", Map("arg" -> "val1")))
  }

  test("apply shoud match multiple key values") {
    val str = """foo[arg1="val1",arg2="val2"]"""
    apply.parse(str) should contain(Apply("foo", Map("arg1" -> "val1", "arg2" -> "val2")))
  }

  test("applyVarArgs should match any name with multiple strings") {
    val str = """foo["val1","val2"]"""
    applyVarArgs.parse(str) should contain(ApplyVarArgs("foo", Seq("val1", "val2")))
  }

  test("combineMethod should match union") {
    combineMethod.parse("union") should contain(Union)
  }

  test("combineMethod should match intersect") {
    combineMethod.parse("intersect") should contain(Intersect)
  }

  test("combineMethod should match difference") {
    combineMethod.parse("difference") should contain(Difference)
  }

  test("combineMethod should match diff") {
    combineMethod.parse("diff") should contain(Difference)
  }

  test("combinedTerm should match two terms and a combine method") {
    combinedTerm.parse("(inChannel union console)") should contain(
      Combine(Union, Constant("inChannel"), Constant("console"))
    )
  }

  test("transformation should match one expression -> another expression") {
    transformation.parse("effect1 -> effect2") should contain(
      Transformation(Constant("effect1"), Some(Transformation(Constant("effect2"), None)))
    )
  }

  test("transformedTerm should match a term and an transformation") {
    transformedTerm.parse("<inChannel effect1 -> effect2>") should contain(
      TransformedTerm(
        Constant("inChannel"),
        Transformation(Constant("effect1"), Some(Transformation(Constant("effect2"), None)))
      )
    )
  }

  test("term should match any complex term") {
    val str = """<(<inChannel effect1> union console) effect2 -> effect3[arg="val1"]>"""
    term.parse(str) should contain(
      TransformedTerm(
        Combine(
          Union,
          TransformedTerm(Constant("inChannel"), Transformation(Constant("effect1"), None)),
          Constant("console")
        ),
        Transformation(Constant("effect2"), Some(Transformation(Apply("effect3", Map("arg" -> "val1")), None)))
      )
    )
  }
}
