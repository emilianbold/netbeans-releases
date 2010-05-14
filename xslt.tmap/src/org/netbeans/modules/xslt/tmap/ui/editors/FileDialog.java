/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License. When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP. Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.xslt.tmap.ui.editors;

import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.swing.filechooser.FileSystemView;

import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.modules.xml.reference.ReferenceUtil;

/**
 * @author Vitaly Bychkov
 */
public final class FileDialog extends FileSystemView {

  public FileDialog(Project project) {
    myDelegatedFileSystemView = getFileSystemView();
    assert project != null;
    myProject = project;
    mySrcFolder = ReferenceUtil.getSrcFolder(myProject);
  }

  @Override
  public File[] getRoots() {
    List<Project> refProjects = ReferenceUtil.getReferencedProjects(myProject);  
    File[] roots = new File[refProjects == null ? 1 : refProjects.size()+1];
    if (refProjects != null) {
      for (int i = 0; i < refProjects.size(); i++) {
          Project refPrj = refProjects.get(i);
          if (refPrj == null) {
              continue;
          }
          roots[i] = FileUtil.toFile(refPrj.getProjectDirectory());
      }
    }
    roots[roots.length -1] = FileUtil.toFile(myProject.getProjectDirectory());
    return roots;
  }

  @Override
  public Boolean isTraversable(File f) {
    f = FileUtil.normalizeFile(f);
    if (!super.isTraversable(f)) {
        return false;
    }
    File[] roots = getRoots();
    FileObject fo =FileUtil.toFileObject(f);
    if (roots != null) {
        for (int i = 0; i < roots.length; i++) {
            if (!FileUtil.isParentOf(fo, FileUtil.toFileObject(roots[i]))) {
                return true;
            }
        }
    }
    return false;
  }

  @Override
  public boolean isFileSystemRoot(File dir) {
    File[] roots = getRoots();
    if (roots != null) {
        for (int i = 0; i < roots.length; i++) {
            File f = roots[i];
            if (f == null) {
                continue;
            }
                    
            if (f != null && f.equals(dir)) {
                return true;
            }
        }
    }
      
    return false;
  }
  
  @Override
  public File getParentDirectory(File dir) {
    if (dir == null) {
      return null;
    }
    
    File[] roots = getRoots();
    FileObject dirFo =FileUtil.toFileObject(dir);
    if (roots != null) {
        for (int i = 0; i < roots.length; i++) {
            File f = roots[i];
            if (f != null && FileUtil.isParentOf(FileUtil.toFileObject(f), dirFo)) {
                return dir.getParentFile();
            }
        }
    }

    return null;
  }

  public File createNewFolder(File containingDir) throws IOException {
    return myDelegatedFileSystemView.createNewFolder(containingDir);
  }

  private Project myProject;
  private FileObject mySrcFolder;
  private FileSystemView myDelegatedFileSystemView;
}
