package mo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;

import org.ros.android.robotlab.R;

public class SelectActivity extends Activity {
    private static AlertDialog.Builder builder;
    private static AlertDialog alertDialog;
    private static int selectionIndex;
    private Button select_activity;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_activity);

        select_activity = (Button) findViewById(R.id.select_activity);
        select_activity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog = builder.create();
                alertDialog.setCanceledOnTouchOutside(false);
                alertDialog.show();
            }
        });
        builder = new AlertDialog.Builder(SelectActivity.this);
        builder.setTitle("请选择").setPositiveButton("Ros", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                selectionIndex = 0;
                Intent intent = new Intent(SelectActivity.this, MainActivity.class);
                startActivity(intent);
            }
        }).setNegativeButton("Chat", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                selectionIndex = 1;
                Intent intent = new Intent(SelectActivity.this, ChatbotActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}
