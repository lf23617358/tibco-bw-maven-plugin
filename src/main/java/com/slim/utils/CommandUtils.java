package com.slim.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import org.apache.maven.plugin.logging.Log;

/**
 * 
 * @author Slimaine BENSADOUN
 * 
 */
public class CommandUtils {

	public static final String GUILLEMETS = "\"";

	// Executable
	private static final String APP_MANAGE_BIN = "AppManage";
	private static final String BUILD_EAR_BIN = "buildear";
	private static final String VALIDATE_PROJECT_BIN = "validateproject";

	// Option list
	public static final String EAR_OPTION = "-ear";
	public static final String EXPORT_OPTION = "-export";
	public static final String OUT_OPTION = "-out";
	public static final String O_OPTION = "-o";
	public static final String DEPLOY_OPTION = "-deploy";
	public static final String DEPLOY_CONFIG_OPTION = "-deployconfig";
	public static final String APP_OPTION = "-app";
	public static final String DOMAIN_OPTION = "-domain";
	public static final String USER_OPTION = "-user";
	public static final String PW_OPTION = "-pw";
	public static final String PROJECT_OPTION = "-p";
	public static final String U_OPTION = "-u";
	public static final String STRING_VIDE = "";
	public static final String SUFFIXE_EXE = ".exe";
	public static final String SUFFIXE_EAR = ".ear";
	public static final String SUFFIXE_XML = ".xml";
	public static final String DFT = "DFT";

	private static String OS = System.getProperty("os.name").toLowerCase();

	/**
	 * 
	 * @param logger
	 * @param execDirectory
	 * @param executableBin
	 * @param options
	 * @return
	 * @throws IOException
	 */
	public static BufferedReader executeCommand(
		Log logger,
		String execDirectory,
		String executableBin,
		Map<String, String> options) throws IOException {

		// Building executable command
		String[] cmdArray = new String[1 + options.size() * 2];
		cmdArray[0] = execDirectory + "\\" + executableBin;

		// Building options
		int i = 1;
		for (Map.Entry<String, String> element : options.entrySet()) {
			cmdArray[i++] = element.getKey();
			cmdArray[i++] = element.getValue();
		}

		// Logging command
		StringBuffer commandLine = new StringBuffer();
		for (String element : cmdArray) {
			commandLine.append(element + " ");
		}
		logger.info(commandLine);

		// Launching command with current options
		Process procBuild = new ProcessBuilder(cmdArray)
			.directory(new File(execDirectory))
			.redirectErrorStream(true)
			.start();

		// Building command line result
		return new BufferedReader(new InputStreamReader(procBuild.getInputStream()));

	}

	/**
	 * 
	 * @param directoryName
	 * @param logger
	 */
	public static void createDirectoryIfNeeded(String directoryName, Log logger) {
		File theDir = new File(directoryName);

		// if the directory does not exist, create it
		if (!theDir.exists()) {
			logger.info("Creating directory: " + directoryName);
			theDir.mkdir();
		}
	}

	public static boolean isWindows() {
		return (OS.indexOf("win") >= 0);
	}

	public static boolean isMac() {
		return (OS.indexOf("mac") >= 0);
	}

	public static boolean isUnix() {
		return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0);
	}

	public static boolean isSolaris() {
		return (OS.indexOf("sunos") >= 0);
	}

	public static String getAppManageBin() {
		String bin = APP_MANAGE_BIN;
		if (isWindows()) {
			bin = APP_MANAGE_BIN + SUFFIXE_EXE;
		}
		return bin;
	}

	public static String getBuildEarBin() {

		String bin = BUILD_EAR_BIN;
		if (isWindows()) {
			bin = BUILD_EAR_BIN + SUFFIXE_EXE;
		}
		return bin;
	}

	public static String getValidateProjectBin() {

		String bin = VALIDATE_PROJECT_BIN;
		if (isWindows()) {
			bin = VALIDATE_PROJECT_BIN + SUFFIXE_EXE;
		}
		return bin;
	}

	public static String getRemoteAppManageBin() {
		String bin = APP_MANAGE_BIN;
		return bin;
	}
}
