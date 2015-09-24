# Command #

mvn tibco-bw:validate

# Description #

The validate goal help you to validate a project without opening the tibco designer application. It enables you to define a file which contains a list of pattern (using regular expression) to ignore.

# Parameters #


## Mandatory ##

**designerHome** : Bin directory of tibco designer installation in local machine.
<br><i>Ex: C:\tibco\designer\5.6\bin\</i>


<h2>Optional</h2>

<b>validationIgnoresFile :</b>  The location and name of the a ignore file used to define pattern to exclude when validating project. By default, the plugin look for a validation.ignore filename in root directory.<br>
<br><i>Ex : resources/validation.ignore</i>

<b>checkUnusedGlobalVariables (true/false) :</b>  A boolean indicating whether or not you want to check unused variables when validating project. When set to true, validation fail if unused global variables are finded. By default, false value is used.<br>
<br><i>Ex: false</i>

<b>outputDirectory :</b> Output directory where validation log is written. By default, the target directory is used.<br>
<br><i>Ex: output/validate</i>