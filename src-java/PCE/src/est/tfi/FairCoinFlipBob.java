package est.tfi;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.Security;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class FairCoinFlipBob extends JFrame {

	private JFrame _frame;
	private JTextField tfPuerto;
	private JButton btnIniciar;
	private JButton btnDetener;
	private JTextArea txtMensajes;
	private InnerTask task;
	private String eleccion = null;
	private static final long serialVersionUID = 1L;

	/**
	 * Create the frame.
	 */
	public FairCoinFlipBob() {
		// referencia local
		_frame = this;
		// se agregará hook al cerrar
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		// tamaño fijo
		setBounds(300, 300, 523, 431);
		setResizable(false);
		// título
		setTitle(Principal.appName + " - Fair Coin Flip como Bob");

		JPanel contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JLabel lblPuerto = new JLabel(
				"N\u00FAmero de conexi\u00F3n para esperar a Alice (puerto TCP)");
		lblPuerto.setBounds(10, 11, 300, 14);
		contentPane.add(lblPuerto);

		tfPuerto = new JTextField();
		tfPuerto.setText("12345");
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

				// esperar conexión (y envío de hash) de parte de Alice
				publish("Esperando conexión de Alice...");
				TcpSimple tcp = new TcpSimple();
				tcp.escuchar(Integer.parseInt(tfPuerto.getText()));
				String hashStr = tcp.recibirString();
				publish("SHA-512 recibido: " + hashStr);

				// preguntar por "cara o seca"
				eleccion = null;
				publish("+++CARAOSECA");
				while (eleccion == null) {
					Thread.sleep(200);
				}
				if (eleccion.equals("")) {
					tcp.desconectar();
					throw new Exception("Se abortará protocolo.");
				}

				// enviar respuesta
				publish("Se enviará elección de " + eleccion + " a Alice...");
				tcp.enviarString(eleccion);

				// recibir respuesta (qué salió?)
				publish("Se esperará respuesta de parte de Alice...");
				String resultado = tcp.recibirString();
				publish("Alice dice: "
						+ resultado
						+ (resultado.equals(eleccion) ? " - Ganó Bob!"
								: " - Ganó Alice!"));

				// recibir número original
				publish("Se esperará número aleatorio de parte de Alice...");
				String randomStr = tcp.recibirString();
				publish("Número recibido: " + randomStr);
				byte[] randomBytes = Principal.hexStringToByteArray(randomStr);

				// cara o seca (par o impar)?
				BigInteger randomNum = new BigInteger(1, randomBytes);
				if (randomNum.mod(BigInteger.valueOf(2)) == BigInteger.ZERO) {
					publish("El número es par: CARA.");
				} else {
					publish("El número es impar: SECA.");
				}

				// sha-512 del número aleatorio
				MessageDigest md = MessageDigest.getInstance("SHA-512");
				byte[] hashBytes = md.digest(randomBytes);
				String hashStrConfirmar = String.format("%0128x",
						new BigInteger(1, hashBytes));
				publish("SHA-512 del número aleatorio calculado: "
						+ hashStrConfirmar);
				if (hashStrConfirmar.equals(hashStr)) {
					publish("El hash calculado coincide con el recibido anteriormente. Alice no hizo trampa.");
				} else {
					publish("El hash calculado no coincide con el recibido anteriormente. Alice hizo trampa.");
				}

				// terminar, dejar de escuchar en el puerto
				tcp.desconectar();

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
							if (str.equals("+++CARAOSECA")) {
								Object[] opciones = { "CARA", "SECA" };
								int n = JOptionPane.showOptionDialog(_frame,
										"¿Cara o Seca?", "TFI - Pregunta",
										JOptionPane.YES_NO_OPTION,
										JOptionPane.QUESTION_MESSAGE, null,
										opciones, null);
								if (n != JOptionPane.CLOSED_OPTION) {
									if (n == JOptionPane.YES_OPTION) {
										eleccion = "CARA";
									} else {
										eleccion = "SECA";
									}
								} else {
									eleccion = "";
								}
							}
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
