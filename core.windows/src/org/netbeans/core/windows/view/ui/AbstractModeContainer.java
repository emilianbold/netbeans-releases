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
import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.WindowManagerImpl;
import org.netbeans.core.windows.view.ModeContainer;
import org.netbeans.core.windows.view.ModeView;
import org.netbeans.core.windows.view.dnd.TopComponentDroppable;
import org.netbeans.core.windows.view.dnd.WindowDnDManager;
import org.openide.windows.TopComponent;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;


/** 
 * Abstract helper implementation of <code>ModeContainer</code>.
 * PENDING: It provides also support for TopComponentDroppable.
 *
 * @author  Peter Zavadsky
 */
public abstract class AbstractModeContainer implements ModeContainer {
    
    /** Associated mode view. */
    protected final ModeView modeView;

    protected final TabbedHandler tabbedHandler;
    
    // PENDING
    protected final WindowDnDManager windowDnDManager;
    // XXX
    private final int kind;
    

    /** Creates a DefaultSeparateContainer. */
    public AbstractModeContainer(ModeView modeView, WindowDnDManager windowDnDManager) {
        this(modeView, windowDnDManager, Constants.MODE_KIND_VIEW);
    }
    
    public AbstractModeContainer(ModeView modeView, WindowDnDManager windowDnDManager, int kind) {
        this.modeView = modeView;
        this.windowDnDManager = windowDnDManager;
        this.tabbedHandler = new TabbedHandler(modeView, kind);
        this.kind = kind;
    }


    public ModeView getModeView() {
        return modeView;
    }
    
    /** */
    public Component getComponent() {
        return getModeComponent();
    }
    
    protected abstract Component getModeComponent();

    public void addTopComponent(TopComponent tc) {
        tabbedHandler.addTopComponent(tc, kind);
    }

    public void removeTopComponent(TopComponent tc) {
        tabbedHandler.removeTopComponent(tc);

        TopComponent selected = tabbedHandler.getSelectedTopComponent();
        updateTitle(selected == null
            ? "" : WindowManagerImpl.getInstance().getTopComponentDisplayName(selected)); // NOI18N
    }
    
    public void setSelectedTopComponent(TopComponent tc) {
        tabbedHandler.setSelectedTopComponent(tc);
        
        updateTitle(WindowManagerImpl.getInstance().getTopComponentDisplayName(tc));
    }
    
    public void setTopComponents(TopComponent[] tcs, TopComponent selected) {
        //Cheaper to do the equality test here than later
        if (!Arrays.equals(tcs, getTopComponents())) {
            tabbedHandler.setTopComponents(tcs, selected);
            updateTitle(WindowManagerImpl.getInstance().getTopComponentDisplayName(selected));
        } else {
            //[dafe] It is also used as selection modifier only, for example when
            // clearing selection to null on sliding modes
            setSelectedTopComponent(selected);
        }
    }
    
    protected abstract void updateTitle(String title);
    
    protected abstract void updateActive(boolean active);
    
    
    public TopComponent getSelectedTopComponent() {
        return tabbedHandler.getSelectedTopComponent();
    }
    
    public void setActive(boolean active) {
        updateActive(active);

        TopComponent selected = tabbedHandler.getSelectedTopComponent();
        updateTitle(selected == null
            ? "" : WindowManagerImpl.getInstance().getTopComponentDisplayName(selected)); // NOI18N
        
        tabbedHandler.setActive(active);
    }
    
    public void focusSelectedTopComponent() {
        // PENDING focus gets main window sometimes, investgate and refine (jdk1.4.1?).
        final TopComponent selectedTopComponent = tabbedHandler.getSelectedTopComponent();
        if(selectedTopComponent != null) {
            if (WindowManagerImpl.getInstance().getEditorAreaState() == Constants.EDITOR_AREA_SEPARATED) {
                selectedTopComponent.requestFocus();
                
            } else {
                selectedTopComponent.requestFocusInWindow();
            }
        }
    }

    
    public TopComponent[] getTopComponents() {
        return tabbedHandler.getTopComponents();
    }

    public void updateName(TopComponent tc) {
        TopComponent selected = getSelectedTopComponent();
        if(tc == selected) {
            updateTitle(tc == null 
                ? "" : WindowManagerImpl.getInstance().getTopComponentDisplayName(tc)); // NOI18N
        }
        
        tabbedHandler.topComponentNameChanged(tc, kind);
    }
    
    public void updateToolTip(TopComponent tc) {
        tabbedHandler.topComponentToolTipChanged(tc);
    }
    
    public void updateIcon(TopComponent tc) {
        tabbedHandler.topComponentIconChanged(tc);
    }

    // XXX
    protected int getKind() {
        return kind;
    }
    
    ////////////////////////
    // Support for TopComponentDroppable
    protected Shape getIndicationForLocation(Point location) {
        return tabbedHandler.getIndicationForLocation(location,
            windowDnDManager.getStartingTransfer(),
            windowDnDManager.getStartingPoint(),
            isAttachingPossible());
    }
    
    protected Object getConstraintForLocation(Point location) {
        return tabbedHandler.getConstraintForLocation(location, isAttachingPossible());
    }
    
    protected abstract boolean isAttachingPossible();
    
    protected ModeView getDropModeView() {
        return modeView;
    }
    
    protected Component getDropComponent() {
        return tabbedHandler.getComponent();
    }
    
    protected abstract TopComponentDroppable getModeDroppable();
    
    protected boolean canDrop(TopComponent transfer) {
        if(Constants.SWITCH_MODE_ADD_NO_RESTRICT
        || WindowManagerImpl.getInstance().isTopComponentAllowedToMoveAnywhere(transfer)) {
            return true;
        }
        
        ModeImpl mode = (ModeImpl)WindowManagerImpl.getInstance().findMode(transfer);
        int kind = mode != null ? mode.getKind() : Constants.MODE_KIND_EDITOR;

        return this.kind == kind;
    }
    // Support for TopComponentDroppable
    ////////////////////////

}

