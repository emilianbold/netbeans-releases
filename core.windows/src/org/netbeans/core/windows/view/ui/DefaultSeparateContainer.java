/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.core.windows.view.ui;


import java.awt.KeyboardFocusManager;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.CharConversionException;
import javax.swing.plaf.basic.BasicHTML;
import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.WindowManagerImpl;
import org.netbeans.core.windows.view.ModeView;
import org.netbeans.core.windows.view.ViewElement;
import org.netbeans.core.windows.view.dnd.TopComponentDroppable;
import org.netbeans.core.windows.view.dnd.WindowDnDManager;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import org.openide.xml.XMLUtil;


/** 
 * Implementation of <code>ModeContainer</code> for separate mode kind.
 *
 * @author  Peter Zavadsky
 */
public final class DefaultSeparateContainer extends AbstractModeContainer {

    /** JFrame instance representing the separated mode. */
    private final JFrame frame;

    /** Creates a DefaultSeparateContainer. */
    public DefaultSeparateContainer(final ModeView modeView, WindowDnDManager windowDnDManager, Rectangle bounds) {
        super(modeView, windowDnDManager); // NOI18N

        frame = new ModeFrame(this, modeView);
        frame.getContentPane().add(tabbedHandler.getComponent());
        frame.setBounds(bounds);

        frame.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent evt) {
                if(DefaultSeparateContainer.this.frame.getExtendedState() == Frame.MAXIMIZED_BOTH) {
                    // Ignore changes when the frame is in maximized state.
                    return;
                }
                
                modeView.getController().userResizedModeBounds(
                    modeView, DefaultSeparateContainer.this.frame.getBounds());
            }
            
            public void componentMoved(ComponentEvent evt) {
                if(DefaultSeparateContainer.this.frame.getExtendedState() == Frame.MAXIMIZED_BOTH) {
                    // Ignore changes when the frame is in maximized state.
                    return;
                }
                
                modeView.getController().userResizedModeBounds(
                        modeView, DefaultSeparateContainer.this.frame.getBounds());
            }
        });
        
        
        frame.addWindowStateListener(new WindowStateListener() {
            
            
            public void windowStateChanged(WindowEvent evt) {
     // All the timestamping is a a workaround beause of buggy GNOME and of its kind who iconify the windows on leaving the desktop.
                Component comp = modeView.getComponent();
                if (comp instanceof Frame /*&& comp.isVisible() */) {
                    long currentStamp = System.currentTimeMillis();
                    if (currentStamp > (modeView.getUserStamp() + 500) && currentStamp > (modeView.getMainWindowStamp() + 1000)) {
                        modeView.getController().userChangedFrameStateMode(modeView, evt.getNewState());
                    } else {
                        modeView.setUserStamp(0);
                        modeView.setMainWindowStamp(0);
                        modeView.updateFrameState();
                    }
                    long stamp = System.currentTimeMillis();
                    modeView.setUserStamp(stamp);
                } 
            }
        });
    }

    /** */
    protected Component getModeComponent() {
        return frame;
    }
    
    protected void updateTitle(String title) {
        if (BasicHTML.isHTMLString(title)) {
            //Output window (and soon others) use HTML - looks
            //nasty in window frame titles
            char[] c = title.toCharArray();
            StringBuffer sb = new StringBuffer(title.length());
            boolean inTag = false;
            boolean inEntity = false;
            for (int i=0; i < c.length; i++) {
                if (inTag && c[i] == '>') { //NOI18N
                    inTag = false;
                    continue;
                }
                if (!inTag && c[i] == '<') { //NOI18N
                    inTag = true;
                    continue;
                }
                if (!inTag) {
                    sb.append(c[i]);
                }
            }
            //XXX, would be nicer to support the full complement of entities...
            title = Utilities.replaceString(sb.toString(), "&nbsp;", " "); //NOI18N
        }
        frame.setTitle(title);
    }
    
    protected void updateActive(boolean active) {
        if(active) {
            if (frame.isVisible()) {
                frame.toFront();
            }
        } 
    }
    
    public boolean isActive() {
        return frame.isActive();
    }
    
    protected boolean isAttachingPossible() {
        return false;
    }
    
    protected TopComponentDroppable getModeDroppable() {
        return (ModeFrame)frame;
    }

    /** */
    private static class ModeFrame extends JFrame
    implements ModeComponent, TopComponentDroppable {
        
        private final AbstractModeContainer abstractModeContainer;
        private final ModeView modeView;
        private long frametimestamp = 0;
        
        public ModeFrame(AbstractModeContainer abstractModeContainer, ModeView view) {
            super(""); // NOI18N
            this.abstractModeContainer = abstractModeContainer;
            modeView = view;
            // To be able to activate on mouse click.
            enableEvents(java.awt.AWTEvent.MOUSE_EVENT_MASK);
            setIconImage(MainWindow.createIDEImage());
            addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent evt) {
                    modeView.getController().userClosingMode(modeView);
                }
            
                public void windowActivated(WindowEvent event) {
                    if (frametimestamp != 0 && System.currentTimeMillis() > frametimestamp + 500) {
                        modeView.getController().userActivatedModeWindow(modeView);
                    } 
                }
                public void windowOpened(WindowEvent event) {
                    frametimestamp = System.currentTimeMillis();
                }
            });
            
        }
        
        public void setVisible(boolean visible) {
            frametimestamp = System.currentTimeMillis();
            super.setVisible(visible);
        }
        
        public void toFront() {
            frametimestamp = System.currentTimeMillis();
            super.toFront();
        }
        
        public ModeView getModeView() {
            return abstractModeContainer.getModeView();
        }
        
        public int getKind() {
            return abstractModeContainer.getKind();
        }
        
        
        // TopComponentDroppable>>
        public Shape getIndicationForLocation(Point location) {
            return abstractModeContainer.getIndicationForLocation(location);
        }
        
        public Object getConstraintForLocation(Point location) {
            return abstractModeContainer.getConstraintForLocation(location);
        }
        
        public Component getDropComponent() {
            return abstractModeContainer.getDropComponent();
        }
        
        public ViewElement getDropViewElement() {
            return abstractModeContainer.getDropModeView();
        }
        
        public boolean canDrop(TopComponent transfer, Point location) {
            return abstractModeContainer.canDrop(transfer);
        }
        
        public boolean supportsKind(int kind, TopComponent transfer) {
            if(Constants.SWITCH_MODE_ADD_NO_RESTRICT
            || WindowManagerImpl.getInstance().isTopComponentAllowedToMoveAnywhere(transfer)) {
                return true;
            }

            return kind == Constants.MODE_KIND_VIEW;
        }
        // TopComponentDroppable<<
    } // End of ModeFrame.
    
}

