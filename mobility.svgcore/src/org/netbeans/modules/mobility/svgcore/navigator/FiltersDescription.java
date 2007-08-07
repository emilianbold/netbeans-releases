/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt. 
  * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */   

package org.netbeans.modules.mobility.svgcore.navigator;

import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;

/**
 * @author Dafe Simonek
 */
public final class FiltersDescription {
    
    public static FiltersManager createManager (FiltersDescription descr) {
        return FiltersManager.create(descr);
    }
    
    /** List of <FilterItem> describing filters properties */
    private List<FilterItem> filters;
    
    /** Creates a new instance of FiltersDescription */
    public FiltersDescription() {
        filters = new ArrayList<FilterItem>();
    }
    
    public void addFilter (String name, String displayName, String tooltip,
            boolean isSelected, Icon selectedIcon, Icon unselectedIcon) {
        FilterItem newItem = new FilterItem(name, displayName, tooltip, 
                isSelected, selectedIcon, unselectedIcon);
        filters.add(newItem);
    }
    
    public int getFilterCount () {
        return filters.size();
    }
    
    public String getName (int index) {
        return ((FilterItem)filters.get(index)).name;
    }
    
    public String getDisplayName (int index) {
        return ((FilterItem)filters.get(index)).displayName;
    }
    
    public String getTooltip (int index) {
        return ((FilterItem)filters.get(index)).tooltip;
    }
    
    public Icon getSelectedIcon (int index) {
        return ((FilterItem)filters.get(index)).selectedIcon;
    }
    
    public Icon getUnselectedIcon (int index) {
        return ((FilterItem)filters.get(index)).unselectedIcon;
    }
    
    public boolean isSelected (int index) {
        return ((FilterItem)filters.get(index)).isSelected;
    }
    
    private static class FilterItem {
        String name;
        String displayName;
        String tooltip;
        Icon selectedIcon;
        Icon unselectedIcon;
        boolean isSelected;
        
        FilterItem (String name, String displayName, String tooltip,
                boolean isSelected, Icon selectedIcon, Icon unselectedIcon) {
            this.name = name;
            this.displayName = displayName;
            this.tooltip = tooltip;
            this.selectedIcon = selectedIcon;
            this.unselectedIcon = unselectedIcon;
            this.isSelected = isSelected;
        }
        
    }
    
}
