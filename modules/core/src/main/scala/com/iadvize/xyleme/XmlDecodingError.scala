package com.iadvize.xyleme

import XmlDecodingError.Kind

final case class XmlDecodingError(
  history: Vector[XmlOperation],
  kind: Kind
) {

  def maybeThrowable: Option[Throwable] =
    Some(kind).collect {
      case pf: XmlDecodingError.ParsingFailed => pf.maybeThrowable
    }.flatten

  override def toString: String = kind match {
    case XmlDecodingError.FieldNotFound =>
      s"Failed to find field at given path: ${XmlOperation.printPath(history)}"
    case XmlDecodingError.ParsingFailed(typeName, given, maybeThrowable) =>
      s"Failed to parse field into $typeName, path: ${XmlOperation.printPath(history)}, text: $given" + maybeThrowable
        .map(_.getMessage)
        .map(", error: " + _)
        .mkString
  }

}

object XmlDecodingError {

  sealed trait Kind
  case object FieldNotFound extends Kind
  final case class ParsingFailed(typeName: String, text: String, maybeThrowable: Option[Throwable]) extends Kind

}
