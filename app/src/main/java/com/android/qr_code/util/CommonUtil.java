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
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonReader;

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


public class CommonUtil {
	public static final String Tag = "CommonUtil";
	private AlertDialog mPositionDlg;

	Intent i;

	public static JSONArray json = null;



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



	public static void startAnimation(Context context, int id, View view, AnimationListener listener) {
		if (view == null)
			return;
		Animation ani = AnimationUtils.loadAnimation(context, id);
		ani.setAnimationListener(listener);
		view.startAnimation(ani);
		view.setVisibility(View.VISIBLE);
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
	    final String id;
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        id =  pref.getString("device_id", "");

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

	    }

	    return uuid.toString();
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


}
