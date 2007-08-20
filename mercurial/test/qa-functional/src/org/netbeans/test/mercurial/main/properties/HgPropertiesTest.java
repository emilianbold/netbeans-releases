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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.test.mercurial.main.properties;

import java.io.File;
import java.io.PrintStream;
import junit.textui.TestRunner;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.test.mercurial.operators.HgPropertiesOperator;
import org.netbeans.test.mercurial.operators.VersioningOperator;
import org.netbeans.test.mercurial.utils.RepositoryMaintenance;
import org.netbeans.test.mercurial.utils.TestKit;

/**
 *
 * @author novakm
 */
public class HgPropertiesTest extends JellyTestCase {

    public static final String PROJECT_NAME = "JavaApp";
    public PrintStream stream;
    String os_name;

    public HgPropertiesTest(String name) {
        super(name);
    }

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

    public static void main(String[] args) {
        // TODO code application logic here
        TestRunner.run(suite());
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new HgPropertiesTest("HgPropertiesTest"));
        return suite;
    }

    public void HgPropertiesTest() throws Exception {
        try {
            TestKit.closeProject(PROJECT_NAME);

            stream = new PrintStream(new File(getWorkDir(), getName() + ".log"));
            TestKit.loadOpenProject(PROJECT_NAME, getDataDir());

            // set hgProperty for file
            Node node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp|Main.java");
            HgPropertiesOperator hgpo = HgPropertiesOperator.invoke(node);
            Thread.sleep(2000);
            // username should be in the table.
            assertEquals("Wrong row count of table at start.", 1, hgpo.propertiesTable().getRowCount());
            hgpo.typePropertyName("default-push");
            hgpo.typePropertyValue("fileValue");
            hgpo.add();
            Thread.sleep(500);
            assertEquals("Wrong row count of table.", 2, hgpo.propertiesTable().getRowCount());
            Thread.sleep(500);
            
            hgpo.propertiesTable().selectCell(1, 0);
            hgpo.remove();
            Thread.sleep(500);
            assertEquals("Wrong row count of table after remove.", 1, hgpo.propertiesTable().getRowCount());
            hgpo.cancel();
        } catch (Exception e) {
            throw new Exception("Test failed: " + e);
        } finally {
            TestKit.closeProject(PROJECT_NAME);
        }
    }
}
