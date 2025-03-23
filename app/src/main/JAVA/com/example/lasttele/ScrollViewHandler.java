package com.example.lasttele;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ScrollViewHandler {

    private static final int TARGET_COLUMNS = 5;
    private static final int CARD_SPACING = 16;
    private static final float FULL_WIDTH_THRESHOLD = 0.75f;

    private final Context context;

    public ScrollViewHandler(Context context) {
        this.context = context;
    }

    public List<Bitmap> captureCardViews(ScrollView scrollView) {
        List<Bitmap> bitmaps = new ArrayList<>();
        ViewGroup container = (ViewGroup) scrollView.getChildAt(0);

        container.measure(
                View.MeasureSpec.makeMeasureSpec(scrollView.getWidth(), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        );
        container.layout(0, 0, container.getMeasuredWidth(), container.getMeasuredHeight());

        for (int i = 0; i < container.getChildCount(); i++) {
            View childView = container.getChildAt(i);

            // Skip buttons and the "Chat Statistics" TextView
            if (childView instanceof Button || isChatStatisticsTextView(childView)) {
                continue;
            }

            if (childView instanceof GridLayout) {
                GridLayout gridLayout = (GridLayout) childView;
                for (int j = 0; j < gridLayout.getChildCount(); j++) {
                    View gridChild = gridLayout.getChildAt(j);
                    captureViewAsBitmap(gridChild, bitmaps);
                }
            } else {
                captureViewAsBitmap(childView, bitmaps);
            }
        }
        return bitmaps;
    }

    /**
     * Checks if the view is the "Chat Statistics" TextView.
     */
    private boolean isChatStatisticsTextView(View view) {
        if (view instanceof TextView) {
            TextView textView = (TextView) view;
            String text = textView.getText().toString();
            return text.equals(context.getString(R.string.Chat_Statistics));
        }
        return false;
    }

    private void captureViewAsBitmap(View view, List<Bitmap> bitmaps) {
        view.measure(
                View.MeasureSpec.makeMeasureSpec(view.getWidth(), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(view.getHeight(), View.MeasureSpec.EXACTLY)
        );
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());

        view.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);

        if (bitmap != null) {
            bitmaps.add(bitmap);
        }
    }

    public Bitmap stitchIntelligentColumns(List<Bitmap> bitmaps) {
        if (bitmaps.isEmpty()) return null;

        List<CardGroup> groups = createCardGroups(bitmaps);
        List<List<CardGroup>> sections = splitByHeightWithHourlyShift(groups, TARGET_COLUMNS);
        List<Bitmap> columns = createColumnBitmaps(sections);
        return stitchColumns(columns);
    }

    private List<CardGroup> createCardGroups(List<Bitmap> bitmaps) {
        List<CardGroup> groups = new ArrayList<>();
        int i = 0;

        while (i < bitmaps.size()) {
            Bitmap current = bitmaps.get(i);
            boolean isFullWidth = current.getWidth() > getScreenWidth() * FULL_WIDTH_THRESHOLD;

            if (isFullWidth) {
                groups.add(new CardGroup(current, true));
                i++;
            } else {
                // Always pair non-full-width cards horizontally
                if (i + 1 < bitmaps.size()) {
                    groups.add(new CardGroup(current, bitmaps.get(i + 1)));
                    i += 2;
                } else {
                    groups.add(new CardGroup(current, false));
                    i++;
                }
            }
        }
        return groups;
    }

    /**
     * Splits groups into sections, ensuring the second "Messages per Hour of the Day" card
     * starts a new column for better balance.
     */
    private List<List<CardGroup>> splitByHeightWithHourlyShift(List<CardGroup> groups, int target) {
        List<List<CardGroup>> sections = new ArrayList<>();
        int totalHeight = groups.stream().mapToInt(g -> g.height).sum();
        int sectionHeight = totalHeight / target;

        List<CardGroup> current = new ArrayList<>();
        int currentHeight = 0;
        boolean hourlyCardFound = false;

        for (CardGroup group : groups) {
            // Check if this is the second "Messages per Hour of the Day" card
            if (isHourlyCard(group) && hourlyCardFound) {
                // Start a new column for the second hourly card
                sections.add(current);
                current = new ArrayList<>();
                currentHeight = 0;
            }

            if (isHourlyCard(group)) {
                hourlyCardFound = true;
            }

            if (currentHeight + group.height > sectionHeight && !current.isEmpty()) {
                sections.add(current);
                current = new ArrayList<>();
                currentHeight = 0;
            }
            current.add(group);
            currentHeight += group.height + CARD_SPACING;
        }
        if (!current.isEmpty()) sections.add(current);

        while (sections.size() > target) {
            mergeSmallestSections(sections);
        }
        return sections;
    }

    /**
     * Checks if a CardGroup represents a "Messages per Hour of the Day" card.
     */
    private boolean isHourlyCard(CardGroup group) {
        // Assuming the "Messages per Hour of the Day" cards are full-width
        return group.isFullWidth && group.height > getScreenWidth() * 0.5f; // Adjust threshold as needed
    }

    private void mergeSmallestSections(List<List<CardGroup>> sections) {
        int minIndex = 0;
        for (int i = 1; i < sections.size(); i++) {
            if (sections.get(i).size() < sections.get(minIndex).size()) {
                minIndex = i;
            }
        }
        if (minIndex > 0) {
            sections.get(minIndex - 1).addAll(sections.remove(minIndex));
        } else {
            sections.get(0).addAll(sections.remove(1));
        }
    }

    private List<Bitmap> createColumnBitmaps(List<List<CardGroup>> sections) {
        List<Bitmap> columns = new ArrayList<>();
        for (List<CardGroup> section : sections) {
            Bitmap column = stitchVertically(section);
            if (column != null) columns.add(column);
        }
        return columns;
    }

    private Bitmap stitchVertically(List<CardGroup> groups) {
        int width = groups.stream().mapToInt(g -> g.width).max().orElse(0);
        int height = groups.stream().mapToInt(g -> g.height + CARD_SPACING).sum() - CARD_SPACING;

        if (width <= 0 || height <= 0) return null;

        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        int y = 0;

        for (CardGroup group : groups) {
            canvas.drawBitmap(group.bitmap, 0, y, null);
            y += group.height + CARD_SPACING;
        }
        return result;
    }

    private Bitmap stitchColumns(List<Bitmap> columns) {
        int totalWidth = columns.stream().mapToInt(b -> b.getWidth() + CARD_SPACING).sum() - CARD_SPACING;
        int maxHeight = columns.stream().mapToInt(b -> b.getHeight()).max().orElse(0);

        if (totalWidth <= 0 || maxHeight <= 0) return null;

        Bitmap result = Bitmap.createBitmap(totalWidth, maxHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        int x = 0;

        for (Bitmap col : columns) {
            canvas.drawBitmap(col, x, 0, null);
            x += col.getWidth() + CARD_SPACING;
        }
        return result;
    }

    private static class CardGroup {
        final Bitmap bitmap;
        final int width;
        final int height;
        final boolean isFullWidth;

        CardGroup(Bitmap bitmap, boolean isFullWidth) {
            this.bitmap = bitmap;
            this.width = bitmap.getWidth();
            this.height = bitmap.getHeight();
            this.isFullWidth = isFullWidth;
        }

        CardGroup(Bitmap first, Bitmap second) {
            int spacing = CARD_SPACING;
            this.width = first.getWidth() + spacing + second.getWidth();
            this.height = Math.max(first.getHeight(), second.getHeight());
            this.isFullWidth = false;

            this.bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawBitmap(first, 0, 0, null);
            canvas.drawBitmap(second, first.getWidth() + spacing, 0, null);
        }
    }

    public void handleShareButtonClick(ScrollView scrollView, Button shareButton) {
        shareButton.setVisibility(View.GONE);
        try {
            Bitmap stitched = stitchIntelligentColumns(captureCardViews(scrollView));
            if (stitched != null) {
                shareBitmap(stitched);
            } else {
                Toast.makeText(context, "Failed to create image", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("ScrollViewHandler", "Error sharing", e);
            Toast.makeText(context, "Sharing failed", Toast.LENGTH_SHORT).show();
        } finally {
            shareButton.setVisibility(View.VISIBLE);
        }
    }

    private void shareBitmap(Bitmap bitmap) {
        try {
            File file = new File(context.getCacheDir(), "share.png");
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();

            Uri uri = FileProvider.getUriForFile(context,
                    context.getPackageName() + ".provider", file);

            Intent intent = new Intent(Intent.ACTION_SEND)
                    .setType("image/png")
                    .putExtra(Intent.EXTRA_STREAM, uri)
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            context.startActivity(Intent.createChooser(intent, "Share Stats"));
        } catch (IOException e) {
            Toast.makeText(context, "Sharing failed", Toast.LENGTH_SHORT).show();
        }
    }

    private int getScreenWidth() {
        return context.getResources().getDisplayMetrics().widthPixels;
    }
}