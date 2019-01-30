package com.erreius.developer.dev2018;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class WebViewApp extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    private String IDUser;
    private String Name;
    private String FullName;
    private String TipoRed;
    JSONParser jParser = new JSONParser();
    private static String url_Servicio = "https://www.mitra.com.ar/pharma/api/Employees/";
    private static String url_home = "http://appcodigos.erreius.com/Default.aspx";
    private static final String TAG_SUCCESS = "StatusCode";
    private static final String TAG_User = "EUS";
    private static final String TAG_Password = "EPS";
    public static final String MY_PREFS_NAME = "MyPrefsFile";
    Menu menu;
    private String currentUrl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        try {
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.setDrawerListener(toggle);
            toggle.syncState();}
        catch (Exception error )
        {
            String er = error.getMessage();
        }

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);
        TextView navUsername = (TextView) headerView.findViewById(R.id.txtUserBar);

        Bundle b = getIntent().getExtras();
        Boolean suscriptor = new Boolean(false);
        if(b != null){
            IDUser = b.getString("id");
            Name = b.getString("name");
            FullName = b.getString("fullname");
            navUsername.setText(FullName);
            TipoRed = b.getString("tipored");
            if (TextUtils.isEmpty(TipoRed))
            {
                IDUser = b.getString("email");
                navUsername.setText(IDUser);
                FullName = b.getString("password");
                suscriptor = true;
            }
        }

        String mensaje ="";

        try {
            mensaje = new ObtenerEncriptados().execute(IDUser,FullName).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        if (!suscriptor) {
            mensaje = mensaje + "&name=" + Name + "&tipored=" + TipoRed + "&mobile=si";
        }
        String url = "http://appcodigos.erreius.com/Login.aspx?" + mensaje;
        if (savedInstanceState == null)
        {
            if (isConnectedToInternet())
            {
            CargarWebView(url);
            }
            else {
                Toast.makeText(WebViewApp.this,"Conexión Perdida",Toast.LENGTH_LONG).show();
            }
        }
        handleIntent(getIntent());
    }


    private void CargarWebView(String url) {
        final ProgressDialog pd = ProgressDialog.show(WebViewApp.this, "", "Por favor espere, cargando documentos...", true);
        final WebView myWebView = (WebView) this.findViewById(R.id.webView);
        myWebView.getSettings().setJavaScriptEnabled(true);
        myWebView.getSettings().setSupportMultipleWindows(true);
        myWebView.getSettings().setLoadWithOverviewMode(true);
        myWebView.getSettings().setUseWideViewPort(true);
        myWebView.getSettings().setBuiltInZoomControls(true);
        myWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog,
                                          boolean isUserGesture, Message resultMsg) {
                                WebView newWebView = new WebView(WebViewApp.this);
                                newWebView.getSettings().setJavaScriptEnabled(true);
                                newWebView.getSettings().setSupportZoom(true);
                                newWebView.getSettings().setBuiltInZoomControls(true);
                                newWebView.getSettings().setPluginState(WebSettings.PluginState.ON);
                                newWebView.getSettings().setSupportMultipleWindows(true);
                                view.addView(newWebView);
                                WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                                transport.setWebView(newWebView);
                                resultMsg.sendToTarget();

                                newWebView.setWebViewClient(new WebViewClient() {
                                    @Override
                                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                                        Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(url));
                                        Bundle b = new Bundle();
                                        b.putBoolean("new_window", true); //sets new window
                                        intent.putExtras(b);
                                        startActivity(intent);
                                        return true;
                                    }
                                });
                                return true;
                            }
                        }
                    );
            myWebView.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(WebViewApp.this, description, Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon)
            {
                pd.show();
                String content = "DetalleArticulo";
                String[] matches = new String[] {"Default.aspx", "Login.aspx","RegistroDeUso"};
                Boolean flagDefault = stringContainsItemFromList(url,matches);
                Boolean flag = url.toLowerCase().contains(content.toLowerCase());
                if (!flag){
                PreferenceManager.getDefaultSharedPreferences(WebViewApp.this).edit().putString("url", url).apply();
                }
                if (!flagDefault)
                {
                    showOverflowMenu(true);
                }else
                {
                    showOverflowMenu(false);
                }
            }
            @Override
            public void onPageFinished(WebView view, String url) {
                pd.dismiss();
                String content = "DetalleArticulo";
                String[] matches = new String[] {"Default.aspx", "Login.aspx","RegistroDeUso"};
                Boolean flagDefault = stringContainsItemFromList(url,matches);
                Boolean flag = url.toLowerCase().contains(content.toLowerCase());
                if (!flag){
                    PreferenceManager.getDefaultSharedPreferences(WebViewApp.this).edit().putString("url", url).apply();
                }
                if (!flagDefault)
                {
                    showOverflowMenu(true);
                }else
                {
                    showOverflowMenu(false);
                }
            }
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                String content = "DetalleArticulo";
                String[] matches = new String[] {"Default.aspx", "Login.aspx","RegistroDeUso"};
                Boolean flagDefault = stringContainsItemFromList(url,matches);
                Boolean flag = url.toLowerCase().contains(content.toLowerCase());
                if (!flag){
                    PreferenceManager.getDefaultSharedPreferences(WebViewApp.this).edit().putString("url", url).apply();
                }
                if (!flagDefault)
                {
                    showOverflowMenu(true);
                }else
                {
                    showOverflowMenu(false);
                }
                return false;
            }
        });
        if (isConnectedToInternet()) {
            myWebView.loadUrl(url);
        }
        else {
            Toast.makeText(WebViewApp.this,"Conexión Perdida",Toast.LENGTH_LONG).show();
        }
    }

    public boolean isConnectedToInternet(){
        ConnectivityManager connectivity = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null)
        {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED)
                    {
                        return true;
                    }
        }
        return false;
    }

    public static boolean stringContainsItemFromList(String inputStr, String[] items)
    {
        for(int i =0; i < items.length; i++)
        {
            if(inputStr.contains(items[i]))
            {
                return true;
            }
        }
        return false;
    }
    private void CargarWebViewBuscador(String url) {
        final WebView myWebView = (WebView) this.findViewById(R.id.webView);
        myWebView.getSettings().setJavaScriptEnabled(true);
        myWebView.getSettings().setLoadWithOverviewMode(true);
        myWebView.getSettings().setUseWideViewPort(true);
        myWebView.getSettings().setBuiltInZoomControls(true);
        myWebView.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(WebViewApp.this, description, Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon)
            {
                PreferenceManager.getDefaultSharedPreferences(WebViewApp.this).edit().putString("url", url).apply();
                //Toast.makeText(WebViewApp.this, url, Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onPageFinished(WebView view, String url) {
                PreferenceManager.getDefaultSharedPreferences(WebViewApp.this).edit().putString("url", url).apply();
                //Toast.makeText(WebViewApp.this, url, Toast.LENGTH_SHORT).show();
            }
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                PreferenceManager.getDefaultSharedPreferences(WebViewApp.this).edit().putString("url", url).apply();
                //Toast.makeText(WebViewApp.this, currentUrl, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        myWebView.loadUrl(url);
    }

    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setQuery("", false);
        return  true;
    }

    public void showOverflowMenu(boolean showMenu){
        if(menu == null)
            return;
        menu.setGroupVisible(R.id.main_menu_group, showMenu);
    }

    @Override
    public  boolean onCreateOptionsMenu(Menu menu){
        this.menu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.password_menu, menu);

        getMenuInflater().inflate(R.menu.search_menu, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();

        final MenuItem searchMenuItem = menu.findItem(R.id.search);
        searchMenuItem.setVisible(false);
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean queryTextFocused) {
                if(!queryTextFocused) {
                    searchMenuItem.collapseActionView();
                    searchView.setQuery("", false);
                }
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {

                // Perform search here!
                showResults(query);

                // Clear the text in search bar but (don't trigger a new search!)
                searchView.setQuery("", false);
                searchView.clearFocus();

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });
        searchView.setSearchableInfo( searchManager.getSearchableInfo(getComponentName()) );

        return  super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_settings:
                SharedPreferences spreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor spreferencesEditor = spreferences.edit();
                spreferencesEditor.clear();
                spreferencesEditor.commit();

                SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
                spreferencesEditor = prefs.edit();
                spreferencesEditor.clear();
                spreferencesEditor.commit();
                Intent myIntent = new Intent(WebViewApp.this, MainActivity.class);
                startActivity(myIntent);
                finish();
                return true;
            case android.R.id.home:
                return super.onOptionsItemSelected(item);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_inicio) {
            if (isConnectedToInternet()) {
                CargarWebView("http://appcodigos.erreius.com/Default.aspx?mobile=si");
            }
            else {
                Toast.makeText(WebViewApp.this,"Conexión Perdida",Toast.LENGTH_LONG).show();
            }
        } else if (id == R.id.nav_registro) {
            if (isConnectedToInternet()) {
                CargarWebView("http://appcodigos.erreius.com/RegistroDeUso.aspx?mobile=si");
            }
            else {
                Toast.makeText(WebViewApp.this,"Conexión Perdida",Toast.LENGTH_LONG).show();
            }
        }
     /*   else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            String url = PreferenceManager.getDefaultSharedPreferences(WebViewApp.this).getString("url", "defaultStringIfNothingFound");
            String content = "About.aspx";
            final WebView myWebView = (WebView) this.findViewById(R.id.webView);
            Boolean flag = myWebView.getUrl().toLowerCase().contains(content.toLowerCase());
            if (flag){
                myWebView.loadUrl(url_home);
            }
            else
            {
                if (!url.equals(url_home)) {
                    myWebView.loadUrl(url);
                }
            }
        }
    }

    class ObtenerEncriptados extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        protected String doInBackground(String... args) {
            String User = args[0];
            String Password = args[1];
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
            nameValuePairs.add(new BasicNameValuePair("EUS", User));
            nameValuePairs.add(new BasicNameValuePair("EPS", Password));

            JSONObject json = jParser.makeHttpRequest(url_Servicio + "EncryptData", "POST", nameValuePairs);
            String Resultado="";
            try {
                int success = json.getInt(TAG_SUCCESS);
                if (success == 200) {
                    Resultado = "eus=" + json.getString(TAG_User);
                    Resultado = Resultado + "&eps=" + json.getString(TAG_Password);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Resultado = e.getMessage();
            }
/*            Resultado = Normalizer.normalize(Resultado, Normalizer.Form.NFD);
            Resultado = Resultado.replaceAll("[^\\p{ASCII}]", "");*/
            return Resultado;
        }
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after getting all products
            super.onPostExecute(file_url);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        //handleIntent(intent);
    }

    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);

            showResults(query);
            intent.removeExtra(SearchManager.QUERY);
        }
    }

    private void showResults(String query) {
        String url = PreferenceManager.getDefaultSharedPreferences(WebViewApp.this).getString("url", "defaultStringIfNothingFound");
        Integer indice = url.indexOf("&buscador");
        if (indice > -1)
        {
            url = url.substring(0, indice);
        }
        url = url + "&buscador=" + query;
        final WebView myWebView = (WebView) this.findViewById(R.id.webView);
        myWebView.loadUrl(url);
    }
}
