package est.tfi.android;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.widget.Toast;

public class SMSBroadcastReceiver extends BroadcastReceiver {

	private MonedaAlAireActivity activity = null;
	private String _tmp_nro_respuesta = "";
	private String _tmp_nro_confirmacion = "";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if (activity == null)	{
			activity = MonedaAlAireActivity.__instancia;
		}
		if (activity._esperando_pregunta || activity._esperando_respuesta || activity._esperando_confirmacion)	{
			if (intent.getAction()
					.equals("android.provider.Telephony.SMS_RECEIVED")) {
				Bundle bundle = intent.getExtras();
				if (bundle != null) {
					Object[] pdus = (Object[]) bundle.get("pdus");
					final SmsMessage[] messages = new SmsMessage[pdus.length];
					for (int i = 0; i < pdus.length; i++) {
						messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
	
						if (activity._esperando_pregunta)	{
							
							if (messages[i].getMessageBody().startsWith("+++CARAOSECA?"))	{
								
								activity._esperando_pregunta = false;
								
								_tmp_nro_respuesta = messages[i].getOriginatingAddress();
								
								activity.logPantalla(" - " + messages[i].getOriginatingAddress() + " / " + messages[i].getDisplayOriginatingAddress() + " pregunta: ¿Cara o seca?", true);
								activity._sha512_en_uso = messages[i].getMessageBody().substring(13);
								activity.logPantalla(" - SHA-512 recibido: " + activity._sha512_en_uso, true);
								
								AlertDialog.Builder builder = new AlertDialog.Builder(activity);
								builder.setTitle(messages[i].getDisplayOriginatingAddress() + "pregunta...");
								builder.setMessage("¿Cara o Seca?");
								builder.setPositiveButton("CARA",
										new DialogInterface.OnClickListener() {
											public void onClick(DialogInterface dialog, int which) {
												activity._cara_o_seca = "CARA";
												enviarRespuestaSMS("+++CARA");
											}
										});
								builder.setNegativeButton("SECA",
										new DialogInterface.OnClickListener() {
											public void onClick(DialogInterface dialog, int which) {
												activity._cara_o_seca = "SECA";
												enviarRespuestaSMS("+++SECA");
											}
										});
								AlertDialog alert = builder.create();
								alert.show();
							}
						}

						if (activity._esperando_respuesta)	{

							if (messages[i].getMessageBody().startsWith("+++CARA") || messages[i].getMessageBody().startsWith("+++SECA"))	{
								
								activity._esperando_respuesta = false;
								
								if (messages[i].getMessageBody().startsWith("+++CARA"))	{
									activity.logPantalla(" - respuesta recibida: CARA", true);
									if (activity._cara_o_seca.equals("CARA"))	{
										activity.resultadoJuego(false, false);
									} else	{
										activity.resultadoJuego(true, false);
									}
								} else	{
									activity.logPantalla(" - respuesta recibida: SECA", true);
									if (activity._cara_o_seca.equals("SECA"))	{
										activity.resultadoJuego(false, false);
									} else	{
										activity.resultadoJuego(true, false);
									}
								}
								
								_tmp_nro_confirmacion = messages[i].getOriginatingAddress();
								enviarConfirmacionSMS("+++NUMERO" + activity._numero_aleatorio);
							}
							
						}
						
						if (activity._esperando_confirmacion)	{

							if (messages[i].getMessageBody().startsWith("+++NUMERO"))	{
								
								activity._esperando_confirmacion = false;
								
								activity._numero_aleatorio = messages[i].getMessageBody().substring(9);
								activity.logPantalla(" - número aleatorio recibido: " + activity._numero_aleatorio, true);
								
								byte[] randomBytes = hexStringToByteArray(activity._numero_aleatorio);
								
								// cara o seca (par o impar)?
								String _cara_o_seca_confirmar = "";
								BigInteger randomNum = new BigInteger(1, randomBytes);
								if (randomNum.mod(BigInteger.valueOf(2)) == BigInteger.ZERO) {
									activity.logPantalla(" - el número es par: CARA.", true);
									_cara_o_seca_confirmar = "CARA";
								} else {
									activity.logPantalla(" - el número es impar: SECA.", true);
									_cara_o_seca_confirmar = "SECA";
								}
								
								// sha-512 del número aleatorio
								MessageDigest md;
								try {
									md = MessageDigest.getInstance("SHA-512");
								} catch (NoSuchAlgorithmException e) {
									activity.logPantalla("ERROR: PROTOCOLO SHA-512 NO SOPORTADO.", true);
									return;
								}
								byte[] hashBytes = md.digest(randomBytes);
								String hashStrConfirmar = String.format("%0128x",
										new BigInteger(1, hashBytes));
								activity.logPantalla(" - SHA-512 del número aleatorio calculado: "
										+ hashStrConfirmar, true);
								if (hashStrConfirmar.equals(activity._sha512_en_uso)) {
									activity.logPantalla("El hash calculado coincide con el recibido anteriormente.No se hizo trampa.", true);
								} else {
									activity.logPantalla("El hash calculado no coincide con el recibido anteriormente. TRAMPA.", true);
									return;
								}
								
								if (activity._cara_o_seca.equals(_cara_o_seca_confirmar))	{
									activity.resultadoJuego(true, true);
								} else	{
									activity.resultadoJuego(false, true);
								}

								
							}
							
						}
	
					}
				}
			}
		}
	}
	
	private void enviarRespuestaSMS(String sms)	{
		SmsManager smsManager = SmsManager.getDefault();
		smsManager.sendTextMessage(_tmp_nro_respuesta, null, sms, null, null);
		Toast toast1 = Toast.makeText(
				activity.getApplicationContext(), "Enviando respuesta vía SMS...",
				Toast.LENGTH_SHORT);
		toast1.show();
		activity.logPantalla(" - se enviará SMS y esperará confirmación...", true);
		activity._esperando_confirmacion = true;
	}

	private void enviarConfirmacionSMS(String sms)	{
		SmsManager smsManager = SmsManager.getDefault();
		smsManager.sendTextMessage(_tmp_nro_confirmacion, null, sms, null, null);
		Toast toast1 = Toast.makeText(
				activity.getApplicationContext(), "Enviando número aleatorio para confirmación vía SMS...",
				Toast.LENGTH_SHORT);
		toast1.show();
		activity.logPantalla(" - se enviará SMS con número aleatorio para su confirmación...", true);
	}
	
	// helper
	public static byte[] hexStringToByteArray(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character
					.digit(s.charAt(i + 1), 16));
		}
		return data;
	}
	
}
