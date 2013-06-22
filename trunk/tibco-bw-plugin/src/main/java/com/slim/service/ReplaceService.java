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
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.maven.model.Profile;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.slim.utils.CommandUtils;

/***
 * 
 * @author Slimaine BENSADOUN
 * 
 */
public class ReplaceService {

	private static final ReplaceService instance = new ReplaceService();

	private ReplaceService() {
		// ... init code
	}

	public static ReplaceService getInstance() {
		return instance;
	}

	/**
	 * 
	 * @param project
	 * @param outputDirectory
	 * @param traHome
	 * @param filterFile
	 * @param machineTibco
	 * @throws TransformerFactoryConfigurationError
	 * @throws MojoFailureException
	 */
	public void replaceXml(
		MavenProject project,
		String outputDirectory,
		String traHome,
		String filterFile,
		String machineTibco,
		Log logger) throws TransformerFactoryConfigurationError, MojoFailureException {
		CommandUtils.createDirectoryIfNeeded(project.getBasedir().toString() + "\\" + outputDirectory, logger);

		String configFile = project.getBasedir().toString()
				+ "\\"
				+ outputDirectory
				+ "\\"
				+ project.getArtifact().getArtifactId()
				+ CommandUtils.SUFFIXE_XML;
		String propertiesFile = project.getBasedir().toString() + "\\" + filterFile;

		logger.info(" - BASE_DIR : " + project.getBasedir().toString());
		logger.info(" - TRA_HOME : " + traHome);
		logger.info(" - OUTPUT_DIRECTORY : " + outputDirectory);
		logger.info(" - CONFIG_FILE : " + configFile);
		logger.info(" - FILTER_FILE : " + propertiesFile);

		Map<String, String> mapAttribute = new HashMap<String, String>();
		mapAttribute = parseFileProperties(propertiesFile, logger);

		Document doc = remplace(configFile, mapAttribute, machineTibco, logger);
		try {
			createFile(doc, outputDirectory, project, logger);
		} catch (TransformerException tfe) {
			logger.error(tfe.getMessage());
			tfe.printStackTrace();
		}
	}

	/**
	 * Parse a filter file and set a Map
	 * 
	 * @param filepath
	 * @return
	 */
	private Map<String, String> parseFileProperties(String filepath, Log logger) {

		Map<String, String> mapAttribute = new HashMap<String, String>();
		BufferedReader buffer = null;

		try {
			String currentLine;
			String cle = null;
			String valeur = null;
			String[] tab = null;

			buffer = new BufferedReader(new FileReader(filepath));

			while ((currentLine = buffer.readLine()) != null) {
				logger.info(currentLine);
				tab = currentLine.split("=");
				if (tab.length == 2 && !currentLine.startsWith("#")) {
					cle = tab[0];
					valeur = tab[1];
					mapAttribute.put(cle, valeur);
				}
			}
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				if (buffer != null) {
					buffer.close();
				}
			} catch (IOException ex) {
				logger.error(ex.getMessage());
				ex.printStackTrace();
			}
		}
		return mapAttribute;
	}

	/**
	 * Replace variables in config file
	 * 
	 * @param filepath
	 * @param mapAttribute
	 * @throws TransformerFactoryConfigurationError
	 * @throws MojoFailureException
	 */
	private Document remplace(String filepath, Map<String, String> mapAttribute, String machineTibco, Log logger)
		throws TransformerFactoryConfigurationError,
		MojoFailureException {
		Document doc = null;
		try {

			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			doc = docBuilder.parse(filepath);

			// Get the root element
			// Node application = doc.getFirstChild();

			Node globalVariables = doc.getElementsByTagName("NVPairs").item(0);

			// update staff attribute
			NamedNodeMap gbattr = globalVariables.getAttributes();
			Node nodeAttr = gbattr.getNamedItem("name");
			if (!nodeAttr.getTextContent().equals("Global Variables")) {
				throw new MojoFailureException("Unable to find global variables node in " + filepath);
			}

			// Loop the staff child node
			NodeList listeGlobalVariable = globalVariables.getChildNodes();

			for (int i = 0; i < listeGlobalVariable.getLength(); i++) {

				Node nameValuePair = listeGlobalVariable.item(i);

				if (nameValuePair.getNodeType() == Node.ELEMENT_NODE) {

					Element element = (Element) nameValuePair;

					String name = element.getElementsByTagName("name").item(0).getTextContent();
					String value = element.getElementsByTagName("value").item(0).getTextContent();
					String newValue = null;
					if (mapAttribute.containsKey(name)) {
						newValue = mapAttribute.get(name);
						element.getElementsByTagName("value").item(0).setTextContent(newValue);
						logger.info("Replacing '" + value + "' with '" + newValue + "' in variable '" + name + "' \n");
					}
				}
			}

			// Modification de la section machine
			Node machine = doc.getElementsByTagName("machine").item(0);

			if (machine.getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element) machine;
				element.setTextContent(machineTibco);
			}

			// Modification de la section repo_instance
			Node repoInstances = doc.getElementsByTagName("repoInstances").item(0);

			NamedNodeMap rpattr = repoInstances.getAttributes();
			Node selectedAttr = rpattr.getNamedItem("selected");
			selectedAttr.setTextContent("local");

			NodeList list = repoInstances.getChildNodes();

			for (int i = 0; i < list.getLength(); i++) {
				Node node = list.item(i);
				if ("httpRepoInstance".equals(node.getNodeName()) || "rvRepoInstance".equals(node.getNodeName())) {
					repoInstances.removeChild(node);
				}
			}

		} catch (ParserConfigurationException pce) {
			logger.error(pce.getMessage());
			pce.printStackTrace();
		} catch (IOException ioe) {
			logger.error(ioe.getMessage());
			ioe.printStackTrace();
		} catch (SAXException sae) {
			logger.error(sae.getMessage());
			sae.printStackTrace();
		}
		return doc;
	}

	/**
	 * 
	 * @param doc
	 * @throws TransformerFactoryConfigurationError
	 * @throws TransformerConfigurationException
	 * @throws TransformerException
	 */
	private void createFile(Document doc, String outputDirectory, MavenProject project, Log logger)
		throws TransformerFactoryConfigurationError,
		TransformerConfigurationException,
		TransformerException {

		// Extrait le profil maven actuel
		String environnement = null;
		if (project.getActiveProfiles().size() > 0) {
			environnement = ((Profile) project.getActiveProfiles().get(0)).getId();
		} else {
			environnement = CommandUtils.DFT;
		}

		// Write the content into xml file
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		CommandUtils.createDirectoryIfNeeded(project.getBasedir().toString()
				+ "\\"
				+ outputDirectory
				+ "\\"
				+ environnement, logger);

		StreamResult result = new StreamResult(new File(project.getBasedir().toString()
				+ "\\"
				+ outputDirectory
				+ "\\"
				+ environnement
				+ "\\"
				+ project.getArtifact().getArtifactId()
				+ CommandUtils.SUFFIXE_XML));
		transformer.transform(source, result);
	}

}
