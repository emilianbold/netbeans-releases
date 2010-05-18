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

import com.sun.rave.web.ui.component.util.Util;
import com.sun.rave.web.ui.event.TablePaginationActionListener;
import com.sun.rave.web.ui.event.TableSortActionListener;
import com.sun.rave.web.ui.model.Option;
import com.sun.rave.web.ui.model.Separator;
import com.sun.rave.web.ui.theme.Theme;
import com.sun.rave.web.ui.theme.ThemeStyles;
import com.sun.rave.web.ui.util.LogUtil;
import com.sun.rave.web.ui.util.ThemeUtilities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.component.NamingContainer;
import javax.faces.component.UIComponent;

/**
 * Component that represents a table.
 *
 * The table component provides a layout mechanism for displaying table actions.
 * UI guidelines describe specific behavior that can applied to the rows and 
 * columns of data such as sorting, filtering, pagination, selection, and custom 
 * user actions. In addition, UI guidelines also define sections of the table 
 * that can be used for titles, row group headers, and placement of pre-defined
 * and user defined actions.
 * <p>
 * Note: Column headers and footers are rendered by TableRowGroupRenderer. Table
 * column footers are rendered by TableRenderer.
 * </p><p>
 * Note: To see the messages logged by this class, set the following global
 * defaults in your JDK's "jre/lib/logging.properties" file.
 * </p><pre>
 * java.util.logging.ConsoleHandler.level = FINE
 * com.sun.rave.web.ui.component.Table.level = FINE
 * </pre><p>
 * See TLD docs for more information.
 * </p>
 */
public class Table extends TableBase implements NamingContainer {
    /** The facet name for the bottom actions area. */
    public static final String ACTIONS_BOTTOM_FACET = "actionsBottom"; //NOI18N

    /** The facet name for top actions area. */
    public static final String ACTIONS_TOP_FACET = "actionsTop"; //NOI18N

    /** The value for the custom filter option. */
    public static final String CUSTOM_FILTER = "_customFilter"; //NOI18N

    /** The value for the custom filter applied option. */
    public static final String CUSTOM_FILTER_APPLIED = "_customFilterApplied"; //NOI18N

    /** The id for the embedded panels bar. */
    public static final String EMBEDDED_PANELS_BAR_ID = "_embeddedPanelsBar"; //NOI18N

    /** The component id for embedded panels. */
    public static final String EMBEDDED_PANELS_ID = "_embeddedPanels"; //NOI18N

    /** The facet name for embedded panels. */
    public static final String EMBEDDED_PANELS_FACET = "embeddedPanels"; //NOI18N

    /** The facet name for the filter area. */
    public static final String FILTER_FACET = "filter"; //NOI18N

    /** The facet name for the filter panel. */
    public static final String FILTER_PANEL_FACET = "filterPanel"; //NOI18N

    /** The facet name for the footer area. */
    public static final String FOOTER_FACET = "footer"; //NOI18N

    /** The facet name for the preferences panel. */
    public static final String PREFERENCES_PANEL_FACET = "preferencesPanel"; //NOI18N

    /** The facet name for the sort panel. */
    public static final String SORT_PANEL_FACET = "sortPanel"; //NOI18N

    /** The id for the table. */
    public static final String TABLE_ID = "_table"; //NOI18N

    /** The id for the bottom actions bar. */
    public static final String TABLE_ACTIONS_BOTTOM_BAR_ID = "_tableActionsBottomBar"; //NOI18N

    /** The component id for bottom actions. */
    public static final String TABLE_ACTIONS_BOTTOM_ID = "_tableActionsBottom"; //NOI18N

    /** The facet name for bottom actions. */
    public static final String TABLE_ACTIONS_BOTTOM_FACET = "tableActionsBottom"; //NOI18N

    /** The id for the top actions bar. */
    public static final String TABLE_ACTIONS_TOP_BAR_ID = "_tableActionsTopBar"; //NOI18N

    /** The component id for top actions. */
    public static final String TABLE_ACTIONS_TOP_ID = "_tableActionsTop"; //NOI18N

    /** The facet name for top actions. */
    public static final String TABLE_ACTIONS_TOP_FACET = "tableActionsTop"; //NOI18N

    /** The id for the table footer. */
    public static final String TABLE_FOOTER_BAR_ID = "_tableFooterBar"; //NOI18N

    /** The component id for the table footer. */
    public static final String TABLE_FOOTER_ID = "_tableFooter"; //NOI18N

    /** The facet name for the table footer. */
    public static final String TABLE_FOOTER_FACET = "tableFooter"; //NOI18N

    /** The id for the title bar. */
    public static final String TITLE_BAR_ID = "_titleBar"; //NOI18N

    /** The facet name for the title area. */
    public static final String TITLE_FACET = "title"; //NOI18N

    // Key prefix for properties cached in the request map.
    private static final String REQUEST_KEY_PREFIX = "com.sun.rave.web.ui_"; //NOI18N

    // Key for properties cached in the request map.
    private static final String PROPERTIES = "_properties"; //NOI18N

    /** Default constructor */
    public Table() {
        super();
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Child methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Get the number of column header bars for all TableRowGroup children.
     *
     * @return The number of column headers.
     */
    public int getColumnHeadersCount() {
        // Get properties cached in request map.
        Properties properties = getProperties();
        int columnHeadersCount = (properties != null)
            ? properties.getColumnHeadersCount() : -1;

        // Get column header count.
        if (columnHeadersCount == -1) {
            columnHeadersCount = 0; // Initialize min value.

            // Iterate over each TableRowGroup child to determine if each group 
            // displays its own column header or if one column header is
            // dispalyed for all row groups.
            Iterator kids = getTableRowGroupChildren();
            while (kids.hasNext()) {
                TableRowGroup group = (TableRowGroup) kids.next();
                Iterator grandkids = group.getTableColumnChildren();
                while (grandkids.hasNext()) {
                    TableColumn col = (TableColumn) grandkids.next();
                    if (col.getHeaderText() != null) {
                        columnHeadersCount++;
                        break; // Break if at least one column header is found.
                    }
                }
            }
            // Save property in request map.
            if (properties != null) {
                properties.setColumnHeadersCount(columnHeadersCount);
            }
        }
        return columnHeadersCount;
    }

    /**
     * Get the number of hidden selected rows for all TableRowGroup children.
     *
     * @return The number of hidden selected rows.
     */
    public int getHiddenSelectedRowsCount() {
        // Get properties cached in request map.
        Properties properties = getProperties();
        int hiddenSelectedRowsCount = (properties != null)
            ? properties.getHiddenSelectedRowsCount() : -1;

        // Get hidden selected rows count.
        if (hiddenSelectedRowsCount == -1) {
            hiddenSelectedRowsCount = 0; // Initialize min value.
            Iterator kids = getTableRowGroupChildren();
            while (kids.hasNext()) {
                TableRowGroup group = (TableRowGroup) kids.next();
                hiddenSelectedRowsCount += group.getHiddenSelectedRowsCount();
            }
            // Save property in request map.
            if (properties != null) {
                properties.setHiddenSelectedRowsCount(hiddenSelectedRowsCount);
            }
        }
        return hiddenSelectedRowsCount;
    }

    /**
     * Get the zero-relative row number of the first row to be displayed for
     * a paginated table for all TableRowGroup children.
     *
     * @return The first row to be displayed.
     */
    public int getFirst() {
        // Get properties cached in request map.
        Properties properties = getProperties();
        int first = (properties != null) ? properties.getFirst() : -1;

        // Get first row.
        if (first == -1) {
            first = 0; // Initialize min value.
            Iterator kids = getTableRowGroupChildren();
            while (kids.hasNext()) {
                TableRowGroup group = (TableRowGroup) kids.next();
                first += group.getFirst();
            }
            // Save property in request map.
            if (properties != null) {
                properties.setFirst(first);
            }
        }
        return first;
    }

    /**
     * Get the max number of pages for all TableRowGroup children.
     *
     * @return The max number of pages.
     */
    public int getPageCount() {
        // Get properties cached in request map.
        Properties properties = getProperties();
        int pageCount = (properties != null) ? properties.getPageCount() : -1;

        // Get page count.
        if (pageCount == -1) {
            pageCount = 1; // Initialize min value.
            Iterator kids = getTableRowGroupChildren();
            while (kids.hasNext()) {
                TableRowGroup group = (TableRowGroup) kids.next();
                int pages = group.getPages();
                if (pageCount < pages) {
                    pageCount = pages;
                }
            }
            // Save property in request map.
            if (properties != null) {
                properties.setPageCount(pageCount);
            }
        }
        return pageCount;
    }
    
    /**
     * Get the number of rows to be displayed per page for a paginated table
     * for all TableRowGroup children.
     *
     * @return The number of rows to be displayed per page for a paginated table.
     */
    public int getRows() {
        // Get properties cached in request map.
        Properties properties = getProperties();
        int rows = (properties != null) ? properties.getRows() : -1;

        // Get rows per page.
        if (rows == -1) {
            rows = 0; // Initialize min value.
            Iterator kids = getTableRowGroupChildren();
            while (kids.hasNext()) {
                TableRowGroup group = (TableRowGroup) kids.next();
                rows += group.getRows();
            }
            // Save property in request map.
            if (properties != null) {
                properties.setRows(rows);
            }
        }
        return rows;
    }

    /**
     * Get the number of rows in the underlying TableDataProvider for all 
     * TableRowGroup children.
     *
     * @return The number of rows.
     */
    public int getRowCount() {
        // Get properties cached in request map.
        Properties properties = getProperties();
        int rowCount = (properties != null) ? properties.getRowCount() : -1;

        // Get row count.
        if (rowCount == -1) {
            rowCount = 0; // Initialize min value.
            Iterator kids = getTableRowGroupChildren();
            while (kids.hasNext()) {
                TableRowGroup group = (TableRowGroup) kids.next();
                rowCount += group.getRowCount();
            }
            // Save property in request map.
            if (properties != null) {
                properties.setRowCount(rowCount);
            }
        }
        return rowCount;
    }

    /**
     * Get the max number of columns found for all TableRowGroup children.
     *
     * @return The max number of columns.
     */
    public int getColumnCount() {
        // Get properties cached in request map.
        Properties properties = getProperties();
        int columnCount = (properties != null)
            ? properties.getColumnCount() : -1;

        // Get column count.
        if (columnCount == -1) {
            columnCount = 1; // Initialize min value.
            Iterator kids = getTableRowGroupChildren();
            while (kids.hasNext()) {
                TableRowGroup group = (TableRowGroup) kids.next();
                int count = group.getColumnCount();
                if (columnCount < count) {
                    columnCount = count;
                }
            }
            // Save property in request map.
            if (properties != null) {
                properties.setColumnCount(columnCount);
            }
        }
        return columnCount;
    }

    /**
     * Get the number of table column footer bars for all TableRowGroup children.
     *
     * @return The number of table column footers.
     */
    public int getTableColumnFootersCount() {
        // Get properties cached in request map.
        Properties properties = getProperties();
        int tableColumnFootersCount = (properties != null)
            ? properties.getTableColumnFootersCount() : -1;

        // Get table column footer count.
        if (tableColumnFootersCount == -1) {
            tableColumnFootersCount = 0; // Initialize min value.

            // Iterate over each TableRowGroup child to determine if each group 
            // displays its own table column footer or if one table column 
            // footer is dispalyed for all row groups.
            Iterator kids = getTableRowGroupChildren();
            while (kids.hasNext()) {
                TableRowGroup group = (TableRowGroup) kids.next();
                Iterator grandkids = group.getTableColumnChildren();
                while (grandkids.hasNext()) {
                    TableColumn col = (TableColumn) grandkids.next();
                    if (col.isRendered() && col.getTableFooterText() != null) {
                        tableColumnFootersCount++;
                        break; // Break if at least one table column footer is shown.
                    }
                }
            }
            // Save property in request map.
            if (properties != null) {
                properties.setTableColumnFootersCount(tableColumnFootersCount);
            }
        }
        return tableColumnFootersCount;
    }

    /**
     * Get the first TableRowGroup child found for the specified component that
     * have a rendered property of true.
     *
     * @return The first TableRowGroup child found.
     */
    public TableRowGroup getTableRowGroupChild() {
        TableRowGroup group = null;
        Iterator kids = getTableRowGroupChildren();
        if (kids.hasNext()) {
            group = (TableRowGroup) kids.next();
        }
        return group;
    }

    /**
     * Get an Iterator over the TableRowGroup children found for this component.
     *
     * @return An Iterator over the TableRowGroup children.
     */
    public Iterator getTableRowGroupChildren() {
        // Get properties cached in request map.
        Properties properties = getProperties();
        List tableRowGroupChildren = (properties != null)
            ? properties.getTableRowGroupChildren() : null;

        // Get TableRowGroup children.
        if (tableRowGroupChildren == null) {
            tableRowGroupChildren = new ArrayList();
            Iterator kids = getChildren().iterator();
            while (kids.hasNext()) {
                UIComponent kid = (UIComponent) kids.next();
                if ((kid instanceof TableRowGroup)) {
                    tableRowGroupChildren.add(kid);
                }
            }
            // Save property in request map.
            if (properties != null) {
                properties.setTableRowGroupChildren(tableRowGroupChildren);
            }
        }
        return tableRowGroupChildren.iterator();
    }

    /**
     * Get the number of child TableRowGroup components found for this component
     * that have a rendered property of true.
     *
     * @return The number of TableRowGroup children.
     */
    public int getTableRowGroupCount() {
        // Get properties cached in request map.
        Properties properties = getProperties();
        int tableRowGroupCount = (properties != null)
            ? properties.getTableRowGroupCount() : -1;

        // Get TableRowGroup children count.
        if (tableRowGroupCount == -1) {
            tableRowGroupCount = 0; // Initialize min value.
            Iterator kids = getTableRowGroupChildren();
            while (kids.hasNext()) {
                UIComponent kid = (UIComponent) kids.next();
                if (kid.isRendered()) {
                    tableRowGroupCount++;
                }
            }
            // Save property in request map.
            if (properties != null) {
                properties.setTableRowGroupCount(tableRowGroupCount);
            }
        }
        return tableRowGroupCount;
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Action methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Get bottom actions.
     *
     * @return The bottom actions.
     */
    public UIComponent getTableActionsBottom() {
        UIComponent facet = getFacet(TABLE_ACTIONS_BOTTOM_FACET);
        if (facet != null) {
            return facet;
        }

        // Get child.
        TableActions child = new TableActions();
	child.setId(TABLE_ACTIONS_BOTTOM_ID);
        child.setColSpan(getColumnCount());
        child.setExtraHtml(getExtraActionBottomHtml());
        child.setNoWrap(true);
        child.setActionsBottom(true);

        // We must determine if all TableRowGroup components are empty. Controls
        // are only hidden when all row groups are empty. Likewise, pagination
        // controls are only hidden when all groups fit on a single page.
        int totalRows = getRowCount();
        boolean emptyTable = (totalRows == 0);
        boolean singleRow = (totalRows == 1);
        boolean singlePage = (totalRows < getRows());

        // Get facets.
        UIComponent actions = getFacet(ACTIONS_BOTTOM_FACET);
        
        // Get flag indicating which facets to render.
        boolean renderActions = !emptyTable && !singleRow
            && actions != null && actions.isRendered();

        // Hide pagination controls when all rows fit on a page.
        boolean renderPaginationControls = !emptyTable && !singlePage
            && isPaginationControls();

        // Hide paginate button for a single row.
        boolean renderPaginateButton = !emptyTable && !singlePage
            && isPaginateButton();

        // Set rendered.
        if (!(renderActions || renderPaginationControls
                || renderPaginateButton)) {
            log("getTableActionsBottom", //NOI18N
                "Action bar not rendered, nothing to display"); //NOI18N
            child.setRendered(false);
        }

        // Save facet and return child.
        getFacets().put(child.getId(), child); 
        return child;
    }

    /**
     * Get top actions.
     *
     * @return The top actions.
     */
    public UIComponent getTableActionsTop() {
        UIComponent facet = getFacet(TABLE_ACTIONS_TOP_FACET);
        if (facet != null) {
            return facet;
        }

        // Get child.
        TableActions child = new TableActions();
	child.setId(TABLE_ACTIONS_TOP_ID);
        child.setColSpan(getColumnCount());
        child.setExtraHtml(getExtraActionTopHtml());
        child.setNoWrap(true);

        // We must determine if all TableRowGroup components are empty. Controls
        // are only hidden when all row groups are empty. Likewise, pagination
        // controls are only hidden when all groups fit on a single page.
        int totalRows = getRowCount();
        boolean emptyTable = (totalRows == 0);
        boolean singleRow = (totalRows == 1);
        boolean singlePage = (totalRows < getRows());

        // Get facets.
        UIComponent actions = getFacet(ACTIONS_TOP_FACET);
        UIComponent filter = getFacet(FILTER_FACET);
        UIComponent sort = getFacet(SORT_PANEL_FACET);
        UIComponent prefs = getFacet(PREFERENCES_PANEL_FACET);

        // Flags indicating which facets to render.
        boolean renderActions = actions != null && actions.isRendered();
        boolean renderFilter = filter != null && filter.isRendered();
        boolean renderSort = sort != null && sort.isRendered();
        boolean renderPrefs = prefs != null && prefs.isRendered();

        // Hide sorting and pagination controls for an empty table or when there
        // is only a single row.
        boolean renderSelectMultipleButton = !emptyTable
            && isSelectMultipleButton();
        boolean renderDeselectMultipleButton = !emptyTable
            && isDeselectMultipleButton();
        boolean renderDeselectSingleButton = !emptyTable
            && isDeselectSingleButton();
        boolean renderClearTableSortButton = !emptyTable && !singleRow
            && isClearSortButton();
        boolean renderTableSortPanelToggleButton = !emptyTable && !singleRow
            && (isSortPanelToggleButton() || renderSort);
        boolean renderPaginateButton = !emptyTable && !singlePage
            && isPaginateButton();

        // Return if nothing is rendered.
        if (!(renderActions || renderFilter || renderPrefs
                || renderSelectMultipleButton
                || renderDeselectMultipleButton
                || renderDeselectSingleButton
                || renderClearTableSortButton
                || renderTableSortPanelToggleButton
                || renderPaginateButton)) {
            log("getTableActionsTop", //NOI18N
                "Action bar not rendered, nothing to display"); //NOI18N
            child.setRendered(false);
        }

        // Save facet and return child.
        getFacets().put(child.getId(), child); 
        return child;
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Filter methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Get the HTML element ID of the dropDown component used to display table 
     * filter options.
     * <p>
     * Note: This is the fully qualified ID rendered in the outter tag enclosing
     * the HTML element. Required for Javascript functions to set the dropDown
     * styles when the embedded filter panel is opened and to reset the default
     * selected value when the panel is closed.
     * </p>
     * @return The HTML element ID of the filter menu.
     */
    public String getFilterId() {
        String filterId = super.getFilterId();
        if (filterId == null) {
            log("getFilterId", "filterId is null, using facet client ID"); //NOI18N
            UIComponent filter = getFacet(FILTER_FACET);
            filterId = (filter != null)
                ? filter.getClientId(getFacesContext())
                : null;
        }
        return filterId;
    }

    /** 
     * Get the "custom filter" options used for a table filter menu.
     * <p>
     * Note: UI guidelines state that a "Custom Filter" option should be added 
     * to the filter menu, used to open the table filter panel. Thus, if the 
     * CUSTOM_FILTER option is selected, Javascript invoked via the onChange
     * event will open the table filter panel.
     * </p><p>
     * UI guidelines also state that a "Custom Filter Applied" option should be 
     * added to the filter menu, indicating that a custom filter has been 
     * applied. In this scenario, set the selected property of the filter menu 
     * as CUSTOM_FILTER_APPLIED. This selection should persist until another 
     * menu option has been selected.
     * </p>
     * @param options An array of options to append to -- may be null.
     * @param customFilterApplied Flag indicating custom filter is applied.
     * @return A new array containing appended "custom filter" options.
     */
    static public Option[] getFilterOptions(Option[] options, 
            boolean customFilterApplied) {
        FacesContext context = FacesContext.getCurrentInstance();
        Theme theme = ThemeUtilities.getTheme(context);
        ArrayList newOptions = new ArrayList();
        
        // Get old options.
        if (options != null) {
            for (int i = 0; i < options.length; i++) { 
                newOptions.add(options[i]);
            }
        }

        // Add options separator.
        newOptions.add(new Separator());
        
        // Add custom filter applied option.
        if (customFilterApplied) {
            Option option = new Option(CUSTOM_FILTER_APPLIED, 
                theme.getMessage("table.viewActions.customFilterApplied")); //NOI18N
            option.setDisabled(true);
            newOptions.add(option);
        }
        
        // Add custom filter option.
	newOptions.add(new Option(CUSTOM_FILTER, 
            theme.getMessage("table.viewActions.customFilter"))); //NOI18N

        // Return options.
	Option[] result = new Option[newOptions.size()];
        return (Option[]) newOptions.toArray(result);
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Footer methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Get table footer.
     *
     * @return The table footer.
     */
    public UIComponent getTableFooter() {
        UIComponent facet = getFacet(TABLE_FOOTER_FACET);
        if (facet != null) {
            return facet;
        }

        // Get child.
        TableFooter child = new TableFooter();
	child.setId(TABLE_FOOTER_ID);
        child.setColSpan(getColumnCount());
        child.setExtraHtml(getExtraFooterHtml());
        child.setTableFooter(true);

        // Set rendered.
        if (!(facet != null && facet.isRendered()
                || getFooterText() != null || isHiddenSelectedRows())) {
            // Note: Footer may be initialized to force rendering. This allows
            // developers to omit the footer text property for select columns.
            log("getTableFooter", //NOI18N
                 "Table footer not rendered, nothing to display"); //NOI18N
            child.setRendered(false);
        }

        // Save facet and return child.
        getFacets().put(child.getId(), child); 
        return child;
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Panel methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Get embedded panels.
     *
     * @return The embedded panels.
     */
    public UIComponent getEmbeddedPanels() {
        UIComponent facet = getFacet(EMBEDDED_PANELS_FACET);
        if (facet != null) {
            return facet;
        }

        // Get child.
        TablePanels child = new TablePanels();
	child.setId(EMBEDDED_PANELS_ID);
        child.setColSpan(getColumnCount());
        child.setExtraHtml(getExtraPanelHtml());
        child.setNoWrap(true);

        // Get facets.
        UIComponent sort = getFacet(SORT_PANEL_FACET);
        UIComponent filter = getFacet(FILTER_PANEL_FACET);
        UIComponent prefs = getFacet(PREFERENCES_PANEL_FACET);

        // Set flags indicating which facets to render.
        boolean renderFilter = filter != null && filter.isRendered();
        boolean renderPrefs = prefs != null && prefs.isRendered();
        boolean renderSort = sort != null && sort.isRendered();

        // Set type of panel to render.
        child.setFilterPanel(renderFilter);
        child.setPreferencesPanel(renderPrefs);

        // Set rendered.
        if (!(renderFilter || renderSort || renderPrefs
                || isSortPanelToggleButton())) {
            log("getEmbeddedPanels", //NOI18N
                "Embedded panels not rendered, nothing to display"); //NOI18N
            child.setRendered(false);
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
        // No query data means the page is rendered for the first time and there
        // is no need to clear cached properties -- the lifecycle jumps right to 
        // the renderResponse phase.
        Map requestParamMap = context.getExternalContext().getRequestParameterMap();
        if (requestParamMap.size() != 0) {
            // Member variables may have been cached via the apply request 
            // values, validate, and update phases and must be re-evaluated
            // during the render response phase. The underlying DataProvider may
            // have changed and TableRenderer may need new calculations for the 
            // title and action bar.
            Map requestMap = context.getExternalContext().getRequestMap();
            requestMap.put(REQUEST_KEY_PREFIX + getClientId(context) +
                PROPERTIES, null); // Clear all properties.
        } else {
            log("encodeBegin", //NOI18N
                "Properties not cleared, request parameter map size is zero"); //NOI18N
        }

        // Initialize the internal virtual form used by this component.
        if (isInternalVirtualForm()) {
            // Get Form component.
            Form form = (Form) Util.getForm(getFacesContext(), this);
            if (form != null) {
                // Create VirtualFormDescriptor object.
                String id = getClientId(context) + "_virtualForm"; //NOI18N
                Form.VirtualFormDescriptor descriptor = 
                    new Form.VirtualFormDescriptor(id);
                String wildSuffix = String.valueOf(
                    NamingContainer.SEPARATOR_CHAR) + 
                    String.valueOf(Form.ID_WILD_CHAR);
                descriptor.setParticipatingIds(new String[]{getId() + wildSuffix});
                descriptor.setSubmittingIds(new String[]{getId() + wildSuffix});
        
                // Add virtual form.
                form.addInternalVirtualForm(descriptor);
            } else {
                log("encodeBegin", //NOI18N
                    "Internal virtual form not set, form ancestor is null"); //NOI18N
            }
        }
        super.encodeBegin(context);
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Private methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Get the properties for this component cached in the request map.
     * <p>
     * Note: Properties may have been cached via the apply request values, 
     * validate, and update phases and must be initialized for the render 
     * response phase. Table components are forced to reinitialize by setting 
     * the cached properties map to null when the table title and actions bar 
     * are rendered. New column and row counts are required each time the table
     * is redisplayed, for example.
     * </p>
     * @return The properties for this component.
     */
    private Properties getProperties() {
        // Get properties for all components.
        FacesContext context = getFacesContext();
        Map requestMap = context.getExternalContext().getRequestMap();
        String propertiesMapId = REQUEST_KEY_PREFIX + getClientId(context) + 
            PROPERTIES;
        Map propertiesMap = (Map) requestMap.get(propertiesMapId);
        if (propertiesMap == null) {
            propertiesMap = new HashMap();   
            requestMap.put(propertiesMapId, propertiesMap);
        }

        // Get properties for this component.
        String propertiesId = getClientId(context);
        Properties properties = (Properties) propertiesMap.get(propertiesId);
        if (properties == null) {
            properties = new Properties();
            propertiesMap.put(propertiesId, properties);
        }

        return properties;
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

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Inner classes
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Object used to cache properties in the request.
     */
    private class Properties {
        // The number of column headers.
        private int columnHeadersCount = -1;

        // The number of hidden selected rows.
        private int hiddenSelectedRowsCount = -1;

        // The max number of pages.
        private int first = -1;

        // The max number of pages.
        private int pageCount = -1;

        // The number of rows to be displayed per page for a paginated table.
        private int rows = -1;

        // The number of rows.
        private int rowCount = -1;

        // The max number of columns.
        private int columnCount = -1;

        // The number of column footers.
        private int tableColumnFootersCount = -1;

        // A List containing TableRowGroup children.
        private List tableRowGroupChildren = null;

        // The number of TableRowGroup children.
        private int tableRowGroupCount = -1;

        /** Default constructor. */
        public Properties() {
        }

        /**
         * Get the number of column header bars for all TableRowGroup children.
         *
         * @return The number of column headers.
         */
        public int getColumnHeadersCount() {
            return columnHeadersCount;
        }

        /**
         * Set the number of column header bars for all TableRowGroup children.
         *
         * @param columnHeadersCount The number of column headers.
         */
        public void setColumnHeadersCount(int columnHeadersCount) {
            this.columnHeadersCount = columnHeadersCount;
        }

        /**
         * Get the number of hidden selected rows for all TableRowGroup children.
         *
         * @return The number of hidden selected rows.
         */
        public int getHiddenSelectedRowsCount() {
            return hiddenSelectedRowsCount;
        }

        /**
         * set the number of hidden selected rows for all TableRowGroup children.
         *
         * @param hiddenSelectedRowsCount The number of hidden selected rows.
         */
        public void setHiddenSelectedRowsCount(int hiddenSelectedRowsCount) {
            this.hiddenSelectedRowsCount = hiddenSelectedRowsCount;
        }

        /**
         * Get the zero-relative row number of the first row to be displayed for
         * a paginated table for all TableRowGroup children.
         *
         * @return The max number of pages.
         */
        public int getFirst() {
            return first;
        }

        /**
         * Set the zero-relative row number of the first row to be displayed for
         * a paginated table for all TableRowGroup children.
         *
         * @param first The max number of pages.
         */
        public void setFirst(int first) {
            this.first = first;
        }

        /**
         * Get the max number of pages for all TableRowGroup children.
         *
         * @return The max number of pages.
         */
        public int getPageCount() {
            return pageCount;
        }

        /**
         * Get the max number of pages for all TableRowGroup children.
         *
         * @return The max number of pages.
         */
        public void setPageCount(int pageCount) {
            this.pageCount = pageCount;
        }

        /**
         * Get the number of rows to be displayed per page for a paginated table
         * for all TableRowGroup children.
         *
         * @return The number of rows to be displayed per page for a paginated table.
         */
        public int getRows() {
            return rows;
        }

        /**
         * Set the number of rows to be displayed per page for a paginated table
         * for all TableRowGroup children.
         *
         * @param rows The number of rows to be displayed per page for a paginated table.
         */
        public void setRows(int rows) {
            this.rows = rows;
        }

        /**
         * Get the number of rows for all TableRowGroup children.
         *
         * @return The number of rows.
         */
        public int getRowCount() {
            return rowCount;
        }

        /**
         * Set the number of rows for all TableRowGroup children.
         *
         * @param rowCount The number of rows.
         */
        public void setRowCount(int rowCount) {
            this.rowCount = rowCount;
        }

        /**
         * Get the max number of columns found for all TableRowGroup children.
         *
         * @return The max number of columns.
         */
        public int getColumnCount() {
            return columnCount;
        }

        /**
         * Set the max number of columns found for all TableRowGroup children.
         *
         * @param columnCount The max number of columns.
         */
        public void setColumnCount(int columnCount) {
            this.columnCount = columnCount;
        }

        /**
         * Get the number of table column footer bars for all TableRowGroup children.
         *
         * @return The number of column footers.
         */
        public int getTableColumnFootersCount() {
            return tableColumnFootersCount;
        }

        /**
         * Set the number of table column footer bars for all TableRowGroup children.
         *
         * @param tableColumnFootersCount The number of column footers.
         */
        public void setTableColumnFootersCount(int tableColumnFootersCount) {
            this.tableColumnFootersCount = tableColumnFootersCount;
        }

        /**
         * Get the TableRowGroup children found for this component.
         *
         * @return The TableRowGroup children.
         */
        public List getTableRowGroupChildren() {
            return tableRowGroupChildren;
        }

        /**
         * Set the TableRowGroup children found for this component.
         *
         * @param tableRowGroupChildren The TableRowGroup children.
         */
        public void setTableRowGroupChildren(List tableRowGroupChildren) {
            this.tableRowGroupChildren = tableRowGroupChildren;
        }

        /**
         * Get the number of child TableRowGroup components found for this component
         * that have a rendered property of true.
         *
         * @return The number of TableRowGroup children.
         */
        public int getTableRowGroupCount() {
            return tableRowGroupCount;
        }

        /**
         * Set the number of child TableRowGroup components found for this component
         * that have a rendered property of true.
         *
         * @param tableRowGroupCount The number of TableRowGroup children.
         */
        public void setTableRowGroupCount(int tableRowGroupCount) {
            this.tableRowGroupCount = tableRowGroupCount;
        }
    }
}
