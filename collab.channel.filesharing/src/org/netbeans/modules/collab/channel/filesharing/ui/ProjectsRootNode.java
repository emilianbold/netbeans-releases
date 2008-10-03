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
package org.netbeans.modules.collab.channel.filesharing.ui;

import com.sun.collablet.CollabException;
import com.sun.collablet.Conversation;
import java.lang.reflect.InvocationTargetException;

import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.actions.PasteAction;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Index;
import org.openide.nodes.Node;
import org.openide.nodes.NodeTransfer;
import org.openide.util.*;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.PasteType;
import org.openide.xml.XMLUtil;

import java.awt.Cursor;
import java.awt.Image;
import java.awt.datatransfer.Transferable;

import java.beans.*;

import java.io.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.Action;
import javax.swing.SwingUtilities;

import org.netbeans.api.java.project.JavaProjectConstants;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.ui.OpenProjects;

import org.netbeans.modules.collab.channel.filesharing.FilesharingCollablet;
import org.netbeans.modules.collab.channel.filesharing.FilesharingConstants;
import org.netbeans.modules.collab.channel.filesharing.FilesharingContext;
import org.netbeans.modules.collab.channel.filesharing.FilesharingEventNotifierFactory;
import org.netbeans.modules.collab.channel.filesharing.FilesharingEventProcessorFactory;
import org.netbeans.modules.collab.channel.filesharing.context.ProjectContext;
import org.netbeans.modules.collab.channel.filesharing.event.DeleteFileEvent;
import org.netbeans.modules.collab.channel.filesharing.event.ProjectActionListEvent;
import org.netbeans.modules.collab.channel.filesharing.event.RenameFileEvent;
import org.netbeans.modules.collab.channel.filesharing.eventlistener.FilesharingTimerTask;
import org.netbeans.modules.collab.channel.filesharing.eventlistener.ProjectActionListTimerTask;
import org.netbeans.modules.collab.channel.filesharing.filehandler.CollabFileHandler;
import org.netbeans.modules.collab.channel.filesharing.filesystem.CollabFilesystem;
import org.netbeans.modules.collab.channel.filesharing.mdc.CollabEvent;
import org.netbeans.modules.collab.channel.filesharing.mdc.EventContext;
import org.netbeans.modules.collab.channel.filesharing.mdc.EventNotifier;
import org.netbeans.modules.collab.channel.filesharing.mdc.EventProcessor;
import org.netbeans.modules.collab.channel.filesharing.projecthandler.SharedProject;
import org.netbeans.modules.collab.channel.filesharing.projecthandler.SharedProjectFactory;
import org.netbeans.modules.collab.channel.filesharing.ui.actions.CollabProjectAction;
import org.netbeans.modules.collab.channel.filesharing.ui.actions.CreateNonProjectAction;
import org.netbeans.modules.collab.channel.filesharing.ui.actions.ProjectAction;
import org.netbeans.modules.collab.channel.filesharing.util.FileshareUtil;
import org.netbeans.modules.collab.core.Debug;

import org.netbeans.spi.project.ui.LogicalViewProvider;


/**
 *
 * @author  Ayub Khan, ayub.khan@sun.com
 */
public class ProjectsRootNode extends AbstractNode implements FilesharingConstants {
    ////////////////////////////////////////////////////////////////////////////
    // Class variables
    ////////////////////////////////////////////////////////////////////////////
    public static final String URL_SHARE_JAVA_NET = "share.java.net:5222"; //NoI18n
    public static final Image PROJECTS_ROOT_BADGE = ImageUtilities.loadImage(
            "org/netbeans/modules/collab/channel/filesharing/resources/project_png.gif", true
        ); // NOI18N
    public static final Image GROUP_BADGE = ImageUtilities.loadImage(
            "org/netbeans/modules/collab/ui/resources/group_png.gif", true
        ); // NOI18N
    public static final List NON_SHAREABLE_NODE_PATTERNS = new ArrayList();

    static {
        NON_SHAREABLE_NODE_PATTERNS.add("jar:file:/");
    }

    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    FileSystem n_filesystem;
    FileObject rootFile;
    String loginUser = null;
    FilesharingContext context;

    public ProjectsRootNode(FileSystem filesystem) {
        super(new Index.ArrayChildren());

        String name = NbBundle.getMessage(ProjectsRootNode.class, "LBL_ProjectsRootNode_DisplayName");
        setName(name);
        setDisplayName(name);
        n_filesystem = filesystem;
        this.context = ((CollabFilesystem) n_filesystem).getContext();
        loginUser = getContext().getLoginUser();
    }

    public Action[] getActions(boolean context) {
        if (getContext().isReadOnlyConversation()) {
            return new SystemAction[] {  };
        } else {
            return new SystemAction[] {
                new CreateNonProjectAction(getContext(), this), null, SystemAction.get(PasteAction.class)
            };
        }
    }

    public Image getIcon(int type) {
        return computeIcon(false, type);
    }

    public Image getOpenedIcon(int type) {
        return computeIcon(true, type);
    }

    private Image computeIcon(boolean opened, int type) {
        //return Utilities.mergeImages(PROJECTS_ROOT_BADGE, GROUP_BADGE, 7, 7);
        //New icons for filesharing
        Image image = ImageUtilities.loadImage(
                "org/netbeans/modules/collab/channel/filesharing/resources/filesharing_png.gif", true
            ); // NOI18N

        return image;
    }

    private FilesharingContext getContext() {
        return this.context;
    }

    private static boolean strContains(String str, String pattern) {
        return str.indexOf(pattern) != -1;
    }

    private DataObject[] getDragDataObjects(Node[] dragNodes, List dragDataObjects) {
        boolean skipProjectFolder = false;

        if ((dragNodes != null) && (dragNodes.length == 1)) {
            DataObject d = (DataObject) dragNodes[0].getCookie(DataObject.class);

            if (
                (d != null) && (d.getPrimaryFile() != null) && d.getPrimaryFile().isFolder() &&
                    ProjectManager.getDefault().isProject(d.getPrimaryFile())
            ) {
                skipProjectFolder = true;
            }
        }

        return getDragDataObjects(dragNodes, dragDataObjects, skipProjectFolder);
    }

    private DataObject[] getDragDataObjects(Node[] dragNodes, List dragDataObjects, boolean skipProjectFolder) {
        Debug.out.println("ProjectsRootNode, getDragDataObjects: " + dragNodes.length);

        if (dragNodes != null) {
            if ((dragNodes.length == 1) && !skipNode(dragNodes[0])) //now find all dataobject, including sub dataobjects
             {
                if (dragNodes[0].isLeaf() || (dragNodes[0].getCookie(DataObject.class) != null)) {
                    Debug.out.println("leaf: " + dragNodes[0].getName());

                    DataObject d = (DataObject) dragNodes[0].getCookie(DataObject.class);

                    if ((d != null) && (d.getPrimaryFile() != null)) //a file or folder
                     {
                        Debug.out.println("leaf a file/folder: " + d.getName());

                        if (d.getPrimaryFile().isData() || strContains(dragNodes[0].getName(), "jar:file:/")) { //a file
                            Debug.out.println("leaf a file: " + d.getName());

                            if (!dragDataObjects.contains(d)) {
                                dragDataObjects.add(d);
                            }
                        } else //a folder
                         {
                            Debug.out.println("leaf a folder: " + d.getName());
                            getChildren(d.getPrimaryFile(), dragDataObjects, skipProjectFolder);
                        }
                    } else {
                        Debug.out.println("leaf not a file/folder: " + d);
                    }
                } else {
                    Debug.out.println("non leaf: " + dragNodes[0].getName());
                    getDragDataObjects(dragNodes[0].getChildren().getNodes(), dragDataObjects, skipProjectFolder);
                }
            } else {
                //find atleast a node that has a dataobject in the dragnodes
                for (int i = 0; i < dragNodes.length; i++) {
                    Debug.out.println("a folder: " + dragNodes[i].getName());

                    if (!(skipNode(dragNodes[i]) || dragNodes[i].getName().equals("important.files"))) {
                        getDragDataObjects(new Node[] { dragNodes[i] }, dragDataObjects, skipProjectFolder);
                    }
                }
            }
        }

        return (DataObject[]) dragDataObjects.toArray(new DataObject[0]);
    }

    private void getChildren(FileObject fObj, List dragDataObjects, boolean skipProjectFolder) {
        Debug.out.println("In getChildren: ");

        FileObject[] childs = fObj.getChildren();

        for (int i = 0; i < childs.length; i++) {
            if (isVCSFile(childs[i]) || //skip VCS files
                    childs[i].getNameExt().startsWith(".")) { //skip hidden files

                continue;
            }

            Debug.out.println("getChildren: [" + childs[i].getNameExt() + "] a file: " + childs[i].isData());

            if (childs[i].isData()) {
                try {
                    DataObject d = DataObject.find(childs[i]);

                    if (d.getPrimaryFile().isData()) { //a file

                        if (!dragDataObjects.contains(d)) {
                            dragDataObjects.add(d);
                        }
                    }
                } catch (DataObjectNotFoundException ddnf) {
                }
            } else {
                if (
                    skipProjectFolder &&
                        (ProjectManager.getDefault().isProject(childs[i]) ||
                        childs[i].getNameExt().equals("build.xml") || //NoI18n
                        childs[i].getNameExt().equals("build") || //NoI18n
                        childs[i].getNameExt().equals("dist") || //NoI18n
                        childs[i].getNameExt().equals("arch") || //NoI18n
                        childs[i].getNameExt().equals("nbproject")) //NoI18n
                ) {
                    continue;
                }

                getChildren(childs[i], dragDataObjects, skipProjectFolder);
            }
        }
    }

    private boolean isVCSFile(FileObject fObj) {
        String VSS_IGNORE_FILE = "vssver.scc"; //defined in org.netbeans.modules.vcs.profiles.vss.list.VssListCommand.IGNORED_FILE
        String CVS_IGNORE_FILE = "CVS";
        String SCCS_IGNORE_FILE = "SCCS";
        String RCS_IGNORE_FILE = "RCS";
        String fileName = fObj.getNameExt();

        if (
            CVS_IGNORE_FILE.equalsIgnoreCase(fileName) || VSS_IGNORE_FILE.equalsIgnoreCase(fileName) ||
                SCCS_IGNORE_FILE.equalsIgnoreCase(fileName) || RCS_IGNORE_FILE.equalsIgnoreCase(fileName)
        ) {
            return true;
        } else {
            return false;
        }
    }

    private Project findShareableProject(FileObject file) {
        Project[] projects = OpenProjects.getDefault().getOpenProjects();

        for (int i = 0; i < projects.length; i++) {
            Project project = projects[i];
            Debug.log("ProjectsRootNode", "ProjectsRootNode, projects[" + i + "]: " + project);

            Project prj = FileOwnerQuery.getOwner(file);

            if ((prj != null) && prj.getProjectDirectory().getPath().equals(project.getProjectDirectory().getPath())) {
                return project; //match found
            }
        }

        return null;
    }

    private String getProjectName(Project project) {
        String projectName = null;

        if (project != null) {
            ProjectInformation info = ProjectUtils.getInformation(project);
            projectName = info.getDisplayName();
            Debug.log("ProjectsRootNode", "ProjectsRootNode, Project name: " + info.getDisplayName());
            Debug.log("ProjectsRootNode", "ProjectsRootNode, Project id: " + info.getName());
        }

        return projectName;
    }

    /*
     * createProjectNode
     *
     * @param nodes
     */
    public void createProjectNode(Node[] nue) throws CollabException {
        setVisibleWaitCursor(true);

        //do actual work here
        doCreateProjectNode(nue, null, null);

        setVisibleWaitCursor(false);
    }

    private void setVisibleWaitCursor(boolean visible) {
        Debug.out.println("In setVisibleWaitCursor: " + visible);

        FilesystemExplorerPanel panel = getContext().getFilesystemExplorer();

        if (panel != null) {
            if (visible) {
                String message = NbBundle.getMessage(
                        FilesystemExplorerPanel.class, "LBL_FilesystemExplorerPanel_PauseMessage"
                    );
                PropertyChangeEvent event = new PropertyChangeEvent(this, FS_STATUS_CHANGE, message, message);
                panel.propertyChange(event);
                Debug.out.println("setting wait cursor");

                try {
                    panel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                } catch (Throwable th) {
                    th.printStackTrace(Debug.out);
                }
            } else {
                PropertyChangeEvent event = new PropertyChangeEvent(this, FS_STATUS_CHANGE, "", "");
                panel.propertyChange(event);
                Debug.out.println("unsetting wait cursor");
                panel.setCursor(null);
            }
        }
    }

    private void doCreateProjectNode(final Node[] nue, String projectName, Project fromProject)
    throws CollabException {
        //return if not shareable
        if (checkNonShareableNodes(nue)) {
            Debug.out.println("skip node: " + nue[0].getName());

            return;
        }

        //find atleast a node that has a dataobject in the dragnodes
        List ddList = new ArrayList();

        if ((nue != null) && (nue.length == 1) && !(skipNode(nue[0]))) //process project node
         {
            Project p = null;
            DataObject d1 = (DataObject) nue[0].getCookie(DataObject.class);

            if ((d1 != null) && (d1.getPrimaryFile() != null)) {
                p = findShareableProject(d1.getPrimaryFile());

                if ((p == null) || !p.getProjectDirectory().getPath().equals(d1.getPrimaryFile().getPath())) {
                    p = null;
                }
            } else {
                Debug.out.println("node may not be a project");
            }

            if (p != null) {
                fromProject = p;
            }

            if (fromProject != null) {
                Debug.out.println("check sgs: ");

                Sources s = ProjectUtils.getSources(fromProject);
                SourceGroup[] sgs = s.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);

                for (int i = 0; i < sgs.length; i++) {
                    Debug.out.println(" sgs: " + sgs[i].getName());
                    Debug.out.println(" sgs: " + sgs[i].getRootFolder());

                    DataObject d = null;

                    try {
                        d = DataObject.find(sgs[i].getRootFolder());
                    } catch (DataObjectNotFoundException ddnf) {
                        ddnf.printStackTrace(Debug.out);
                    }

                    if (d != null) {
                        getChildren(d.getPrimaryFile(), ddList, true);
                    }
                }

                if ((ddList.size() == 0) && (d1 != null)) { //try using nodes
                    ddList.add(d1);
                }
            } else { //not a project node
                getDragDataObjects(nue, ddList);
            }
        } else { //nodes
            getDragDataObjects(nue, ddList);
        }

        final DataObject[] dragDataObjects = (DataObject[]) ddList.toArray(new DataObject[0]);
        Debug.out.println("getDragDataObjects return length: " + dragDataObjects.length);

        if (dragDataObjects.length == 0) {
            return;
        }

        //return if atleast a dataobject exceed 1MB limit for share.java.net
        //this check is commented out for now

        /*String serverURL=URL_SHARE_JAVA_NET;// NOI18N
        Debug.out.println("serverURL: "+serverURL);
        String userID=context.getConversation().getCollabSession().
                                getUserPrincipal().getIdentifier();
        String userIDServerInfo=userID.split("@")[1];
        Debug.out.println("userIDServerInfo: "+userIDServerInfo);
        for(int i=0;i<dragDataObjects.length;i++)
        {
                if(isExceedShareableSizeLimit(new DataObject[]{dragDataObjects[i]}) &&
                        userIDServerInfo!=null &&
                                userIDServerInfo.contains(serverURL.split(":")[0]))//NoI18n
                {
                        return;
                }
        }*/
        if (projectName == null) {
            projectName = SHARED_COMMON_DIR;
            fromProject = findShareableProject(dragDataObjects[0].getPrimaryFile());

            if (fromProject != null) {
                projectName = getProjectName(fromProject);

                //return if project already shared
                if (!isProjectShared(fromProject)) {
                    return;
                }
            }

            SharedProject sharedProject = SharedProjectFactory.createSharedProject(
                    projectName, loginUser, fromProject, getContext().getSharedProjectManager()
                );
            getContext().getSharedProjectManager().addSharedProject(loginUser, projectName, sharedProject);
        }

        //now add dragnodes
        final String projectNodeName = projectName;
        final Project prj = fromProject;

        try {
            n_filesystem.runAtomicAction(
                new FileSystem.AtomicAction() {
                    public void run() throws IOException {
                        Debug.log(
                            "ProjectsRootNode", "ProjectsRootNode, " + "doAddChildren:: projectName: " +
                            projectNodeName
                        );
                        doCreateProjectNode(loginUser, projectNodeName, dragDataObjects, prj);
                        setVisibleWaitCursor(false);
                    }
                }
            );
        } catch (IOException iox) {
            Debug.log("ProjectsRootNode", "ProjectsRootNode, PasteType::ex: " + iox);
            iox.printStackTrace(Debug.out);
        } catch (Throwable th) {
            Debug.log("ProjectsRootNode", "ProjectsRootNode, PasteType::ex: " + th);
            th.printStackTrace(Debug.out);
        }
    }

    private void showMessage(String message) {
        final NotifyDescriptor descriptor = new NotifyDescriptor.Message(message);
        SwingUtilities.invokeLater(
            new Runnable() {
                public void run() {
                    DialogDisplayer.getDefault().notify(descriptor);
                }
            }
        );
    }

    private boolean checkNonShareableNodes(Node[] dragNodes) {
        Debug.out.println("In checkNonShareableNodes");

        List checkNodes = NON_SHAREABLE_NODE_PATTERNS;

        for (int i = 0; i < dragNodes.length; i++) {
            DataObject d1 = (DataObject) dragNodes[i].getCookie(DataObject.class);

            if (d1 != null) {
                String fPath = d1.getPrimaryFile().getPath();
                String rPath = getRootDir().getPath().replace('\\', FILE_SEPERATOR_CHAR);
                Debug.out.println("path: " + fPath);
                Debug.out.println("rpath: " + rPath);

                if (strContains(fPath, rPath)) {
                    return true; //skip copy share file/folder to share area
                }
            }

            for (int j = 0; j < checkNodes.size(); j++) {
                String pattern = (String) checkNodes.get(j);

                if (
                    (dragNodes[i] != null) && (dragNodes[i].getName() != null) &&
                        strContains(dragNodes[i].getName(), pattern)
                ) {
                    String message = NbBundle.getMessage(
                            ProjectsRootNode.class, "MSG_ProjectsRootNode_CannotShareNode",
                            dragNodes[i].getDisplayName()
                        );
                    showMessage(message);

                    return true;
                }
            }
        }

        return false;
    }

    private boolean isExceedShareableSizeLimit(DataObject[] dragDataObjects) {
        long totalSize = calculateSize(dragDataObjects);

        if (totalSize > SHAREABLE_LIMIT) {
            String message = NbBundle.getMessage(
                    ProjectsRootNode.class, "MSG_ProjectsRootNode_SharedFilesExceedLimit",
                    dragDataObjects[0].getPrimaryFile().getNameExt(), URL_SHARE_JAVA_NET
                );
            showMessage(message);

            return true;
        }

        return false;
    }

    private long calculateSize(DataObject[] dragDataObjects) {
        long totalSize = 0;

        for (int i = 0; i < dragDataObjects.length; i++) {
            FileObject[] fileObjects = (FileObject[]) dragDataObjects[i].files().toArray(new FileObject[0]);

            for (int j = 0; j < fileObjects.length; j++) {
                long fileSize = fileObjects[j].getSize();
                Debug.log(
                    "ProjectsRootNode", "ProjectsRootNode, file: " + fileObjects[j].getNameExt() + " size: " +
                    fileSize
                );

                if ((totalSize + fileSize) >= Long.MAX_VALUE) {
                    return Long.MAX_VALUE;
                }

                totalSize += fileSize;
                Debug.log("ProjectsRootNode", "ProjectsRootNode, totalSize: " + totalSize);
            }
        }

        Debug.log("ProjectsRootNode", "ProjectsRootNode, final totalSize: " + totalSize);

        return totalSize;
    }

    private boolean isProjectShared(Project project) {
        Conversation[] conv = FilesharingCollablet.getConversations(project);
        boolean checkShareable = ((conv == null) || (conv.length == 0) ||
            ((conv.length == 1) &&
            conv[0].getDisplayName().equals(getContext().getChannel().getConversation().getDisplayName()))) ? true : false;

        if (!checkShareable) {
            String sharedConvName = ((conv != null) && (conv[0] != null)) ? conv[0].getDisplayName() : "";
            String message = NbBundle.getMessage(
                    ProjectsRootNode.class, "MSG_ProjectsRootNode_ProjectAlreadyShared", sharedConvName
                );
            showMessage(message);
        }

        return checkShareable;
    }

    protected void createPasteTypes(Transferable t, List ls) {
        if (getContext().isReadOnlyConversation()) {
            return;
        }

        final Node[] ns = NodeTransfer.nodes(t, NodeTransfer.DND_COPY);

        if (ns != null) {
            ls.add(
                new PasteType() {
                    public Transferable paste() throws IOException {
                        final Node[] nue = new Node[ns.length];

                        for (int i = 0; i < nue.length; i++) {
                            nue[i] = ns[i].cloneNode();
                        }
                        
                        RequestProcessor.getDefault().post(new Runnable() {
                            public void run() {
                                try {
                                    createProjectNode(nue);
                                } catch (CollabException ce) {
                                }
                            }
                        });

                        return null;
                    }
                }
            );
        }

        // Also try superclass, but give it lower priority:
        super.createPasteTypes(t, ls);
    }

    /*
     * init
     *
     */
    public void init() {
        Debug.out.println("PSRN init");

        SharedProject[] sharedProjects = getContext().getSharedProjectManager().getAllSharedProjects();

        for (int i = 0; i < sharedProjects.length; i++) {
            String projectOwner = sharedProjects[i].getProjectOwner();
            String projectName = sharedProjects[i].getName();

            try {
                final Node projectNode = createProjectNode(projectOwner, projectName);
                final FileObject projectFolder = ((ProjectNode) projectNode).getProjectDir();

                //init all child nodes
                FileSystem fs = getContext().getCollabFilesystem();
                fs.runAtomicAction(
                    new FileSystem.AtomicAction() {
                        public void run() throws IOException {
                            //find atleast a node that has a dataobject in the dragnodes
                            List ddList = new ArrayList();
                            getChildren(projectFolder, ddList, true);

                            DataObject[] dd = (DataObject[]) ddList.toArray(new DataObject[0]);
                            Debug.out.println("getDragDataObjects return length: " + dd.length);
                            processPaste(projectNode, dd, null, false);
                        }
                    }
                );
            } catch (IOException iox) {
                //ignore
            }
        }
    }

    /*
     * createProjectNode
     *
     * @param userName
     * @param projectName
     * @return projectNode
     */
    public Node createProjectNode(final String userName, final String projectName)
    throws IOException {
        return doCreateProjectNode(userName, projectName, null, null);
    }

    /*
     * doCreateProjectNode
     *
     * @param userName
     * @param projectName
     * @param dragDataObjects
     * @return projectNode
     */
    private Node doCreateProjectNode(
        final String userName, final String projectName, DataObject[] dragDataObjects, Project fromProject
    ) throws IOException {
        Debug.log("ProjectsRootNode", "ProjectsRootNode, \n\nADDCHILD userNode(loginUser name): " + userName);

        SharedProject sharedProject = getContext().getSharedProjectManager().getSharedProject(userName, projectName);

        if (sharedProject != null) {
            Debug.log("ProjectsRootNode", "ProjectsRootNode, found project: " + projectName);
        } else {
            Debug.log("ProjectsRootNode", "ProjectsRootNode, createSharedProject: " + projectName);
            sharedProject = SharedProjectFactory.createSharedProject(
                    projectName, userName, null, getContext().getSharedProjectManager()
                );
            getContext().getSharedProjectManager().addSharedProject(userName, projectName, sharedProject);
        }

        Node userNode = createUserNode(userName);

        if (userNode == null) {
            Debug.log("ProjectsRootNode", "ProjectsRootNode, ADDCHILD:: find userNode null");

            return null;
        }

        //add project nodes to the userNode now
        Node projectNode = addProjectNodes(userNode, projectName, fromProject);

        if ((projectNode != null) && (dragDataObjects != null)) {
            processPaste(projectNode, dragDataObjects, fromProject, true);
        }

        return projectNode;
    }

    /*
     * add project nodes to the userNode
     *
     * @param userName
     */
    private Node addProjectNodes(Node userNode, String projectName, final Project fromProject)
    throws IOException {
        final String userName = userNode.getName();
        Children children = userNode.getChildren();

        if (projectName == null) {
            projectName = SHARED_COMMON_DIR;
        }

        if (fromProject != null) {
            LogicalViewProvider lvp = (LogicalViewProvider) fromProject.getLookup().lookup(LogicalViewProvider.class);
            Node[] nodes = new Node[] { lvp.createLogicalView() };
            Debug.log("ProjectsRootNode", "ProjectsRootNode, PRN createNodes length: " + nodes.length);

            if (nodes[0].getLookup().lookup(Project.class) != fromProject) {
                // Various actions, badging, etc. are not going to work.
                ErrorManager.getDefault().log(
                    ErrorManager.WARNING,
                    "Warning - project1 " + ProjectUtils.getInformation(fromProject).getName() +
                    " failed to supply itself in the lookup of the " + "root node of its own logical view"
                ); // NOI18N
            }

            projectName = nodes[0].getDisplayName();
        }

        Node projectNode = children.findChild(projectName);

        if (projectNode == null) {
            projectNode = new ProjectNode(projectName, userNode, fromProject, null);
            Debug.out.println("ProjectsRootNode, Created project node: " + projectName);
            children.add(new Node[] { projectNode });
            
            final Node pNode = projectNode;
            final String pName = projectName;

            Runnable run = new Runnable() {
                public void run() {
                    //get Actions from original project
                    if (fromProject != null) {
                        processActionsFromSharedProject(pNode, fromProject);
                    } else {
                        SharedProject sharedProject = getContext().getSharedProjectManager().getSharedProject(
                                userName, pName);
                        processActions(pNode, sharedProject);
                    }                    
                }
            };
	    if (SwingUtilities.isEventDispatchThread()) {
                run.run();
            } else {
                try {
                    SwingUtilities.invokeAndWait(run);
                } catch (InterruptedException ie) {
                    // ignore
                } catch (InvocationTargetException ite) {
                    ErrorManager.getDefault().notify(ite);
                }
            }
        }

        //create original files folder for owner
        Children projectChildren = projectNode.getChildren();

        if (
            !projectName.equals(SHARED_COMMON_DIR) && //skip creating for non project
                loginUser.equals(userName) && //skip creating for non project owner
                (projectChildren.findChild(ARCHIVE_DIR) == null)
        ) {
            ((ProjectNode) projectNode).getChildNode(ARCHIVE_DIR);
        }

        return projectNode;
    }

    private boolean skipNode(Node childNode) {
        if (
            (childNode == null) || (childNode.getName() == null) || childNode.getName().equals(LIBRARY_NODE) ||
                childNode.getName().equals(TEST_LIBRARY_NODE)
        ) {
            return true;
        }

        return false;
    }

    private FileObject getRootDir() {
        if (rootFile == null) {
            rootFile = FileUtil.toFileObject(((CollabFilesystem) n_filesystem).getCollabRoot());
        }

        return rootFile;
    }

    private Node createUserNode(String userName) {
        Node userNode = new UserNode(userName);
        Debug.log("ProjectsRootNode", "ProjectsRootNode, Created user node: " + userNode.getName());

        Children userNodes = getChildren();

        if (userNodes.findChild(userName) == null) {
            userNodes.add(new Node[] { userNode });
        }

        userNodes = getChildren();
        userNode = userNodes.findChild(userName);

        return userNode;
    }

    private void processActionsFromSharedProject(Node projectNode, Project fromProject) {
        HashMap actionMap = new HashMap();
        LogicalViewProvider lvp_fromProject = (LogicalViewProvider) fromProject.getLookup().lookup(
                LogicalViewProvider.class
            );
        Node[] nodes_fromProject = new Node[] { lvp_fromProject.createLogicalView() };
        Debug.log("ProjectsRootNode", "ProjectsRootNode, PRN createNodes length: " + nodes_fromProject.length);

        if (nodes_fromProject[0].getLookup().lookup(Project.class) != fromProject) {
            // Various actions, badging, etc. are not going to work.
            ErrorManager.getDefault().log(
                ErrorManager.WARNING,
                "Warning - project1 " + ProjectUtils.getInformation(fromProject).getName() +
                " failed to supply itself in the lookup of the " + "root node of its own logical view"
            ); // NOI18N
        }

        Node[] badgedNodes_fromProject = new Node[nodes_fromProject.length];

        for (int j = 0; j < nodes_fromProject.length; j++) {
            badgedNodes_fromProject[j] = new BadgingNode(nodes_fromProject[j]);
        }

        Object[] actions = badgedNodes_fromProject[0].getActions(false);

        for (int j = 0; j < actions.length; j++) {
            if (actions[j] instanceof Action) {
                Action action = (Action) actions[j];
                Debug.out.println("ProjectsRootNode" + "ProjectsRootNode, Swg action: " + action.toString());

                String actionValue = (String) action.getValue(Action.NAME);
                Debug.out.println("ProjectsRootNode" + "ProjectsRootNode, getValue" + actionValue);
                actionMap.put(actionValue, action);
            }
        }

        SharedProject sharedProject = getContext().getSharedProjectManager().getSharedProject(
                loginUser, projectNode.getName()
            );

        if (sharedProject != null) {
            sharedProject.setProjectActions((Action[]) actionMap.values().toArray(new Action[0]));
        }

        SystemAction[] pNodeactions = processActions(projectNode, sharedProject);

        //Send Action List
        EventContext evContext = new ProjectContext(
                ProjectActionListEvent.getEventID(), sharedProject.getProjectOwner(), projectNode.getName(),
                pNodeactions
            );
        final CollabEvent ce = new ProjectActionListEvent(evContext);

        /* send projectActionList message after a delay */
        ProjectActionListTimerTask sendProjectActionListTimerTask = new ProjectActionListTimerTask(
                getContext().getChannelEventNotifier(), new ProjectActionListEvent(evContext), getContext()
            );
        getContext().addTimerTask(
            SEND_PROJECTACTIONLIST_TIMER_TASK, projectNode.getName(), sendProjectActionListTimerTask
        );
        sendProjectActionListTimerTask.schedule(FilesharingTimerTask.PERIOD * 3);
    }

    private SystemAction[] processActions(Node projectNode, SharedProject sharedProject) {
        Action[] nodeActions = sharedProject.getProjectActions();
        Project fromProject = sharedProject.getOriginalProject();
        SystemAction[] pNodeactions = FileshareUtil.setProjectActions(
                projectNode, nodeActions, fromProject, getContext()
            );

        return pNodeactions;
    }

    public void processPaste(Node projectNode, DataObject[] dragDataObjects, Project fromProject, boolean performPaste)
    throws IOException, DataObjectNotFoundException {
        Debug.out.println("In PSRN processPaste");

        for (int i = 0; i < dragDataObjects.length; i++) {
            if (dragDataObjects[i] == null) {
                continue;
            }

            DataObject dragDataObject = dragDataObjects[i];

            if (
                (dragDataObject == null) || (dragDataObject.getPrimaryFile() == null) ||
                    dragDataObject.getPrimaryFile().isFolder()
            ) { //not a file

                continue;
            }

            try {
                if (performPaste && getContext().getSharedFileGroupManager().isShared(dragDataObject.getPrimaryFile())) {
                    Debug.out.println("Skip paste, file already shared: " + dragDataObject.getPrimaryFile().getPath());

                    continue; //skip if file already shared
                }
            } catch (Throwable th) {
                th.printStackTrace(Debug.out);
            }

            String dragNodePath = dragDataObject.getPrimaryFile().getPath();
            Debug.out.println("PSRN, dragDataObject: " + dragNodePath);

            String uri = null;

            if (projectNode.getParentNode().getName().equals(loginUser)) { //perform for project owner

                if (
                    !performPaste && //Create new file/folder to Shared Common root
                        projectNode.getName().equals(SHARED_COMMON_DIR)
                ) //DnD to Shared Common from Shared Common
                 { //truncate path for New Shared Common files
                    uri = getRelativePath(((ProjectNode) projectNode).getProjectDir().getPath(), dragNodePath);
                } else {
                    if (fromProject != null) { //dragging to a SharedProjectNode

                        //uri="src/japp/Main.java" if dragNodePath is "C:/test/javaapp/src/japp/Main.java", 
                        //project path is "C:/test/javaapp"
                        //uri=dragNodePath.substring(fromProject.getProjectDirectory().getPath().length()+1);
                        uri = getRelativePath(fromProject.getProjectDirectory().getPath(), dragNodePath);
                    } else { //dragging to a SharedNonProjectNode

                        //uri="myfolder/myfile.txt" if dragNodePath is "C:/myfolder/myfile.txt"
                        uri = dragNodePath.substring(dragNodePath.indexOf(FILE_SEPERATOR) + 1);
                    }
                }
            } else { //remote user

                //uri="src/japp/Main.java" if dragNodePath is "C:/test/javaapp/src/japp/Main.java", 
                //project path is "C:/test/javaapp"
                //uri=dragNodePath.substring(((ProjectNode)projectNode).getProjectDir().getPath().length()+1);	
                uri = getRelativePath(((ProjectNode) projectNode).getProjectDir().getPath(), dragNodePath);

                if (uri.startsWith("/") || uri.startsWith("\\")) {
                    uri = uri.substring(1);
                }
            }

            Debug.log("ProjectsRootNode", "ProjectsRootNode, uri full: " + uri);

            int firstindex = uri.indexOf(FILE_SEPERATOR);
            int lastindex = uri.lastIndexOf(FILE_SEPERATOR);
            String destFolderPath = null;
            String destFldr = null;

            if (firstindex != -1) {
                //find the corresponding node src for src, web for web etc
                ((CollabFilesystem) n_filesystem).refresh(false);

                Node tmpNode = ((ProjectNode) projectNode).getChildNode(uri.substring(0, firstindex));
                Debug.log("ProjectsRootNode", "ProjectsRootNode, uri short: " + uri.substring(0, firstindex));

                if (tmpNode != null) //if folder exist
                 {
                    if ((firstindex != -1) && (lastindex != -1) && (firstindex < lastindex)) {
                        //"japp"
                        destFolderPath = uri.substring(firstindex + 1, lastindex);
                    }
                } else //drop the file into collab directory with its structure 
                 {
                    //"src/japp"
                    destFolderPath = uri.substring(0, lastindex);
                }

                destFldr = uri.substring(0, firstindex);
            } else //drop the file into collab directory with its structure 
             {
                destFldr = null;
            }

            Node dragNode = null;
            DataFolder leafFldr = null;

            //make actual paste here
            if (performPaste) {
                if (destFldr == null) {
                    leafFldr = DataFolder.findFolder(((ProjectNode) projectNode).getProjectDir());
                } else {
                    leafFldr = DataFolder.findFolder(
                            ((ProjectNode) projectNode).getProjectDir().getFileObject(destFldr)
                        );
                }

                Debug.log("ProjectsRootNode", "PRN, leafFldr : " + leafFldr.getPrimaryFile().getPath());
                Debug.log("ProjectsRootNode", "PRN, destFolderPath : " + destFolderPath);

                if ((destFolderPath != null) && !destFolderPath.equals("")) {
                    leafFldr = replicateFolderStructure(leafFldr.getPrimaryFile(), destFolderPath);
                }

                Debug.log("ProjectsRootNode", "PRN, leafFldr : " + leafFldr.getPrimaryFile().getPath());

                DataObject dd = null;

                if (fromProject != null) {
                    dd = dragDataObject.createShadow(leafFldr);
                } else {
                    dd = dragDataObject.copy(leafFldr);
                }

                ((CollabFilesystem) n_filesystem).refresh(false);
                dragNode = dd.getNodeDelegate();
            } else {
                leafFldr = DataFolder.findFolder(((ProjectNode) projectNode).getProjectDir());
                dragNode = dragDataObject.getNodeDelegate();
            }

            boolean isLocal = loginUser.equals(projectNode.getParentNode().getName());

            //Now create filter node of the dragDataObject
            if (projectNode.getName().equals(SHARED_COMMON_DIR)) {
                dragNode = new SharedNonProjectNode(
                        dragDataObject.getPrimaryFile().getNameExt(), dragNode, isLocal, getContext()
                    );
            } else {
                dragNode = new SharedProjectNode(dragDataObject.getNodeDelegate(), isLocal, getContext(), false);
            }

            Node destNode = ((ProjectNode) projectNode).getChildNode(destFldr);

            if (destNode != null) {
                if (destNode instanceof PackagesNode) {
                    String pkgName = DEFAULT_PKG;

                    if (destFolderPath != null) {
                        pkgName = destFolderPath;
                    }

                    Debug.log("ProjectsRootNode", "PRN, pkg 1: " + pkgName);

                    //Seperate conf files, usually in WebApps have src/conf/MANIFEST.mf
                    if (pkgName.equals(CONF_NODE)) {
			//fix bug#6284927
                        //destNode = ((ProjectNode) projectNode).getChildNode(CONF_NODE);
                        //if (destNode.getChildren().findChild(dragNode.getName()) == null) {
                        //    destNode.getChildren().add(new Node[] { dragNode });
                        //}
                    } else {
                        if (pkgName.startsWith(JAVA_PKG)) {
                            if (pkgName.length() == 4) {
                                pkgName = DEFAULT_PKG;
                            } else if (pkgName.startsWith(JAVA_PKG + FILE_SEPERATOR)) {
                                pkgName = pkgName.substring(5);
                            }
                        }

                        PackagesNode.PackagesNodeChildren pkgs = ((PackagesNode) destNode).getPackagesNodeChildren();

                        if (pkgs.findChild(pkgName) == null) {
                            pkgs.add(pkgName, new Node[] { dragNode });
                        } else {
                            pkgs.findChild(pkgName).getChildren().add(new Node[] { dragNode });
                        }
                    }

                    Debug.log("ProjectsRootNode", "PRN, pkg 2: " + pkgName);
                } else {
                } //nothing to do
            } else {
                if (projectNode.getChildren().findChild(dragNode.getName()) == null) {
                    projectNode.getChildren().add(new Node[] { dragNode });
                }
            }

            //expand node
            FilesystemExplorerPanel panel = getContext().getFilesystemExplorer();

            if (panel != null) {
                if (destFolderPath != null) {
                    panel.expandTreeNode(dragNode, destFolderPath);
                } else {
                    panel.expandTreeNode(dragNode.getParentNode(), destFolderPath);
                }
            }

            //now send file
            if (performPaste) {
                getContext().getSharedFileGroupManager().sendDataObject(
                    loginUser, projectNode.getName(), leafFldr, dragDataObject
                );
            }

            if ((fromProject != null) && performPaste) {
                //save a copy of the original file of project
                leafFldr = DataFolder.findFolder(
                        ((ProjectNode) projectNode).getProjectDir().getFileObject(ARCHIVE_DIR)
                    );
                destFolderPath = null;

                if (lastindex != -1) {
                    destFolderPath = uri.substring(0, lastindex); //"src/japp"	
                }

                if ((destFolderPath != null) && !destFolderPath.equals("")) {
                    leafFldr = replicateFolderStructure(leafFldr.getPrimaryFile(), destFolderPath);
                    ((CollabFilesystem) n_filesystem).refresh(false);
                }

                dragDataObject.copy(leafFldr);
            }
        }
    }

    private String getRelativePath(String folderPath, String path) {
        Debug.out.println("In getRelativePath for loginUser: " + loginUser);
        Debug.out.println("folderPath: " + folderPath + " path: " + path);

        if (path.startsWith("/") || path.startsWith("\\")) {
            path = path.substring(1);
        }

        if (folderPath.startsWith("/") || folderPath.startsWith("\\")) {
            folderPath = folderPath.substring(1);
        }

        if (path.length() > folderPath.length()) {
            return path.substring(folderPath.length() + 1);
        } else {
            return folderPath;
        }
    }

    protected DataFolder replicateFolderStructure(FileObject dropRoot, String dragParentPath)
    throws IOException, DataObjectNotFoundException {
        Debug.out.println("In replicateFolderStructure for: " + dragParentPath);

        DataObject d = DataObject.find(FileUtil.createFolder(dropRoot, dragParentPath));

        return (DataFolder) d.getCookie(DataFolder.class);
    }

    private boolean isExist(String name, File[] userDirs) {
        for (int i = 0; i < userDirs.length; i++) {
            if (userDirs[i].getName().equals(name)) {
                Debug.log("ProjectsRootNode", "ProjectsRootNode, found name: " + name);

                return true;
            }
        }

        return false;
    }

    class UserNode extends AbstractNode {
        ////////////////////////////////////////////////////////////////////////////
        // Instance variables
        ////////////////////////////////////////////////////////////////////////////
        private FileObject userFolder = null;
        private Image USER_BADGE = ImageUtilities.loadImage(
                "org/netbeans/modules/collab/core/resources/account_png.gif", true
            ); // NOI18N		

        public UserNode(String name) {
            super(new Index.ArrayChildren());
            Debug.log("ProjectsRootNode", "ProjectsRootNode, UNuser: " + name);
            setName(name);
            setDisplayName(getContext().getPrincipal(name).getDisplayName());
        }

        public Action[] getActions(boolean context) {
            return new SystemAction[] {  };
        }

        public Image getIcon(int type) {
            return computeIcon(false, type);
        }

        public Image getOpenedIcon(int type) {
            return computeIcon(true, type);
        }

        private Image computeIcon(boolean opened, int type) {
            return USER_BADGE;
        }

        /**
         *
         *
         */
        public boolean canCut() {
            return false;
        }

        /**
         *
         *
         */
        public boolean canCopy() {
            return false;
        }

        /**
         *
         *
         */
        public boolean canDestroy() {
            return false;
        }

        /**
         *
         *
         */
        public boolean canRename() {
            return false;
        }

        private FileObject getUserDir() {
            if (userFolder != null) {
                return userFolder;
            }

            FileObject rootFile = FileUtil.toFileObject(((CollabFilesystem) n_filesystem).getCollabRoot());

            if (rootFile == null) {
                return null;
            }

            userFolder = rootFile.getFileObject(getName());

            try {
                if (userFolder == null) {
                    userFolder = rootFile.createFolder(getName());
                }
            } catch (IOException iox) {
                iox.printStackTrace(Debug.out);
            }

            return userFolder;
        }
    }

    public class ProjectNode extends AbstractNode {
        ////////////////////////////////////////////////////////////////////////////
        // Instance variables
        ////////////////////////////////////////////////////////////////////////////
        private Image PROJECT_BADGE = PROJECTS_ROOT_BADGE;
        private SystemAction[] actions = null;
        private HashMap actionMap = new HashMap();
        private String parentNodeName;
        private Project fromProject;
        private FileObject projectFolder;
        private boolean isLocal = false;

        public ProjectNode(final String name, Node userNode, Project fromProject, SystemAction[] actions) {
            super(new Index.ArrayChildren());
            Debug.log("ProjectsRootNode", "ProjectsNode: " + name);
            setName(name);

            if (name.equals(SHARED_COMMON_DIR)) {
                setDisplayName(COLLAB_NON_PROJECT_FOLDER_NAME);
            } else {
                setDisplayName(name);
            }

            this.parentNodeName = userNode.getName();
            this.fromProject = fromProject;
            setActions(actions);

            FileObject userFolder = ((UserNode) userNode).getUserDir();
            projectFolder = userFolder.getFileObject(name);

            try {
                if (projectFolder == null) {
                    projectFolder = userFolder.createFolder(name);
                }
            } catch (IOException iox) {
                iox.printStackTrace(Debug.out);

                return;
            }

            Debug.out.println("ProjectsRootNode, projectDir: " + projectFolder.getPath());
            Debug.out.println("SharedProject pDir: " + getProjectDir());
            this.isLocal = loginUser.equals(this.parentNodeName);
            init();
        }

        public Image getIcon(int type) {
            return computeIcon(false, type);
        }

        public Image getOpenedIcon(int type) {
            return computeIcon(true, type);
        }

        private Image computeIcon(boolean opened, int type) {
            return PROJECT_BADGE;
        }

        public Action[] getActions(boolean context) {
            return (actions == null) ? new SystemAction[] {  } : actions;
        }

        /**
         *
         *
         */
        public boolean canCut() {
            return false;
        }

        /**
         *
         *
         */
        public boolean canCopy() {
            return false;
        }

        /**
         *
         *
         */
        public boolean canDestroy() {
            return false;
        }

        /**
         *
         *
         */
        public boolean canRename() {
            return false;
        }

        public void setActions(SystemAction[] actions) {
            int size;

            if (actions == null) {
                size = 0;
            } else {
                size = actions.length;
            }

            this.actions = new SystemAction[(size * 2) + 3];

            int index = 0;

            if (loginUser.equals(parentNodeName)) {
                if (!getContext().isReadOnlyConversation()) {
                    if (this.getName().equals(SHARED_COMMON_DIR)) {
                        this.actions[index++] = new CreateNonProjectAction(getContext(), this);
                    }

                    this.actions[index++] = SystemAction.get(PasteAction.class);
                }
            }

            this.actions[index++] = null;
            actionMap.clear();

            for (int i = 0; i < size; i++) {
                //this.actions[index+i]=actions[i];
                if (actions[i] != null) {
                    actionMap.put(((ProjectAction) actions[i]).getID(), actions[i]);
                }
            }

            //re-order actions
            if (actionMap.get(CollabProjectAction.COMMAND_BUILD) != null) {
                this.actions[index++] = (SystemAction) actionMap.get(CollabProjectAction.COMMAND_BUILD);
            }

            if (actionMap.get(CollabProjectAction.COMMAND_REBUILD) != null) {
                this.actions[index++] = (SystemAction) actionMap.get(CollabProjectAction.COMMAND_REBUILD);
                this.actions[index++] = null;
            }

            if (actionMap.get(CollabProjectAction.COMMAND_RUN) != null) {
                this.actions[index++] = (SystemAction) actionMap.get(CollabProjectAction.COMMAND_RUN);
                this.actions[index++] = null;
            }

            if (actionMap.get(CollabProjectAction.COMMAND_INSTALL) != null) {
                this.actions[index++] = (SystemAction) actionMap.get(CollabProjectAction.COMMAND_INSTALL);
                this.actions[index++] = null;
            }
        }

        public SystemAction getAction(String name) {
            Debug.out.println("ProjectsRootNode, ProjectsRootNode, actionMap: " + actionMap);

            return (SystemAction) actionMap.get(name);
        }

        protected void createPasteTypes(Transferable t, List ls) {
            if (getContext().isReadOnlyConversation()) {
                return;
            }

            final Node[] ns = NodeTransfer.nodes(t, NodeTransfer.DND_COPY);
            final String projectNodeName = this.getName();

            if (ns != null) {
                ls.add(
                    new PasteType() {
                        public Transferable paste() throws IOException {
                            //return if not the owner of this project
                            if (!loginUser.equals(parentNodeName)) {
                                return null;
                            }

                            Node[] nue = new Node[ns.length];

                            for (int i = 0; i < nue.length; i++) {
                                nue[i] = ns[i].cloneNode();
                            }

                            try {
                                createProjectNode(nue);
                            } catch (CollabException ce) {
                                //ignore
                            }

                            return null;
                        }
                    }
                );
            }

            // Also try superclass, but give it lower priority:
            super.createPasteTypes(t, ls);
        }

        public Node getChildNode(String childName) throws DataObjectNotFoundException {
            if (childName == null) {
                return null;
            }

            Children projectChildren = getChildren();
            String projectName = getName();
            String userName = getParentNode().getName();
            Node childNode = null;

            if (getName().equals(SHARED_COMMON_DIR)) {
                childNode = projectChildren.findChild(childName); //NoI18n		

                if (childNode == null) {
                    FileObject pChildFolder = getProjectDir().getFileObject(childName);

                    try {
                        if (pChildFolder == null) {
                            pChildFolder = getProjectDir().createFolder(childName);
                        }

                        projectChildren.add(
                            new Node[] {
                                new SharedNonProjectNode(childName, DataObject.find(pChildFolder).getNodeDelegate(),
                                    loginUser.equals(parentNodeName), getContext()
                                )
                            }
                        );
                    } catch (IOException iox) {
                        iox.printStackTrace(Debug.out);
                    }
                }
            }

            if (childName.equals(SRC_DIR) || childName.equals(TEST_DIR) || childName.equals(JAVAHELP_DIR)) {
                String displayName = childName;

                if (childName.equals(SRC_DIR)) {
                    childNode = (projectChildren.findChild(SRC_DIR) != null) ? projectChildren.findChild(SRC_DIR)
                                                                             : projectChildren.findChild(SRC_NODE); //NoI18n
                    displayName = SRC_FOLDER_NAME;
                } else if (childName.equals(TEST_DIR)) {
                    childNode = (projectChildren.findChild(TEST_DIR) != null) ? projectChildren.findChild(TEST_DIR)
                                                                              : projectChildren.findChild(TEST_NODE); //NoI18n
                    displayName = TEST_FOLDER_NAME;
                } else if (childName.equals(JAVAHELP_DIR)) {
                    childNode = (projectChildren.findChild(JAVAHELP_DIR) != null)
                        ? projectChildren.findChild(JAVAHELP_DIR) : projectChildren.findChild(JAVAHELP_NODE); //NoI18n
                    displayName = JAVAHELP_FOLDER_NAME;
                }

                if (childNode == null) {
                    FileObject pChildFolder = getProjectDir().getFileObject(childName);

                    try {
                        if (pChildFolder == null) {
                            pChildFolder = getProjectDir().createFolder(childName);
                        }

                        if (projectChildren.findChild(childName) == null) {
                            if (displayName == null) {
                                displayName = childName;
                            }

                            String packageRootPath = pChildFolder.getPath();
                            projectChildren.add(
                                new Node[] {
                                    new PackagesNode(displayName, packageRootPath,
                                        DataObject.find(pChildFolder).getNodeDelegate(), isLocal, getContext()
                                    )
                                }
                            );
                        }
                    } catch (IOException iox) {
                        iox.printStackTrace(Debug.out);
                    }
                }
            } else if (childName.equals(WEB_DIR)) //NoI18n
             {
                childNode = projectChildren.findChild(WEB_DIR); //NoI18n		

                if (childNode == null) {
                    FileObject pChildFolder = getProjectDir().getFileObject(childName);

                    try {
                        if (pChildFolder == null) {
                            pChildFolder = getProjectDir().createFolder(childName);
                        }

                        projectChildren.add(
                            new Node[] {
                                new SharedProjectNode(WEB_FOLDER_NAME, DataObject.find(pChildFolder).getNodeDelegate(),
                                    isLocal, getContext()
                                )
                            }
                        );
                    } catch (IOException iox) {
                        iox.printStackTrace(Debug.out);
                    }
                }
            } else if (childName.equals(CONF_NODE)) {
                childNode = projectChildren.findChild(CONF_NODE); //NoI18n		

                if (childNode == null) {
                    FileObject pChildFolder = getProjectDir().getFileObject(CONF_DIR);

                    try {
                        if (pChildFolder == null) {
                            pChildFolder = getProjectDir().createFolder(CONF_DIR);
                        }

                        projectChildren.add(
                            new Node[] {
                                new SharedProjectNode(CONF_FOLDER_NAME, DataObject.find(pChildFolder).getNodeDelegate(),
                                    isLocal, getContext()
                                )
                            }
                        );
                    } catch (IOException iox) {
                        iox.printStackTrace(Debug.out);
                    }
                }
            } else if (childName.equals(ARCHIVE_DIR)) {
                FileObject pChildFolder = getProjectDir().getFileObject(ARCHIVE_DIR);

                try {
                    if (pChildFolder == null) {
                        pChildFolder = getProjectDir().createFolder(ARCHIVE_DIR);
                    }
                } catch (IOException iox) {
                    iox.printStackTrace(Debug.out);
                }
            } else {
                childNode = projectChildren.findChild(childName);

                if (childNode == null) {
                    FileObject pChildFolder = getProjectDir().getFileObject(childName);

                    try {
                        if (pChildFolder == null) {
                            pChildFolder = getProjectDir().createFolder(childName);
                        }

                        projectChildren.add(
                            new Node[] {
                                new SharedProjectNode(childName, DataObject.find(pChildFolder).getNodeDelegate(),
                                    isLocal, getContext()
                                )
                            }
                        );
                    } catch (IOException iox) {
                        iox.printStackTrace(Debug.out);
                    }
                }
            }

            return projectChildren.findChild(childName);
        }

        public FileObject getProjectDir() {
            return this.projectFolder;
        }

        public String getParentName() {
            return this.parentNodeName;
        }

        private void init() {
            if (getName().equals(SHARED_COMMON_DIR)) {
                FileSystem fs = null;

                try {
                    fs = getProjectDir().getFileSystem();
                } catch (Exception e) {
                    Debug.out.println("SharedProject getFileSystem() failed" + e);
                    e.printStackTrace(Debug.out);
                }

                FileChangeListener scfl = new SharedCommonFileListener(this, parentNodeName);

                if (fs != null) {
                    fs.addFileChangeListener(scfl);
                    getContext().setSharedCommonFileListener(fs, scfl);
                }
            }
        }
    }

    public class SharedCommonFileListener extends FileChangeAdapter {
        private ProjectNode pNode;
        private String userNodeName;
        private String ppath;
        private EventNotifier fileChangeNotifier = null;

        public SharedCommonFileListener(ProjectNode node, String userNodeName) {
            this.pNode = node;
            this.userNodeName = userNodeName;
            this.ppath = node.getProjectDir().getPath();
            Debug.out.println("In scfl() for loginUser: " + loginUser);
            Debug.out.println("projectDir: " + ppath);

            //create a notifer first
            try {
                EventProcessor ep = FilesharingEventProcessorFactory.getDefault().createEventProcessor(
                        getContext().getProcessorConfig(), getContext()
                    );
                fileChangeNotifier = FilesharingEventNotifierFactory.getDefault().createEventNotifier(
                        getContext().getNotifierConfig(), ep
                    );
            } catch (CollabException e) {
                e.printStackTrace(Debug.out);
            }
        }

        public void fileDataCreated(FileEvent e) {
            if (!isShared(e)) {
                return;
            }

            FileObject fObj = e.getFile();
            Debug.out.println("FileDataCreated: " + fObj.getNameExt());

            String fileName = fObj.getNameExt();

            if (!(fileName.equals(".nbattrs") || (fileName.startsWith(".LCK") && fileName.endsWith("~")))) {
                if (getRelativePath(fObj.getPath()).equals(fObj.getNameExt())) {
                    addNode(e);
                }

                try {
                    Debug.out.println("fileDataCreated: " + fileName);

                    DataObject folder = (DataObject) DataObject.find(fObj.getParent());
                    DataFolder leafFldr = (DataFolder) folder.getCookie(DataFolder.class);

                    if (userNodeName.equals(loginUser)) {
                        DataObject d = DataObject.find(fObj);
                        context.getSharedFileGroupManager().sendDataObject(loginUser, getName(), leafFldr, d);
                    }
                } catch (DataObjectNotFoundException ddnf) {
                    //ignore
                }
            }
        }

        public void fileFolderCreated(FileEvent e) {
            if (!isShared(e)) {
                return;
            }

            FileObject fObj = e.getFile();
            Debug.out.println("FileFolderCreated: " + fObj.getNameExt());

            if (getRelativePath(fObj.getPath()).equals(fObj.getNameExt())) {
                addNode(e);
            }
        }

        public void fileDeleted(FileEvent fe) {
            if (!isShared(fe)) {
                return;
            }

            FileObject fObj = fe.getFile();
            Debug.out.println("File Delete: " + fObj.getNameExt());

            if (fileChangeNotifier == null) {
                return;
            }

            try {
                if (userNodeName.equals(loginUser)) //send if owner
                 {
                    if (!getContext().getSharedFileGroupManager().isShared(fObj)) {
                        return;
                    }

                    CollabFileHandler fh = getContext().getSharedFileGroupManager().getFileHandler(fObj);
                    EventContext evContext = new EventContext(DeleteFileEvent.getEventID(), fh);
                    CollabEvent ce = new DeleteFileEvent(evContext);
                    fileChangeNotifier.notify(ce);
                }
            } catch (Exception e) {
                Debug.out.println("Exception sendDelete: " + e);
                e.printStackTrace(Debug.out);
            }
        }

        public void fileRenamed(FileRenameEvent fre) {
            if (!isShared(fre)) {
                return;
            }

            FileObject fObj = fre.getFile();
            String origName = fre.getName();
            String origExt = fre.getExt();

            if ((origExt != null) && !origExt.equals("")) {
                origName = origName + "." + origExt;
            }

            Debug.out.println("File Rename from: " + origName);
            Debug.out.println("File Rename to: " + fObj.getNameExt());

            if (fileChangeNotifier == null) {
                return;
            }

            try {
                if (getRelativePath(fObj.getPath()).equals(fObj.getNameExt())) {
                    Node node = pNode.getChildren().findChild(fObj.getNameExt());

                    if (node == null) {
                        node = pNode.getChildren().findChild(origName);
                    }

                    if (node != null) {
                        Debug.out.println("removing node: " + origName);
                        pNode.getChildren().remove(new Node[] { node });
                        Debug.out.println("adding node: " + fObj.getNameExt());
                        addNode(fre);
                    } else {
                        Debug.out.println("node null for: " + fObj.getNameExt());
                    }
                }

                if (userNodeName.equals(loginUser)) //send if owner
                 {
                    Debug.out.println("check isShared: " + fObj.getNameExt());

                    if (!getContext().getSharedFileGroupManager().isShared(fObj)) {
                        return;
                    }

                    CollabFileHandler fh = getContext().getSharedFileGroupManager().getFileHandler(fObj);
                    String fileName = fh.getName();
                    Debug.out.println("shared: " + fileName);

                    EventContext evContext = new EventContext(RenameFileEvent.getEventID(), fh);
                    CollabEvent ce = new RenameFileEvent(evContext);
                    fileChangeNotifier.notify(ce);
                }
            } catch (Exception e) {
                Debug.out.println("Exception sendRename: " + e);
                e.printStackTrace(Debug.out);
            }
        }

        /*
         * check if operation belongs to this right shared folder
         */
        private boolean isShared(FileEvent e) {
            FileObject fObj = e.getFile();
            String fPath = fObj.getPath();
            String rPath = ppath.replace('\\', FILE_SEPERATOR_CHAR);
            Debug.out.println("path: " + fPath);
            Debug.out.println("rpath: " + rPath);

            if (strContains(fPath, rPath)) {
                return true; //a shared file
            }

            return false;
        }

        private void addNode(FileEvent e) {
            FileObject fObj = e.getFile();
            Debug.out.println("ProjectsRootNode fileadded: " + fObj.getPath());

            try {
                DataObject d = DataObject.find(fObj);

                if (fObj.isData()) {
                    ProjectsRootNode.this.processPaste(pNode, new DataObject[] { d }, null, false);
                } else {
                    if (pNode.getChildren().findChild(fObj.getName()) == null) {
                        pNode.getChildren().add(
                            new Node[] {
                                new SharedNonProjectNode(d.getNodeDelegate(), pNode.getParentName().equals(loginUser),
                                    getContext()
                                )
                            }
                        );
                    }
                }
            } catch (DataObjectNotFoundException dne) {
                dne.printStackTrace(Debug.out);
            } catch (IOException iox) {
                iox.printStackTrace(Debug.out);
            }
        }

        private String getRelativePath(String path) {
            return ProjectsRootNode.this.getRelativePath(ppath, path);
        }
    }

    private static final class BadgingNode extends FilterNode implements PropertyChangeListener {
        public BadgingNode(Node n) {
            super(n, null, //default children
                n.getLookup()
            );
        }

        public String getDisplayName() {
            String original = super.getDisplayName();

            return original;
        }

        public String getHtmlDisplayName() {
            String htmlName = getOriginal().getHtmlDisplayName();
            String dispName = null;

            if (htmlName == null) {
                dispName = super.getDisplayName();

                try {
                    dispName = XMLUtil.toElementContent(dispName);
                } catch (CharConversionException ex) {
                    // ignore
                }
            }

            return htmlName; //NOI18N
        }

        public void propertyChange(PropertyChangeEvent e) {
        }
    }
}
