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

import org.apache.maven.model.Profile;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import com.slim.utils.CommandUtils;
import com.slim.utils.SSHClientUtils;

/***
 * 
 * @author Slimaine BENSADOUN
 * 
 */
public class DeployService {

	private static final DeployService instance = new DeployService();

	private DeployService() {
		// ... init code
	}

	public static DeployService getInstance() {
		return instance;
	}

	/**
	 * 
	 * @param project
	 * @param hostServer
	 * @param hostUser
	 * @param hostPassword
	 * @param tibcoDomain
	 * @param tibcoUser
	 * @param tibcoPassword
	 * @param outputDirectory
	 * @param remoteDirectory
	 * @throws MojoExecutionException
	 */
	public void deploy(
		MavenProject project,
		String hostServer,
		String hostUser,
		String hostPassword,
		String tibcoDomain,
		String tibcoUser,
		String tibcoPassword,
		String outputDirectory,
		String remoteDirectory,
		String appLocation,
		Log logger) throws MojoExecutionException {
		// TODO Check parameter
		try {

			String appTibco = appLocation == null || appLocation.trim().length() == 0 ? project
				.getArtifact()
				.getArtifactId() : appLocation;

			String remotePath = remoteDirectory + "/" + appTibco;

			// Extrait le profil maven actuel
			String environnement = null;
			if (project.getActiveProfiles().size() > 0) {
				environnement = ((Profile) project.getActiveProfiles().get(0)).getId();
			} else {
				environnement = CommandUtils.DFT;
				;
			}

			String localEarFile = project.getBasedir().toString()
					+ "\\"
					+ outputDirectory
					+ "\\"
					+ project.getArtifact().getArtifactId()
					+ CommandUtils.SUFFIXE_EAR;

			String localXmlFile = project.getBasedir().toString()
					+ "\\"
					+ outputDirectory
					+ "\\"
					+ environnement
					+ "\\"
					+ project.getArtifact().getArtifactId()
					+ CommandUtils.SUFFIXE_XML;

			String remoteEarFile = remotePath + "/" + project.getArtifact().getArtifactId() + CommandUtils.SUFFIXE_EAR;

			String remoteXmlFile = remotePath + "/" + project.getArtifact().getArtifactId() + CommandUtils.SUFFIXE_XML;

			SSHClientUtils sshclient = new SSHClientUtils(hostServer, hostUser, hostPassword, logger);

			logger.info("Creating directory " + remotePath + " ...");
			sshclient.sendShell("mkdir -p " + remotePath, remoteDirectory);

			logger.info("Moving " + localEarFile + " to " + remoteEarFile + " ...");
			sshclient.sendFile(localEarFile, remoteEarFile);

			logger.info("Moving " + localXmlFile + " to " + remoteXmlFile + " ...");
			sshclient.sendFile(localXmlFile, remoteXmlFile);

			StringBuilder commandDeploy = new StringBuilder();
			commandDeploy.append(CommandUtils.getRemoteAppManageBin()).append(" -deploy");
			commandDeploy.append(" -ear ").append(remoteEarFile);
			commandDeploy.append(" -deployconfig ").append(remoteXmlFile);
			commandDeploy.append(" -app ").append(appTibco);
			commandDeploy.append(" -domain ").append(tibcoDomain);
			commandDeploy.append(" -user ").append(tibcoUser);
			commandDeploy.append(" -pw ").append(tibcoPassword);

			logger.info("Executing " + commandDeploy.toString() + " ...");

			sshclient.sendShell(commandDeploy.toString(), remoteDirectory);

		} catch (Exception e) {
			e.printStackTrace();
			throw new MojoExecutionException("Error in deploying ear ..." + e.getMessage());
		}
	}

}
