
package cz.it4i.fiji.scpclient;

import com.jcraft.jsch.JSchException;

import java.util.List;

public class SshExecuteCommandException extends JSchException {

	private static final long serialVersionUID = 8491385678306282231L;

	private final int exitStatus;
	private final List<String> stdout;
	private final List<String> stderr;

	public SshExecuteCommandException(int exitStatus, List<String> stdout,
		List<String> stderr)
	{
		super("exitStatus: " + exitStatus + ", error output: " + String.join("\n",
			stderr));
		this.exitStatus = exitStatus;
		this.stdout = stdout;
		this.stderr = stderr;
	}

	public int getExitStatus() {
		return exitStatus;
	}

	public List<String> getStdout() {
		return stdout;
	}

	public List<String> getStderr() {
		return stderr;
	}

}
