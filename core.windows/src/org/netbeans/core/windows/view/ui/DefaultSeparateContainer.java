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


import java.awt.Component;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.view.dnd.TopComponentDroppable;
import org.netbeans.core.windows.view.dnd.WindowDnDManager;
import org.netbeans.core.windows.view.ModeContainer;
import org.netbeans.core.windows.view.ModeView;
import org.netbeans.core.windows.view.ViewElement;
import org.netbeans.core.windows.WindowManagerImpl;

import org.openide.windows.TopComponent;


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

        frame = new ModeFrame(this);
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
        
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                modeView.getController().userClosingMode(modeView);
            }
        });
        
        frame.addWindowStateListener(new WindowStateListener() {
            public void windowStateChanged(WindowEvent evt) {
                modeView.getController().userChangedFrameStateMode(modeView, evt.getNewState());
            }
        });
    }

    /** */
    protected Component getModeComponent() {
        return frame;
    }
    
    protected void updateTitle(String title) {
        frame.setTitle(title);
    }
    
    protected void updateActive(boolean active) {
        if(active) {
            frame.toFront();
        } 
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
        
        public ModeFrame(AbstractModeContainer abstractModeContainer) {
            super(""); // NOI18N
            this.abstractModeContainer = abstractModeContainer;
            // To be able to activate on mouse click.
            enableEvents(java.awt.AWTEvent.MOUSE_EVENT_MASK);
            
            setIconImage(MainWindow.createIDEImage());
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

