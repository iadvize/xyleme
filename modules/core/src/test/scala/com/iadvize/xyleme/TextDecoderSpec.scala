package com.iadvize.xyleme

import org.scalatest.flatspec.AnyFlatSpec

import java.util.UUID

class TextDecoderSpec extends AnyFlatSpec with XmlDecoderBehavior {

  ("an integer" should behave).like(
    xmlTextBehavior[Int](
      "Int",
      successValue = "5",
      successParsedValue = 5,
      failureValue = "b"
    )
  )

  ("a long" should behave).like(
    xmlTextBehavior[Long](
      "Long",
      successValue = "512367312837",
      successParsedValue = 512367312837L,
      failureValue = "334567890456789045TYUIOP13"
    )
  )

  ("a short" should behave).like(
    xmlTextBehavior[Short](
      "Short",
      successValue = "100",
      successParsedValue = 100,
      failureValue = "11111113313"
    )
  )

  ("an uuid" should behave).like(
    xmlTextBehavior[UUID](
      "UUID",
      successValue = "c0c15cbc-1533-4813-a5af-befe59695212",
      successParsedValue = UUID.fromString("c0c15cbc-1533-4813-a5af-befe59695212"),
      failureValue = "11111113313"
    )
  )

  "text decoder" should "enable mapping to other types" in {
    case class Wrapper(value: String)
    val mappedDecoder: TextDecoder[Wrapper] = TextDecoder.decodeString.map(Wrapper)
    assert(ElemCursor.from(<xml>value</xml>).text.as[Wrapper](mappedDecoder).toEither == Right(Wrapper("value")))
  }

}
