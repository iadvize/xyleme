package com.iadvize.xyleme

import cats.syntax.apply._
import org.scalatest.flatspec.AnyFlatSpec

class XmlDecoderSpec extends AnyFlatSpec with XmlDecoderBehavior {

  private implicit val stringElementDecoder: ElemDecoder[String] =
    ElemDecoder.instance(_.text.as[String])

  "an element with a namespace" should "correctly be decoded" in {

    case class BabarMessage(value: String, attribute: String)
    implicit val babarDecoder: ElemDecoder[BabarMessage] =
      ElemDecoder.instance(cursor =>
        (
          cursor.text.as[String],
          cursor.downAttributeNS("https://babar.com", "attribute").as[String]
        ).mapN(BabarMessage)
      )

    val xml =
      <root><babarland:message xmlns:babarland="https://babar.com" babarland:attribute="valeur">dazd</babarland:message></root>

    val res = ElemCursor.from(xml).downElemNS("https://babar.com", "message").as[BabarMessage]
    assert(res.toEither == Right(BabarMessage("dazd", "valeur")))
  }

  "a namespace in an element" should "be parsed" in {
    assert(
      ElemCursor
        .from(<babar:xml xmlns:babar="https://namespace.com"/>)
        .namespace
        .as[String]
        .toEither
        .value == "https://namespace.com"
    )
  }

  "a missing namespace in an element" should "return a well formatted error" in {
    assert(
      ElemCursor
        .from(<xml/>)
        .namespace
        .as[String]
        .toEither
        .left
        .value
        .head
        .toString == "Failed to find field at given path: /xml/@namespace"
    )
  }

  "a namespace in a missing element" should "return a well formatted error" in {
    assert(
      ElemCursor
        .from(<xml/>)
        .downElem("missing")
        .namespace
        .as[String]
        .toEither
        .left
        .value
        .head
        .toString == "Failed to find field at given path: /xml/missing/@namespace"
    )
  }

  "a missing element" should "lead to a well formatted error" in {
    assert(
      ElemCursor
        .from(<xml></xml>)
        .downElem("missing")
        .downElemNS("http://namespaced", "missing-with-namespace")
        .downElem("missing-again")
        .as[String]
        .toEither
        .left
        .value
        .head
        .toString == "Failed to find field at given path: /xml/missing/http://namespaced:missing-with-namespace/missing-again/text()"
    )
  }

  "a missing element in an array" should "lead to a well formatted error" in {
    assert(
      ElemCursor
        .from(<xml><missing/></xml>)
        .downElem("missing")
        .next
        .next
        .as[String]
        .toEither
        .left
        .value
        .head
        .toString == "Failed to find field at given path: /xml/missing[3]/text()"
    )
  }

  "an attribute in a missing element" should "lead to a well formatted error" in {
    assert(
      ElemCursor
        .from(<xml></xml>)
        .downElem("missing")
        .attribute("missing-attribute")
        .as[String]
        .toEither
        .left
        .value
        .head
        .toString == "Failed to find field at given path: /xml/missing/@missing-attribute"
    )
  }

  "a missing attribute" should "lead to a well formatted error" in {
    assert(
      ElemCursor
        .from(<xml></xml>)
        .attribute("missing")
        .as[String]
        .toEither
        .left
        .value
        .head
        .toString == "Failed to find field at given path: /xml/@missing"
    )
  }

}
