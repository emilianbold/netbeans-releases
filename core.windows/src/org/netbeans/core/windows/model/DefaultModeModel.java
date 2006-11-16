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

package org.netbeans.core.windows.model;



import java.awt.*;
import java.util.List;

import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.SplitConstraint;
import org.openide.windows.TopComponent;


/**
 *
 * @author  Peter Zavadsky
 */
final class DefaultModeModel implements ModeModel {


    /** Programatic name of mode. */
    private final String name;

    private final Rectangle bounds = new Rectangle();

    private final Rectangle boundsSeparetedHelp = new Rectangle();

    /** State of mode: split or separate. */
    private /*final*/ int state;
    /** Kind of mode: editor or view. */
    private final int kind;
    
    /** Frame state. */
    private int frameState;

    /** Permanent property. */
    private final boolean permanent;

    /** Sub model which manages TopComponents stuff. */
    private final TopComponentSubModel topComponentSubModel;
    
    /** Context of tcx. Lazy initialization, because this will be used only by
     * sliding kind of modes */
    private TopComponentContextSubModel topComponentContextSubModel = null;

    // Locks>>
    /** */
    private final Object LOCK_STATE = new Object();
    /** */
    private final Object LOCK_BOUNDS = new Object();
    /** */
    private final Object LOCK_BOUNDS_SEPARATED_HELP = new Object();
    /** Locks frameState. */
    private final Object LOCK_FRAMESTATE = new Object();
    /** Locks top components. */
    private final Object LOCK_TOPCOMPONENTS = new Object();
    /** Locks tc contexts */
    private final Object LOCK_TC_CONTEXTS = new Object();
    
    
    public DefaultModeModel(String name, int state, int kind, boolean permanent) {
        this.name = name;
        this.state = state;
        this.kind = kind;
        this.permanent = permanent;
        this.topComponentSubModel = new TopComponentSubModel(kind);
    }

    /////////////////////////////////////
    // Mutator methods >>
    /////////////////////////////////////
    public void setState(int state) {
        synchronized(LOCK_STATE) {
            this.state = state;
        }
    }
    
    public void removeTopComponent(TopComponent tc) {
        synchronized(LOCK_TOPCOMPONENTS) {
            topComponentSubModel.removeTopComponent(tc);
        }
    }
    
    // XXX
    public void removeClosedTopComponentID(String tcID) {
        synchronized(LOCK_TOPCOMPONENTS) {
            topComponentSubModel.removeClosedTopComponentID(tcID);
        }
    }
    
    /** Adds opened TopComponent. */
    public void addOpenedTopComponent(TopComponent tc) {
        synchronized(LOCK_TOPCOMPONENTS) {
            topComponentSubModel.addOpenedTopComponent(tc);
        }
    }
    
    public void insertOpenedTopComponent(TopComponent tc, int index) {
        synchronized(LOCK_TOPCOMPONENTS) {
            topComponentSubModel.insertOpenedTopComponent(tc, index);
        }
    }
    
    public void addClosedTopComponent(TopComponent tc) {
        synchronized(LOCK_TOPCOMPONENTS) {
            topComponentSubModel.addClosedTopComponent(tc);
        }
    }
    
    public void addUnloadedTopComponent(String tcID) {
        synchronized(LOCK_TOPCOMPONENTS) {
            topComponentSubModel.addUnloadedTopComponent(tcID);
        }
    }
    
    public void setUnloadedSelectedTopComponent(String tcID) {
        synchronized(LOCK_TOPCOMPONENTS) {
            topComponentSubModel.setUnloadedSelectedTopComponent(tcID);
        }
    }
    
    public void setUnloadedPreviousSelectedTopComponent(String tcID) {
        synchronized(LOCK_TOPCOMPONENTS) {
            topComponentSubModel.setUnloadedPreviousSelectedTopComponent(tcID);
        }
    }
    
    /** Sets seleted TopComponent. */
    public void setSelectedTopComponent(TopComponent selected) {
        synchronized(LOCK_TOPCOMPONENTS) {
            topComponentSubModel.setSelectedTopComponent(selected);
        }
    }
    
    public void setPreviousSelectedTopComponent(TopComponent prevSelected) {
        synchronized(LOCK_TOPCOMPONENTS) {
            topComponentSubModel.setPreviousSelectedTopComponent(prevSelected);
        }
    }

    /** Sets frame state */
    public void setFrameState(int frameState) {
        synchronized(LOCK_FRAMESTATE) {
            this.frameState = frameState;
        }
    }

    public void setBounds(Rectangle bounds) {
        if(bounds == null) {
            return;
        }
        
        synchronized(LOCK_BOUNDS) {
            this.bounds.setBounds(bounds);
        }
    }
    
    public void setBoundsSeparatedHelp(Rectangle boundsSeparatedHelp) {
        if(bounds == null) {
            return;
        }
        
        synchronized(LOCK_BOUNDS_SEPARATED_HELP) {
            this.boundsSeparetedHelp.setBounds(boundsSeparatedHelp);
        }
    }
    /////////////////////////////////////
    // Mutator methods <<
    /////////////////////////////////////


    /////////////////////////////////////
    // Accessor methods >>
    /////////////////////////////////////
    public String getName() {
        return name;
    }
    
    public Rectangle getBounds() {
        synchronized(LOCK_BOUNDS) {
            return (Rectangle)this.bounds.clone();
        }
    }
    
    public Rectangle getBoundsSeparatedHelp() {
        synchronized(LOCK_BOUNDS_SEPARATED_HELP) {
            return (Rectangle)this.boundsSeparetedHelp.clone();
        }
    }
    
    public int getState() {
        synchronized(LOCK_STATE) {
            return this.state;
        }
    }
    
    public int getKind() {
        return this.kind;
    }
    
    /** Gets frame state. */
    public int getFrameState() {
        synchronized(LOCK_FRAMESTATE) {
            return this.frameState;
        }
    }
    
    public boolean isPermanent() {
        return this.permanent;
    }
    
    public boolean isEmpty() {
        synchronized(LOCK_TOPCOMPONENTS) {
            return topComponentSubModel.isEmpty();
        }
    }
    
    public boolean containsTopComponent(TopComponent tc) {
        synchronized(LOCK_TOPCOMPONENTS) {
            return topComponentSubModel.containsTopComponent(tc);
        }
    }

    /** Gets list of top components in this workspace. */
    public List<TopComponent> getTopComponents() {
        synchronized(LOCK_TOPCOMPONENTS) {
            return topComponentSubModel.getTopComponents();
        }
    }


    /** Gets selected TopComponent. */
    public TopComponent getSelectedTopComponent() {
        synchronized(LOCK_TOPCOMPONENTS) {
            return topComponentSubModel.getSelectedTopComponent();
        }
    }
    /** Gets the top component that was selected before switching to/from maximized mode */
    public TopComponent getPreviousSelectedTopComponent() {
        synchronized(LOCK_TOPCOMPONENTS) {
            return topComponentSubModel.getPreviousSelectedTopComponent();
        }
    }

    /** Gets list of top components. */
    public List<TopComponent> getOpenedTopComponents() {
        synchronized(LOCK_TOPCOMPONENTS) {
            return topComponentSubModel.getOpenedTopComponents();
        }
    }
    
    // XXX
    public List<String> getOpenedTopComponentsIDs() {
        synchronized(LOCK_TOPCOMPONENTS) {
            return topComponentSubModel.getOpenedTopComponentsIDs();
        }
    }
    
    public List<String> getClosedTopComponentsIDs() {
        synchronized(LOCK_TOPCOMPONENTS) {
            return topComponentSubModel.getClosedTopComponentsIDs();
        }
    }
    
    public List<String> getTopComponentsIDs() {
        synchronized(LOCK_TOPCOMPONENTS) {
            return topComponentSubModel.getTopComponentsIDs();
        }
    }
    
    public SplitConstraint[] getTopComponentPreviousConstraints(String tcID) {
        synchronized(LOCK_TC_CONTEXTS) {
            return getContextSubModel().getTopComponentPreviousConstraints(tcID);
        }
    }
    
    public ModeImpl getTopComponentPreviousMode(String tcID) {
        synchronized(LOCK_TC_CONTEXTS) {
            return getContextSubModel().getTopComponentPreviousMode(tcID);
        }
    }
    /** Gets the tab index of the top component in its previous mode */
    public int getTopComponentPreviousIndex(String tcID) {
        synchronized(LOCK_TC_CONTEXTS) {
            return getContextSubModel().getTopComponentPreviousIndex(tcID);
        }
    }
    
    public void setTopComponentPreviousConstraints(String tcID, SplitConstraint[] constraints) {
        synchronized(LOCK_TC_CONTEXTS) {
            getContextSubModel().setTopComponentPreviousConstraints(tcID, constraints);
        }
    }
    
    public void setTopComponentPreviousMode(String tcID, ModeImpl mode, int prevIndex) {
        synchronized(LOCK_TC_CONTEXTS) {
            getContextSubModel().setTopComponentPreviousMode(tcID, mode, prevIndex);
        }
    }
    
    /////////////////////////////////////
    // Accessor methods <<
    /////////////////////////////////////
    
    private TopComponentContextSubModel getContextSubModel() {
        if (topComponentContextSubModel == null) {
            topComponentContextSubModel = new TopComponentContextSubModel();
        }
        return topComponentContextSubModel;
    }
    
}

