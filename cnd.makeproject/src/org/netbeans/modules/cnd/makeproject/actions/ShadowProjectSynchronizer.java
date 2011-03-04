/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.makeproject.actions;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.IIOException;
import org.netbeans.modules.cnd.api.remote.RemoteProject;
import org.netbeans.modules.cnd.makeproject.MakeProject;
import org.netbeans.modules.cnd.makeproject.MakeProjectType;
import org.netbeans.modules.cnd.makeproject.configurations.CommonConfigurationXMLCodec;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author Alexander Simon
 * @author Vladimir Kvashin
 */
public class ShadowProjectSynchronizer {

    private static final String PROJECT_CONFIGURATION_FILE = "nbproject/configurations.xml"; // NOI18N
    private static final String PROJECT_PRIVATE_CONFIGURATION_FILE = "nbproject/private/configurations.xml"; // NOI18N
    
    private final FileObject remoteProject;
    private final FileObject localProject;
    private final ExecutionEnvironment env;
    
    private static final Logger LOGGER = Logger.getLogger("cnd.remote.logger"); //NOI18N

    public ShadowProjectSynchronizer(FileObject remoteProject, FileObject localProject, ExecutionEnvironment env) {
        this.remoteProject = remoteProject;
        this.localProject = localProject;
        this.env = env;
    }
    
    public void createShadowProject() throws IOException, SAXException {
        FileObject remoteNbprojectFO = remoteProject.getFileObject("nbproject"); // NOI18N
        copy(remoteNbprojectFO, localProject, "nbproject"); //NOI18N
        updateLocalProjectXml();
        updateLocalConfiguration();
        updateLocalPrivateConfiguration();
    }
    
    public void updateRemoteProject() throws IOException, SAXException {
        FileObject localNbprojectFO = localProject.getFileObject("nbproject"); // NOI18N
        if (localNbprojectFO == null || ! localNbprojectFO.isValid()) {
            return;
        }
        FileObject tmpFO = createTempDir(localProject.getName(), ".tmp"); // NOI18N
        copy(localNbprojectFO, tmpFO, "nbproject"); //NOI18N        
        try {
            FileObject tmpNbProjFO = copy(localNbprojectFO, tmpFO, "nbproject"); //NOI18N
            updateRemoteProjectXml(tmpFO);
            updateRemoteConfiguration(tmpFO, remoteProject);
            updateRemotePrivateConfiguration(tmpFO);
            FileObject remoteNbprojectFO = remoteProject.getFileObject("nbproject"); // NOI18N
            remoteNbprojectFO.refresh();
            copy(tmpNbProjFO, remoteProject, "nbproject"); //NOI18N
        } finally {
            remove(tmpFO);
        }
    }
    
    private static FileObject createTempDir(String prefix, String suffix) throws IOException {
        File tmpFile = FileUtil.normalizeFile(File.createTempFile(prefix, suffix));
        if (!tmpFile.delete()) {
            throw new IOException("Can not delete temporary file " + tmpFile.getAbsolutePath()); //NOI18N
        }
        if (!tmpFile.mkdirs()) {
            throw new IOException("Can create temporary directory " + tmpFile.getAbsolutePath()); //NOI18N
        }
        FileObject tmpFO = FileUtil.toFileObject(tmpFile);
        return tmpFO;
    }
    
    private boolean remove(FileObject fo) {
        if (fo.isFolder()) {
            boolean success = true;
            for (FileObject child : fo.getChildren()) {
                if (!remove(child)) {
                    success = false;
                }
            }
            if (success) {
                try {
                    fo.delete();
                } catch (IOException ex) {
                    success = false;
                    LOGGER.log(Level.INFO, "Exception when performing cleanup in " + fo.getPath(), ex);
                }
            }
            return success;
        } else {
            try {
                fo.delete();
                return true;
            } catch (IOException ex) {                
                LOGGER.log(Level.INFO, "Exception when performing cleanup in " + fo.getPath(), ex);
                return false;
            }
        }
    }
    
    private static void updateRemoteProjectXml(FileObject remoteProjectCopy) throws IOException, SAXException {
        FileObject projectFO = remoteProjectCopy.getFileObject(AntProjectHelper.PROJECT_XML_PATH);
        Document doc = XMLUtil.parse(new InputSource(projectFO.getInputStream()), false, true, null, null);
        Element root = doc.getDocumentElement();
        if (root != null) {
            String[] tagsToRemove = new String[] { 
                MakeProject.REMOTE_MODE, 
                MakeProject.REMOTE_FILESYSTEM_HOST, 
                MakeProject.REMOTE_FILESYSTEM_BASE_DIR };
            
            for (String tag : tagsToRemove) {
                NodeList nodeList = root.getElementsByTagName(tag);
                for (int i = 0; i < nodeList.getLength(); i++) {
                    Node node = nodeList.item(i);
                    node.getParentNode().removeChild(node);
                }
            }
        }
        saveXml(doc, remoteProjectCopy, AntProjectHelper.PROJECT_XML_PATH);
    }

    private static void updateRemoteConfiguration(FileObject remoteProjectCopy, FileObject remoteProjectOrig) throws IOException, SAXException {

        String origCsName = "default"; //NOI18N
        {
            FileObject origPublicConfigurationsFO = remoteProjectOrig.getFileObject(PROJECT_CONFIGURATION_FILE);
            Document origDoc = XMLUtil.parse(new InputSource(origPublicConfigurationsFO.getInputStream()), false, true, null, null);
            Element origRoot = origDoc.getDocumentElement();
            NodeList origCSList = origRoot.getElementsByTagName(CommonConfigurationXMLCodec.COMPILER_SET_ELEMENT);
            if (origCSList.getLength() > 0) {
                Node origCsNode = origCSList.item(0);
                if (origCsNode.getChildNodes().getLength() >0) {
                    origCsName = origCsNode.getChildNodes().item(0).getNodeValue();
                }
            }
        }
        
        FileObject fo = remoteProjectCopy.getFileObject(PROJECT_CONFIGURATION_FILE);
        Document doc = XMLUtil.parse(new InputSource(fo.getInputStream()), false, true, null, null);
        Element root = doc.getDocumentElement();
        if (root != null) {
            NodeList toolSetList = root.getElementsByTagName(CommonConfigurationXMLCodec.TOOLS_SET_ELEMENT);
            for(int i = 0; i < toolSetList.getLength(); i++) {
                Node node = toolSetList.item(i);
                NodeList childNodes = node.getChildNodes();
                List<Node> list = new ArrayList<Node>();
                for(int j = 0; j < childNodes.getLength(); j++) {
                    list.add(childNodes.item(j));
                }
                for(Node n : list) {
                    node.removeChild(n);
                }
                Element el = doc.createElement(MakeProject.REMOTE_MODE);
                el.setTextContent(RemoteProject.Mode.LOCAL_SOURCES.name());
                node.appendChild(el);
                el = doc.createElement(CommonConfigurationXMLCodec.COMPILER_SET_ELEMENT);
                el.setTextContent(origCsName); //NOI18N
                node.appendChild(el);
            }
        }
        saveXml(doc, remoteProjectCopy, PROJECT_CONFIGURATION_FILE);
    }
    
    private static void updateRemotePrivateConfiguration(FileObject remoteProjectCopy) throws IOException, SAXException {
        FileObject fo = remoteProjectCopy.getFileObject(PROJECT_PRIVATE_CONFIGURATION_FILE);
        Document doc = XMLUtil.parse(new InputSource(fo.getInputStream()), false, true, null, null);
        Element root = doc.getDocumentElement();
        if (root != null) {
            NodeList toolSetList = root.getElementsByTagName(CommonConfigurationXMLCodec.TOOLS_SET_ELEMENT);
            if (toolSetList.getLength() > 0) {
                for(int i = 0; i < toolSetList.getLength(); i++) {
                    Node node = toolSetList.item(i);
                    NodeList childNodes = node.getChildNodes();
                    List<Node> list = new ArrayList<Node>();
                    for(int j = 0; j < childNodes.getLength(); j++) {
                        list.add(childNodes.item(j));
                    }
                    for(Node n : list) {
                        node.removeChild(n);
                    }
                    Element remoteMode = doc.createElement(CommonConfigurationXMLCodec.DEVELOPMENT_SERVER_ELEMENT);
                    remoteMode.setTextContent(ExecutionEnvironmentFactory.toUniqueID(ExecutionEnvironmentFactory.getLocal()));
                    node.appendChild(remoteMode);
                }
            }
        }
        saveXml(doc, remoteProjectCopy, PROJECT_PRIVATE_CONFIGURATION_FILE);
    }

    private void updateLocalProjectXml() throws IOException, SAXException {
        FileObject fo = localProject.getFileObject(AntProjectHelper.PROJECT_XML_PATH);
        File projXml = FileUtil.toFile(fo);
        Document doc = XMLUtil.parse(new InputSource(projXml.toURI().toString()), false, true, null, null);
        Element root = doc.getDocumentElement();
        if (root != null) {
            NodeList dataList = root.getElementsByTagName(MakeProjectType.PROJECT_CONFIGURATION_NAME);
            if (dataList.getLength() > 0) {
                Node masterConfs = dataList.item(0);
                Element remoteMode = doc.createElement(MakeProject.REMOTE_MODE);
                remoteMode.setTextContent(RemoteProject.Mode.REMOTE_SOURCES.name());
                masterConfs.appendChild(remoteMode);
                Element remoteHost = doc.createElement(MakeProject.REMOTE_FILESYSTEM_HOST);
                remoteHost.setTextContent(ExecutionEnvironmentFactory.toUniqueID(env));
                masterConfs.appendChild(remoteHost);
                Element remoteBaseDir = doc.createElement(MakeProject.REMOTE_FILESYSTEM_BASE_DIR);
                remoteBaseDir.setTextContent(remoteProject.getPath());
                masterConfs.appendChild(remoteBaseDir);
            }
        }
        saveXml(doc, localProject, AntProjectHelper.PROJECT_XML_PATH);
    }

    private void updateLocalConfiguration() throws IOException, SAXException {
        FileObject fo = localProject.getFileObject(PROJECT_CONFIGURATION_FILE);
        File confXml = FileUtil.toFile(fo);
        Document doc = XMLUtil.parse(new InputSource(confXml.toURI().toString()), false, true, null, null);
        Element root = doc.getDocumentElement();
        if (root != null) {
            NodeList toolSetList = root.getElementsByTagName(CommonConfigurationXMLCodec.TOOLS_SET_ELEMENT);
            if (toolSetList.getLength() > 0) {
                for(int i = 0; i < toolSetList.getLength(); i++) {
                    Node node = toolSetList.item(i);
                    NodeList childNodes = node.getChildNodes();
                    List<Node> list = new ArrayList<Node>();
                    for(int j = 0; j < childNodes.getLength(); j++) {
                        list.add(childNodes.item(j));
                    }
                    for(Node n : list) {
                        node.removeChild(n);
                    }
                    Element remoteMode = doc.createElement(CommonConfigurationXMLCodec.FIXED_SYNC_FACTORY_ELEMENT);
                    remoteMode.setTextContent("full"); //NOI18N
                    node.appendChild(remoteMode);
                    remoteMode = doc.createElement(MakeProject.REMOTE_MODE);
                    remoteMode.setTextContent(RemoteProject.Mode.REMOTE_SOURCES.name());
                    node.appendChild(remoteMode);
                    remoteMode = doc.createElement(CommonConfigurationXMLCodec.COMPILER_SET_ELEMENT);
                    remoteMode.setTextContent("default"); //NOI18N
                    node.appendChild(remoteMode);
                }
            }
        }
        saveXml(doc, localProject, PROJECT_CONFIGURATION_FILE);
    }

    private void updateLocalPrivateConfiguration() throws IOException, SAXException {
        FileObject fo = localProject.getFileObject(PROJECT_PRIVATE_CONFIGURATION_FILE);
        File confXml = FileUtil.toFile(fo);
        Document doc = XMLUtil.parse(new InputSource(confXml.toURI().toString()), false, true, null, null);
        Element root = doc.getDocumentElement();
        if (root != null) {
            NodeList toolSetList = root.getElementsByTagName(CommonConfigurationXMLCodec.TOOLS_SET_ELEMENT);
            if (toolSetList.getLength() > 0) {
                for(int i = 0; i < toolSetList.getLength(); i++) {
                    Node node = toolSetList.item(i);
                    NodeList childNodes = node.getChildNodes();
                    List<Node> list = new ArrayList<Node>();
                    for(int j = 0; j < childNodes.getLength(); j++) {
                        list.add(childNodes.item(j));
                    }
                    for(Node n : list) {
                        node.removeChild(n);
                    }
                    Element remoteMode = doc.createElement(CommonConfigurationXMLCodec.DEVELOPMENT_SERVER_ELEMENT);
                    remoteMode.setTextContent(ExecutionEnvironmentFactory.toUniqueID(env));
                    node.appendChild(remoteMode);
                }
            }
        }
        saveXml(doc, localProject, PROJECT_PRIVATE_CONFIGURATION_FILE);
    }

    private static FileObject getOrCreateFileObject(FileObject dstParent, String name, boolean folder) throws IOException {
        FileObject fo = dstParent.getFileObject(name);
        if (fo != null && fo.isValid() && (fo.isFolder() != folder)) {
            fo.delete();
            fo = null;
        }
        if (fo == null || ! fo.isValid()) {
            try {
                fo = folder ? dstParent.createFolder(name) : dstParent.createData(name);
            } catch (IOException ex) {
                dstParent.refresh();
                // it might happen that it has been already created; check this
                fo = dstParent.getFileObject(name);
                if (fo == null || ! fo.isValid() && (fo.isFolder() != folder)) {
                    throw ex;
                }
            }
        }
        if (fo == null || ! fo.isValid()) {
            throw new IIOException("Can not create " + (folder ? "folder " : "file ") + name + " in " + dstParent); //NOI18N
        }
        return fo;
    }
            
    private FileObject copy(FileObject src, FileObject dstParent, String name) throws IOException {
        if (src.isFolder()) {
            FileObject dst = getOrCreateFileObject(dstParent, name, true);
            FileUtil.copyAttributes(src, dst);
            for (FileObject fo : src.getChildren()) {
                copy(fo, dst, fo.getNameExt());
            }
            return dst;
        } else {
            FileObject dest = copyImpl(src, dstParent, name);
            return dest;
        }
    }

    /** Copies file to the selected folder.
     * This implementation simply copies the file by stream content.
    * @param source source file object
    * @param destFolder destination folder
    * @param newName file name (without extension) of destination file
    * @return the created file object in the destination folder
    * @exception IOException if <code>destFolder</code> is not a folder or does not exist; the destination file already exists; or
    *      another critical error occurs during copying
    */
    private FileObject copyImpl(FileObject source, FileObject destFolder, String newName)  throws IOException {
        FileObject dest = getOrCreateFileObject(destFolder, newName, false);
        FileLock lock = null;
        InputStream bufIn = null;
        OutputStream bufOut = null;

        try {
            lock = dest.lock();
            bufIn = source.getInputStream();
            bufOut = dest.getOutputStream(lock);
            FileUtil.copy(bufIn, bufOut);
            FileUtil.copyAttributes(source, dest);
        } finally {
            if (bufIn != null) {
                bufIn.close();
            }
            if (bufOut != null) {
                bufOut.close();
            }
            if (lock != null) {
                lock.releaseLock();
            }
        }
        return dest;
    }

    /**
     * Save an XML configuration file to a named path.
     * If the file does not yet exist, it is created.
     */
    private static void saveXml(Document doc, FileObject dir, String path) throws IOException {
        FileObject xml = getOrCreateFileObject(dir, path, false);
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
}
