package com.iadvize.xyleme

sealed trait XmlOperation

object XmlOperation {

  case class DownElem(name: String) extends XmlOperation
  case object NextElem extends XmlOperation
  case class DownAttribute(name: String) extends XmlOperation
  case object DownText extends XmlOperation

  private case class PathFoldingState(result: String, maybeIndex: Option[Int]) {

    def append(str: String): PathFoldingState = maybeIndex match {
      case Some(value) => copy(result + value + "]" + str, None)
      case None        => copy(result + str)
    }

    def increment: PathFoldingState = maybeIndex match {
      case Some(index) => copy(result, Some(index + 1))
      case None        => copy(result + "[", Some(2))
    }
  }

  def printPath(operations: Vector[XmlOperation]): String =
    operations
      .foldLeft(PathFoldingState("", None)) {
        case (state, NextElem)            => state.increment
        case (state, DownElem(name))      => state.append("/" + name)
        case (state, DownAttribute(name)) => state.append(s"/@$name")
        case (state, DownText)            => state.append("/text()")
      }
      .result

}
