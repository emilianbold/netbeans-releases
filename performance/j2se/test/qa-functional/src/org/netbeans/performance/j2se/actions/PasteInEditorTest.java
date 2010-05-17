/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.performance.j2se.actions;

import java.awt.event.KeyEvent;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;
import javax.swing.JEditorPane;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.j2se.setup.J2SESetup;

import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.EditorWindowOperator;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.actions.Action.Shortcut;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbModuleSuite;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;

/**
 * Test of Paste text to opened source editor.
 *
 * @author  anebuzelsky@netbeans.org, mmirilovic@netbeans.org
 */
public class PasteInEditorTest extends PerformanceTestCase {
    
    private EditorOperator editorOperator1, editorOperator2;
    
    /** Creates a new instance of PasteInEditor */
    public PasteInEditorTest(String testName) {
        super(testName);
        expectedTime = UI_RESPONSE;
        WAIT_AFTER_OPEN=200;
    }
    
    /** Creates a new instance of PasteInEditor */
    public PasteInEditorTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = UI_RESPONSE;
        WAIT_AFTER_OPEN=200;
    }

    public static NbTestSuite suite() {
        CountingSecurityManager.initialize("non-existing");
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(J2SESetup.class)
             .addTest(PasteInEditorTest.class)
             .enableModules(".*").clusters(".*")));
        return suite;
    }

    public void testPasteInEditor(){
        doMeasurement();
    }    
    
    @Override
    public void initialize() {
        EditorOperator.closeDiscardAll();
        repaintManager().addRegionFilter(repaintManager().EDITOR_FILTER);
        SourcePackagesNode sourcePackagesNode = new SourcePackagesNode("PerformanceTestData");
        new OpenAction().performAPI(new Node(sourcePackagesNode, "org.netbeans.test.performance|Main20kB.java"));
        editorOperator1 = EditorWindowOperator.getEditor("Main20kB.java");
        new OpenAction().performAPI(new Node(sourcePackagesNode, "org.netbeans.test.performance|TestClassForCopyPaste.java"));
        editorOperator2 = EditorWindowOperator.getEditor("TestClassForCopyPaste.java");
    }
    
    public void prepare() {
        editorOperator1.makeComponentVisible();
        editorOperator1.select(53,443);
        new ActionNoBlock(null, null, new Shortcut(KeyEvent.VK_C, KeyEvent.CTRL_MASK)).perform(editorOperator1);
        editorOperator2.makeComponentVisible();
        editorOperator2.setCaretPositionToLine(29);
        EditorCookie ec = Utilities.actionsGlobalContext().lookup(EditorCookie.class);
        assertNotNull("Editor is selected", ec);
        int len = ec.getDocument().getLength();
        JEditorPane[] arr = ec.getOpenedPanes();
        assertNotNull("Pane opened", arr);
        assertEquals("One Pane opened", 1, arr.length);
        arr[0].setCaretPosition(len);
        try {
            SourceUtils.waitScanFinished();
        } catch (InterruptedException ex) {
            fail("No interrupts please");
        }

        FileObject fo = Utilities.actionsGlobalContext().lookup(FileObject.class);
        assertNotNull("File object found", fo);
        assertEquals("Correct name", "TestClassForCopyPaste.java", fo.getNameExt());
        File dir = FileUtil.toFile(fo.getParent());
        assertNotNull("Directory is backed by java.io.File", dir);

        try {
            ec.saveDocument();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        assertFalse("not modified yet", ec.isModified());
        AtomicLong l = new AtomicLong();
        editorOperator2.pushKey(KeyEvent.VK_ENTER);
        assertTrue("modified now", ec.isModified());
        CountingSecurityManager.initialize(dir.getPath());
        editorOperator2.pushKey(KeyEvent.VK_ENTER);
        CountingSecurityManager.assertCounts("The file is touched twice in our current state, could be zero if we tried more, see issue 171330", 4, l);
        assertTrue("still modified", ec.isModified());
        try {
            ec.saveDocument();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        assertFalse("no longer modified", ec.isModified());
   }
    
    public ComponentOperator open(){
        new ActionNoBlock(null, null, new Shortcut(KeyEvent.VK_V, KeyEvent.CTRL_MASK)).perform(editorOperator2);
        return null;
    }
    
    @Override
    public void shutdown() {
        editorOperator2.closeDiscard();
        editorOperator1.closeDiscard();
        repaintManager().resetRegionFilters();
    }
    
}
