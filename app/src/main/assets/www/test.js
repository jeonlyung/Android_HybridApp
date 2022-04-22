
function getVerion() {
  // android
  if (navigator.userAgent.toLowerCase().indexOf('android') > 0) {
    window.callbackHandler.appVersionInfo('callbackAppVersionInfo');
  }

  // iOS
  if (navigator.userAgent.toLowerCase().indexOf('ios') > 0) {
    window.webkit.messageHandlers.callbackHandler.postMessage({'appVersionInfo':'callbackAppVersionInfo'})
  }
}

function callbackAppVersionInfo(version) {
  alert(version);
}

function screenShot() {
  // android
  if (navigator.userAgent.toLowerCase().indexOf('android') > 0) {
    window.callbackHandler.snsShare();
  }

  // iOS
  if (navigator.userAgent.toLowerCase().indexOf('ios') > 0) {
    window.webkit.messageHandlers.callbackHandler.postMessage('snsShare')
  }
}

function snsShareSelected(selected) {
  // android
  if (navigator.userAgent.toLowerCase().indexOf('android') > 0) {
    window.callbackHandler.snsShareSelected(selected);
  }

  // iOS
  if (navigator.userAgent.toLowerCase().indexOf('ios') > 0) {
    window.webkit.messageHandlers.callbackHandler.postMessage({'snsShareSelected':selected});
  }
}

//ScanQR 호출 함수
function ScanQR() {
  //호스피아 앱에서 접속했는지 구분할 수 있는 값 : HORSEPIA_aOS
  //navigator.userAgent.toLowerCase().indexOf('HORSEPIA_aOS') > 0
  if (navigator.userAgent.toLowerCase().indexOf('android') > 0) {
      // Android
      console.log("ScanQR called");
      if(window.android){
        console.log("bridge success!");
        window.android.goScanQR(); //네이티브 goScanQR() 함수 호출 (Bridge "android")
      }else{
        console.log("bridge failed!");
      }
  }else if (navigator.userAgent.toLowerCase().indexOf('ios') > 0) {
      //iOS

  }else{
      //WEB
  }

}


//데이터 저장 함수
function setStringVal() {
    var idvalue = document.getElementById("idVal").value;
    var pwvalue = document.getElementById("pwVal").value;
    var idSaveCk = document.getElementById('idSaveCk').checked;
    var loginSaveCk = document.getElementById('loginSaveCk').checked;

    //아이디저장 체크
    if(idSaveCk){
        window.android.setSharedPreferencesString("userId",idvalue); //네이티브 setSharedPreferencesString(key,value) 함수 호출
    }else{
        window.android.setSharedPreferencesString("userId",""); //네이티브 setSharedPreferencesString(key,value) 함수 호출

    }

    //자동로그인 체크
    if(loginSaveCk){
        window.android.setSharedPreferencesString("userId",idvalue); //네이티브 setSharedPreferencesString(key,value) 함수 호출
        window.android.setSharedPreferencesString("userPw",pwvalue); //네이티브 setSharedPreferencesString(key,value) 함수 호출
    }else{
        window.android.setSharedPreferencesString("userId",""); //네이티브 setSharedPreferencesString(key,value) 함수 호출
        windw.android.setSharedPreferencesString("userPw",""); //네이티브 setSharedPreferencesString(key,value) 함수 호출
    }

    window.android.setSharedPreferencesString("idSaveCk",idSaveCk); //네이티브 setSharedPreferencesString(key,value) 함수 호출
    window.android.setSharedPreferencesString("loginSaveCk",loginSaveCk); //네이티브 setSharedPreferencesString(key,value) 함수 호출


    //데이터 초기화
    document.getElementById("idVal").value = "";
    document.getElementById("pwVal").value = "";

}

//데이터 불러오기 함수
function getStringVal() {
    var idVal = window.android.getSharedPreferencesString("userId"); //네이티브 setSharedPreferencesString(key) 호출시 value값 불러오
    var pwVal = window.android.getSharedPreferencesString("userPw");
    var idSaveCkVal = window.android.getSharedPreferencesString("idSaveCk");
    var loginSaveCkVal = window.android.getSharedPreferencesString("loginSaveCk");


    if(idSaveCkVal){
         document.getElementById("idVal").value = idVal;
        // document.getElementById('idSaveCk').checked = true;
    }else{
    }

    //자동로그인 체크
    if(loginSaveCkVal){
         document.getElementById("idVal").value = idVal;
         document.getElementById("pwVal").value = pwVal;
         //document.getElementById('loginSaveCk').checked = true;
     }

    document.getElementById("idVal").value = idVal;
    document.getElementById("pwVal").value = pwVal;

}

//SharedPreferences data 초기화
function cleartData(){
    window.android.clearSharedPreferencesData();
}

//ScanQR success callback함수
function SuccessScanQR(resultValue){
    alert("success : " +resultValue);
}

