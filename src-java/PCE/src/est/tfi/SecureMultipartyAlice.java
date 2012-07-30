package est.tfi;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.X509EncodedKeySpec;
import java.util.List;

import javax.crypto.Cipher;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;

public class SecureMultipartyAlice extends JFrame {

	private JFrame _frame;
	private JTextField tfPuerto;
	private JButton btnIniciar;
	private JButton btnDetener;
	private JTextArea txtMensajes;
	private InnerTask task;
	private static final long serialVersionUID = 1L;
	private JLabel lblDestino;
	private JTextField tfDestino;
	private JLabel lblDestinoPuerto;
	private JTextField tfDestinoPuerto;
	private JTextField tfSalario;

	/**
	 * Create the frame.
	 */
	public SecureMultipartyAlice() {
		// referencia local
		_frame = this;
		// se agregar� hook al cerrar
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		// tama�o fijo
		setBounds(300, 300, 523, 431);
		setResizable(false);
		// t�tulo
		setTitle("TFI - PCE - Secure Multiparty Computation como Alice");

		JPanel contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JLabel lblPuerto = new JLabel(
				"N\u00FAmero de conexi\u00F3n para esperar a Dave (puerto TCP)");
		lblPuerto.setBounds(10, 11, 300, 14);
		contentPane.add(lblPuerto);

		tfPuerto = new JTextField();
		tfPuerto.setText("11111");
		tfPuerto.setBounds(320, 6, 70, 23);
		contentPane.add(tfPuerto);
		tfPuerto.setColumns(10);

		JSeparator separator = new JSeparator();
		separator.setBounds(10, 90, 500, 14);
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
		scrollPane.setBounds(10, 130, 500, 260);
		contentPane.add(scrollPane);

		txtMensajes = new JTextArea();
		scrollPane.setViewportView(txtMensajes);
		txtMensajes.setEditable(false);
		txtMensajes.setFont(new Font("Monospaced", Font.PLAIN, 11));
		txtMensajes.setText("Esperando inicio...\n");

		lblDestino = new JLabel(
				"Direcci\u00F3n de Bob (FQDN o direcci\u00F3n IP)");
		lblDestino.setBounds(10, 35, 233, 14);
		contentPane.add(lblDestino);

		tfDestino = new JTextField();
		tfDestino.setText("localhost");
		tfDestino.setColumns(10);
		tfDestino.setBounds(249, 31, 140, 23);
		contentPane.add(tfDestino);

		lblDestinoPuerto = new JLabel(
				"N\u00FAmero de conexi\u00F3n de Bob (puerto TCP)");
		lblDestinoPuerto.setBounds(10, 60, 233, 14);
		contentPane.add(lblDestinoPuerto);

		tfDestinoPuerto = new JTextField();
		tfDestinoPuerto.setText("22222");
		tfDestinoPuerto.setColumns(10);
		tfDestinoPuerto.setBounds(248, 56, 70, 23);
		contentPane.add(tfDestinoPuerto);

		JLabel lblSalario = new JLabel(
				"Salario o sueldo propio para c\u00F3mputo seguro de promedio ($)");
		lblSalario.setBounds(10, 100, 360, 14);
		contentPane.add(lblSalario);

		tfSalario = new JTextField();
		tfSalario.setHorizontalAlignment(SwingConstants.RIGHT);
		tfSalario.setText("5000");
		tfSalario.setColumns(10);
		tfSalario.setBounds(380, 96, 70, 23);
		contentPane.add(tfSalario);

		JSeparator separator_1 = new JSeparator();
		separator_1.setBounds(10, 125, 500, 14);
		contentPane.add(separator_1);

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

				// generar llave p�blica y privada (1024 bits)
				publish("Generando llave p�blica y llave privada (1024 bits)...");
				KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA",
						"BC");
				keyGen.initialize(1024, new SecureRandom());
				KeyPair keyPair = keyGen.generateKeyPair();
				publish("Llave privada generada:\n"
						+ keyPair.getPrivate().toString());
				publish("Llave p�blica generada:\n"
						+ keyPair.getPublic().toString());

				// n�mero aleatorio de 16 bytes
				SecureRandom sr = new SecureRandom();
				byte[] randomBytes = new byte[16];
				sr.nextBytes(randomBytes);
				BigInteger randomNum = new BigInteger(1, randomBytes);
				String randomStr = String.format("%032x", randomNum);
				publish("Generaci�n de n�mero aleatorio (16 bytes): "
						+ randomStr);

				// salario + n�mero aleatorio
				BigInteger randomNumPlusSalario = randomNum.add(new BigInteger(
						tfSalario.getText()));
				String randomPlusSalarioStr = String.format("%032x",
						randomNumPlusSalario);
				publish("Salario sumado a n�mero aleatorio: "
						+ randomPlusSalarioStr);

				// conectar con Bob
				publish("Conectando con Bob para obtener su llave p�blica...");
				TcpSimple tcp = new TcpSimple();
				tcp.conectar(tfDestino.getText(),
						Integer.parseInt(tfDestinoPuerto.getText()));
				tcp.enviarString("LLAVE?");
				String llavePubOtroStr = tcp.recibirString();
				byte[] llavePubOtroBytes = Base64.decode(llavePubOtroStr
						.getBytes("ISO-8859-1"));
				X509EncodedKeySpec spec = new X509EncodedKeySpec(
						llavePubOtroBytes);
				KeyFactory factory = KeyFactory.getInstance("RSA", "BC");
				PublicKey llavePubOtro = factory.generatePublic(spec);
				publish("Llave p�blica obtenida:\n" + llavePubOtro.toString());

				// cifrar n�mero con llave p�blica de Bob
				Cipher cipher = Cipher.getInstance("RSA", "BC");
				cipher.init(Cipher.ENCRYPT_MODE, llavePubOtro);
				byte[] cifradoBytes = cipher.doFinal(randomPlusSalarioStr
						.getBytes("ISO-8859-1"));
				String cifradoStr = new String(Base64.encode(cifradoBytes),
						"ISO-8859-1");
				publish("Texto cifrado (n�mero + salario) con llave p�blica de Bob: "
						+ cifradoStr);
				tcp.enviarString(cifradoStr);
				publish("Texto cifrado enviado a Bob.");

				// esperar conexi�n (y pedido de llave) de parte de Dave
				publish("Esperando conexi�n de Dave...");
				TcpSimple tcpAnt = new TcpSimple();
				tcpAnt.escuchar(Integer.parseInt(tfPuerto.getText()));
				String xxx = tcpAnt.recibirString();
				if (xxx.equals("LLAVE?")) {
					String llavePubStr = new String(Base64.encode(keyPair
							.getPublic().getEncoded()), "ISO-8859-1");
					tcpAnt.enviarString(llavePubStr);
					publish("Pedido de llave p�blica recibido y respondido.");
				}

				// recibir texto cifrado, descifrar n�mero y calcular promedio
				publish("Esperando n�mero a computar...");
				String cifradoRecibidoStr = tcpAnt.recibirString();
				publish("Texto cifrado recibido: " + cifradoRecibidoStr);
				byte[] cifradoRecibidoBytes = Base64.decode(cifradoRecibidoStr);
				Cipher cipherDec = Cipher.getInstance("RSA", "BC");
				cipherDec.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
				byte[] decifradoBytes = cipherDec.doFinal(cifradoRecibidoBytes);
				String numStr = new String(decifradoBytes, "ISO-8859-1");
				publish("N�mero descifrado: " + numStr);
				byte[] numBytes = Principal.hexStringToByteArray(numStr);
				BigInteger numBI = new BigInteger(1, numBytes);
				BigInteger numResultadoBI = numBI.subtract(randomNum).divide(
						BigInteger.valueOf(4));
				publish("N�mero recibido menos n�mero aleatorio dividido cantidad de partes (4): $ "
						+ numResultadoBI);

				// terminar, cerrar conexiones
				tcp.desconectar();
				tcpAnt.desconectar();

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
