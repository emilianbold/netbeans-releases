/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.junit;

import java.awt.EventQueue;
import java.util.ArrayList;
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
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.cookies.EditorCookie;
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
    public String getName() {
        return NbBundle.getMessage(CreateTestAction.class,
                                   "LBL_Action_CreateTest");            //NOI18N
    }

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

        // show configuration dialog
        // when dialog is canceled, escape the action
        JUnitCfgOfCreate cfg = new JUnitCfgOfCreate(nodes);
        if (!cfg.configure()) {
            return;
        }

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

}
