package com.android.volley.variant;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.text.TextUtils;

public final class MD5Utils {

	public static final String encodeBy16BitMD5(String source) {

		return encrypt(source, true);
	}

	public static final String encodeBy32BitMD5(String source) {

		return encrypt(source, false);
	}

	static final String encrypt(String source, boolean is16bit) {

		if (TextUtils.isEmpty(source)) {

			return null;
		}

		String encryptedStr = null;

		try {
			MessageDigest digester = MessageDigest.getInstance("MD5");

			encryptedStr = convertToHexString(digester.digest(source
					.getBytes("utf-8")));

			if (is16bit) {

				encryptedStr = encryptedStr.substring(8, 24);
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return encryptedStr;
	}

	static final String convertToHexString(byte data[]) {

		int i;

		StringBuffer buf = new StringBuffer();

		for (int offset = 0; offset < data.length; offset++) {

			i = data[offset];

			if (i < 0) {

				i += 256;
			}
			if (i < 16) {

				buf.append("0");
			}
			buf.append(Integer.toHexString(i));
		}
		return buf.toString();
	}

	public static String MD5Formatting(String s) {
		char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'a', 'b', 'c', 'd', 'e', 'f' };
		try {
			byte[] strTemp = s.getBytes();
			MessageDigest mdTemp = MessageDigest.getInstance("MD5");
			mdTemp.update(strTemp);
			byte[] md = mdTemp.digest();
			int j = md.length;
			char str[] = new char[j * 2];
			int k = 0;
			for (int i = 0; i < j; i++) {
				byte byte0 = md[i];
				str[k++] = hexDigits[byte0 >>> 4 & 0xf];
				str[k++] = hexDigits[byte0 & 0xf];
			}
			return new String(str);
		} catch (Exception e) {
			return null;
		}
	}
}
