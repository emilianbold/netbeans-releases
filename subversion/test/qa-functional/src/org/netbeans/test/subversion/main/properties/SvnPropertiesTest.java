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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.test.subversion.main.properties;

import java.io.File;
import java.io.PrintStream;
import junit.framework.Test;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.OutputOperator;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.test.subversion.operators.CheckoutWizardOperator;
import org.netbeans.test.subversion.operators.RepositoryStepOperator;
import org.netbeans.test.subversion.operators.SvnPropertiesOperator;
import org.netbeans.test.subversion.operators.VersioningOperator;
import org.netbeans.test.subversion.operators.WorkDirStepOperator;
import org.netbeans.test.subversion.utils.RepositoryMaintenance;
import org.netbeans.test.subversion.utils.TestKit;

/**
 *
 * @author novakm
 */
public class SvnPropertiesTest extends JellyTestCase {

    public static final String TMP_PATH = "/tmp";
    public static final String REPO_PATH = "repo";
    public static final String WORK_PATH = "work";
    public static final String PROJECT_NAME = "JavaApp";
    public File projectPath;
    public PrintStream stream;
    String os_name;

    public SvnPropertiesTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        os_name = System.getProperty("os.name");
        //System.out.println(os_name);
        System.out.println("### " + getName() + " ###");
    }

    protected boolean isUnix() {
        boolean unix = false;
        if (os_name.indexOf("Windows") == -1) {
            unix = true;
        }
        return unix;
    }
    
    public static Test suite() {
         return NbModuleSuite.create(
                 NbModuleSuite.createConfiguration(SvnPropertiesTest.class).addTest(
                    "SvnPropertiesTest"
                 )
                 .enableModules(".*")
                 .clusters(".*")
        );
     }

    public void SvnPropertiesTest() throws Exception {
        try {
            stream = new PrintStream(new File(getWorkDir(), getName() + ".log"));
            VersioningOperator.invoke();
            OutputOperator.invoke();
            TestKit.showStatusLabels();
            CheckoutWizardOperator.invoke();
            RepositoryStepOperator rso = new RepositoryStepOperator();

            //create repository...
            File work = new File(TMP_PATH + File.separator + WORK_PATH + File.separator + "w" + System.currentTimeMillis());
            new File(TMP_PATH).mkdirs();
            work.mkdirs();
            RepositoryMaintenance.deleteFolder(new File(TMP_PATH + File.separator + REPO_PATH));
            RepositoryMaintenance.createRepository(TMP_PATH + File.separator + REPO_PATH);
            RepositoryMaintenance.loadRepositoryFromFile(TMP_PATH + File.separator + REPO_PATH, getDataDir().getCanonicalPath() + File.separator + "repo_dump");
            rso.setRepositoryURL(RepositoryStepOperator.ITEM_FILE + RepositoryMaintenance.changeFileSeparator(TMP_PATH + File.separator + REPO_PATH, false));

            rso.next();
            WorkDirStepOperator wdso = new WorkDirStepOperator();
            wdso.setRepositoryFolder("trunk/" + PROJECT_NAME);
            wdso.setLocalFolder(work.getCanonicalPath());
            wdso.checkCheckoutContentOnly(false);
            wdso.finish();

            //open project
            OutputTabOperator oto = new OutputTabOperator("file:///tmp/repo");
            oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
            oto.waitText("Checking out... finished.");
            NbDialogOperator nbdialog = new NbDialogOperator("Checkout Completed");
            JButtonOperator open = new JButtonOperator(nbdialog, "Open Project");
            open.push();
            TestKit.waitForScanFinishedAndQueueEmpty();

            // set svnProperty for file
            Node node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp|Main.java");
            Thread.sleep(3000);
            SvnPropertiesOperator spo = SvnPropertiesOperator.invoke(node);
            spo.typePropertyName("fileName");
            spo.typePropertyValue("fileValue");
            spo.add();
            Thread.sleep(1000);
            assertEquals("1. Wrong row count of table.", 1, spo.propertiesTable().getRowCount());
            assertFalse("Recursively checkbox should be disabled on file! ", spo.cbRecursively().isEnabled());
            Thread.sleep(1000);
            spo.cancel();
            Thread.sleep(1000);
            //  set svnProperty for folder - one recursive and one nonrecursive
            node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp");
            spo = SvnPropertiesOperator.invoke(node);
            assertTrue("Recursively checkbox should be enabled on package! ", spo.cbRecursively().isEnabled());
            spo.checkRecursively(false);
            spo.typePropertyName("nonrecursiveName");
            spo.typePropertyValue("nonrecursiveValue");
            spo.add();
            Thread.sleep(1000);
            spo.checkRecursively(true);
            spo.typePropertyName("recursiveName");
            spo.typePropertyValue("recursiveValue");
            spo.add();
            spo.refresh();
            Thread.sleep(1000);
            assertEquals("2. Wrong row count of table.", 2, spo.propertiesTable().getRowCount());
            spo.cancel();

            //  verify whether the recursive property is present on file
            node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp|Main.java");
            spo = SvnPropertiesOperator.invoke(node);
            Thread.sleep(1000);
            assertEquals("3. Wrong row count of table.", 2, spo.propertiesTable().getRowCount());
            assertEquals("Expected file is missing.", "recursiveName", spo.propertiesTable().getModel().getValueAt(1, 0).toString());
            spo.propertiesTable().selectCell(1, 0);
            spo.remove();
            spo.refresh();
            Thread.sleep(5000);
            assertEquals("4. Wrong row count of table.", 1, spo.propertiesTable().getRowCount());
            spo.cancel();
        } finally {
            TestKit.closeProject(PROJECT_NAME);
        }
    }
}
