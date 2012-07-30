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
import javax.swing.JCheckBox;
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

public class AnonMessBroadBob extends JFrame {

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
	private JLabel lblQuienPaga;
	private JSeparator separator_1;
	private JCheckBox chkPaga;

	/**
	 * Create the frame.
	 */
	public AnonMessBroadBob() {
		// referencia local
		_frame = this;
		// se agregar� hook al cerrar
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		// tama�o fijo
		setBounds(300, 300, 523, 431);
		setResizable(false);
		// t�tulo
		setTitle("TFI - PCE - Anonymous Message Broadcast como Bob");

		JPanel contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JLabel lblPuerto = new JLabel(
				"N\u00FAmero de conexi\u00F3n para esperar a Alice (puerto TCP)");
		lblPuerto.setBounds(10, 11, 300, 14);
		contentPane.add(lblPuerto);

		tfPuerto = new JTextField();
		tfPuerto.setText("22222");
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
				"Direcci\u00F3n de Carol (FQDN o direcci\u00F3n IP)");
		lblDestino.setBounds(10, 35, 233, 14);
		contentPane.add(lblDestino);

		tfDestino = new JTextField();
		tfDestino.setText("localhost");
		tfDestino.setColumns(10);
		tfDestino.setBounds(249, 31, 140, 23);
		contentPane.add(tfDestino);

		lblDestinoPuerto = new JLabel(
				"N\u00FAmero de conexi\u00F3n de Carol (puerto TCP)");
		lblDestinoPuerto.setBounds(10, 60, 233, 14);
		contentPane.add(lblDestinoPuerto);

		tfDestinoPuerto = new JTextField();
		tfDestinoPuerto.setText("33333");
		tfDestinoPuerto.setColumns(10);
		tfDestinoPuerto.setBounds(248, 56, 70, 23);
		contentPane.add(tfDestinoPuerto);

		lblQuienPaga = new JLabel(
				"<html>Soy quien pagar\u00E1 la cena (ver <i>Dining cryptographers problem</i>)");
		lblQuienPaga.setBounds(45, 101, 450, 14);
		contentPane.add(lblQuienPaga);

		separator_1 = new JSeparator();
		separator_1.setBounds(10, 125, 500, 14);
		contentPane.add(separator_1);
		
		chkPaga = new JCheckBox("");
		chkPaga.setFont(new Font("Tahoma", Font.PLAIN, 13));
		chkPaga.setBounds(20, 95, 25, 25);
		contentPane.add(chkPaga);

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

				// esperar conexi�n de parte de Alice
				publish("Esperando conexi�n de Alice...");
				TcpSimple tcpAnt = new TcpSimple();
				tcpAnt.escuchar(Integer.parseInt(tfPuerto.getText()));
				String randomStrOtro = tcpAnt.recibirString();
				publish("N�mero aleatorio recibido: " + randomStrOtro);
				byte[] randomBytesOtro = Principal.hexStringToByteArray(randomStrOtro);
				BigInteger randomNumOtro = new BigInteger(1, randomBytesOtro);
				boolean monedaAliceCara = false;
				if (randomNumOtro.mod(BigInteger.valueOf(2)) == BigInteger.ZERO) {
					publish("El n�mero es par: CARA.");
					monedaAliceCara = true;
				} else {
					publish("El n�mero es impar: SECA.");
					monedaAliceCara = false;
				}

				// generar n�mero aleatorio de 16 bytes
				SecureRandom sr = new SecureRandom();
				byte[] randomBytes = new byte[16];
				sr.nextBytes(randomBytes);
				BigInteger randomNum = new BigInteger(1, randomBytes);
				String randomStr = String.format("%032x", randomNum);
				publish("Generaci�n de n�mero aleatorio (16 bytes): "
						+ randomStr);
				boolean monedaBobCara = false;
				if (randomNum.mod(BigInteger.valueOf(2)) == BigInteger.ZERO) {
					publish("El n�mero es par: CARA.");
					monedaBobCara = true;
				} else {
					publish("El n�mero es impar: SECA.");
					monedaBobCara = false;
				}

				// conectar con Carol
				publish("Conectando con Carol...");
				TcpSimple tcp = new TcpSimple();
				tcp.conectar(tfDestino.getText(),
						Integer.parseInt(tfDestinoPuerto.getText()));
				tcp.enviarString(randomStr);
				publish("N�mero aleatorio enviado.");

				// coincidencia / diferencia
				boolean coincidencia = (monedaBobCara && monedaAliceCara) || (!monedaBobCara && !monedaAliceCara);
				publish("Dados los resultados (monedas) se informar�a: " + (coincidencia ? "COINCIDENCIA." : "DIFERENCIA." ));
				boolean coincidencia_enviar = chkPaga.isSelected() ? !coincidencia : coincidencia;
				publish("Considerando la selecci�n de pago se informar�: " + (coincidencia_enviar ? "COINCIDENCIA." : "DIFERENCIA." ));
				
				tcp.enviarString(coincidencia_enviar ? "COINCIDENCIA" : "DIFERENCIA");
				
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
