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

package org.netbeans.modules.java.navigation.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.netbeans.modules.java.navigation.base.FiltersDescription;
import org.netbeans.modules.java.navigation.base.FiltersManager;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;

/** Popup submenu consisting of boolean state filters
 *
 * @author Dafe Simonek
 */
public final class FilterSubmenuAction extends AbstractAction implements Presenter.Popup {
    
    private static final String PROP_FILTER_NAME = "nbFilterName";
    /** access to filter manager */        
    private FiltersManager filters;
    
    /** Creates a new instance of FilterSubmenuAction */
    public FilterSubmenuAction(FiltersManager filters) {
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
    
    public final JMenuItem getPopupPresenter() {
        return createSubmenu();
    }
    
    private JMenuItem createSubmenu () {
        FiltersDescription filtersDesc = filters.getDescription();
        JMenuItem menu = new JMenu(NbBundle.getMessage(FilterSubmenuAction.class, "LBL_FilterSubmenu")); //NOI18N
        JMenuItem menuItem = null;
        String filterName = null;
        for (int i = 0; i < filtersDesc.getFilterCount(); i++) {
            filterName = filtersDesc.getName(i);
            menuItem = new JCheckBoxMenuItem(
                    filtersDesc.getDisplayName(i), filters.isSelected(filterName)); 
            menuItem.addActionListener(this);
            menuItem.putClientProperty(PROP_FILTER_NAME, filterName);
            menu.add(menuItem);
        }
        return menu;
    }
    
    
}
