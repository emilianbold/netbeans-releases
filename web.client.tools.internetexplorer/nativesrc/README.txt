NetBeansExtension project is created using Visual Studio 2005 professional edition

In order to build this project, 
1)Open the solution or project file in visual studio 2005 professional edition
2)Install Active Debug headers and libraries (http://support.microsoft.com/kb/223389) which can be downloaded from Microsoft(R) web site.  Install these files under the "nativesrc/activescript" subdirectory of this module.
3)Install Visual studio 2005 SDK(http://www.microsoft.com/downloads/details.aspx?FamilyID=94c9970d-c247-4ded-a76d-f7b3d589e71d&DisplayLang=en). 
  Install it under "nativesrc/VSIP 8.0" subdirectory of this module. Uncheck all except Visual Studio .NET Debugging SDK in Select Features dialog
4)If different directories are used in step 2 and 3, update additional includes and libraries in the project properties dialog to include the active debug headers and libraries.

