# Introduction

This is a start kit for those that will participate in Considition 2023 using Java.

Considition2023.java contains an example of:

- Fetching required data
- Submitting a solution to Considition 2023
- Scoring a solution locally and saving the "game"

# Getting Started

We recommended using Visual studio 2022 (https://visualstudio.microsoft.com/vs/)

----Running code-----

1. Install java https://www.oracle.com/se/java/technologies/downloads/
* Check/update the environment variables to make sure you are using the correct SDK
2. In the Visual Studio Code install the Extension Pack for Java (it should install 6 packages)
  * Debugger for Java  
  * Project Manager for Java 
  * Maven for Java
  * Test Runner for Java
  * Language Support for Java(TM) by Re
  * IntelliCode
2. Configure your apikey in Considition2023 line 13
3. Run the program via visual studio (the easiest way) 
   * or go to main folder 2023-java build the project with mvn clean install or mvn clean package
   * go to target and run  java -jar nameOfJarWithDependencies.jar- (change the name of the jar)