package com.moon.TeleHeart.shareresults;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.moon.TeleHeart.R;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CardSelectionDialog extends DialogFragment {
    public final Map<String, CardPair> cardPairs = new LinkedHashMap<String, CardPair>() {{
        put("Messages Count", new CardPair(R.id.yourMessagesTextView, R.id.herMessagesTextView, true));
        put("Last 10 Days", new CardPair(R.id.yourLast10DaysTextView, R.id.herLast10DaysTextView, true));
        put("Days Most Messages", new CardPair(R.id.yourDaysMostMessagesChart, R.id.herDaysMostMessagesChart, true));
        put("Messages Per Month", new CardPair(R.id.yourMessagesPerMonthLineChart, R.id.herMessagesPerMonthLineChart, false)); // Changed to false
        put("Hour of Day", new CardPair(R.id.yourhourOfDayLineChart, R.id.herhourOfDayLineChart, false)); // Changed to false
        put("Day of Week", new CardPair(R.id.yourbarcharts, R.id.herbarcharts, true));
        put("Average Message Length", new CardPair(R.id.yourAverageMessageLengthTextView, R.id.herAverageMessageLengthTextView, true));
        put("Median Message Length", new CardPair(R.id.yourMedianMessageLengthTextView, R.id.herMedianMessageLengthTextView, true));
        put("Favorite Emoji", new CardPair(R.id.yourFavoriteEmojiTextView, R.id.herFavoriteEmojiTextView, true));
        put("Most Used Emojis", new CardPair(R.id.yourMostUsedEmojisChart, R.id.herMostUsedEmojisChart, true));
        put("Most Used Phrase", new CardPair(R.id.yourMostUsedPhrasesTextView, R.id.herMostUsedPhrasesTextView, true));
        put("Most Used Words", new CardPair(R.id.yourMostUsedWordsChart, R.id.herMostUsedWordsChart, true));
        put("Number of Links", new CardPair(R.id.yourLinksPieChart, R.id.herLinksPieChart, false)); // Changed to false
        put("Number of Media Files", new CardPair(R.id.yourMediaFilesTextView, R.id.herMediaFilesTextView, true));
        put("Longest Message", new CardPair(R.id.your_longest_message_card, R.id.her_longest_message_card, true));
        put("Average Response Time", new CardPair(R.id.yourAverageTimeTextView, R.id.herAverageTimeTextView, true));
        put("Median Response Time", new CardPair(R.id.yourMedianAnsweringTimeTextView, R.id.herMedianAnsweringTimeTextView, true));
        put("Conversation Starts", new CardPair(R.id.yourConversationStartsTextView, R.id.herConversationStartsTextView, true));
        put("Unreplied Chats", new CardPair(R.id.yourUnrepliedChatsTextView, R.id.herUnrepliedChatsTextView, true));
    }};

    private final Map<String, Integer> individualCards = new LinkedHashMap<String, Integer>() {{
        put("Largest Communication Streak", R.id.yourLargestCommunicationStreakDatesTextView);
        put("Largest Days Without Conversation", R.id.yourLargestNoConversationDaysTextView);
        put("Longest Conversation", R.id.yourLongestConversationTextView);
        put("Interest Meter", R.id.interestMeterChart);
    }};

    private List<String> selectedCards = new ArrayList<>();
    private CardSelectionListener listener;

    public interface CardSelectionListener {
        void onCardsSelected(List<Integer> selectedCardIds, List<String> sideBySidePairs);
        void onAllSelected();
    }

    public static class CardPair {
        public final int yourId;
        public final int theirId;
        public final boolean shouldSideBySide;

        public CardPair(int yourId, int theirId, boolean shouldSideBySide) {
            this.yourId = yourId;
            this.theirId = theirId;
            this.shouldSideBySide = shouldSideBySide;
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle("Select Cards to Share");

        ScrollView scrollView = new ScrollView(getContext());
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(layout);

        CheckBox selectAll = new CheckBox(getContext());
        selectAll.setText("Select All");
        selectAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            for (int i = 1; i < layout.getChildCount(); i++) {
                View child = layout.getChildAt(i);
                if (child instanceof CheckBox) {
                    ((CheckBox) child).setChecked(isChecked);
                }
            }
        });
        layout.addView(selectAll);

        TextView pairsHeader = new TextView(getContext());
        pairsHeader.setText("Paired Statistics");
        pairsHeader.setTextAppearance(getContext(), android.R.style.TextAppearance_Medium);
        layout.addView(pairsHeader);

        for (String pairName : cardPairs.keySet()) {
            CheckBox checkBox = new CheckBox(getContext());
            checkBox.setText(pairName);
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedCards.add(pairName);
                } else {
                    selectedCards.remove(pairName);
                    selectAll.setChecked(false);
                }
            });
            layout.addView(checkBox);
        }

        TextView individualHeader = new TextView(getContext());
        individualHeader.setText("Individual Statistics");
        individualHeader.setTextAppearance(getContext(), android.R.style.TextAppearance_Medium);
        layout.addView(individualHeader);

        for (String cardName : individualCards.keySet()) {
            CheckBox checkBox = new CheckBox(getContext());
            checkBox.setText(cardName);
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedCards.add(cardName);
                } else {
                    selectedCards.remove(cardName);
                    selectAll.setChecked(false);
                }
            });
            layout.addView(checkBox);
        }

        builder.setView(scrollView);
        builder.setPositiveButton("Share", (dialog, which) -> {
            if (selectAll.isChecked()) {
                if (listener != null) listener.onAllSelected();
            } else {
                List<Integer> selectedIds = new ArrayList<>();
                List<String> selectedPairsForSideBySide = new ArrayList<>();

                for (String selection : selectedCards) {
                    if (cardPairs.containsKey(selection)) {
                        CardPair pair = cardPairs.get(selection);
                        selectedIds.add(pair.yourId);
                        selectedIds.add(pair.theirId);
                        if (pair.shouldSideBySide) {
                            selectedPairsForSideBySide.add(selection);
                        }
                    } else if (individualCards.containsKey(selection)) {
                        selectedIds.add(individualCards.get(selection));
                    }
                }

                if (listener != null) listener.onCardsSelected(selectedIds, selectedPairsForSideBySide);
            }
        });

        builder.setNegativeButton("Cancel", null);
        return builder.create();
    }

    public void setCardSelectionListener(CardSelectionListener listener) {
        this.listener = listener;
    }
}