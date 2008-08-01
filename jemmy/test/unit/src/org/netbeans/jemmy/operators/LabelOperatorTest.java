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

import java.awt.Frame;
import java.awt.Label;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.netbeans.jemmy.util.NameComponentChooser;

/**
 * A JUnit test for LabelOperator.
 *
 * @author Manfred Riem (mriem@netbeans.org)
 * @version $Revision$
 */
public class LabelOperatorTest extends TestCase {
    /**
     * Stores the frame.
     */
    private Frame frame;
    
    /**
     * Stores the label.
     */
    private Label label;
    
    /**
     * Constructor.
     *
     * @param testName the name of the test.
     */
    public LabelOperatorTest(String testName) {
        super(testName);
    }

    /**
     * Setup before testing.
     *
     * @throws Exception when a major problem occurs.
     */
    protected void setUp() throws Exception {
        frame = new Frame();
        label = new Label("LabelOperatorTest");
        label.setName("LabelOperatorTest");
        frame.add(label);
    }

    /**
     * Cleanup after testing.
     *
     * @throws Exception when a major problem occurs.
     */
    protected void tearDown() throws Exception {
        frame.setVisible(false);
        frame.dispose();
        frame = null;
    }

    /**
     * Suite method.
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(LabelOperatorTest.class);
        
        return suite;
    }
    
    /**
     * Test constructor.
     */
    public void testConstructor() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        LabelOperator operator1 = new LabelOperator(operator);
        assertNotNull(operator1);
        
        LabelOperator operator2 = new LabelOperator(operator, new NameComponentChooser("LabelOperatorTest"));
        assertNotNull(operator2);
        
        LabelOperator operator3 = new LabelOperator(operator, "LabelOperatorTest");
        assertNotNull(operator3);
    }

    /**
     * Test findLabel method.
     */
    public void testFindLabel() {
        frame.setVisible(true);
        
        Label label = LabelOperator.findLabel(frame, "LabelOperatorTest", false, false);
        assertNotNull(label);
        
        Label label2 = LabelOperator.findLabel(frame, new NameComponentChooser("LabelOperatorTest"));
        assertNotNull(label2);
    }

    /**
     * Test waitLabel method.
     */
    public void testWaitLabel() {
        frame.setVisible(true);
        
        Label label = LabelOperator.waitLabel(frame, "LabelOperatorTest", false, false);
        assertNotNull(label);
        
        Label label2 = LabelOperator.waitLabel(frame, new NameComponentChooser("LabelOperatorTest"));
        assertNotNull(label2);
    }

    /**
     * Test getDump method.
     */
    public void testGetDump() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        LabelOperator operator1 = new LabelOperator(operator);
        assertNotNull(operator1);
        
        operator1.getDump();
    }

    /**
     * Test getAlignment method.
     */
    public void testGetAlignment() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        LabelOperator operator1 = new LabelOperator(operator);
        assertNotNull(operator1);
        
        operator1.setAlignment(operator1.getAlignment());
    }

    /**
     * Test getText method.
     */
    public void testGetText() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        LabelOperator operator1 = new LabelOperator(operator);
        assertNotNull(operator1);
        
        operator1.setText(operator1.getText());
    }
}
