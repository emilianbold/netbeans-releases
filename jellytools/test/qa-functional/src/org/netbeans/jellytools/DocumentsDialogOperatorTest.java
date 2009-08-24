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
package org.netbeans.jellytools;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import junit.framework.TestSuite;
import org.netbeans.jellytools.actions.CopyAction;
import org.netbeans.jellytools.actions.DeleteAction;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.actions.PasteActionNoBlock;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTest;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.project.ui.actions.TestSupport;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/** Test DocumentsDialogOperator.
 *
 * @author Jiri.Skrivanek@sun.com
 */
public class DocumentsDialogOperatorTest extends JellyTestCase {
    
    public DocumentsDialogOperatorTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    static String[] tests = new String[] { 
                    "testInvoke",
                    "testVerify",
                    "testSelectDocument",
                    "testSelectDocuments",
                    "testGetDescription",
                    "testSaveDocuments",
                    "testCloseDocuments",
                    "testSwitchToDocument"
    };
    public static NbTest suite() {
        /*
        NbTestSuite suite = new NbTestSuite();
        // test cases have to be in particular order
        suite.addTest(new DocumentsDialogOperatorTest("testInvoke"));
        suite.addTest(new DocumentsDialogOperatorTest("testVerify"));
        suite.addTest(new DocumentsDialogOperatorTest("testSelectDocument"));
        suite.addTest(new DocumentsDialogOperatorTest("testSelectDocuments"));
        suite.addTest(new DocumentsDialogOperatorTest("testGetDescription"));
        suite.addTest(new DocumentsDialogOperatorTest("testSaveDocuments"));
        suite.addTest(new DocumentsDialogOperatorTest("testCloseDocuments"));
        suite.addTest(new DocumentsDialogOperatorTest("testSwitchToDocument"));
        return suite;
         */
        return (NbTest) NbModuleSuite.create(DocumentsDialogOperatorTest.class,
                ".*", ".*", tests);
    }
    
    /** Print out test name. */
    public void setUp() throws IOException {
        openDataProjects("SampleProject");
        closeOpenedProjects();
        openDataProjects("SampleProject");
    }
    
    private static DocumentsDialogOperator documentsOper;
    private static Node editableSourceNode;
    
    /**
     * Test of invoke method.
     */
    public void testInvoke() {
        EditorOperator.closeDiscardAll();
        Node sample1 = new Node(new SourcePackagesNode("SampleProject"), "sample1");  // NOI18N
        Node sample2 = new Node(new SourcePackagesNode("SampleProject"), "sample1.sample2");  // NOI18N
        Node node = new Node(sample1, "SampleClass1");// NOI18N
        new OpenAction().performAPI(node);
        node = new Node(sample2, "SampleClass2");// NOI18N
        new OpenAction().performAPI(node);
        // copy node to be able to write in
        new CopyAction().performAPI(node);
        new PasteActionNoBlock().performAPI(sample2);
        String copyClassTitle = Bundle.getString("org.netbeans.modules.refactoring.java.ui.Bundle", "LBL_CopyClass");
        NbDialogOperator copyClassOper = new NbDialogOperator(copyClassTitle);
        // "Refactor"
        String refactorLabel = Bundle.getStringTrimmed("org.netbeans.modules.refactoring.spi.impl.Bundle", "CTL_Finish");
        new JButtonOperator(copyClassOper, refactorLabel).push();
        copyClassOper.getTimeouts().setTimeout("Waiter.WaitingTime", 30000);
        copyClassOper.waitClosed();
        editableSourceNode = new Node(sample2, "SampleClass21");// NOI18N
        new OpenAction().performAPI(editableSourceNode);
        documentsOper = DocumentsDialogOperator.invoke();
    }
    
    /**
     * Test of verify method.
     */
    public void testVerify() {
        documentsOper.verify();
    }

    /**
     * Test of selectDocument method.
     */
    public void testSelectDocument() {
        documentsOper.selectDocument("SampleClass1.java"); // NOI18N
        assertEquals("Wrong document selected.", "SampleClass1.java", 
                     documentsOper.lstDocuments().getSelectedValue().toString());  // NOI18N
        documentsOper.selectDocument(2);
        assertEquals("Wrong document selected.", 2, documentsOper.lstDocuments().getSelectedIndex());  // NOI18N
    }
    
    /**
     * Test of selectDocuments method.
     */
    public void testSelectDocuments() {
        String[] documents = {"SampleClass1.java", "SampleClass2.java"}; // NOI18N
        documentsOper.selectDocuments(documents);
        Object[] selected = documentsOper.lstDocuments().getSelectedValues();
        for(int i = 0;i<selected.length;i++) {
            assertEquals("Wrong document selected by names.", documents[i], selected[i].toString());
        }
        // test one document
        documentsOper.selectDocuments(new String[] {"SampleClass21.java"}); // NOI18N
        assertEquals("Wrong document selected.", "SampleClass21.java", 
                     documentsOper.lstDocuments().getSelectedValue().toString());  // NOI18N
        
        int[] indexes = {0, 1};
        documentsOper.selectDocuments(indexes);
        assertTrue("Wrong documents selected by indexes.", 
                   Arrays.equals(indexes, documentsOper.lstDocuments().getSelectedIndices()));  // NOI18N
        // test one document
        documentsOper.selectDocuments(new int[] {2});
        assertEquals("Wrong document selected.", 2, documentsOper.lstDocuments().getSelectedIndex());  // NOI18N
    }

    /**
     * Test of getDescription method.
     */
    public void testGetDescription() {
        documentsOper.selectDocument("SampleClass1.java"); // NOI18N
        assertTrue("Wrong description obtain.", documentsOper.getDescription().indexOf("SampleClass1.java") > -1); // NOI18N
    }

    /**
     * Test of saveDocuments method.
     */
    public void testSaveDocuments() {
        EditorOperator eo = new EditorOperator("SampleClass21.java"); // NOI18N
        eo.insert("//dummy\n", 1, 1); // NOI18N
        documentsOper.selectDocument("SampleClass21.java");  // NOI18N
        documentsOper.saveDocuments();
        boolean modified = eo.isModified();
        eo.closeDiscard();
        assertFalse("Document is not saved.", modified);//NOI18N
    }
    
    /**
     * Test of closeDocuments method.
     */ 
    public void testCloseDocuments() {
        documentsOper.selectDocument("SampleClass2.java");  // NOI18N
        documentsOper.closeDocuments();
        assertTrue("Document was not closed.", documentsOper.lstDocuments().getModel().getSize() == 1);
    }
    
    /**
     * Test of switchToDocument method.
     */
    public void testSwitchToDocument() {
        documentsOper.selectDocument("SampleClass1.java"); //NOI18N
        documentsOper.switchToDocument();
        // clean up - delete editable source
        new DeleteAction().performAPI(editableSourceNode);
        // "Safe Delete"
        String safeDeleteTitle = Bundle.getString("org.netbeans.modules.refactoring.java.ui.Bundle", "LBL_SafeDel_Delete"); // NOI18N
        new NbDialogOperator(safeDeleteTitle).ok();
    }
    /*
    private static class P implements Project, ProjectConfigurationProvider {
        Lookup l = Lookups.singleton(this);
        FileObject fo;
        PC conf = new PC();
        
        public P(File dir) throws IOException {
            fo = FileUtil.createFolder(dir);
        }

        public FileObject getProjectDirectory() {
            return fo;
        }

        public Lookup getLookup() {
            return l;
        }

        public Collection getConfigurations() {
            return Collections.singleton(conf);
        }

        public ProjectConfiguration getActiveConfiguration() {
            return conf;
        }

        public void setActiveConfiguration(ProjectConfiguration configuration) throws IllegalArgumentException, IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean hasCustomizer() {
            return true;
        }

        public void customize() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean configurationsAffectAction(String command) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void addPropertyChangeListener(PropertyChangeListener lst) {
        }

        public void removePropertyChangeListener(PropertyChangeListener lst) {
        }
    }
    
    private static final class PC implements ProjectConfiguration {
        public String getDisplayName() {
            return "Default";
        }
        
    }
    */
}
