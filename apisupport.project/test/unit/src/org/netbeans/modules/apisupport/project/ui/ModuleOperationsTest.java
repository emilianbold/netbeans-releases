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

package org.netbeans.modules.apisupport.project.ui;

import java.awt.EventQueue;
import java.util.Arrays;
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
import org.openide.util.ContextAwareAction;
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
    
    protected @Override void setUp() throws Exception {
        super.setUp();
        InstalledFileLocatorImpl.registerDestDir(destDirF);
    }
    
    public void testDelete() throws Exception {
        NbModuleProject project = generateStandaloneModule("module");
        project.open();
        ActionProvider ap = project.getLookup().lookup(ActionProvider.class);
        assertNotNull("have an action provider", ap);
        assertTrue("delete action is enabled", ap.isActionEnabled(ActionProvider.COMMAND_DELETE, null));
        
        FileObject prjDir = project.getProjectDirectory();
        
        FileObject buildXML = prjDir.getFileObject(GeneratedFilesHelper.BUILD_XML_PATH);
        
        // build project
        ActionUtils.runTarget(buildXML, new String[] { "compile" }, null).waitFinished();
        assertNotNull("project was build", prjDir.getFileObject("build"));
        
        FileObject[] expectedMetadataFiles = {
            buildXML,
            prjDir.getFileObject("manifest.mf"),
            prjDir.getFileObject("nbproject"),
        };
        assertEquals("correct metadata files", Arrays.asList(expectedMetadataFiles), ProjectOperations.getMetadataFiles(project));
        
        FileObject[] expectedDataFiles = {
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
        final NbModuleProject project = generateStandaloneModule("module");
        cgpi.setProject(project);
        DialogDisplayerImpl dd = (DialogDisplayerImpl) Lookup.getDefault().lookup(DialogDisplayer.class);
        FileObject lock = FileUtil.createData(project.getProjectDirectory(), "build/testuserdir/lock");
        EventQueue.invokeAndWait(new Runnable() {
            public void run() {
                ((ContextAwareAction) CommonProjectActions.deleteProjectAction()).createContextAwareInstance(Lookups.singleton(project)).actionPerformed(null);
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
