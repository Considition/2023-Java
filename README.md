# Introduction

This is a start kit for those that will participate in Considition 2023 using Java.

Considition2023.java contains an example of:

- Fetching required data
- Submitting a solution to Considition 2023
- Scoring a solution locally and saving the "game"

# Getting Started

We recommended using Visual studio code 2022
* Windows  https://visualstudio.microsoft.com/vs/
* Mac or Linux https://code.visualstudio.com/Download

----Running code-----
 # Installation instructions

In this program we use Java 17 and Maven 3.9. If you already have installed those programs you can skeep point 1 and 2. 

For windows users we recomend to use chocolatey to faster install Java and Maven, if you would like to do so follow step 3. 

1. Install java https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html
* Update the system environment variables to make sure you are using the correct SDK update the path that points to the bin directory (exampel: JAVA_HOME C:\Program Files\Java\jdk-17.0.2) 
* Check that java is install run java --version and you should see the SDK you just have installed
2. Install maven https://maven.apache.org/download.cgi 
* Update the system environment variables to the bin path of the maven package (exampel: MAVEN_HOME C:\Program Files\Maven\apache-maven-3.9.4)
* Check that maven is installed mvn -version
3. Only if you want to skip point 1 and 3 you can install chocolatey https://chocolatey.org/install 
* Follow the instructions and make sure you have installed choco
* To install java just run in a new terminal: choco install openjdk --version=17.0.2
* To install maven just run in a new terminal: choco install maven --version=3.9.4
4. In the Visual Studio Code install the Extension Pack for Java (it should install 6 packages)
  * Debugger for Java  
  * Project Manager for Java 
  * Maven for Java
  * Test Runner for Java
  * Language Support for Java(TM) by Re
  * IntelliCode
2. Configure your apikey in Considition2023 line 13
3. Run the program via visual studio (the easiest way)  or
   * Go to main folder 2023-java build the project with mvn clean install or mvn clean package
   * Go to target and run  java -jar nameOfJarWithDependencies.jar- (change the name of the jar)
