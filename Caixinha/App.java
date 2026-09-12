cd ~
rm -rf CaixinhaApp
mkdir -p CaixinhaApp/src/main/java/com/caixinha/app
mkdir -p CaixinhaApp/src/main
mkdir -p CaixinhaApp/res/values
cd CaixinhaApp

cat > res/values/strings.xml << EOF
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">Caixinha</string>
</resources>
EOF

cat > res/values/styles.xml << EOF
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="AppTheme" parent="android:Theme.Material.NoActionBar">
    </style>
</resources>
EOF

cat > src/main/java/com/caixinha/app/MainActivity.java << EOF
package com.caixinha.app;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
EOF