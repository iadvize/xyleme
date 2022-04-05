package com.iadvize.xyleme

import org.scalatest.flatspec.AnyFlatSpec

class ElemDecoderSpec extends AnyFlatSpec with XmlDecoderBehavior {

  private implicit val stringElementDecoder: ElemDecoder[String] =
    ElemDecoder.instance(_.text.as[String])

  "xml element decoder" should "be able too fallback to another one" in {
    val myDecoder: ElemDecoder[String] =
      ElemDecoder
        .instance(_.downElem("node").text.as[String])
        .or(ElemDecoder.instance(_.attribute("fallback").as[String]))
    assert(ElemCursor.from(<xml fallback="result"/>).as[String](myDecoder).toEither == Right("result"))
  }

  it should "map to other types" in {
    case class Wrapper(value: String)
    val myDecoder: ElemDecoder[Wrapper] = stringElementDecoder.map(Wrapper)
    assert(ElemCursor.from(<xml>result</xml>).as[Wrapper](myDecoder).toEither == Right(Wrapper("result")))
  }

  ("an option" should behave).like(xmlTraversableBehavior(None, Some("dazd")))
  ("a list" should behave).like(
    xmlTraversableBehavior(
      Nil,
      List("dazd", "dazd", "dazd")
    )
  )

}
