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
public class BuildService {

	private static final BuildService instance = new BuildService();

	private BuildService() {
		// ... init code
	}

	public static BuildService getInstance() {
		return instance;
	}

	public void buildear(MavenProject project, String outputDirectory, String archiveURI, String traHome, Log logger)
		throws MojoExecutionException {
		logger.info(" - BASE_DIR : " + project.getBasedir().toString());
		logger.info(" - TRA_HOME : " + traHome);
		logger.info(" - OUTPUT_DIRECTORY : " + outputDirectory);

		try {
			CommandUtils.createDirectoryIfNeeded(project.getBasedir().toString() + "\\" + outputDirectory, logger);

			if (archiveURI == null || archiveURI.length() == 0) {
				archiveURI = "/" + project.getArtifact().getArtifactId();
			}

			Map<String, String> options = new HashMap<String, String>();
			options.put("-v", "");
			options.put("-xs", "");
			options.put(CommandUtils.EAR_OPTION, CommandUtils.GUILLEMETS + archiveURI + CommandUtils.GUILLEMETS);
			options.put(CommandUtils.PROJECT_OPTION, CommandUtils.GUILLEMETS
					+ project.getBasedir().toString()
					+ "\\"
					+ project.getArtifact().getArtifactId()
					+ CommandUtils.GUILLEMETS);
			options.put(CommandUtils.O_OPTION, CommandUtils.GUILLEMETS
					+ project.getBasedir().toString()
					+ "\\"
					+ outputDirectory
					+ "\\"
					+ project.getArtifact().getArtifactId()
					+ ".ear"
					+ CommandUtils.GUILLEMETS);

			BufferedReader buildOutput = CommandUtils.executeCommand(
				logger,
				traHome,
				CommandUtils.getBuildEarBin(),
				options);

			boolean isError = false;
			StringBuffer result = new StringBuffer();
			String line = null;
			while ((line = buildOutput.readLine()) != null) {
				if (line.contains("Error")) {
					isError = true;
				}
				result.append(line + "\n");
			}

			if (isError) {
				logger.error(result.toString());
				throw new Exception("Error in building ear ...");
			} else {
				logger.info(result.toString());
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
