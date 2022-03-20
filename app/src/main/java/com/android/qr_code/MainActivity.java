package com.android.qr_code;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.DownloadListener;
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

public class MainActivity extends AppCompatActivity {

    public WebView webView;
    public WebView childWebView = null;

    String Tag = " MainAct ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //웹뷰 셋팅
        webInit();

    }


    public void webInit(){
        //원격디버깅 테스트용 :
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView.setWebContentsDebuggingEnabled(true);
        }

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setSupportMultipleWindows(true);

        //userAgent set
        String userAgent = webView.getSettings().getUserAgentString();
        webView.getSettings().setUserAgentString(userAgent + "QR-Code");

        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webView.clearHistory();
        webView.clearFormData();
        webView.clearCache(true);

        webView.setWebViewClient(new WebViewClientClass());
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


        Handler handler = new Handler(){
            public void handleMessage(Message msg) {
                Log.e("asdd", Tag + " 531 === handleMessage() = " + msg.what);
                switch (msg.what) {
                    case 1:
                        Log.e("asdd", Tag + " 534 === handleMessage() 1 = " + msg.obj.toString());
                        webView.loadUrl(msg.obj.toString());
                        break;

                    case 2:
                        Log.e("asdd", Tag + " 537 === handleMessage() 2 = " + msg.obj.toString());
                        webView.loadUrl(msg.obj.toString());
                        break;

                    case 3:
                        Log.e("asdd", Tag + " 538 === handleMessage() 3 = " + msg.obj.toString());
                        externalBrowser((String[])msg.obj);
                        break;

                    default:
                        break;
                }
            };
        };

        private void externalBrowser(final String[] params) {
            String openUrl = params[0];
            if(!"".equals(openUrl)) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(openUrl));
                startActivity(intent);
            }
        }

        private class AndroidBridge {

            @JavascriptInterface
            public String sendDevice() {
                Log.e("asdd", Tag + " 547 === Javascript call sendDevice() = ");
                return commonUtil.getDeviceUUID(mainContext);

            }

            @JavascriptInterface
            public void shareFacebook(String link) {
                Log.e("asdd", Tag + " 553 === Javascript call shareFacebook() = " + link);
//			webView.loadUrl("javascript:'function명'");
//			windows.android.shareFacebook(param);
//			commonUtil.shareFacebook(mainContext, "?url=http://www.samsunghospital.com&title=test123");
                commonUtil.shareFacebook(mainContext, link);
            };

            @JavascriptInterface
            public void shareTwitter(String link) {
                Log.e("asdd", Tag + " 562 === Javascript call shareTwitter() = " + link);
//			commonUtil.shareTwitter(mainContext, "?url=http://www.samsunghospital.com&title=test123");
                commonUtil.shareTwitter(mainContext, link);
            }

            @JavascriptInterface
            public void shareKakaoTalk(String link) {
                Log.e("asdd", Tag + " 569 === Javascript call shareKakaoTalk() = " + link);
//			commonUtil.shareKakaoTalk(mainContext, "http://www.samsunghospital.com/m/healthInfo/content/contenView.do?CONT_SRC_ID=32614&CONT_SRC=HOMEPAGE&CONT_ID=5293&CONT_CLS_CD=001024001001");
                commonUtil.shareKakaoTalk(mainContext, link);
            }

            @JavascriptInterface
            public void goPay(String url) {
                Log.e("asdd", Tag + " 576 === Javascript call goPay() = " + url);
                //webView.loadUrl("https://pay.kra.co.kr:452/pg/cnspayLiteRequest.jsp?payKey=Mit19wJOZJR4A7F501pH5el8gi1OQtGT");
                //webView.loadUrl(CommonUtil.SERVER + "app/main/index.do");
                Message msg = handler.obtainMessage(1, url);
                handler.sendMessage(msg);
            }



            @JavascriptInterface
            public void externalBrowser(String[] arry) {
                final String[] strArry = new String[10];
                strArry[0] = arry[0];

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Message msg = Message.obtain();
                        msg.what = 3;
                        msg.obj = strArry;
                        handler.sendMessage(msg);
                    }
                });
            }

            //QR Scan javascript call
            @JavascriptInterface
            public void goScanQR() {
                Log.e("asdd", Tag + " 534 === Javascript call goScanQR()");
                new IntentIntegrator(MainActivity.this).initiateScan();
            };

            //setSharedPreferencesString(데이터 저장)
            @JavascriptInterface
            public void setSharedPreferencesString(final String key, final String value) {
                Log.e("asdd", Tag + " 535 === Javascript call setSharedPreferencesString(key, value)");

                pref = getSharedPreferences(sharedName, MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putString(key, value);
                editor.commit();
            };

            //getSharedPreferencesString(데이터 불러오기)
            @JavascriptInterface
            public String getSharedPreferencesString(final String key) {
                Log.e("asdd", Tag + " 535 === Javascript call getSharedPreferencesString(key)");
                pref = getSharedPreferences(sharedName, MODE_PRIVATE);
                String result = pref.getString(key,"");
                return result;
            };

            //clearSharedPreferencesData(데이터 삭제)
            @JavascriptInterface
            public void clearSharedPreferencesData() {
                Log.e("asdd", Tag + " 535 === Javascript call clearSharedPreferencesData()");
                pref = getSharedPreferences(sharedName, MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.clear();
                editor.commit();
            };

            //테스트
            @JavascriptInterface
            public void SessionTest() {
                Log.e("asdd", Tag + " 535 === Javascript call clearSharedPreferencesData()");
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        webView.loadUrl("javascript:'scriptTest()'");
                    }
                });
            };

        }



    }
}