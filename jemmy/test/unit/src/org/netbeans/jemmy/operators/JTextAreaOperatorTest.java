/*
 * $Id$
 *
 * ---------------------------------------------------------------------------
 *
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
 * Contributor(s): Manfred Riem (mriem@netbeans.org).
 *
 * The Original Software is the Jemmy library. The Initial Developer of the
 * Original Software is Alexandre Iline. All Rights Reserved.
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
 * ---------------------------------------------------------------------------
 *
 */
package org.netbeans.jemmy.operators;

import javax.swing.JFrame;
import javax.swing.JTextArea;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.util.NameComponentChooser;

/**
 * A JUnit test for JTextAreaOperator.
 *
 * @author Manfred Riem (mriem@netbeans.org)
 * @version $Revision$
 */
public class JTextAreaOperatorTest extends TestCase {
    /**
     * Stores the frame we use.
     */
    private JFrame frame;
    
    /**
     * Stores the text area we use.
     */
    private JTextArea textArea;
    
    /**
     * Constructor.
     *
     * @param testName the name of the test.
     */
    public JTextAreaOperatorTest(String testName) {
        super(testName);
    }

    /**
     * Setup before testing.
     */
    protected void setUp() throws Exception {
        frame = new JFrame();
        textArea = new JTextArea("JTextAreaOperatorTest");
        textArea.setName("JTextAreaOperatorTest");
        frame.getContentPane().add(textArea);
        frame.pack();
        frame.setLocationRelativeTo(null);
    }

    /**
     * Cleanup after testing.
     */
    protected void tearDown() throws Exception {
        frame.setVisible(false);
    }

    /**
     * Suite method.
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(JTextAreaOperatorTest.class);
        
        return suite;
    }
    
    /**
     * Test constructor.
     */
    public void testConstructor() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTextAreaOperator operator2 = new JTextAreaOperator(operator);
        assertNotNull(operator2);
        
        JTextAreaOperator operator3 = new JTextAreaOperator(operator, new NameComponentChooser("JTextAreaOperatorTest"));
        assertNotNull(operator3);
        
        JTextAreaOperator operator4 = new JTextAreaOperator(operator, "JTextAreaOperatorTest");
        assertNotNull(operator4);
    }

    /**
     * Test findJTextArea method.
     */
    public void testFindJTextArea() {
        frame.setVisible(true);
        
        JTextArea textArea1 = JTextAreaOperator.findJTextArea(frame, "JTextAreaOperatorTest", false, false);
        assertNotNull(textArea1);
        
        JTextArea textArea2 = JTextAreaOperator.findJTextArea(frame, new NameComponentChooser("JTextAreaOperatorTest"));
        assertNotNull(textArea2);
    }

    /**
     * Test waitJTextArea method.
     */
    public void testWaitJTextArea() {
        frame.setVisible(true);
        
        JTextArea textArea1 = JTextAreaOperator.waitJTextArea(frame, "JTextAreaOperatorTest", false, false);
        assertNotNull(textArea1);
        
        JTextArea textArea2 = JTextAreaOperator.waitJTextArea(frame, new NameComponentChooser("JTextAreaOperatorTest"));
        assertNotNull(textArea2);
    }

    /**
     * Test usePageNavigationKeys method.
     */
    public void testUsePageNavigationKeys() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTextAreaOperator operator2 = new JTextAreaOperator(operator);
        assertNotNull(operator2);

        operator2.usePageNavigationKeys(true);
    }

    /**
     * Test changeCaretRow method.
     */
    public void testChangeCaretRow() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTextAreaOperator operator2 = new JTextAreaOperator(operator);
        assertNotNull(operator2);
        
        operator2.changeCaretRow(0);
    }

    /**
     * Test changeCaretPosition method.
     */
    public void testChangeCaretPosition() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTextAreaOperator operator2 = new JTextAreaOperator(operator);
        assertNotNull(operator2);
        
        operator2.changeCaretPosition(0, 0);
    }

    /**
     * Test typeText method.
     */
    public void testTypeText() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTextAreaOperator operator2 = new JTextAreaOperator(operator);
        assertNotNull(operator2);
        
        operator2.typeText("Booh!", 0, 0);
    }

    /**
     * Test selectText method.
     */
    public void testSelectText() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTextAreaOperator operator2 = new JTextAreaOperator(operator);
        assertNotNull(operator2);
        
        operator2.selectText("JTextAreaOperatorTest");
    }

    /**
     * Test selectLines method.
     */
    public void testSelectLines() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTextAreaOperator operator2 = new JTextAreaOperator(operator);
        assertNotNull(operator2);

        operator2.selectLines(0, 0);
    }

    /**
     * Test getDump method.
     */
    public void testGetDump() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTextAreaOperator operator2 = new JTextAreaOperator(operator);
        assertNotNull(operator2);

        assertNotNull(operator2.getDump());
    }

    /**
     * Test append method.
     */
    public void testAppend() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTextAreaOperator operator2 = new JTextAreaOperator(operator);
        assertNotNull(operator2);

        operator2.append("Booh!");
    }

    /**
     * Test getColumns method.
     */
    public void testGetColumns() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTextAreaOperator operator2 = new JTextAreaOperator(operator);
        assertNotNull(operator2);

        operator2.setColumns(2);
        assertEquals(2, operator2.getColumns());
    }

    /**
     * Test getLineCount method.
     */
    public void testGetLineCount() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTextAreaOperator operator2 = new JTextAreaOperator(operator);
        assertNotNull(operator2);

        operator2.getLineCount();
    }

    /**
     * Test getLineWrap method.
     */
    public void testGetLineWrap() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTextAreaOperator operator2 = new JTextAreaOperator(operator);
        assertNotNull(operator2);

        operator2.setLineWrap(true);
        assertTrue(operator2.getLineWrap());
        
        operator2.setLineWrap(false);
        assertTrue(!operator2.getLineWrap());
    }

    /**
     * Test getRows method.
     */
    public void testGetRows() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTextAreaOperator operator2 = new JTextAreaOperator(operator);
        assertNotNull(operator2);

        operator2.setRows(1);
        assertEquals(1, operator2.getRows());
    }

    /**
     * Test getTabSize method.
     */
    public void testGetTabSize() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTextAreaOperator operator2 = new JTextAreaOperator(operator);
        assertNotNull(operator2);

        operator2.setTabSize(11);
        assertEquals(11, operator2.getTabSize());
    }

    /**
     * Test getWrapStyleWord method.
     */
    public void testGetWrapStyleWord() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTextAreaOperator operator2 = new JTextAreaOperator(operator);
        assertNotNull(operator2);

        operator2.setWrapStyleWord(true);
        assertTrue(operator2.getWrapStyleWord());
        
        operator2.setWrapStyleWord(false);
        assertTrue(!operator2.getWrapStyleWord());
    }

    /**
     * Test insert method.
     */
    public void testInsert() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTextAreaOperator operator2 = new JTextAreaOperator(operator);
        assertNotNull(operator2);

        operator2.insert("Booh!", 0);
    }

    /**
     * Test replaceRange method.
     */
    public void testReplaceRange() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTextAreaOperator operator2 = new JTextAreaOperator(operator);
        assertNotNull(operator2);

        operator2.replaceRange("Booh!", 0, 0);
    }
}
