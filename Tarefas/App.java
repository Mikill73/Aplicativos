cd ~/TaskApp
cat > src/main/java/com/task/app/MainActivity.java << 'EOF'
package com.task.app;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ScrollView;
import android.widget.Toast;
import android.view.View;
import android.view.Gravity;
import android.graphics.Color;
import android.view.ViewGroup;
import android.os.Handler;
import android.os.Looper;
import java.io.*;
import java.net.*;
import java.util.*;
import org.json.*;

public class MainActivity extends Activity {

    private LinearLayout taskContainer;
    private ScrollView scrollView;
    private List<JSONObject> taskList = new ArrayList<>();
    private Map<Integer, String> sectionTitles = new HashMap<>();
    private String fileSha = null;
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    private boolean isLoading = false;
    private String GITHUB_TOKEN = "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";
    private String GITHUB_USER = "Mikill73";
    private String GITHUB_REPO = "Desenvolvimento";
    private String FILE_PATH = "Perfis/Rotina.json";
    private TextView statusText;
    private LinearLayout descLayout;
    private TextView descText;
    private View descOverlay;
    private int trophyCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(Color.parseColor("#0d0d0d"));
        layout.setLayoutParams(new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ));

        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        topBar.setPadding(20, 20, 20, 20);
        topBar.setLayoutParams(new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        TextView title = new TextView(this);
        title.setText("Tarefas");
        title.setTextColor(Color.WHITE);
        title.setTextSize(22);
        title.setGravity(Gravity.CENTER);
        title.setLayoutParams(new LinearLayout.LayoutParams(
            0,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            1
        ));
        topBar.addView(title);

        Button refreshBtn = new Button(this);
        refreshBtn.setText("↻");
        refreshBtn.setTextColor(Color.WHITE);
        refreshBtn.setTextSize(24);
        refreshBtn.setBackgroundColor(Color.TRANSPARENT);
        refreshBtn.setPadding(10, 0, 10, 0);
        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                carregarTarefas();
            }
        });
        topBar.addView(refreshBtn);

        layout.addView(topBar);

        statusText = new TextView(this);
        statusText.setText("Carregando...");
        statusText.setTextColor(Color.parseColor("#888888"));
        statusText.setTextSize(14);
        statusText.setGravity(Gravity.CENTER);
        statusText.setPadding(20, 20, 20, 20);
        layout.addView(statusText);

        scrollView = new ScrollView(this);
        scrollView.setLayoutParams(new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            0,
            1
        ));
        scrollView.setBackgroundColor(Color.parseColor("#0d0d0d"));

        taskContainer = new LinearLayout(this);
        taskContainer.setOrientation(LinearLayout.VERTICAL);
        taskContainer.setPadding(20, 10, 20, 20);
        taskContainer.setLayoutParams(new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        scrollView.addView(taskContainer);
        layout.addView(scrollView);

        descOverlay = new View(this);
        descOverlay.setBackgroundColor(Color.parseColor("#88000000"));
        descOverlay.setLayoutParams(new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ));
        descOverlay.setVisibility(View.GONE);
        descOverlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fecharDescricao();
            }
        });

        descLayout = new LinearLayout(this);
        descLayout.setOrientation(LinearLayout.VERTICAL);
        descLayout.setBackgroundColor(Color.parseColor("#1a1a1a"));
        descLayout.setPadding(30, 30, 30, 30);
        descLayout.setLayoutParams(new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        descText = new TextView(this);
        descText.setTextColor(Color.parseColor("#cccccc"));
        descText.setTextSize(14);
        descText.setPadding(0, 0, 0, 20);
        descText.setLayoutParams(new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        Button closeBtn = new Button(this);
        closeBtn.setText("Fechar");
        closeBtn.setTextColor(Color.WHITE);
        closeBtn.setBackgroundColor(Color.parseColor("#ff6b00"));
        closeBtn.setPadding(20, 10, 20, 10);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fecharDescricao();
            }
        });

        descLayout.addView(descText);
        descLayout.addView(closeBtn);

        LinearLayout centerLayout = new LinearLayout(this);
        centerLayout.setOrientation(LinearLayout.VERTICAL);
        centerLayout.setGravity(Gravity.CENTER);
        centerLayout.setLayoutParams(new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ));
        centerLayout.addView(descLayout);

        LinearLayout overlayWrapper = new LinearLayout(this);
        overlayWrapper.setOrientation(LinearLayout.VERTICAL);
        overlayWrapper.setLayoutParams(new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ));
        overlayWrapper.addView(descOverlay);
        overlayWrapper.addView(centerLayout);

        addContentView(overlayWrapper, new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ));

        setContentView(layout);
        carregarTarefas();
    }

    private void fecharDescricao() {
        descOverlay.setVisibility(View.GONE);
        descLayout.setVisibility(View.GONE);
    }

    private void carregarTarefas() {
        if (isLoading) return;
        isLoading = true;
        statusText.setText("Carregando...");
        statusText.setTextColor(Color.parseColor("#888888"));

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String urlStr = "https://api.github.com/repos/" + GITHUB_USER + "/" + GITHUB_REPO + "/contents/" + FILE_PATH;
                    URL url = new URL(urlStr);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Authorization", "token " + GITHUB_TOKEN);
                    conn.setRequestProperty("Accept", "application/vnd.github.v3+json");
                    conn.setConnectTimeout(30000);
                    conn.setReadTimeout(30000);
                    conn.connect();

                    final int responseCode = conn.getResponseCode();
                    if (responseCode == 200) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();
                        conn.disconnect();

                        JSONObject json = new JSONObject(response.toString());
                        String content = json.getString("content");
                        fileSha = json.getString("sha");
                        byte[] decodedBytes = android.util.Base64.decode(content, android.util.Base64.DEFAULT);
                        String decoded = new String(decodedBytes, "UTF-8");

                        extrairTarefas(decoded);

                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                verificarEAtualizarTarefas();
                                renderizarTarefas();
                                statusText.setText("Tarefas carregadas: " + taskList.size());
                                statusText.setTextColor(Color.parseColor("#8bc34a"));
                                isLoading = false;
                            }
                        });
                    } else {
                        final String errorMsg = "Erro " + responseCode + ": " + conn.getResponseMessage();
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                statusText.setText(errorMsg);
                                statusText.setTextColor(Color.parseColor("#ff6b6b"));
                                isLoading = false;
                                Toast.makeText(MainActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } catch (final Exception e) {
                    final String errorMsg = "Erro: " + e.getMessage();
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            statusText.setText(errorMsg);
                            statusText.setTextColor(Color.parseColor("#ff6b6b"));
                            isLoading = false;
                            Toast.makeText(MainActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).start();
    }

    private void verificarEAtualizarTarefas() {
        Calendar hoje = Calendar.getInstance();
        int diaSemanaHoje = hoje.get(Calendar.DAY_OF_WEEK);
        int diaSemanaBrasil;
        if (diaSemanaHoje == Calendar.SUNDAY) {
            diaSemanaBrasil = 0;
        } else {
            diaSemanaBrasil = diaSemanaHoje - 1;
        }
        long umDia = 24 * 60 * 60 * 1000L;
        long agora = System.currentTimeMillis();
        boolean mudou = false;

        for (JSONObject task : taskList) {
            try {
                if (task.has("completedAt") && !task.isNull("completedAt")) {
                    long completedAt = task.optLong("completedAt", 0);
                    if (completedAt > 0 && (agora - completedAt) > umDia) {
                        task.put("completed", false);
                        task.put("completedAt", JSONObject.NULL);
                        mudou = true;
                        continue;
                    }
                }

                if (task.has("daysOfWeek")) {
                    JSONArray days = task.getJSONArray("daysOfWeek");
                    if (days.length() > 0) {
                        boolean hojeTem = false;
                        for (int i = 0; i < days.length(); i++) {
                            if (days.getInt(i) == diaSemanaBrasil) {
                                hojeTem = true;
                                break;
                            }
                        }
                        if (!hojeTem) {
                            task.put("completed", false);
                            task.put("completedAt", JSONObject.NULL);
                            mudou = true;
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (mudou) {
            salvarMudancas();
        }
    }

    private void extrairTarefas(String textContent) {
        taskList.clear();
        sectionTitles.clear();
        trophyCount = 0;
        try {
            JSONObject root = new JSONObject(textContent);
            if (root.has("sections")) {
                JSONArray sections = root.getJSONArray("sections");
                for (int i = 0; i < sections.length(); i++) {
                    JSONObject section = sections.getJSONObject(i);
                    int id = section.getInt("id");
                    String title = section.getString("title");
                    sectionTitles.put(id, title);
                }
            }
            if (root.has("meta")) {
                JSONArray meta = root.getJSONArray("meta");
                for (int i = 0; i < meta.length(); i++) {
                    JSONObject item = meta.getJSONObject(i);
                    if (item.getString("key").equals("trophyCount")) {
                        trophyCount = item.getInt("value");
                    }
                }
            }
            if (root.has("data")) {
                JSONArray array = root.getJSONArray("data");
                for (int i = 0; i < array.length(); i++) {
                    JSONObject item = array.getJSONObject(i);
                    if (!item.has("completedAt")) {
                        item.put("completedAt", JSONObject.NULL);
                    }
                    taskList.add(item);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void renderizarTarefas() {
        taskContainer.removeAllViews();

        if (taskList.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("Nenhuma tarefa encontrada.");
            empty.setTextColor(Color.parseColor("#666666"));
            empty.setTextSize(16);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, 50, 0, 50);
            taskContainer.addView(empty);
            return;
        }

        TextView trophyView = new TextView(this);
        trophyView.setText("🏆 " + trophyCount);
        trophyView.setTextColor(Color.parseColor("#ffd700"));
        trophyView.setTextSize(18);
        trophyView.setGravity(Gravity.CENTER);
        trophyView.setPadding(0, 0, 0, 10);
        taskContainer.addView(trophyView);

        Map<Integer, List<JSONObject>> sections = new HashMap<>();
        for (JSONObject task : taskList) {
            int sectionId = task.optInt("sectionId", 0);
            if (!sections.containsKey(sectionId)) {
                sections.put(sectionId, new ArrayList<JSONObject>());
            }
            sections.get(sectionId).add(task);
        }

        List<Integer> sortedSectionIds = new ArrayList<>(sections.keySet());
        Collections.sort(sortedSectionIds);

        for (int sectionId : sortedSectionIds) {
            List<JSONObject> items = sections.get(sectionId);
            Collections.sort(items, new Comparator<JSONObject>() {
                @Override
                public int compare(JSONObject a, JSONObject b) {
                    int orderA = a.optInt("order", 0);
                    int orderB = b.optInt("order", 0);
                    return Integer.compare(orderA, orderB);
                }
            });

            String sectionTitle = sectionTitles.containsKey(sectionId) ? sectionTitles.get(sectionId) : "Seção " + sectionId;
            TextView titleView = new TextView(this);
            titleView.setText(sectionTitle);
            titleView.setTextColor(Color.parseColor("#88aaff"));
            titleView.setTextSize(20);
            titleView.setPadding(0, 20, 0, 10);
            titleView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            taskContainer.addView(titleView);

            View divider = new View(this);
            divider.setBackgroundColor(Color.parseColor("#2a2a2a"));
            divider.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                1
            ));
            taskContainer.addView(divider);

            List<JSONObject> parents = new ArrayList<>();
            List<JSONObject> children = new ArrayList<>();
            for (JSONObject item : items) {
                if (item.isNull("parentId") || item.optInt("parentId") == 0) {
                    parents.add(item);
                } else {
                    children.add(item);
                }
            }

            for (JSONObject parent : parents) {
                int parentId = parent.optInt("id");
                renderizarItem(parent, 0);
                for (JSONObject child : children) {
                    if (child.optInt("parentId") == parentId) {
                        renderizarItem(child, 1);
                    }
                }
            }
        }
    }

    private void renderizarItem(final JSONObject task, final int level) {
        try {
            String name = task.optString("name", "Sem nome");
            String description = task.optString("description", "");
            boolean completed = task.optBoolean("completed", false);
            final int index = taskList.indexOf(task);

            LinearLayout taskItem = new LinearLayout(this);
            taskItem.setOrientation(LinearLayout.VERTICAL);
            taskItem.setBackgroundColor(completed ? Color.parseColor("#1a1a1a") : Color.parseColor("#252525"));
            taskItem.setPadding(16, 12, 16, 12);
            LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            );
            itemParams.setMargins(level * 30, 0, 0, 8);
            taskItem.setLayoutParams(itemParams);

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ));

            if (level == 1) {
                TextView prefix = new TextView(this);
                prefix.setText("  └─ ");
                prefix.setTextColor(Color.parseColor("#666666"));
                prefix.setTextSize(16);
                prefix.setPadding(0, 0, 4, 0);
                row.addView(prefix);
            }

            Button checkBtn = new Button(this);
            checkBtn.setText(completed ? "✓" : "○");
            checkBtn.setTextColor(completed ? Color.parseColor("#4CAF50") : Color.parseColor("#666666"));
            checkBtn.setTextSize(20);
            checkBtn.setBackgroundColor(Color.TRANSPARENT);
            checkBtn.setPadding(0, 0, 20, 0);
            checkBtn.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            checkBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleTask(index);
                }
            });
            row.addView(checkBtn);

            TextView taskName = new TextView(this);
            taskName.setText(name);
            taskName.setTextColor(completed ? Color.parseColor("#666666") : Color.WHITE);
            taskName.setTextSize(level == 1 ? 14 : 16);
            taskName.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1
            ));
            row.addView(taskName);

            if (!description.isEmpty() || !task.optString("justification", "").isEmpty()) {
                Button descBtn = new Button(this);
                descBtn.setText("i");
                descBtn.setTextColor(Color.parseColor("#88aaff"));
                descBtn.setTextSize(16);
                descBtn.setBackgroundColor(Color.TRANSPARENT);
                descBtn.setPadding(10, 0, 0, 0);
                descBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mostrarDescricao(task);
                    }
                });
                row.addView(descBtn);
            }

            taskItem.addView(row);

            if (completed) {
                TextView doneLabel = new TextView(this);
                doneLabel.setText("Concluído");
                doneLabel.setTextColor(Color.parseColor("#4CAF50"));
                doneLabel.setTextSize(12);
                doneLabel.setPadding(level == 1 ? 70 : 40, 4, 0, 0);
                taskItem.addView(doneLabel);
            }

            taskContainer.addView(taskItem);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void mostrarDescricao(JSONObject task) {
        try {
            String name = task.optString("name", "");
            String description = task.optString("description", "");
            String justification = task.optString("justification", "");

            StringBuilder msg = new StringBuilder();
            msg.append("📌 ").append(name).append("\n\n");
            msg.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
            if (!description.isEmpty()) {
                msg.append("📝 DESCRICAO:\n");
                msg.append(description).append("\n\n");
            }
            if (!justification.isEmpty()) {
                msg.append("💡 JUSTIFICATIVA:\n");
                msg.append(justification).append("\n\n");
            }
            if (description.isEmpty() && justification.isEmpty()) {
                msg.append("Sem descricao ou justificativa.\n");
            }

            descText.setText(msg.toString());
            descOverlay.setVisibility(View.VISIBLE);
            descLayout.setVisibility(View.VISIBLE);

        } catch (Exception e) {
            Toast.makeText(this, "Erro ao mostrar descricao", Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleTask(int index) {
        try {
            JSONObject task = taskList.get(index);
            boolean current = task.optBoolean("completed", false);
            boolean novoEstado = !current;
            int taskId = task.optInt("id", 0);

            List<Integer> childrenIds = new ArrayList<>();
            List<Integer> parentIds = new ArrayList<>();
            for (JSONObject t : taskList) {
                if (t.optInt("parentId", 0) == taskId) {
                    childrenIds.add(taskList.indexOf(t));
                }
                if (t.optInt("id", 0) == task.optInt("parentId", 0)) {
                    parentIds.add(taskList.indexOf(t));
                }
            }

            if (novoEstado) {
                long now = System.currentTimeMillis();
                task.put("completed", true);
                task.put("completedAt", now);
                for (int childIndex : childrenIds) {
                    JSONObject child = taskList.get(childIndex);
                    child.put("completed", true);
                    child.put("completedAt", now);
                }

                if (parentIds.isEmpty()) {
                    trophyCount++;
                } else {
                    boolean allChildrenCompleted = true;
                    for (int childIndex : childrenIds) {
                        if (!taskList.get(childIndex).optBoolean("completed", false)) {
                            allChildrenCompleted = false;
                            break;
                        }
                    }
                    if (allChildrenCompleted && !childrenIds.isEmpty()) {
                        trophyCount++;
                    }
                }
            } else {
                task.put("completed", false);
                task.put("completedAt", JSONObject.NULL);
                for (int childIndex : childrenIds) {
                    JSONObject child = taskList.get(childIndex);
                    child.put("completed", false);
                    child.put("completedAt", JSONObject.NULL);
                }
            }

            renderizarTarefas();
            salvarMudancas();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void salvarMudancas() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String urlStr = "https://api.github.com/repos/" + GITHUB_USER + "/" + GITHUB_REPO + "/contents/" + FILE_PATH;
                    URL url = new URL(urlStr);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Authorization", "token " + GITHUB_TOKEN);
                    conn.setRequestProperty("Accept", "application/vnd.github.v3+json");
                    conn.connect();

                    if (conn.getResponseCode() == 200) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();
                        conn.disconnect();

                        JSONObject json = new JSONObject(response.toString());
                        String content = json.getString("content");
                        String sha = json.getString("sha");
                        byte[] decodedBytes = android.util.Base64.decode(content, android.util.Base64.DEFAULT);
                        String decoded = new String(decodedBytes, "UTF-8");

                        JSONObject root = new JSONObject(decoded);
                        JSONArray items = root.getJSONArray("data");

                        for (int i = 0; i < items.length(); i++) {
                            JSONObject item = items.getJSONObject(i);
                            int id = item.optInt("id", 0);
                            for (JSONObject task : taskList) {
                                int taskId = task.optInt("id", 0);
                                if (id == taskId && taskId != 0) {
                                    item.put("completed", task.optBoolean("completed", false));
                                    if (task.has("completedAt") && !task.isNull("completedAt")) {
                                        item.put("completedAt", task.optLong("completedAt", 0));
                                    } else {
                                        item.put("completedAt", JSONObject.NULL);
                                    }
                                    break;
                                }
                            }
                        }

                        if (root.has("meta")) {
                            JSONArray meta = root.getJSONArray("meta");
                            for (int i = 0; i < meta.length(); i++) {
                                JSONObject metaItem = meta.getJSONObject(i);
                                if (metaItem.getString("key").equals("trophyCount")) {
                                    metaItem.put("value", trophyCount);
                                }
                            }
                        }

                        long timestamp = System.currentTimeMillis();
                        root.put("timestampANDROID", timestamp);

                        String updatedJson = root.toString(2);
                        byte[] encodedBytes = updatedJson.getBytes("UTF-8");
                        String encoded = android.util.Base64.encodeToString(encodedBytes, android.util.Base64.DEFAULT);

                        HttpURLConnection putConn = (HttpURLConnection) url.openConnection();
                        putConn.setRequestMethod("PUT");
                        putConn.setRequestProperty("Authorization", "token " + GITHUB_TOKEN);
                        putConn.setRequestProperty("Accept", "application/vnd.github.v3+json");
                        putConn.setRequestProperty("Content-Type", "application/json");
                        putConn.setDoOutput(true);

                        JSONObject body = new JSONObject();
                        body.put("message", "Atualizando tarefas - Android " + timestamp);
                        body.put("content", encoded);
                        body.put("sha", sha);

                        OutputStream os = putConn.getOutputStream();
                        os.write(body.toString().getBytes("UTF-8"));
                        os.flush();
                        os.close();

                        final int putResponse = putConn.getResponseCode();
                        if (putResponse == 200 || putResponse == 201) {
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this, "Salvo no GitHub!", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        putConn.disconnect();
                    }
                } catch (final Exception e) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Erro ao salvar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }
}
EOF
