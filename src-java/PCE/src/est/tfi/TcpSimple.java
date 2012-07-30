package est.tfi;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Cliente/Servidor TCP simple para enviar y recibir strings
 */
public class TcpSimple {

	private ServerSocket _serverSocket = null;
	private Socket _socket = null;
	private final String _escapeoNL = "XXX-NL-XXX";
	private final String _escapeoCR = "XXX-CR-XXX";

	public void escuchar(int puerto) throws UnknownHostException, IOException {
		_serverSocket = new ServerSocket(puerto);
		_socket = _serverSocket.accept();
	}

	public void conectar(String direccion, int puerto)
			throws UnknownHostException, IOException {
		_socket = new Socket(direccion, puerto);
	}

	public void enviarString(String string) throws IOException {
		DataOutputStream out = new DataOutputStream(_socket.getOutputStream());
		out.writeBytes(string.replaceAll("\n", _escapeoNL).replaceAll("\r", _escapeoCR) + "\n");
		// xxx - espera/delay
		try {
			Thread.sleep(250);
		} catch (InterruptedException e) {
			throw new IOException("Interrumpido en espera: " + e.getMessage());
		}
	}

	public String recibirString() throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(
				_socket.getInputStream()));
		return (in.readLine().replaceAll(_escapeoNL, "\n").replaceAll(_escapeoCR, "\r"));
	}

	public void desconectar() throws IOException {
		if (_socket != null)
			_socket.close();
		if (_serverSocket != null)
			_serverSocket.close();
	}

}
