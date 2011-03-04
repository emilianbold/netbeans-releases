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

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileView;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.cnd.api.remote.RemoteFileUtil;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.makeproject.ui.wizards.RemoteProjectImportWizard;
import org.netbeans.modules.cnd.makeproject.ui.wizards.RemoteProjectImportWizard.ImportedProject;
import org.netbeans.modules.cnd.utils.ui.ModalMessageDlg;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.remote.api.ui.FileChooserBuilder.JFileChooserEx;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.WindowManager;
import org.xml.sax.SAXException;

/**
 * @author Alexander Simon
 * @author Vladimir Kvashin
 */
@ActionID(id = "org.netbeans.modules.cnd.makeproject.actions.OpenRemoteProjectAction", category = "Project")
@ActionRegistration(iconInMenu = false, displayName = "#CTL_ImportProjectMenuItem")
@ActionReference(path = "Menu/File/Import", position = 3000)
public class OpenRemoteProjectAction implements ActionListener {

    private static Map<ExecutionEnvironment, String> lastUsedDirs = new HashMap<ExecutionEnvironment, String>();
    
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
            return;
        }
        RemoteProjectImportWizard wizard = new RemoteProjectImportWizard();
        wizard.start();
        List<ImportedProject> projectsToImport = wizard.getProjectsToImport();
        if (wizard.isCancelled() || projectsToImport == null || projectsToImport.isEmpty()) {
            return;
        }
//        if (record.isOffline()) {
            final ModalMessageDlg.LongWorker runner = new ModalMessageDlg.LongWorker() {
                private volatile String homeDir;
                @Override
                public void doWork() {
                    record.validate(true);
                    homeDir = lastUsedDirs.get(record.getExecutionEnvironment());
                    if (homeDir == null && record.isOnline()) {
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
                NbBundle.getMessage(OpenRemoteProjectAction.class, "OpenRemoteProjectAction.title"),//NOI18N
                NbBundle.getMessage(OpenRemoteProjectAction.class, "OpenRemoteProjectAction.open"), //NOI18N
                JFileChooser.DIRECTORIES_ONLY, null, homeDir, true);
        fileChooser.setFileView(new MyFileView(fileChooser));
        int ret = fileChooser.showOpenDialog(mainWindow);
        if (ret == JFileChooser.CANCEL_OPTION) {
            return;
        }
        FileObject remoteProjectFO = fileChooser.getSelectedFileObject();
        if (remoteProjectFO == null) {
            return;
        }
        lastUsedDirs.put(env, remoteProjectFO.getParent().getPath());
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
            ShadowProjectSynchronizer synchronizer = new ShadowProjectSynchronizer(remoteProjectFO, localProject, record.getExecutionEnvironment());
            synchronizer.createShadowProject();
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

    private static final class MyFileView extends FileView implements Runnable {
        private final JFileChooser chooser;
        private final Map<File,Icon> knownProjectIcons = new HashMap<File,Icon>();
        private final RequestProcessor.Task task = new RequestProcessor("ProjectIconFileView").create(this);//NOI18N
        private File lookingForIcon;
        
        public MyFileView(JFileChooser chooser) {
            this.chooser = chooser;
        }

        @Override
        public Icon getIcon(File f) {
            if (f.isDirectory() && // #173958: do not call ProjectManager.isProject now, could block
                    !f.toString().matches("/[^/]+") && // Unix: /net, /proc, etc. //NOI18N
                    f.getParentFile() != null) { // do not consider drive roots
                synchronized (this) {
                    Icon icon = knownProjectIcons.get(f);
                    if (icon != null) {
                        return icon;
                    } else if (lookingForIcon == null) {
                        lookingForIcon = f;
                        task.schedule(20);
                        // Only calculate one at a time.
                        // When the view refreshes, the next unknown icon
                        // should trigger the task to be reloaded.
                    }
                }
            }
            return chooser.getFileSystemView().getSystemIcon(f);
        }

//        public Icon _getIcon(File f) {
//            try {
//                if (f != null &&
//                    f.isDirectory() && // #173958: do not call ProjectManager.isProject now, could block
//                    !f.toString().matches("/[^/]+") && // Unix: /net, /proc, etc. // NOI18N
//                    f.getParentFile() != null) { // do not consider drive roots
//                    String path = f.getAbsolutePath();
//                    String project = path+"/nbproject"; // NOI18N
//                    File projectDir = chooser.getFileSystemView().createFileObject(project);
//                    if (projectDir.exists() && projectDir.isDirectory() && projectDir.canRead()) {
//                        String projectXml = path+"/nbproject/project.xml"; // NOI18N
//                        File projectFile = chooser.getFileSystemView().createFileObject(projectXml);
//                        if (projectFile.exists()) {
//                            String conf = path+"/nbproject/configurations.xml"; // NOI18N
//                            File configuration = chooser.getFileSystemView().createFileObject(conf);
//                            if (configuration.exists()) {
//                                return ImageUtilities.loadImageIcon("org/netbeans/modules/cnd/makeproject/ui/resources/makeProject.gif", true); // NOI18N
//                            }
//                        }
//                    }
//                }
//            } catch (Throwable t) {
//                //
//            }
//            return chooser.getFileSystemView().getSystemIcon(f);
//        }
        
        @Override
        public void run() {
            String path = lookingForIcon.getAbsolutePath();
            String project = path + "/nbproject"; // NOI18N
            File projectDir = chooser.getFileSystemView().createFileObject(project);
            Icon icon = chooser.getFileSystemView().getSystemIcon(lookingForIcon);;
            if (projectDir.exists() && projectDir.isDirectory() && projectDir.canRead()) {
                String projectXml = path + "/nbproject/project.xml"; // NOI18N
                File projectFile = chooser.getFileSystemView().createFileObject(projectXml);
                if (projectFile.exists()) {
                    String conf = path + "/nbproject/configurations.xml"; // NOI18N
                    File configuration = chooser.getFileSystemView().createFileObject(conf);
                    if (configuration.exists()) {
                        icon = ImageUtilities.loadImageIcon("org/netbeans/modules/cnd/makeproject/ui/resources/makeProject.gif", true); // NOI18N
                    }
                }
            }
            synchronized (this) {
                knownProjectIcons.put(lookingForIcon, icon);
                lookingForIcon = null;
            }
            chooser.repaint();
        }
    }
}
