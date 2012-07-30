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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class BitCommitmentBob extends JFrame {

	private JFrame _frame;
	private JTextField tfPuerto;
	private JButton btnIniciar;
	private JButton btnDetener;
	private JTextArea txtMensajes;
	private InnerTask task;
	private static final long serialVersionUID = 1L;

	/**
	 * Create the frame.
	 */
	public BitCommitmentBob() {
		// referencia local
		_frame = this;
		// se agregará hook al cerrar
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		// tamaño fijo
		setBounds(300, 300, 523, 431);
		setResizable(false);
		// título
		setTitle(Principal.appName + " - Bit Commitment como Bob");

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

				// esperar conexión (y envío de compromiso) de parte de Alice
				publish("Esperando conexión de Alice...");
				TcpSimple tcp = new TcpSimple();
				tcp.escuchar(Integer.parseInt(tfPuerto.getText()));
				publish("Esperando valores de compromiso de parte de Alice...");
				String R1Str = tcp.recibirString();
				publish("R1 recibido: " + R1Str);
				String hashStr = tcp.recibirString();
				publish("SHA-512 recibido: " + hashStr);

				// esperar a que Alice revele su compromiso
				publish("Esperando a que Alice decida revelar el valor... ");
				String R1Str_rev = tcp.recibirString();
				String R2Str_rev = tcp.recibirString();
				String ValorStr_rev = tcp.recibirString();
				publish("Compromiso revelado por parte de Alice.");
				publish("R1 recibido: " + R1Str_rev);
				publish("R2 recibido: " + R2Str_rev);
				publish("Valor recibido: " + ValorStr_rev);

				// verificar
				publish("Se verificarán valores...");
				// conjunto sobre el que se aplicará hash (string)
				String toHash = R1Str_rev + R2Str_rev + ValorStr_rev;
				// sha-512 de los números aleatorios y el valor
				MessageDigest md = MessageDigest.getInstance("SHA-512");
				byte[] hashBytes = md.digest(toHash.getBytes("ISO-8859-1"));
				String hashStr_rev = String.format("%0128x", new BigInteger(1,
						hashBytes));
				publish("SHA-512 del conjunto (R1 + R2 + Valor, como string) calculado: "
						+ hashStr_rev);
				if (hashStr_rev.equals(hashStr) && R1Str_rev.equals(R1Str)) {
					publish("El primer número aleatorio y el hash calculado coinciden con lo esperado. Alice no hizo trampa.");
				} else {
					publish("El primer número aleatorio y el hash calculado no coinciden con lo esperado. Alice hizo trampa.");
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
