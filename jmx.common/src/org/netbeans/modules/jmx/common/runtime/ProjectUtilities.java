/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.jmx.common.runtime;

import org.apache.tools.ant.module.api.support.ActionUtils;

import org.netbeans.api.project.Project;

import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;

import org.netbeans.spi.project.ui.support.MainProjectSensitiveActions;
import org.netbeans.spi.project.ui.support.ProjectActionPerformer;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import javax.swing.*;
import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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
    }, "", null);// NOI18N
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
      e.printStackTrace();
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
