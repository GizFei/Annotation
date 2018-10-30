package com.giztk.util;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	public static JSONObject formLoginJson(String username, String password){
        try {
            JSONObject object = new JSONObject();
            object.put("username", username);
            object.put("password", password);
            return object;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static JSONObject formRegisterJson(String username, String email, String password){
        Map<String, String> params = new HashMap<>();
        params.put("username", username);
        params.put("password", password);
        params.put("email", email);
        return new JSONObject(params);
    }

    public static JSONObject formTokenJson(String token){
        Map<String, String> params = new HashMap<>();
        params.put("token", token);
        return new JSONObject(params);
    }

    public static JSONObject getTestContent(){
		String text = "{\"title\": \"欧巴马总统和中国国家主席习近平在北京举行的联合记者会上发表讲 话 | 美国驻华大使馆和领事馆\", " +
				"\"content\": \"我们希望这方面的进展能够继续，因为正如我以前所说，这对我们各 方都有益。 " +
				"我相信习主席和我就我们国家之间的关系能够如何向前迈进有共同的理解。我们同意可以在我们利益重叠或一致的领域扩大我们的合作。" +
				"当我们意见不 同时，我们将坦诚而明确地表达我们的意图，并将在可能的情况下缩小这些分歧。 即使我们在一些领域有竞争和不同意见，" +
				"我相信我们能够继续推进我们的人民和世 界各地人民的安全和繁荣。我们希望这方面的进展能够继续，因为正如我以前所说，这对我们各 方都有益."+
                "我相信习主席和我就我们国家之间的关系能够如何向前迈进有共同的理解。\", " +
				"\"sent_id\": 2, " +
				"\"doc_id\": \"ba2e5888-d080-11e8-9d6d-0242ac110003\"}";
		try{
            return new JSONObject(text);
        }catch (Exception e){
		    e.printStackTrace();
		    return null;
        }
	}
}
