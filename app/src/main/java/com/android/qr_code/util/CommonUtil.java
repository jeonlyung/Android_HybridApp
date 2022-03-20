package com.android.qr_code.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonReader;
import com.kakao.KakaoLink;
import com.kakao.KakaoParameterException;
import com.kakao.KakaoTalkLinkMessageBuilder;
import com.mobile.horsepia.R;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class CommonUtil {

	public static final String[] URL_SERVERS = {"https://www.horsepia.com/", "http://test.intra.kra.co.kr:8067/"};
//	public static final String[] URL_SERVERS = {"http://www.horsepia.com/m/index.do", "http://test.intra.kra.co.kr:8067/"};
	public static String SERVER = URL_SERVERS[0];

	public static final String Tag = "CommonUtil";
	static final String PREF_NAME = "Mobile_Horsepia_pref";

	public static final String TYPEFACE_NAME1 = "www/component/static/font/android_font.ttf.mp3";
	public static final String TYPEFACE_NAME_BOLD1 = "www/component/static/font/android_Bold.ttf.mp3";
	public static final String TYPEFACE_NAME = "www/component/static/font/NanumBarunGothic.woff";
	public static final String TYPEFACE_NAME_BOLD = "www/component/static/font/NanumBarunGothicBold.woff";
	Typeface typeface = null;
	Typeface typeface_bold = null;
	
	private AlertDialog mPositionDlg;
	
	Intent i;
	
	public static JSONArray json = null;

	public void loadTypeface(Context context) {
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
			typeface = Typeface.SANS_SERIF;
			typeface_bold = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);
		} else {
			try {
				if (typeface == null) {
					try {
						typeface = Typeface.createFromAsset(context.getAssets(), TYPEFACE_NAME);
					} catch (NullPointerException e) {
						typeface = Typeface.createFromAsset(context.getAssets(), TYPEFACE_NAME1);
					}
				}

				if (typeface_bold == null) {
					try {
						typeface_bold = Typeface.createFromAsset(context.getAssets(), TYPEFACE_NAME_BOLD);
					} catch (NullPointerException e) {
						typeface_bold = Typeface.createFromAsset(context.getAssets(), TYPEFACE_NAME_BOLD1);
					}
				}
			} catch (NullPointerException e) {
				typeface = Typeface.SANS_SERIF;
				typeface_bold = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);
			}
		}
	}

	/*********************************
	 * /* Convert
	 **********************************/
	// map -> url pramater
	public static String urlEncodeUTF8(String s) {
		try {
			return URLEncoder.encode(s, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new UnsupportedOperationException(e);
		}
	}

	public static Map<String, Object> jsonParser(String returnMsg) {
		JsonParser jsParser = new JsonParser();
		JsonReader jReader = new JsonReader(new StringReader(returnMsg));
		JsonElement firstElement = jsParser.parse(jReader);
		Map<String, Object> resultMap = (Map<String, Object>) jsonToJava(firstElement);

		return resultMap;
	}

	public static Object jsonToJava(JsonElement jsonElement) {
		if (jsonElement.isJsonArray()) {
			JsonArray jsonArray = jsonElement.getAsJsonArray();
			List<Object> assembledList = new ArrayList<Object>();
			Iterator<?> jsonIterator = jsonArray.iterator();
			while (jsonIterator.hasNext()) {
				JsonElement entryElement = (JsonElement) jsonIterator.next();
				Object entryObj = jsonToJava(entryElement);
				assembledList.add(entryObj);
			}
			return assembledList;

		} else if (jsonElement.isJsonObject()) {
			JsonObject jsonObj = jsonElement.getAsJsonObject();
			Map<String, Object> assembledMap = new HashMap<String, Object>();
			for (Entry<String, JsonElement> entry : jsonObj.entrySet()) {
				String key = entry.getKey();
				JsonElement value = entry.getValue();
				Object entryObj = jsonToJava(value);
				assembledMap.put(key, entryObj);
			}
			return assembledMap;

		} else if (jsonElement.isJsonPrimitive()) {
			JsonPrimitive jsonPrimitive = jsonElement.getAsJsonPrimitive();
			if (jsonPrimitive.isBoolean()) {
				return jsonPrimitive.getAsBoolean();
			} else if (jsonPrimitive.isNumber()) {
				return jsonPrimitive.getAsBigDecimal();
			} else if (jsonPrimitive.isString()) {
				return jsonPrimitive.getAsString();
			} else {
				return null;
			}
			
		} else if (jsonElement.isJsonNull()) {
			return null;

		} else {
			return null;
		}
	}

	// map -> param
	public static String mapToParam(Map<?, ?> map) {
		if (map == null) {
			return "";
		}

		StringBuilder sb = new StringBuilder();
		for (Entry<?, ?> entry : map.entrySet()) {
			if (sb.length() > 0) {
				sb.append("&");
			}
			sb.append(String.format("%s=%s",
					// urlEncodeUTF8(entry.getKey().toString()),
					// urlEncodeUTF8(entry.getValue().toString())
					entry.getKey().toString(), entry.getValue().toString()));
		}
		return sb.toString();
	}

	// jsonObject -> param
	public static String jsonToParam(JSONObject json) throws JSONException {
		if (json == null) {
			return "";
		}

		Map<String, String> map = new HashMap<String, String>();
		// fill the list

		Iterator iter = json.keys();
		while (iter.hasNext()) {
			String currentKey = (String) iter.next();
			map.put(currentKey, json.getString(currentKey));
		}

		StringBuilder sb = new StringBuilder();
		for (Entry<?, ?> entry : map.entrySet()) {
			if (sb.length() > 0) {
				sb.append("&");
			}
			sb.append(String.format("%s=%s",
					// urlEncodeUTF8(entry.getKey().toString()),
					// urlEncodeUTF8(entry.getValue().toString())
					entry.getKey().toString(), entry.getValue().toString()));
		}
		return sb.toString();
	}

	// JSONArray => ArrayList<String>
	public static ArrayList<String> jsonToArrayString(JSONArray json) throws JSONException {
		ArrayList<String> list = new ArrayList<String>();
		if (json != null) {
			int len = json.length();
			for (int i = 0; i < len; i++) {
				list.add(json.getString(i).toString());
			}
		}
		return list;
	}

	// JSONArray => ArrayList<Map<String, String>>
	public static ArrayList<Map<String, String>> jsonToArrayMap(JSONArray json) throws JSONException {
		ArrayList<Map<String, String>> list = new ArrayList<Map<String, String>>();
		Map<String, String> map;
		// fill the list
		for (int i = 0; i < json.length(); i++) {
			map = new HashMap<String, String>();

			JSONObject jo = (JSONObject) json.get(i);
			// fill map
			Iterator iter = jo.keys();
			while (iter.hasNext()) {
				String currentKey = (String) iter.next();
				map.put(currentKey, jo.getString(currentKey));
			}
			// add map to list
			list.add(map);
		}
		return list;
	}

	public static Map<String, String> jsonToMap(JSONObject json) throws JSONException {
		Map<String, String> map = new HashMap<String, String>();
		// fill the list
		Iterator iter = json.keys();
		while (iter.hasNext()) {
			String currentKey = (String) iter.next();
			map.put(currentKey, json.getString(currentKey));
		}
		return map;
	}

	// JSONArray => ArrayList<Map<String, String>>
	public static ArrayList<String> jsonToArrayKey(JSONArray json, String key) throws JSONException {
		ArrayList<String> list = new ArrayList<String>();
		if (json != null) {
			int len = json.length();
			for (int i = 0; i < len; i++) {
				JSONObject jo = (JSONObject) json.get(i);
				list.add(jo.getString(key).toString());
			}
		}

		return list;
	}

	public static boolean isInstalledApplication(Context context, String packageName) {
		PackageManager pm = context.getPackageManager();
		try {
			pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
		} catch (NameNotFoundException e) {
			return false;
		}

		return true;
	}

	public static void appStart(final Context context, String name, final String packagename) {
		if (!isInstalledApplication(context, packagename)) {
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			// builder.setTitle("알림");
			builder.setMessage(name + "App이 설치되어 있지 않습니다.\n설치하기 위해 Store로 이동하시겠습니까?")
					.setPositiveButton("확인", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							String url = "https://market.android.com/details?id=" + packagename;
							setUpViewer(context, url);
						}
					}).create().show();
		} else {

			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			// builder.setTitle("알림");
			builder.setMessage(name + "App을 실행하시겠습니까?").setPositiveButton("확인", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					Intent intent = context.getPackageManager().getLaunchIntentForPackage(packagename);
					context.startActivity(intent);
				}
			}).create().show();

		}
	}

	public static void sendMsgKakao(final Context context, String msg) {
		KakaoLink kakaoLink;
		KakaoTalkLinkMessageBuilder kakaoTalkLinkMessageBuilder;
		try {
			kakaoLink = KakaoLink.getKakaoLink(context);
			kakaoTalkLinkMessageBuilder = kakaoLink.createKakaoTalkLinkMessageBuilder();
			kakaoTalkLinkMessageBuilder.addText(msg);
			final String linkContents = kakaoTalkLinkMessageBuilder.build();
			kakaoLink.sendMessage(linkContents, context);
		} catch (KakaoParameterException e) {
			// TODO Auto-generated catch block
			System.out.println("예외 발생");
		}
	}

	public static void setUpViewer(Context context, String url) {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(url));
		context.startActivity(intent);
	}

	// 화면 캡쳐블러처리
	@SuppressLint("NewApi")
	public static Bitmap screenblur(Activity av2, View view, float radius) throws Exception {
		if (view == null)
			view = av2.getWindow().getDecorView();
		view.setDrawingCacheEnabled(true);
		Bitmap screenshot = view.getDrawingCache();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			RenderScript rs = RenderScript.create(av2);

			final Allocation input = Allocation.createFromBitmap(rs, screenshot);
			final Allocation output = Allocation.createTyped(rs, input.getType());
			final ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
			script.setRadius(radius);
			script.setInput(input);
			script.forEach(output);
			output.copyTo(screenshot);
		}

		return screenshot;
	}

	// top activity 이름 가져오기
	public static String getTopActivityStackName(Context context) {
		ActivityManager am = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);
		List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
		ComponentName componentInfo = taskInfo.get(0).topActivity;

		return componentInfo.getClassName();
	}

	/**
	 * 현재 날짜 YYYY.MM.DD
	 * 
	 * @return
	 */
	public static String getCurrentDate(String gubun) {

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy" + gubun + "MM" + gubun + "dd",
				java.util.Locale.getDefault());
		Date date = new Date();
		String strDate = dateFormat.format(date);

		return strDate;
	}

	/**
	 * 현재 날짜 YYYY.MM.DD
	 * 
	 * @return
	 */
	public static String getCurrentDate() {
		Calendar cal = Calendar.getInstance();
		return "" + cal.get(Calendar.YEAR) + "." + (cal.get(Calendar.MONTH) + 1) + "." + cal.get(Calendar.DATE);
	}

	/**
	 * 요일
	 * 
	 * @return
	 */
	public static String getCurrentDayOfWeek() {
		Calendar cal = Calendar.getInstance();
		int dayNum = cal.get(Calendar.DAY_OF_WEEK);
		String day = "";
		switch (dayNum) {
		case 1:
			day = "일";
			break;
		case 2:
			day = "월";
			break;
		case 3:
			day = "화";
			break;
		case 4:
			day = "수";
			break;
		case 5:
			day = "목";
			break;
		case 6:
			day = "금";
			break;
		case 7:
			day = "토";
			break;
		}
		return day;
	}

	public static int StringToInt(String num) {
		int result = 0;
		try {
			result = Integer.parseInt(num);
		} catch (NullPointerException e) {
			Log.e("asdd", Tag + " 487 === Exception occured ");
		}
		return result;
	}

	public static float converDpToPx(float dp, Context context) {
		Resources r = context.getResources();
		float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
		return px;
	}

	public static float converSpToPx(float sp, Context context) {
		Resources r = context.getResources();
		float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, sp, r.getDisplayMetrics());
		return px;
	}

	public static float converPxToSp(float px, Context context) {
		Resources r = context.getResources();
		float sp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, px, r.getDisplayMetrics());
		return sp;
	}

	public static void AnimationScaleDn(Context context, View view, AnimationListener listener) {
		if (view == null)
			return;
		Animation ani = AnimationUtils.loadAnimation(context, R.anim.scale_dn);
		ani.setAnimationListener(listener);
		view.startAnimation(ani);
		view.setVisibility(View.VISIBLE);
	}

	public static void AnimationScaleUp(Context context, View view, AnimationListener listener) {
		if (view == null)
			return;
		Animation ani = AnimationUtils.loadAnimation(context, R.anim.scale_up);
		ani.setAnimationListener(listener);
		view.startAnimation(ani);
		view.setVisibility(View.VISIBLE);
	}

	public static void startAnimation(Context context, int id, View view, AnimationListener listener) {
		if (view == null)
			return;
		Animation ani = AnimationUtils.loadAnimation(context, id);
		ani.setAnimationListener(listener);
		view.startAnimation(ani);
		view.setVisibility(View.VISIBLE);
	}

	public static void kakaoLink(Context mContext, String text, String imageUrl, String webLink) {
		try {
			KakaoLink kakaoLink = KakaoLink.getKakaoLink(mContext);
			KakaoTalkLinkMessageBuilder kakaoTalkLinkMessageBuilder = kakaoLink.createKakaoTalkLinkMessageBuilder();

			if (text != null)
				kakaoTalkLinkMessageBuilder.addText(text);

			if (imageUrl != null)
				kakaoTalkLinkMessageBuilder.addImage(imageUrl, 300, 200);

			if (webLink == null)
				kakaoTalkLinkMessageBuilder.addAppLink("앱으로 이동");
			else {
				kakaoTalkLinkMessageBuilder.addWebButton("웹으로 이동", webLink);
			}

			kakaoLink.sendMessage(kakaoTalkLinkMessageBuilder.build(), mContext);
		} catch (KakaoParameterException e) {
			System.out.println("예외 발생");
		}
	}
	
	String regularPattern(String urlData){
		StringBuffer numstr = new StringBuffer();
		if( urlData != null ){ 
			String patternStr = "";
			if(urlData.contains("CONT_ID"))
				patternStr = "CONT_ID=([0-9]{2,})&";  
			Pattern pattern = Pattern.compile(patternStr); 
			Matcher matcher = pattern.matcher(urlData); 

			while(matcher.find()) { 
				numstr.append(matcher.group(1));
			}
		} 
	    
	    return numstr.toString();
	}
	
	public void shareFacebook(Context context, String param) {
		String url = "https://api.addthis.com/oexchange/0.8/forward/facebook/offer" + param;
		i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(i);
	}
	
	public void shareTwitter(Context context, String param) {
		String url = "https://api.addthis.com/oexchange/0.8/forward/twitter/offer" + param;
		i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(i);
	}
	
	public void shareKakaoTalk(final Context context, String link) {
		if (CommonUtil.isInstalledApplication(context, "com.kakao.talk")) {
			CommonUtil.kakaoLink(context, null, null, link);
		} else {
			((Activity) context).runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					new AlertDialog.Builder(context).setTitle("").setMessage("카카오톡 앱이 설치되어 있지 않습니다.")
					.setPositiveButton(android.R.string.ok, new AlertDialog.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							// TODO Auto-generated method stub
							arg0.dismiss();
						}
					})
					.setOnCancelListener(new AlertDialog.OnCancelListener() {
						@Override
						public void onCancel(DialogInterface arg0) {
							arg0.dismiss();
						}
					})
					.setCancelable(false).create().show();
				}
			});
		}
	}
	
	public static void goDaumMap(final Context context, String link) {
		if (CommonUtil.isInstalledApplication(context, "net.daum.android.map")) {
			//CommonUtil.kakaoLink(context, null, null, link);
			Log.e("asdd", Tag + " 596 ========== " + link);
			Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
			context.startActivity(i);
		} else {
			((Activity) context).runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					new AlertDialog.Builder(context).setTitle("").setMessage("다음지도 앱이 설치되어 있지 않습니다.")
					.setPositiveButton(android.R.string.ok, new AlertDialog.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							// TODO Auto-generated method stub
							arg0.dismiss();
						}
					})
					.setOnCancelListener(new AlertDialog.OnCancelListener() {
						@Override
						public void onCancel(DialogInterface arg0) {
							arg0.dismiss();
						}
					})
					.setCancelable(false).create().show();
				}
			});
		}
	}
	
	public String getDeviceUUID(final Context context) {
	    final String id = SMBPreferenceUtil.getPreferences(context, "device_id");
	 
	    UUID uuid = null;
	    if (id != "") {
	        uuid = UUID.fromString(id);
	    } else {
	        final String androidId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
	        try {
	            if (!"9774d56d682e549c".equals(androidId)) {
	                uuid = UUID.nameUUIDFromBytes(androidId.getBytes("utf8"));
	            } else {
	                final String deviceId = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
	                uuid = deviceId != null ? UUID.nameUUIDFromBytes(deviceId.getBytes("utf8")) : UUID.randomUUID();
	            }
	        } catch (UnsupportedEncodingException e) {
	            throw new RuntimeException(e);
	        }
	 
	        try {
				SMBPreferenceUtil.savePreferences(context, "device_id", uuid.toString());
	        } catch (NullPointerException e) {
				Log.e("asdd", Tag + " 672 === NullPointerException occured ");
			} catch (Exception e) {
				Log.e("asdd", Tag + " 674 === Exception occured ");
			}
	    }
	 
	    return encrypt(uuid.toString(), "horsepia");
	}
	
	public String encrypt(String str, String pw) {
		Log.e("asdd", Tag + " 687 ========== " + str + " : " + pw);
		byte[] bytes = pw.getBytes();
		String enc = null;
		try {
			SecureRandom sr =SecureRandom.getInstance("SHA1PRNG", "Crypto");
			sr.setSeed(bytes);
			KeyGenerator kgen = KeyGenerator.getInstance("AES");
			kgen.init(128, sr);
			
			SecretKey skey = kgen.generateKey();
			SecretKeySpec skeySpec = new SecretKeySpec(skey.getEncoded(), "AES");
			Cipher c = Cipher.getInstance("AES");
			c.init(Cipher.ENCRYPT_MODE, skeySpec);
			
			byte[] encrypted = c.doFinal(str.getBytes());
			Log.e("asdd", Tag + " 702 ========== " + encrypted);
			enc = toHexString(encrypted);
			//enc = Hex.encodeHexString(encrypted);
			Log.e("asdd", Tag + " 704 ========== " + enc);
		} catch (NoSuchAlgorithmException e) {
			Log.e("asdd", Tag + " 707 === NoSuchAlgorithmException occured ");
		} catch (NoSuchProviderException e) {
			Log.e("asdd", Tag + " 709 === NoSuchProviderException occured ");
		} catch (NoSuchPaddingException e) {
			Log.e("asdd", Tag + " 711 === NoSuchPaddingException occured ");
		} catch (InvalidKeyException e) {
			Log.e("asdd", Tag + " 713 === InvalidKeyException occured ");
		} catch (IllegalBlockSizeException e) {
			Log.e("asdd", Tag + " 715 === IllegalBlockSizeException occured ");
		} catch (BadPaddingException e) {
			Log.e("asdd", Tag + " 717 === BadPaddingException occured ");
		}
		return enc;
		
//		MessageDigest m = null;
//		String hash = null;
//		//String keyString = "horsepia";
//		byte[] keyBytes = toByteArray(toHexString(s));
//		
//		//Log.e("asdd", Tag + " 680 ========== " + s);
//		
//		try {
////			m = MessageDigest.getInstance("MD5");
////			m.update(s.getBytes(), 0, s.length());
////			hash = new BigInteger(1, m.digest()).toString(16);
//			SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
//			Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding");
//			c.init(Cipher.ENCRYPT_MODE, key);
//			hash = new String(Base64.encodeToString(toHexString(c.doFinal(s.getBytes())).getBytes(), 0));
//		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
//			// TODO Auto-generated catch block
//			System.out.println("예외 발생");
//			Log.e("asdd", Tag + " 686 ========== " + e.toString());
//		}
//		
//		Log.e("asdd", Tag + " 688 ========== " + hash);
//		return hash;
	}
	
	public String decrypt(String str, String pw) {
		Log.e("asdd", Tag + " 747 ========== " + str + " : " + pw);
		byte[] bytes = pw.getBytes();
		SecureRandom sr;
		String dec = null;
		try {
			sr = SecureRandom.getInstance("SHA1PRNG", "Crypto");
			sr.setSeed(bytes);
			KeyGenerator kgen = KeyGenerator.getInstance("AES");
			kgen.init(128, sr);
			
			SecretKey skey = kgen.generateKey();
			SecretKeySpec skeySpec = new SecretKeySpec(skey.getEncoded(), "AES");
			
			Cipher c = Cipher.getInstance("AES");
			c.init(Cipher.DECRYPT_MODE, skeySpec);
			byte[] decrypted = c.doFinal(Hex.decodeHex(str.toCharArray()));
			Log.e("asdd", Tag + " 763 ========== " + decrypted);
			dec = new String(decrypted);
			Log.e("asdd", Tag + " 765 ========== " + dec);
		} catch (NoSuchAlgorithmException e) {
			Log.e("asdd", Tag + " 767 === NoSuchAlgorithmException occured ");
		} catch (NoSuchProviderException e) {
			Log.e("asdd", Tag + " 769 === NoSuchProviderException occured ");
		} catch (NoSuchPaddingException e) {
			Log.e("asdd", Tag + " 771 === NoSuchPaddingException occured ");
		} catch (InvalidKeyException e) {
			Log.e("asdd", Tag + " 773 === InvalidKeyException occured ");
		} catch (IllegalBlockSizeException e) {
			Log.e("asdd", Tag + " 775 === IllegalBlockSizeException occured ");
		} catch (BadPaddingException e) {
			Log.e("asdd", Tag + " 777 === BadPaddingException occured ");
		} catch (DecoderException e) {
			Log.e("asdd", Tag + " 779 === DecoderException occured ");
		}
		return dec;
		
//		String hash = null;
//		//String keyString = "horsepiahorsepia";
//		byte[] keyBytes = toByteArray(toHexString(enc));
//		
//		Log.e("asdd", Tag + " 710 ========== " + enc);
//		
//		try {
//			String base64 = new String(Base64.decode(enc, 0));
//			SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
//			Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding");
//			c.init(Cipher.DECRYPT_MODE, key);
//			hash = new String(c.doFinal(toByteArray(base64)));
//		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
//			System.out.println(" decrypt 예외 발생");
//			Log.e("asdd", Tag + " 714 ========== " + e.toString());
//		}
//		
//		Log.e("asdd", Tag + " 717 ========== " + hash);
//		return hash;
	}
	
	public static String getPhoneNumber(Context context) {
		TelephonyManager telephone = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		String mPhoneNumber = telephone.getLine1Number();
		try {
			if (mPhoneNumber != null) {
				mPhoneNumber = mPhoneNumber.substring(mPhoneNumber.length() - 10, mPhoneNumber.length());
				mPhoneNumber = "0" + mPhoneNumber;
			} else {
				mPhoneNumber = "";
			}
		} catch (NullPointerException e) {
			Log.e("asdd", Tag + " 739 === NullPointerException occured ");
		}
		Log.e("asdd", Tag + " 607 === mPhoneNumber = " + mPhoneNumber);
		return mPhoneNumber;
	}
	
	public static void deviceCertPMS(final Context context) {
		Log.e("asdd", Tag + " 616 === deviceCertPMS()");
		/*
		//JSONObject json = new JSONObject();
		JSONObject userData = new JSONObject();
		try {
			//json.put("phoneNumber", CommonUtil.getPhoneNumber(context));
			//json.put("data1", PhoneState.getGlobalDeviceToken(context));
			userData.put("phoneNumber", getPhoneNumber(context));
			
			String uuid = "";
			
			uuid = PhoneState.getGlobalDeviceToken(context);
			Log.d("asdd", Tag + "uuid : " + uuid);
			
			userData.put("data1", uuid);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			System.out.println("예외 발생");
		}
		
		new DeviceCert(context).request(userData, new APICallback() {
			@Override
			public void response (String code, JSONObject json) {
				Log.e("asdd", Tag + " 629 === PUSH DeviceCert() = " + code);
				Log.e("asdd", Tag + " 630 === PUSH DeviceCert() = " + json);
				
				pushSetMsgKind(context, "Y", "99");
				pushSetMsgKind(context, "Y", "00");
				pushSetMsgKind(context, "Y", "01");
				pushSetMsgKind(context, "Y", "02");
				pushSetMsgKind(context, "Y", "03");
				pushSetMsgKind(context, "Y", "04");
			}
		});
		*/
	}
	
	public static void loginPMS(Context context, String id) {
		/*
		Log.e("asdd", Tag + " 636 === loginPMS()");
		JSONObject json = new JSONObject();
		try {
			json.put("custName", SMBPreferenceUtil.getPreferences(context, "loginNm"));
			json.put("phoneNumber", CommonUtil.getPhoneNumber(context));
			json.put("data1", PhoneState.getGlobalDeviceToken(context));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			System.out.println("예외 발생");
		}
		
		new LoginPms(context).request(id, json,  new APICallback() {
		    public void response(String code, JSONObject json) {
		    	Log.e("asdd", Tag + " 649 === PUSH LoginPms() = " + code);
				Log.e("asdd", Tag + " 650 === PUSH LoginPms() = " + json);
		    }
		});
		*/
	}
	
	public static void logoutPMS(Context context) {
		/*
		new LogoutPms(context).request(new APICallback() {
		    @Override
		    public void response (String code, JSONObject json) {
		    	Log.e("asdd", Tag + " 659 === PUSH LogoutPms() = " + code);
				Log.e("asdd", Tag + " 660 === PUSH LogoutPms() = " + json);
		    }
		});
		PMS.clear();
		*/
	}
	
	public static void newMsgPMS(Context context) {
		/*
		new NewMsg(context).request("P", "-1", "-1", "1", "50", new APICallback() {
		    public void response(String code, JSONObject json) {
		    	Log.e("asdd", Tag + " 669 === PUSH LogoutPms() = " + code);
				Log.e("asdd", Tag + " 670 === PUSH LogoutPms() = " + json);
		    }
		});
		new NewMsg(context).request("N", PMS.getInstance(context).getMaxUserMsgId(), "-1", "1", "50", new APICallback() {
		    public void response(String code, JSONObject json) {
		    	Log.e("asdd", Tag + " 675 === PUSH LogoutPms() = " + code);
				Log.e("asdd", Tag + " 676 === PUSH LogoutPms() = " + json);
		    }
		});
		*/
	}
	
	public static void pushSetMsgKind(Context context, String msgFlag, String kind) {
		/*
		Log.d("asdd",  Tag + "pushSetConfig msgFlag : " + msgFlag);
		Log.d("asdd",  Tag + "pushSetConfig kind : " + kind);

		new SetMsgKind(context).request(msgFlag, kind, new APICallback() {
			@Override
			public void response (String arg0, JSONObject arg1) {
				Log.d("asdd", Tag + "Push SetMsgKind : " + arg1.toString());
			}
		});
		*/
	}
	
//	public static void checkMsgPMS(Context context) {
//		new SetMsgKind(context).request(null, null, new APICallback() {
//		    @Override
//		    public void response (String arg0, JSONObject arg1) {
//		    	Log.e("asdd", Tag + " 685 === PUSH checkMsgPMS() = " + arg0);
//				Log.e("asdd", Tag + " 686 === PUSH checkMsgPMS() = " + arg1);
//				//pushCheck = arg1.getJSONArray("data").getJSONObject(0).getString("00");
//		    }
//		});
//	}
	
//	public static void setMsgPMS(final Context context, final String m00Flag, final String m11Flag) {
//		new SetMsgKind(context).request(m00Flag, "00", new APICallback() {
//		    @Override
//		    public void response (String arg0, JSONObject arg1) {
//		    	Log.e("asdd", Tag + " 696 === PUSH setMsgPMS() 00 = " + m00Flag);
//				Log.e("asdd", Tag + " 697 === PUSH setMsgPMS() 00 = " + arg1);
//		    }
//		});
//		new SetMsgKind(context).request(m11Flag, "11", new APICallback() {
//		    @Override
//		    public void response (String arg0, JSONObject arg1) {
//		    	Log.e("asdd", Tag + " 703 === PUSH setMsgPMS() 11 = " + m11Flag);
//				Log.e("asdd", Tag + " 704 === PUSH setMsgPMS() 11 = " + arg1);
//		    }
//		});
//	}
	
	public static void setConfig(final Context context, final String msgFlag, final String notiFlag) {
		/*
		new SetConfig(context).request(msgFlag, notiFlag, new APICallback() {
		    @Override
		    public void response (String code, JSONObject json) {
		    	Log.e("asdd", Tag + " 713 === PUSH SetConfig() = " + code);
				Log.e("asdd", Tag + " 714 === PUSH SetConfig() = " + json);
				if (!code.equals("000")) {
					new SetConfig(context).request(msgFlag, notiFlag, new APICallback() {
					    @Override
					    public void response (String code, JSONObject json) {
					    	Log.e("asdd", Tag + " 719 === PUSH SetConfig() 2 = " + code);
							Log.e("asdd", Tag + " 720 === PUSH SetConfig() 2 = " + json);
					    }
					});
				}
		    }
		});
		*/
	}
	
	public static String getStringFromInputStream(InputStream is) {
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {

            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        } catch (IOException e) {
            System.out.println("예외 발생");
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    System.out.println("예외 발생");
                }
            }
        }
        return sb.toString();
    }
	
	public static JSONArray getUserContentInfo(final Context context) {
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				
//				if (!SMBPreferenceUtil.getPreferences(context, "isLogin").equals("true")) {
//					Log.e("asdd", "799 ======= isLogin false");
//					return;
//				}
				
				try {
					URL mUrl = new URL(CommonUtil.SERVER + context.getResources().getString(R.string.url_user_content));
					HttpURLConnection mConnection = (HttpURLConnection) mUrl.openConnection();
					mConnection.setDoInput(true);
					mConnection.setDoOutput(true);
			
					int resCode = mConnection.getResponseCode();
					if (resCode == HttpURLConnection.HTTP_OK) {
						InputStream in = new BufferedInputStream(mConnection.getInputStream());
						json = new JSONArray(getStringFromInputStream(in));
						Log.e("asdd", "814 ======= json = " + json);
					}
				} catch (JSONException | NotFoundException | IOException e) {
					Log.e("asdd", Tag + " 819 === exception = " + e.toString());
					System.out.println("예외 발생");
				}
				
			}
		}).start();
		
		try {Thread.sleep(300);} catch(InterruptedException e){System.out.println("예외 발생");}
		
		try {
			for(int i=0; i<json.length(); i++) {
				Log.e("asdd", "829 ======= json = " + json.getJSONObject(i));
			}
		} catch (NullPointerException | JSONException e) {
			System.out.println("예외 발생");
		}
		Log.e("asdd", "836 ======= return json = " + json);
		return json;
	}
	
	public void checkLocationService(final Context context) {
		LocationManager lm = null;
		lm = (LocationManager) context.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
		
		boolean isEnableNetwork = false;
		try {
			isEnableNetwork = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		} catch (NullPointerException ex) {
			Log.e("asdd", Tag + " 936 === NullPointerException occured ");
		}
		
		Log.e("asdd", Tag + " 228 checkLocationService mPositionDlg " + mPositionDlg);
		
		if (mPositionDlg != null) {
			Log.d("asdd", Tag + "checkLocationService mPositionDlg.isShowing() " + mPositionDlg.isShowing());
			if (mPositionDlg.isShowing()) {
				return;
			}
		}
		
		Resources res = context.getApplicationContext().getResources();
		
		if (!isEnableNetwork) {
			if (mPositionDlg != null) {
				mPositionDlg.dismiss();
				mPositionDlg = null;
			}
			AlertDialog.Builder dialog = new AlertDialog.Builder(context);
			dialog.setMessage(res.getString(R.string.network_not_enabled));
			dialog.setPositiveButton(res.getString(R.string.open_location_settings), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
					context.startActivity(intent);
				}
			});
			dialog.setNegativeButton(res.getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					Toast.makeText(context, "위치정보가 활성화되지 않았습니다.", Toast.LENGTH_LONG).show();
				}
			});
			dialog.setOnDismissListener(new OnDismissListener() {
				public void onDismiss(DialogInterface arg0) {
					mPositionDlg = null;
				}
			});
			
			if(dialog != null)	{
				mPositionDlg = dialog.show();
			}
		}
		
	}
	
	public static byte[] toByteArray(String hexString) {
		int len = hexString.length()/2;
		byte[] result = new byte[len];
		for(int i=0; i<len; i++) {
			result[i] = Integer.valueOf(hexString.substring(2*i, 2*i+2), 16).byteValue();
		}
		return result;
	}
	
	public static String toHexString(byte[] buf) {
		if (buf == null) {
			return "";
		}
		StringBuffer result = new StringBuffer(2*buf.length);
		for(int i=0; i<buf.length; i++) {
			appendHex(result, buf[i]);
		}
		return result.toString();
	}
	
	private final static String HEX = "0123456789abcdef";
	private static void appendHex(StringBuffer sb, byte b) {
		sb.append(HEX.charAt((b>>4)&0x0f)).append(HEX.charAt(b&0x0f));
	}
	
	public static String toHexString(String txt) {
		return toHexString(txt.getBytes());
	}
}
