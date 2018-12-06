package party.loveit.eosforandroid;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.math.BigInteger;
import java.util.List;

import party.loveit.bip44forandroidlibrary.utils.Bip44Utils;
import party.loveit.eosforandroidlibrary.Ecc;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<String> words = Bip44Utils.generateMnemonicWords(MainActivity.this);
                    Log.e("TAG", "words: " + words.toString());


                    BigInteger pri = Bip44Utils.getPathPrivateKey(words,"m/44'/194'/0'/0/0");
                    Log.e("TAG", "pri1: " + pri.toString(16));

                    String eospk =  Ecc.seedPrivate(pri);
                    Log.e("TAG", "EOS privateKey: " + eospk);

                    String eospuk =  Ecc.privateToPublic(eospk);
                    Log.e("TAG", "EOS publicKey: " + eospuk);


                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("TAG", "Exception: " + e);
                }
            }
        }).start();
    }
}
