package com.iadvize.xyleme

import XmlDecodingError.ParsingFailed

import java.util.UUID
import scala.util.Try
import cats.syntax.either._

sealed trait TextDecoder[A] {
  def decode(str: String): TextDecoder.Result[A]

  def map[B](fun: A => B): TextDecoder[B] = TextDecoder.instance { str =>
    decode(str).map(fun)
  }

}

object TextDecoder {

  type Result[A] = Either[ParsingFailed, A]

  def apply[A](implicit textDecoder: TextDecoder[A]): TextDecoder[A] = textDecoder

  def instance[A](fun: String => Result[A]): TextDecoder[A] =
    new TextDecoder[A] {
      override def decode(str: String): Result[A] = fun(str)
    }

  def fromTry[A](typeName: String)(fun: String => Try[A]): TextDecoder[A] = TextDecoder.instance { str =>
    fun(str).toEither.leftMap(err => ParsingFailed(typeName, str, Some(err)))
  }

  def fromOption[A](typeName: String)(fun: String => Option[A]): TextDecoder[A] = TextDecoder.instance { str =>
    fun(str).toRight(ParsingFailed(typeName, str, None))
  }

  implicit val decodeString: TextDecoder[String] =
    TextDecoder.instance(Right(_))

  implicit val decodeInt: TextDecoder[Int] =
    fromOption("Int")(str => Try(str.toInt).toOption)

  implicit val decodeLong: TextDecoder[Long] =
    fromOption("Long")(str => Try(str.toLong).toOption)

  implicit val decodeShort: TextDecoder[Short] =
    fromOption("Short")(str => Try(str.toShort).toOption)

  implicit val decodeBoolean: TextDecoder[Boolean] =
    fromOption("Boolean")(str => Try(str.toBoolean).toOption)

  implicit val decodeDouble: TextDecoder[Double] =
    fromOption("Double")(str => Try(str.toDouble).toOption)

  implicit val decodeUUID: TextDecoder[UUID] =
    fromTry("UUID")(str => Try(UUID.fromString(str)))

}
