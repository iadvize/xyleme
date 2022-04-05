package com.iadvize.xyleme

import com.iadvize.xyleme.ElemDecoder.AccumulatingResult

trait TextCursor {
  def as[A: TextDecoder]: AccumulatingResult[A]
  def asOpt[A: TextDecoder]: AccumulatingResult[Option[A]]
}
