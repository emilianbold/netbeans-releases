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

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;

/** Popup submenu consisting of boolean state filters
 *
 * @author Dafe Simonek
 */
public final class FilterActions extends AbstractAction {
    
    private static final String PROP_FILTER_NAME = "nbFilterName"; //NOI18N
    /** access to filter manager */        
    private FiltersManager filters;
    
    /** Creates a new instance of FilterSubmenuAction */
    public FilterActions(FiltersManager filters) {
        this.filters = filters;
    }
    
    public void actionPerformed(ActionEvent ev) {
        Object source = ev.getSource();
        // react just on submenu items, not on submenu click itself
        if (source instanceof JCheckBoxMenuItem) {
            JCheckBoxMenuItem menuItem = (JCheckBoxMenuItem)source;
            String filterName = (String)(menuItem.getClientProperty(PROP_FILTER_NAME));
            filters.setSelected(filterName, menuItem.isSelected());
        }
    }
    
    public JMenuItem[] createMenuItems () {
        FiltersDescription filtersDesc = filters.getDescription();
        List<JMenuItem> menuItems = new ArrayList<JMenuItem>();
        for (int i = 0; i < filtersDesc.getFilterCount(); i++) {
            String filterName = filtersDesc.getName(i);
            JMenuItem menuItem = new JCheckBoxMenuItem(
                    filtersDesc.getDisplayName(i), filters.isSelected(filterName)); 
            menuItem.addActionListener(this);
            menuItem.putClientProperty(PROP_FILTER_NAME, filterName);
            menuItems.add(menuItem);
        }
        return (JMenuItem[])menuItems.toArray(new JMenuItem[]{});
    }
}
