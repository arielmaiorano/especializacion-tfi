package est.tfi;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.security.Security;
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

public class SecretSharingTrent extends JFrame {

	private JFrame _frame;
	private JTextField tfDestinoBob;
	private JTextField tfPuertoBob;
	private JButton btnIniciar;
	private JButton btnDetener;
	private JTextArea txtMensajes;
	private InnerTask task;
	private static final long serialVersionUID = 1L;
	private JTextField tfDestinoAlice;
	private JTextField tfPuertoAlice;
	private JTextField tfDestinoCarol;
	private JTextField tfPuertoCarol;
	private JTextField tfSecreto;

	/**
	 * Create the frame.
	 */
	public SecretSharingTrent() {
		// referencia local
		_frame = this;
		// se agregará hook al cerrar
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		// tamaño fijo
		setBounds(200, 200, 523, 431);
		setResizable(false);
		// título
		setTitle(Principal.appName + " - Secret Sharing como Trent");

		JPanel contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JLabel lblDestinoBob = new JLabel(
				"Direcci\u00F3n de Bob (FQDN o direcci\u00F3n IP)");
		lblDestinoBob.setBounds(11, 103, 233, 14);
		contentPane.add(lblDestinoBob);

		tfDestinoBob = new JTextField();
		tfDestinoBob.setText("localhost");
		tfDestinoBob.setBounds(250, 99, 140, 23);
		contentPane.add(tfDestinoBob);
		tfDestinoBob.setColumns(10);

		JLabel lblPuertoBob = new JLabel(
				"N\u00FAmero de conexi\u00F3n de Bob (puerto TCP)");
		lblPuertoBob.setBounds(11, 128, 233, 14);
		contentPane.add(lblPuertoBob);

		tfPuertoBob = new JTextField();
		tfPuertoBob.setText("22222");
		tfPuertoBob.setBounds(249, 124, 70, 23);
		contentPane.add(tfPuertoBob);
		tfPuertoBob.setColumns(10);

		JSeparator separator = new JSeparator();
		separator.setBounds(11, 207, 500, 14);
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
		scrollPane.setBounds(10, 217, 500, 175);
		contentPane.add(scrollPane);

		txtMensajes = new JTextArea();
		scrollPane.setViewportView(txtMensajes);
		txtMensajes.setEditable(false);
		txtMensajes.setFont(new Font("Monospaced", Font.PLAIN, 11));
		txtMensajes.setText("Esperando inicio...\n");

		JLabel lblDestinoAlice = new JLabel(
				"Direcci\u00F3n de Alice (FQDN o direcci\u00F3n IP)");
		lblDestinoAlice.setBounds(11, 48, 233, 14);
		contentPane.add(lblDestinoAlice);

		JLabel lblPuertoAlice = new JLabel(
				"N\u00FAmero de conexi\u00F3n de Alice (puerto TCP)");
		lblPuertoAlice.setBounds(11, 73, 233, 14);
		contentPane.add(lblPuertoAlice);

		tfDestinoAlice = new JTextField();
		tfDestinoAlice.setText("localhost");
		tfDestinoAlice.setColumns(10);
		tfDestinoAlice.setBounds(250, 44, 140, 23);
		contentPane.add(tfDestinoAlice);

		tfPuertoAlice = new JTextField();
		tfPuertoAlice.setText("11111");
		tfPuertoAlice.setColumns(10);
		tfPuertoAlice.setBounds(249, 69, 70, 23);
		contentPane.add(tfPuertoAlice);

		JLabel lblDestinoCarol = new JLabel(
				"Direcci\u00F3n de Carol (FQDN o direcci\u00F3n IP)");
		lblDestinoCarol.setBounds(11, 157, 233, 14);
		contentPane.add(lblDestinoCarol);

		JLabel lblPuertoCarol = new JLabel(
				"N\u00FAmero de conexi\u00F3n de Carol (puerto TCP)");
		lblPuertoCarol.setBounds(11, 182, 233, 14);
		contentPane.add(lblPuertoCarol);

		tfDestinoCarol = new JTextField();
		tfDestinoCarol.setText("localhost");
		tfDestinoCarol.setColumns(10);
		tfDestinoCarol.setBounds(250, 153, 140, 23);
		contentPane.add(tfDestinoCarol);

		tfPuertoCarol = new JTextField();
		tfPuertoCarol.setText("33333");
		tfPuertoCarol.setColumns(10);
		tfPuertoCarol.setBounds(249, 178, 70, 23);
		contentPane.add(tfPuertoCarol);

		JLabel lblSecreto = new JLabel("Secreto a compartir");
		lblSecreto.setBounds(10, 10, 233, 14);
		contentPane.add(lblSecreto);

		tfSecreto = new JTextField();
		tfSecreto.setText("Este es el secreto.");
		tfSecreto.setColumns(10);
		tfSecreto.setBounds(249, 6, 140, 23);
		contentPane.add(tfSecreto);

		JSeparator separator_1 = new JSeparator();
		separator_1.setBounds(10, 35, 380, 14);
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

				String secreto = tfSecreto.getText();
				publish("Secreto a compartir: " + secreto);

				BigInteger biSecreto = new BigInteger(
						secreto.getBytes("ISO-8859-1"));

				publish("Secreto a compartir (num): " + biSecreto);

				BigInteger biPrimo = biSecreto.nextProbablePrime();
				publish("Número (probablemente) primo siguiente: " + biPrimo);
				int tmp_biPrimo_bitCount = biPrimo.bitCount();
				if (tmp_biPrimo_bitCount >= 256) {
					publish("El número es de "
							+ tmp_biPrimo_bitCount
							+ " bits, mayor o igual a mínimo (arbitrario) de 256 bits; se utilizará este número.");
				} else {
					biPrimo = BigInteger
							.probablePrime(1024, new SecureRandom());
					publish("El número es de "
							+ tmp_biPrimo_bitCount
							+ " bits, menor a mínimo (arbitrario) de 256 bits; se usará otro primo aleatorio (pidiendo 1024 bits): "
							+ biPrimo);
				}

				// coeficiente a1
				SecureRandom sr = new SecureRandom();
				byte[] randomBytes = new byte[16];
				sr.nextBytes(randomBytes);
				BigInteger coef_a1 = new BigInteger(1, randomBytes);
				coef_a1 = coef_a1.mod(biPrimo);
				// String coef_a1_Str = String.format("%032x", coef_a1);
				publish("Generación de número aleatorio (coeficiente a1 en Zp) (16 bytes): "
						+ coef_a1);

				// conectar con las otras tres partes y enviar datos

				publish("Enviando información (primo, x, f(x)) a Alice...");
				TcpSimple tcpAlice = new TcpSimple();
				tcpAlice.conectar(tfDestinoAlice.getText(),
						Integer.parseInt(tfPuertoAlice.getText()));
				tcpAlice.enviarString("Bob");
				tcpAlice.enviarString(tfDestinoBob.getText());
				tcpAlice.enviarString(tfPuertoBob.getText());
				tcpAlice.enviarString("Carol");
				tcpAlice.enviarString(tfDestinoCarol.getText());
				tcpAlice.enviarString(tfPuertoCarol.getText());
				tcpAlice.enviarString(biPrimo.toString());
				tcpAlice.enviarString("1");
				BigInteger fxAlice = coef_a1.multiply(BigInteger.valueOf(1))
						.add(biSecreto);
				publish("(x = 1, f(x) = " + fxAlice + ")");
				tcpAlice.enviarString(fxAlice.toString());
				tcpAlice.desconectar();
				publish("Información enviada.");

				publish("Enviando información (primo, x, f(x)) a Bob...");
				TcpSimple tcpBob = new TcpSimple();
				tcpBob.conectar(tfDestinoBob.getText(),
						Integer.parseInt(tfPuertoBob.getText()));
				tcpBob.enviarString("Alice");
				tcpBob.enviarString(tfDestinoAlice.getText());
				tcpBob.enviarString(tfPuertoAlice.getText());
				tcpBob.enviarString("Carol");
				tcpBob.enviarString(tfDestinoCarol.getText());
				tcpBob.enviarString(tfPuertoCarol.getText());
				tcpBob.enviarString(biPrimo.toString());
				tcpBob.enviarString("2");
				BigInteger fxBob = coef_a1.multiply(BigInteger.valueOf(2)).add(
						biSecreto);
				publish("(x = 2, f(x) = " + fxBob + ")");
				tcpBob.enviarString(fxBob.toString());
				tcpBob.desconectar();
				publish("Información enviada.");

				publish("Enviando información (primo, x, f(x)) a Carol...");
				TcpSimple tcpCarol = new TcpSimple();
				tcpCarol.conectar(tfDestinoCarol.getText(),
						Integer.parseInt(tfPuertoCarol.getText()));
				tcpCarol.enviarString("Alice");
				tcpCarol.enviarString(tfDestinoAlice.getText());
				tcpCarol.enviarString(tfPuertoAlice.getText());
				tcpCarol.enviarString("Bob");
				tcpCarol.enviarString(tfDestinoBob.getText());
				tcpCarol.enviarString(tfPuertoBob.getText());
				tcpCarol.enviarString(biPrimo.toString());
				tcpCarol.enviarString("3");
				BigInteger fxCarol = coef_a1.multiply(BigInteger.valueOf(3))
						.add(biSecreto);
				publish("(x = 3, f(x) = " + fxCarol + ")");
				tcpCarol.enviarString(fxCarol.toString());
				tcpCarol.desconectar();
				publish("Información enviada.");

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
