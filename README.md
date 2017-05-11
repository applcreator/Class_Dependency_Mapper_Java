# Class_Dependency_Mapper_Java
This project uses the java7 grammar file and Antlr4 framework developed by Terrence Parr and Sam Harwell. These files are used as is without any modification and hence will continue to be the property of its creators.

Please add the antlr-4.5.3-complete.jar to your classpath before executing the VisitorParser.

VisitorParser.java - identifies objects with dependencies to other classes within the class under test.
Acceptable form of input - is the class name under test. Eg: java VisitorParser sampleClassUnderTest.java

The output is a list of couplings documented in a CSV file format.

This project is able to parse files using Java7. Additional features of Java8 such as Lambda expresssions are not handled in this version.
