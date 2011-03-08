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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.cnd.api.remote.RemoteProject;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.makeproject.MakeProject;
import org.netbeans.modules.cnd.makeproject.MakeProjectType;
import org.netbeans.modules.cnd.makeproject.configurations.CommonConfigurationXMLCodec;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils.ExitStatus;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.awt.Notification;
import org.openide.awt.NotificationDisplayer;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author Alexander Simon
 * @author Vladimir Kvashin
 */
public class ShadowProjectSynchronizer {
    
    public static final String DEFAULT = "default"; // NOI18N

    private static final String PROJECT_CONFIGURATION_FILE = "nbproject/configurations.xml"; // NOI18N
    private static final String PROJECT_PRIVATE_CONFIGURATION_FILE = "nbproject/private/configurations.xml"; // NOI18N

    private static final String TMP_NBPROJECT_SUBFOLDER_NAME = "full_remote_tmp"; // NOI18N
    private static final String TMP_NBPROJECT_SUBFOLDER_PATH = "private/" + TMP_NBPROJECT_SUBFOLDER_NAME; // NOI18N

    private final ExecutionEnvironment env;
    private final String remoteProjectPath;
    private final String localProjectPath;
    private FileObject remoteProject;
    private FileObject localProject;
    
    private static final Logger LOGGER = Logger.getLogger("cnd.remote.logger"); //NOI18N

    public ShadowProjectSynchronizer(String remoteProjectPath, String localProjectPath, ExecutionEnvironment env) {
        this.remoteProjectPath = remoteProjectPath;
        this.localProjectPath = localProjectPath;
        this.env = env;
    }
    
    public FileObject createShadowProject() throws IOException, SAXException {
        File localProjectFile = new File(localProjectPath);
        if (localProjectFile.exists()) {
            throw new IOException(NbBundle.getMessage(ShadowProjectSynchronizer.class, "ERR_DirAlreadyExists", localProjectFile.getAbsolutePath()));
        }        
        this.localProject = FileUtil.createFolder(FileUtil.normalizeFile(localProjectFile));

        boolean success = false;
        try {
            this.remoteProject = CndFileUtils.toFileObject(FileSystemProvider.getFileSystem(env), remoteProjectPath);
            if (remoteProject == null || ! remoteProject.isValid()) {
                throw new IOException(NbBundle.getMessage(ShadowProjectSynchronizer.class, "ERR_DirDoesNotExist", remoteProjectPath));
            }
            FileObject remoteNbprojectFO = getFileObject(remoteProject, "nbproject"); // NOI18N
            remoteNbprojectFO.refresh();
            copy(remoteNbprojectFO, localProject, "nbproject", null); //NOI18N
            updateLocalProjectXml();
            updateLocalConfiguration();
            updateLocalPrivateConfiguration();
            success = true;
        } finally {
            if (!success) {
                remove(localProject);
            }
            
        }
        return localProject;
    }
            
    public void updateRemoteProject() throws IOException, SAXException, InterruptedException {
        File localProjectFile = new File(localProjectPath);
        if (!localProjectFile.exists()) {
            throw new FileNotFoundException(NbBundle.getMessage(ShadowProjectSynchronizer.class, "ERR_DirDoesNotExist", localProjectPath));
        }        
        this.localProject = FileUtil.createFolder(FileUtil.normalizeFile(localProjectFile));
        ProgressHandle progress = ProgressHandleFactory.createHandle(
                NbBundle.getMessage(ShadowProjectSynchronizer.class, "Progress_sync", localProject.getNameExt(), env.getDisplayName()));
        progress.start();
        try {
            updateRemoteProjectImpl(progress);
        } finally {
            progress.finish();
        }
    }
    
    private FileObject[] filterChildren(FileObject[] children) {
        for (int i = 0; i < children.length; i++) {
            if (TMP_NBPROJECT_SUBFOLDER_NAME.equals(children[i].getName())) {
                FileObject[] result = new FileObject[children.length - 1];
                if (i == 0) {
                    System.arraycopy(children, 1, result, 0, children.length-1);
                } else {
                    System.arraycopy(children, 0, result, 0, i);
                    System.arraycopy(children, i+1, result, i, children.length-i-1);
                }
                return result;
            }            
        }
        return children;
    }
    
    private boolean differ(FileObject fo1, FileObject fo2) {
        Parameters.notNull("file object 1", fo1);
        Parameters.notNull("file object 2", fo2);
        if (fo1.isValid() != fo2.isValid()) {
            LOGGER.log(Level.FINEST, "PrjDiff: validity differ for {0} and {1}", new Object[] {fo1, fo2});
            return true;
        }
        if (fo1.isFolder()) {
            if (!fo2.isFolder()) {
                LOGGER.log(Level.FINEST, "PrjDiff: {0} is not a folder", fo2);
                return true;
            }
            FileObject[] children1 = filterChildren(fo1.getChildren());
            FileObject[] children2 = filterChildren(fo2.getChildren());
            if (children1.length != children2.length) {
                LOGGER.log(Level.FINEST, "PrjDiff: child count differ for {0} and {1}: {2} vs {3}", new Object[]{fo1, fo2, children1.length, children2.length});
                return true;
            }
            Comparator<FileObject> comparator = new Comparator<FileObject>() {
                @Override
                public int compare(FileObject o1, FileObject o2) {
                    return o1.getNameExt().compareTo(o2.getNameExt());
                }                
            };
            Arrays.sort(children1, comparator);
            Arrays.sort(children2, comparator);
            for (int i = 0; i < children1.length; i++) {
                if (differ(children1[i], children2[i])) {                    
                    return true;
                }
            }
        } else {
            if (fo2.isFolder()) {
                LOGGER.log(Level.FINEST, "PrjDiff: {0} is folder\n", fo2);
                return true;
            }
            long size1 = fo1.getSize();
            long size2 = fo2.getSize();
            if (size1 != size2) {
                LOGGER.log(Level.FINEST, "PrjDiff: size differ for {0} and {1}: {2} vs {3}", new Object[]{fo1, fo2, size1, size2});
                return true;
            }
            BufferedReader reader1 = null, reader2 = null;
            try {
                reader1 = new BufferedReader(new InputStreamReader(fo1.getInputStream()));
                reader2 = new BufferedReader(new InputStreamReader(fo2.getInputStream()));
                int c1;
                while ((c1 = reader1.read()) >= 0) {
                    int c2 = reader2.read();
                    if (c1 != c2) {
                        LOGGER.log(Level.FINEST, "PrjDiff: {0} and {1} differ in content\n", new Object[]{fo1, fo2});
                        return true;
                    }
                }
            } catch (IOException e) {
                // does not make sense to report
                LOGGER.log(Level.FINEST, "PrjDiff: exception: {0} when reading {1} or {2}\n", new Object[]{e.getMessage(), fo1, fo2});
                return true;
            } finally {
                try {
                    if (reader1 != null) {
                        reader1.close();
                    }
                    if (reader2 != null) {
                        reader1.close();
                    }
                } catch (IOException e) {
                    // does not make sense to report
                }
            }
        }
        return false;
    }

    public void updateRemoteProjectImpl(ProgressHandle progress) throws IOException, SAXException, InterruptedException {
        FileObject localNbprojectFO = getFileObject(localProject, "nbproject"); // NOI18N
        
        this.remoteProject = getFileObject(env, remoteProjectPath);
        FileObject remoteNbprojectFO = getFileObject(remoteProject, "nbproject"); // NOI18N

        progress.switchToDeterminate(7);
        
        // Schedule remote temporary directory creation
        AtomicReference<FileObject> remoteTmpFoRef = new AtomicReference<FileObject>();
        final AtomicReference<Boolean> remoteTmpFOCreationSuccess = new AtomicReference<Boolean>(false);
        AtomicReference<String> remoteTmpFOErrMsg = new AtomicReference<String>();
        RequestProcessor.Task remoteTmpFOTask = prepareRemoteNbProject(remoteNbprojectFO, remoteTmpFoRef, 
                remoteTmpFOCreationSuccess, remoteTmpFOErrMsg);
        
        FileObject tmpFO = createTempDir(localProject.getName(), ".tmp"); // NOI18N
        try {
            // Create local temp dir and copy project files there
            progress.progress(NbBundle.getMessage(ShadowProjectSynchronizer.class, "Progress_sync_local_tmp"));
            FileObject tmpNbProjFO = copy(localNbprojectFO, tmpFO, "nbproject", null); //NOI18N
            progress.progress(1);            
            
            // Process files in local temp dir
            progress.progress(NbBundle.getMessage(ShadowProjectSynchronizer.class, "Progress_sync_local_merge"));
            updateRemoteProjectXml(tmpFO);
            updateRemoteConfiguration(tmpFO);
            updateRemotePrivateConfiguration(tmpFO);
            progress.progress(2);
            
            if (!differ(tmpNbProjFO, remoteNbprojectFO)) {
                LOGGER.fine("PrjDiff: Project has not change - no synchronization to remote host is needed");
                return;
            }
            
            // wait for remote temporary directory creation, make sure it's ok
            progress.progress(NbBundle.getMessage(ShadowProjectSynchronizer.class, "Progress_sync_remote_temp"));
            remoteTmpFOTask.waitFinished();
            if (!remoteTmpFOCreationSuccess.get()) {
                throw new IOException(remoteTmpFOErrMsg.get());
            }
            FileObject remoteTmpFo = remoteTmpFoRef.get();
            CndUtils.assertNotNull(remoteTmpFo, "Null remote temp file object"); //NOI18N
            progress.progress(3);

            // copy prepared files from local temp dir to remote tmp dir
            progress.progress(NbBundle.getMessage(ShadowProjectSynchronizer.class, "Progress_sync_copy_to_remote"));
            Collection<FileObject> objectsToTrack = new ArrayList<FileObject>();
            copy(tmpNbProjFO, remoteTmpFo, objectsToTrack); //NOI18N
            Collection<String> failed = new ArrayList<String>();
            FileSystemProvider.waitWrites(env, objectsToTrack, failed);
            progress.progress(4);
            
            // ok, all files are ready and delivered to the remote host
            // now change project content
            progress.progress(NbBundle.getMessage(ShadowProjectSynchronizer.class, "Progress_sync_update_project"));
            ExitStatus moveStatus = ProcessUtils.executeInDir(remoteNbprojectFO.getPath(), env, "/bin/sh", "-c", //NOI18N
                    "mv " + TMP_NBPROJECT_SUBFOLDER_PATH + "/private/* " + "private/; " + //NOI18N
                    "rmdir " + TMP_NBPROJECT_SUBFOLDER_PATH + "/private; " + //NOI18N
                    "mv " + TMP_NBPROJECT_SUBFOLDER_PATH + "/* ."); //NOI18N
            progress.progress(5);
            if (!moveStatus.isOK()) {
                throw new IOException(NbBundle.getMessage(ShadowProjectSynchronizer.class, "ERR_MovingRemoteTmpToProject", moveStatus.error));
            }
            
            progress.progress(NbBundle.getMessage(ShadowProjectSynchronizer.class, "Progress_sync_cleanup"));
            Future<Integer> rmTask = CommonTasksSupport.rmDir(
                    env, remoteNbprojectFO.getPath() + '/' + TMP_NBPROJECT_SUBFOLDER_PATH, true, new PrintWriter(System.err));

            remoteNbprojectFO.refresh();
            progress.progress(6);

            // it's better to wait, otherwise subsequent save might clash with this cleanup
            try {
                int rc = rmTask.get().intValue();
                if (rc != 0) {
                    LOGGER.info("Error cleaning up remote temporary directory");
                }
            } catch (InterruptedException ex) {
                // don't log InterruptedException
            } catch (ExecutionException ex) {
                LOGGER.log(Level.INFO, "Exception when cleaning up remote temporary directory", ex);
            }
        } finally {
            remove(tmpFO);
            progress.progress(7);
        }
    }
    
    private RequestProcessor.Task prepareRemoteNbProject(
            final FileObject remoteNbprojectFO,
            final AtomicReference<FileObject> createdFO,
            final AtomicReference<Boolean> success,
            final AtomicReference<String> errMsg) {
        
        Runnable r = new Runnable() {
            @Override
            public void run() {
                ExitStatus status = ProcessUtils.executeInDir(remoteNbprojectFO.getPath(), env, "/bin/sh", "-c", //NOI18N
                        "mkdir -p " + TMP_NBPROJECT_SUBFOLDER_PATH + " && " + //NOI18N
                        "rm -rf " + TMP_NBPROJECT_SUBFOLDER_PATH + "/*; "); //NOI18N
                if (status.isOK()) {
                    remoteNbprojectFO.refresh();
                    FileObject fo = remoteNbprojectFO.getFileObject(TMP_NBPROJECT_SUBFOLDER_PATH);
                    if (fo == null) {
                        success.set(false);
                        errMsg.set(NbBundle.getMessage(ShadowProjectSynchronizer.class, "ERR_RemoteTmpDirNotFound"));
                    } else {
                        createdFO.set(fo);
                        success.set(true);
                    }
                } else {
                    errMsg.set(status.error);
                    success.set(false);
                }
            }
        };
        return new RequestProcessor("Preparing to sync back project " + remoteProjectPath, 1).post(r);
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
    
    private static FileObject updateRemoteProjectXml(FileObject remoteProjectCopy) throws IOException, SAXException {
        FileObject projectXmlFO = getFileObject(remoteProjectCopy, AntProjectHelper.PROJECT_XML_PATH);
        Document doc = XMLUtil.parse(new InputSource(projectXmlFO.getInputStream()), false, true, null, null);
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
        return saveXml(doc, remoteProjectCopy, AntProjectHelper.PROJECT_XML_PATH);        
    }

    private static String getOrigCompilerSet(FileObject remoteProjectOrig) throws IOException, SAXException {
        FileObject origPublicConfigurationsFO = getFileObject(remoteProjectOrig, PROJECT_CONFIGURATION_FILE);
        Document origDoc = XMLUtil.parse(new InputSource(origPublicConfigurationsFO.getInputStream()), false, true, null, null);
        Element origRoot = origDoc.getDocumentElement();
        return getOrigCompilerSet(origRoot);
    }

    private static String getOrigCompilerSet(Element origRoot) throws IOException, SAXException {
        String origCsName = DEFAULT; //NOI18N
        NodeList origCSList = origRoot.getElementsByTagName(CommonConfigurationXMLCodec.COMPILER_SET_ELEMENT);
        if (origCSList.getLength() > 0) {
            Node origCsNode = origCSList.item(0);
            if (origCsNode.getChildNodes().getLength() >0) {
                origCsName = getSimpleNodeValue(origCsNode);
            }
        }
        return origCsName;
    }
    
    private static String getSimpleNodeValue(Node node) {
        if (node != null) {
            NodeList childNodes = node.getChildNodes();
            if (childNodes.getLength() > 0) {
                return childNodes.item(0).getNodeValue();
            }
        }
        return null;
    }
    
    private static FileObject getFileObject(ExecutionEnvironment env, String absPath) throws FileNotFoundException {
        FileObject fo = CndFileUtils.toFileObject(FileSystemProvider.getFileSystem(env), absPath);
        if (fo == null || ! fo.isValid()) {
            String text = env.getDisplayName() + ':' + absPath; //NOI18N
            throw new FileNotFoundException(NbBundle.getMessage(ShadowProjectSynchronizer.class, "ERR_DirDoesNotExist", text));
        }        
        return fo;
    }

    private static FileObject getFileObject(FileObject base, String relPath) throws FileNotFoundException {
        FileObject fo = base.getFileObject(relPath);
        if (fo == null || ! fo.isValid()) {
            String text;
            try {
                text = base.getURL().toString();
                text += (text.endsWith("/") ? "" : "/") + relPath; //NOI18N
            } catch (FileStateInvalidException ex) {
                text = base.getPath() + '/' + relPath; //NOI18N
            }
            throw new FileNotFoundException(NbBundle.getMessage(ShadowProjectSynchronizer.class, "ERR_DirDoesNotExist", text));
        }
        return fo;
    }
    
    private FileObject updateRemoteConfiguration(FileObject remoteProjectCopy) throws IOException, SAXException {
        String origCsName = getOrigCompilerSet(remoteProject);
        FileObject fo = getFileObject(remoteProjectCopy, PROJECT_CONFIGURATION_FILE);
        Document doc = XMLUtil.parse(new InputSource(fo.getInputStream()), false, true, null, null);
        Element root = doc.getDocumentElement();
        if (root != null) {
            NodeList toolSetList = root.getElementsByTagName(CommonConfigurationXMLCodec.TOOLS_SET_ELEMENT);
            for(int i = 0; i < toolSetList.getLength(); i++) {
                Node node = toolSetList.item(i);
                removeChildren(node);
                Element el = doc.createElement(MakeProject.REMOTE_MODE);
                el.setTextContent(RemoteProject.Mode.LOCAL_SOURCES.name());
                node.appendChild(el);
                el = doc.createElement(CommonConfigurationXMLCodec.COMPILER_SET_ELEMENT);
                el.setTextContent(origCsName); //NOI18N
                node.appendChild(el);
            }
        }
        return saveXml(doc, remoteProjectCopy, PROJECT_CONFIGURATION_FILE);
    }

    private Map<String, ServerAndPlatform> getOrigPlatforms() throws IOException, SAXException {
        Map<String, ServerAndPlatform> compilerSets = new HashMap<String, ServerAndPlatform>();
        FileObject origPublicConfigurationsFO = getFileObject(remoteProject, PROJECT_PRIVATE_CONFIGURATION_FILE);
        Document doc = XMLUtil.parse(new InputSource(origPublicConfigurationsFO.getInputStream()), false, true, null, null);
        for (Node conf : getIterableByTagName(doc.getDocumentElement(), CommonConfigurationXMLCodec.CONF_ELEMENT)) {
            NamedNodeMap attrs = conf.getAttributes();
            String confName = attrs.getNamedItem("name").getNodeValue();
            for (Node toolSetNode : getIterableByTagName(conf, CommonConfigurationXMLCodec.TOOLS_SET_ELEMENT)) {
                Node serverNode = getNodeByTagName(toolSetNode, CommonConfigurationXMLCodec.DEVELOPMENT_SERVER_ELEMENT);
                Node platformNode = getNodeByTagName(toolSetNode, CommonConfigurationXMLCodec.PLATFORM_ELEMENT);
                ServerAndPlatform sp = new ServerAndPlatform(getSimpleNodeValue(serverNode), getSimpleNodeValue(platformNode));
                compilerSets.put(confName, sp);
            }
        }
        return compilerSets;
    }
    
    private FileObject updateRemotePrivateConfiguration(FileObject remoteProjectCopy) throws IOException, SAXException {
        Map<String, ServerAndPlatform> origPlatforms = getOrigPlatforms();
        FileObject fo = getFileObject(remoteProjectCopy, PROJECT_PRIVATE_CONFIGURATION_FILE);
        Document doc = XMLUtil.parse(new InputSource(fo.getInputStream()), false, true, null, null);
        for (Node conf : getIterableByTagName(doc.getDocumentElement(), CommonConfigurationXMLCodec.CONF_ELEMENT)) {
            NamedNodeMap attrs = conf.getAttributes();
            String confName = attrs.getNamedItem("name").getNodeValue();
            ServerAndPlatform sp = origPlatforms.get(confName);
            if (sp == null) {
                sp = new ServerAndPlatform(ExecutionEnvironmentFactory.toUniqueID(ExecutionEnvironmentFactory.getLocal()), 
                        Integer.toString(getPlatform(env)));
            }
            for (Node node : getIterableByTagName(conf, CommonConfigurationXMLCodec.TOOLS_SET_ELEMENT)) {
                removeChildren(node);
                Element remoteMode;
                remoteMode = doc.createElement(CommonConfigurationXMLCodec.DEVELOPMENT_SERVER_ELEMENT);
                remoteMode.setTextContent(sp.server);
                node.appendChild(remoteMode);
                remoteMode = doc.createElement(CommonConfigurationXMLCodec.PLATFORM_ELEMENT);
                remoteMode.setTextContent(sp.platform);
                node.appendChild(remoteMode);
            }
        }
        return saveXml(doc, remoteProjectCopy, PROJECT_PRIVATE_CONFIGURATION_FILE);
    }

    private FileObject updateLocalProjectXml() throws IOException, SAXException {
        FileObject fo = getFileObject(localProject, AntProjectHelper.PROJECT_XML_PATH);
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
        return saveXml(doc, localProject, AntProjectHelper.PROJECT_XML_PATH);
    }

    private FileObject updateLocalConfiguration() throws IOException, SAXException {
        FileObject fo = getFileObject(localProject, PROJECT_CONFIGURATION_FILE);
        File confXml = FileUtil.toFile(fo);
        Document doc = XMLUtil.parse(new InputSource(confXml.toURI().toString()), false, true, null, null);
        Element root = doc.getDocumentElement();
        if (root != null) {
            String csNameAndFlavor;
            String origCsNameAndFlavor = getOrigCompilerSet(root); //NOI18N
            if (DEFAULT.equals(origCsNameAndFlavor)) {
                csNameAndFlavor = DEFAULT;
            } else {
                CompilerSetManager csManager = CompilerSetManager.get(env);
                int pos = origCsNameAndFlavor.indexOf('|');            
                String origCsName = (pos > 0) ? origCsNameAndFlavor.substring(0, pos) : origCsNameAndFlavor;            
                CompilerSet existentCompilerSet = csManager.getCompilerSet(origCsName);                
                if (existentCompilerSet == null) {
                    csNameAndFlavor = DEFAULT; //NOI18N
                    reportNotFoundCompilerSet(origCsName);
                } else {
                    csNameAndFlavor = origCsNameAndFlavor;
                }
            }
            NodeList toolSetList = root.getElementsByTagName(CommonConfigurationXMLCodec.TOOLS_SET_ELEMENT);
            if (toolSetList.getLength() > 0) {
                for(int i = 0; i < toolSetList.getLength(); i++) {
                    Node node = toolSetList.item(i);
                    removeChildren(node);
                    Element remoteMode = doc.createElement(CommonConfigurationXMLCodec.FIXED_SYNC_FACTORY_ELEMENT);
                    remoteMode.setTextContent("full"); //NOI18N
                    node.appendChild(remoteMode);
                    remoteMode = doc.createElement(MakeProject.REMOTE_MODE);
                    remoteMode.setTextContent(RemoteProject.Mode.REMOTE_SOURCES.name());
                    node.appendChild(remoteMode);
                    remoteMode = doc.createElement(CommonConfigurationXMLCodec.COMPILER_SET_ELEMENT);
                    remoteMode.setTextContent(csNameAndFlavor);
                    node.appendChild(remoteMode);
                }
            }
        }
        return saveXml(doc, localProject, PROJECT_CONFIGURATION_FILE);
    }

    private void reportNotFoundCompilerSet(String origCsName) {
        String title = NbBundle.getMessage(ShadowProjectSynchronizer.class, "ERR_CS_Title", remoteProject.getName());
        ImageIcon icon = ImageUtilities.loadImageIcon("org/netbeans/modules/cnd/makeproject/ui/resources/exclamation.gif", false); // NOI18N
        String details = NbBundle.getMessage(ShadowProjectSynchronizer.class, "ERR_CS_Details", origCsName);
        Notification n = NotificationDisplayer.getDefault().notify(title, icon, details, null, NotificationDisplayer.Priority.HIGH);
    }

    private FileObject updateLocalPrivateConfiguration() throws IOException, SAXException {
        FileObject fo = getFileObject(localProject, PROJECT_PRIVATE_CONFIGURATION_FILE);
        File confXml = FileUtil.toFile(fo);
        Document doc = XMLUtil.parse(new InputSource(confXml.toURI().toString()), false, true, null, null);
        Element root = doc.getDocumentElement();
        if (root != null) {
            NodeList toolSetList = root.getElementsByTagName(CommonConfigurationXMLCodec.TOOLS_SET_ELEMENT);
            if (toolSetList.getLength() > 0) {
                for(int i = 0; i < toolSetList.getLength(); i++) {
                    Node node = toolSetList.item(i);
                    removeChildren(node);
                    Element remoteMode = doc.createElement(CommonConfigurationXMLCodec.DEVELOPMENT_SERVER_ELEMENT);
                    remoteMode.setTextContent(ExecutionEnvironmentFactory.toUniqueID(env));
                    node.appendChild(remoteMode);
                    Element platformElement = doc.createElement(CommonConfigurationXMLCodec.PLATFORM_ELEMENT);
                    platformElement.setTextContent(Integer.toString(getPlatform(env)));
                    node.appendChild(platformElement);
                }
            }
        }
        return saveXml(doc, localProject, PROJECT_PRIVATE_CONFIGURATION_FILE);
    }

    private static int getPlatform(ExecutionEnvironment env) {
        return CompilerSetManager.get(env).getPlatform();
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
            throw new IOException("Can not create " + (folder ? "folder " : "file ") + name + " in " + dstParent); //NOI18N
        }
        return fo;
    }
            
    private FileObject copy(FileObject src, FileObject dst, Collection<FileObject> createdFileObjects) throws IOException {
        return copy(src, dst.getParent(), dst.getNameExt(), createdFileObjects);
    }
    
    private FileObject copy(FileObject src, FileObject dstParent, String name, Collection<FileObject> createdFileObjects) throws IOException {
        if (src.isFolder()) {
            FileObject dst = getOrCreateFileObject(dstParent, name, true);
            FileUtil.copyAttributes(src, dst);
            if (createdFileObjects != null && dst != null) {
                createdFileObjects.add(dst);
            }
            for (FileObject fo : src.getChildren()) {
                if (!TMP_NBPROJECT_SUBFOLDER_NAME.equals(fo.getNameExt())) {
                    FileObject copiedFO = copy(fo, dst, fo.getNameExt(), createdFileObjects);
                    if (createdFileObjects != null && copiedFO != null) {
                        createdFileObjects.add(copiedFO);
                    }
                }
            }
            return dst;
        } else {
            FileObject dest = copyImpl(src, dstParent, name);
            if (createdFileObjects != null && dest != null) {
                createdFileObjects.add(dest);
            }
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
    private static FileObject saveXml(Document doc, FileObject dir, String path) throws IOException {
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
        return xml;
    }

    private static void removeChildren(Node node) {
        NodeList childNodes = node.getChildNodes();
        List<Node> list = new ArrayList<Node>();
        for(int j = 0; j < childNodes.getLength(); j++) {
            list.add(childNodes.item(j));
        }
        for(Node n : list) {
            node.removeChild(n);
        }
    }
    
    private static Node getNodeByTagName(Node node, String tagName) {
        Iterator<Node> it = getIteratorByTagName(node, tagName);
        if (it.hasNext()) {
            Node result = it.next();
            if (it.hasNext()) {
                LOGGER.log(Level.WARNING, "More than one node of type {0}", tagName); //NOI18N
            }
            return result;
        }
        return null;
    }
    
    private static Iterator<Node> getIteratorByTagName(Node node, String tagName) {
        if (node != null && node.getNodeType() == Node.ELEMENT_NODE) {
            return new NodeListIterator(((Element) node).getElementsByTagName(tagName));
        }
        return new EmptyIterator<Node>();
    }

    private static NodeListIterable getIterableByTagName(Node node, String tagName) {
        if (node != null && node.getNodeType() == Node.ELEMENT_NODE) {
            return new NodeListIterable(((Element) node).getElementsByTagName(tagName));
        }
        return new NodeListIterable(null);
    }
    
    private static class NodeListIterable implements Iterable<Node> {

        private final NodeList nodeList;

        public NodeListIterable(NodeList nodeList) {
            this.nodeList = nodeList;
        }
        
        @Override
        public Iterator<Node> iterator() {
            return (nodeList == null) ? new EmptyIterator<Node>() : new NodeListIterator(nodeList);
        }
    }

    private static class ServerAndPlatform {
        public final String server;
        public final String platform;
        
        public ServerAndPlatform(String server, String platform) {
            this.server = server;
            this.platform = platform;
        }
        
        @Override
        public String toString() {
            return server + ',' + platform;
        }        
    }
    
    private static class NodeListIterator implements Iterator<Node> {
        
        private final NodeList nodeList;
        private int curr = 0;

        public NodeListIterator(NodeList nodeList) {
            this.nodeList = nodeList;
        }

        @Override
        public boolean hasNext() {
            return curr < nodeList.getLength();
        }

        @Override
        public Node next() {
            return nodeList.item(curr++);
        }

        @Override
        public void remove() {
            throw new IllegalStateException();
        }
    }

    private static class EmptyIterator<E> implements Iterator<E> {
        @Override
        public boolean hasNext() {
            return false;
        }
        @Override
        public E next() {
            throw new NoSuchElementException();
        }
        @Override
        public void remove() {
            throw new IllegalStateException();
        }
    }    
}
