package com.moon.TeleHeart.shareresults;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.cardview.widget.CardView;

import com.moon.TeleHeart.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PartialScrollViewHandler {
    private static final int CARD_SPACING = 16;
    private final Context context;
    private final Map<Integer, Integer> cardPairs = new HashMap<Integer, Integer>() {{
        put(R.id.yourMessagesTextView, R.id.herMessagesTextView);
        put(R.id.yourLast10DaysTextView, R.id.herLast10DaysTextView);
        put(R.id.yourDaysMostMessagesChart, R.id.herDaysMostMessagesChart);
        put(R.id.yourMessagesPerMonthLineChart, R.id.herMessagesPerMonthLineChart);
        put(R.id.yourhourOfDayLineChart, R.id.herhourOfDayLineChart);
        put(R.id.yourbarcharts, R.id.herbarcharts);
        put(R.id.yourAverageMessageLengthTextView, R.id.herAverageMessageLengthTextView);
        put(R.id.yourMedianMessageLengthTextView, R.id.herMedianMessageLengthTextView);
        put(R.id.yourFavoriteEmojiTextView, R.id.herFavoriteEmojiTextView);
        put(R.id.yourMostUsedEmojisChart, R.id.herMostUsedEmojisChart);
        put(R.id.yourMostUsedPhrasesTextView, R.id.herMostUsedPhrasesTextView);
        put(R.id.yourMostUsedWordsChart, R.id.herMostUsedWordsChart);
        put(R.id.yourLinksPieChart, R.id.herLinksPieChart);
        put(R.id.yourMediaFilesTextView, R.id.herMediaFilesTextView);
        put(R.id.your_longest_message_card, R.id.her_longest_message_card);
        put(R.id.yourAverageTimeTextView, R.id.herAverageTimeTextView);
        put(R.id.yourMedianAnsweringTimeTextView, R.id.herMedianAnsweringTimeTextView);
        put(R.id.yourConversationStartsTextView, R.id.herConversationStartsTextView);
        put(R.id.yourUnrepliedChatsTextView, R.id.herUnrepliedChatsTextView);
    }};

    public PartialScrollViewHandler(Context context) {
        this.context = context;
    }

    public Bitmap captureSelectedViews(ScrollView scrollView, List<Integer> selectedIds, List<String> sideBySidePairs) {
        // Force layout measurement
        scrollView.measure(
                View.MeasureSpec.makeMeasureSpec(scrollView.getWidth(), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        );
        scrollView.layout(0, 0, scrollView.getMeasuredWidth(), scrollView.getMeasuredHeight());

        ViewGroup container = (ViewGroup) scrollView.getChildAt(0);
        List<View> viewsToCapture = new ArrayList<>();
        List<Integer> processedIds = new ArrayList<>();

        // Process side-by-side pairs
        for (Map.Entry<Integer, Integer> entry : cardPairs.entrySet()) {
            if (selectedIds.contains(entry.getKey()) && selectedIds.contains(entry.getValue())) {
                View yourView = container.findViewById(entry.getKey());
                View theirView = container.findViewById(entry.getValue());

                if (yourView != null && theirView != null) {
                    View yourCard = findParentCardView(yourView);
                    View theirCard = findParentCardView(theirView);

                    if (yourCard != null && theirCard != null) {
                        // Create combined view
                        LinearLayout pairLayout = new LinearLayout(context);
                        pairLayout.setOrientation(LinearLayout.HORIZONTAL);
                        pairLayout.addView(copyView(yourCard));
                        pairLayout.addView(copyView(theirCard));

                        // Measure and layout
                        int width = yourCard.getWidth() + theirCard.getWidth() + CARD_SPACING;
                        int height = Math.max(yourCard.getHeight(), theirCard.getHeight());
                        pairLayout.measure(
                                View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                                View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
                        );
                        pairLayout.layout(0, 0, width, height);

                        viewsToCapture.add(pairLayout);
                        processedIds.add(entry.getKey());
                        processedIds.add(entry.getValue());
                    }
                }
            }
        }

        // Process individual cards
        for (int id : selectedIds) {
            if (!processedIds.contains(id)) {
                View view = container.findViewById(id);
                if (view != null) {
                    View card = findParentCardView(view);
                    if (card != null) {
                        viewsToCapture.add(copyView(card));
                    }
                }
            }
        }

        // Create final bitmap
        int totalHeight = 0;
        for (View view : viewsToCapture) {
            totalHeight += view.getHeight() + CARD_SPACING;
        }
        if (!viewsToCapture.isEmpty()) {
            totalHeight -= CARD_SPACING;
        }

        Bitmap result = Bitmap.createBitmap(
                scrollView.getWidth(),
                totalHeight,
                Bitmap.Config.ARGB_8888
        );
        Canvas canvas = new Canvas(result);

        // Draw all views
        int yPos = 0;
        for (View view : viewsToCapture) {
            canvas.save();
            canvas.translate(0, yPos);
            view.draw(canvas);
            canvas.restore();
            yPos += view.getHeight() + CARD_SPACING;
        }

        return result;
    }

    private View findParentCardView(View view) {
        ViewParent parent = view.getParent();
        while (parent != null) {
            if (parent instanceof CardView) {
                return (View) parent;
            }
            parent = parent.getParent();
        }
        return view;
    }

    private View copyView(View original) {
        Bitmap bitmap = Bitmap.createBitmap(
                original.getWidth(),
                original.getHeight(),
                Bitmap.Config.ARGB_8888
        );
        Canvas canvas = new Canvas(bitmap);
        original.draw(canvas);

        ImageView copy = new ImageView(context);
        copy.setImageBitmap(bitmap);
        copy.setLayoutParams(new ViewGroup.LayoutParams(
                original.getWidth(),
                original.getHeight()
        ));
        return copy;
    }
}