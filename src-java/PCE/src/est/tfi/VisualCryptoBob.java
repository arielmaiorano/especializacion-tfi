package est.tfi;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.Security;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
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
import org.bouncycastle.util.encoders.Base64;

public class VisualCryptoBob extends JFrame {

	private JFrame _frame;
	private JTextField tfPuerto;
	private JButton btnIniciar;
	private JButton btnDetener;
	private JTextArea txtMensajes;
	private InnerTask task;
	private static final long serialVersionUID = 1L;
	private JLabel lblTransparencia1;
	private JPanel pnlTransparencia1;
	private JLabel lblTransparencia2;
	private JPanel pnlTransparencia2;
	private JLabel lblResultado;
	private JPanel pnlResultado;
	private JLabel lblImprimir;

	/**
	 * Create the frame.
	 */
	public VisualCryptoBob() {
		// referencia local
		_frame = this;
		// se agregar� hook al cerrar
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		// tama�o fijo
		setBounds(300, 300, 523, 752);
		setResizable(false);
		// t�tulo
		setTitle("TFI - PCE - Visual Cryptography como Bob");

		JPanel contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JLabel lblPuerto = new JLabel(
				"N\u00FAmero de conexi\u00F3n para esperar a Trent (puerto TCP)");
		lblPuerto.setBounds(10, 11, 300, 14);
		contentPane.add(lblPuerto);

		tfPuerto = new JTextField();
		tfPuerto.setText("22222");
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
		scrollPane.setBounds(10, 73, 500, 120);
		contentPane.add(scrollPane);

		txtMensajes = new JTextArea();
		scrollPane.setViewportView(txtMensajes);
		txtMensajes.setEditable(false);
		txtMensajes.setFont(new Font("Monospaced", Font.PLAIN, 11));
		txtMensajes.setText("Esperando inicio...\n");

		lblTransparencia1 = new JLabel(
				"Transparencia # 1 recibida de parte de Trent");
		lblTransparencia1.setBounds(12, 204, 300, 14);
		contentPane.add(lblTransparencia1);

		pnlTransparencia1 = new JPanel();
		pnlTransparencia1.setBounds(8, 226, 502, 142);
		contentPane.add(pnlTransparencia1);
		pnlTransparencia1.setLayout(new GridLayout(1, 0, 0, 0));

		lblTransparencia2 = new JLabel(
				"Transparencia # 2 recibida de parte de Alice");
		lblTransparencia2.setBounds(10, 377, 478, 14);
		contentPane.add(lblTransparencia2);

		pnlTransparencia2 = new JPanel();
		pnlTransparencia2.setBounds(8, 397, 502, 142);
		contentPane.add(pnlTransparencia2);
		pnlTransparencia2.setLayout(new GridLayout(1, 0, 0, 0));

		lblResultado = new JLabel("Resultado al suporponer transparencias");
		lblResultado.setBounds(10, 550, 501, 14);
		contentPane.add(lblResultado);

		pnlResultado = new JPanel();
		pnlResultado.setBounds(8, 570, 502, 142);
		contentPane.add(pnlResultado);
		pnlResultado.setLayout(new GridLayout(1, 0, 0, 0));
		
		lblImprimir = new JLabel("click sobre la imagen para imprimir");
		lblImprimir.setFont(new Font("Tahoma", Font.ITALIC, 8));
		lblImprimir.setBounds(380, 209, 136, 14);
		contentPane.add(lblImprimir);
		lblImprimir.setVisible(false);

		// hook al cerrar
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				// ((JFrame)e.getSource()).dispose();
				_frame.dispose();
			}
		});

	}

	private class InnerTask extends SwingWorker<Void, String> {

		String recv_2daParteNombre = null;
		String recv_2daParteDireccion = null;
		String recv_2daPartePuerto = null;
		String recv_imagen1 = null;
		String seleccion = null;

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

				// esperar conexi�n (y env�o) de parte de Trent
				publish("Esperando conexi�n de Trent...");
				TcpSimple tcp = new TcpSimple();
				tcp.escuchar(Integer.parseInt(tfPuerto.getText()));
				publish("Esperando datos de parte de Trent...");

				// recibir valores e imagen
				recv_2daParteNombre = tcp.recibirString();
				recv_2daParteDireccion = tcp.recibirString();
				recv_2daPartePuerto = tcp.recibirString();
				publish("Informaci�n de la otra parte: " + recv_2daParteNombre
						+ " @ " + recv_2daParteDireccion + ":"
						+ recv_2daPartePuerto);
				recv_imagen1 = tcp.recibirString();
				publish("Imagen de transparencia recibida: " + recv_imagen1);

				// cargar imagen de transparencia # 1
				InputStream in = new ByteArrayInputStream(
						Base64.decode(recv_imagen1.getBytes("ISO-8859-1")));
				BufferedImage canvasT1 = ImageIO.read(in);

				// pintar panel con imagen de transparencia # 1
				JLabel canvasT1Label = new JLabel(new ImageIcon(canvasT1));
				pnlTransparencia1.removeAll();
				pnlTransparencia1.add(canvasT1Label, null);
				pnlTransparencia1.setBorder(BorderFactory.createLineBorder(
						Color.gray, 1));
				pnlTransparencia1.setBorder(BorderFactory.createEmptyBorder());

				// hacer "imprimible" la imagen
				pnlTransparencia1.addMouseListener(new MouseAdapter() {
					public void mouseReleased(MouseEvent evt) {
						VisualCryptoTrent.imprimirPanel(pnlTransparencia1);
					}
				});
				lblImprimir.setVisible(true);

				// terminar, dejar de escuchar en el puerto
				tcp.desconectar();

				// preguntar por espera o incio contra parte
				seleccion = null;
				publish("+++SELECCION");
				while (seleccion == null) {
					Thread.sleep(200);
				}
				if (seleccion.equals("")) {
					throw new Exception("Se abortar� protocolo.");
				}

				if (seleccion.equals("esperar")) {

					publish("Se esperar� a que " + recv_2daParteNombre
							+ " comparta su transparencia...");

					// recibir segunda imagen
					TcpSimple tcp2 = new TcpSimple();
					tcp2.escuchar(Integer.parseInt(tfPuerto.getText()));
					String quienComparte = tcp2.recibirString();
					String imagenComparte = tcp2.recibirString();
					tcp2.desconectar();
					publish("Imagen recibida de parte de " + quienComparte
							+ ": " + imagenComparte);

					// cargar imagen de transparencia # 2
					InputStream in2 = new ByteArrayInputStream(
							Base64.decode(imagenComparte.getBytes("ISO-8859-1")));
					BufferedImage canvasT2 = ImageIO.read(in2);

					// pintar panel con imagen de transparencia # 1
					JLabel canvasT2Label = new JLabel(new ImageIcon(canvasT2));
					pnlTransparencia2.removeAll();
					pnlTransparencia2.add(canvasT2Label, null);
					pnlTransparencia2.setBorder(BorderFactory.createLineBorder(
							Color.gray, 1));
					pnlTransparencia2.setBorder(BorderFactory
							.createEmptyBorder());

					// reconstruir imagen
					BufferedImage canvasR = new BufferedImage(500, 140,
							BufferedImage.TYPE_INT_RGB);
					for (int y = 0; y < canvasR.getHeight(); y++) {
						for (int x = 0; x < canvasR.getWidth(); x++) {
							if (new Color(canvasT1.getRGB(x, y))
									.equals(Color.white)
									&& new Color(canvasT2.getRGB(x, y))
											.equals(Color.white)) {
								canvasR.setRGB(x, y, Color.white.getRGB());
							} else {
								canvasR.setRGB(x, y, Color.black.getRGB());
							}
						}
					}

					// pintar panel con imagen resultado de superposici�n
					JLabel canvasRLabel = new JLabel(new ImageIcon(canvasR));
					pnlResultado.removeAll();
					pnlResultado.add(canvasRLabel, null);
					pnlResultado.setBorder(BorderFactory.createLineBorder(
							Color.gray, 1));
					pnlResultado.setBorder(BorderFactory.createEmptyBorder());

					publish("Imagen de superposici�n generada.");

				} else if (seleccion.equals("compartir_con_2da_parte")) {

					publish("Enviando transparencia a " + recv_2daParteNombre
							+ "...");
					TcpSimple tcpComparte = new TcpSimple();
					tcpComparte.conectar(recv_2daParteDireccion,
							Integer.parseInt(recv_2daPartePuerto));
					tcpComparte.enviarString("Bob");
					tcpComparte.enviarString(recv_imagen1);
					tcpComparte.desconectar();
					publish("Informaci�n enviada.");
				}

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
							if (str.equals("+++SELECCION")) {
								Object[] opciones = {
										"Esperar a " + recv_2daParteNombre,
										"Compartir con " + recv_2daParteNombre };
								int n = JOptionPane.showOptionDialog(_frame,
										"�C�mo reconstruir la imagen secreta?",
										"TFI - Selecci�n",
										JOptionPane.YES_NO_CANCEL_OPTION,
										JOptionPane.QUESTION_MESSAGE, null,
										opciones, null);
								if (n != JOptionPane.CLOSED_OPTION) {
									if (n == JOptionPane.YES_OPTION) {
										seleccion = "esperar";
									} else {
										seleccion = "compartir_con_2da_parte";
									}
								} else {
									seleccion = "";
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
