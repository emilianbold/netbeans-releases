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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.xml.schema.refactoring.ui;

import org.openide.util.ImageUtilities;
import org.openide.ErrorManager;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ColorUIResource;
import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;

// #21380.
/**
 * Copy of original CloseButtonTabbedPane from the NetBeans 3.4 winsys.  Old code never dies.
 *
 * !!! jbecicka comment: 
 * !!! This class was copy/pasted from org.netbeans.core.output2.ui
 * !!! See issue 44576
 * !!! Remove this class as soon as issue 55845 is fixed
 *
 * @author Tran Duc Trung
 *
 */
final public class CloseButtonTabbedPane extends JTabbedPane implements ChangeListener, Runnable {

    public static final long serialVersionUID = 1L;
    private final Image closeTabImage =
        ImageUtilities.loadImage("org/netbeans/modules/refactoring/resources/RefCloseTab.gif"); // NOI18N
    private final Image closeTabInactiveImage =
        ImageUtilities.loadImage("org/netbeans/modules/refactoring/resources/RefCloseTabInactive.gif"); // NOI18N

    public static final String PROP_CLOSE = "close"; // NOI18N

    public CloseButtonTabbedPane() {
        addChangeListener(this);
        CloseButtonListener.install();
        //Bugfix #28263: Disable focus.
        setFocusable(false);
        setBorder(javax.swing.BorderFactory.createEmptyBorder());
        setFocusCycleRoot(true);
        setFocusTraversalPolicy(new CBTPPolicy());
    }

    private Component sel() {
        Component c = getSelectedComponent();
        return c == null ? this : c;
    }

    private class CBTPPolicy extends FocusTraversalPolicy {
        public Component getComponentAfter(Container aContainer, Component aComponent) {
            return sel();
        }

        public Component getComponentBefore(Container aContainer, Component aComponent) {
            return sel();
        }

        public Component getFirstComponent(Container aContainer) {
            return sel();
        }

        public Component getLastComponent(Container aContainer) {
            return sel();
        }

        public Component getDefaultComponent(Container aContainer) {
            return sel();
        }
    }

    public int tabForCoordinate(int x, int y) {
        return getUI().tabForCoordinate(this, x, y);
    }

    private int pressedCloseButtonIndex = -1;
    private int mouseOverCloseButtonIndex = -1;
    private boolean draggedOut = false;

    public void stateChanged (ChangeEvent e) {
        reset();
    }
    
    public Component add (Component c) {
        Component result = super.add(c);
        String s = c.getName();
        if (s != null) {
            s += " "; // NOI18N
        }
        setTitleAt (getComponentCount() - 1, s);
        return result;
    }

    public void setTitleAt(int idx, String title) {
        String nue = title.indexOf("</html>") != -1 ? //NOI18N
            title.replace("</html>", "&nbsp;&nbsp;</html>") //NOI18N
            : title + "  "; // NOI18N
        if (!title.equals(getTitleAt(idx))) {
            super.setTitleAt(idx, nue);
        }
    }

    private void reset() {
        setMouseOverCloseButtonIndex(-1);
        setPressedCloseButtonIndex(-1);
        draggedOut = false;
    }

    private Rectangle getCloseButtonBoundsAt(int i) {
        Rectangle b = getBoundsAt(i);
        if (b == null)
            return null;
        else {
            b = new Rectangle(b);
            fixGetBoundsAt(b);

            Dimension tabsz = getSize();
            if (b.x + b.width >= tabsz.width
                || b.y + b.height >= tabsz.height)
                return null;

            return new Rectangle(b.x + b.width - 13,
                                 b.y + b.height / 2 - 5,
                                 8,
                                 8);
        }
    }


    /** Checks whether current L&F sets used keys for colors.
     * If not puts default values. */
    private static void checkUIColors() {
        if(UIManager.getColor("Button.shadow") == null) { // NOI18N
            UIManager.put("Button.shadow", // NOI18N
                new ColorUIResource(153, 153, 153));
        }
        if(UIManager.getColor("Button.darkShadow") == null) { // NOI18N
            UIManager.put("Button.darkShadow", // NOI18N
                new ColorUIResource(102, 102, 102));
        }
        if(UIManager.getColor("Button.highlight") == null) { // NOI18N
            UIManager.put("Button.highlight", // NOI18N
                new ColorUIResource(Color.white));
        }
        if(UIManager.getColor("Button.background") == null) { // NOI18N
            UIManager.put("Button.background", // NOI18N
                new ColorUIResource(204, 204, 204));
        }
    }
    
    public void paint(Graphics g) {
        super.paint(g);

        // #29181 All L&F doesn't support the colors used.
        checkUIColors();

        // Have a look at
        // http://ui.netbeans.org/docs/ui/closeButton/closeButtonUISpec.html
        // to see how the buttons are specified to be drawn.

        int selectedIndex = getSelectedIndex();
        for (int i = 0, n = getTabCount(); i < n; i++) {
            Rectangle r = getCloseButtonBoundsAt(i);
            if (r == null)
                continue;
            
            if(i == pressedCloseButtonIndex && !draggedOut) {
                g.setColor(UIManager.getColor("Button.shadow")); //NOI18N
                g.fillRect(r.x , r.y, r.width, r.height);
            }
            
            if (i != selectedIndex)
                g.drawImage(closeTabInactiveImage, r.x + 2, r.y + 2, this);
            else
                g.drawImage(closeTabImage, r.x + 2, r.y + 2, this);
            
            if (i == mouseOverCloseButtonIndex
            || (i == pressedCloseButtonIndex && draggedOut)) {
                g.setColor(UIManager.getColor("Button.darkShadow")); //NOI18N
                g.drawRect(r.x, r.y, r.width, r.height);
                g.setColor(i == selectedIndex
                    ? UIManager.getColor("Button.highlight") //NOI18N
                    : UIManager.getColor("Button.background")); //NOI18N
                g.drawRect(r.x + 1, r.y + 1, r.width, r.height);
                
                // Draw the dots.
                g.setColor (UIManager.getColor ("Button.highlight").brighter()); //NOI18N
                g.drawLine(r.x + r.width, r.y + 1, r.x + r.width, r.y + 1);
                g.drawLine(r.x + 1, r.y + r.height, r.x + 1, r.y + r.height);
            } else if (i == pressedCloseButtonIndex) {
                g.setColor(UIManager.getColor("Button.shadow")); //NOI18N
                g.drawRect(r.x, r.y, r.width, r.height);
                g.setColor(i == selectedIndex
                    ? UIManager.getColor("Button.highlight") //NOI18N
                    : UIManager.getColor("Button.background")); //NOI18N
                g.drawLine(r.x + 1,
                           r.y + r.height + 1,
                           r.x + r.width + 1,
                           r.y + r.height + 1);
                g.drawLine(r.x + r.width + 1,
                           r.y + 1,
                           r.x + r.width + 1,
                           r.y + r.height + 1);
                
                // Draw the lines.
                g.setColor(UIManager.getColor("Button.background")); //NOI18N
                g.drawLine(r.x + 1, r.y + 1, r.x + r.width, r.y + 1);
                g.drawLine(r.x + 1, r.y + 1, r.x + 1, r.y + r.height);
            }
        }
    }

    private void setPressedCloseButtonIndex(int index) {
        if (pressedCloseButtonIndex == index)
            return;

        if (pressedCloseButtonIndex >= 0
        && pressedCloseButtonIndex < getTabCount()) {
            Rectangle r = getCloseButtonBoundsAt(pressedCloseButtonIndex);
            repaint(r.x, r.y, r.width + 2, r.height + 2);

            JComponent c = (JComponent)
                getComponentAt(pressedCloseButtonIndex);
            setToolTipTextAt(pressedCloseButtonIndex, c.getToolTipText());
        }

        pressedCloseButtonIndex = index;

        if (pressedCloseButtonIndex >= 0
        && pressedCloseButtonIndex < getTabCount()) {
            Rectangle r = getCloseButtonBoundsAt(pressedCloseButtonIndex);
            repaint(r.x, r.y, r.width + 2, r.height + 2);
            setMouseOverCloseButtonIndex(-1);
            setToolTipTextAt(pressedCloseButtonIndex, null);
        }
    }

    private void setMouseOverCloseButtonIndex(int index) {
        if (mouseOverCloseButtonIndex == index)
            return;

        if (mouseOverCloseButtonIndex >= 0
        && mouseOverCloseButtonIndex < getTabCount()) {
            Rectangle r = getCloseButtonBoundsAt(mouseOverCloseButtonIndex);
            repaint(r.x, r.y, r.width + 2, r.height + 2);
            JComponent c =  (JComponent)
                getComponentAt(mouseOverCloseButtonIndex);
            setToolTipTextAt(mouseOverCloseButtonIndex, c.getToolTipText());
        }

        mouseOverCloseButtonIndex = index;

        if (mouseOverCloseButtonIndex >= 0
        && mouseOverCloseButtonIndex < getTabCount()) {
            Rectangle r = getCloseButtonBoundsAt(mouseOverCloseButtonIndex);
            repaint(r.x, r.y, r.width + 2, r.height + 2);
            setPressedCloseButtonIndex(-1);
            setToolTipTextAt(mouseOverCloseButtonIndex, null);
        }
    }

    private void fireCloseRequest(Component c) {
        firePropertyChange(PROP_CLOSE, null, c);
    }

    public static void fixGetBoundsAt(Rectangle b) {
        if (b.y < 0)
            b.y = -b.y;
        if (b.x < 0)
            b.x = -b.x;
    }

    public static int findTabForCoordinate(JTabbedPane tab, int x, int y) {
        for (int i = 0; i < tab.getTabCount(); i++) {
            Rectangle b = tab.getBoundsAt(i);
            if (b != null) {
                b = new Rectangle(b);
                fixGetBoundsAt(b);

                if (b.contains(x, y)) {
                    return i;
                }
            }
        }
        return -1;
    }
    
    boolean closingTab = false;
    public void doLayout() {
        //JDK 1.5, Win L&F - we cannot do the layout synchronously when we've
        //just removed a tab - the layout will have out of sync cache data
        if (closingTab) {
            SwingUtilities.invokeLater (this);
        } else {
            super.doLayout();
        }
    }
    
    public void run() {
        doLayout();
        closingTab = false;
        repaint();
    }

    protected void processMouseEvent (MouseEvent me) {
        try {
            super.processMouseEvent (me);
        } catch (ArrayIndexOutOfBoundsException aioobe) {
            //Bug in BasicTabbedPaneUI$Handler:  The focusIndex field is not
            //updated when tabs are removed programmatically, so it will try to
            //repaint a tab that's not there
            ErrorManager.getDefault().annotate(aioobe, "Suppressed " + //NOI18N
                    "AIOOBE bug in BasicTabbedPaneUI"); //NOI18N
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, aioobe);
        }
    }


    private static class CloseButtonListener implements AWTEventListener
    {
        private static boolean installed = false;

        private CloseButtonListener() {}

        private static synchronized void install() {
            if (installed)
                return;

            installed = true;
            Toolkit.getDefaultToolkit().addAWTEventListener(
                new CloseButtonListener(),
                AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
        }

        public void eventDispatched (AWTEvent ev) {
            MouseEvent e = (MouseEvent) ev;

            Component c = (Component) e.getSource();
            while (c != null && !(c instanceof CloseButtonTabbedPane))
                c = c.getParent();
            if (c == null)
                return;
            final CloseButtonTabbedPane tab = (CloseButtonTabbedPane) c;

            Point p = SwingUtilities.convertPoint((Component) e.getSource(),
                                                  e.getPoint(),
                                                  tab);

            if (e.getID() == MouseEvent.MOUSE_CLICKED) {
                //Not interested in clicked, and it can cause an NPE
                return;
            }
            
            int index = findTabForCoordinate(tab, p.x, p.y);

            Rectangle r = null;
            if (index >= 0)
                r = tab.getCloseButtonBoundsAt(index);
            if (r == null)
                r = new Rectangle(0,0,0,0);

            switch(e.getID()) {
                case MouseEvent.MOUSE_PRESSED:
                    if (r.contains(p)) {
                        tab.setPressedCloseButtonIndex(index);
                        tab.draggedOut = false;
                        e.consume();
                        return;
                    }
                    break;

                case MouseEvent.MOUSE_RELEASED:
                    if (r.contains(p) && tab.pressedCloseButtonIndex >= 0) {
                        tab.closingTab = true;
                        Component tc =
                            tab.getComponentAt(tab.pressedCloseButtonIndex);
                        tab.reset();

                        tab.fireCloseRequest(tc);
                        e.consume();
                        return;
                    }
                    else {
                        tab.reset();
                    }
                    break;

                case MouseEvent.MOUSE_ENTERED:
                    break;

                case MouseEvent.MOUSE_EXITED:
                    //tab.reset();

                    // XXX(-ttran) when the user clicks on the close button on
                    // an unfocused (internal) frame the focus is transferred
                    // to the frame and an unexpected MOUSE_EXITED event is
                    // fired.  If we call reset() at every MOUSE_EXITED event
                    // then when the mouse button is released the tab is not
                    // closed.  See bug #24450
                    
                    break;

                case MouseEvent.MOUSE_MOVED:
                    if (r.contains(p)) {
                        tab.setMouseOverCloseButtonIndex(index);
                        tab.draggedOut = false;
                        e.consume();
                        return;
                    }
                    else if (tab.mouseOverCloseButtonIndex >= 0) {
                        tab.setMouseOverCloseButtonIndex(-1);
                        tab.draggedOut = false;
                        e.consume();
                    }
                    break;

                case MouseEvent.MOUSE_DRAGGED:
                    if (tab.pressedCloseButtonIndex >= 0) {
                        if (tab.draggedOut != !r.contains(p)) {
                            tab.draggedOut = !r.contains(p);
                            tab.repaint(r.x, r.y, r.width + 2, r.height + 2);
                        }
                        e.consume();
                        return;
                    }
                    break;
            }
        }
    }
}
