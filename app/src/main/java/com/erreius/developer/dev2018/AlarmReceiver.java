package com.erreius.developer.dev2018;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class AlarmReceiver extends BroadcastReceiver {
    private ProgressDialog pDialog;
    private TextView texto;
    // Creating JSON Parser object
    JSONParser jParser = new JSONParser();

    ArrayList<HashMap<String, String>> empresaList;

    // url to get all products list
    //private static String url_Barcode = "http://10.0.2.2/api/Employees/GetNotification";
    private static String url_Barcode = "https://www.mitra.com.ar/pharma/api/Employees/GetNotification";
    // JSON Node names
    private static final String TAG_SUCCESS = "StatusCode";
    private static final String TAG_Mensaje = "Message";

    // products JSONArray
    JSONArray products = null;
    private String mensaje ="a" ;
    ListView lista;
    private PendingIntent pendingIntent;

    @Override
    public void onReceive(Context context, Intent intent) {
        String usuario = intent.getStringExtra("key");
        try {
            mensaje = new AlarmReceiver.VerificarAlertas().execute(usuario).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        if (!mensaje.equals("")){

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
            mBuilder.setSmallIcon(R.drawable.ic_info_outline_black_24dp);
            mBuilder.setContentTitle("Erreius");
            mBuilder.setContentText(mensaje);
            mBuilder.setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 });
            mBuilder.setLights(Color.RED, 3000, 3000);
            //HomeActivity.getInstace().updateTheTextView(mensaje);
            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            mBuilder.setSound(alarmSound);
            mBuilder.setAutoCancel(true);
            Bitmap bitmap_image = BitmapFactory.decodeResource(context.getResources(), R.drawable.face);
            mBuilder.setLargeIcon(bitmap_image);

            //get the bitmap to show in notification bar
            //Bitmap bitmap_image = BitmapFactory.decodeResource(context.getResources(), R.drawable.itbclogo);
            //NotificationCompat.BigPictureStyle s = new NotificationCompat.BigPictureStyle().bigPicture(bitmap_image);
            //s.setSummaryText("Summary text appears on expanding the notification");
            //mBuilder.setStyle(s);

            //texto = (TextView) ((HomeActivity)context).findViewById(R.id.txtAlerta);
           // texto.setText(mensaje);

            /*Intent resultIntent = new Intent(context, HomeActivity.class);
            resultIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            resultIntent.putExtra("message", mensaje);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addParentStack(MainActivity.class);

            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(resultPendingIntent);

            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            int notificationID= 1234;
            mNotificationManager.notify(notificationID, mBuilder.build());*/
        }
    }

    class VerificarAlertas extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        protected String doInBackground(String... args) {
            Boolean flag = false;
            String nombre = args[0];
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
            nameValuePairs.add(new BasicNameValuePair("EmailEmployee", nombre));

            List params = new ArrayList();
            JSONObject json = jParser.makeHttpRequest(url_Barcode, "POST", nameValuePairs);
            String Resultado="";
            try {
                int success = json.getInt(TAG_SUCCESS);
                if (success == 200) {
                        Resultado = json.getString(TAG_Mensaje);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Resultado = e.getMessage();
            }
            Resultado = Normalizer.normalize(Resultado, Normalizer.Form.NFD);
            Resultado = Resultado.replaceAll("[^\\p{ASCII}]", "");
            return Resultado;
        }
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after getting all products
            super.onPostExecute(file_url);
        }
    }
}
