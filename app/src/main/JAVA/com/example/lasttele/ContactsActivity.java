package com.example.lasttele;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import java.util.ArrayList;
import java.util.List;

public class ContactsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_main);










        TextView textView2 = findViewById(R.id.textView2);


        // Get Python instance and module



        // Initialize Python environment
        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }

        Python py = Python.getInstance();
        PyObject pyObj = py.getModule("helloworld");

        PyObject result = pyObj.callAttr("get_chats");


        List<String> chats = new ArrayList<>();
        for (PyObject item : result.asList()) {
            chats.add(item.toString());
        }


        if (!chats.isEmpty()) {
            StringBuilder chatText = new StringBuilder();
            for (String chat : chats) {
                chatText.append(chat).append("\n\n");
            }
            textView2.setText(chatText.toString());
        } else {
            textView2.setText("No chats found.");
        }



        // Find the TextView and Button

        List<String> names = new ArrayList<>();
        names.add("contact1,");


        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        // Input string array
        String[] chatData = {
                "chatname: Olga, chat id: 1072804297",
                "chatname: Каждый день, chat id: -1002300320383",
                "chatname: moon, chat id: 1541937998",
                "chatname: Fighterbomber, chat id: -1001251217154",
                "chatname: The Right People Z, chat id: -1001597987792",
                "chatname: Давлат Журакулов, chat id: 948526732",
                "chatname: КБ, chat id: -1001135818819",
                "chatname: random content, chat id: -1001746961434",
                "chatname: damn pictures🥀, chat id: -1001529305487",
                "chatname: Золотая молодёжь фестиваля, chat id: -1002113865053",
                "chatname: ВАНТУЗ, chat id: -1001872761682",
                "chatname: STRATPOL, chat id: -1001424357819",
                "chatname: Adventures of foreigners in Russia chat/ Чат Приключения иностранцев в России, chat id: -1002104524790",
                "chatname: Художка | Рисование и референсы, chat id: -1001609514955",
                "chatname: 2025 ТВ- и Онлайн-журналистика Школа RT, chat id: -1002272002518",
                "chatname: Ваш Слон, chat id: -1001371006753",
                "chatname: Визы в Шенген (gofortravel.ru) - Chat, chat id: -1001994547612"
        };

        // List to store ContactList objects
        List<contactList> items = new ArrayList<>();

        // Process each string in the array
        for (String chat : chatData) {
            // Split the string to extract chatname and chat id
            String[] parts = chat.split(", chat id: ");
            String chatName = parts[0].replace("chatname: ", ""); // Extract chatname
            long chatId = Long.parseLong(parts[1]); // Extract chat id

            // Skip the entire entry if chat id is negative
            if (chatId < 0) {
                continue; // Skip this iteration
            }

            // Use R.drawable.individual for positive chat ids
            int imageResource = R.drawable.individual;

            // Create a ContactList object and add it to the list
            items.add(new contactList(chatName, imageResource, chatId));
        }
        // Set up button click listener

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new myadapter(getApplicationContext(), items));

        // Get Python instance and module
    }

}

