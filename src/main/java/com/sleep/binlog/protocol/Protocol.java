package com.sleep.binlog.protocol;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author yafeng.huang
 *
 */
public abstract class Protocol {

	private ByteBuffer buf;

	public Protocol(ByteBuffer buf) {
		this.buf = buf;
	}

	public int remaining() {
		return buf.remaining();
	}

	/**
	 * java byte 去符号位转 int
	 * 
	 * @param value
	 * @return
	 */
	private int toInt(byte value) {
		return value & 0xff;
	}

	/**
	 * java byte 去符号位转 long
	 * 
	 * @param value
	 * @return
	 */
	private long toLong(byte value) {
		return (long) toInt(value);
	}

	public int[] read(int length) {
		int[] result = new int[length];
		for (int i = 0; i < length; i++) {
			result[i] = toInt(buf.get());
		}
		return result;
	}

	public byte[] readByte(int length) {
		byte[] result = new byte[length];
		for (int i = 0; i < length; i++) {
			result[i] = buf.get();
		}
		return result;
	}

	/**
	 * 以小头序读取若干字节的 int
	 * 
	 * @param length
	 * @return
	 */
	public int readInt(int length) {
		int result = 0;
		for (int i = 0; i < length; i++) {
			result |= (toInt(buf.get()) << (i << 3));
		}
		return result;
	}

	public int readBigedianInt(int length) {
		int result = 0;
		for (int i = length - 1; i >= 0; i--) {
			result |= (toInt(buf.get()) << (i << 3));
		}
		return result;
	}

	public long readLong(int length) {
		long result = 0;
		for (int i = 0; i < length; i++) {
			result |= (toLong(buf.get()) << (i << 3));
		}
		return result;
	}

	/**
	 * 读取以 0 为结尾字节的字符串
	 * 
	 * @return
	 */
	public String readZeroEndString() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte temp = 0;
		while ((temp = buf.get()) != 0) {
			out.write(temp);
		}
		return new String(out.toByteArray());
	}

	/**
	 * 读取固定长度的字符串
	 * 
	 * @param length
	 * @return
	 */
	public String readFixedLengthString(int length) {
		if (length == 0) {
			return null;
		}
		byte[] arr = new byte[length];
		for (int i = 0; i < length; i++) {
			arr[i] = buf.get();
		}
		return new String(arr);
	}

	/**
	 * 忽略若干字节
	 * 
	 * @param length
	 */
	public void ignore(int length) {
		for (int i = 0; i < length; i++) {
			buf.get();
		}
	}

	/**
	 * <a href="http://dev.mysql.com/doc/internals/en/integer.html">
	 * 
	 * @return
	 * @throws IOException
	 */
	public Long readLengthEncodedInt() throws IOException {
		int firstByte = toInt(buf.get());
		if (firstByte < 0xfb) {
			return (long) firstByte;
		} else if (firstByte == 0xfb) {
			return null;
		} else if (firstByte == 0xfc) {
			return (long) readInt(2);
		} else if (firstByte == 0xfd) {
			return (long) readInt(3);
		} else if (firstByte == 0xfe) {
			return readLong(8);
		}
		// 不符合要求的直接抛出异常
		throw new IOException("Not length encode integer " + firstByte);
	}

	public String readEOFString() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		while (buf.hasRemaining()) {
			out.write(buf.get());
		}
		return new String(out.toByteArray());
	}

	public int[] readBitmap(int count) {
		int bitmapLength = (count + 7) >> 3;
		int[] temp = read(bitmapLength);
		for (int i = 0, len = temp.length >> 1; i < len; i++) {
			int t = temp[i];
			temp[i] = temp[temp.length - i - 1];
			temp[temp.length - i - 1] = t;
		}
		int[] bitmap = new int[count];
		for (int i = 0; i < count; i++) {
			bitmap[i] = ((temp[i >> 3] & (1 << (i % 8))) == 0) ? 0 : 1;
		}
		return bitmap;
	}

	public int[] readBigedianBitmap(int count) {
		int bitmapLength = (count + 7) >> 3;
		int[] temp = read(bitmapLength);
		int[] bitmap = new int[count];
		for (int i = 0; i < count; i++) {
			bitmap[i] = ((temp[i >> 3] & (1 << (i % 8))) == 0) ? 0 : 1;
		}
		return bitmap;
	}
}
