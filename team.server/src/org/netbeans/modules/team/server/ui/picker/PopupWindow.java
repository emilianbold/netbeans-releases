/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.team.server.ui.picker;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import org.netbeans.modules.team.server.ui.common.LinkButton;
import org.netbeans.modules.team.commons.treelist.ListRendererPanel;
import org.openide.util.Utilities;
import org.openide.windows.WindowManager;

/**
 *
 */
final class PopupWindow  {
    
    // private static MyFocusListener mfl = new MyFocusListener();
    
    private static final String CLOSE_KEY = "CloseKey"; //NOI18N
    private static final Action CLOSE_ACTION = new CloseAction();
    private static final KeyStroke ESC_KEY_STROKE = KeyStroke.getKeyStroke( KeyEvent.VK_ESCAPE, 0 ); 
        
    private static final String POPUP_NAME = "popupComponent"; //NOI18N
    private static JDialog popupWindow;
    private static JComponent invokerComponent;
    private static Frame owner;
    private static HideAWTListener hideListener = new HideAWTListener();
    private static Component focusAfterOpen;
    
    // Singleton
    private PopupWindow() {
    }
    
    
    public static void showPopup(final JComponent content, JComponent invoker) {
        if (popupWindow != null ) {
            return; // Content already showing
        }
                           
        Toolkit.getDefaultToolkit().addAWTEventListener(hideListener, AWTEvent.MOUSE_EVENT_MASK);
        
        // NOT using PopupFactory
        // 1. on linux, creates mediumweight popup taht doesn't refresh behind visible glasspane
        // 2. on mac, needs an owner frame otherwise hiding tooltip also hides the popup. (linux requires no owner frame to force heavyweight)
        // 3. the created window is not focusable window
        owner = ( Frame ) SwingUtilities.getAncestorOfClass(Frame.class, invoker);
        popupWindow = new JDialog( getMainWindow() );
        popupWindow.setName( POPUP_NAME );
        popupWindow.setUndecorated(true);
        popupWindow.getRootPane().getInputMap( JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT ).put( ESC_KEY_STROKE, CLOSE_KEY );
        popupWindow.getRootPane().getActionMap().put( CLOSE_KEY, CLOSE_ACTION );
	
        invokerComponent = invoker;
	//set a11y
	String a11yName = content.getAccessibleContext().getAccessibleName();
	if(a11yName != null && !a11yName.equals(""))
	    popupWindow.getAccessibleContext().setAccessibleName(a11yName);
	String a11yDesc = content.getAccessibleContext().getAccessibleDescription();
	if(a11yDesc != null && !a11yDesc.equals(""))
	    popupWindow.getAccessibleContext().setAccessibleDescription(a11yDesc);
	    
        // popupWindow.setAlwaysOnTop( true );
        popupWindow.getContentPane().add(content);
        // popupWindow.addFocusListener( mfl );                        
        // content.addFocusListener( mfl );                        
                
        getMainWindow().addWindowStateListener(hideListener);
        getMainWindow().addComponentListener(hideListener);
        resizePopup();

        int x = invoker.getLocationOnScreen().x;
        int y = invoker.getLocationOnScreen().y+invoker.getHeight();
        Point p = fitToScreen( x, y );
        Rectangle screen = org.openide.util.Utilities.getUsableScreenBounds();
        if (p.y < screen.y) {
            int yAdjustment = screen.y - p.y;
            p.y += yAdjustment;
            popupWindow.setSize(popupWindow.getWidth(), popupWindow.getHeight() - yAdjustment);
        }
        popupWindow.setLocation(p.x, p.y);
        popupWindow.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                Component focus = focusAfterOpen;
                if (focus == null) {
                    focus = content;
                }
                focus.requestFocus();
                focus.requestFocusInWindow();
                if (focus == content) { // the content component itself (a panel) is not focusable
                    KeyboardFocusManager.getCurrentKeyboardFocusManager().focusNextComponent(content);
                }
            }
        });
        popupWindow.setVisible( true );
    }

    public static void hidePopup() {
        if (popupWindow != null) {
            Toolkit.getDefaultToolkit().removeAWTEventListener(hideListener);
            
            popupWindow.setVisible( false );
            popupWindow.dispose();
        }
        getMainWindow().removeWindowStateListener(hideListener);
        getMainWindow().removeComponentListener(hideListener);
        popupWindow = null;
        owner = null;
        invokerComponent = null;
        focusAfterOpen = null;
    }

    static void setFocusedComponent(Component comp) {
        focusAfterOpen = comp;
    }

    static boolean isShowing() {
        return popupWindow != null;
    }

    
    static void pack() {
        if( null == popupWindow ) {
            return;
        }
        popupWindow.pack();
    }

    private static void resizePopup() {
        popupWindow.pack();
        Point point = new Point(0,0);
        SwingUtilities.convertPointToScreen(point, getMainWindow());
        popupWindow.setLocation( point.x + (getMainWindow().getWidth() - popupWindow.getWidth()) / 2,
                                 point.y + (getMainWindow().getHeight() - popupWindow.getHeight()) / 3);
    }
    
    private static final int X_INSET = 10;
    private static final int Y_INSET = X_INSET;
    
    private static Point fitToScreen( int x, int y ) {
        
        Rectangle screen = Utilities.getUsableScreenBounds();
                
        Point p = new Point( x, y );
        
        // Adjust the x postition if necessary
        if ( ( p.x + popupWindow.getWidth() ) > ( screen.x + screen.width - X_INSET ) ) {
            p.x = screen.x + screen.width - X_INSET - popupWindow.getWidth(); 
        }
        
        // Adjust the y position if necessary
        if ( ( p.y + popupWindow.getHeight() ) > ( screen.y + screen.height - Y_INSET ) ) {
            p.y = p.y - popupWindow.getHeight();
        }
        
        return p;     
    }

    
    private static Frame getMainWindow() {
        return owner != null ? owner : WindowManager.getDefault().getMainWindow();
    }
    
    // Innerclasses ------------------------------------------------------------
    
    private static class HideAWTListener extends ComponentAdapter implements  AWTEventListener, WindowStateListener {
        
        @Override
        public void eventDispatched(java.awt.AWTEvent aWTEvent) {
            if (aWTEvent instanceof MouseEvent) {
                MouseEvent mv = (MouseEvent)aWTEvent;
                if (mv.getID() == MouseEvent.MOUSE_CLICKED && mv.getClickCount() > 0) {
                    //#118828
                    if (! (aWTEvent.getSource() instanceof Component)) {
                        hidePopup();
                        return;
                    }
                    
                    Component comp = (Component)aWTEvent.getSource();
                     //ignore click into modal dialogs
                    if( comp instanceof Dialog && ((Dialog)comp).isModal() )
                        return;
                    Window window = SwingUtilities.getWindowAncestor( comp );
                    if( window instanceof Dialog && ((Dialog)window).isModal() )
                        return;
                    if( null != invokerComponent && SwingUtilities.isDescendingFrom( comp, invokerComponent) )
                        return;
                    if( popupWindow == comp )
                        return;
                    if( SwingUtilities.getAncestorOfClass(ListRendererPanel.class, comp) != null) {
                        Object c = comp instanceof LinkButton ? ((LinkButton)comp).getClientProperty("MM.NotClosing") : null;
                        if(c != null && (boolean)c) 
                        {
                            return;
                        }
                    }
                    Container par = SwingUtilities.getAncestorNamed(POPUP_NAME, comp); //NOI18N
                    // Container barpar = SwingUtilities.getAncestorOfClass(PopupUtil.class, comp);
                    // if (par == null && barpar == null) {
                    if ( par == null ) {
                        hidePopup();
                    }
                }
            }
        }

        @Override
        public void windowStateChanged(WindowEvent windowEvent) {
            if (popupWindow != null ) {
                int oldState = windowEvent.getOldState();
                int newState = windowEvent.getNewState();
            
                if (((oldState & Frame.ICONIFIED) == 0) &&
                    ((newState & Frame.ICONIFIED) == Frame.ICONIFIED)) {
                    hidePopup();
//                } else if (((oldState & Frame.ICONIFIED) == Frame.ICONIFIED) && 
//                           ((newState & Frame.ICONIFIED) == 0 )) {
//                    //TODO remember we showed before and show again? I guess not worth the efford, not part of spec.
                }
            }

        }
        
        @Override
        public void componentResized(ComponentEvent evt) {
            if (popupWindow != null) {
                hidePopup();
            }
        }
        
        @Override
        public void componentMoved(ComponentEvent evt) {
            if (popupWindow!= null) {
                hidePopup();
            }
        }        
    }
    
    private static class CloseAction extends AbstractAction {
        
        @Override
        public void actionPerformed(java.awt.event.ActionEvent e) {
            hidePopup();
        }
    }
    
}
