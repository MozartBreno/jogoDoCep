package com.example.client_servertcp_pingpong;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;


public class MainActivity extends AppCompatActivity {

    EditText edtCep;
    TextView tvStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        edtCep = findViewById(R.id.edtCep);
        tvStatus = findViewById(R.id.tvStatus);

    }

    public void onClickServer(View v)
    {
        Thread t = new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        validarCEP(ServerTCP.class);
                    }
                }
        );
        t.start();
    }
    public void onClickClient(View v)
    {
        Thread t = new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        validarCEP(ClienteTCP.class);
                    }
                }
        );
        t.start();
    }
    private void validarCEP(Class classe) {

        //https://viacep.com.br/ws/60010020/json/


        String CEP = edtCep.getText().toString();
        //String CEP = "62908020";
        //"6001002000";
        Log.v("PMD", "CEP:" + CEP);
        try {
            URL url = new URL("https://viacep.com.br/ws/" + CEP + "/json/");
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true); //Vou ler dados?
            conn.connect();


            String result[] = new String[1];
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), "utf-8"));
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                result[0] = response.toString();
                //  Log.v ("PDM","Resultado:"+result[0]);

                JSONObject respostaJSON = new JSONObject(result[0]);
                try {
                    respostaJSON.getString("erro");
                    Log.v("Erro json","erro no json");
                    tvStatus.post(new Runnable() {
                        @Override
                        public void run() {
                            tvStatus.setText("Cep não encontrado na base dos correios");
                        }
                    });
                }
                catch (Exception e)
                {
                    //faz nada nao
                }

                final String loc = respostaJSON.getString("logradouro");
                String cidade = respostaJSON.getString("localidade");

                Log.v("PDM", "Esse é o CEP da rua " + loc + " da cidade " + cidade);
                tvStatus.post(new Runnable() {
                    @Override
                    public void run() {
                        tvStatus.setText("Sucesso");
                    }
                });
                Bundle bundle = new Bundle();
                bundle.putString("cep", CEP);
                Intent intent = new Intent(this, classe);
                intent.putExtras(bundle);
                startActivity(intent);


            }
            else {
                tvStatus.post(new Runnable() {
                    @Override
                    public void run() {
                        tvStatus.setText("Cep inválido");
                    }
                });
            }
        } catch (Exception e) {
            tvStatus.post(new Runnable() {
                @Override
                public void run() {
                    tvStatus.setText("Cep inválido");
                }
            });
            e.printStackTrace();
        }
    }
}