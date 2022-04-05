package com.iadvize.xyleme

import cats.data.{ Validated, ValidatedNec, ValidatedNel }
import cats.syntax.either._
import cats.syntax.traverse._

sealed trait ElemDecoder[A] {
  def decodeAccumulating(c: ElemCursor): ElemDecoder.AccumulatingResult[A]

  def or[B >: A](xmlDecoder: ElemDecoder[B]): ElemDecoder[B] =
    ElemDecoder.instance { cursor =>
      decodeAccumulating(cursor).orElse(xmlDecoder.decodeAccumulating(cursor))
    }

  def map[B](fun: A => B): ElemDecoder[B] = ElemDecoder.instance { cursor =>
    decodeAccumulating(cursor).map(fun)
  }

}

object ElemDecoder {

  type AccumulatingResult[A] = ValidatedNec[XmlDecodingError, A]

  def apply[A](implicit parser: ElemDecoder[A]): ElemDecoder[A] = parser

  def instance[A](fun: ElemCursor => AccumulatingResult[A]): ElemDecoder[A] =
    new ElemDecoder[A] {

      override def decodeAccumulating(c: ElemCursor): AccumulatingResult[A] =
        fun(
          c
        )
    }

  implicit def decodeList[A: ElemDecoder]: ElemDecoder[List[A]] =
    ElemDecoder.instance { mainCursor =>
      Iterator
        .iterate(mainCursor)(_.next)
        .takeWhile {
          case _: FoundElemCursor => true
          case _                  => false
        }
        .toList
        .traverse(ElemDecoder[A].decodeAccumulating)
    }

  implicit def decodeOption[A: ElemDecoder]: ElemDecoder[Option[A]] =
    ElemDecoder.instance {
      case cursor: FoundElemCursor => cursor.as[A].map(Some(_))
      case _: FailedCursor         => Validated.Valid(None)
    }

}
