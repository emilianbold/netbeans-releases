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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.ui;

import java.awt.EventQueue;
import java.util.Arrays;
import javax.swing.JDialog;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.project.Project;
import org.netbeans.modules.apisupport.project.DialogDisplayerImpl;
import org.netbeans.modules.apisupport.project.InstalledFileLocatorImpl;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.layers.LayerTestBase;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.support.ProjectOperations;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ContextGlobalProvider;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 * Test ModuleOperations.
 *
 * @author Martin Krauskopf
 */
public class ModuleOperationsTest extends TestBase {
    
    private static ContextGlobalProviderImpl cgpi = new ContextGlobalProviderImpl();
    
    static {
        System.setProperty("org.netbeans.core.startup.ModuleSystem.CULPRIT", "true");
        LayerTestBase.Lkp.setLookup(new Object[] {
            cgpi,
        });
        DialogDisplayerImpl.returnFromNotify(DialogDescriptor.NO_OPTION);
    }
    
    public ModuleOperationsTest(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        InstalledFileLocatorImpl.registerDestDir(destDirF);
    }
    
    public void testDelete() throws Exception {
        NbModuleProject project = generateStandaloneModule("module");
        project.open();
        ActionProvider ap = (ActionProvider) project.getLookup().lookup(ActionProvider.class);
        assertNotNull("have an action provider", ap);
        assertTrue("delete action is enabled", ap.isActionEnabled(ActionProvider.COMMAND_DELETE, null));
        
        FileObject prjDir = project.getProjectDirectory();
        
        FileObject buildXML = prjDir.getFileObject(GeneratedFilesHelper.BUILD_XML_PATH);
        prjDir.createData(".cvsignore");
        
        // build project
        ActionUtils.runTarget(buildXML, new String[] { "compile" }, null).waitFinished();
        assertNotNull("project was build", prjDir.getFileObject("build"));
        
        FileObject[] expectedMetadataFiles = new FileObject[] {
            buildXML,
            prjDir.getFileObject("manifest.mf"),
            prjDir.getFileObject("nbproject"),
            prjDir.getFileObject(".cvsignore"),
        };
        assertEquals("correct metadata files", Arrays.asList(expectedMetadataFiles), ProjectOperations.getMetadataFiles(project));
        
        FileObject[] expectedDataFiles = new FileObject[] {
            prjDir.getFileObject("src"),
            prjDir.getFileObject("test"),
        };
        assertEquals("correct data files", Arrays.asList(expectedDataFiles), ProjectOperations.getDataFiles(project));
        
        // It is hard to simulate exact scenario invoked by user. Let's test at least something.
        ProjectOperations.notifyDeleting(project);
        prjDir.getFileSystem().refresh(true);
        assertNull(prjDir.getFileObject("build"));
    }
    
    public void testOperationActions() throws Exception { // #72397
        NbModuleProject project = generateStandaloneModule("module");
        cgpi.setProject(project);
        DialogDisplayerImpl dd = (DialogDisplayerImpl) Lookup.getDefault().lookup(DialogDisplayer.class);
        dd.setDialog(new JDialog() {
            public void setVisible(boolean b) { /* do not show during test-run */ }
        });
        FileObject lock = FileUtil.createData(project.getProjectDirectory(), "build/testuserdir/lock");
        EventQueue.invokeAndWait(new Runnable() {
            public void run() {
                CommonProjectActions.deleteProjectAction().actionPerformed(null);
            }
        });
        assertNotNull("warning message emitted", dd.getLastNotifyDescriptor());
        assertEquals("warning message emitted", dd.getLastNotifyDescriptor().getMessage(),
                NbBundle.getMessage(ModuleOperationsTest.class, "ERR_ModuleIsBeingRun"));
        dd.reset();
        lock.delete();
        EventQueue.invokeAndWait(new Runnable() {
            public void run() {
                CommonProjectActions.deleteProjectAction().actionPerformed(null);
            }
        });
        assertNull("no warning message", dd.getLastNotifyDescriptor());
    }
    
    static final class ContextGlobalProviderImpl implements ContextGlobalProvider {
        
        private Lookup contextLookup;
        
        void setProject(final Project project) {
            contextLookup = Lookups.singleton(project);
        }
        
        public Lookup createGlobalContext() {
            return contextLookup;
        }
        
    }
    
}
