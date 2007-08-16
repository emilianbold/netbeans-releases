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

import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;

/**
 * Use the <code>ui:table</code>
 * tag to create a table that can be configured to perform actions on
 * objects in the table, and to manipulate the rows and columns of the
 * table. The table component features behaviors that let the user sort,
 * filter, and paginate the table objects, and provides actions that can
 * be performed on selected objects. The component also allows you to
 * implement developer-defined custom actions. <br>
 * <p>Note that the <code>ui:table</code>
 * tag is not intended to be used to create HTML tables that are used
 * purely to handle page layout. The
 * table component renders a
 * table with
 * a well-defined structure, including a title, column headings, and
 * borders. You can use tags
 * such as <code><a href="propertySheet.html">ui:propertySheet</a></code>
 * and <code><a href="panelGroup.html">ui:panelGroup</a></code>
 * for page
 * layout if
 * possible.<br>
 * </p>
 * <p>The table component
 * implements Sun's user interface (UI) guidelines for web
 * applications. The guidelines describe in detail how the table should
 * appear and behave, including the placement of titles, group headers,
 * and actions. The table component's default behavior implements the UI
 * guidelines. The component is also extensible through JavaServer Faces
 * facets to
 * allow the component to be used for tables that do not need to adhere
 * strictly to the UI guidelines.<br>
 * </p>
 * <p>The <code>ui:table</code>
 * tag must be used with the <a href="tableRowGroup.html"><code>ui:tableRowGroup</code></a>
 * and <a href="tableColumn.html"><code>ui:tableColumn</code></a>
 * tags. The <code>ui:table</code>
 * tag is
 * used to define the structure and actions of the table, and is a
 * container for <code>ui:tableRowGroup</code>
 * tags.&nbsp; The <code>ui:tableRowGroup</code> tag is used to define
 * the rows of the table, and is a container for <code>ui:tableColumn</code>
 * tags. The <code>ui:tableColumn</code> tag is used to define the
 * columns of the table. <br>
 * </p>
 * <h3>HTML Elements and Layout</h3>
 * The table component renders
 * an XHTML <code>&lt;table&gt;</code> element.&nbsp; Depending upon the
 * attributes specified with the <code>ui:table</code> tag, the table
 * component can also render a title in a <code>&lt;caption&gt;</code>
 * element, and image hyperlinks for the various buttons for sorting and
 * pagination.&nbsp; The table component does not render table rows except
 * for
 * the rows that contain
 * the view-changing and pagination controls. <br>
 * <h3>Table Structure<code></code><br>
 * </h3>
 * <p>Tables are composed of several
 * discrete areas. You can use&nbsp;
 * <code>ui:table</code> tag attributes to cause the table component to
 * create the default layout for each
 * area. The default layout strictly adheres to UI guidelines.&nbsp; The
 * layout for
 * each area can also be customized by using facets.&nbsp; Some areas
 * require you to use facets to implement the content you want in those
 * areas. Descriptions of
 * the table areas are shown below, followed by a <a href="#diagram">diagram</a>
 * that shows the placement of the areas.<br>
 * </p>
 * <ul>
 * <li>
 * <p>Title - displays a title for the table,
 * which you can also use to include information about paginated rows and
 * applied
 * filters. Use the <code>title</code> attribute to specify the title
 * content. Use the <code>itemsText</code> attribute to specify the text
 * displayed for the table title for an unpaginated table.&nbsp;<span
 * style="font-weight: bold; color: rgb(255, 0, 0);"></span><span
 * style="color: rgb(255, 0, 0);"> </span>Use
 * the <code>filterText</code>
 * attribute to specify text to include in the title about the filter that
 * is applied.&nbsp; <span style="color: rgb(102, 102, 204);"></span>You
 * can override the default implementation of the
 * title bar
 * with a different component by using the <code>title</code>
 * facet.&nbsp; <br style="color: rgb(255, 153, 0);">
 * </p>
 * </li>
 * <li>
 * <p>Action Bar (top) - in the first row of the table, displays
 * controls that operate on the
 * table and its data. This area contains the following sub areas:</p>
 * </li>
 * <ul>
 * <li>
 * <p>Actions - displays local actions that apply to
 * the objects in the table. You must provide the components for each
 * action, by specifying them in the <code>actionsTop</code>
 * facet.&nbsp; You can specify the same actions for the Action Bar
 * (bottom) area in the <code>actionsBottom</code> facet.<br>
 * </p>
 * </li>
 * </ul>
 * <ul>
 * <li>
 * <p>View-Changing
 * Controls - displays controls for changing the view of the
 * table data, such as custom filtering and sorting. This area
 * contains the following&nbsp; sub areas: <br>
 * </p>
 * </li>
 * <ul>
 * <li>
 * <p> Filter - displays a drop down menu of filter options,
 * which allow users to select criteria to be used to determine the items
 * to display. The component provides a default implementation for adding
 * options when the Custom filter becomes active, to display the filter
 * panel, etc. However, you must implement your filters
 * through custom options. An example
 * filter is shown in <span style="color: rgb(255, 153, 0);"><a
 * href="table.html#Filter.java">Filter.java.</a></span></p>
 * </li>
 * <li>Clear Sort - displays a button that is used to remove
 * all sorting of the table.&nbsp; Use the <code>clearTableSortButton</code>
 * attribute to display the clear table sort button. </li>
 * <li>
 * <p><span
 * style="color: rgb(255, 0, 0); text-decoration: line-through;"></span>Sort
 * - displays a
 * toggle button for the sort panel. Clicking this button
 * opens and closes an embedded panel with custom sort
 * options. The panel
 * opens inside the table below the Action Bar (top).&nbsp; Use the <code>sortPanelToggleButton
 * </code>attribute
 * to display the default sort button with a default
 * layout of the sort panel.&nbsp; You can
 * provide custom content for the sort panel by using the <code>sortPanel</code>
 * facet. </p>
 * </li>
 * <li>
 * <p>Preferences
 * - displays a toggle button for setting the
 * user's preferences. Clicking this
 * button opens and closes an embedded
 * panel with view preferences. The preferences panel opens inside
 * the table below
 * the Action Bar (top). You must provide the content of
 * the preferences panel by using the <code>preferencesPanel</code>
 * facet. There is no
 * default implementation of the preferences panel content, and therefore
 * no attribute to specify that the button should be displayed. <br>
 * </p>
 * </li>
 * </ul>
 * <li>
 * <p>Vertical Pagination - The Vertical Pagination area displays a
 * paginate button, which allows users to switch between viewing the table
 * as multiple pages, or as a single scrolling page.&nbsp; You can specify
 * the <code>paginateButton</code>&nbsp; attributes to display the
 * default paginate button.&nbsp; Note that the Vertical
 * Pagination area is limited to this button. You cannot use extra
 * pagination controls in this area, as you can in the bottom Pagination
 * area.&nbsp;</p>
 * </li>
 * </ul>
 * <li>
 * <p>Action Bar (bottom)</p>
 * </li>
 * <ul>
 * <li>
 * <p>Actions - displays local actions that apply to
 * the objects in the table. You must provide the implementation for each
 * table action, by using the <code>actionsBottom</code> facet . The
 * same actions can exist for the Table both the "Action
 * Bar
 * (top)" and "Action Bar
 * (bottom)" sections</p>
 * </li>
 * <li>
 * <p>Pagination Controls
 * - displays controls for pagination, including the
 * paginate button, which allows users to switch between viewing the table
 * as multiple pages, or as a single scrolling page. The Pagination area
 * also includes buttons for turning the pages in sequence, jumping to a
 * specific page, and jumping to the first or last page. You can specify
 * the <code>paginateButton</code> and <code>paginateControls</code>
 * attributes to display the default layout&nbsp; of the Pagination area. <code></code></p>
 * </li>
 * </ul>
 * <li>
 * <p>Footer -&nbsp; displays a footer across all columns at the
 * bottom of the table. You can specify the <code>footerText</code>
 * attribute to display footer content with a default layout, or specify a
 * component for the footer by using the <code>footer</code> facet.</p>
 * </li>
 * </ul>
 * <a name="diagram"></a>The
 * following diagram shows
 * the relative location of the table areas and facets that can be used
 * for each area. The areas that are specified with the <code>ui:table</code>
 * tag&nbsp; attributes are highlighted<span
 * style="color: rgb(102, 102, 204);"> </span>in blue.&nbsp; The grayed
 * out area
 * is controlled with ui:<code>tableRowGroup</code> and <code>ui:tableColumn</code>
 * tags, but is shown here for context.<br>
 * <br>
 * <br>
 * <table style="text-align: left; width: 100%;" border="1" cellpadding="2"
 * cellspacing="2">
 * <tbody>
 * <tr>
 * <td
 * style="vertical-align: top; background-color: rgb(210, 224, 235);"
 * rowspan="1" colspan="1"><span style="color: rgb(0, 0, 0);">Title
 * Bar <code>title</code> <br>
 * </span></td>
 * </tr>
 * <tr style="color: rgb(0, 0, 0);">
 * <td
 * style="vertical-align: top; background-color: rgb(210, 224, 235);"
 * rowspan="1" colspan="1">Action
 * Bar (top) &nbsp;
 * <table style="text-align: left; width: 100%;" border="1"
 * cellpadding="2" cellspacing="2">
 * <tbody>
 * <tr>
 * <td style="vertical-align: top;">Actions <code>actionsTop </code></td>
 * <td style="vertical-align: top;">View-Changing Controls<br>
 * <table style="text-align: left; width: 100%;" border="1"
 * cellpadding="2" cellspacing="2">
 * <tbody>
 * <tr>
 * <td style="vertical-align: top;">Filter<br>
 * <code>filter</code></td>
 * <td style="vertical-align: top;">Sort<br>
 * <code></code></td>
 * <td style="vertical-align: top;">Clear Sort </td>
 * <td style="vertical-align: top;">Preferences</td>
 * </tr>
 * </tbody>
 * </table>
 * </td>
 * <td style="vertical-align: top;">Vertical
 * Pagination <code></code></td>
 * </tr>
 * <tr style="color: rgb(0, 0, 0);">
 * <td style="vertical-align: top;" rowspan="1" colspan="3">Embedded
 * Panels <code>filterPanel sortPanel preferencesPanel</code></td>
 * </tr>
 * </tbody>
 * </table>
 * <code></code> </td>
 * </tr>
 * <tr>
 * <td
 * style="vertical-align: top; background-color: rgb(210, 224, 235);">
 * <table style="text-align: left; height: 223px; width: 100%;"
 * border="1" cellpadding="2" cellspacing="2">
 * <tbody>
 * <tr>
 * <td
 * style="vertical-align: top; background-color: rgb(232, 232, 232); color: rgb(153, 153, 153);">Column
 * Header <br>
 * </td>
 * <td
 * style="vertical-align: top; background-color: rgb(232, 232, 232); color: rgb(153, 153, 153);">Column
 * Header<br>
 * </td>
 * </tr>
 * <tr style="color: rgb(153, 153, 153);">
 * <td
 * style="vertical-align: top; background-color: rgb(232, 232, 232);"
 * rowspan="1" colspan="2">Group
 * Header Bar&nbsp; </td>
 * </tr>
 * <tr>
 * <td
 * style="vertical-align: top; background-color: rgb(232, 232, 232); color: rgb(153, 153, 153);"><span
 * style="font-style: italic;">Table
 * data</span> <code></code><br>
 * <code> <br>
 * <br>
 * <br>
 * </code></td>
 * <td
 * style="vertical-align: top; background-color: rgb(232, 232, 232); color: rgb(153, 153, 153);"><span
 * style="font-style: italic;">Table
 * data </span><code></code><br>
 * </td>
 * </tr>
 * <tr>
 * <td
 * style="vertical-align: top; background-color: rgb(232, 232, 232); color: rgb(153, 153, 153);">Column
 * Footer</td>
 * <td
 * style="vertical-align: top; background-color: rgb(232, 232, 232); color: rgb(153, 153, 153);">Column
 * Footer <br>
 * </td>
 * </tr>
 * <tr style="color: rgb(153, 153, 153);">
 * <td
 * style="vertical-align: top; background-color: rgb(232, 232, 232);"
 * rowspan="1" colspan="2">Group
 * Footer Bar&nbsp; <br>
 * </td>
 * </tr>
 * <tr>
 * <td
 * style="vertical-align: top; background-color: rgb(232, 232, 232); color: rgb(153, 153, 153);">Table
 * Column Footer </td>
 * <td
 * style="vertical-align: top; background-color: rgb(232, 232, 232); color: rgb(153, 153, 153);">Table
 * Column Footer </td>
 * </tr>
 * <tr>
 * <td style="vertical-align: top;" rowspan="1" colspan="2">Action
 * Bar (bottom)<br>
 * <table style="text-align: left; width: 100%;" border="1"
 * cellpadding="2" cellspacing="2">
 * <tbody>
 * <tr>
 * <td style="vertical-align: top;">Actions <code>actionsBottom</code><br>
 * </td>
 * <td style="vertical-align: top;">Pagination Controls<br>
 * </td>
 * </tr>
 * </tbody>
 * </table>
 * </td>
 * </tr>
 * </tbody>
 * </table>
 * <big><span style="color: rgb(0, 0, 0);"> </span></big></td>
 * </tr>
 * <tr>
 * <td
 * style="vertical-align: top; background-color: rgb(210, 224, 235);"
 * rowspan="1" colspan="1"><span style="color: rgb(0, 0, 0);">Footer<code></code></span><code>
 * </code></td>
 * </tr>
 * </tbody>
 * </table>
 * <ul>
 * </ul>
 * <h3 style="color: rgb(0, 0, 0);">Buttons and Controls</h3>
 * <span style="color: rgb(0, 0, 0);">The following attributes can
 * be specified to add buttons and controls to the table:</span><br
 * style="color: rgb(0, 0, 0);">
 * <ul style="color: rgb(0, 0, 0);">
 * <li>
 * <p><code>clearSortButton </code>adds a button to the View-Changing
 * Controls area that clears any sorting of the
 * table.</p>
 * </li>
 * <li>
 * <p><code>deselectMultipleButton&nbsp; </code>adds a button for
 * tables in which multiple rows can be
 * selected, to allow users to deselect all table rows that are currently
 * displayed.</p>
 * </li>
 * <li>
 * <p><code>deselectSingleButton </code>adds a button for tables in
 * which only a single table row
 * can be selected at a time, to allow users to deselect a column of radio
 * buttons </p>
 * </li>
 * <li>
 * <p><code>paginateButton </code>adds a button
 * to allow users to switch between
 * viewing all data on a single page (unpaginated) or to see data in
 * multiple pages (paginated).</p>
 * </li>
 * <li>
 * <p><code>paginationControls </code>adds table
 * pagination controls to allow users to change which page is
 * displayed.</p>
 * </li>
 * <li>
 * <p><code>selectMultipleButton </code>adds a button that is used
 * for selecting multiple rows.&nbsp;<br>
 * </p>
 * </li>
 * <li>
 * <p><code>sortPanelToggleButton</code>&nbsp; adds a button that is
 * used to open and close the sort panel. </p>
 * </li>
 * </ul>
 * <span style="color: rgb(0, 0, 0);">
 * </span>
 * <h3 style="color: rgb(0, 0, 0);">Cell Spacing and Shading</h3>
 * <p style="color: rgb(0, 0, 0);">The following attribute can be
 * specified to change the spacing and shading weight of the table:</p>
 * <ul style="color: rgb(0, 0, 0);">
 * <li>
 * <p><code>cellPadding&nbsp;</code> specifies the amount of
 * whitespace that
 * should be placed between the cell contents and the cell borders in all
 * the cells of the table. </p>
 * </li>
 * <li>
 * <p><code>cellSpacing&nbsp;</code> specifies the amount of
 * whitespace
 * that should be placed between cells, and between the edges of the table
 * content area and the sides of the table. </p>
 * </li>
 * <li>
 * <p><code>lite&nbsp;</code> renders the table in a style
 * that makes the table look lighter weight.</p>
 * </li>
 * </ul>
 * <span style="color: rgb(0, 0, 0);">
 * </span>
 * <h3 style="color: rgb(0, 0, 0);">Headers</h3>
 * <p style="color: rgb(0, 0, 0);">The table component allows
 * for multiple headers. The following types of headers are supported:<br>
 * </p>
 * <ul style="color: rgb(0, 0, 0);">
 * <li>Column header - confined to the column for which
 * it is defined, and displayed by default at the top of the
 * table,
 * below the Action Bar and above all row groups.&nbsp; Column headers are
 * controlled with
 * attributes in the <code>ui:tableColumn</code> tag. User interface
 * guidelines recommend
 * that column headers are rendered once for each table. In tables with
 * multiple groups, the column headers should be defined in the <code>ui:tableColumn</code>
 * tags that are contained in the first <code>ui:tableRowGroup</code>
 * tag.
 * See the <a href="tableColumn.html"><code>ui:tableColumn</code>
 * documentation</a> for more
 * information. <br>
 * </li>
 * </ul>
 * <ul style="color: rgb(0, 0, 0);">
 * <li>Group header - spans across all table columns,
 * and is displayed above each row group. The first group header is
 * displayed below the column headers, above the table data.&nbsp; Group
 * headers for any other groups specified in the table are displayed above
 * the data rows for the group. Group headers are set in the <code>ui:tableRowGroup</code>
 * tag.&nbsp; See the <a href="tableRowGroup.html"><code>ui:tableRowGroup</code>
 * documentation</a> for more
 * information.</li>
 * </ul>
 * <h3 style="color: rgb(0, 0, 0);">Footers</h3>
 * <p style="color: rgb(0, 0, 0);">The table component allows
 * for multiple footers. The following types of footers are supported:<br>
 * </p>
 * <ul>
 * <li style="color: rgb(0, 0, 0);">
 * <p>Footer - spans the full
 * width of the table, and displayed at the bottom of the table. Only one
 * table footer is displayed in each table. The table footer&nbsp; is
 * defined in the <code>ui:table</code> tag.<br>
 * </p>
 * </li>
 * <li style="color: rgb(0, 0, 0);">
 * <p>Column footer - confined to the
 * column for which it is defined, and displayed by default at the bottom
 * of the column. In tables with multiple groups of rows, each group can
 * display its own column footer. The column footer is defined in <code>ui:tableColumn</code>
 * tags. See the <a href="tableColumn.html"><code>ui:tableColumn</code>
 * documentation</a> for more
 * information.</p>
 * </li>
 * <li>
 * <p><span style="color: rgb(0, 0, 0);">Table column footer -
 * confined to the column for which
 * it is defined, and displayed by default near the bottom of the table,
 * below all row groups, and above the Action Bar (bottom). User interface
 * guidelines recommend
 * that table column footers are rendered once for each table. In tables
 * with multiple groups, the table column footers should be defined in the
 * <code>ui:tableColumn</code> tags that are contained in the first <code>ui:tableRowGroup</code>
 * tag. See the </span><a href="tableColumn.html"
 * style="color: rgb(0, 0, 0);"><code>ui:tableColumn</code>
 * documentation</a><span style="color: rgb(102, 102, 204);"><span
 * style="color: rgb(0, 0, 0);"> for more
 * information. </span><br>
 * </span> </p>
 * </li>
 * </ul>
 * <h3>Facets</h3>
 * The <code>ui:table</code> tag supports the following facets, which
 * allow you to customize the
 * layout&nbsp; of the component.<br>
 * <br>
 * <span style="color: rgb(255, 153, 0);"></span><br>
 * <table style="width: 100%;" border="1" cellpadding="2" cellspacing="2">
 * <tbody>
 * <tr>
 * <td style="vertical-align: top;"><span style="font-weight: bold;">Facet
 * Name</span><code><br>
 * </code></td>
 * <td style="vertical-align: top; font-weight: bold;">Table Item
 * Implemented by the Facet<br>
 * </td>
 * </tr>
 * <tr>
 * <td><code>actionsBottom</code></td>
 * <td>Actions area of
 * the Action Bar (bottom).<br>
 * </td>
 * </tr>
 * <tr>
 * <td><code>actionsTop</code></td>
 * <td>Actions area of
 * the Action Bar (top).<br>
 * </td>
 * </tr>
 * <tr>
 * <td><code>filter</code></td>
 * <td>Drop down menu for
 * selecting a filter, in the Filter area of the Action Bar
 * (top).<br>
 * </td>
 * </tr>
 * <tr>
 * <td><code>filterPanel</code></td>
 * <td>Embedded panel for
 * specifying custom filter options, which is displayed when user selects
 * the Custom Filter option from the filter drop down menu. The
 * Custom Filter option is a recommended option that you can provide in
 * the drop down component that you specify in the filter
 * facet.&nbsp; The Custom Filter can be used to give users greater
 * control over filtering. The <a href="table.html#TableFilter">Filter
 * example</a>
 * explains filters in more detail.<span style="color: rgb(255, 153, 0);"><br>
 * </span></td>
 * </tr>
 * <tr>
 * <td><code>footer</code></td>
 * <td>Footer that spans
 * the
 * width of the table. <br>
 * </td>
 * </tr>
 * <tr>
 * <td><code>preferencesPanel</code></td>
 * <td>Panel displayed when
 * the preferences
 * toggle button is
 * clicked, to allow users to specify
 * preferences for viewing the table.<br>
 * </td>
 * </tr>
 * <tr>
 * <td><code>sortPanel</code></td>
 * <td>Panel displayed when
 * the sort toggle
 * button is clicked, to
 * allow users to specify sort
 * options.<br>
 * </td>
 * </tr>
 * <tr>
 * <td><code>title</code></td>
 * <td>Title in the top bar
 * of
 * the table.<br>
 * </td>
 * </tr>
 * </tbody>
 * </table>
 * <br>
 * <h3><a name="JavaScript"></a>Client-side
 * JavaScript
 * Functions</h3>
 * <p>The following JavaScript
 * functions are available in any page that uses the <code>ui:table</code>
 * tag. After the table is rendered, the functions you specify in the JSP
 * page can be invoked directly on the rendered HTML elements. For
 * example:
 * </p>
 * <code>var table =
 * document.getElementById("form1:table1");<br>
 * var count = table.getAllSelectedRowsCount();<br>
 * <br>
 * </code>
 * Note:
 * To use the JavaScript functions, <code>formElements.js</code> file
 * must be included in the page. The file is automatically included
 * by
 * the basic components such as the button and dropDown components.<br>
 * <br>
 * <span style="color: rgb(255, 153, 0);"><br>
 * </span>
 * <table style="text-align: left; width: 100%;" border="1" cellpadding="2"
 * cellspacing="2">
 * <tbody>
 * <tr>
 * <td style="vertical-align: top;"><span style="font-weight: bold;">Function
 * Name</span><br>
 * </td>
 * <td style="vertical-align: top; font-weight: bold;">Purpose<br>
 * </td>
 * </tr>
 * <tr>
 * <td style="vertical-align: top;"><code>confirmSelectedRows(message)<br>
 * </code></td>
 * <td style="vertical-align: top;">Confirm the number of
 * selected rows affected by an action such as edit, archive, etc. </td>
 * </tr>
 * <tr>
 * <td style="vertical-align: top;"><code>confirmDeleteSelectedRows()</code></td>
 * <td style="vertical-align: top;">Confirm the number of
 * selected rows affected by a delete action.</td>
 * </tr>
 * <tr>
 * <td><code><span style="">filterMenuChanged()</span></code></td>
 * <td>Toggle the filter panel when the user selects Custom
 * Filter in the Filter menu.<span style="color: rgb(102, 102, 204);"> </span>
 * </td>
 * </tr>
 * <tr>
 * <td><code>getAllSelectedRowsCount()</code></td>
 * <td> Get the number of
 * selected rows in the table, including the rows that are rendered
 * in the
 * current page, and rows that are hidden from view on other pages.<br>
 * </td>
 * </tr>
 * <tr>
 * <td><code>getAllHiddenSelectedRowsCount()</code></td>
 * <td>Get the number of
 * selected rows in the table that are on pages that are not currently
 * displayed. <br>
 * </td>
 * </tr>
 * <tr>
 * <td style="vertical-align: top;"><code>getAllRenderedSelectedRowsCount()</code></td>
 * <td style="vertical-align: top;">Get the number of
 * selected rows
 * that are currently rendered in the table. This function does not count
 * the rows that are hidden from view on other pages. Note that rows might
 * be rendered but not visible, and invisible rows are counted.&nbsp; See
 * the description of the <code>visible </code>and <code>rendered </code>attributes.
 * <br>
 * </td>
 * </tr>
 * <tr>
 * <td><code>initAllRows()</code></td>
 * <td>Initialize all rows displayed
 * in the table when the state
 * of selected components change,
 * such as when checkboxes or radiobuttons are used to select or deselect
 * all rows. <br>
 * </td>
 * </tr>
 * <tr>
 * <td style="vertical-align: top;"><code>toggleFilterPanel()</code></td>
 * <td style="vertical-align: top;">Open and close the custom filter
 * panel.</td>
 * </tr>
 * <tr>
 * <td style="vertical-align: top;"><code>togglePreferencesPanel()</code></td>
 * <td style="vertical-align: top;">Open and close the table
 * preferences panel.</td>
 * </tr>
 * </tbody>
 * </table>
 * <ul>
 * </ul>
 * <ul>
 * </ul>
 * <br>
 * <h3>Notes about <code>ui:table</code> tag</h3>
 * <h4><span style="font-weight: bold;">Life Cycle</span></h4>
 * See the <a href="tableRowGroup.html#Lifecycle">Lifecycle
 * description </a>in
 * the documentation for the <code>ui:tableRowGroup</code> component.<br>
 * <h4><span style="font-weight: bold;"><a name="LayoutTables"></a>Layout
 * Tables</span></h4>
 * <span style="color: rgb(0, 0, 0);">You should not use the </span><code
 * style="color: rgb(0, 0, 0);">ui:table</code><span
 * style="color: rgb(0, 0, 0);"> tag for page layout. The table
 * component renders elements for a table title, columns headers, and row
 * headers. These elements should not be used in a layout table, and
 * create an
 * accessibility issue.&nbsp; Use a standard HTML </span><code
 * style="color: rgb(0, 0, 0);">&lt;table&gt;</code><span
 * style="color: rgb(0, 0, 0);"> element, a </span><code
 * style="color: rgb(0, 0, 0);">ui:propertySheet</code><span
 * style="color: rgb(0, 0, 0);"> tag, or the JavaServer Faces </span><code
 * style="color: rgb(0, 0, 0);">h:dataTable</code><span
 * style="color: rgb(0, 0, 0);"> tag to perform page layout. If
 * your application needs to iterate over the same components as in
 * jato:tiledView, use a custom tag.&nbsp; The JSP Standard Tag Library
 * (JSTL) <code>forEach</code> tag might also be useful, but there are
 * interoperability issues with JSTL and JavaServer Faces, so use with
 * caution. </span><br>
 * <h4><span style="font-weight: bold;">Nested Tables</span></h4>
 * <span style="text-decoration: line-through;"></span>
 * <span style="color: rgb(0, 0, 0);">Although it is technically
 * possible to nest tables with the <code>ui:tabl</code>e tag, you should
 * not use the table component to layout multiple tables for the following
 * reasons:<br>
 * </span>
 * <ul style="color: rgb(0, 0, 0);">
 * <li>
 * <p>Nested tables create
 * accessibility issues, as described in <a href="#LayoutTables">Layout
 * Tables.</a></p>
 * </li>
 * <li>
 * <p>UI guidelines do not
 * support nested tables.</p>
 * </li>
 * <li>
 * <p>Styles used by the
 * table component might not display properly because they are not
 * intended to be nested.</p>
 * </li>
 * <li>
 * <p>Table sorting is not
 * supported in nested tables.&nbsp; The table component does not
 * support complex components that maintain state not defined by the
 * JavaServer Faces <code>EditableValueHolder</code> interface. Since a
 * single
 * component instance is
 * used when iterating over DataProvider rows, only the state of an <code>EditableValueHolder</code>
 * can be maintained. <br>
 * </p>
 * </li>
 * </ul>
 * <br style="color: rgb(0, 0, 0);">
 * <span style="color: rgb(0, 0, 0);">If you need to display similar
 * types of data using the same table
 * layout, you should use a group table. Sorting is supported for group
 * tables because they are
 * configured using separate tableRowGroup instances.</span><br
 * style="color: rgb(0, 0, 0);">
 * <span style="font-weight: bold; color: rgb(0, 0, 0);"><br>
 * Validation</span><br style="color: rgb(0, 0, 0);">
 * <p style="color: rgb(0, 0, 0);">To maintain state, the table
 * component submits the surrounding form.
 * For example, when the table is sorted, the form is submitted to update
 * checkbox values. Likewise, the form might be submitted to update text
 * field values when the table component must display a new page of a
 * paginated table.&nbsp; These components cannot be updated if validation
 * fails for any reason. If a component requires a value to be entered,
 * and no value is entered, validation fails.&nbsp; <br>
 * </p>
 * <p style="color: rgb(0, 0, 0);">Consider the case where a
 * required text field and
 * table
 * appear on the same page. If the user clicks on a table sort button
 * while the required text field has no value, the sort action is never
 * invoked because a value was required and validation failed. <br>
 * </p>
 * <p style="color: rgb(0, 0, 0);">To prevent this validation issue,
 * you can do either of the following:<br>
 * </p>
 * <ul style="color: rgb(0, 0, 0);">
 * <li>Place the table and the required component in separate forms, to
 * allow the table data to be submitted separately from the required
 * field. The table can then be sorted without triggering a validation
 * failure on a required text field that has no value because the text
 * field's form is not submitted.&nbsp; However, the values of one form
 * are lost when the other form is submitted, which is expected HTML
 * behavior. In this example, if a user places a value in a required text
 * field and then sorts the table, the value in the text field is
 * lost.&nbsp; <br>
 * </li>
 * </ul>
 * <ul style="color: rgb(0, 0, 0);">
 * <li>Place the table in a virtual
 * form by setting the <code>internalVirtualForm</code>
 * method of the Table component to true.&nbsp; For example: <br>
 * <br>
 * &nbsp;&nbsp;&nbsp;&nbsp; <code>// Set table component.<br>
 * &nbsp;&nbsp;&nbsp; public void setTable(Table table) {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; this.table = table;<br>
 * <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; // This binding is used only
 * for the internal virtual form example.<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * table.setInternalVirtualForm(true);<br>
 * &nbsp;&nbsp;&nbsp; }</code><br>
 * <br>
 * A virtual form allows the table data to be
 * submitted separately from the
 * other components on the page, just as using two separate HTML forms
 * would allow.&nbsp; Placing
 * the table in a virtual form allows the table sort action to complete
 * because validation for the required text field is not processed. This
 * behavior is similar to that caused by setting the <code>immediate</code>
 * property of a
 * button, but allows
 * table children to be updated so that selected checkbox values may be
 * sorted, for example. The
 * advantage to using a virtual form is that the values of the other
 * components on the page are not lost when table data is submitted.<br>
 * </li>
 * </ul>
 * <p style="margin-left: 40px; color: rgb(0, 0, 0);">
 * </p>
 * <h4 style="color: rgb(0, 0, 0);"><span style="font-weight: bold;">Value
 * Bindings</span></h4>
 * <span style="color: rgb(0, 0, 0);">Value
 * expressions that use DataProviders must use the following syntax:
 * </span><br style="color: rgb(0, 0, 0);">
 * <code style="color: rgb(0, 0, 0);">#{sourceVar['PERSON.NAME']}<br>
 * #{sourceVar.value['PERSON.NAME']}<br>
 * </code>
 * <p style="color: rgb(0, 0, 0);">Note
 * that the word <code>value</code> is between the DataProvider and the
 * FieldKey to bind. The&nbsp; brackets [] are required
 * only if the FieldKey contains dot syntax.&nbsp; <br>
 * </p>
 * <span style="color: rgb(0, 0, 0);">For example:</span><br
 * style="color: rgb(0, 0, 0);">
 * <ul style="color: rgb(0, 0, 0);">
 * <li>To
 * bind to a FieldKey named <code>last</code>, where the <code>sourceVar</code>
 * property of the table component is <code>names</code>:<br>
 * <p><code>#{names.value.last}</code></p>
 * </li>
 * </ul>
 * <div style="margin-left: 40px; color: rgb(0, 0, 0);"><code></code></div>
 * <code style="color: rgb(0, 0, 0);"></code><br
 * style="color: rgb(0, 0, 0);">
 * <ul style="color: rgb(0, 0, 0);">
 * <li>To
 * bind to a property named <code>test</code>, where the backing bean is
 * named <code>TableBean</code>.</li>
 * </ul>
 * <div style="margin-left: 40px; color: rgb(0, 0, 0);"><code>#{TableBean.test}</code><br>
 * <code></code></div>
 * <h4 style="color: rgb(0, 0, 0);"><span style="font-weight: bold;">Method
 * Bindings</span></h4>
 * <span style="color: rgb(0, 0, 0);">A JavaServer Faces issue prevents
 * method bindings from
 * working in a DataProvider. To work around the issue, you can bind to a
 * method in a backing bean, and use that method to retrieve values from
 * the DataProvider. </span><span
 * style="font-weight: bold; color: rgb(0, 0, 0);"><br>
 * <br>
 * </span><span style="color: rgb(0, 0, 0);">For example, the
 * following
 * syntax to bind to a DataProvider does not work:</span><br
 * style="color: rgb(0, 0, 0);">
 * <br>
 * <code>&lt;ui:hyperlink text="#{name.last}" action="#{name.action}"/&gt;</code><br>
 * <br>
 * Instead of using a DataProvider to handle the action (which breaks the
 * MVC paradigm), bind the action to a method in the backing bean. For
 * example:<br>
 * <br>
 * <code>&lt;ui:hyperlink text="#{name.last}"
 * action="#{TableBean.action}"/&gt;</code><br>
 * <br>
 * In the <code>TableBean.action</code> method, you may invoke the
 * following code to
 * retrieve values from the DataProvider:<br>
 * <br>
 * <code>public String action() {<br>
 * &nbsp;&nbsp;&nbsp; FacesContext context =
 * FacesContext.getCurrentInstance();<br>
 * &nbsp;&nbsp;&nbsp; ValueBinding vb =
 * context.getApplication().createValueBinding("#{name.first}");<br>
 * &nbsp;&nbsp;&nbsp; String first = (String) (vb.getValue(context));<br>
 * &nbsp;&nbsp;&nbsp; return "whatever";<br>
 * }</code><br>
 * <br>
 * If you have an instance of the DataProvider, you can also invoke the
 * following code to retrieve values: <br>
 * <br>
 * <code>public String action() {<br>
 * &nbsp;&nbsp;&nbsp; FacesContext context =
 * FacesContext.getCurrentInstance();<br>
 * &nbsp;&nbsp;&nbsp; ValueBinding vb =
 * context.getApplication().createValueBinding("#{name.tableRow}");<br>
 * &nbsp;&nbsp;&nbsp; RowKey row = (RowKey) (vb.getValue(context));<br>
 * &nbsp;&nbsp;&nbsp; String first = (String)
 * provider.getFieldKey("first"), row)<br>
 * }</code><br>
 * <h4 style="color: rgb(0, 0, 0);"><span style="font-weight: bold;">Logging</span></h4>
 * To see messages logged by the table component, set the following global
 * defaults in your JDK's "jre/lib/logging.properties" file.
 * Alternatively, you can use a different file by specifying a filename
 * with the java.util.logging.config.file system property (e.g., setenv
 * CATALINA_OPTS -Djava.util.logging.config.file=myFile).<br>
 * <br>
 * <code></code><code>java.util.logging.ConsoleHandler.level = FINE <br>
 * com.sun.rave.web.ui.event.TablePaginationActionListener.level = FINE <br>
 * com.sun.rave.web.ui.event.TableSelectPhaseListener.level = FINE <br>
 * com.sun.rave.web.ui.event.TableSortActionListener.level = FINE <br>
 * com.sun.rave.web.ui.component.Table.level = FINE<br>
 * com.sun.rave.web.ui.component.TableActions.level = FINE <br>
 * com.sun.rave.web.ui.component.TableColumn.level = FINE <br>
 * com.sun.rave.web.ui.component.TableFooter.level = FINE <br>
 * com.sun.rave.web.ui.component.TableHeader.level = FINE <br>
 * com.sun.rave.web.ui.component.TablePanels.level = FINE <br>
 * com.sun.rave.web.ui.component.TableRowGroup.level = FINE <br>
 * com.sun.rave.web.ui.renderer.TableActionsRenderer.level = FINE <br>
 * com.sun.rave.web.ui.renderer.TableColumnRenderer.level = FINE <br>
 * com.sun.rave.web.ui.renderer.TableFooterRenderer.level = FINE <br>
 * com.sun.rave.web.ui.renderer.TableHeaderRenderer.level = FINE <br>
 * com.sun.rave.web.ui.renderer.TablePanelsRenderer.level = FINE <br>
 * com.sun.rave.web.ui.renderer.TableRenderer.level = FINE <br>
 * com.sun.rave.web.ui.renderer.TableRowGroupRenderer.level = FINE<br>
 * </code><code></code><br>
 * <h3 style="color: rgb(0, 0, 0);">Examples</h3>
 * <span style="color: rgb(0, 0, 0);">The following examples use a
 * backing bean called </span><a href="table.html#Example:_TableBean_"
 * style="color: rgb(0, 0, 0);">TableBean</a><span
 * style="color: rgb(0, 0, 0);">
 * and </span><a href="table.html#UtilityClasses"
 * style="color: rgb(0, 0, 0);">some
 * utility classes</a><span style="color: rgb(0, 0, 0);">, which are
 * included after the examples.&nbsp;
 * Additional examples are shown in the </span><code
 * style="color: rgb(0, 0, 0);"><a href="tableRowGroup.html">ui:tableRowGroup</a></code><span
 * style="color: rgb(0, 0, 0);">
 * and </span><code style="color: rgb(0, 0, 0);"><a
 * href="tableColumn.html">ui:tableColumn</a></code><span
 * style="color: rgb(0, 0, 0);">
 * documents.</span><br style="color: rgb(0, 0, 0);">
 * <br>
 * <span style="font-weight: bold;">Examples in this file:</span>
 * <div style="margin-left: 40px;">
 * <p><a href="table.html#BasicTable">Example
 * 1: Basic Table</a></p>
 * <p><a href="table.html#CustomTitle">Example
 * 2: Custom Title in Table</a></p>
 * <p><a href="table.html#PaginatedTable">Example
 * 3: Paginated Table</a></p>
 * <p><a href="table.html#TableSortPanel">Example
 * 4: Table Sort Panel</a></p>
 * <p><a href="table.html#TablePreferences">Example
 * 5: Table Preferences</a></p>
 * <p><a href="table.html#TableFilter">Example
 * 6: Table Filter</a></p>
 * <p><a href="table.html#TableActions">Example
 * 7: Table Actions</a></p>
 * </div>
 * <p style="font-weight: bold;">Supporting files:</p>
 * <div style="margin-left: 40px;">
 * <p><a href="table.html#Example:_TableBean_">TableBean
 * backing bean </a></p>
 * <p><a href="table.html#UtilityClasses">Utility
 * classes used in the examples</a></p>
 * </div>
 * <h4><a name="BasicTable"></a>Example 1: Basic Table</h4>
 * This example shows how to create a basic
 * table.<br>
 * <br>
 * <code>&lt;!-- Basic Table --&gt;<br>
 * &lt;ui:table id="table1" title="Basic Table"&gt;<br>
 * &nbsp; &lt;ui:tableRowGroup id="rowGroup1"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; sourceData="#{TableBean.groupB.names}"
 * sourceVar="name"&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;ui:tableColumn id="col1"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; alignKey="last"
 * headerText="Last Name" rowHeader="true"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;ui:staticText
 * text="#{name.value.last}"/&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;/ui:tableColumn&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;ui:tableColumn id="col2" alignKey="first"
 * headerText="First Name"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;ui:staticText
 * text="#{name.value.first}"/&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;/ui:tableColumn&gt;<br>
 * &nbsp; &lt;/ui:tableRowGroup&gt;<br>
 * &lt;/ui:table&gt;</code><br>
 * <br>
 * <a name="CustomTitle"></a><span style="font-weight: bold;">Example 2:
 * Custom Title</span><br>
 * <span style="color: rgb(0, 0, 0);">This example shows how
 * to
 * create a custom title for a table, using the <code></code><code>title</code>
 * facet. When
 * you
 * use the
 * title
 * attribute as shown in the <a href="#BasicTable">BasicTable</a>
 * example, the
 * component provides a default title implementation which can include
 * information regarding paginated rows and applied
 * filters. This example implements the title with a <code>ui:staticText</code>
 * tag in
 * the <code>title</code> facet.</span><br>
 * <code><br>
 * &lt;!-- Custom Title --&gt;<br>
 * &lt;ui:table id="table1"&gt;<br>
 * &nbsp; &lt;ui:tableRowGroup id="rowGroup1" <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; sourceData="#{TableBean.groupB.names}"
 * sourceVar="name"&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;ui:tableColumn id="col1" <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; alignKey="last"
 * headerText="Last Name" rowHeader="true"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;ui:staticText
 * text="#{name.value.last}"/&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;/ui:tableColumn&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;ui:tableColumn id="col2" alignKey="first"
 * headerText="First Name"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;ui:staticText
 * text="#{name.value.first}"/&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;/ui:tableColumn&gt;<br>
 * &nbsp; &lt;/ui:tableRowGroup&gt;<br>
 * &nbsp; &lt;!-- Title --&gt;<br>
 * &nbsp; &lt;f:facet name="title"&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;ui:staticText text="Custom Title"/&gt;<br>
 * &nbsp; &lt;/f:facet&gt;<br>
 * &lt;/ui:table&gt;</code><br>
 * <h4><a name="PaginatedTable"></a>Example 3: Paginated Table<br>
 * </h4>
 * This example shows how to create a paginated
 * table. The default number of rows to be displayed for a paginated table
 * is 25 per page.<span style="color: rgb(102, 102, 204);"> </span>You
 * can override this value with
 * the
 * <code>rows</code> attribute<span style="color: rgb(102, 102, 204);"> </span>in
 * the <code>ui:tableRowGroup</code> tag. <br>
 * <br>
 * Note: The rows attribute is used only for paginated tables.<br>
 * <br>
 * <code>&lt;!-- Paginated Table --&gt;<br>
 * &lt;ui:table id="table1"<br>
 * &nbsp;&nbsp;&nbsp; paginateButton="true"<br>
 * &nbsp;&nbsp;&nbsp; paginationControls="true"<br>
 * &nbsp;&nbsp;&nbsp; title="Paginated Table"&gt;<br>
 * &nbsp; &lt;ui:tableRowGroup id="rowGroup1"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; sourceData="#{TableBean.groupA.names}"
 * sourceVar="name" rows="5"&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;ui:tableColumn id="col1"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; alignKey="last"
 * headerText="Last Name" rowHeader="true"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;ui:staticText
 * text="#{name.value.last}"/&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;/ui:tableColumn&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;ui:tableColumn id="col2" alignKey="first"
 * headerText="First Name"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;ui:staticText
 * text="#{name.value.first}"/&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;/ui:tableColumn&gt;<br>
 * &nbsp; &lt;/ui:tableRowGroup&gt;<br>
 * &lt;/ui:table&gt;</code><br>
 * <h4><a name="TableSortPanel"></a>Example 4: Sort Panel<br>
 * </h4>
 * This example shows how to add
 * the sort panel. You
 * can use the
 * default sort panel by setting the
 * <code>sortPanelToggleButton</code>
 * attribute to true. This button opens an embedded panel in the table,
 * displaying a default implementation.&nbsp;&nbsp; The default
 * implementation shows
 * one, two, or three drop down menus that represent the primary,
 * secondary, and
 * tertiary sorts. The menus
 * list the column headers for each sortable column. If a column is not
 * sortable, it is not shown as a sortable option. If there are
 * only two sortable columns, only the primary and secondary drop down
 * menus are shown. If there is only one sort, only the
 * primary drop down menu is shown. Next to each sort menu is a menu to
 * select ascending or descending sort order.<span
 * style="color: rgb(102, 102, 204);"><br>
 * </span>
 * <p> </p>
 * <code>&lt;!-- Sort Panel --&gt;<br>
 * &lt;ui:table id="table"<br>
 * &nbsp;&nbsp;&nbsp; clearSortButton="true"<br>
 * &nbsp;&nbsp;&nbsp; sortPanelToggleButton="true"<br>
 * &nbsp;&nbsp;&nbsp; title="Sort Panel"&gt;<br>
 * &nbsp; &lt;!-- Insert tableRowGroup tag here --&gt;<br>
 * &lt;/ui:table&gt;</code><br>
 * <br>
 * To override the default sort panel, use the <code>sortPanel</code>
 * facet.&nbsp; The sort panel toggle
 * button is shown when you use the facet, so you do not
 * need to specify the <code>sortPanelToggleButton</code>
 * attribute. <br>
 * <code></code>
 * <h4><a name="TablePreferences"></a>Example 5: Table Preferences<br>
 * </h4>
 * <code></code>This example shows how
 * to add the preferences toggle button and the table panel. The
 * preferences panel toggle button is
 * shown only when you use the <code>preferencesPanel</code>
 * facet.
 * The button opens an embedded panel in the table, displaying the
 * contents that you provide in the <code>preferencesPanel</code> facet. <br
 * style="color: rgb(102, 102, 204);">
 * <br style="color: rgb(102, 102, 204);">
 * In this example, the preferences panel is used to set
 * the number
 * of paginated rows with the rows attribute of <code>ui:tableRowGroup</code>.
 * See the <a href="#Preferences.java">Preferences.java</a> example
 * utility class, which provides functionality for preferences for
 * this example.<br>
 * <code></code><br>
 * <code>&lt;!-- Preferences --&gt;<br>
 * &lt;ui:table id="table1" paginationControls="true"
 * title="Preferences"&gt;<br>
 * &nbsp; &lt;ui:tableRowGroup id="rowGroup1"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * rows="#{TableBean.groupA.preferences.rows}"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; sourceData="#{TableBean.groupA.names}"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; sourceVar="name"&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;ui:tableColumn id="col1"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; alignKey="last"
 * headerText="Last Name" rowHeader="true"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;ui:staticText
 * text="#{name.value.last}"/&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;/ui:tableColumn&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;ui:tableColumn id="col2" alignKey="first"
 * headerText="First Name"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;ui:staticText
 * text="#{name.value.first}"/&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;/ui:tableColumn&gt;<br>
 * &nbsp; &lt;/ui:tableRowGroup&gt;<br>
 * <br>
 * &nbsp; &lt;!-- Preferences Panel --&gt;<br>
 * &nbsp; &lt;f:facet name="preferencesPanel"&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;f:subview id="preferencesPanel"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;jsp:include
 * page="preferencesPanel.jsp"/&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;/f:subview&gt;<br>
 * &nbsp; &lt;/f:facet&gt;<br>
 * &lt;/ui:table&gt;</code><br>
 * <br style="color: rgb(102, 102, 204);">
 * <span style="font-weight: bold;">preferencesPanel.jsp</span><br
 * style="color: rgb(102, 102, 204);">
 * <br>
 * This example shows the contents of the <code>preferencesPanel.jsp</code>
 * file included in the JSP page example above.<br>
 * <br>
 * <code>&lt;!-- Preferences Panel --&gt;<br>
 * &lt;ui:textField id="rows"<br>
 * &nbsp;&nbsp;&nbsp; columns="5"<br>
 * &nbsp;&nbsp;&nbsp; label="Rows Per Page:"<br>
 * &nbsp;&nbsp;&nbsp; labelLevel="2"<br>
 * &nbsp;&nbsp;&nbsp; onKeyPress="if (event.keyCode==13) {var
 * e=document.getElementById('form1:table1:preferencesPanel:submit'); if
 * (e != null) e.click(); return false}"<br>
 * &nbsp;&nbsp;&nbsp;
 * text="#{TableBean.groupA.preferences.preference}"/&gt;<br>
 * &lt;ui:markup tag="div" styleClass="TblPnlBtnDiv"&gt;<br>
 * &nbsp; &lt;ui:button id="submit"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * action="#{TableBean.groupA.preferences.applyPreferences}"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; mini="true"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; primary="true"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; text="OK"/&gt;<br>
 * &nbsp; &lt;ui:button id="cancel"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; mini="true"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; onClick="togglePreferencesPanel();
 * return false"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; text="Cancel"/&gt;<br>
 * &lt;/ui:markup&gt;<br>
 * <br>
 * &lt;!-- Note: If the user presses the enter key while the text field
 * has focus,<br>
 * &nbsp;&nbsp;&nbsp;&nbsp; the page will be submitted incorrectly, unless
 * we capture the onKeyPress<br>
 * &nbsp;&nbsp;&nbsp;&nbsp; event and invoke the click method of the
 * submit button. --&gt;</code><br>
 * <br>
 * <span style="font-weight: bold;">preferences.js</span><br>
 * <br>
 * This example shows the contents of the preferences.js file used in the <code>tablePreferencesPanel.jsp</code>&nbsp;
 * example
 * above. The <code>togglePreferencesPanel()</code> JavaScript function
 * is used with
 * the <code>onClick</code> attribute of a <code>ui:button</code> tag.<br>
 * <br>
 * <code>// Use this function to toggle the preferences panel open or
 * closed. This<br>
 * // functionality requires the filterId of the table component to be set.<br>
 * function togglePreferencesPanel() {<br>
 * &nbsp;&nbsp;&nbsp; var table = document.getElementById("form1:table1");<br>
 * &nbsp;&nbsp;&nbsp; table.togglePreferencesPanel();<br>
 * }</code><br>
 * <h4><a name="TableFilter"></a>Example 6: Table Filter<br>
 * </h4>
 * <p>This example shows how to add filters using a filter drop down
 * menu and the filter panel. In this example, the filter
 * panel is used to set a custom filter.
 * The custom filter removes all rows from the view that do not match the
 * given last name. <span style="color: rgb(0, 0, 0);">See the </span><a
 * href="#Filter.java" style="color: rgb(0, 0, 0);">Filter.java</a><span
 * style="color: rgb(0, 0, 0);">
 * example, which provides the&nbsp; functionality for filters for
 * this example.</span></p>
 * <p style="color: rgb(0, 0, 0);">Basic
 * filters
 * are filters that
 * you define for the users of your application. Custom filters enable
 * users to specify the data to be used by the table component to
 * determine which table entries to display.&nbsp; You specify the basic
 * filter names as items in a <a href="dropDown.html"><code>ui:dropDown</code>
 * </a>tag in the <code>filter</code>
 * facet. If you want to allow users to use a custom filter, include a
 * "Custom Filter" item as one of the <code>ui:dropDown</code>
 * items. <br>
 * </p>
 * <p style="color: rgb(0, 0, 0);">If you
 * include a "Custom Filter" item in
 * the Filter drop down menu, you can allow users to
 * open a filter panel to filter the table data using a custom
 * filter.&nbsp; When the Custom
 * Filter option is selected, an embedded panel in the table is
 * opened,
 * displaying the contents that you provide in the <code>filterPanel</code>
 * facet. The default
 * custom filter functionality requires you to use the
 * <code>filterMenuChanged</code> JavaScript function for the <code>onChange</code>
 * event in the <code>ui:dropDown</code> tag.&nbsp; After the custom
 * filter is
 * applied, you should display a non-selectable "Custom
 * Filter Applied" item in
 * the
 * filter drop down menu,&nbsp;
 * to indicate that a custom filter has been applied. You should also set
 * the <code>filterText</code> attribute. The table component updates the
 * table title to
 * indicate that a basic or custom
 * filter has been applied by inserting the text <span
 * style="font-style: italic;">&lt;filterText&gt;</span> Filter Applied. </p>
 * <p style="color: rgb(0, 0, 0);">The
 * default
 * custom filter
 * functionality depends on a specific value assigned to the
 * Custom Filter item in the dropDown component. The table component
 * provides a method named <code>getFilterOptions()</code> to add the
 * item text and value for the custom filter option to the drop down list.
 * You can use <code>getFilterOptions()</code> in your backing
 * bean to append either the
 * "Custom Filter" or "Custom Filter Applied" item to the basic filter
 * items. <br>
 * </p>
 * <p style="color: rgb(0, 0, 0);">In the <a href="#Filter.java">Filter.java</a>
 * util example, the
 * "Custom Filter" and "Custom Filter Applied" items are
 * assigned
 * based on&nbsp;the
 * boolean value <code style="text-decoration: line-through;"></code>
 * provided
 * to <code>getFilterOptions(</code><code>)</code>. If
 * this&nbsp;value is true,
 * the
 * "Custom Filter Applied" item value is added. If the value is false,
 * "Custom Filter" item value is added. The <code>filterMenuChanged</code>
 * JavaScript function, assigned to the dropDown component's <code>onChange</code>
 * event,&nbsp; behaves differently depending on
 * which item is added.&nbsp; See the comments in <a href="#filters.js">filters.js&nbsp;</a>for
 * more information. <br>
 * </p>
 * <p style="color: rgb(0, 0, 0);">The
 * default custom filter functionality
 * also depends on the id of the
 * <code>ui:dropDown</code> tag in the <code>filter</code>
 * facet. Note that if you use the <code>ui:dropDown</code> tag as the
 * only component
 * in the filter facet, the <code>filterId</code> is optional. If you use
 * a custom
 * component, or use the <code>ui:dropDown</code> as a child component,
 * you must
 * specify a filterID.</p>
 * <span style="color: rgb(0, 0, 0);">The table
 * implements functionality to reset the dropDown menu. If you
 * use the <code>filterId</code> attribute, the menu can be reset
 * whenever the sort and preferences toggle buttons are clicked. You can
 * also use the filterMenuChanged JavaScript function to reset the menu at
 * some other time.&nbsp;&nbsp; Note:
 * This functionality requires the </span><code
 * style="color: rgb(0, 0, 0);">selected</code><span
 * style="color: rgb(0, 0, 0);"> value of the <code>ui:dropDown</code>
 * tag to be set in order
 * to
 * restore the default selected value when the embedded filter panel is
 * closed.</span><br style="color: rgb(0, 0, 0);">
 * <span style="color: rgb(0, 0, 0);"><br>
 * </span><span style="color: rgb(102, 102, 204);"><span
 * style="color: rgb(0, 0, 0);">The filter
 * code can be placed in a util class, as shown in the </span><a
 * href="#Filter.java" style="color: rgb(0, 0, 0);">Filter.java</a><span
 * style="color: rgb(0, 0, 0);"> example, or in a backing bean.</span><span
 * style="font-weight: bold;"><br>
 * </span></span><span style="color: rgb(102, 102, 204);"></span><br>
 * <code>&lt;!-- Filter --&gt;<br>
 * &lt;ui:table id="table1"<br>
 * &nbsp;&nbsp;&nbsp; filterText="#{TableBean.groupA.filter.filterText}"<br>
 * &nbsp;&nbsp;&nbsp; paginateButton="true"<br>
 * &nbsp;&nbsp;&nbsp; paginationControls="true"<br>
 * &nbsp;&nbsp;&nbsp; title="Filter"&gt;<br>
 * &nbsp; &lt;ui:tableRowGroup id="rowGroup1"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * binding="#{TableBean.groupA.tableRowGroup}"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; rows="5"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; sourceData="#{TableBean.groupA.names}"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; sourceVar="name"&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;ui:tableColumn id="col1"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; alignKey="last"
 * headerText="Last Name" rowHeader="true"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;ui:staticText
 * text="#{name.value.last}"/&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;/ui:tableColumn&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;ui:tableColumn id="col2" alignKey="first"
 * headerText="First Name"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;ui:staticText
 * text="#{name.value.first}"/&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;/ui:tableColumn&gt;<br>
 * &nbsp; &lt;/ui:tableRowGroup&gt;<br>
 * <br>
 * &nbsp; &lt;!-- Filter --&gt;<br>
 * &nbsp; &lt;f:facet name="filter"&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;ui:dropDown submitForm="true" id="filter"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * action="#{TableBean.groupA.filter.applyBasicFilter}" <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * items="#{TableBean.groupA.filter.filterOptions}"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; onChange="if
 * (filterMenuChanged() == false) return false"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * selected="#{TableBean.groupA.filter.basicFilter}"/&gt;<br>
 * &nbsp; &lt;/f:facet&gt;<br>
 * <br>
 * &nbsp; &lt;!-- Filter Panel --&gt;<br>
 * &nbsp; &lt;f:facet name="filterPanel"&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;f:subview id="filterPanel"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;jsp:include
 * page="filterPanel.jsp"/&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;/f:subview&gt;<br>
 * &nbsp; &lt;/f:facet&gt;<br>
 * &lt;/ui:table&gt;</code><br>
 * <br>
 * <span style="font-weight: bold;">filterPanel.jsp</span><br>
 * <br>
 * This example shows the contents of the <code>filterPanel.jsp</code><span
 * style="font-weight: bold;"> </span>file included in the JSP page in
 * the example above.<br>
 * <code><br>
 * &lt;!-- Filter Panel --&gt;<br>
 * &lt;ui:textField id="customFilter"<br>
 * &nbsp;&nbsp;&nbsp; columns="50"<br>
 * &nbsp;&nbsp;&nbsp; label="Show only rows containing last name:"<br>
 * &nbsp;&nbsp;&nbsp; labelLevel="2"<br>
 * &nbsp;&nbsp;&nbsp; onKeyPress="if (event.keyCode==13) {var
 * e=document.getElementById('form1:table1:filterPanel:submit'); if (e !=
 * null) e.click(); return false}"<br>
 * &nbsp;&nbsp;&nbsp; text="#{TableBean.groupA.filter.customFilter}"/&gt;<br>
 * &lt;ui:markup tag="div" styleClass="TblPnlBtnDiv"&gt;<br>
 * &nbsp; &lt;ui:button id="submit"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * action="#{TableBean.groupA.filter.applyCustomFilter}"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; mini="true"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; primary="true"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; text="OK"/&gt;<br>
 * &nbsp; &lt;ui:button id="cancel"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; mini="true"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; onClick="toggleFilterPanel(); return
 * false"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; text="Cancel"/&gt;<br>
 * &lt;/ui:markup&gt;<br>
 * <br>
 * &lt;!-- Note: If the user presses the enter key while the text field
 * has focus,<br>
 * &nbsp;&nbsp;&nbsp;&nbsp; the page will be submitted incorrectly, unless
 * we capture the onKeyPress<br>
 * &nbsp;&nbsp;&nbsp;&nbsp; event and invoke the click method of the
 * submit button. --&gt;</code><br>
 * <br>
 * <span style="font-weight: bold;"><a name="filters.js"></a>filters.js</span><br>
 * <br>
 * This example shows the contents of the <code>filters.js</code><span
 * style="font-weight: bold;"> </span>file used in the <code>filterPanel.jsp<br>
 * </code>example above. The <code>toggleFilterPanel()</code>
 * JavaScript function is used with the <code>onClick</code> attribute of
 * a <code>ui:button</code> tag to allow the user to close the
 * filter panel without specifying a filter.<br>
 * <br>
 * <code>// Toggle the filter panel from the filter menu.<br>
 * //<br>
 * // If the "Custom Filter" option has been selected, the filter panel is
 * <br>
 * // toggled. In this scenario, false is returned indicating the onChange
 * event,<br>
 * // generated by the filter menu, should not be allowed to continue.<br>
 * // <br>
 * // If the "Custom Filter Applied" option has been selected, no action
 * is taken.<br>
 * // Instead, the filter menu is reverted back to the original selection.
 * In this<br>
 * // scenario, false is also returned indicating the onChange event,
 * generated by<br>
 * // the filter menu, should not be allowed to continue.<br>
 * //<br>
 * // For all other selections, true is returned indicating the onChange
 * event, <br>
 * // generated by the filter menu, should be allowed to continue.<br>
 * function filterMenuChanged() {<br>
 * &nbsp;&nbsp;&nbsp; var table = document.getElementById("form1:table1");<br>
 * &nbsp;&nbsp;&nbsp; return table.filterMenuChanged();<br>
 * }<br>
 * <br>
 * // Use this function to toggle the filter panel open or closed. This<br>
 * // functionality requires the filterId of the table component to be
 * set. In <br>
 * // addition, the selected value must be set as well to restore the
 * default<br>
 * // selected value when the embedded filter panel is closed.<br>
 * function toggleFilterPanel() {<br>
 * &nbsp;&nbsp;&nbsp; var table = document.getElementById("form1:table1");<br>
 * &nbsp;&nbsp;&nbsp; table.toggleFilterPanel();<br>
 * }</code><br>
 * <h4><a name="TableActions"></a>Example 7: Table Actions<br>
 * </h4>
 * <span style="color: rgb(0, 0, 0);">This example shows how to add
 * actions </span><span style="color: rgb(102, 102, 204);"><span
 * style="color: rgb(0, 0, 0);">to a table by using the </span><code
 * style="color: rgb(0, 0, 0);">actionsTop</code><span
 * style="color: rgb(0, 0, 0);">
 * and </span><code style="color: rgb(0, 0, 0);">actionsBottom</code><span
 * style="color: rgb(0, 0, 0);">
 * facets. Four buttons and a drop down menu are added to the Action Bar
 * (top) and Action Bar (bottom). When the page is initially
 * displayed, all actions
 * are disabled. When the user selects at least one checkbox, the actions
 * are enabled. If the user deselects all checkboxes, the actions are
 * disabled again. </span><br>
 * <br>
 * <span style="color: rgb(0, 0, 0);">Note that this example defines a
 * JavaScript function called </span><code style="color: rgb(0, 0, 0);">disableActions</code><span
 * style="color: rgb(0, 0, 0);">, which is shown in </span><a
 * href="table.html#actions.js" style="color: rgb(0, 0, 0);">actions.js.
 * </a><span style="color: rgb(0, 0, 0);">The </span></span><code
 * style="color: rgb(0, 0, 0);">disableActions</code><span
 * style="color: rgb(0, 0, 0);"> function is
 * defined by the developer, and is not part of the table component. </span><br
 * style="color: rgb(0, 0, 0);">
 * <br style="color: rgb(0, 0, 0);">
 * <span style="color: rgb(0, 0, 0);">When the
 * user clicks on a checkbox, a JavaScript <code>disableActions</code>
 * function is invoked
 * with the <code>onClick</code> event.&nbsp; The JavaScript <code>setTimeout</code>
 * function
 * is used to ensure checkboxes are selected immediately, instead of
 * waiting for
 * the JavaScript function to complete.
 * See the <a href="#Actions.java">Actions.java</a> example, which
 * provides functionality for table actions in this example.</span><br
 * style="color: rgb(0, 0, 0);">
 * <br>
 * <code>&lt;!-- Actions --&gt;<br>
 * &lt;ui:table id="table1"<br>
 * &nbsp;&nbsp;&nbsp; deselectMultipleButton="true"<br>
 * &nbsp;&nbsp;&nbsp;
 * deselectMultipleButtonOnClick="setTimeout('disableActions()', 0)"<br>
 * &nbsp;&nbsp;&nbsp; paginateButton="true"<br>
 * &nbsp;&nbsp;&nbsp; paginationControls="true"<br>
 * &nbsp;&nbsp;&nbsp; selectMultipleButton="true"<br>
 * &nbsp;&nbsp;&nbsp;
 * selectMultipleButtonOnClick="setTimeout('disableActions()', 0)"<br>
 * &nbsp;&nbsp;&nbsp; title="Actions"&gt;<br>
 * &nbsp; &lt;ui:tableRowGroup id="rowGroup1"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * binding="#{TableBean.groupA.tableRowGroup}"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; rows="5"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * selected="#{TableBean.groupA.select.selectedState}"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; sourceData="#{TableBean.groupA.names}"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; sourceVar="name"&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;ui:tableColumn id="col0"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; selectId="select"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * sort="#{TableBean.groupA.select.selectedState}"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;ui:checkbox id="select"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * onClick="setTimeout('initAllRows(); disableActions()', 0)"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * selected="#{TableBean.groupA.select.selected}"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * selectedValue="#{TableBean.groupA.select.selectedValue}"/&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;/ui:tableColumn&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;ui:tableColumn id="col1" <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; alignKey="last"
 * headerText="Last Name" rowHeader="true"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;ui:staticText
 * text="#{name.value.last}"/&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;/ui:tableColumn&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;ui:tableColumn id="col2" alignKey="first"
 * headerText="First Name"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;ui:staticText
 * text="#{name.value.first}"/&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;/ui:tableColumn&gt;<br>
 * &nbsp; &lt;/ui:tableRowGroup&gt;<br>
 * <br>
 * &nbsp; &lt;!-- Actions (Top) --&gt;<br>
 * &nbsp; &lt;f:facet name="actionsTop"&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;f:subview id="actionsTop"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;jsp:include
 * page="actionsTop.jsp"/&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;/f:subview&gt;<br>
 * &nbsp; &lt;/f:facet&gt;<br>
 * <br>
 * &nbsp; &lt;!-- Actions (Bottom) --&gt;<br>
 * &nbsp; &lt;f:facet name="actionsBottom"&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;f:subview id="actionsBottom"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;jsp:include
 * page="actionsBottom.jsp"/&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;/f:subview&gt;<br>
 * &nbsp; &lt;/f:facet&gt;<br>
 * &lt;/ui:table&gt;</code><code><br>
 * <br>
 * </code><span style="font-weight: bold;">actionsTop.jsp</span><br>
 * <br>
 * This example shows the contents of the <code>actionsTop.jsp</code><span
 * style="font-weight: bold;"> </span>file included&nbsp; in the <code>actionsTop</code>
 * facet in the JSP page in the example above.<br>
 * <br>
 * <code>&lt;!-- Actions (Top) --&gt;<br>
 * &lt;ui:button id="action1"<br>
 * &nbsp;&nbsp;&nbsp; action="#{TableBean.groupA.actions.delete}"<br>
 * &nbsp;&nbsp;&nbsp; disabled="#{TableBean.groupA.actions.disabled}"<br>
 * &nbsp;&nbsp;&nbsp; onClick="if (confirmDeleteSelectedRows() == false)
 * return false"<br>
 * &nbsp;&nbsp;&nbsp; text="Delete"/&gt;<br>
 * &lt;ui:button id="action2"<br>
 * &nbsp;&nbsp;&nbsp; action="#{TableBean.groupA.actions.action}"<br>
 * &nbsp;&nbsp;&nbsp; disabled="#{TableBean.groupA.actions.disabled}"<br>
 * &nbsp;&nbsp;&nbsp; onClick="if (confirmSelectedRows() == false) return
 * false"<br>
 * &nbsp;&nbsp;&nbsp; text="Action 2"/&gt;<br>
 * &lt;ui:button id="action3"<br>
 * &nbsp;&nbsp;&nbsp; action="#{TableBean.groupA.actions.action}"<br>
 * &nbsp;&nbsp;&nbsp; disabled="#{TableBean.groupA.actions.disabled}"<br>
 * &nbsp;&nbsp;&nbsp; onClick="if (confirmSelectedRows() == false) return
 * false"<br>
 * &nbsp;&nbsp;&nbsp; text="Action 3"/&gt;<br>
 * &lt;ui:button id="action4"<br>
 * &nbsp;&nbsp;&nbsp; action="#{TableBean.groupA.actions.action}"<br>
 * &nbsp;&nbsp;&nbsp; disabled="#{TableBean.groupA.actions.disabled}"<br>
 * &nbsp;&nbsp;&nbsp; onClick="if (confirmSelectedRows() == false) return
 * false"<br>
 * &nbsp;&nbsp;&nbsp; text="Action 4"/&gt;<br>
 * &lt;ui:dropDown submitForm="true" id="moreActions"<br>
 * &nbsp;&nbsp;&nbsp; action="#{TableBean.groupA.actions.moreActions}"<br>
 * &nbsp;&nbsp;&nbsp; disabled="#{TableBean.groupA.actions.disabled}"<br>
 * &nbsp;&nbsp;&nbsp;
 * items="#{TableBean.groupA.actions.moreActionsOptions}"<br>
 * &nbsp;&nbsp;&nbsp;
 * selected="#{TableBean.groupA.actions.moreActions}"/&gt;</code><br>
 * <br>
 * <span style="font-weight: bold;">actionsBottom.jsp</span><br>
 * <br>
 * This example shows the contents of the <code>actionsBottom.jsp</code><span
 * style="font-weight: bold;"> </span>file included&nbsp; in the <code>actionsBottom</code>
 * facet in the JSP page in the example above.<br>
 * <br>
 * <code>&lt;!-- Actions (Bottom) --&gt;<br>
 * &lt;ui:button id="action1"<br>
 * &nbsp;&nbsp;&nbsp; action="#{TableBean.groupA.actions.delete}"<br>
 * &nbsp;&nbsp;&nbsp; disabled="#{TableBean.groupA.actions.disabled}"<br>
 * &nbsp;&nbsp;&nbsp; onClick="if (confirmDeleteSelectedRows() == false)
 * return false"<br>
 * &nbsp;&nbsp;&nbsp; text="Delete"/&gt;<br>
 * &lt;ui:button id="action2"<br>
 * &nbsp;&nbsp;&nbsp; action="#{TableBean.groupA.actions.action}"<br>
 * &nbsp;&nbsp;&nbsp; disabled="#{TableBean.groupA.actions.disabled}"<br>
 * &nbsp;&nbsp;&nbsp; onClick="if (confirmSelectedRows() == false) return
 * false"<br>
 * &nbsp;&nbsp;&nbsp; text="Action 2"/&gt;<br>
 * &lt;ui:button id="action3"<br>
 * &nbsp;&nbsp;&nbsp; action="#{TableBean.groupA.actions.action}"<br>
 * &nbsp;&nbsp;&nbsp; disabled="#{TableBean.groupA.actions.disabled}"<br>
 * &nbsp;&nbsp;&nbsp; onClick="if (confirmSelectedRows() == false) return
 * false"<br>
 * &nbsp;&nbsp;&nbsp; text="Action 3"/&gt;<br>
 * &lt;ui:button id="action4"<br>
 * &nbsp;&nbsp;&nbsp; action="#{TableBean.groupA.actions.action}"<br>
 * &nbsp;&nbsp;&nbsp; disabled="#{TableBean.groupA.actions.disabled}"<br>
 * &nbsp;&nbsp;&nbsp; onClick="if (confirmSelectedRows() == false) return
 * false"<br>
 * &nbsp;&nbsp;&nbsp; text="Action 4"/&gt;<br>
 * &lt;ui:dropDown submitForm="true" id="moreActions"<br>
 * &nbsp;&nbsp;&nbsp; action="#{TableBean.groupA.actions.moreActions}"<br>
 * &nbsp;&nbsp;&nbsp; disabled="#{TableBean.groupA.actions.disabled}"<br>
 * &nbsp;&nbsp;&nbsp;
 * items="#{TableBean.groupA.actions.moreActionsOptions}"<br>
 * &nbsp;&nbsp;&nbsp;
 * selected="#{TableBean.groupA.actions.moreActions}"/&gt;</code><br>
 * <br>
 * <span style="font-weight: bold;">select.js</span><br>
 * <br>
 * This example shows the contents of the <code>select.js</code><span
 * style="font-weight: bold;"> </span>file used in the example above.<br>
 * <br>
 * <code>// Use this function to initialize all rows displayed in the
 * table when the<br>
 * // state of selected components change (i.e., checkboxes or
 * radiobuttons used to<br>
 * // de/select rows of the table). This functionality requires the
 * selectId <br>
 * // property of the tableColumn component to be set.<br>
 * // <br>
 * // Note: Use setTimeout when invoking this function. This will ensure
 * that <br>
 * // checkboxes and radiobutton are selected immediately, instead of
 * waiting for <br>
 * // the onClick event to complete. For example: <br>
 * //<br>
 * // onClick="setTimeout('initAllRows(); disableActions()', 0)"<br>
 * function initAllRows() {<br>
 * &nbsp;&nbsp;&nbsp; // Disable table actions by default.<br>
 * &nbsp;&nbsp;&nbsp; var table = document.getElementById("form1:table1");<br>
 * &nbsp;&nbsp;&nbsp; table.initAllRows();<br>
 * }</code><br>
 * <br>
 * <span style="font-weight: bold;"><a name="actions.js"></a></span><span
 * style="font-weight: bold;">actions.js</span><br>
 * <br>
 * This example shows the contents of the <code>actions.js</code><span
 * style="font-weight: bold;"> </span>file used in the example above.<br>
 * <br>
 * <code>// Set disabled state of table actions. If a selection has been
 * made, actions<br>
 * // are enabled. If no selection has been made, actions are disabled.<br>
 * // <br>
 * // Note: Use setTimeout when invoking this function. This will ensure
 * that <br>
 * // checkboxes and radiobutton are selected immediately, instead of
 * waiting for <br>
 * // the onClick event to complete. For example: <br>
 * //<br>
 * // onClick="setTimeout('initAllRows(); disableActions()', 0)"<br>
 * function disableActions() {<br>
 * &nbsp;&nbsp;&nbsp; // Disable table actions by default.<br>
 * &nbsp;&nbsp;&nbsp; var table = document.getElementById("form1:table1");<br>
 * &nbsp;&nbsp;&nbsp; var selections = table.getAllSelectedRowsCount(); //
 * Hidden &amp; visible selections.<br>
 * &nbsp;&nbsp;&nbsp; var disabled = (selections &gt; 0) ? false : true;<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Set disabled state for top actions.<br>
 * &nbsp;&nbsp;&nbsp;
 * document.getElementById("form1:table1:actionsTop:action1").setDisabled(disabled);<br>
 * &nbsp;&nbsp;&nbsp;
 * document.getElementById("form1:table1:actionsTop:action2").setDisabled(disabled);<br>
 * &nbsp;&nbsp;&nbsp;
 * document.getElementById("form1:table1:actionsTop:action3").setDisabled(disabled);<br>
 * &nbsp;&nbsp;&nbsp;
 * document.getElementById("form1:table1:actionsTop:action4").setDisabled(disabled);<br>
 * &nbsp;&nbsp;&nbsp;
 * dropDown_setDisabled("form1:table1:actionsTop:moreActions", disabled);<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Set disabled state for bottom actions.<br>
 * &nbsp;&nbsp;&nbsp;
 * document.getElementById("form1:table1:actionsBottom:action1").setDisabled(disabled);<br>
 * &nbsp;&nbsp;&nbsp;
 * document.getElementById("form1:table1:actionsBottom:action2").setDisabled(disabled);<br>
 * &nbsp;&nbsp;&nbsp;
 * document.getElementById("form1:table1:actionsBottom:action3").setDisabled(disabled);<br>
 * &nbsp;&nbsp;&nbsp;
 * document.getElementById("form1:table1:actionsBottom:action4").setDisabled(disabled);<br>
 * &nbsp;&nbsp;&nbsp;
 * dropDown_setDisabled("form1:table1:actionsBottom:moreActions",
 * disabled);<br>
 * }<br>
 * <br>
 * //<br>
 * // Use this function to confirm the number of selected components
 * (i.e., <br>
 * // checkboxes or radiobuttons used to de/select rows of the table),
 * affected by<br>
 * // a delete action. This functionality requires the selectId property
 * of the<br>
 * // tableColumn component and hiddenSelectedRows property of the
 * tableRowGroup<br>
 * // component to be set.<br>
 * // <br>
 * // If selections are hidden from view, the confirmation message
 * indicates the<br>
 * // number of selections not displayed in addition to the total number of<br>
 * // selections. If selections are not hidden, the confirmation message
 * indicates<br>
 * // only the total selections.<br>
 * function confirmDeleteSelectedRows() {<br>
 * &nbsp;&nbsp;&nbsp; var table = document.getElementById("form1:table1");<br>
 * &nbsp;&nbsp;&nbsp; return table.confirmDeleteSelectedRows();<br>
 * }<br>
 * <br>
 * // Use this function to confirm the number of selected components
 * (i.e., <br>
 * // checkboxes or radiobuttons used to de/select rows of the table),
 * affected by<br>
 * // an action such as edit, archive, etc. This functionality requires
 * the <br>
 * // selectId property of the tableColumn component and hiddenSelectedRows<br>
 * // property of the tableRowGroup component to be set.<br>
 * // <br>
 * // If selections are hidden from view, the confirmation message
 * indicates the<br>
 * // number of selections not displayed in addition to the total number of<br>
 * // selections. If selections are not hidden, the confirmation message
 * indicates<br>
 * // only the total selections.<br>
 * function confirmSelectedRows() {<br>
 * &nbsp;&nbsp;&nbsp; var table = document.getElementById("form1:table1");<br>
 * &nbsp;&nbsp;&nbsp; return table.confirmSelectedRows("\n\nArchive all
 * selections?");<br>
 * }</code><br>
 * <h3>faces_config.xml Entry for Managed Bean</h3>
 * The previous examples are based on managed beans, such as the example
 * below, added to the<code>
 * faces_config.xml </code>file.<br>
 * <br>
 * <code>&lt;!DOCTYPE faces-config PUBLIC<br>
 * &nbsp;&nbsp;&nbsp; '-//Sun Microsystems, Inc.//DTD JavaServer Faces
 * Config 1.0//EN'<br>
 * &nbsp;&nbsp;&nbsp; 'http://java.sun.com/dtd/web-facesconfig_1_1.dtd'&gt;<br>
 * <br>
 * &lt;faces-config&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;managed-bean&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;description&gt;The
 * backing bean for the table example&lt;/description&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &lt;managed-bean-name&gt;TableBean&lt;/managed-bean-name&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &lt;managed-bean-class&gt;table.TableBean&lt;/managed-bean-class&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &lt;managed-bean-scope&gt;session&lt;/managed-bean-scope&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;/managed-bean&gt;<br>
 * &lt;/faces-config&gt;</code><br>
 * <h3><a name="Example:_TableBean_"></a>TableBean<br>
 * </h3>
 * <code>package table;<br>
 * <br>
 * import com.sun.rave.web.ui.component.Alarm;<br>
 * <br>
 * import java.util.ArrayList;<br>
 * <br>
 * import table.util.Group;<br>
 * import table.util.Name;<br>
 * <br>
 * // Backing bean for table examples.<br>
 * public class TableBean {<br>
 * &nbsp;&nbsp;&nbsp; // Group util for table examples.<br>
 * &nbsp;&nbsp;&nbsp; private Group groupA = null; // List (rows 0-19).<br>
 * &nbsp;&nbsp;&nbsp; private Group groupB = null; // Array (rows 0-9).<br>
 * &nbsp;&nbsp;&nbsp; private Group groupC = null; // Array (rows 10-19).<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Alarms.<br>
 * &nbsp;&nbsp;&nbsp; private static final Alarm down = new
 * Alarm(Alarm.SEVERITY_DOWN);<br>
 * &nbsp;&nbsp;&nbsp; private static final Alarm critical = new
 * Alarm(Alarm.SEVERITY_CRITICAL);<br>
 * &nbsp;&nbsp;&nbsp; private static final Alarm major = new
 * Alarm(Alarm.SEVERITY_MAJOR);<br>
 * &nbsp;&nbsp;&nbsp; private static final Alarm minor = new
 * Alarm(Alarm.SEVERITY_MINOR);<br>
 * &nbsp;&nbsp;&nbsp; private static final Alarm ok = new
 * Alarm(Alarm.SEVERITY_OK);<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Data for table examples.<br>
 * &nbsp;&nbsp;&nbsp; protected static final Name[] names = {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; new Name("William",
 * "Dupont", down),<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; new Name("Anna", "Keeney",
 * critical),<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; new Name("Mariko", "Randor",
 * major),<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; new Name("John", "Wilson",
 * minor),<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; new Name("Lynn",
 * "Seckinger", ok),<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; new Name("Richard",
 * "Tattersall", down),<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; new Name("Gabriella",
 * "Sarintia", critical),<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; new Name("Lisa", "Hartwig",
 * major),<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; new Name("Shirley", "Jones",
 * minor),<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; new Name("Bill", "Sprague",
 * ok),<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; new Name("Greg", "Doench",
 * down),<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; new Name("Solange",
 * "Nadeau", critical),<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; new Name("Heather",
 * "McGann", major),<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; new Name("Roy", "Martin",
 * minor),<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; new Name("Claude",
 * "Loubier", ok),<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; new Name("Dan", "Woodard",
 * down),<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; new Name("Ron", "Dunlap",
 * critical),<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; new Name("Keith",
 * "Frankart", major),<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; new Name("Andre", "Nadeau",
 * minor),<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; new Name("Horace",
 * "Celestin", ok),<br>
 * &nbsp;&nbsp;&nbsp; };<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Default constructor.<br>
 * &nbsp;&nbsp;&nbsp; public TableBean() {<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Get Group util created with a List containing all
 * names.<br>
 * &nbsp;&nbsp;&nbsp; public Group getGroupA() {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; if (groupA != null) {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * return groupA;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; }<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; // Create List with all
 * names.<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; ArrayList newNames = new
 * ArrayList();<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; for (int i = names.length -
 * 1; i &gt;= 0; i--) {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * newNames.add(names[i]);<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; }<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; return (groupA = new
 * Group(newNames));<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Get Group util created with an array containing a
 * subset of names.<br>
 * &nbsp;&nbsp;&nbsp; public Group getGroupB() {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; if (groupB != null) {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * return groupB;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; }<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; // Create an array with
 * subset of names (i.e., 0-9).<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; Name[] newNames = new
 * Name[10];<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; System.arraycopy(names, 0,
 * newNames, 0, 10);<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; return (groupB = new
 * Group(newNames));<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Get Group util created with an array containing a
 * subset of names.<br>
 * &nbsp;&nbsp;&nbsp; public Group getGroupC() {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; if (groupC != null) {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * return groupC;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; }<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; // Create an array with
 * subset of names (i.e., 10-19).<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; Name[] newNames = new
 * Name[10];<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; System.arraycopy(names, 10,
 * newNames, 0, 10);<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; return (groupC = new
 * Group(newNames));<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * }</code><br>
 * <h3><a name="UtilityClasses"></a>Utility Classes used in the examples</h3>
 * The following utility classes are used in the examples for the table
 * tags:<code> ui:table</code>, <code>ui:tableRowGroup</code>, and <code>ui:tableColumn.<br>
 * </code><code></code>
 * <p><a href="#Group.java">Group.java</a><br>
 * <a href="#Actions.java">Actions.java</a><br>
 * <a href="#Filter.java">Filter.java</a><br>
 * <a href="#Name.java">Name.java</a><br>
 * <a href="#Select.java">Select.java</a><br>
 * <a href="#Preferences.java">Preferences.java</a><br>
 * </p>
 * <h4><a name="Group.java"></a>Group.java Utility Class<br>
 * </h4>
 * <code>package table.util;<br>
 * <br>
 * import com.sun.data.provider.TableDataProvider;<br>
 * import com.sun.data.provider.impl.ObjectArrayDataProvider;<br>
 * import com.sun.data.provider.impl.ObjectListDataProvider;<br>
 * import com.sun.rave.web.ui.component.Checkbox;<br>
 * import com.sun.rave.web.ui.component.TableRowGroup;<br>
 * <br>
 * import java.util.List;<br>
 * <br>
 * // This class contains data provider and util classes. Note that not
 * all util<br>
 * // classes are used for each example.<br>
 * public class Group {<br>
 * &nbsp;&nbsp;&nbsp; private TableRowGroup tableRowGroup = null; //
 * TableRowGroup component.<br>
 * &nbsp;&nbsp;&nbsp; private TableDataProvider provider = null; // Data
 * provider.<br>
 * &nbsp;&nbsp;&nbsp; private Checkbox checkbox = null; // Checkbox
 * component.<br>
 * &nbsp;&nbsp;&nbsp; private Preferences prefs = null; // Preferences
 * util.<br>
 * &nbsp;&nbsp;&nbsp; private Messages messages = null; // Messages util.<br>
 * &nbsp;&nbsp;&nbsp; private Actions actions = null; // Actions util.<br>
 * &nbsp;&nbsp;&nbsp; private Filter filter = null; // Filter util.<br>
 * &nbsp;&nbsp;&nbsp; private Select select = null; // Select util.<br>
 * &nbsp;&nbsp;&nbsp; private Clean clean = null; // Clean util.<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Default constructor.<br>
 * &nbsp;&nbsp;&nbsp; public Group() {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; actions = new Actions(this);<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; filter = new Filter(this);<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; select = new Select(this);<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; clean = new Clean(this);<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; prefs = new Preferences();<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; messages = new Messages();<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Construct an instance using given Object array.<br>
 * &nbsp;&nbsp;&nbsp; public Group(Object[] array) {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; this();<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; provider = new
 * ObjectArrayDataProvider(array);<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Construct an instance using given List.<br>
 * &nbsp;&nbsp;&nbsp; public Group(List list) {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; this();<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; provider = new
 * ObjectListDataProvider(list);<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Get data provider.<br>
 * &nbsp;&nbsp;&nbsp; public TableDataProvider getNames() {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; return provider;<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Get Actions util.<br>
 * &nbsp;&nbsp;&nbsp; public Actions getActions() {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; return actions;<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Get Filter util.<br>
 * &nbsp;&nbsp;&nbsp; public Filter getFilter() {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; return filter;<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Get Messages util.<br>
 * &nbsp;&nbsp;&nbsp; public Messages getMessages() {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; return messages;<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Get Preferences util.<br>
 * &nbsp;&nbsp;&nbsp; public Preferences getPreferences() {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; return prefs;<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Get Select util.<br>
 * &nbsp;&nbsp;&nbsp; public Select getSelect() {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; return select;<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Get tableRowGroup component.<br>
 * &nbsp;&nbsp;&nbsp; public TableRowGroup getTableRowGroup() {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; return tableRowGroup;<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Set tableRowGroup component.<br>
 * &nbsp;&nbsp;&nbsp; public void setTableRowGroup(TableRowGroup
 * tableRowGroup) {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; this.tableRowGroup =
 * tableRowGroup;<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Get checkbox component.<br>
 * &nbsp;&nbsp;&nbsp; public Checkbox getCheckbox() {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; return checkbox;<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Set checkbox component.<br>
 * &nbsp;&nbsp;&nbsp; public void setCheckbox(Checkbox checkbox) {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; this.checkbox = checkbox;<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * }</code><span style="font-family: monospace;"></span><br>
 * <h4><a name="Actions.java"></a>Actions.java Utility Class<br>
 * </h4>
 * <code>package table.util;<br>
 * <br>
 * import com.sun.data.provider.FieldKey;<br>
 * import com.sun.data.provider.RowKey;<br>
 * import com.sun.data.provider.TableDataProvider;<br>
 * import com.sun.data.provider.impl.ObjectListDataProvider;<br>
 * import com.sun.rave.web.ui.model.Option;<br>
 * <br>
 * import java.util.List;<br>
 * import java.util.Map;<br>
 * <br>
 * import javax.faces.context.FacesContext;<br>
 * <br>
 * // This class provides functionality for table actions.<br>
 * public class Actions {<br>
 * &nbsp;&nbsp;&nbsp; private Group group = null; // Group util.<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Action menu items.<br>
 * &nbsp;&nbsp;&nbsp; protected static final Option[] moreActionsOptions =
 * {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; new Option("ACTION0",
 * "&amp;#8212; More Actions &amp;#8212;"),<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; new Option("ACTION1",
 * "Action 1"),<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; new Option("ACTION2",
 * "Action 2"),<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; new Option("ACTION3",
 * "Action 3"),<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; new Option("ACTION4",
 * "Action 4"),<br>
 * &nbsp;&nbsp;&nbsp; };<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Default constructor.<br>
 * &nbsp;&nbsp;&nbsp; public Actions(Group group) {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; this.group = group;<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Action button event.<br>
 * &nbsp;&nbsp;&nbsp; public void action() {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; String message = null;<br>
 * <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; // Get hyperlink parameter
 * used for embedded actions example.<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; Map map =
 * FacesContext.getCurrentInstance().getExternalContext()<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * .getRequestParameterMap();<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; String param = (String)
 * map.get("param");<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; if (param != null) {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * message = "Embedded Action Selected: Parameter = " + param;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; } else {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * message = "Table Action Selected";<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; }<br>
 * <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * group.getMessages().setMessage(message);<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Action to remove rows from ObjectListDataProvider.<br>
 * &nbsp;&nbsp;&nbsp; public void delete() {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; // Since mutiple examples
 * are using the same beans, the binding<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; // simply tells us that
 * checkbox state is maintained arcoss pages.<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; if
 * (group.getSelect().isKeepSelected()) {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; //
 * If we got here, then we're maintaining state across pages.<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * delete(group.getTableRowGroup().getSelectedRowKeys());<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; } else {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; //
 * If we got here, then we're using the phase listener and must<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; //
 * take filtering, sorting, and pagination into account.<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * delete(group.getTableRowGroup().getRenderedSelectedRowKeys());<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; }<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Set disabled value for table actions.<br>
 * &nbsp;&nbsp;&nbsp; public boolean getDisabled() {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; // If there is at least one
 * row selection, actions are enabled.<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; boolean result = true;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; if (group.getTableRowGroup()
 * == null) {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * return result;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; }<br>
 * <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; // Since mutiple examples
 * are using the same beans, the binding<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; // simply tells us that
 * checkbox state is maintained arcoss pages.<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; if
 * (group.getSelect().isKeepSelected()) {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; //
 * If we got here, then we're maintaining state across pages.<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * result = group.getTableRowGroup().getSelectedRowsCount() &lt; 1;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; } else {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; //
 * If we got here, then we're using the phase listener and must<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; //
 * take filtering, sorting, and pagination into account.<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * result = group.getTableRowGroup().getRenderedSelectedRowsCount() &lt; 1;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; }<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; return result;<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Get action.<br>
 * &nbsp;&nbsp;&nbsp; public String getMoreActions() {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; // Per the UI guidelines,
 * always snap back to "More Actions...".<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; return "ACTION0";<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Get action menu options.<br>
 * &nbsp;&nbsp;&nbsp; public Option[] getMoreActionsOptions() {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; return moreActionsOptions;<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Action menu event.<br>
 * &nbsp;&nbsp;&nbsp; public void moreActions() {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * group.getMessages().setMessage("More Actions Menu Selected");<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Set action.<br>
 * &nbsp;&nbsp;&nbsp; public void setMoreActions(String action) {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; // Do nothing.<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; //
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~<br>
 * &nbsp;&nbsp;&nbsp; // Private methods<br>
 * &nbsp;&nbsp;&nbsp; //
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Action to remove rows from ObjectListDataProvider.<br>
 * &nbsp;&nbsp;&nbsp; private void delete(RowKey[] rowKeys) {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; if (rowKeys == null) {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * return;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; }<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; TableDataProvider provider =
 * group.getNames();<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; for (int i = 0; i &lt;
 * rowKeys.length; i++) {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * RowKey rowKey = rowKeys[i];<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; if
 * (provider.canRemoveRow(rowKey)) {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * provider.removeRow(rowKey);<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; }<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; }<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; ((ObjectListDataProvider)
 * provider).commitChanges(); // Commit.<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; group.getSelect().clear();
 * // Clear phase listener.<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * }</code><br>
 * <h4><a name="Filter.java"></a>Filter.java Utility Class<br>
 * </h4>
 * <code>package table.util;<br>
 * <br>
 * import com.sun.data.provider.FilterCriteria;<br>
 * import com.sun.data.provider.impl.CompareFilterCriteria;<br>
 * import com.sun.rave.web.ui.component.Table;<br>
 * import com.sun.rave.web.ui.model.Option;<br>
 * <br>
 * // This class provides functionality for table filters.<br>
 * //<br>
 * // This util class sets filters directly on the TableRowGroup component
 * using <br>
 * // FilterCriteria; however, there is also a FilteredTableDataProvider
 * class that<br>
 * // can used for filtering outside of the table. The table will pick up
 * what ever <br>
 * // filter has been applied automatically, for example:<br>
 * //<br>
 * // // Some choice of TableDataProvider.<br>
 * // TableDataProvider provider = new ...<br>
 * //<br>
 * // // This wraps and filters an existing TableDataProvider.<br>
 * // FilteredTableDataProvider filteredProvider = new
 * FilteredTableDataProvider();<br>
 * // filteredProvider.setTableDataProvider(provider);<br>
 * //<br>
 * // // Set FilteredTableDataProvider in the TableRowGroup component.<br>
 * // tableRowGroup.setSourceData(filteredProvider);<br>
 * //<br>
 * // The table component itself has no idea that there is any filtering
 * going on, <br>
 * // but the filtering functionality has been encapsulated in the data
 * provider. <br>
 * // The developer can then use different FilterCriteria types to apply
 * filters,<br>
 * // for example:<br>
 * //<br>
 * // CompareFilterCriteria cfc = new ...<br>
 * // RegexFilterCriteria rxfc = new ...<br>
 * // filteredProvider.setFilterCriteria(new FilterCriteria[] { cfc, fxfc
 * });<br>
 * public class Filter {<br>
 * &nbsp;&nbsp;&nbsp; private String customFilter = null; // Custom filter.<br>
 * &nbsp;&nbsp;&nbsp; private String basicFilter = null; // Basic filter
 * menu option.<br>
 * &nbsp;&nbsp;&nbsp; private String filterText = null; // Filter text.<br>
 * &nbsp;&nbsp;&nbsp; private Group group = null; // Group util.<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Filter menu items.<br>
 * &nbsp;&nbsp;&nbsp; protected static final Option[] filterOptions = {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; new Option("FILTER0", "All
 * Items"),<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; new Option("FILTER1",
 * "Filter 1"),<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; new Option("FILTER2",
 * "Filter 2"),<br>
 * &nbsp;&nbsp;&nbsp; };<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Default constructor.<br>
 * &nbsp;&nbsp;&nbsp; public Filter(Group group) {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; this.group = group;<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // UI guidelines state that a "Custom Filter" option
 * should be added to the<br>
 * &nbsp;&nbsp;&nbsp; // filter menu, used to open the table filter panel.
 * Thus, if the <br>
 * &nbsp;&nbsp;&nbsp; // CUSTOM_FILTER option is selected, Javascript
 * invoked via the onChange<br>
 * &nbsp;&nbsp;&nbsp; // event will open the table filter panel.<br>
 * &nbsp;&nbsp;&nbsp; //<br>
 * &nbsp;&nbsp;&nbsp; // UI guidelines also state that a "Custom Filter
 * Applied" option should be <br>
 * &nbsp;&nbsp;&nbsp; // added to the filter menu, indicating that a
 * custom filter has been <br>
 * &nbsp;&nbsp;&nbsp; // applied. In this scenario, set the selected
 * property of the filter menu <br>
 * &nbsp;&nbsp;&nbsp; // as CUSTOM_FILTER_APPLIED. This selection should
 * persist until another <br>
 * &nbsp;&nbsp;&nbsp; // menu option has been selected.<br>
 * &nbsp;&nbsp;&nbsp; //<br>
 * &nbsp;&nbsp;&nbsp; // Further, UI guidelines state that the table title
 * should indicate that a <br>
 * &nbsp;&nbsp;&nbsp; // custom filter has been applied. To add this text
 * to the table title, set <br>
 * &nbsp;&nbsp;&nbsp; // the filter property.<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Basic filter event.<br>
 * &nbsp;&nbsp;&nbsp; public void applyBasicFilter() {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; if
 * (basicFilter.equals("FILTER1")) {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * filterText = "Filter 1";<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; } else if
 * (basicFilter.equals("FILTER2")) {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * filterText = "Filter 2";<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; } else {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * filterText = null;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; }<br>
 * <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; // Clear all filters since
 * we don't have an example here.<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; //<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; // Note: TableRowGroup
 * ensures pagination is reset per UI guidelines.<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * group.getTableRowGroup().setFilterCriteria(null);<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Custom filter event.<br>
 * &nbsp;&nbsp;&nbsp; public void applyCustomFilter() {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; basicFilter =
 * Table.CUSTOM_FILTER_APPLIED; // Set filter menu option.<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; filterText = "Custom";<br>
 * <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; // Filter rows that do not
 * match custom filter.<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; CompareFilterCriteria
 * criteria = new CompareFilterCriteria(<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * group.getNames().getFieldKey("last"), customFilter);<br>
 * <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; // Note: TableRowGroup
 * ensures pagination is reset per UI guidelines.<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * group.getTableRowGroup().setFilterCriteria(<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; new
 * FilterCriteria[] {criteria});<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Get basic filter.<br>
 * &nbsp;&nbsp;&nbsp; public String getBasicFilter() {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; // Note: the selected value
 * must be set to restore the default selected<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; // value when the embedded
 * filter panel is closed. Further, the selected<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; // value should never be set
 * as "Custom Filter...".<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; return (basicFilter != null
 * &amp;&amp; !basicFilter.equals(Table.CUSTOM_FILTER))<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; ?
 * basicFilter : "FILTER0";<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Set basic filter.<br>
 * &nbsp;&nbsp;&nbsp; public void setBasicFilter(String value) {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; basicFilter = value;<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Get custom filter.<br>
 * &nbsp;&nbsp;&nbsp; public String getCustomFilter() {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; return customFilter;<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Set custom filter.<br>
 * &nbsp;&nbsp;&nbsp; public void setCustomFilter(String value) {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; customFilter = value;<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Get filter menu options.<br>
 * &nbsp;&nbsp;&nbsp; public Option[] getFilterOptions() {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; // Get filter options based
 * on the selected filter menu option.<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; return
 * Table.getFilterOptions(filterOptions,<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * basicFilter == Table.CUSTOM_FILTER_APPLIED);<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Get filter text.<br>
 * &nbsp;&nbsp;&nbsp; public String getFilterText() {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; return filterText;<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * }</code><br>
 * <h4><a name="Name.java"></a>Name.java Utility Class<br>
 * </h4>
 * <code>package table.util;<br>
 * <br>
 * import com.sun.rave.web.ui.component.Alarm;<br>
 * import com.sun.rave.web.ui.theme.ThemeImages;<br>
 * <br>
 * public class Name {<br>
 * &nbsp;&nbsp;&nbsp; private String last = null; // Last name.<br>
 * &nbsp;&nbsp;&nbsp; private String first = null; // First name.<br>
 * &nbsp;&nbsp;&nbsp; private Alarm alarm = null; // Alarm.<br>
 * &nbsp;&nbsp;&nbsp; <br>
 * &nbsp;&nbsp;&nbsp; // Default constructor.<br>
 * &nbsp;&nbsp;&nbsp; public Name(String first, String last) {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; this.last = last;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; this.first = first;<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Construct an instance with given alarm severity.<br>
 * &nbsp;&nbsp;&nbsp; public Name(String first, String last, Alarm alarm) {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; this(first, last);<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; this.alarm = alarm;<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Get first name.<br>
 * &nbsp;&nbsp;&nbsp; public String getFirst() {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; return (alarm != null) ? " "
 * + first : first;<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Set first name.<br>
 * &nbsp;&nbsp;&nbsp; public void setFirst(String first) {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; this.first = first;<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Get last name.<br>
 * &nbsp;&nbsp;&nbsp; public String getLast() {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; return last;<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Set last name.<br>
 * &nbsp;&nbsp;&nbsp; public void setLast(String last) {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; this.last = last;<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Get alarm.<br>
 * &nbsp;&nbsp;&nbsp; public Alarm getAlarm() {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; return alarm;<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Get alarm.<br>
 * &nbsp;&nbsp;&nbsp; public void setAlarm(Alarm alarm) {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; this.alarm = alarm;<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Get alarm severity.<br>
 * &nbsp;&nbsp;&nbsp; public String getSeverity() {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; return alarm.getSeverity();<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Get alarm severity.<br>
 * &nbsp;&nbsp;&nbsp; public void setSeverity(String severity) {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; alarm.setSeverity(severity);<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * }</code><br>
 * <h4><a name="Select.java"></a>Select.java Utility Class<br>
 * </h4>
 * <code>package table.util;<br>
 * <br>
 * import com.sun.data.provider.FieldKey;<br>
 * import com.sun.data.provider.RowKey;<br>
 * import com.sun.data.provider.TableDataProvider;<br>
 * import com.sun.rave.web.ui.event.TableSelectPhaseListener;<br>
 * <br>
 * import javax.faces.context.FacesContext;<br>
 * import javax.faces.el.ValueBinding;<br>
 * <br>
 * // This class provides functionality for select tables.<br>
 * //<br>
 * // Note: UI guidelines recomend that rows should be unselected when no
 * longer in<br>
 * // view. For example, when a user selects rows of the table and
 * navigates to<br>
 * // another page. Or, when a user applies a filter or sort that may hide<br>
 * // previously selected rows from view. If a user invokes an action to
 * delete<br>
 * // the currently selected rows, they may inadvertently remove rows not<br>
 * // displayed on the current page. Using TableSelectPhaseListener ensures<br>
 * // that invalid row selections are not rendered by clearing selected
 * state<br>
 * // after the render response phase.<br>
 * public class Select {<br>
 * &nbsp;&nbsp;&nbsp; private TableSelectPhaseListener tspl = null; //
 * Phase listener.<br>
 * &nbsp;&nbsp;&nbsp; private Group group = null; // Group util.<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Default constructor.<br>
 * &nbsp;&nbsp;&nbsp; public Select(Group group) {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; this.group = group;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; tspl = new
 * TableSelectPhaseListener();<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Clear selected state from phase listener (e.g.,
 * when deleting rows).<br>
 * &nbsp;&nbsp;&nbsp; public void clear() {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; tspl.clear();<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Test flag indicating that selected objects should
 * not be cleared.<br>
 * &nbsp;&nbsp;&nbsp; public boolean isKeepSelected() {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; return tspl.isKeepSelected();<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Set flag indicating that selected objects should
 * not be cleared.<br>
 * &nbsp;&nbsp;&nbsp; public void keepSelected(boolean keepSelected) {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * tspl.keepSelected(keepSelected);<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Get selected property.<br>
 * &nbsp;&nbsp;&nbsp; public Object getSelected() {<br>
 * &nbsp;&nbsp;&nbsp; return tspl.getSelected(getTableRow());<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Set selected property.<br>
 * &nbsp;&nbsp;&nbsp; public void setSelected(Object object) {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; RowKey rowKey =
 * getTableRow();<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; if (rowKey != null) {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * tspl.setSelected(rowKey, object);<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; }<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Get selected value property.<br>
 * &nbsp;&nbsp;&nbsp; public Object getSelectedValue() {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; RowKey rowKey =
 * getTableRow();<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; return (rowKey != null) ?
 * rowKey.getRowId() : null;<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Get the selected state -- Sort on checked state
 * only.<br>
 * &nbsp;&nbsp;&nbsp; public boolean getSelectedState() {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; // Typically, selected state
 * is tested by comparing the selected and <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; // selectedValue properties.
 * In this example, however, the phase <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; // listener value is not
 * null when selected.<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; return
 * getSelectedState(getTableRow());<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Get the selected state.<br>
 * &nbsp;&nbsp;&nbsp; public boolean getSelectedState(RowKey rowKey) {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; return
 * tspl.isSelected(rowKey);<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Get current table row.<br>
 * &nbsp;&nbsp;&nbsp; //<br>
 * &nbsp;&nbsp;&nbsp; // Note: To obtain a RowKey for the current table
 * row, the use the same <br>
 * &nbsp;&nbsp;&nbsp; // sourceVar property given to the TableRowGroup
 * component. For example, if <br>
 * &nbsp;&nbsp;&nbsp; // sourceVar="name", use "#{name.tableRow}" as the
 * expression string.<br>
 * &nbsp;&nbsp;&nbsp; private RowKey getTableRow() {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; FacesContext context =
 * FacesContext.getCurrentInstance();<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; ValueBinding vb =
 * context.getApplication().createValueBinding(<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * "#{name.tableRow}");<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; return (RowKey)
 * vb.getValue(context);<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * }</code><br>
 * <h4><a name="Preferences.java"></a>Preferences.java Utility Class<br>
 * </h4>
 * <code>package table.util;<br>
 * <br>
 * // This class provides functionality for table preferences.<br>
 * public class Preferences {<br>
 * &nbsp;&nbsp;&nbsp; private String preference = null; // Rows preference.<br>
 * &nbsp;&nbsp;&nbsp; private int rows = 5; // Rows per page.<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Default constructor.<br>
 * &nbsp;&nbsp;&nbsp; public Preferences() {<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Table preferences event.<br>
 * &nbsp;&nbsp;&nbsp; public void applyPreferences() {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; try {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; int
 * rows = Integer.parseInt(preference);<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; if
 * (rows &gt; 0) {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * this.rows = rows;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; }<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; } catch
 * (NumberFormatException e) {}<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Get rows per page.<br>
 * &nbsp;&nbsp;&nbsp; public int getRows() {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; return rows;<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Get preference.<br>
 * &nbsp;&nbsp;&nbsp; public String getPreference() {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; return
 * Integer.toString(rows);<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Set preference.<br>
 * &nbsp;&nbsp;&nbsp; public void setPreference(String value) {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; preference = value;<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * }
 * </code><br>
 * <br>
 * <p>Auto-generated component class.
 * Do <strong>NOT</strong> modify; all changes
 * <strong>will</strong> be lost!</p>
 */

public abstract class TableBase extends javax.faces.component.UIComponentBase {

    /**
     * <p>Construct a new <code>TableBase</code>.</p>
     */
    public TableBase() {
        super();
        setRendererType("com.sun.rave.web.ui.Table");
    }

    /**
     * <p>Return the identifier of the component family to which this
     * component belongs.  This identifier, in conjunction with the value
     * of the <code>rendererType</code> property, may be used to select
     * the appropriate {@link Renderer} for this component instance.</p>
     */
    public String getFamily() {
        return "com.sun.rave.web.ui.Table";
    }

    // align
    private String align = null;

    /**
 * <p>Sets the alignment of the table (left, right or center) on the page (deprecated in HTML 4.0)</p>
     */
    public String getAlign() {
        if (this.align != null) {
            return this.align;
        }
        ValueBinding _vb = getValueBinding("align");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Sets the alignment of the table (left, right or center) on the page (deprecated in HTML 4.0)</p>
     * @see #getAlign()
     */
    public void setAlign(String align) {
        this.align = align;
    }

    // augmentTitle
    private boolean augmentTitle = false;
    private boolean augmentTitle_set = false;

    /**
 * <p>Flag indicating that the table title should be augmented with the range of items 
 * currently displayed and the total number of items in the table. For example, 
 * "(1 - 25 of 200)". If the table is not currently paginated, the title is 
 * augmented with the number of displayed items. For example, "(18)". When set to 
 * false, any values set for <code>itemsText</code> and <code>filterText</code> 
 * are overridden.</p>
     */
    public boolean isAugmentTitle() {
        if (this.augmentTitle_set) {
            return this.augmentTitle;
        }
        ValueBinding _vb = getValueBinding("augmentTitle");
        if (_vb != null) {
            Object _result = _vb.getValue(getFacesContext());
            if (_result == null) {
                return false;
            } else {
                return ((Boolean) _result).booleanValue();
            }
        }
        return true;
    }

    /**
 * <p>Flag indicating that the table title should be augmented with the range of items 
 * currently displayed and the total number of items in the table. For example, 
 * "(1 - 25 of 200)". If the table is not currently paginated, the title is 
 * augmented with the number of displayed items. For example, "(18)". When set to 
 * false, any values set for <code>itemsText</code> and <code>filterText</code> 
 * are overridden.</p>
     * @see #isAugmentTitle()
     */
    public void setAugmentTitle(boolean augmentTitle) {
        this.augmentTitle = augmentTitle;
        this.augmentTitle_set = true;
    }

    // bgColor
    private String bgColor = null;

    /**
 * <p>Sets the background color for the table (deprecated in HTML 4.0)</p>
     */
    public String getBgColor() {
        if (this.bgColor != null) {
            return this.bgColor;
        }
        ValueBinding _vb = getValueBinding("bgColor");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Sets the background color for the table (deprecated in HTML 4.0)</p>
     * @see #getBgColor()
     */
    public void setBgColor(String bgColor) {
        this.bgColor = bgColor;
    }

    // border
    private int border = Integer.MIN_VALUE;
    private boolean border_set = false;

    /**
 * <p>Set the border width in pixels within the table</p>
     */
    public int getBorder() {
        if (this.border_set) {
            return this.border;
        }
        ValueBinding _vb = getValueBinding("border");
        if (_vb != null) {
            Object _result = _vb.getValue(getFacesContext());
            if (_result == null) {
                return Integer.MIN_VALUE;
            } else {
                return ((Integer) _result).intValue();
            }
        }
        return Integer.MIN_VALUE;
    }

    /**
 * <p>Set the border width in pixels within the table</p>
     * @see #getBorder()
     */
    public void setBorder(int border) {
        this.border = border;
        this.border_set = true;
    }

    // cellPadding
    private String cellPadding = null;

    /**
 * <p>Sets the whitespace between the borders and the contents of a cell</p>
     */
    public String getCellPadding() {
        if (this.cellPadding != null) {
            return this.cellPadding;
        }
        ValueBinding _vb = getValueBinding("cellPadding");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Sets the whitespace between the borders and the contents of a cell</p>
     * @see #getCellPadding()
     */
    public void setCellPadding(String cellPadding) {
        this.cellPadding = cellPadding;
    }

    // cellSpacing
    private String cellSpacing = null;

    /**
 * <p>Sets the whitespace between cells and also at the edges of the table</p>
     */
    public String getCellSpacing() {
        if (this.cellSpacing != null) {
            return this.cellSpacing;
        }
        ValueBinding _vb = getValueBinding("cellSpacing");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Sets the whitespace between cells and also at the edges of the table</p>
     * @see #getCellSpacing()
     */
    public void setCellSpacing(String cellSpacing) {
        this.cellSpacing = cellSpacing;
    }

    // clearSortButton
    private boolean clearSortButton = false;
    private boolean clearSortButton_set = false;

    /**
 * <p>In the View-Changing Controls area of the Action Bar, display a button that 
 * clears any sorting of the table. When the button is clicked, the table items 
 * return to the order they were in when the page was initially rendered.</p>
     */
    public boolean isClearSortButton() {
        if (this.clearSortButton_set) {
            return this.clearSortButton;
        }
        ValueBinding _vb = getValueBinding("clearSortButton");
        if (_vb != null) {
            Object _result = _vb.getValue(getFacesContext());
            if (_result == null) {
                return false;
            } else {
                return ((Boolean) _result).booleanValue();
            }
        }
        return false;
    }

    /**
 * <p>In the View-Changing Controls area of the Action Bar, display a button that 
 * clears any sorting of the table. When the button is clicked, the table items 
 * return to the order they were in when the page was initially rendered.</p>
     * @see #isClearSortButton()
     */
    public void setClearSortButton(boolean clearSortButton) {
        this.clearSortButton = clearSortButton;
        this.clearSortButton_set = true;
    }

    // deselectMultipleButton
    private boolean deselectMultipleButton = false;
    private boolean deselectMultipleButton_set = false;

    /**
 * <p>In the Action Bar, display a deselect button for tables in which multiple rows 
 * can be selected, to allow users to deselect all table rows that are currently 
 * displayed. This button is used to deselect a column of checkboxes using the id 
 * that was given to the selectId attribute of the <code>ui:tableColumn</code> tag.</p>
     */
    public boolean isDeselectMultipleButton() {
        if (this.deselectMultipleButton_set) {
            return this.deselectMultipleButton;
        }
        ValueBinding _vb = getValueBinding("deselectMultipleButton");
        if (_vb != null) {
            Object _result = _vb.getValue(getFacesContext());
            if (_result == null) {
                return false;
            } else {
                return ((Boolean) _result).booleanValue();
            }
        }
        return false;
    }

    /**
 * <p>In the Action Bar, display a deselect button for tables in which multiple rows 
 * can be selected, to allow users to deselect all table rows that are currently 
 * displayed. This button is used to deselect a column of checkboxes using the id 
 * that was given to the selectId attribute of the <code>ui:tableColumn</code> tag.</p>
     * @see #isDeselectMultipleButton()
     */
    public void setDeselectMultipleButton(boolean deselectMultipleButton) {
        this.deselectMultipleButton = deselectMultipleButton;
        this.deselectMultipleButton_set = true;
    }

    // deselectMultipleButtonOnClick
    private String deselectMultipleButtonOnClick = null;

    /**
 * <p>Scripting code that is executed when the user clicks the deselect multiple 
 * button. You should use the JavaScript <code>setTimeout()</code> function to 
 * invoke the script to ensure that checkboxes are deselected immediately, instead 
 * of waiting for the script to complete.</p>
     */
    public String getDeselectMultipleButtonOnClick() {
        if (this.deselectMultipleButtonOnClick != null) {
            return this.deselectMultipleButtonOnClick;
        }
        ValueBinding _vb = getValueBinding("deselectMultipleButtonOnClick");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Scripting code that is executed when the user clicks the deselect multiple 
 * button. You should use the JavaScript <code>setTimeout()</code> function to 
 * invoke the script to ensure that checkboxes are deselected immediately, instead 
 * of waiting for the script to complete.</p>
     * @see #getDeselectMultipleButtonOnClick()
     */
    public void setDeselectMultipleButtonOnClick(String deselectMultipleButtonOnClick) {
        this.deselectMultipleButtonOnClick = deselectMultipleButtonOnClick;
    }

    // deselectSingleButton
    private boolean deselectSingleButton = false;
    private boolean deselectSingleButton_set = false;

    /**
 * <p>In the Action Bar, display a deselect button for tables in which only a single 
 * table row can be selected at a time. This button is used to deselect a column of 
 * radio buttons using the id that was given to the selectId attribute of the 
 * <code>ui:tableColumn</code> tag.</p>
     */
    public boolean isDeselectSingleButton() {
        if (this.deselectSingleButton_set) {
            return this.deselectSingleButton;
        }
        ValueBinding _vb = getValueBinding("deselectSingleButton");
        if (_vb != null) {
            Object _result = _vb.getValue(getFacesContext());
            if (_result == null) {
                return false;
            } else {
                return ((Boolean) _result).booleanValue();
            }
        }
        return false;
    }

    /**
 * <p>In the Action Bar, display a deselect button for tables in which only a single 
 * table row can be selected at a time. This button is used to deselect a column of 
 * radio buttons using the id that was given to the selectId attribute of the 
 * <code>ui:tableColumn</code> tag.</p>
     * @see #isDeselectSingleButton()
     */
    public void setDeselectSingleButton(boolean deselectSingleButton) {
        this.deselectSingleButton = deselectSingleButton;
        this.deselectSingleButton_set = true;
    }

    // deselectSingleButtonOnClick
    private String deselectSingleButtonOnClick = null;

    /**
 * <p>Scripting code that is executed when the user clicks the deselect single button.
 * You should use the JavaScript <code>setTimeout()</code> function to invoke the 
 * script to ensure that the radio button is deselected immediately, instead of 
 * waiting for the script to complete.</p>
     */
    public String getDeselectSingleButtonOnClick() {
        if (this.deselectSingleButtonOnClick != null) {
            return this.deselectSingleButtonOnClick;
        }
        ValueBinding _vb = getValueBinding("deselectSingleButtonOnClick");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Scripting code that is executed when the user clicks the deselect single button.
 * You should use the JavaScript <code>setTimeout()</code> function to invoke the 
 * script to ensure that the radio button is deselected immediately, instead of 
 * waiting for the script to complete.</p>
     * @see #getDeselectSingleButtonOnClick()
     */
    public void setDeselectSingleButtonOnClick(String deselectSingleButtonOnClick) {
        this.deselectSingleButtonOnClick = deselectSingleButtonOnClick;
    }

    // extraActionBottomHtml
    private String extraActionBottomHtml = null;

    /**
 * <p>Extra HTML code to be appended to the <code>&lt;td&gt; </code>HTML element that  
 * is rendered for the Action Bar (bottom). Use only code that is valid in an HTML 
 * <code>&lt;td&gt;</code> element. The code you specify is inserted in the HTML 
 * element, and is not checked for validity. For example, you might set this 
 * attribute to <code>"style=`myActionBarStyle'"</code>.</p>
     */
    public String getExtraActionBottomHtml() {
        if (this.extraActionBottomHtml != null) {
            return this.extraActionBottomHtml;
        }
        ValueBinding _vb = getValueBinding("extraActionBottomHtml");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Extra HTML code to be appended to the <code>&lt;td&gt; </code>HTML element that  
 * is rendered for the Action Bar (bottom). Use only code that is valid in an HTML 
 * <code>&lt;td&gt;</code> element. The code you specify is inserted in the HTML 
 * element, and is not checked for validity. For example, you might set this 
 * attribute to <code>"style=`myActionBarStyle'"</code>.</p>
     * @see #getExtraActionBottomHtml()
     */
    public void setExtraActionBottomHtml(String extraActionBottomHtml) {
        this.extraActionBottomHtml = extraActionBottomHtml;
    }

    // extraActionTopHtml
    private String extraActionTopHtml = null;

    /**
 * <p>Extra HTML code to be appended to the <code>&lt;td&gt; </code>HTML element that 
 * is rendered for the Action Bar (top). Use only code that is valid in an HTML 
 * <code>&lt;td&gt;</code> element. The code you specify is inserted in the HTML 
 * element, and is not checked for validity. For example, you might set this 
 * attribute to <code>"style=`myActionBarStyle'"</code>.</p>
     */
    public String getExtraActionTopHtml() {
        if (this.extraActionTopHtml != null) {
            return this.extraActionTopHtml;
        }
        ValueBinding _vb = getValueBinding("extraActionTopHtml");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Extra HTML code to be appended to the <code>&lt;td&gt; </code>HTML element that 
 * is rendered for the Action Bar (top). Use only code that is valid in an HTML 
 * <code>&lt;td&gt;</code> element. The code you specify is inserted in the HTML 
 * element, and is not checked for validity. For example, you might set this 
 * attribute to <code>"style=`myActionBarStyle'"</code>.</p>
     * @see #getExtraActionTopHtml()
     */
    public void setExtraActionTopHtml(String extraActionTopHtml) {
        this.extraActionTopHtml = extraActionTopHtml;
    }

    // extraFooterHtml
    private String extraFooterHtml = null;

    /**
 * <p>Extra HTML code to be appended to the <code>&lt;td&gt; </code>HTML element that 
 * is rendered for the table footer. Use only code that is valid in an HTML 
 * <code>&lt;td&gt;</code> element. The code you specify is inserted in the HTML 
 * element, and is not checked for validity. For example, you might set this 
 * attribute to <code>"nowrap=`nowrap'"</code>.</p>
     */
    public String getExtraFooterHtml() {
        if (this.extraFooterHtml != null) {
            return this.extraFooterHtml;
        }
        ValueBinding _vb = getValueBinding("extraFooterHtml");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Extra HTML code to be appended to the <code>&lt;td&gt; </code>HTML element that 
 * is rendered for the table footer. Use only code that is valid in an HTML 
 * <code>&lt;td&gt;</code> element. The code you specify is inserted in the HTML 
 * element, and is not checked for validity. For example, you might set this 
 * attribute to <code>"nowrap=`nowrap'"</code>.</p>
     * @see #getExtraFooterHtml()
     */
    public void setExtraFooterHtml(String extraFooterHtml) {
        this.extraFooterHtml = extraFooterHtml;
    }

    // extraPanelHtml
    private String extraPanelHtml = null;

    /**
 * <p>Extra HTML code to be appended to the <code>&lt;td&gt; </code>HTML element that 
 * is rendered for an embedded panel. Use only code that is valid in an HTML 
 * <code>&lt;td&gt;</code> element. The code you specify is inserted in the HTML 
 * element, and is not checked for validity.</p>
     */
    public String getExtraPanelHtml() {
        if (this.extraPanelHtml != null) {
            return this.extraPanelHtml;
        }
        ValueBinding _vb = getValueBinding("extraPanelHtml");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Extra HTML code to be appended to the <code>&lt;td&gt; </code>HTML element that 
 * is rendered for an embedded panel. Use only code that is valid in an HTML 
 * <code>&lt;td&gt;</code> element. The code you specify is inserted in the HTML 
 * element, and is not checked for validity.</p>
     * @see #getExtraPanelHtml()
     */
    public void setExtraPanelHtml(String extraPanelHtml) {
        this.extraPanelHtml = extraPanelHtml;
    }

    // extraTitleHtml
    private String extraTitleHtml = null;

    /**
 * <p>Extra HTML code to be appended to the <code>&lt;caption&gt;</code> HTML element 
 * that is rendered for the table title. Use only code that is valid in an HTML 
 * <code>&lt;caption&gt;</code> element. The code you specify is inserted in the 
 * HTML element, and is not checked for validity. For example, you might set this 
 * attribute to <code>"style=`myTitleStyle'"</code>.</p>
     */
    public String getExtraTitleHtml() {
        if (this.extraTitleHtml != null) {
            return this.extraTitleHtml;
        }
        ValueBinding _vb = getValueBinding("extraTitleHtml");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Extra HTML code to be appended to the <code>&lt;caption&gt;</code> HTML element 
 * that is rendered for the table title. Use only code that is valid in an HTML 
 * <code>&lt;caption&gt;</code> element. The code you specify is inserted in the 
 * HTML element, and is not checked for validity. For example, you might set this 
 * attribute to <code>"style=`myTitleStyle'"</code>.</p>
     * @see #getExtraTitleHtml()
     */
    public void setExtraTitleHtml(String extraTitleHtml) {
        this.extraTitleHtml = extraTitleHtml;
    }

    // filterId
    private String filterId = null;

    /**
 * <p>The element id to be applied to the outermost HTML element that is rendered 
 * for the dropDown component used to display filter options. The id must be 
 * fully qualified. This id is required for JavaScript functions to set the 
 * dropDown styles when the embedded filter panel is opened, and to reset the 
 * default selected value when the panel is closed. Note that if you use the 
 * <code>ui:dropDown</code> tag as the only component in the <code>filter</code> 
 * facet, the <code>filterId</code> is optional. If you use a custom component, or 
 * use the <code>ui:dropDown</code> as a child component, you must specify a 
 * filterID.</p>
     */
    public String getFilterId() {
        if (this.filterId != null) {
            return this.filterId;
        }
        ValueBinding _vb = getValueBinding("filterId");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>The element id to be applied to the outermost HTML element that is rendered 
 * for the dropDown component used to display filter options. The id must be 
 * fully qualified. This id is required for JavaScript functions to set the 
 * dropDown styles when the embedded filter panel is opened, and to reset the 
 * default selected value when the panel is closed. Note that if you use the 
 * <code>ui:dropDown</code> tag as the only component in the <code>filter</code> 
 * facet, the <code>filterId</code> is optional. If you use a custom component, or 
 * use the <code>ui:dropDown</code> as a child component, you must specify a 
 * filterID.</p>
     * @see #getFilterId()
     */
    public void setFilterId(String filterId) {
        this.filterId = filterId;
    }

    // filterPanelFocusId
    private String filterPanelFocusId = null;

    /**
 * <p>The element id used to set focus when the filter panel is open.</p>
     */
    public String getFilterPanelFocusId() {
        if (this.filterPanelFocusId != null) {
            return this.filterPanelFocusId;
        }
        ValueBinding _vb = getValueBinding("filterPanelFocusId");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>The element id used to set focus when the filter panel is open.</p>
     * @see #getFilterPanelFocusId()
     */
    public void setFilterPanelFocusId(String filterPanelFocusId) {
        this.filterPanelFocusId = filterPanelFocusId;
    }

    // filterText
    private String filterText = null;

    /**
 * <p>Text to be inserted into the table title bar when a filter is applied. This text 
 * is expected to be the name of the filter that the user has selected. The 
 * attribute value should be a JavaServer Faces EL expression that resolves to a 
 * backing bean property whose value is set in your filter code. The value of the 
 * filterText attribute is inserted into the table title, as follows: Your Table's 
 * Title <span style="font-style: italic;">filterText</span> Filter Applied.</p>
     */
    public String getFilterText() {
        if (this.filterText != null) {
            return this.filterText;
        }
        ValueBinding _vb = getValueBinding("filterText");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Text to be inserted into the table title bar when a filter is applied. This text 
 * is expected to be the name of the filter that the user has selected. The 
 * attribute value should be a JavaServer Faces EL expression that resolves to a 
 * backing bean property whose value is set in your filter code. The value of the 
 * filterText attribute is inserted into the table title, as follows: Your Table's 
 * Title <span style="font-style: italic;">filterText</span> Filter Applied.</p>
     * @see #getFilterText()
     */
    public void setFilterText(String filterText) {
        this.filterText = filterText;
    }

    // footerText
    private String footerText = null;

    /**
 * <p>The text to be displayed in the table footer, which expands across the width of 
 * the table.</p>
     */
    public String getFooterText() {
        if (this.footerText != null) {
            return this.footerText;
        }
        ValueBinding _vb = getValueBinding("footerText");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>The text to be displayed in the table footer, which expands across the width of 
 * the table.</p>
     * @see #getFooterText()
     */
    public void setFooterText(String footerText) {
        this.footerText = footerText;
    }

    // frame
    private String frame = null;

    /**
 * <p>Specifies the width in pixels of the border around a table.</p>
     */
    public String getFrame() {
        if (this.frame != null) {
            return this.frame;
        }
        ValueBinding _vb = getValueBinding("frame");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Specifies the width in pixels of the border around a table.</p>
     * @see #getFrame()
     */
    public void setFrame(String frame) {
        this.frame = frame;
    }

    // hiddenSelectedRows
    private boolean hiddenSelectedRows = false;
    private boolean hiddenSelectedRows_set = false;

    /**
 * <p>Flag indicating that selected rows might be currently hidden from view. UI 
 * guidelines recommend that rows that are not in view are deselected. For example, 
 * when users select rows of the table and navigate to another page, the selected 
 * rows should be deselected automatically. Or, when a user applies a filter or 
 * sort that hides previously selected rows from view, those selected rows should 
 * be deselected. By deselecting hidden rows, you prevent the user from 
 * inadvertantly invoking an action on rows that are not displayed.
 * <br/><br/>
 * However, sometimes state must be maintained aross table pages. If your table 
 * must maintain state, you must set the hiddenSelectedRows attribute to true. The 
 * attribute causes text to be displayed in the table title and footer to indicate 
 * the number of selected rows that are currently hidden from view. This title and 
 * footer text is also displayed with a count of 0 when there are no hidden 
 * selections, to make the user aware of the possibility of hidden selections.
 * <br/><br/>
 * Note: When hiddenSelectedRows is false, the descending sort button for the 
 * select column is disabled when the table is paginated. Disabling this button 
 * prevents a sort from placing selected rows on a page other than the current 
 * page.</p>
     */
    public boolean isHiddenSelectedRows() {
        if (this.hiddenSelectedRows_set) {
            return this.hiddenSelectedRows;
        }
        ValueBinding _vb = getValueBinding("hiddenSelectedRows");
        if (_vb != null) {
            Object _result = _vb.getValue(getFacesContext());
            if (_result == null) {
                return false;
            } else {
                return ((Boolean) _result).booleanValue();
            }
        }
        return false;
    }

    /**
 * <p>Flag indicating that selected rows might be currently hidden from view. UI 
 * guidelines recommend that rows that are not in view are deselected. For example, 
 * when users select rows of the table and navigate to another page, the selected 
 * rows should be deselected automatically. Or, when a user applies a filter or 
 * sort that hides previously selected rows from view, those selected rows should 
 * be deselected. By deselecting hidden rows, you prevent the user from 
 * inadvertantly invoking an action on rows that are not displayed.
 * <br/><br/>
 * However, sometimes state must be maintained aross table pages. If your table 
 * must maintain state, you must set the hiddenSelectedRows attribute to true. The 
 * attribute causes text to be displayed in the table title and footer to indicate 
 * the number of selected rows that are currently hidden from view. This title and 
 * footer text is also displayed with a count of 0 when there are no hidden 
 * selections, to make the user aware of the possibility of hidden selections.
 * <br/><br/>
 * Note: When hiddenSelectedRows is false, the descending sort button for the 
 * select column is disabled when the table is paginated. Disabling this button 
 * prevents a sort from placing selected rows on a page other than the current 
 * page.</p>
     * @see #isHiddenSelectedRows()
     */
    public void setHiddenSelectedRows(boolean hiddenSelectedRows) {
        this.hiddenSelectedRows = hiddenSelectedRows;
        this.hiddenSelectedRows_set = true;
    }

    // internalVirtualForm
    private boolean internalVirtualForm = false;
    private boolean internalVirtualForm_set = false;

    /**
 * <p>Flag indicating that this component should use a virtual form. A virtual form is 
 * equivalent to enclosing the table component in its own HTML form element, 
 * separate from other HTML elements on the same page. As an example, consider the 
 * case where a required text field and table appear on the same page. If the user 
 * clicks on a table sort button, while the required text field has no value, the 
 * sort action is never invoked because a value was required and validation failed. 
 * Placing the table in a virtual form allows the table sort action to complete 
 * because validation for the required text field is not processed. This is similar 
 * to using the immediate property of a button, but allows table children to be 
 * submitted so that selected checkbox values may be sorted, for example.</p>
     */
    public boolean isInternalVirtualForm() {
        if (this.internalVirtualForm_set) {
            return this.internalVirtualForm;
        }
        ValueBinding _vb = getValueBinding("internalVirtualForm");
        if (_vb != null) {
            Object _result = _vb.getValue(getFacesContext());
            if (_result == null) {
                return false;
            } else {
                return ((Boolean) _result).booleanValue();
            }
        }
        return false;
    }

    /**
 * <p>Flag indicating that this component should use a virtual form. A virtual form is 
 * equivalent to enclosing the table component in its own HTML form element, 
 * separate from other HTML elements on the same page. As an example, consider the 
 * case where a required text field and table appear on the same page. If the user 
 * clicks on a table sort button, while the required text field has no value, the 
 * sort action is never invoked because a value was required and validation failed. 
 * Placing the table in a virtual form allows the table sort action to complete 
 * because validation for the required text field is not processed. This is similar 
 * to using the immediate property of a button, but allows table children to be 
 * submitted so that selected checkbox values may be sorted, for example.</p>
     * @see #isInternalVirtualForm()
     */
    public void setInternalVirtualForm(boolean internalVirtualForm) {
        this.internalVirtualForm = internalVirtualForm;
        this.internalVirtualForm_set = true;
    }

    // itemsText
    private String itemsText = null;

    /**
 * <p>Text to add to the title of an unpaginated table. For example, if your table 
 * title is "Critical" and there are 20 items in the table, the default unpaginated 
 * table title would be Critical (20). If you specify itemsText="alerts", the title 
 * would be Critical (20 alerts).</p>
     */
    public String getItemsText() {
        if (this.itemsText != null) {
            return this.itemsText;
        }
        ValueBinding _vb = getValueBinding("itemsText");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Text to add to the title of an unpaginated table. For example, if your table 
 * title is "Critical" and there are 20 items in the table, the default unpaginated 
 * table title would be Critical (20). If you specify itemsText="alerts", the title 
 * would be Critical (20 alerts).</p>
     * @see #getItemsText()
     */
    public void setItemsText(String itemsText) {
        this.itemsText = itemsText;
    }

    // lite
    private boolean lite = false;
    private boolean lite_set = false;

    /**
 * <p>Renders the table in a style that makes the table look lighter weight, generally 
 * by omitting the shading around the table and in the title bar.</p>
     */
    public boolean isLite() {
        if (this.lite_set) {
            return this.lite;
        }
        ValueBinding _vb = getValueBinding("lite");
        if (_vb != null) {
            Object _result = _vb.getValue(getFacesContext());
            if (_result == null) {
                return false;
            } else {
                return ((Boolean) _result).booleanValue();
            }
        }
        return false;
    }

    /**
 * <p>Renders the table in a style that makes the table look lighter weight, generally 
 * by omitting the shading around the table and in the title bar.</p>
     * @see #isLite()
     */
    public void setLite(boolean lite) {
        this.lite = lite;
        this.lite_set = true;
    }

    // onClick
    private String onClick = null;

    /**
 * <p>Scripting code executed when a mouse click
 *     occurs over this component.</p>
     */
    public String getOnClick() {
        if (this.onClick != null) {
            return this.onClick;
        }
        ValueBinding _vb = getValueBinding("onClick");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Scripting code executed when a mouse click
 *     occurs over this component.</p>
     * @see #getOnClick()
     */
    public void setOnClick(String onClick) {
        this.onClick = onClick;
    }

    // onDblClick
    private String onDblClick = null;

    /**
 * <p>Scripting code executed when a mouse double click
 *     occurs over this component.</p>
     */
    public String getOnDblClick() {
        if (this.onDblClick != null) {
            return this.onDblClick;
        }
        ValueBinding _vb = getValueBinding("onDblClick");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Scripting code executed when a mouse double click
 *     occurs over this component.</p>
     * @see #getOnDblClick()
     */
    public void setOnDblClick(String onDblClick) {
        this.onDblClick = onDblClick;
    }

    // onKeyDown
    private String onKeyDown = null;

    /**
 * <p>Scripting code executed when the user presses down on a key while the
 *     component has focus.</p>
     */
    public String getOnKeyDown() {
        if (this.onKeyDown != null) {
            return this.onKeyDown;
        }
        ValueBinding _vb = getValueBinding("onKeyDown");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Scripting code executed when the user presses down on a key while the
 *     component has focus.</p>
     * @see #getOnKeyDown()
     */
    public void setOnKeyDown(String onKeyDown) {
        this.onKeyDown = onKeyDown;
    }

    // onKeyPress
    private String onKeyPress = null;

    /**
 * <p>Scripting code executed when the user presses and releases a key while
 *     the component has focus.</p>
     */
    public String getOnKeyPress() {
        if (this.onKeyPress != null) {
            return this.onKeyPress;
        }
        ValueBinding _vb = getValueBinding("onKeyPress");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Scripting code executed when the user presses and releases a key while
 *     the component has focus.</p>
     * @see #getOnKeyPress()
     */
    public void setOnKeyPress(String onKeyPress) {
        this.onKeyPress = onKeyPress;
    }

    // onKeyUp
    private String onKeyUp = null;

    /**
 * <p>Scripting code executed when the user releases a key while the
 *     component has focus.</p>
     */
    public String getOnKeyUp() {
        if (this.onKeyUp != null) {
            return this.onKeyUp;
        }
        ValueBinding _vb = getValueBinding("onKeyUp");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Scripting code executed when the user releases a key while the
 *     component has focus.</p>
     * @see #getOnKeyUp()
     */
    public void setOnKeyUp(String onKeyUp) {
        this.onKeyUp = onKeyUp;
    }

    // onMouseDown
    private String onMouseDown = null;

    /**
 * <p>Scripting code executed when the user presses a mouse button while the
 *     mouse pointer is on the component.</p>
     */
    public String getOnMouseDown() {
        if (this.onMouseDown != null) {
            return this.onMouseDown;
        }
        ValueBinding _vb = getValueBinding("onMouseDown");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Scripting code executed when the user presses a mouse button while the
 *     mouse pointer is on the component.</p>
     * @see #getOnMouseDown()
     */
    public void setOnMouseDown(String onMouseDown) {
        this.onMouseDown = onMouseDown;
    }

    // onMouseMove
    private String onMouseMove = null;

    /**
 * <p>Scripting code executed when the user moves the mouse pointer while
 *     over the component.</p>
     */
    public String getOnMouseMove() {
        if (this.onMouseMove != null) {
            return this.onMouseMove;
        }
        ValueBinding _vb = getValueBinding("onMouseMove");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Scripting code executed when the user moves the mouse pointer while
 *     over the component.</p>
     * @see #getOnMouseMove()
     */
    public void setOnMouseMove(String onMouseMove) {
        this.onMouseMove = onMouseMove;
    }

    // onMouseOut
    private String onMouseOut = null;

    /**
 * <p>Scripting code executed when a mouse out movement
 *     occurs over this component.</p>
     */
    public String getOnMouseOut() {
        if (this.onMouseOut != null) {
            return this.onMouseOut;
        }
        ValueBinding _vb = getValueBinding("onMouseOut");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Scripting code executed when a mouse out movement
 *     occurs over this component.</p>
     * @see #getOnMouseOut()
     */
    public void setOnMouseOut(String onMouseOut) {
        this.onMouseOut = onMouseOut;
    }

    // onMouseOver
    private String onMouseOver = null;

    /**
 * <p>Scripting code executed when the user moves the  mouse pointer into
 *     the boundary of this component.</p>
     */
    public String getOnMouseOver() {
        if (this.onMouseOver != null) {
            return this.onMouseOver;
        }
        ValueBinding _vb = getValueBinding("onMouseOver");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Scripting code executed when the user moves the  mouse pointer into
 *     the boundary of this component.</p>
     * @see #getOnMouseOver()
     */
    public void setOnMouseOver(String onMouseOver) {
        this.onMouseOver = onMouseOver;
    }

    // onMouseUp
    private String onMouseUp = null;

    /**
 * <p>Scripting code executed when the user releases a mouse button while
 *     the mouse pointer is on the component.</p>
     */
    public String getOnMouseUp() {
        if (this.onMouseUp != null) {
            return this.onMouseUp;
        }
        ValueBinding _vb = getValueBinding("onMouseUp");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Scripting code executed when the user releases a mouse button while
 *     the mouse pointer is on the component.</p>
     * @see #getOnMouseUp()
     */
    public void setOnMouseUp(String onMouseUp) {
        this.onMouseUp = onMouseUp;
    }

    // paginateButton
    private boolean paginateButton = false;
    private boolean paginateButton_set = false;

    /**
 * <p>Show table paginate button to allow users to switch between viewing all data on 
 * a single page (unpaginated) or to see data in multiple pages (paginated).</p>
     */
    public boolean isPaginateButton() {
        if (this.paginateButton_set) {
            return this.paginateButton;
        }
        ValueBinding _vb = getValueBinding("paginateButton");
        if (_vb != null) {
            Object _result = _vb.getValue(getFacesContext());
            if (_result == null) {
                return false;
            } else {
                return ((Boolean) _result).booleanValue();
            }
        }
        return false;
    }

    /**
 * <p>Show table paginate button to allow users to switch between viewing all data on 
 * a single page (unpaginated) or to see data in multiple pages (paginated).</p>
     * @see #isPaginateButton()
     */
    public void setPaginateButton(boolean paginateButton) {
        this.paginateButton = paginateButton;
        this.paginateButton_set = true;
    }

    // paginationControls
    private boolean paginationControls = false;
    private boolean paginationControls_set = false;

    /**
 * <p>Show the table pagination controls, which allow users to change which page is 
 * displayed. The controls include an input field for specifying the page number, a 
 * Go button to go to the specified page, and buttons for going to the first, last, 
 * previous, and next page.</p>
     */
    public boolean isPaginationControls() {
        if (this.paginationControls_set) {
            return this.paginationControls;
        }
        ValueBinding _vb = getValueBinding("paginationControls");
        if (_vb != null) {
            Object _result = _vb.getValue(getFacesContext());
            if (_result == null) {
                return false;
            } else {
                return ((Boolean) _result).booleanValue();
            }
        }
        return false;
    }

    /**
 * <p>Show the table pagination controls, which allow users to change which page is 
 * displayed. The controls include an input field for specifying the page number, a 
 * Go button to go to the specified page, and buttons for going to the first, last, 
 * previous, and next page.</p>
     * @see #isPaginationControls()
     */
    public void setPaginationControls(boolean paginationControls) {
        this.paginationControls = paginationControls;
        this.paginationControls_set = true;
    }

    // preferencesPanelFocusId
    private String preferencesPanelFocusId = null;

    /**
 * <p>The element id used to set focus when the preferences panel is open.</p>
     */
    public String getPreferencesPanelFocusId() {
        if (this.preferencesPanelFocusId != null) {
            return this.preferencesPanelFocusId;
        }
        ValueBinding _vb = getValueBinding("preferencesPanelFocusId");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>The element id used to set focus when the preferences panel is open.</p>
     * @see #getPreferencesPanelFocusId()
     */
    public void setPreferencesPanelFocusId(String preferencesPanelFocusId) {
        this.preferencesPanelFocusId = preferencesPanelFocusId;
    }

    // rules
    private String rules = null;

    public String getRules() {
        if (this.rules != null) {
            return this.rules;
        }
        ValueBinding _vb = getValueBinding("rules");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    public void setRules(String rules) {
        this.rules = rules;
    }

    // selectMultipleButton
    private boolean selectMultipleButton = false;
    private boolean selectMultipleButton_set = false;

    /**
 * <p>Show the button that is used for selecting multiple rows. The button is 
 * displayed in the Action Bar (top), and allows users to select all rows currently 
 * displayed. The button selects a column of checkboxes using the id specified in 
 * the selectId attribute of the <code>ui:tableColumn</code> tag.</p>
     */
    public boolean isSelectMultipleButton() {
        if (this.selectMultipleButton_set) {
            return this.selectMultipleButton;
        }
        ValueBinding _vb = getValueBinding("selectMultipleButton");
        if (_vb != null) {
            Object _result = _vb.getValue(getFacesContext());
            if (_result == null) {
                return false;
            } else {
                return ((Boolean) _result).booleanValue();
            }
        }
        return false;
    }

    /**
 * <p>Show the button that is used for selecting multiple rows. The button is 
 * displayed in the Action Bar (top), and allows users to select all rows currently 
 * displayed. The button selects a column of checkboxes using the id specified in 
 * the selectId attribute of the <code>ui:tableColumn</code> tag.</p>
     * @see #isSelectMultipleButton()
     */
    public void setSelectMultipleButton(boolean selectMultipleButton) {
        this.selectMultipleButton = selectMultipleButton;
        this.selectMultipleButton_set = true;
    }

    // selectMultipleButtonOnClick
    private String selectMultipleButtonOnClick = null;

    /**
 * <p>Scripting code executed when the user clicks the mouse on the select multiple 
 * button.</p>
     */
    public String getSelectMultipleButtonOnClick() {
        if (this.selectMultipleButtonOnClick != null) {
            return this.selectMultipleButtonOnClick;
        }
        ValueBinding _vb = getValueBinding("selectMultipleButtonOnClick");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Scripting code executed when the user clicks the mouse on the select multiple 
 * button.</p>
     * @see #getSelectMultipleButtonOnClick()
     */
    public void setSelectMultipleButtonOnClick(String selectMultipleButtonOnClick) {
        this.selectMultipleButtonOnClick = selectMultipleButtonOnClick;
    }

    // sortPanelFocusId
    private String sortPanelFocusId = null;

    /**
 * <p>The element id used to set focus when the sort panel is open.</p>
     */
    public String getSortPanelFocusId() {
        if (this.sortPanelFocusId != null) {
            return this.sortPanelFocusId;
        }
        ValueBinding _vb = getValueBinding("sortPanelFocusId");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>The element id used to set focus when the sort panel is open.</p>
     * @see #getSortPanelFocusId()
     */
    public void setSortPanelFocusId(String sortPanelFocusId) {
        this.sortPanelFocusId = sortPanelFocusId;
    }

    // sortPanelToggleButton
    private boolean sortPanelToggleButton = false;
    private boolean sortPanelToggleButton_set = false;

    /**
 * <p>Show the button that is used to open and close the sort panel.</p>
     */
    public boolean isSortPanelToggleButton() {
        if (this.sortPanelToggleButton_set) {
            return this.sortPanelToggleButton;
        }
        ValueBinding _vb = getValueBinding("sortPanelToggleButton");
        if (_vb != null) {
            Object _result = _vb.getValue(getFacesContext());
            if (_result == null) {
                return false;
            } else {
                return ((Boolean) _result).booleanValue();
            }
        }
        return false;
    }

    /**
 * <p>Show the button that is used to open and close the sort panel.</p>
     * @see #isSortPanelToggleButton()
     */
    public void setSortPanelToggleButton(boolean sortPanelToggleButton) {
        this.sortPanelToggleButton = sortPanelToggleButton;
        this.sortPanelToggleButton_set = true;
    }

    // style
    private String style = null;

    /**
 * <p>CSS style(s) to be applied when this component is rendered.</p>
     */
    public String getStyle() {
        if (this.style != null) {
            return this.style;
        }
        ValueBinding _vb = getValueBinding("style");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>CSS style(s) to be applied when this component is rendered.</p>
     * @see #getStyle()
     */
    public void setStyle(String style) {
        this.style = style;
    }

    // styleClass
    private String styleClass = null;

    /**
 * <p>CSS style class(es) to be applied when this component is rendered.</p>
     */
    public String getStyleClass() {
        if (this.styleClass != null) {
            return this.styleClass;
        }
        ValueBinding _vb = getValueBinding("styleClass");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>CSS style class(es) to be applied when this component is rendered.</p>
     * @see #getStyleClass()
     */
    public void setStyleClass(String styleClass) {
        this.styleClass = styleClass;
    }

    // summary
    private String summary = null;

    /**
 * <p>Summary text that describes the table for accessibility purposes</p>
     */
    public String getSummary() {
        if (this.summary != null) {
            return this.summary;
        }
        ValueBinding _vb = getValueBinding("summary");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Summary text that describes the table for accessibility purposes</p>
     * @see #getSummary()
     */
    public void setSummary(String summary) {
        this.summary = summary;
    }

    // tabIndex
    private int tabIndex = Integer.MIN_VALUE;
    private boolean tabIndex_set = false;

    /**
 * <p>The position of this component in the tabbing order sequence</p>
     */
    public int getTabIndex() {
        if (this.tabIndex_set) {
            return this.tabIndex;
        }
        ValueBinding _vb = getValueBinding("tabIndex");
        if (_vb != null) {
            Object _result = _vb.getValue(getFacesContext());
            if (_result == null) {
                return Integer.MIN_VALUE;
            } else {
                return ((Integer) _result).intValue();
            }
        }
        return Integer.MIN_VALUE;
    }

    /**
 * <p>The position of this component in the tabbing order sequence</p>
     * @see #getTabIndex()
     */
    public void setTabIndex(int tabIndex) {
        this.tabIndex = tabIndex;
        this.tabIndex_set = true;
    }

    // title
    private String title = null;

    /**
 * <p>The text displayed for the table title.</p>
     */
    public String getTitle() {
        if (this.title != null) {
            return this.title;
        }
        ValueBinding _vb = getValueBinding("title");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>The text displayed for the table title.</p>
     * @see #getTitle()
     */
    public void setTitle(String title) {
        this.title = title;
    }

    // toolTip
    private String toolTip = null;

    /**
 * <p>Display the text as a tooltip for this component</p>
     */
    public String getToolTip() {
        if (this.toolTip != null) {
            return this.toolTip;
        }
        ValueBinding _vb = getValueBinding("toolTip");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Display the text as a tooltip for this component</p>
     * @see #getToolTip()
     */
    public void setToolTip(String toolTip) {
        this.toolTip = toolTip;
    }

    // visible
    private boolean visible = false;
    private boolean visible_set = false;

    /**
 * <p>Use the visible attribute to indicate whether the component should be
 *     viewable by the user in the rendered HTML page. If set to false, the
 *     HTML code for the component is present in the page, but the component
 *     is hidden with style attributes. By default, visible is set to true, so
 *     HTML for the component HTML is included and visible to the user. If the
 *     component is not visible, it can still be processed on subsequent form
 *     submissions because the HTML is present.</p>
     */
    public boolean isVisible() {
        if (this.visible_set) {
            return this.visible;
        }
        ValueBinding _vb = getValueBinding("visible");
        if (_vb != null) {
            Object _result = _vb.getValue(getFacesContext());
            if (_result == null) {
                return false;
            } else {
                return ((Boolean) _result).booleanValue();
            }
        }
        return true;
    }

    /**
 * <p>Use the visible attribute to indicate whether the component should be
 *     viewable by the user in the rendered HTML page. If set to false, the
 *     HTML code for the component is present in the page, but the component
 *     is hidden with style attributes. By default, visible is set to true, so
 *     HTML for the component HTML is included and visible to the user. If the
 *     component is not visible, it can still be processed on subsequent form
 *     submissions because the HTML is present.</p>
     * @see #isVisible()
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
        this.visible_set = true;
    }

    // width
    private String width = null;

    /**
 * <p>Set the width of the table on the page (deprecated in HTML 4.0)</p>
     */
    public String getWidth() {
        if (this.width != null) {
            return this.width;
        }
        ValueBinding _vb = getValueBinding("width");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Set the width of the table on the page (deprecated in HTML 4.0)</p>
     * @see #getWidth()
     */
    public void setWidth(String width) {
        this.width = width;
    }

    /**
     * <p>Restore the state of this component.</p>
     */
    public void restoreState(FacesContext _context,Object _state) {
        Object _values[] = (Object[]) _state;
        super.restoreState(_context, _values[0]);
        this.align = (String) _values[1];
        this.augmentTitle = ((Boolean) _values[2]).booleanValue();
        this.augmentTitle_set = ((Boolean) _values[3]).booleanValue();
        this.bgColor = (String) _values[4];
        this.border = ((Integer) _values[5]).intValue();
        this.border_set = ((Boolean) _values[6]).booleanValue();
        this.cellPadding = (String) _values[7];
        this.cellSpacing = (String) _values[8];
        this.clearSortButton = ((Boolean) _values[9]).booleanValue();
        this.clearSortButton_set = ((Boolean) _values[10]).booleanValue();
        this.deselectMultipleButton = ((Boolean) _values[11]).booleanValue();
        this.deselectMultipleButton_set = ((Boolean) _values[12]).booleanValue();
        this.deselectMultipleButtonOnClick = (String) _values[13];
        this.deselectSingleButton = ((Boolean) _values[14]).booleanValue();
        this.deselectSingleButton_set = ((Boolean) _values[15]).booleanValue();
        this.deselectSingleButtonOnClick = (String) _values[16];
        this.extraActionBottomHtml = (String) _values[17];
        this.extraActionTopHtml = (String) _values[18];
        this.extraFooterHtml = (String) _values[19];
        this.extraPanelHtml = (String) _values[20];
        this.extraTitleHtml = (String) _values[21];
        this.filterId = (String) _values[22];
        this.filterPanelFocusId = (String) _values[23];
        this.filterText = (String) _values[24];
        this.footerText = (String) _values[25];
        this.frame = (String) _values[26];
        this.hiddenSelectedRows = ((Boolean) _values[27]).booleanValue();
        this.hiddenSelectedRows_set = ((Boolean) _values[28]).booleanValue();
        this.internalVirtualForm = ((Boolean) _values[29]).booleanValue();
        this.internalVirtualForm_set = ((Boolean) _values[30]).booleanValue();
        this.itemsText = (String) _values[31];
        this.lite = ((Boolean) _values[32]).booleanValue();
        this.lite_set = ((Boolean) _values[33]).booleanValue();
        this.onClick = (String) _values[34];
        this.onDblClick = (String) _values[35];
        this.onKeyDown = (String) _values[36];
        this.onKeyPress = (String) _values[37];
        this.onKeyUp = (String) _values[38];
        this.onMouseDown = (String) _values[39];
        this.onMouseMove = (String) _values[40];
        this.onMouseOut = (String) _values[41];
        this.onMouseOver = (String) _values[42];
        this.onMouseUp = (String) _values[43];
        this.paginateButton = ((Boolean) _values[44]).booleanValue();
        this.paginateButton_set = ((Boolean) _values[45]).booleanValue();
        this.paginationControls = ((Boolean) _values[46]).booleanValue();
        this.paginationControls_set = ((Boolean) _values[47]).booleanValue();
        this.preferencesPanelFocusId = (String) _values[48];
        this.rules = (String) _values[49];
        this.selectMultipleButton = ((Boolean) _values[50]).booleanValue();
        this.selectMultipleButton_set = ((Boolean) _values[51]).booleanValue();
        this.selectMultipleButtonOnClick = (String) _values[52];
        this.sortPanelFocusId = (String) _values[53];
        this.sortPanelToggleButton = ((Boolean) _values[54]).booleanValue();
        this.sortPanelToggleButton_set = ((Boolean) _values[55]).booleanValue();
        this.style = (String) _values[56];
        this.styleClass = (String) _values[57];
        this.summary = (String) _values[58];
        this.tabIndex = ((Integer) _values[59]).intValue();
        this.tabIndex_set = ((Boolean) _values[60]).booleanValue();
        this.title = (String) _values[61];
        this.toolTip = (String) _values[62];
        this.visible = ((Boolean) _values[63]).booleanValue();
        this.visible_set = ((Boolean) _values[64]).booleanValue();
        this.width = (String) _values[65];
    }

    /**
     * <p>Save the state of this component.</p>
     */
    public Object saveState(FacesContext _context) {
        Object _values[] = new Object[66];
        _values[0] = super.saveState(_context);
        _values[1] = this.align;
        _values[2] = this.augmentTitle ? Boolean.TRUE : Boolean.FALSE;
        _values[3] = this.augmentTitle_set ? Boolean.TRUE : Boolean.FALSE;
        _values[4] = this.bgColor;
        _values[5] = new Integer(this.border);
        _values[6] = this.border_set ? Boolean.TRUE : Boolean.FALSE;
        _values[7] = this.cellPadding;
        _values[8] = this.cellSpacing;
        _values[9] = this.clearSortButton ? Boolean.TRUE : Boolean.FALSE;
        _values[10] = this.clearSortButton_set ? Boolean.TRUE : Boolean.FALSE;
        _values[11] = this.deselectMultipleButton ? Boolean.TRUE : Boolean.FALSE;
        _values[12] = this.deselectMultipleButton_set ? Boolean.TRUE : Boolean.FALSE;
        _values[13] = this.deselectMultipleButtonOnClick;
        _values[14] = this.deselectSingleButton ? Boolean.TRUE : Boolean.FALSE;
        _values[15] = this.deselectSingleButton_set ? Boolean.TRUE : Boolean.FALSE;
        _values[16] = this.deselectSingleButtonOnClick;
        _values[17] = this.extraActionBottomHtml;
        _values[18] = this.extraActionTopHtml;
        _values[19] = this.extraFooterHtml;
        _values[20] = this.extraPanelHtml;
        _values[21] = this.extraTitleHtml;
        _values[22] = this.filterId;
        _values[23] = this.filterPanelFocusId;
        _values[24] = this.filterText;
        _values[25] = this.footerText;
        _values[26] = this.frame;
        _values[27] = this.hiddenSelectedRows ? Boolean.TRUE : Boolean.FALSE;
        _values[28] = this.hiddenSelectedRows_set ? Boolean.TRUE : Boolean.FALSE;
        _values[29] = this.internalVirtualForm ? Boolean.TRUE : Boolean.FALSE;
        _values[30] = this.internalVirtualForm_set ? Boolean.TRUE : Boolean.FALSE;
        _values[31] = this.itemsText;
        _values[32] = this.lite ? Boolean.TRUE : Boolean.FALSE;
        _values[33] = this.lite_set ? Boolean.TRUE : Boolean.FALSE;
        _values[34] = this.onClick;
        _values[35] = this.onDblClick;
        _values[36] = this.onKeyDown;
        _values[37] = this.onKeyPress;
        _values[38] = this.onKeyUp;
        _values[39] = this.onMouseDown;
        _values[40] = this.onMouseMove;
        _values[41] = this.onMouseOut;
        _values[42] = this.onMouseOver;
        _values[43] = this.onMouseUp;
        _values[44] = this.paginateButton ? Boolean.TRUE : Boolean.FALSE;
        _values[45] = this.paginateButton_set ? Boolean.TRUE : Boolean.FALSE;
        _values[46] = this.paginationControls ? Boolean.TRUE : Boolean.FALSE;
        _values[47] = this.paginationControls_set ? Boolean.TRUE : Boolean.FALSE;
        _values[48] = this.preferencesPanelFocusId;
        _values[49] = this.rules;
        _values[50] = this.selectMultipleButton ? Boolean.TRUE : Boolean.FALSE;
        _values[51] = this.selectMultipleButton_set ? Boolean.TRUE : Boolean.FALSE;
        _values[52] = this.selectMultipleButtonOnClick;
        _values[53] = this.sortPanelFocusId;
        _values[54] = this.sortPanelToggleButton ? Boolean.TRUE : Boolean.FALSE;
        _values[55] = this.sortPanelToggleButton_set ? Boolean.TRUE : Boolean.FALSE;
        _values[56] = this.style;
        _values[57] = this.styleClass;
        _values[58] = this.summary;
        _values[59] = new Integer(this.tabIndex);
        _values[60] = this.tabIndex_set ? Boolean.TRUE : Boolean.FALSE;
        _values[61] = this.title;
        _values[62] = this.toolTip;
        _values[63] = this.visible ? Boolean.TRUE : Boolean.FALSE;
        _values[64] = this.visible_set ? Boolean.TRUE : Boolean.FALSE;
        _values[65] = this.width;
        return _values;
    }

}
