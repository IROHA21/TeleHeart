package com.moon.TeleHeart.shareresults;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ScrollView;

import androidx.cardview.widget.CardView;

import com.moon.TeleHeart.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PartialScrollViewHandler {
    private static final int CARD_SPACING = 32;
    private static final int LOGO_PADDING = 10;
    private static final float LOGO_SCALE = 0.15f; // 15% of original size

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

    public List<Bitmap> captureSelectedViewsAsBitmaps(ScrollView scrollView,
                                                      List<Integer> selectedIds,
                                                      List<String> sideBySidePairs) {
        List<Bitmap> bitmaps = new ArrayList<>();
        ViewGroup container = (ViewGroup) scrollView.getChildAt(0);

        // Load and scale logo
        Bitmap logo = loadAndScaleLogo();

        // Process pairs first
        Map<Integer, Boolean> processedIds = new HashMap<>();
        for (Map.Entry<Integer, Integer> entry : cardPairs.entrySet()) {
            if (selectedIds.contains(entry.getKey()) && selectedIds.contains(entry.getValue())) {
                View view1 = container.findViewById(entry.getKey());
                View view2 = container.findViewById(entry.getValue());

                if (view1 != null && view2 != null) {
                    View card1 = findParentCardView(view1);
                    View card2 = findParentCardView(view2);

                    if (card1 != null && card2 != null) {
                        bitmaps.add(createPairBitmapWithLogo(
                                card1,
                                card2,
                                shouldStackVertically(entry.getKey()),
                                logo
                        ));
                        processedIds.put(entry.getKey(), true);
                        processedIds.put(entry.getValue(), true);
                    }
                }
            }
        }

        // Process individual cards
        for (int id : selectedIds) {
            if (!processedIds.containsKey(id)) {
                View view = container.findViewById(id);
                if (view != null) {
                    View card = findParentCardView(view);
                    if (card != null) {
                        bitmaps.add(createSingleBitmapWithLogo(card, logo));
                    }
                }
            }
        }

        if (logo != null) {
            logo.recycle();
        }
        return bitmaps;
    }

    private Bitmap loadAndScaleLogo() {
        try {
            Bitmap logo = BitmapFactory.decodeResource(context.getResources(), R.drawable.legend_logo);
            if (logo != null) {
                int scaledWidth = (int)(logo.getWidth() * LOGO_SCALE);
                int scaledHeight = (int)(logo.getHeight() * LOGO_SCALE);
                return Bitmap.createScaledBitmap(logo, scaledWidth, scaledHeight, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Bitmap createPairBitmapWithLogo(View view1, View view2, boolean stackVertically, Bitmap logo) {
        // Calculate content dimensions
        int contentWidth, contentHeight;
        if (stackVertically) {
            contentWidth = Math.max(view1.getWidth(), view2.getWidth());
            contentHeight = view1.getHeight() + view2.getHeight() + CARD_SPACING;
        } else {
            contentWidth = view1.getWidth() + view2.getWidth() + CARD_SPACING;
            contentHeight = Math.max(view1.getHeight(), view2.getHeight());
        }

        // Calculate total dimensions with logo
        int logoHeight = logo != null ? logo.getHeight() : 0;
        int totalHeight = contentHeight + (logo != null ? (logoHeight + LOGO_PADDING) : 0);
        int totalWidth = Math.max(contentWidth, logo != null ? logo.getWidth() : 0);

        Bitmap bitmap = Bitmap.createBitmap(totalWidth, totalHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Draw the card pair
        if (stackVertically) {
            // Draw first card
            view1.draw(canvas);

            // Draw second card below first card
            canvas.translate(0, view1.getHeight() + CARD_SPACING);
            view2.draw(canvas);
            canvas.translate(0, -(view1.getHeight() + CARD_SPACING)); // Reset position

            // Draw logo at the very bottom
            if (logo != null) {
                canvas.drawBitmap(logo,
                        (totalWidth - logo.getWidth()) / 2f,
                        contentHeight + LOGO_PADDING,
                        null);
            }
        } else {
            // Original horizontal layout code
            view1.draw(canvas);
            canvas.translate(view1.getWidth() + CARD_SPACING, 0);
            view2.draw(canvas);
            canvas.translate(-(view1.getWidth() + CARD_SPACING), 0); // Reset

            if (logo != null) {
                canvas.drawBitmap(logo,
                        (totalWidth - logo.getWidth()) / 2f,
                        contentHeight + LOGO_PADDING,
                        null);
            }
        }

        return bitmap;
    }

    private Bitmap createSingleBitmapWithLogo(View view, Bitmap logo) {
        // Calculate dimensions with logo space
        int logoHeight = logo != null ? logo.getHeight() : 0;
        int totalHeight = view.getHeight() + (logo != null ? (logoHeight + LOGO_PADDING) : 0);
        int totalWidth = Math.max(view.getWidth(), logo != null ? logo.getWidth() : 0);

        Bitmap bitmap = Bitmap.createBitmap(totalWidth, totalHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Draw the card
        view.draw(canvas);

        // Draw logo if available
        if (logo != null) {
            canvas.drawBitmap(logo,
                    (totalWidth - logo.getWidth()) / 2f,
                    view.getHeight() + LOGO_PADDING,
                    null);
        }

        return bitmap;
    }

    private boolean shouldStackVertically(int viewId) {
        return viewId == R.id.yourLinksPieChart ||
                viewId == R.id.yourhourOfDayLineChart ||
                viewId == R.id.yourMessagesPerMonthLineChart;
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
}