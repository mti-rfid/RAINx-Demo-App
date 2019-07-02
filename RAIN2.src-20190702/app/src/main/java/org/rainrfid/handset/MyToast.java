package org.rainrfid.handset;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.rainrfid.handset.R;

public class MyToast {

    private static Toast toast;

    private MyToast() {

    }

    public static void show(Context context, int msgId) {
        cancel();
        View view = LayoutInflater.from(context).inflate(R.layout.toast, null);
        TextView text = view.findViewById(R.id.text_view_toast);
        text.setText(msgId);
        toast = new Toast(context);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(view);
        toast.show();
    }

    public static void cancel() {
        if (toast != null) {
            toast.cancel();
        }
    }
}
