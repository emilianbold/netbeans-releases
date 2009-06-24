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
package org.netbeans.modules.cnd.makeproject.ui.wizards;

import java.io.File;
import java.io.IOException;
import org.netbeans.modules.cnd.actions.MakeAction;
import org.netbeans.modules.cnd.api.execution.ExecutionListener;
import org.netbeans.modules.cnd.builds.MakeExecSupport;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.test.CndBaseTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.TemplateWizard;
import static org.junit.Assert.*;
import org.junit.Test;
import org.openide.nodes.Node;
import org.openide.util.Utilities;

public class MakeSampleProjectIteratorTest extends CndBaseTestCase {

    private boolean done = false;
    private int build_rc = -1;

    public MakeSampleProjectIteratorTest(String name) {
        super(name);
    }

    @Test
    public void testArguments() throws IOException {
        testSample("Arguments", "Arguments", "all");
    }

    @Test
    public void testInputOutput() throws IOException {
        testSample("InputOutput", "InputOutput", "all");
    }

    @Test
    public void testWelcome() throws IOException {
        testSample("Welcome", "Welcome", "all");
    }

    @Test
    public void testQuote() throws IOException {
        testSample("Quote", "Quote", "all");
    }

    @Test
    public void testPi() throws IOException {
        if (Utilities.getOperatingSystem() == Utilities.OS_SOLARIS) {
            testSample("Pi", "Pi", "all");
        }
    }

    @Test
    public void testFreeway() throws IOException {
        if (Utilities.getOperatingSystem() == Utilities.OS_SOLARIS) {
            testSample("Freeway", "Freeway", "all");
        }
    }

    @Test
    public void testFractal() throws IOException {
        testSample("Fractal", "Fractal", "all");
    }

    @Test
    public void testLexYacc() throws IOException {
        testSample("LexYacc", "LexYacc", "all");
    }

    @Test
    public void testMP() throws IOException {
        testSample("MP", "MP", "all");
    }

    private static void instantiateSample(String name, File destdir) throws IOException {
        FileObject templateFO = FileUtil.getConfigFile("Templates/Project/Samples/Native/" + name);
        assertNotNull("FileObject for " + name + " sample not found", templateFO);
        DataObject templateDO = DataObject.find(templateFO);
        assertNotNull("DataObject for " + name + " sample not found", templateDO);
        MakeSampleProjectIterator projectCreator = new MakeSampleProjectIterator();
        TemplateWizard wiz = new TemplateWizard();
        wiz.setTemplate(templateDO);
        projectCreator.initialize(wiz);
        wiz.putProperty("name", destdir.getName());
        wiz.putProperty("projdir", destdir);
        projectCreator.instantiate(wiz);
    }

    public void testSample(String projectName, String sample, String target) throws IOException {
        File workDir = getWorkDir();//new File("/tmp");
        File projectDir = new File(workDir, projectName);
        instantiateSample(sample, projectDir);

        FileObject projectDirFO = FileUtil.toFileObject(projectDir);
        ConfigurationDescriptorProvider descriptorProvider = new ConfigurationDescriptorProvider(projectDirFO);
        MakeConfigurationDescriptor descriptor = descriptorProvider.getConfigurationDescriptor(true);
        descriptor.save(); // make sure all necessary configuration files in nbproject/ are written

        File makefile = new File(projectDir, "Makefile");
        FileObject makefileFileObject = FileUtil.toFileObject(makefile);
        assertTrue("makefileFileObject == null", makefileFileObject != null);
        DataObject dObj = null;
        try {
            dObj = DataObject.find(makefileFileObject);
        } catch (DataObjectNotFoundException ex) {
        }
        assertTrue("DataObjectNotFoundException", dObj != null);
        Node node = dObj.getNodeDelegate();
        assertTrue("node == null", node != null);

        MakeExecSupport ses = node.getCookie(MakeExecSupport.class);
        assertTrue("ses == null", ses != null);

        MakeAction makeAction = new MakeAction();
        build_rc = -1;
        done = false;
        makeAction.execute(node, target, new MyExecutionListener(), null, null, null);

        while (!done) {
            try {
                Thread.currentThread().sleep(1000);
            } catch (Exception e) {
            }
        }
        assertTrue("build failed - rc = " + build_rc, build_rc == 0);
    }

    class MyExecutionListener implements ExecutionListener {

        public void executionFinished(int rc) {
            build_rc = rc;
            done = true;
        }

        public void executionStarted() {
            done = false;
        }
    }
}
