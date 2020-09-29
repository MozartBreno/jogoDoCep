package com.example.client_servertcp_pingpong;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class ClienteTCP extends AppCompatActivity {
    TextView tvStatus, tvNumPìngsPongs, tvCepEscolhido;
    Socket clientSocket;
    DataOutputStream socketOutput;
    BufferedReader socketEntrada;
    DataInputStream socketInput;
    Button btConectar;
    EditText edtIp,edtCep2;
    long pings,pongs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cliente_t_c_p);
        tvStatus=findViewById(R.id.tvStatusClient);
        btConectar=findViewById(R.id.btConectar);
        tvNumPìngsPongs=findViewById(R.id.tvNumPP_C);
        edtIp=findViewById(R.id.edtCep);
        edtCep2=findViewById(R.id.edtCep2);
        tvCepEscolhido = findViewById(R.id.tvCepEscolhido);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String cep = extras.getString("cep");
            tvCepEscolhido.setText(cep);
        }
    }

    public void atualizarStatus() {
        //Método que vai atualizar os pings e pongs, usando post para evitar problemas com as threads
        tvNumPìngsPongs.post(new Runnable() {
            @Override
            public void run() {
                tvNumPìngsPongs.setText("Enviados "+pings+" Pings e "+pongs+" Pongs");
            }
        });
    }
    public void conectar(View v) {
        final String ip=edtIp.getText().toString();
        tvStatus.setText("Conectando em "+ip+":9090");


        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    clientSocket = new Socket (ip,9090);

                    tvStatus.post(new Runnable() {
                        @Override
                        public void run() {
                            tvStatus.setText("Conectado com "+ip+":9090");
                        }
                    });
                    socketOutput =
                            new DataOutputStream(clientSocket.getOutputStream());
                    socketInput=
                            new DataInputStream (clientSocket.getInputStream());
                    while (socketInput!=null) {
                        String result = socketInput.readUTF();
                        if (result.compareTo("PING") == 0) {
                            //enviar Pong
                            pongs++;
                            socketOutput.writeUTF("PONG");
                            socketOutput.flush();
                            atualizarStatus();
                        }
                    }


                } catch (Exception e) {

                    tvStatus.post(new Runnable() {
                        @Override
                        public void run() {
                            tvStatus.setText("Erro na conexão com "+ip+":9090");
                        }
                    });

                    e.printStackTrace();
                }
            }
        });
        t.start();
    }

    public void mandarPing(View v){
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (socketOutput!=null) {
                        socketOutput.writeUTF("PING");
                        socketOutput.flush();
                        pings++;
                        atualizarStatus();
                    }else{
                        tvStatus.setText("Cliente Desconectado");
                        btConectar.setEnabled(true);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }
    private void validarCEP(Class classe) {

        //https://viacep.com.br/ws/60010020/json/


        String CEP = edtCep2.getText().toString();
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

                startActivity(new Intent(this, classe));


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