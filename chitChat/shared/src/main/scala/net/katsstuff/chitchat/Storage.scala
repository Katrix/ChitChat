/*
 * This file is part of HomeSweetHome, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2016 Katrix
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package net.katsstuff.chitchat

import java.nio.file.{Files, Path}

import scala.collection.JavaConverters._

import cats.effect.IO
import io.circe._
import io.circe.syntax._
import net.katsstuff.chitchat.chat.channel.Channel
import net.katsstuff.katlib.KatPlugin
import net.katsstuff.katlib.helper.LogHelper

object Storage {

  def load(path: Path)(implicit scalaPlugin: KatPlugin): IO[Map[String, Channel]] = {
    for {
      fileExists <- IO {
        LogHelper.info("Loading homes")
        Files.exists(path)
      }
      data <- if (fileExists) {
        IO(Files.readAllLines(path).asScala.mkString("\n")).flatMap { content =>
          IO.fromEither {
            parser.parse(content).flatMap { json =>
              val cursor = json.hcursor
              cursor.get[Int]("version").flatMap {
                case 1 =>
                  cursor.get[Map[String, Channel]]("channel")
                case _ =>
                  Left(new Exception("Unsupported storage version"))
              }
            }
          }
        }
      } else
        IO {
          LogHelper.info("No homes found")
          Map.empty[String, Channel]
        }
    } yield data
  }

  def save(path: Path, channels: Map[String, Channel]): IO[Unit] = {
    val printer = Printer.noSpaces.copy(dropNullValues = true)
    val json    = Json.obj("version" -> 1.asJson, "channel" -> channels.asJson)

    for {
      _ <- IO(Files.createDirectories(path.getParent))
      _ <- IO(Files.write(path, json.pretty(printer).lines.toSeq.asJava))
    } yield ()
  }
}
