package com.lab.akatuslibtest;

import java.io.InputStream;

import com.lib.akatusmobile.AkatusAuthTemplate;
import com.lib.akatusmobile.AkatusInstallmentTemplate;
import com.lib.akatusmobile.AkatusReaderInterface;
import com.lib.akatusmobile.AkatusReaderListener;
import com.lib.akatusmobile.AkatusTransactionTemplate;
import com.lib.akatusmobile.AuthRequest;
import com.lib.akatusmobile.AuthResponse;
import com.lib.akatusmobile.InstallmentsResponse;
import com.lib.akatusmobile.Payer;
import com.lib.akatusmobile.TransactionInvalidException;
import com.lib.akatusmobile.TransactionRequest;
import com.lib.akatusmobile.TransactionResponse;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;

public class MainActivity extends Activity implements AkatusReaderInterface, OnClickListener{
	
	private TextView txtReaderStatus, txtConfig;
	private Button btnConfig, btnSwipe, btnLogin, btnSend;
	private TransactionRequest transaction;
	private AuthResponse user;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		txtReaderStatus = (TextView)findViewById(R.id.lbl_reader_status);
		txtConfig = (TextView)findViewById(R.id.lbl_config_status);
		btnConfig = (Button) findViewById(R.id.btn_config);
		btnSwipe = (Button) findViewById(R.id.btn_swipe);
		btnLogin = (Button) findViewById(R.id.btn_login);
		btnSend = (Button) findViewById(R.id.bt_send);
		
		btnConfig.setOnClickListener(this);
		btnSwipe.setOnClickListener(this);		
		btnLogin.setOnClickListener(this);		
		btnSend.setOnClickListener(this);		
		
		fillTransaction();
	}
	AkatusReaderListener reader;
	@Override
	public void onClick(View v) {
		if( v == btnConfig){
			reader = new AkatusReaderListener(this, this);
			if(!reader.isConfigured())
				reader.startConfig();
			else
				reader.connectWithProfile();
		}else if(v== btnSwipe){
			reader.startCardReading();
		}else if(v == btnLogin){
			try {
				String email = ((EditText)findViewById(R.id.txt_login)).getText().toString();
				String senha = ((EditText)findViewById(R.id.txt_password)).getText().toString();
				
				AuthRequest req = new AuthRequest(email,senha,null,null);
				
				user = new AkatusAuthTemplate(true).login(req);
				transaction.setToken(user.getToken());
				((TextView)findViewById(R.id.lblTransacao)).setText("Transaction: "+transaction.toString());
			} catch (Exception e) {
				Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
			}
		}else if(v == btnSend){
			sendAkatusTransaction();
		}
	}

	@Override
	public void handleAutoCfgStatus(final int status, final String percent) {
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				switch (status) {
				case AkatusReaderListener.AUTO_CFG_STARTED:
					// Show a progress dialog: "Configuring reader..."
					txtConfig.setText("Auto Config Started");
					break;
				case AkatusReaderListener.AUTO_CFG_PROGRESS:
					// Change the progress message using "percent" parameter
					txtConfig.setText("Auto Config Progress: "+percent);
					break;
				case AkatusReaderListener.AUTO_CFG_FINISHED:
					// Dismiss the progress and wait for handleReaderStatus() method
					txtConfig.setText("Auto Config Sucess!");
					break;
				case AkatusReaderListener.AUTO_CFG_FAILED:
					// Ask the user if should run auto-config again
					txtConfig.setText("Auto Config Failed!");
					break;
				}
			}
		});
	}

	@Override
	public void handleCardSwiped(final byte[] data) {
		// Set the "track1" attribute for your TransactionRequest instance
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				txtReaderStatus.setText(data.toString());
			//	sendAkatusTransaction(data);
			}
		});
	}

	@Override
	public void handleReaderStatus(final int status) {
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {

		switch (status) {
		case AkatusReaderListener.READER_CONNECTING:
			// Alert the user to wait to pass a card
			txtReaderStatus.setText("CONNECTING");
			break;
		case AkatusReaderListener.READER_CONNECTED_NO_PROFILE:
			// Store this in your app and use reader.startConfig() futurelly for listen the reader 
			txtReaderStatus.setText("CONNECTED");
			break;
		case AkatusReaderListener.READER_CONNECTED:
			// A good place to call reader.startCardReading() method			
			txtReaderStatus.setText("CONNECTED");
			break;
		case AkatusReaderListener.READER_DISCONNECTED:
			// Alert the user to connect the reader
			txtReaderStatus.setText("DISCONNECTED");
			break;
		}
			}
		});
	}
	
	private void fillTransaction(){
		try {
			transaction = new TransactionRequest(this);
			
			transaction.setAmount("100.0");


			transaction.setCard_number("5899703016106199");
			transaction.setCvv("123");
			transaction.setDescription("ITEM1");
			transaction.setExpiration("201309");
			transaction.setGeolocation(new double[]{1234,1234});
			transaction.setHolder_name("CLIENT");
			transaction.setToken(new AuthResponse().getToken());

			//Installments
			String email = ((EditText)findViewById(R.id.txt_login)).getText().toString();
			String apiKey = user.getApi_key();
			double value = Double.parseDouble(transaction.getAmount());			
			InstallmentsResponse.ParcelaInfo[] resp = new AkatusInstallmentTemplate().getInstallmentsList(email, apiKey, value);
			transaction.setInstallments(resp[0].getQuantidade()+"");

			InputStream sig_is = getResources().openRawResource(R.raw.assinatura);
			byte[] sig = new byte[sig_is.available()];
			sig_is.read(sig);
			transaction.setSignatureBytes(sig);
			
			InputStream photo_is = getResources().openRawResource(R.raw.produto);
			byte[] photo = new byte[photo_is.available()];
			photo_is.read(photo);
			transaction.setPhotoBytes(photo);
			
			Payer p = new Payer();
			p.setCpf("38974440890");
			p.setEmail("a@a.com");
			p.setName("TESTE TRANSACTION");
			p.setPhone("11987654321");
			transaction.setPayer(p);
			
			((TextView)findViewById(R.id.lblTransacao)).setText("Transaction: "+transaction.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void sendAkatusTransaction(){
		try {	
				
			AkatusTransactionTemplate ws = new AkatusTransactionTemplate(true);
			TransactionResponse response = ws.postTransaction(transaction);
			
			if(response.getReturn_code() == AkatusTransactionTemplate.TRANSACTION_OK)
				Toast.makeText(this, "Transaction accepted! "+response.getMessage(), Toast.LENGTH_SHORT).show();
			else
				Toast.makeText(this, "Transaction recused! "+response.getMessage(), Toast.LENGTH_SHORT).show();
		} catch (TransactionInvalidException e) {
			Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
		}
	}
}
