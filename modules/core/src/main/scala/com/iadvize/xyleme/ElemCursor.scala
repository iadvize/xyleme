package com.iadvize.xyleme

import ElemDecoder.AccumulatingResult

import scala.xml.Elem

trait ElemCursor {

  def downElem(
    label: String
  ): ElemCursor

  def downElemNS(
    namespace: String,
    label: String
  ): ElemCursor

  def namespace: TextCursor

  def attribute(
    attrName: String
  ): TextCursor

  def downAttributeNS(
    namespace: String,
    attrName: String
  ): TextCursor

  def next: ElemCursor

  def text: TextCursor

  final def as[A: ElemDecoder]: AccumulatingResult[A] =
    ElemDecoder[A].decodeAccumulating(this)

}

object ElemCursor {

  def from(elem: Elem): ElemCursor =
    FoundElemCursor(Vector(XmlOperation.DownElem(elem.label)), elem, Iterator.empty, 0)
}
