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
import org.openide.util.ImageUtilities;
import org.openide.util.Utilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
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

    @Override
    public Component add (Component c) {
        Component result = super.add(c);
        if (isNoCloseButton(c)) {
            return result;
        }
        // #75317 - don't try to set the title if LF (such as Substance LF)
        // is adding some custom UI components into tabbed pane
        if (!(c instanceof UIResource)) {
            String s = c.getName();
            if (s != null) {
                s += "  ";
                setTitleAt(getTabCount()-1, s);
            }
        }
        return result;
    }

    private static final boolean HTML_TABS_BROKEN = htmlTabsBroken();
    private static boolean htmlTabsBroken() {
        String version = System.getProperty("java.version");
        for (int i = 14; i < 18; i++) {
            if (version.startsWith("1.6.0_" + i)) {
                return true;
            }
        }
        return false;
    }
    private final Pattern removeHtmlTags = HTML_TABS_BROKEN ? Pattern.compile("\\<.*?\\>") : null;

    @Override
    public void setTitleAt(int idx, String title) {
        // workaround for JDK bug (http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6670274)
        // NB issue #113388
        if (removeHtmlTags != null && title.startsWith("<html>")) {
            title = removeHtmlTags.matcher(title).replaceAll("");
            title = title.replace("&nbsp;", "");
        }

        Component c = findTabAt(idx);
        //if NO_CLOSE_BUTTON -> just call super
        if (isNoCloseButton(c)) {
            super.setTitleAt(idx, title);
        }

        String nue = title.indexOf("</html>") != -1 ? //NOI18N
            title.replace("</html>", "&nbsp;&nbsp;</html>") //NOI18N
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

    private Component findTabAt(int index) {
        int componentIndex = -1;
        for( Component c : getComponents() ) {
            if( c instanceof UIResource )
                continue;
            if( ++componentIndex == index )
                return c;
        }
        return null;
    }

    private boolean isNoCloseButton(Component c) {
        if (c!=null && c instanceof JComponent) {
            Object prop = ((JComponent) c).getClientProperty(TabbedPaneFactory.NO_CLOSE_BUTTON);
            if (prop!=null && prop instanceof Boolean && (Boolean) prop) {
                return true;
            }
        }
        return false;
    }

    private Rectangle getCloseButtonBoundsAt(int i) {
        Component c = findTabAt(i);
        //if NO_CLOSE_BUTTON -> return null
        if (isNoCloseButton(c)) {
            return null;
        }
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
            } else if( isGTKLaF() && i == getSelectedIndex() ) {
                b.x -= 1;
                b.y -= 2;
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

    private boolean isGTKLaF () {
        return "GTK".equals( UIManager.getLookAndFeel().getID() ); //NOI18N
    }
    
    @Override
    public void paint(Graphics g) {
        super.paint(g);

        // Have a look at
        // http://ui.netbeans.org/docs/ui/closeButton/closeButtonUISpec.html
        // to see how the buttons are specified to be drawn.

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
                closeTabImage = ImageUtilities.loadImage("org/openide/awt/resources/vista_close_enabled.png"); // NOI18N
            } else if( isWindowsXPLaF() ) {
                closeTabImage = ImageUtilities.loadImage("org/openide/awt/resources/xp_close_enabled.png"); // NOI18N
            } else if( isWindowsLaF() ) {
                closeTabImage = ImageUtilities.loadImage("org/openide/awt/resources/win_close_enabled.png"); // NOI18N
            } else if( isAquaLaF() ) {
                closeTabImage = ImageUtilities.loadImage("org/openide/awt/resources/mac_close_enabled.png"); // NOI18N
            } else {
                closeTabImage = ImageUtilities.loadImage("org/openide/awt/resources/metal_close_enabled.png"); // NOI18N
            }
        }
        return closeTabImage;
    }
    
    private Image getCloseTabPressedImage() {
        if( null == closeTabPressedImage ) {
            if( isWindowsVistaLaF() ) {
                closeTabPressedImage = ImageUtilities.loadImage("org/openide/awt/resources/vista_close_pressed.png"); // NOI18N
            } else if( isWindowsXPLaF() ) {
                closeTabPressedImage = ImageUtilities.loadImage("org/openide/awt/resources/xp_close_pressed.png"); // NOI18N
            } else if( isWindowsLaF() ) {
                closeTabPressedImage = ImageUtilities.loadImage("org/openide/awt/resources/win_close_pressed.png"); // NOI18N
            } else if( isAquaLaF() ) {
                closeTabPressedImage = ImageUtilities.loadImage("org/openide/awt/resources/mac_close_pressed.png"); // NOI18N
            } else {
                closeTabPressedImage = ImageUtilities.loadImage("org/openide/awt/resources/metal_close_pressed.png"); // NOI18N
            }
        }
        return closeTabPressedImage;
    }
    
    private Image getCloseTabMouseOverImage() {
        if( null == closeTabMouseOverImage ) {
            if( isWindowsVistaLaF() ) {
                closeTabMouseOverImage = ImageUtilities.loadImage("org/openide/awt/resources/vista_close_rollover.png"); // NOI18N
            } else if( isWindowsXPLaF() ) {
                closeTabMouseOverImage = ImageUtilities.loadImage("org/openide/awt/resources/xp_close_rollover.png"); // NOI18N
            } else if( isWindowsLaF() ) {
                closeTabMouseOverImage = ImageUtilities.loadImage("org/openide/awt/resources/win_close_rollover.png"); // NOI18N
            } else if( isAquaLaF() ) {
                closeTabMouseOverImage = ImageUtilities.loadImage("org/openide/awt/resources/mac_close_rollover.png"); // NOI18N
            } else {
                closeTabMouseOverImage = ImageUtilities.loadImage("org/openide/awt/resources/metal_close_rollover.png"); // NOI18N
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
            if (r != null) {
                repaint(r.x, r.y, r.width + 2, r.height + 2);
            }

            JComponent c = _getJComponentAt(pressedCloseButtonIndex);
            if( c != null )
                setToolTipTextAt(pressedCloseButtonIndex, c.getToolTipText());
        }

        pressedCloseButtonIndex = index;

        if (pressedCloseButtonIndex >= 0
        && pressedCloseButtonIndex < getTabCount()) {
            Rectangle r = getCloseButtonBoundsAt(pressedCloseButtonIndex);
            if (r != null) {
                repaint(r.x, r.y, r.width + 2, r.height + 2);
            }
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
            if (r != null) {
                repaint(r.x, r.y, r.width + 2, r.height + 2);
            }
            JComponent c = _getJComponentAt(mouseOverCloseButtonIndex);
            if( c != null )
                setToolTipTextAt(mouseOverCloseButtonIndex, c.getToolTipText());
        }

        mouseOverCloseButtonIndex = index;

        if (mouseOverCloseButtonIndex >= 0
        && mouseOverCloseButtonIndex < getTabCount()) {
            Rectangle r = getCloseButtonBoundsAt(mouseOverCloseButtonIndex);
            if (r != null) {
                repaint(r.x, r.y, r.width + 2, r.height + 2);
            }
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
    

    @Override
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

    @Override
    protected void fireStateChanged() {
        try {
            super.fireStateChanged();
        } catch( ArrayIndexOutOfBoundsException e ) {
            if( Utilities.isMac() ) {
                //#126651 - JTabbedPane is buggy on Mac OS
            } else {
                throw e;
            }
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
            //#118828
            if (! (ev.getSource() instanceof Component)) {
                return;
            }
        
            
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
                    if (r.contains(p) || e.getButton() == MouseEvent.BUTTON2) {
                        tab.setPressedCloseButtonIndex(index);
                        tab.draggedOut = false;
                        e.consume();
                        return;
                    }
                    break;

                case MouseEvent.MOUSE_RELEASED:
                    if ((r.contains(p) && tab.pressedCloseButtonIndex >= 0)
                            || (e.getButton() == MouseEvent.BUTTON2 && index == tab.pressedCloseButtonIndex)) {
                        
                        Component tc = null;
                        if( tab.pressedCloseButtonIndex >= 0
                                && tab.pressedCloseButtonIndex < tab.getComponentCount() ) {
                            tc = tab.findTabAt( tab.pressedCloseButtonIndex );
                        }
                        tab.reset();
                        if( null != tc )
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
