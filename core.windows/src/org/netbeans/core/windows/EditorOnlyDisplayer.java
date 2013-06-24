/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.core.windows;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLayer;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.plaf.LayerUI;
import org.openide.filesystems.FileUtil;
import org.openide.util.actions.Presenter;
import org.openide.windows.TopComponent;

/**
 * Replaces main window's content pane with currently selected editor TopComponent
 * to provide distraction-free editing environment. When deactivated or when
 * other TopComponent is activated it puts back the original content pane.
 *
 * @author S. Aubrecht
 */
public class EditorOnlyDisplayer {

    private static EditorOnlyDisplayer theInstance;

    private final PropertyChangeListener registryListener;
    private Container originalContentPane = null;
    private boolean originalShowEditorToolbar = true;

    private EditorOnlyDisplayer() {
        registryListener = new PropertyChangeListener() {
            @Override
            public void propertyChange( PropertyChangeEvent evt ) {
                onRegistryChange( evt );
            }
        };
    }

    public static EditorOnlyDisplayer getInstance() {
        synchronized( EditorOnlyDisplayer.class ) {
            if( null == theInstance ) {
                theInstance = new EditorOnlyDisplayer();
            }
        }
        return theInstance;
    }

    public boolean isActive() {
        return null != originalContentPane;
    }

    public void setActive( boolean activate ) {
        if( activate == isActive() )
            return;
        if( isActive() ) {
            cancel();
        } else {
            activate();
        }
    }

    private void onRegistryChange( PropertyChangeEvent evt ) {
        if( TopComponent.Registry.PROP_ACTIVATED.equals( evt.getPropertyName() ) ) {
            
            if( switchCurrentEditor() ) {
                return;
            }
            cancel();
        }
    }

    private boolean switchCurrentEditor() {
        final TopComponent tc = TopComponent.getRegistry().getActivated();
        if( null == tc || !TopComponentTracker.getDefault().isEditorTopComponent( tc ) )
            return false;

        final WindowManagerImpl wmi = WindowManagerImpl.getInstance();
        final JFrame mainWnd = ( JFrame ) wmi.getMainWindow();
        if( SwingUtilities.isDescendingFrom( tc, mainWnd.getContentPane() ) )
            return true;
        JPanel panel = new JPanel( new BorderLayout() );
        panel.add( tc, BorderLayout.CENTER  );
        mainWnd.setContentPane( panel );
        mainWnd.invalidate();
        mainWnd.revalidate();
        mainWnd.repaint();
        SwingUtilities.invokeLater( new Runnable() {
            @Override
            public void run() {
                tc.requestFocusInWindow();
            }
        });
        return true;
    }

    private void cancel() {
        TopComponent.getRegistry().removePropertyChangeListener( registryListener );
        JFrame frame = ( JFrame ) WindowManagerImpl.getInstance().getMainWindow();
        frame.setContentPane( originalContentPane );
        originalContentPane = null;
        setShowEditorToolbar( originalShowEditorToolbar );
        final TopComponent tc = TopComponent.getRegistry().getActivated();
        if( null != tc ) {
            SwingUtilities.invokeLater( new Runnable() {
                @Override
                public void run() {
                    tc.requestFocusInWindow();
                }
            });
        }
    }

    private void activate() {
        assert null == originalContentPane;

        final TopComponent tc = TopComponent.getRegistry().getActivated();
        if( null == tc || !TopComponentTracker.getDefault().isEditorTopComponent( tc ) )
            return;

        final WindowManagerImpl wmi = WindowManagerImpl.getInstance();
        final JFrame mainWnd = ( JFrame ) wmi.getMainWindow();

        originalContentPane = mainWnd.getContentPane();

        JPanel panel = new JPanel( new BorderLayout() );
        panel.add( tc, BorderLayout.CENTER  );
//        mainWnd.setContentPane( new JLayer( panel, new EditorLayerUI() ) );
        mainWnd.setContentPane( panel );
        mainWnd.invalidate();
        mainWnd.revalidate();
        mainWnd.repaint();

        wmi.getRegistry().addPropertyChangeListener( registryListener );

        originalShowEditorToolbar = setShowEditorToolbar( false );

        SwingUtilities.invokeLater( new Runnable() {

            @Override
            public void run() {
                tc.requestFocusInWindow();
            }
        });
    }

    private static class EditorLayerUI extends LayerUI<JPanel> {

        @Override
        public void paint( Graphics g, JComponent c ) {
            super.paint( g, c ); //To change body of generated methods, choose Tools | Templates.

            g.setColor( Color.red );
            int width = c.getWidth();
            int height = c.getHeight();
            g.drawRect( width-30, 10, 20, 20 );
        }

        @Override
        protected void processMouseMotionEvent( MouseEvent e, JLayer<? extends JPanel> l ) {
            super.processMouseMotionEvent( e, l ); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        protected void processMouseEvent( MouseEvent e, JLayer<? extends JPanel> l ) {
            if( e.getID() == MouseEvent.MOUSE_CLICKED && e.getButton() == MouseEvent.BUTTON1 ) {
                Point p = e.getPoint();
                Component c = e.getComponent();

                int width = c.getWidth();
                int height = c.getHeight();
                Rectangle rect = new Rectangle( width-30, 10, 20, 20 );
                if( rect.contains( p ) ) {
                    e.consume();
                    EditorOnlyDisplayer.getInstance().cancel();
                }
            }
        }

        @Override
        public void installUI( JComponent c ) {
            super.installUI( c );
            ((JLayer) c).setLayerEventMask(AWTEvent.MOUSE_MOTION_EVENT_MASK + AWTEvent.MOUSE_EVENT_MASK);
        }

        @Override
        public void uninstallUI(JComponent c) {
           super.uninstallUI(c);
            // reset the layer event mask
            ((JLayer) c).setLayerEventMask(0);
        }
    }

    private static boolean setShowEditorToolbar( boolean show ) {
        boolean res = true;
        Action toggleEditorToolbar = FileUtil.getConfigObject( "Editors/Actions/toggle-toolbar.instance", Action.class ); //NOI18N
        if( null != toggleEditorToolbar ) {
            if( toggleEditorToolbar instanceof Presenter.Menu ) {
                JMenuItem menuItem = ((Presenter.Menu)toggleEditorToolbar).getMenuPresenter();
                if( menuItem instanceof JCheckBoxMenuItem ) {
                    JCheckBoxMenuItem checkBoxMenu = ( JCheckBoxMenuItem ) menuItem;
                    res = checkBoxMenu.isSelected();
                    if( checkBoxMenu.isSelected() != show ) {
                        try {
                            toggleEditorToolbar.actionPerformed( new ActionEvent( menuItem, 0, "")); //NOII18N
                        } catch( Exception ex ) {
                            //don't worry too much if it isn't working, we're just trying to be helpful here
                            Logger.getLogger( EditorOnlyDisplayer.class.getName()).log( Level.FINE, null, ex );
                        }
                    }
                }
            }
        }

        return res;
    }
}
