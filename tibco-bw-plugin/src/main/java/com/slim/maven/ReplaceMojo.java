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

import com.slim.service.ReplaceService;

/**
 * Goal which prepare xml for deployment. It replace global variables of generic
 * xml
 * 
 * 
 * @author Slimaine BENSADOUN
 * @goal replace
 * 
 */
public class ReplaceMojo extends AbstractMojo {

	/**
	 * The maven project
	 * 
	 * @parameter property="project"
	 * @required
	 * @readonly
	 */
	private MavenProject project;

	/**
	 * Directory where you can find Designer executable
	 * 
	 * @parameter property="traHome"
	 * @required
	 */
	private String traHome;

	/**
	 * Directory where you can find Designer executable
	 * 
	 * @parameter property="filterFile"
	 * @required
	 */
	private String filterFile;

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
	 * @parameter property="machineTibco" default-value=""
	 * @required
	 */
	private String machineTibco;

	/**
	 * 
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {

		ReplaceService.getInstance().replaceXml(project, outputDirectory, traHome, filterFile, machineTibco, getLog());

	}

}
