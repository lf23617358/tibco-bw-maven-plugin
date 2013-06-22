package com.slim.service;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import com.slim.utils.CommandUtils;

/***
 * 
 * @author Slimaine BENSADOUN
 * 
 */
public class ValidateService {

	private static final ValidateService instance = new ValidateService();

	private ValidateService() {
		// ... init code
	}

	public static ValidateService getInstance() {
		return instance;
	}

	/**
	 * 
	 * @param project
	 * @param designerBinDir
	 * @param outputDirectory
	 * @param validationIgnoresFile
	 * @param logger
	 * @throws MojoExecutionException
	 * @throws MojoFailureException
	 */
	public void validate(
		MavenProject project,
		String designerHome,
		String outputDirectory,
		String validationIgnoresFile,
		boolean checkUnusedGlobalVariables,
		Log logger) throws MojoExecutionException, MojoFailureException {
		String validateOutputFile = project.getBasedir().toString() + "\\" + outputDirectory + "\\" + "outValidate.txt";

		logger.info(" - DESIGNER_HOME : " + designerHome);
		logger.info(" - BASE_DIR : " + project.getBasedir().toString());
		logger.info(" - VALIDATE_OUTPUT_LOG : " + validateOutputFile);
		logger.info(" - IGNORE_FILE : " + validationIgnoresFile);

		boolean validated = true;

		Pattern pattern = preparePattern(project, designerHome, outputDirectory, validationIgnoresFile, logger);
		Pattern patternResult = Pattern.compile(".*Found.*errors.*and.*warnings.*");

		try {
			CommandUtils.createDirectoryIfNeeded(project.getBasedir().toString() + "\\" + outputDirectory, logger);

			Map<String, String> options = new HashMap<String, String>();
			if (checkUnusedGlobalVariables) {
				options.put(CommandUtils.U_OPTION, project.getBasedir().toString()
						+ "\\"
						+ project.getArtifact().getArtifactId());
			} else {
				options.put(CommandUtils.STRING_VIDE, project.getBasedir().toString()
						+ "\\"
						+ project.getArtifact().getArtifactId());
			}

			BufferedReader validateOutput = CommandUtils.executeCommand(
				logger,
				designerHome,
				CommandUtils.getValidateProjectBin(),
				options);

			BufferedWriter outLogStream = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
				validateOutputFile)));
			outLogStream.append("#Log generated at " + new Date() + ".\n");

			String line = null;
			while ((line = validateOutput.readLine()) != null) {
				boolean ignored = false;
				String trimmedLine = line.trim();
				if (trimmedLine.length() == 0 || pattern.matcher(trimmedLine).matches()) {
					ignored = true;
				}
				if (!ignored) {
					validated = false;
					logger.warn(trimmedLine);
				}
				if (patternResult.matcher(line).matches()) {
					logger.info(line + "\n");
				}
				outLogStream.append(line + "\n");
			}

			outLogStream.close();

			logger.info("*******************************************************************");
			logger.info("Complete validation log is written to " + validateOutputFile);
			logger.info("*******************************************************************");
			logger.info("");
		} catch (IOException e) {
			e.printStackTrace();
			throw new MojoExecutionException("Error", e);
		} catch (Throwable e) {
			e.printStackTrace();
			throw new MojoExecutionException("Error", e);
		}

		if (!validated) {
			throw new MojoFailureException("Project Validation failed ! Please check and verify Project or configure "
					+ validationIgnoresFile);
		}
	}

	/**
	 * Prepare pattern of expressions to ignore
	 * 
	 * @return
	 */
	private Pattern preparePattern(
		MavenProject project,
		String designerBinDir,
		String outputDirectory,
		String validationIgnoresFile,
		Log logger) {
		String lineIgnore;
		Pattern p = Pattern.compile("");
		try {
			BufferedReader ignoredStream = new BufferedReader(new InputStreamReader(new FileInputStream(
				validationIgnoresFile)));
			lineIgnore = null;
			StringBuilder sb = new StringBuilder();
			while ((lineIgnore = ignoredStream.readLine()) != null) {
				if (!lineIgnore.startsWith("#")) {
					sb.append("(?:" + lineIgnore + ")|");
				}
			}
			sb.deleteCharAt(sb.length() - 1);// remove the last "|" (OR)
			logger.debug("Pattern to match is : " + sb.toString());
			p = Pattern.compile(sb.toString());
		} catch (Exception ex) {
			logger.warn("********************************************************************************");
			logger.warn(validationIgnoresFile
					+ " file is empty or could not be loaded. Run in debug mode to have Exception info");
			logger.warn("********************************************************************************");
			logger.debug(ex);
		}
		return p;
	}

}
