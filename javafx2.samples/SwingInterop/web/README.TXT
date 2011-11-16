================================
JavaFX2-in-Swing in NetBeans 7.1
================================

Building the sample SwingInterop produces in project dist/ subdirectory
a JAR file, a HTML file and two JNLP files - one to enable standalone 
application launch, the other to enable launch as applet within the HTML page. 
Note that the application is not a JavaFX 2.0 application and
the built files do not follow the JavaFX 2.0 deployment 
model as described at http://www.javafx.com. The JNLP files
are pre-FX WebStart files, extended only by the reference to the
required JavaFX 2.0+ runtime, which must be present in the system
in order to run the application. The application itself launches
either as standard Swing application or as (pre-FX) applet.
To run the sample outside of NetBeans IDE do one of the following:

a) java -jar SwingInterop.jar
b) double-click SwingInterop_application.jnlp
c) open SwingInterop.html in a browser with installed JavaFX plug-in


Creating JavaFX-in-Swing projects
=================================

NetBeans 7.1 supports either pure Java2SE (Swing) projects or pure
JavaFX 2.0+ projects, but does not provide specific support for 
JavaFX-in-Swing projects. Such support will be included in NetBeans 7.2. 
Before NetBeans 7.2 is available, the following workarounds can be followed:

1) Use SwingInterop sample as starting point and modify its contents
   as required. Note that the main.class property must be updated
   manually in nbproject/project.properties to point at the main class of
   your modified project.

2) To start from scratch, create a new JavaFX Preloader project (in menu
   File->New Project->JavaFX->JavaFX Preloader). Replace build.xml by
   build.xml taken from the SwingInterop sample (and preferably edit 
   <project> name in it). Copy the whole web/ subdirectory from
   SwingInterop sample to the new project. In nbproject/project.properties
   add manually the property main.class and set it to the true
   main class name when your java sources are ready.

Use Run to run the application within the IDE.
Use Clean and Build to obtain the deployment files as described above.
