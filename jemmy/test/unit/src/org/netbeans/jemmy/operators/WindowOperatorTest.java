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
import java.awt.Window;
import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.netbeans.jemmy.util.NameComponentChooser;

/**
 * A JUnit test for WindowOperator.
 *
 * @author Manfred Riem (mriem@netbeans.org)
 * @version $Revision$
 */
public class WindowOperatorTest extends TestCase {
    /**
     * Stores the frame.
     */
    private Frame frame;
    
    /**
     * Stores the sub window.
     */
    private Dialog subWindow;
    
    /**
     * Constructor.
     *
     * @param testName the name of the test.
     */
    public WindowOperatorTest(String testName) {
        super(testName);
    }

    /**
     * Setup before testing.
     *
     * @throws Exception when a serious error occurs.
     */
    protected void setUp() throws Exception {
        frame = new Frame();
        frame.setName("WindowOperatorTest");
        frame.setLocationRelativeTo(null);
        
        subWindow = new Dialog(frame, false);
        subWindow.setName("SubWindow");
        subWindow.setLocationRelativeTo(null);
    }

    /**
     * Cleanup after testing.
     *
     * @throws Exception when a serious error occurs.
     */
    protected void tearDown() throws Exception {
        frame.setVisible(false);
        frame.dispose();
        frame = null;
        
        subWindow.setVisible(false);
        subWindow.dispose();
        subWindow = null;
    }

    /**
     * Suite method.
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(WindowOperatorTest.class);
        
        return suite;
    }
    
    /**
     * Test constructor.
     */
    public void testConstructor() {
        frame.setVisible(true);
        subWindow.setVisible(true);
        
        WindowOperator operator = new WindowOperator();
        assertNotNull(operator);
        
        FrameOperator operator1 = new FrameOperator();
        assertNotNull(operator1);
        
        WindowOperator operator2 = new WindowOperator(operator1);
        assertNotNull(operator2);
        
        WindowOperator operator3 = new WindowOperator(operator1, new NameComponentChooser("SubWindow"));
        assertNotNull(operator3);
    }

    /**
     * Test findWindow method.
     */
    public void testFindWindow() {
        frame.setVisible(true);
        subWindow.setVisible(true);
        
        Window window1 = WindowOperator.findWindow(new NameComponentChooser("WindowOperatorTest"));
        assertNotNull(window1);
        
        Window window2 = WindowOperator.findWindow(frame, new NameComponentChooser("SubWindow"));
        assertNotNull(window2);
    }

    /**
     * Test waitWindow method.
     */
    public void testWaitWindow() {
        frame.setVisible(true);
        subWindow.setVisible(true);
        
        Window window1 = WindowOperator.waitWindow(new NameComponentChooser("WindowOperatorTest"));
        assertNotNull(window1);
        
        Window window2 = WindowOperator.waitWindow(frame, new NameComponentChooser("SubWindow"));
        assertNotNull(window2);
    }

    /**
     * Test activate method.
     */
    public void testActivate() {
        frame.setVisible(true);
        
        WindowOperator operator = new WindowOperator();
        assertNotNull(operator);
        
        operator.activate();
    }

    /**
     * Test close method.
     */
    public void testClose() {
        frame.setVisible(true);
        
        WindowOperator operator = new WindowOperator();
        assertNotNull(operator);

        operator.close();
    }

    /**
     * Test move method.
     */
    public void testMove() {
        frame.setVisible(true);
        
        WindowOperator operator = new WindowOperator();
        assertNotNull(operator);

        operator.move(100, 100);
    }

    /**
     * Test resize method.
     */
    public void testResize() {
        frame.setVisible(true);
        
        WindowOperator operator = new WindowOperator();
        assertNotNull(operator);
        
        operator.resize(100, 100);
    }

    /**
     * Test findSubWindow method.
     */
    public void testFindSubWindow() {
        frame.setVisible(true);
        subWindow.setVisible(true);
        
        WindowOperator operator = new WindowOperator();
        assertNotNull(operator);

        Window window = operator.findSubWindow(new NameComponentChooser("SubWindow"));
        assertNotNull(window);
    }

    /**
     * Test waitSubWindow method.
     */
    public void testWaitSubWindow() {
        frame.setVisible(true);
        subWindow.setVisible(true);
        
        WindowOperator operator = new WindowOperator();
        assertNotNull(operator);

        Window window = operator.waitSubWindow(new NameComponentChooser("SubWindow"));
        assertNotNull(window);
    }

    /**
     * Test waitClosed method.
     */
    public void testWaitClosed() {
        frame.setVisible(true);
        
        WindowOperator operator = new WindowOperator();
        assertNotNull(operator);

        operator.close();
        operator.waitClosed();
    }

    /**
     * Test addWindowListener method.
     */
    public void testAddWindowListener() {
        frame.setVisible(true);
        
        WindowOperator operator = new WindowOperator();
        assertNotNull(operator);

        operator.addWindowListener(null);
    }

    /**
     * Test applyResourceBundle method.
     */
    public void testApplyResourceBundle() {
        frame.setVisible(true);
        
        WindowOperator operator = new WindowOperator();
        assertNotNull(operator);

        try {
            operator.applyResourceBundle("");
        }
        catch(Exception exception) {
        }
        
        operator.applyResourceBundle(new ResourceBundleTest());
    }
    
    /**
     * Inner class for testing.
     */
    public class ResourceBundleTest extends ResourceBundle {
        public Locale getLocale() {
            return Locale.US;
        }
        
        protected Object handleGetObject(String key) {
            return "";
        }

        public Enumeration getKeys() {
            return null;
        }
    }

    /**
     * Test dispose method.
     */
    public void testDispose() {
        frame.setVisible(true);
        
        WindowOperator operator = new WindowOperator();
        assertNotNull(operator);

        operator.dispose();
    }

    /**
     * Test getFocusOwner method.
     */
    public void testGetFocusOwner() {
        frame.setVisible(true);
        
        WindowOperator operator = new WindowOperator();
        assertNotNull(operator);

        operator.getFocusOwner();
    }

    /**
     * Test getOwnedWindows method.
     */
    public void testGetOwnedWindows() {
        frame.setVisible(true);
        
        WindowOperator operator = new WindowOperator();
        assertNotNull(operator);

        operator.getOwnedWindows();
    }

    /**
     * Test getOwner method.
     */
    public void testGetOwner() {
        frame.setVisible(true);
        
        WindowOperator operator = new WindowOperator();
        assertNotNull(operator);

        operator.getOwner();
    }

    /**
     * Test getWarningString method.
     */
    public void testGetWarningString() {
        frame.setVisible(true);
        
        WindowOperator operator = new WindowOperator();
        assertNotNull(operator);

        operator.getWarningString();
    }

    /**
     * Test pack method.
     */
    public void testPack() {
        frame.setVisible(true);
        
        WindowOperator operator = new WindowOperator();
        assertNotNull(operator);

        operator.pack();
    }

    /**
     * Test removeWindowListener method.
     */
    public void testRemoveWindowListener() {
        frame.setVisible(true);
        
        WindowOperator operator = new WindowOperator();
        assertNotNull(operator);

        operator.removeWindowListener(null);
    }

    /**
     * Test toBack method.
     */
    public void testToBack() {
        frame.setVisible(true);
        
        WindowOperator operator = new WindowOperator();
        assertNotNull(operator);

        operator.toBack();
    }

    /**
     * Test toFront method.
     */
    public void testToFront() {
        frame.setVisible(true);
        
        WindowOperator operator = new WindowOperator();
        assertNotNull(operator);

        operator.toFront();
    }

    /**
     * Test isFocused method.
     */
    public void testIsFocused() {
        frame.setVisible(true);
        
        WindowOperator operator = new WindowOperator();
        assertNotNull(operator);

        operator.isFocused();
    }

    /**
     * Test isActive method.
     */
    public void testIsActive() {
        frame.setVisible(true);
        
        WindowOperator operator = new WindowOperator();
        assertNotNull(operator);

        operator.isActive();
    }
}
