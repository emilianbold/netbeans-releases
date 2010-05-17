/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package com.sun.rave.web.ui.event;

import com.sun.data.provider.SortCriteria;
import com.sun.rave.web.ui.component.DropDown;
import com.sun.rave.web.ui.component.Table;
import com.sun.rave.web.ui.component.TableActions;
import com.sun.rave.web.ui.component.TableColumn;
import com.sun.rave.web.ui.component.TableHeader;
import com.sun.rave.web.ui.component.TablePanels;
import com.sun.rave.web.ui.component.TableRowGroup;
import com.sun.rave.web.ui.util.LogUtil;

import java.util.Iterator;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

/**
 * A listener for receiving sort action events. Depending on the id of the event
 * source, SortCriteria objects are either added as next level sort, set as the
 * primary sort, or all sorting currently applied is cleared.
 * <p>
 * A class that is interested in receiving such events registers itself with the
 * source TableColumn of interest, by calling addActionListener().
 * </p><p>
 * Note: To see the messages logged by this class, set the following global
 * defaults in your JDK's "jre/lib/logging.properties" file.
 * </p><p><pre>
 * java.util.logging.ConsoleHandler.level = FINE
 * com.sun.rave.web.ui.event.TableSortActionListener.level = FINE
 * </pre></p>
 */
public class TableSortActionListener implements ActionListener {
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Process methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Invoked when the action described by the specified ActionEvent occurs. 
     * The source parent is expected to be a Table or TableColumn object.
     *
     * @param event The ActionEvent that has occurred
     *
     * @exception AbortProcessingException Signal the JavaServer Faces
     * implementation that no further processing on the current event
     * should be performed.
     */
    public void processAction(ActionEvent event) 
            throws AbortProcessingException {
        UIComponent source = (event != null)
            ? (UIComponent) event.getSource() : null;
        if (source == null) {
            log("processAction", //NOI18N
                "Cannot process action, ActionEvent source is null"); //NOI18N
            return;
        }

        // If the parent is TableColumn, this is an action from a column sort
        // button. If the parent is Table, this action is either from the clear
        // sort button or the custom sort panel.
        TableColumn col = getTableColumnAncestor(source);
        if (col != null) {
            processTableColumn(col, source.getId());
        } else {
            Table table = getTableAncestor(source);
            if (table != null) {
                processTable(table, source.getId());
            } else {
                log("processAction", "Cannot process action, Table is null"); //NOI18N
            }
        }
    }

    /**
     * Helper method to process Table components.
     *
     * @param component Table component being sorted.
     * @param id The source id.
     */    
    private void processTable(Table component, String id) {
        if (component == null) {
            log("processTable", "Cannot process Table action, Table is null"); //NOI18N
            return;
        }

        // Clear sort for each TableRowGroup child. This action is the same for
        // both the clear sort button and the custom sort panel.
        if (id.equals(TableActions.CLEAR_SORT_BUTTON_ID)
                || id.equals(TablePanels.SORT_PANEL_SUBMIT_BUTTON_ID)) {
            Iterator kids = component.getTableRowGroupChildren();
            while (kids.hasNext()) {
                TableRowGroup group = (TableRowGroup) kids.next();
                group.clearSort();
            }
        }

        // Set sort for custom sort panel.
        if (id.equals(TablePanels.SORT_PANEL_SUBMIT_BUTTON_ID)) {
            customSort(component);
        }
    }

    /**
     * Helper method to process TableColumn components.
     *
     * @param component TableColumn component being sorted.
     * @param id The source id.
     */
    private void processTableColumn(TableColumn component, String id) {
        if (component == null) {
            log("processTableColumn", //NOI18N
                "Cannot process TableColumn action, TableColumn is null"); //NOI18N
            return;
        }

        // We must determine if sorting applies to all TableRowGroup components
        // or an individual component. That is, there could be a single column
        // header for all row groups or one for each group. If there is more
        // than one column header,  we will apply the sort only for the group in
        // which the TableColumn component belongs. If there is only one column
        // header, sorting applies to all groups.
        Table table = component.getTableAncestor();
        if (table != null && table.getColumnHeadersCount() > 1) {
            TableRowGroup group = component.getTableRowGroupAncestor();
            setSort(group, id, component.getSortCriteria());
        } else {
            sort(table, id, component.getSortCriteria());
        }
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Sort methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /** 
     * Set the sort for Table components. This sort is applied to 
     * all TableRowGroup chlidren.
     *
     * @param component Table component being sorted.
     * @param id The source id.
     * @param criteria SortCriteria to find column index.
     */
    private void sort(Table component, String id, SortCriteria criteria) {
        if (component == null || criteria == null) {
            log("sort", "Cannot sort, Table or SortCriteria is null"); //NOI18N
            return;
        }

        // Get the index associated with the TableColumn node to be sorted.
        int index = getNodeIndex(component, criteria.getCriteriaKey());

        // Iterate over each TableRowGroup child and set the sort.
        Iterator kids = component.getTableRowGroupChildren();
        while (kids.hasNext()) {
            // When multiple row groups are used, we need to find the unique 
            // sort value binding associated with the column for each group.
            TableRowGroup group = (TableRowGroup) kids.next();
            TableColumn col = getTableColumn(group, index);
            setSort(group, id, (col != null) ? col.getSortCriteria() : null);
        }
    }

    /** 
     * Set the sort for the given TableRowGroup component.
     *
     * @param component Table component being sorted.
     * @param id The source id.
     * @param criteria SortCriteria to find column index.
     */
    private void setSort(TableRowGroup component, String id, 
            SortCriteria criteria) {
        if (component == null) {
            log("setSort", "Cannot set sort, TableRowGroup is null"); //NOI18N
            return;
        }

        if (id.equals(TableHeader.ADD_SORT_BUTTON_ID)) {
            component.addSort(criteria);
        } else if (id.equals(TableHeader.SELECT_SORT_BUTTON_ID)
                || id.equals(TableHeader.PRIMARY_SORT_BUTTON_ID)
                || id.equals(TableHeader.PRIMARY_SORT_LINK_ID)) {
            component.clearSort();
            component.addSort(criteria);
        } else if (id.equals(TableHeader.TOGGLE_SORT_BUTTON_ID)) {
            if (criteria != null) {
                criteria.setAscending(component.isDescendingSort(criteria));
            }
            component.addSort(criteria);
        } else {
            log("setSort", "Cannot add sort, unknown component Id: " + id); //NOI18N
        }
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Custom sort methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
      
    /** 
     * Process the properties for the custom table sort panel and set the sort.
     *
     * @param component Table component being sorted.
     */
    private void customSort(Table component) {
        UIComponent panels = component.getFacet(Table.EMBEDDED_PANELS_ID);
        if (panels == null) {
            log("customSort", //NOI18N
                "Cannot custom sort, embedded panels facet is null"); //NOI18N
            return;
        }

        // Get menu children.
        Map map = panels.getFacets();
        UIComponent primarySortColumnMenu = (UIComponent) map.get(
            TablePanels.PRIMARY_SORT_COLUMN_MENU_ID);
        UIComponent primarySortOrderMenu = (UIComponent) map.get(
            TablePanels.PRIMARY_SORT_ORDER_MENU_ID);
        UIComponent secondarySortColumnMenu = (UIComponent) map.get(
            TablePanels.SECONDARY_SORT_COLUMN_MENU_ID);
        UIComponent secondarySortOrderMenu = (UIComponent) map.get(
            TablePanels.SECONDARY_SORT_ORDER_MENU_ID);
        UIComponent tertiarySortColumnMenu = (UIComponent) map.get(
            TablePanels.TERTIARY_SORT_COLUMN_MENU_ID);
        UIComponent tertiarySortOrderMenu = (UIComponent) map.get(
            TablePanels.TERTIARY_SORT_ORDER_MENU_ID);

        // Set primary sort.
        if (primarySortColumnMenu != null && primarySortOrderMenu != null
                && primarySortColumnMenu instanceof DropDown 
                && primarySortOrderMenu instanceof DropDown) {
            int index = getNodeIndex(component,
                (String) ((DropDown) primarySortColumnMenu).getSelected());
            setCustomSort(component, index, Boolean.valueOf(
                (String) ((DropDown) primarySortOrderMenu).getSelected()).booleanValue());
        } else {
            log("customSort", //NOI18N
                "Cannot set custom sort, primary sort column menu is null"); //NOI18N
        }

        // Set secondary sort.
        if (secondarySortColumnMenu != null && secondarySortOrderMenu != null
                && secondarySortColumnMenu instanceof DropDown
                && secondarySortOrderMenu instanceof DropDown) {
            int index = getNodeIndex(component,
                (String) ((DropDown) secondarySortColumnMenu).getSelected());
            setCustomSort(component, index, Boolean.valueOf(
                (String) ((DropDown) secondarySortOrderMenu).getSelected()).booleanValue());
        } else {
            log("customSort", //NOI18N
                "Cannot set custom sort, secondary sort column menu is null"); //NOI18N
        }

        // Set tertiary sort.
        if (tertiarySortColumnMenu != null && tertiarySortOrderMenu != null
                && tertiarySortColumnMenu instanceof DropDown
                && tertiarySortOrderMenu instanceof DropDown) {
            int index = getNodeIndex(component, 
                (String) ((DropDown) tertiarySortColumnMenu).getSelected());
            setCustomSort(component, index, Boolean.valueOf(
                (String) ((DropDown) tertiarySortOrderMenu).getSelected()).booleanValue());
        } else {
            log("customSort", //NOI18N
                "Cannot set custom sort, tertiary sort column menu is null"); //NOI18N
        }
    }

    /** 
     * Set the sort for the custom sort panel. This sort is applied to all
     * TableRowGroup chlidren.
     *
     * @param component Table component being sorted.
     * @param index The index associated with the TableColumn node to be sorted.
     * @param descending The sort order to be applied.
     */
    private void setCustomSort(Table component, int index, 
            boolean descending) {
        if (component == null || index < 0) {
            log("setCustomSort", //NOI18N
                "Cannot set custom sort, Table is null or index < 0"); //NOI18N
            return;
        }

        // Iterate over each TableRowGroup child and set the sort.
        Iterator kids = component.getTableRowGroupChildren();
        while (kids.hasNext()) {
            // When multiple row groups are used, we need to find the sort value 
            // binding associated with the column for each group.
            TableRowGroup group = (TableRowGroup) kids.next();
            TableColumn col = getTableColumn(group, index);

            // Get new SortCriteria to add and set sort order.
            SortCriteria criteria = (col != null) ? col.getSortCriteria() : null;
            if (criteria != null) {
                criteria.setAscending(!descending);
            }
            group.addSort(criteria);
        }
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Nested TableColumn methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Helper method to get the node index associated with the given criteria
     * key (i.e., a value binding expression string or FieldKey id).
     *
     * @param component Table for which to extract children.
     * @param criteriaKey The criteria key associated with the column sort.
     * @return The node index or -1 if TableColumn was not found.
     */
    private int getNodeIndex(Table component, String criteriaKey) {
        // If the criteria key is an empty string, "None" has been selected.
        if (component == null || criteriaKey == null 
                || criteriaKey.length() == 0) {
            log("getNodeIndex", //NOI18N
                "Cannot obtain node index, Table or sort criteria key is null"); //NOI18N
            return -1;
        }

        // Use the first TableRowGroup child to obtain column index.
        TableRowGroup group = component.getTableRowGroupChild();
        if (group != null) {
            Integer index = new Integer(-1); // Initialize index for testing.

            // Get node index for nested TableColumn children.
            Iterator kids = group.getTableColumnChildren();
            while (kids.hasNext()) {
                TableColumn col = (TableColumn) kids.next();
                index = new Integer(index.intValue() + 1); // Increment index.
                int result = getNodeIndex(col, criteriaKey, index);
                if (result > -1) {
                    return result; // Match found.
                }
            }
        } else {
            log("getNodeIndex", //NOI18N
                "Cannot obtain node index, TableRowGroup is null"); //NOI18N
        }
        return -1;
    }

    /**
     * Helper method to get the node index, associated with the given criteria
     * key (i.e., a value binding expression string or FieldKey id), for nested 
     * TableColumn components.
     *
     * @param component Table for which to extract children.
     * @param criteriaKey The criteria key associated with the column sort.
     * @param index An index associated with the current TableColumn node.
     * @return The node index or -1 if TableColumn was not found.
     */
    private int getNodeIndex(TableColumn component, String criteriaKey,
            Integer index) {
        // If the criteria key is an empty string, "None" has been selected.
        if (component == null || criteriaKey == null 
                || criteriaKey.length() == 0) {
            log("getNodeIndex", //NOI18N
                "Cannot obtain node index, TableColumn or sort criteria key is null"); //NOI18N
            return -1;
        }

        // Get node index for nested TableColumn children.
        Iterator kids = component.getTableColumnChildren();
        if (kids.hasNext()) {
            while (kids.hasNext()) {
                TableColumn col = (TableColumn) kids.next();
                index = new Integer(index.intValue() + 1); // Increment index.
                int result = getNodeIndex(col, criteriaKey, index);
                if (result > -1) {
                    return result; // Match found.
                }
            }
        }

        // Find index by matching TableColumn SortCriteria with given key.
        SortCriteria criteria = component.getSortCriteria();
        String key = (criteria != null) ? criteria.getCriteriaKey() : null;
        return (key != null && key.equals(criteriaKey)) ? index.intValue() : -1;
    }

    /**
     * Helper method to get the TableColumn associated with the given node index.
     *
     * @param component TableRowGroup for which to extract children.
     * @param index The index associated with the TableColumn node to be sorted.
     * @return The TableColumn associated with index.
     */
    private TableColumn getTableColumn(TableRowGroup component, int index) {
        if (component == null) {
            log("getTableColumn", //NOI18N
                "Cannot obtain TableColumn component, TableRowGroup is null"); //NOI18N
            return null;
        }

        // Initialize index for testing.
        index++;

        // Get node index for nested TableColumn children.
        Iterator kids = component.getTableColumnChildren();
        while (kids.hasNext()) {
            TableColumn col = (TableColumn) kids.next();
            TableColumn result = getTableColumn(col, --index);
            if (result != null) {
                return result; // Match found.
            }
        }
        return null;
    }

    /**
     * Helper method to get the TableColumn associated with the given node index.
     *
     * @param component TableColumn for which to extract children.
     * @param index The index associated with the TableColumn node to be sorted.
     * @return The TableColumn associated with index.
     */
    private TableColumn getTableColumn(TableColumn component, int index) {
        if (component == null) {
            log("getTableColumn", //NOI18N
                "Cannot obtain TableColumn component, TableColumn is null"); //NOI18N
            return null;
        }

        // Get node index for nested TableColumn children.
        Iterator kids = component.getTableColumnChildren();
        if (kids.hasNext()) {
            while (kids.hasNext()) {
                TableColumn col = (TableColumn) kids.next();
                TableColumn result = getTableColumn(col, --index);
                if (result != null) {
                    return result; // Match found.
                }
            }
        }

        // Find TableColumn by matching index.
        if (index == 0) {
            return component;
        } else {
            log("getTableColumn", //NOI18N
                "Cannot obtain TableColumn component, cannot match node index"); //NOI18N
        }
        return null;
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Child methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Get the closest Table ancestor that encloses this component.
     *
     * @component UIcomponent to retrieve ancestor.
     * @return The Table ancestor.
     */
    private Table getTableAncestor(UIComponent component) {
        while (component != null) {
            component = component.getParent();
            if (component instanceof Table) {
                return (Table) component;
            }
        }
        return null;
    }

    /**
     * Get the closest TableColumn ancestor that encloses this component.
     *
     * @component UIcomponent to retrieve ancestor.
     * @return The TableColumn ancestor.
     */
    private TableColumn getTableColumnAncestor(UIComponent component) {
        while (component != null) {
            component = component.getParent();
            if (component instanceof TableColumn) {
                return (TableColumn) component;
            }
        }
        return null;
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Misc methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Log fine messages.
     */
    private void log(String method, String message) {
        // Get class.
        Class clazz = this.getClass();
	if (LogUtil.fineEnabled(clazz)) {
            // Log method name and message.
            LogUtil.fine(clazz, clazz.getName() + "." + method + ": " + message); //NOI18N
        }
    }
}
