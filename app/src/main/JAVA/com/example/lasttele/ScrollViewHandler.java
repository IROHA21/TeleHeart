package com.example.lasttele;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.util.Log;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ScrollViewHandler {

    private Context context;

    public ScrollViewHandler(Context context) {
        this.context = context;
    }

    public Bitmap getScrollViewBitmap(ScrollView scrollView) {
        int totalHeight = scrollView.getChildAt(0).getHeight();
        int totalWidth = scrollView.getChildAt(0).getWidth();

        Bitmap bitmap = Bitmap.createBitmap(totalWidth, totalHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        scrollView.draw(canvas);

        return bitmap;
    }

    public void shareBitmapDirectly(Bitmap bitmap) {
        // Convert the bitmap to a byte array
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes);

        // Save the bitmap to the cache directory
        File file = new File(context.getCacheDir(), "shareable_chat_stats.png");
        Log.d("ScrollViewHandler", "File path: " + file.getAbsolutePath());

        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(bytes.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Failed to save image", Toast.LENGTH_SHORT).show();
            return;
        }

        // Share the file using an Intent
        Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
        Log.d("ScrollViewHandler", "File URI: " + uri.toString());

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/png");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(Intent.createChooser(intent, "Share via"));
    }

    public void handleShareButtonClick(ScrollView scrollView) {
        Bitmap bitmap = getScrollViewBitmap(scrollView);
        if (bitmap != null) {
            shareBitmapDirectly(bitmap);
        } else {
            Toast.makeText(context, "Failed to capture image", Toast.LENGTH_SHORT).show();
        }
    }
}