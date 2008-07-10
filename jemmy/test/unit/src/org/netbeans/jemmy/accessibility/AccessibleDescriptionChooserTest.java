/*
 * $Id$
 *
 * ----------------------------------------------------------------------------
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
 * ----------------------------------------------------------------------------
 *
 */
package org.netbeans.jemmy.accessibility;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JWindow;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.WindowOperator;

/**
 * A JUnit test for AccessibleDescriptionChooser.
 *
 * @author Manfred Riem (mriem@netbeans.org)
 * @version $Revision$
 */
public class AccessibleDescriptionChooserTest extends TestCase {
    /**
     * Stores the frame.
     */
    private JFrame frame;
    
    /**
     * Stores the button.
     */
    private JButton button;

    /**
     * Stores the dialog.
     */
    private JDialog dialog;
    
    /**
     * Stores the window.
     */
    private JWindow window;
        
    /**
     * Constructor.
     *
     * @param testName the name of the test.
     */
    public AccessibleDescriptionChooserTest(String testName) {
        super(testName);
    }

    /**
     * Setup before testing.
     *
     * @throws Exception when a major error occurs.
     */
    protected void setUp() throws Exception {
        frame = new JFrame();
        frame.getAccessibleContext().setAccessibleDescription("Frame");
        button = new JButton("Button");
        button.getAccessibleContext().setAccessibleDescription("Accessible");
        frame.getContentPane().add(button);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        dialog = new JDialog();
        dialog.getAccessibleContext().setAccessibleDescription("Dialog");
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);

        window = new JWindow(frame);
        window.getAccessibleContext().setAccessibleDescription("Window");
        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }

    /**
     * Cleanup after testing.
     *
     * @throws Exception when a major error occurs.
     */
    protected void tearDown() throws Exception {
        frame.setVisible(false);
        frame.dispose();
        frame = null;

        dialog.setVisible(false);
        dialog.dispose();
        dialog = null;
        
        window.setVisible(false);
        window.dispose();
        window = null;
    }

    /**
     * Suite method.
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(AccessibleDescriptionChooserTest.class);
        
        return suite;
    }

    /**
     * Test checkContext method.
     */
    public void testCheckContext() {
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JButtonOperator operator1 = new JButtonOperator(operator, new AccessibleDescriptionChooser("Accessible"));
        assertNotNull(operator1);
        
        JDialogOperator operator2 = new JDialogOperator(new AccessibleDescriptionChooser("Dialog"));
        assertNotNull(operator2);
        
        JFrameOperator operator3 = new JFrameOperator(new AccessibleDescriptionChooser("Frame"));
        assertNotNull(operator3);

        WindowOperator operator4 = new WindowOperator(operator, new AccessibleDescriptionChooser("Window"));
        assertNotNull(operator4);
    }

    /**
     * Test getDescription method.
     */
    public void testGetDescription() {
        assertEquals("JComponent with \"Accessible\" accessible description", 
                new AccessibleDescriptionChooser("Accessible").getDescription());
    }
}
