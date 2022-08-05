package com.example.bh_einkaufsliste;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.lights.LightState;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> liste = new ArrayList<String>();
    SharedPreferences sharedPreferences;
    ArrayAdapter<String> adapter;
    ListView myListView;
    EditText eingabeArtikel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myListView = (ListView) findViewById(R.id.einkaufsListe);

        // lade Liste aus Cash
        sharedPreferences = getPreferences(MODE_PRIVATE);
        String saveList = sharedPreferences.getString("LISTE", "");
        System.out.println(saveList);
        if (saveList != "") loadData();

        eingabeArtikel = findViewById(R.id.artikel);
        eingabeArtikel.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN && keyCode == keyEvent.KEYCODE_ENTER)
                {
                    onSpeichernClick(view);
                    return true;
                }
                return false;
            }
        });

        adapter=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, liste)
        {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
            {
                // Get the current Item for ListView
                TextView view = (TextView)super.getView(position, convertView, parent);
//                if (position > 0 && position %2 == 1)
                if (view.getText().toString().startsWith("*"))
                {
                    // Set dark background
                    view.setBackgroundColor(Color.LTGRAY);
                    view.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
                }
                else
                {
                    view.setBackgroundColor(Color.WHITE);
                    view.setPaintFlags(0);
                }
                return view;
            }
        };

        myListView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        // Instanziieren der anonymen Implementierung des OnItemClickListener-Interfaces
        AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // innerhalb dieser Methode kann auf den Item-Klick reagiert werden
                String pos = String.valueOf(position);
                TextView text = (TextView) view;

                System.out.println(position + " / " + text.getPaintFlags() + " / " + text.getDrawingCacheBackgroundColor()
                                            + " / " + Paint.STRIKE_THRU_TEXT_FLAG
                                            + " / " + text.getText().toString().substring(0, 0) + "|"
                                            + " / " + text.getText().toString().substring(1) + "|");

//                if ((text.getPaintFlags() & Paint.STRIKE_THRU_TEXT_FLAG) > 0)

                if (text.getText().toString().startsWith("*") ) {
                    text.setPaintFlags(0);
                    text.setBackgroundColor(Color.WHITE);
                    System.out.println("**** Get It! ******");
                    text.setText(text.getText().toString().substring(1));
                }
                else
                {
                    text.setBackgroundColor(Color.LTGRAY);
                    text.setText("*" + text.getText());
                    text.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
                }

                liste.set(position,text.getText().toString());
//                Toast.makeText(MainActivity.this, pos, Toast.LENGTH_SHORT).show();

                SharedPreferences.Editor editor = sharedPreferences.edit();
                String json = new Gson().toJson(liste);
                editor.putString("LISTE", json);
                editor.apply();
            }
        };

        myListView.setOnItemClickListener(onItemClickListener);

        AdapterView.OnItemLongClickListener onItemLongClickListener = new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // innerhalb dieser Methode kann auf den Item-Klick reagiert werden
                String pos = String.valueOf(position);
                TextView text = (TextView) view;

                liste.remove(position);
                adapter.notifyDataSetChanged();
                myListView.invalidateViews();
                myListView.refreshDrawableState();

                System.out.println(position + " / " + text.getPaintFlags() + " / " + text.getDrawingCacheBackgroundColor() +" / " + Paint.STRIKE_THRU_TEXT_FLAG);

//                Toast.makeText(MainActivity.this, "Eintrag gelöscht", Toast.LENGTH_SHORT).show();
                return true;
            }
        };
        myListView.setOnItemLongClickListener(onItemLongClickListener);

        // setze focus auf die mic-taste
        Button buttonSpeak = (Button)findViewById(R.id.sprechen);
        buttonSpeak.requestFocus();

    }

    public void onSprechenClick(View v)
    {
        hideSoftInput();

        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Bitte sprechen sie jetzt");

        startActivityForResult(i, 100);

    }

    public void onSpeichernClick(View v)
    {
        String eingabe = ((EditText)findViewById(R.id.artikel)).getText().toString();
        if ( eingabe.length() == 0) return;

        System.out.println("Aktuelle eingabe -> " + eingabe);
        liste.add(eingabe);
        myListView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        myListView.invalidateViews();
        myListView.refreshDrawableState();

        System.out.println(liste);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        String json = new Gson().toJson(liste);
        editor.putString("LISTE", json);
        editor.apply();

        ((EditText)findViewById(R.id.artikel)).setText("");
    }

    private void hideSoftInput() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
//        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK) {

            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            if (matches != null && matches.size() > 0) {
                String sentence = matches.get(0);
                ((EditText)findViewById(R.id.artikel)).setText(sentence);
                return;
            }
        }
    }


    private void loadData(){
        System.out.println("LESE  DATEN EIN ....");
        Toast.makeText(MainActivity.this, "Lese Liste...", Toast.LENGTH_LONG).show();
//        SharedPreferences sharedpreferences = getSharedPreferences("Shared Preferences", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("LISTE", null);
        System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        System.out.println(json);
        Type type = new TypeToken<ArrayList<String>>(){}.getType();
        liste = gson.fromJson(json, type);
        if(liste == null){
            liste = new ArrayList<String>();
        }
        System.out.println("---------------------------------------------------------------");
        System.out.println(liste);

    }

}