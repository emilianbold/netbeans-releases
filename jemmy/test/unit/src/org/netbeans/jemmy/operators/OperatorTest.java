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
import java.awt.Panel;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.util.NameComponentChooser;
import org.netbeans.jemmy.util.RegExComparator;

/**
 * A JUnit test for Operator.
 *
 * @author Manfred Riem (mriem@manorrock.org)
 * @version $Revision$
 */
public class OperatorTest extends TestCase {
    /**
     * Stores the frame.
     */
    private Frame frame;
    
    /**
     * Stores the panel.
     */
    private Panel panel;
    
    /**
     * Constructor.
     *
     * @param testName the name of the test.
     */
    public OperatorTest(String testName) {
        super(testName);
    }

    /**
     * Setup before testing.
     *
     * @throws Exception when a serious error occurs.
     */
    protected void setUp() throws Exception {
        frame = new Frame();
        panel = new Panel();
        panel.setName("OperatorTest");
        frame.add(panel);
        frame.setLocationRelativeTo(null);
    }

    /**
     * Cleanup after testing.
     *
     * @throws Exception when serious error occurs.
     */
    protected void tearDown() throws Exception {
        frame.setVisible(true);
        frame.dispose();
        frame = null;
    }

    /**
     * Suite method.
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(OperatorTest.class);
        
        return suite;
    }
    
    /**
     * Test constructor.
     */
    public void testConstructor() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ContainerOperator operator1 = new ContainerOperator(operator);
        assertNotNull(operator1);
    }

    /**
     * Test isCaptionEqual method.
     */
    public void testIsCaptionEqual() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ContainerOperator operator1 = new ContainerOperator(operator);
        assertNotNull(operator1);
        
        operator1.isCaptionEqual("", "");
        operator1.isCaptionEqual("", "", false, false);
        operator1.isCaptionEqual("", "", new RegExComparator());
    }

    /**
     * Test getParentPath method.
     */
    public void testGetParentPath() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ContainerOperator operator1 = new ContainerOperator(operator);
        assertNotNull(operator1);
        
        operator1.getParentPath(new ComponentChooser[] { new NameComponentChooser("1") } );
    }

    /**
     * Test getCharsKeys method.
     */
    public void testGetCharsKeys() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ContainerOperator operator1 = new ContainerOperator(operator);
        assertNotNull(operator1);
        
        operator1.getCharsKeys("");
        operator1.getCharsKeys(new char[] { 'a', 'b'});
    }

    /**
     * Test getCharsModifiers method.
     */
    public void testGetCharsModifiers() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ContainerOperator operator1 = new ContainerOperator(operator);
        assertNotNull(operator1);
        
        operator1.getCharsModifiers("");
        operator1.getCharsModifiers(new char[] { 'b', 'b'} );
    }

    /**
     * Test printDump method.
     */
    public void testPrintDump() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ContainerOperator operator1 = new ContainerOperator(operator);
        assertNotNull(operator1);
        
        operator1.printDump();
    }

    /**
     * Test getDump method.
     */
    public void testGetDump() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ContainerOperator operator1 = new ContainerOperator(operator);
        assertNotNull(operator1);
        
        operator1.getDump();
    }

    /**
     * Test unlockAndThrow method.
     */
    public void testUnlockAndThrow() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ContainerOperator operator1 = new ContainerOperator(operator);
        assertNotNull(operator1);
        
        try {
            operator1.unlockAndThrow(new Exception(""));
            fail();
        }
        catch(Exception exception) {
        }
    }
    
    /**
     * Test runMapping method.
     */
    public void testRunMapping() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ContainerOperator operator1 = new ContainerOperator(operator);
        assertNotNull(operator1);
    }
}
