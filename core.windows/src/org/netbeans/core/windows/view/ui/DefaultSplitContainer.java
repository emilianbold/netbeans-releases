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


import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.WindowManagerImpl;
import org.netbeans.core.windows.view.ModeView;
import org.netbeans.core.windows.view.ViewElement;
import org.netbeans.core.windows.view.dnd.TopComponentDroppable;
import org.netbeans.core.windows.view.dnd.WindowDnDManager;
import org.netbeans.core.windows.view.ui.tabcontrol.TabbedAdapter;
import org.openide.windows.TopComponent;

import javax.swing.*;
import java.awt.*;


/** 
 * Implementation of <code>ModeContainer</code> for separate mode kind.
 *
 * @author  Peter Zavadsky
 */
public final class DefaultSplitContainer extends AbstractModeContainer {


    /** JPanel instance representing split mode. */
    private final JPanel panel;
    

    /** Creates a DefaultSeparateContainer. */
    public DefaultSplitContainer(ModeView modeView, WindowDnDManager windowDnDManager, int kind) {
        super(modeView, windowDnDManager, kind);
        
        panel = new ModePanel(this);
        
        panel.add(this.tabbedHandler.getComponent(), BorderLayout.CENTER);
        
        // To be able to move split dividers.
        panel.setMinimumSize(new Dimension(1, 1));
    }
    
    public void requestAttention (TopComponent tc) {
        tabbedHandler.requestAttention(tc);
    }
    
    public void cancelRequestAttention (TopComponent tc) {
        tabbedHandler.cancelRequestAttention(tc);
    }

    /** */
    protected Component getModeComponent() {
        return panel;
    }
    
    protected Tabbed createTabbed() {
        Tabbed tabbed;
        if(getKind() == Constants.MODE_KIND_EDITOR) {
            tabbed = new TabbedAdapter(Constants.MODE_KIND_EDITOR);
        } else {
            tabbed = new TabbedAdapter(Constants.MODE_KIND_VIEW);
        }
        return tabbed;    
    }    
    
    protected void updateTitle(String title) {
        // no op
    }
    
    protected void updateActive(boolean active) {
        if(active) {
            Window window = SwingUtilities.getWindowAncestor(panel);
            if(window != null && !window.isActive() && WindowManagerImpl.getInstance().getEditorAreaState() == Constants.EDITOR_AREA_SEPARATED) {
                // only front in SDI, in MID assume that it's either active or user doens't want it active..
                window.toFront();
            }
        }
    }
    
    public boolean isActive() {
        Window window = SwingUtilities.getWindowAncestor(panel);
        // #54791 and #56613 - just a doublecheck, IMHO should not happen anymore
        // after the winsys reenetrancy fix.
        return window != null ? window.isActive() : false;
    }

    protected boolean isAttachingPossible() {
        return true;
    }
    
    protected TopComponentDroppable getModeDroppable() {
        return (ModePanel)panel;
    }


    /** */
    private static class ModePanel extends JPanel
    implements ModeComponent, TopComponentDroppable {
    
        private final AbstractModeContainer abstractModeContainer;
        
        public ModePanel(AbstractModeContainer abstractModeContainer) {
            super(new BorderLayout());
            this.abstractModeContainer = abstractModeContainer;
            // To be able to activate on mouse click.
            enableEvents(java.awt.AWTEvent.MOUSE_EVENT_MASK);
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
            
            boolean isNonEditor = kind == Constants.MODE_KIND_VIEW || kind == Constants.MODE_KIND_SLIDING;
            boolean thisIsNonEditor = getKind() == Constants.MODE_KIND_VIEW || getKind() == Constants.MODE_KIND_SLIDING;

            return (isNonEditor == thisIsNonEditor);

        }
        // TopComponentDroppable<<
    } // End of ModePanel.
}

