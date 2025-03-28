package com.moon.TeleHeart.result;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.moon.TeleHeart.shareresults.CardSelectionDialog;
import com.moon.TeleHeart.shareresults.PartialScrollViewHandler;
import com.moon.TeleHeart.R;
import com.moon.TeleHeart.shareresults.ScrollViewHandler;
import com.moon.TeleHeart.database.DatabaseHelper;
import com.moon.TeleHeart.firstscreen.Firstscreen;
import com.yandex.mobile.ads.banner.BannerAdEventListener;
import com.yandex.mobile.ads.banner.BannerAdSize;
import com.yandex.mobile.ads.banner.BannerAdView;
import com.yandex.mobile.ads.common.AdRequest;
import com.yandex.mobile.ads.common.AdRequestError;
import com.yandex.mobile.ads.common.ImpressionData;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResultsActivity extends AppCompatActivity {
    // MUST BE FIRST LINE

    // TextViews to display results
    private TextView yourMessagesTextView, herMessagesTextView;
    private TextView yourAverageTimeTextView, herAverageTimeTextView;
    private TextView yourFavoriteEmojiTextView, herFavoriteEmojiTextView;
    private TextView yourMediaFilesTextView, herMediaFilesTextView;
    // Pie charts for number of links per user
    private PieChart yourLinksPieChart;
    private PieChart herLinksPieChart;
    private BannerAdView mBannerAd;
    // Line charts for messages per month
    private LineChart yourMessagesPerMonthLineChart;
    private LineChart herMessagesPerMonthLineChart;
    // Bar charts for days with the most messages
    private HorizontalBarChart yourDaysMostMessagesChart;
    private HorizontalBarChart herDaysMostMessagesChart;
    private TextView yourLast10DaysTextView, herLast10DaysTextView;

    // Bar charts for most used words
    private HorizontalBarChart yourMostUsedWordsChart;
    private HorizontalBarChart herMostUsedWordsChart;

    private boolean switcher;
    private Handler handler = new Handler(Looper.getMainLooper()); // Single Handler instance
    private Runnable disconnectRunnable;
    private int countdownTime = 3 * 60;

    // Bar charts for days of the week
    private HorizontalBarChart yourBarChart;
    private HorizontalBarChart herBarChart;

    // Line charts for messages per hour of the day
    private LineChart yourHourOfDayLineChart;
    private LineChart herHourOfDayLineChart;

    // Bar charts for most used emojis
    private HorizontalBarChart yourMostUsedEmojisChart;
    private HorizontalBarChart herMostUsedEmojisChart;


    private ScrollView scrollView;
    // Longest messages
    private TextView herlongestmesssage, yourlongestmesssage;
    // TextViews for Median Answering Time
    private TextView yourMedianAnsweringTimeTextView;
    private TextView herMedianAnsweringTimeTextView;

    // TextViews for Largest No Conversation Days
    private TextView yourLargestNoConversationDaysTextView;

    // TextViews for Conversation Starts
    private TextView yourConversationStartsTextView;
    private TextView herConversationStartsTextView;

    // TextViews for Unreplied Chats
    private TextView yourUnrepliedChatsTextView;
    private TextView herUnrepliedChatsTextView;

    // TextViews for Longest Conversations
    private TextView yourLongestConversationTextView;
    private TextView yourLargestCommunicationStreakTextView;
    private TextView yourLargestCommunicationStreakDatesTextView;

    // TextViews for most used phrases
    private TextView yourMostUsedPhrasesTextView;
    private TextView herMostUsedPhrasesTextView;

    // Pie chart for the interest meter
    private PieChart interestMeterChart; // Add this line

    private Button shareButton;
    private Button disconnect;

    private ScrollViewHandler scrollViewHandler;
    private PartialScrollViewHandler partialScrollViewHandler;

    private TextView yourAverageMessageLengthTextView,herAverageMessageLengthTextView,yourMedianMessageLengthTextView,herMedianMessageLengthTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setLocaleFromPreferences();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.results_screen);

        MobileAds.initialize(this, initializationStatus -> {
            // SDK initialized, now you can load ads
            loadBannerAd();
        });

        disconnect = findViewById(R.id.disconnect1);
        disconnect.setOnClickListener(this::ondisconnect);




        // Initialize the button
        // Initialize handlers
        scrollViewHandler = new ScrollViewHandler(this);
        partialScrollViewHandler = new PartialScrollViewHandler(this);

        // Get the share button
        shareButton = findViewById(R.id.shareButton);

        // Set the click listener
        shareButton.setOnClickListener(v -> showCardSelectionDialog());




        // Initialize TextViews
        yourMessagesTextView = findViewById(R.id.yourMessagesTextView);
        herMessagesTextView = findViewById(R.id.herMessagesTextView);
        yourAverageTimeTextView = findViewById(R.id.yourAverageTimeTextView);
        herAverageTimeTextView = findViewById(R.id.herAverageTimeTextView);
        yourFavoriteEmojiTextView = findViewById(R.id.yourFavoriteEmojiTextView);
        herFavoriteEmojiTextView = findViewById(R.id.herFavoriteEmojiTextView);
        yourMediaFilesTextView = findViewById(R.id.yourMediaFilesTextView);
        herMediaFilesTextView = findViewById(R.id.herMediaFilesTextView);

        // Initialize number of links pie charts
        yourLinksPieChart = findViewById(R.id.yourLinksPieChart);
        herLinksPieChart = findViewById(R.id.herLinksPieChart);

        // Initialize the interest meter chart
        interestMeterChart = findViewById(R.id.interestMeterChart); // Add this line

        // Initialize bar charts
        yourBarChart = findViewById(R.id.yourbarcharts);
        herBarChart = findViewById(R.id.herbarcharts);

        // Initialize line charts
        yourHourOfDayLineChart = findViewById(R.id.yourhourOfDayLineChart);
        herHourOfDayLineChart = findViewById(R.id.herhourOfDayLineChart);

        // Initialize most used emojis charts
        yourMostUsedEmojisChart = findViewById(R.id.yourMostUsedEmojisChart);
        herMostUsedEmojisChart = findViewById(R.id.herMostUsedEmojisChart);

        // Initialize messages per month line charts
        yourMessagesPerMonthLineChart = findViewById(R.id.yourMessagesPerMonthLineChart);
        herMessagesPerMonthLineChart = findViewById(R.id.herMessagesPerMonthLineChart);

        // Initialize days with the most messages charts
        yourDaysMostMessagesChart = findViewById(R.id.yourDaysMostMessagesChart);
        herDaysMostMessagesChart = findViewById(R.id.herDaysMostMessagesChart);

        yourLast10DaysTextView = findViewById(R.id.yourLast10DaysTextView);
        herLast10DaysTextView = findViewById(R.id.herLast10DaysTextView);

        // Initialize most used words charts
        yourMostUsedWordsChart = findViewById(R.id.yourMostUsedWordsChart);
        herMostUsedWordsChart = findViewById(R.id.herMostUsedWordsChart);

        // Longest message
        herlongestmesssage = findViewById(R.id.herlongestmesssage);
        yourlongestmesssage = findViewById(R.id.yourlongestmesssage);

        // Median answer time
        yourMedianAnsweringTimeTextView = findViewById(R.id.yourMedianAnsweringTimeTextView);
        herMedianAnsweringTimeTextView = findViewById(R.id.herMedianAnsweringTimeTextView);
       // largest no conv days
        yourLargestNoConversationDaysTextView = findViewById(R.id.yourLargestNoConversationDaysTextView);
        TextView yourLargestNoConversationDatesTextView = findViewById(R.id.yourLargestNoConversationDatesTextView);
       // who starts convs
        yourConversationStartsTextView = findViewById(R.id.yourConversationStartsTextView);
        herConversationStartsTextView = findViewById(R.id.herConversationStartsTextView);

        yourUnrepliedChatsTextView = findViewById(R.id.yourUnrepliedChatsTextView);
        herUnrepliedChatsTextView = findViewById(R.id.herUnrepliedChatsTextView);

        yourLongestConversationTextView = findViewById(R.id.yourLongestConversationTextView);

        // largest conv streak
        // Initialize TextViews for largest communication streak
        TextView yourLargestCommunicationStreakTextView = findViewById(R.id.yourLargestCommunicationStreakTextView);
        TextView yourLargestCommunicationStreakDatesTextView = findViewById(R.id.yourLargestCommunicationStreakDatesTextView);

        // Initialize most used phrases TextViews
        yourMostUsedPhrasesTextView = findViewById(R.id.yourMostUsedPhrasesTextView);
        herMostUsedPhrasesTextView = findViewById(R.id.herMostUsedPhrasesTextView);

        TextView yourAverageMessageLengthTextView = findViewById(R.id.yourAverageMessageLengthTextView);
        TextView herAverageMessageLengthTextView = findViewById(R.id.herAverageMessageLengthTextView);
        TextView yourMedianMessageLengthTextView = findViewById(R.id.yourMedianMessageLengthTextView);
        TextView herMedianMessageLengthTextView = findViewById(R.id.herMedianMessageLengthTextView);



        // Retrieve messages from the database in a background thread
        new Thread(() -> {
            DatabaseHelper dbHelper = new DatabaseHelper(this);
            List<String> messages = dbHelper.getMessages();

            // Analyze the chat data
            ChatAnalyzer analyzer = new ChatAnalyzer(messages);

            // Get results
            Map<Long, Integer> messageCounts = analyzer.getNumberOfMessagesPerUser();
            Map<Long, Long> averageTimes = analyzer.getAverageTimeToAnswer();
            Map<Long, String> favoriteEmojis = analyzer.getFavoriteEmojiPerUser();
            Map<Long, Integer> mediaCounts = analyzer.getNumberOfMediaFilesPerUser();
            Map<Long, Map<String, Integer>> linkCounts = analyzer.getNumberOfLinksPerUser();
            Map<Long, Map<String, Integer>> dayOfWeekCounts = analyzer.getMessagesPerDayOfWeek();
            Map<Long, Map<Integer, Integer>> hourOfDayCounts = analyzer.getMessagesPerHourOfDay();
            Map<Long, Map<String, Integer>> monthCounts = analyzer.getMessagesPerMonth();
            Map<Long, List<Map.Entry<String, Integer>>> daysWithMostMessages = analyzer.getDaysWithMostMessages();
            Map<Long, Integer> last10DaysCounts = analyzer.getMessagesInLast10Days();
            Map<Long, Map<String, Integer>> mostUsedWords = analyzer.getMostUsedWords();
            Map<Long, Map<String, Integer>> mostUsedEmojis = analyzer.getMostUsedEmojis();
            Map<Long, String> longestMessages = analyzer.getLongestMessagePerUser();
            Map<Long, Long> medianTimes = analyzer.getMedianAnsweringTime();
            Map<Long, Map.Entry<Integer, Map.Entry<Date, Date>>> largestGaps = analyzer.getLargestNoConversationDays();
            Map<Long, Integer> conversationStarts = analyzer.getConversationStarts();
            Map<Long, Integer> unrepliedChats = analyzer.getUnrepliedChats();
            Map<Long, Long> longestConversations = analyzer.getLongestConversations();
            Map<Long, Map.Entry<String, Integer>> mostUsedPhrase = analyzer.getMostUsedPhrase(5);
            Map<Long, Double> averageMessageLengths = analyzer.getAverageMessageLength();
            Map<Long, Integer> medianMessageLengths = analyzer.getMedianMessageLength();

            // Largest Streak of Days with Communications
            Map<Long, Map.Entry<Integer, Map.Entry<Date, Date>>> largestStreaks = analyzer.getLargestCommunicationStreak();
            // Debugging outputs
            System.out.println("Message Counts: " + messageCounts);
            System.out.println("Average Times: " + averageTimes);
            System.out.println("Favorite Emojis: " + favoriteEmojis);
            System.out.println("Media Counts: " + mediaCounts);
            System.out.println("Link Counts: " + linkCounts);
            System.out.println("Day of Week Counts: " + dayOfWeekCounts);
            System.out.println("Hour of Day Counts: " + hourOfDayCounts);
            System.out.println("Month Counts: " + monthCounts);
            System.out.println("Days with Most Messages: " + daysWithMostMessages);
            System.out.println("Last 10 Days Counts: " + last10DaysCounts);
            System.out.println("Most Used Words: " + mostUsedWords);
            System.out.println("Most Used Emojis: " + mostUsedEmojis);
            System.out.println("Longest message: " + longestMessages);
            System.out.println("Median Answering Times: " + medianTimes);
            System.out.println("largestGapss: " + largestGaps);
            System.out.println("Conversation Starts: " + conversationStarts);
            System.out.println("Unreplied Chats: " + unrepliedChats);
            System.out.println("Longest Conversations: " + longestConversations);
            System.out.println(" most used words : "+  mostUsedPhrase);
            System.out.println("averege message length : "+ averageMessageLengths);
            System.out.println("median message length : "+ medianMessageLengths);
            System.out.println("largest streak : "+ largestStreaks);



            // Update the UI on the main thread
            new Handler(Looper.getMainLooper()).post(() -> {
                // Assuming chatId 1 is "You" and chatId 2 is "Her"
                Intent intent = getIntent();
                String selectedContactId = intent.getStringExtra("selectedContactId");
                switcher = intent.getBooleanExtra("switcher", false);
                System.out.println("switcher check inside of result : " + switcher);
                long selectedContactId2 = Long.parseLong(selectedContactId);

                long herChatId = selectedContactId2;

                String user_id = intent.getStringExtra("user_id");
                long longid = Long.parseLong(user_id);
                long yourChatId = longid;

                // Number of Messages
                yourMessagesTextView.setText( getString(R.string.You) +" " + messageCounts.getOrDefault(yourChatId, 0));
                herMessagesTextView.setText(getString(R.string.Them) +" "+ messageCounts.getOrDefault(herChatId, 0));

                // Average Time to Answer
                yourAverageTimeTextView.setText(getString(R.string.You)+ " "+ (averageTimes.getOrDefault(yourChatId, 0L) / 1000) + getString(R.string.s));
                herAverageTimeTextView.setText(getString(R.string.Them)+" "+ (averageTimes.getOrDefault(herChatId, 0L) / 1000) + getString(R.string.s));

                // Favorite Emoji
                yourFavoriteEmojiTextView.setText(getString(R.string.You) +" " +favoriteEmojis.getOrDefault(yourChatId, ""));
                herFavoriteEmojiTextView.setText(getString(R.string.Them) +" "+ favoriteEmojis.getOrDefault(herChatId, ""));

                // Number of Media Files
                yourMediaFilesTextView.setText(getString(R.string.You) +" "+ mediaCounts.getOrDefault(yourChatId, 0));
                herMediaFilesTextView.setText(getString(R.string.Them) +" "+ mediaCounts.getOrDefault(herChatId, 0));

                // Set up Number of Links per User pie charts
                setupLinksPieChart(yourLinksPieChart, linkCounts.getOrDefault(yourChatId, new HashMap<>()), getString(R.string.You));
                setupLinksPieChart(herLinksPieChart, linkCounts.getOrDefault(herChatId, new HashMap<>()), getString(R.string.Them));

                // Messages per Day of the Week
                setupBarChart(yourBarChart, dayOfWeekCounts.getOrDefault(yourChatId, new HashMap<>()), getString(R.string.You));
                setupBarChart(herBarChart, dayOfWeekCounts.getOrDefault(herChatId, new HashMap<>()), getString(R.string.Them));

                // Messages per Hour of the Day
                setupHourOfDayLineChart(yourHourOfDayLineChart, hourOfDayCounts.getOrDefault(yourChatId, new HashMap<>()), getString(R.string.You), Color.parseColor("#00d4ff"));
                setupHourOfDayLineChart(herHourOfDayLineChart, hourOfDayCounts.getOrDefault(herChatId, new HashMap<>()), getString(R.string.Them), Color.parseColor("#FFD700"));

                // Set up Messages per Month line charts
                setupMessagesPerMonthLineChart(yourMessagesPerMonthLineChart, monthCounts.getOrDefault(yourChatId, new HashMap<>()), getString(R.string.You),  Color.parseColor("#00d4ff"));
                setupMessagesPerMonthLineChart(herMessagesPerMonthLineChart, monthCounts.getOrDefault(herChatId, new HashMap<>()), getString(R.string.Them), Color.parseColor("#FFD700"));

                // Days with Most Messages
                setupDaysMostMessagesChart(yourDaysMostMessagesChart, daysWithMostMessages.getOrDefault(yourChatId, new ArrayList<>()), getString(R.string.You));
                setupDaysMostMessagesChart(herDaysMostMessagesChart, daysWithMostMessages.getOrDefault(herChatId, new ArrayList<>()), getString(R.string.Them));

                // Messages in Last 10 Days
                yourLast10DaysTextView.setText(getString(R.string.You)+ " " + last10DaysCounts.getOrDefault(yourChatId, 0));
                herLast10DaysTextView.setText(getString(R.string.Them) + " " +last10DaysCounts.getOrDefault(herChatId, 0));

                // Set up Most Used Words charts
                setupMostUsedWordsChart(yourMostUsedWordsChart, mostUsedWords.getOrDefault(yourChatId, new HashMap<>()), getString(R.string.You));
                setupMostUsedWordsChart(herMostUsedWordsChart, mostUsedWords.getOrDefault(herChatId, new HashMap<>()), getString(R.string.Them));

                // Longest message
                String yourLongestMessage = longestMessages.getOrDefault(yourChatId, "N/A");
                yourlongestmesssage.setText(getString(R.string.You) +" "+ yourLongestMessage.length() +" "+ getString(R.string.characters));

                String herLongestMessage = longestMessages.getOrDefault(herChatId, "N/A");
                herlongestmesssage.setText(getString(R.string.Them) +" "+ herLongestMessage.length() +" "+ getString(R.string.characters));

                // Set up Most Used Emojis charts
                setupMostUsedEmojisChart(yourMostUsedEmojisChart, mostUsedEmojis.getOrDefault(yourChatId, new HashMap<>()), getString(R.string.You));
                setupMostUsedEmojisChart(herMostUsedEmojisChart, mostUsedEmojis.getOrDefault(herChatId, new HashMap<>()), getString(R.string.Them));

                // Set click listeners for the CardViews
                CardView yourLongestMessageCard = findViewById(R.id.your_longest_message_card);
                CardView herLongestMessageCard = findViewById(R.id.her_longest_message_card);

                yourLongestMessageCard.setOnClickListener(v -> showOverlay(yourLongestMessage));
                herLongestMessageCard.setOnClickListener(v -> showOverlay(herLongestMessage));

                //median answer time
                setupMedianAnsweringTimeTextView(yourMedianAnsweringTimeTextView,  medianTimes.getOrDefault(yourChatId, 0L), getString(R.string.You));
                setupMedianAnsweringTimeTextView(herMedianAnsweringTimeTextView, medianTimes.getOrDefault(herChatId, 0L), getString(R.string.Them));
                //LargestNoConversationDays
                setupLargestNoConversationDaysTextView(
                        yourLargestNoConversationDaysTextView,
                        yourLargestNoConversationDatesTextView,
                        largestGaps.getOrDefault(yourChatId, null),
                        "You"
                );
               // conv started
                setupConversationStartsTextView(yourConversationStartsTextView, conversationStarts.getOrDefault(yourChatId, 0), getString(R.string.You));
                setupConversationStartsTextView(herConversationStartsTextView, conversationStarts.getOrDefault(herChatId, 0), getString(R.string.Them));

                setupUnrepliedChatsTextView(yourUnrepliedChatsTextView, unrepliedChats.getOrDefault(yourChatId, 0), getString(R.string.You));
                setupUnrepliedChatsTextView(herUnrepliedChatsTextView, unrepliedChats.getOrDefault(herChatId, 0), getString(R.string.Them));

                setupLongestConversationsTextView(yourLongestConversationTextView, longestConversations.getOrDefault(yourChatId, 0L), getString(R.string.You));

                setupLargestCommunicationStreakTextView(
                        yourLargestCommunicationStreakTextView,
                        yourLargestCommunicationStreakDatesTextView,
                        largestStreaks.getOrDefault(yourChatId, null),
                        getString(R.string.You)
                );
                yourMostUsedPhrasesTextView.setText( formatPhrase(mostUsedPhrase.getOrDefault(yourChatId, null)));
                herMostUsedPhrasesTextView.setText( formatPhrase(mostUsedPhrase.getOrDefault(herChatId, null)));

                yourAverageMessageLengthTextView.setText(getString(R.string.You) +" "+ String.format("%.2f", averageMessageLengths.getOrDefault(yourChatId, 0.0)) +" " +getString(R.string.characters));
                herAverageMessageLengthTextView.setText(getString(R.string.Them) +" " + String.format("%.2f", averageMessageLengths.getOrDefault(herChatId, 0.0)) + " "+getString(R.string.characters));
                yourMedianMessageLengthTextView.setText(getString(R.string.You) +" "+ medianMessageLengths.getOrDefault(yourChatId, 0) +" "+ getString(R.string.characters));
                herMedianMessageLengthTextView.setText(getString(R.string.Them)+" "+ medianMessageLengths.getOrDefault(herChatId, 0) +" "+ getString(R.string.characters));

                // Calculate max values dynamically
                Map<String, Float> maxValues = calculateMaxValues(
                        messageCounts, averageTimes, mediaCounts, conversationStarts, unrepliedChats,
                        last10DaysCounts, medianTimes, medianMessageLengths
                );

                // Calculate scores for both users with debug logs
                float yourScore = calculateUserScore(
                        messageCounts.getOrDefault(yourChatId, 0),
                        averageTimes.getOrDefault(yourChatId, 0L),
                        mediaCounts.getOrDefault(yourChatId, 0),
                        conversationStarts.getOrDefault(yourChatId, 0),
                        unrepliedChats.getOrDefault(yourChatId, 0),
                        last10DaysCounts.getOrDefault(yourChatId, 0),
                        medianTimes.getOrDefault(yourChatId, 0L),
                        medianMessageLengths.getOrDefault(yourChatId, 0),
                        maxValues
                );

                float theirScore = calculateUserScore(
                        messageCounts.getOrDefault(herChatId, 0),
                        averageTimes.getOrDefault(herChatId, 0L),
                        mediaCounts.getOrDefault(herChatId, 0),
                        conversationStarts.getOrDefault(herChatId, 0),
                        unrepliedChats.getOrDefault(herChatId, 0),
                        last10DaysCounts.getOrDefault(herChatId, 0),
                        medianTimes.getOrDefault(herChatId, 0L),
                        medianMessageLengths.getOrDefault(herChatId, 0),
                        maxValues
                );

                // Debug logs to verify values
                Log.d("InterestMeter", "Your Score: " + yourScore);
                Log.d("InterestMeter", "Their Score: " + theirScore);
                Log.d("InterestMeter", "Max Values: " + maxValues);

                // Update the chart with zero-score handling
                setupInterestMeterChart(interestMeterChart, yourScore, theirScore);


            });
        }).start();
    }



    // Helper method to format the phrase list
    private String formatPhrase(Map.Entry<String, Integer> phraseEntry) {
        if (phraseEntry == null) {
            return "No phrases found";
        }
        return phraseEntry.getKey() + " (" + phraseEntry.getValue() +" "+ getString(R.string.times)+ ")";
    }



    // Helper method to calculate the user's interest score
    // Helper method to calculate the user's interest score
    private float calculateUserScore(
            int messageCount, long averageTime, int mediaCount, int conversationStarts,
            int unrepliedChats, int last10DaysCounts, long medianAnswerTime, int medianMessageLength,
            Map<String, Float> maxValues
    ) {
        // Normalize each metric using dynamic max values
        float normalizedMessageCount = normalize(messageCount, 0, maxValues.get("messageCount"));
        float normalizedAverageTime = normalize(averageTime, 0, maxValues.get("averageTime"));
        float normalizedMediaCount = normalize(mediaCount, 0, maxValues.get("mediaCount"));
        float normalizedConversationStarts = normalize(conversationStarts, 0, maxValues.get("conversationStarts"));
        float normalizedUnrepliedChats = normalize(unrepliedChats, 0, maxValues.get("unrepliedChats"));
        float normalizedLast10DaysCounts = normalize(last10DaysCounts, 0, maxValues.get("last10DaysCounts"));
        float normalizedMedianAnswerTime = normalize(medianAnswerTime, 0, maxValues.get("medianAnswerTime"));
        float normalizedMedianMessageLength = normalize(medianMessageLength, 0, maxValues.get("medianMessageLength"));

        // Assign weights (adjust as needed)
        float weightMessageCount = 0.30f;
        float weightAverageTime = 0.15f;
        float weightMediaCount = 0.10f;
        float weightConversationStarts = 0.10f;
        float weightUnrepliedChats = 0.05f;
        float weightLast10DaysCounts = 0.10f;
        float weightMedianAnswerTime = 0.10f;
        float weightMedianMessageLength = 0.10f;

        // Calculate weighted score
        return (normalizedMessageCount * weightMessageCount) +
                (normalizedAverageTime * weightAverageTime) +
                (normalizedMediaCount * weightMediaCount) +
                (normalizedConversationStarts * weightConversationStarts) +
                (normalizedUnrepliedChats * weightUnrepliedChats) +
                (normalizedLast10DaysCounts * weightLast10DaysCounts) +
                (normalizedMedianAnswerTime * weightMedianAnswerTime) +
                (normalizedMedianMessageLength * weightMedianMessageLength);
    }

    // Helper method to normalize values
    private float normalize(float value, float min, float max) {
        if (max - min <= 0) return 0;
        float normalized = ((value - min) / (max - min)) * 100;
        return Math.max(0, Math.min(100, normalized)); // Clamp between 0-100
    }

    // Fixed max value calculation with minimum 1 protection
    private Map<String, Float> calculateMaxValues(
            Map<Long, Integer> messageCounts,
            Map<Long, Long> averageTimes,
            Map<Long, Integer> mediaCounts,
            Map<Long, Integer> conversationStarts,
            Map<Long, Integer> unrepliedChats,
            Map<Long, Integer> last10DaysCounts,
            Map<Long, Long> medianAnswerTimes,
            Map<Long, Integer> medianMessageLengths
    ) {
        Map<String, Float> maxValues = new HashMap<>();

        maxValues.put("messageCount", Math.max(1f, (float) messageCounts.values().stream()
                .mapToInt(Integer::intValue).max().orElse(0)));

        maxValues.put("averageTime", Math.max(1f, (float) averageTimes.values().stream()
                .mapToLong(Long::longValue).max().orElse(0L)));

        maxValues.put("mediaCount", Math.max(1f, (float) mediaCounts.values().stream()
                .mapToInt(Integer::intValue).max().orElse(0)));

        maxValues.put("conversationStarts", Math.max(1f, (float) conversationStarts.values().stream()
                .mapToInt(Integer::intValue).max().orElse(0)));

        maxValues.put("unrepliedChats", Math.max(1f, (float) unrepliedChats.values().stream()
                .mapToInt(Integer::intValue).max().orElse(0)));

        maxValues.put("last10DaysCounts", Math.max(1f, (float) last10DaysCounts.values().stream()
                .mapToInt(Integer::intValue).max().orElse(0)));

        maxValues.put("medianAnswerTime", Math.max(1f, (float) medianAnswerTimes.values().stream()
                .mapToLong(Long::longValue).max().orElse(0L)));

        maxValues.put("medianMessageLength", Math.max(1f, (float) medianMessageLengths.values().stream()
                .mapToInt(Integer::intValue).max().orElse(0)));

        return maxValues;
    }

    // Helper method to set up the interest meter chart
    private void setupInterestMeterChart(PieChart pieChart, float yourScore, float theirScore) {
        float totalScore = yourScore + theirScore;
        float yourPercentage = 50f; // Default to 50/50
        float theirPercentage = 50f;

        if (totalScore > 0) {
            yourPercentage = (yourScore / totalScore) * 100;
            theirPercentage = (theirScore / totalScore) * 100;
        }

        // Clamp percentages between 0-100
        yourPercentage = Math.max(0, Math.min(100, yourPercentage));
        theirPercentage = Math.max(0, Math.min(100, theirPercentage));

        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(yourPercentage, getString(R.string.You) + String.format(" %.1f%%", yourPercentage)));
        entries.add(new PieEntry(theirPercentage, getString(R.string.Them) + String.format(" %.1f%%", theirPercentage)));

        // Create a PieDataSet with the entries
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(new int[]{Color.parseColor("#00d4ff"), Color.parseColor("#FFD700")});
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);

        // Use a custom ValueFormatter to display the labels correctly
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.1f%%", value); // Display percentages with one decimal place
            }
        });

        // Create a PieData object with the PieDataSet
        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);

        // Customize the chart to look like a semi-circle
        pieChart.setRotationAngle(180);
        pieChart.setHoleRadius(50f);
        pieChart.setTransparentCircleRadius(55f);
        pieChart.setDrawEntryLabels(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setDescription(null);

        // Configure the legend
        Legend legend = pieChart.getLegend();
        legend.setEnabled(true); // Enable the legend
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM); // Place the legend at the bottom
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER); // Center the legend horizontally
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL); // Display the legend horizontally
        legend.setDrawInside(false); // Draw the legend outside the chart
        legend.setXEntrySpace(20f); // Increase the space between legend entries
        legend.setYEntrySpace(0f); // Set the space between legend rows
        legend.setYOffset(20f); // Increase the vertical offset of the legend
        legend.setTextColor(Color.WHITE); // Set legend text color to white
        legend.setTextSize(12f); // Set legend text size

        // Animate the chart
        pieChart.animateY(1000);
        pieChart.invalidate();
    }
    // Helper method to set up the Largest Communication Streak TextView
    private void setupLargestCommunicationStreakTextView(TextView daysTextView, TextView datesTextView, Map.Entry<Integer, Map.Entry<Date, Date>> streakInfo, String label) {
        if (streakInfo != null) {
            int days = streakInfo.getKey();
            Date startDate = streakInfo.getValue().getKey();
            Date endDate = streakInfo.getValue().getValue();

            // Format the dates
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            String startDateStr = dateFormat.format(startDate);
            String endDateStr = dateFormat.format(endDate);

            // Display the number of days
            daysTextView.setText(getString(R.string.time)+ ": " + days + " " + getString(R.string.days));

            // Display the date range
            datesTextView.setText(getString(R.string.dates)+ " " + startDateStr+ " " + getString(R.string.to)+" " + endDateStr);
        } else {
            daysTextView.setText(getString(R.string.time) + ": No streak found");
            datesTextView.setText(getString(R.string.dates)+" : N/A");
        }
    }
    // Helper method to set up the Longest Conversations TextView
    private void setupLongestConversationsTextView(TextView textView, long duration, String label) {
        long hours = TimeUnit.MILLISECONDS.toHours(duration);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(duration) % 60;
        String durationText = String.format("%d h %d m", hours, minutes);
        if (getString(R.string.Total).equals("Total :")) {
            durationText = String.format("%d h %d m", hours, minutes);
        }else {
            durationText = String.format("%d ч %d m", hours, minutes);
        }
        textView.setText(getString(R.string.Total) + " " + durationText);
    }
    // Helper method to set up the Unreplied Chats TextView
    private void setupUnrepliedChatsTextView(TextView textView, int unreplied, String label) {
        textView.setText( unreplied+ " " + getString(R.string.times));
    }
    // Helper method to set up the Conversation Starts TextView
    private void setupConversationStartsTextView(TextView textView, int starts, String label) {
        textView.setText(starts+" "+getString(R.string.times));
    }

    // Helper method to set up the Largest No Conversation Days TextView
    // Helper method to set up the Largest No Conversation Days TextView
    private void setupLargestNoConversationDaysTextView(TextView daysTextView, TextView datesTextView, Map.Entry<Integer, Map.Entry<Date, Date>> gapInfo, String label) {
        if (gapInfo != null) {
            int days = gapInfo.getKey();
            Date startDate = gapInfo.getValue().getKey();
            Date endDate = gapInfo.getValue().getValue();

            // Format the dates
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            String startDateStr = dateFormat.format(startDate);
            String endDateStr = dateFormat.format(endDate);

            // Display the number of days
            daysTextView.setText(getString(R.string.time)  + ": " + days +" "+ getString(R.string.days));

            // Display the date range
            datesTextView.setText(getString(R.string.dates) + " "+ startDateStr +" "+ getString(R.string.to)+" " + endDateStr);
        } else {
            daysTextView.setText(getString(R.string.time)+ ": No gaps found");
            datesTextView.setText( getString(R.string.dates)+"N/A");
        }
    }

    // Helper method to set up the Median Answering Time TextView
    private void setupMedianAnsweringTimeTextView(TextView textView, long medianTime, String label) {
        long medianTimeSeconds = medianTime / 1000; // Convert milliseconds to seconds
        textView.setText(getString(R.string.time) + ": " + medianTimeSeconds +   getString(R.string.s));
    }

    // Helper method to set up the Number of Links per User pie chart
    private void setupLinksPieChart(PieChart pieChart, Map<String, Integer> linkCounts, String label) {
        // Create a list of PieEntry objects
        List<PieEntry> pieEntries = new ArrayList<>();

        // Sort the links by count (descending)
        List<Map.Entry<String, Integer>> sortedLinks = new ArrayList<>(linkCounts.entrySet());
        sortedLinks.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue())); // Sort by count (descending)

        // Add data to pieEntries (top 5 websites)
        for (int i = 0; i < Math.min(5, sortedLinks.size()); i++) {
            Map.Entry<String, Integer> entry = sortedLinks.get(i);
            pieEntries.add(new PieEntry(entry.getValue(), entry.getKey())); // Add website and count
        }

        // Create a PieDataSet with the entries
        PieDataSet pieDataSet = new PieDataSet(pieEntries, label);

        // Define a custom color array with more unique colors
        int[] customColors = {
                Color.parseColor("#FFD700"), // Light red
                Color.parseColor("#FF6B6B"), // Light blue
                Color.parseColor("#00D4FF"), // Light orange
                Color.parseColor("#FF00FF"), // Light green
                Color.parseColor("#008080"), // Light purple

        };

        // Use the custom colors for the PieDataSet
        pieDataSet.setColors(customColors);

        pieDataSet.setValueTextColor(Color.WHITE); // Set text color for values
        pieDataSet.setValueTextSize(12f); // Set text size for values

        // Create a PieData object with the PieDataSet
        PieData pieData = new PieData(pieDataSet);
        pieChart.setData(pieData);

        // Customize the chart
        pieChart.getDescription().setEnabled(false); // Disable description
        pieChart.setDrawHoleEnabled(true); // Enable a hole in the center of the pie chart
        pieChart.setHoleRadius(30f); // Set the radius of the hole
        pieChart.setTransparentCircleRadius(35f); // Set the radius of the transparent circle
        pieChart.setEntryLabelColor(Color.WHITE); // Set the color of the entry labels
        pieChart.setEntryLabelTextSize(12f); // Set the size of the entry labels

        // Configure the legend
        Legend legend = pieChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false); // Draw the legend outside the chart
        legend.setXEntrySpace(7f); // Set the space between legend entries
        legend.setYEntrySpace(0f); // Set the space between legend rows
        legend.setYOffset(10f); // Set the vertical offset of the legend
        legend.setTextColor(Color.WHITE); // Set X-axis label color to white

        // Animate the chart
        pieChart.animateY(1000); // Animate the chart vertically

        // Refresh the chart
        pieChart.invalidate();
    }

    // Helper method to extract the website category from a link
    private String extractWebsiteCategory(String link) {
        try {
            // Extract the host (e.g., www.ozon.ru) from the link
            java.net.URI uri = new java.net.URI(link);
            String host = uri.getHost();
            if (host != null) {
                return host.startsWith("www.") ? host.substring(4) : host; // Remove "www." if present
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Unknown"; // Default category if the link is invalid
    }

    // Helper method to set up the Days with the Most Messages bar chart
    private void setupDaysMostMessagesChart(HorizontalBarChart barChart, List<Map.Entry<String, Integer>> daysWithMostMessages, String label) {
        // Create a list of BarEntry objects
        List<BarEntry> barEntries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        // Add data to barEntries and labels
        for (int i = 0; i < daysWithMostMessages.size(); i++) {
            Map.Entry<String, Integer> entry = daysWithMostMessages.get(i);
            barEntries.add(new BarEntry(i, entry.getValue()));
            labels.add(entry.getKey()); // Use the date as the label
        }

        // Create a BarDataSet with the sorted entries
        BarDataSet barDataSet = new BarDataSet(barEntries, label);
        barDataSet.setColor(barChart == yourDaysMostMessagesChart ? Color.parseColor("#00d4ff") : Color.parseColor("#FFD700")); // Set bar color based on chart
        barDataSet.setValueTextColor(Color.WHITE); // Set text color for values (white for better contrast)
        barDataSet.setValueTextSize(12f); // Set text size for values

        // Create a BarData object with the BarDataSet
        BarData barData = new BarData(barDataSet);
        barData.setBarWidth(0.4f); // Set the width of the bars
        barChart.setData(barData);

        // Customize the chart
        barChart.getDescription().setEnabled(false); // Disable description
        barChart.setDrawValueAboveBar(true); // Draw values above bars
        barChart.setFitBars(true); // Make the bars fit the chart

        // Configure X-axis (vertical axis in HorizontalBarChart)
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // Place X-axis at the bottom
        xAxis.setDrawGridLines(false); // Disable grid lines for X-axis
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels)); // Set date labels
        xAxis.setLabelCount(labels.size()); // Ensure all labels are shown
        xAxis.setGranularity(1f); // Set granularity to 1 to avoid skipping labels
        xAxis.setLabelRotationAngle(-45); // Rotate labels for better visibility
        xAxis.setDrawLabels(true); // Enable date labels
        xAxis.setTextColor(Color.WHITE); // Set X-axis label color to white

        // Add padding to the left axis to make space for the labels
        barChart.setExtraLeftOffset(25f);
        barChart.setExtraRightOffset(30f);

        // Configure Y-axis (horizontal axis in HorizontalBarChart)
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f); // Start Y-axis from 0
        leftAxis.setDrawLabels(false); // Disable Y-axis labels (0, 20, 40, ...)
        leftAxis.setDrawGridLines(false); // Disable grid lines for Y-axis

        YAxis rightAxis = barChart.getAxisRight();
        rightAxis.setEnabled(false); // Disable right Y-axis

        // Disable the legend (if any)
        barChart.getLegend().setEnabled(false);

        // Set chart background color to transparent
        barChart.setBackgroundColor(Color.TRANSPARENT);

        // Set grid and axis colors for better visibility
        barChart.getAxisLeft().setGridColor(Color.parseColor("#50FFFFFF")); // Light grid lines
        barChart.getXAxis().setGridColor(Color.parseColor("#50FFFFFF")); // Light grid lines

        // Animate the chart
        barChart.animateY(1000); // Animate the chart vertically

        // Refresh the chart
        barChart.invalidate();
    }
  // message per month
  private void setupMessagesPerMonthLineChart(LineChart lineChart, Map<String, Integer> monthCounts, String label, int lineColor) {
      // Create entries for the LineChart
      ArrayList<Entry> entries = new ArrayList<>();
      List<String> labels = new ArrayList<>();

      // Get the last 12 months (or fewer if the conversation is shorter)
      Calendar calendar = Calendar.getInstance();
      SimpleDateFormat monthFormat = new SimpleDateFormat("MMM yyyy", Locale.US); // Use "MMM" for short month names

      // Create a list of the last 12 months
      List<String> last12Months = new ArrayList<>();
      for (int i = 0; i < 12; i++) {
          last12Months.add(monthFormat.format(calendar.getTime()));
          calendar.add(Calendar.MONTH, -1); // Move to the previous month
      }

      // Reverse the list to show the oldest month first
      Collections.reverse(last12Months);

      // Add data to entries and labels
      for (int i = 0; i < last12Months.size(); i++) {
          String month = last12Months.get(i);
          // Convert the month label to the format used in monthCounts (e.g., "January 2023")
          SimpleDateFormat fullMonthFormat = new SimpleDateFormat("MMMM yyyy", Locale.US);
          try {
              Date date = monthFormat.parse(month);
              String fullMonth = fullMonthFormat.format(date);

              // Get the count for the month
              int count = monthCounts.getOrDefault(fullMonth, 0);
              entries.add(new Entry(i, count));
              labels.add(month.substring(0, 3)); // Use only the first 3 letters of the month
          } catch (ParseException e) {
              e.printStackTrace();
              // Handle the exception (e.g., log it or skip this month)
          }
      }

      // Create a LineDataSet with the entries
      LineDataSet dataSet = new LineDataSet(entries, label);
      dataSet.setColor(lineColor); // Set line color
      dataSet.setCircleColor(lineColor); // Set circle color
      dataSet.setLineWidth(2.5f); // Increase line width for better visibility
      dataSet.setCircleRadius(5f); // Increase circle radius for better visibility
      dataSet.setValueTextSize(12f); // Increase value text size
      dataSet.setValueTextColor(Color.WHITE); // Set value text color to white for better contrast
      dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER); // Use smooth curves for the line

      // Add ValueFormatter to display values as integers (e.g., 0 instead of 0.0)
      dataSet.setValueFormatter(new ValueFormatter() {
          @Override
          public String getFormattedValue(float value) {
              return String.valueOf((int) value); // Convert float to integer
          }
      });

      // Create a LineData object with the LineDataSet
      LineData lineData = new LineData(dataSet);
      lineChart.setData(lineData);

      // Customize the chart
      lineChart.getDescription().setEnabled(false); // Disable description
      lineChart.setDrawGridBackground(false); // Disable grid background
      lineChart.setTouchEnabled(true); // Enable touch interactions
      lineChart.setDragEnabled(true); // Enable dragging
      lineChart.setScaleEnabled(true); // Enable scaling
      lineChart.setPinchZoom(true); // Enable pinch zoom

      // Configure X-axis
      XAxis xAxis = lineChart.getXAxis();
      xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // Place X-axis at the bottom
      xAxis.setGranularity(1f); // Set granularity to 1 month
      xAxis.setValueFormatter(new IndexAxisValueFormatter(labels)); // Set month labels
      xAxis.setLabelCount(labels.size(), true); // Show all month labels
      xAxis.setDrawGridLines(false); // Disable grid lines for X-axis
      xAxis.setTextColor(Color.WHITE); // Set X-axis label color to white
      xAxis.setLabelRotationAngle(90); // Rotate labels vertically
      lineChart.setExtraBottomOffset(20f); // Add extra bottom offset for labels

      // Configure Y-axis
      YAxis leftAxis = lineChart.getAxisLeft();
      leftAxis.setAxisMinimum(0f); // Start Y-axis from 0
      leftAxis.setGranularity(1f); // Set granularity to 1 message
      leftAxis.setDrawGridLines(true); // Enable grid lines for Y-axis
      leftAxis.setGridColor(Color.parseColor("#50FFFFFF")); // Light grid lines for better visibility
      leftAxis.setTextColor(Color.WHITE); // Set Y-axis label color to white

      YAxis rightAxis = lineChart.getAxisRight();
      rightAxis.setEnabled(false); // Disable right Y-axis

      // Disable the legend (if any)
      lineChart.getLegend().setEnabled(true); // Enable legend to show the label
      lineChart.getLegend().setTextColor(Color.WHITE); // Set legend text color to white

      // Set chart background color to transparent
      lineChart.setBackgroundColor(Color.TRANSPARENT);

      // Animate the chart
      lineChart.animateY(1000); // Animate the chart vertically

      // Refresh the chart
      lineChart.invalidate();
  }
    // Helper method to set up the Most Used Words chart
    private void setupMostUsedWordsChart(HorizontalBarChart barChart, Map<String, Integer> wordCounts, String label) {
        // Create a list of BarEntry objects
        List<BarEntry> barEntries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        // Sort the words by count (descending)
        List<Map.Entry<String, Integer>> sortedWords = new ArrayList<>(wordCounts.entrySet());
        sortedWords.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

        // Take the top 10 words
        for (int i = 0; i < Math.min(10, sortedWords.size()); i++) {
            barEntries.add(new BarEntry(i, sortedWords.get(i).getValue()));
            labels.add(sortedWords.get(i).getKey());
        }

        // Create a BarDataSet with the sorted entries
        BarDataSet barDataSet = new BarDataSet(barEntries, label);
        barDataSet.setColor(barChart == yourMostUsedWordsChart ? Color.parseColor("#00d4ff") : Color.parseColor("#FFD700")); // Set bar color based on chart
        barDataSet.setValueTextColor(Color.WHITE); // Set text color for values
        barDataSet.setValueTextSize(12f); // Set text size for values

        // Create a BarData object with the BarDataSet
        BarData barData = new BarData(barDataSet);
        barData.setBarWidth(0.5f); // Set the width of the bars
        barChart.setData(barData);

        // Customize the chart
        barChart.getDescription().setEnabled(false); // Disable description
        barChart.setDrawValueAboveBar(true); // Draw values above bars
        barChart.setFitBars(true); // Make the bars fit the chart

        // Configure X-axis (vertical axis in HorizontalBarChart)
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // Place X-axis at the bottom
        xAxis.setDrawGridLines(false); // Disable grid lines for X-axis
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels)); // Set word labels
        xAxis.setLabelCount(labels.size()); // Ensure all labels are shown
        xAxis.setGranularity(1f); // Set granularity to 1 to avoid skipping labels
        xAxis.setLabelRotationAngle(-45); // Rotate labels for better visibility
        xAxis.setDrawLabels(true); // Enable word labels
        xAxis.setTextColor(Color.WHITE); // Set X-axis label color to white

        // Add padding to the left axis to make space for the labels
        barChart.setExtraLeftOffset(25f);
        barChart.setExtraRightOffset(30f);

        // Configure Y-axis (horizontal axis in HorizontalBarChart)
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f); // Start Y-axis from 0
        leftAxis.setDrawLabels(false); // Disable Y-axis labels (0, 20, 40, ...)
        leftAxis.setDrawGridLines(false); // Disable grid lines for Y-axis

        YAxis rightAxis = barChart.getAxisRight();
        rightAxis.setEnabled(false); // Disable right Y-axis

        // Disable the legend (if any)
        barChart.getLegend().setEnabled(false);

        // Animate the chart
        barChart.animateY(1000); // Animate the chart vertically

        // Refresh the chart
        barChart.invalidate();
    }

    // Helper method to set up the Most Used Emojis chart
    private void setupMostUsedEmojisChart(HorizontalBarChart barChart, Map<String, Integer> emojiCounts, String label) {
        // Create a list of BarEntry objects
        List<BarEntry> barEntries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        // Sort the emojis by count (descending)
        List<Map.Entry<String, Integer>> sortedEmojis = new ArrayList<>(emojiCounts.entrySet());
        sortedEmojis.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

        // Take the top 5 emojis
        for (int i = 0; i < Math.min(5, sortedEmojis.size()); i++) {
            barEntries.add(new BarEntry(i, sortedEmojis.get(i).getValue()));
            labels.add(sortedEmojis.get(i).getKey());
        }

        // Create a BarDataSet with the sorted entries
        BarDataSet barDataSet = new BarDataSet(barEntries, label);
        barDataSet.setColor(barChart == yourMostUsedEmojisChart ? Color.parseColor("#00d4ff") : Color.parseColor("#FFD700")); // Set bar color based on chart
        barDataSet.setValueTextColor(Color.WHITE); // Set text color for values
        barDataSet.setValueTextSize(12f); // Set text size for values

        // Create a BarData object with the BarDataSet
        BarData barData = new BarData(barDataSet);
        barData.setBarWidth(0.5f); // Set the width of the bars
        barChart.setData(barData);

        // Customize the chart
        barChart.getDescription().setEnabled(false); // Disable description
        barChart.setDrawValueAboveBar(true); // Draw values above bars
        barChart.setFitBars(true); // Make the bars fit the chart

        // Configure X-axis (vertical axis in HorizontalBarChart)
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // Place X-axis at the bottom
        xAxis.setDrawGridLines(false); // Disable grid lines for X-axis
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels)); // Set emoji labels
        xAxis.setLabelCount(labels.size()); // Ensure all labels are shown
        xAxis.setGranularity(1f); // Set granularity to 1 to avoid skipping labels
        xAxis.setDrawLabels(true); // Enable emoji labels

        // Add padding to the left axis to make space for the labels
        barChart.setExtraLeftOffset(25f);
        barChart.setExtraRightOffset(30f);

        // Configure Y-axis (horizontal axis in HorizontalBarChart)
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f); // Start Y-axis from 0
        leftAxis.setDrawLabels(false); // Disable Y-axis labels (0, 20, 40, ...)
        leftAxis.setDrawGridLines(false); // Disable grid lines for Y-axis

        YAxis rightAxis = barChart.getAxisRight();
        rightAxis.setEnabled(false); // Disable right Y-axis

        // Disable the legend (if any)
        barChart.getLegend().setEnabled(false);

        // Animate the chart
        barChart.animateY(1000); // Animate the chart vertically

        // Refresh the chart
        barChart.invalidate();
    }

    // Helper method to format a Map into a readable string
    private String formatMap(Map<?, ?> map) {
        StringBuilder result = new StringBuilder();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            result.append(entry.getKey()).append(" - ").append(entry.getValue()).append(", ");
        }
        return result.length() > 0 ? result.substring(0, result.length() - 2) : "";
    }

    // Helper method to format a List of Map entries into a readable string
    private String formatList(List<Map.Entry<String, Integer>> list) {
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, Integer> entry : list) {
            result.append(entry.getKey()).append(" - ").append(entry.getValue()).append(", ");
        }
        return result.length() > 0 ? result.substring(0, result.length() - 2) : "";
    }

    // Helper method to set up the bar chart
    private void setupBarChart(HorizontalBarChart barChart, Map<String, Integer> data, String label) {
        // Create a list of BarEntry objects
        List<BarEntry> barEntries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        // Sort the data by value (descending order)
        String[] daysOfWeek = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        List<Map.Entry<String, Integer>> sortedData = new ArrayList<>();
        for (String day : daysOfWeek) {
            if (data.containsKey(day)) {
                sortedData.add(new AbstractMap.SimpleEntry<>(day, data.get(day)));
            }
        }

        // Add data to barEntries and labels
        for (int i = 0; i < sortedData.size(); i++) {
            barEntries.add(new BarEntry(i, sortedData.get(i).getValue()));
            labels.add(sortedData.get(i).getKey());
        }

        // Create a BarDataSet with the sorted entries
        BarDataSet barDataSet = new BarDataSet(barEntries, label);
        barDataSet.setColor(barChart == yourBarChart ? Color.parseColor("#00d4ff") : Color.parseColor("#FFD700")); // Set bar color based on chart
        barDataSet.setValueTextColor(Color.WHITE); // Set text color for values (white for better contrast)
        barDataSet.setValueTextSize(12f); // Set text size for values

        // Create a BarData object with the BarDataSet
        BarData barData = new BarData(barDataSet);
        barData.setBarWidth(0.5f); // Set the width of the bars
        barChart.setData(barData);

        // Customize the chart
        barChart.getDescription().setEnabled(false); // Disable description
        barChart.setDrawValueAboveBar(true); // Draw values above bars
        barChart.setFitBars(true); // Make the bars fit the chart

        // Configure X-axis (vertical axis in HorizontalBarChart)
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // Place X-axis at the bottom
        xAxis.setDrawGridLines(false); // Disable grid lines for X-axis
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels)); // Set sorted day labels
        xAxis.setLabelCount(labels.size()); // Ensure all labels are shown
        xAxis.setGranularity(1f); // Set granularity to 1 to avoid skipping labels
        xAxis.setLabelRotationAngle(-45); // Rotate labels for better visibility
        xAxis.setDrawLabels(true); // Enable day labels
        xAxis.setTextColor(Color.WHITE); // Set X-axis label color to white

        // Add padding to the left axis to make space for the labels
        barChart.setExtraLeftOffset(25f);
        barChart.setExtraRightOffset(30f);

        // Configure Y-axis (horizontal axis in HorizontalBarChart)
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f); // Start Y-axis from 0
        leftAxis.setDrawLabels(false); // Disable Y-axis labels (0, 20, 40, ...)
        leftAxis.setDrawGridLines(false); // Disable grid lines for Y-axis

        YAxis rightAxis = barChart.getAxisRight();
        rightAxis.setEnabled(false); // Disable right Y-axis

        // Disable the legend (if any)
        barChart.getLegend().setEnabled(false);

        // Set chart background color to transparent
        barChart.setBackgroundColor(Color.TRANSPARENT);

        // Set grid and axis colors for better visibility
        barChart.getAxisLeft().setGridColor(Color.parseColor("#50FFFFFF")); // Light grid lines
        barChart.getXAxis().setGridColor(Color.parseColor("#50FFFFFF")); // Light grid lines

        // Animate the chart
        barChart.animateY(1000); // Animate the chart vertically

        // Refresh the chart
        barChart.invalidate();
    }

    // Helper method to set up the LineChart for "Messages per Hour of the Day"
    private void setupHourOfDayLineChart(LineChart lineChart, Map<Integer, Integer> hourOfDayCounts, String label, int lineColor) {
        // Create entries for the LineChart
        ArrayList<Entry> entries = new ArrayList<>();
        for (int hour = 0; hour < 24; hour++) {
            entries.add(new Entry(hour, hourOfDayCounts.getOrDefault(hour, 0)));
        }

        // Create a LineDataSet with the entries
        LineDataSet dataSet = new LineDataSet(entries, label);
        dataSet.setColor(lineColor); // Set line color
        dataSet.setCircleColor(lineColor); // Set circle color
        dataSet.setLineWidth(2.5f); // Increase line width for better visibility
        dataSet.setCircleRadius(5f); // Increase circle radius for better visibility
        dataSet.setValueTextSize(12f); // Increase value text size
        dataSet.setValueTextColor(Color.WHITE); // Set value text color to white for better contrast
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER); // Use smooth curves for the line

        // Add ValueFormatter to display values as integers (e.g., 0 instead of 0.0)
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value); // Convert float to integer
            }
        });

        // Create a LineData object with the LineDataSet
        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        // Customize the chart
        lineChart.getDescription().setEnabled(false); // Disable description
        lineChart.setDrawGridBackground(false); // Disable grid background
        lineChart.setTouchEnabled(true); // Enable touch interactions
        lineChart.setDragEnabled(true); // Enable dragging
        lineChart.setScaleEnabled(true); // Enable scaling
        lineChart.setPinchZoom(true); // Enable pinch zoom

        // Configure X-axis
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // Place X-axis at the bottom
        xAxis.setGranularity(1f); // Set granularity to 1 hour
        xAxis.setValueFormatter(new IndexAxisValueFormatter(getHourLabels())); // Set hour labels
        xAxis.setLabelCount(24, true); // Show all 24 hours
        xAxis.setDrawGridLines(false); // Disable grid lines for X-axis
        xAxis.setTextColor(Color.WHITE); // Set X-axis label color to white
        xAxis.setLabelRotationAngle(90); // Rotate labels vertically
        lineChart.setExtraBottomOffset(20f); // Add extra bottom offset for labels

        // Configure Y-axis
        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f); // Start Y-axis from 0
        leftAxis.setGranularity(1f); // Set granularity to 1 message
        leftAxis.setDrawGridLines(true); // Enable grid lines for Y-axis
        leftAxis.setGridColor(Color.parseColor("#50FFFFFF")); // Light grid lines for better visibility
        leftAxis.setTextColor(Color.WHITE); // Set Y-axis label color to white

        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(false); // Disable right Y-axis

        // Disable the legend (if any)
        lineChart.getLegend().setEnabled(true); // Enable legend to show the label
        lineChart.getLegend().setTextColor(Color.WHITE); // Set legend text color to white

        // Set chart background color to transparent
        lineChart.setBackgroundColor(Color.TRANSPARENT);

        // Animate the chart
        lineChart.animateY(1000); // Animate the chart vertically

        // Refresh the chart
        lineChart.invalidate();
    }

    // Helper method to generate hour labels (0-23)
    private String[] getHourLabels() {
        String[] labels = new String[24];
        for (int i = 0; i < 24; i++) {
            labels[i] = String.valueOf(i);
        }
        return labels;
    }

    // Method to show the overlay with the longest message
    private void showOverlay(String longestMessage) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View overlayView = getLayoutInflater().inflate(R.layout.overlay_longest_message, null);
        bottomSheetDialog.setContentView(overlayView);

        if (bottomSheetDialog.getWindow() != null) {
            bottomSheetDialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.8),
                    (int) (getResources().getDisplayMetrics().heightPixels * 0.8)
            );
        }

        TextView overlayText = overlayView.findViewById(R.id.overlay_longest_message_text);
        overlayText.setText(longestMessage);

        Button closeButton = overlayView.findViewById(R.id.close_button);
        closeButton.setOnClickListener(v -> bottomSheetDialog.dismiss());

        bottomSheetDialog.show();
    }

    // Inner class to analyze chat data
    private static class ChatAnalyzer {
        private final List<ChatMessage> messages;

        public ChatAnalyzer(List<String> rawMessages) {
            this.messages = parseMessages(rawMessages);
        }

        // Parse raw messages into ChatMessage objects
        private List<ChatMessage> parseMessages(List<String> rawMessages) {
            List<ChatMessage> parsedMessages = new ArrayList<>();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

            for (String rawMessage : rawMessages) {
                try {
                    String[] parts = rawMessage.split(", chat id: ");
                    String datePart = parts[0].replace("chatdate: ", "");
                    String[] rest = parts[1].split(", content: ");
                    long chatId = Long.parseLong(rest[0]);
                    String content = rest[1].replace(" end", "");

                    Date date = dateFormat.parse(datePart);
                    parsedMessages.add(new ChatMessage(date, chatId, content));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            return parsedMessages;
        }

        // 1. Calculate the number of messages per user
        public Map<Long, Integer> getNumberOfMessagesPerUser() {
            Map<Long, Integer> messageCounts = new HashMap<>();

            for (ChatMessage message : messages) {
                messageCounts.put(message.chatId, messageCounts.getOrDefault(message.chatId, 0) + 1);
            }

            return messageCounts;
        }

        // 2. Calculate the average time to answer for each user
        public Map<Long, Long> getAverageTimeToAnswer() {
            // Sort messages by date
            messages.sort((m1, m2) -> m1.date.compareTo(m2.date));

            Map<Long, Long> totalTime = new HashMap<>();
            Map<Long, Integer> responseCounts = new HashMap<>();
            Map<Long, Date> lastMessageTimes = new HashMap<>();

            // Define the maximum time difference for a conversation (1 hour in milliseconds)
            long maxConversationGap = 60 * 3 * 60 * 1000; // 1 hour in milliseconds

            for (ChatMessage message : messages) {
                if (lastMessageTimes.containsKey(message.chatId)) {
                    long timeDiff = message.date.getTime() - lastMessageTimes.get(message.chatId).getTime();

                    // Only consider time differences within the conversation threshold
                    if (timeDiff >= 0 && timeDiff <= maxConversationGap) {
                        totalTime.put(message.chatId, totalTime.getOrDefault(message.chatId, 0L) + timeDiff);
                        responseCounts.put(message.chatId, responseCounts.getOrDefault(message.chatId, 0) + 1);
                    }
                }
                lastMessageTimes.put(message.chatId, message.date);
            }

            Map<Long, Long> averageTimes = new HashMap<>();
            for (Long chatId : totalTime.keySet()) {
                averageTimes.put(chatId, totalTime.get(chatId) / responseCounts.get(chatId));
            }

            return averageTimes;
        }

        // 3. Find the favorite emoji for each user
        public Map<Long, String> getFavoriteEmojiPerUser() {
            Map<Long, Map<String, Integer>> emojiCounts = new HashMap<>();

            for (ChatMessage message : messages) {
                Map<String, Integer> userEmojiCounts = emojiCounts.getOrDefault(message.chatId, new HashMap<>());
                List<String> emojis = extractEmojis(message.content); // Extract all emojis from the message

                // Count each emoji
                for (String emoji : emojis) {
                    userEmojiCounts.put(emoji, userEmojiCounts.getOrDefault(emoji, 0) + 1);
                }
                emojiCounts.put(message.chatId, userEmojiCounts);
            }

            // Find the favorite emoji for each user
            Map<Long, String> favoriteEmojis = new HashMap<>();
            for (Long chatId : emojiCounts.keySet()) {
                String favoriteEmoji = emojiCounts.get(chatId).entrySet().stream()
                        .max(Map.Entry.comparingByValue()) // Find the emoji with the highest count
                        .map(Map.Entry::getKey) // Get the emoji itself
                        .orElse(""); // Default to an empty string if no emojis are found
                favoriteEmojis.put(chatId, favoriteEmoji);
            }

            return favoriteEmojis;
        }

        // 4. Number of media files per user
        public Map<Long, Integer> getNumberOfMediaFilesPerUser() {
            Map<Long, Integer> mediaCounts = new HashMap<>();

            for (ChatMessage message : messages) {
                if (message.content.contains("<Media/Non-text message>")) {
                    mediaCounts.put(message.chatId, mediaCounts.getOrDefault(message.chatId, 0) + 1);
                }
            }

            return mediaCounts;
        }

        // 5. Number of links per user, grouped by website
        public Map<Long, Map<String, Integer>> getNumberOfLinksPerUser() {
            Map<Long, Map<String, Integer>> linkCounts = new HashMap<>();
            Pattern linkPattern = Pattern.compile("https?://([^/]+)"); // Extract the domain part of the URL

            for (ChatMessage message : messages) {
                Matcher matcher = linkPattern.matcher(message.content);
                while (matcher.find()) {
                    String domain = matcher.group(1); // Extract the domain (e.g., www.ozon.ru)
                    Map<String, Integer> userLinkCounts = linkCounts.getOrDefault(message.chatId, new HashMap<>());
                    userLinkCounts.put(domain, userLinkCounts.getOrDefault(domain, 0) + 1);
                    linkCounts.put(message.chatId, userLinkCounts);
                }
            }

            return linkCounts;
        }

        // 6. Messages per day of the week for each user
        public Map<Long, Map<String, Integer>> getMessagesPerDayOfWeek() {
            Map<Long, Map<String, Integer>> dayCounts = new HashMap<>();
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.US);

            for (ChatMessage message : messages) {
                String day = dayFormat.format(message.date);
                Map<String, Integer> userDayCounts = dayCounts.getOrDefault(message.chatId, new HashMap<>());
                userDayCounts.put(day, userDayCounts.getOrDefault(day, 0) + 1);
                dayCounts.put(message.chatId, userDayCounts);
            }

            return dayCounts;
        }

        // 7. Messages per hour of the day for each user
        public Map<Long, Map<Integer, Integer>> getMessagesPerHourOfDay() {
            Map<Long, Map<Integer, Integer>> hourCounts = new HashMap<>();
            Calendar calendar = Calendar.getInstance();

            for (ChatMessage message : messages) {
                calendar.setTime(message.date);
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                Map<Integer, Integer> userHourCounts = hourCounts.getOrDefault(message.chatId, new HashMap<>());
                userHourCounts.put(hour, userHourCounts.getOrDefault(hour, 0) + 1);
                hourCounts.put(message.chatId, userHourCounts);
            }

            return hourCounts;
        }

        // 8. Messages per month
        public Map<Long, Map<String, Integer>> getMessagesPerMonth() {
            Map<Long, Map<String, Integer>> monthCounts = new HashMap<>();
            SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.US);

            for (ChatMessage message : messages) {
                String month = monthFormat.format(message.date);
                Map<String, Integer> userMonthCounts = monthCounts.getOrDefault(message.chatId, new HashMap<>());
                userMonthCounts.put(month, userMonthCounts.getOrDefault(month, 0) + 1);
                monthCounts.put(message.chatId, userMonthCounts);
            }

            return monthCounts;
        }

        // 9. Days with the most messages (sorted from highest to lowest, top 5 only)
        public Map<Long, List<Map.Entry<String, Integer>>> getDaysWithMostMessages() {
            Map<Long, Map<String, Integer>> dayCounts = new HashMap<>();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

            for (ChatMessage message : messages) {
                String date = dateFormat.format(message.date);
                Map<String, Integer> userDayCounts = dayCounts.getOrDefault(message.chatId, new HashMap<>());
                userDayCounts.put(date, userDayCounts.getOrDefault(date, 0) + 1);
                dayCounts.put(message.chatId, userDayCounts);
            }

            Map<Long, List<Map.Entry<String, Integer>>> sortedDays = new HashMap<>();
            for (Long chatId : dayCounts.keySet()) {
                List<Map.Entry<String, Integer>> sortedList = new ArrayList<>(dayCounts.get(chatId).entrySet());
                sortedList.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue())); // Sort by count (descending)

                // Take only the top 5 days
                sortedDays.put(chatId, sortedList.subList(0, Math.min(5, sortedList.size())));
            }

            return sortedDays;
        }

        // 10. Number of messages in the last 10 days
        public Map<Long, Integer> getMessagesInLast10Days() {
            Map<Long, Integer> last10DaysCounts = new HashMap<>();
            Calendar calendar = Calendar.getInstance();
            Date now = new Date();
            calendar.setTime(now);
            calendar.add(Calendar.DAY_OF_YEAR, -10);
            Date tenDaysAgo = calendar.getTime();

            for (ChatMessage message : messages) {
                if (message.date.after(tenDaysAgo)) {
                    last10DaysCounts.put(message.chatId, last10DaysCounts.getOrDefault(message.chatId, 0) + 1);
                }
            }

            return last10DaysCounts;
        }

        // 11. Most used words (top 10, with words longer than 4 characters, excluding media files and unwanted placeholders)
        public Map<Long, Map<String, Integer>> getMostUsedWords() {
            Map<Long, Map<String, Integer>> wordCounts = new HashMap<>();

            for (ChatMessage message : messages) {
                // Skip media files and unwanted placeholders
                if (message.content.equals("<Media/Non-text message>") || message.content.contains("message>")) {
                    continue;
                }

                Map<String, Integer> userWordCounts = wordCounts.getOrDefault(message.chatId, new HashMap<>());
                String[] words = message.content.split("\\s+");
                for (String word : words) {
                    // Ignore empty words and words with 4 or fewer characters
                    if (!word.isEmpty() && word.length() > 4) {
                        userWordCounts.put(word, userWordCounts.getOrDefault(word, 0) + 1);
                    }
                }
                wordCounts.put(message.chatId, userWordCounts);
            }

            // Limit to top 10 most used words for each user
            Map<Long, Map<String, Integer>> top10Words = new HashMap<>();
            for (Long chatId : wordCounts.keySet()) {
                Map<String, Integer> userWordCounts = wordCounts.get(chatId);

                // Sort the words by frequency (descending)
                List<Map.Entry<String, Integer>> sortedWords = new ArrayList<>(userWordCounts.entrySet());
                sortedWords.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

                // Take the top 10
                Map<String, Integer> top10 = new LinkedHashMap<>();
                for (int i = 0; i < Math.min(10, sortedWords.size()); i++) {
                    Map.Entry<String, Integer> entry = sortedWords.get(i);
                    top10.put(entry.getKey(), entry.getValue());
                }

                top10Words.put(chatId, top10);
            }

            return top10Words;
        }

        // 12. Most used emojis (top 5, in descending order)
        public Map<Long, Map<String, Integer>> getMostUsedEmojis() {
            Map<Long, Map<String, Integer>> emojiCounts = new HashMap<>();

            for (ChatMessage message : messages) {
                Map<String, Integer> userEmojiCounts = emojiCounts.getOrDefault(message.chatId, new HashMap<>());
                List<String> emojis = extractEmojis(message.content); // Extract all emojis from the message

                // Count each emoji
                for (String emoji : emojis) {
                    userEmojiCounts.put(emoji, userEmojiCounts.getOrDefault(emoji, 0) + 1);
                }
                emojiCounts.put(message.chatId, userEmojiCounts);
            }

            // Limit to top 5 most used emojis for each user
            Map<Long, Map<String, Integer>> top5Emojis = new HashMap<>();
            for (Long chatId : emojiCounts.keySet()) {
                Map<String, Integer> userEmojiCounts = emojiCounts.get(chatId);

                // Sort the emojis by frequency (descending)
                List<Map.Entry<String, Integer>> sortedEmojis = new ArrayList<>(userEmojiCounts.entrySet());
                sortedEmojis.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

                // Take the top 5
                Map<String, Integer> top5 = new LinkedHashMap<>();
                for (int i = 0; i < Math.min(5, sortedEmojis.size()); i++) {
                    Map.Entry<String, Integer> entry = sortedEmojis.get(i);
                    top5.put(entry.getKey(), entry.getValue());
                }

                top5Emojis.put(chatId, top5);
            }

            return top5Emojis;
        }

        // Helper method to extract all emojis from a string
        private List<String> extractEmojis(String input) {
            List<String> emojis = new ArrayList<>();
            int length = input.codePointCount(0, input.length());

            for (int i = 0; i < length; i++) {
                int codePoint = input.codePointAt(i);
                if (isEmoji(codePoint)) {
                    // Convert the code point to a string and add it to the list
                    emojis.add(new String(Character.toChars(codePoint)));
                }
            }

            return emojis;
        }

        // Helper method to check if a code point is an emoji
        private boolean isEmoji(int codePoint) {
            return (codePoint >= 0x1F600 && codePoint <= 0x1F64F) || // Emoticons
                    (codePoint >= 0x1F300 && codePoint <= 0x1F5FF) || // Misc Symbols and Pictographs
                    (codePoint >= 0x1F680 && codePoint <= 0x1F6FF) || // Transport and Map Symbols
                    (codePoint >= 0x2600 && codePoint <= 0x26FF) ||   // Misc Symbols
                    (codePoint >= 0x2700 && codePoint <= 0x27BF) ||   // Dingbats
                    (codePoint >= 0xFE00 && codePoint <= 0xFE0F) ||   // Variation Selectors
                    (codePoint >= 0x1F900 && codePoint <= 0x1F9FF) || // Supplemental Symbols and Pictographs
                    (codePoint >= 0x1F1E6 && codePoint <= 0x1F1FF);   // Flags
        }

        // 13. Get the longest message per user
        public Map<Long, String> getLongestMessagePerUser() {
            Map<Long, String> longestMessages = new HashMap<>();

            // Regular expression to detect URLs
            Pattern urlPattern = Pattern.compile("https?://\\S+");

            for (ChatMessage message : messages) {
                // Skip messages that contain a URL
                Matcher matcher = urlPattern.matcher(message.content);
                if (matcher.find()) {
                    continue; // Skip this message if it contains a URL
                }

                // Get the current longest message for this user (default to empty string if not found)
                String currentLongestMessage = longestMessages.getOrDefault(message.chatId, "");

                // Check if the current message is longer
                if (message.content.length() > currentLongestMessage.length()) {
                    longestMessages.put(message.chatId, message.content);
                }
            }

            return longestMessages;
        }


        // 14. Calculate the median answering time for each user
        public Map<Long, Long> getMedianAnsweringTime() {
            // Sort messages by date
            messages.sort((m1, m2) -> m1.date.compareTo(m2.date));

            Map<Long, List<Long>> responseTimes = new HashMap<>();
            Map<Long, Date> lastMessageTimes = new HashMap<>();

            // Define the maximum time difference for a conversation (3 hours in milliseconds)
            long maxConversationGap = 3 * 60 * 60 * 1000; // 3 hours in milliseconds

            for (ChatMessage message : messages) {
                if (lastMessageTimes.containsKey(message.chatId)) {
                    long timeDiff = message.date.getTime() - lastMessageTimes.get(message.chatId).getTime();

                    // Only consider time differences within the conversation threshold
                    if (timeDiff >= 0 && timeDiff <= maxConversationGap) {
                        responseTimes.computeIfAbsent(message.chatId, k -> new ArrayList<>()).add(timeDiff);
                    }
                }
                lastMessageTimes.put(message.chatId, message.date);
            }

            Map<Long, Long> medianTimes = new HashMap<>();
            for (Long chatId : responseTimes.keySet()) {
                List<Long> times = responseTimes.get(chatId);
                Collections.sort(times);
                int middle = times.size() / 2;
                if (times.size() % 2 == 1) {
                    medianTimes.put(chatId, times.get(middle));
                } else {
                    medianTimes.put(chatId, (times.get(middle - 1) + times.get(middle)) / 2);
                }
            }

            return medianTimes;
        }


        // 15. Calculate the largest number of days with no conversation and the start/end dates
        public Map<Long, Map.Entry<Integer, Map.Entry<Date, Date>>> getLargestNoConversationDays() {
            // Sort messages by date
            messages.sort((m1, m2) -> m1.date.compareTo(m2.date));

            Map<Long, Map.Entry<Integer, Map.Entry<Date, Date>>> largestGaps = new HashMap<>();
            Map<Long, Date> lastMessageTimes = new HashMap<>();

            for (ChatMessage message : messages) {
                if (lastMessageTimes.containsKey(message.chatId)) {
                    long timeDiff = message.date.getTime() - lastMessageTimes.get(message.chatId).getTime();
                    int daysDiff = (int) (timeDiff / (1000 * 60 * 60 * 24));

                    // Get the current largest gap for this user
                    Map.Entry<Integer, Map.Entry<Date, Date>> currentLargestGap = largestGaps.getOrDefault(message.chatId, null);

                    // If the current gap is larger than the stored one, update it
                    if (currentLargestGap == null || daysDiff > currentLargestGap.getKey()) {
                        // Create a new entry with the gap size and the start/end dates
                        Map.Entry<Date, Date> startEndDates = new AbstractMap.SimpleEntry<>(lastMessageTimes.get(message.chatId), message.date);
                        Map.Entry<Integer, Map.Entry<Date, Date>> newGap = new AbstractMap.SimpleEntry<>(daysDiff, startEndDates);
                        largestGaps.put(message.chatId, newGap);
                    }
                }
                lastMessageTimes.put(message.chatId, message.date);
            }

            return largestGaps;
        }
        // 16. Calculate the number of times each user started a conversation
        public Map<Long, Integer> getConversationStarts() {
            // Sort messages by date
            messages.sort((m1, m2) -> m1.date.compareTo(m2.date));

            Map<Long, Integer> conversationStarts = new HashMap<>();
            Map<Long, Date> lastMessageTimes = new HashMap<>();

            // Define the maximum time difference for a conversation (3 hours in milliseconds)
            long maxConversationGap = 3 * 60 * 60 * 1000; // 3 hours in milliseconds

            for (ChatMessage message : messages) {
                if (!lastMessageTimes.containsKey(message.chatId) ||
                        message.date.getTime() - lastMessageTimes.get(message.chatId).getTime() > maxConversationGap) {
                    // If the gap is more than 3 hours, it's a new conversation start
                    conversationStarts.put(message.chatId, conversationStarts.getOrDefault(message.chatId, 0) + 1);
                }
                // Update the last message time for this user
                lastMessageTimes.put(message.chatId, message.date);
            }

            return conversationStarts;
        }



        // 17. Find chats where the last message was from the user, but no reply came back
        public Map<Long, Integer> getUnrepliedChats() {
            // Sort messages by date
            messages.sort((m1, m2) -> m1.date.compareTo(m2.date));

            Map<Long, Integer> unrepliedChats = new HashMap<>();
            long maxConversationGap = 3 * 60 * 60 * 1000; // 3 hours in milliseconds

            int i = 0;
            while (i < messages.size()) {
                ChatMessage currentMessage = messages.get(i);
                long currentChatId = currentMessage.chatId; // Assuming chatId represents the user ID
                long lastMessageTime = currentMessage.date.getTime();
                long lastMessageUserId = currentChatId; // Using chatId as user ID

                // Find the end of the conversation
                int j = i + 1;
                while (j < messages.size()) {
                    ChatMessage nextMessage = messages.get(j);
                    long nextMessageTime = nextMessage.date.getTime();

                    // If the next message is within 3 hours, continue the conversation
                    if (nextMessageTime - lastMessageTime <= maxConversationGap) {
                        lastMessageTime = nextMessageTime;
                        lastMessageUserId = nextMessage.chatId; // Using chatId as user ID
                        j++;
                    } else {
                        // Conversation ends
                        break;
                    }
                }

                // Determine who the last message is from
                if (lastMessageUserId != currentChatId) {
                    // Last message is from the other user: unreplied chat for the other user
                    unrepliedChats.put(lastMessageUserId, unrepliedChats.getOrDefault(lastMessageUserId, 0) + 1);
                } else {
                    // Last message is from you: unreplied chat for you
                    unrepliedChats.put(currentChatId, unrepliedChats.getOrDefault(currentChatId, 0) + 1);
                }

                // Move to the next conversation
                i = j;
            }

            return unrepliedChats;
        }
        // 18. Calculate the longest conversations (time)
        // 18. Calculate the longest conversations (time)
        public Map<Long, Long> getLongestConversations() {
            // Sort messages by date
            messages.sort((m1, m2) -> m1.date.compareTo(m2.date));

            Map<Long, Long> longestConversations = new HashMap<>();
            Map<Long, Date> conversationStartTimes = new HashMap<>();
            Map<Long, Date> lastMessageTimes = new HashMap<>();

            // Define the maximum time difference for a conversation (3 hours in milliseconds)
            long maxConversationGap = 3 * 60 * 60 * 1000; // 3 hours in milliseconds

            for (ChatMessage message : messages) {
                long chatId = message.chatId;

                // If no conversation is ongoing or the gap is more than 3 hours, start a new conversation
                if (!conversationStartTimes.containsKey(chatId) ||
                        message.date.getTime() - lastMessageTimes.get(chatId).getTime() > maxConversationGap) {
                    // If a conversation was ongoing, calculate its duration and update the longest duration
                    if (conversationStartTimes.containsKey(chatId)) {
                        long conversationDuration = lastMessageTimes.get(chatId).getTime() - conversationStartTimes.get(chatId).getTime();
                        if (conversationDuration > longestConversations.getOrDefault(chatId, 0L)) {
                            longestConversations.put(chatId, conversationDuration);
                        }
                    }

                    // Start a new conversation
                    conversationStartTimes.put(chatId, message.date);
                }

                // Update the last message time for this chat
                lastMessageTimes.put(chatId, message.date);
            }

            // After the loop, check the last conversation for each chat
            for (Long chatId : conversationStartTimes.keySet()) {
                long conversationDuration = lastMessageTimes.get(chatId).getTime() - conversationStartTimes.get(chatId).getTime();
                if (conversationDuration > longestConversations.getOrDefault(chatId, 0L)) {
                    longestConversations.put(chatId, conversationDuration);
                }
            }

            return longestConversations;
        }


        // 19. Calculate the largest streak of days with communications
        // 19. Calculate the largest streak of days with communications
        public Map<Long, Map.Entry<Integer, Map.Entry<Date, Date>>> getLargestCommunicationStreak() {
            // Sort messages by date
            messages.sort((m1, m2) -> m1.date.compareTo(m2.date));

            Map<Long, Map.Entry<Integer, Map.Entry<Date, Date>>> largestStreaks = new HashMap<>();
            Map<Long, Integer> currentStreaks = new HashMap<>();
            Map<Long, Date> streakStartDates = new HashMap<>();
            Map<Long, Date> lastMessageDates = new HashMap<>();

            for (ChatMessage message : messages) {
                long chatId = message.chatId;
                Date currentDate = message.date;

                // Check if the current message is on the same day as the last message
                if (lastMessageDates.containsKey(chatId)) {
                    Date lastDate = lastMessageDates.get(chatId);
                    long timeDiff = currentDate.getTime() - lastDate.getTime();
                    int daysDiff = (int) (timeDiff / (1000 * 60 * 60 * 24));

                    if (daysDiff == 0) {
                        // Same day: do not increment the streak
                        continue;
                    } else if (daysDiff == 1) {
                        // Consecutive day: increment the streak
                        currentStreaks.put(chatId, currentStreaks.getOrDefault(chatId, 0) + 1);
                    } else if (daysDiff > 1) {
                        // Gap of more than 1 day: reset the streak
                        currentStreaks.put(chatId, 1);
                        streakStartDates.put(chatId, currentDate);
                    }
                } else {
                    // First message for this user: start a new streak
                    currentStreaks.put(chatId, 1);
                    streakStartDates.put(chatId, currentDate);
                }

                // Update the last message date for this user
                lastMessageDates.put(chatId, currentDate);

                // Check if the current streak is the largest
                int currentStreak = currentStreaks.getOrDefault(chatId, 0);
                Map.Entry<Integer, Map.Entry<Date, Date>> currentLargestStreak = largestStreaks.getOrDefault(chatId, null);

                if (currentLargestStreak == null || currentStreak > currentLargestStreak.getKey()) {
                    // Create a new entry with the streak size and the start/end dates
                    Map.Entry<Date, Date> startEndDates = new AbstractMap.SimpleEntry<>(streakStartDates.get(chatId), currentDate);
                    Map.Entry<Integer, Map.Entry<Date, Date>> newStreak = new AbstractMap.SimpleEntry<>(currentStreak, startEndDates);
                    largestStreaks.put(chatId, newStreak);
                }
            }

            return largestStreaks;
        }
        // 20. Get the most used phrases (excluding links, emoji-only phrases, and phrases with 4 or fewer words)


        public Map<Long, Map.Entry<String, Integer>> getMostUsedPhrase(int minPhraseLength) {
            Map<Long, Map<String, Integer>> phraseCounts = new HashMap<>();

            // Regular expression to detect URLs
            Pattern urlPattern = Pattern.compile("https?://\\S+");

            for (ChatMessage message : messages) {
                // Skip messages that contain a URL
                Matcher matcher = urlPattern.matcher(message.content);
                if (matcher.find()) {
                    continue; // Skip this message if it contains a URL
                }

                // Skip messages that contain only emojis
                if (isEmojiOnly(message.content)) {
                    continue; // Skip this message if it contains only emojis
                }

                // Split the message into words
                String[] words = message.content.split("\\s+");

                // Only process messages with more than minPhraseLength words
                if (words.length > minPhraseLength) {
                    // Generate phrases with more than minPhraseLength words
                    for (int i = 0; i <= words.length - minPhraseLength; i++) {
                        StringBuilder phraseBuilder = new StringBuilder();
                        for (int j = 0; j < minPhraseLength; j++) {
                            phraseBuilder.append(words[i + j]).append(" ");
                        }
                        String phrase = phraseBuilder.toString().trim();

                        // Update the phrase count for the user
                        Map<String, Integer> userPhraseCounts = phraseCounts.getOrDefault(message.chatId, new HashMap<>());
                        userPhraseCounts.put(phrase, userPhraseCounts.getOrDefault(phrase, 0) + 1);
                        phraseCounts.put(message.chatId, userPhraseCounts);
                    }
                }
            }

            // Prepare the result with the top phrase for each user
            Map<Long, Map.Entry<String, Integer>> topPhrases = new HashMap<>();
            for (Long chatId : phraseCounts.keySet()) {
                Map<String, Integer> userPhraseCounts = phraseCounts.get(chatId);

                // Sort the phrases by frequency (descending)
                List<Map.Entry<String, Integer>> sortedPhrases = new ArrayList<>(userPhraseCounts.entrySet());
                sortedPhrases.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

                // Take the top phrase
                if (!sortedPhrases.isEmpty()) {
                    topPhrases.put(chatId, sortedPhrases.get(0));
                }
            }

            return topPhrases;
        }
        // Helper method to check if a message contains only emojis
        private boolean isEmojiOnly(String input) {
            if (input == null || input.isEmpty()) {
                return false; // Empty input is not considered emoji-only
            }

            int length = input.codePointCount(0, input.length());
            for (int i = 0; i < length; i++) {
                int codePoint = input.codePointAt(i);
                if (!isEmoji(codePoint) && !Character.isWhitespace(codePoint)) {
                    return false; // Found a non-emoji character
                }
            }
            return true; // Only emojis and whitespace
        }
        // 21. Calculate the average message length for each user
        public Map<Long, Double> getAverageMessageLength() {
            Map<Long, Double> averageLengths = new HashMap<>();
            Map<Long, Integer> totalLengths = new HashMap<>();
            Map<Long, Integer> messageCounts = new HashMap<>();

            for (ChatMessage message : messages) {
                // Skip media files and unwanted placeholders
                if (message.content.equals("<Media/Non-text message>") || message.content.contains("message>")) {
                    continue;
                }

                // Update the total length and message count for the user
                totalLengths.put(message.chatId, totalLengths.getOrDefault(message.chatId, 0) + message.content.length());
                messageCounts.put(message.chatId, messageCounts.getOrDefault(message.chatId, 0) + 1);
            }

            // Calculate the average length for each user
            for (Long chatId : totalLengths.keySet()) {
                int totalLength = totalLengths.get(chatId);
                int count = messageCounts.get(chatId);
                averageLengths.put(chatId, (double) totalLength / count);
            }

            return averageLengths;
        }
        // 22. Calculate the median message length for each user
        public Map<Long, Integer> getMedianMessageLength() {
            Map<Long, List<Integer>> messageLengths = new HashMap<>();

            for (ChatMessage message : messages) {
                // Skip media files and unwanted placeholders
                if (message.content.equals("<Media/Non-text message>") || message.content.contains("message>")) {
                    continue;
                }

                // Add the message length to the user's list
                messageLengths.computeIfAbsent(message.chatId, k -> new ArrayList<>()).add(message.content.length());
            }

            // Calculate the median length for each user
            Map<Long, Integer> medianLengths = new HashMap<>();
            for (Long chatId : messageLengths.keySet()) {
                List<Integer> lengths = messageLengths.get(chatId);
                Collections.sort(lengths);

                int median;
                int size = lengths.size();
                if (size % 2 == 0) {
                    median = (lengths.get(size / 2 - 1) + lengths.get(size / 2)) / 2;
                } else {
                    median = lengths.get(size / 2);
                }

                medianLengths.put(chatId, median);
            }

            return medianLengths;
        }




    }

    // Inner class to represent a chat message
    private static class ChatMessage {
        Date date;
        long chatId;
        String content;

        public ChatMessage(Date date, long chatId, String content) {
            this.date = date;
            this.chatId = chatId;
            this.content = content;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Cancel the countdown if the user returns to the app
        if (disconnectRunnable != null) {
            handler.removeCallbacks(disconnectRunnable);
            disconnectRunnable = null;
            countdownTime = 3 * 60; // Reset the countdown time
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Ensure the Handler is cleared when the activity is destroyed
        if (disconnectRunnable != null) {
            handler.removeCallbacks(disconnectRunnable);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        System.out.println("switcher destroy activated login");

        if (!switcher) {
            // Clear any existing tasks
            handler.removeCallbacks(disconnectRunnable);

            // Create a new Runnable for the countdown
            disconnectRunnable = new Runnable() {
                @Override
                public void run() {
                    if (countdownTime > 0) {
                        // Log the remaining seconds
                        System.out.println("Seconds remaining: " + countdownTime);

                        // Decrement the countdown time
                        countdownTime--;

                        // Schedule the next iteration after 1 second
                        handler.postDelayed(this, 1000); // 1000ms = 1 second
                    } else {
                        // Time's up, disconnect the client
                        Python py = Python.getInstance();
                        PyObject pyObj = py.getModule("helloworld");
                        PyObject result = pyObj.callAttr("terminate_and_disconnect");
                        Toast.makeText(ResultsActivity.this, result.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            };

            // Start the countdown
            handler.post(disconnectRunnable);
        }
    }
    private void loadBannerAd() {
        FrameLayout adContainer = findViewById(R.id.adContainer);
        BannerAdView bannerAdView = findViewById(R.id.bannerAdView);

        // Calculate ad size based on container width and desired max height
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int adWidthPixels = adContainer.getWidth();
        if (adWidthPixels == 0) {
            adWidthPixels = displayMetrics.widthPixels;
        }
        int adWidth = Math.round(adWidthPixels / displayMetrics.density);
        int maxAdHeight = Math.round(80 / displayMetrics.density); // 80dp max height

        BannerAdSize adSize = BannerAdSize.inlineSize(this, adWidth, maxAdHeight);
        bannerAdView.setAdSize(adSize);

        // Set your actual ad unit ID here (replace with your Yandex ad unit ID)
        bannerAdView.setAdUnitId("demo-banner-yandex"); // Use demo ID for testing

        bannerAdView.setBannerAdEventListener(new BannerAdEventListener() {
            @Override
            public void onAdLoaded() {
                Log.d("YandexAds", "Banner ad loaded");
                if (isDestroyed() && bannerAdView != null) {
                    bannerAdView.destroy();
                }
            }

            @Override
            public void onAdFailedToLoad(@NonNull AdRequestError adRequestError) {
                Log.e("YandexAds", "Banner ad failed to load: " + adRequestError.getDescription());
                // Don't try to load a new ad immediately
            }

            @Override
            public void onAdClicked() {
                Log.d("YandexAds", "Banner ad clicked");
            }

            @Override
            public void onLeftApplication() {
                Log.d("YandexAds", "Banner ad left application");
            }

            @Override
            public void onReturnedToApplication() {
                Log.d("YandexAds", "Banner ad returned to application");
            }

            @Override
            public void onImpression(@Nullable ImpressionData impressionData) {
                Log.d("YandexAds", "Banner ad impression recorded");
            }
        });

        // Load the ad
        AdRequest adRequest = new AdRequest.Builder().build();
        bannerAdView.loadAd(adRequest);
    }
    private void ondisconnect(View view) {

        Python py = Python.getInstance();
        PyObject pyObj = py.getModule("helloworld");
        PyObject result = pyObj.callAttr("terminate_and_disconnect");
        Toast.makeText(ResultsActivity.this, R.string.disconnected, Toast.LENGTH_SHORT).show();
        Intent intent777 = new Intent(ResultsActivity.this, Firstscreen.class);
        intent777.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent777);
        finish();

    }
    // Add these to MainActivity.java and all other activities
    private void setLocaleFromPreferences() {
        String languageCode = getSavedLanguage();
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }

    private String getSavedLanguage() {
        SharedPreferences preferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        return preferences.getString("language", "en");
    }

    private void showCardSelectionDialog() {
        CardSelectionDialog dialog = new CardSelectionDialog();
        dialog.setCardSelectionListener(new CardSelectionDialog.CardSelectionListener() {
            @Override
            public void onCardsSelected(List<Integer> selectedCardIds, List<String> sideBySidePairs) {
                ScrollView scrollView = findViewById(R.id.scrollView);

                // Ensure views are measured before capture
                scrollView.post(() -> {
                    Bitmap stitched = partialScrollViewHandler.captureSelectedViews(
                            scrollView,
                            selectedCardIds,
                            sideBySidePairs
                    );

                    if (stitched != null) {
                        shareBitmap(stitched);
                    } else {
                        Toast.makeText(ResultsActivity.this,
                                "Failed to create image", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onAllSelected() {
                ScrollView scrollView = findViewById(R.id.scrollView);
                scrollViewHandler.handleShareButtonClick(scrollView, shareButton);
            }
        });
        dialog.show(getSupportFragmentManager(), "CardSelectionDialog");
    }

    private void shareBitmap(Bitmap bitmap) {
        try {
            File file = new File(getCacheDir(), "shared_stats.png");
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();

            Uri uri = FileProvider.getUriForFile(this,
                    getPackageName() + ".provider", file);

            Intent shareIntent = new Intent(Intent.ACTION_SEND)
                    .setType("image/png")
                    .putExtra(Intent.EXTRA_STREAM, uri)
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(shareIntent, "Share Stats"));
        } catch (IOException e) {
            Toast.makeText(this, "Sharing failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}





