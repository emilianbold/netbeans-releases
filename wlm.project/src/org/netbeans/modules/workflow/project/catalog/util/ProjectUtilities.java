/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.workflow.project.catalog.util;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import java.util.Set;
import org.openide.filesystems.FileObject;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;

//import org.netbeans.modules.workflow.project.catalog.DefaultProjectCatalogSupport;
import org.netbeans.modules.workflow.project.catalog.ProjectConstants;
import org.netbeans.modules.xml.catalogsupport.DefaultProjectCatalogSupport;

/**
 * Utility Class to provide project reference support.
 * NOTE: This class copied from catalogsupport module of xml2
 * the reason being that the process to add new friend to catalogsupport module
 * is still being worked out
 * 
 */

public class ProjectUtilities {

  private ProjectUtilities() {}
    
  public static List<FileObject> getXSDFilesRecursively(Project project) {
    return getFilesRecursively(project, XSD);
  }

  public static List<FileObject> getWSDLFilesRecursively(Project project) {
    return getFilesRecursively(project, WSDL);
  }

  private static List<FileObject> getFilesRecursively(Project project, final String extension) {
    final List<FileObject> files = new ArrayList<FileObject>();

    ProjectUtilities.visitRecursively(project, new ProjectVisitor() {
      public void visit(Project project) {
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup [] groups = sources.getSourceGroups(ProjectConstants.SOURCES_TYPE_XML);

        for (SourceGroup group : groups) {
          Enumeration children = group.getRootFolder().getChildren(true);

          while (children.hasMoreElements()) {
            FileObject file = (FileObject) children.nextElement();

            if (file.getExt().toLowerCase().equals(extension)) {
              files.add(file);
            }
          }
        }
      }
    });

    return files;
  }

  public static List<ProjectWSDL> getProjectWSDLRecursively(Project project) {
    final List<ProjectWSDL> wsdls = new ArrayList<ProjectWSDL>();

    ProjectUtilities.visitRecursively(project, new ProjectVisitor() {
      public void visit(Project project) {
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup [] groups = sources.getSourceGroups(ProjectConstants.SOURCES_TYPE_XML);

        for (SourceGroup group : groups) {
          Enumeration files = group.getRootFolder().getChildren(true);

          while (files.hasMoreElements()) {
            FileObject file = (FileObject) files.nextElement();

            if (file.getExt().toLowerCase().equals(WSDL)) {
              wsdls.add(new ProjectWSDL(file, project));
            }
          }
        }
      }
    });

    return wsdls;
  }

    /**
     * Returns the set of projects to which the base project references to. 
     * Not only direct referenced, but all project are taken into consideration.
     * 
     * @param baseProj
     * @return
     */
    public static Set getReferencedProjects(Project baseProj) {
        HashSet<Project> projectSet = new HashSet<Project>();
        populateReferencedProjects(baseProj, projectSet);
        return projectSet;
    }

    /**
     * Fill in the set of projects to which the base project references to. 
     * @param baseProj
     * @param projectSet
     */
    private static void populateReferencedProjects(
            Project baseProj, Set<Project> projectSet) {
        DefaultProjectCatalogSupport instance = DefaultProjectCatalogSupport.
                getInstance(baseProj.getProjectDirectory());
        //
        if (instance != null) {
            Set references = instance.getProjectReferences();
            for (Object proj : references) {
                assert proj instanceof Project;
                projectSet.add((Project) proj);
                //
                populateReferencedProjects((Project) proj, projectSet);
            }
        }
    }

    /**
     * Collects the content of source folders of the project. 
     * If the extension is specified, then files only with the extension 
     * are returned.
     * 
     * @param project
     * @param extension
     * @return
     */
    public static List<FileObject> getDirectSources(Project project, String extension) {
        final List<FileObject> files = new ArrayList<FileObject>();
        //
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] groups = sources.getSourceGroups(ProjectConstants.SOURCES_TYPE_XML);
        //
        for (SourceGroup group : groups) {
            Enumeration children = group.getRootFolder().getChildren(true);
            //
            while (children.hasMoreElements()) {
                FileObject file = (FileObject) children.nextElement();
                //
                if (file.isFolder()) {
                    files.add(file);
                } else if (extension == null) {
                    files.add(file);
                } else if (file.getExt().toLowerCase().equals(extension)) {
                    files.add(file);
                }
            }
        }
        //
        return files;
    }
  
  private static void visitRecursively(Project project, ProjectVisitor visitor) {
    visitRecursively(project, visitor, new ArrayList<Project>());
  }

  private static void visitRecursively(Project project, ProjectVisitor visitor, List<Project> visited) {
    if (project == null) {
      return;
    }
    if (visited.contains(project)) {
      return;
    }
    visited.add(project);
    visitor.visit(project);

    DefaultProjectCatalogSupport instance = DefaultProjectCatalogSupport.getInstance(project.getProjectDirectory());

    if (instance == null) {
      return;
    }
    Iterator projects = instance.getProjectReferences().iterator();

    while (projects.hasNext()) {
      Project next = (Project) projects.next();
      visitRecursively(next, visitor, visited);
    }
  }

  public static FileObject getSrcFolder(Project project) {
    return project.getProjectDirectory().getFileObject(SRC_FOLDER);
      }
      
  public static final String XSD = "xsd"; // NOI18N
  public static final String WSDL = "wsdl"; // NOI18N
  public static final String SRC_FOLDER = "src"; // NOI18N
  public static final String SLASHED_SRC = "/" + SRC_FOLDER + "/"; // NOI18N
}
