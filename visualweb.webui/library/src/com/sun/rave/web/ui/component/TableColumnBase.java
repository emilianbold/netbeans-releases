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
 * Use the <code>ui:tableColumn</code>
 * tag to define the column cells of a table, inside a <a
 * href="tableRowGroup.html"><code>ui:table</code></a>
 * tag.&nbsp; A <code>ui:tableRowGroup</code> must include at least one <code>ui:tableColumn</code>
 * tag.<br>
 * <p><span style="color: rgb(0, 0, 0);">The <code>ui:table</code>
 * tag is
 * used to define the structure and actions of the table, and is a
 * container for <a href="tableRowGroup.html"><code>ui:tableRowGroup</code></a>
 * which define the rows of a table.&nbsp; The <code>ui:tableRowGroup</code>
 * tag is a container for <code></code></span><span
 * style="color: rgb(0, 0, 0);"><code>ui:tableColumn</code></span><span
 * style="color: rgb(0, 0, 0);"><code></code>
 * tags, which are used&nbsp;<code></code>to define the
 * columns of the table.&nbsp;</span><span
 * style="color: rgb(102, 102, 204);"><span style="color: rgb(0, 0, 0);">
 * The </span><a href="table.html" style="color: rgb(0, 0, 0);">documentation
 * for the <code>ui:table</code> tag</a><span style="color: rgb(0, 0, 0);">
 * contains detailed information
 * about the table component.&nbsp; This page provides details about how
 * to define table columns only.</span><br>
 * </span></p>
 * <p>
 * </p>
 * <p>
 * </p>
 * <h3>HTML Elements and Layout</h3>
 * <span style="text-decoration: line-through;"></span><span
 * style="color: rgb(0, 0, 0);">The tableColumn component is
 * used to define attributes for XHTML <code>&lt;td&gt;</code>
 * elements, which are used
 * to display table data cells. However, the rendering of
 * column headers and footers is handled by the tableRowGroup component.
 * The <a href="#diagram">diagram</a> shows the table layout, and
 * highlights the areas that are defined with the <code>ui:tableColumn</code>
 * tag. </span><br>
 * <br>
 * <a name="diagram"></a><br>
 * <table style="text-align: left; width: 100%;" border="1" cellpadding="2"
 * cellspacing="2">
 * <tbody>
 * <tr style="color: rgb(192, 192, 192);">
 * <td
 * style="vertical-align: top; background-color: rgb(153, 153, 153);"
 * rowspan="1" colspan="1">Title
 * Bar&nbsp;</td>
 * </tr>
 * <tr style="color: rgb(153, 153, 153);">
 * <td
 * style="vertical-align: top; background-color: rgb(208, 208, 208);"
 * rowspan="1" colspan="1">Action
 * Bar (top)&nbsp;&nbsp;</td>
 * </tr>
 * <tr>
 * <td
 * style="vertical-align: top; background-color: rgb(210, 224, 235);">
 * <table style="text-align: left; width: 100%;" border="1"
 * cellpadding="2" cellspacing="2">
 * <tbody>
 * <tr>
 * <td style="vertical-align: top;">Column Header <big><span
 * style="color: rgb(0, 0, 0);"><small>(specified </small></span></big>with
 * <code>headerText</code>
 * attribute or <code>header</code> facet <big><span
 * style="color: rgb(0, 0, 0);"><small>in first <code>ui:tableColumn</code>
 * tag in&nbsp; <code>ui:tableRowGroup</code> tag)</small></span></big></td>
 * <td style="vertical-align: top;">Column Header <big><span
 * style="color: rgb(0, 0, 0);"><small>(specified </small></span></big>with
 * <code>headerText</code>
 * attribute or <code>header</code> facet <big><span
 * style="color: rgb(0, 0, 0);"><small>in second <code>ui:tableColumn</code>
 * tag in <code>ui:tableRowGroup</code> tag)</small></span></big></td>
 * </tr>
 * <tr style="color: rgb(153, 153, 153);">
 * <td
 * style="vertical-align: top; background-color: rgb(232, 232, 232);"
 * rowspan="1" colspan="2">Group
 * Header Bar&nbsp;</td>
 * </tr>
 * <tr>
 * <td
 * style="vertical-align: top; background-color: rgb(232, 232, 232); color: rgb(153, 153, 153);"><span
 * style="font-style: italic;">Table
 * data</span> <code><br>
 * <br>
 * <br>
 * <br>
 * <br>
 * </code></td>
 * <td
 * style="vertical-align: top; background-color: rgb(232, 232, 232); color: rgb(153, 153, 153);"><span
 * style="font-style: italic;">Table
 * data</span> <code></code></td>
 * </tr>
 * <tr>
 * <td style="vertical-align: top;">Column Footer<span
 * style="color: rgb(0, 0, 0);"> (specified </span>with <code>footerText</code>
 * attribute or <code>footer</code>
 * facet <span style="color: rgb(0, 0, 0);">in first <code>ui:tableColumn</code>
 * tag in <code>ui:tableRowGroup</code> tag)</span></td>
 * <td style="vertical-align: top;">Column Footer <span
 * style="color: rgb(0, 0, 0);">(specified </span>with <code>footerText</code>
 * attribute or <code>footer</code>
 * facet <span style="color: rgb(0, 0, 0);">in second <code>ui:tableColumn</code>
 * tag in <code>ui:tableRowGroup</code> tag)</span></td>
 * </tr>
 * <tr style="color: rgb(153, 153, 153);">
 * <td
 * style="vertical-align: top; background-color: rgb(232, 232, 232);"
 * rowspan="1" colspan="2">Group
 * Footer Bar&nbsp;</td>
 * </tr>
 * <tr>
 * <td style="vertical-align: top;">Table
 * Column Footer (specified with <code>tableFooterText</code>
 * attribute <code></code>or <code>tableFooter</code> facet in <code>ui:tableColumn</code>
 * tag)</td>
 * <td style="vertical-align: top;">Table
 * Column Footer (specified with <code>tableFooterText</code>
 * attribute <code></code>or <code>tableFooter</code> facet <code></code>in
 * <code>ui:tableColumn</code>
 * tag)</td>
 * </tr>
 * </tbody>
 * </table>
 * <big><span style="color: rgb(0, 0, 0);"> </span></big></td>
 * </tr>
 * <tr>
 * <td
 * style="vertical-align: top; background-color: rgb(208, 208, 208);"><span
 * style="color: rgb(153, 153, 153);">Action
 * Bar (bottom)&nbsp;</span><small> </small> </td>
 * </tr>
 * <tr style="color: rgb(192, 192, 192);">
 * <td
 * style="vertical-align: top; background-color: rgb(153, 153, 153);"
 * rowspan="1" colspan="1">Footer<code></code><code> </code></td>
 * </tr>
 * </tbody>
 * </table>
 * <br style="color: rgb(102, 102, 204);">
 * <h4 style="color: rgb(0, 0, 0);">Column Header</h4>
 * <span style="color: rgb(0, 0, 0);">The Column Header area
 * displays
 * a header for each table column.&nbsp; If you specify the text of a
 * column header with the <code>headerText</code> attribute in the <code>ui:tableColumn</code>
 * tag, the default implementation of the header is rendered. You can
 * specify a separate component to provide column header content by using
 * the <code>header</code>
 * facet, which overrides the <code>headerText</code> attribute.&nbsp;
 * You can add extra HTML code to the
 * header's rendered<code> &lt;td&gt;</code> element with the <code>extraHeaderHtml</code>
 * attribute.<br>
 * <br>
 * The following <code>ui:tableColumn</code> attributes can be used to
 * change
 * the appearance and behavior for sorting of the Column Header:<br>
 * </span>
 * <ul style="color: rgb(0, 0, 0);">
 * <li>
 * <p><code>sort</code>
 * specifies a sort key and makes a column sortable.&nbsp; </p>
 * <p> </p>
 * </li>
 * <li>
 * <p><code>sortIcon</code>
 * specifies a theme identifier to select a different image to use as the
 * sort icon that is displayed in the header of a sortable column.</p>
 * </li>
 * <li>
 * <p><code>sortImageURL</code>
 * specifies the path to an image to use as the sort icon that is
 * displayed in the header of a
 * sortable column.</p>
 * </li>
 * <li>
 * <p><code>descending</code> when set to true causes the column
 * to be sorted in descending order.</p>
 * </li>
 * <li><code>severity </code>specifies the severity of an alarm
 * in each cell, and causes the column to&nbsp; sort on the severity value
 * if used with the <code>sort</code> attribute. <code></code></li>
 * </ul>
 * <span style="color: rgb(0, 0, 0);">
 * </span>
 * <h4 style="color: rgb(0, 0, 0);">Column Footer</h4>
 * <span style="color: rgb(0, 0, 0);">The Column Footers area
 * displays a footer for each table column.
 * If you specify the text of a column footer with the <code>footerText</code>
 * attribute in the <code>ui:tableColumn</code> tag, the default
 * implementation of the footer is rendered. You can specify a separate
 * component to provide footer content by using the <code>footer</code>
 * facet, which overrides the <code>footerText</code> attribute.&nbsp;
 * You can add extra HTML code to the
 * footer's rendered<code> &lt;td&gt;</code> element with the <code>extraFooterHtml</code>
 * attribute.<br>
 * </span>
 * <h4 style="color: rgb(0, 0, 0);">Table Column Footer</h4>
 * <span style="color: rgb(0, 0, 0);">The Table Column
 * Footers area displays column footers at the
 * bottom of the table.
 * The table column footers are useful in tables with multiple groups of
 * rows. If you specify the text of table column footers with the <code>tableFooterText</code>
 * attribute, the default implementation of the footer is rendered.&nbsp;
 * You can specify a separate component to provide the content for a table
 * column footer by
 * using the <code>tableFooter</code> facet, which overrides the <code>tableFooterText</code>
 * attribute.&nbsp; You
 * can add extra HTML code to the table footer's rendered<code> &lt;td&gt;</code>
 * element with the <code>extraTableFooterHtml</code> attribute.<br>
 * </span>
 * <h4 style="color: rgb(0, 0, 0);">Alignment and Formatting
 * of Cells</h4>
 * <span style="color: rgb(0, 0, 0);">In addition to defining the
 * headers and footers for columns, the
 * <code>ui:tableColumn</code> tag can be used to set other aspects of the
 * table's
 * appearance and&nbsp; behavior.&nbsp; <br>
 * <br>
 * The following attributes affect
 * the alignment of table cells:<br>
 * </span>
 * <ul style="color: rgb(0, 0, 0);">
 * <li><code>align </code>specifies the horizontal alignment
 * for
 * the cell data in the
 * column</li>
 * <li><code>alignKey </code>specifies a particular data element on
 * which to align the
 * cell data <br>
 * </li>
 * <li><code>char </code>specifies a character to use for
 * horizontal alignment of cell data<br>
 * </li>
 * <li><code>charOff </code>specifies the offset of
 * the first
 * occurrence of
 * the alignment character</li>
 * <li><code>valign </code>specifies the vertical alignment
 * for
 * the content of each cell</li>
 * </ul>
 * <span style="color: rgb(0, 0, 0);">
 * Attributes that can be used to make the column headers more accessible
 * for adaptive technologies include:<br>
 * </span>
 * <ul style="color: rgb(0, 0, 0);">
 * <li> <code>scope</code> set
 * to a keyword to specify the portion of the table that this header
 * applies to. <br>
 * </li>
 * <li><code>rowHeader</code>
 * set to true to specify that the content of the column's cells applies
 * to the row in which the
 * cell is located. </li>
 * </ul>
 * <span style="color: rgb(0, 0, 0);">Attributes that affect other
 * aspects of cells include:</span><br style="color: rgb(0, 0, 0);">
 * <ul style="color: rgb(0, 0, 0);">
 * <li><code>abbr</code>&nbsp;
 * specifies an abbreviated form of the cell's content, to
 * be used when the browser has little space to render the content.<br>
 * </li>
 * <li><code>embeddedActions</code>
 * when set to true causes separators to be rendered
 * between multiple action hyperlinks.</li>
 * <li><code>emptyCell</code> when
 * set to
 * true causes an unexpectedly empty cell to be
 * rendered with an appropriate image.</li>
 * <li><code>spacerColumn</code> when
 * set to true causes the column to be rendered as a blank column to
 * enhance table spacing.<code><br>
 * </code></li>
 * <li><code>height </code>specifies
 * the height of a column's cells.<code><br>
 * </code></li>
 * <li><code>width </code>specifies
 * the width of a column's cells.<code><br>
 * </code></li>
 * <li><code>nowrap </code>prevents
 * the content of the cell from wrapping to a new line.<br>
 * </li>
 * </ul>
 * <h4 style="color: rgb(0, 0, 0);">Selection Column</h4>
 * <span style="color: rgb(0, 0, 0);">To make table rows selectable,
 * the first column of the table should display only checkboxes or radio
 * buttons that the user clicks to select the row. When you set the <code>selectId</code>
 * attribute in the <code>ui:tableColumn</code> tag and include a <code>ui:checkbox</code>
 * or <code>ui:radioButton</code> tag as a child of the <code>ui:tableColumn</code></span><span
 * style="color: rgb(102, 102, 204);"><span style="color: rgb(0, 0, 0);">
 * tag, the first column is rendered
 * appropriately.&nbsp; See the</span><a href="#SelectSingleRow"
 * style="color: rgb(0, 0, 0);"> Select Single
 * Row example </a><span style="color: rgb(0, 0, 0);">for more
 * information. </span><br style="color: rgb(0, 0, 0);">
 * <br>
 * </span>
 * <h3>Facets</h3>
 * <span style="color: rgb(0, 0, 0);">The </span><code
 * style="color: rgb(0, 0, 0);">ui:tableColumn</code><span
 * style="color: rgb(0, 0, 0);"> tag supports the following facets,
 * which
 * allow you to customize the
 * layout of the component.</span><br style="color: rgb(0, 0, 0);">
 * <br style="color: rgb(0, 0, 0);">
 * <table style="width: 100%; color: rgb(0, 0, 0);" border="1"
 * cellpadding="2" cellspacing="2">
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
 * <td><code>footer&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * <br>
 * </code></td>
 * <td>Footer that is displayed at the bottom of the column within
 * the group of rows. The footer applies to the column of cells that are
 * defined
 * by the <code>ui:tableColumn</code>
 * tag.&nbsp; This facet can be used to replace the default footer for the
 * column.<br>
 * </td>
 * </tr>
 * <tr>
 * <td><code>header</code></td>
 * <td>Header that applies to the column of cells that are defined
 * by the <code>ui:tableColumn</code>
 * tag. This facet can be used to replace the default header for the
 * column. </td>
 * </tr>
 * <tr>
 * <td style="vertical-align: top;"><code>tableFooter<br>
 * </code></td>
 * <td style="vertical-align: top;">Footer that is displayed at the
 * bottom of the table, below the last group of rows, above the Action Bar
 * and overall table footer. The table footer content should apply to the
 * column for all the groups of rows in the table.&nbsp; This facet can be
 * used to replace the default table footer for the
 * column. </td>
 * </tr>
 * </tbody>
 * </table>
 * <br style="color: rgb(0, 0, 0);">
 * <h3 style="color: rgb(0, 0, 0);">Client Side JavaScript
 * Functions</h3>
 * <span style="color: rgb(0, 0, 0);">See the </span><a
 * href="table.html#JavaScript" style="color: rgb(0, 0, 0);"><code>ui:table</code>
 * tag's
 * JavaScript documentation</a><span style="color: rgb(0, 0, 0);">.&nbsp;
 * The same functions are used for the
 * <code>ui:tableColumn</code> tag.</span><br>
 * <br>
 * <h3>Examples
 * </h3>
 * <span style="color: rgb(0, 0, 0);">The following examples use a
 * backing bean called </span><a href="table.html#Example:_TableBean_"
 * style="color: rgb(0, 0, 0);">TableBean</a><span
 * style="color: rgb(0, 0, 0);">,
 * which is shown in the <code>ui:table</code> tag documentation. </span><a
 * href="table.html#UtilityClasses" style="color: rgb(0, 0, 0);">Utility
 * classes</a><span style="color: rgb(0, 0, 0);"> used in the examples are
 * included
 * in this page, after the examples.&nbsp;
 * Additional examples are shown in the </span><code
 * style="color: rgb(0, 0, 0);"><a href="table.html#Examples">ui:table</a></code><span
 * style="color: rgb(0, 0, 0);">
 * and </span><code style="color: rgb(0, 0, 0);"><a
 * href="tableRowGroup.html">ui:tableRowGroup</a></code><span
 * style="color: rgb(0, 0, 0);">
 * documents.<br>
 * <br>
 * All examples assume that the <code>ui:table</code> tag is contained
 * within an HTML <code>&lt;form&gt;</code> element so that actions can
 * submit form data. <br>
 * </span><br style="color: rgb(0, 0, 0);">
 * <span style="font-weight: bold; color: rgb(0, 0, 0);">Examples in
 * this file:<br>
 * <br>
 * </span>
 * <div style="margin-left: 40px; color: rgb(0, 0, 0);"><a
 * href="tableColumn.html#SortableTable">Example
 * 1: Sortable Table</a><br>
 * <p><a href="tableColumn.html#SelectSingleRow">Example
 * 2: Select Single
 * Row</a><br>
 * </p>
 * <p><a href="tableColumn.html#SelectMultipleRows">Example
 * 3: Select
 * Multiple Rows</a><br>
 * </p>
 * <p><a href="#HiddenSelectedRows">Example 4: Hidden Selected Rows</a><br>
 * </p>
 * <a href="#SpacerColumn">Example
 * 5: Spacer Columns</a><br>
 * <p><a href="#EmptyCells">Example 6: Empty Cells</a><br>
 * </p>
 * <p><a href="#EmbeddedActions">Example 7:&nbsp; Embedded Actions</a><br>
 * </p>
 * <p><a href="#Alarms">Example 8: Alarms</a><br>
 * </p>
 * <p><a href="#MultiColumnHeaders">Example 9: Multiple Column Headers and
 * Footers</a><br>
 * </p>
 * </div>
 * <span style="font-weight: bold; color: rgb(0, 0, 0);">Supporting
 * files:</span>
 * <div style="margin-left: 40px;">
 * <p style="color: rgb(0, 0, 0);"><a href="table.html#Example:_TableBean_">TableBean
 * backing bean in <code>ui:table</code> documentation<br>
 * </a></p>
 * <p><a href="table.html#UtilityClasses" style="color: rgb(0, 0, 0);">Utility
 * classes in <code>ui:table</code> documentation</a><a
 * href="table.html#UtilityClasses"><span style="color: rgb(0, 0, 0);"> </span><br>
 * </a></p>
 * </div>
 * <h3><a name="SortableTable"></a>Example 1: Sortable Table<br>
 * </h3>
 * <span style="text-decoration: line-through;"></span><span
 * style="color: rgb(0, 0, 0);">This example shows how to
 * implement table sorting, and uses the </span><a
 * href="table.html#Example:_TableBean_" style="color: rgb(0, 0, 0);">TableBean</a><span
 * style="color: rgb(0, 0, 0);"> and <a href="table.html#Name.java">Name.java</a>
 * code shown in the <code>ui:table</code> documentation. Notice that the
 * <code>ui:table</code>
 * tag includes the <code>clearSortButton</code> attribute to enable
 * users to clear any sorts applied to the table.<br>
 * <br>
 * The value binding objects that you assign to the <code>sort</code>
 * attribute in <code>ui:tableColumn</code> must be the proper data type
 * for sorting to work as expected. For example, you should not use String
 * objects for numeric data because the digits will be sorted according to
 * their ASCII values. Sorting the numbers as strings causes the number 2
 * to be displayed before the number 11, for example.&nbsp; Be sure to
 * sort using objects such as Number, Character, Date, Boolean, etc.</span><br
 * style="color: rgb(0, 0, 0);">
 * <br style="color: rgb(0, 0, 0);">
 * <span style="color: rgb(0, 0, 0);">You can use a FieldKey id or
 * value binding to define criteria for sorting the contents of
 * TableDataProvider. However, when sorting a column of checkboxes or
 * radio buttons, you must use a value binding because values are external
 * to the data (i.e., TableDataProvider does not contain FieldKey ids for
 * a selected checkbox value).&nbsp; <br>
 * <br>
 * User interface
 * guidelines recommend not setting a default initial sort. However, if
 * you want to set a default initial sort, you can do so by using the <code>addSort(SortCriteria)</code>
 * method of TableRowGroup. When the table is rendered, the data is sorted
 * and the primary sort column is highlighted.&nbsp; </span><br
 * style="color: rgb(0, 0, 0);">
 * <br style="color: rgb(102, 102, 204);">
 * <span style="color: rgb(102, 102, 204);"></span><code>&lt;!-- Sortable
 * Table --&gt;<br>
 * &lt;ui:table id="table1" <br>
 * &nbsp;&nbsp;&nbsp; clearSortButton="true"<br>
 * &nbsp;&nbsp;&nbsp; sortPanelToggleButton="true"<br>
 * &nbsp;&nbsp;&nbsp; title="Sortable Table"&gt;<br>
 * &nbsp; &lt;ui:tableRowGroup id="rowGroup1"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; sourceData="#{TableBean.groupB.names}"
 * sourceVar="name"&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;ui:tableColumn id="col1"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; alignKey="last"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; headerText="Last Name"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; rowHeader="true"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; sort="last"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;ui:staticText
 * text="#{name.value.last}"/&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;/ui:tableColumn&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;ui:tableColumn id="col2"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; alignKey="first"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; headerText="First Name"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; sort="first"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;ui:staticText
 * text="#{name.value.first}"/&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;/ui:tableColumn&gt;<br>
 * &nbsp; &lt;/ui:tableRowGroup&gt;<br>
 * &lt;/ui:table&gt;</code><br>
 * <h3><a name="SelectSingleRow"></a>Example 2: Select Single Row</h3>
 * <span style="text-decoration: line-through;"></span><span
 * style="color: rgb(0, 0, 0);">This example shows a column of
 * radioButton components that are used to select
 * a single table row. Dynamic row highlighting is set by invoking an
 * <code>initAllRows()</code> JavaScript function whenever the state of
 * the radio button
 * changes. The <code>initAllRows()</code>
 * function is defined in <a href="#select.js"><code>select.js</code>
 * shown below</a>. The
 * radio button state is maintained through the <code>selected</code>
 * attribute of the
 * <code>ui:tableRowGroup</code> tag. This example does not maintain state
 * across paginated
 * pages.</span><br style="color: rgb(0, 0, 0);">
 * <br style="color: rgb(0, 0, 0);">
 * <span style="color: rgb(0, 0, 0);">Note: UI guidelines recommend
 * that items should not remain selected when they cannot be seen by the
 * user. Using the
 * <code>com.sun.rave.web.ui.event.TableSelectPhaseListener</code> object
 * ensures that rows that are hidden from view are
 * deselected because the phase listener clears the selected state after
 * the
 * rendering phase. The <code>TableSelectPhaseListener</code>
 * object is used in this example in </span><a
 * href="table.html#Select.java" style="color: rgb(0, 0, 0);">Select.java
 * in the <code>ui:table</code> documentation</a><span
 * style="color: rgb(0, 0, 0);">.&nbsp; Also refer to the JavaDoc
 * for <code>TableSelectPhaseListener</code> for more information.&nbsp; </span><br
 * style="color: rgb(0, 0, 0);">
 * <br>
 * <code>&lt;!-- Single Select Row --&gt;<br>
 * &lt;ui:table id="table1"<br>
 * &nbsp;&nbsp;&nbsp; deselectSingleButton="true"<br>
 * &nbsp;&nbsp;&nbsp; paginateButton="true"<br>
 * &nbsp;&nbsp;&nbsp; paginationControls="true"<br>
 * &nbsp;&nbsp;&nbsp; title="Select Single Row"&gt;<br>
 * &nbsp; &lt;ui:tableRowGroup id="rowGroup1"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * selected="#{TableBean.groupA.select.selectedState}"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; sourceData="#{TableBean.groupA.names}"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; sourceVar="name" rows="5"&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;ui:tableColumn id="col0"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * onClick="setTimeout('initAllRows()', 0)"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; selectId="select"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * sort="#{TableBean.groupA.select.selectedState}"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;ui:radioButton id="select"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; name="select"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * selected="#{TableBean.groupA.select.selected}"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * selectedValue="#{TableBean.groupA.select.selectedValue}"/&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;/ui:tableColumn&gt;<br>
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
 * <span style="font-weight: bold;"><a name="select.js"></a>select.js</span><br>
 * <br>
 * This example shows the contents of the <code>select.js</code> file
 * used in the
 * example above.<br>
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
 * <h3><a name="SelectMultipleRows"></a>Example 3: Select Multiple Rows<br>
 * </h3>
 * <span style="text-decoration: line-through;"></span><span
 * style="color: rgb(0, 0, 0);">This example shows a column of
 * checkbox components that are used to select multiple table rows.
 * Dynamic row highlighting is set by
 * invoking an
 * <code>initAllRows()</code> JavaScript function whenever the state of
 * the checkbox
 * changes. The <code>initAllRows()</code>
 * function is defined in&nbsp; <a href="#select.js"><code>select.js </code>in
 * the previous example</a>. The checkbox state is maintained
 * through the <code>selected</code> attribute of the
 * <code>ui:tableRowGroup</code> tag. This example does not maintain state
 * across paginated
 * pages.</span><br style="color: rgb(0, 0, 0);">
 * <span style="color: rgb(0, 0, 0);"><br>
 * Note: UI guidelines recommend
 * that items should not remain selected when they cannot be seen by the
 * user. Using the
 * <code>com.sun.rave.web.ui.event.TableSelectPhaseListener</code> object
 * ensures that rows that are hidden from view are
 * deselected because the phase listener clears the selected state after
 * the
 * rendering phase. The <code>TableSelectPhaseListener</code>
 * object is used in this example in </span><a
 * href="table.html#Select.java" style="color: rgb(0, 0, 0);">Select.java</a><span
 * style="color: rgb(0, 0, 0);">,
 * shown in the </span><code style="color: rgb(0, 0, 0);">ui:table</code><span
 * style="color: rgb(0, 0, 0);"> documentation.&nbsp; Also refer to the
 * JavaDoc
 * for <code>TableSelectPhaseListener</code> for more information.&nbsp; </span><br>
 * <br>
 * <code>&lt;!-- Select Multiple Rows --&gt;<br>
 * &lt;ui:table id="table1"<br>
 * &nbsp;&nbsp;&nbsp; deselectMultipleButton="true"<br>
 * &nbsp;&nbsp;&nbsp; selectMultipleButton="true"<br>
 * &nbsp;&nbsp;&nbsp; paginateButton="true"<br>
 * &nbsp;&nbsp;&nbsp; paginationControls="true"<br>
 * &nbsp;&nbsp;&nbsp; title="Select Multiple Rows"&gt;<br>
 * &nbsp; &lt;ui:tableRowGroup id="rowGroup1"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * selected="#{TableBean.groupA.select.selectedState}"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; sourceData="#{TableBean.groupA.names}"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; sourceVar="name" rows="5"&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;ui:tableColumn id="col0"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; selectId="select"
 * sort="#{TableBean.groupA.select.selectedState}"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;ui:checkbox id="select"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * onClick="setTimeout('initAllRows()', 0)"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * selected="#{TableBean.groupA.select.selected}"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * selectedValue="#{TableBean.groupA.select.selectedValue}"/&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;/ui:tableColumn&gt;<br>
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
 * <span style="text-decoration: line-through;"></span>
 * <h3><a name="HiddenSelectedRows"></a>Example 4: Hidden Selected Rows<br>
 * </h3>
 * <span style="text-decoration: line-through;"></span><span
 * style="color: rgb(0, 0, 0);">This example is the same as <a
 * href="#SelectMultipleRows">Example 3: Select Multiple Rows</a> except
 * that it maintains state across paginated pages, and shows how to deal
 * appropriately with the possibility of hiding rows that have been
 * selected.&nbsp; As in the previous example, the first column is a
 * column of checkboxes that can be used to select multiple rows. The
 * checkbox state is maintained through the <code>selected</code>
 * attribute of
 * the
 * <code>ui:tableRowGroup</code> tag.&nbsp; Dynamic row highlighting is
 * set by
 * invoking an
 * <code>initAllRows()</code> JavaScript function whenever the state of
 * the checkbox
 * changes. The <code>initAllRows()</code>
 * function is defined in&nbsp; <a href="tableColumn.html#select.js"><code>select.js
 * </code>in the previous example</a>. </span><br
 * style="color: rgb(0, 0, 0);">
 * <br style="color: rgb(0, 0, 0);">
 * <span style="color: rgb(0, 0, 0);">If your table must maintain state,
 * you must set the <code>hiddenSelectedRows</code> attribute to true in
 * the <code>ui:table</code> tag as shown in this example. The attribute
 * causes text to be
 * displayed in the table title and footer to indicate the number of
 * selected rows that are currently hidden from view.&nbsp; See the </span><a
 * href="table.html#Select.java" style="color: rgb(0, 0, 0);">Select.java</a><span
 * style="color: rgb(0, 0, 0);">
 * </span><span style="color: rgb(102, 102, 204);"><span
 * style="color: rgb(0, 0, 0);">utility class in the </span><code
 * style="color: rgb(0, 0, 0);">ui:table</code><span
 * style="color: rgb(0, 0, 0);">
 * documentation.</span></span><br>
 * <br>
 * <code></code><span
 * style="font-weight: bold; text-decoration: line-through;"></span><code>&lt;!--
 * Hidden Selected Rows --&gt;<br>
 * &lt;ui:table id="table1"<br>
 * &nbsp;&nbsp;&nbsp; deselectMultipleButton="true"<br>
 * &nbsp;&nbsp;&nbsp;
 * deselectMultipleButtonOnClick="setTimeout('disableActions()', 0)"<br>
 * &nbsp;&nbsp;&nbsp; hiddenSelectedRows="true"<br>
 * &nbsp;&nbsp;&nbsp; paginateButton="true"<br>
 * &nbsp;&nbsp;&nbsp; paginationControls="true"<br>
 * &nbsp;&nbsp;&nbsp; selectMultipleButton="true"<br>
 * &nbsp;&nbsp;&nbsp;
 * selectMultipleButtonOnClick="setTimeout('disableActions()', 0)"<br>
 * &nbsp;&nbsp;&nbsp; title="Hidden Selected Rows"&gt;<br>
 * &nbsp; &lt;ui:tableRowGroup id="rowGroup1"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * binding="#{TableBean.groupA.tableRowGroup}"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * selected="#{TableBean.groupA.select.selectedState}"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; sourceData="#{TableBean.groupA.names}"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; sourceVar="name" rows="5"&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;ui:tableColumn id="col0"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; selectId="select"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * sort="#{TableBean.groupA.select.selectedState}"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;ui:checkbox id="select"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * binding="#{TableBean.groupA.checkbox}"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * onClick="setTimeout('initAllRows(); disableActions()', 0)"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * selected="#{TableBean.groupA.select.selected}"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * selectedValue="#{TableBean.groupA.select.selectedValue}"/&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;/ui:tableColumn&gt;<br>
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
 * &lt;/ui:table&gt;</code><br>
 * <h3><a name="SpacerColumn"></a>Example 5: Spacer Column<br>
 * </h3>
 * <span style="color: rgb(0, 0, 0);">This example shows how to
 * create a blank column to use for spacing in a table. The spacer column
 * is especially useful in two-column tables. A property table, which is
 * used to display properties for a single object, typically includes two
 * data columns. The
 * first column identifies the properties of the object, and the second
 * column displays the values for each of the properties. Because tables
 * created with the <code>ui:table</code> tag expand to the width of the
 * browser window, the two data columns might become so wide that the
 * properties and their values are not close together, and readability is
 * reduced. To solve this problem, you can add a spacer column to one side
 * of the table.<br>
 * <br>
 * In the example, the third column includes the <code>spacerColumn</code>
 * attribute set to true, and the <code>width </code>attribute set to
 * 70%. The column has no header or footer text, and no data. This column
 * acts to always keep
 * the data
 * of the first two columns in close proximity.&nbsp; If a column header
 * and footer are required, provide an empty string for the <code>headerText</code>
 * and
 * <code>footerText</code> attributes. Set the width attribute to a value
 * that achieves the desired spacing. </span><br>
 * <br>
 * <code>&lt;!-- Spacer Column --&gt;<br>
 * &lt;ui:table id="table1" title="Spacer Column"&gt;<br>
 * &nbsp; &lt;ui:tableRowGroup id="rowGroup1" <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; sourceData="#{TableBean.groupB.names}"
 * sourceVar="name"&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;ui:tableColumn id="col1"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; alignKey="last"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; footerText="Column Footer"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; headerText="Last Name"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; rowHeader="true"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;ui:staticText
 * text="#{name.value.last}"/&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;/ui:tableColumn&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;ui:tableColumn id="col2"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; alignKey="first"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; footerText="Column Footer"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; headerText="First Name"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;ui:staticText
 * text="#{name.value.first}"/&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;/ui:tableColumn&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;ui:tableColumn id="col3" spacerColumn="true"
 * width="70%"/&gt;<br>
 * &nbsp; &lt;/ui:tableRowGroup&gt;<br>
 * &lt;/ui:table&gt;</code><br>
 * <h3><a name="EmptyCells"></a>Example 6: Empty Cells<br>
 * </h3>
 * <span style="text-decoration: line-through;"></span><span
 * style="color: rgb(0, 0, 0);">This example shows how to
 * display a theme-specific icon that indicates an empty cell, when
 * the content of a table cell is not applicable or is unexpectedly empty.
 * UI guidelines
 * suggest that the empty cell icon should not be used for a value that is
 * truly null, such as an
 * empty alarm cell or a comment field that is blank. In addition, the
 * icon should not be used for cells that
 * contain user interface elements such as checkboxes or drop-down lists
 * when these elements are not applicable. Instead, the elements should
 * not be displayed so the cell is left empty.</span><br
 * style="color: rgb(0, 0, 0);">
 * <br style="color: rgb(0, 0, 0);">
 * <span style="color: rgb(0, 0, 0);">In this example, the <code>emptyCell
 * </code>attribute is set to
 * an expression that evaluates to true in every fifth row. In your
 * application, it is up to you to decide how to
 * test if the cell is truly empty. For example, you could use this
 * syntax: <code>emptyCell="#{name.value.last == null}"</code></span><br
 * style="color: rgb(0, 0, 0);">
 * <code style="color: rgb(102, 102, 204);"></code><code><br>
 * &lt;!-- Empty Cells --&gt;<br>
 * &lt;ui:table id="table1" title="Empty Cells"&gt;<br>
 * &nbsp; &lt;ui:tableRowGroup id="rowGroup1"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * selected="#{TableBean.groupB.select.selectedState}"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; sourceData="#{TableBean.groupB.names}"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; sourceVar="name" rows="5"&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;ui:tableColumn id="col0"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * emptyCell="#{name.tableRow.rowId % 5 == 0}"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; selectId="select"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;ui:checkbox id="select"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * onClick="setTimeout('initAllRows()', 0)"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * selected="#{TableBean.groupB.select.selected}"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * selectedValue="#{TableBean.groupB.select.selectedValue}"/&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;/ui:tableColumn&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;ui:tableColumn id="col1"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * emptyCell="#{name.tableRow.rowId % 5 == 0}"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; alignKey="last"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; headerText="Last Name"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; rowHeader="true"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;ui:staticText
 * text="#{name.value.last}"/&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;/ui:tableColumn&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;ui:tableColumn id="col2"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * emptyCell="#{name.tableRow.rowId % 5 == 0}"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; alignKey="first" <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; headerText="First Name"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;ui:staticText
 * text="#{name.value.first}"/&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;/ui:tableColumn&gt;<br>
 * &nbsp; &lt;/ui:tableRowGroup&gt;<br>
 * &lt;/ui:table&gt;</code><br>
 * <h3><a name="EmbeddedActions"></a>Example 7: Embedded Actions<br>
 * </h3>
 * <span style="color: rgb(0, 0, 0);">This example shows how to add
 * embedded actions to a table. If the
 * </span><code style="color: rgb(0, 0, 0);">ui:tableColumn</code><span
 * style="color: rgb(0, 0, 0);"> tag contains more than one tag
 * such as&nbsp;</span><code style="color: rgb(0, 0, 0);"></code><code
 * style="color: rgb(0, 0, 0);">ui:hyperlink</code><span
 * style="color: rgb(0, 0, 0);"> that a</span><code
 * style="color: rgb(0, 0, 0);"></code><span style="color: rgb(0, 0, 0);">re
 * used as embedded actions, you
 * should set the <code>ui:tableColumn</code> tag's </span><code
 * style="color: rgb(0, 0, 0);">embeddedActions </code><span
 * style="color: rgb(0, 0, 0);">attribute to
 * true. This attribute causes an action separator image to be displayed
 * between each of the rendered hyperlinks, as recommended in UI
 * guidelines.</span><br style="color: rgb(0, 0, 0);">
 * <br>
 * <code>&lt;!-- Embedded Actions --&gt;<br>
 * &lt;ui:table id="table1" title="Embedded Actions"&gt;<br>
 * &nbsp; &lt;ui:tableRowGroup id="rowGroup1"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; sourceData="#{TableBean.groupB.names}"
 * sourceVar="name"&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;ui:tableColumn id="col0" embeddedActions="true"
 * headerText="Actions"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;ui:hyperlink id="action1"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * action="#{TableBean.groupB.actions.action}"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; text="Action
 * 1"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;f:param name="param"
 * value="#{name.value.last}"/&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;/ui:hyperlink&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;ui:hyperlink id="action2"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * action="#{TableBean.groupB.actions.action}"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; text="Action
 * 2"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;f:param name="param"
 * value="#{name.tableRow.rowId}"/&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;/ui:hyperlink&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;/ui:tableColumn&gt;<br>
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
 * <h3><a name="Alarms"></a>Example 8: Alarms<br>
 * </h3>
 * <span style="text-decoration: line-through;"></span><span
 * style="color: rgb(0, 0, 0);">This example shows how to add
 * alarms to table data cells. The second </span><code
 * style="color: rgb(0, 0, 0);">ui:tableColumn</code><span
 * style="color: rgb(0, 0, 0);"> tag includes a </span><code
 * style="color: rgb(0, 0, 0);">ui:alarm</code><span
 * style="color: rgb(0, 0, 0);"> tag to render the alarm icon. The </span><code
 * style="color: rgb(0, 0, 0);">ui:tableColumn</code><span
 * style="color: rgb(0, 0, 0);"> tag's </span><code
 * style="color: rgb(0, 0, 0);">severity</code><span
 * style="color: rgb(0, 0, 0);">&nbsp; attribute is set to true, which
 * causes the
 * table data cell to appear highlighted according to level of
 * severity.&nbsp;
 * Note also that the column
 * is set to sort on the severity of the alarms. See
 * the <a href="table.html#Example:_TableBean_">TableBean</a>
 * backing bean
 * and&nbsp; </span><a style="color: rgb(0, 0, 0);"
 * href="table.html#Name.java">Name.java</a><span
 * style="color: rgb(0, 0, 0);"> utlity class example in the </span><code
 * style="color: rgb(0, 0, 0);">ui:table</code><span
 * style="color: rgb(0, 0, 0);"> documentation for the model data. </span><br
 * style="color: rgb(255, 153, 0);">
 * <br>
 * <code>&lt;!-- Alarms --&gt;<br>
 * &lt;ui:table id="table1" title="Alarms"&gt;<br>
 * &nbsp; &lt;ui:tableRowGroup id="rowGroup1" <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; sourceData="#{TableBean.groupB.names}"
 * sourceVar="name"&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;ui:tableColumn id="col1"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; alignKey="last"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; headerText="Last Name"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; rowHeader="true"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; sort="last"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;ui:staticText
 * text="#{name.value.last}"/&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;/ui:tableColumn&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;ui:tableColumn id="col2"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; alignKey="first"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; headerText="First Name"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * severity="#{name.value.severity}"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; sort="alarm"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;ui:alarm id="alarm"
 * severity="#{name.value.severity}"/&gt; <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;ui:staticText
 * text="#{name.value.first}"/&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;/ui:tableColumn&gt;<br>
 * &nbsp; &lt;/ui:tableRowGroup&gt;<br>
 * &lt;/ui:table&gt;</code><br>
 * <h3><a name="MultiColumnHeaders"></a>Example 9: Multiple Column Headers
 * &amp; Footers<br>
 * </h3>
 * This example shows how to use nested tableColumn components to create
 * multiple headers and footers. The column header of the parent
 * tableColumn component will span the nested tableColumn children. Not
 * all attributes are supported in this configuration. Typically, only the
 * header, footer, tableFooter, and sort would apply to a header and
 * spaning multuple columns. Sorting is supported, but is recommended only
 * for tableColumn children. Further, nesting tableColumn components will
 * render; however, the styles used here support one level of nexting.<br>
 * <br>
 * <span style="color: rgb(0, 0, 0);">This example shows how to use
 * nested <code>ui:tableColumn</code> tags to create
 * multiple headers and footers. The third <code>ui:tableColumn</code>
 * (col3) contains four nested columns col3a, col3b, col3c, and col3d. The
 * column header specified in col3 spans the four nested columns.&nbsp;
 * However, not
 * all <code>ui:tableColumn</code></span><span
 * style="color: rgb(102, 102, 204);"><span style="color: rgb(0, 0, 0);">
 * attributes are supported when the
 * tags are nested. Typically, only the
 * header, footer, tableFooter, and sort would apply to a header and
 * spaning multiple columns. Sorting in the parent tableColumn is
 * supported, but for usability, sorting is recommended only for
 * tableColumn children. In addition, nesting of more than one level of
 * tableColumn components will render, but the CSS styles only support one
 * level of nesting.&nbsp;&nbsp;&nbsp;</span>
 * <br style="color: rgb(0, 0, 0);">
 * </span><br>
 * <code>&lt;!-- Multiple Headers &amp;amp; Footers --&gt;<br>
 * &lt;ui:table id="table1"<br>
 * &nbsp;&nbsp;&nbsp; clearSortButton="true"<br>
 * &nbsp;&nbsp;&nbsp; deselectMultipleButton="true"<br>
 * &nbsp;&nbsp;&nbsp;
 * deselectMultipleButtonOnClick="setTimeout('disableActions()', 0)"<br>
 * &nbsp;&nbsp;&nbsp; footerText="Table Footer"<br>
 * &nbsp;&nbsp;&nbsp; paginateButton="true"<br>
 * &nbsp;&nbsp;&nbsp; paginationControls="true"<br>
 * &nbsp;&nbsp;&nbsp; selectMultipleButton="true"<br>
 * &nbsp;&nbsp;&nbsp;
 * selectMultipleButtonOnClick="setTimeout('disableActions()', 0)"<br>
 * &nbsp;&nbsp;&nbsp; sortPanelToggleButton="true"<br>
 * &nbsp;&nbsp;&nbsp; title="Multiple Headers &amp;amp; Footers"&gt;<br>
 * &nbsp; &lt;ui:tableRowGroup id="rowGroup1"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * binding="#{TableBean.groupA.tableRowGroup}"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * rows="#{TableBean.groupA.preferences.rows}"<br>
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
 * &nbsp;&nbsp;&nbsp; &lt;ui:tableColumn id="col1"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; alignKey="last"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; headerText="Last Name"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; rowHeader="true"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;ui:staticText
 * text="#{name.value.last}"/&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;/ui:tableColumn&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;ui:tableColumn id="col2"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; alignKey="first"
 * headerText="First Name"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;ui:staticText
 * text="#{name.value.first}"/&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;/ui:tableColumn&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;ui:tableColumn id="col3" headerText="Task
 * Status"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;!-- Nested Columns --&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;ui:tableColumn id="col3a"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; headerText="A"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * footerText="ColFtrA"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; sort="last"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * sortIcon="ALARM_CRITICAL_MEDIUM"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * tableFooterText="TblFtrA"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;ui:staticText
 * text="a"/&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;/ui:tableColumn&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;ui:tableColumn id="col3b"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; headerText="B"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * footerText="ColFtrB"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; sort="first"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * sortIcon="ALARM_MAJOR_MEDIUM"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * tableFooterText="TblFtrB"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;ui:staticText
 * text="b"/&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;/ui:tableColumn&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;ui:tableColumn id="col3c"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; headerText="C"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * footerText="ColFtrC"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * sortIcon="ALARM_MINOR_MEDIUM"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * tableFooterText="TblFtrC"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;ui:staticText
 * text="c"/&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;/ui:tableColumn&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;ui:tableColumn id="col3d"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; headerText="D"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * footerText="ColFtrD"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * sortIcon="ALARM_DOWN_MEDIUM"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * tableFooterText="TblFtrD"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;ui:staticText
 * text="d"/&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;/ui:tableColumn&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;/ui:tableColumn&gt;<br>
 * &nbsp; &lt;/ui:tableRowGroup&gt;<br>
 * &lt;/ui:table&gt;</code><span style="font-family: monospace;"></span><br>
 * <h3 style="color: rgb(0, 0, 0);">faces_config.xml Entry for
 * Managed Beans</h3>
 * <span style="color: rgb(0, 0, 0);">The examples use the <a
 * href="table.html#Example:_TableBean_">TableBean</a>
 * managed bean, which requires the following entry to be added to the </span><code
 * style="color: rgb(0, 0, 0);">faces_config.xml</code><span
 * style="color: rgb(0, 0, 0);"> file. </span><br>
 * <br>
 * <code>&lt;!DOCTYPE faces-config PUBLIC <br>
 * &nbsp;&nbsp;&nbsp; '-//Sun Microsystems, Inc.//DTD JavaServer Faces
 * Config 1.0//EN' <br>
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
 * &lt;/faces-config&gt;</code>
 * <br>
 * <p>Auto-generated component class.
 * Do <strong>NOT</strong> modify; all changes
 * <strong>will</strong> be lost!</p>
 */

public abstract class TableColumnBase extends javax.faces.component.UIComponentBase {

    /**
     * <p>Construct a new <code>TableColumnBase</code>.</p>
     */
    public TableColumnBase() {
        super();
        setRendererType("com.sun.rave.web.ui.TableColumn");
    }

    /**
     * <p>Return the identifier of the component family to which this
     * component belongs.  This identifier, in conjunction with the value
     * of the <code>rendererType</code> property, may be used to select
     * the appropriate {@link Renderer} for this component instance.</p>
     */
    public String getFamily() {
        return "com.sun.rave.web.ui.TableColumn";
    }

    // abbr
    private String abbr = null;

    /**
 * <p>An abbreviated version of the cell's content</p>
     */
    public String getAbbr() {
        if (this.abbr != null) {
            return this.abbr;
        }
        ValueBinding _vb = getValueBinding("abbr");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>An abbreviated version of the cell's content</p>
     * @see #getAbbr()
     */
    public void setAbbr(String abbr) {
        this.abbr = abbr;
    }

    // align
    private String align = null;

    /**
 * <p>Sets the horizontal alignment (left, right, justify, center) for the cell contents</p>
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
 * <p>Sets the horizontal alignment (left, right, justify, center) for the cell contents</p>
     * @see #getAlign()
     */
    public void setAlign(String align) {
        this.align = align;
    }

    // alignKey
    private Object alignKey = null;

    /**
 * <p>Use the <code>alignKey</code> attribute to specify the FieldKey id or FieldKey 
 * to be used as an identifier for a specific data element on which to align the 
 * table cell data in the column. If <code>alignKey</code> specifies a 
 * FieldKey, the FieldKey is used as is; otherwise, a FieldKey is created using 
 * the <code>alignKey</code> value that you specify. Alignment is based on 
 * the object type of the data element. For example, Date and Number objects are 
 * aligned "right", Character and String objects are aligned "left", and Boolean 
 * objects are aligned "center". All columns, including select columns, are 
 * aligned "left" by default. Note that the align property overrides this value.</p>
     */
    public Object getAlignKey() {
        if (this.alignKey != null) {
            return this.alignKey;
        }
        ValueBinding _vb = getValueBinding("alignKey");
        if (_vb != null) {
            return (Object) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Use the <code>alignKey</code> attribute to specify the FieldKey id or FieldKey 
 * to be used as an identifier for a specific data element on which to align the 
 * table cell data in the column. If <code>alignKey</code> specifies a 
 * FieldKey, the FieldKey is used as is; otherwise, a FieldKey is created using 
 * the <code>alignKey</code> value that you specify. Alignment is based on 
 * the object type of the data element. For example, Date and Number objects are 
 * aligned "right", Character and String objects are aligned "left", and Boolean 
 * objects are aligned "center". All columns, including select columns, are 
 * aligned "left" by default. Note that the align property overrides this value.</p>
     * @see #getAlignKey()
     */
    public void setAlignKey(Object alignKey) {
        this.alignKey = alignKey;
    }

    // axis
    private String axis = null;

    /**
 * <p>Provides a method for categorizing cells</p>
     */
    public String getAxis() {
        if (this.axis != null) {
            return this.axis;
        }
        ValueBinding _vb = getValueBinding("axis");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Provides a method for categorizing cells</p>
     * @see #getAxis()
     */
    public void setAxis(String axis) {
        this.axis = axis;
    }

    // bgColor
    private String bgColor = null;

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

    public void setBgColor(String bgColor) {
        this.bgColor = bgColor;
    }

    // char
    private String _char = null;

    public String getChar() {
        if (this._char != null) {
            return this._char;
        }
        ValueBinding _vb = getValueBinding("char");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    public void setChar(String _char) {
        this._char = _char;
    }

    // charOff
    private String charOff = null;

    public String getCharOff() {
        if (this.charOff != null) {
            return this.charOff;
        }
        ValueBinding _vb = getValueBinding("charOff");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    public void setCharOff(String charOff) {
        this.charOff = charOff;
    }

    // colSpan
    private int colSpan = Integer.MIN_VALUE;
    private boolean colSpan_set = false;

    /**
 * <p>The number of columns spanned by a cell</p>
     */
    public int getColSpan() {
        if (this.colSpan_set) {
            return this.colSpan;
        }
        ValueBinding _vb = getValueBinding("colSpan");
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
 * <p>The number of columns spanned by a cell</p>
     * @see #getColSpan()
     */
    public void setColSpan(int colSpan) {
        this.colSpan = colSpan;
        this.colSpan_set = true;
    }

    // descending
    private boolean descending = false;
    private boolean descending_set = false;

    /**
 * <p>Use the <code>descending</code> attribute to specify that the first 
 * user-applied sort is descending. By default, the first time a user clicks a 
 * column's sort button or column header, the sort is ascending. Note that this 
 * not an initial sort. The data is initially displayed unsorted.</p>
     */
    public boolean isDescending() {
        if (this.descending_set) {
            return this.descending;
        }
        ValueBinding _vb = getValueBinding("descending");
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
 * <p>Use the <code>descending</code> attribute to specify that the first 
 * user-applied sort is descending. By default, the first time a user clicks a 
 * column's sort button or column header, the sort is ascending. Note that this 
 * not an initial sort. The data is initially displayed unsorted.</p>
     * @see #isDescending()
     */
    public void setDescending(boolean descending) {
        this.descending = descending;
        this.descending_set = true;
    }

    // embeddedActions
    private boolean embeddedActions = false;
    private boolean embeddedActions_set = false;

    /**
 * <p>Set the <code>embeddedActions</code> attribute to true when the column includes 
 * more than one embedded action. This attribute causes a separator image to be 
 * displayed between the action links. This attribute is overridden by the 
 * <code>emptyCell</code> attribute.</p>
     */
    public boolean isEmbeddedActions() {
        if (this.embeddedActions_set) {
            return this.embeddedActions;
        }
        ValueBinding _vb = getValueBinding("embeddedActions");
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
 * <p>Set the <code>embeddedActions</code> attribute to true when the column includes 
 * more than one embedded action. This attribute causes a separator image to be 
 * displayed between the action links. This attribute is overridden by the 
 * <code>emptyCell</code> attribute.</p>
     * @see #isEmbeddedActions()
     */
    public void setEmbeddedActions(boolean embeddedActions) {
        this.embeddedActions = embeddedActions;
        this.embeddedActions_set = true;
    }

    // emptyCell
    private boolean emptyCell = false;
    private boolean emptyCell_set = false;

    /**
 * <p>Use the <code>emptyCell</code> attribute to cause a theme-specific image to be 
 * displayed when the content of a table cell is not applicable or is unexpectedly 
 * empty. You should not use this attribute for a value that is truly null, such 
 * as an empty alarm cell or a comment field that is blank. In addition, the image 
 * should not be used for cells that contain user interface elements such as 
 * checkboxes or drop-down lists when these elements are not applicable. Instead, 
 * the elements should simply not be displayed so the cell is left empty.</p>
     */
    public boolean isEmptyCell() {
        if (this.emptyCell_set) {
            return this.emptyCell;
        }
        ValueBinding _vb = getValueBinding("emptyCell");
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
 * <p>Use the <code>emptyCell</code> attribute to cause a theme-specific image to be 
 * displayed when the content of a table cell is not applicable or is unexpectedly 
 * empty. You should not use this attribute for a value that is truly null, such 
 * as an empty alarm cell or a comment field that is blank. In addition, the image 
 * should not be used for cells that contain user interface elements such as 
 * checkboxes or drop-down lists when these elements are not applicable. Instead, 
 * the elements should simply not be displayed so the cell is left empty.</p>
     * @see #isEmptyCell()
     */
    public void setEmptyCell(boolean emptyCell) {
        this.emptyCell = emptyCell;
        this.emptyCell_set = true;
    }

    // extraFooterHtml
    private String extraFooterHtml = null;

    /**
 * <p>Extra HTML code to be appended to the <code>&lt;td&gt;</code> HTML element that 
 * is rendered for the column footer. Use only code that is valid in an HTML 
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
 * <p>Extra HTML code to be appended to the <code>&lt;td&gt;</code> HTML element that 
 * is rendered for the column footer. Use only code that is valid in an HTML 
 * <code>&lt;td&gt;</code> element. The code you specify is inserted in the HTML 
 * element, and is not checked for validity. For example, you might set this 
 * attribute to <code>"nowrap=`nowrap'"</code>.</p>
     * @see #getExtraFooterHtml()
     */
    public void setExtraFooterHtml(String extraFooterHtml) {
        this.extraFooterHtml = extraFooterHtml;
    }

    // extraHeaderHtml
    private String extraHeaderHtml = null;

    /**
 * <p>Extra HTML code to be appended to the <code>&lt;th&gt;</code> HTML element that 
 * is rendered for the column header. Use only code that is valid in an HTML 
 * <code>&lt;td&gt;</code> element. The code you specify is inserted in the HTML 
 * element, and is not checked for validity. For example, you might set this 
 * attribute to <code>"nowrap=`nowrap'"</code>.</p>
     */
    public String getExtraHeaderHtml() {
        if (this.extraHeaderHtml != null) {
            return this.extraHeaderHtml;
        }
        ValueBinding _vb = getValueBinding("extraHeaderHtml");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Extra HTML code to be appended to the <code>&lt;th&gt;</code> HTML element that 
 * is rendered for the column header. Use only code that is valid in an HTML 
 * <code>&lt;td&gt;</code> element. The code you specify is inserted in the HTML 
 * element, and is not checked for validity. For example, you might set this 
 * attribute to <code>"nowrap=`nowrap'"</code>.</p>
     * @see #getExtraHeaderHtml()
     */
    public void setExtraHeaderHtml(String extraHeaderHtml) {
        this.extraHeaderHtml = extraHeaderHtml;
    }

    // extraTableFooterHtml
    private String extraTableFooterHtml = null;

    /**
 * <p>Extra HTML code to be appended to the <code>&lt;td&gt;</code> HTML element that 
 * is rendered for the table column footer. Use only code that is valid in an HTML 
 * <code>&lt;td&gt;</code> element. The code you specify is inserted in the HTML 
 * element, and is not checked for validity. For example, you might set this 
 * attribute to <code>"nowrap=`nowrap'"</code>.</p>
     */
    public String getExtraTableFooterHtml() {
        if (this.extraTableFooterHtml != null) {
            return this.extraTableFooterHtml;
        }
        ValueBinding _vb = getValueBinding("extraTableFooterHtml");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Extra HTML code to be appended to the <code>&lt;td&gt;</code> HTML element that 
 * is rendered for the table column footer. Use only code that is valid in an HTML 
 * <code>&lt;td&gt;</code> element. The code you specify is inserted in the HTML 
 * element, and is not checked for validity. For example, you might set this 
 * attribute to <code>"nowrap=`nowrap'"</code>.</p>
     * @see #getExtraTableFooterHtml()
     */
    public void setExtraTableFooterHtml(String extraTableFooterHtml) {
        this.extraTableFooterHtml = extraTableFooterHtml;
    }

    // footerText
    private String footerText = null;

    /**
 * <p>The text to be displayed in the column footer.</p>
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
 * <p>The text to be displayed in the column footer.</p>
     * @see #getFooterText()
     */
    public void setFooterText(String footerText) {
        this.footerText = footerText;
    }

    // headerText
    private String headerText = null;

    /**
 * <p>The text to be displayed in the column header.</p>
     */
    public String getHeaderText() {
        if (this.headerText != null) {
            return this.headerText;
        }
        ValueBinding _vb = getValueBinding("headerText");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>The text to be displayed in the column header.</p>
     * @see #getHeaderText()
     */
    public void setHeaderText(String headerText) {
        this.headerText = headerText;
    }

    // headers
    private String headers = null;

    /**
 * <p>Space separated list of header cell ID values</p>
     */
    public String getHeaders() {
        if (this.headers != null) {
            return this.headers;
        }
        ValueBinding _vb = getValueBinding("headers");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Space separated list of header cell ID values</p>
     * @see #getHeaders()
     */
    public void setHeaders(String headers) {
        this.headers = headers;
    }

    // height
    private String height = null;

    /**
 * <p>Set the cell height in pixels (deprecated in HTML 4.0)</p>
     */
    public String getHeight() {
        if (this.height != null) {
            return this.height;
        }
        ValueBinding _vb = getValueBinding("height");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Set the cell height in pixels (deprecated in HTML 4.0)</p>
     * @see #getHeight()
     */
    public void setHeight(String height) {
        this.height = height;
    }

    // noWrap
    private boolean noWrap = false;
    private boolean noWrap_set = false;

    /**
 * <p>Disable word wrapping (deprecated in HTML 4.0)</p>
     */
    public boolean isNoWrap() {
        if (this.noWrap_set) {
            return this.noWrap;
        }
        ValueBinding _vb = getValueBinding("noWrap");
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
 * <p>Disable word wrapping (deprecated in HTML 4.0)</p>
     * @see #isNoWrap()
     */
    public void setNoWrap(boolean noWrap) {
        this.noWrap = noWrap;
        this.noWrap_set = true;
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

    // rowHeader
    private boolean rowHeader = false;
    private boolean rowHeader_set = false;

    /**
 * <p>Use the <code>rowHeader</code> attribute to specify that the cells of the 
 * column are acting as row headers. Row headers are cells that "label" the row. 
 * For example, consider a table where the first column contains checkboxes, and 
 * the second column contains user names. The third and subsequent columns contain 
 * attributes of those users. The content of the cells in the user name column are 
 * acting as row headers. The <code>ui:tableColumn</code> tag for the user name 
 * column should set the <code>rowHeader</code> attribute to true. If a table 
 * contains, for example, a system log with time stamp and log entry columns, 
 * neither column is acting as a row header, so the <code>rowHeader</code> 
 * attribute should not be set. 
 * <br><br>
 * By default, most column cells are rendered by the table component with HTML 
 * <code>&lt;td scope="col"&gt;</code> elements. The exceptions are columns that 
 * contain checkboxes or radio buttons and spacer columns, all of which are 
 * rendered as <code>&lt;td&gt;</code> elements without a scope property. 
 * <br><br>
 * When you set the <code>rowHeader</code> attribute, the column cells are 
 * rendered as <code>&lt;th scope="row"&gt;</code> elements, which enables 
 * adaptive technologies such as screen readers to properly read the table to 
 * indicate that the contents of these cells are headers for the rows.</p>
     */
    public boolean isRowHeader() {
        if (this.rowHeader_set) {
            return this.rowHeader;
        }
        ValueBinding _vb = getValueBinding("rowHeader");
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
 * <p>Use the <code>rowHeader</code> attribute to specify that the cells of the 
 * column are acting as row headers. Row headers are cells that "label" the row. 
 * For example, consider a table where the first column contains checkboxes, and 
 * the second column contains user names. The third and subsequent columns contain 
 * attributes of those users. The content of the cells in the user name column are 
 * acting as row headers. The <code>ui:tableColumn</code> tag for the user name 
 * column should set the <code>rowHeader</code> attribute to true. If a table 
 * contains, for example, a system log with time stamp and log entry columns, 
 * neither column is acting as a row header, so the <code>rowHeader</code> 
 * attribute should not be set. 
 * <br><br>
 * By default, most column cells are rendered by the table component with HTML 
 * <code>&lt;td scope="col"&gt;</code> elements. The exceptions are columns that 
 * contain checkboxes or radio buttons and spacer columns, all of which are 
 * rendered as <code>&lt;td&gt;</code> elements without a scope property. 
 * <br><br>
 * When you set the <code>rowHeader</code> attribute, the column cells are 
 * rendered as <code>&lt;th scope="row"&gt;</code> elements, which enables 
 * adaptive technologies such as screen readers to properly read the table to 
 * indicate that the contents of these cells are headers for the rows.</p>
     * @see #isRowHeader()
     */
    public void setRowHeader(boolean rowHeader) {
        this.rowHeader = rowHeader;
        this.rowHeader_set = true;
    }

    // rowSpan
    private int rowSpan = Integer.MIN_VALUE;
    private boolean rowSpan_set = false;

    /**
 * <p>The number of rows spanned by a cell</p>
     */
    public int getRowSpan() {
        if (this.rowSpan_set) {
            return this.rowSpan;
        }
        ValueBinding _vb = getValueBinding("rowSpan");
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
 * <p>The number of rows spanned by a cell</p>
     * @see #getRowSpan()
     */
    public void setRowSpan(int rowSpan) {
        this.rowSpan = rowSpan;
        this.rowSpan_set = true;
    }

    // scope
    private String scope = null;

    /**
 * <p>Indicates that information in a cell is also acting as a header</p>
     */
    public String getScope() {
        if (this.scope != null) {
            return this.scope;
        }
        ValueBinding _vb = getValueBinding("scope");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Indicates that information in a cell is also acting as a header</p>
     * @see #getScope()
     */
    public void setScope(String scope) {
        this.scope = scope;
    }

    // selectId
    private String selectId = null;

    /**
 * <p>Use the <code>selectId</code> attribute in select columns, which contain 
 * checkboxes or radio buttons for selecting table rows. The value of 
 * <code>selectId</code> must match the <code>id</code> attribute of the checkbox 
 * or radioButton component that is a child of the tableColumn component. A fully 
 * qualified ID based on the tableColumn component ID and the 
 * <code>selectId</code> for the current row will be dynamically created for the 
 * <code>&lt;input&gt;</code> element that is rendered for the checkbox or radio 
 * button. The <code>selectId</code> is required for functionality that supports 
 * the toggle buttons for selecting rows. The <code>selectId</code> also 
 * identifies the column as a select column, for which the table component 
 * uses different CSS styles.</p>
     */
    public String getSelectId() {
        if (this.selectId != null) {
            return this.selectId;
        }
        ValueBinding _vb = getValueBinding("selectId");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Use the <code>selectId</code> attribute in select columns, which contain 
 * checkboxes or radio buttons for selecting table rows. The value of 
 * <code>selectId</code> must match the <code>id</code> attribute of the checkbox 
 * or radioButton component that is a child of the tableColumn component. A fully 
 * qualified ID based on the tableColumn component ID and the 
 * <code>selectId</code> for the current row will be dynamically created for the 
 * <code>&lt;input&gt;</code> element that is rendered for the checkbox or radio 
 * button. The <code>selectId</code> is required for functionality that supports 
 * the toggle buttons for selecting rows. The <code>selectId</code> also 
 * identifies the column as a select column, for which the table component 
 * uses different CSS styles.</p>
     * @see #getSelectId()
     */
    public void setSelectId(String selectId) {
        this.selectId = selectId;
    }

    // severity
    private String severity = null;

    /**
 * <p>Use the <code>severity</code> attribute when including the <code>ui:alarm</code> 
 * component in a column, to match the severity of the alarm. Valid values are 
 * described in the <code>ui:alarm</code> documentation. When the 
 * <code>severity</code> attribute is set in the tableColumn, the table 
 * component renders sort tool tips to indicate that the column will be sorted 
 * least/most severe first, and the table cell appears hightlighted according to 
 * the level of severity. This functionality is overridden by the 
 * <code>emptyCell</code> attribute.</p>
     */
    public String getSeverity() {
        if (this.severity != null) {
            return this.severity;
        }
        ValueBinding _vb = getValueBinding("severity");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Use the <code>severity</code> attribute when including the <code>ui:alarm</code> 
 * component in a column, to match the severity of the alarm. Valid values are 
 * described in the <code>ui:alarm</code> documentation. When the 
 * <code>severity</code> attribute is set in the tableColumn, the table 
 * component renders sort tool tips to indicate that the column will be sorted 
 * least/most severe first, and the table cell appears hightlighted according to 
 * the level of severity. This functionality is overridden by the 
 * <code>emptyCell</code> attribute.</p>
     * @see #getSeverity()
     */
    public void setSeverity(String severity) {
        this.severity = severity;
    }

    // sort
    private Object sort = null;

    /**
 * <p>Use the <code>sort</code> attribute to specify a FieldKey id or SortCriteria 
 * that defines the criteria to use for sorting the contents of a 
 * TableDataProvider. If SortCriteria is provided, the object is used for sorting 
 * as is. If an id is provided, a FieldIdSortCriteria is created for sorting. In 
 * addition, a value binding can also be used to sort on an object that is 
 * external to TableDataProvider, such as the selected state of a checkbox or 
 * radiobutton. When a value binding is used, a ValueBindingSortCriteria object 
 * is created for sorting. All sorting is based on the object type associated with 
 * the data element (for example, Boolean, Character, Comparator, Date, Number, 
 * and String). If the object type cannot be determined, the object is compared as 
 * a String. The <code>sort</code> attribute is required for a column to be shown 
 * as sortable.</p>
     */
    public Object getSort() {
        if (this.sort != null) {
            return this.sort;
        }
        ValueBinding _vb = getValueBinding("sort");
        if (_vb != null) {
            return (Object) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Use the <code>sort</code> attribute to specify a FieldKey id or SortCriteria 
 * that defines the criteria to use for sorting the contents of a 
 * TableDataProvider. If SortCriteria is provided, the object is used for sorting 
 * as is. If an id is provided, a FieldIdSortCriteria is created for sorting. In 
 * addition, a value binding can also be used to sort on an object that is 
 * external to TableDataProvider, such as the selected state of a checkbox or 
 * radiobutton. When a value binding is used, a ValueBindingSortCriteria object 
 * is created for sorting. All sorting is based on the object type associated with 
 * the data element (for example, Boolean, Character, Comparator, Date, Number, 
 * and String). If the object type cannot be determined, the object is compared as 
 * a String. The <code>sort</code> attribute is required for a column to be shown 
 * as sortable.</p>
     * @see #getSort()
     */
    public void setSort(Object sort) {
        this.sort = sort;
    }

    // sortIcon
    private String sortIcon = null;

    /**
 * <p>The theme identifier to use for the sort button that is displayed in the column 
 * header. Use this attribute to override the default image.</p>
     */
    public String getSortIcon() {
        if (this.sortIcon != null) {
            return this.sortIcon;
        }
        ValueBinding _vb = getValueBinding("sortIcon");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>The theme identifier to use for the sort button that is displayed in the column 
 * header. Use this attribute to override the default image.</p>
     * @see #getSortIcon()
     */
    public void setSortIcon(String sortIcon) {
        this.sortIcon = sortIcon;
    }

    // sortImageURL
    private String sortImageURL = null;

    /**
 * <p>Absolute or relative URL to the image used for the sort button that is 
 * displayed in the column header.</p>
     */
    public String getSortImageURL() {
        if (this.sortImageURL != null) {
            return this.sortImageURL;
        }
        ValueBinding _vb = getValueBinding("sortImageURL");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Absolute or relative URL to the image used for the sort button that is 
 * displayed in the column header.</p>
     * @see #getSortImageURL()
     */
    public void setSortImageURL(String sortImageURL) {
        this.sortImageURL = sortImageURL;
    }

    // spacerColumn
    private boolean spacerColumn = false;
    private boolean spacerColumn_set = false;

    /**
 * <p>Use the <code>spacerColumn</code> attribute to use the column as a blank column 
 * to enhance spacing in two or three column tables. When the 
 * <code>spacerColumn</code> attribute is true, the CSS styles applied to the 
 * column make it appear as if the columns are justified. If a column header and 
 * footer are required, provide an empty string for the <code>headerText</code> 
 * and <code>footerText</code> attributes. Set the <code>width</code> attribute to 
 * justify columns accordingly.</p>
     */
    public boolean isSpacerColumn() {
        if (this.spacerColumn_set) {
            return this.spacerColumn;
        }
        ValueBinding _vb = getValueBinding("spacerColumn");
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
 * <p>Use the <code>spacerColumn</code> attribute to use the column as a blank column 
 * to enhance spacing in two or three column tables. When the 
 * <code>spacerColumn</code> attribute is true, the CSS styles applied to the 
 * column make it appear as if the columns are justified. If a column header and 
 * footer are required, provide an empty string for the <code>headerText</code> 
 * and <code>footerText</code> attributes. Set the <code>width</code> attribute to 
 * justify columns accordingly.</p>
     * @see #isSpacerColumn()
     */
    public void setSpacerColumn(boolean spacerColumn) {
        this.spacerColumn = spacerColumn;
        this.spacerColumn_set = true;
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

    // tableFooterText
    private String tableFooterText = null;

    /**
 * <p>The text to be displayed in the table column footer. The table column footer is 
 * displayed once per table, and is especially useful in tables with multiple 
 * groups of rows.</p>
     */
    public String getTableFooterText() {
        if (this.tableFooterText != null) {
            return this.tableFooterText;
        }
        ValueBinding _vb = getValueBinding("tableFooterText");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>The text to be displayed in the table column footer. The table column footer is 
 * displayed once per table, and is especially useful in tables with multiple 
 * groups of rows.</p>
     * @see #getTableFooterText()
     */
    public void setTableFooterText(String tableFooterText) {
        this.tableFooterText = tableFooterText;
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

    // valign
    private String valign = null;

    /**
 * <p>Vertical alignment (top, middle, bottom) for the content of each cell in the column</p>
     */
    public String getValign() {
        if (this.valign != null) {
            return this.valign;
        }
        ValueBinding _vb = getValueBinding("valign");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Vertical alignment (top, middle, bottom) for the content of each cell in the column</p>
     * @see #getValign()
     */
    public void setValign(String valign) {
        this.valign = valign;
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
 * <p>Set the width of the column in either pixels or percent(deprecated in HTML 4.0)</p>
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
 * <p>Set the width of the column in either pixels or percent(deprecated in HTML 4.0)</p>
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
        this.abbr = (String) _values[1];
        this.align = (String) _values[2];
        this.alignKey = (Object) _values[3];
        this.axis = (String) _values[4];
        this.bgColor = (String) _values[5];
        this._char = (String) _values[6];
        this.charOff = (String) _values[7];
        this.colSpan = ((Integer) _values[8]).intValue();
        this.colSpan_set = ((Boolean) _values[9]).booleanValue();
        this.descending = ((Boolean) _values[10]).booleanValue();
        this.descending_set = ((Boolean) _values[11]).booleanValue();
        this.embeddedActions = ((Boolean) _values[12]).booleanValue();
        this.embeddedActions_set = ((Boolean) _values[13]).booleanValue();
        this.emptyCell = ((Boolean) _values[14]).booleanValue();
        this.emptyCell_set = ((Boolean) _values[15]).booleanValue();
        this.extraFooterHtml = (String) _values[16];
        this.extraHeaderHtml = (String) _values[17];
        this.extraTableFooterHtml = (String) _values[18];
        this.footerText = (String) _values[19];
        this.headerText = (String) _values[20];
        this.headers = (String) _values[21];
        this.height = (String) _values[22];
        this.noWrap = ((Boolean) _values[23]).booleanValue();
        this.noWrap_set = ((Boolean) _values[24]).booleanValue();
        this.onClick = (String) _values[25];
        this.onDblClick = (String) _values[26];
        this.onKeyDown = (String) _values[27];
        this.onKeyPress = (String) _values[28];
        this.onKeyUp = (String) _values[29];
        this.onMouseDown = (String) _values[30];
        this.onMouseMove = (String) _values[31];
        this.onMouseOut = (String) _values[32];
        this.onMouseOver = (String) _values[33];
        this.onMouseUp = (String) _values[34];
        this.rowHeader = ((Boolean) _values[35]).booleanValue();
        this.rowHeader_set = ((Boolean) _values[36]).booleanValue();
        this.rowSpan = ((Integer) _values[37]).intValue();
        this.rowSpan_set = ((Boolean) _values[38]).booleanValue();
        this.scope = (String) _values[39];
        this.selectId = (String) _values[40];
        this.severity = (String) _values[41];
        this.sort = (Object) _values[42];
        this.sortIcon = (String) _values[43];
        this.sortImageURL = (String) _values[44];
        this.spacerColumn = ((Boolean) _values[45]).booleanValue();
        this.spacerColumn_set = ((Boolean) _values[46]).booleanValue();
        this.style = (String) _values[47];
        this.styleClass = (String) _values[48];
        this.tableFooterText = (String) _values[49];
        this.toolTip = (String) _values[50];
        this.valign = (String) _values[51];
        this.visible = ((Boolean) _values[52]).booleanValue();
        this.visible_set = ((Boolean) _values[53]).booleanValue();
        this.width = (String) _values[54];
    }

    /**
     * <p>Save the state of this component.</p>
     */
    public Object saveState(FacesContext _context) {
        Object _values[] = new Object[55];
        _values[0] = super.saveState(_context);
        _values[1] = this.abbr;
        _values[2] = this.align;
        _values[3] = this.alignKey;
        _values[4] = this.axis;
        _values[5] = this.bgColor;
        _values[6] = this._char;
        _values[7] = this.charOff;
        _values[8] = new Integer(this.colSpan);
        _values[9] = this.colSpan_set ? Boolean.TRUE : Boolean.FALSE;
        _values[10] = this.descending ? Boolean.TRUE : Boolean.FALSE;
        _values[11] = this.descending_set ? Boolean.TRUE : Boolean.FALSE;
        _values[12] = this.embeddedActions ? Boolean.TRUE : Boolean.FALSE;
        _values[13] = this.embeddedActions_set ? Boolean.TRUE : Boolean.FALSE;
        _values[14] = this.emptyCell ? Boolean.TRUE : Boolean.FALSE;
        _values[15] = this.emptyCell_set ? Boolean.TRUE : Boolean.FALSE;
        _values[16] = this.extraFooterHtml;
        _values[17] = this.extraHeaderHtml;
        _values[18] = this.extraTableFooterHtml;
        _values[19] = this.footerText;
        _values[20] = this.headerText;
        _values[21] = this.headers;
        _values[22] = this.height;
        _values[23] = this.noWrap ? Boolean.TRUE : Boolean.FALSE;
        _values[24] = this.noWrap_set ? Boolean.TRUE : Boolean.FALSE;
        _values[25] = this.onClick;
        _values[26] = this.onDblClick;
        _values[27] = this.onKeyDown;
        _values[28] = this.onKeyPress;
        _values[29] = this.onKeyUp;
        _values[30] = this.onMouseDown;
        _values[31] = this.onMouseMove;
        _values[32] = this.onMouseOut;
        _values[33] = this.onMouseOver;
        _values[34] = this.onMouseUp;
        _values[35] = this.rowHeader ? Boolean.TRUE : Boolean.FALSE;
        _values[36] = this.rowHeader_set ? Boolean.TRUE : Boolean.FALSE;
        _values[37] = new Integer(this.rowSpan);
        _values[38] = this.rowSpan_set ? Boolean.TRUE : Boolean.FALSE;
        _values[39] = this.scope;
        _values[40] = this.selectId;
        _values[41] = this.severity;
        _values[42] = this.sort;
        _values[43] = this.sortIcon;
        _values[44] = this.sortImageURL;
        _values[45] = this.spacerColumn ? Boolean.TRUE : Boolean.FALSE;
        _values[46] = this.spacerColumn_set ? Boolean.TRUE : Boolean.FALSE;
        _values[47] = this.style;
        _values[48] = this.styleClass;
        _values[49] = this.tableFooterText;
        _values[50] = this.toolTip;
        _values[51] = this.valign;
        _values[52] = this.visible ? Boolean.TRUE : Boolean.FALSE;
        _values[53] = this.visible_set ? Boolean.TRUE : Boolean.FALSE;
        _values[54] = this.width;
        return _values;
    }

}
