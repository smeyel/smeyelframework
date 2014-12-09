package hu.bme.aut.smeyelframework;

import android.app.Activity;
import android.os.Bundle;

/**
 * Simple Activity displaying the author name and other data for the app.
 */
public class CreditsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credits);
    }
}
