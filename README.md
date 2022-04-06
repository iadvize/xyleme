# Xyleme
_A quick way to parse XML structures_

[![codecov](https://codecov.io/gh/iadvize/xyleme/branch/main/graph/badge.svg?token=K6BM30SJX7)](https://codecov.io/gh/iadvize/xyleme)


### Motivation

Xyleme comes from the frustration of having to quickly parse XML elements. 
In my experience, two well-known libraries enable to parse XML in Scala : 
- native [scala-xml](https://github.com/scala/scala-xml) enables fast formatting, but is tedious while parsing 
- [scalaxb](https://github.com/eed3si9n/scalaxb) is safer, but needs XSD to be defined and sometimes is not ideal (complex structure, duplicate names)

The idea of Xyleme is to use [circe](https://github.com/circe/circe) as inspiration to enable safer and easier parsing of xml elements.

### Getting started

First, add Xyleme dependency to your module definition in `build.sbt`
```sbt
libraryDependencies += "com.iadvize" %% "xyleme" % "<version>"
```

It brings to transitive dependencies : [cats-core](https://github.com/typelevel/cats) and [scala-xml](https://github.com/scala/scala-xml)

Then, use `ElemDecoder` and `ElemCursor` to parse your nodes 

```scala
import com.iadvize.xyleme._
import scala.util.Try
import cats.syntax.apply._

case class Price(value: Long)
case class Album(name: String, artist: String, price: Price)

implicit val priceDecoder: TextDecoder[Price] = TextDecoder.fromOption("Price") { str =>
  Try((BigDecimal(str) * 100).toLongExact).toOption.map(Price)
}

implicit val decoder: ElemDecoder[Album] = ElemDecoder.instance { elem =>
  (
    elem.downElem("name").text.as[String],
    elem.downElem("artist").text.as[String],
    elem.attribute("price").as[Price]
    ).mapN(Album)
}

val xml = <albums>
  <album price="15.00">
    <name>One Size Fits All</name>
    <artist>Frank Zappa</artist>
  </album>
  <album price="13.00">
    <name>Maggot Brain</name>
    <artist>Funkadelic</artist>
  </album>
</albums>

val albums = ElemCursor.from(xml).downElem("album").as[List[Album]]

println(albums)
// Valid(List(
//    Album(One Size Fits All,Frank Zappa,Price(1500)), 
//    Album(Maggot Brain,Funkadelic,Price(1300))
// ))
```

Xyleme is able to detect formatting errors. These are accumulated thanks to the [ValidatedNec](https://typelevel.org/cats/datatypes/validated.html) datatype

```scala
val wrongXml = <albums>
  <album price="not-a-price">
    <name>One Size Fits All</name>
    <artist>Frank Zappa</artist>
  </album>
  <album price="13.00">
    <name>Maggot Brain</name>
  </album>
</albums>

val wrongAlbums = ElemCursor.from(wrongXml).downElem("album").as[List[Album]]

println(wrongAlbums)
// Invalid(Chain(
//    Failed to parse field into Price, path: /albums/album/@price, text: not-a-price, 
//    Failed to find field at given path: /albums/album[2]/artist/text()
// ))
```

### Current state

Xyleme is currently in development : 
- its API is not stable and could change
- it certainly has a lot of edge cases
- its current implementation is not optimized (multiple traversals of DOM)

Feel free to contribute :). I already think of several improvements: 
- a stax cursor implementation, quite complex, but I think it could be possible
- add property based tests to improve coverage of edge cases