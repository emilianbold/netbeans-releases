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

package org.netbeans.core.windows.model;


import org.openide.windows.TopComponent;

import java.awt.*;
import java.util.List;


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
    /** Sets seleted TopComponent. */
    public void setSelectedTopComponent(TopComponent selected);
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
    /** Removes TopComponent from mode. */
    public void removeTopComponent(TopComponent tc);
    // XXX
    public void removeClosedTopComponentID(String tcID);



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
    /** Gets list of top components in mode. */
    public List getTopComponents();
    /** Gets list of top components in mode. */
    public List getOpenedTopComponents();
    // XXX
    public List getOpenedTopComponentsIDs();
    public List getClosedTopComponentsIDs();
    public List getTopComponentsIDs();    
    
}

