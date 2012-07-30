package est.tfi;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.ByteArrayOutputStream;
import java.security.SecureRandom;
import java.security.Security;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
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
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;

public class VisualCryptoTrent extends JFrame {

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
	private BufferedImage canvas;
	protected JPanel pnlTransparencia1;
	protected JPanel pnlTransparencia2;

	/**
	 * Create the frame.
	 */
	public VisualCryptoTrent() {
		// referencia local
		_frame = this;
		// se agregar� hook al cerrar
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		// tama�o fijo
		setBounds(200, 200, 523, 721);
		setResizable(false);
		// t�tulo
		setTitle("TFI - PCE - Visual Cryptography como Trent");

		JPanel contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JLabel lblDestinoBob = new JLabel(
				"Direcci\u00F3n de Bob (FQDN o direcci\u00F3n IP)");
		lblDestinoBob.setBounds(12, 161, 233, 14);
		contentPane.add(lblDestinoBob);

		tfDestinoBob = new JTextField();
		tfDestinoBob.setText("localhost");
		tfDestinoBob.setBounds(251, 157, 140, 23);
		contentPane.add(tfDestinoBob);
		tfDestinoBob.setColumns(10);

		JLabel lblPuertoBob = new JLabel(
				"N\u00FAmero de conexi\u00F3n de Bob (puerto TCP)");
		lblPuertoBob.setBounds(12, 186, 233, 14);
		contentPane.add(lblPuertoBob);

		tfPuertoBob = new JTextField();
		tfPuertoBob.setText("22222");
		tfPuertoBob.setBounds(250, 182, 70, 23);
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
		scrollPane.setBounds(10, 217, 500, 120);
		contentPane.add(scrollPane);

		txtMensajes = new JTextArea();
		scrollPane.setViewportView(txtMensajes);
		txtMensajes.setEditable(false);
		txtMensajes.setFont(new Font("Monospaced", Font.PLAIN, 11));
		txtMensajes.setText("Esperando inicio...\n");

		JLabel lblDestinoAlice = new JLabel(
				"Direcci\u00F3n de Alice (FQDN o direcci\u00F3n IP)");
		lblDestinoAlice.setBounds(12, 106, 233, 14);
		contentPane.add(lblDestinoAlice);

		JLabel lblPuertoAlice = new JLabel(
				"N\u00FAmero de conexi\u00F3n de Alice (puerto TCP)");
		lblPuertoAlice.setBounds(12, 131, 233, 14);
		contentPane.add(lblPuertoAlice);

		tfDestinoAlice = new JTextField();
		tfDestinoAlice.setText("localhost");
		tfDestinoAlice.setColumns(10);
		tfDestinoAlice.setBounds(251, 102, 140, 23);
		contentPane.add(tfDestinoAlice);

		tfPuertoAlice = new JTextField();
		tfPuertoAlice.setText("11111");
		tfPuertoAlice.setColumns(10);
		tfPuertoAlice.setBounds(250, 127, 70, 23);
		contentPane.add(tfPuertoAlice);

		JLabel lblImagenSecreta = new JLabel("Imagen secreta");
		lblImagenSecreta.setBounds(10, 10, 109, 14);
		contentPane.add(lblImagenSecreta);

		JSeparator separator_1 = new JSeparator();
		separator_1.setBounds(11, 93, 500, 14);
		contentPane.add(separator_1);

		JPanel pnlImagenSecreta = new JPanel();
		pnlImagenSecreta.setBounds(120, 6, 272, 82);
		contentPane.add(pnlImagenSecreta);

		// hook al cerrar
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				// ((JFrame)e.getSource()).dispose();
				_frame.dispose();
			}
		});

		// inicializaci�n de canvas para dibujar secreto
		canvas = new BufferedImage(250, 70, BufferedImage.TYPE_INT_RGB);
		Graphics g = canvas.getGraphics();
		g.setColor(Color.white);
		g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
		JLabel canvasLabel = new JLabel(new ImageIcon(canvas));
		pnlImagenSecreta.add(canvasLabel, null);
		pnlImagenSecreta.setBackground(Color.gray);
		pnlImagenSecreta.setBorder(BorderFactory
				.createBevelBorder(BevelBorder.RAISED));

		JLabel lblImagenSecreta2 = new JLabel(
				"<html>(Dibujar sobre fondo<br />en blanco lo que ser\u00E1<br /> la imagen secreta a<br />compartir)");
		lblImagenSecreta2.setVerticalAlignment(SwingConstants.TOP);
		lblImagenSecreta2.setFont(new Font("Tahoma", Font.ITALIC, 10));
		lblImagenSecreta2.setBounds(10, 25, 109, 57);
		contentPane.add(lblImagenSecreta2);

		pnlTransparencia1 = new JPanel();
		pnlTransparencia1.setBounds(8, 370, 502, 142);
		contentPane.add(pnlTransparencia1);
		pnlTransparencia1.setLayout(new GridLayout(1, 0, 0, 0));

		pnlTransparencia2 = new JPanel();
		pnlTransparencia2.setBounds(8, 541, 502, 142);
		contentPane.add(pnlTransparencia2);
		pnlTransparencia2.setLayout(new GridLayout(1, 0, 0, 0));

		JLabel lblTransparencia1 = new JLabel(
				"Transparencia # 1, a enviar a Alice (shares o partes aleatorios)");
		lblTransparencia1.setBounds(12, 348, 498, 14);
		contentPane.add(lblTransparencia1);

		JLabel lblTransparencia2 = new JLabel(
				"Transparencia # 2, a enviar a Bob (shares o partes complementarios a los anteriores)");
		lblTransparencia2.setBounds(12, 521, 501, 14);
		contentPane.add(lblTransparencia2);

		canvasLabel.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent evt) {
				jPanel2MousePressed(evt);
			}

			public void mouseReleased(MouseEvent evt) {
				jPanel2MouseReleased(evt);
			}
		});
		canvasLabel.addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseDragged(MouseEvent evt) {
				jPanel2MouseDragged(evt);
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

				publish("Generando transparencias...");

				// generador aleatorio para generar primera transparencia
				SecureRandom sr1 = new SecureRandom();
				byte[] randomByte = new byte[1];

				// shares posibles (4 x 4 pixles, 6 combinaciones)
				int shares[][] = new int[6][4];
				int rgbBlanco = Color.white.getRGB();
				int rgbNegro = Color.black.getRGB();
				shares[0][0] = rgbBlanco; // negros a la derecha
				shares[0][1] = rgbNegro;
				shares[0][2] = rgbBlanco;
				shares[0][3] = rgbNegro;
				shares[1][0] = rgbNegro; // negros a la izquierda
				shares[1][1] = rgbBlanco;
				shares[1][2] = rgbNegro;
				shares[1][3] = rgbBlanco;
				shares[2][0] = rgbNegro; // negros arriba
				shares[2][1] = rgbNegro;
				shares[2][2] = rgbBlanco;
				shares[2][3] = rgbBlanco;
				shares[3][0] = rgbBlanco; // negros abajo
				shares[3][1] = rgbBlanco;
				shares[3][2] = rgbNegro;
				shares[3][3] = rgbNegro;
				shares[4][0] = rgbBlanco; // diagonal, 1er. negro arriba/derecha
				shares[4][1] = rgbNegro;
				shares[4][2] = rgbNegro;
				shares[4][3] = rgbBlanco;
				shares[5][0] = rgbNegro; // diagonal, 1er. negro arriba/izq.
				shares[5][1] = rgbBlanco;
				shares[5][2] = rgbBlanco;
				shares[5][3] = rgbNegro;

				// im�genes de transparencias
				BufferedImage canvasT1 = new BufferedImage(500, 140,
						BufferedImage.TYPE_INT_RGB);
				BufferedImage canvasT2 = new BufferedImage(500, 140,
						BufferedImage.TYPE_INT_RGB);
				for (int y = 0; y < canvas.getHeight(); y++) {
					for (int x = 0; x < canvas.getWidth(); x++) {
						// coordenas en im�genes de salida (tama�o doblado)
						int yy = y * 2;
						int xx = x * 2;
						// obtener share aleatoriamente (alguno de los 6
						// posibles)
						sr1.nextBytes(randomByte);
						int share = (randomByte[0] % 6);
						// xxx, java %
						if (share < 0)
							share *= -1;
						// pintar transparencia # 1 (aleatoria)
						canvasT1.setRGB(xx, yy, shares[share][0]);
						canvasT1.setRGB(xx + 1, yy, shares[share][1]);
						canvasT1.setRGB(xx, yy + 1, shares[share][2]);
						canvasT1.setRGB(xx + 1, yy + 1, shares[share][3]);
						// pintar transparencia # 2, pixel pintado se invierte
						if (new Color(canvas.getRGB(x, y)).equals(Color.white)) {
							canvasT2.setRGB(xx, yy, shares[share][0]);
							canvasT2.setRGB(xx + 1, yy, shares[share][1]);
							canvasT2.setRGB(xx, yy + 1, shares[share][2]);
							canvasT2.setRGB(xx + 1, yy + 1, shares[share][3]);
						} else {
							canvasT2.setRGB(xx, yy,
									shares[share][0] == rgbNegro ? rgbBlanco
											: rgbNegro);
							canvasT2.setRGB(xx + 1, yy,
									shares[share][1] == rgbNegro ? rgbBlanco
											: rgbNegro);
							canvasT2.setRGB(xx, yy + 1,
									shares[share][2] == rgbNegro ? rgbBlanco
											: rgbNegro);
							canvasT2.setRGB(xx + 1, yy + 1,
									shares[share][3] == rgbNegro ? rgbBlanco
											: rgbNegro);
						}
					}
				}

				// pintar panel con imagen de transparencia # 1
				JLabel canvasT1Label = new JLabel(new ImageIcon(canvasT1));
				pnlTransparencia1.removeAll();
				pnlTransparencia1.add(canvasT1Label, null);
				pnlTransparencia1.setBorder(BorderFactory.createLineBorder(
						Color.gray, 1));
				pnlTransparencia1.setBorder(BorderFactory.createEmptyBorder());

				// pintar panel con imagen de transparencia # 2
				JLabel canvasT2Label = new JLabel(new ImageIcon(canvasT2));
				pnlTransparencia2.removeAll();
				pnlTransparencia2.add(canvasT2Label, null);
				pnlTransparencia2.setBorder(BorderFactory.createLineBorder(
						Color.gray, 1));
				pnlTransparencia2.setBorder(BorderFactory.createEmptyBorder());

				// hacer "imprimibles" las im�genes
				pnlTransparencia1.addMouseListener(new MouseAdapter() {
					public void mouseReleased(MouseEvent evt) {
						VisualCryptoTrent.imprimirPanel(pnlTransparencia1);
					}
				});
				pnlTransparencia2.addMouseListener(new MouseAdapter() {
					public void mouseReleased(MouseEvent evt) {
						VisualCryptoTrent.imprimirPanel(pnlTransparencia2);
					}
				});
				
				// bajar im�genes a strings base64

				ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
				ImageIO.write(canvasT1, "png", baos1);
				baos1.flush();
				byte[] bytesT1 = baos1.toByteArray();
				baos1.close();
				String canvasT1Str = new String(Base64.encode(bytesT1),
						"ISO-8859-1");
				publish("Transparencia #1 generada: " + canvasT1Str);

				ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
				ImageIO.write(canvasT2, "png", baos2);
				baos2.flush();
				byte[] bytesT2 = baos2.toByteArray();
				baos2.close();
				String canvasT2Str = new String(Base64.encode(bytesT2),
						"ISO-8859-1");
				publish("Transparencia #2 generada: " + canvasT2Str);

				// conectar con las otras tres partes y enviar datos

				publish("Enviando transparencia # 1 a Alice...");
				TcpSimple tcpAlice = new TcpSimple();
				tcpAlice.conectar(tfDestinoAlice.getText(),
						Integer.parseInt(tfPuertoAlice.getText()));
				tcpAlice.enviarString("Bob");
				tcpAlice.enviarString(tfDestinoBob.getText());
				tcpAlice.enviarString(tfPuertoBob.getText());
				tcpAlice.enviarString(canvasT1Str);
				tcpAlice.desconectar();
				publish("Informaci�n enviada.");

				publish("Enviando transparencia # 2 a Bob...");
				TcpSimple tcpBob = new TcpSimple();
				tcpBob.conectar(tfDestinoBob.getText(),
						Integer.parseInt(tfPuertoBob.getText()));
				tcpBob.enviarString("Alice");
				tcpBob.enviarString(tfDestinoAlice.getText());
				tcpBob.enviarString(tfPuertoAlice.getText());
				tcpBob.enviarString(canvasT2Str);
				tcpBob.desconectar();
				publish("Informaci�n enviada.");

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

	// /////////////////////////////////////////////////////////////////////////
	// Variables miembro y m�todos espec�ficos para el dibujo del secreto
	// /////////////////////////////////////////////////////////////////////////

	int currentX, currentY, oldX, oldY;
	int grosorLapiz = 7;

	// update/redibujo del canvas del panel para dibujar secreto
	public void updateCanvas() {
		Graphics2D g2 = canvas.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setPaint(Color.black);
		g2.fillOval(currentX - grosorLapiz, currentY - grosorLapiz,
				grosorLapiz, grosorLapiz);
		repaint();
	}

	// evento sobre el canvas
	private void jPanel2MouseReleased(MouseEvent evt) {
		currentX = evt.getX();
		currentY = evt.getY();
	}

	// evento sobre el canvas
	private void jPanel2MouseDragged(MouseEvent evt) {
		currentX = evt.getX();
		currentY = evt.getY();
		updateCanvas();
		oldX = currentX;
		oldY = currentY;
	}

	// evento sobre el canvas
	private void jPanel2MousePressed(MouseEvent evt) {
		oldX = evt.getX();
		oldY = evt.getY();
	}

	// impresi�n
	public static void imprimirPanel(final JPanel componente) {
		PrinterJob pj = PrinterJob.getPrinterJob();
		pj.setJobName("Impresi�n");
		pj.setPrintable(new Printable() {
			public int print(Graphics graphics, PageFormat pageFormat,
					int pageIndex) throws PrinterException {
				if (pageIndex > 0) {
					return Printable.NO_SUCH_PAGE;
				}
				Graphics2D g2 = (Graphics2D) graphics;
				g2.translate(pageFormat.getImageableX(),
						pageFormat.getImageableY());
				componente.paint(g2);
				return Printable.PAGE_EXISTS;
			}
		});
		if (pj.printDialog() == false)
			return;
		try {
			pj.print();
		} catch (PrinterException ex) {
		}
	}
}
