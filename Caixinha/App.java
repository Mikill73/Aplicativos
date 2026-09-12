cat > src/main/java/com/caixinha/app/MainActivity.java << 'EOF'
package com.caixinha.app;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {

    private static final String PREFS = "caixinha_prefs";
    private static final String TOKEN_KEY = "github_token";
    private static final String REPO_USER = "Mikill73";
    private static final String REPO_NAME = "Aplicativos";
    private static final String FILE_PATH = "Caixinha/Dados";

    private String GITHUB_TOKEN = "";
    private String fileSha = null;

    private double yuri = 0.0;
    private double gustavo = 0.0;
    private final List<JSONObject> history = new ArrayList<>();

    private SharedPreferences prefs;
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    private LinearLayout tokenScreen, mainScreen;
    private EditText tokenInput;
    private TextView yuriBalance, gustavoBalance, yuriPct, gustavoPct, totalBalance, historyContainer;
    private EditText yuriInput, gustavoInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        GITHUB_TOKEN = prefs.getString(TOKEN_KEY, "");

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF111114);
        root.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        tokenScreen = buildTokenScreen();
        mainScreen = buildMainScreen();
        mainScreen.setVisibility(View.GONE);

        root.addView(tokenScreen);
        root.addView(mainScreen);
        setContentView(root);

        if (GITHUB_TOKEN.isEmpty()) {
            tokenScreen.setVisibility(View.VISIBLE);
            mainScreen.setVisibility(View.GONE);
        } else {
            tokenScreen.setVisibility(View.GONE);
            mainScreen.setVisibility(View.VISIBLE);
            carregarDados();
        }
    }

    // ============ TELA DE TOKEN ============
    private LinearLayout buildTokenScreen() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(40, 40, 40, 40);
        layout.setBackgroundColor(0xFF111114);
        layout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        TextView title = new TextView(this);
        title.setText("Token do GitHub");
        title.setTextColor(0xFFFFFFFF);
        title.setTextSize(24);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 20);
        layout.addView(title);

        TextView desc = new TextView(this);
        desc.setText("Cole seu token pessoal do GitHub abaixo");
        desc.setTextColor(0xFF888888);
        desc.setTextSize(14);
        desc.setGravity(Gravity.CENTER);
        desc.setPadding(0, 0, 0, 30);
        layout.addView(desc);

        tokenInput = new EditText(this);
        tokenInput.setHint("github_pat_...");
        tokenInput.setHintTextColor(0xFF666666);
        tokenInput.setTextColor(0xFFFFFFFF);
        tokenInput.setInputType(InputType.TYPE_CLASS_TEXT);
        tokenInput.setBackgroundColor(0xFF1A1A1A);
        tokenInput.setPadding(20, 15, 20, 15);
        layout.addView(tokenInput);

        Button saveBtn = new Button(this);
        saveBtn.setText("Salvar Token");
        saveBtn.setTextColor(0xFFFFFFFF);
        saveBtn.setBackgroundColor(0xFF27272A);
        saveBtn.setPadding(20, 15, 20, 15);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 20, 0, 0);
        saveBtn.setLayoutParams(lp);
        saveBtn.setOnClickListener(v -> {
            String t = tokenInput.getText().toString().trim();
            if (t.isEmpty()) {
                Toast.makeText(this, "Token vazio!", Toast.LENGTH_SHORT).show();
                return;
            }
            prefs.edit().putString(TOKEN_KEY, t).apply();
            GITHUB_TOKEN = t;
            tokenScreen.setVisibility(View.GONE);
            mainScreen.setVisibility(View.VISIBLE);
            carregarDados();
        });
        layout.addView(saveBtn);

        return layout;
    }

    // ============ TELA PRINCIPAL ============
    private LinearLayout buildMainScreen() {
        LinearLayout main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);
        main.setBackgroundColor(0xFF111114);
        main.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        topBar.setPadding(20, 20, 20, 10);
        topBar.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView title = new TextView(this);
        title.setText("Caixinha");
        title.setTextColor(0xFFFFFFFF);
        title.setTextSize(22);
        title.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        topBar.addView(title);

        Button refreshBtn = new Button(this);
        refreshBtn.setText("↻");
        refreshBtn.setTextColor(0xFFFFFFFF);
        refreshBtn.setTextSize(20);
        refreshBtn.setBackgroundColor(0x00000000);
        refreshBtn.setOnClickListener(v -> carregarDados());
        topBar.addView(refreshBtn);

        Button editTokenBtn = new Button(this);
        editTokenBtn.setText("🔑");
        editTokenBtn.setTextColor(0xFFFFFFFF);
        editTokenBtn.setTextSize(20);
        editTokenBtn.setBackgroundColor(0x00000000);
        editTokenBtn.setOnClickListener(v -> {
            tokenInput.setText(GITHUB_TOKEN);
            mainScreen.setVisibility(View.GONE);
            tokenScreen.setVisibility(View.VISIBLE);
        });
        topBar.addView(editTokenBtn);

        main.addView(topBar);

        ScrollView scroll = new ScrollView(this);
        scroll.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(20, 0, 20, 20);

        // Card Yuri
        content.addView(buildCard("Yuri", true));
        // Card Gustavo
        content.addView(buildCard("Gustavo", false));

        // Total
        LinearLayout totalBox = new LinearLayout(this);
        totalBox.setOrientation(LinearLayout.HORIZONTAL);
        totalBox.setBackgroundColor(0xFF18181B);
        totalBox.setPadding(20, 20, 20, 20);
        totalBox.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams totalLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        totalLp.setMargins(0, 10, 0, 10);
        totalBox.setLayoutParams(totalLp);

        TextView totalLabel = new TextView(this);
        totalLabel.setText("Saldo total");
        totalLabel.setTextColor(0xFF71717A);
        totalLabel.setTextSize(14);
        totalLabel.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        totalBox.addView(totalLabel);

        totalBalance = new TextView(this);
        totalBalance.setText("R$ 0,00");
        totalBalance.setTextColor(0xFFE4E4E7);
        totalBalance.setTextSize(18);
        totalBox.addView(totalBalance);
        content.addView(totalBox);

        // Botão calcular retirada
        Button withdrawBtn = new Button(this);
        withdrawBtn.setText("Calcular retirada");
        withdrawBtn.setTextColor(0xFFE4E4E7);
        withdrawBtn.setBackgroundColor(0xFF27272A);
        withdrawBtn.setPadding(20, 20, 20, 20);
        LinearLayout.LayoutParams wLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        wLp.setMargins(0, 0, 0, 10);
        withdrawBtn.setLayoutParams(wLp);
        withdrawBtn.setOnClickListener(v -> calcularRetirada());
        content.addView(withdrawBtn);

        // Histórico
        LinearLayout histBox = new LinearLayout(this);
        histBox.setOrientation(LinearLayout.VERTICAL);
        histBox.setBackgroundColor(0xFF18181B);
        histBox.setPadding(20, 20, 20, 20);
        histBox.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView histTitle = new TextView(this);
        histTitle.setText("Histórico");
        histTitle.setTextColor(0xFFA1A1AA);
        histTitle.setTextSize(14);
        histTitle.setPadding(0, 0, 0, 10);
        histBox.addView(histTitle);

        historyContainer = new TextView(this);
        historyContainer.setTextColor(0xFF71717A);
        historyContainer.setTextSize(12);
        historyContainer.setText("Nenhuma movimentação.");
        histBox.addView(historyContainer);

        content.addView(histBox);

        scroll.addView(content);
        main.addView(scroll);

        return main;
    }

    private LinearLayout buildCard(String who, boolean isYuri) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundColor(0xFF18181B);
        card.setPadding(20, 20, 20, 20);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 10, 0, 0);
        card.setLayoutParams(lp);

        TextView name = new TextView(this);
        name.setText(who);
        name.setTextColor(0xFFA1A1AA);
        name.setTextSize(14);
        name.setPadding(0, 0, 0, 8);
        card.addView(name);

        TextView balance = new TextView(this);
        balance.setText("R$ 0,00");
        balance.setTextColor(0xFFE4E4E7);
        balance.setTextSize(24);
        balance.setPadding(0, 0, 0, 4);
        card.addView(balance);

        TextView pct = new TextView(this);
        pct.setText("0,00% do total");
        pct.setTextColor(0xFF71717A);
        pct.setTextSize(12);
        pct.setPadding(0, 0, 0, 12);
        card.addView(pct);

        EditText input = new EditText(this);
        input.setHint("Valor");
        input.setHintTextColor(0xFF666666);
        input.setTextColor(0xFFE4E4E7);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setBackgroundColor(0xFF111114);
        input.setPadding(20, 15, 20, 15);
        card.addView(input);

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setPadding(0, 10, 0, 0);

        Button add = new Button(this);
        add.setText("Adicionar");
        add.setTextColor(0xFFD4D4D8);
        add.setBackgroundColor(0xFF27272A);
        add.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        add.setOnClickListener(v -> adicionar(who, input));
        actions.addView(add);

        Button rem = new Button(this);
        rem.setText("Remover");
        rem.setTextColor(0xFFD4D4D8);
        rem.setBackgroundColor(0xFF27272A);
        rem.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        rem.setOnClickListener(v -> remover(who, input));
        actions.addView(rem);

        card.addView(actions);

        if (isYuri) {
            yuriBalance = balance;
            yuriPct = pct;
            yuriInput = input;
        } else {
            gustavoBalance = balance;
            gustavoPct = pct;
            gustavoInput = input;
        }

        return card;
    }

    // ============ LÓGICA ============
    private double parseValor(String s) {
        try {
            return Double.parseDouble(s.replace(",", ".").trim());
        } catch (Exception e) {
            return -1;
        }
    }

    private void adicionar(String who, EditText input) {
        double val = parseValor(input.getText().toString());
        if (val <= 0) {
            Toast.makeText(this, "Valor inválido.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (who.equals("Yuri")) yuri += val; else gustavo += val;
        registrarHistorico(who, "adicionou", val);
        input.setText("");
        updateUI();
        salvarDados();
    }

    private void remover(String who, EditText input) {
        double val = parseValor(input.getText().toString());
        if (val <= 0) {
            Toast.makeText(this, "Valor inválido.", Toast.LENGTH_SHORT).show();
            return;
        }
        double atual = who.equals("Yuri") ? yuri : gustavo;
        if (val > atual) {
            Toast.makeText(this, "Saldo insuficiente.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (who.equals("Yuri")) yuri -= val; else gustavo -= val;
        registrarHistorico(who, "removeu", val);
        input.setText("");
        updateUI();
        salvarDados();
    }

    private void registrarHistorico(String who, String tipo, double val) {
        try {
            JSONObject h = new JSONObject();
            h.put("who", who);
            h.put("type", tipo);
            h.put("value", val);
            h.put("time", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", new Locale("pt", "BR")).format(new Date()));
            history.add(0, h);
            while (history.size() > 50) history.remove(history.size() - 1);
        } catch (Exception ignored) {}
    }

    private void calcularRetirada() {
        double total = yuri + gustavo;
        if (total <= 0) {
            Toast.makeText(this, "Adicione valores antes.", Toast.LENGTH_SHORT).show();
            return;
        }
        final EditText lucroInput = new EditText(this);
        lucroInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        lucroInput.setHint("Valor do lucro");
        lucroInput.setTextColor(0xFFFFFFFF);
        lucroInput.setHintTextColor(0xFF666666);

        new android.app.AlertDialog.Builder(this)
                .setTitle("Quanto rendeu de lucro?")
                .setView(lucroInput)
                .setPositiveButton("Calcular", (d, w) -> {
                    double lucro = parseValor(lucroInput.getText().toString());
                    if (lucro < 0) {
                        Toast.makeText(this, "Lucro inválido.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    double yPct = yuri / total;
                    double gPct = gustavo / total;
                    double yRecebe = yuri + (lucro * yPct);
                    double gRecebe = gustavo + (lucro * gPct);
                    String msg = "Yuri: " + formatBRL(yRecebe) + "\nGustavo: " + formatBRL(gRecebe);
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private String formatBRL(double v) {
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        return nf.format(v);
    }

    private String formatPct(double v) {
        return String.format(new Locale("pt", "BR"), "%.2f%%", v);
    }

    private void updateUI() {
        double total = yuri + gustavo;
        double yPct = total > 0 ? (yuri / total) * 100 : 0;
        double gPct = total > 0 ? (gustavo / total) * 100 : 0;

        yuriBalance.setText(formatBRL(yuri));
        gustavoBalance.setText(formatBRL(gustavo));
        yuriPct.setText(formatPct(yPct) + " do total");
        gustavoPct.setText(formatPct(gPct) + " do total");
        totalBalance.setText(formatBRL(total));

        StringBuilder sb = new StringBuilder();
        if (history.isEmpty()) {
            sb.append("Nenhuma movimentação.");
        } else {
            for (int i = 0; i < Math.min(history.size(), 30); i++) {
                try {
                    JSONObject h = history.get(i);
                    String sinal = h.getString("type").equals("adicionou") ? "+" : "−";
                    sb.append(h.getString("who"))
                            .append(" ").append(sinal).append(" ")
                            .append(formatBRL(h.getDouble("value")))
                            .append("  ").append(h.getString("time"))
                            .append("\n");
                } catch (Exception ignored) {}
            }
        }
        historyContainer.setText(sb.toString());
    }

    // ============ GITHUB SYNC ============
    private void carregarDados() {
        new Thread(() -> {
            try {
                String urlStr = "https://api.github.com/repos/" + REPO_USER + "/" + REPO_NAME + "/contents/" + FILE_PATH;
                HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "token " + GITHUB_TOKEN);
                conn.setRequestProperty("Accept", "application/vnd.github.v3+json");

                int code = conn.getResponseCode();
                if (code == 200) {
                    BufferedReader r = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String l;
                    while ((l = r.readLine()) != null) sb.append(l);
                    r.close();
                    conn.disconnect();

                    JSONObject json = new JSONObject(sb.toString());
                    String content = json.getString("content");
                    fileSha = json.getString("sha");
                    byte[] decodedBytes = android.util.Base64.decode(content, android.util.Base64.DEFAULT);
                    String decoded = new String(decodedBytes, StandardCharsets.UTF_8);

                    parseDados(decoded);

                    mainHandler.post(this::updateUI);
                } else {
                    mainHandler.post(() -> Toast.makeText(this, "Nenhum dado remoto ainda.", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void parseDados(String raw) {
        try {
            JSONObject root = new JSONObject(raw);
            yuri = root.optDouble("yuri", 0);
            gustavo = root.optDouble("gustavo", 0);
            history.clear();
            if (root.has("history")) {
                JSONArray arr = root.getJSONArray("history");
                for (int i = 0; i < arr.length(); i++) history.add(arr.getJSONObject(i));
            }
        } catch (Exception ignored) {}
    }

    private void salvarDados() {
        new Thread(() -> {
            try {
                JSONObject root = new JSONObject();
                root.put("yuri", yuri);
                root.put("gustavo", gustavo);
                JSONArray arr = new JSONArray();
                for (JSONObject h : history) arr.put(h);
                root.put("history", arr);
                root.put("timestamp", System.currentTimeMillis());

                String content = root.toString(2);
                byte[] encoded = android.util.Base64.encode(content.getBytes(StandardCharsets.UTF_8), android.util.Base64.DEFAULT);
                String encodedStr = new String(encoded, StandardCharsets.UTF_8);

                // Pega o sha atual (se existir)
                String urlStr = "https://api.github.com/repos/" + REPO_USER + "/" + REPO_NAME + "/contents/" + FILE_PATH;
                HttpURLConnection getConn = (HttpURLConnection) new URL(urlStr).openConnection();
                getConn.setRequestMethod("GET");
                getConn.setRequestProperty("Authorization", "token " + GITHUB_TOKEN);
                getConn.setRequestProperty("Accept", "application/vnd.github.v3+json");
                if (getConn.getResponseCode() == 200) {
                    BufferedReader r = new BufferedReader(new InputStreamReader(getConn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String l;
                    while ((l = r.readLine()) != null) sb.append(l);
                    r.close();
                    JSONObject j = new JSONObject(sb.toString());
                    fileSha = j.getString("sha");
                }
                getConn.disconnect();

                JSONObject body = new JSONObject();
                body.put("message", "Atualizando caixinha - Android " + System.currentTimeMillis());
                body.put("content", encodedStr);
                if (fileSha != null) body.put("sha", fileSha);

                HttpURLConnection putConn = (HttpURLConnection) new URL(urlStr).openConnection();
                putConn.setRequestMethod("PUT");
                putConn.setRequestProperty("Authorization", "token " + GITHUB_TOKEN);
                putConn.setRequestProperty("Accept", "application/vnd.github.v3+json");
                putConn.setRequestProperty("Content-Type", "application/json");
                putConn.setDoOutput(true);

                OutputStream os = putConn.getOutputStream();
                os.write(body.toString().getBytes(StandardCharsets.UTF_8));
                os.flush();
                os.close();

                final int rc = putConn.getResponseCode();
                putConn.disconnect();

                mainHandler.post(() -> {
                    if (rc == 200 || rc == 201) {
                        Toast.makeText(this, "Salvo no GitHub!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Erro ao salvar: " + rc, Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                mainHandler.post(() -> Toast.makeText(this, "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }
}
EOF
