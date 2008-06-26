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

import java.awt.Dialog;
import java.awt.Frame;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.netbeans.jemmy.util.NameComponentChooser;

/**
 * A JUnit test for DialogOperator.
 *
 * @author Manfred Riem (mriem@netbeans.org)
 * @version $Revision$
 */
public class DialogOperatorTest extends TestCase {
    /**
     * Stores the frame.
     */
    private Frame frame;
    
    /**
     * Stores the dialog.
     */
    private Dialog dialog;
    
    /**
     * Constructor.
     *
     * @param testName the name of the test.
     */
    public DialogOperatorTest(String testName) {
        super(testName);
    }

    /**
     * Setup before testing.
     *
     * @throws Exception when a major problem occurs.
     */
    protected void setUp() throws Exception {
        frame = new Frame();
        dialog = new Dialog(frame, "DialogOperatorTest");
        dialog.setName("DialogOperatorTest");
    }

    /**
     * Cleanup after testing.
     *
     * @throws Exception when a major problem occurs.
     */
    protected void tearDown() throws Exception {
        dialog.setVisible(false);
        frame.setVisible(false);
        
        dialog.dispose();
        frame.dispose();
    }

    /**
     * Suite method.
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(DialogOperatorTest.class);
        
        return suite;
    }

    /**
     * Test constructor.
     */
    public void testConstructor() {
        frame.setVisible(true);
        dialog.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        DialogOperator operator1 = new DialogOperator(operator);
        assertNotNull(operator1);
        
        DialogOperator operator2 = new DialogOperator();
        assertNotNull(operator2);
        
        DialogOperator operator3 = new DialogOperator(new NameComponentChooser("DialogOperatorTest"));
        assertNotNull(operator3);
        
        DialogOperator operator4 = new DialogOperator("DialogOperatorTest");
        assertNotNull(operator4);
        
        DialogOperator operator5 = new DialogOperator((WindowOperator) operator, new NameComponentChooser("DialogOperatorTest"));
        assertNotNull(operator5);
        
        DialogOperator operator6 = new DialogOperator((WindowOperator) operator, "DialogOperatorTest");
        assertNotNull(operator6);
    }
    
    /**
     * Test waitTitle method.
     */
    public void testWaitTitle() {
        frame.setVisible(true);
        dialog.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        DialogOperator operator1 = new DialogOperator(operator);
        assertNotNull(operator1);

        operator1.setTitle("BOOH");
        operator1.waitTitle("BOOH");
    }

    /**
     * Test getDump method.
     */
    public void testGetDump() {
        frame.setVisible(true);
        dialog.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        DialogOperator operator1 = new DialogOperator(operator);
        assertNotNull(operator1);
        
        operator1.getDump();
    }

    /**
     * Test getTitle method.
     */
    public void testGetTitle() {
        frame.setVisible(true);
        dialog.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        DialogOperator operator1 = new DialogOperator(operator);
        assertNotNull(operator1);

        operator1.getTitle();
    }

    /**
     * Test isModal method.
     */
    public void testIsModal() {
        frame.setVisible(true);
        dialog.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        DialogOperator operator1 = new DialogOperator(operator);
        assertNotNull(operator1);

        operator1.setModal(false);
        assertTrue(!operator1.isModal());
    }

    /**
     * Test isResizable method.
     */
    public void testIsResizable() {
        frame.setVisible(true);
        dialog.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        DialogOperator operator1 = new DialogOperator(operator);
        assertNotNull(operator1);

        operator1.setResizable(true);
        assertTrue(operator1.isResizable());
    }
}
