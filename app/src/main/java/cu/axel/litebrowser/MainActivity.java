package cu.axel.litebrowser;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebResourceRequest;
import android.webkit.PermissionRequest;
import android.view.KeyEvent;
import android.view.View.OnKeyListener;
import android.text.TextWatcher;
import android.text.Editable;
import androidx.appcompat.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.ProgressBar;
import android.view.View.OnTouchListener;
import android.view.MotionEvent;
import android.graphics.Bitmap;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.ActionMenuView.OnMenuItemClickListener;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import android.os.Environment;
import java.io.File;
import android.webkit.ValueCallback;
import android.app.DownloadManager;
import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {
    private LinearLayout webContainer, suggestionsPanel;
    private ArrayList<WebView> webViews;
    private TextView tabsBtn;
    private EditText urlField;
    private WebView webView;
    private ProgressBar loadPb;
    private ImageButton reloadBtn;
    private SharedPreferences sp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        if (ContextCompat.checkSelfPermission(this, "android.permission.WRITE_EXTERNAL_STORAGE") != 0) {
            ActivityCompat.requestPermissions(this, new String[]{"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"}, 0);
        }
        
        webContainer = findViewById(R.id.web_container);
        suggestionsPanel = findViewById(R.id.suggestions_panel);
        webViews = new ArrayList<WebView>();
        tabsBtn = findViewById(R.id.btn_tabs);
        urlField = findViewById(R.id.url_field);
        loadPb = findViewById(R.id.load_pb);
        reloadBtn = findViewById(R.id.btn_reload);
        urlField.setSelectAllOnFocus(true);
        //TODO: Switch to ax
        sp = PreferenceManager.getDefaultSharedPreferences(this);

        suggestionsPanel.setOnTouchListener(new OnTouchListener(){

                @Override
                public boolean onTouch(View p1, MotionEvent p2) {
                    if (p2.getAction() == MotionEvent.ACTION_OUTSIDE)
                        suggestionsPanel.setVisibility(View.GONE);
                    return false;
                }
            });

        urlField.setOnKeyListener(new OnKeyListener(){

                @Override
                public boolean onKey(View p1, int p2, KeyEvent p3) {
                    if (p3.getAction() == KeyEvent.ACTION_DOWN && p2 == KeyEvent.KEYCODE_ENTER) {
                        suggestionsPanel.setVisibility(View.GONE);
                        webView.loadUrl(Utils.getUrl(urlField.getText().toString(), 0));
                    }
                    return false;
                }
            });

        urlField.addTextChangedListener(new TextWatcher(){

                @Override
                public void beforeTextChanged(CharSequence p1, int p2, int p3, int p4) {
                }

                @Override
                public void onTextChanged(CharSequence p1, int p2, int p3, int p4) {
                }

                @Override
                public void afterTextChanged(Editable p1) {
                    String text = p1.toString();
                    suggestionsPanel.setVisibility(text.length() > 1 && !text.equals(webView.getUrl()) ? View.VISIBLE : View.GONE);

                }
            });

        if (getIntent().getAction().equals("android.intent.action.VIEW")) {
            newTab(getIntent().getData().toString(), false);
        } else {
            newTab("", false);
        }
    }

    private void newTab(String url, boolean inBackground) {
        WebView webView = new WebView(this);
        webView.setWebChromeClient(new MyWebChromeClient());
        webView.setWebViewClient(new MyWebViewClient());
        applyWebSettings(webView);
        webViews.add(webView);
        if (!inBackground) {
            this.webView = webView;
            webContainer.removeAllViews();
            webContainer.addView(webView);
        }
        tabsBtn.setText(webViews.size() + "");
        if (!url.isEmpty()) {
            webView.loadUrl(Utils.getUrl(url, 0));
        }
    }

    public void reloadCancel(View v) {
        if (webView.getProgress() > 0 && webView.getProgress() < 100)
            webView.stopLoading();
        else
            webView.reload();
    }

    public void showTabsDialog(View v) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setAdapter(new TabsAdapter(this, webViews), new DialogInterface.OnClickListener(){

                @Override
                public void onClick(DialogInterface p1, int p2) {
                    WebView wv = webViews.get(p2);
                    webView = wv;
                    webContainer.removeAllViews();
                    webContainer.addView(webView);
                    if (webView.getProgress() > 0) {
                        loadPb.setVisibility(View.VISIBLE);
                        loadPb.setProgress(webView.getProgress());
                    } else {
                        loadPb.setVisibility(View.GONE);
                    }
                }
            });
        dialog.setNeutralButton("New tab", new DialogInterface.OnClickListener(){

                @Override
                public void onClick(DialogInterface p1, int p2) {
                    newTab("", false);
                }
            });
        dialog.show();
    }

    public void showMainMenu(View v) {
        PopupMenu menu = new PopupMenu(this, v);
        menu.inflate(R.menu.menu_main);
        Utils.setForceShowIcon(menu);

        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener(){

                @Override
                public boolean onMenuItemClick(MenuItem p1) {
                    switch (p1.getItemId()) {
                        case R.id.action_blocker:
                            showBlocker();
                            break;
                        case R.id.action_back:
                            webView.goBack();
                            break;
                        case R.id.action_forward:
                            webView.goForward();
                            break;
                        case R.id.action_save:
                            String path = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Pages/" + Utils.strip(webView.getTitle()) + ".mhtml").getAbsolutePath();
                            Toast.makeText(MainActivity.this, path, 5000).show();
                            webView.saveWebArchive(path, false, new ValueCallback<String>(){

                                    @Override
                                    public void onReceiveValue(String p1) {
                                    }

                                });
                            break;
                    }
                    return false;
                }
            });

        menu.show();
    }

    private void applyWebSettings(WebView wv) {
        wv.getSettings().setJavaScriptEnabled(!sp.getBoolean("block_js", false));
        wv.getSettings().setLoadsImagesAutomatically(sp.getBoolean("block_img", false));
    }

    private void showBlocker() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_blocker, null);
        dialog.setView(view);
        final CheckBox jsChkbx = view.findViewById(R.id.block_js_chkbx);
        final CheckBox imChkbx = view.findViewById(R.id.block_img_chkbx);
        final CheckBox cssChkbx = view.findViewById(R.id.block_css_chkbx);
        final CheckBox externalChkbx = view.findViewById(R.id.block_external_chkbx);

        jsChkbx.setChecked(sp.getBoolean("block_js", false));
        imChkbx.setChecked(sp.getBoolean("block_img", false));
        cssChkbx.setChecked(sp.getBoolean("block_css", false));
        externalChkbx.setChecked(sp.getBoolean("block_external", false));
        dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener(){

                @Override
                public void onClick(DialogInterface p1, int p2) {
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putBoolean("block_js", jsChkbx.isChecked());
                    editor.putBoolean("block_img", imChkbx.isChecked());
                    editor.putBoolean("block_css", cssChkbx.isChecked());
                    editor.putBoolean("block_external", externalChkbx.isChecked());
                    editor.commit();
                    applyWebSettings(webView);
                }
            });
        dialog.show();
    }

    private class MyWebViewClient extends WebViewClient {
        private String currentPage="";
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            reloadBtn.setVisibility(View.VISIBLE);
            reloadBtn.setImageResource(R.drawable.ic_close);
            loadPb.setVisibility(View.VISIBLE);
            urlField.setText(url);
            currentPage = url;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            reloadBtn.setImageResource(R.drawable.ic_reload);
            loadPb.setVisibility(View.GONE);
            currentPage = "";
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            return shouldInteceptWebRequest(webView, request.getUrl().toString());
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            return shouldInteceptWebRequest(view, url);
        }

        private WebResourceResponse shouldInteceptWebRequest(WebView wv, String url) {
            if (sp.getBoolean("block_external", false) && !currentPage.isEmpty() && !url.contains(Utils.getBaseUrl(currentPage)))
                return new WebResourceResponse("text/plain", "uftf-8", new ByteArrayInputStream("blocked".getBytes()));
            else if (sp.getBoolean("block_css", false) && url.toLowerCase().contains(".css")) {
                try {
                    return new WebResourceResponse("text/css", "uftf-8", getAssets().open(sp.getBoolean("block_img", false) ? "styles_no_images.css" : "styles.css"));
                } catch (IOException e) {}
            }

            return super.shouldInterceptRequest(webView, url);
        }

    }
    private class MyWebChromeClient extends WebChromeClient {

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            if (webView.equals(view))
                loadPb.setProgress(newProgress);
        }

        @Override
        public void onPermissionRequest(PermissionRequest request) {
            super.onPermissionRequest(request);
        }


    }

}
