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


import org.openide.windows.TopComponent;

import java.awt.*;
import java.util.List;
import java.util.Map;

import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.SplitConstraint;
import org.openide.windows.TopComponent;


/**
 *
 * @author  Peter Zavadsky
 */
interface ModeModel {

    // Mutators
    /** Sets state. */
    public void setState(int state);
    /** Sets bounds. */
    public void setBounds(Rectangle bounds);
    /** */
    public void setBoundsSeparatedHelp(Rectangle bounds);
    /** Sets frame state. */
    public void setFrameState(int frameState);
    /** Sets selected TopComponent. */
    public void setSelectedTopComponent(TopComponent selected);
    /** Set top component that was selected before switching to/from maximized mode */
    public void setPreviousSelectedTopComponent(TopComponent prevSelected);
    /** Adds opened TopComponent. */
    public void addOpenedTopComponent(TopComponent tc);
    /** Inserts opened TopComponent. */
    public void insertOpenedTopComponent(TopComponent tc, int index);
    /** Adds closed TopComponent. */
    public void addClosedTopComponent(TopComponent tc);
    // XXX
    public void addUnloadedTopComponent(String tcID);
    // XXX
    public void setUnloadedSelectedTopComponent(String tcID);
    /** Set top component that was selected before switching to/from maximized mode */
    public void setUnloadedPreviousSelectedTopComponent(String tcID);
    /** Removes TopComponent from mode. */
    public void removeTopComponent(TopComponent tc);
    // XXX
    public void removeClosedTopComponentID(String tcID);
    
    // Info about previous top component context, used by sliding kind of modes
    
    /** Sets information of previous mode top component was in. */
    public void setTopComponentPreviousMode(String tcID, ModeImpl mode, int prevIndex);
    /** Sets information of previous constraints of mode top component was in. */
    public void setTopComponentPreviousConstraints(String tcID, SplitConstraint[] constraints);

    // Accessors
    /** Gets programatic name of mode. */
    public String getName();
    /** Gets bounds. */
    public Rectangle getBounds();
    /** */
    public Rectangle getBoundsSeparatedHelp();
    /** Gets state. */
    public int getState();
    /** Gets kind. */
    public int getKind();
    /** Gets frame state. */
    public int getFrameState();
    /** Gets whether it is permanent. */
    public boolean isPermanent();
    /** */
    public boolean isEmpty();
    /** */
    public boolean containsTopComponent(TopComponent tc);
    /** Gets selected TopComponent. */
    public TopComponent getSelectedTopComponent();
    /** Gets the top component that was selected before switching to/from maximized mode */
    public TopComponent getPreviousSelectedTopComponent();
    /** Gets list of top components in mode. */
    public List<TopComponent> getTopComponents();
    /** Gets list of top components in mode. */
    public List<TopComponent> getOpenedTopComponents();
    // XXX
    public List<String> getOpenedTopComponentsIDs();
    public List<String> getClosedTopComponentsIDs();
    public List<String> getTopComponentsIDs();    
    
    // Info about previous top component context, used by sliding kind of modes
    
    public ModeImpl getTopComponentPreviousMode(String tcID);
    /** Gets the tab index of the top component in its previous mode */
    public int getTopComponentPreviousIndex(String tcID);
    
    public SplitConstraint[] getTopComponentPreviousConstraints(String tcID);
    
}

