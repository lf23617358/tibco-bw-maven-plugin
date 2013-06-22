package com.slim.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.maven.plugin.logging.Log;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

public class SSHClientUtils {

	private static final String END_KSH = "end.ksh";

	/** Channel modes */
	public static final String EXEC_MODE = "exec";

	public static final String SHELL_MODE = "shell";

	public static final String SFTP_MODE = "sftp";

	/** Authentication */
	private static String USER;

	private static String PWD;

	private static String HOST;

	/** Utils */
	private final String CMD_INVITE;

	private PipedInputStream fromServer;

	private OutputStream toServer;

	private Channel channel;

	private Session session;

	private static final String TERMINATOR = "zDonez";

	private String lastCommand = "";

	private final Pattern alphaNumeric = Pattern.compile("([^a-zA-z0-9])");

	private int nbChannel = 0;

	private String previousMode;

	private final int SERVER_RESPONSE_TIMEOUT = 1500;

	/** Low-level layer to provide ssh functions */
	private Scp scp;

	/** Shell */
	private final JSch shell;

	private final Log LOGGER;

	public SSHClientUtils(String host, String user, String password, Log logger) throws Exception {
		LOGGER = logger;
		shell = new JSch();
		USER = user;
		PWD = password;
		HOST = host;
		CMD_INVITE = HOST + ":" + USER + "#";
		initSession();
	}

	/**
	 * Init SSH session
	 * 
	 * @throws Exception
	 * @throws JSchException
	 */
	private void initSession() throws Exception {
		try {
			session = shell.getSession(USER, HOST, 22);
			MyUserInfo ui = new MyUserInfo();
			ui.setPassword(PWD);
			session.setUserInfo(ui);

			Properties config = new Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);

			session.connect();
			scp = new Scp(session);
		} catch (JSchException e) {
			e.printStackTrace();
			throw new Exception(e.getLocalizedMessage());
		}
	}

	/**
	 * Connects to remote machine
	 * 
	 * @param mode
	 *            Type of channel to open : can be EXEC_MODE, SHELL_MODE,
	 *            SFTP_MODE
	 * @throws Exception
	 * @throws JSchException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private void connect(String mode) throws Exception {

		if (!isConnected()) {
			try {
				channel = session.openChannel(mode);
				previousMode = mode;
				// If Shell mode, customize I/O streams to get response
				if (mode.equals(SHELL_MODE)) {
					initShellMode();
					channel.connect();
				}
				// Pour laisser le temps de se connecter
				Thread.sleep(100);
			} catch (Exception e) {
				e.printStackTrace();
				throw new Exception(e.getLocalizedMessage());
			}
		} else {
			// To avoid 10 files upload limitation in session
			if (nbChannel == 9) {
				disconnect();
				nbChannel = 0;
				connect(mode);
			}
			// Change channel
			if (!previousMode.equals(mode)) {
				previousMode = mode;
				channel.disconnect();
				connect(mode);
			}
			nbChannel++;
		}
	}

	/**
	 * Init shell mode
	 * 
	 * @throws IOException
	 */
	private void initShellMode() throws IOException {
		PipedOutputStream po = new PipedOutputStream();
		fromServer = new PipedInputStream(po);
		channel.setOutputStream(po);

		toServer = new PipedOutputStream();
		PipedInputStream pi = new PipedInputStream((PipedOutputStream) toServer);
		channel.setInputStream(pi);
	}

	/**
	 * Check if client is connected
	 * 
	 * @return YES or NO
	 */
	public boolean isConnected() {
		return (channel != null && channel.isConnected());
	}

	/**
	 * Disconnect channel and session
	 */
	public void disconnect() {
		if (isConnected()) {
			channel.disconnect();
			session.disconnect();
		}
	}

	/**
	 * Disconnect client and open new session
	 * 
	 * @throws Exception
	 */
	public void reconnect() throws Exception {
		disconnect();
		initSession();
	}

	/**
	 * Send simple SHELL command
	 * 
	 * @param command
	 * @return server response
	 * @throws Exception
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public String sendShell(String command, String workingDir) throws Exception {

		String endScript = workingDir + "/" + END_KSH;

		sendShell("echo \"echo " + TERMINATOR + "\" > " + endScript);
		sendShell("chmod 0777 " + endScript);

		connect(SHELL_MODE);
		lastCommand = command;

		command += "; " + endScript + " \n";
		command = endScript + "; " + command;
		try {
			toServer.write(command.getBytes());
			// To ensure server answer reception
			// and manage a communication time out.

			Thread.sleep(SERVER_RESPONSE_TIMEOUT);

		} catch (IOException ex) {
			ex.printStackTrace();
			throw new Exception(ex.getLocalizedMessage());
		} catch (InterruptedException iex) {
			iex.printStackTrace();
			throw new Exception(iex.getLocalizedMessage());
		}

		String rep = getServerResponse();
		sendShell("rm " + endScript);
		return rep;
	}

	/**
	 * 
	 * @param command
	 * @return
	 * @throws Exception
	 */
	public String sendShell(String command) throws Exception {
		connect(SHELL_MODE);
		lastCommand = command;
		command += "; echo \"" + TERMINATOR + "\" \n";

		try {
			toServer.write(command.getBytes());
			// To ensure server answer reception
			// and manage a communication time out.

			Thread.sleep(SERVER_RESPONSE_TIMEOUT);

		} catch (IOException ex) {
			ex.printStackTrace();
			throw new Exception(ex.getLocalizedMessage());
		} catch (InterruptedException iex) {
			iex.printStackTrace();
			throw new Exception(iex.getLocalizedMessage());
		}
		return getServerResponse();
	}

	/**
	 * Retrieve server response to SHELL command
	 * 
	 * @return the response
	 * @throws Exception
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private String getServerResponse() throws Exception {
		StringBuffer builder = new StringBuffer();

		String result = null;
		try {
			int count = 0;
			String line = "";

			BufferedReader reader = new BufferedReader(new InputStreamReader(fromServer));
			if (reader.ready()) {
				for (int i = 0; true; i++) {
					try {
						line = reader.readLine();
					} catch (IOException e) {
						LOGGER.warn("Communication seems to be closed...");
						break;
					}
					builder.append(line).append("\n");
					if (line.indexOf(TERMINATOR) == -1 && count > 0) {
						LOGGER.info(line);
					}

					if (line.indexOf(TERMINATOR) != -1 && ++count > 1) {
						break;
					}
				}
				result = builder.toString();

				result = result.substring(result.indexOf(TERMINATOR), result.lastIndexOf(TERMINATOR));
				result = result.replaceAll(escape(TERMINATOR), "").trim();
				LOGGER.debug("Command : " + lastCommand + " -> Result : " + result);

				return result;
			} else {
				throw new Exception("Server did not answer in the time (" + SERVER_RESPONSE_TIMEOUT + "ms) to command (" + lastCommand + ")");
			}
		} catch (IOException e) {
			e.printStackTrace();
			LOGGER.error(e);
			throw new Exception(e.getLocalizedMessage());
		}
	}

	private String escape(String subjectString) {
		return alphaNumeric.matcher(subjectString).replaceAll("\\\\$1");
	}

	/**
	 * Execute remote script
	 * 
	 * @param scriptPath
	 * @throws Exception
	 */
	public String executeScript(String scriptPath, String[] params) throws Exception {
		String commandLine = scriptPath;
		for (String p : params) {
			commandLine += " " + p;
		}
		return sendShell(commandLine);
	}

	/**
	 * Send a file on server (implementation of <CODE>exec 'scp -t rfile'</CODE>
	 * command)
	 * 
	 * @param localFilePath
	 * @param remoteFilePath
	 * @throws Exception
	 */
	public void sendFile(String localFilePath, String remoteFilePath) throws Exception {
		String remoteTargetDir = remoteFilePath.substring(0, remoteFilePath.lastIndexOf("/") + 1);
		String remoteTargetName = remoteFilePath.substring(remoteTargetDir.length());
		if (nbChannel == 9) {
			nbChannel = 0;
			reconnect();
		}
		nbChannel++;
		scp.put(localFilePath, remoteTargetDir, remoteTargetName, "0777");
	}

	/**
	 * Get files
	 * 
	 * @param remotePath
	 * @param localDestinationPath
	 * @throws Exception
	 * @throws IOException
	 */
	public List<String> getResultsFiles(String remotePath, String localDestinationPath) throws Exception {
		List<String> filesPaths = new ArrayList<String>();
		// To ensure well connection
		reconnect();
		// List files
		String list = sendShell("ls " + remotePath);
		if (list != null) {
			if (list.indexOf(CMD_INVITE) != -1) {
				list = list.substring(list.indexOf(CMD_INVITE) + CMD_INVITE.length());
			}
			if (list.indexOf("not found") != -1) {
				throw new Exception("No output files!");
			}
			List<String> files = getFilesList(list);
			if (files.size() > 0) {
				File tmp = null;
				for (String f : files) {
					tmp = new File(localDestinationPath, f);
					if (tmp.exists()) {
						tmp.delete();
					}
					if (nbChannel == 9) {
						nbChannel = 0;
						reconnect();
					}
					nbChannel++;
					String absPath = tmp.getAbsolutePath();
					filesPaths.add(absPath);
					scp.get(remotePath + f, absPath);
				}
				return filesPaths;
			} else {
				throw new Exception("No output files!");
			}
		} else {
			throw new Exception("Impossible to retreive files list!");
		}
	}

	/**
	 * Compute file list received from server ("ls" command) to a list of files
	 * 
	 * @param lsResult
	 * @return
	 */
	private List<String> getFilesList(String lsResult) {
		List<String> files = new ArrayList<String>();
		String[] tabl = lsResult.split("\\n");
		for (String s : tabl) {
			s = s.trim();
			if (!"".equals(s)) {
				if (s.indexOf(" ") == -1) {
					files.add(s);
				} else {
					String[] tabl2 = s.split(" ");
					for (String s2 : tabl2) {
						s2 = s2.trim();
						if (!"".equals(s2)) {
							files.add(s2);
						}
					}
				}
			}
		}
		return files;
	}

	/**
	 * Move command shell to specified directory
	 * 
	 * @param path
	 * @throws Exception
	 */
	public void moveToDir(String path) throws Exception {
		sendShell("cd " + path);
	}

	/**
	 * Local class that implements logging credentials
	 */
	private static class MyUserInfo implements UserInfo {

		private String password;

		public void setPassword(String password) {
			this.password = password;
		}

		@Override
		public String getPassphrase() {
			return null;
		}

		@Override
		public String getPassword() {
			return password;
		}

		@Override
		public boolean promptPassword(String arg0) {
			return true;
		}

		@Override
		public boolean promptPassphrase(String arg0) {
			return true;
		}

		@Override
		public boolean promptYesNo(String arg0) {
			return true;
		}

		@Override
		public void showMessage(String arg0) {
			System.out.println(arg0);
		}
	}
}