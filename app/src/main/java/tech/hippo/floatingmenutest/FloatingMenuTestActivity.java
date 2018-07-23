package tech.hippo.floatingmenutest;

import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.util.Arrays;

import tech.hippo.floatingmenu.FloatingMenu;
import tech.hippo.floatingmenu.FloatingMenuAdapter;

public class FloatingMenuTestActivity extends AppCompatActivity implements FloatingMenu.OnFloatingMenuClickListener {

    private static final String LOG_TAG = FloatingMenuTestActivity.class.getSimpleName();
    private FloatingMenuAdapter adapter;
    private FloatingMenu floatingMenu;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_floating_menu_test);

        configureView();
    }

    private void configureView() {

        floatingMenu = findViewById(R.id.floatingMenu);

        adapter = new FloatingMenuAdapter(this, Arrays.asList(getResources().getStringArray(R.array.optionsMenu)), getResources().obtainTypedArray(R.array.drawables));
        floatingMenu.setAdapter(adapter);
        floatingMenu.setOptionTypeface(Typeface.createFromAsset(this.getAssets(), "fonts/Titillium-Bold.otf"));
        floatingMenu.addOnFloatingMenuClickListener(this);

    }

    @Override
    public void onClick(TextView view, int position) {
        Log.d(LOG_TAG, "The position is: " + position + ". And Text is " + view.getText());
    }
}
