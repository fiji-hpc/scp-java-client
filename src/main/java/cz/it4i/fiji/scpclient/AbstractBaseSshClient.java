
package cz.it4i.fiji.scpclient;

import com.jcraft.jsch.Identity;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

import java.io.Closeable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.commons.DoActionEventualy;

public class AbstractBaseSshClient implements Closeable {

	protected static final int MAX_NUMBER_OF_CONNECTION_ATTEMPTS = 5;

	protected static final long TIMEOUT_BETWEEN_CONNECTION_ATTEMPTS = 500;

	private static final int KEEP_ALIVE_MESSAGE_INTEVAL = 5000;

	private static final Logger log = LoggerFactory.getLogger(
		AbstractBaseSshClient.class);

	private String hostName;
	private String username;
	private final JSch jsch = new JSch();
	private Session session;

	private int port = 22;

	private String password = null;

	public AbstractBaseSshClient(String hostName, String username,
		byte[] privateKeyFile) throws JSchException
	{
		init(hostName, username, new ByteIdentity(jsch, privateKeyFile));
	}

	public AbstractBaseSshClient(String hostName, String username,
		Identity privateKeyFile) throws JSchException
	{
		init(hostName, username, privateKeyFile);
	}

	public AbstractBaseSshClient(String hostName, String userName, String keyFile,
		String pass) throws JSchException
	{
		Identity id = IdentityFile.newInstance(keyFile, null, jsch);
		try {
			if (pass != null) {
				id.setPassphrase(pass.getBytes("UTF-8"));
			}
		}
		catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		init(hostName, userName, id);
	}

	public AbstractBaseSshClient(String hostName, String userName,
		String password)
	{
		this.hostName = hostName;
		this.username = userName;
		this.password = password;
	}

	public void setPort(int port) {
		this.port = port;
	}

	@Override
	public void close() {
		if (session != null && session.isConnected()) {
			try (DoActionEventualy actionEventualy = new DoActionEventualy(
				TIMEOUT_BETWEEN_CONNECTION_ATTEMPTS, this::interruptSessionThread))
			{
				session.disconnect();
			}
			log.info("SSH Disconnected");
		}
		session = null;
	}

	protected Session getConnectedSession() throws JSchException {
		if (session == null) {
			session = jsch.getSession(username, hostName, port);

			Properties properties = new Properties();
			properties.setProperty("StrictHostKeyChecking", "no");
			if (this.password != null) {
				session.setPassword(password);
			}
			session.setConfig(properties);

			UserInfo ui = new P_UserInfo();

			session.setUserInfo(ui);

			// Prevent timeout due to user inactivity by regularly sending a message:
			session.setServerAliveInterval(KEEP_ALIVE_MESSAGE_INTEVAL);
		}

		int connectRetry = 0;
		long timoutBetweenRetry = TIMEOUT_BETWEEN_CONNECTION_ATTEMPTS;
		while (!session.isConnected()) {
			try {
				session.connect();
				log.info("SSH Connected");
			}
			catch (JSchException e) {
				if (e.getMessage().contains("Auth fail") || e.getMessage().contains(
					"Packet corrupt"))
				{
					if (connectRetry < MAX_NUMBER_OF_CONNECTION_ATTEMPTS) {
						connectRetry++;
						try {
							Thread.sleep(timoutBetweenRetry);
							timoutBetweenRetry *= 2;
						}
						catch (InterruptedException exc) {
							log.info("Interruption detected");
							throw new JSchException(exc.getMessage(), exc);
						}
						continue;
					}
					e = new AuthFailException(e.getMessage(), e);
				}
				throw e;
			}
		}
		return session;
	}

	private void interruptSessionThread() {
		try {
			Field f = session.getClass().getDeclaredField("connectThread");
			if (!f.isAccessible()) {
				f.setAccessible(true);
				Thread thread = (Thread) f.get(session);
				thread.interrupt();
			}
		}
		catch (NoSuchFieldException | SecurityException | IllegalArgumentException
				| IllegalAccessException exc)
		{
			log.error(exc.getMessage(), exc);
		}
	}

	private void init(String initHostName, String initUsername,
		Identity privateKeyFile) throws JSchException
	{
		this.hostName = initHostName;
		this.username = initUsername;
		jsch.addIdentity(privateKeyFile, null);
	}

	private class P_UserInfo implements UserInfo {

		@Override
		public String getPassphrase() {
			return null;
		}

		@Override
		public String getPassword() {
			return null;
		}

		@Override
		public boolean promptPassword(String message) {
			return false;
		}

		@Override
		public boolean promptPassphrase(String message) {
			return false;
		}

		@Override
		public boolean promptYesNo(String message) {
			return true;
		}

		@Override
		public void showMessage(String message) {}

	}

}
