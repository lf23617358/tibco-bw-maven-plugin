# Command #

mvn tibco-bw:buildear

# Description #

The buildear goal enables you to build a deployable archive (ear) of a tibco project. The plugin use a  tibco archive description file to build the ear.

# Parameters #

## Mandatory ##

**traHome :** Bin directory of tibco tra installation in local machine
<br><i>Ex: C:\tibco\tra\5.6\bin\</i>


<b>archiveURI :</b> The archive resource file location defining the list of configured component to package.<br>
<br><i>Ex: /TIB_APP/DeploymentConfiguration/TIB_APP.archive</i>


<h2>Optional</h2>

<b>outputDirectory :</b> Output directory where ear file is generated. By default, the target directory is used.<br>
<br><i>Ex: output/validate</i>