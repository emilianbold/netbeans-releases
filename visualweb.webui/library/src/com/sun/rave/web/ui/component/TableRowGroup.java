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

import com.sun.data.provider.FieldKey;
import com.sun.data.provider.RowKey;
import com.sun.data.provider.FilterCriteria;
import com.sun.data.provider.SortCriteria;
import com.sun.data.provider.TableDataFilter;
import com.sun.data.provider.TableDataProvider;
import com.sun.data.provider.TableDataSorter;
import com.sun.data.provider.impl.BasicTableDataFilter;
import com.sun.data.provider.impl.BasicTableDataSorter;
import com.sun.data.provider.impl.ObjectListDataProvider;
import com.sun.data.provider.impl.ObjectArrayDataProvider;
import com.sun.data.provider.impl.TableRowDataProvider;
import com.sun.rave.web.ui.theme.Theme;
import com.sun.rave.web.ui.theme.ThemeImages;
import com.sun.rave.web.ui.theme.ThemeStyles;
import com.sun.rave.web.ui.util.ConversionUtilities;
import com.sun.rave.web.ui.util.LogUtil;
import com.sun.rave.web.ui.util.ThemeUtilities;

import java.beans.Beans;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.component.EditableValueHolder;
import javax.faces.component.NamingContainer;
import javax.faces.component.UIComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.component.UIViewRoot;
import javax.faces.el.ValueBinding;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.FacesEvent;
import javax.faces.event.FacesListener;
import javax.faces.event.PhaseId;

/**
 * Component that represents a group of table rows.
 * <p>
 * The TableRowGroup component provides a layout mechanism for displaying rows 
 * of data. UI guidelines describe specific behavior that can applied to the 
 * rows and columns of data such as sorting, filtering, pagination, selection, 
 * and custom user actions. In addition, UI guidelines also define sections of 
 * the table that can be used for titles, row group headers, and placement of 
 * pre-defined and user defined actions.
 * </p><p>
 * The TableRowGroup component supports a data binding to a collection of data 
 * objects represented by a TableDataProvider instance, which is the 
 * current value of this component itself. During iterative processing over the
 * rows of data in the data provider, the TableDataProvider for the current row 
 * is exposed as a request attribute under the key specified by the 
 * var property.
 * </p><p>
 * Only children of type TableColumn should be processed by renderers associated
 * with this component.
 * </p><p>
 * Note: Column headers and footers are rendered by TableRowGroupRenderer. Table
 * column footers are rendered by TableRenderer.
 * </p><p>
 * Note: To see the messages logged by this class, set the following global
 * defaults in your JDK's "jre/lib/logging.properties" file.
 * </p><p><pre>
 * java.util.logging.ConsoleHandler.level = FINE
 * com.sun.rave.web.ui.component.TableRowGroup.level = FINE
 * </pre></p><p>
 * See TLD docs for more information.
 * </p>
 */
public class TableRowGroup extends TableRowGroupBase implements 
        NamingContainer {
    /** The id for the column footer bar. */
    public static final String COLUMN_FOOTER_BAR_ID = "_columnFooterBar"; //NOI18N

    /** The id for the column header bar. */
    public static final String COLUMN_HEADER_BAR_ID = "_columnHeaderBar"; //NOI18N

    /** The component id for the empty data column. */
    public static final String EMPTY_DATA_COLUMN_ID = "_emptyDataColumn"; //NOI18N

    /** The facet name for the empty data column. */
    public static final String EMPTY_DATA_COLUMN_FACET = "emptyDataColumn"; //NOI18N

    /** The component id for the empty data text. */
    public static final String EMPTY_DATA_TEXT_ID = "_emptyDataText"; //NOI18N

    /** The facet name for the empty data text. */
    public static final String EMPTY_DATA_TEXT_FACET = "emptyDataText"; //NOI18N

    /** The facet name for the group footer area. */
    public static final String FOOTER_FACET = "footer"; //NOI18N

    /** The id for the group footer bar. */
    public static final String GROUP_FOOTER_BAR_ID = "_groupFooterBar"; //NOI18N

    /** The component id for the group footer. */
    public static final String GROUP_FOOTER_ID = "_groupFooter"; //NOI18N

    /** The facet name for the group footer. */
    public static final String GROUP_FOOTER_FACET = "groupFooter"; //NOI18N
  
    /** The id for the table row group header bar. */
    public static final String GROUP_HEADER_BAR_ID = "_groupHeaderBar"; //NOI18N

    /** The component id for the table row group header. */
    public static final String GROUP_HEADER_ID = "_groupHeader"; //NOI18N

    /** The facet name for the table row group header. */
    public static final String GROUP_HEADER_FACET = "groupHeader"; //NOI18N

    /** The facet name for the group header area. */
    public static final String HEADER_FACET = "header"; //NOI18N

    /** The id for the table column footers bar. */
    public static final String TABLE_COLUMN_FOOTER_BAR_ID = "_tableColumnFooterBar"; //NOI18N

    // Key prefix for properties cached in the request map.
    private static final String REQUEST_KEY_PREFIX = "com.sun.rave.web.ui_"; //NOI18N

    // Key for properties cached in the request map.
    private static final String PROPERTIES = "_properties"; //NOI18N

    // This map contains SavedState instances for each descendant
    // component, keyed by the client identifier of the descendant. Because
    // descendant client identifiers will contain the RowKey value of the
    // parent, per-row state information is actually preserved.
    private Map saved = new HashMap();

    // TableDataFilter object used to apply filter. This object is not part of
    // the saved and restored state of the component.
    private transient TableDataFilter filter = null;

    // TableDataSorter object used to apply sort. This object is not part of
    // the saved and restored state of the component.
    private transient TableDataSorter sorter = null;

    // Flag indicating paginated state.
    private boolean paginated = false;
    private boolean paginated_set = false;

    // The TableRowDataProvider associated with this component, lazily
    // instantiated if requested. This object is not part of the saved and
    // restored state of the component.
    private TableRowDataProvider provider = null;
    
    // The Table ancestor enclosing this component.
    private Table table = null;

    /** Default constructor */
    public TableRowGroup() {
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
        // Don't cache in the request since it's used as the properties key.
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
     * Get an Iterator over the TableColumn children found for
     * this component.
     *
     * @return An Iterator over the TableColumn children.
     */
    public Iterator getTableColumnChildren() {
        // Get properties cached in request map.
        Properties properties = getProperties();
        List tableColumnChildren = (properties != null)
            ? properties.getTableColumnChildren() : null;

        // Get TableColumn children.
        if (tableColumnChildren == null) {
            tableColumnChildren = new ArrayList();
            Iterator kids = getChildren().iterator();
            while (kids.hasNext()) {
                UIComponent kid = (UIComponent) kids.next();
                if ((kid instanceof TableColumn)) {
                    tableColumnChildren.add(kid);
                }
            }
            // Save property in request map.
            if (properties != null) {
                properties.setTableColumnChildren(tableColumnChildren);
            }
        }
        return tableColumnChildren.iterator();
    }

    /**
     * Get the number of columns found for this component that have a rendered
     * property of true.
     *
     * @return The number of rendered columns.
     */
    public int getColumnCount() {
        // Get properties cached in request map.
        Properties properties = getProperties();
        int columnCount = (properties != null)
            ? properties.getColumnCount() : -1;

        // Get column count.
        if (columnCount == -1) {
            columnCount = 0; // Initialize min value.
            Iterator kids = getTableColumnChildren();
            while (kids.hasNext()) {
                TableColumn col = (TableColumn) kids.next();
                columnCount += col.getColumnCount();
            }
            // Save property in request map.
            if (properties != null) {
                properties.setColumnCount(columnCount);
            }
        }
        return columnCount;
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Component methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Get empty data column.
     *
     * @return The empty data column.
     */
    public UIComponent getEmptyDataColumn() {
        UIComponent facet = getFacet(EMPTY_DATA_COLUMN_FACET);
        if (facet != null) {
            return facet;
        }

        // Get child.
        TableColumn child = new TableColumn();
	child.setId(EMPTY_DATA_COLUMN_ID);
        child.setColSpan(getColumnCount());
        child.getChildren().add(getEmptyDataText());

        // Save facet and return child.
        getFacets().put(child.getId(), child);
        return child;
    }

    /**
     * Get empty data text.
     *
     * @return The empty data text.
     */
    public UIComponent getEmptyDataText() {
        UIComponent facet = getFacet(EMPTY_DATA_TEXT_FACET);
        if (facet != null) {
            return facet;
        }

        Theme theme = getTheme();

        // Get message.
        String msg = null;
        if (getEmptyDataMsg() != null) {
            msg = getEmptyDataMsg();
        } else {
            // Get unfiltered row keys.
            RowKey[] rowKeys = getRowKeys();
            if (rowKeys != null && rowKeys.length > 0) {
                msg = theme.getMessage("table.filteredData"); //NOI18N
            } else {
                msg = theme.getMessage("table.emptyData"); //NOI18N
            }
        }

        // Get child.
        StaticText child = new StaticText();
        child.setId(EMPTY_DATA_TEXT_ID);        
        child.setStyleClass(theme.getStyleClass(ThemeStyles.TABLE_MESSAGE_TEXT));
        child.setText(msg);

        // Save facet and return child.
        getFacets().put(child.getId(), child);
        return child;
    }

    /**
     * Get group footer.
     *
     * @return The group footer.
     */
    public UIComponent getGroupFooter() {
        UIComponent facet = getFacet(GROUP_FOOTER_FACET);
        if (facet != null) {
            return facet;
        }

        // Get child.
        TableFooter child = new TableFooter();
	child.setId(GROUP_FOOTER_ID);
        child.setColSpan(getColumnCount());
        child.setExtraHtml(getExtraFooterHtml());
        child.setGroupFooter(true);

        // Set rendered.
        facet = getFacet(FOOTER_FACET);
        if (!(facet != null && facet.isRendered() || getFooterText() != null)) {
            child.setRendered(false);
        } else {
            log("getGroupFooter", //NOI18N
                "Group footer not rendered, nothing to display"); //NOI18N
        }

        // Save facet and return child.
        getFacets().put(child.getId(), child);
        return child;
    }

    /**
     * Get group header.
     *
     * @return The group header.
     */
    public UIComponent getGroupHeader() {
        UIComponent facet = getFacet(GROUP_HEADER_FACET);
        if (facet != null) {
            return facet;
        }

        // Get child.
        TableHeader child = new TableHeader();
	child.setId(GROUP_HEADER_ID);
        child.setScope("colgroup"); //NOI18N
        child.setColSpan(getColumnCount());
        child.setExtraHtml(getExtraHeaderHtml());
        child.setGroupHeader(true);

        // Don't render for an empty table.
        boolean emptyTable = getRowCount() == 0;
        boolean renderControls = !emptyTable
            && (isSelectMultipleToggleButton() || isGroupToggleButton());

        // Set rendered.
        facet = getFacet(HEADER_FACET);
        if (!(facet != null && facet.isRendered()
                || getHeaderText() != null || renderControls)) {
            child.setRendered(false);
        } else {
            log("getGroupHeader", //NOI18N
                "Group header not rendered, nothing to display"); //NOI18N
        }

        // Save facet and return child.
        getFacets().put(child.getId(), child);
        return child;
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Filter methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Clear FilterCriteria objects from the TableDataFilter instance used by
     * this component. 
     * <p>
     * Note: This method clears the cached filter and sort, then resets
     * pagination to the first page per UI guidelines.
     * </p>
     */
    public void clearFilter() {
        getTableDataFilter().setFilterCriteria(null); // Clear all FilterCriteria.
        setFirst(0); // Reset to first page.

        // Clear properties cached in request map.
        Properties properties = getProperties();
        if (properties != null) {
            properties.setFilteredRowKeys(null); // Clear filtered row keys.
            properties.setSortedRowKeys(null); // Clear sorted row keys.
        } else {
            log("clearFilter", //NOI18N
                "Cannot clear filtered and sorted row keys, Properties is null"); //NOI18N
        }
    }

    /**
     * Get an array containing filtered RowKey objects.
     * <p>
     * Note: This filter depends on the FilterCriteria objects provided to the
     * TableDataFilter instance used by this component. Due to filtering, the
     * size of the returned array may be less than the total number of RowKey
     * objects for the underlying TableDataProvider.
     * </p><p>
     * Note: The returned RowKey objects are cached. If the TableDataFilter
     * instance used by this component is modified directly, invoke the
     * clearFilter method to clear the previous filter.
     * </p>
     * @return An array containing filtered RowKey objects.
     */
    public RowKey[] getFilteredRowKeys() {
        // Get properties cached in request map.
        Properties properties = getProperties();
        RowKey[] filteredRowKeys = (properties != null)
            ? properties.getFilteredRowKeys() : null;

        // Initialize RowKey objects, if not cached already.
        if (filteredRowKeys != null) {
            return filteredRowKeys;
        } else {
            filteredRowKeys = getRowKeys();
        }

        // Do not attempt to filter with a null provider.
        TableDataProvider provider = getTableRowDataProvider().
            getTableDataProvider();
        if (provider == null) {
            log("getFilteredRowKeys", //NOI18N
                "Cannot obtain filtered row keys, TableDataProvider is null"); //NOI18N
            return filteredRowKeys;
        }

        // If TableDataFilter and TableDataProvider are the same instance, the
        // filter method is never called. The filter order is assumed to be
        // intrinsic in the row data of the TableDataProvider.
        TableDataFilter filter = getTableDataFilter();
        if (provider != filter) {
            filteredRowKeys = filter.filter(provider, filteredRowKeys);
        } else {
            log("getFilteredRowKeys", //NOI18N
                "Row keys already filtered, TableDataFilter and TableDataProvider are the same instance"); //NOI18N
        }

        // Save properties.
        if (properties != null) {
            properties.setFilteredRowKeys(filteredRowKeys);
        } else {
            log("getFilteredRowKeys", //NOI18N
                "Cannot save filtered row keys, Properties is null"); //NOI18N
        }
        return filteredRowKeys;
    }

    /**
     * Get the TableDataFilter object used to filter rows.
     *
     * @return The TableDataFilter object used to filter rows.
     */
    public TableDataFilter getTableDataFilter() {
        // Method is overriden because TableDataFilter is not serializable.
        TableDataFilter tdf = super.getTableDataFilter();
        if (tdf != null) {
            return tdf;
        }

        // Get default filter.
        if (filter == null) {
            filter = new BasicTableDataFilter();
        }
        return filter;
    }

    /**
     * Set FilterCriteria objects for the TableDataFilter instance used by this
     * component. 
     * <p>
     * Note: This method clears the cached filter and sort, then resets
     * pagination to the first page per UI guidelines.
     * </p>
     * @param filterCriteria An array of FilterCriteria objects defining the
     * filter order on this TableDataFilter.
     */
    public void setFilterCriteria(FilterCriteria[] filterCriteria) {
        clearFilter();
        getTableDataFilter().setFilterCriteria(filterCriteria);
    }

    /**
     * Set the TableDataFilter object used to filter rows.
     *
     * @param filter The TableDataFilter object used to filter rows.
     */
    public void setTableDataFilter(TableDataFilter filter) {
        // Method is overriden because TableDataFilter is not serializable.
        this.filter = filter;
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Pagination methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Get the zero-relative row number of the first row to be displayed for
     * a paginated table.
     * <p>
     * Note: When ever a new DataProvider is used, UI Guiedlines recommend that
     * pagination should be reset (e.g., remaining on the 4th page of a new set
     * of data makes no sense).
     * </p><p>
     * Note: If rows have been removed from the table, there is a chance that 
     * the first row could be greater than the total number of rows. In this 
     * case, the zero-relative row number of the last page to be displayed is 
     * returned.
     * </p>
     * @return The zero-relative row number of the first row to be displayed.
     */
    public int getFirst() {
        // Ensure the first row is less than the row number of the last page.
        int last = getLast();
        int first = isPaginated() ? Math.max(0, super.getFirst()) : 0;
        return (first < last) ? first : last;
    }

    /**
     * Set the zero-relative row number of the first row to be displayed for
     * a paginated table.
     *
     * @param first The first row number.
     * @exception IllegalArgumentException for negative values.
     */
    public void setFirst(int first) {
        if (first < 0) {
            log("setFirst", "First row cannot be < 0"); //NOI18N
            throw new IllegalArgumentException(Integer.toString(first));
        }
        super.setFirst(first);
    }

    /**
     * Get the zero-relative row number of the last page to be displayed.
     *
     * @return The zero-relative row number of the last page to be displayed.
     */
    public int getLast() {
        return Math.max(0, getPages() - 1) * getRows();
    }

    /**
     * Get current page number to be displayed.
     * <p>
     * Note: The default is 1 when the table is not paginated.
     * </p>
     * @return The current page number to be displayed.
     */
    public int getPage() {
        if (!isPaginated()) { // Rows is zero when paginated.
            return 1;
        }
        return (getFirst() / getRows()) + 1;
    }

    /**
     * Get total number of pages to be displayed. The default is 1 when the
     * table is not paginated.
     * <p>
     * Note: The page count depends on the FilterCriteria objects provided to
     * the TableDataFilter instance used by this component. Further, the filter
     * used to obtain the page count is cached. If the TableDataFilter instance 
     * used by this component is to be modified directly, invoke the clearFilter
     * method to clear the previous filter.
     * </p>
     * @return The total number of pages to be displayed.
     */
    public int getPages() {
        if (!isPaginated()) {
            return 1;
        }

        int rowCount = getRowCount(); // Get row count.
        int rows = getRows(); // Get rows per page. 
        
        // Note: Rows should be > 0 when paginated.
        int modulus = (rows > 0) ? rowCount % rows : 0;
        int result = (rows > 0) ? rowCount / rows : 1;

        // Increment result for extra rows.
        return (modulus > 0) ? ++result : result;
    }

    /**
     * Test the paginated state of this component.
     * <p>
     * Note: If the paginationControls property of the Table component is true,
     * this property will be initialized as true.
     * </p>
     * @return true for paginate mode, false for scroll mode.
     */
    public boolean isPaginated() {
        if (!paginated_set) {
            Table table = getTableAncestor();
            if (table != null) {
                setPaginated(table.isPaginationControls());
            } else {
                log("isPaginated", //NOI18N
                    "Cannot initialize paginated state, Table is null"); //NOI18N
            }
        }
        return paginated;
    }
    
    /**
     * A convenience method to set the current page to be displayed.
     * <p>
     * Note: You can also set the current, first, next, prev, and last pages by
     * invoking the setFirst(int) method directly. For example, you could use
     * setFirst(0) to display the first page and setFirst(getLast()) to display
     * the last page. The setFirst(int) method is particularly useful when a
     * subset of data is displayed in scroll mode or when overriding pagination.
     * </p><p>
     * Note: When ever a new DataProvider is used, UI Guiedlines recommend that
     * pagination should be reset (e.g., remaining on the 4th page of a new set
     * of data makes no sense).
     * </p>
     * @param page The current page.
     */
    public void setPage(int page) {
        // Set the starting row for the new page.
        int row = (page - 1) * getRows();

        // Result cannot be greater than the row index for the last page.
        int result = Math.min(row, getLast());

        // Result cannot be greater than total number of rows or less than zero.
        setFirst(Math.min(Math.max(result, 0), getRowCount()));
    }

    /**
     * Set the paginated state of this component.
     * <p>
     * Note: When pagination controls are used, a value of true allows both
     * pagination controls and paginate buttons to be displayed. A value of
     * false allows only paginate buttons to be displayed. However, when all
     * data fits on one page, neither pagination controls or paginate buttons
     * are displayed.
     * </p><p>
     * Note: To properly maintain the paginated state of the table per UI
     * guidelines, the paginated property is cached. If the paginationControls 
     * property of the table component changes (e.g., in an application builder 
     * environment), use this method to set the paginated property accordingly.
     * </p>
     * @param paginated The paginated state of this component.
     */
    public void setPaginated(boolean paginated) {
        this.paginated = paginated;
        paginated_set = true;
    }

    /**
     * Get the number of rows to be displayed for a paginated table.
     * <p>
     * Note: UI guidelines recommend a default value of 25 rows per page.
     * </p>
     * @return The number of rows to be displayed for a paginated table.
     */
    public int getRows() {
        return isPaginated() ? Math.max(1, super.getRows()) : 0;
    }

    /**
     * Set the number of rows to be displayed for a paginated table.
     *
     * @param rows The number of rows to be displayed for a paginated table.
     * @exception IllegalArgumentException for negative values.
     */
    public void setRows(int rows) {
        if (rows < 0) {
            log("setRows", "Paginated rows cannot be < 0"); //NOI18N
            throw new IllegalArgumentException(Integer.toString(rows));
        }
        super.setRows(rows);
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Row methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Get the flag indicating whether there is row data available for the
     * current RowKey. If no row data is available, false is returned.
     *
     * @return The flag indicating whether there is row data available.
     */
    public boolean isRowAvailable() {
        boolean result = false;
        TableDataProvider provider = getTableRowDataProvider().
            getTableDataProvider();
        if (provider != null) {
            result = provider.isRowAvailable(getRowKey());
        } else {
            log("isRowAvailable", //NOI18N
                "Cannot determine if row is available, TableDataProvider is null"); //NOI18N
        }
        return result;
    }

    /**
     * Get an array of hidden RowKey objects from the underlying 
     * TableDataProvider taking filtering, sorting, and pagination into account.
     * <p>
     * Note: The returned RowKey objects depend on the FilterCriteria and 
     * SortCriteria objects provided to the TableDataFilter and TableDataSorter
     * instances used by this component. If TableDataFilter and TableDataSorter
     * are modified directly, invoke the clearSort and clearFilter method to
     * clear the previous sort and filter.
     * </p>
     * @return An array of RowKey objects.
     */
    public RowKey[] getHiddenRowKeys() {
        if (!isPaginated()) {
            return null; // No rows are hidden during scroll mode.
        }

        // Get sorted RowKey objects.
        RowKey[] rowKeys = getSortedRowKeys();
        if (rowKeys == null) {
            return rowKeys;
        }  

        // Find the number of selected rows hidden from view.
        ArrayList list = new ArrayList();
        int first = getFirst();
        int rows = getRows();
        for (int i = 0; i < rowKeys.length; i++) {
            // Have we displayed the paginated number of rows?
            if (i >= first && i < first + rows) {
                continue;
            }
            list.add(rowKeys[i]);
        }
        rowKeys = new RowKey[list.size()];
        return (RowKey[]) list.toArray(rowKeys);
    }

    /**
     * Get the FieldKey from the underlying TableDataProvider.
     *
     * @param fieldId The id of the requested FieldKey.
     * @return The RowKey from the underlying TableDataProvider.
     */
    public FieldKey getFieldKey(String fieldId) {
        return getTableRowDataProvider().getFieldKey(fieldId);
    }

    /**
     * Get the number of rows in the underlying TableDataProvider. If the
     * number of available rows is unknown, -1 is returned.
     * <p>
     * Note: This row count depends on the FilterCriteria objects provided to
     * the TableDataFilter instance used by this component. Further, the filter
     * used to obtain the row count is cached. If the TableDataFilter instance 
     * used by this component is modified directly, invoke the clearFilter
     * method to clear the previous filter.
     * </p>
     * @return The number of rows in the underlying TableDataProvider.
     */
    public int getRowCount() {
        RowKey[] rowKeys = getFilteredRowKeys();
        return (rowKeys != null) ? rowKeys.length : 0;
    }

    /**
     * Get the RowKey associated with the current row.
     *
     * @return The RowKey associated with the current row.
     */
    public RowKey getRowKey() {
        return getTableRowDataProvider().getTableRow();
    }

    /**
     * Get all RowKey objects for the underlying TableDataProvider.
     *
     * @return All RowKey objects for the underlying TableDataProvider.
     */
    public RowKey[] getRowKeys() {       
        RowKey[] rowKeys = null;
        TableDataProvider provider = getTableRowDataProvider().
            getTableDataProvider();
        if (provider == null) {
            log("getRowKeys", //NOI18N
                "Cannot obtain row keys, TableDataProvider is null"); //NOI18N
            return rowKeys;
        }

        // Create fake data for design-time behavior. The ResultSetDataProvider 
        // returns 3 rows of dummy data; however, this is not enough to display
        // pagination controls properly. When all rows fit on a single page, or 
        // when we have an empty table, certain controls are hidden from view. 
        // Thus, if a user specifies 20 rows per page, we want to create 20 + 1 
        // rows of data forcing controls to be displayed.
        if (Beans.isDesignTime()) {
            log("getRowKeys", "Creating dummy data for design-time behavior"); //NOI18N
            rowKeys = provider.getRowKeys(provider.getRowCount(), null);
            // If pagination is not enabled, dummy data is not required.
            if (getRows() == 0 || rowKeys == null || rowKeys.length == 0) {
                log("getRowKeys", //NOI18N
                    "Cannot create dummy data, DataProvider has no rows"); //NOI18N
                return rowKeys;
            } else {
                ArrayList list = new ArrayList();
                for (int i = 0; i < getRows() + 1; i++) {
                    list.add(rowKeys[i % rowKeys.length]);
                }
                rowKeys = new RowKey[list.size()];
                return ((RowKey[]) list.toArray(rowKeys));
            }
        }

        // It's possible that the provider returned -1 because it does not
        // actually have all the rows, so it's up to the consumer of the
        // interface to fetch them. Typically, 99% of the data providers will
        // return a valid row count (at least our providers will), but we still
        // need to handle the scenario where -1 is returned.
        int rowCount = provider.getRowCount();
        if (rowCount == -1) {
            log("getRowKeys", //NOI18N
                "Manually calculating row count, DataProvider.getRowCount() is -1"); //NOI18N
            int index = 0;
            do {
                // Keep trying until all rows are obtained.
                rowCount = 1000000 * ++index;
                rowKeys = provider.getRowKeys(rowCount, null);
            } while (rowKeys != null && rowKeys.length - 1 == rowCount);
        } else {
            rowKeys = provider.getRowKeys(rowCount, null);
        }
        return rowKeys;
    }

    /**
     * Get the TableRowDataProvider object representing the data objects that
     * we will iterate over in this component's rendering.
     *
     * @return The TableRowDataProvider object.
     */
    protected TableRowDataProvider getTableRowDataProvider() {
        // Get properties cached in request map.
        Properties properties = getProperties();
        TableRowDataProvider provider = (properties != null)
            ? properties.getTableRowDataProvider() : null;

        // Get provider.
        if (provider == null) {
            log("getTableRowDataProvider", //NOI18N
                "Re-evaluating sourceData, TableRowDataProvider is null"); //NOI18N
            
            // Synthesize a TableDataProvider around source data, if possible.
            TableDataProvider tdp;
            Object obj = getSourceData();
            if (obj == null) {
                tdp = null;
            } else if (obj instanceof TableDataProvider) {
                tdp = (TableDataProvider) obj;
            } else if (obj instanceof List) {
                tdp = new ObjectListDataProvider((List) obj);
            } else if (Object[].class.isAssignableFrom(obj.getClass())) {
                tdp = new ObjectArrayDataProvider((Object[]) obj);
            } else {
                // Default "single variable" case.
                ArrayList list = new ArrayList(1);
                list.add(obj);
                tdp = new ObjectListDataProvider(list);
            }
            provider = new TableRowDataProvider(tdp);

            // Save property in request map.
            if (properties != null) {
                properties.setTableRowDataProvider(provider);
            } else {
                log("getTableRowDataProvider", //NOI18N
                    "Cannot save TableRowDataProvider, Properties is null"); //NOI18N
            }
        }
        return provider;
    }

    /**
     * Get the data type of the data element referenced by the given FieldKey.
     *
     * @param fieldKey The FieldKey identifying the data element whose type is
     * to be returned.
     * @return The data type of the data element referenced by the given FieldKey.
     */
    public Class getType(FieldKey fieldKey) {
        return getTableRowDataProvider().getType(fieldKey);
    }

    /**
     * Get an array of rendered RowKey objects from the underlying 
     * TableDataProvider taking filtering, sorting, and pagination into account.
     * <p>
     * Note: The returned RowKey objects depend on the FilterCriteria and 
     * SortCriteria objects provided to the TableDataFilter and TableDataSorter
     * instances used by this component. If TableDataFilter and TableDataSorter
     * are modified directly, invoke the clearSort and clearFilter method to
     * clear the previous sort and filter.
     * </p>
     * @return An array of RowKey objects.
     */
    public RowKey[] getRenderedRowKeys() {
        // Get sorted RowKey objects.
        RowKey[] rowKeys = getSortedRowKeys();
        if (rowKeys == null) {
            return rowKeys;
        }  

        // Find the number of selected rows hidden from view.
        ArrayList list = new ArrayList();
        int first = getFirst();
        int rows = getRows();
        for (int i = first; i < rowKeys.length; i++) {
            // Have we displayed the paginated number of rows?
            if (isPaginated() && i >= first + rows) {
                break;
            }
            list.add(rowKeys[i]);
        }
        rowKeys = new RowKey[list.size()];
        return (RowKey[]) list.toArray(rowKeys);
    }

    /**
     * Set the RowKey associated with the current row or null for no current row
     * association.
     * <p>
     * Note: It is possible to set the RowKey at a value for which the 
     * underlying TableDataProvider does not contain any row data. Therefore,
     * callers may use the isRowAvailable() method to detect whether row data
     * will be available.
     * <ul>
     * <li>Save current state information for all descendant components (as
     *     described below).
     * <li>Store the new RowKey, and pass it on to the TableDataProvider 
     *     associated with this TableRowGroup instance.</li>
     * <li>If the new RowKey value is null:
     *     <ul>
     *     <li>If the var property is not null,
     *         remove the corresponding request scope attribute (if any).</li>
     *     <li>Reset the state information for all descendant components
     *         (as described below).</li>
     *     </ul></li>
     * <li>If the new RowKey value is not null:
     *     <ul>
     *     <li>If the var property is not null, expose the
     *         data provider as a request scope attribute whose key is the
     *         var property value.</li>
     *     <li>Reset the state information for all descendant components
     *         (as described below).
     *     </ul></li>
     * </ul></p><p>
     * To save current state information for all descendant components,
     * TableRowGroup must maintain per-row information for each descendant as
     * follows:
     * <ul>
     * <li>If the descendant is an instance of EditableValueHolder,
     *     save the state of its localValue property.</li>
     * <li>If the descendant is an instance of EditableValueHolder,
     *     save the state of the localValueSet property.</li>
     * <li>If the descendant is an instance of EditableValueHolder,
     *     save the state of the valid property.</li>
     * <li>If the descendant is an instance of EditableValueHolder,
     *     save the state of the submittedValue property.</li>
     * </ul></p><p>
     * To restore current state information for all descendant components,
     * TableRowGroup must reference its previously stored information
     * for the current RowKey and call setters for each descendant
     * as follows:
     * <ul>
     * <li>If the descendant is an instance of EditableValueHolder,
     *     restore the value property.</li>
     * <li>If the descendant is an instance of EditableValueHolder,
     *     restore the state of the localValueSet property.</li>
     * <li>If the descendant is an instance of EditableValueHolder,
     *     restore the state of the valid property.</li>
     * <li>If the descendant is an instance of EditableValueHolder,
     *     restore the state of the submittedValue property.</li>
     * </ul></p>
     *
     * @param rowKey The RowKey associated with the current row or
     * null for no association.
     */
    public void setRowKey(RowKey rowKey) {
        // Save current state for the previous row.
        saveDescendantState();

        // Update to the new row.
        getTableRowDataProvider().setTableRow(rowKey);

        // Clear or expose the current row data as a request scope attribute
        String sourceVar = getSourceVar();
        if (sourceVar != null) {
            Map requestMap =
                getFacesContext().getExternalContext().getRequestMap();
            if (rowKey == null) {
                requestMap.remove(sourceVar);
            } else if (isRowAvailable()) {
                requestMap.put(sourceVar, getTableRowDataProvider());
            } else {
                requestMap.remove(sourceVar);
            }
        } else {
            log("setRowKey", "Cannot set row key, sourceVar property is null"); //NOI18N
        }

        // Reset current state information for the new row.
        restoreDescendantState();
    }

    /**
     * Set the source data of the TableRowGroup.
     * <p>
     * Note: When ever a new DataProvider is used, UI Guiedlines recommend that
     * pagination should be reset (e.g., remaining on the 4th page of a new set
     * of data makes no sense). However, properties such as the sort and filter 
     * criteria should not automatically be cleared (e.g., there may be 
     * situations where one or both should be left as specified by the user). In
     * this scenario, pagination is set to the first page.
     * </p>
     * @param sourceData The source data of the TableRowGroup.
     */
    public void setSourceData(Object sourceData) {
        super.setSourceData(sourceData);
        init();
    }
    
     

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Selected methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Get the number of objects from the underlying data provider where the
     * selected property of this component is set to true and the row is 
     * currently hidden from view.
     * <p>
     * Note: UI guidelines recomend that rows should be unselected when no
     * longer in view. For example, when a user selects rows of the table and 
     * navigates to another page. Or, when a user applies a filter or sort that
     * may hide previously selected rows from view. If a user invokes an action
     * to delete the currently selected rows, they may inadvertently remove rows
     * not displayed on the current page. That said, there are cases when
     * maintaining state across table pages is necessary. When maintaining state
     * and there are currently no hidden selections, UI guidelines recomend that
     * the number zero should be shown.
     * </p><p>
     * Note: This count depends on the FilterCriteria and SortCriteria objects
     * provided to the TableDataFilter and TableDataSorter instances used by
     * this component. If TableDataFilter and TableFilterSorter are modified
     * directly, invoke the clearFilter method to clear the previous filter and
     * sort.
     * </p>
     * @return The number of selected rows currently hidden from view.
     */
    public int getHiddenSelectedRowsCount() {
        RowKey[] rowKeys = getHiddenSelectedRowKeys();
        return (rowKeys != null) ? rowKeys.length : 0;
    }

    /**
     * Get an array of RowKey objects from the underlying data provider where
     * the selected property of this component is set to true and the row is
     * currently hidden from view.
     * <p>
     * Note: UI guidelines recomend that rows should be unselected when no
     * longer in view. For example, when a user selects rows of the table and 
     * navigates to another page. Or, when a user applies a filter or sort that
     * may hide previously selected rows from view. If a user invokes an action
     * to delete the currently selected rows, they may inadvertently remove rows
     * not displayed on the current page.
     * </p><p>
     * Note: The returned RowKey objects depend on the FilterCriteria and 
     * SortCriteria objects provided to the TableDataFilter and TableDataSorter
     * instances used by this component. If TableDataFilter and TableDataSorter
     * are modified directly, invoke the clearSort and clearFilter method to
     * clear the previous sort and filter.
     * </p>
     * @return An array of RowKey objects.
     */
    public RowKey[] getHiddenSelectedRowKeys() {
        // Get hidden RowKey objects.
        RowKey[] rowKeys = getHiddenRowKeys();
        if (rowKeys == null) {
            return rowKeys;
        }  

        // Save the current RowKey.
        RowKey rowKey = getRowKey();

        // Find the number of selected rows hidden from view.
        ArrayList list = new ArrayList();
        for (int i = 0; i < rowKeys.length; i++) {
            setRowKey(rowKeys[i]);
            if (isRowAvailable() && isSelected()) {
                list.add(rowKeys[i]);
            }
        }
        setRowKey(rowKey); // Restore the current RowKey.
        rowKeys = new RowKey[list.size()];
        return (RowKey[]) list.toArray(rowKeys);
    }

    /**
     * Get the number of selected rows from the underlying data provider where
     * the selected property of this component is set to true.
     * <p>
     * Note: This count depends on the FilterCriteria objects provided to the
     * TableDataFilter instance used by this component. If TableDataFilter is
     * modified directly, invoke the clearFilter method to clear the previous
     * filter.
     * </p>
     * @return The number of selected rows.
     */
    public int getSelectedRowsCount() {
        RowKey[] rowKeys = getSelectedRowKeys();
        return (rowKeys != null) ? rowKeys.length : 0;
    }

    /**
     * Get an array of RowKey objects from the underlying data provider where
     * the selected property of this component is set to true.
     * <p>
     * Note: The returned RowKey objects depend on the FilterCriteria objects 
     * provided to the TableDataFilter instance used by this component. If 
     * TableDataFilter is modified directly, invoke the clearFilter method to
     * clear the previous filter.
     * </p>
     * @return An array of RowKey objects.
     */
    public RowKey[] getSelectedRowKeys() {
        // Get filtered RowKey objects.
        RowKey[] rowKeys = getFilteredRowKeys();
        if (rowKeys == null) {
            return rowKeys;
        }

        // Save the current RowKey.
        RowKey rowKey = getRowKey();
        
        // Find the number of selected rows.
        ArrayList list = new ArrayList();
        for (int i = 0; i < rowKeys.length; i++) {
            setRowKey(rowKeys[i]);
            if (isRowAvailable() && isSelected()) {
                list.add(rowKeys[i]);
            }
        }
        setRowKey(rowKey); // Restore the current RowKey.
        rowKeys = new RowKey[list.size()];
        return (RowKey[]) list.toArray(rowKeys);
    }

    /**
     * Get the number of objects from the underlying data provider where the
     * selected property of this component is set to true and the row is 
     * rendered.
     * <p>
     * Note: UI guidelines recomend that rows should be unselected when no
     * longer in view. For example, when a user selects rows of the table and 
     * navigates to another page. Or, when a user applies a filter or sort that
     * may hide previously selected rows from view. If a user invokes an action
     * to delete the currently selected rows, they may inadvertently remove rows
     * not displayed on the current page.
     * </p><p>
     * Note: This count depends on the FilterCriteria and SortCriteria objects
     * provided to the TableDataFilter and TableDataSorter instances used by
     * this component. If TableDataFilter and TableFilterSorter are modified
     * directly, invoke the clearFilter method to clear the previous filter and
     * sort.
     * </p>
     * @return The number of selected rows currently hidden from view.
     */
    public int getRenderedSelectedRowsCount() {
        RowKey[] rowKeys = getRenderedSelectedRowKeys();
        return (rowKeys != null) ? rowKeys.length : 0;
    }

    /**
     * Get an array of RowKey objects from the underlying data provider where
     * the selected property of this component is set to true and the row is
     * rendered.
     * <p>
     * Note: UI guidelines recomend that rows should be unselected when no
     * longer in view. For example, when a user selects rows of the table and 
     * navigates to another page. Or, when a user applies a filter or sort that
     * may hide previously selected rows from view. If a user invokes an action
     * to delete the currently selected rows, they may inadvertently remove rows
     * not displayed on the current page.
     * </p><p>
     * Note: The returned RowKey objects depend on the FilterCriteria and 
     * SortCriteria objects provided to the TableDataFilter and TableDataSorter
     * instances used by this component. If TableDataFilter and TableDataSorter
     * are modified directly, invoke the clearSort and clearFilter method to
     * clear the previous sort and filter.
     * </p>
     * @return An array of RowKey objects.
     */
    public RowKey[] getRenderedSelectedRowKeys() {
        // Get rendered RowKey objects.
        RowKey[] rowKeys = getRenderedRowKeys();
        if (rowKeys == null) {
            return rowKeys;
        }  

        // Save the current RowKey.
        RowKey rowKey = getRowKey();

        // Find the number of selected rows in view.
        ArrayList list = new ArrayList();
        for (int i = 0; i < rowKeys.length; i++) {
            setRowKey(rowKeys[i]);
            if (isRowAvailable() && isSelected()) {
                list.add(rowKeys[i]);
            }
        }
        setRowKey(rowKey); // Restore the current RowKey.
        rowKeys = new RowKey[list.size()];
        return (RowKey[]) list.toArray(rowKeys);
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Sort methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Add a SortCriteria object to sort.
     * <p>
     * Note: Objects are sorted in the reverse order they were added. For 
     * example, the first object added, will be the last sort applied as the 
     * primary sort. The second object added, will be the second to last sort
     * applied as the secondary sort. The third object added, will be the third
     * to last sort applied as the tertiary sort and so on. If an existing
     * SortCriteria object is found with the same FieldKey, the sort order is
     * replaced with the new value. Note that sorts are not actually applied
     * until the getSortedRowKeys() method is invoked, which happens
     * automatically by the renderer.
     * </p><p>
     * Note: This method also resets pagination to the first page per UI 
     * guidelines.
     * </p>
     * @param criteria The SortCriteria object to sort.
     */
    public void addSort(SortCriteria criteria) {
        if (criteria == null) {
            return;
        }

        TableDataSorter sorter = getTableDataSorter();
        SortCriteria[] oldCriteria = sorter.getSortCriteria();

        // Iterate over each SortCriteria object and check for a match.
        if (oldCriteria != null) {
            for (int i = 0; i < oldCriteria.length; i++) {
                if (oldCriteria[i] == null) {
                    continue;
                }
                String key = oldCriteria[i].getCriteriaKey();
                if (key != null && key.equals(criteria.getCriteriaKey())) {
                    oldCriteria[i] = criteria;
                    return; // No further processing is required.
                }
            }
        }

        // Create array to hold new criteria.
        int oldLength = (oldCriteria != null) ? oldCriteria.length : 0;
        SortCriteria[] newCriteria = new SortCriteria[oldLength + 1];
        for (int i = 0; i < oldLength; i++) {
            newCriteria[i] = oldCriteria[i];
        }

        // Add new SortCriteria object.
        newCriteria[oldLength] = criteria;
        sorter.setSortCriteria(newCriteria); // Set new SortCriteria.
        setFirst(0); // Reset to first page.

        // Clear properties cached in request map.
        Properties properties = getProperties();
        if (properties != null) {
            properties.setSortedRowKeys(null);
        } else {
            log("addSort", "Cannot clear sorted row keys, Properties is null"); //NOI18N
        }
    }

    /**
     * Clear SortCriteria objects from the TableDataSorter instance used by this
     * component. 
     * <p>
     * Note: This method clears the cached sort, then resets pagination to the
     * first page per UI guidelines.
     * </p>
     */
    public void clearSort() {
        getTableDataSorter().setSortCriteria(null); // Clear all SortCriteria.
        setFirst(0); // Reset to first page.

        // Clear properties cached in request map.
        Properties properties = getProperties();
        if (properties != null) {
            properties.setSortedRowKeys(null);
        } else {
            log("clearSort", "Cannot clear sorted row keys, Properties is null"); //NOI18N
        }
    }

    /**
     * Get the number of SortCriteria objects to sort.
     *
     * @return The number of SortCriteria objects to sort.
     */
    public int getSortCount() {
        int result = 0;
        SortCriteria[] sortCriteria = getTableDataSorter().getSortCriteria();
        if (sortCriteria != null) {
            result = sortCriteria.length;
        }
        return result;
    }

    /**
     * Get the level of the given SortCriteria object to sort.
     * <p>
     * Note: The primary sort is level 1, the secondary sort is level 2, the 
     * tertiary sort is level 3, and so on. If the SortCriteria 
     * object was not previously added using the addSort method, the level will
     * be returned as -1.
     * </p>
     * @param criteria The SortCriteria object to sort.
     * @return The sort level or -1 if the SortCriteria object was not 
     * previously added.
     */
    public int getSortLevel(SortCriteria criteria) {
        int result = -1;
        if (criteria == null) {
            return result;
        }

        // Iterate over each SortCriteria object and check for a match.
        SortCriteria[] sortCriteria = getTableDataSorter().getSortCriteria();
        if (sortCriteria != null) {
            for (int i = 0; i < sortCriteria.length; i++) {
                if (sortCriteria[i] == null) {
                    continue;
                }
                String key = sortCriteria[i].getCriteriaKey();
                if (key != null && key.equals(criteria.getCriteriaKey())) {
                    result = i + 1;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Test if given SortCriteria object is a descending sort.
     *
     * @param criteria The SortCriteria object to sort.
     * @return true if descending, else false.
     */
    public boolean isDescendingSort(SortCriteria criteria) {
        boolean result = false;
        if (criteria == null) {
            return result;
        }

        // Iterate over each SortCriteria object and check for a match.
        SortCriteria[] sortCriteria = getTableDataSorter().getSortCriteria();
        if (sortCriteria != null) {
            for (int i = 0; i < sortCriteria.length; i++) {
                if (sortCriteria[i] == null) {
                    continue;
                }
                String key = sortCriteria[i].getCriteriaKey();
                if (key != null && key.equals(criteria.getCriteriaKey())) {
                    // Note: SortCriteria tests ascending instead of descending.
                    result = !sortCriteria[i].isAscending();
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Get an array containing sorted RowKey objects.
     * <p>
     * Note: This sort depends on the SortCriteria objects provided to the
     * TableDataSorter instance used by this component. For better performance,
     * this sort also depends on the FilterCriteria objects provided to the
     * TableDataFilter instance used by this component. Due to filtering, the
     * size of the returned array may be less than the total number of RowKey
     * objects for the underlying TableDataProvider.
     * </p><p>
     * Note: The returned RowKey objects are cached. If the TableDataSorter and
     * TableDataFilter instances used by this component are modified directly,
     * invoke the clearSort and clearFilter methods to clear the previous sort
     * and filter.
     * </p>
     * @return An array containing sorted RowKey objects.
     */
    public RowKey[] getSortedRowKeys() {
        // Get properties cached in request map.
        Properties properties = getProperties();
        RowKey[] sortedRowKeys = (properties != null)
            ? properties.getSortedRowKeys() : null;

        // Initialize RowKey objects, if not cached already.
        if (sortedRowKeys != null) {
            return sortedRowKeys;
        } else {
            sortedRowKeys = getFilteredRowKeys();
        }

        // Do not attempt to sort with a null provider. BasicTableDataSorter
        // throws NullPointerException -- bugtraq id #6268451.
        TableDataProvider provider = getTableRowDataProvider().
            getTableDataProvider();
        if (provider == null) {
            log("getSortedRowKeys", //NOI18N
                "Cannot obtain sorted row keys, TableDataProvider is null"); //NOI18N
            return sortedRowKeys;
        }

        // If TableDataSorter and TableDataProvider are the same instance, the
        // sort method is never called. The sort order is assumed to be
        // intrinsic in the row order of the TableDataProvider.
        TableDataSorter sorter = getTableDataSorter();
        if (provider != sorter) {
            sortedRowKeys = sorter.sort(provider, sortedRowKeys);
        }

        // Save properties.
        if (properties != null) {
            properties.setSortedRowKeys(sortedRowKeys);
        } else {
            log("getSortedRowKeys", //NOI18N
                "Cannot save sorted row keys, Properties is null"); //NOI18N
        }
        return sortedRowKeys;
    }

    /**
     * Get the TableDataSorter object used to sort rows.
     *
     * @return The TableDataSorter object used to sort rows.
     */
    public TableDataSorter getTableDataSorter() {
        // Method is overriden because TableDataSorter is not serializable.
        TableDataSorter tds = super.getTableDataSorter();
        if (tds != null) {
            return tds;
        }

        // Get default sorter.
        if (sorter == null) {
            sorter = new BasicTableDataSorter();
        }
        return sorter;
    }

    /**
     * Set the TableDataSorter object used to sort rows.
     *
     * @param sorter The TableDataSorter object used to sort rows.
     */
    public void setTableDataSorter(TableDataSorter sorter) {
        // Method is overriden because TableDataSorter is not serializable.
        this.sorter = sorter;
    }

    /**
     * Set SortCriteria objects for the TableDataSorter instance used by this
     * component. 
     * <p>
     * Note: This method clears the cached sort, then resets pagination to the 
     * first page per UI guidelines.
     * </p>
     * @param sortCriteria An array of SortCriteria objects defining the sort
     * order on this TableDataSorter.
     */
    public void setSortCriteria(SortCriteria[] sortCriteria) {
        clearSort();
        getTableDataSorter().setSortCriteria(sortCriteria);
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // State methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Restore the state of this component.
     */
    public void restoreState(FacesContext context, Object state) {
        Object values[] = (Object[]) state;
        super.restoreState(context, values[0]);
        saved = (Map) values[1];
        setPaginated(((Boolean) values[2]).booleanValue());

        // Note: When the iterate method is called (during the decode, validate,
        // update phases), the previously displayed sort must be used to iterate
        // over the previously displayed children. If child values have changed
        // (e.g., TableSelectPhaseListener has cleared checkbox state after the
        // rendering phase), a new sort would not represent the same rows and
        // state may be lost. Thus, we must restore the previously sorted RowKey
        // objects.

        // Restore SortCriteria.
        TableDataSorter sorter = getTableDataSorter();
        sorter.setSortCriteria((SortCriteria[]) values[3]);

        // Restore FilterCriteria.
        TableDataFilter filter = getTableDataFilter();
        filter.setFilterCriteria((FilterCriteria[]) values[4]);

        // Restore previously filtered and sorted RowKey objects.
        Properties properties = getProperties();
        if (properties != null) {
            properties.setFilteredRowKeys((RowKey[]) values[5]);
            properties.setSortedRowKeys((RowKey[]) values[6]);
        } else {
            log("restoreState", //NOI18N
                "Cannot save sorted and filtered row keys, Properties is null"); //NOI18N
        }
    }

    /**
     * Save the state of this component.
     *
     * @return An array of Object values.
     */
    public Object saveState(FacesContext context) {
        Object values[] = new Object[8];
        values[0] = super.saveState(context);
        values[1] = saved;
        values[2] = isPaginated() ? Boolean.TRUE : Boolean.FALSE;
        values[3] = getTableDataSorter().getSortCriteria(); // Save SortCriteria.
        values[4] = getTableDataFilter().getFilterCriteria(); // Save FilterCriteria.
        values[5] = getFilteredRowKeys(); // Save filtered RowKey objects.
        values[6] = getSortedRowKeys(); // Save sorted RowKey objects.
        return values;
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // UIComponent methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Set the ValueBinding used to calculate the value for the specified
     * attribute or property name, if any.  In addition, if a ValueBinding is
     * set for the value property, remove any synthesized TableDataProvider for
     * the data previously bound to this component.
     *
     * @param name Name of the attribute or property for which to set a
     * ValueBinding.
     * @param binding The ValueBinding to set, or null to remove any currently
     * set ValueBinding.
     *
     * @exception IllegalArgumentException If name is one of sourceVar.
     * @exception NullPointerException If name is null.
     */
    public void setValueBinding(String name, ValueBinding binding) {
        if ("sourceData".equals(name)) { //NOI18N
            init();
        } else if ("sourceVar".equals(name)) { //NOI18N
            log("setValueBinding", "sourceVar cannot equal given name"); //NOI18N
            throw new IllegalArgumentException();
        }
        super.setValueBinding(name, binding);
    }

    /**
     * Return a client identifier for this component that includes the current
     * value of the RowKey property, if it is not set to null. This implies that
     * multiple calls to getClientId() may return different results, but ensures
     * that child components can themselves generate row-specific client
     * identifiers (since TableRowGroup is a NamingContainer).
     *
     * @exception NullPointerException if FacesContext is null.
     * @return The client id.
     */
    public String getClientId(FacesContext context) {
        if (context == null) {
            log("getClientId", "Cannot obtain client Id, FacesContext is null"); //NOI18N
            throw new NullPointerException();
        }

        String baseClientId = super.getClientId(context);
        if (getRowKey() != null) {
            return (baseClientId + NamingContainer.SEPARATOR_CHAR +
                getRowKey().getRowId());
        } else {
            return (baseClientId);
        }
    }

    /**
     * Override the default UIComponentBase.queueEvent() processing to wrap any
     * queued events in a wrapper so that we can reset the current RowKey in
     * broadcast().
     *
     * @param event FacesEvent to be queued.
     *
     * @exception IllegalStateException If this component is not a descendant
     * of a UIViewRoot.
     * @exception NullPointerException If FacesEvent is null.
     */
    public void queueEvent(FacesEvent event) {
        super.queueEvent(new WrapperEvent(this, event, getRowKey()));
    }

    /**
     * Override the default UIComponentBase.broadcast() processing to unwrap any
     * wrapped FacesEvent and reset the current RowKey, before the event is
     * actually broadcast. For events that we did not wrap (in queueEvent()), 
     * default processing will occur.
     *
     * @param event The FacesEvent to be broadcast.
     *
     * @exception AbortProcessingException Signal the JavaServer Faces
     * implementation that no further processing on the current event
     * should be performed.
     * @exception IllegalArgumentException if the implementation class
     * of this FacesEvent is not supported by this component.
     * @exception NullPointerException if FacesEvent is null.
     */
    public void broadcast(FacesEvent event) throws AbortProcessingException {
        if (!(event instanceof WrapperEvent)) {
            super.broadcast(event);
            return;
        }

        // Set up the correct context and fire our wrapped event
        WrapperEvent revent = (WrapperEvent) event;
        RowKey oldRowKey = getRowKey();
        setRowKey(revent.getRowKey());
        FacesEvent rowEvent = revent.getFacesEvent();
        rowEvent.getComponent().broadcast(rowEvent);
        setRowKey(oldRowKey);
        return;
    }

    /**
     * In addition to the default behavior, ensure that any saved per-row state
     * for our child input components is discarded unless it is needed to
     * rerender the current page with errors.
     *
     * @param context FacesContext for the current request.
     *
     * @exception IOException if an input/output error occurs while rendering.
     * @exception NullPointerException if FacesContext is null.
     */
    public void encodeBegin(FacesContext context) throws IOException {
        // Clear objects cached during the decode, validate, and update phases
        // so nested tables can render new TableDataProvider objects.
        if (isNestedWithinTableRowGroup()) {
            init();
        }
        if (!keepSaved(context)) {
            saved = new HashMap();
        }
        super.encodeBegin(context);
    }

    /**
     * Override the default UIComponentBase.processDecodes() processing to
     * perform the following steps.
     *
     * <ul>
     * <li>If the rendered property of this UIComponent is false, skip further
     *     processing.</li>
     * <li>Set the current RowKey to null.</li>
     * <li>Call the processDecodes() method of all facets of this TableRowGroup,
     *     in the order determined by a call to getFacets().keySet().iterator().</li>
     * <li>Call the processDecodes() method of all facets of the TableColumn
     *     children of this TableRowGroup.</li>
     * <li>Iterate over the set of rows that were included when this component
     *     was rendered (i.e. those defined by the first and rows properties),
     *     performing the following processing for each row:</li>
     * <li>Set the current RowKey to the appropriate value for this row.</li>
     * <li>If isRowAvailable() returns true, iterate over the children
     *     components of each TableColumn child of this TableRowGroup component,
     *     calling the processDecodes() method for each such child.</li>
     * <li>Set the current RowKey to null.</li>
     * <li>Call the decode() method of this component.</li>
     * <li>If a RuntimeException is thrown during decode processing, call 
     *    FacesContext.renderResponse() and re-throw the exception.</li>
     * </ul>
     *
     * @param context FacesContext for the current request.
     *
     * @exception NullPointerException if FacesContext is null.
     */
    public void processDecodes(FacesContext context) {
        if (context == null) {
            log("processDecodes", "Cannot decode, FacesContext is null"); //NOI18N
            throw new NullPointerException();
        }
        if (!isRendered()) {
            log("processDecodes", "Component not rendered, nothing to decode"); //NOI18N
            return;
        }       
        if (saved == null || !keepSaved(context)) {
            saved = new HashMap(); // We don't need saved state here
        }
        iterate(context, PhaseId.APPLY_REQUEST_VALUES);
        decode(context);
    }

    /**
     * Override the default UIComponentBase.processValidators() processing to
     * perform the following steps.
     *
     * <ul>
     * <li>If the rendered property of this UIComponent is false, skip further
     *     processing.</li>
     * <li>Set the current RowKey to null.</li>
     * <li>Call the processValidators() method of all facets of this
     *     TableRowGroup, in the order determined by a call to 
     *     getFacets().keySet().iterator().</li>
     * <li>Call the processValidators() method of all facets of the TableColumn
     *     children of this TableRowGroup.</li>
     * <li>Iterate over the set of rows that were included when this component
     *     was rendered (i.e. those defined by the first and rows properties),
     *     performing the following processing for each row:</li>
     * <li>Set the current RowKey to the appropriate value for this row.</li>
     * <li>If isRowAvailable() returns true, iterate over the children
     *     components of each TableColumn child of this TableRowGroup component,
     *     calling the processValidators() method for each such child.</li>
     * <li>Set the current RowKey to null.</li>
     * </ul>
     *
     * @param context FacesContext for the current request.
     *
     * @exception NullPointerException if FacesContext is null.
     */
    public void processValidators(FacesContext context) {
        if (context == null) {
            log("processValidators", "Cannot validate, FacesContext is null"); //NOI18N
            throw new NullPointerException();
        }
        if (!isRendered()) {
            log("processValidators", //NOI18N
                "Component not rendered, nothing to validate"); //NOI18N
            return;
        }
        iterate(context, PhaseId.PROCESS_VALIDATIONS);
        // This is not a EditableValueHolder, so no further processing is required
    }

    /**
     * Override the default UIComponentBase.processUpdates() processing to
     * perform the following steps.
     *
     * <ul>
     * <li>If the rendered property of this UIComponent is false, skip further
     *     processing.</li>
     * <li>Set the current RowKey to null.</li>
     * <li>Call the processUpdates() method of all facets of this TableRowGroup,
     *     in the order determined by a call to getFacets().keySet().iterator().</li>
     * <li>Call the processUpdates() method of all facets of the TableColumn
     *     children of this TableRowGroup.</li>
     * <li>Iterate over the set of rows that were included when this component
     *     was rendered (i.e. those defined by the first and rows properties),
     *     performing the following processing for each row:</li>
     * <li>Set the current RowKey to the appropriate value for this row.</li>
     * <li>If isRowAvailable() returns true, iterate over the children
     *     components of each TableColumn child of this TableRowGroup component,
     *     calling the processUpdates() method for each such child.</li>
     * <li>Set the current RowKey to null.</li>
     * </ul>
     *
     * @param context FacesContext for the current request.
     *
     * @exception NullPointerException if FacesContext is null.
     */
    public void processUpdates(FacesContext context) {
        if (context == null) {
            log("processUpdates", "Cannot update, FacesContext is null"); //NOI18N
            throw new NullPointerException();
        }
        if (!isRendered()) {
            log("processUpdates", "Component not rendered, nothing to update"); //NOI18N
            return;
        }
        iterate(context, PhaseId.UPDATE_MODEL_VALUES);

        // Set collapsed property applied client-side.
        UIComponent header = getFacet(GROUP_HEADER_ID);
        UIComponent field = (header != null)
            ? (UIComponent) header.getFacets().get(
                TableHeader.COLLAPSED_HIDDEN_FIELD_ID)
            : null;
        if (field instanceof HiddenField) {
            Boolean value = (field != null) 
                ? (Boolean) ((HiddenField) field).getValue() : null;
            setCollapsed(value.booleanValue());
        } else {
            log("processUpdates", "Cannot obtain collapsed hidden field value"); //NOI18N
        }
        // This is not a EditableValueHolder, so no further processing is required
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
        // Get Table ancestor.
        Table table = getTableAncestor();
        if (table == null) {
            log("getProperties", "Cannot obtain Properties, Table is null"); //NOI18N
            return null;
        }

        // Get properties for all components.
        FacesContext context = getFacesContext();
        Map requestMap = context.getExternalContext().getRequestMap();
        String propertiesMapId = REQUEST_KEY_PREFIX + 
            table.getClientId(context) + PROPERTIES;
        Map propertiesMap = (Map) requestMap.get(propertiesMapId);
        if (propertiesMap == null) {
            propertiesMap = new HashMap();
            requestMap.put(propertiesMapId, propertiesMap);
        }

        // Get properties for this component.
        String propertiesId = super.getClientId(context); // Don't append row ID.
        Properties properties = (Properties) propertiesMap.get(propertiesId);
        if (properties == null) {
            properties = new Properties();
            propertiesMap.put(propertiesId, properties);
        }

        return properties;
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
     * Initialize member variables.
     * <p>
     * Note: When ever a new DataProvider is used, UI Guiedlines recommend that
     * pagination should be reset (e.g., remaining on the 4th page of a new set
     * of data makes no sense). However, properties such as the sort and filter 
     * criteria should not automatically be cleared (e.g., there may be 
     * situations where one or both should be left as specified by the user). In
     * this scenario, pagination is set to the first page.
     * </p><p>
     * Note: When ever the underlying DataProvider has changed, cached 
     * properties must be re-evaluated even with server-side state saving -- 
     * bugtraq #6304818.
     * </p>
     */
    private void init() {
        setFirst(0); // Reset to first page.

        // Get Table ancestor.
        Table table = getTableAncestor();
        if (table == null) {
            log("init", "Cannot initialize Properties, Table is null"); //NOI18N
            return;
        }

        // Get properties for all components.
        FacesContext context = getFacesContext();
        Map requestMap = context.getExternalContext().getRequestMap();
        String propertiesId = REQUEST_KEY_PREFIX + table.getClientId(context) + 
            PROPERTIES;
        Map propertiesMap = (Map) requestMap.get(propertiesId);
        
        // Clear all properties cached in request map for this component.
        if (propertiesMap != null) {
            propertiesMap.put(super.getClientId(context), null);
        } else {
            log("init", //NOI18N
                "Cannot initialize Properties, request properties map is null"); //NOI18N
        }
    }

    /**
     * Helper method to determine if this component is nested within another
     * TableRowGroup component.
     *
     * @return true if this component is nested, else false.
     */
    private boolean isNestedWithinTableRowGroup() {
        UIComponent parent = this;
        while (null != (parent = parent.getParent())) {
            if (parent instanceof TableRowGroup) {
                return true;
            }
        }
        return (false);
    }

    /**
     * Helper method to perform the appropriate phase-specific processing and
     * per-row iteration for the specified phase, as follows:
     *
     * <ul>
     * <li>Set the RowKey property to null, and process the facets
     *     of this TableRowGroup component exactly once.</li>
     * <li>Set the RowKey property to null, and process the facets
     *     of the TableColumn children of this TableRowGroup component exactly
     *     once.</li>
     * <li>Iterate over the relevant rows, based on the first and row
     *     properties, and process the children of the TableColumn children of
     *     this TableRowGroup component once per row.</li>
     * </ul>
     *
     * @param context FacesContext for the current request.
     * @param phaseId PhaseId of the phase we are currently running.
     */
    private void iterate(FacesContext context, PhaseId phaseId) {
        // Note: When the iterate method is called via the processDecode,
        // processValidate, and processUpdate methods), the previously displayed
        // sort must be used to iterate over the previously displayed children.
        // (The previously displayed sort is cached/restored via the 
        // save/restoreState methods.) If child values have changed (e.g., 
        // TableSelectPhaseListener has cleared checkbox state after the 
        // rendering phase), obtaining a new sort here may not represent the 
        // same rows and state may be lost. Thus, don't clear cached properties
        // unless nested.
        if (isNestedWithinTableRowGroup()) {
            // Re-evaluate even with server-side state saving.
            init();
        }

        // Process each facet of this component exactly once.
        setRowKey(null);
        Iterator facets = getFacets().keySet().iterator(); // Get facet keys.
        while (facets.hasNext()) {
            // Get facet.
            UIComponent facet = (UIComponent) getFacets().get(facets.next());
            if (phaseId == PhaseId.APPLY_REQUEST_VALUES) {
                facet.processDecodes(context);
            } else if (phaseId == PhaseId.PROCESS_VALIDATIONS) {
                facet.processValidators(context);
            } else if (phaseId == PhaseId.UPDATE_MODEL_VALUES) {
                facet.processUpdates(context);
            } else {
                log("iterate", //NOI18N
                    "Cannot process component facets, Invalid phase ID"); //NOI18N
                throw new IllegalArgumentException();
            }
        }

        // Process the facet of each TableColumn child exactly once.
        setRowKey(null);
        Iterator kids = getTableColumnChildren();
        while (kids.hasNext()) {
            TableColumn kid = (TableColumn) kids.next();
            if (!kid.isRendered()) {
                log("iterate", //NOI18N
                    "Cannot process TableColumn facets, TableColumn not rendered"); //NOI18N
                continue;
            }
            iterateTableColumnFacets(context, kid, phaseId);
        }

        // Get rendered row keys.
        RowKey[] rowKeys = getRenderedRowKeys();
        if (rowKeys == null) {
            log("iterate", //NOI18N
                "Cannot iterate over TableColumn children, RowKey array is null"); //NOI18N
            return;
        }

        // Iterate over the sorted, rendered RowKey objects.
        for (int i = 0; i < rowKeys.length; i++) {
            setRowKey(rowKeys[i]);
            if (!isRowAvailable()) {
                log("iterate", //NOI18N
                    "Cannot iterate over TableColumn children, row not available"); //NOI18N
                break;
            }

            // Perform phase-specific processing as required on the children
            // of the TableColumn (facets have been done a single time with
            // setRowKey(null) already)
            kids = getTableColumnChildren();
            while (kids.hasNext()) {
                TableColumn kid = (TableColumn) kids.next();
                if (!kid.isRendered()) {
                    log("iterate", "Cannot process TableColumn, not rendered"); //NOI18N
                    continue;
                }
                Iterator grandkids = kid.getChildren().iterator();
                while (grandkids.hasNext()) {
                    UIComponent grandkid = (UIComponent) grandkids.next();
                    if (!grandkid.isRendered()) {
                        log("iterate", //NOI18N
                            "Cannot process TableColumn child, not rendered"); //NOI18N
                        continue;
                    }
                    iterateTableColumnChildren(context, grandkid, phaseId);
                }
            }
        }
        setRowKey(null); // Clean up after ourselves.
    }

    /**
     * Helper method to iterate over nested TableColumn facets.
     *
     * @param context FacesContext for the current request.
     * @param component The TableColumn component to be rendered.
     * @param phaseId PhaseId of the phase we are currently running.
     */
    private void iterateTableColumnFacets(FacesContext context,
            TableColumn component, PhaseId phaseId) {
        if (component == null) {
            log("iterateTableColumnFacets", //NOI18N
                "Cannot iterate over TableColumn facets, TableColumn is null"); //NOI18N
            return;
        }

        Iterator kids = component.getTableColumnChildren();
        if (kids.hasNext()) {
            while (kids.hasNext()) {
                TableColumn col = (TableColumn) kids.next();
                iterateTableColumnFacets(context, col, phaseId);
            }
        } else {
            // Get facet keys.
            Iterator facets = component.getFacets().keySet().iterator();
            while (facets.hasNext()) {
                // Get facet.
                UIComponent facet = (UIComponent) component.getFacets().get(
                    facets.next());
                if (phaseId == PhaseId.APPLY_REQUEST_VALUES) {
                    facet.processDecodes(context);
                } else if (phaseId == PhaseId.PROCESS_VALIDATIONS) {
                    facet.processValidators(context);
                } else if (phaseId == PhaseId.UPDATE_MODEL_VALUES) {
                    facet.processUpdates(context);
                } else {
                    log("iterateTableColumnFacets", //NOI18N
                        "Cannot iterate over TableColumn facets, Invalid phase ID"); //NOI18N
                    throw new IllegalArgumentException();
                }
            }
        }
    }

    /**
     * Helper method to iterate over nested TableColumn children.
     *
     * @param context FacesContext for the current request.
     * @param component The TableColumn component to be rendered.
     * @param phaseId PhaseId of the phase we are currently running.
     */
    private void iterateTableColumnChildren(FacesContext context,
            UIComponent component, PhaseId phaseId) {
        if (component == null) {
            log("iterateTableColumnChildren", //NOI18N
                "Cannot iterate over TableColumn children, UIComponent is null"); //NOI18N
            return;
        }

        // Do not process nested TableColumn components so facets will not be 
        // decoded for each row of the table.
        if (component instanceof TableColumn) {
            Iterator kids = component.getChildren().iterator();
            if (kids.hasNext()) {
                while (kids.hasNext()) {
                    UIComponent kid = (UIComponent) kids.next();
                    iterateTableColumnChildren(context, kid, phaseId);
                }
            }
        } else {
            if (phaseId == PhaseId.APPLY_REQUEST_VALUES) {
                component.processDecodes(context);
            } else if (phaseId == PhaseId.PROCESS_VALIDATIONS) {
                component.processValidators(context);
            } else if (phaseId == PhaseId.UPDATE_MODEL_VALUES) {
                component.processUpdates(context);
            } else {
                log("iterateTableColumnChildren", //NOI18N
                    "Cannot iterate over TableColumn children, Invalid phase ID"); //NOI18N
                throw new IllegalArgumentException();
            }
        }
    }

    /**
     * Helper method to get flag indicating that we need to keep the saved 
     * per-child state information. This will be the case if any of the
     * following are true:
     *
     * <ul>
     * <li>Any of the saved state corresponds to components that have messages
     *     that must be displayed.</li>
     * <li>This TableRowGroup instance is nested inside of another TableRowGroup
     *     instance.</li>
     * </ul>
     *
     * @param context FacesContext for the current request.
     * @return true if state should be saved, else false.
     */
    private boolean keepSaved(FacesContext context) {
        Iterator clientIds = saved.keySet().iterator();
        while (clientIds.hasNext()) {
            String clientId = (String) clientIds.next();

            // Fix for immediate property -- see bugtraq #6269737.
            SavedState state = (SavedState) saved.get(clientId);
            if (state != null && state.getSubmittedValue() != null) {
                return (true);
            }
        }
        //<RAVE>
        //bug 6377769 -- check all messages for an error, not just the messages on EditableValueHolders within the TableRowGroup
        Iterator messages = context.getMessages();
        while (messages.hasNext()) {
            FacesMessage message = (FacesMessage) messages.next();
            if (message.getSeverity().
                    compareTo(FacesMessage.SEVERITY_ERROR) >= 0) {
                return (true);
            }
        }
        //</RAVE>
        return (isNestedWithinTableRowGroup());
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
     * Helper method to restore state information for all descendant components,
     * as described for setRowKey().
     */
    private void restoreDescendantState() {
        FacesContext context = getFacesContext();
        Iterator kids = getTableColumnChildren();
        while (kids.hasNext()) {
            TableColumn kid = (TableColumn) kids.next();
            if (!kid.isRendered()) {
                continue;
            }
            restoreDescendantState(kid, context);
        }
    }

    /**
     * Helper method to restore state information for the specified component
     * and its descendants.
     *
     * @param component Component for which to restore state information.
     * @param context FacesContext for the current request.
     */
    private void restoreDescendantState(UIComponent component,
            FacesContext context) {
        // Reset the client identifier for this component
        String id = component.getId();
        component.setId(id); // Forces client id to be reset

        // Restore state for this component (if it is a EditableValueHolder)
        if (component instanceof EditableValueHolder) {
            EditableValueHolder input = (EditableValueHolder) component;
            String clientId = component.getClientId(context);
            SavedState state = (SavedState) saved.get(clientId);
            if (state == null) {
                state = new SavedState();
            }
            input.setValue(state.getValue());
            input.setValid(state.isValid());
            input.setSubmittedValue(state.getSubmittedValue());
            // This *must* be set after the call to setValue(), since
            // calling setValue() always resets "localValueSet" to true.
            input.setLocalValueSet(state.isLocalValueSet());

	    ConversionUtilities.restoreRenderedValueState(context, component);
        }

        // Restore state for children of this component
        Iterator kids = component.getChildren().iterator();
        while (kids.hasNext()) {
            restoreDescendantState((UIComponent) kids.next(), context);
        }
    }

    /**
     * Helper method to save state information for all descendant components, as
     * described for setRowKey().
     */
    private void saveDescendantState() {
        FacesContext context = getFacesContext();
        Iterator kids = getTableColumnChildren();
        while (kids.hasNext()) {
            TableColumn kid = (TableColumn) kids.next();
            if (!kid.isRendered()) {
                log("saveDescendantState", //NOI18N
                    "Cannot save descendant state, TableColumn not rendered"); //NOI18N
                continue;
            }
            saveDescendantState(kid, context);
        }
    }

    /**
     * Helper method to save state information for the specified component and
     * its descendants.
     *
     * @param component Component for which to save state information.
     * @param context FacesContext for the current request.
     */
    private void saveDescendantState(UIComponent component,
        FacesContext context) {

        // Save state for this component (if it is a EditableValueHolder)
        if (component instanceof EditableValueHolder) {
            EditableValueHolder input = (EditableValueHolder) component;
            String clientId = component.getClientId(context);
            SavedState state = (SavedState) saved.get(clientId);
            if (state == null) {
                state = new SavedState();
                saved.put(clientId, state);
            }
            state.setValue(input.getLocalValue());
            state.setValid(input.isValid());
            state.setSubmittedValue(input.getSubmittedValue());
            state.setLocalValueSet(input.isLocalValueSet());

	    ConversionUtilities.saveRenderedValueState(context, component);
        }

        // Note: Don't bother logging messages here -- too many messages.
        // For example, staticText is not an EditableValueHolder.

        // Save state for children of this component
        Iterator kids = component.getChildren().iterator();
        while (kids.hasNext()) {
            saveDescendantState((UIComponent) kids.next(), context);
        }
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Inner classes
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Object used to cache properties in the request.
     */
    private class Properties {
        // The TableRowDataProvider associated with this component, lazily
        // instantiated if requested. This object is not part of the saved and
        // restored state of the component.
        private TableRowDataProvider provider = null;

        // Array containing currently filtered RowKey objects.
        private RowKey[] filteredRowKeys = null;

        // Array containing currently sorted RowKey objects. This sort will be 
        // cached and used to iterate over children during the decode, validate,
        // and update phases.
        private RowKey[] sortedRowKeys = null;

        // A List of TableColumn children found for this component.
        private List tableColumnChildren = null;

        // The number of columns to be rendered.
        private int columnCount = -1;

        /** Default constructor. */
        public Properties() {
        }

        /**
         * Get the number of columns found for this component that have a 
         * rendered property of true.
         *
         * @return The number of rendered columns.
         */
        public int getColumnCount() {
            return columnCount;
        }

        /**
         * Set the number of columns found for this component that have a
         * rendered property of true.
         *
         * @param columnCount The number of rendered columns.
         */
        public void setColumnCount(int columnCount) {
            this.columnCount = columnCount;
        }

        /**
         * Get an array containing filtered RowKey objects.
         *
         * @return An array containing filtered RowKey objects.
         */
        public RowKey[] getFilteredRowKeys() {
            return filteredRowKeys;
        }

        /**
         * Set an array containing filtered RowKey objects.
         *
         * @param filteredRowKeys An array containing filtered RowKey objects.
         */
        public void setFilteredRowKeys(RowKey[] filteredRowKeys) {
            this.filteredRowKeys = filteredRowKeys;
        }

        /**
         * Get an array containing sorted RowKey objects.
         *
         * @return An array containing sorted RowKey objects.
         */
        public RowKey[] getSortedRowKeys() {
            return sortedRowKeys;
        }

        /**
         * Set an array containing sorted RowKey objects.
         *
         * @param sortedRowKeys An array containing sorted RowKey objects.
         */
        public void setSortedRowKeys(RowKey[] sortedRowKeys) {
            this.sortedRowKeys = sortedRowKeys;
        }

        /**
         * Get the TableColumn children found for this component.
         *
         * @return The TableColumn children.
         */
        public List getTableColumnChildren() {
            return tableColumnChildren;
        }

        /**
         * Set the TableColumn children found for this component.
         *
         * @param tableColumnChildren The TableColumn children.
         */
        public void setTableColumnChildren(List tableColumnChildren) {
            this.tableColumnChildren = tableColumnChildren;
        }

        /**
         * Get the TableRowDataProvider object representing the data objects
         * that we will iterate over in this component's rendering.
         *
         * @return The TableRowDataProvider object.
         */
        public TableRowDataProvider getTableRowDataProvider() {
            return provider;
        }

        /**
         * Set the TableRowDataProvider object representing the data objects
         * that we will iterate over in this component's rendering.
         *
         * @return The TableRowDataProvider object.
         */
        public void setTableRowDataProvider(TableRowDataProvider provider) {
            this.provider = provider;
        }
    }
}

// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// Private classes
// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// Private class to represent saved state information.
class SavedState implements Serializable {
    private Object submittedValue;
    private boolean valid = true;
    private Object value;
    private boolean localValueSet;

    public Object getSubmittedValue() {
        return (this.submittedValue);
    }

    public void setSubmittedValue(Object submittedValue) {
        this.submittedValue = submittedValue;
    }

    public boolean isValid() {
        return (this.valid);
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public Object getValue() {
        return (this.value);
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public boolean isLocalValueSet() {
        return (this.localValueSet);
    }

    public void setLocalValueSet(boolean localValueSet) {
        this.localValueSet = localValueSet;
    }

    public String toString() {
        return ("submittedValue: " + submittedValue + " value: " + value + //NOI18N
            " localValueSet: " + localValueSet); //NOI18N
    }
}

// Private class to wrap an event with a RowKey.
class WrapperEvent extends FacesEvent {
    private FacesEvent event = null;
    private RowKey rowKey = null;

    public WrapperEvent(UIComponent component, FacesEvent event, RowKey rowKey) {
        super(component);
        this.event = event;
        this.rowKey = rowKey;
    }

    public FacesEvent getFacesEvent() {
        return (this.event);
    }

    public RowKey getRowKey() {
        return (this.rowKey);
    }

    public PhaseId getPhaseId() {
    return (this.event.getPhaseId());
    }

    public void setPhaseId(PhaseId phaseId) {
    this.event.setPhaseId(phaseId);
    }

    public boolean isAppropriateListener(FacesListener listener) {
        return (false);
    }

    public void processListener(FacesListener listener) {
        throw new IllegalStateException();
    }
}
