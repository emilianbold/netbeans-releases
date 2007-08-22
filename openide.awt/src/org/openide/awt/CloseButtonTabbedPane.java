/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.awt;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FocusTraversalPolicy;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import javax.swing.plaf.UIResource;
import org.openide.util.Utilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.openide.util.Exceptions;

/**
 * Copy of original CloseButtonTabbedPane from the NetBeans 3.4 winsys.  Old code never dies.
 *
 * @author Tran Duc Trung
 * @author S. Aubrecht
 * @since 6.10.0
 *
 */
final class CloseButtonTabbedPane extends JTabbedPane {

    private Image closeTabImage;
    private Image closeTabPressedImage;
    private Image closeTabMouseOverImage;

    static final String PROP_CLOSE = "close";

    CloseButtonTabbedPane() {
        addChangeListener( new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                reset();
            }
        });
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

    private int pressedCloseButtonIndex = -1;
    private int mouseOverCloseButtonIndex = -1;
    private boolean draggedOut = false;

    public Component add (Component c) {
        Component result = super.add(c);
        // #75317 - don't try to set the title if LF (such as Substance LF)
        // is adding some custom UI components into tabbed pane
        if (!(c instanceof UIResource)) {
            String s = c.getName();
            if (s != null) {
                s += "  ";
            }
            setTitleAt (getComponentCount() - 1, s);
        }
        return result;
    }

    public void setTitleAt(int idx, String title) {
        String nue = title.indexOf("</html>") != -1 ? //NOI18N
            Utilities.replaceString(title, "</html>", "&nbsp;&nbsp;</html>") //NOI18N
            : title + "  ";
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
             // bugfix #110654
             if (b.width == 0 || b.height == 0) {
                 return null;
             }
            if( (isWindowsVistaLaF() || isWindowsXPLaF() || isWindowsLaF()) && i == getSelectedIndex() ) {
                b.x -= 3;
                b.y -= 2;
            } else if( isWindowsXPLaF() || isWindowsLaF() || isAquaLaF() ) {
                b.x -= 2;
            }
            if( i == getTabCount()-1 ) {
                if( isMetalLaF() )
                    b.x--;
                else if( isAquaLaF() ) 
                    b.x -= 3;
            }
            return new Rectangle(b.x + b.width - 13,
                                 b.y + b.height / 2 - 5,
                                 12,
                                 12);
        }
    }


    private boolean isWindowsVistaLaF() {
        String osName = System.getProperty ("os.name");
        return osName.indexOf("Vista") >= 0 
            || (osName.equals( "Windows NT (unknown)" ) && "6.0".equals( System.getProperty("os.version") ));
    }
    
    private boolean isWindowsXPLaF() {
        Boolean isXP = (Boolean)Toolkit.getDefaultToolkit().
                        getDesktopProperty("win.xpstyle.themeActive"); //NOI18N
        return isWindowsLaF() && (isXP == null ? false : isXP.booleanValue());
    }
    
    private boolean isWindowsLaF () {
        String lfID = UIManager.getLookAndFeel().getID();
        return lfID.endsWith("Windows"); //NOI18N
    }
    
    private boolean isAquaLaF() {
        return "Aqua".equals( UIManager.getLookAndFeel().getID() );
    }
    
    private boolean isMetalLaF () {
        String lfID = UIManager.getLookAndFeel().getID();
        return "Metal".equals( lfID ); //NOI18N
    }
    
    public void paint(Graphics g) {
        super.paint(g);

        // Have a look at
        // http://ui.netbeans.org/docs/ui/closeButton/closeButtonUISpec.html
        // to see how the buttons are specified to be drawn.

        int selectedIndex = getSelectedIndex();
        for (int i = 0, n = getTabCount(); i < n; i++) {
            Rectangle r = getCloseButtonBoundsAt(i);
            if (r == null)
                continue;

            if (i == mouseOverCloseButtonIndex
            || (i == pressedCloseButtonIndex && draggedOut)) {
                g.drawImage(getCloseTabMouseOverImage(), r.x, r.y , this);
            } else if (i == pressedCloseButtonIndex) {
                g.drawImage(getCloseTabPressedImage(), r.x, r.y , this);
            } else {
                g.drawImage(getCloseTabImage(), r.x, r.y , this);
            }
        }
    }

    private Image getCloseTabImage() {
        if( null == closeTabImage ) {
            if( isWindowsVistaLaF() ) {
                closeTabImage = org.openide.util.Utilities.loadImage("org/openide/awt/resources/vista_close_enabled.png"); // NOI18N
            } else if( isWindowsXPLaF() ) {
                closeTabImage = org.openide.util.Utilities.loadImage("org/openide/awt/resources/xp_close_enabled.png"); // NOI18N
            } else if( isWindowsLaF() ) {
                closeTabImage = org.openide.util.Utilities.loadImage("org/openide/awt/resources/win_close_enabled.png"); // NOI18N
            } else if( isAquaLaF() ) {
                closeTabImage = org.openide.util.Utilities.loadImage("org/openide/awt/resources/mac_close_enabled.png"); // NOI18N
            } else {
                closeTabImage = org.openide.util.Utilities.loadImage("org/openide/awt/resources/metal_close_enabled.png"); // NOI18N
            }
        }
        return closeTabImage;
    }
    
    private Image getCloseTabPressedImage() {
        if( null == closeTabPressedImage ) {
            if( isWindowsVistaLaF() ) {
                closeTabPressedImage = org.openide.util.Utilities.loadImage("org/openide/awt/resources/vista_close_pressed.png"); // NOI18N
            } else if( isWindowsXPLaF() ) {
                closeTabPressedImage = org.openide.util.Utilities.loadImage("org/openide/awt/resources/xp_close_pressed.png"); // NOI18N
            } else if( isWindowsLaF() ) {
                closeTabPressedImage = org.openide.util.Utilities.loadImage("org/openide/awt/resources/win_close_pressed.png"); // NOI18N
            } else if( isAquaLaF() ) {
                closeTabPressedImage = org.openide.util.Utilities.loadImage("org/openide/awt/resources/mac_close_pressed.png"); // NOI18N
            } else {
                closeTabPressedImage = org.openide.util.Utilities.loadImage("org/openide/awt/resources/metal_close_pressed.png"); // NOI18N
            }
        }
        return closeTabPressedImage;
    }
    
    private Image getCloseTabMouseOverImage() {
        if( null == closeTabMouseOverImage ) {
            if( isWindowsVistaLaF() ) {
                closeTabMouseOverImage = org.openide.util.Utilities.loadImage("org/openide/awt/resources/vista_close_rollover.png"); // NOI18N
            } else if( isWindowsXPLaF() ) {
                closeTabMouseOverImage = org.openide.util.Utilities.loadImage("org/openide/awt/resources/xp_close_rollover.png"); // NOI18N
            } else if( isWindowsLaF() ) {
                closeTabMouseOverImage = org.openide.util.Utilities.loadImage("org/openide/awt/resources/win_close_rollover.png"); // NOI18N
            } else if( isAquaLaF() ) {
                closeTabMouseOverImage = org.openide.util.Utilities.loadImage("org/openide/awt/resources/mac_close_rollover.png"); // NOI18N
            } else {
                closeTabMouseOverImage = org.openide.util.Utilities.loadImage("org/openide/awt/resources/metal_close_rollover.png"); // NOI18N
            }
        }
        return closeTabMouseOverImage;
    }
    
    private void setPressedCloseButtonIndex(int index) {
        if (pressedCloseButtonIndex == index)
            return;

        if (pressedCloseButtonIndex >= 0
        && pressedCloseButtonIndex < getTabCount()) {
            Rectangle r = getCloseButtonBoundsAt(pressedCloseButtonIndex);
            repaint(r.x, r.y, r.width + 2, r.height + 2);

            JComponent c = _getJComponentAt(pressedCloseButtonIndex);
            if( c != null )
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
            JComponent c = _getJComponentAt(mouseOverCloseButtonIndex);
            if( c != null )
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

    private JComponent _getJComponentAt( int tabIndex ) {
        Component c = getComponentAt( tabIndex );
        return c instanceof JComponent ? (JComponent)c : null;
    }
    
    private void fireCloseRequest(Component c) {
        firePropertyChange(PROP_CLOSE, null, c);
    }

    static void fixGetBoundsAt(Rectangle b) {
        if (b.y < 0)
            b.y = -b.y;
        if (b.x < 0)
            b.x = -b.x;
    }

    static int findTabForCoordinate(JTabbedPane tab, int x, int y) {
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
    

    protected void processMouseEvent (MouseEvent me) {
        try {
            super.processMouseEvent (me);
        } catch (ArrayIndexOutOfBoundsException aioobe) {
            //Bug in BasicTabbedPaneUI$Handler:  The focusIndex field is not
            //updated when tabs are removed programmatically, so it will try to
            //repaint a tab that's not there
            Exceptions.attachLocalizedMessage(aioobe,
                                              "Suppressed AIOOBE bug in BasicTabbedPaneUI"); //NOI18N
            Logger.getAnonymousLogger().log(Level.WARNING, null, aioobe);
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
                            tab.repaint(r.x, r.y, r.width + 2+6, r.height + 2+6);
                        }
                        e.consume();
                        return;
                    }
                    break;
            }
        }
    }
}
