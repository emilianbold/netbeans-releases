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

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import javax.swing.DefaultSingleSelectionModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.netbeans.jemmy.util.NameComponentChooser;

/**
 * A JUnit test for JTabbedPaneOperator.
 *
 * @author Manfred Riem (mriem@netbeans.org)
 * @version $Revision$
 */
public class JTabbedPaneOperatorTest extends TestCase {
    /**
     * Stores the frame.
     */
    private JFrame frame;
    
    /**
     * Stores the tabbed pane.
     */
    private JTabbedPane tabbedPane;
    
    /**
     * Constructor.
     *
     * @param testName the name of the test.
     */
    public JTabbedPaneOperatorTest(String testName) {
        super(testName);
    }

    /**
     * Setup before testing.
     */
    protected void setUp() throws Exception {
        frame = new JFrame();
        tabbedPane = new JTabbedPane();
        tabbedPane.setName("JTabbedPaneOperatorTest");
        tabbedPane.setToolTipText("JTabbedPaneOperatorTest");
        
        JPanel panel1 = new JPanel();
        panel1.setName("Tab1");
        
        JPanel panel2 = new JPanel();
        panel2.setName("Tab2");
        
        tabbedPane.add(panel1);
        tabbedPane.add(panel2);
        
        frame.getContentPane().add(tabbedPane);
        frame.setSize(400, 300);
        frame.setLocationRelativeTo(null);
    }

    /**
     * Cleanup after testing.
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
        TestSuite suite = new TestSuite(JTabbedPaneOperatorTest.class);
        
        return suite;
    }
    
    /**
     * Test constructor.
     */
    public void testConstructor() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTabbedPaneOperator operator1 = new JTabbedPaneOperator(operator);
        assertNotNull(operator1);
        
        JTabbedPaneOperator operator2 = new JTabbedPaneOperator(operator, "Tab1");
        assertNotNull(operator2);
        
        JTabbedPaneOperator operator3 = new JTabbedPaneOperator(operator, new NameComponentChooser("JTabbedPaneOperatorTest"));
        assertNotNull(operator3);
    }

    /**
     * Test findJTabbedPane method.
     */
    public void testFindJTabbedPane() {
        frame.setVisible(true);
        
        JTabbedPane tabbedPane1 = JTabbedPaneOperator.findJTabbedPane(frame, new NameComponentChooser("JTabbedPaneOperatorTest"));
        assertNotNull(tabbedPane1);
        
        JTabbedPane tabbedPane2 = JTabbedPaneOperator.findJTabbedPane(frame, "Tab1", false, false, 0);
        assertNotNull(tabbedPane2);
    }

    /**
     * Test findJTabbedPaneUnder method.
     */
    public void testFindJTabbedPaneUnder() {
        frame.setVisible(true);
        
        JTabbedPane tabbedPane1 = JTabbedPaneOperator.findJTabbedPaneUnder(new JPanel());
        assertNull(tabbedPane1);
        
        JTabbedPane tabbedPane2 = JTabbedPaneOperator.findJTabbedPaneUnder(new JPanel(), new NameComponentChooser("Test"));
        assertNull(tabbedPane2);
    }

    /**
     * Test waitJTabbedPane method.
     */
    public void testWaitJTabbedPane() {
        frame.setVisible(true);
        
        JTabbedPane tabbedPane1 = JTabbedPaneOperator.waitJTabbedPane(frame, new NameComponentChooser("JTabbedPaneOperatorTest"));
        assertNotNull(tabbedPane1);
        
        JTabbedPane tabbedPane2 = JTabbedPaneOperator.waitJTabbedPane(frame, "Tab1", false, false, 0);
        assertNotNull(tabbedPane2);
    }

    /**
     * Test findPage method.
     */
    public void testFindPage() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTabbedPaneOperator operator1 = new JTabbedPaneOperator(operator);
        assertNotNull(operator1);
        
        operator1.findPage("Tab1");
        operator1.findPage("Tab1", false, false);
    }

    /**
     * Test selectPage method.
     */
    public void testSelectPage() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTabbedPaneOperator operator1 = new JTabbedPaneOperator(operator);
        assertNotNull(operator1);
        
        operator1.selectPage("Tab1");
        operator1.selectPage("Tab1", false, false);
    }

    /**
     * Test waitPage method.
     */
    public void testWaitPage() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTabbedPaneOperator operator1 = new JTabbedPaneOperator(operator);
        assertNotNull(operator1);
        
        operator1.waitPage("Tab1");
    }

    /**
     * Test waitSelected method.
     */
    public void testWaitSelected() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTabbedPaneOperator operator1 = new JTabbedPaneOperator(operator);
        assertNotNull(operator1);
        
        operator1.selectPage("Tab1");
        operator1.waitSelected("Tab1");
    }

    /**
     * Test getDump method.
     */
    public void testGetDump() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTabbedPaneOperator operator1 = new JTabbedPaneOperator(operator);
        assertNotNull(operator1);
        
        operator1.getDump();
    }

    /**
     * Test addChangeListener method.
     */
    public void testAddChangeListener() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTabbedPaneOperator operator1 = new JTabbedPaneOperator(operator);
        assertNotNull(operator1);
        
        ChangeListenerTest listener = new ChangeListenerTest();
        operator1.addChangeListener(listener);
        operator1.removeChangeListener(listener);
    }
    
    /**
     * Inner class used for testing.
     */
    public class ChangeListenerTest implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
        }
    }

    /**
     * Test addTab method.
     */
    public void testAddTab() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTabbedPaneOperator operator1 = new JTabbedPaneOperator(operator);
        assertNotNull(operator1);
        
        operator1.addTab("Test", new JPanel());
        operator1.addTab("Tab1", new IconTest(), new JPanel());
        operator1.addTab("Tab1", new IconTest(), new JPanel(), "Tab1");
    }
    
    /**
     * Inner class for testing.
     */
    public class IconTest implements Icon {
        public void paintIcon(Component c, Graphics g, int x, int y) {
        }

        public int getIconWidth() {
            return 0;
        }

        public int getIconHeight() {
            return 0;
        }
    }

    /**
     * Test getBackgroundAt method.
     */
    public void testGetBackgroundAt() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTabbedPaneOperator operator1 = new JTabbedPaneOperator(operator);
        assertNotNull(operator1);
        
        operator1.setBackgroundAt(0, Color.black);
        operator1.getBackgroundAt(0);
    }

    /**
     * Test getBoundsAt method.
     */
    public void testGetBoundsAt() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTabbedPaneOperator operator1 = new JTabbedPaneOperator(operator);
        assertNotNull(operator1);
        
        operator1.getBoundsAt(0);
    }

    /**
     * Test getComponentAt method.
     */
    public void testGetComponentAt() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTabbedPaneOperator operator1 = new JTabbedPaneOperator(operator);
        assertNotNull(operator1);
        
        operator1.setComponentAt(0, new JPanel());
        operator1.getComponentAt(0);
    }

    /**
     * Test getDisabledIconAt method.
     */
    public void testGetDisabledIconAt() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTabbedPaneOperator operator1 = new JTabbedPaneOperator(operator);
        assertNotNull(operator1);
        
        operator1.setDisabledIconAt(0, new ImageIcon());
        operator1.getDisabledIconAt(0);
    }

    /**
     * Test getForegroundAt method.
     */
    public void testGetForegroundAt() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTabbedPaneOperator operator1 = new JTabbedPaneOperator(operator);
        assertNotNull(operator1);
        
        operator1.setForegroundAt(0, Color.white);
        operator1.getForegroundAt(0);
    }

    /**
     * Test getIconAt method.
     */
    public void testGetIconAt() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTabbedPaneOperator operator1 = new JTabbedPaneOperator(operator);
        assertNotNull(operator1);

        operator1.setIconAt(0, new ImageIcon());
        operator1.getIconAt(0);
    }

    /**
     * Test getModel method.
     */
    public void testGetModel() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTabbedPaneOperator operator1 = new JTabbedPaneOperator(operator);
        assertNotNull(operator1);

        operator1.setModel(new DefaultSingleSelectionModel());
        operator1.getModel();
    }

    /**
     * Test getSelectedComponent method.
     */
    public void testGetSelectedComponent() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTabbedPaneOperator operator1 = new JTabbedPaneOperator(operator);
        assertNotNull(operator1);
        
        operator1.setSelectedComponent(operator1.getSelectedComponent());
    }

    /**
     * Test getSelectedIndex method.
     */
    public void testGetSelectedIndex() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTabbedPaneOperator operator1 = new JTabbedPaneOperator(operator);
        assertNotNull(operator1);
        
        operator1.setSelectedIndex(0);
        operator1.getSelectedIndex();
    }

    /**
     * Test getTabCount method.
     */
    public void testGetTabCount() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTabbedPaneOperator operator1 = new JTabbedPaneOperator(operator);
        assertNotNull(operator1);
        
        operator1.getTabCount();
    }

    /**
     * Test getTabPlacement method.
     */
    public void testGetTabPlacement() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTabbedPaneOperator operator1 = new JTabbedPaneOperator(operator);
        assertNotNull(operator1);
        
        operator1.setTabPlacement(operator1.getTabPlacement());
    }

    /**
     * Test getTabRunCount method.
     */
    public void testGetTabRunCount() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTabbedPaneOperator operator1 = new JTabbedPaneOperator(operator);
        assertNotNull(operator1);
        
        operator1.getTabRunCount();
    }

    /**
     * Test getTitleAt method.
     */
    public void testGetTitleAt() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTabbedPaneOperator operator1 = new JTabbedPaneOperator(operator);
        assertNotNull(operator1);
        
        operator1.setTitleAt(0, "Title");
        operator1.getTitleAt(0);
    }

    /**
     * Test getUI method.
     */
    public void testGetUI() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTabbedPaneOperator operator1 = new JTabbedPaneOperator(operator);
        assertNotNull(operator1);
        
        operator1.setUI(operator1.getUI());
    }

    /**
     * Test indexOfComponent method.
     */
    public void testIndexOfComponent() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTabbedPaneOperator operator1 = new JTabbedPaneOperator(operator);
        assertNotNull(operator1);
        
        operator1.indexOfComponent(new JPanel());
    }

    /**
     * Test indexOfTab method.
     */
    public void testIndexOfTab() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTabbedPaneOperator operator1 = new JTabbedPaneOperator(operator);
        assertNotNull(operator1);
        
        operator1.indexOfTab("Tab1");
        operator1.indexOfTab(new ImageIcon());
    }

    /**
     * Test insertTab method.
     */
    public void testInsertTab() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTabbedPaneOperator operator1 = new JTabbedPaneOperator(operator);
        assertNotNull(operator1);
        
        operator1.insertTab("Insert", null, new JPanel(), "Insert", 0);
    }

    /**
     * Test isEnabledAt method.
     */
    public void testIsEnabledAt() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTabbedPaneOperator operator1 = new JTabbedPaneOperator(operator);
        assertNotNull(operator1);
        
        operator1.isEnabledAt(0);
    }

    /**
     * Test removeTabAt method.
     */
    public void testRemoveTabAt() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTabbedPaneOperator operator1 = new JTabbedPaneOperator(operator);
        assertNotNull(operator1);
        
        operator1.removeTabAt(0);
    }

    /**
     * Test setEnabledAt method.
     */
    public void testSetEnabledAt() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTabbedPaneOperator operator1 = new JTabbedPaneOperator(operator);
        assertNotNull(operator1);
        
        operator1.setEnabledAt(0, true);
    }
}
