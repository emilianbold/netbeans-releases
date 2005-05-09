/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL.
 * Use is subject to license terms.
 */
package org.netbeans.modules.jmx.runtime;


import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;

import org.netbeans.spi.project.AuxiliaryConfiguration;

import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;

import org.openide.util.Mutex;
import org.openide.util.MutexException;

import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Element;

import org.openide.util.NbBundle;

import java.io.*;
import java.util.Properties;

import java.util.Set;
import java.util.Iterator;

/**
 * @author Ian Formanek
 */
public class J2SEProjectType {

  public static final ErrorManager err = ErrorManager.getDefault().getInstance("org.netbeans.modules.profiler.j2se"); // NOI18N

  private static final String J2SE_PROJECT_NAMESPACE_40 = "http://www.netbeans.org/ns/j2se-project/1";
  private static final String J2SE_PROJECT_NAMESPACE_41 = "http://www.netbeans.org/ns/j2se-project/2";

  private static final String STANDARD_IMPORT_STRING = "<import file=\"nbproject/build-impl.xml\"/>";
  private static final String MANAGEMENT_IMPORT_STRING = "<import file=\"nbproject/management-build-impl.xml\"/>";
  private static final String MANAGEMENT_NAME_SPACE = "http://www.netbeans.org/ns/jmx/1";

  public static boolean isProjectTypeSupported(Project project) {
    AuxiliaryConfiguration aux = (AuxiliaryConfiguration) project.getLookup().lookup(AuxiliaryConfiguration.class);
    if (aux == null) {
      System.err.println("Auxiliary Configuration is null for Project: "+project);
      return false;
    }
    Element e = aux.getConfigurationFragment("data", J2SE_PROJECT_NAMESPACE_40, true);
    if (e== null) e = aux.getConfigurationFragment("data", J2SE_PROJECT_NAMESPACE_41, true);
    return (e != null);
  }

  public static boolean checkProjectCanBeManaged(Project project) {
      Properties pp = getProjectProperties(project);
      String mainClass = pp.getProperty("main.class");
      boolean res = false;
      if (mainClass != null && !"".equals(mainClass)) {
        FileObject fo = findFileForClass(mainClass, true);
        if (fo != null) res = true;
      }
      if (!res) {
        ManagementDialogs.getDefault().notify(
        new NotifyDescriptor.Message(NbBundle.getMessage(J2SEProjectType.class, "ERR_MainClassNotSet"), NotifyDescriptor.WARNING_MESSAGE));
       
        return false;
      }
      return true;
  }
  
 public static FileObject findFileForClass (String className, boolean tryInnerclasses) {
    FileObject fo = null;
    try {
      String resourceName = className.replaceAll("\\.", "/") + ".java"; //NOI18N
      GlobalPathRegistry gpr = GlobalPathRegistry.getDefault();
      Set paths = gpr.getPaths("classpath/source"); //NOI18N
      for (Iterator iterator = paths.iterator(); iterator.hasNext();) {
        ClassPath cp = (ClassPath) iterator.next();
        fo = cp.findResource(resourceName);
        if (fo != null) break;
      }
    } catch (Exception e) {
      e.printStackTrace(System.err);
    }
    if ((fo == null) && tryInnerclasses) {
      // not found - will try without last .xxx to see if the last name is not an innerclass name
      int dotIndex = className.lastIndexOf('.');
      if (dotIndex != -1)
         return findFileForClass(className.substring(0, dotIndex), true);
    }
    return fo;
  }
  public static boolean checkProjectIsModifiedForManagement(Project project) {
    Element e = ((AuxiliaryConfiguration) project.getLookup().lookup(AuxiliaryConfiguration.class)).getConfigurationFragment("data", MANAGEMENT_NAME_SPACE, true);
    if (e != null) return true; // already modified, nothing more to do

    if (ManagementDialogs.getDefault().notify(
        new NotifyDescriptor.Confirmation (
            "This is the first time this project is to be managed. This module needs to modify the project build script to enable management.\n" +
            "A simple import will be created in the project build.xml, the original will be backed up as build-before-management.xml\n" +
            "Do you wish to perform this change and continue?\n\n" +
            "If you have not performed custom edits to your project's build script, click \"OK\"",
            NotifyDescriptor.OK_CANCEL_OPTION)
        ) != NotifyDescriptor.OK_OPTION)
    {
        return false; // cancelled by the user
    }

    // not yet modified for profiler => create profiler-build-impl & modify build.xml and project.xml
    Element mgtFragment = XMLUtil.createDocument("ignore", null, null, null).createElementNS(MANAGEMENT_NAME_SPACE, "data");
    mgtFragment.setAttribute("version", "0.4");
    ((AuxiliaryConfiguration) project.getLookup().lookup(AuxiliaryConfiguration.class)).putConfigurationFragment(mgtFragment, true);
    try {
      ProjectManager.getDefault().saveProject(project);
    } catch (IOException e1) {
      err.notify(e1);
      e1.printStackTrace(System.err);
      return false;
    }

    try {
      GeneratedFilesHelper gfh = new GeneratedFilesHelper(project.getProjectDirectory());
      gfh.refreshBuildScript("nbproject/management-build-impl.xml", J2SEProjectType.class.getResource("management-build-impl.xsl"), false);
    } catch (IOException e1) {
      err.notify(ErrorManager.INFORMATIONAL, e1);
      return false;
    }

    String buildScript = ProjectUtilities.getProjectBuildScript(project);

    if (buildScript == null) {
      ManagementDialogs.getDefault().notify(
          new NotifyDescriptor.Message (
              "Cannot find the project build.xml file.",
              NotifyDescriptor.ERROR_MESSAGE)
          );
      return false;
    }

    if (!ProjectUtilities.backupBuildScript(project)) {
      if (ManagementDialogs.getDefault().notify(
          new NotifyDescriptor.Confirmation (
              "The project build script cannot be backed up. Do you want to continue anyway?",
              NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.WARNING_MESSAGE)
          ) != NotifyDescriptor.OK_OPTION)
      {
          return false; // cancelled by the user
      }
    }

    StringBuffer newDataBuffer = new StringBuffer (buildScript.length() + 200);
    int importIndex = buildScript.indexOf(STANDARD_IMPORT_STRING);
    if (importIndex == -1) {
      // notify the user that the build script cannot be modified, and he should perform the change himself
      ManagementDialogs.getDefault().notify(
          new NotifyDescriptor.Message (
              "The project build.xml file cannot be automatically modified for management. You need to manually insert the following import clause in it:\n" +
              "<import file=\"nbproject/management-build-impl.xml\"/>\n" +
              "Please perform this change and start management again.",
              NotifyDescriptor.WARNING_MESSAGE)
          );
      return false;
    }
    String indent = "";
    int idx = importIndex-1;
    while (idx >= 0) {
      if (buildScript.charAt(idx) == ' ') indent = " " + indent;
      else if (buildScript.charAt(idx) == '\t') indent = "\t" + indent;
      else break;
      idx--;
    }
    newDataBuffer.append(buildScript.substring(0, importIndex+STANDARD_IMPORT_STRING.length()+1));
    newDataBuffer.append("\n");
    newDataBuffer.append(indent);
    newDataBuffer.append(MANAGEMENT_IMPORT_STRING);
    newDataBuffer.append(buildScript.substring(importIndex+STANDARD_IMPORT_STRING.length()+1));

    FileObject buildFile = project.getProjectDirectory().getFileObject("build.xml");
    FileLock lock = null;
    PrintWriter writer = null;
    try {
      lock = buildFile.lock();
      writer = new PrintWriter(buildFile.getOutputStream(lock));
      writer.println(newDataBuffer.toString());

    } catch (FileNotFoundException e1) {
      e1.printStackTrace(System.err);
      err.notify(e1);
    } catch (IOException e1) {
      e1.printStackTrace(System.err);
      err.notify(e1);
    } finally {
        lock.releaseLock();
        if (writer != null)
            writer.close();
    }
    return true;
  }
  
  public static void overwriteProperty(Project project, final String key, final String value) throws Exception {
      FileObject privatePropsFile = project.getProjectDirectory().getFileObject(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
      final File projectPropsFile = FileUtil.toFile(project.getProjectDirectory().getFileObject(AntProjectHelper.PROJECT_PROPERTIES_PATH));
      //final File userPropsFile = InstalledFileLocator.getDefault().locate("build.properties", null, false);
      ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction() {
          public Object run() throws Exception {
              java.util.Properties p = new java.util.Properties();
              
              FileInputStream fis = new FileInputStream(projectPropsFile);
              try {
                  p.load(fis);
                  p.setProperty(key, value);
              }finally{
                  fis.close();
              }
              FileOutputStream fos = new FileOutputStream(projectPropsFile);
              try {
                  p.store(fos,null);
              }finally{
                  fos.close();
              }
              return null;
          }
      });
  }
  
  public static Properties getProjectProperties(Project project) {
    Properties props = new Properties();
    FileObject privatePropsFile = project.getProjectDirectory().getFileObject(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
    FileObject projectPropsFile = project.getProjectDirectory().getFileObject(AntProjectHelper.PROJECT_PROPERTIES_PATH);
    File userPropsFile = InstalledFileLocator.getDefault().locate("build.properties", null, false);

    // the order is 1. private, 2. project, 3. user to reflect how Ant handles property definitions (immutable, once set property value cannot be changed)
    if (privatePropsFile != null) {
      try {
        InputStream is = privatePropsFile.getInputStream();
        try {
          props.load(is);
        } finally {
          is.close();
        }
      } catch (IOException e) {
        err.notify(ErrorManager.INFORMATIONAL, e);
      }
    }

    if (projectPropsFile != null) {
      try {
        InputStream is = projectPropsFile.getInputStream();
        try {
          props.load(is);
        } finally {
          is.close();
        }
      } catch (IOException e) {
        err.notify(ErrorManager.INFORMATIONAL, e);
      }
    }

    if (userPropsFile != null) {
      try {
        InputStream is = new BufferedInputStream (new FileInputStream (userPropsFile));
        try {
          props.load(is);
        } finally {
          is.close();
        }
      } catch (IOException e) {
        err.notify(ErrorManager.INFORMATIONAL, e);
      }
    }
    return props;
  }

  /*
  public SessionSettings getProjectSessionSettings(Project project) {
    SessionSettings ss = new SessionSettings();
    Properties pp = getProjectProperties(project);
    ss.setMainClass(pp.getProperty("main.class", ""));
    ss.setMainArgs(pp.getProperty("application.args", ""));
    ss.setMainClassPath(pp.getProperty("run.classpath", ""));

    return ss;
  }
*/
  public String getProfilerTargetName(Project project, int type, FileObject profiledClass) {
      /*
    switch (type) {
      case TARGET_PROFILE: return "profile";
      case TARGET_PROFILE_SINGLE:
        if (SourceUtilities.isApplet (profiledClass)) return "profile-applet";
        else return "profile-single";
      case TARGET_PROFILE_TEST: return "profile-test-single";
      default: return null;
    }
       **/
      return null;
  }

  public void configurePropertiesForProfiling(Properties props, Project project, FileObject profiledClassFile) {
      /*
    if (profiledClassFile != null) { // In case the class to profile is explicitely selected (profile-single)
      // 1. specify profiled class name
      String profiledClass = SourceUtilities.getMainClassName(profiledClassFile);
      props.setProperty("profile.class", profiledClass); //NOI18N

      // 2. include it in javac.includes so that the compile-single picks it up
      String clazz = FileUtil.getRelativePath(ProjectUtilities.getRoot(ProjectUtilities.getSourceRoots(project), profiledClassFile), profiledClassFile);
      props.setProperty("javac.includes", clazz); //NOI18N
    }
  }
       */
  }
}
