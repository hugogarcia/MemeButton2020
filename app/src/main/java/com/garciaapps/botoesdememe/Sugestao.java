package com.garciaapps.botoesdememe;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import com.google.android.material.snackbar.Snackbar;
import 	androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;

public class Sugestao extends AppCompatActivity{
    private String corpoEmail;
    private static Sugestao parent;
    private AdView adView;
    private AdRequest adRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sugestao_activity);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbarSugestao);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        final TextView txtSugestao = (TextView)findViewById(R.id.txtSugestao);
        Button enviar = (Button)findViewById(R.id.botaoEnviar);
        enviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                corpoEmail = txtSugestao.getText().toString();
                if(corpoEmail.trim().isEmpty()){
                    Toast.makeText(getApplicationContext(), "Informe o campo de sugestão!", Toast.LENGTH_SHORT).show();
                    return;
                }
                sendMessage();
                Toast.makeText(getApplicationContext(), R.string.obrigado, Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        adView = (AdView)findViewById(R.id.adViewSugestao);
        adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendMessage() {
        String[] recipients = {"hgrandomapps.reply@gmail.com"};
        SendEmailAsyncTask email = new SendEmailAsyncTask();
        email.activity = this;
        email.m = new Gmail("hgrandomapps.reply@gmail.com", "951753123");
        email.m.set_from("hgrandomapps.reply@gmail.com");
        email.m.setBody(corpoEmail);
        email.m.set_to(recipients);
        email.m.set_subject("Sugestão de Meme");
        email.execute();
    }

    public void displayMessage(final String message) {
        //Snackbar.make(findViewById(R.id.fab), message, Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }
}

class SendEmailAsyncTask extends AsyncTask<Void, Void, Boolean> {
    Gmail m;
    Sugestao activity;

    public SendEmailAsyncTask() {}

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            if (m.send()) {
                activity.displayMessage("Email sent.");
            } else {
                activity.displayMessage("Email failed to send.");
            }

            return true;
        } catch (AuthenticationFailedException e) {
            Log.e(SendEmailAsyncTask.class.getName(), "Bad account details");
            e.printStackTrace();
            activity.displayMessage("Authentication failed.");
            return false;
        } catch (MessagingException e) {
            Log.e(SendEmailAsyncTask.class.getName(), "Email failed");
            e.printStackTrace();
            activity.displayMessage("Email failed to send.");
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            activity.displayMessage("Unexpected error occured.");
            return false;
        }
    }
}
