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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class BitCommitmentAlice extends JFrame {

	private JFrame _frame;
	private JTextField tfDestino;
	private JTextField tfPuerto;
	private JButton btnIniciar;
	private JButton btnDetener;
	private JTextArea txtMensajes;
	private InnerTask task;
	private String valorOferta = null;
	private boolean revelarOferta = false;
	private static final long serialVersionUID = 1L;

	/**
	 * Create the frame.
	 */
	public BitCommitmentAlice() {
		// referencia local
		_frame = this;
		// se agregará hook al cerrar
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		// tamaño fijo
		setBounds(200, 200, 523, 431);
		setResizable(false);
		// título
		setTitle(Principal.appName + " - Bit Commitment como Alice");

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

				// número aleatorio de 16 bytes 1
				SecureRandom sr1 = new SecureRandom();
				byte[] randomBytes1 = new byte[16];
				sr1.nextBytes(randomBytes1);
				BigInteger randomNum1 = new BigInteger(1, randomBytes1);
				String randomStr1 = String.format("%032x", randomNum1);
				publish("Generación de número aleatorio R1 (16 bytes): "
						+ randomStr1);

				// número aleatorio de 16 bytes 1
				SecureRandom sr2 = new SecureRandom();
				byte[] randomBytes2 = new byte[16];
				sr2.nextBytes(randomBytes2);
				BigInteger randomNum2 = new BigInteger(1, randomBytes2);
				String randomStr2 = String.format("%032x", randomNum2);
				publish("Generación de número aleatorio R2 (16 bytes): "
						+ randomStr2);

				// preguntar valor
				valorOferta = null;
				publish("+++VALOR");
				while (valorOferta == null) {
					Thread.sleep(200);
				}
				if (valorOferta.equals("")) {
					throw new Exception("Se abortará protocolo.");
				}
				publish("Valor (bit) a ofertar (comprometer): " + valorOferta);

				// conjunto sobre el que se aplicará hash (string)
				String toHash = randomStr1 + randomStr2 + valorOferta;
				// sha-512 de los números aleatorios y el valor
				MessageDigest md = MessageDigest.getInstance("SHA-512");
				byte[] hashBytes = md.digest(toHash.getBytes("ISO-8859-1"));
				String hashStr = String.format("%0128x", new BigInteger(1,
						hashBytes));
				publish("SHA-512 del conjunto (R1 + R2 + Valor, como string): "
						+ hashStr);

				// conectar con Bob
				publish("Enviando compromsio (R1 y hash) a Bob...");
				TcpSimple tcp = new TcpSimple();
				tcp.conectar(tfDestino.getText(),
						Integer.parseInt(tfPuerto.getText()));
				tcp.enviarString(randomStr1);
				tcp.enviarString(hashStr);
				publish("Compromiso enviado.");

				// xxx
				Thread.sleep(1000);

				// esperar revelación
				revelarOferta = false;
				publish("+++REVELAR");
				while (revelarOferta == false) {
					Thread.sleep(200);
				}
				tcp.enviarString(randomStr1);
				tcp.enviarString(randomStr2);
				tcp.enviarString(valorOferta);
				publish("Valor (bit) a ofertado (comprometido) y números aleatorios enviados (revelados) a Bob.");

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
							if (str.equals("+++VALOR")) {
								String valorTmp = (String) JOptionPane
										.showInputDialog(
												_frame,
												"Ejemplo de oferta económica, favor de ingresar \n"
														+ "valor (bit) de la oferta (compromiso)",
												"TFI - Pregunta",
												JOptionPane.QUESTION_MESSAGE,
												null, null, null);
								if (valorTmp != null && !valorTmp.equals("")) {
									valorOferta = valorTmp;
								} else {
									valorOferta = "";
								}
							}
							// mensaje especial
							if (str.equals("+++REVELAR")) {
								Object[] opciones = { "Revelar ahora" };
								/* int n = */JOptionPane
										.showOptionDialog(
												_frame,
												"Oferta (compromiso) de valor (bit) ya enviado.\nBob a la espera de la revelación del valor...",
												"TFI - Pregunta",
												JOptionPane.YES_OPTION,
												JOptionPane.QUESTION_MESSAGE,
												null, opciones, null);
								// xxx
								// if (n == JOptionPane.YES_OPTION) {
								// revelarOferta = true;
								// } else {
								// throw new Exception(
								// "Se abortará protocolo.");
								// }
								revelarOferta = true;
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
