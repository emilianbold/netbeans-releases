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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.javacard.project;

import org.apache.tools.ant.module.api.AntProjectCookie;
import org.apache.tools.ant.module.api.AntTargetExecutor;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.javacard.constants.ProjectPropertyNames;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.swing.event.ChangeListener;
import java.awt.event.WindowAdapter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.netbeans.modules.javacard.spi.ProjectKind;

final class ProjectRenamer extends WindowAdapter implements AntProjectCookie, Mutex.ExceptionAction<Void> {

    final String newName;
    private final AntProjectHelper antHelper;
    private final ProjectKind kind;

    ProjectRenamer(String newName, AntProjectHelper antHelper, ProjectKind kind) {
        this.newName = newName;
        this.antHelper = antHelper;
        this.kind = kind;
    }

    void doRename() {
        try {
            cleanTheProject();
            renameProjectMetadata();
            modifyBuildScriptProjectName();
            modifyProjectPropertiesProjectName();
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
    }

    private void cleanTheProject() throws IOException {
        //clean the project
        AntTargetExecutor.Env env = new AntTargetExecutor.Env();
        AntTargetExecutor exe = AntTargetExecutor.createTargetExecutor(env);
        exe.execute(this, new String[]{"clean"}); //NOI18N
    }

    private void renameProjectMetadata() throws IOException {
        //run the rename code
        try {
            ProjectManager.mutex().writeAccess(this);
        } catch (MutexException e) {
            IOException ioe = new IOException();
            ioe.initCause(e);
            throw ioe;
        }
    }

    private void modifyBuildScriptProjectName() throws IOException {
        //regenerate the build scripts
        FileObject bs = getFileObject();
        if (bs == null) {
            throw new IOException ("Build script missing at " +  //NOI18N
                    getFile().getAbsolutePath());
        }
        //XXX something is rewriting the build.xml with no name after we do here
        InputStream inStream = bs.getInputStream();
        Document nue = null;
        try {
            InputSource in = new InputSource (inStream);
            nue = XMLUtil.parse(in, false, false, null, null);
            Element root = nue.getDocumentElement();
            iterDocument(root);
        } catch (SAXException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            inStream.close();
        }
        if (nue != null) {
            EditableProperties ep = antHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            String encoding = ep.getProperty(ProjectPropertyNames.PROJECT_PROP_SOURCE_ENCODING);
            FileLock lock = bs.lock();
            OutputStream out = bs.getOutputStream(lock);
            try {
                XMLUtil.write(nue, out, encoding == null ? "UTF-8" : encoding); //NOI18N
            } finally {
                out.close();
                lock.releaseLock();
            }
        }
    }

    private void modifyProjectPropertiesProjectName() {
        EditableProperties props = antHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        props.setProperty(ProjectPropertyNames.PROJECT_PROP_DISPLAYNAME, newName);
        antHelper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
    }

    public Void run() throws Exception {
        // -------- Update project name in project.xml ------------
        Element data = antHelper.getPrimaryConfigurationData(true);
        // XXX replace by XMLUtil when that has findElement, findText, etc.
        NodeList nl = data.getElementsByTagNameNS(
                JCProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name"); //NOI18N
        Element nameEl;
        if (nl.getLength() == 1) {
            nameEl = (Element) nl.item(0);
            NodeList deadKids = nameEl.getChildNodes();
            while (deadKids.getLength() > 0) {
                nameEl.removeChild(deadKids.item(0));
            }
        } else {
            nameEl = data.getOwnerDocument().createElementNS(
                    JCProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name"); //NOI18N
            //NOI18N
            data.insertBefore(nameEl, data.getChildNodes().item(0));
        }
        nameEl.appendChild(data.getOwnerDocument().createTextNode(newName));
        antHelper.putPrimaryConfigurationData(data, true);
        // -------- Update dest.war.name in project.properties
        EditableProperties props = antHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
//        kind.onSetName(props, newName);
        props.setProperty(ProjectPropertyNames.PROJECT_PROP_DISPLAYNAME, newName);
        antHelper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
        return null;
    }

    private void iterDocument(Element root) {
        NodeList nl = root.getChildNodes();
        int len = nl.getLength();
        for (int i = 0; i < len; i++) {
            Node node = nl.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                if ("project".equals(node.getNodeName())) {
                    Element el = (Element) node;
                    el.setAttribute("name", newName);
                }
            }
        }
    }

    //Ant cookie methods needed to invoke clean

    public File getFile() {
        return antHelper.resolveFile("build.xml"); //NOI18N
    }

    public FileObject getFileObject() {
        return FileUtil.toFileObject(FileUtil.normalizeFile(getFile()));
    }

    public Document getDocument() {
        return antHelper.getPrimaryConfigurationData(true).getOwnerDocument();
    }

    public Element getProjectElement() {
        return antHelper.getPrimaryConfigurationData(true);
    }

    public Throwable getParseException() {
        return null;
    }

    public void addChangeListener(ChangeListener arg0) {
    }

    public void removeChangeListener(ChangeListener arg0) {
    }
}
