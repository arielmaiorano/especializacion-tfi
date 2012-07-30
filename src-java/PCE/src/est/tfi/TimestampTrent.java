package est.tfi;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;

public class TimestampTrent extends JFrame {

	private JFrame _frame;
	private JTextField tfPuerto;
	private JButton btnIniciar;
	private JButton btnDetener;
	private JTextArea txtMensajes;
	private InnerTask task;
	private static final long serialVersionUID = 1L;
	private JTextField tfPuertoBob;

	/**
	 * Create the frame.
	 */
	public TimestampTrent() {
		// referencia local
		_frame = this;
		// se agregará hook al cerrar
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		// tamaño fijo
		setBounds(300, 300, 523, 431);
		setResizable(false);
		// título
		setTitle("TFI - PCE - Timestamping Services como Trent");

		JPanel contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JLabel lblPuerto = new JLabel(
				"N\u00FAmero de conexi\u00F3n para esperar a Alice (puerto TCP)");
		lblPuerto.setBounds(10, 11, 300, 14);
		contentPane.add(lblPuerto);

		tfPuerto = new JTextField();
		tfPuerto.setText("11111");
		tfPuerto.setBounds(320, 6, 70, 23);
		contentPane.add(tfPuerto);
		tfPuerto.setColumns(10);

		JSeparator separator = new JSeparator();
		separator.setBounds(10, 61, 500, 14);
		contentPane.add(separator);

		btnIniciar = new JButton("Iniciar");
		btnIniciar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				btnIniciar.setEnabled(false);
				btnDetener.setEnabled(true);
				task = new InnerTask();
				task.execute();
			}
		});
		btnIniciar.setBounds(422, 6, 89, 25);
		contentPane.add(btnIniciar);

		btnDetener = new JButton("Detener");
		btnDetener.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				task.cancel(true);
			}
		});
		btnDetener.setEnabled(false);
		btnDetener.setBounds(422, 31, 89, 25);
		contentPane.add(btnDetener);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 73, 500, 320);
		contentPane.add(scrollPane);

		txtMensajes = new JTextArea();
		scrollPane.setViewportView(txtMensajes);
		txtMensajes.setEditable(false);
		txtMensajes.setFont(new Font("Monospaced", Font.PLAIN, 11));
		txtMensajes.setText("Esperando inicio...\n");

		JLabel lblPuertoBob = new JLabel(
				"N\u00FAmero de conexi\u00F3n para esperar a Bob (puerto TCP)");
		lblPuertoBob.setBounds(10, 36, 300, 14);
		contentPane.add(lblPuertoBob);

		tfPuertoBob = new JTextField();
		tfPuertoBob.setText("33333");
		tfPuertoBob.setColumns(10);
		tfPuertoBob.setBounds(320, 33, 70, 23);
		contentPane.add(tfPuertoBob);

		// hook al cerrar
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				// ((JFrame)e.getSource()).dispose();
				_frame.dispose();
			}
		});

	}

	private class InnerTask extends SwingWorker<Void, String> {

		public InnerTask() {
		}

		protected Void doInBackground() throws Exception {
			try {
				// cargar el proveedor bouncy castle (BC)
				Security.addProvider(new BouncyCastleProvider());
				if (Security.getProvider("BC") == null) {
					throw new Exception(
							"Proveedor Bouncy Castle (BC) no disponible");
				}

				// generar llave pública y privada (1024 bits)
				publish("Generando llave pública y llave privada (1024 bits)...");
				KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA",
						"BC");
				keyGen.initialize(1024, new SecureRandom());
				KeyPair keyPair = keyGen.generateKeyPair();
				publish("Llave privada generada:\n"
						+ keyPair.getPrivate().toString());
				publish("Llave pública generada:\n"
						+ keyPair.getPublic().toString());

				// esperar conexión (y envío de hash) de parte de Alice
				publish("Esperando conexión de Alice...");
				TcpSimple tcp = new TcpSimple();
				tcp.escuchar(Integer.parseInt(tfPuerto.getText()));
				String hashStr = tcp.recibirString();
				publish("SHA-512 recibido: " + hashStr);

				// fechado, armar hash + fecha para volver a "hashear"
				DateFormat dateFormat = new SimpleDateFormat(
						"dd/MM/yyyy HH:mm:ss");
				Date date = new Date();
				String fechaStr = dateFormat.format(date);
				publish("Se fechará con la fecha/hora actual: " + fechaStr);
				String hashAFecharStr = hashStr + fechaStr;

				// hash de hash de alice + fecha
				MessageDigest md = MessageDigest.getInstance("SHA-512");
				byte[] hashBytes = md.digest(hashAFecharStr
						.getBytes("ISO-8859-1"));
				String hashFechadoStr = String.format("%0128x", new BigInteger(
						1, hashBytes));
				publish("SHA-512 del hash de Alice + fecha: " + hashFechadoStr);

				// firma de hashFechadoStr y envío a Alice
				Signature signature = Signature.getInstance("SHA256WithRSA",
						"BC");
				signature.initSign(keyPair.getPrivate(), new SecureRandom());
				byte[] afirmar = hashFechadoStr.getBytes("ISO-8859-1");
				signature.update(afirmar);
				byte[] firma = signature.sign();
				String firmaStr = new String(Base64.encode(firma), "ISO-8859-1");
				publish("Fecha a enviar a Alice: " + fechaStr);
				tcp.enviarString(fechaStr);
				publish("Firma o Recibo a enviar a Alice: " + firmaStr);
				tcp.enviarString(firmaStr);
				publish("Recibo enviado.");

				// conexión de bob para recibir llave pública
				publish("Esperando ahora conexión de Bob...");
				TcpSimple tcpBob = new TcpSimple();
				tcpBob.escuchar(Integer.parseInt(tfPuertoBob.getText()));
				String xxx = tcpBob.recibirString();
				if (xxx.equals("LLAVE?")) {
					String llavePub = new String(Base64.encode(keyPair
							.getPublic().getEncoded()), "ISO-8859-1");
					tcpBob.enviarString(llavePub);
					publish("Pedido de llave pública recibido y respondido.");
				}

				/*
				 * RECUPERACION DE PRIVADA PKCS8EncodedKeySpec privateKeySpec =
				 * new PKCS8EncodedKeySpec(keyPair.getPrivate().getEncoded());
				 * KeyFactory keyFactory = KeyFactory.getInstance("RSA", "BC");
				 * PrivateKey privatekey =
				 * keyFactory.generatePrivate(privateKeySpec);
				 */

				/*
				 * RECUPERACION DE PUBLICA X509EncodedKeySpec publicKeySpec =
				 * new X509EncodedKeySpec(keyPair.getPublic().getEncoded());
				 * KeyFactory keyFactory = KeyFactory.getInstance("RSA", "BC");
				 * PublicKey publickey =
				 * keyFactory.generatePublic(publicKeySpec);
				 */

				// terminar, dejar de escuchar en los puertos
				tcp.desconectar();
				tcpBob.desconectar();

				publish("PROTOCOLO TERMINADO.");

			} catch (InterruptedException ie) {
				publish("INTERRUMPIDO: " + ie.getMessage());
			} catch (Exception e) {
				publish("ERROR: " + e.getMessage());
			}
			return null;
		}

		protected void process(List<String> strs) {
			try {
				for (String str : strs) {
					if (str != null && !str.equals("")) {
						if (!str.startsWith("+++")) {
							txtMensajes.setText(txtMensajes.getText() + str
									+ "\n");
						} else {
							// mensaje especial
						}
					}
				}
			} catch (Exception e) {
				txtMensajes.setText(txtMensajes.getText() + "\n" + "ERROR: "
						+ e.getMessage() + "\n");
			}
		}

		protected void done() {
			try {
				btnDetener.setEnabled(false);
				btnIniciar.setEnabled(true);
			} catch (Exception e) {
			}
		}
	}
}
