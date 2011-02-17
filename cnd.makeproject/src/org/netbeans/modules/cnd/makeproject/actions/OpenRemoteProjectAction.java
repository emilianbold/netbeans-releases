/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.makeproject.actions;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileView;
import javax.swing.plaf.basic.BasicInternalFrameTitlePane.TitlePaneLayout;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.cnd.api.remote.RemoteFileUtil;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.makeproject.MakeProject;
import org.netbeans.modules.cnd.makeproject.MakeProjectType;
import org.netbeans.modules.cnd.makeproject.configurations.CommonConfigurationXMLCodec;
import org.netbeans.modules.cnd.utils.ui.ModalMessageDlg;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.remote.api.ui.FileChooserBuilder.JFileChooserEx;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Alexander Simon
 */
public class OpenRemoteProjectAction extends AbstractAction {
    private static final String PROJECT_CONFIGURATION_FILE = "nbproject/configurations.xml"; // NOI18N
    private static final String PROJECT_PRIVATE_CONFIGURATION_FILE = "nbproject/private/configurations.xml"; // NOI18N

    private ServerRecord getDefaultRemoteServerRecord() {
        ServerRecord record = ServerList.getDefaultRecord();
        if (record.isRemote()) {
            return record;
        } else {
            Collection<? extends org.netbeans.modules.cnd.api.remote.ServerRecord> records = ServerList.getRecords();
            for (ServerRecord r : records) {
                if (r.isRemote()) {
                    return r;
                }
            }
        }
        return null;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {       
        final ServerRecord record = getDefaultRemoteServerRecord();
        if (record == null) {
            JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), 
                    NbBundle.getMessage(OpenRemoteProjectAction.class, "OpenRemoteProjectNoHost.text"), 
                    NbBundle.getMessage(OpenRemoteProjectAction.class, "OpenRemoteProjectNoHost.title"), 
                    JOptionPane.ERROR_MESSAGE);
        }
//        if (record.isOffline()) {
            final ModalMessageDlg.LongWorker runner = new ModalMessageDlg.LongWorker() {
                private volatile String homeDir;
                @Override
                public void doWork() {
                    record.validate(true);
                    if (record.isOnline()) {
                        homeDir = getHomeDir(record.getExecutionEnvironment());
                    }
                }
                @Override
                public void doPostRunInEDT() {
                    if (record.isOnline()) {
                        openProject(record, homeDir);
                    }
                }
            };
            Frame mainWindow = WindowManager.getDefault().getMainWindow();
            String title = NbBundle.getMessage(OpenRemoteProjectAction.class, "OpenRemoteProjectAction.comment.title"); //NOI18N
            String msg = NbBundle.getMessage(OpenRemoteProjectAction.class, "OpenRemoteProjectAction.comment.message",record.getDisplayName()); //NOI18N
            ModalMessageDlg.runLongTask(mainWindow, title, msg, runner, null);
//        } else {
//            openProject(record);
//        }
    }

    private String getHomeDir(ExecutionEnvironment env) {
        try {
            return HostInfoUtils.getHostInfo(env).getUserDir();
        } catch (IOException ex) {
            ex.printStackTrace(System.err); // it doesn't make sense to disturb user
        } catch (CancellationException ex) {
            ex.printStackTrace(System.err); // it doesn't make sense to disturb user
        }        
        return null;
    }
            
    private void openProject(ServerRecord record, String homeDir) {
        final ExecutionEnvironment env = record.getExecutionEnvironment();
        Frame mainWindow = WindowManager.getDefault().getMainWindow();
        JFileChooserEx fileChooser = (JFileChooserEx) RemoteFileUtil.createFileChooser(env,
                record.getDisplayName(), NbBundle.getMessage(OpenRemoteProjectAction.class, "OpenRemoteProjectAction.open"), //NOI18N
                JFileChooser.DIRECTORIES_ONLY, null, homeDir, true);
        int ret = fileChooser.showOpenDialog(mainWindow);
        if (ret == JFileChooser.CANCEL_OPTION) {
            return;
        }
        FileObject remoteProjectFO = fileChooser.getSelectedFileObject();
        FileObject nbprojectFO = remoteProjectFO.getFileObject("nbproject"); // NOI18N
        if (nbprojectFO == null) {
            return;
        }
        try {
            String path = ProjectChooser.getProjectsFolder().getAbsolutePath()+"/"+remoteProjectFO.getNameExt() + "-shadow"; //NOI18N
            File destination = new File(path); //NOI18N
            int loop = 0;
            while(true) {
                if (!destination.exists()) {
                    break;
                }
                loop++;
                destination = new File(path + '-' + loop);
            }
            FileObject localProject = FileUtil.createFolder(destination);
            copy(nbprojectFO, localProject, "nbproject"); //NOI18N
            updateProject(remoteProjectFO, localProject, record);
            updateConfiguration(localProject, record);
            updatePrivateConfiguration(localProject, record);
            Project findProject = ProjectManager.getDefault().findProject(localProject);
            if (findProject != null) {
                OpenProjects.getDefault().open(new Project[]{findProject}, false);
            }
        } catch (SAXException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalArgumentException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void updateProject(FileObject remoteProject, FileObject localProject, ServerRecord record) throws IOException, SAXException {
        FileObject fo = localProject.getFileObject(AntProjectHelper.PROJECT_XML_PATH);
        File projXml = FileUtil.toFile(fo);
        Document doc = XMLUtil.parse(new InputSource(projXml.toURI().toString()), false, true, null, null);
        Element root = doc.getDocumentElement();
        if (root != null) {
            NodeList dataList = root.getElementsByTagName(MakeProjectType.PROJECT_CONFIGURATION_NAME);
            if (dataList.getLength() > 0) {
                Node masterConfs = dataList.item(0);
                Element remoteMode = doc.createElement(MakeProject.REMOTE_MODE);
                remoteMode.setTextContent("REMOTE_SOURCES"); //NOI18N
                masterConfs.appendChild(remoteMode);
                Element remoteHost = doc.createElement(MakeProject.REMOTE_FILESYSTEM_HOST);
                remoteHost.setTextContent(ExecutionEnvironmentFactory.toUniqueID(record.getExecutionEnvironment()));
                masterConfs.appendChild(remoteHost);
                Element remoteBaseDir = doc.createElement(MakeProject.REMOTE_FILESYSTEM_BASE_DIR);
                remoteBaseDir.setTextContent(remoteProject.getPath());
                masterConfs.appendChild(remoteBaseDir);
            }
        }
        saveXml(doc, localProject, AntProjectHelper.PROJECT_XML_PATH);
    }

    private void updateConfiguration(FileObject localProject, ServerRecord record) throws IOException, SAXException {
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
                    remoteMode.setTextContent("REMOTE_SOURCES"); //NOI18N
                    node.appendChild(remoteMode);
                    remoteMode = doc.createElement(CommonConfigurationXMLCodec.COMPILER_SET_ELEMENT);
                    remoteMode.setTextContent("default"); //NOI18N
                    node.appendChild(remoteMode);
                }
            }
        }
        saveXml(doc, localProject, PROJECT_CONFIGURATION_FILE);
    }

    private void updatePrivateConfiguration(FileObject localProject, ServerRecord record) throws IOException, SAXException {
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
                    remoteMode.setTextContent(ExecutionEnvironmentFactory.toUniqueID(record.getExecutionEnvironment()));
                    node.appendChild(remoteMode);
                }
            }
        }
        saveXml(doc, localProject, PROJECT_PRIVATE_CONFIGURATION_FILE);
    }

    @Override
    public boolean isEnabled() {
        Collection<? extends org.netbeans.modules.cnd.api.remote.ServerRecord> records = ServerList.getRecords();
        for(ServerRecord record : records) {
            if (record.isRemote()) {
                return true;
            }
        }
        return false;
    }

    private FileObject copy(FileObject base, FileObject proxy, String name) throws IOException {
        if (base.isFolder()) {
            FileObject peer = proxy.createFolder(name);
            FileUtil.copyAttributes(base, peer);
            for (FileObject fo : base.getChildren()) {
                copy(fo, peer, fo.getNameExt());
            }
            return peer;
        } else {
            FileObject dest = copyImpl(base, proxy, name);
            return dest;
        }
    }

    /** Copies file to the selected folder.
     * This implementation simply copies the file by stream content.
    * @param source source file object
    * @param destFolder destination folder
    * @param newName file name (without extension) of destination file
    * @param newExt extension of destination file
    * @return the created file object in the destination folder
    * @exception IOException if <code>destFolder</code> is not a folder or does not exist; the destination file already exists; or
    *      another critical error occurs during copying
    */
    private FileObject copyImpl(FileObject source, FileObject destFolder, String newName)  throws IOException {
        FileObject dest = destFolder.createData(newName);

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
     * Save an XML config file to a named path.
     * If the file does not yet exist, it is created.
     */
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

    private static final class MyFileView extends FileView {
        private final JFileChooser chooser;
        private final Map<File,Icon> knownProjectIcons = new HashMap<File,Icon>();

        public MyFileView(JFileChooser chooser) {
            this.chooser = chooser;
        }

        @Override
        public Icon getIcon(File f) {
            Icon res = knownProjectIcons.get(f);
            if (res == null) {
                res = _getIcon(f);
                knownProjectIcons.put(f, res);
            }
            return res;
        }

        public Icon _getIcon(File f) {
            try {
                if (f != null &&
                    f.isDirectory() && // #173958: do not call ProjectManager.isProject now, could block
                    !f.toString().matches("/[^/]+") && // Unix: /net, /proc, etc. // NOI18N
                    f.getParentFile() != null) { // do not consider drive roots
                    String path = f.getAbsolutePath();
                    String project = path+"/nbproject"; // NOI18N
                    File projectDir = chooser.getFileSystemView().createFileObject(project);
                    if (projectDir.exists() && projectDir.isDirectory() && projectDir.canRead()) {
                        String projectXml = path+"/nbproject/project.xml"; // NOI18N
                        File projectFile = chooser.getFileSystemView().createFileObject(projectXml);
                        if (projectFile.exists()) {
                            String conf = path+"/nbproject/configurations.xml"; // NOI18N
                            File configuration = chooser.getFileSystemView().createFileObject(conf);
                            if (configuration.exists()) {
                                return ImageUtilities.loadImageIcon("org/netbeans/modules/cnd/makeproject/ui/resources/makeProject.gif", true); // NOI18N
                            }
                        }
                    }
                }
            } catch (Throwable t) {
                //
            }
            return chooser.getFileSystemView().getSystemIcon(f);
        }
    }
}
