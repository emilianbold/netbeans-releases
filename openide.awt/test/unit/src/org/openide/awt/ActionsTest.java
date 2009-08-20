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
 * Software is Nokia. Portions Copyright 2004 Nokia. All Rights Reserved.
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

package org.openide.awt;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;

/**
 * Tests for the Actions class.
 * @author David Strupl
 */
public class ActionsTest extends NbTestCase {
    
    // colors of the testing images in this order:
    // (test recognizes the icon by the white/black colors in specified positions :-)))
    // testIcon.gif
    // testIcon_rollover.gif
    // testIcon_pressed.gif
    // testIcon_disabled.gif
    private static int[][] RESULT_COLORS_00 = {
        {255, 255, 255},
        {0, 0, 0},
        {255, 255, 255},
        {0, 0, 0},
        {255, 255, 255},
        {0, 0, 0},
        {255, 255, 255},
        {0, 0, 0},
    };
    private static int[][] RESULT_COLORS_01 = {
        {255, 255, 255},
        {255, 255, 255},
        {0, 0, 0},
        {0, 0, 0},
        {255, 255, 255},
        {255, 255, 255},
        {0, 0, 0},
        {0, 0, 0},
    };
    private static int[][] RESULT_COLORS_11 = {
        {255, 255, 255},
        {255, 255, 255},
        {255, 255, 255},
        {255, 255, 255},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
        {0, 0, 0},
    };
    
    
    public ActionsTest(String name) {
        super(name);
    }
    
    protected void setUp() {
        MockServices.setServices(TestConnector.class);
    }
    
    /**
     * Test whether pressed, rollover and disabled icons
     * work for javax.swing.Action.
     */
    public void testIconsAction() throws Exception {
        JButton jb = new JButton();
        Actions.connect(jb, new TestAction());
        
        Icon icon = jb.getIcon();
        assertNotNull(icon);
        checkIfLoadedCorrectIcon(icon, jb, 0, "Enabled icon");
        
        Icon rolloverIcon = jb.getRolloverIcon();
        assertNotNull(rolloverIcon);
        checkIfLoadedCorrectIcon(rolloverIcon, jb, 1, "Rollover icon");
        
        Icon pressedIcon = jb.getPressedIcon();
        assertNotNull(pressedIcon);
        checkIfLoadedCorrectIcon(pressedIcon, jb, 2, "Pressed icon");
        
        Icon disabledIcon = jb.getDisabledIcon();
        assertNotNull(disabledIcon);
        checkIfLoadedCorrectIcon(disabledIcon, jb, 3, "Disabled icon");
    }
    
    /**
     * Test whether pressed, rollover and disabled icons
     * work for SystemAction.
     */
    public void testIconsSystemAction() throws Exception {
        SystemAction saInstance = SystemAction.get(TestSystemAction.class);
        
        JButton jb = new JButton();
        Actions.connect(jb, saInstance);
        
        Icon icon = jb.getIcon();
        assertNotNull(icon);
        checkIfLoadedCorrectIcon(icon, jb, 0, "Enabled icon");
        
        Icon rolloverIcon = jb.getRolloverIcon();
        assertNotNull(rolloverIcon);
        checkIfLoadedCorrectIcon(rolloverIcon, jb, 1, "Rollover icon");
        
        Icon pressedIcon = jb.getPressedIcon();
        assertNotNull(pressedIcon);
        checkIfLoadedCorrectIcon(pressedIcon, jb, 2, "Pressed icon");
        
        Icon disabledIcon = jb.getDisabledIcon();
        assertNotNull(disabledIcon);
        checkIfLoadedCorrectIcon(disabledIcon, jb, 3, "Disabled icon");
    }
    
    /**
     * Test whether pressed, rollover and disabled 24x24 icons
     * work for javax.swing.Action.
     */
    public void testIconsAction24() throws Exception {
        JButton jb = new JButton();
        jb.putClientProperty("PreferredIconSize",new Integer(24));
        Actions.connect(jb, new TestAction());
        
        Icon icon = jb.getIcon();
        assertNotNull(icon);
        checkIfLoadedCorrectIcon(icon, jb, 4, "Enabled icon");
        
        Icon rolloverIcon = jb.getRolloverIcon();
        assertNotNull(rolloverIcon);
        checkIfLoadedCorrectIcon(rolloverIcon, jb, 5, "Rollover icon");
        
        Icon pressedIcon = jb.getPressedIcon();
        assertNotNull(pressedIcon);
        checkIfLoadedCorrectIcon(pressedIcon, jb, 6, "Pressed icon");
        
        Icon disabledIcon = jb.getDisabledIcon();
        assertNotNull(disabledIcon);
        checkIfLoadedCorrectIcon(disabledIcon, jb, 7, "Disabled icon");
    }
    
    /**
     * #47527
     * Tests if "noIconInMenu" really will NOT push the icon from the action
     * to the menu item.
     */
    public void testNoIconInMenu() throws Exception {
        JMenuItem item = new JMenuItem();
        item.setIcon(null);
        Actions.connect(item, new TestNoMenuIconAction(), false);
        assertNull(item.getIcon());
    }
    
    /**
     * Test whether pressed, rollover and disabled 24x24 icons
     * work for SystemAction.
     */
    public void testIconsSystemAction24() throws Exception {
        SystemAction saInstance = SystemAction.get(TestSystemAction.class);
        
        JButton jb = new JButton();
        jb.putClientProperty("PreferredIconSize",new Integer(24));
        Actions.connect(jb, saInstance);
        
        Icon icon = jb.getIcon();
        assertNotNull(icon);
        checkIfLoadedCorrectIcon(icon, jb, 4, "Enabled icon");
        
        Icon rolloverIcon = jb.getRolloverIcon();
        assertNotNull(rolloverIcon);
        checkIfLoadedCorrectIcon(rolloverIcon, jb, 5, "Rollover icon");
        
        Icon pressedIcon = jb.getPressedIcon();
        assertNotNull(pressedIcon);
        checkIfLoadedCorrectIcon(pressedIcon, jb, 6, "Pressed icon");
        
        Icon disabledIcon = jb.getDisabledIcon();
        assertNotNull(disabledIcon);
        checkIfLoadedCorrectIcon(disabledIcon, jb, 7, "Disabled icon");
    }
    
    /**
     * Tests if changes in accelerator key or name of the action does not change the tooltip
     * of the button if the action has a custom tooltip. See first part of #57974.
     */
    public void testTooltipsArePersistent() throws Exception {
        Action action = new ActionsTest.TestActionWithTooltip();
        JButton button = new JButton();
        
        Actions.connect(button, action);
        
        JFrame f = new JFrame();
        
        f.getContentPane().add(button);
        f.setVisible(true);
        
        assertTrue(button.getToolTipText().equals(TestActionWithTooltip.TOOLTIP));
        
        action.putValue(Action.NAME, "new-name");
        
        assertTrue(button.getToolTipText().equals(TestActionWithTooltip.TOOLTIP));
        
        action.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('a'));
        
        assertTrue(button.getToolTipText().indexOf(TestActionWithTooltip.TOOLTIP) != (-1));
        
        f.setVisible(false);
    }
    
    /**
     * Tests if the tooltip is made out of the NAME if there is not tooltip set for an action.
     * See also #57974.
     */
    public void testTooltipsIsBuiltFromNameIfNoTooltip() throws Exception {
        Action action = new ActionsTest.TestAction();
        JButton button = new JButton();
        
        Actions.connect(button, action);
        
        JFrame f = new JFrame();
        
        f.getContentPane().add(button);
        f.setVisible(true);
        
        assertTrue(button.getToolTipText().equals("test"));
        
        action.putValue(Action.NAME, "new-name");
        
        assertTrue(button.getToolTipText().equals("new-name"));
        
        action.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('a'));
        
        assertTrue(button.getToolTipText().indexOf("new-name") != (-1));
        
        f.setVisible(false);
    }
    
    /**
     * Tests if the accelerator key is shown in the button's tooltip for actions with
     * custom tooltips.
     */
    public void testTooltipsContainAccelerator() throws Exception {
        Action action = new ActionsTest.TestActionWithTooltip();
        JButton button = new JButton();
        
        Actions.connect(button, action);
        
        JFrame f = new JFrame();
        
        f.getContentPane().add(button);
        f.setVisible(true);
        
        assertTrue(button.getToolTipText().equals(TestActionWithTooltip.TOOLTIP));
        
        action.putValue(Action.NAME, "new-name");
        
        assertTrue(button.getToolTipText().equals(TestActionWithTooltip.TOOLTIP));
        
        action.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK));

        String ctrlMod = Utilities.isMac() ? "\u2303" : "Ctrl";
        assertTrue(button.getToolTipText().indexOf(ctrlMod + "+C") != (-1));
        
        action.putValue(Action.SHORT_DESCRIPTION, null);
        
        assertTrue(button.getToolTipText().indexOf(ctrlMod + "+C") != (-1));
        
        f.setVisible(false);
    }

    /**
     * Tests whether the ButtonActionConnector is being called. The testing
     * implementation is set to "active" only for this test - so the other
     * tests should retain the behaviour like running without the
     * ButtonActionConnector.
     */
    public void testButtonActionConnector() throws Exception {
        TestConnector tc = Lookup.getDefault().lookup(TestConnector.class);
        tc.setActive(true);
        Action action = new ActionsTest.TestAction();
        JButton button = new JButton();
        Actions.connect(button, action);
        assertEquals(1, tc.getConnectCalled());
        JMenuItem jmi = new JMenuItem();
        Actions.connect(jmi, action, false);
        assertEquals(3, tc.getConnectCalled());
        tc.setActive(false);
    }

    @SuppressWarnings("deprecation")
    private static Object peer(Component menu) {
        return menu.getPeer();
    }
    
    public void testPopupTextIsTaken() throws Exception {
        Action action = new ActionsTest.TestAction();
        JMenuItem item = new JMenuItem();
        JMenu jmenu = new JMenu();
        jmenu.addNotify();
        assertNotNull("Peer created", peer(jmenu));
        jmenu.getPopupMenu().addNotify();
        assertNotNull("Peer for popup", peer(jmenu.getPopupMenu()));

        action.putValue("popupText", "&Ahoj");
        action.putValue("menuText", "&Ble");
        action.putValue(action.NAME, "&Mle");
        
        Actions.connect(item, action, true);
        
        assertEquals(Utilities.isMac() ? 0 : 'A', item.getMnemonic());
        assertEquals("Ahoj", item.getText());
    }

    public void testMenuTextIsTaken() throws Exception {
        Action action = new ActionsTest.TestAction();
        JMenuItem item = new JMenuItem();
        JMenu jmenu = new JMenu();
        jmenu.addNotify();
        assertNotNull("Peer created", peer(jmenu));
        jmenu.getPopupMenu().addNotify();
        assertNotNull("Peer for popup", peer(jmenu.getPopupMenu()));

        //action.putValue("popupText", "&Ahoj");
        action.putValue("menuText", "&Ble");
        action.putValue(action.NAME, "&Mle");
        
        Actions.connect(item, action, true);
        
        assertEquals(Utilities.isMac() ? 0 : 'B', item.getMnemonic());
        assertEquals("Ble", item.getText());
    }
    
    public void testActionNameIsTaken() throws Exception {
        Action action = new ActionsTest.TestAction();
        JMenuItem item = new JMenuItem();
        JMenu jmenu = new JMenu();
        jmenu.addNotify();
        assertNotNull("Peer created", peer(jmenu));
        jmenu.getPopupMenu().addNotify();
        assertNotNull("Peer for popup", peer(jmenu.getPopupMenu()));

        //action.putValue("popupText", "&Ahoj");
        //action.putValue("menuText", "&Ble");
        action.putValue(action.NAME, "&Mle");
        
        Actions.connect(item, action, true);
        
        assertEquals(Utilities.isMac() ? 0 : 'M', item.getMnemonic());
        assertEquals("Mle", item.getText());
    }
    
    
    protected boolean runInEQ() {
        return true;
    }
    
    private void checkIfLoadedCorrectIcon(Icon icon, Component c, int rowToCheck, String nameOfIcon) {
        checkIfIconOk(icon, c, 0, 0, RESULT_COLORS_00[rowToCheck], nameOfIcon);
        checkIfIconOk(icon, c, 0, 1, RESULT_COLORS_01[rowToCheck], nameOfIcon);
        checkIfIconOk(icon, c, 1, 1, RESULT_COLORS_11[rowToCheck], nameOfIcon);
    }
    
    /**
     * Checks colors on coordinates X,Y of the icon and compares them
     * to expectedResult.
     */
    private void checkIfIconOk(Icon icon, Component c, int pixelX, int pixelY, int[] expectedResult, String nameOfIcon) {
        BufferedImage bufImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
        icon.paintIcon(c, bufImg.getGraphics(), 0, 0);
        int[] res = bufImg.getData().getPixel(pixelX, pixelY, (int[])null);
        log("Icon height is " + icon.getIconHeight());
        log("Icon width is " + icon.getIconWidth());
        for (int i = 0; i < res.length; i++) {
            // Huh, Ugly hack. the sparc returns a fuzzy values +/- 1 unit e.g. 254 for Black instead of 255 as other OSs do
            // this hack doesn't broken the functionality which should testing
            assertTrue(nameOfIcon + ": Color of the ["+pixelX+","+pixelY+"] pixel is " + res[i] + ", expected was " + expectedResult[i], Math.abs(res[i] - expectedResult[i]) < 10);
        }
    }
    
    private static final class TestSystemAction extends SystemAction {
        
        public void actionPerformed(ActionEvent e) {
        }
        
        public HelpCtx getHelpCtx() {
            return null;
        }
        
        public String getName() {
            return "TestSystemAction";
        }
        
        protected String iconResource() {
            return "org/openide/awt/data/testIcon.gif";
        }
        
    }
    
    private static final class TestAction extends AbstractAction {
        
        public TestAction() {
            putValue("iconBase", "org/openide/awt/data/testIcon.gif");
            putValue(NAME, "test");
        }
        
        public void actionPerformed(ActionEvent e) {
        }
        
    }
    
    private static final class TestNoMenuIconAction extends AbstractAction {
        
        public TestNoMenuIconAction() {
            putValue("iconBase", "org/openide/awt/data/testIcon.gif");
            putValue("noIconInMenu", Boolean.TRUE);
        }
        
        public void actionPerformed(ActionEvent e) {
        }
        
    }
    
    private static final class TestActionWithTooltip extends AbstractAction {
        
        private static String TOOLTIP = "tooltip";
        
        public TestActionWithTooltip() {
            putValue(NAME, "name");
            putValue(SHORT_DESCRIPTION, TOOLTIP);
        }
        
        public void actionPerformed(ActionEvent e) {
        }
        
    }
    
    public static final class TestConnector implements Actions.ButtonActionConnector {
        
        private int called = 0;
        private boolean active = false;
        
        public TestConnector() {}
        
        public boolean connect(AbstractButton button, Action action) {
            if (!active) {
                return false;
            }
            called +=1;
            return true;
        }

        public boolean connect(JMenuItem item, Action action, boolean popup) {
            if (!active) {
                return false;
            }
            called += 2;
            return true;
        }
        
        public int getConnectCalled() {
            return called;
        }
        public void setActive(boolean a) {
            called = 0;
            active = a;
        }
    }
    
}
