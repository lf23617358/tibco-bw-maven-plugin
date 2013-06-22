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
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import com.slim.utils.CommandUtils;

/***
 * 
 * @author Slimaine BENSADOUN
 * 
 */
public class GenerateService {

	private static final GenerateService instance = new GenerateService();

	private GenerateService() {
		// ... init code
	}

	public static GenerateService getInstance() {
		return instance;
	}

	public
		void
		generateXml(MavenProject project, String outputDirectory, String archiveURI, String traHome, Log logger)
			throws MojoExecutionException {
		CommandUtils.createDirectoryIfNeeded(project.getBasedir().toString() + "\\" + outputDirectory, logger);

		String outConfigurationFile = CommandUtils.GUILLEMETS
				+ project.getBasedir().toString()
				+ "\\"
				+ outputDirectory
				+ "\\"
				+ project.getArtifact().getArtifactId()
				+ ".xml"
				+ CommandUtils.GUILLEMETS;

		String earFile = CommandUtils.GUILLEMETS
				+ project.getBasedir().toString()
				+ "\\"
				+ outputDirectory
				+ "\\"
				+ project.getArtifact().getArtifactId()
				+ ".ear"
				+ CommandUtils.GUILLEMETS;

		logger.info(" - BASE_DIR : " + project.getBasedir().toString());
		logger.info(" - TRA_HOME : " + traHome);
		logger.info(" - XML_CONFIG_LOCATION : " + outConfigurationFile);
		logger.info(" - EAR_LOCATION : " + earFile);

		try {
			if (archiveURI == null || archiveURI.length() == 0) {
				archiveURI = "/" + project.getArtifact().getArtifactId();
			}

			Map<String, String> options = new HashMap<String, String>();
			options.put(CommandUtils.EXPORT_OPTION, "");
			options.put(CommandUtils.EAR_OPTION, earFile);
			options.put(CommandUtils.OUT_OPTION, outConfigurationFile);

			BufferedReader buildOutput = CommandUtils.executeCommand(
				logger,
				traHome,
				CommandUtils.getAppManageBin(),
				options);

			String line = null;
			while ((line = buildOutput.readLine()) != null) {
				// boolean ignored = false;
				logger.info(line);
			}

		} catch (IOException e) {
			e.printStackTrace();
			throw new MojoExecutionException("Error", e);
		} catch (Throwable e) {
			e.printStackTrace();
			throw new MojoExecutionException("Error", e);
		}
	}
}
