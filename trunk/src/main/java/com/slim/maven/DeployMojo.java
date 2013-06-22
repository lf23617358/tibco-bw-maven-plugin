package com.slim.maven;

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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import com.slim.service.DeployService;

/**
 * Goal which calls the tibco Appmanage tool to deploy an application
 * 
 * @author Slimaine BENSADOUN
 * @goal deploy
 * 
 */
public class DeployMojo extends AbstractMojo {
	/**
	 * The maven project.
	 * 
	 * @parameter property="project"
	 * @required
	 * @readonly
	 */
	private MavenProject project;

	/**
	 * Directory where you can find Designer executable
	 * 
	 * @parameter property="hostServer"
	 * @required
	 */
	private String hostServer;

	/**
	 * Directory where you can find Designer executable
	 * 
	 * @parameter property="hostUser"
	 * @required
	 */
	private String hostUser;

	/**
	 * Directory where you can find Designer executable
	 * 
	 * @parameter property="hostPassword"
	 * @required
	 */
	private String hostPassword;

	/**
	 * Directory where you can find Designer executable
	 * 
	 * @parameter property="tibcoDomain"
	 * @required
	 */
	private String tibcoDomain;

	/**
	 * Directory where you can find Designer executable
	 * 
	 * @parameter property="tibcoUser"
	 * @required
	 */
	private String tibcoUser;

	/**
	 * Directory where you can find Designer executable
	 * 
	 * @parameter property="tibcoPassword"
	 * @required
	 */
	private String tibcoPassword;

	/**
	 * Directory where you can find Designer executable
	 * 
	 * @parameter property="outputDirectory" default-value="target"
	 * @required
	 */
	private String outputDirectory;

	/**
	 * Directory where you can find Designer executable
	 * 
	 * @parameter property="remoteDirectory" default-value="/tmp/"
	 * @required
	 */
	private String remoteDirectory;

	/**
	 * Directory where you can find Designer executable
	 * 
	 * @parameter property="appLocation"
	 * @required
	 */
	private String appLocation;

	public void execute() throws MojoExecutionException, MojoFailureException {

		DeployService.getInstance().deploy(project, hostServer, hostUser, hostPassword, tibcoDomain, tibcoUser, tibcoPassword, outputDirectory, remoteDirectory,
				appLocation, getLog());

	}

}
