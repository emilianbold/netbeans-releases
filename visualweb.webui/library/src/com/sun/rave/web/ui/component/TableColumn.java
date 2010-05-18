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
import com.sun.data.provider.FieldKey;
import com.sun.data.provider.impl.FieldIdSortCriteria;
import com.sun.data.provider.TableDataProvider;
import com.sun.rave.web.ui.component.Hyperlink;
import com.sun.rave.web.ui.component.Icon;
import com.sun.rave.web.ui.component.IconHyperlink;
import com.sun.rave.web.ui.component.Label;
import com.sun.rave.web.ui.component.util.Util;
import com.sun.rave.web.ui.faces.ValueBindingSortCriteria;
import com.sun.rave.web.ui.event.TableSortActionListener;
import com.sun.rave.web.ui.theme.Theme;
import com.sun.rave.web.ui.theme.ThemeImages;
import com.sun.rave.web.ui.theme.ThemeStyles;
import com.sun.rave.web.ui.util.LogUtil;
import com.sun.rave.web.ui.util.ThemeUtilities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.faces.context.FacesContext;
import javax.faces.component.UIComponent;
import javax.faces.component.NamingContainer;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;

/**
 * Component that represents a table column.
 * <p>
 * The tableColumn component provides a layout mechanism for displaying columns 
 * of data. UI guidelines describe specific behavior that can applied to the 
 * rows and columns of data such as sorting, filtering, pagination, selection, 
 * and custom user actions. In addition, UI guidelines also define sections of 
 * the table that can be used for titles, row group headers, and placement of 
 * pre-defined and user defined actions.
 * </p><p>
 * Note: Column headers and footers are rendered by TableRowGroupRenderer. Table
 * column footers are rendered by TableRenderer.
 * </p><p>
 * Note: To see the messages logged by this class, set the following global
 * defaults in your JDK's "jre/lib/logging.properties" file.
 * </p><p><pre>
 * java.util.logging.ConsoleHandler.level = FINE
 * com.sun.rave.web.ui.component.TableColumn.level = FINE
 * </pre></p><p>
 * See TLD docs for more information.
 * </p>
 */
public class TableColumn extends TableColumnBase implements NamingContainer {
    /** The component id for the column footer. */
    public static final String COLUMN_FOOTER_ID = "_columnFooter"; //NOI18N

    /** The facet name for the column footer. */
    public static final String COLUMN_FOOTER_FACET = "columnFooter"; //NOI18N

    /** The component id for the column header. */
    public static final String COLUMN_HEADER_ID = "_columnHeader"; //NOI18N

    /** The facet name for the column header. */
    public static final String COLUMN_HEADER_FACET = "columnHeader"; //NOI18N

    /** The facet name for the header area. */
    public static final String HEADER_FACET = "header"; //NOI18N

    /** The component id for the embedded action separator icon. */
    public static final String EMBEDDED_ACTION_SEPARATOR_ICON_ID =
        "_embeddedActionSeparatorIcon"; //NOI18N

    /** The facet name for the embedded action separator icon. */
    public static final String EMBEDDED_ACTION_SEPARATOR_ICON_FACET =
        "embeddedActionSeparatorIcon"; //NOI18N

    /** The component id for the empty cell icon. */
    public static final String EMPTY_CELL_ICON_ID = "_emptyCellIcon"; //NOI18N

    /** The facet name for the empty cell icon. */
    public static final String EMPTY_CELL_ICON_FACET = "emptyCellIcon"; //NOI18N

    /** The facet name for the footer area. */
    public static final String FOOTER_FACET = "footer"; //NOI18N

    /** The component id for the table column footer. */
    public static final String TABLE_COLUMN_FOOTER_ID = "_tableColumnFooter"; //NOI18N

    /** The facet name for the table column footer. */
    public static final String TABLE_COLUMN_FOOTER_FACET = "tableColumnFooter"; //NOI18N

    /** The facet name for the table footer area. */
    public static final String TABLE_FOOTER_FACET = "tableFooter"; //NOI18N

    // Key prefix for properties cached in the request map.
    private static final String REQUEST_KEY_PREFIX = "com.sun.rave.web.ui_"; //NOI18N

    // Key for properties cached in the request map.
    private static final String PROPERTIES = "_properties"; //NOI18N

    // The Table ancestor enclosing this component.
    private Table table = null;

    /** Default constructor */
    public TableColumn() {
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
     * Get the closest TableColumn ancestor that encloses this component.
     *
     * @return The TableColumn ancestor.
     */
    public TableColumn getTableColumnAncestor() {
        // Get properties cached in request map.
        Properties properties = getProperties();
        TableColumn tableColumn = (properties != null)
            ? properties.getTableColumnAncestor() : null;

        // Get TableColumn ancestor.
        if (tableColumn == null) {
            UIComponent component = this;
            while (component != null) {
                component = component.getParent();
                if (component instanceof TableColumn) {
                    tableColumn = (TableColumn) component;
                    break;
                }
            }
            // Save property in request map.
            if (properties != null) {
                properties.setTableColumnAncestor(tableColumn);
            }
        }
        return tableColumn;
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
            columnCount = getColumnCount(this);

            // Save property in request map.
            if (properties != null) {
                properties.setColumnCount(columnCount);
            }
        }
        return columnCount;
    }

    /**
     * Get the closest TableRowGroup ancestor that encloses this component.
     *
     * @return The TableRowGroup ancestor.
     */
    public TableRowGroup getTableRowGroupAncestor() {
        // Get properties cached in request map.
        Properties properties = getProperties();
        TableRowGroup tableRowGroup = (properties != null)
            ? properties.getTableRowGroupAncestor() : null;

        // Get TableRowGroup ancestor.
        if (tableRowGroup == null) {
            UIComponent component = this;
            while (component != null) {
                component = component.getParent();
                if (component instanceof TableRowGroup) {
                    tableRowGroup = (TableRowGroup) component;
                    break;
                }
            }
            // Save property in request map.
            if (properties != null) {
                properties.setTableRowGroupAncestor(tableRowGroup);
            }
        }
        return tableRowGroup;
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Column methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Get column footer.
     *
     * @return The column footer.
     */
    public UIComponent getColumnFooter() {
        UIComponent facet = getFacet(COLUMN_FOOTER_FACET);
        if (facet != null) {
            return facet;
        }

        // Get child.
        TableFooter child = new TableFooter();
	child.setId(COLUMN_FOOTER_ID);
        child.setAlign(getAlign());
        child.setExtraHtml(getExtraFooterHtml());

        // Set rendered.
        if (!(facet != null && facet.isRendered()
                || getFooterText() != null || isColumnFooterRendered())) {
            // Note: Footer may be initialized to force rendering. This allows
            // developers to omit the footerText property for select columns.
            child.setRendered(false);
        } else {
            log("getColumnFooter", //NOI18N
                "Column footer not rendered, nothing to display"); //NOI18N
        }

        // If only showing one level, don't set colspan or rowspan.
        TableRowGroup group = getTableRowGroupAncestor();
        if (group != null && group.isMultipleColumnFooters()) {
            // Set colspan for nested TableColumn children, else rowspan.
            Iterator kids = getTableColumnChildren();
            if (kids.hasNext()) {
                int colspan = getColumnCount();
                if (colspan > 1) {
                    child.setColSpan(colspan);
                }
            } else {
                int rowspan = getRowCount();
                if (rowspan > 1) {
                    child.setRowSpan(rowspan);
                }
            }
        }

        // Save facet and return child.
        getFacets().put(child.getId(), child); 
        return child;
    }

    /**
     * Get column header.
     *
     * @return The column header.
     */
    public UIComponent getColumnHeader() {
        UIComponent facet = getFacet(COLUMN_HEADER_FACET);
        if (facet != null) {
            return facet;
        }

        // Get child.
        TableHeader child = new TableHeader();
	child.setId(COLUMN_HEADER_ID);
        child.setScope("col"); //NOI18N
        child.setAlign(getAlign());
        child.setWidth((getSelectId() != null) ? "3%" : null); //NOI18N
        child.setNoWrap((getSelectId() != null) ? true : false);
        child.setExtraHtml(getExtraHeaderHtml());

        // Set type of header to render.
        boolean emptyTable = isEmptyTable();
        SortCriteria criteria = getSortCriteria();
        if (criteria != null && getSelectId() != null && !emptyTable) {
            child.setSelectHeader(true);
        } else if (criteria != null && getHeaderText() != null && !emptyTable) {
            child.setSortHeader(true);
        } else {
            log("getColumnHeader", //NOI18N
                "Render default column header, no SortCriteria or selectId"); //NOI18N
        }

        // Set rendered.
        if (!(facet != null && facet.isRendered()
                || getHeaderText() != null || isColumnHeaderRendered())) {
            // Note: Footer may be initialized to force rendering. This allows
            // developers to omit the headerText property for select columns.
            log("getColumnHeader", //NOI18N
                "Column header not rendered, nothing to display"); //NOI18N
            child.setRendered(false);            
        }

        // Set colspan for nested TableColumn children, else rowspan.
        Iterator kids = getTableColumnChildren();
        if (kids.hasNext()) {
            int colspan = getColumnCount();
            if (colspan > 1) {
                child.setColSpan(colspan);
            }
        } else {
            int rowspan = getRowCount();
            if (rowspan > 1) {
                child.setRowSpan(rowspan);
            }
        }

        // Save facet and return child.
        getFacets().put(child.getId(), child); 
        return child;
    }

    /**
     * Get table column footer.
     *
     * @return The table column footer.
     */
    public UIComponent getTableColumnFooter() {
        UIComponent facet = getFacet(TABLE_COLUMN_FOOTER_FACET);
        if (facet != null) {
            return facet;
        }

        // Get child.
        TableFooter child = new TableFooter();
	child.setId(TABLE_COLUMN_FOOTER_ID);
        child.setAlign(getAlign());
        child.setExtraHtml(getExtraTableFooterHtml());
        child.setTableColumnFooter(true);

        // Set rendered.
        if (!(facet != null && facet.isRendered()
                || getTableFooterText() != null || isTableColumnFooterRendered())) {
            // Note: Footer may be initialized to force rendering. This allows
            // developers to omit the tableFooterText property for select columns.
            child.setRendered(false);
        } else {
            log("getTableColumnFooter", //NOI18N
                "Table column footer not rendered, nothing to display"); //NOI18N
        }

        // If only showing one level, don't set colspan or rowspan.
        TableRowGroup group = getTableRowGroupAncestor();
        if (group != null && group.isMultipleTableColumnFooters()) {
            // Set colspan for nested TableColumn children, else rowspan.
            Iterator kids = getTableColumnChildren();
            if (kids.hasNext()) {
                int colspan = getColumnCount();
                if (colspan > 1) {
                    child.setColSpan(colspan);
                }
            } else {
                int rowspan = getRowCount();
                if (rowspan > 1) {
                    child.setRowSpan(rowspan);
                }
            }
        }

        // Save facet and return child.
        getFacets().put(child.getId(), child); 
        return child;
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // TableColumnBase methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Get the horizontal alignment for the cell.
     * <p>
     * Note: If the align property is specified, it is returned as is. However, 
     * if the alignKey property is provided, alignment is based on the object 
     * type of the data element. For example, Date and Number objects are
     * aligned using "right", Character and String objects are aligned using 
     * "left", and Boolean objects are aligned using "center". Note that select 
     * columns are aligned using "center" by default.
     * </p>
     * @return The horizontal alignment for the cell. If the align property is 
     * null or the object type cannot be determined, "left" is returned by 
     * default.
     */
    public String getAlign() {
        // Note: The align property overrides alignKey.
        if (super.getAlign() != null) {
            return super.getAlign();
        }

        // Get alignment.
        String result = null;
        Class type = getType();
        if (type != null 
                && (type.equals(Character.class) || type.equals(String.class))) {
            result = "left"; //NOI18N
        } else if (type != null
                && (type.equals(Date.class) || type.equals(Number.class))) {
            result = "right"; //NOI18N
        } else if (type != null && type.equals(Boolean.class)) {
            result = "center"; //NOI18N
        } else {
            // Note: Select columns also default to "left".
            result = "left"; //NOI18N
        }
        return result;
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Empty cell methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Get the icon used to display inapplicable or unexpectedly empty cells.
     * <p>
     * Note: UI guidelines suggest not to use this for a value that is truly
     * null, such as an empty alarm cell or a comment field which is blank,
     * neither of which should have the dash image. Further, it is recomended
     * not to use the dash image for cells that contain user interface elements
     * such as checkboxes or drop-down lists when these elements are not 
     * applicable. Instead, simply do not display the user interface element.
     * </p>
     * @return The icon used to display empty cells.
     */
    public UIComponent getEmptyCellIcon() {       
        UIComponent facet = getFacet(EMPTY_CELL_ICON_FACET);
        if (facet != null) {
            return facet;
        }

        Theme theme = getTheme();

        // Get child.
        Icon child = theme.getIcon(ThemeImages.TABLE_EMPTY_CELL);
	child.setId(EMPTY_CELL_ICON_ID);
        child.setBorder(0);

        // Set tool tip.
        String toolTip = theme.getMessage("table.emptyTableCell"); //NOI18N
        child.setToolTip(toolTip);
        child.setAlt(toolTip);
        
        // Save facet and return child.
        getFacets().put(child.getId(), child); 
        return child;
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Separator methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Get separator icon for embedded actions.
     *
     * @return The separator icon for embedded actions.
     */
    public UIComponent getEmbeddedActionSeparatorIcon() {
        UIComponent facet = getFacet(EMBEDDED_ACTION_SEPARATOR_ICON_FACET);
        if (facet != null) {
            return facet;
        }

        // Get child.
        Icon child = getTheme().getIcon(
            ThemeImages.TABLE_EMBEDDED_ACTIONS_SEPARATOR);
	child.setId(EMBEDDED_ACTION_SEPARATOR_ICON_ID);
        child.setBorder(0);
        child.setAlign("top"); //NOI18N
        
        // Save facet and return child.
        getFacets().put(child.getId(), child); 
        return child;
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Sort methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Get SortCriteria used for sorting the contents of a TableDataProvider.
     * <p>
     * Note: If the sortKey attribute resolves to a SortCriteria object, it is
     * returned as is. However, if there is a value binding, and it's not null,
     * a ValueBindingSortCriteria object is created. If there is no value 
     * binding, a FieldIdSortCriteria object is created.
     * </p>
     * @return The SortCriteria used for sorting.
     */
    public SortCriteria getSortCriteria() {
        // Return if value binding resolves to a SortCriteria object.
        Object key = getSort();
        if (key instanceof SortCriteria) {
            return (SortCriteria) key;
        }

        SortCriteria result = null;
        ValueBinding vb = getValueBinding("sort"); //NOI18N
        if (vb != null) {
            ValueBindingSortCriteria vbsc = new ValueBindingSortCriteria(vb, 
                !isDescending()); // Note: Constructor accepts ascending param.
            TableRowGroup group = getTableRowGroupAncestor();
            if (group != null) {
                vbsc.setRequestMapKey(group.getSourceVar());
            }
            result = vbsc;
        } else if (key != null) {
            result = new FieldIdSortCriteria(key.toString(), !isDescending());
        }
        return result;
    }

    /**
     * Get sort tool tip augment based on the value given to the align 
     * property of the tableColumn component.
     *
     * @param descending Flag indicating descending sort.
     * @return The sort tool tip augment.
     */
    public String getSortToolTipAugment(boolean descending) {
        String result = null;

        // To do: Test for toolTip property? The alarm or other custom 
        // components may need to set the tooltip. If so, do we need both
        // ascending and descending tooltips?
       
        // Get object type.
        Class type = getType();

        // Get tooltip.
        ValueBinding vb = getValueBinding("severity"); //NOI18N
        if (getSeverity() != null || vb != null) {
            result = (descending)
                ? "table.sort.augment.alarmDescending" //NOI18N
                : "table.sort.augment.alarmAscending"; //NOI18N
        } else if (getSelectId() != null
                || (type != null && type.equals(Boolean.class))) {
            result = (descending)
                ? "table.sort.augment.booleanDescending" //NOI18N
                : "table.sort.augment.booleanAscending"; //NOI18N
        } else if (type != null && type.equals(String.class)) {
            result = (descending)
                ? "table.sort.augment.stringDescending" //NOI18N
                : "table.sort.augment.stringAscending"; //NOI18N
        } else if (type != null && type.equals(Character.class)) {
            result = (descending)
                ? "table.sort.augment.charDescending" //NOI18N
                : "table.sort.augment.charAscending"; //NOI18N
        } else if (type != null && type.equals(Date.class)) {
            result = (descending)
                ? "table.sort.augment.dateDescending" //NOI18N
                : "table.sort.augment.dateAscending"; //NOI18N
        } else if (type != null && type.equals(Number.class)) {
            result = (descending)
                ? "table.sort.augment.numericDescending" //NOI18N
                : "table.sort.augment.numericAscending"; //NOI18N    
        } else {
            result = (descending)
                ? "table.sort.augment.undeterminedDescending" //NOI18N
                : "table.sort.augment.undeterminedAscending"; //NOI18N
        }
        return getTheme().getMessage(result);
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Private methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Helper method to get the number of columns found for this component that 
     * have a rendered property of true.
     *
     * @param component TableColumn to be rendered.
     * @return The first selectId property found.
     */
    private int getColumnCount(TableColumn component) {       
        int count = 0;
        if (component == null) {
            log("getColumnCount", //NOI18N
                "Cannot obtain column count, TableColumn is null"); //NOI18N
            return count;
        }
    
        // Get column count for nested TableColumn children.
        Iterator kids = component.getTableColumnChildren();
        if (kids.hasNext()) {
            while (kids.hasNext()) {
                TableColumn col = (TableColumn) kids.next();
                if (!col.isRendered()) {
                    continue;
                }
                count += getColumnCount(col);
            }
        } else {
            // Do not include root TableColumn nodes in count.
            if (component.isRendered()) {
                count++;
            }
        }
        return count;
    }

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
        String propertiesId = getClientId(context);
        Properties properties = (Properties) propertiesMap.get(propertiesId);
        if (properties == null) {
            properties = new Properties();
            propertiesMap.put(propertiesId, properties);
        }

        return properties;
    }

    /**
     * Helper method to get the number of rows found for this component that
     * have a rendered property of true.
     *
     * @return The number of rendered rows.
     */
    private int getRowCount() {
        // Get properties cached in request map.
        Properties properties = getProperties();
        int rowCount = (properties != null)
            ? properties.getRowCount() : -1;
        if (rowCount == -1) {
            rowCount = 0; // Initialize min value.

            // Get all TableColumn children at the same level of the tree.
            Iterator kids = null;
            TableColumn col = getTableColumnAncestor();
            if (col != null) {
                kids = col.getTableColumnChildren();
            } else {
                TableRowGroup group = getTableRowGroupAncestor();
                kids = (group != null) ? group.getTableColumnChildren() : null;
            }

            // Get max row count for this level of the tree.
            if (kids != null) {
                while (kids.hasNext()) {
                    int result = getRowCount((TableColumn) kids.next());
                    if (rowCount < result) {
                        rowCount = result;
                    }
                }
            }
        }
        properties.setRowCount(rowCount);
        return rowCount;
    }

    /**
     * Helper method to get the number of rows found for this component that
     * have a rendered property of true.
     *
     * @param component TableColumn to be rendered.
     * @return The first selectId property found.
     */
    private int getRowCount(TableColumn component) {
        int count = 0;
        if (component == null) {
            log("getRowCount", "Cannot obtain row count, TableColumn is null"); //NOI18N
            return count;
        }

        // Get max row count for nested TableColumn children.
        Iterator kids = component.getTableColumnChildren();
        if (kids.hasNext()) {
            while (kids.hasNext()) {
                TableColumn col = (TableColumn) kids.next();
                if (!col.isRendered()) {
                    continue;
                }
                int result = getRowCount(col);
                if (count < result) {
                    count = result;
                }
            }
        }
        // Include root TableColumn component in count.
        return ++count;
    }

    /**
     * Helper method to get the data type of the data element referenced by the
     * alignKey property.
     *
     * @return The data type of the data element.
     */
    private Class getType() {
        // Note: Avoid using getSourceData when possible. If developers do not
        // cache their TableDataProvider objects, this may cause providers to be
        // recreated, for each reference, which affects performance. Instead, 
        // get the type cached in TableRowGroup.
        TableRowGroup group = getTableRowGroupAncestor();
        if (group == null) {
            log("getType", "Cannot obtain data type, TableRowGroup is null"); //NOI18N
            return null;
        }

        // Get FieldKey.
        FieldKey key = null;
        if (getAlignKey() instanceof FieldKey) {
            // If value binding resolves to FieldKey, use as is.
            key = (FieldKey) getAlignKey();
        } else if (getAlignKey() != null) {
            key = group.getFieldKey(getAlignKey().toString());
        }
        return (key != null) ? group.getType(key) : String.class;
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
     * Helper method to test if column footers should be rendered.
     * <p>
     * Note: Since headers and footers are optional, we do not render them by 
     * default. However, if any of the properties above are set, they must be
     * set for all columns, including nested columns. Otherwise, we may end up
     * with no header or footer and columns shift left. Alternatively, 
     * developers could add an empty string for each property.
     * </p>
     */
    private boolean isColumnFooterRendered() {
        boolean result = false; // Assume no headers or footers are used.
        TableRowGroup group = getTableRowGroupAncestor();
        if (group == null) {
            log("isColumnFooterRendered", //NOI18N
                "Cannot determine if column footer is rendered, TableRowGroup is null"); //NOI18N
            return result;
        }

        // Test the footerText property for all TableColumn components.
        Iterator kids = group.getTableColumnChildren();
        while (kids.hasNext()) {
            TableColumn col = (TableColumn) kids.next();
            if (isColumnFooterRendered(col)) {
                result = true;
                break;
            }
        }
        return result;
    }

    /**
     * Helper method to test the footerText property for nested TableColumn
     * components.
     *
     * @param component TableColumn component to render.
     */
    private boolean isColumnFooterRendered(TableColumn component) {
        boolean rendered = false;
        if (component == null) {
            log("isColumnFooterRendered", //NOI18N
                "Cannot determine if column footer is rendered, TableColumn is null"); //NOI18N
            return rendered;
        }

        // Test the footerText property for all TableColumn components.
        Iterator kids = component.getTableColumnChildren();
        if (kids.hasNext()) {
            while (kids.hasNext()) {
                TableColumn col = (TableColumn) kids.next();
                if (isColumnFooterRendered(col)) {
                    // When footer text is found, don't go any further.
                    return true;
                }
            }
        }

        // If either a facet or text are defined, set rendered property.
        UIComponent facet = component.getFacet(COLUMN_FOOTER_FACET);            
        if (facet != null || component.getFooterText() != null) {
            rendered = true;
        }
        return rendered;
    }

    /**
     * Helper method to test if column headers should be rendered.
     * <p>
     * Note: Since headers and footers are optional, we do not render them by 
     * default. However, if any of the properties above are set, they must be
     * set for all columns, including nested columns. Otherwise, we may end up
     * with no header or footer and columns shift left. Alternatively, 
     * developers could add an empty string for each property.
     * </p>
     */
    private boolean isColumnHeaderRendered() {
        boolean result = false; // Assume no headers or footers are used.
        TableRowGroup group = getTableRowGroupAncestor();
        if (group == null) {
            log("isColumnHeaderRendered", //NOI18N
                "Cannot determine if column header is rendered, TableRowGroup is null"); //NOI18N
            return result;
        }

        // Test the headerText property for all TableColumn components.
        Iterator kids = group.getTableColumnChildren();
        while (kids.hasNext()) {
            TableColumn col = (TableColumn) kids.next();
            if (isColumnHeaderRendered(col)) {
                result = true;
                break;
            }
        }
        return result;
    }

    /**
     * Helper method to test the headerText property for nested TableColumn
     * components.
     *
     * @param component TableColumn component to render.
     */
    private boolean isColumnHeaderRendered(TableColumn component) {
        boolean rendered = false;
        if (component == null) {
            log("isColumnHeaderRendered", //NOI18N
                "Cannot determine if column header is rendered, TableColumn is null"); //NOI18N
            return rendered;
        }

        // Test the headerText property for all TableColumn components.
        Iterator kids = component.getTableColumnChildren();
        if (kids.hasNext()) {
            while (kids.hasNext()) {
                TableColumn col = (TableColumn) kids.next();
                if (isColumnHeaderRendered(col)) {
                    // When header text is found, don't go any further.
                    return true;
                }
            }
        }

        // If either a facet or text are defined, set rendered property.
        UIComponent facet = component.getFacet(COLUMN_HEADER_FACET);            
        if (facet != null || component.getHeaderText() != null) {
            rendered = true;
        }
        return rendered;
    }

    /**
     * Helper method to determine if table is empty.
     * <p>
     * Note: We must determine if column headers are available for all or
     * individual TableRowGroup components. That is, there could be a single
     * column header for all row groups or one for each group. If there is more
     * than one column header, we must test the row count of all groups. If
     * there is only one column header and other groups have more than one row,
     * we want to make sorting available. Thus, sorting is available only there
     * is more than on row for all row groups.
     * </p>
     * @return true if sorting should be available, else false.
     */
    private boolean isEmptyTable() {
        boolean result = false;
        Table table = getTableAncestor();
        TableRowGroup group = getTableRowGroupAncestor();
        if (table != null && group != null) {
            // Get total rows and headers for all TableRowGroup components.
            int rows = table.getRowCount();
            int headers = table.getColumnHeadersCount();
            result = (headers > 1)
                ? !(group.getRowCount() > 1) // Test individual groups.
                : rows == 0 || rows == 1; // No sorting for single row.
        }
        return result;
    }

    /**
     * Helper method to test if table column footers should be rendered.
     * <p>
     * Note: Since headers and footers are optional, we do not render them by 
     * default. However, if any of the properties above are set, they must be
     * set for all columns, including nested columns. Otherwise, we may end up
     * with no header or footer and columns shift left. Alternatively, 
     * developers could add an empty string for each property.
     * </p>
     */
    private boolean isTableColumnFooterRendered() {
        boolean result = false; // Assume no headers or footers are used.
        TableRowGroup group = getTableRowGroupAncestor();
        if (group == null) {
            log("isTableColumnFooterRendered", //NOI18N
                "Cannot determine if table column footer is rendered, TableRowGroup is null"); //NOI18N
            return result;
        }

        // Test the tableFooterText property for all TableColumn components.
        Iterator kids = group.getTableColumnChildren();
        while (kids.hasNext()) {
            TableColumn col = (TableColumn) kids.next();
            if (isTableColumnFooterRendered(col)) {
                result = true;
                break;
            }
        }
        return result;
    }

    /**
     * Helper method to test the tableFooterText property for nested TableColumn
     * components.
     *
     * @param component TableColumn component to render.
     */
    private boolean isTableColumnFooterRendered(TableColumn component) {
        boolean rendered = false;
        if (component == null) {
            log("isTableColumnFooterRendered", //NOI18N
                "Cannot determine if table column footer is rendered, TableColumn is null"); //NOI18N
            return rendered;
        }

        // Test the tableFooterText property for all TableColumn components.
        Iterator kids = component.getTableColumnChildren();
        if (kids.hasNext()) {
            while (kids.hasNext()) {
                TableColumn col = (TableColumn) kids.next();
                if (isTableColumnFooterRendered(col)) {
                    // When footer text is found, don't go any further.
                    return true;
                }
            }
        }

        // If either a facet or text are defined, set rendered property.
        UIComponent facet = component.getFacet(TABLE_COLUMN_FOOTER_FACET);            
        if (facet != null || component.getTableFooterText() != null) {
            rendered = true;
        }
        return rendered;
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
        // The TableColumn ancestor enclosing this component.
        private TableColumn tableColumn = null;

        // A List of TableColumn children found for this component.
        private List tableColumnChildren = null;

        // The TableRowGroup ancestor enclosing this component.
        private TableRowGroup tableRowGroup = null;

        // The number of columns to be rendered.
        private int columnCount = -1;

        // The number of rows to be rendered for headers and footers.
        private int rowCount = -1;

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
         * Get the number of rows found for this component that have a rendered 
         * property of true.
         *
         * @return The number of rendered rows.
         */
        private int getRowCount() {
            return rowCount;
        }

        /**
         * Set the number of rows found for this component that have a rendered
         * property of true.
         *
         * @param rowCount The number of rendered rows.
         */
        private void setRowCount(int rowCount) {
            this.rowCount = rowCount;
        }

        /**
         * Get the closest TableColumn ancestor that encloses this component.
         *
         * @return The TableColumn ancestor.
         */
        public TableColumn getTableColumnAncestor() {
            return tableColumn;
        }

        /**
         * Get the closest TableColumn ancestor that encloses this component.
         *
         * @param tableColumn The TableColumn ancestor.
         */
        public void setTableColumnAncestor(TableColumn tableColumn) {
            this.tableColumn = tableColumn;
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
         * Get the closest TableRowGroup ancestor that encloses this component.
         *
         * @return The TableRowGroup ancestor.
         */
        public TableRowGroup getTableRowGroupAncestor() {
            return tableRowGroup;
        }

        /**
         * Set the closest TableRowGroup ancestor that encloses this component.
         *
         * @param tableRowGroup The TableRowGroup ancestor.
         */
        public void setTableRowGroupAncestor(TableRowGroup tableRowGroup) {
            this.tableRowGroup = tableRowGroup;
        }
    }
}
