package com.iadvize.xyleme

import com.iadvize.xyleme.ElemDecoder.AccumulatingResult
import com.iadvize.xyleme.XmlDecodingError.FieldNotFound
import cats.syntax.validated._

case class MissingTextCursor(history: Vector[XmlOperation]) extends TextCursor {
  override def as[A: TextDecoder]: AccumulatingResult[A] = XmlDecodingError(history, FieldNotFound).invalidNec

  override def asOpt[A: TextDecoder]: AccumulatingResult[Option[A]] = None.valid
}
