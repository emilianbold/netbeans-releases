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

import java.util.Collection;
import java.util.Set;


/**
 *
 * @author  Peter Zavadsky
 */
interface TopComponentGroupModel {

    public String getName();

    public void open(Collection openedTopComponents, Collection openedBeforeTopComponents);
    public void close();
    public boolean isOpened();
    
    public Set getTopComponents();
    
    public Set getOpenedTopComponents();
    public Set getOpenedBeforeTopComponents();
    
    public Set getOpeningTopComponents();
    public Set getClosingTopComponents();
    
    public boolean addUnloadedTopComponent(String tcID);
    public boolean removeUnloadedTopComponent(String tcID);
    
    public boolean addOpeningTopComponent(TopComponent tc);
    public boolean removeOpeningTopComponent(TopComponent tc);
    
    public boolean addUnloadedOpeningTopComponent(String tcID);
    public boolean removeUnloadedOpeningTopComponent(String tcID);
    
    public boolean addUnloadedClosingTopComponent(String tcID);
    public boolean removeUnloadedClosingTopComponent(String tcID);
    
    // XXX
    public boolean addUnloadedOpenedTopComponent(String tcID);
    
    // XXX>>
    public Set getTopComponentsIDs();
    public Set getOpeningSetIDs();
    public Set getClosingSetIDs();
    public Set getOpenedTopComponentsIDs();
    // XXX<<
}
