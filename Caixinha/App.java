cd ~
rm -rf CaixinhaApp
mkdir -p CaixinhaApp/src/main/java/com/caixinha/app
mkdir -p CaixinhaApp/res/values
cd CaixinhaApp

cat > src/main/java/com/caixinha/app/MainActivity.java << 'EOF'
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