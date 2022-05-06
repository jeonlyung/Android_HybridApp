package com.android.qr_code;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;


//QR SCanner
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import android.Manifest;
import android.content.pm.PackageManager;


public class MainActivity extends Activity {

    public WebView webView;
    public WebView childWebView = null;

    public String baseUrl = "";

    //Activity Result Value
    public final static int SCANQR_PAGE = 49374;
    public final static int FILE_CHOOSE_PAGE = 1234;


    String Tag = " MainAct ";

    public CookieManager cookieManager;
    public Context mainContext;

    private SharedPreferences pref;
    //SharedPreferences 구분
    private final String sharedName = "userInfo";

    //로컬페이지 구분
    private final Boolean localPage = true;

    //파일 업로드 관련
    private ValueCallback mFilePathCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainContext = this;

        cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.acceptCookie();

        webInit();

    }

    // 앱 권한 체크 메소드
   public void checkAppPemission() {

        /*if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            Log.d("checkVerify() : ","if문 들어옴");

            //카메라 또는 저장공간 권한 획득 여부 확인
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.CAMERA)) {

                Toast.makeText(getApplicationContext(),"권한 관련 요청을 허용해 주셔야 카메라 캡처이미지 사용등의 서비스를 이용가능합니다.",Toast.LENGTH_SHORT).show();

            } else {
//                Log.d("checkVerify() : ","카메라 및 저장공간 권한 요청");
                // 카메라 및 저장공간 권한 요청
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.INTERNET, Manifest.permission.CAMERA,
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        }*/
    }


    //웹뷰 셋팅
    public void webInit(){
        //원격디버깅 테스트용 :
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView.setWebContentsDebuggingEnabled(true);
        }

        webView = (WebView) findViewById(R.id.webview);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAllowContentAccess(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setSupportZoom(true);

        //userAgent set
        String userAgent = webView.getSettings().getUserAgentString();
        webView.getSettings().setUserAgentString(userAgent + "QR-Code");

        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webView.clearHistory();
        webView.clearFormData();
        webView.clearCache(true);

        //javascript Bridge 연결
        webView.addJavascriptInterface(new AndroidBridge(), "android");

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                //return super.onJsAlert(view, url, message, result);
                //result.confirm();
                Log.e("asdd", Tag + " 337 === onJsAlert() = " + message);
                new AlertDialog.Builder(MainActivity.this).setTitle("").setMessage(message)
                        .setPositiveButton(android.R.string.ok, new AlertDialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                // TODO Auto-generated method stub
                                result.confirm();
                            }
                        })
                        .setOnCancelListener(new AlertDialog.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface arg0) {
                                result.cancel();
                            }
                        })
                        .setCancelable(false).create().show();
                return true;
            }

            @Override
            public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
                result.confirm();
                return true;
            }

            //웹페이지에서 input(type="file") 클릭시 이벤트 발생
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                Log.d("asdd", "***** onShowFileChooser()");
                //Callback 초기화
                //return super.onShowFileChooser(webView, filePathCallback, fileChooserParams);

                /* 파일 업로드 */
                if (mFilePathCallback != null) {
                    //파일을 한번 오픈했으면 mFilePathCallback 를 초기화를 해줘야함
                    // -- 그렇지 않으면 다시 파일 오픈 시 열리지 않는 경우 발생
                    mFilePathCallback.onReceiveValue(null);
                    mFilePathCallback = null;
                }
                mFilePathCallback = filePathCallback;

                //권한 체크
                if(true) {

                    //권한이 있으면 처리
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("*/*");  //모든 contentType 파일 표시
    //            intent.setType("image/*");  //contentType 이 image 인 파일만 표시
                    startActivityForResult(intent, FILE_CHOOSE_PAGE);
                    return true;
                } else {
                    //권한이 없으면 처리
                    return false;
                }

            }

            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
				Log.e("asdd", Tag + " 362 === onCreateWindow() = " + resultMsg.toString());
//				Log.e("asdd", Tag + " 363 === onCreateWindow() isDialog = " + isDialog);

                view.removeAllViews();
                //webView.setVisibility(View.GONE);
                childWebView = new WebView(MainActivity.this);
                childWebView.getSettings().setJavaScriptEnabled(true);
                childWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
                childWebView.setWebChromeClient(this);
                childWebView.setWebViewClient(new WebViewClient());
                childWebView.setLayoutParams(new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));

                webView.addView(childWebView);

                WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                transport.setWebView(childWebView);
                resultMsg.sendToTarget();
                Log.e("asdd", Tag + " 391 === onCreateWindow() = " + childWebView.getUrl());

                return true;
            }

            @Override
            public void onCloseWindow(WebView window) {
                webView.removeView(window);
            }

        });

        if(localPage){
            baseUrl = "file:///android_asset/www/test.html";
        }else{
            baseUrl = "https://www.naver.com";
        }
        webView.loadUrl(baseUrl);
    }




    //자바스크립트 연결(Bridge)
    private class AndroidBridge {
        //QR Scan javascript call
        @JavascriptInterface
        public void goScanQR() {
            Log.e("asdd", Tag + " 534 === Javascript call goScanQR()");
            new IntentIntegrator(MainActivity.this).initiateScan();
        }

        //setSharedPreferencesString(데이터 저장)
        @JavascriptInterface
        public void setSharedPreferencesString(final String key, final String value) {
            Log.e("asdd", Tag + " 535 === Javascript call setSharedPreferencesString(key, value)");

            pref = getSharedPreferences(sharedName, MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString(key, value);
            editor.commit();
        }

        //getSharedPreferencesString(데이터 불러오기)
        @JavascriptInterface
        public String getSharedPreferencesString(final String key) {
            Log.e("asdd", Tag + " 535 === Javascript call getSharedPreferencesString(key)");
            pref = getSharedPreferences(sharedName, MODE_PRIVATE);
            String result = pref.getString(key,"");
            return result;
        }

        //clearSharedPreferencesData(데이터 삭제)
        @JavascriptInterface
        public void clearSharedPreferencesData() {
            Log.e("asdd", Tag + " 535 === Javascript call clearSharedPreferencesData()");
            pref = getSharedPreferences(sharedName, MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.clear();
            editor.commit();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.e("asdd", Tag + " 876 === onPause()");
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.e("asdd", Tag + " 1124 === onDestroy()");
        super.onDestroy();
        try {
            cookieManager.removeSessionCookie();
        } catch (NullPointerException e) {
            Log.e("asdd", this.getClass() + " 1571 === NullPointerException occured ");
        } catch (Exception e) {
            Log.e("asdd", this.getClass() + " 1573 === Exception occured ");
        }

        webView.clearCache(true);
        webView.clearHistory();
        webView.clearFormData();
        webView.removeAllViews();
        webView.clearSslPreferences();
        webView.destroy();

        System.exit(0);
        //android.os.Process.killProcess(Process.myPid());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e("asdd", Tag + " 1036 === onActivityResult() requestCode = " + requestCode);
        Log.e("asdd", Tag + " 1037 === onActivityResult() resultCode = " + resultCode);


        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                //QR Scanner 인식
                case SCANQR_PAGE:
                    IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
                    if(result != null) {
                        if(result.getContents() == null) {
                            Toast.makeText(this, "스캔인식을 실패하였습니다. 다시 시도 해주세요.", Toast.LENGTH_LONG).show();

                        } else {
                            webView.loadUrl("javascript:SuccessScanQR('"+result.getContents()+"')");
                            //Toast.makeText(this, result.getContents(), Toast.LENGTH_LONG).show();
                            //Toast.makeText(this, "스캔이 완료 되었습니다.", Toast.LENGTH_LONG).show();

                        }
                    } else {
                        super.onActivityResult(requestCode, resultCode, data);
                    }
                    break;

                case FILE_CHOOSE_PAGE:
                    //fileChooser 로 파일 선택 후 onActivityResult 에서 결과를 받아 처리함
                    if(requestCode == FILE_CHOOSE_PAGE) {
                        //파일 선택 완료 했을 경우
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {// LOLLIPOP : 21(안드로이드 5)
                            mFilePathCallback.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));
                        }else{
                            mFilePathCallback.onReceiveValue(new Uri[]{data.getData()});
                        }
                        mFilePathCallback = null;
                    } else {
                        //cancel 했을 경우
                        if(mFilePathCallback != null) {
                            mFilePathCallback.onReceiveValue(null);
                            mFilePathCallback = null;
                        }
                    }
                    break;


                default:
                    break;


            }
        } else if (resultCode == RESULT_CANCELED) {

        }
    }
}