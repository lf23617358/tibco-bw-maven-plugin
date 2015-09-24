# Command #

mvn tibco-bw:deploy

# Description #

The maven deploy goal help you to deploy a local tibco project to a remote tibco domain. It uses a SSH connection to deploy the application.

# Parameters #

## Mandatory ##

**appLocation :** The application repository structure in tibco administrator console.

**machineTibco :** The name of tibco server machine where the application will be deployed.
This is the name configured in Resource Management/Machines in Tibco Administrator console.

**hostServer :** The alias or ip of physical remote server where to deploy the tibco poject.

**hostUser :** The user of physical remote server where to deploy the tibco poject.

**hostPassword :** The password of physical remote server where to deploy the tibco poject.

**tibcoDomain :** The tibco administator domain.

**tibcoUser :** The tibco administrator user.

**tibcoPassword :** The tibco administrator password.

**remoteDirectory :** A temporary directory in remote server used to copy local ear and deployment configuration file before tibco deployment.