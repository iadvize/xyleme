package com.iadvize.xyleme

import com.iadvize.xyleme.XmlOperation.{ DownAttribute, DownText }

import scala.xml.{ Elem, Text }

private[xyleme] final case class FoundElemCursor(
  history: Vector[XmlOperation],
  elem: Elem,
  neighbours: Iterator[Elem],
  currentIndex: Int
) extends ElemCursor {

  override def downElem(
    label: String
  ): ElemCursor = downElemNamespaced(label, None)

  override def downElemNS(
    namespace: String,
    label: String
  ): ElemCursor =
    downElemNamespaced(label, Some(namespace))

  override def namespace: TextCursor =
    leadTo(Option(elem.namespace), XmlOperation.DownAttribute("namespace"))

  override def attribute(
    attrName: String
  ): TextCursor = downAttributeNamespaced(attrName, None)

  override def downAttributeNS(
    namespace: String,
    attrName: String
  ): TextCursor = downAttributeNamespaced(attrName, Some(namespace))

  override def next: ElemCursor =
    if (neighbours.hasNext)
      goTo(
        neighbours.next(),
        neighbours,
        currentIndex + 1,
        XmlOperation.NextElem
      )
    else fail(XmlOperation.NextElem)

  override def text: TextCursor = leadTo(Some(elem.text), DownText)

  private def fail(op: XmlOperation): FailedCursor = FailedCursor(history :+ op)

  private def leadTo(maybeText: Option[String], operation: XmlOperation) =
    maybeText match {
      case Some(text) => FoundTextCursor(history :+ operation, text)
      case None       => MissingTextCursor(history :+ operation)
    }

  private def goTo(elem: Elem, neighbors: Iterator[Elem], currentIndex: Int, operation: XmlOperation) =
    FoundElemCursor(history :+ operation, elem, neighbors, currentIndex)

  private def downElemNamespaced(
    label: String,
    namespace: Option[String]
  ): ElemCursor = {
    val children = elem.child.collect {
      case elem: Elem if elem.label == label && namespace.forall(_ == elem.namespace) =>
        elem
    }.iterator

    if (!children.hasNext) fail(XmlOperation.DownElem(label))
    else
      goTo(
        children.next(),
        children,
        0,
        XmlOperation.DownElem(label)
      )
  }

  private def downAttributeNamespaced(
    attrName: String,
    maybeNamespace: Option[String]
  ): TextCursor = {
    val maybeAttributeValue = (maybeNamespace match {
      case Some(namespace) => elem.attributes.get(namespace, elem, attrName)
      case None            => elem.attributes.get(attrName)
    }).collect {
      case text: Text =>
        text.text
    }
    leadTo(maybeAttributeValue, DownAttribute(attrName))
  }

}
