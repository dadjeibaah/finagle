package com.twitter.finagle.exp.mysql

import com.twitter.finagle.exp.mysql.transport.MysqlBuf
import com.twitter.io.Buf
import java.sql.Timestamp
import java.util.TimeZone
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class TimestampValueTest extends FunSuite {
  val timestampValueLocal = new TimestampValue(TimeZone.getDefault, TimeZone.getDefault)

  test("encode timestamp") {
    val RawValue(_, _, true, bytes)
      = timestampValueLocal(Timestamp.valueOf("2014-10-09 08:27:53.123456789"))
    val br = MysqlBuf.reader(bytes)

    assert(br.readShortLE() == 2014)
    assert(br.readByte()  == 10)
    assert(br.readByte()  == 9)
    assert(br.readByte()  == 8)
    assert(br.readByte()  == 27)
    assert(br.readByte()  == 53)
    assert(br.readIntLE()   == 123456)
  }

  test("decode binary timestamp") {
    val bw = MysqlBuf.writer(new Array[Byte](11))
    bw.writeShortLE(2015)
      .writeByte(1)
      .writeByte(2)
      .writeByte(3)
      .writeByte(4)
      .writeByte(5)
      .writeIntLE(678901)

    val array = Buf.ByteArray.Owned.extract(bw.owned())

    val timestampValueLocal(ts) = RawValue(Type.Timestamp, Charset.Binary, true, array)
    assert(ts == Timestamp.valueOf("2015-01-02 03:04:05.678901"))
  }

  test("decode text timestamp") {
    val str = "2015-01-02 03:04:05.67890"

    val timestampValueLocal(ts) = RawValue(Type.Timestamp, Charset.Binary, false, str.getBytes)
    assert(ts == Timestamp.valueOf("2015-01-02 03:04:05.6789"))
  }

  test("decode zero timestamp") {
    val str = "0000-00-00 00:00:00"

    val timestampValueLocal(ts) = RawValue(Type.Timestamp, Charset.Binary, false, str.getBytes)
    assert(ts == new Timestamp(0))
  }
}
