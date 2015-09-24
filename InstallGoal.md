# Command #

mvn tibco-bw:install

# Description #

The maven install goal is a simply aggregation of the following goal:
-	validate
-	buildear
-	generate
-	replace

If any of these steps fail, the installation fail.

# Parameters #

## Mandatory ##

**designerHome :** Bin directory of tibco designer installation in local machine.
<br><i>Ex: C:\tibco\designer\5.6\bin\</i>

<b>filterFile :</b> The location of properties file where you can find key/value couple to replace.<br>
<br>
<b>machineTibco :</b> The name of tibco server machine where the application will be deployed.<br>
This is the name configured in Resource Management/Machines in Tibco Administrator.<br>
<br>
<br>
<b>traHome :</b> Bin directory of tibco tra installation in local machine<br>
<br><i>Ex: C:\tibco\tra\5.6\bin\</i>


<b>archiveURI :</b> The archive resource file location defining the list of configured component to package.<br>
<br><i>Ex: /TIB_APP/DeploymentConfiguration/TIB_APP.archive</i>


<h2>Optional</h2>

<b>validationIgnoresFile :</b> The location and name of the a ignore file used to define pattern to exclude when validating project. By default, the plugin look for a validation.ignore filename in root directory.<br>
<br><i>Ex : resources/validation.ignore</i>

<b>checkUnusedGlobalVariables (true/false) :</b>  A boolean indicating whether or not you want to check unused variables when validating project. When set to true, validation fail if unused global variables are finded. By default, false value is used.<br>
<br><i>Ex: false</i>


<b>outputDirectory :</b> Output directory where ear file is generated. By default, the target directory is used.<br>
<br><i>Ex: output/validate</i>