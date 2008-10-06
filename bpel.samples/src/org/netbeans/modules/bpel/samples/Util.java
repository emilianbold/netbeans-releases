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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.bpel.samples;

import java.io.File;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.xml.sax.SAXException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.modules.compapp.projects.jbi.api.JbiProjectConstants;
import org.netbeans.modules.compapp.projects.jbi.ui.actions.AddProjectAction;
import org.netbeans.spi.project.ant.AntArtifactProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;

public final class Util {

  private Util () {}

  public static void unZipFile(InputStream source, FileObject rootFolder) throws IOException {
      try {
          ZipInputStream str = new ZipInputStream(source);
          ZipEntry entry;

          while ((entry = str.getNextEntry()) != null) {
              if (entry.isDirectory()) {
                  continue;
              }
              FileObject fo = FileUtil.createData(rootFolder, entry.getName());
              FileLock lock = fo.lock();

              try {
                  OutputStream out = fo.getOutputStream(lock);

                  try {
                      FileUtil.copy(str, out);
                  }
                  finally {
                      out.close();
                  }
              } 
              finally {
                  lock.releaseLock();
              }
          }
      }
      finally {
          source.close();
      }
  }
 
  public static void setProjectName(FileObject prjLoc, String projTypeName, String newName, String defaultName) {
    renameInXml(prjLoc, projTypeName, newName);
    renameInProperties(prjLoc, newName, defaultName);
  }

  public static void renameInProperties(FileObject prjLoc, String newName, String defaultName) {
    FileObject propertiesFile = prjLoc.getFileObject("nbproject/project.properties"); // NOI18N

    try {
      String text = readContent(propertiesFile);
      text = replace(text, defaultName, newName);
      BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
              propertiesFile.getOutputStream(), "UTF-8")); //NOI18N
      try {
        writer.write(text);
      } finally {
        writer.close();
      }
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static String readContent(FileObject fileObject) throws IOException {
    StringBuffer buffer = new StringBuffer();
    String separator = System.getProperty("line.separator"); // NOI18N
    BufferedReader reader = new BufferedReader(new InputStreamReader(
            fileObject.getInputStream(), "UTF-8")); //NOI18N
    
    try {
        String line = reader.readLine();

        while (line != null) {
          buffer.append(line);
          buffer.append(separator);
          line = reader.readLine();
        }
    } finally {
        reader.close();
    }

    return buffer.toString();
  }

  private static void renameInXml(FileObject prjLoc, String projTypeName, String name) {
      try {
          File projXml = FileUtil.toFile(prjLoc.getFileObject(AntProjectHelper.PROJECT_XML_PATH));
          Document doc = XMLUtil.parse(new InputSource(projXml.toURI().toString()), false, true, null, null);
          NodeList nlist = doc.getElementsByTagNameNS(projTypeName, "name");       //NOI18N
          if (nlist != null) {
              for (int i=0; i < nlist.getLength(); i++) {
                  Node n = nlist.item(i);
                  if (n.getNodeType() != Node.ELEMENT_NODE) {
                      continue;
                  }
                  Element e = (Element)n;
                  
                  replaceText(e, name);
              }
              saveXml(doc, prjLoc, AntProjectHelper.PROJECT_XML_PATH);
          }
      } 
      catch (IOException e) {
          e.printStackTrace();
      }
      catch (SAXException e) {
          e.printStackTrace();
      }
  }

  public static void addJbiModule(final FileObject compAppDir, final FileObject moduleDir) throws IOException {
      Project compAppProject = ProjectManager.getDefault().findProject(compAppDir);
      Project jbiModule = ProjectManager.getDefault().findProject(moduleDir);

      AddProjectAction addJbiModuleAction = new AddProjectAction();
      AntArtifactProvider artifactProvider = (AntArtifactProvider) jbiModule.getLookup().lookup(AntArtifactProvider.class);
      AntArtifact[] antArtifacts = null;

      if (artifactProvider != null) {
          antArtifacts = getFilteredAntArtifacts(artifactProvider.getBuildArtifacts());
      }
      if (antArtifacts != null) {
          for (AntArtifact antArtifact : antArtifacts) {
              addJbiModuleAction.addProject(compAppProject, antArtifact);
          }
      }
  }

  private static AntArtifact[] getFilteredAntArtifacts(AntArtifact[] antArtifacts) {
      if (antArtifacts == null) {
          return null;
      }
      List<AntArtifact> filteredArtifacts = new ArrayList<AntArtifact>();
      for (AntArtifact artifact : antArtifacts) {
          for (String artifactPrefix : JAVAEE_ARTIFACT_TYPES) {
              if (artifact.getType().startsWith(artifactPrefix)) {
                  filteredArtifacts.add(artifact);
              }
          }
      }
      return filteredArtifacts.toArray(new AntArtifact[filteredArtifacts.size()]);
  }

  private static String replace(String source, String searchFor, String replaceWith) {
    StringBuffer buffer = new StringBuffer();
    int last = 0;

    for (int k = source.indexOf(searchFor, last); k >= 0; last = k + searchFor.length(), k = source.indexOf(searchFor, last)) {
      buffer.append(source.substring(last, k));
      buffer.append(replaceWith);
    }
    if (last > 0) {
      buffer.append(source.substring(last));
      return buffer.toString();
    } 
    else {
      return source;
    }
  }

  private static void replaceText(Element parent, String name) {
      NodeList l = parent.getChildNodes();
      for (int i = 0; i < l.getLength(); i++) {
          if (l.item(i).getNodeType() == Node.TEXT_NODE) {
              Text text = (Text)l.item(i);
              text.setNodeValue(name);
              return;
          }
      }
  }

  private static void saveXml(Document doc, FileObject dir, String path) throws IOException {
      FileObject xml = FileUtil.createData(dir, path);
      FileLock lock = xml.lock();
      try {
          OutputStream os = xml.getOutputStream(lock);
          try {
              XMLUtil.write(doc, os, "UTF-8"); // NOI18N
          } finally {
              os.close();
          }
      } finally {
          lock.releaseLock();
      }
  }

  private static final String [] JAVAEE_ARTIFACT_TYPES = new String [] {
    "j2ee_archive", // NOI18N
    "war", // NOI18N
    JbiProjectConstants.ARTIFACT_TYPE_JBI_ASA
  };

  public static final String RESERVATION_PARTNER_SERVICES = "ReservationPartnerServices"; // NOI18N
  public static final String BPEL_PROJECT_CONFIGURATION_NAMESPACE = "http://www.netbeans.org/ns/j2ee-bpelpro/1"; // NOI18N
  public static final String COMPAPP_PROJECT_CONFIGURATION_NAMESPACE = "http://www.netbeans.org/ns/j2ee-jbi/1";  // NOI18N
}
