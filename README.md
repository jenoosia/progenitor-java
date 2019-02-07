*OBSOLETE*

# Progenitor/Java

Progenitor/Java is a baseline web application template for server-side development.
This template is set up as an Eclipse project, as it is the most widespread IDE used
for Java development. It should be relatively easy to convert for use in IDEA, Netbeans, or
other IDEs.

## Eclipse Usage Notes

*(A future update to this template may utilize the gradle eclipse plugin to generate these
project files automatically.)*

Eclipse-specific dotfiles are checked in with the project. This allows you to quickly
bootstrap development in Eclipse right after importing the project. As a guideline,
**when development is in full-swing, developers should never commit any personalized
changes to these files into the repo**. The files are only committed initially to help
set up quickly.

Note that the following Eclipse plugins are recommended to be installed in the dev's 
environment for all Eclipse-related settings to be utilized:

*   Web Tools Platform (WTP) - Should come bundled with the J2EE Eclipse build.
*   JBoss Tools - For server runtime. Ignore this if you're planning to use Tomcat or another
    servlet container.
*   SpringSource Toolsuite / Spring IDE
*   Gradle Integration using SpringSource

## Major Components

### Gradle

Gradle will be the server-side dependency management and build tool. Gradle must be installed
in the development environment.

### Progenitor

The front-end version of Progenitor will be used as part of the front-end (View) development side
of any web application. As such, this part of Progenitor/Java will be periodically updated as
more best practices or boilerplate are added to the companion project.

Right now, this syncing will be done manually, as the /webapp/ folder is a mix of Progenitor and 
Java web deployment files.

## Framework

Progenitor/Java comes with an opinionated base framework for developing web applications
on the server side.

### MVC

*   Struts 2

### ORM

*   Hibernate 3.6
*   Generic Dao

### Dependency Injection / AOP / Transaction Management

*   Spring

### Static File Serving

*   BalusC's File Servlet
