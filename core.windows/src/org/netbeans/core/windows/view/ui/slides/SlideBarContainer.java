/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.windows.view.ui.slides;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Window;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.WindowManagerImpl;
import org.netbeans.core.windows.view.ModeView;
import org.netbeans.core.windows.view.SlidingView;
import org.netbeans.core.windows.view.ViewElement;
import org.netbeans.core.windows.view.dnd.WindowDnDManager;
import org.netbeans.core.windows.view.ui.AbstractModeContainer;
import org.netbeans.core.windows.view.ui.ModeComponent;
import org.netbeans.core.windows.view.dnd.TopComponentDroppable;
import org.netbeans.core.windows.view.ui.Tabbed;
import org.openide.windows.TopComponent;


/*
 * SlideBarContainer.java
 *
 * @author Dafe Simonek
 */
public final class SlideBarContainer extends AbstractModeContainer {
    
    /** panel displaying content of this container */
    VisualPanel panel;
    
    /** Creates a new instance of SlideBarContainer */
    public SlideBarContainer(ModeView modeView, WindowDnDManager windowDnDManager) {
        super(modeView, windowDnDManager, Constants.MODE_KIND_SLIDING);
        
        panel = new VisualPanel(this);
        panel.setBorder(computeBorder(getSlidingView().getSide()));
        panel.add(this.tabbedHandler.getComponent(), BorderLayout.CENTER);
    }
    
    
    private SlidingView getSlidingView() {
        return (SlidingView)super.getModeView();
    }
    public void requestAttention (TopComponent tc) {
        tabbedHandler.requestAttention(tc);
    }

    public void cancelRequestAttention (TopComponent tc) {
        tabbedHandler.cancelRequestAttention (tc);
    }    
    
    public void setTopComponents(TopComponent[] tcs, TopComponent selected) {
        super.setTopComponents(tcs, selected);
    }
    
    public Rectangle getTabBounds(int tabIndex) {
        return tabbedHandler.getTabBounds(tabIndex);
    }

    protected Component getModeComponent() {
        return panel;
    }
    
    protected Tabbed createTabbed() {
        return new TabbedSlideAdapter(((SlidingView)modeView).getSide());
    }
    
    protected boolean isAttachingPossible() {
        return false;
    }

    protected TopComponentDroppable getModeDroppable() {
        return panel;
    }    
    
    protected void updateActive(boolean active) {
        // #48588 - when in SDI, slidein needs to front the editor frame.
        if(active) {
            Window window = SwingUtilities.getWindowAncestor(panel);
            if(window != null && !window.isActive() && WindowManagerImpl.getInstance().getEditorAreaState() == Constants.EDITOR_AREA_SEPARATED) {
                window.toFront();
            }
        }
    }
    
    public boolean isActive() {
        Window window = SwingUtilities.getWindowAncestor(panel);
        return window.isActive();
    }    
    
    protected void updateTitle(String title) {
        // XXX - we have no title?
    }
    
    /** Builds empty border around slide bar. Computes its correct size
     * based on given orientation
     */
    private static Border computeBorder(String orientation) {
        int bottom = 0, left = 0, right = 0, top = 0;
        if (Constants.LEFT.equals(orientation)) {
            top = 1; left = 1; bottom = 1; right = 1; 
        }
        if (Constants.BOTTOM.equals(orientation)) {
            top = 2; left = 1; bottom = 1; right = 1; 
        }
        if (Constants.RIGHT.equals(orientation)) {
            top = 1; left = 2; bottom = 1; right = 1; 
        }
        return new EmptyBorder(top, left, bottom, right);
    }
    
    
    /** Component enclosing slide boxes, implements needed interfaces to talk
     * to rest of winsys
     */
    private static class VisualPanel extends JPanel implements ModeComponent, TopComponentDroppable {
    
        private final SlideBarContainer modeContainer;
        
        public VisualPanel (SlideBarContainer modeContainer) {
            super(new BorderLayout());
            this.modeContainer = modeContainer;
            // To be able to activate on mouse click.
            enableEvents(java.awt.AWTEvent.MOUSE_EVENT_MASK);
            setMinimumSize(new Dimension(1,1));
        }
        
        public ModeView getModeView() {
            return modeContainer.getModeView();
        }
        
        public int getKind() {
            return modeContainer.getKind();
        }
        
        // TopComponentDroppable>>
        public Shape getIndicationForLocation(Point location) {
            return modeContainer.getIndicationForLocation(location);
        }
        
        public Object getConstraintForLocation(Point location) {
            return modeContainer.getConstraintForLocation(location);
        }
        
        public Component getDropComponent() {
            return modeContainer.getDropComponent();
        }
        
        public ViewElement getDropViewElement() {
            return modeContainer.getDropModeView();
        }
        
        public boolean canDrop(TopComponent transfer, Point location) {
            return modeContainer.canDrop(transfer);
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
        
    } // End of VisualPanel
    
}
