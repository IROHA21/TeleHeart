package com.moon.TeleHeart.shareresults;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.moon.TeleHeart.R;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CardSelectionDialog extends DialogFragment {
    private Map<String, CardPair> cardPairs;


    private Map<String, Integer> individualCards;
    private List<String> selectedCards = new ArrayList<>();
    private CardSelectionListener listener;

    public interface CardSelectionListener {
        void onCardsSelected(List<Integer> selectedCardIds, List<String> sideBySidePairs);
        void onAllSelected();

        void onAllSelectedPdf();
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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeCardMaps();
    }

    private void initializeCardMaps() {
        cardPairs = new LinkedHashMap<>();
        individualCards = new LinkedHashMap<>();

        // Initialize paired cards
        addCardPair(R.string.Number_of_messages, R.id.yourMessagesTextView, R.id.herMessagesTextView, true);
        addCardPair(R.string.Your_Messages_Last_10_Days, R.id.yourLast10DaysTextView, R.id.herLast10DaysTextView, true);
        addCardPair(R.string.Your_Days_with_Most_Messages, R.id.yourDaysMostMessagesChart, R.id.herDaysMostMessagesChart, true);
        addCardPair(R.string.Your_Messages_per_Month, R.id.yourMessagesPerMonthLineChart, R.id.herMessagesPerMonthLineChart, false);
        addCardPair(R.string.Your_Messages_per_Hour_of_the_Day, R.id.yourhourOfDayLineChart, R.id.herhourOfDayLineChart, false);
        addCardPair(R.string.Your_Messages_per_Day_of_the_Week, R.id.yourbarcharts, R.id.herbarcharts, true);
        addCardPair(R.string.Average_Message_Length, R.id.yourAverageMessageLengthTextView, R.id.herAverageMessageLengthTextView, true);
        addCardPair(R.string.Median_Message_Length, R.id.yourMedianMessageLengthTextView, R.id.herMedianMessageLengthTextView, true);
        addCardPair(R.string.Favorite_Emoji, R.id.yourFavoriteEmojiTextView, R.id.herFavoriteEmojiTextView, true);
        addCardPair(R.string.Most_Used_Emojis, R.id.yourMostUsedEmojisChart, R.id.herMostUsedEmojisChart, true);
        addCardPair(R.string.Most_Used_Phrase, R.id.yourMostUsedPhrasesTextView, R.id.herMostUsedPhrasesTextView, true);
        addCardPair(R.string.Most_Used_Words, R.id.yourMostUsedWordsChart, R.id.herMostUsedWordsChart, true);
        addCardPair(R.string.your_Number_of_Links_per_User, R.id.yourLinksPieChart, R.id.herLinksPieChart, false);
        addCardPair(R.string.Number_of_Media_Files, R.id.yourMediaFilesTextView, R.id.herMediaFilesTextView, true);
        addCardPair(R.string.Longest_Message, R.id.your_longest_message_card, R.id.her_longest_message_card, true);
        addCardPair(R.string.Average_Response_Time, R.id.yourAverageTimeTextView, R.id.herAverageTimeTextView, true);
        addCardPair(R.string.Median_Response_Time, R.id.yourMedianAnsweringTimeTextView, R.id.herMedianAnsweringTimeTextView, true);
        addCardPair(R.string.Conversation_Starts, R.id.yourConversationStartsTextView, R.id.herConversationStartsTextView, true);
        addCardPair(R.string.Unreplied_Chats, R.id.yourUnrepliedChatsTextView, R.id.herUnrepliedChatsTextView, true);

        // Initialize individual cards
        addIndividualCard(R.string.Largest_Communication_Streak, R.id.yourLargestCommunicationStreakDatesTextView);
        addIndividualCard(R.string.Largest_No_Conversation_Days, R.id.yourLargestNoConversationDaysTextView);
        addIndividualCard(R.string.Longest_Conversation, R.id.yourLongestConversationTextView);
        addIndividualCard(R.string.Chat_Interest_Meter, R.id.interestMeterChart);
    }

    private void addCardPair(int stringRes, int yourId, int theirId, boolean sideBySide) {
        cardPairs.put(getString(stringRes), new CardPair(yourId, theirId, sideBySide));
    }

    private void addIndividualCard(int stringRes, int viewId) {
        individualCards.put(getString(stringRes), viewId);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle(getString(R.string.Select_cards));

        ScrollView scrollView = new ScrollView(requireContext());
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(layout);

        // Add Select All checkbox
        CheckBox selectAll = new CheckBox(requireContext());
        selectAll.setText(R.string.Select_All);
        selectAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            for (int i = 1; i < layout.getChildCount(); i++) {
                View child = layout.getChildAt(i);
                if (child instanceof CheckBox) {
                    ((CheckBox) child).setChecked(isChecked);
                }
            }
        });
        layout.addView(selectAll);








// In onCreateDialog() after selectAll checkbox
        Button selectAllPdf = new Button(requireContext());
        selectAllPdf.setText(R.string.Select_All_PDF);
        selectAllPdf.setOnClickListener(v -> {
            if (listener != null) listener.onAllSelectedPdf();
            dismiss();
        });
        layout.addView(selectAllPdf);













        // Add paired statistics section
        addSectionHeader(layout, getString(R.string.Paired_Statistics));
        for (String pairName : cardPairs.keySet()) {
            addCheckBox(layout, pairName, selectAll);
        }

        // Add individual statistics section
        addSectionHeader(layout, getString(R.string.Individual_Statistics));
        for (String cardName : individualCards.keySet()) {
            addCheckBox(layout, cardName, selectAll);
        }

        builder.setView(scrollView);
        builder.setPositiveButton(getString(R.string.Share), (dialog, which) -> handleShareAction(selectAll));
        builder.setNegativeButton(getString(R.string.Cancel), null);

        return builder.create();
    }

    private void addSectionHeader(LinearLayout layout, String title) {
        TextView header = new TextView(requireContext());
        header.setText(title);
        header.setTextAppearance(requireContext(), android.R.style.TextAppearance_Medium);
        layout.addView(header);
    }

    private void addCheckBox(LinearLayout layout, String text, CheckBox selectAll) {
        CheckBox checkBox = new CheckBox(requireContext());
        checkBox.setText(text);
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedCards.add(text);
            } else {
                selectedCards.remove(text);
                selectAll.setChecked(false);
            }
        });
        layout.addView(checkBox);
    }

    private void handleShareAction(CheckBox selectAll) {
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

            if (listener != null) {
                listener.onCardsSelected(selectedIds, selectedPairsForSideBySide);
            }
        }
    }

    public void setCardSelectionListener(CardSelectionListener listener) {
        this.listener = listener;
    }
}