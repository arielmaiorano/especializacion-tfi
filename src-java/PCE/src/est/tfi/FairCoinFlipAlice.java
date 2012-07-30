package est.tfi;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigInteger;
import java.security.MessageDigest;
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

public class FairCoinFlipAlice extends JFrame {

	private JFrame _frame;
	private JTextField tfDestino;
	private JTextField tfPuerto;
	private JButton btnIniciar;
	private JButton btnDetener;
	private JTextArea txtMensajes;
	private InnerTask task;
	private static final long serialVersionUID = 1L;

	/**
	 * Create the frame.
	 */
	public FairCoinFlipAlice() {
		// referencia local
		_frame = this;
		// se agregará hook al cerrar
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		// tamaño fijo
		setBounds(200, 200, 523, 431);
		setResizable(false);
		// título
		setTitle(Principal.appName + " - Fair Coin Flip como Alice");

		JPanel contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JLabel lblDestino = new JLabel(
				"Direcci\u00F3n de Bob (FQDN o direcci\u00F3n IP)");
		lblDestino.setBounds(10, 11, 233, 14);
		contentPane.add(lblDestino);

		tfDestino = new JTextField();
		tfDestino.setText("localhost");
		tfDestino.setBounds(249, 6, 140, 23);
		contentPane.add(tfDestino);
		tfDestino.setColumns(10);

		JLabel lblPuerto = new JLabel(
				"N\u00FAmero de conexi\u00F3n de Bob (puerto TCP)");
		lblPuerto.setBounds(10, 36, 233, 14);
		contentPane.add(lblPuerto);

		tfPuerto = new JTextField();
		tfPuerto.setText("12345");
		tfPuerto.setBounds(249, 31, 70, 23);
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

				// número aleatorio de 16 bytes
				SecureRandom sr = new SecureRandom();
				byte[] randomBytes = new byte[16];
				sr.nextBytes(randomBytes);
				BigInteger randomNum = new BigInteger(1, randomBytes);
				String randomStr = String.format("%032x", randomNum);
				publish("Moneda al aire - generación de número aleatorio (16 bytes): "
						+ randomStr);

				// cara o seca (par o impar)?
				boolean resultoCara = false;
				if (randomNum.mod(BigInteger.valueOf(2)) == BigInteger.ZERO) {
					publish("Resultó número par: CARA.");
					resultoCara = true;
				} else {
					publish("Resultó número impar: SECA.");
				}

				// sha-512 del número aleatorio
				MessageDigest md = MessageDigest.getInstance("SHA-512");
				byte[] hashBytes = md.digest(randomBytes);
				String hashStr = String.format("%0128x", new BigInteger(1,
						hashBytes));
				publish("SHA-512 del número aleatorio: " + hashStr);

				// conectar con Bob
				publish("Enviando hash a Bob...");
				TcpSimple tcp = new TcpSimple();
				tcp.conectar(tfDestino.getText(),
						Integer.parseInt(tfPuerto.getText()));

				// enviar hash y esperar "cara o seca"
				tcp.enviarString(hashStr);
				publish("Hash enviado, esperando elección de \"CARA o SECA\" de parte de Bob...");
				String strRespuesta = tcp.recibirString();

				// respuesta recibida
				publish("Respuesta recibida, Bob eligió: "
						+ strRespuesta
						+ (((strRespuesta.equals("CARA") && resultoCara) || (strRespuesta
								.equals("SECA") && !resultoCara)) ? " - Ganó Bob!"
								: " - Ganó Alice!"));

				// enviar resultado y número original a Bob
				publish("Se enviará resultado y número aleatorio a Bob para que lo confirme...");
				if (resultoCara) {
					tcp.enviarString("CARA");
				} else {
					tcp.enviarString("SECA");
				}
				tcp.enviarString(randomStr);

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
