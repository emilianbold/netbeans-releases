/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.junit;

import java.awt.EventQueue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.swing.Action;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.junit.plugin.JUnitPlugin;
import org.netbeans.modules.junit.plugin.JUnitPlugin.CreateTestParam;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.loaders.DataFolder;
import org.openide.util.RequestProcessor;

/** Action sensitive to some cookie that does something useful.
 *
 * @author  vstejskal, David Konecny
 * @author  Marian Petras
 * @author  Ondrej Rypacek
 */
public final class CreateTestAction extends TestAction {
        
    public CreateTestAction() {
        putValue("noIconInMenu", Boolean.TRUE);                         //NOI18N
    }

    /* public members */

    @Override
    public String getName() {
        return NbBundle.getMessage(CreateTestAction.class,
                                   "LBL_Action_CreateTest");            //NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(CreateTestAction.class);
    }

    @Override
    protected void initialize() {
        super.initialize();
        putProperty(Action.SHORT_DESCRIPTION,
                    NbBundle.getMessage(CreateTestAction.class,
                                        "HINT_Action_CreateTest"));     //NOI18N
    }

    @Override
    protected String iconResource() {
        return "org/netbeans/modules/junit/resources/"                  //NOI18N
               + "CreateTestActionIcon.gif";                            //NOI18N
    }

    @Override
    protected boolean enable(Node[] nodes) {
        if (nodes.length == 0) {
            return false;
        }

        /*
         * In most cases, there is just one node selected - that is why
         * this case is handled in a special, more effective way
         * (no collections and iterators created).
         */
        if (nodes.length == 1) {
            final Node node = nodes[0];
            DataObject dataObj;
            FileObject fileObj;
            Project project;
            if (((dataObj = node.getCookie(DataObject.class)) != null)
                && ((fileObj = dataObj.getPrimaryFile()) != null)
                && fileObj.isValid()
                && ((project = FileOwnerQuery.getOwner(fileObj)) != null)
                && (getSourceGroup(fileObj, project) != null)
                && (TestUtil.isJavaFile(fileObj)
                    || (node.getCookie(DataFolder.class) != null))) {

                JUnitPlugin plugin = TestUtil.getPluginForProject(project);
                return JUnitPluginTrampoline.DEFAULT.canCreateTests(plugin,
                                                                    fileObj);
            } else {
                return false;
            }
        }

        final Collection<FileObject> fileObjs
                = new ArrayList<FileObject>(nodes.length);
        Project theProject = null;
        boolean result = false;
        for (Node node : nodes) {
            DataObject dataObj = node.getCookie(DataObject.class);
            if (dataObj != null) {
                FileObject fileObj = dataObj.getPrimaryFile();
                if ((fileObj == null) || !fileObj.isValid()) {
                    continue;
                }

                fileObjs.add(fileObj);
                
                Project prj = FileOwnerQuery.getOwner(fileObj);
                if (prj != null) {
                    if (theProject == null) {
                        theProject = prj;
                    }
                    if (prj != theProject) {
                        return false;        /* files from different projects */
                    }

                    if ((getSourceGroup(fileObj, prj) != null)
                        && (TestUtil.isJavaFile(fileObj)
                            || (node.getCookie(DataFolder.class) != null))) {
                        result = true;
                    }
                }
            }
        }

        if (theProject != null) {
            JUnitPlugin plugin = TestUtil.getPluginForProject(theProject);
            result &= JUnitPluginTrampoline.DEFAULT.canCreateTests(
                            plugin,
                            fileObjs.toArray(new FileObject[fileObjs.size()]));
        }

        return result;
    }

    /**
     * Checks that the selection of nodes the dialog is invoked on is valid. 
     * @return String message describing the problem found or null, if the
     *         selection is ok
     */
    private static String checkNodesValidity(Node[] nodes) {
        FileObject[] files = getFiles(nodes);

        Project project = getProject(files);
        if (project == null) {
            return NbBundle.getMessage(CreateTestAction.class,
                                       "MSG_multiproject_selection");   //NOI18N
        }

        if (!checkPackages(files)) {
            return NbBundle.getMessage(CreateTestAction.class,
                                       "MSG_invalid_packages");         //NOI18N
        }

        return null;
    }

    /**
     * Check that all the files (folders or java files) have correct java
     * package names.
     * @return true if all are fine
     */
    private static boolean checkPackages(FileObject[] files) {
        if (files.length == 0) {
            return true;
        } else {
            Project project = FileOwnerQuery.getOwner(files[0]);
            for (int i = 0 ; i < files.length; i++) {
                String packageName = getPackage(project, files[i]);
                if ((packageName == null)
                        || !TestUtil.isValidPackageName(packageName)) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Get the package name of <code>file</code>.
     *
     * @param project owner of the file (for performance reasons)
     * @param file the FileObject whose packagename to get
     * @return package name of the file or null if it cannot be retrieved
     */
    private static String getPackage(Project project, FileObject file) {
        SourceGroup srcGrp = TestUtil.findSourceGroupOwner(project, file);
        if (srcGrp!= null) {
            ClassPath cp = ClassPathSupport.createClassPath(
                    new FileObject [] {srcGrp.getRootFolder()});
            return cp.getResourceName(file, '.', false);
        } else {
            return null;
        }
    }


    private static FileObject[] getFiles(Node[] nodes) {
        FileObject[] ret = new FileObject[nodes.length];
        for (int i = 0 ; i < nodes.length ; i++) {
            ret[i]  = TestUtil.getFileObjectFromNode(nodes[i]);
        }
        return ret;
    }

    /**
     * Get the single project for <code>nodes</code> if there is such.
     * If the nodes belong to different projects or some of the nodes doesn't
     * have a project, return null.
     */
    private static Project getProject(FileObject[] files) {
        Project project = null;
        for (int i = 0 ; i < files.length; i++) {
            Project nodeProject = FileOwnerQuery.getOwner(files[i]);
            if (project == null) {
                project = nodeProject;
            } else if (project != nodeProject) {
                return null;
            }
        }
        return project;
    }

    @Override
    protected void performAction(Node[] nodes) {
        String problem;
        if ((problem = checkNodesValidity(nodes)) != null) {
            // TODO report problem
            NotifyDescriptor msg = new NotifyDescriptor.Message(
                                problem, NotifyDescriptor.WARNING_MESSAGE);
            DialogDisplayer.getDefault().notify(msg);
            return;
        } 

        final FileObject[] filesToTest = getFileObjectsFromNodes(nodes);
        if (filesToTest == null) {
            return;     //XXX: display some message
        }

        /* Determine the plugin to be used: */
        final JUnitPlugin plugin = TestUtil.getPluginForProject(
                FileOwnerQuery.getOwner(filesToTest[0]));

        if (!JUnitPluginTrampoline.DEFAULT.createTestActionCalled(
                                                        plugin, filesToTest)) {
            return;
        }

        final DataObject[] modified = DataObject.getRegistry().getModified();

        // show configuration dialog
        // when dialog is canceled, escape the action
        JUnitCfgOfCreate cfg = new JUnitCfgOfCreate(nodes, 
                                            modified.length == 0 ? false: true);
        if (!cfg.configure()) {
            return;
        }

        saveAll(modified); // #149048

        /* Store the configuration data: */
        final boolean singleClass = cfg.isSingleClass();
        final Map<CreateTestParam, Object> params
                = TestUtil.getSettingsMap(!singleClass);
        if (singleClass) {
            params.put(CreateTestParam.CLASS_NAME, cfg.getTestClassName());
        }
        final FileObject targetFolder = cfg.getTargetFolder();
        cfg = null;

        RequestProcessor.getDefault().post(new Runnable() {
                @Override
                public void run() {
                    /* Now create the tests: */
                    final FileObject[] testFileObjects
                            = JUnitPluginTrampoline.DEFAULT.createTests(
                                    plugin,
                                    filesToTest,
                                    targetFolder,
                                    params);

                    /* Open the created/updated test class if appropriate: */
                    if (testFileObjects.length == 1) {
                        try {
                            DataObject dobj = DataObject.find(testFileObjects[0]);
                            final EditorCookie ec = dobj.getCookie(EditorCookie.class);
                            if (ec != null) {
                                EventQueue.invokeLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            ec.open();
                                        }
                                });
                            }
                        } catch (DataObjectNotFoundException ex) {
                            ex.printStackTrace();
                        }
                    }
                }});
    }

    /**
     * Extracts {@code FileObject}s from the given nodes.
     * Nodes that have (direct or indirect) parent nodes among the given
     * nodes are ignored.
     *
     * @return  a non-empty array of {@code FileObject}s
     *          represented by the given nodes;
     *          or {@code null} if no {@code FileObject} was found;
     */
    private static FileObject[] getFileObjectsFromNodes(final Node[] nodes){
        FileObject[] fileObjects = new FileObject[nodes.length];
        List<FileObject> fileObjectsList = null;

        for (int i = 0; i < nodes.length; i++) {
            final Node node = nodes[i];
            final FileObject fo;
            if (!hasParentAmongNodes(nodes, i)
                    && ((fo = getTestFileObject(node)) != null)) {
                if (fileObjects != null) {
                    fileObjects[i] = fo;
                } else {
                    if (fileObjectsList == null) {
                        fileObjectsList = new ArrayList<FileObject>(
                                                        nodes.length - i);
                    }
                    fileObjectsList.add(fo);
                }
            } else {
                fileObjects = null;     //signs that some FOs were skipped
            }
        }
        if (fileObjects == null) {
            if (fileObjectsList != null) {
                fileObjects = fileObjectsList.toArray(
                        new FileObject[fileObjectsList.size()]);
                fileObjectsList = null;
            }
        }

        return fileObjects;
    }

    /**
     * Grabs and checks a <code>FileObject</code> from the given node.
     * If either the file could not be grabbed or the file does not pertain
     * to any project, a message is displayed.
     *
     * @param  node  node to get a <code>FileObject</code> from.
     * @return  the grabbed <code>FileObject</code>,
     *          or <code>null</code> in case of failure
     */
    private static FileObject getTestFileObject(final Node node) {
        final FileObject fo = TestUtil.getFileObjectFromNode(node);
        if (fo == null) {
            TestUtil.notifyUser(NbBundle.getMessage(
                    CreateTestAction.class,
                    "MSG_file_from_node_failed"));                      //NOI18N
            return null;
        }
        ClassPath cp = ClassPath.getClassPath(fo, ClassPath.SOURCE);
        if (cp == null) {
            TestUtil.notifyUser(NbBundle.getMessage(
                    CreateTestAction.class,
                    "MSG_no_project",                                   //NOI18N
                    fo));
            return null;
        }
        return fo;
    }

    private static boolean hasParentAmongNodes(final Node[] nodes,
                                               final int idx) {
        Node node;

        node = nodes[idx].getParentNode();
        while (null != node) {
            for (int i = 0; i < nodes.length; i++) {
                if (i == idx) {
                    continue;
                }
                if (node == nodes[i]) {
                    return true;
                }
            }
            node = node.getParentNode();
        }
        return false;
    }

    private void saveAll(DataObject[] dataObjects) {
        for(DataObject dataObject: dataObjects) {
            SaveCookie saveCookie = dataObject.getCookie(SaveCookie.class);
            if(saveCookie != null) {
                try {
                    saveCookie.save();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }

}
