package com.example.lasttele;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

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
import com.google.android.material.bottomsheet.BottomSheetDialog;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResultsActivity extends AppCompatActivity {

    // TextViews to display results
    private TextView yourMessagesTextView, herMessagesTextView;
    private TextView yourAverageTimeTextView, herAverageTimeTextView;
    private TextView yourFavoriteEmojiTextView, herFavoriteEmojiTextView;
    private TextView yourMediaFilesTextView, herMediaFilesTextView;
    // Pie charts for number of links per user
    private PieChart yourLinksPieChart;
    private PieChart herLinksPieChart;
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

    // Longest messages
    private TextView herlongestmesssage, yourlongestmesssage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.results_screen);

        Intent intent3 = getIntent();
        switcher = intent3.getBooleanExtra("switcher", false);

        System.out.println("switcher check inside of result : " + switcher);

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

            // Update the UI on the main thread
            new Handler(Looper.getMainLooper()).post(() -> {
                // Assuming chatId 1 is "You" and chatId 2 is "Her"
                Intent intent = getIntent();
                String selectedContactId = intent.getStringExtra("selectedContactId");
                long selectedContactId2 = Long.parseLong(selectedContactId);

                long herChatId = selectedContactId2;

                String user_id = intent.getStringExtra("user_id");
                long longid = Long.parseLong(user_id);
                long yourChatId = longid;

                // Number of Messages
                yourMessagesTextView.setText("You: " + messageCounts.getOrDefault(yourChatId, 0));
                herMessagesTextView.setText("Them: " + messageCounts.getOrDefault(herChatId, 0));

                // Average Time to Answer
                yourAverageTimeTextView.setText("You: " + (averageTimes.getOrDefault(yourChatId, 0L) / 1000) + "s");
                herAverageTimeTextView.setText("Them: " + (averageTimes.getOrDefault(herChatId, 0L) / 1000) + "s");

                // Favorite Emoji
                yourFavoriteEmojiTextView.setText("You: " + favoriteEmojis.getOrDefault(yourChatId, ""));
                herFavoriteEmojiTextView.setText("Them: " + favoriteEmojis.getOrDefault(herChatId, ""));

                // Number of Media Files
                yourMediaFilesTextView.setText("You: " + mediaCounts.getOrDefault(yourChatId, 0));
                herMediaFilesTextView.setText("Them: " + mediaCounts.getOrDefault(herChatId, 0));

                // Set up Number of Links per User pie charts
                // Set up Number of Links per User pie charts

                setupLinksPieChart(yourLinksPieChart, linkCounts.getOrDefault(yourChatId, new HashMap<>()), "You");
                setupLinksPieChart(herLinksPieChart, linkCounts.getOrDefault(herChatId, new HashMap<>()), "Them");
                // Messages per Day of the Week
                setupBarChart(yourBarChart, dayOfWeekCounts.getOrDefault(yourChatId, new HashMap<>()), "You");
                setupBarChart(herBarChart, dayOfWeekCounts.getOrDefault(herChatId, new HashMap<>()), "Them");

                // Messages per Hour of the Day
                setupHourOfDayLineChart(yourHourOfDayLineChart, hourOfDayCounts.getOrDefault(yourChatId, new HashMap<>()), "You", Color.BLUE);
                setupHourOfDayLineChart(herHourOfDayLineChart, hourOfDayCounts.getOrDefault(herChatId, new HashMap<>()), "Them", Color.parseColor("#800080"));

                // Set up Messages per Month line charts
                setupMessagesPerMonthLineChart(yourMessagesPerMonthLineChart, monthCounts.getOrDefault(yourChatId, new HashMap<>()), "You", Color.BLUE);
                setupMessagesPerMonthLineChart(herMessagesPerMonthLineChart, monthCounts.getOrDefault(herChatId, new HashMap<>()), "Them", Color.parseColor("#800080"));
                // Days with Most Messages
                // Set up Days with the Most Messages charts
                setupDaysMostMessagesChart(yourDaysMostMessagesChart, daysWithMostMessages.getOrDefault(yourChatId, new ArrayList<>()), "You");
                setupDaysMostMessagesChart(herDaysMostMessagesChart, daysWithMostMessages.getOrDefault(herChatId, new ArrayList<>()), "Them");
                // Messages in Last 10 Days
                yourLast10DaysTextView.setText("You: " + last10DaysCounts.getOrDefault(yourChatId, 0));
                herLast10DaysTextView.setText("Them: " + last10DaysCounts.getOrDefault(herChatId, 0));



                // Set up Most Used Words charts
                setupMostUsedWordsChart(yourMostUsedWordsChart, mostUsedWords.getOrDefault(yourChatId, new HashMap<>()), "You");
                setupMostUsedWordsChart(herMostUsedWordsChart, mostUsedWords.getOrDefault(herChatId, new HashMap<>()), "Them");

                // Longest message
                String yourLongestMessage = longestMessages.getOrDefault(yourChatId, "N/A");
                yourlongestmesssage.setText("You: " + yourLongestMessage.length() + " characters");

                String herLongestMessage = longestMessages.getOrDefault(herChatId, "N/A");
                herlongestmesssage.setText("Them: " + herLongestMessage.length() + " characters");

                // Set up Most Used Emojis charts
                setupMostUsedEmojisChart(yourMostUsedEmojisChart, mostUsedEmojis.getOrDefault(yourChatId, new HashMap<>()), "You");
                setupMostUsedEmojisChart(herMostUsedEmojisChart, mostUsedEmojis.getOrDefault(herChatId, new HashMap<>()), "Them");

                // Set click listeners for the CardViews
                CardView yourLongestMessageCard = findViewById(R.id.your_longest_message_card);
                CardView herLongestMessageCard = findViewById(R.id.her_longest_message_card);

                yourLongestMessageCard.setOnClickListener(v -> showOverlay(yourLongestMessage));
                herLongestMessageCard.setOnClickListener(v -> showOverlay(herLongestMessage));
            });
        }).start();
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
                Color.rgb(255, 102, 102), // Light red
                Color.rgb(102, 178, 255), // Light blue
                Color.rgb(255, 178, 102), // Light orange
                Color.rgb(102, 255, 178), // Light green
                Color.rgb(178, 102, 255), // Light purple
                Color.rgb(255, 255, 102), // Light yellow
                Color.rgb(102, 255, 255), // Light cyan
                Color.rgb(255, 102, 255)  // Light magenta
        };

        // Use the custom colors for the PieDataSet
        pieDataSet.setColors(customColors);

        pieDataSet.setValueTextColor(Color.BLACK); // Set text color for values
        pieDataSet.setValueTextSize(12f); // Set text size for values

        // Create a PieData object with the PieDataSet
        PieData pieData = new PieData(pieDataSet);
        pieChart.setData(pieData);

        // Customize the chart
        pieChart.getDescription().setEnabled(false); // Disable description
        pieChart.setDrawHoleEnabled(true); // Enable a hole in the center of the pie chart
        pieChart.setHoleRadius(30f); // Set the radius of the hole
        pieChart.setTransparentCircleRadius(35f); // Set the radius of the transparent circle
        pieChart.setEntryLabelColor(Color.BLACK); // Set the color of the entry labels
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
        barDataSet.setColor(barChart == yourDaysMostMessagesChart ? Color.parseColor("#1C3B9B") : Color.parseColor("#800080")); // Set bar color based on chart
        barDataSet.setValueTextColor(Color.BLACK); // Set text color for values
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
    // Helper method to set up the Messages per Month line chart
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
        dataSet.setLineWidth(2f); // Set line width
        dataSet.setCircleRadius(4f); // Set circle radius
        dataSet.setValueTextSize(10f); // Set value text size

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
        xAxis.setLabelRotationAngle(90); // Rotate labels vertically
        lineChart.setExtraBottomOffset(20f);

        // Configure Y-axis
        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f); // Start Y-axis from 0
        leftAxis.setGranularity(1f); // Set granularity to 1 message
        leftAxis.setDrawGridLines(true); // Enable grid lines for Y-axis

        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(false); // Disable right Y-axis

        // Disable the legend (if any)
        lineChart.getLegend().setEnabled(true); // Enable legend to show the label

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
        barDataSet.setColor(barChart == yourMostUsedWordsChart ? Color.parseColor("#1C3B9B") : Color.parseColor("#800080")); // Set bar color based on chart
        barDataSet.setValueTextColor(Color.BLACK); // Set text color for values
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
        barDataSet.setColor(barChart == yourMostUsedEmojisChart ? Color.parseColor("#1C3B9B") : Color.parseColor("#800080")); // Set bar color based on chart
        barDataSet.setValueTextColor(Color.BLACK); // Set text color for values
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
        barDataSet.setColor(barChart == yourBarChart ? Color.parseColor("#1C3B9B") : Color.parseColor("#800080")); // Set bar color based on chart
        barDataSet.setValueTextColor(Color.BLACK); // Set text color for values
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
        dataSet.setLineWidth(2f); // Set line width
        dataSet.setCircleRadius(4f); // Set circle radius
        dataSet.setValueTextSize(10f); // Set value text size

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

        // Configure Y-axis
        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f); // Start Y-axis from 0
        leftAxis.setGranularity(1f); // Set granularity to 1 message
        leftAxis.setDrawGridLines(true); // Enable grid lines for Y-axis

        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(false); // Disable right Y-axis

        // Disable the legend (if any)
        lineChart.getLegend().setEnabled(true); // Enable legend to show the label

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

        // 5. Number of links per user
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

}
