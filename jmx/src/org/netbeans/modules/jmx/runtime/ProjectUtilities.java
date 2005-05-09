/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL.
 * Use is subject to license terms.
 */

package org.netbeans.modules.jmx.runtime;

import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.java.project.JavaProjectConstants;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;

//import org.netbeans.netfluid.Profiler;

import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;

import org.netbeans.spi.project.ui.support.MainProjectSensitiveActions;
import org.netbeans.spi.project.ui.support.ProjectActionPerformer;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

/**
 * Utilities for interaction with the NetBeans IDE, specifically related to Projects
 *
 * @author Ian Formanek
 */
public class ProjectUtilities {
  private static Action mainProjectHack; // only used in 4.0
  private static Object mainProject;     // only used in 4.0

  static {
    // a hack in 4.0 to obtain current main project
    ProjectUtilities.mainProjectHack = MainProjectSensitiveActions.mainProjectSensitiveAction(new ProjectActionPerformer() {
      public boolean enable(Project project) { return true; }
      public void perform(Project project) {
        ProjectUtilities.mainProject = project;
      }
    }, "", null);
  }


  public static Project getMainProject() {
    mainProjectHack.actionPerformed(null);
    return (Project)mainProject;
  }

 

  public static ExecutorTask runTarget(Project project, String target) {
    return runTarget(project, target, null);
  }

  public static ExecutorTask runTarget(Project project, String target, Properties props) {
    FileObject buildFile = project.getProjectDirectory().getFileObject(GeneratedFilesHelper.BUILD_XML_PATH);
    try {
      return ActionUtils.runTarget(buildFile, new String[] { target }, props);
    } catch (IOException e) {
      //Profiler.getDefault().notifyException(Profiler.EXCEPTION, e);
        //XXX REVISIT Exception handling
    }
    return null;
  }

  public static boolean isProjectTypeSupported(Project project) {
   return J2SEProjectType.isProjectTypeSupported(project);
  }

  public static FileObject findTestForFile(FileObject fo) {
    return null;  // TODO: [ian] test if the fo is a test, if so return it, if not find a test for it
  }

  public static FileObject getRoot(FileObject[] roots, FileObject file) {
    FileObject srcDir = null;
    for (int i = 0; i < roots.length; i++) {
      if (FileUtil.isParentOf(roots[i], file)) {
        srcDir = roots[i];
        break;
      }
    }
    return srcDir;
  }

  public static boolean backupBuildScript(Project project) {
    FileObject buildFile = project.getProjectDirectory().getFileObject("build.xml"); //NOI18N
    FileObject buildBackupFile = project.getProjectDirectory().getFileObject("build-before-management.xml"); //NOI18N
    if (buildBackupFile != null) {
      try {
        buildBackupFile.delete();
      } catch (IOException e) {
        e.printStackTrace(System.err);
        return false;
        // cannot delete already existing backup
      }
    }
    try {
      buildFile.copy(project.getProjectDirectory(), "build-before-management", "xml"); //NOI18N
    } catch (IOException e1) {
      e1.printStackTrace(System.err);
      return false;
    }
    return true;
  }

  public static String getProjectBuildScript(Project project) {
    FileObject buildFile = project.getProjectDirectory().getFileObject("build.xml"); //NOI18N
    BufferedInputStream bis = null;
    byte[] data = null;
    try {
      bis = new BufferedInputStream(buildFile.getInputStream());
      data = new byte[(int) buildFile.getSize()];
      bis.read(data);
    } catch (FileNotFoundException e2) {
      e2.printStackTrace(System.err);
      return null;
    } catch (IOException e2) {
      e2.printStackTrace(System.err);
      return null;
    } finally {
      if (bis != null)
        try {
          bis.close();
        } catch (IOException e2) {
          e2.printStackTrace(System.err);
        }
    }
    return new String(data);
  }
}
