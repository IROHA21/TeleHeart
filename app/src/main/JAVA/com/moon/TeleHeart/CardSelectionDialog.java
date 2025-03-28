package com.moon.TeleHeart;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.View;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class CardViewHandler {
    private final Context context;

    public CardViewHandler(Context context) {
        this.context = context;
    }

    public void shareSingleCard(View cardView, String fileName) {
        try {
            Bitmap bitmap = captureViewAsBitmap(cardView);
            if (bitmap != null) {
                shareBitmap(bitmap, fileName);
            } else {
                Toast.makeText(context, "Failed to create image", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(context, "Sharing failed", Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap captureViewAsBitmap(View view) {
        view.setDrawingCacheEnabled(true);
        view.measure(
                View.MeasureSpec.makeMeasureSpec(view.getWidth(), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(view.getHeight(), View.MeasureSpec.EXACTLY)
        );
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());

        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);
        return bitmap;
    }

    private void shareBitmap(Bitmap bitmap, String fileName) {
        try {
            File file = new File(context.getCacheDir(), fileName + ".png");
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();

            Uri uri = FileProvider.getUriForFile(context,
                    context.getPackageName() + ".provider", file);

            Intent intent = new Intent(Intent.ACTION_SEND)
                    .setType("image/png")
                    .putExtra(Intent.EXTRA_STREAM, uri)
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            context.startActivity(Intent.createChooser(intent, "Share Card"));
        } catch (IOException e) {
            Toast.makeText(context, "Sharing failed", Toast.LENGTH_SHORT).show();
        }
    }
}