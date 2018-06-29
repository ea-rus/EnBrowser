package com.example.andrew.bybrows2;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class LoadText extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_text);



         Button button_loadtextex=(Button) findViewById(R.id.button_loadtextex);

        final EditText editText_loadtext= (EditText) findViewById(R.id.editText_loadtext);

        editText_loadtext.setText("");

        button_loadtextex.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String text =  editText_loadtext.getText().toString();


                        //String htmlString= Html.toHtml(editText_loadtext.getText());

                        //Pattern pattern = Pattern.compile("[a-zA-Z]+", Pattern.CASE_INSENSITIVE);
                        String outHTML = makeHTML (getApplicationContext(),  text, "");

                        MyActivity.myWebView.loadDataWithBaseURL("", outHTML, "text/html", "UTF-8", "");

                        //loadHighlightToWebView(getApplicationContext(),  text);
;
                        finish();
                    }
                }
        );



    }

    public  static  String makeHTML(Context context, String text, String flag){
        /*
        * flag: "" - make html, "UNKNOWN" - extract unknown
        * */

        String outHTML="<html><head>" +  highlight_style   +  "</head><body>";

        if (flag.equals("UNKNOWN")) {
            text = extractUnknown(context, text);
        }else{
            text = dictHighlight(context, text).replace("\n", "<br>");
        }
        outHTML = outHTML+ text;

        outHTML=outHTML+"</body></html>";

        return outHTML;

    }



    public static String  highlight_style= "<style>" +
            "body {background-color: #000000; color: #ffffff;}" +
            ".unknown { text-decoration: underline;}" +
            ".todo {color: #cccccc ; text-decoration: underline;}" +
            ".append {color: #cccccc ; }" +
            "</style>" ;



    public static String dictHighlight(Context context, String instr){

        SQLiteDatabase db;
        try {
            db = SQLiteDatabase.openDatabase(MyActivity.DB_PATH_MY, null, SQLiteDatabase.OPEN_READONLY);
        }catch (SQLiteException e){
            Toast toast = Toast.makeText(context, "base not found :" + MyActivity.DB_PATH_MY, Toast.LENGTH_SHORT);
            toast.show();
            return  "";
        }

        Pattern pattern = Pattern.compile("[a-zA-Z]+");

        StringBuffer output = new StringBuffer();
        Matcher matcher = pattern.matcher(instr);
        while (matcher.find()) {

            String word =  matcher.group();

            if (word.length()>2) {
                String wordcasei = word.toLowerCase();

                String[] args = {wordcasei};

                Cursor cursor = db.rawQuery("select state from words where eng =?", args);

                String out = "";
                cursor.moveToFirst();

                String state = "";
                if (cursor.getCount() > 0) {
                    state = cursor.getString(cursor.getColumnIndex("state"));
                }

                cursor.close();


                                /*
                                * status
                                *   N новый
                                *   Y известный
                                *   U неизвестный
                                *   T подучить
                                *   A добавлено, запрашивалось
                                * */

                if (state.equals("") || state.equals( "N")) {
                    word = "<span class='unknown' >" + word + "</span>";
                } else if (state.equals("T")) {
                    word = "<span class='todo' >" + word + "</span>";
                } else if (state.equals("A")) {
                    word = "<span class='append' >" + word + "</span>";
                }

            }
//
//                            rep =  String.format("%d %s POSINT_TAG",
//                                            matcher.group().length(),
//                                            matcher.group());

            matcher.appendReplacement(output, word);
        }
        matcher.appendTail(output);

        db.close();

        return output.toString();

    }


    public static String extractUnknown(Context context, String instr){

        SQLiteDatabase db;
        try {
            db = SQLiteDatabase.openDatabase(MyActivity.DB_PATH_MY, null, SQLiteDatabase.OPEN_READONLY);
        }catch (SQLiteException e){
            Toast toast = Toast.makeText(context, "base not found :" + MyActivity.DB_PATH_MY, Toast.LENGTH_SHORT);
            toast.show();
            return  "";
        }
        String stroutput = "";

        Pattern pattern = Pattern.compile("[a-zA-Z]+");

        Matcher matcher = pattern.matcher(instr);
        while (matcher.find()) {

            String word =  matcher.group();

            if (word.length()>2) {
                String wordcasei = word.toLowerCase();

                String[] args = {wordcasei};

                Cursor cursor = db.rawQuery("select state from words where eng =?", args);

                String out = "";
                cursor.moveToFirst();

                String state = "";
                if (cursor.getCount() > 0) {
                    state = cursor.getString(cursor.getColumnIndex("state"));
                }

                cursor.close();

                if (state.equals("") || state.equals( "N")) {
                    word = "<span class='unknown' >" + word + "</span><br>";
                    stroutput = stroutput + word;
                } else if (state.equals("T")) {
                    word = "<span>" + word + "</span><br>";
                    stroutput = stroutput + word;
                }
            }
        }
        db.close();
        return stroutput;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_load_text, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
