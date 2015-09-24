# Command #

mvn tibco-bw:replace

# Description #

The maven replace goal replaces global variables of deployment configuration file with key/value couple defined in a properties file. It enables to adapt xml deployment configuration file to a specific deployment environment.

# Parameters #

## Mandatory ##

**filterFile:**The location of properties file where you can find key/value couple to replace.
<br><i>Ex: resources/filter-DEV.properties, resources/filter-INT.properties, resources/filter-UAT.properties</i>


<b>machineTibco :</b> The name of tibco server machine where the application will be deployed.<br>
This is the name configured in Resource Management/Machines in Tibco Administrator. console.<br>
<br>
<h2>Optional</h2>

<b>outputDirectory :</b> Output directory where ear file is generated. By default, the target directory is used.<br>
<br><i>Ex: output/validate</i>