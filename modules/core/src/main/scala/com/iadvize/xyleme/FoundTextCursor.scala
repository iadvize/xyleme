package com.iadvize.xyleme

import cats.syntax.either._
import ElemDecoder.AccumulatingResult
import cats.syntax.traverse._

case class FoundTextCursor(history: Vector[XmlOperation], text: String) extends TextCursor {

  final def as[A: TextDecoder]: AccumulatingResult[A] =
    implicitly[TextDecoder[A]].decode(text).leftMap(XmlDecodingError(history, _)).toValidatedNec

  final def asOpt[A: TextDecoder]: AccumulatingResult[Option[A]] = Some(text).filter(_.nonEmpty).traverse { text =>
    TextDecoder[A].decode(text).leftMap(XmlDecodingError(history, _)).toValidatedNec
  }

}
