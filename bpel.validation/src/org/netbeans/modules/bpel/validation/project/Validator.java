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
package org.netbeans.modules.bpel.validation.project;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Lookup;

import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;

import org.netbeans.modules.compapp.projects.jbi.api.ProjectValidator;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Process;
import static org.netbeans.modules.xml.ui.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2007.05.03
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.compapp.projects.jbi.api.ProjectValidator.class)
public class Validator implements ProjectValidator {

  // # 100036    
  public String validateProjects(List<Project> projects) {
//out();
//out();
//out("projects: " + projects);
//out();
//out();
    List<BPELFile> files = new ArrayList<BPELFile>();

    for (Project project : projects) {
      String result = addBPELFiles(project, files);

      if (result != null) {
        return result;
      }
    }
    return null;
  }

  private String addBPELFiles(Project project, List<BPELFile> files) {
    Sources sources = ProjectUtils.getSources(project);
    boolean allowBuildWithError = isAllowBuildWithError(project);
    SourceGroup [] groups =
      sources.getSourceGroups("xml"); // NOI18N

    for (SourceGroup group : groups) {
      Enumeration children = group.getRootFolder().getChildren(true);

      while (children.hasMoreElements()) {
        FileObject file = (FileObject) children.nextElement();

        if ( !file.getExt().toLowerCase().equals("bpel")) { // NOI18N
          continue;
        }
        String qName = getQName(file);

        if (qName == null) {
          continue;
        }
        String result = check(files, new BPELFile(file, project, qName), allowBuildWithError);

        if (result != null) {
          return result;
        }
      }
    }
    return null;
  }

  // # 106342
  private boolean isAllowBuildWithError(Project project) {
    AntProjectHelper helper = project.getLookup().lookup(AntProjectHelper.class);

    if (helper == null) {
      return false;
    }
    EditableProperties properties = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);

    if (properties == null) {
      return false;
    }
    return "true".equals(properties.get("allow.build.with.error")); // NOI18N
  }

  private String check(List<BPELFile> files, BPELFile file, boolean allowBuildWithError) {
    String qName = file.getQName();
//out();
//out("check: " + file.getName());
//out("qname: " + qName);

    for (BPELFile current : files) {
//out("  see: " + current.getQName() + " " + current.getName());
      if (current.getQName().equals(qName)) {
        if ( !allowBuildWithError) { // # 106342
          return i18n(getClass(), "ERR_Same_QName", // NOI18N
            current.getName(),
            file.getName(),
            qName
          );
        }
      }
    }
    files.add(file);

    return null;
  }

  private String getQName(FileObject file) {
    DataObject data;
    
    try {
      data = DataObject.find(file);
    }
    catch (DataObjectNotFoundException e) {
      return null;
    }
//out();
//out("data: " + data);

    if ( !(data instanceof Lookup.Provider)) {
      return null;
    }
    BpelModel model = (BpelModel) ((Lookup.Provider) data).getLookup().lookup(BpelModel.class);
    Process process = model.getProcess();

    return process.getName() + ", " + process.getTargetNamespace(); // NOI18N
  }

  // ---------------------------
  public static class BPELFile {
    public BPELFile(FileObject file, Project project, String qName) {
      myFile = file;
      myProject = project;
      myQName = qName;
    }

    public String getQName() {
      return myQName;
    }

    public String getName() {
      return "[" + ProjectUtils.getInformation(myProject).getDisplayName() + "]/" + // NOI18N
        myFile.getName();
    }

    private FileObject myFile;
    private Project myProject;
    private String myQName;
  }
}
