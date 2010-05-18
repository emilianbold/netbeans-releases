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
package com.sun.rave.web.ui.component;

import com.sun.data.provider.SortCriteria;
import com.sun.rave.web.ui.event.TableSortActionListener;
import com.sun.rave.web.ui.model.Option;
import com.sun.rave.web.ui.theme.Theme;
import com.sun.rave.web.ui.util.LogUtil;
import com.sun.rave.web.ui.util.ThemeUtilities;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.component.NamingContainer;
import javax.faces.component.UIComponent;

/**
 * Component that represents an embedded panel.
 * <p>
 * Note: Column headers and footers are rendered by TableRowGroupRenderer. Table
 * column footers are rendered by TableRenderer.
 * </p><p>
 * Note: To see the messages logged by this class, set the following global
 * defaults in your JDK's "jre/lib/logging.properties" file.
 * </p><p><pre>
 * java.util.logging.ConsoleHandler.level = FINE
 * com.sun.rave.web.ui.component.TablePanels.level = FINE
 * </pre></p>
 */
public class TablePanels extends TablePanelsBase implements NamingContainer {
    /** The facet name for the filter panel. */
    public static final String FILTER_PANEL_ID = "_filterPanel"; //NOI18N

    /** The facet name for the preferences panel. */
    public static final String PREFERENCES_PANEL_ID = "_preferencesPanel"; //NOI18N

    /** The component id for the primary sort column menu. */
    public static final String PRIMARY_SORT_COLUMN_MENU_ID = "_primarySortColumnMenu"; //NOI18N

    /** The facet name for the primary sort column menu. */
    public static final String PRIMARY_SORT_COLUMN_MENU_FACET = "primarySortColumnMenu"; //NOI18N

    /** The component id for the primary sort column menu label. */
    public static final String PRIMARY_SORT_COLUMN_MENU_LABEL_ID = "_primarySortColumnMenuLabel"; //NOI18N

    /** The facet name for the primary sort column menu label. */
    public static final String PRIMARY_SORT_COLUMN_MENU_LABEL_FACET = "primarySortColumnMenuLabel"; //NOI18N

    /** The component id for the primary sort order menu. */
    public static final String PRIMARY_SORT_ORDER_MENU_ID = "_primarySortOrderMenu"; //NOI18N

    /** The facet name for the primary sort order menu. */
    public static final String PRIMARY_SORT_ORDER_MENU_FACET = "primarySortOrderMenu"; //NOI18N

    /** The component id for the secondary sort column menu. */
    public static final String SECONDARY_SORT_COLUMN_MENU_ID = "_secondarySortColumnMenu"; //NOI18N

    /** The facet name for the secondary sort column menu. */
    public static final String SECONDARY_SORT_COLUMN_MENU_FACET = "secondarySortColumnMenu"; //NOI18N

    /** The component id for the secondary sort column menu label. */
    public static final String SECONDARY_SORT_COLUMN_MENU_LABEL_ID = "_secondarySortColumnMenuLabel"; //NOI18N

    /** The facet name for the secondary sort column menu label. */
    public static final String SECONDARY_SORT_COLUMN_MENU_LABEL_FACET = "secondarySortColumnMenuLabel"; //NOI18N

    /** The component id for the secondary sort order menu. */
    public static final String SECONDARY_SORT_ORDER_MENU_ID = "_secondarySortOrderMenu"; //NOI18N

    /** The facet name for the secondary sort order menu. */
    public static final String SECONDARY_SORT_ORDER_MENU_FACET = "secondarySortOrderMenu"; //NOI18N

    /** The facet name for the sort panel. */
    public static final String SORT_PANEL_ID = "_sortPanel"; //NOI18N

    /** The component id for the sort panel cancel button. */
    public static final String SORT_PANEL_CANCEL_BUTTON_ID = "_sortPanelCancelButton"; //NOI18N

    /** The facet name for the sort panel cancel button. */
    public static final String SORT_PANEL_CANCEL_BUTTON_FACET = "sortPanelCancelButton"; //NOI18N

    /** The component id for the sort panel submit button. */
    public static final String SORT_PANEL_SUBMIT_BUTTON_ID = "_sortPanelSubmitButton"; //NOI18N

    /** The facet name for the sort panel submit button. */
    public static final String SORT_PANEL_SUBMIT_BUTTON_FACET = "sortPanelSubmitButton"; //NOI18N

    /** The component id for the tertiary sort column menu. */
    public static final String TERTIARY_SORT_COLUMN_MENU_ID = "_tertiarySortColumnMenu"; //NOI18N

    /** The facet name for the tertiary sort column menu. */
    public static final String TERTIARY_SORT_COLUMN_MENU_FACET = "tertiarySortColumnMenu"; //NOI18N

    /** The component id for the tertiary sort column menu label. */
    public static final String TERTIARY_SORT_COLUMN_MENU_LABEL_ID = "_tertiarySortColumnMenuLabel"; //NOI18N

    /** The facet name for the tertiary sort column menu label. */
    public static final String TERTIARY_SORT_COLUMN_MENU_LABEL_FACET = "tertiarySortColumnMenuLabel"; //NOI18N

    /** The component id for the tertiary sort order menu. */
    public static final String TERTIARY_SORT_ORDER_MENU_ID = "_tertiarySortOrderMenu"; //NOI18N

    /** The facet name for the tertiary sort order menu. */
    public static final String TERTIARY_SORT_ORDER_MENU_FACET = "tertiarySortOrderMenu"; //NOI18N

    // The Table ancestor enclosing this component.
    private Table table = null;

    /** Default constructor */
    public TablePanels() {
        super();
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Child methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Get the closest Table ancestor that encloses this component.
     *
     * @return The Table ancestor.
     */
    public Table getTableAncestor() {
        if (table == null) {
            UIComponent component = this;
            while (component != null) {
                component = component.getParent();
                if (component instanceof Table) {
                    table = (Table) component;
                    break;
                }
            }
        }
        return table;
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Sort panel methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Get primary sort column menu used in the sort panel.
     *
     * @return The primary sort column menu.
     */
    public UIComponent getPrimarySortColumnMenu() {
        UIComponent facet = getFacet(PRIMARY_SORT_COLUMN_MENU_FACET);
        if (facet != null) {
            return facet;
        }

        // Get child.
        DropDown child = new DropDown();
	child.setId(PRIMARY_SORT_COLUMN_MENU_ID);
        child.setItems(getSortColumnMenuOptions());
        child.setSelected(getSelectedSortColumnMenuOption(1));

        // Set JS to initialize the sort column menu.
        Table table = getTableAncestor();
        if (table != null) {
            child.setTabIndex(table.getTabIndex());
            child.setOnChange("document.getElementById('" + //NOI18N
                table.getClientId(getFacesContext()) + "').initPrimarySortOrderMenu()"); //NOI18N
        } else {
            log("getPrimarySortColumnMenu", //NOI18N
                "Tab index & onChange not set, Table is null"); //NOI18N
        }

        // Save facet and return child.
        getFacets().put(child.getId(), child); 
        return child;
    }

    /**
     * Get primary sort column menu label used in the sort panel.
     *
     * @return The primary sort column menu label.
     */
    public UIComponent getPrimarySortColumnMenuLabel() {
        UIComponent facet = getFacet(PRIMARY_SORT_COLUMN_MENU_LABEL_FACET);
        if (facet != null) {
            return facet;
        }

        // Get child.
        Label child = new Label();
        child.setId(PRIMARY_SORT_COLUMN_MENU_LABEL_ID);
        child.setText(getTheme().getMessage("table.panel.primarySortColumn")); //NOI18N
        child.setLabelLevel(2);
        
        // Save facet and return child.
        getFacets().put(child.getId(), child); 
        return child;
    }

    /**
     * Get primary sort order menu used in the sort panel.
     *
     * @return The primary sort order menu.
     */
    public UIComponent getPrimarySortOrderMenu() {
        UIComponent facet = getFacet(PRIMARY_SORT_ORDER_MENU_FACET);
        if (facet != null) {
            return facet;
        }

        // Get child.
        DropDown child = new DropDown();
	child.setId(PRIMARY_SORT_ORDER_MENU_ID);
        child.setItems(getSortOrderMenuOptions());
        child.setSelected(getSelectedSortOrderMenuOption(1));
        
        // Set JS to initialize the sort order menu.
        Table table = getTableAncestor();
        if (table != null) {
            child.setTabIndex(table.getTabIndex());
            child.setOnChange("document.getElementById('" + //NOI18N
                table.getClientId(getFacesContext()) + 
                "').initPrimarySortOrderMenuToolTip()"); //NOI18N
        } else {
            log("getPrimarySortOrderMenu", //NOI18N
                "Tab index & onChange not set, Table is null"); //NOI18N
        }

        // Save facet and return child.
        getFacets().put(child.getId(), child); 
        return child;
    }

    /**
     * Get secondary sort column menu used in the sort panel.
     *
     * @return The secondary sort column menu.
     */
    public UIComponent getSecondarySortColumnMenu() {
        UIComponent facet = getFacet(SECONDARY_SORT_COLUMN_MENU_FACET);
        if (facet != null) {
            return facet;
        }

        // Get child.
        DropDown child = new DropDown();
	child.setId(SECONDARY_SORT_COLUMN_MENU_ID);
        child.setItems(getSortColumnMenuOptions());
        child.setSelected(getSelectedSortColumnMenuOption(2));

        // Set JS to initialize the sort column menu.
        Table table = getTableAncestor();
        if (table != null) {
            child.setTabIndex(table.getTabIndex());
            child.setOnChange("document.getElementById('" + //NOI18N
                table.getClientId(getFacesContext()) + 
                "').initSecondarySortOrderMenu()"); //NOI18N
        } else {
            log("getSecondarySortColumnMenu", //NOI18N
                "Tab index & onChange not set, Table is null"); //NOI18N
        }

        // Save facet and return child.
        getFacets().put(child.getId(), child); 
        return child;
    }

    /**
     * Get secondary sort column menu label used in the sort panel.
     *
     * @return The secondary sort column menu label.
     */
    public UIComponent getSecondarySortColumnMenuLabel() {
        UIComponent facet = getFacet(SECONDARY_SORT_COLUMN_MENU_LABEL_FACET);
        if (facet != null) {
            return facet;
        }

        // Get child.
        Label child = new Label();
        child.setId(SECONDARY_SORT_COLUMN_MENU_LABEL_ID);
        child.setText(getTheme().getMessage("table.panel.secondarySortColumn")); //NOI18N
        child.setLabelLevel(2);
        
        // Save facet and return child.
        getFacets().put(child.getId(), child); 
        return child;
    }

    /**
     * Get secondary sort order menu used in the sort panel.
     *
     * @return The secondary sort order menu.
     */
    public UIComponent getSecondarySortOrderMenu() {
        UIComponent facet = getFacet(SECONDARY_SORT_ORDER_MENU_FACET);
        if (facet != null) {
            return facet;
        }

        // Get child.
        DropDown child = new DropDown();
	child.setId(SECONDARY_SORT_ORDER_MENU_ID);
        child.setItems(getSortOrderMenuOptions());
        child.setSelected(getSelectedSortOrderMenuOption(2));

        // Set JS to initialize the sort order menu.
        Table table = getTableAncestor();
        if (table != null) {
            child.setTabIndex(table.getTabIndex());
            child.setOnChange("document.getElementById('" + //NOI18N
                table.getClientId(getFacesContext()) + 
                "').initSecondarySortOrderMenuToolTip()"); //NOI18N
        } else {
            log("getSecondarySortOrderMenu", //NOI18N
                "Tab index & onChange not set, Table is null"); //NOI18N
        }

        // Save facet and return child.
        getFacets().put(child.getId(), child); 
        return child;
    }

    /**
     * Get sort panel cancel button.
     *
     * @return The sort panel cancel button.
     */
    public UIComponent getSortPanelCancelButton() {
        UIComponent facet = getFacet(SORT_PANEL_CANCEL_BUTTON_FACET);
        if (facet != null) {
            return facet;
        }

        // Get child.
        Button child = new Button();
        child.setId(SORT_PANEL_CANCEL_BUTTON_ID);
        child.setMini(true);
        child.setText(getTheme().getMessage("table.panel.cancel")); //NOI18N
        child.setToolTip(getTheme().getMessage("table.panel.cancelChanges")); //NOI18N

        // Set JS to close the sort panel.
        Table table = getTableAncestor();
        if (table != null) {
            child.setTabIndex(table.getTabIndex());
            child.setOnClick("document.getElementById('" + //NOI18N
                table.getClientId(getFacesContext()) +
                "').toggleSortPanel(); return false"); //NOI18N
        } else {
            log("getSortPanelCancelButton", //NOI18N
                "Tab index & onClick not set, Table is null"); //NOI18N
        }

        // Save facet and return child.
        getFacets().put(child.getId(), child);
        return child;
    }

    /**
     * Get sort panel submit button.
     *
     * @return The sort panel submit button.
     */
    public UIComponent getSortPanelSubmitButton() {
        UIComponent facet = getFacet(SORT_PANEL_SUBMIT_BUTTON_FACET);
        if (facet != null) {
            return facet;
        }

        // Get child.
        Button child = new Button();
        child.setId(SORT_PANEL_SUBMIT_BUTTON_ID);
        child.setMini(true);
        child.setPrimary(true);
        child.setText(getTheme().getMessage("table.panel.submit")); //NOI18N
        child.setToolTip(getTheme().getMessage("table.panel.applyChanges")); //NOI18N
        child.addActionListener(new TableSortActionListener());

        // Set JS to validate user selections.
        Table table = getTableAncestor();
        if (table != null) {
            child.setTabIndex(table.getTabIndex());
            child.setOnClick("return document.getElementById('" + //NOI18N
                table.getClientId(getFacesContext()) + 
                "').validateSortPanel()"); //NOI18N
        } else {
            log("getSortPanelSubmitButton", //NOI18N
                "Tab index & onClick not set, Table is null"); //NOI18N
        }

        // Save facet and return child.
        getFacets().put(child.getId(), child);
        return child;
    }

    /**
     * Get tertiary sort column menu used in the sort panel.
     *
     * @return The tertiary sort column menu.
     */
    public UIComponent getTertiarySortColumnMenu() {
        UIComponent facet = getFacet(TERTIARY_SORT_COLUMN_MENU_FACET);
        if (facet != null) {
            return facet;
        }

        // Get child.
        DropDown child = new DropDown();
	child.setId(TERTIARY_SORT_COLUMN_MENU_ID);
        child.setItems(getSortColumnMenuOptions());
        child.setSelected(getSelectedSortColumnMenuOption(3));

        // Set JS to initialize the sort column menu.
        Table table = getTableAncestor();
        if (table != null) {
            child.setTabIndex(table.getTabIndex());
            child.setOnChange("document.getElementById('" + //NOI18N
                table.getClientId(getFacesContext()) + 
                "').initTertiarySortOrderMenu()"); //NOI18N
        } else {
            log("getTertiarySortColumnMenu", //NOI18N
                "Tab index & onChange not set, Table is null"); //NOI18N
        }

        // Save facet and return child.
        getFacets().put(child.getId(), child); 
        return child;
    }

    /**
     * Get tertiary sort column menu label used in the sort panel.
     *
     * @return The tertiary sort column menu label.
     */
    public UIComponent getTertiarySortColumnMenuLabel() {
        UIComponent facet = getFacet(TERTIARY_SORT_COLUMN_MENU_LABEL_FACET);
        if (facet != null) {
            return facet;
        }

        // Get child.
        Label child = new Label();
        child.setId(TERTIARY_SORT_COLUMN_MENU_LABEL_ID);
        child.setText(getTheme().getMessage("table.panel.tertiarySortColumn")); //NOI18N
        child.setLabelLevel(2);

        // Save facet and return child.
        getFacets().put(child.getId(), child); 
        return child;
    }

    /**
     * Get tertiary sort order menu used in the sort panel.
     *
     * @return The tertiary sort order menu.
     */
    public UIComponent getTertiarySortOrderMenu() {
        UIComponent facet = getFacet(TERTIARY_SORT_ORDER_MENU_FACET);
        if (facet != null) {
            return facet;
        }

        // Get child.
        DropDown child = new DropDown();
	child.setId(TERTIARY_SORT_ORDER_MENU_ID);
        child.setItems(getSortOrderMenuOptions());
        child.setSelected(getSelectedSortOrderMenuOption(3));

        // Set JS to initialize the sort order menu.
        Table table = getTableAncestor();
        if (table != null) {
            child.setTabIndex(table.getTabIndex());
            child.setOnChange("document.getElementById('" + //NOI18N
                table.getClientId(getFacesContext()) + 
                "').initTertiarySortOrderMenuToolTip()"); //NOI18N
        } else {
            log("getTertiarySortOrderMenu", //NOI18N
                "Tab index & onChange not set, Table is null"); //NOI18N
        }

        // Save facet and return child.
        getFacets().put(child.getId(), child); 
        return child;
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // UIComponent methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * If the rendered property is true, render the begining of the current
     * state of this UIComponent to the response contained in the specified
     * FacesContext.
     *
     * If a Renderer is associated with this UIComponent, the actual encoding 
     * will be delegated to Renderer.encodeBegin(FacesContext, UIComponent).
     *
     * @param context FacesContext for the current request.
     *
     * @exception IOException if an input/output error occurs while rendering.
     * @exception NullPointerException if FacesContext is null.
     */
    public void encodeBegin(FacesContext context) throws IOException {
        // Clear cached variables -- bugtraq #6300020.
        table = null;
        super.encodeBegin(context);
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Private methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /** 
     * Helper method to get selected option value used by the sort column menu
     * of the table sort panel.
     *
     * @param level The sort level.
     * @return The selected menu option value.
     */
    private String getSelectedSortColumnMenuOption(int level) {
        String result = null;
        Table table = getTableAncestor();
        TableRowGroup group = (table != null)
            ? table.getTableRowGroupChild() : null;

        // Find the column that matches the given sort level and return the
        // SortCriteria key value.
        if (group != null) {
            Iterator kids = group.getTableColumnChildren();
            while (kids.hasNext()) {
                TableColumn col = (TableColumn) kids.next();
                if (!col.isRendered()) {
                    continue;
                }
                result = getSelectedSortColumnMenuOption(col, level);
                if (result != null) {
                    break;
                }
            }
        } else {
            log("getSelectedSortColumnMenuOption", //NOI18N
                "Cannot obtain select sort column menu option, TableRowGroup is null"); //NOI18N
        }
        return result;
    }

    /**
     * Helper method to get selected option value for nested TableColumn 
     * components, used by the sort column menu of the table sort panel.
     *
     * @param level The sort level.
     * @param component The TableColumn component to render.
     * @return The selected menu option value.
     */
    private String getSelectedSortColumnMenuOption(TableColumn component,
            int level) {
        String result = null;
        if (component == null) {
            log("getSelectedSortColumnMenuOption", //NOI18N
                "Cannot obtain select sort column menu option, TableColumn is null"); //NOI18N
            return result;
        }

        Iterator kids = component.getTableColumnChildren();
        if (kids.hasNext()) {
            while (kids.hasNext()) {
                TableColumn kid = (TableColumn) kids.next();
                result = getSelectedSortColumnMenuOption(kid, level);
                if (result != null) {
                    return result;
                }
            }
        }

        Table table = getTableAncestor();
        TableRowGroup group = (table != null)
            ? table.getTableRowGroupChild() : null;

        // Get SortCriteria.
        if (group != null) {
            SortCriteria criteria = component.getSortCriteria();
            if (criteria != null) {
                // Get initial selected option value.
                int sortLevel = group.getSortLevel(criteria);
                if (sortLevel == level) {
                    result = criteria.getCriteriaKey();
                }
            }
        } else {
            log("getSelectedSortColumnMenuOption", //NOI18N
                "Cannot obtain select sort column menu option, TableRowGroup is null"); //NOI18N
        }
        return result;
    }

    /** 
     * Helper method to get selected option value used by the sort order menu of
     * the table sort panel.
     *
     * @param level The sort level.
     * @return The selected menu option value.
     */
    private String getSelectedSortOrderMenuOption(int level) {
        String result = null;
        Table table = getTableAncestor();
        TableRowGroup group = (table != null)
            ? table.getTableRowGroupChild() : null;

        // Find the column that matches the given sort level and return the
        // sort order.
        if (group != null) {
            Iterator kids = group.getTableColumnChildren();
            while (kids.hasNext()) {
                TableColumn col = (TableColumn) kids.next();
                if (!col.isRendered()) {
                    continue;
                }
                result = getSelectedSortOrderMenuOption(col, level);
                if (result != null) {
                    break;
                }
            }
        } else {
            log("getSelectedSortOrderMenuOption", //NOI18N
                "Cannot obtain select sort order menu option, TableRowGroup is null"); //NOI18N
        }
        return result;
    }

    /** 
     * Helper method to get selected option value for nested TableColumn 
     * components, used by the sort order menu of the table sort panel.
     *
     * @param level The sort level.
     * @param component The TableColumn component to render.
     * @return The selected menu option value.
     */
    private String getSelectedSortOrderMenuOption(TableColumn component,
            int level) {
        String result = null;
        if (component == null) {
            log("getSelectedSortOrderMenuOption", //NOI18N
                "Cannot obtain select sort column order option, TableColumn is null"); //NOI18N
            return result;            
        }

        Iterator kids = component.getTableColumnChildren();
        if (kids.hasNext()) {
            while (kids.hasNext()) {
                TableColumn kid = (TableColumn) kids.next();
                result = getSelectedSortColumnMenuOption(kid, level);
                if (result != null) {
                    return result;
                }
            }
        }

        Table table = getTableAncestor();
        TableRowGroup group = (table != null)
            ? table.getTableRowGroupChild() : null;

        // Get SortCriteria.
        if (group != null) {
            SortCriteria criteria = component.getSortCriteria();
            if (criteria != null) {
                // Get initial selected option value.
                int sortLevel = group.getSortLevel(criteria);
                if (sortLevel == level) {
                    result = Boolean.toString(group.isDescendingSort(criteria));
                }
            }
        } else {
            log("getSelectedSortOrderMenuOption", //NOI18N
                "Cannot obtain select sort order menu option, TableRowGroup is null"); //NOI18N
        }
        return result;
    }

    /**
     * Helper method to get options used by the sort column menu of the table
     * sort panel.
     *
     * @return An array of menu options.
     */
    private Option[] getSortColumnMenuOptions() {
        ArrayList list = new ArrayList();
        Table table = getTableAncestor();
        TableRowGroup group = (table != null)
            ? table.getTableRowGroupChild() : null;

        // Add default "None" option -- an empty string represents no sort.
        list.add(new Option("", getTheme().getMessage("table.panel.none"))); //NOI18N

        // For each sortable TableColumn, use the header text for each label and
        // the SortCriteria key as the value.
        if (group != null) {
            Iterator kids = group.getTableColumnChildren();
            while (kids.hasNext()) {
                TableColumn col = (TableColumn) kids.next();
                if (!col.isRendered()) {
                    continue;
                }
                // Get header text and sort value binding expression string.
                initSortColumnMenuOptions(col, list);
            }
        } else {
            log("getSortColumnMenuOptions", //NOI18N
                "Cannot obtain sort column menu options, TableRowGroup is null"); //NOI18N
        }
        // Set options.
        Option[] options = new Option[list.size()];
        return (Option[]) list.toArray(options);
    }

    /** 
     * Helper method to get options for the sort order menu used in the table
     * sort panel.
     *
     * @return An array of menu options.
     */
    private Option[] getSortOrderMenuOptions() {
        ArrayList results = new ArrayList();

        // Add default option.
        results.add(new Option("false", getTheme().getMessage( //NOI18N
            "table.sort.augment.undeterminedAscending"))); //NOI18N
        results.add(new Option("true", getTheme().getMessage( //NOI18N
            "table.sort.augment.undeterminedDescending"))); //NOI18N

        // Set default options. Other options will be added client-side when
        // menu is initialized.
        Option[] options = new Option[results.size()];
        return (Option[]) results.toArray(options);
    }

    /**
     * Helper method to get Theme objects.
     *
     * @return The current theme.
     */
    private Theme getTheme() {
	return ThemeUtilities.getTheme(getFacesContext());
    }

    /**
     * Helper method to get options for nested TableColumn components, used by 
     * the sort column menu of the table sort panel.
     *
     * @param component The TableColumn component to render.
     * @param list The array used to store menu options.
     * @return An array of menu options.
     */
    private void initSortColumnMenuOptions(TableColumn component, List list) {
        if (component == null) {
            return;
        }

        Iterator kids = component.getTableColumnChildren();
        if (kids.hasNext()) {
            while (kids.hasNext()) {
                TableColumn kid = (TableColumn) kids.next();
                initSortColumnMenuOptions(kid, list);
            }
        }

        // Get header text and sort value binding expression string.
        SortCriteria criteria = component.getSortCriteria(); //NOI18N
        if (criteria == null) {
            log("initSortColumnMenuOptions", //NOI18N
                "Cannot initialize sort column menu options, SortCriteria is null"); //NOI18N
            return;
        }

        // Get label.
        String label = (component.getSelectId() != null)
            ? getTheme().getMessage("table.select.selectedItems") //NOI18N
            : component.getHeaderText();

        // Add option.
        list.add(new Option(criteria.getCriteriaKey(), 
            (label != null) ? label : "")); //NOI18N
    }

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
