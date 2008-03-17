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

package org.netbeans.modules.visualweb.project.jsf.framework;

import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectConstants;
import org.netbeans.modules.visualweb.project.jsf.api.ProjectTemplate;
import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectUtils;
import org.netbeans.modules.visualweb.project.jsf.libraries.JsfProjectLibrary;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.netbeans.api.project.Project;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author dey
 */
public class JsfProjectTemplate extends ProjectTemplate {
    /** Creates a new instance of JsfProjectTemplate */
    public JsfProjectTemplate() {
    }

    public void addLibrary(Project project) throws IOException {
        // Add the Creator libraries to the project
        JsfProjectLibrary.addLibrary(project);
    }

    public void create(Project project, String pageName) throws IOException {
        // Extract the project template file for this project type
        InputStream is = JsfProjectTemplate.class.getResourceAsStream("JsfProjectTemplate.xml"); // NOI18N
        Document document = null;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Node node;

        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.parse(is);
            FileObject projDir = project.getProjectDirectory();
            instantiateFile(project, document.getDocumentElement(), project.getProjectDirectory(), pageName);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            ErrorManager.getDefault().notify(e);
        }
    }

    public void instantiateFile(Project project, Node node, FileObject folder, String pageName) throws IOException {
        String nodeName = node.getNodeName();
        String fileName = getAttr(node, "name"); // NOI18N
        
        if (nodeName.equals("file")) {
            if ((pageName != null) && "Page1.jsp".equals(fileName)) {
                fileName = pageName;
            }
            
            Map<String, String> templateParameters = new HashMap<String, String>();
            templateParameters.put("folder", ""); //NOI18N
            templateParameters.put("creatingProject", "true"); //NOI18N
            templateParameters.put("j2eePlatformVersion", JsfProjectUtils.getJ2eePlatformVersion(project)); //NOI18N
            templateParameters.put("sourceLevel", JsfProjectUtils.getSourceLevel(project)); //NOI18N
            
            if ((fileName.length() > 0) && folder.getFileObject(fileName) == null) {
                instantiateFileTemplate(folder, fileName, getAttr(node, "template"), templateParameters);  // NOI18N
            }
        } else {
            FileObject newFolder = null;
            if (nodeName.equals("folder")) {  // NOI18N
                if (fileName.equals("${" + JsfProjectConstants.SRC_DIR + "}")) {
                    FileObject src = JsfProjectUtils.getSourceRoot(project);
                    fileName = FileUtil.getRelativePath(project.getProjectDirectory(), src);
                    if (fileName == null) {
                        fileName = "src/java"; // NOI18N
                    }
                } else if (fileName.equals("${" + JsfProjectConstants.WEB_DOCBASE_DIR + "}")) {
                    FileObject webDocbase = JsfProjectUtils.getDocumentRoot(project);
                    fileName = FileUtil.getRelativePath(project.getProjectDirectory(), webDocbase);
                    if (fileName == null) {
                        fileName = "web"; // NOI18N
                    }
                } else if (fileName.equals("${" + JsfProjectConstants.PROP_JSF_PAGEBEAN_PACKAGE + "}")) {
                    fileName = getBeanPackage();
                    if (fileName == null) {
                        fileName = deriveSafeName(project.getProjectDirectory().getName());
                    }
                }
                fileName = fileName.replace('.',  '/');
                if ((newFolder = folder.getFileObject(fileName)) == null) {
                    newFolder = FileUtil.createFolder(folder, fileName);
                }
            } else {
                newFolder = folder;
            }
            
            NodeList nodes = node.getChildNodes();
            for (int i = 0; i < nodes.getLength(); i++) {
                instantiateFile(project, nodes.item(i), newFolder, pageName);
            }
        }
    }
}
