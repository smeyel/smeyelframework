package hu.bme.aut.smeyelframework;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created on 2014.10.24..
 *
 * @author Ákos Pap
 */
public class BaseActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SMEyeLFrameworkApplication.activityBorn(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SMEyeLFrameworkApplication.activityDied(this);
    }

}
