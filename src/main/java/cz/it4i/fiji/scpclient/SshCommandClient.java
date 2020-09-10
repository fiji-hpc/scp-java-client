
package cz.it4i.fiji.scpclient;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Identity;
import com.jcraft.jsch.JSchException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SshCommandClient extends AbstractBaseSshClient {

	private static final Logger log = LoggerFactory.getLogger(
		SshCommandClient.class);

	public SshCommandClient(String hostName, String username, String password) {
		super(hostName, username, password);
	}

	public SshCommandClient(String hostName, String username,
		byte[] privateKeyFile) throws JSchException
	{
		super(hostName, username, privateKeyFile);
	}

	public SshCommandClient(String hostName, String username,
		Identity privateKeyFile) throws JSchException
	{
		super(hostName, username, privateKeyFile);
	}

	public SshCommandClient(String hostName, String userName, String keyFile,
		String pass) throws JSchException
	{
		super(hostName, userName, keyFile, pass);
	}

	public SshExecutionSession openSshExecutionSession(String command) {
		return openSshExecutionSession(command, false);
	}

	public SshExecutionSession openSshExecutionSession(String command,
		boolean pty)
	{
		try {
			ChannelExec channelExec = (ChannelExec) getConnectedSession().openChannel(
				"exec");
			channelExec.setPty(pty);
			channelExec.setCommand(command);
			channelExec.connect();
			return new P_SshExecutionSession(channelExec);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public List<String> executeCommand(String command) {
		List<String> result = new LinkedList<>();
		List<String> errors = new LinkedList<>();
		try (SshExecutionSession session = openSshExecutionSession(command)) {
			String line;

			try (InputStream stdout = session.getStdout();
					InputStreamReader inputStreamReader = new InputStreamReader(stdout);
					BufferedReader reader = new BufferedReader(inputStreamReader);
					InputStream stderr = session.getStderr();
					InputStreamReader errorStreamReader = new InputStreamReader(stderr);
					BufferedReader errorReader = new BufferedReader(errorStreamReader);)
			{
				log.debug("Before reading: {} of command: {} ", new Date(),
					command);
				// Get the command's output:
				while ((line = reader.readLine()) != null) {
					result.add(line);
				}
				log.debug("After reading: {} of command: {} ", new Date(), command);

				// Get the errors if the command fails:
				log.debug("Before stderr: {} of command: {} ", new Date(), command);
				while ((line = errorReader.readLine()) != null) {
					errors.add(line);
				}
				log.debug("After stderr: {} of command: {} ", new Date(), command);
			}
			catch (Exception exc) {
				exc.printStackTrace();
			}

			int exitStatus = session.getExitStatus();

			if (exitStatus < 0) {
				log.debug("Done, but exit status not set!");
			}
			else if (exitStatus > 0) {
				log.debug("Done, but with error! {}", errors);
				throw new SshExecuteCommandException(exitStatus, result, errors);
			}
			else {
				log.debug("Done! " + new Date());
			}
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		return result;
	}

	public boolean setPortForwarding(int lport, String rhost, int rport) {
		try {
			getConnectedSession().setPortForwardingL(lport, rhost, rport);
		}
		catch (JSchException exc) {
			log.error("forward", exc);
			return false;
		}
		return true;
	}

	private class P_SshExecutionSession implements SshExecutionSession {

		private ChannelExec channel;

		public P_SshExecutionSession(ChannelExec channel) {
			this.channel = channel;
		}

		@Override
		public InputStream getStdout() throws IOException {
			return channel.getInputStream();
		}

		@Override
		public InputStream getStderr() throws IOException {
			return channel.getErrStream();
		}

		@Override
		public int getExitStatus() {
			return channel.getExitStatus();
		}

		@Override
		public void close() {
			channel.disconnect();
		}

	}
}
