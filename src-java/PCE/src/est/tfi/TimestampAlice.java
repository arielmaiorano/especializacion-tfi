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

public class TimestampAlice extends JFrame {

	private JFrame _frame;
	private JButton btnIniciar;
	private JButton btnDetener;
	private JTextArea txtMensajes;
	private InnerTask task;
	private String esperando = "";
	private static final long serialVersionUID = 1L;
	private JTextField tfDestinoTrent;
	private JTextField tfPuertoTrent;
	private JTextField tfDestinoBob;
	private JTextField tfPuertoBob;
	private JTextField tfDocumento;

	/**
	 * Create the frame.
	 */
	public TimestampAlice() {
		// referencia local
		_frame = this;
		// se agregará hook al cerrar
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		// tamaño fijo
		setBounds(200, 200, 523, 431);
		setResizable(false);
		// título
		setTitle("TFI - PCE - Timestamping Services como Alice");

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
		separator.setBounds(11, 153, 500, 14);
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
		scrollPane.setBounds(10, 170, 500, 210);
		contentPane.add(scrollPane);

		txtMensajes = new JTextArea();
		scrollPane.setViewportView(txtMensajes);
		txtMensajes.setEditable(false);
		txtMensajes.setFont(new Font("Monospaced", Font.PLAIN, 11));
		txtMensajes.setText("Esperando inicio...\n");

		JLabel lblDestinoTrent = new JLabel(
				"Direcci\u00F3n de Trent (FQDN o direcci\u00F3n IP)");
		lblDestinoTrent.setBounds(11, 48, 233, 14);
		contentPane.add(lblDestinoTrent);

		JLabel lblPuertoTrent = new JLabel(
				"N\u00FAmero de conexi\u00F3n de Trent (puerto TCP)");
		lblPuertoTrent.setBounds(11, 73, 233, 14);
		contentPane.add(lblPuertoTrent);

		tfDestinoTrent = new JTextField();
		tfDestinoTrent.setText("localhost");
		tfDestinoTrent.setColumns(10);
		tfDestinoTrent.setBounds(250, 44, 140, 23);
		contentPane.add(tfDestinoTrent);

		tfPuertoTrent = new JTextField();
		tfPuertoTrent.setText("11111");
		tfPuertoTrent.setColumns(10);
		tfPuertoTrent.setBounds(249, 69, 70, 23);
		contentPane.add(tfPuertoTrent);

		JLabel lblDocumento = new JLabel("Contenido del documento a fechar");
		lblDocumento.setBounds(10, 10, 233, 14);
		contentPane.add(lblDocumento);

		tfDocumento = new JTextField();
		tfDocumento.setText("Documento de prueba.");
		tfDocumento.setColumns(10);
		tfDocumento.setBounds(249, 6, 140, 23);
		contentPane.add(tfDocumento);

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

				String documento = tfDocumento.getText();
				publish("Documento a fechar: " + documento);

				// sha-512 del documento
				MessageDigest md = MessageDigest.getInstance("SHA-512");
				byte[] hashBytes = md.digest(documento.getBytes("ISO-8859-1"));
				String hashStr = String.format("%0128x", new BigInteger(1,
						hashBytes));
				publish("SHA-512 del documento: " + hashStr);

				// conectar con Trent
				publish("Enviando hash a Trent...");
				TcpSimple tcp = new TcpSimple();
				tcp.conectar(tfDestinoTrent.getText(),
						Integer.parseInt(tfPuertoTrent.getText()));

				// enviar hash y esperar fecha y recibo
				tcp.enviarString(hashStr);
				publish("Hash enviado, esperando fecha y recibo de parte de Trent...");
				String strFecha = tcp.recibirString();
				String strRecibo = tcp.recibirString();
				publish("Fechado realizado por Trent: " + strFecha);
				publish("Recibo de parte Trent: " + strRecibo);

				// xxx
				Thread.sleep(500);

				// esperar (?) a para enviar a Bob...
				esperando = null;
				publish("+++ESPERAR");
				while (esperando == null) {
					Thread.sleep(200);
				}
				if (esperando.equals("")) {
					tcp.desconectar();
					throw new Exception("Se abortará protocolo.");
				}

				// enviar respuesta
				publish("Se enviará información a Bob...");
				TcpSimple tcpBob = new TcpSimple();
				tcpBob.conectar(tfDestinoBob.getText(),
						Integer.parseInt(tfPuertoBob.getText()));
				tcpBob.enviarString(documento);
				tcpBob.enviarString(strFecha);
				tcpBob.enviarString(strRecibo);

				// terminar, cerrar conexiones
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
							if (str.equals("+++ESPERAR")) {
								Object[] opciones = { "SI", "NO" };
								int n = JOptionPane
										.showOptionDialog(
												_frame,
												"¿Enviar información a Bob en este momento?",
												"TFI - Pregunta",
												JOptionPane.YES_NO_OPTION,
												JOptionPane.QUESTION_MESSAGE,
												null, opciones, null);
								if (n != JOptionPane.CLOSED_OPTION) {
									if (n == JOptionPane.YES_OPTION) {
										esperando = "SI";
									} else {
										esperando = "";
									}
								} else {
									esperando = "";
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
