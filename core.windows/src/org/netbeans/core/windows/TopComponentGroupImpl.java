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


package org.netbeans.core.windows;


import java.util.Iterator;
import java.util.Set;

import org.openide.windows.TopComponent;
import org.openide.windows.TopComponentGroup;


/**
 * Class representing group of TopComponents. Those TopComponents belong together
 * in the sense they can be opened or closed at one step.
 *
 * @author  Peter Zavadsky
 */
public class TopComponentGroupImpl implements TopComponentGroup {

    
    /** Creates a new instance of TopComponentGroup */
    public TopComponentGroupImpl(String name) {
        this(name, false);
    }
    
    public TopComponentGroupImpl(String name, boolean opened) {
        getCentral().createGroupModel(this, name, opened);
    }
    

    public void open() {
        getCentral().openGroup(this);
    }
    
    public void close() {
        getCentral().closeGroup(this);
    }
    
    public Set getTopComponents() {
        return getCentral().getGroupTopComponents(this);
    }

    
    public String getName() {
        return getCentral().getGroupName(this);
    }
    
    public boolean isOpened() {
        return getCentral().isGroupOpened(this);
    }
    
    public Set getOpeningSet() {
        return getCentral().getGroupOpeningTopComponents(this);
    }
    
    public Set getClosingSet() {
        return getCentral().getGroupClosingTopComponents(this);
    }

    public boolean addUnloadedTopComponent(String tcID) {
        return getCentral().addGroupUnloadedTopComponent(this, tcID);
    }
    
    public boolean removeUnloadedTopComponent(String tcID) {
        return getCentral().removeGroupUnloadedTopComponent(this, tcID);
    }
    
    public boolean addUnloadedOpeningTopComponent(String tcID) {
        return getCentral().addGroupUnloadedOpeningTopComponent(this, tcID);
    }
    
    public boolean removeUnloadedOpeningTopComponent(String tcID) {
        return getCentral().removeGroupUnloadedOpeningTopComponent(this, tcID);
    }
    
    public boolean addUnloadedClosingTopComponent(String tcID) {
        return getCentral().addGroupUnloadedClosingTopComponent(this, tcID);
    }
    
    public boolean removeUnloadedClosingTopComponent(String tcID) {
        return getCentral().removeGroupUnloadedClosingTopComponent(this, tcID);
    }
    
    // XXX
    /** Just for persistence management. */
    public boolean addGroupUnloadedOpenedTopComponent(String tcID) {
        return getCentral().addGroupUnloadedOpenedTopComponent(this, tcID);
    }
    
    public Set getGroupOpenedTopComponents() {
        return getCentral().getGroupOpenedTopComponents(this);
    }
    
    private Central getCentral() {
        return WindowManagerImpl.getInstance().getCentral();
    }
    
    public String toString() {
        StringBuffer buff = new StringBuffer();
        for(Iterator it = getTopComponents().iterator(); it.hasNext(); ) {
            TopComponent tc = (TopComponent)it.next();
            buff.append("\n\t" + tc.getClass().getName() + "@" + Integer.toHexString(tc.hashCode()) // NOI18N
                + "[name=" + tc.getName() // NOI18N
                + ", openFlag=" + getOpeningSet().contains(tc) // NOI18N
                + ", closeFlag=" + getClosingSet().contains(tc) + "]"); // NOI18N
        }
        
        return super.toString() + "[topComponents=[" + buff.toString() + "\n]]"; // NOI18N
    }

}
