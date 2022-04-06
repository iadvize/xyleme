package com.iadvize.xyleme

import com.iadvize.xyleme.XmlOperation.{ DownAttribute, DownText }

private[xyleme] final case class FailedCursor(history: Vector[XmlOperation]) extends ElemCursor {
  override def downElem(label: String): ElemCursor = appendToHistory(XmlOperation.DownElem(label))

  override def downElemNS(namespace: String, label: String): ElemCursor =
    appendToHistory(XmlOperation.DownElem(s"$namespace:$label"))

  override def namespace: TextCursor = MissingTextCursor(history :+ DownAttribute("namespace"))

  override def attribute(attrName: String): TextCursor =
    MissingTextCursor(history :+ DownAttribute(attrName))

  override def downAttributeNS(namespace: String, attrName: String): TextCursor =
    MissingTextCursor(history :+ DownAttribute(s"$namespace:$attrName"))

  override def next: ElemCursor = appendToHistory(XmlOperation.NextElem)

  override def text: TextCursor = MissingTextCursor(history :+ DownText)

  override def isFailed: Boolean = true

  private def appendToHistory(operation: XmlOperation) = FailedCursor(history :+ operation)
}
