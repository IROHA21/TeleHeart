package com.example.lasttele;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResultsActivity extends AppCompatActivity {

    // TextViews to display results
    private TextView yourMessagesTextView, herMessagesTextView;
    private TextView yourAverageTimeTextView, herAverageTimeTextView;
    private TextView yourFavoriteEmojiTextView, herFavoriteEmojiTextView;
    private TextView yourMediaFilesTextView, herMediaFilesTextView;
    private TextView yourLinksTextView, herLinksTextView;
    private TextView yourDayOfWeekTextView, herDayOfWeekTextView;
    private TextView yourHourOfDayTextView, herHourOfDayTextView;
    private TextView yourMonthTextView, herMonthTextView;
    private TextView yourDaysMostMessagesTextView, herDaysMostMessagesTextView;
    private TextView yourLast10DaysTextView, herLast10DaysTextView;
    private TextView yourMostUsedWordsTextView, herMostUsedWordsTextView;
    private TextView yourMostUsedEmojisTextView, herMostUsedEmojisTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.results_screen);

        // Initialize TextViews
        yourMessagesTextView = findViewById(R.id.yourMessagesTextView);
        herMessagesTextView = findViewById(R.id.herMessagesTextView);
        yourAverageTimeTextView = findViewById(R.id.yourAverageTimeTextView);
        herAverageTimeTextView = findViewById(R.id.herAverageTimeTextView);
        yourFavoriteEmojiTextView = findViewById(R.id.yourFavoriteEmojiTextView);
        herFavoriteEmojiTextView = findViewById(R.id.herFavoriteEmojiTextView);
        yourMediaFilesTextView = findViewById(R.id.yourMediaFilesTextView);
        herMediaFilesTextView = findViewById(R.id.herMediaFilesTextView);
        yourLinksTextView = findViewById(R.id.yourLinksTextView);
        herLinksTextView = findViewById(R.id.herLinksTextView);
        yourDayOfWeekTextView = findViewById(R.id.yourDayOfWeekTextView);
        herDayOfWeekTextView = findViewById(R.id.herDayOfWeekTextView);
        yourHourOfDayTextView = findViewById(R.id.yourHourOfDayTextView);
        herHourOfDayTextView = findViewById(R.id.herHourOfDayTextView);
        yourMonthTextView = findViewById(R.id.yourMonthTextView);
        herMonthTextView = findViewById(R.id.herMonthTextView);
        yourDaysMostMessagesTextView = findViewById(R.id.yourDaysMostMessagesTextView);
        herDaysMostMessagesTextView = findViewById(R.id.herDaysMostMessagesTextView);
        yourLast10DaysTextView = findViewById(R.id.yourLast10DaysTextView);
        herLast10DaysTextView = findViewById(R.id.herLast10DaysTextView);
        yourMostUsedWordsTextView = findViewById(R.id.yourMostUsedWordsTextView);
        herMostUsedWordsTextView = findViewById(R.id.herMostUsedWordsTextView);
        yourMostUsedEmojisTextView = findViewById(R.id.yourMostUsedEmojisTextView);
        herMostUsedEmojisTextView = findViewById(R.id.herMostUsedEmojisTextView);

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
            Map<Long, Integer> linkCounts = analyzer.getNumberOfLinksPerUser();
            Map<Long, Map<String, Integer>> dayOfWeekCounts = analyzer.getMessagesPerDayOfWeek();
            Map<Long, Map<Integer, Integer>> hourOfDayCounts = analyzer.getMessagesPerHourOfDay();
            Map<Long, Map<String, Integer>> monthCounts = analyzer.getMessagesPerMonth();
            Map<Long, List<Map.Entry<String, Integer>>> daysWithMostMessages = analyzer.getDaysWithMostMessages();
            Map<Long, Integer> last10DaysCounts = analyzer.getMessagesInLast10Days();
            Map<Long, Map<String, Integer>> mostUsedWords = analyzer.getMostUsedWords();
            Map<Long, Map<String, Integer>> mostUsedEmojis = analyzer.getMostUsedEmojis();

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

            // Update the UI on the main thread
            new Handler(Looper.getMainLooper()).post(() -> {
                // Assuming chatId 1 is "You" and chatId 2 is "Her"
                long yourChatId = 1541937998;
                long herChatId = 1072804297;



                // Number of Messages
                yourMessagesTextView.setText("You: " + messageCounts.getOrDefault(yourChatId, 0));
                herMessagesTextView.setText("Her: " + messageCounts.getOrDefault(herChatId, 0));

                // Average Time to Answer
                yourAverageTimeTextView.setText("You: " + (averageTimes.getOrDefault(yourChatId, 0L) / 1000) + "s");
                herAverageTimeTextView.setText("Her: " + (averageTimes.getOrDefault(herChatId, 0L) / 1000) + "s");

                // Favorite Emoji
                yourFavoriteEmojiTextView.setText("You: " + favoriteEmojis.getOrDefault(yourChatId, ""));
                herFavoriteEmojiTextView.setText("Her: " + favoriteEmojis.getOrDefault(herChatId, ""));

                // Number of Media Files
                yourMediaFilesTextView.setText("You: " + mediaCounts.getOrDefault(yourChatId, 0));
                herMediaFilesTextView.setText("Her: " + mediaCounts.getOrDefault(herChatId, 0));

                // Number of Links
                yourLinksTextView.setText("You: " + linkCounts.getOrDefault(yourChatId, 0));
                herLinksTextView.setText("Her: " + linkCounts.getOrDefault(herChatId, 0));

                // Messages per Day of the Week
                yourDayOfWeekTextView.setText("You: " + formatMap(dayOfWeekCounts.getOrDefault(yourChatId, new HashMap<>())));
                herDayOfWeekTextView.setText("Her: " + formatMap(dayOfWeekCounts.getOrDefault(herChatId, new HashMap<>())));

                // Messages per Hour of the Day
                yourHourOfDayTextView.setText("You: " + formatMap(hourOfDayCounts.getOrDefault(yourChatId, new HashMap<>())));
                herHourOfDayTextView.setText("Her: " + formatMap(hourOfDayCounts.getOrDefault(herChatId, new HashMap<>())));

                // Messages per Month
                yourMonthTextView.setText("You: " + formatMap(monthCounts.getOrDefault(yourChatId, new HashMap<>())));
                herMonthTextView.setText("Her: " + formatMap(monthCounts.getOrDefault(herChatId, new HashMap<>())));

                // Days with Most Messages
                yourDaysMostMessagesTextView.setText("You: " + formatList(daysWithMostMessages.getOrDefault(yourChatId, new ArrayList<>())));
                herDaysMostMessagesTextView.setText("Her: " + formatList(daysWithMostMessages.getOrDefault(herChatId, new ArrayList<>())));

                // Messages in Last 10 Days
                yourLast10DaysTextView.setText("You: " + last10DaysCounts.getOrDefault(yourChatId, 0));
                herLast10DaysTextView.setText("Her: " + last10DaysCounts.getOrDefault(herChatId, 0));

                // Most Used Words
                yourMostUsedWordsTextView.setText("You: " + formatMap(mostUsedWords.getOrDefault(yourChatId, new HashMap<>())));
                herMostUsedWordsTextView.setText("Her: " + formatMap(mostUsedWords.getOrDefault(herChatId, new HashMap<>())));

                // Most Used Emojis
                yourMostUsedEmojisTextView.setText("You: " + formatMap(mostUsedEmojis.getOrDefault(yourChatId, new HashMap<>())));
                herMostUsedEmojisTextView.setText("Her: " + formatMap(mostUsedEmojis.getOrDefault(herChatId, new HashMap<>())));
            });
        }).start();
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
        public Map<Long, Integer> getNumberOfLinksPerUser() {
            Map<Long, Integer> linkCounts = new HashMap<>();
            Pattern linkPattern = Pattern.compile("https?://\\S+");

            for (ChatMessage message : messages) {
                Matcher matcher = linkPattern.matcher(message.content);
                while (matcher.find()) {
                    linkCounts.put(message.chatId, linkCounts.getOrDefault(message.chatId, 0) + 1);
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
}