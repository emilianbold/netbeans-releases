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
import com.sun.data.provider.RowKey;
import com.sun.rave.web.ui.component.Hyperlink;
import com.sun.rave.web.ui.component.IconHyperlink;
import com.sun.rave.web.ui.event.TableSortActionListener;
import com.sun.rave.web.ui.theme.Theme;
import com.sun.rave.web.ui.theme.ThemeImages;
import com.sun.rave.web.ui.theme.ThemeStyles;
import com.sun.rave.web.ui.util.LogUtil;
import com.sun.rave.web.ui.util.RenderingUtilities;
import com.sun.rave.web.ui.util.ThemeUtilities;

import java.io.IOException;

import javax.faces.component.NamingContainer;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

/**
 * Component that represents various table headers, including sortable,
 * selection, and group headers.
 * <p>
 * Note: Column headers and footers are rendered by TableRowGroupRenderer. Table
 * column footers are rendered by TableRenderer.
 * </p><p>
 * Note: To see the messages logged by this class, set the following global
 * defaults in your JDK's "jre/lib/logging.properties" file.
 * </p><p><pre>
 * java.util.logging.ConsoleHandler.level = FINE
 * com.sun.rave.web.ui.component.TableHeader.level = FINE
 * </pre></p>
 */
public class TableHeader extends TableHeaderBase implements NamingContainer {
    /** The component id for the add sort button. */
    public static final String ADD_SORT_BUTTON_ID = "_addSortButton"; //NOI18N

    /** The facet name for the add sort button. */
    public static final String ADD_SORT_BUTTON_FACET = "addSortButton"; //NOI18N

    /** The component id for the collapsed hidden field. */
    public static final String COLLAPSED_HIDDEN_FIELD_ID = "_collapsedHiddenField"; //NOI18N

    /** The facet name for the collapsed hidden field. */
    public static final String COLLAPSED_HIDDEN_FIELD_FACET = "collapsedHiddenField"; //NOI18N

    /** The component id for the table row group toggle button. */
    public static final String GROUP_PANEL_TOGGLE_BUTTON_ID = "_groupPanelToggleButton"; //NOI18N

    /** The facet name for the table row group toggle button. */
    public static final String GROUP_PANEL_TOGGLE_BUTTON_FACET = "groupPanelToggleButton"; //NOI18N

    /** The component id for the primary sort button. */
    public static final String PRIMARY_SORT_BUTTON_ID = "_primarySortButton"; //NOI18N

    /** The facet name for the primary sort button. */
    public static final String PRIMARY_SORT_BUTTON_FACET = "primarySortButton"; //NOI18N

    /** The component id for the primary sort link. */
    public static final String PRIMARY_SORT_LINK_ID = "_primarySortLink"; //NOI18N

    /** The facet name for the primary sort link. */
    public static final String PRIMARY_SORT_LINK_FACET = "primarySortLink"; //NOI18N

    /** The component id for the select multiple toggle button. */
    public static final String SELECT_MULTIPLE_TOGGLE_BUTTON_ID = "_selectMultipleToggleButton"; //NOI18N

    /** The facet name for the select multiple toggle button. */
    public static final String SELECT_MULTIPLE_TOGGLE_BUTTON_FACET = "selectMultipleToggleButton"; //NOI18N

    /** The component id for the selection column sort button. */
    public static final String SELECT_SORT_BUTTON_ID = "_selectSortButton"; //NOI18N

    /** The facet name for the selection column sort button. */
    public static final String SELECT_SORT_BUTTON_FACET = "selectSortButton"; //NOI18N

    /** The component id for the sort level text. */
    public static final String SORT_LEVEL_TEXT_ID = "_sortLevelText"; //NOI18N

    /** The facet name for the sort level text. */
    public static final String SORT_LEVEL_TEXT_FACET = "sortLevelText"; //NOI18N

    /** The component id for the toggle sort button. */
    public static final String TOGGLE_SORT_BUTTON_ID = "_toggleSortButton"; //NOI18N

    /** The facet name for the toggle sort button. */
    public static final String TOGGLE_SORT_BUTTON_FACET = "toggleSortButton"; //NOI18N

    /** The component id for the warning icon. */
    public static final String WARNING_ICON_ID = "_warningIcon"; //NOI18N

    /** The facet name for the warning icon. */
    public static final String WARNING_ICON_FACET = "warningIcon"; //NOI18N

    // The Table ancestor enclosing this component.
    private Table table = null;

    // The TableColumn ancestor enclosing this component.
    private TableColumn tableColumn = null;

    // The TableRowGroup ancestor enclosing this component.
    private TableRowGroup tableRowGroup = null;

    // Flag indicating that the next sort order is descending.
    private boolean descending = false;
    private boolean descending_set = false;

    // The total number of selected rows.
    private int selectedRowsCount = -1;

    // The total number of sorts applied.
    private int sortCount = -1;

    // Sort level for this component.
    private int sortLevel = -1;

    /** Default constructor */
    public TableHeader() {
        super();
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Child methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Helper method to get the total number of sorts applied.
     *
     * @return The sort count.
     */
    public int getSortCount() {
        if (sortCount == -1) {
            TableRowGroup group = getTableRowGroupAncestor();
            sortCount = (group != null) ? group.getSortCount() : 0;
        }
        return sortCount;
    }

    /**
     * Helper method to get sort level for this component.
     *
     * @return The sort level or 0 if sort does not apply.
     */
    public int getSortLevel() {
        if (sortLevel == -1) {
            TableColumn col = getTableColumnAncestor();
            TableRowGroup group = getTableRowGroupAncestor();
            if (col != null && group != null) {
                sortLevel = group.getSortLevel(col.getSortCriteria());
            } else {
                log("getSortLevel", //NOI18N
                    "Cannot obtain sort level, TableColumn or TableRowGroup is null"); //NOI18N
            }
        }
        return sortLevel;
    }

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

    /**
     * Get the closest TableColumn ancestor that encloses this component.
     *
     * @return The TableColumn ancestor.
     */
    public TableColumn getTableColumnAncestor() {
        if (tableColumn == null) {
            UIComponent component = this;
            while (component != null) {
                component = component.getParent();
                if (component instanceof TableColumn) {
                    tableColumn = (TableColumn) component;
                    break;
                }
            }
        }
        return tableColumn;
    }

    /**
     * Get the closest TableRowGroup ancestor that encloses this component.
     *
     * @return The TableRowGroup ancestor.
     */
    public TableRowGroup getTableRowGroupAncestor() {
        if (tableRowGroup == null) {
            UIComponent component = this;
            while (component != null) {
                component = component.getParent();
                if (component instanceof TableRowGroup) {
                    tableRowGroup = (TableRowGroup) component;
                    break;
                }
            }
        }
        return tableRowGroup;
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Group methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     
    /**
     * Get select multiple toggle button.
     *
     * @return The select multiple toggle button.
     */
    public UIComponent getCollapsedHiddenField() {
        UIComponent facet = getFacet(COLLAPSED_HIDDEN_FIELD_FACET);
        if (facet != null) {
            return facet;
        }

        // Get child.
        HiddenField child = new HiddenField();
	child.setId(COLLAPSED_HIDDEN_FIELD_ID);

        // Set value.
        TableRowGroup group = getTableRowGroupAncestor();
        if (group != null) {
            child.setValue(new Boolean(group.isCollapsed()));
        } else {
            log("getCollapsedHiddenField", //NOI18N
                "Cannot set collapsed hidden field value, TableRowGroup is null"); //NOI18N
        }

        // Save facet and return child.
        getFacets().put(child.getId(), child);
        return child;
    }

    /**
     * Get group panel toggle button.
     *
     * @return The group panel toggle button.
     */
    public UIComponent getGroupPanelToggleButton() {
        UIComponent facet = getFacet(GROUP_PANEL_TOGGLE_BUTTON_FACET);
        if (facet != null) {
            return facet;
        }

        Theme theme = getTheme();
        Table table = getTableAncestor();
        TableRowGroup group = getTableRowGroupAncestor();

        // Get child.        
        IconHyperlink child = new IconHyperlink();
	child.setId(GROUP_PANEL_TOGGLE_BUTTON_ID);
        child.setIcon((group != null && group.isCollapsed())
            ? ThemeImages.TABLE_GROUP_PANEL
            : ThemeImages.TABLE_GROUP_PANEL_FLIP);
        child.setBorder(0);

        // Set JS to display table preferences panel.        
        StringBuffer buff = new StringBuffer(128);
        if (table != null && group != null) {
            buff.append("document.getElementById('") //NOI18N
                .append(table.getClientId(getFacesContext()))
                .append("').toggleGroupPanel('") //NOI18N
                .append(group.getClientId(getFacesContext()))
                .append("'); return false"); //NOI18N
            child.setOnClick(buff.toString());
        } else {
            log("getGroupPanelToggleButton", //NOI18N
                "onClick not set, Table or TableRowGroup is null"); //NOI18N
        }

        // Set tool tip.
        String toolTip = (group != null && group.isCollapsed())
            ? theme.getMessage("table.group.expand") //NOI18N
            : theme.getMessage("table.group.collapse"); //NOI18N
        child.setAlt(toolTip);
        child.setToolTip(toolTip);

        // Set tab index.
        if (table != null) {
            child.setTabIndex(table.getTabIndex());
        } else {
            log("getGroupPanelToggleButton", "Tab index not set, Table is null"); //NOI18N
        }

        // Save facet and return child.
        getFacets().put(child.getId(), child);
        return child;
    }

    /**
     * Get select multiple toggle button.
     *
     * @return The select multiple toggle button.
     */
    public UIComponent getSelectMultipleToggleButton() {
        UIComponent facet = getFacet(SELECT_MULTIPLE_TOGGLE_BUTTON_FACET);
        if (facet != null) {
            return facet;
        }

        Table table = getTableAncestor();
        TableRowGroup group = getTableRowGroupAncestor();

        // Get child.
        Checkbox child = new Checkbox();
	child.setId(SELECT_MULTIPLE_TOGGLE_BUTTON_ID);
        child.setSelectedValue(Boolean.TRUE);

        // Set JS to display table preferences panel.
        StringBuffer buff = new StringBuffer(128);
        if (table != null && group != null) {
            buff.append("document.getElementById('") //NOI18N
                .append(table.getClientId(getFacesContext()))
                .append("').selectGroupRows('") //NOI18N
                .append(group.getClientId(getFacesContext()))
                .append("', this.checked)"); //NOI18N
            child.setOnClick(buff.toString());
        } else {
            log("getSelectMultipleToggleButton", //NOI18N
                "onClick not set, Table or TableRowgroup is null"); //NOI18N
        }

        // Set selected property.
        if (group != null) {
            // Checkbox is checked only if all rendered rows are selected.
            RowKey[] rowKeys = group.getRenderedRowKeys();
            if (rowKeys != null && rowKeys.length > 0 
                    && rowKeys.length == getSelectedRowsCount()) {
                child.setSelected(Boolean.TRUE);
                child.setToolTip(getTheme().getMessage(
                    "table.group.deselectMultiple")); //NOI18N
            } else {
                child.setToolTip(getTheme().getMessage(
                    "table.group.selectMultiple")); //NOI18N
            }
        } else {
            log("getSelectMultipleToggleButton", //NOI18N
                "Tool tip & selected not set, TableRowGroup is null"); //NOI18N
        }

        // Set tab index.
        if (table != null) {
            child.setTabIndex(table.getTabIndex());
        } else {
            log("getSelectMultipleToggleButton", //NOI18N
                "Tab index not set, Table is null"); //NOI18N
        }

        // Save facet and return child.
        getFacets().put(child.getId(), child);
        return child;
    }

    /**
     * Get warning icon.
     *
     * @return The warning icon.
     */
    public UIComponent getWarningIcon() {
        UIComponent facet = getFacet(WARNING_ICON_FACET);
        if (facet != null) {
            return facet;
        }

        Theme theme = getTheme();
        TableRowGroup group = getTableRowGroupAncestor();

        // Get child.
        Icon child = theme.getIcon(ThemeImages.ALERT_WARNING_SMALL);

        // Warning icon is only rendered if at least one row is selected and the
        // select multiple toggle is not checked.
        RowKey[] rowKeys = group.getRenderedRowKeys();
        int rows = getSelectedRowsCount();
        if (group != null && !group.isCollapsed() || rows == 0
                || rowKeys != null && rowKeys.length > 0 
                && rowKeys.length == rows) {
            // Replace default icon with place holder.
            Icon placeHolder = theme.getIcon(ThemeImages.DOT);
            placeHolder.setHeight(child.getHeight());
            placeHolder.setWidth(child.getWidth());
            child = placeHolder;
        } else {
            log("getWarningIcon", //NOI18N
                "Height & width not set, TableRowGroup is null"); //NOI18N
        }
        child.setId(WARNING_ICON_ID);
        child.setBorder(0);

        // Set tool tip.
        String toolTip = (group != null && group.isCollapsed())
            ? theme.getMessage("table.group.warning") : null; //NOI18N        
        child.setAlt(toolTip);
        child.setToolTip(toolTip);

        // Save facet and return child.
        getFacets().put(child.getId(), child); 
        return child;
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Sort methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Get add sort button.
     *
     * @return The add sort button.
     */
    public UIComponent getAddSortButton() {
        UIComponent facet = getFacet(ADD_SORT_BUTTON_FACET);
        if (facet != null) {
            return facet;
        }

        Theme theme = getTheme();
        Table table = getTableAncestor();
        TableColumn col = getTableColumnAncestor();

        // Get child.
        IconHyperlink child = new IconHyperlink();
	child.setId(ADD_SORT_BUTTON_ID);
        child.setIcon(ThemeImages.TABLE_SORT_ADD);
        child.setBorder(0);
        child.setAlign("top"); //NOI18N
        child.setStyleClass(ThemeStyles.TABLE_HEADER_LINK_IMG);
        child.addActionListener(new TableSortActionListener());

        // Set tool tip.       
        String toolTip = getTheme().getMessage("table.sort.button.add", //NOI18N
            new String[] {getNextSortToolTipAugment()});
        child.setToolTip(toolTip);

        // Set alt.
        if (isSelectHeader()) {
            child.setAlt(theme.getMessage("table.sort.alt.add", //NOI18N
                new String[] {theme.getMessage("table.select.selectionColumn")})); //NOI18N
        } else {
            String header = (col != null && col.getHeaderText() != null)
                ? col.getHeaderText() : ""; //NOI18N
            // Select column does not have header text.
            child.setAlt(theme.getMessage("table.sort.alt.add", //NOI18N
                new String[] {header}));
        }

        // Set tab index.
        if (table != null) {
            child.setTabIndex(table.getTabIndex());
        } else {
            log("getAddSortButton", "Tab index not set, Table is null"); //NOI18N
        }

        // Add sort level text child.
        if (getSortCount() > 0 && getSortLevel() > 0) {
            // Span must appear within hyperlink for style to render properly.
            child.getChildren().add(getSortLevelText());
        }

        // Save facet and return child.
        getFacets().put(child.getId(), child);

        // Set focus when sort buttons are displayed -- bugtraq #6316565.
        setSortFocus(child);
        return child;
    }

    /**
     * Get primary sort button.
     *
     * @return The primary sort button.
     */
    public UIComponent getPrimarySortButton() {
        UIComponent facet = getFacet(PRIMARY_SORT_BUTTON_FACET);
        if (facet != null) {
            return facet;
        }

        Theme theme = getTheme();
        Table table = getTableAncestor();
        TableColumn col = getTableColumnAncestor();

        // Get child.
        IconHyperlink child = new IconHyperlink();
	child.setId(PRIMARY_SORT_BUTTON_ID);
        child.setIcon(ThemeImages.TABLE_SORT_PRIMARY);
        child.setBorder(0);
        child.setAlign("top"); //NOI18N
        child.setStyleClass(ThemeStyles.TABLE_HEADER_LINK_IMG);
        child.addActionListener(new TableSortActionListener());

        // Set tool tip.        
        String toolTip = theme.getMessage("table.sort.button.primary", //NOI18N
            new String[] {getNextSortToolTipAugment()});
        child.setToolTip(toolTip);

        // Set alt.        
        if (col != null) {
            String header = (col.getHeaderText() != null)
                ? col.getHeaderText() : ""; //NOI18N
            child.setAlt(theme.getMessage("table.sort.alt.primary", //NOI18N
                new String[] {header}));
        } else {
            log("getPrimarySortButton", "Alt text not set, TableColumn is null"); //NOI18N
        }

        // Set tab index.
        if (table != null) {
            child.setTabIndex(table.getTabIndex());
        } else {
            log("getPrimarySortButton", "Tab index not set, Table is null"); //NOI18N
        }

        // Save facet and return child.
        getFacets().put(child.getId(), child);

        // Set focus when sort buttons are displayed -- bugtraq #6316565.
        setSortFocus(child);
        return child;
    }

    /**
     * Get primary sort link.
     *
     * @return The primary sort link.
     */
    public UIComponent getPrimarySortLink() {        
        UIComponent facet = getFacet(PRIMARY_SORT_LINK_FACET);
        if (facet != null) {
            return facet;
        }

        Table table = getTableAncestor();
        TableColumn col = getTableColumnAncestor();

        // Get child.
        IconHyperlink child = new IconHyperlink();
	child.setId(PRIMARY_SORT_LINK_ID);
        child.setStyleClass(ThemeStyles.TABLE_HEADER_LINK);
        child.addActionListener(new TableSortActionListener());
        
        // Get tool tip.
        String toolTip = "table.sort.link.other"; //NOI18N
        if (getSortLevel() == 1 && getSortCount() == 1) {
            // Primary sort column, only sort applied.
            toolTip = "table.sort.link.primary"; //NOI18N
        } else if (getSortCount() == 0) {
            // No sorts applied.
            toolTip = "table.sort.link.none"; //NOI18N
        }

        // Set column properties.
        if (col != null) {
            child.setIcon(col.getSortIcon());
            child.setText(col.getHeaderText());
            child.setImageURL(col.getSortImageURL());            
            child.setToolTip(getTheme().getMessage(toolTip,
                new String[] {col.getSortToolTipAugment(col.isDescending())}));
        } else {
            log("getPrimarySortLink", //NOI18N
                "Tool tip, icon, text, & image URL not set, TableColumn is null"); //NOI18N
        }

        // Set tab index.
        if (table != null) {
            child.setTabIndex(table.getTabIndex());
        } else {
            log("getPrimarySortLink", "Tab index not set, Table is null"); //NOI18N
        }

        // Save facet and return child.
        getFacets().put(child.getId(), child);
        return child;
    }

    /**
     * Get select sort button.
     *
     * @return The title sort button.
     */
    public UIComponent getSelectSortButton() {
        UIComponent facet = getFacet(SELECT_SORT_BUTTON_FACET);
        if (facet != null) {
            return facet;
        }

        Theme theme = getTheme();
        Table table = getTableAncestor();

        // Get child.
        IconHyperlink child = new IconHyperlink();
	child.setId(SELECT_SORT_BUTTON_ID);
        child.setIcon(ThemeImages.TABLE_SORT_SELECT);
        child.setBorder(0);
        child.setAlign("top"); //NOI18N
        child.setStyleClass(ThemeStyles.TABLE_HEADER_LINK);
        child.addActionListener(new TableSortActionListener());

        // Set tool tip.
        String toolTip = theme.getMessage("table.sort.button.primary", //NOI18N
            new String[] {getNextSortToolTipAugment()});
        child.setToolTip(toolTip);

        // Set alt.
        String alt = theme.getMessage("table.sort.alt.primary", //NOI18N
            new String[] {theme.getMessage("table.select.selectionColumn")}); //NOI18N
        child.setAlt(alt);

        // Set tab index.
        if (table != null) {
            child.setTabIndex(table.getTabIndex());
        } else {
            log("getSelectSortButton", "Tab index not set, Table is null"); //NOI18N
        }

        // Save facet and return child.
        getFacets().put(child.getId(), child);
        return child;
    }

    /**
     * Get sort level static text.
     *
     * @return The sort level static text.
     */
    public UIComponent getSortLevelText() {
        UIComponent facet = getFacet(SORT_LEVEL_TEXT_FACET);
        if (facet != null) {
            return facet;
        }
       
        // Get child.
        StaticText child = new StaticText();
        child.setId(SORT_LEVEL_TEXT_ID);
        child.setText(Integer.toString(getSortLevel()));
        child.setStyleClass(getTheme().getStyleClass(
            ThemeStyles.TABLE_HEADER_SORTNUM));

        // Save facet and return child.
        getFacets().put(child.getId(), child);
        return child;
    }

    /**
     * Get toggle sort button.
     *
     * @return The toggle sort button.
     */
    public UIComponent getToggleSortButton() {
        UIComponent facet = getFacet(TOGGLE_SORT_BUTTON_FACET);
        if (facet != null) {
            return facet;
        }

        Theme theme = getTheme();
        Table table = getTableAncestor();
        TableColumn col = getTableColumnAncestor();
        TableRowGroup group = getTableRowGroupAncestor();

        // Get child.
        IconHyperlink child = new IconHyperlink();
	child.setId(TOGGLE_SORT_BUTTON_ID);
        child.setBorder(0);
        child.setAlign("top"); //NOI18N
        child.addActionListener(new TableSortActionListener());

        // Disable descending sort so selections don't move off page.
        if (table != null && col != null) {
            if (!isDescending() && group.isPaginated()
                    && col.getSelectId() != null
                    && !table.isHiddenSelectedRows()) {
                child.setDisabled(true);
            }
        } else {
            log("getToggleSortButton", //NOI18N
                "Disabled state not set, Table or TableColumn is null"); //NOI18N
        }

        // Set alt and tool tip for the next sort applied.        
        if (col != null) {
            // Get tool tip.
            child.setToolTip(theme.getMessage("table.sort.button.toggle", //NOI18N
                new String[] {col.getSortToolTipAugment(!isDescending())}));

            // Get alt.
            if (isSelectHeader()) {
                // Select column does not have header text.
                child.setAlt(theme.getMessage("table.sort.alt.primary", //NOI18N
                    new String[] {theme.getMessage("table.select.selectionColumn"), //NOI18N
                        col.getSortToolTipAugment(isDescending()),
                        Integer.toString(getSortLevel())}));
            } else {
                String header = (col.getHeaderText() != null)
                    ? col.getHeaderText() : ""; //NOI18N
                child.setAlt(theme.getMessage("table.sort.alt.toggle", //NOI18N
                    new String[] {header,
                        col.getSortToolTipAugment(isDescending()), 
                        Integer.toString(getSortLevel())}));
            }
        } else {
            log("getToggleSortButton", "Alt text not set, TableColumn is null"); //NOI18N
        }

        // Set icon for the next sort applied.
        if (child.isDisabled()) {
            child.setIcon(ThemeImages.TABLE_SORT_DESCENDING_DISABLED);
        } else if (!isDescending()) {
            child.setIcon(ThemeImages.TABLE_SORT_DESCENDING);
        } else {
            child.setIcon(ThemeImages.TABLE_SORT_ASCENDING);
        }

        // Set styleClass.
        if (child.isDisabled()) {
            if (getSortLevel() == 1) {
                child.setStyleClass(ThemeStyles.TABLE_HEADER_SORT_DISABLED);
            } else {
                child.setStyleClass(ThemeStyles.TABLE_HEADER_SELECTCOL_DISABLED);
            }
        } else {
            child.setStyleClass(ThemeStyles.TABLE_HEADER_LINK_IMG);
        }

        // Add sort level text.
        if (getSortLevel() > 0 && getSortCount() > 0) {
            // Span must appear within hyperlink for style to render properly.
            child.getChildren().add(getSortLevelText());
        }

        // Set tab index.
        if (table != null) {
            child.setTabIndex(table.getTabIndex());
        } else {
            log("getToggleSortButton", "Tab index not set, Table is null"); //NOI18N
        }

        // Save facet and return child.
        getFacets().put(child.getId(), child);

        // Set focus when sort buttons are displayed -- bugtraq #6316565.
        setSortFocus(child);
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
        tableColumn = null;
        tableRowGroup = null;
        descending = false;
        descending_set = false;
        selectedRowsCount = -1;
        sortCount = -1;
        sortLevel = -1;
        super.encodeBegin(context);
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Private methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Helper method to get next sort tool tip augment based on the value for
     * the align property of TableColumn.
     *
     * @param descending Flag indicating descending sort.
     * @return The sort tool tip augment.
     */
    private String getNextSortToolTipAugment() {
        TableColumn col = getTableColumnAncestor();
        return (col != null) ? col.getSortToolTipAugment(isDescending()) : ""; //NOI18N
    }

    /**
     * Helper method to get the total number of selected rows.
     *
     * @return The number of selected rows.
     */
    private int getSelectedRowsCount() {
        if (selectedRowsCount == -1) {
            TableRowGroup group = getTableRowGroupAncestor();
            if (group != null) {
                selectedRowsCount = group.getRenderedSelectedRowsCount();
            }
        }
        return selectedRowsCount;
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
     * Helper method to test if the next sort order is descending.
     *
     * @return true if descending, else false.
     */
    private boolean isDescending() {
        if (!descending_set) {
            TableColumn col = getTableColumnAncestor();
            TableRowGroup group = getTableRowGroupAncestor();
       
            // Get next sort order.
            if (col != null && group != null) {
                descending = (getSortLevel() > 0)
                    ? group.isDescendingSort(col.getSortCriteria())
                    : col.isDescending();
                descending_set = true;
            }
        }
        return descending;
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

    /**
     * Set focus when sort buttons are displayed.
     */
    private void setSortFocus(UIComponent component) {
        if (component == null) {
            return;
        }

        // Get prefix for all IDs.
        FacesContext context = getFacesContext();
        String prefix = getClientId(context) + NamingContainer.SEPARATOR_CHAR;

        // Get the client ID of the last component to have focus.
        String id = RenderingUtilities.getLastClientID(context);
        if (id == null) {
            return;
        }

        // Set component focus if any match was found. Don't include select
        // sort button here as that component does not change.
        if (id.equals(prefix + ADD_SORT_BUTTON_ID)
                || id.equals(prefix + PRIMARY_SORT_BUTTON_ID)
                || id.equals(prefix + TOGGLE_SORT_BUTTON_ID)) {
            RenderingUtilities.setLastClientID(context, 
                component.getClientId(context));
        }        
    }
}
