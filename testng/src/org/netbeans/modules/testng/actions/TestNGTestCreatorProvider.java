/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.testng.actions;

import java.awt.EventQueue;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gsf.testrunner.api.TestCreatorProvider;
import org.netbeans.modules.gsf.testrunner.plugin.CommonPlugin;
import org.netbeans.modules.java.testrunner.CommonTestUtil;
import org.netbeans.modules.java.testrunner.GuiUtils;
import org.netbeans.modules.testng.api.TestNGSupport;
import org.netbeans.modules.testng.ui.TestNGPlugin;
import org.netbeans.modules.testng.ui.TestNGPluginTrampoline;
import org.netbeans.modules.testng.ui.TestUtil;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.text.Line;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author theofanis
 */
@TestCreatorProvider.Registration(displayName=GuiUtils.TESTNG_TEST_FRAMEWORK)
public class TestNGTestCreatorProvider extends TestCreatorProvider {

    private static final Logger LOGGER = Logger.getLogger(TestNGTestCreatorProvider.class.getName());
    
    @Override
    public boolean canHandleMultipleClasses(Node[] activatedNodes) {
        return true;
//        if (activatedNodes.length != 1) {
//            return false;
//        }
//        DataObject dataObj = activatedNodes[0].getLookup().lookup(DataObject.class);
//        if (dataObj == null) {
//            return false;
//        }
//        return dataObj.getPrimaryFile().isData();
    }

    @Override
    public boolean enable(Node[] activatedNodes) {
        if (activatedNodes.length == 0) {
            return false;
        }
        DataObject dataObject = activatedNodes[0].getLookup().lookup(DataObject.class);
        if (dataObject != null) {
            Project p = FileOwnerQuery.getOwner(dataObject.getPrimaryFile());
            return TestNGSupport.isActionSupported(TestNGSupport.Action.CREATE_TEST, p);
        }
        return false;
    }

    @Override
    public void createTests(Context context) {
        final DataObject dataObject = context.getActivatedNodes()[0].getLookup().lookup(DataObject.class);
        if (dataObject == null) {
            return;
        }
        
        final FileObject[] filesToTest = getFileObjectsFromNodes(context.getActivatedNodes());
        if (filesToTest == null) {
            return;     //XXX: display some message
        }

        /*
         * Determine the plugin to be used:
         */
        final TestNGPlugin plugin = TestUtil.getPluginForProject(
                FileOwnerQuery.getOwner(filesToTest[0]));

        if (!TestNGPluginTrampoline.DEFAULT.createTestActionCalled(
                plugin, filesToTest)) {
            return;
        }

        /*
         * Store the configuration data:
         */
        final boolean singleClass = context.isSingleClass();
        final Map<CommonPlugin.CreateTestParam, Object> params = CommonTestUtil.getSettingsMap(!singleClass);
        if (singleClass) {
            String name = context.getTestClassName();
            params.put(CommonPlugin.CreateTestParam.CLASS_NAME, name.substring(name.indexOf("testng.") + 7));
        }
        
        FileObject trgFolder = context.getTargetFolder();        
        
        try {
            trgFolder = FileUtil.createFolder(trgFolder, "testng");
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        final FileObject targetFolder = trgFolder;
        TestNGSupport.findTestNGSupport(FileOwnerQuery.getOwner(targetFolder)).configureProject(targetFolder);
        RequestProcessor.getDefault().post(new Runnable() {

            @Override
            public void run() {
                /*
                 * Now create the tests:
                 */
                final FileObject[] testFileObjects = TestNGPluginTrampoline.DEFAULT.createTests(
                        plugin,
                        filesToTest,
                        targetFolder,
                        params);

                /*
                 * Open the created/updated test class if appropriate:
                 */
                if (testFileObjects.length == 1) {
                    try {
                        DataObject dobj = DataObject.find(testFileObjects[0]);
                        final EditorCookie ec = dobj.getLookup().lookup(EditorCookie.class);
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
            }
        });
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

    /**
     * Grabs and checks a <code>FileObject</code> from the given node.
     * If either the file could not be grabbed or the file does not pertain
     * to any project, a message is displayed.
     *
     * @param  node  node to get a <code>FileObject</code> from.
     * @return  the grabbed <code>FileObject</code>,
     *          or <code>null</code> in case of failure
     */
    @NbBundle.Messages({"MSG_file_from_node_failed=File cannot be found for selected node.",
        "# {0} - source file",
        "MSG_no_project=Source file {0} does not belong to any project."})
    private static FileObject getTestFileObject(final Node node) {
        final FileObject fo = TestUtil.getFileObjectFromNode(node);
        if (fo == null) {
            TestUtil.notifyUser(Bundle.MSG_file_from_node_failed());
            return null;
        }
        ClassPath cp = ClassPath.getClassPath(fo, ClassPath.SOURCE);
        if (cp == null) {
            TestUtil.notifyUser(Bundle.MSG_no_project(fo));
            return null;
        }
        return fo;
    }
    
}
