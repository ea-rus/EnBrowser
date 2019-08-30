package com.example.andrew.bybrows2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.Html;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import java.sql.Array;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java.util.Date;




public class MyActivity extends Activity {

    static String DB_PATH="/storage/emulated/0/_my/dictEN-RU.db";

    static String DB_PATH_MY="/storage/emulated/0/_my/top3k.db";

    static public WebView myWebView;
    private ProgressBar ProgressLoad;
    public CheckBox checkBox_translate;


    private LocationManager locationManager;

    public static final String PREFS_NAME = "TranslateBrowser";

    @Override
    public void onBackPressed() {
        if (myWebView.canGoBack()) {
            myWebView.goBack();
        } else {
            this.finish();
        }
    }

    private Activity linkActivity;



    public void alert(final String text, final ArrayList<String> wordForms) {


        LinearLayout parent = new LinearLayout(this);

        parent.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        parent.setOrientation(LinearLayout.VERTICAL);

//children of parent linearlayout

        TextView tv1 = new TextView(this);
        tv1.setText(Html.fromHtml(text));
        parent.addView(tv1);

        LinearLayout layout2 = new LinearLayout(this);

        layout2.setLayoutParams(new LinearLayout.LayoutParams (
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        layout2.setOrientation(LinearLayout.HORIZONTAL);
        parent.addView(layout2);



        final AlertDialog dialog =  new AlertDialog.Builder(this)
                .setView(parent)
                .setNegativeButton("As text",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();

                                myWebView.post(new Runnable() {
                                    public void run() {
                                        String HTML = LoadText.makeHTML(getApplicationContext(), text, "");
                                        myWebView.loadDataWithBaseURL("", HTML, "text/html", "UTF-8", "");
                                    }
                                });
                            }
                        }
                ).setPositiveButton("ОК",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        }
                ).show();

        for (int i=0; i<wordForms.size(); i++){
            final String word = wordForms.get(i);

            Button button1 = new Button(this);
            button1.setText(word);

            button1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.cancel();
                    translateAlert(word);
                }
            });

            layout2.addView(button1);

        }
    }

    public void translateAlert(final String word) {
        String msg = makeTranslate(word);

        ArrayList<String> wordForms = checkWordForms(word);

        if (! msg.equals("") || ! wordForms.isEmpty()) {
            alert(msg, wordForms);
        }else {
            Toast toast = Toast.makeText(getApplicationContext(),
                    "Translate not found: "+word, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    public ArrayList<String> checkWordForms(String word){

        ArrayList<String> founded = new ArrayList<String>();
        String word0;

        // check past form
        if (word.endsWith("ed")){
            word0 = word.substring(0, word.length()-1);
            if (wordExist(word0))  founded.add(word0);

            word0 = word.substring(0, word.length()-2);
            if (wordExist(word0)) founded.add(word0);
        }

        // check plural
        if (word.endsWith("es")) {
            word0 = word.substring(0, word.length() - 2);
            if (wordExist(word0))  founded.add(word0);
        }
        if (word.endsWith("s")) {
            word0 = word.substring(0, word.length()-1);
            if (wordExist(word0))  founded.add(word0);
        }

        // check gerund
        if (word.endsWith("ing")) {
            word0 = word.substring(0, word.length() - 3);
            if (wordExist(word0))  founded.add(word0);

            word0 = word.substring(0, word.length()-3) + 'e';
            if (wordExist(word0))  founded.add(word0);
        }

        return founded;
    }



    public Boolean wordExist(String word){


        SQLiteDatabase db;
        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
        }catch (SQLiteException e){
            Toast toast = Toast.makeText(getApplicationContext(),
                    "base not found :" + DB_PATH, Toast.LENGTH_SHORT);
            toast.show();
            return false;
        }

        String[] args = {};
        Cursor cursor = db.rawQuery("select caseword from dictionary where word = '"+word+"' ", args);

        int count = cursor.getCount();

        cursor.close();
        db.close();

        return count>0;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);

        // Save the state of the WebView
        myWebView.saveState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);

        // Restore the state of the WebView
        myWebView.restoreState(savedInstanceState);
    }


    public String makeTranslate(String word) {


        word=word.toLowerCase().trim();

        SQLiteDatabase db;
        try {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
        }catch (SQLiteException e){
            Toast toast = Toast.makeText(getApplicationContext(),
                    "base not found :" + DB_PATH, Toast.LENGTH_SHORT);
            toast.show();
            return "";
        }

        String[] args = {};
        Cursor cursor = db.rawQuery("select caseword, substr(descr,0,600) descr from dictionary where word = '"+word+"' ", args);

        String rus="";

        String eng="";

        String out = "";
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            rus = cursor.getString(cursor.getColumnIndex("descr"));
            eng=cursor.getString(cursor.getColumnIndex("caseword"));
            out += eng;
            out += ":<br> ";
            out += rus;
            out += "<br>";
            cursor.moveToNext();
        }
        cursor.close();
        db.close();

        if (out!=""){

            db = null;
            try {
                db = SQLiteDatabase.openDatabase(DB_PATH_MY, null, SQLiteDatabase.OPEN_READWRITE);
            }catch (SQLiteException e){
                Toast toast = Toast.makeText(getApplicationContext(), "base not found :" + MyActivity.DB_PATH_MY, Toast.LENGTH_SHORT);
                toast.show();
            }

            if (db!=null) {
                String[] args2 = {eng};

                cursor = db.rawQuery("select state, count, id from words where eng =?", args2);

                cursor.moveToFirst();


                if (cursor.getCount() == 0) {

                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    String stringTime = df.format(new Date(System.currentTimeMillis()));

                    String[] args3 = {eng, rus, stringTime};

                    cursor = db.rawQuery("insert into words (id, eng,rus,state, lastdate, count) " +
                            "values( (select max(id)+1 from words), ?, ?, 'A', ?, 1)", args3);
                    cursor.moveToFirst();
                }else{

                    Integer count = cursor.getInt(cursor.getColumnIndex("count"));
                    Integer id = cursor.getInt(cursor.getColumnIndex("id"));

                    if (count ==null) count=1;

                    count+=1;

                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    String stringTime = df.format(new Date(System.currentTimeMillis()));


                    String[] args3 = {stringTime, count.toString(), id.toString()};

                    cursor = db.rawQuery("update words set lastdate=?, count=? where id= ?", args3);
                    cursor.moveToFirst();

                    out="Count: "+count.toString() + "<br>" + out;
                }

                cursor.close();
                db.close();
            }
        }

        return out;

    }

    final int MENU_EXIT = 1;
    final int MENU_HIGHLIGHT = 2;
    final int MENU_TEXT = 3;

    private void showPopupMenu(View v) {
        PopupMenu popupMenu = new PopupMenu(this, v);
        popupMenu.inflate(R.menu.main_menu);

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {

                switch (item.getItemId()) {

                    case R.id.menu_exit:
                        linkActivity.finish();
                        return true;
                    case R.id.menu_text:
                        Intent intent = new Intent(MyActivity.this, LoadText.class);
                        startActivity(intent);
                        return true;
                    case R.id.menu_unknown:
                        myWebView.loadUrl("javascript:injectedObject.selectedToText( window.getSelection().toString(), 'UNKNOWN');");
                        return true;
                    case R.id.menu_highlight:
                        myWebView.loadUrl("javascript:injectedObject.selectedToText( window.getSelection().toString(), '');");
                        return true;
                    default:
                        return false;
                }
            }
        });

        popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {

            @Override
            public void onDismiss(PopupMenu menu) {
            }
        });
        popupMenu.show();
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_my);

        myWebView = (WebView) findViewById(R.id.webView);

        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(true);


        if (savedInstanceState != null) {
            myWebView.restoreState(savedInstanceState);
            return;
        }


        Button button_menu= (Button) findViewById(R.id.button_menu);



        button_menu.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showPopupMenu(v);
                    }
                }
        );

        linkActivity = this;

        myWebView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                EditText text_url = (EditText) findViewById(R.id.editText_url);


                //  UrlHistory[UrlHistory.length] = url;


                text_url.setText(url);
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {

                super.onPageFinished(view, url);
                // -> String javascript = "javascript:document.onselectionchange = function () {var t=window.getSelection().toString(); if (t!='') {  injectedObject.translate (t)} } ";

                String javascript = "javascript: document.addEventListener('click', function(e) {"
                        +"if( injectedObject.isEnable ()=='Y'){"
                        +"   e.preventDefault();"
                        +"    var target = e.target || e.srcElement;"
                        +"    var text;"
                        +"   if (target.tagName=='A'){"
                        +"       text = target.textContent || text.innerText;"
                        +"   }else{"
                        +"       var s = window.getSelection();"
                        +"       s.modify('extend','backward','word');"
                        +"       var b = s.toString();"
                        +"       s.modify('extend','forward','word');"
                        +"       var a = s.toString();"
                        +"       s.modify('move','forward','character');"
                        +"         text=b+a;"
                        +"     }"
                        +"      injectedObject.translate(text);"
                        +"   }"
                        +" }, false);";
                view.loadUrl(javascript);

                //no double
                //String z = UrlHistory.get( UrlHistory.size()-1);
//                if (!  UrlHistory.get( UrlHistory.size()-1).equals(url)){
//                    UrlHistory.add(url);
//                }
//                Log.d("x", UrlHistory.toString());



                if (scrollPosition!=null) {
                    view.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            myWebView.scrollTo(0, scrollPosition);
                            scrollPosition=null;
                        }
                        // Delay the scrollTo to make it work
                    }, 300);


                }
            }
        });
        myWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                if (progress < 100 && ProgressLoad.getVisibility() == ProgressBar.GONE) {
                    ProgressLoad.setVisibility(ProgressBar.VISIBLE);
                }
                ProgressLoad.setProgress(progress);
                if (progress == 100) {
                    ProgressLoad.setVisibility(ProgressBar.GONE);

                }
            }
        });

        Button button_go = (Button) findViewById(R.id.button_go);
//        Button button_exit = (Button) findViewById(R.id.button_exit);
        EditText text_url = (EditText) findViewById(R.id.editText_url);

        checkBox_translate = (CheckBox) findViewById(R.id.checkBox_translate);

        ProgressLoad = (ProgressBar) findViewById(R.id.progressLoad);

        ProgressLoad.setVisibility(View.GONE);

        class JsObject {
            @JavascriptInterface
            public void translate(String word) {
                    translateAlert(word);
            }

            @JavascriptInterface
            public String isEnable() {
                if (checkBox_translate.isChecked()){
                    return "Y";
                }
                return "N";
            }

            @JavascriptInterface
            public void selectedToText(String str, String flag) {


                if (str.equals("") || str ==null){
                    Toast.makeText(getApplicationContext(), "Empty selection", Toast.LENGTH_SHORT).show();
                }else {
                    final String HTML = LoadText.makeHTML(getApplicationContext(), str, flag);

                    myWebView.post(new Runnable() {
                        public void run() {
                            myWebView.loadDataWithBaseURL("", HTML, "text/html", "UTF-8", "");
                        }
                    });



                }
            }

        }
        myWebView.addJavascriptInterface(new JsObject(), "injectedObject");

        text_url.setMaxLines(1);

        button_go.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        GoToUrl();


                    }

                }

        );

        text_url.setOnKeyListener(
                new View.OnKeyListener() {
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        if (event.getAction() == KeyEvent.ACTION_DOWN
                                && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                            GoToUrl();
                            return true;
                        }
                        return false;
                    }
                    //  return false;
                }
        );

        String url = null;

        Intent intent = getIntent();
        if (intent.getAction() == "android.intent.action.SEND") {
            ClipData intentData = intent.getClipData();
            if (intentData.getItemCount() > 0) {
                is_intent=true;
                url = intentData.getItemAt(0).getText().toString();

            }
        }else if (intent.getAction() == "android.intent.action.VIEW"){

            url =intent.getData().toString();
        }

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        if (url == null) {
            url = settings.getString("lastUrl", null);
        }
        if (url != null) {
            scrollPosition = settings.getInt("pos:" + url, 0);
        }


        if (url != null && savedInstanceState == null) {
            GoToUrl(url);
        }
    }

    private  boolean is_intent = false;

    private Integer scrollPosition = null;

    private ArrayList<String> UrlHistory;


    protected void GoToUrl(String url) {
        EditText text_url = (EditText) findViewById(R.id.editText_url);
        text_url.setText(url);
        GoToUrl();
    }


    protected void GoToUrl() {
        EditText text_url = (EditText) findViewById(R.id.editText_url);
        String url = text_url.getText().toString();

        if (!url.startsWith("http") && !url.startsWith("file")) {

            url = "http://" + url;
            text_url.setText(url);

        }

        myWebView.loadUrl(url);
    }

    @Override
    protected void onStop() {
        super.onStop();


        if (!is_intent) {
            EditText text_url = (EditText) findViewById(R.id.editText_url);
            SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            String url = text_url.getText().toString();
            editor.putString("lastUrl", url);
            editor.putInt("pos:" + url, myWebView.getScrollY());
            editor.commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}

