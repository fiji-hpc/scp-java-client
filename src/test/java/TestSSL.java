import java.io.IOException;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;

import com.jcraft.jsch.Session;
import cz.it4i.fiji.scpclient.ScpClient;

public class TestSSL {

	public static void testScp() throws JSchException, IOException {
		try(ScpClient scp = new ScpClient("salomon.it4i.cz", "koz01", "/home/koz01/.ssh/it4i_rsa-np", null)) {
//			System.out.println( scp.upload(
//					Paths.get("/home/koz01/Work/vyzkumnik/fiji/work/aaa/spim-data/exampleSingleChannel(9).czi"), "'/home/koz01/exampleSingleChannel(9).czi'"));
			System.out.println( scp.size("'/home/koz01/exampleSingleChannel(9).czi'"));
		}
	}

	public static void testJSch(final String host) throws JSchException, IOException {
		final String login = "xulman";

		final JSch jsch = new JSch();
		jsch.setKnownHosts("/home/ulman/.ssh/known_hosts");
		jsch.addIdentity("/home/ulman/.ssh/TESTREMOVE");
		final Session session = jsch.getSession(login, host, 22);
		session.setTimeout(5000);

		final java.util.Properties config = new java.util.Properties();
		//reguired on the first run during which it adds line to the known_hosts file,
		//and since then this 'config' is not needed at all
		config.put("StrictHostKeyChecking", "no");
		config.put("trust", "yes");

		//config.put("server_host_key", "ssh-dss");
		session.setConfig(config);

		session.connect();

		if (session.isConnected()) {
			System.out.printf("Connected to host [%s]%n", host);
			System.out.println( session.getServerVersion() );
			System.out.println( session.getClientVersion() );
		} else {
			System.out.printf("Unable to connect to host [%s]%n", host);
		}

		session.disconnect();
	}

	public static void main(String[] args) {
		try {
			//testScp();
			testJSch("barbora.it4i.cz");
			testJSch("karolina.it4i.cz");
		} catch (JSchException e) {
			System.out.println("JSchException: "+e.getMessage());
			e.printStackTrace(System.out);
			throw new RuntimeException(e);
		} catch (IOException e) {
			System.out.println("IOException: "+e.getMessage());
			e.printStackTrace(System.out);
		}
	}
}
