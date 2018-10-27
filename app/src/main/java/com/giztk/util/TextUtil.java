package com.giztk.util;

import java.util.ArrayList;
import java.util.List;

public class TextUtil {

	/**
	 * 将一个句子分裂成单个字组成的列表
	 * @param sentence String
	 * 		    整个句子
	 * @return 单字列表
	 */
	public static List<String> splitSentence(String sentence) {
		List<String> chars = new ArrayList<>();
		for(char c: sentence.toCharArray()) {
			chars.add(String.valueOf(c));
		}
		return chars;
	}

	/**
	 * 将单字列表中的一段合成一个字符串（实体）
	 * @param chars 单字列表
	 * @param startOffset 开始偏移量（包括，从0开始）
	 * @param endOffset   结束偏移量（不包括）
	 * @return 实体字符串
	 */
	public static String formEntity(List<String> chars, int startOffset, int endOffset) {
		// 超出列表长度返回空，开始偏移量小于0返回空
		if(endOffset > chars.size() || startOffset < 0) {
			return null;
		}
		StringBuilder entity = new StringBuilder();
		for(int i = startOffset; i < endOffset; i++) {
			entity.append(chars.get(i));
		}
		return entity.toString();
	}
}
