package es.biobuddi.fillet;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.autofill.AutofillManager;
import android.widget.TextView;
import java.util.stream.Collectors;

/** Request the autofill service setting, then list hard-coded profiles. */
public class EnableActivity extends Activity {

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        if (!getSystemService(AutofillManager.class).hasEnabledAutofillServices()) {
            startActivity(
                new Intent(Settings.ACTION_REQUEST_SET_AUTOFILL_SERVICE).setData(
                    Uri.parse("package:" + getPackageName())
                )
            );
        }
        var text = new TextView(this);
        text.setPadding(48, 48, 48, 48); // px
        text.setText(
            Profile.ALL.stream()
                .map(profile -> profile.label() + "\n" + String.join("\n", profile.associations()))
                .collect(Collectors.joining("\n\n"))
        );
        setContentView(text);
    }
}
