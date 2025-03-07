package com.example.lasttele;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResultsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.results_screen);

        // TextViews to display results
        TextView resultsTextView = findViewById(R.id.resultsTextView);
        TextView averageTimeTextView = findViewById(R.id.averageTimeTextView);
        TextView favoriteEmojiTextView = findViewById(R.id.favoriteEmojiTextView);
        TextView numberOfMessagesTextView = findViewById(R.id.numberOfMessagesTextView);

        // Get the total characters and messages from the intent
        int totalCharacters = getIntent().getIntExtra("totalCharacters", 0);
        ArrayList<String> messages = getIntent().getStringArrayListExtra("messages");

        // Display the total characters
        resultsTextView.setText("Total characters: " + totalCharacters);

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

        // Display the results
        StringBuilder results = new StringBuilder();
        for (Long chatId : messageCounts.keySet()) {
            results.append("User ").append(chatId).append(":\n");
            results.append("Number of messages: ").append(messageCounts.get(chatId)).append("\n");
            results.append("Average time to answer: ").append(averageTimes.get(chatId) / 1000).append(" seconds\n");
            results.append("Favorite emoji: ").append(favoriteEmojis.get(chatId)).append("\n");
            results.append("Number of media files: ").append(mediaCounts.get(chatId)).append("\n");
            results.append("Number of links: ").append(linkCounts.get(chatId)).append("\n");
            results.append("Messages per day of the week: ").append(dayOfWeekCounts.get(chatId)).append("\n");
            results.append("Messages per hour of the day: ").append(hourOfDayCounts.get(chatId)).append("\n");
            results.append("Messages per month: ").append(monthCounts.get(chatId)).append("\n");
            results.append("Days with most messages: ").append(daysWithMostMessages.get(chatId)).append("\n");
            results.append("Messages in last 10 days: ").append(last10DaysCounts.get(chatId)).append("\n");
            results.append("Most used words: ").append(mostUsedWords.get(chatId)).append("\n");
            results.append("Most used emojis: ").append(mostUsedEmojis.get(chatId)).append("\n\n");
        }

        averageTimeTextView.setText("Average Time to Answer: Calculated");
        favoriteEmojiTextView.setText("Favorite Emoji: Calculated");
        numberOfMessagesTextView.setText("Number of Messages: Calculated");

        // Display the detailed results in the main TextView
        resultsTextView.append("\n\nAnalysis Results:\n" + results.toString());
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
            long maxConversationGap = 60 * 60 * 1000; // 1 hour in milliseconds

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
                String[] words = message.content.split(" ");
                for (String word : words) {
                    if (isEmoji(word)) {
                        userEmojiCounts.put(word, userEmojiCounts.getOrDefault(word, 0) + 1);
                    }
                }
                emojiCounts.put(message.chatId, userEmojiCounts);
            }

            Map<Long, String> favoriteEmojis = new HashMap<>();
            for (Long chatId : emojiCounts.keySet()) {
                String favoriteEmoji = emojiCounts.get(chatId).entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .map(Map.Entry::getKey)
                        .orElse("");
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

        // 9. Days with the most messages (sorted from highest to lowest)
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
                sortedList.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));
                sortedDays.put(chatId, sortedList);
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

        // 11. Most used words (top 10)
        public Map<Long, Map<String, Integer>> getMostUsedWords() {
            Map<Long, Map<String, Integer>> wordCounts = new HashMap<>();

            for (ChatMessage message : messages) {
                Map<String, Integer> userWordCounts = wordCounts.getOrDefault(message.chatId, new HashMap<>());
                String[] words = message.content.split("\\s+");
                for (String word : words) {
                    // Ignore empty words and media placeholders
                    if (!word.isEmpty() && !word.equals("<Media/Non-text message>")) {
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

        // 12. Most used emojis
        public Map<Long, Map<String, Integer>> getMostUsedEmojis() {
            Map<Long, Map<String, Integer>> emojiCounts = new HashMap<>();

            for (ChatMessage message : messages) {
                Map<String, Integer> userEmojiCounts = emojiCounts.getOrDefault(message.chatId, new HashMap<>());
                String[] words = message.content.split(" ");
                for (String word : words) {
                    if (isEmoji(word)) {
                        userEmojiCounts.put(word, userEmojiCounts.getOrDefault(word, 0) + 1);
                    }
                }
                emojiCounts.put(message.chatId, userEmojiCounts);
            }

            return emojiCounts;
        }

        // Helper method to check if a string is a single emoji
        private boolean isEmoji(String input) {
            if (input == null || input.isEmpty()) {
                return false;
            }

            // Check if the input is a single character and falls within emoji Unicode ranges
            if (input.codePointCount(0, input.length()) == 1) {
                int codePoint = input.codePointAt(0);
                return (codePoint >= 0x1F600 && codePoint <= 0x1F64F) || // Emoticons
                        (codePoint >= 0x1F300 && codePoint <= 0x1F5FF) || // Misc Symbols and Pictographs
                        (codePoint >= 0x1F680 && codePoint <= 0x1F6FF) || // Transport and Map Symbols
                        (codePoint >= 0x2600 && codePoint <= 0x26FF) ||   // Misc Symbols
                        (codePoint >= 0x2700 && codePoint <= 0x27BF) ||   // Dingbats
                        (codePoint >= 0xFE00 && codePoint <= 0xFE0F) ||   // Variation Selectors
                        (codePoint >= 0x1F900 && codePoint <= 0x1F9FF) || // Supplemental Symbols and Pictographs
                        (codePoint >= 0x1F1E6 && codePoint <= 0x1F1FF);   // Flags
            }

            return false;
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