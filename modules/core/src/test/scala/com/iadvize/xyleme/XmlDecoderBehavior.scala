package com.iadvize.xyleme

import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec

trait XmlDecoderBehavior extends EitherValues { this: AnyFlatSpec =>

  def xmlTextBehavior[A: TextDecoder](
    typeName: String,
    successValue: String,
    successParsedValue: A,
    failureValue: String
  ): Unit = {
    xmlElementBehavior[A](
      typeName,
      successValue,
      successParsedValue,
      failureValue
    )
    xmlAttributeBehavior[A](
      typeName,
      successValue,
      successParsedValue,
      failureValue
    )
  }

  def xmlTraversableBehavior[F: ElemDecoder](
    emptyValue: F,
    fullValue: F
  ): Unit = {

    case class Element(b: String)

    it should "be empty if no element matches" in {
      val empty = <b></b>
      assert(
        ElemCursor.from(empty).downElem("a").as[F].toEither.value == emptyValue
      )
    }

    it should "not be empty if at least one element matches" in {
      val full = <b><a>dazd</a><a>dazd</a><a>dazd</a></b>
      assert(
        ElemCursor.from(full).downElem("a").as[F].toEither.value == fullValue
      )
    }

  }

  private def xmlElementBehavior[A: TextDecoder](
    typeName: String,
    successValue: String,
    successParsedValue: A,
    failureValue: String
  ): Unit = {

    case class BClass(a: List[A])

    implicit val elementDecoder: ElemDecoder[A] =
      ElemDecoder.instance(_.text.as[A])

    implicit val decodeB: ElemDecoder[BClass] = ElemDecoder.instance { cursor =>
      cursor.downElem("a").as[List[A]].map(BClass)
    }

    it should "be successfully parsed in an element" in {
      val succeedingElement = <b><a>{successValue}</a><a>{successValue}</a></b>
      assert(
        ElemCursor.from(succeedingElement).as[BClass].toEither.value == BClass(
          List(successParsedValue, successParsedValue)
        )
      )
    }

    it should "return a well formatted error in an element" in {
      val failingElement = <b><a>{successValue}</a><a>{failureValue}</a></b>
      val errors = ElemCursor.from(failingElement).as[BClass].toEither.left.value
      assert(errors.length == 1)
      assert(
        errors.head.toString
          .startsWith(s"Failed to parse field into $typeName, path: /b/a[2]/text(), text: $failureValue")
      )
    }

  }

  private def xmlAttributeBehavior[A: TextDecoder](
    typeName: String,
    successValue: String,
    successParsedValue: A,
    failureValue: String
  ): Unit = {

    case class BClass(a: List[A])

    implicit val attributeDecoder: ElemDecoder[A] =
      ElemDecoder.instance(_.attribute("attr").as[A])

    implicit val decodeB: ElemDecoder[BClass] = ElemDecoder.instance { cursor =>
      cursor.downElem("a").as[List[A]].map(BClass)
    }

    it should "be successfully parsed in an attribute" in {
      val succeedingAttribute = <b><a attr={successValue}>text</a><a attr={
        successValue
      }/></b>
      assert(
        ElemCursor.from(succeedingAttribute).as[BClass].toEither.value == BClass(
          List(successParsedValue, successParsedValue)
        )
      )
    }

    it should "return a well formatted error in an attribute" in {
      val failingAttribute = <b><a attr={successValue}>text</a><a attr={
        failureValue
      }/><a>{successValue}</a>/></b>
      val errors =
        ElemCursor.from(failingAttribute).as[BClass].toEither.left.value
      assert(errors.length == 2)
      assert(
        errors.head.toString
          .startsWith(s"Failed to parse field into $typeName, path: /b/a[2]/@attr, text: $failureValue")
      )
      assert(
        errors.toNonEmptyList.tail.head.toString == s"Failed to find field at given path: /b/a[3]/@attr"
      )
    }

  }

}
