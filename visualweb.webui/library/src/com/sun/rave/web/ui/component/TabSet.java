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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package com.sun.rave.web.ui.component;

import java.util.List;

/**
 * Defines a Tabs component.
 *
 * @author  Sean Comerford
 */
public class TabSet extends TabSetBase {

    /** Default constructor */
    public TabSet() {
        super();
    }

    /**
     * <p>Searches the children of this TabSet for a Tab with the given
     * id. Returns the desired Tab object or null if no Tab with the given
     * id is a descendant of this TabSet.</p>
     *
     * @param tabId The id of the desired Tab component.
     * @return The Tab object or null if no Tab with the given id is a
     * descendant of this TabSet.
     */
    public Tab findChildTab(String tabId) {
        int numKids = getChildCount();
        if (numKids < 1) {
            return null;
        }

        List tabs = getChildren();
        Tab foundTab = null;
        for (int i = 0; i < numKids && foundTab == null; i++) {
            foundTab = findTab((Tab) tabs.get(i), tabId);
        }
        
        return foundTab;
    }
    
    /** Recursive helper function for findChildTab */
    private Tab findTab(Tab tab, String tabId) {
        if (tab == null || tabId == null) {
            return null;
        }
        
        if (tab.getId().equals(tabId)) {
            return tab;
        }
        
        Tab foundTab = null;
        if (tab.getChildCount() > 0 && tab.hasTabChildren()) {
            List kids = tab.getChildren();
            int numKids = kids.size();
            for (int i = 0; i < numKids && foundTab == null; i++) {
                foundTab = findTab((Tab) kids.get(i), tabId);
                if (foundTab != null) {
                    break;
                }
            }
        }
        
        return foundTab;
    }
}
