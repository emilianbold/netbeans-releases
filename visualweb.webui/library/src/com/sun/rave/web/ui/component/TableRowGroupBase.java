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
 * Use the <code>ui:tableRowGroup</code>
 * tag to define the rows of a table, inside a <a href="table.html"><code>ui:table</code></a>
 * tag.&nbsp; A table must include at least one tableRowGroup component to
 * contain the table row content. <br>
 * <p style="color: rgb(0, 0, 0);">The <code>ui:table</code>
 * tag is
 * used to define the structure and actions of the table, and is a
 * container for <code>ui:tableRowGroup</code>
 * tags.&nbsp; The <code>ui:tableRowGroup</code> tag is a container for <code></code><a
 * href="tableColumn.html"><code>ui:tableColumn</code></a><code></code>
 * tags, which are used&nbsp;<code></code>to define the
 * columns of the table.&nbsp; The <a href="table.html">documentation
 * for the <code>ui:table</code> tag</a> contains detailed information
 * about the table component.&nbsp; This page provides details about how
 * to create table rows only.<br>
 * </p>
 * <p style="color: rgb(0, 0, 0);">When you use one <code>ui:tableRowGroup</code>
 * tag in the <code>ui:table</code> tag, you create a basic table.&nbsp;
 * Examples of basic tables are shown in the <a table.html=""><code>ui:table</code>
 * tag documentation</a>. When
 * you use multiple <code>ui:tableRowGroup</code> tags, you create a
 * group table, which is discussed in detail in this document.<br>
 * </p>
 * <p style="color: rgb(0, 0, 0);">
 * </p>
 * <h3 style="color: rgb(0, 0, 0);">HTML Elements and Layout</h3>
 * <span style="color: rgb(0, 0, 0);">The tableRowGroup component is used
 * to define attributes for XHTML </span><code
 * style="color: rgb(0, 0, 0);">&lt;tr&gt;</code>
 * <span style="color: rgb(0, 0, 0);">&nbsp;
 * elements, which
 * are used
 * to display&nbsp;rows of data. You can specify multiple <code>ui:tableRowGroup</code>
 * tags to create groups of rows. Each group is visually separate from the
 * other groups, but all rows of the table can be sorted and filtered at
 * once, within their respective groups. <br>
 * <br>
 * UI guidelines recommend
 * that
 * column headers and table column footers are
 * only rendered
 * once for each table. Column headers typically appear at the top of the
 * table,
 * below the Action Bar and above all row groups. Table column footers
 * appear only at the
 * bottom of the table, below all row groups. The column headers and
 * table column footers are defined in the <code>headerText</code>
 * and <code>tableFooterText</code> attributes of the <code>ui:tableColumn</code>
 * tags. To ensure that these headers and footers are rendered only once,
 * you should define the&nbsp;
 * <code>headerText</code>
 * and <code>tableFooterText</code> attributes only in the <code>ui:tableColumn</code>
 * tags inside the
 * first <code>ui:tableRowGroup</code> tag in the
 * table.&nbsp; See the </span><a href="tableColumn.html"
 * style="color: rgb(0, 0, 0);"><code>ui:tableColumn</code>
 * documentation</a><span style="color: rgb(0, 0, 0);"> for more
 * information. </span><br style="color: rgb(0, 0, 0);">
 * <span style="color: rgb(0, 0, 0);"><br>
 * The following <a href="tableRowGroup.html#diagram">diagram</a>
 * shows the placement of the areas of a table, and highlights the areas
 * that are defined with <code>ui:tableRowGroup</code> tags. This diagram
 * depicts two row groups.</span><br>
 * <br>
 * <a name="diagram"></a><br>
 * <table style="text-align: left; width: 100%;" border="1" cellpadding="2"
 * cellspacing="2">
 * <tbody>
 * <tr style="color: rgb(204, 204, 204);">
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
 * <tr>
 * <td
 * style="vertical-align: top; background-color: rgb(210, 224, 235);"
 * rowspan="1" colspan="2">Group
 * Header Bar (specified with <code>headerText</code>
 * attribute and <code>header</code> facet in first <code>ui:tableRowGroup</code>
 * tag) </td>
 * </tr>
 * <tr>
 * <td
 * style="vertical-align: top; background-color: rgb(210, 224, 235);">Table
 * data
 * specified in <code>ui:tableRowGroup</code><br>
 * <code> <br>
 * <br>
 * <br>
 * </code></td>
 * <td
 * style="vertical-align: top; background-color: rgb(210, 224, 235);">Table
 * data
 * specified in <code>ui:tableRowGroup</code><br>
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
 * <tr>
 * <td
 * style="vertical-align: top; background-color: rgb(210, 224, 235);"
 * rowspan="1" colspan="2">Group
 * Footer Bar&nbsp; <br>
 * </td>
 * </tr>
 * <tr>
 * <td
 * style="vertical-align: top; background-color: rgb(163, 184, 203);"
 * rowspan="1" colspan="2">Group Header Bar<br>
 * </td>
 * </tr>
 * <tr>
 * <td
 * style="vertical-align: top; background-color: rgb(163, 184, 203);">Table
 * data specified in <code>ui:tableRowGroup</code><br>
 * <br>
 * <br>
 * <br>
 * <br>
 * </td>
 * <td
 * style="vertical-align: top; background-color: rgb(163, 184, 203);">Table
 * data specified in <code>ui:tableRowGroup</code><br>
 * </td>
 * </tr>
 * <tr>
 * <td
 * style="vertical-align: top; background-color: rgb(232, 232, 232); color: rgb(153, 153, 153);">Column
 * Footer<br>
 * </td>
 * <td
 * style="vertical-align: top; background-color: rgb(232, 232, 232); color: rgb(153, 153, 153);">Column
 * Footer</td>
 * </tr>
 * <tr>
 * <td
 * style="vertical-align: top; background-color: rgb(163, 184, 203);"
 * rowspan="1" colspan="2">Group Footer Bar<br>
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
 * <tr>
 * <td
 * style="vertical-align: top; background-color: rgb(153, 153, 153);"
 * rowspan="1" colspan="1"><span style="color: rgb(0, 0, 0);"><span
 * style="color: rgb(204, 204, 204);">Footer</span><code></code></span><code>
 * </code></td>
 * </tr>
 * </tbody>
 * </table>
 * <span style="text-decoration: line-through;"></span><br>
 * <h4 style="color: rgb(0, 0, 0);">Group Header Bar</h4>
 * <span style="color: rgb(0, 0, 0);">The Group Header Bar displays
 * a header by default at the top of each group of rows.&nbsp; The text of
 * the group header
 * is specified with the <code>headerText</code> attribute. You can
 * specify a separate component to provide header content by using the <code>header</code>
 * facet, which overrides the <code>headerText</code> attribute.&nbsp;
 * You can
 * use the following <code>ui:tableRowGroup</code> attributes to change
 * the appearance of the group header:<br>
 * </span>
 * <ul style="color: rgb(0, 0, 0);">
 * <li>
 * <p><code>aboveColumnHeader</code>
 * set to true makes the group header display above the column header.</p>
 * </li>
 * <li>
 * <p><code>selectMultipleToggleButton</code>
 * set to true adds a checkbox that allows users to
 * select and deselect all rows in the group</p>
 * </li>
 * <li>
 * <p><code>collapsed</code>
 * set to true causes the group to be rendered with the rows hidden, and
 * only the group header is visible.&nbsp; </p>
 * <p> </p>
 * </li>
 * <li>
 * <p><code>groupToggleButton</code>
 * set to true adds a button image that allows users to expand and
 * collapse
 * the group of rows. When the group collapses, all the rows are hidden
 * and only the header is visible. </p>
 * <p> </p>
 * </li>
 * <li>
 * <p><code>extraHeaderHtml</code>
 * can be used to append HTML code to the <code>&lt;tr&gt;</code> element
 * that is rendered for the group header</p>
 * </li>
 * </ul>
 * <span style="color: rgb(0, 0, 0);">Note: Detailed descriptions of
 * the
 * attributes are in the attributes table at the end of this document.<br>
 * </span>
 * <h4 style="color: rgb(0, 0, 0);">Group Footer Bar</h4>
 * <p style="color: rgb(0, 0, 0);">The Group Footer Bar
 * displays an optional footer below each group of rows. The text of the
 * group footer is specified with the <code>footerText</code> attribute.
 * You can specify a separate component to provide the footer content by
 * using the <code>footer</code> facet, which overrides the <code>footerText</code>
 * attribute.&nbsp; You can use the following
 * <code>ui:tableRowGroup</code>
 * attributes to change
 * the appearance of the group footer:<br>
 * <code><br>
 * </code></p>
 * <ul style="color: rgb(0, 0, 0);">
 * <li><code>aboveColumnFooter</code>
 * set to true makes the group footer display above the column footer.</li>
 * <li>
 * <p>&nbsp;<code>extraFooterHtml</code> can be used to
 * append HTML code to the <code>&lt;tr&gt;</code> element that is
 * rendered for the group footer</p>
 * </li>
 * </ul>
 * <h4 style="color: rgb(0, 0, 0);">Table Data</h4>
 * <p><span style="color: rgb(0, 0, 0);">The table data is specified with
 * the <code>sourceData</code> and <code>sourceVar</code> attributes.
 * The <code>sourceData</code> attribute specifies the source of the data
 * that populates the table. The <code>sourceVar</code> attribute
 * specifies the name of the request-scope variable to use for exposing
 * the model data when iterating over table rows. Each table in a JSP page
 * must use a unique <code>sourceVar</code> value. See the <a
 * href="#Lifecycle">Life Cycle section </a>for more information about
 * these attributes.&nbsp; &nbsp; </span><span
 * style="color: rgb(102, 102, 204);"><span
 * style="color: rgb(255, 153, 0);">
 * </span></span><span style="color: rgb(102, 102, 204);"> </span></p>
 * <h3>Facets</h3>
 * <span style="color: rgb(0, 0, 0);">The </span><code
 * style="color: rgb(0, 0, 0);">ui:tableRowGroup</code><span
 * style="color: rgb(0, 0, 0);"> tag supports the following facets,
 * which
 * allow you to customize the
 * layout&nbsp; of the component.</span><br style="color: rgb(0, 0, 0);">
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
 * <td>Footer that applies to the group of rows defined by the <code>ui:tableRowGroup</code>
 * tag.&nbsp; This facet can be used to&nbsp; replace the default footer.<br>
 * </td>
 * </tr>
 * <tr>
 * <td><code>header</code></td>
 * <td>Header that applies to the group of rows defined by the <code>ui:tableRowGroup</code>
 * tag. This facet can be used to replace the default header. </td>
 * </tr>
 * </tbody>
 * </table>
 * <br>
 * <h3 style="color: rgb(0, 0, 0);">Client Side JavaScript
 * Functions</h3>
 * <span style="color: rgb(0, 0, 0);">See the </span><a
 * href="table.html#JavaScript" style="color: rgb(0, 0, 0);"><code>ui:table</code>
 * tag's
 * JavaScript documentation</a><span style="color: rgb(0, 0, 0);">.&nbsp;
 * The same functions are used for the
 * <code>ui:tableRowGroup</code> tag.</span><br>
 * <ul>
 * </ul>
 * <h3>Notes for the <code>ui:tableRowGroup</code> tag</h3>
 * All examples assume that<span style="color: rgb(102, 102, 204);"></span>
 * the <code>ui:table</code> tag is contained within a HTML <code>&lt;form&gt;</code>
 * element so actions can<span style="text-decoration: line-through;"></span><span
 * style="color: rgb(102, 102, 204);"></span> submit form data.
 * For basic table and sort
 * examples,<span style="text-decoration: line-through;"></span>
 * see the <a href="table.html"><code>ui:table</code>
 * tag documentation</a>
 * and <a href="tableColumn.html"><code>ui:tableColumn</code>
 * documentation</a>. <br>
 * <h4><span style="font-weight: bold;"><a name="Lifecycle"></a>Life Cycle</span></h4>
 * <span style="text-decoration: line-through;"></span>The <code>sourceData</code>
 * property of tableRowGroup is invoked at
 * least twice during
 * the JavaServer Faces life cycle. The <code>sourceData</code> is
 * invoked once during
 * one of the Apply Request Values, Process Validations, or Update Model
 * Values phases, and once during the Render Response phase. In order to
 * process the
 * previously displayed children during the Apply Request Values, Process
 * Validations, or Update Model Values phases,
 * the table
 * must use the same DataProvider that was used to render the previous
 * page. For
 * example, suppose that sorting, filtering, and pagination were applied
 * on the previous page, and
 * rows 10-20 of 1000 were currently displayed. You want to update only
 * the currently
 * displayed components, rows 10-20. To do this, you must not update the
 * DataProvider until the Invoke Application phase or Render Response
 * phases, when it is safe to render new
 * data.<br>
 * <p style="color: rgb(0, 0, 0);">Note that if the underlying
 * DataProvider has changed in any way, processing of the previously
 * displayed children might not be possible during the Apply Request
 * Values, Process
 * Validations, or Update Model Values phases. In addition, if the
 * DataProvider is null or empty, no children will be processed and
 * their <code>processDecodes()</code>, <code>processValidators()</code>,
 * and <code>processUpdates()</code> methods will not be invoked. If a
 * component has not been decoded (in the Apply Request Values phase),
 * action events might not be received and component properties such as
 * hyperlink query parameters cannot be retrieved.&nbsp; Also, if a
 * component has not been updated (in the Update Model Values phase),
 * submitted values cannot be retrieved for checkboxes, radio buttons,
 * etc. </p>
 * <span style="color: rgb(0, 0, 0);">When obtaining data is
 * expensive, consider caching the DataProvider. A cached DataProvider
 * persists across requests if the backing bean uses session scope, or if
 * the application uses server-side state saving and the <code>TableRowGroup.setSourceData</code>
 * method is set. Note that a phase listener may be used to initialize the
 * DataProvider during the Invoke Application Phase. However, when the
 * underlying DataProvider has changed in any way, UI guidelines recommend
 * that pagination is reset to the first page. You can use the
 * tableRowGroup component's <code>setFirst()</code> method to set the
 * first row to be displayed.<br>
 * <br>
 * </span>
 * <h3 style="color: rgb(0, 0, 0);"><a name="Examples"></a>Examples</h3>
 * <span style="color: rgb(0, 0, 0);">The following examples use a
 * backing bean called </span><a href="table.html#Example:_TableBean_"
 * style="color: rgb(0, 0, 0);">TableBean</a><span
 * style="color: rgb(0, 0, 0);">,
 * which is shown in the <code>ui:table</code> tag documentation. </span><a
 * href="tableRowGroup.html#UtilityClasses" style="color: rgb(0, 0, 0);">Utility
 * classes</a><span style="color: rgb(0, 0, 0);"> used in the examples are
 * included
 * in this <code>ui:tableRowGroup</code> page, after the examples.&nbsp;
 * Additional examples are shown in the </span><code
 * style="color: rgb(0, 0, 0);"><a href="table.html#Examples">ui:table</a></code><span
 * style="color: rgb(0, 0, 0);">
 * and </span><code style="color: rgb(0, 0, 0);"><a
 * href="tableColumn.html">ui:tableColumn</a></code><span
 * style="color: rgb(0, 0, 0);">
 * documents.</span><br style="color: rgb(0, 0, 0);">
 * <br style="color: rgb(0, 0, 0);">
 * <span style="font-weight: bold; color: rgb(0, 0, 0);">Examples in
 * this file:<br>
 * <br>
 * </span>
 * <div style="margin-left: 40px; color: rgb(0, 0, 0);"><a
 * href="#GroupTable">Example 1: Group Table</a><br>
 * <p><a href="#DynamicTable">Example 2: Dynamic Table</a><br>
 * </p>
 * <p><a href="#DynamicGroupTable">Example 3: Dynamic Group Table</a><br>
 * </p>
 * </div>
 * <span style="font-weight: bold; color: rgb(0, 0, 0);">
 * </span>
 * <p style="font-weight: bold; color: rgb(0, 0, 0);">Supporting
 * files:</p>
 * <div style="margin-left: 40px; color: rgb(0, 0, 0);">
 * <p><a href="table.html#Example:_TableBean_">TableBean
 * backing bean in <code>ui:table</code> documentation<br>
 * </a></p>
 * <p><a href="tableRowGroup.html#UtilityClasses">Utility
 * classes used in the examples</a></p>
 * </div>
 * <h4><a name="GroupTable"></a>Example 1: Group Table<br>
 * </h4>
 * <span style="text-decoration: line-through;"></span><span
 * style="color: rgb(0, 0, 0);">This example shows how to
 * create a group table. A group table contains rows of data that are
 * arranged in discrete sections or groups
 * within the table.&nbsp; In a
 * basic table, each column typically has a header and perhaps a footer.
 * However, in a group table, each group of rows can have its own header
 * and footer. In addition, a group table can display table column footers
 * and
 * an overall table footer below all
 * data groups.&nbsp; The table column footers are specified in <code>ui:tableColumn</code>
 * tags and the overall footer is specified in the <code>ui:table</code>
 * tag. See the <a href="#diagram">diagram </a>of the
 * table areas. </span><br style="color: rgb(0, 0, 0);">
 * <br style="color: rgb(0, 0, 0);">
 * <span style="color: rgb(0, 0, 0);">Additional
 * attributes should be
 * used in the <code>ui:tableColumn</code> tags within each <code>ui:tableRowGroup</code>
 * tag to specify
 * functionality. For
 * example, the <code>selectId</code> and <code>sort</code>&nbsp;
 * attributes allow column
 * headers to sort on all row groups at once.&nbsp; <br>
 * <br>
 * The example uses the backing bean called TableBean for the table data.
 * See the
 * </span><a href="table.html#Example:_TableBean_"
 * style="color: rgb(0, 0, 0);">TableBean
 * backing bean in <code>ui:table</code> documentation</a><span
 * style="color: rgb(0, 0, 0);">.</span><br style="color: rgb(0, 0, 0);">
 * <br style="color: rgb(0, 0, 0);">
 * <span style="color: rgb(0, 0, 0);">Note: UI guidelines recommend
 * that items should not remain selected when they cannot be seen by the
 * user. Using the
 * <code>com.sun.rave.web.ui.event.TableSelectPhaseListener</code> object
 * ensures that rows that are hidden from view are
 * deselected because the phase listener clears the table state after the
 * rendering phase. Although
 * pagination is not used for a group table, the <code>TableSelectPhaseListener</code>
 * object is used in this example in the Select util, which is shown in </span><a
 * href="table.html#Select.java" style="color: rgb(0, 0, 0);">Select.java
 * in the <code>ui:table</code> documentation</a><span
 * style="color: rgb(0, 0, 0);">.&nbsp; Also refer to the JavaDoc
 * for <code>TableSelectPhaseListener</code> for more information.&nbsp; </span><br
 * style="color: rgb(0, 0, 0);">
 * <br>
 * <code>&lt;!-- Group Table --&gt;<br>
 * &lt;ui:table id="table1"<br>
 * &nbsp;&nbsp;&nbsp; clearSortButton="true"<br>
 * &nbsp;&nbsp;&nbsp; deselectMultipleButton="true"<br>
 * &nbsp;&nbsp;&nbsp; selectMultipleButton="true"<br>
 * &nbsp;&nbsp;&nbsp; sortPanelToggleButton="true"<br>
 * &nbsp;&nbsp;&nbsp; footerText="Table Footer"&gt;<br>
 * <br>
 * &nbsp; &lt;!-- Title --&gt;<br>
 * &nbsp; &lt;f:facet name="title"&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;ui:staticText text="Group Table"/&gt;<br>
 * &nbsp; &lt;/f:facet&gt;<br>
 * <br>
 * &nbsp; &lt;ui:tableRowGroup id="rowGroup1"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * binding="#{TableBean.groupB.tableRowGroup}"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; footerText="Group Footer"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; headerText="Group Header"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * selected="#{TableBean.groupB.select.selectedState}"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; selectMultipleToggleButton="true"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; sourceData="#{TableBean.groupB.names}"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; sourceVar="name"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; groupToggleButton="true"&gt;<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; &lt;ui:tableColumn id="col0"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * extraHeaderHtml="nowrap='nowrap'"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * extraFooterHtml="nowrap='nowrap'"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * extraTableFooterHtml="nowrap='nowrap'"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; footerText="ColFtr"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; selectId="select"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * sort="#{TableBean.groupB.select.selectedState}"&gt;<br>
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
 * extraHeaderHtml="nowrap='nowrap'"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; alignKey="last"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; footerText="Column Footer"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; headerText="Last Name"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; rowHeader="true"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; sort="last"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;ui:staticText
 * text="#{name.value.last}"/&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;/ui:tableColumn&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;ui:tableColumn id="col2"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; alignKey="first"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; footerText="Column Footer"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; headerText="First Name"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; sort="first"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;ui:staticText
 * text="#{name.value.first}"/&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;/ui:tableColumn&gt;<br>
 * &nbsp; &lt;/ui:tableRowGroup&gt;<br>
 * <br>
 * &nbsp; &lt;ui:tableRowGroup id="rowGroup2"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * binding="#{TableBean.groupC.tableRowGroup}"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; collapsed="true"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * selected="#{TableBean.groupC.select.selectedState}"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; selectMultipleToggleButton="true"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; sourceData="#{TableBean.groupC.names}"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; sourceVar="name"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; groupToggleButton="true"&gt;<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; &lt;!-- Row group header --&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;f:facet name="header"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;ui:panelGroup id="groupHeader"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;ui:markup tag="span"
 * extraAttributes="class='TblGrpLft'"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &lt;ui:staticText styleClass="TblGrpTxt" text="Group Header"/&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;/ui:markup&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;ui:markup tag="span"
 * extraAttributes="class='TblGrpRt'"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &lt;ui:staticText styleClass="TblGrpMsgTxt" text="Right-Aligned
 * Text"/&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;/ui:markup&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;/ui:panelGroup&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;/f:facet&gt;<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; &lt;!-- Row group footer --&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;f:facet name="footer"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;ui:staticText
 * styleClass="TblGrpFtrRowTxt" text="Group Footer"/&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;/f:facet&gt;<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; &lt;ui:tableColumn id="col0"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * extraHeaderHtml="nowrap='nowrap'"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * extraFooterHtml="nowrap='nowrap'"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * extraTableFooterHtml="nowrap='nowrap'"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; footerText="ColFtr"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; selectId="select"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * sort="#{TableBean.groupC.select.selectedState}"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * tableFooterText="TblColFtr"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;ui:checkbox id="select"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * onClick="setTimeout('initAllRows()', 0)"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * selected="#{TableBean.groupC.select.selected}"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * selectedValue="#{TableBean.groupC.select.selectedValue}"/&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;/ui:tableColumn&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;ui:tableColumn id="col1"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; alignKey="last"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; footerText="Column Footer"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; rowHeader="true"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; sort="last"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; tableFooterText="Table
 * Column Footer"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;ui:staticText
 * text="#{name.value.last}"/&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;/ui:tableColumn&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;ui:tableColumn id="col2"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; alignKey="first"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; footerText="Column Footer"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; sort="first"<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; tableFooterText="Table
 * Column Footer"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;ui:staticText
 * text="#{name.value.first}"/&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;/ui:tableColumn&gt;<br>
 * &nbsp; &lt;/ui:tableRowGroup&gt;<br>
 * &lt;/ui:table&gt;</code><br>
 * <br>
 * <span style="font-weight: bold;">select.js</span><br>
 * <br>
 * This example shows the contents of the <code>select.js</code><span
 * style="font-weight: bold;"> </span>file used in the example above.<br>
 * <code><br>
 * // Use this function to initialize all rows displayed in the table when
 * the<br>
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
 * <h4><a name="DynamicTable"></a>Example 2: Dynamic Table<br>
 * </h4>
 * <span style="color: rgb(0, 0, 0);">This example shows how to use
 * the <code>ui:table</code> tag to create a binding to a backing
 * bean to
 * dynamically create a table layout. The dynamic table is created as
 * needed and can be changed each time the page is rendered.&nbsp;&nbsp; </span><br
 * style="color: rgb(0, 0, 0);">
 * <br style="color: rgb(0, 0, 0);">
 * <code style="color: rgb(0, 0, 0);">&lt;ui:table id="table1"
 * binding="#{DynamicTableBean.table}"/&gt;<br>
 * <br>
 * <br>
 * </code><span style="color: rgb(0, 0, 0);">The backing bean,
 * DynamicTableBean, is
 * shown in the following </span><a href="#DynamicTableBean"
 * style="color: rgb(0, 0, 0);"><code>DynamicTableBean.java</code></a><span
 * style="color: rgb(0, 0, 0);"> class. This bean is used only to
 * create the table layout, and shows how to use the tableRowGroup
 * component directly
 * through Java code instead of through the JSP tag&nbsp; </span><code
 * style="color: rgb(0, 0, 0);">ui:tableRowGroup.</code><span
 * style="color: rgb(0, 0, 0);"> The <a href="#Dynamic.java">Dynamic.java</a>
 * utility class provides the functionality for adding properties to the
 * table.&nbsp; The table
 * also uses methods that are
 * defined in the </span><a href="table.html#Example:_TableBean_"
 * style="color: rgb(0, 0, 0);">TableBean
 * shown in the <code>ui:table</code> documentation</a><span
 * style="color: rgb(0, 0, 0);">.</span><br>
 * <h4><code style="color: rgb(102, 102, 204);"><a name="DynamicTableBean"></a><span
 * style="color: rgb(0, 0, 0);"></span></code><span
 * style="color: rgb(0, 0, 0);">DynamicTableBean.java Backing Bean</span><br>
 * </h4>
 * <h4><code style="font-weight: normal;">package table;<br>
 * <br>
 * import com.sun.rave.web.ui.component.Table;<br>
 * import com.sun.rave.web.ui.component.TableRowGroup;<br>
 * <br>
 * import table.util.Dynamic;<br>
 * <br>
 * // Backing bean for dynamic table examples.<br>
 * //<br>
 * // Note: To simplify the example, this bean is used only to create the
 * table <br>
 * // layout. The resulting table will use methods already defined in
 * TableBean.<br>
 * public class DynamicTableBean {<br>
 * &nbsp;&nbsp;&nbsp; private Dynamic dynamic = null; // Dynamic util.<br>
 * &nbsp;&nbsp;&nbsp; private Table table = null; // Table component.<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Default constructor.<br>
 * &nbsp;&nbsp;&nbsp; public DynamicTableBean() {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; dynamic = new Dynamic();<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Get Table component.<br>
 * &nbsp;&nbsp;&nbsp; public Table getTable() {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; if (table == null) {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; //
 * Get table row group.<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * TableRowGroup rowGroup1 = dynamic.getTableRowGroup("rowGroup1",<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * "#{TableBean.groupB.names}",<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * "#{TableBean.groupB.select.selectedState}", null);<br>
 * <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; //
 * Set table row group properties.<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * dynamic.setTableRowGroupChildren(rowGroup1,<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * "#{TableBean.groupB.select.selectedState}",<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * "#{TableBean.groupB.select.selected}",<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * "#{TableBean.groupB.select.selectedValue}",<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * "#{TableBean.groupB.actions.action}", true);<br>
 * <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; //
 * Get table.<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * table = dynamic.getTable("table1", "Dynamic Table");<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * table.getChildren().add(rowGroup1);<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; }<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; return table;<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Set Table component.<br>
 * &nbsp;&nbsp;&nbsp; //<br>
 * &nbsp;&nbsp;&nbsp; // @param table The Table component.<br>
 * &nbsp;&nbsp;&nbsp; public void setTable(Table table) {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; this.table = table;<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * }</code><br>
 * </h4>
 * <code>
 * </code>
 * <h4><a name="DynamicGroupTable"></a>Example 3: Dynamic Group Table<br>
 * </h4>
 * This example shows how to<span style="color: rgb(0, 0, 0);"> use
 * the <code>ui:table</code> tag to create a binding to a backing
 * bean to
 * dynamically create a group table layout. </span><span
 * style="text-decoration: line-through; color: rgb(0, 0, 0);"></span><span
 * style="color: rgb(0, 0, 0);">The dynamic group table is created
 * as
 * needed and can be changed each time the page is rendered.</span><br
 * style="color: rgb(0, 0, 0);">
 * <br style="color: rgb(0, 0, 0);">
 * <code style="color: rgb(0, 0, 0);">&lt;ui:table id="table1"
 * binding="#{DynamicGroupTableBean.table}"/&gt;<br>
 * <br>
 * </code><span style="color: rgb(0, 0, 0);">The backing bean,
 * DynamicGroupTableBean, is
 * shown in the following </span><a href="#DynamicGroupTableBean"
 * style="color: rgb(0, 0, 0);"><code>DynamicGroupTableBean.java</code></a><span
 * style="color: rgb(0, 0, 0);"> class. This bean is used only to
 * create the table layout, and shows how to use the tableRowGroup
 * component directly
 * through Java code instead of through the JSP tag&nbsp; </span><code
 * style="color: rgb(0, 0, 0);">ui:tableRowGroup.</code><span
 * style="color: rgb(0, 0, 0);"> The <code><a
 * href="tableRowGroup.html#Dynamic.java">Dynamic.java</a></code>
 * utility
 * class provides the functionality for adding
 * properties to the table.&nbsp; The table also
 * uses methods that are
 * defined in the </span><a href="table.html#Example:_TableBean_"
 * style="color: rgb(0, 0, 0);">TableBean
 * shown in the <code>ui:table</code> documentation</a><span
 * style="color: rgb(0, 0, 0);">.</span><br>
 * <h4><code style="color: rgb(102, 102, 204);"><a
 * name="DynamicGroupTableBean"></a><span style="color: rgb(0, 0, 0);"></span></code><span
 * style="color: rgb(0, 0, 0);">DynamicGroupTableBean.java Backing Bean</span></h4>
 * <code>package table;<br>
 * <br>
 * import table.util.Dynamic;<br>
 * <br>
 * import com.sun.rave.web.ui.component.StaticText;<br>
 * import com.sun.rave.web.ui.component.Table;<br>
 * import com.sun.rave.web.ui.component.TableRowGroup;<br>
 * <br>
 * // Backing bean for dynamic group table examples.<br>
 * //<br>
 * // Note: To simplify the example, this bean is used only to create the
 * table <br>
 * // layout. The resulting table will use methods already defined in
 * TableBean.<br>
 * public class DynamicGroupTableBean {<br>
 * &nbsp;&nbsp;&nbsp; private Dynamic dynamic = null; // Dynamic util.<br>
 * &nbsp;&nbsp;&nbsp; private Table table = null; // Table component.<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Default constructor.<br>
 * &nbsp;&nbsp;&nbsp; public DynamicGroupTableBean() {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; dynamic = new Dynamic();<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Get Table component.<br>
 * &nbsp;&nbsp;&nbsp; public Table getTable() {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; if (table == null) {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; //
 * Get table row group.<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * TableRowGroup rowGroup1 = dynamic.getTableRowGroup("rowGroup1",<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * "#{TableBean.groupB.names}",<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * "#{TableBean.groupB.select.selectedState}",<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * "Group Header");<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * TableRowGroup rowGroup2 = dynamic.getTableRowGroup("rowGroup2",<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * "#{TableBean.groupC.names}",<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * "#{TableBean.groupC.select.selectedState}",<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * "Group Header");<br>
 * <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; //
 * Set table row group properties.<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * dynamic.setTableRowGroupChildren(rowGroup1, <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * "#{TableBean.groupB.select.selectedState}", <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * "#{TableBean.groupB.select.selected}",<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * "#{TableBean.groupB.select.selectedValue}", null, true);<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * dynamic.setTableRowGroupChildren(rowGroup2, <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * "#{TableBean.groupC.select.selectedState}", <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * "#{TableBean.groupC.select.selected}",<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * "#{TableBean.groupC.select.selectedValue}", null, false);<br>
 * <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; //
 * Set select and row group toggle buttons.<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * rowGroup1.setSelectMultipleToggleButton(true); <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * rowGroup2.setSelectMultipleToggleButton(true); <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * rowGroup1.setGroupToggleButton(true);<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * rowGroup2.setGroupToggleButton(true);<br>
 * <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; //
 * Get table.<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * table = dynamic.getTable("table1", null);<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * table.getChildren().add(rowGroup1);<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * table.getChildren().add(rowGroup2);<br>
 * <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; //
 * Add title facet.<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * StaticText title = new StaticText();<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * title.setText("Dynamic Group Table");<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * table.getFacets().put(Table.TITLE_FACET, title);<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; }<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; return table;<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Set Table component. <br>
 * &nbsp;&nbsp;&nbsp; //<br>
 * &nbsp;&nbsp;&nbsp; // @param table The Table component.<br>
 * &nbsp;&nbsp;&nbsp; public void setTable(Table table) {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; this.table = table;<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * }</code><br>
 * <h3 style="color: rgb(0, 0, 0);">faces_config.xml Entry for
 * Managed Beans</h3>
 * The examples are based on managed beans, such as the example
 * below, added to the
 * <code>faces_config.xml</code> file.<br>
 * <span style="color: rgb(255, 153, 0);"><br>
 * </span><code>&lt;!DOCTYPE faces-config PUBLIC <br>
 * &nbsp;&nbsp;&nbsp; '-//Sun Microsystems, Inc.//DTD JavaServer Faces
 * Config 1.0//EN' <br>
 * &nbsp;&nbsp;&nbsp; 'http://java.sun.com/dtd/web-facesconfig_1_1.dtd'&gt;<br>
 * <br>
 * &lt;faces-config&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;managed-bean&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;description&gt;Backing
 * bean for the group table example&lt;/description&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &lt;managed-bean-name&gt;TableBean&lt;/managed-bean-name&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &lt;managed-bean-class&gt;table.TableBean&lt;/managed-bean-class&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &lt;managed-bean-scope&gt;session&lt;/managed-bean-scope&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;/managed-bean&gt;<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; &lt;managed-bean&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;description&gt;Backing
 * bean for the dynamic table example&lt;/description&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &lt;managed-bean-name&gt;DynamicTableBean&lt;/managed-bean-name&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &lt;managed-bean-class&gt;table.DynamicTableBean&lt;/managed-bean-class&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &lt;managed-bean-scope&gt;session&lt;/managed-bean-scope&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;/managed-bean&gt;<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; &lt;managed-bean&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;description&gt;Backing
 * bean for the dynamic group table example&lt;/description&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &lt;managed-bean-name&gt;DynamicGroupTableBean&lt;/managed-bean-name&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &lt;managed-bean-class&gt;table.DynamicGroupTableBean&lt;/managed-bean-class&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &lt;managed-bean-scope&gt;session&lt;/managed-bean-scope&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;/managed-bean&gt;<br>
 * &lt;/faces-config&gt;</code><br>
 * <h3><a name="UtilityClasses"></a>Utility Classes used in the examples</h3>
 * The&nbsp; following utility class is used in the group table
 * examples shown in this page.<br>
 * <p><a href="table.html#Group.java"><code></code></a></p>
 * <a href="tableRowGroup.html#Dynamic.java">Dynamic.java</a><br>
 * <br>
 * The following utility classes are shown in the <code>ui:table</code>
 * documentation, and used in the examples for the table
 * tags<code> ui:table</code>, <code>ui:tableRowGroup</code>, and <code>ui:tableColumn.<br>
 * </code><br>
 * <code></code><a href="table.html#Group.java">Group.java
 * </a><br>
 * <a href="table.html#Name.java">Name.java
 * </a><a href="table.html#Group.java"><code></code></a><br>
 * <a href="table.html#Select.java">Select.java
 * </a><br>
 * <h4><a name="Dynamic.java"></a><span style="color: rgb(0, 0, 0);">Dynamic.java
 * Utility Class</span><br>
 * </h4>
 * <code>package table.util;<br>
 * <br>
 * import com.sun.rave.web.ui.component.Checkbox;<br>
 * import com.sun.rave.web.ui.component.Hyperlink;<br>
 * import com.sun.rave.web.ui.component.StaticText;<br>
 * import com.sun.rave.web.ui.component.Table;<br>
 * import com.sun.rave.web.ui.component.TableColumn;<br>
 * import com.sun.rave.web.ui.component.TableRowGroup;<br>
 * <br>
 * import javax.faces.context.FacesContext;<br>
 * import javax.faces.component.UIComponent;<br>
 * import javax.faces.component.UIParameter;<br>
 * import javax.faces.el.ValueBinding;<br>
 * <br>
 * // This class provides functionality for dynamic tables.<br>
 * public class Dynamic {<br>
 * &nbsp;&nbsp;&nbsp; public static final String CHECKBOX_ID = "select";<br>
 * &nbsp;&nbsp;&nbsp; public static final String HYPERLINK_ID = "link";<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Default constructor.<br>
 * &nbsp;&nbsp;&nbsp; public Dynamic() {<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Note: When using tags in a JSP page,
 * UIComponentTag automatically creates<br>
 * &nbsp;&nbsp;&nbsp; // a unique id for the component. However, when
 * dynamically creating <br>
 * &nbsp;&nbsp;&nbsp; // components, via a backing bean, the id has not
 * been set. In this <br>
 * &nbsp;&nbsp;&nbsp; // scenario, allowing JSF to create unique Ids may
 * cause problems with<br>
 * &nbsp;&nbsp;&nbsp; // Javascript and components may not be able to
 * maintain state properly. <br>
 * &nbsp;&nbsp;&nbsp; // For example, if a component was assigned "_id6"
 * as an id, that means <br>
 * &nbsp;&nbsp;&nbsp; // there were 5 other components that also have
 * auto-generated ids. Let us <br>
 * &nbsp;&nbsp;&nbsp; // assume one of those components was a complex
 * component that, as part of <br>
 * &nbsp;&nbsp;&nbsp; // its processing, adds an additional non-id'd child
 * before redisplaying the<br>
 * &nbsp;&nbsp;&nbsp; // view. Now, the id of this component will be
 * "_id7" instead of "_id6". <br>
 * &nbsp;&nbsp;&nbsp; // Assigning your own id ensures that conflicts do
 * not occur.<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Get Table component. <br>
 * &nbsp;&nbsp;&nbsp; //<br>
 * &nbsp;&nbsp;&nbsp; // @param id The component id.<br>
 * &nbsp;&nbsp;&nbsp; // @param title The table title text.<br>
 * &nbsp;&nbsp;&nbsp; public Table getTable(String id, String title) {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; // Get table.<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; Table table = new Table();<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * table.setDeselectMultipleButton(true); // Show deselect multiple button.<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * table.setSelectMultipleButton(true); // Show select multiple button.<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; table.setTitle(title); //
 * Set title text.<br>
 * <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; return table;<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Get TableRowGroup component with header.<br>
 * &nbsp;&nbsp;&nbsp; //<br>
 * &nbsp;&nbsp;&nbsp; // @param id The component id.<br>
 * &nbsp;&nbsp;&nbsp; // @param sourceData Value binding expression for
 * model data.<br>
 * &nbsp;&nbsp;&nbsp; // @param selected Value binding expression for
 * selected property.<br>
 * &nbsp;&nbsp;&nbsp; // @param header Value binding expression for row
 * group header text.<br>
 * &nbsp;&nbsp;&nbsp; public TableRowGroup getTableRowGroup(String id,
 * String sourceData,<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * String selected, String header) {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; // Get table row group.<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; TableRowGroup rowGroup = new
 * TableRowGroup();<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; rowGroup.setId(id); // Set
 * id.<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * rowGroup.setSourceVar("name"); // Set source var.<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * rowGroup.setHeaderText(header); // Set header text.<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; setValueBinding(rowGroup,
 * "selected", selected); // Set row highlight.<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; setValueBinding(rowGroup,
 * "sourceData", sourceData); // Set source data.<br>
 * <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; return rowGroup;<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Get TableColumn component.<br>
 * &nbsp;&nbsp;&nbsp; //<br>
 * &nbsp;&nbsp;&nbsp; // @param id The component id.<br>
 * &nbsp;&nbsp;&nbsp; // @param sort Value binding expression for column
 * sort.<br>
 * &nbsp;&nbsp;&nbsp; // @param align The field key for column alignment.<br>
 * &nbsp;&nbsp;&nbsp; // @param header The column header text.<br>
 * &nbsp;&nbsp;&nbsp; // @param selectId The component id used to select
 * table rows.<br>
 * &nbsp;&nbsp;&nbsp; public TableColumn getTableColumn(String id, String
 * sort, String align,<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * String header, String selectId) {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; // Get table column.<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; TableColumn col = new
 * TableColumn();<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; col.setId(id); // Set id.<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; col.setSelectId(selectId);
 * // Set id used to select table rows.<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; col.setHeaderText(header);
 * // Set header text.<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; col.setAlignKey(align); //
 * Set align key.<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; setValueBinding(col, "sort",
 * sort); // Set sort.<br>
 * <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; return col;<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Get Checkbox component used for select column.<br>
 * &nbsp;&nbsp;&nbsp; //<br>
 * &nbsp;&nbsp;&nbsp; // @param id The component id.<br>
 * &nbsp;&nbsp;&nbsp; // @param selected Value binding expression for
 * selected property.<br>
 * &nbsp;&nbsp;&nbsp; // @param selectedValue Value binding expression for
 * selectedValue property.<br>
 * &nbsp;&nbsp;&nbsp; public Checkbox getCheckbox(String id, String
 * selected, <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * String selectedValue) {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; // Get checkbox.<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; Checkbox cb = new Checkbox();<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; cb.setId(id); // Set id here
 * and set row highlighting below.<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * cb.setOnClick("setTimeout('initAllRows()', 0)");<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; setValueBinding(cb,
 * "selected", selected); // Set selected.<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; setValueBinding(cb,
 * "selectedValue", selectedValue); // Set selected value.<br>
 * <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; return cb;<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Get Hyperlink component.<br>
 * &nbsp;&nbsp;&nbsp; //<br>
 * &nbsp;&nbsp;&nbsp; // @param id The component id.<br>
 * &nbsp;&nbsp;&nbsp; // @param text Value binding expression for text.<br>
 * &nbsp;&nbsp;&nbsp; // @param action Method binding expression for
 * action.<br>
 * &nbsp;&nbsp;&nbsp; // @param parameter Value binding expression for
 * parameter.<br>
 * &nbsp;&nbsp;&nbsp; public Hyperlink getHyperlink(String id, String
 * text, String action,<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * String parameter) {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; // Get hyperlink.<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; Hyperlink hyperlink = new
 * Hyperlink();<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; hyperlink.setId(id); // Set
 * id.<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; setValueBinding(hyperlink,
 * "text", text); // Set text.<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; setMethodBinding(hyperlink,
 * "action", action); // Set action.<br>
 * <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; // Create paramerter.<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; UIParameter param = new
 * UIParameter();<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; param.setId(id + "_param");<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; param.setName("param");<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; setValueBinding(param,
 * "value", parameter); // Set parameter.<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * hyperlink.getChildren().add(param);<br>
 * <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; return hyperlink;<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Get StaticText component.<br>
 * &nbsp;&nbsp;&nbsp; //<br>
 * &nbsp;&nbsp;&nbsp; // @param text Value binding expression for text.<br>
 * &nbsp;&nbsp;&nbsp; public StaticText getText(String text) {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; // Get static text.<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; StaticText staticText = new
 * StaticText();<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; setValueBinding(staticText,
 * "text", text); // Set text.<br>
 * <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; return staticText;<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Set TableRowGroup children.<br>
 * &nbsp;&nbsp;&nbsp; //<br>
 * &nbsp;&nbsp;&nbsp; // @param rowGroup The TableRowGroup component.<br>
 * &nbsp;&nbsp;&nbsp; // @param cbSort Value binding expression for cb
 * sort.<br>
 * &nbsp;&nbsp;&nbsp; // @param cbSelected Value binding expression for cb
 * selected property.<br>
 * &nbsp;&nbsp;&nbsp; // @param cbSelectedValue Value binding expression
 * for cb selectedValue property.<br>
 * &nbsp;&nbsp;&nbsp; // @param action The Method binding expression for
 * hyperlink action.<br>
 * &nbsp;&nbsp;&nbsp; // @param showHeader Flag indicating to display
 * column header text.<br>
 * &nbsp;&nbsp;&nbsp; public void setTableRowGroupChildren(TableRowGroup
 * rowGroup, String cbSort,<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; String cbSelected, String
 * cbSelectedValue, String action,<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * boolean showHeader) {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; // UI guidelines recomend no
 * headers for second row group.<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; String header1 = showHeader
 * ? "Last Name" : null;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; String header2 = showHeader
 * ? "First Name" : null;<br>
 * <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; // Get columns.<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; TableColumn col1 =
 * getTableColumn(<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * "col0", cbSort, null, null, CHECKBOX_ID);<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; TableColumn col2 =
 * getTableColumn(<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * "col1", "#{name.value.last}", "last", header1,
 * null);&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; TableColumn col3 =
 * getTableColumn(<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * "col2", "#{name.value.first}", "first", header2, null);<br>
 * <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; // Get column components.<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; Checkbox cb =
 * getCheckbox(CHECKBOX_ID, cbSelected, cbSelectedValue);<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; StaticText firstName =
 * getText("#{name.value.first}");<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; // If action was provided,
 * add a hyperlink; otherwise, use static text.<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; if (action != null) {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * Hyperlink lastName = getHyperlink(HYPERLINK_ID, <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * "#{name.value.last}", action,<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * "#{name.value.last}");<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * col2.getChildren().add(lastName);<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; } else {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * StaticText lastName = getText("#{name.value.last}");<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * col2.getChildren().add(lastName);<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; }<br>
 * <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; // Add Children.<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; col1.getChildren().add(cb);<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * col3.getChildren().add(firstName);<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * rowGroup.getChildren().add(col1);<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * rowGroup.getChildren().add(col2);<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * rowGroup.getChildren().add(col3);<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Helper method to set value bindings.<br>
 * &nbsp;&nbsp;&nbsp; //<br>
 * &nbsp;&nbsp;&nbsp; // @param component The UIComponent to set a value
 * binding for.<br>
 * &nbsp;&nbsp;&nbsp; // @param name The name of the value binding.<br>
 * &nbsp;&nbsp;&nbsp; // @param value The value of the value binding.<br>
 * &nbsp;&nbsp;&nbsp; protected void setValueBinding(UIComponent
 * component, String name, <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * String value) {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; if (value == null) {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * return;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; }<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; FacesContext context =
 * FacesContext.getCurrentInstance();<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * component.setValueBinding(name, context.getApplication().<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * createValueBinding(value));<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * <br>
 * &nbsp;&nbsp;&nbsp; // Helper method to set method bindings.<br>
 * &nbsp;&nbsp;&nbsp; //<br>
 * &nbsp;&nbsp;&nbsp; // @param component The UIComponent to set a value
 * binding for.<br>
 * &nbsp;&nbsp;&nbsp; // @param name The name of the method binding.<br>
 * &nbsp;&nbsp;&nbsp; // @param action The action of the method binding.<br>
 * &nbsp;&nbsp;&nbsp; protected void setMethodBinding(UIComponent
 * component, String name,<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * String action) {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; if (action == null) {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * return;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; }<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; FacesContext context =
 * FacesContext.getCurrentInstance();<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * component.getAttributes().put(name, context.getApplication().<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * createMethodBinding(action, new Class[0]));<br>
 * &nbsp;&nbsp;&nbsp; }<br>
 * }</code><br>
 * <br>
 * <p>Auto-generated component class.
 * Do <strong>NOT</strong> modify; all changes
 * <strong>will</strong> be lost!</p>
 */

public abstract class TableRowGroupBase extends javax.faces.component.UIComponentBase {

    /**
     * <p>Construct a new <code>TableRowGroupBase</code>.</p>
     */
    public TableRowGroupBase() {
        super();
        setRendererType("com.sun.rave.web.ui.TableRowGroup");
    }

    /**
     * <p>Return the identifier of the component family to which this
     * component belongs.  This identifier, in conjunction with the value
     * of the <code>rendererType</code> property, may be used to select
     * the appropriate {@link Renderer} for this component instance.</p>
     */
    public String getFamily() {
        return "com.sun.rave.web.ui.TableRowGroup";
    }

    // aboveColumnFooter
    private boolean aboveColumnFooter = false;
    private boolean aboveColumnFooter_set = false;

    /**
 * <p>Set the <code>aboveColumnFooter</code> attribute to true to display the group 
 * footer bar above the column footers bar. The default is to display the group 
 * footer below the column footers.</p>
     */
    public boolean isAboveColumnFooter() {
        if (this.aboveColumnFooter_set) {
            return this.aboveColumnFooter;
        }
        ValueBinding _vb = getValueBinding("aboveColumnFooter");
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
 * <p>Set the <code>aboveColumnFooter</code> attribute to true to display the group 
 * footer bar above the column footers bar. The default is to display the group 
 * footer below the column footers.</p>
     * @see #isAboveColumnFooter()
     */
    public void setAboveColumnFooter(boolean aboveColumnFooter) {
        this.aboveColumnFooter = aboveColumnFooter;
        this.aboveColumnFooter_set = true;
    }

    // aboveColumnHeader
    private boolean aboveColumnHeader = false;
    private boolean aboveColumnHeader_set = false;

    /**
 * <p>Set the <code>aboveColumnHeader</code> attribute to true to display the group 
 * header bar above the column headers bar. The default is to display the group 
 * header below the column headers.</p>
     */
    public boolean isAboveColumnHeader() {
        if (this.aboveColumnHeader_set) {
            return this.aboveColumnHeader;
        }
        ValueBinding _vb = getValueBinding("aboveColumnHeader");
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
 * <p>Set the <code>aboveColumnHeader</code> attribute to true to display the group 
 * header bar above the column headers bar. The default is to display the group 
 * header below the column headers.</p>
     * @see #isAboveColumnHeader()
     */
    public void setAboveColumnHeader(boolean aboveColumnHeader) {
        this.aboveColumnHeader = aboveColumnHeader;
        this.aboveColumnHeader_set = true;
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

    // collapsed
    private boolean collapsed = false;
    private boolean collapsed_set = false;

    /**
 * <p>Use the collapsed attribute to initially render the group as collapsed, so that 
 * the data rows are hidden and only the header row is visible. The default is to 
 * show the group expanded.</p>
     */
    public boolean isCollapsed() {
        if (this.collapsed_set) {
            return this.collapsed;
        }
        ValueBinding _vb = getValueBinding("collapsed");
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
 * <p>Use the collapsed attribute to initially render the group as collapsed, so that 
 * the data rows are hidden and only the header row is visible. The default is to 
 * show the group expanded.</p>
     * @see #isCollapsed()
     */
    public void setCollapsed(boolean collapsed) {
        this.collapsed = collapsed;
        this.collapsed_set = true;
    }

    // emptyDataMsg
    private String emptyDataMsg = null;

    /**
 * <p>The text to be displayed when the table does not contain data. The text is 
 * displayed left-aligned in a single row that contains one cell that spans all 
 * columns. The <code>emptyDataMsg</code> text might be something similar to "No 
 * items found." If users can add items to the table, the message might include 
 * instructions, such as "This table contains no files. To add a file to monitor, 
 * click the New button."</p>
     */
    public String getEmptyDataMsg() {
        if (this.emptyDataMsg != null) {
            return this.emptyDataMsg;
        }
        ValueBinding _vb = getValueBinding("emptyDataMsg");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>The text to be displayed when the table does not contain data. The text is 
 * displayed left-aligned in a single row that contains one cell that spans all 
 * columns. The <code>emptyDataMsg</code> text might be something similar to "No 
 * items found." If users can add items to the table, the message might include 
 * instructions, such as "This table contains no files. To add a file to monitor, 
 * click the New button."</p>
     * @see #getEmptyDataMsg()
     */
    public void setEmptyDataMsg(String emptyDataMsg) {
        this.emptyDataMsg = emptyDataMsg;
    }

    // extraFooterHtml
    private String extraFooterHtml = null;

    /**
 * <p>Extra HTML code to be appended to the <code>&lt;tr&gt;</code> HTML element that 
 * is rendered for the group footer. Use only code that is valid in an HTML 
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
 * <p>Extra HTML code to be appended to the <code>&lt;tr&gt;</code> HTML element that 
 * is rendered for the group footer. Use only code that is valid in an HTML 
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
 * <p>Extra HTML code to be appended to the <code>&lt;tr&gt;</code> HTML element that 
 * is rendered for the group header. Use only code that is valid in an HTML 
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
 * <p>Extra HTML code to be appended to the <code>&lt;tr&gt;</code> HTML element that 
 * is rendered for the group header. Use only code that is valid in an HTML 
 * <code>&lt;td&gt;</code> element. The code you specify is inserted in the HTML 
 * element, and is not checked for validity. For example, you might set this 
 * attribute to <code>"nowrap=`nowrap'"</code>.</p>
     * @see #getExtraHeaderHtml()
     */
    public void setExtraHeaderHtml(String extraHeaderHtml) {
        this.extraHeaderHtml = extraHeaderHtml;
    }

    // first
    private int first = Integer.MIN_VALUE;
    private boolean first_set = false;

    /**
 * <p>Use the <code>first</code> attribute to specify which row should be the first 
 * to be displayed. This value is used only when the table is paginated. By 
 * default, the first row (0) is displayed first. The value of this property is 
 * maintained as part of the table's state, and the value is updated when the user 
 * clicks on buttons to page through the table.</p>
     */
    public int getFirst() {
        if (this.first_set) {
            return this.first;
        }
        ValueBinding _vb = getValueBinding("first");
        if (_vb != null) {
            Object _result = _vb.getValue(getFacesContext());
            if (_result == null) {
                return Integer.MIN_VALUE;
            } else {
                return ((Integer) _result).intValue();
            }
        }
        return 0;
    }

    /**
 * <p>Use the <code>first</code> attribute to specify which row should be the first 
 * to be displayed. This value is used only when the table is paginated. By 
 * default, the first row (0) is displayed first. The value of this property is 
 * maintained as part of the table's state, and the value is updated when the user 
 * clicks on buttons to page through the table.</p>
     * @see #getFirst()
     */
    public void setFirst(int first) {
        this.first = first;
        this.first_set = true;
    }

    // footerText
    private String footerText = null;

    /**
 * <p>The text to be displayed in the group footer.</p>
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
 * <p>The text to be displayed in the group footer.</p>
     * @see #getFooterText()
     */
    public void setFooterText(String footerText) {
        this.footerText = footerText;
    }

    // groupToggleButton
    private boolean groupToggleButton = false;
    private boolean groupToggleButton_set = false;

    /**
 * <p>Use the <code>groupToggleButton</code> attribute to display a button in the 
 * group header to allow users to collapse and expand the group of rows.</p>
     */
    public boolean isGroupToggleButton() {
        if (this.groupToggleButton_set) {
            return this.groupToggleButton;
        }
        ValueBinding _vb = getValueBinding("groupToggleButton");
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
 * <p>Use the <code>groupToggleButton</code> attribute to display a button in the 
 * group header to allow users to collapse and expand the group of rows.</p>
     * @see #isGroupToggleButton()
     */
    public void setGroupToggleButton(boolean groupToggleButton) {
        this.groupToggleButton = groupToggleButton;
        this.groupToggleButton_set = true;
    }

    // headerText
    private String headerText = null;

    /**
 * <p>The text to be displayed in the group header.</p>
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
 * <p>The text to be displayed in the group header.</p>
     * @see #getHeaderText()
     */
    public void setHeaderText(String headerText) {
        this.headerText = headerText;
    }

    // multipleColumnFooters
    private boolean multipleColumnFooters = false;
    private boolean multipleColumnFooters_set = false;

    /**
 * <p>Use the <code>multipleColumnFooters</code> attribute when the 
 * <code>ui:tableRowGroup</code> contains nested <code>ui:tableColumn</code> tags, 
 * and you want the footers of all the <code>ui:tableColumn</code> tags to be 
 * shown. The default is to show the footers of only the innermost level of nested 
 * <code>ui:tableColumn</code> tags.</p>
     */
    public boolean isMultipleColumnFooters() {
        if (this.multipleColumnFooters_set) {
            return this.multipleColumnFooters;
        }
        ValueBinding _vb = getValueBinding("multipleColumnFooters");
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
 * <p>Use the <code>multipleColumnFooters</code> attribute when the 
 * <code>ui:tableRowGroup</code> contains nested <code>ui:tableColumn</code> tags, 
 * and you want the footers of all the <code>ui:tableColumn</code> tags to be 
 * shown. The default is to show the footers of only the innermost level of nested 
 * <code>ui:tableColumn</code> tags.</p>
     * @see #isMultipleColumnFooters()
     */
    public void setMultipleColumnFooters(boolean multipleColumnFooters) {
        this.multipleColumnFooters = multipleColumnFooters;
        this.multipleColumnFooters_set = true;
    }

    // multipleTableColumnFooters
    private boolean multipleTableColumnFooters = false;
    private boolean multipleTableColumnFooters_set = false;

    /**
 * <p>Use the <code>multipleTableColumnFooters</code> attribute when the 
 * <code>ui:tableRowGroup</code> contains nested <code>ui:tableColumn</code> tags, 
 * and you want the table footers of all the <code>ui:tableColumn</code> tags to 
 * be shown. The default is to show the table footers of only the innermost level 
 * of nested <code>ui:tableColumn</code> tags.</p>
     */
    public boolean isMultipleTableColumnFooters() {
        if (this.multipleTableColumnFooters_set) {
            return this.multipleTableColumnFooters;
        }
        ValueBinding _vb = getValueBinding("multipleTableColumnFooters");
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
 * <p>Use the <code>multipleTableColumnFooters</code> attribute when the 
 * <code>ui:tableRowGroup</code> contains nested <code>ui:tableColumn</code> tags, 
 * and you want the table footers of all the <code>ui:tableColumn</code> tags to 
 * be shown. The default is to show the table footers of only the innermost level 
 * of nested <code>ui:tableColumn</code> tags.</p>
     * @see #isMultipleTableColumnFooters()
     */
    public void setMultipleTableColumnFooters(boolean multipleTableColumnFooters) {
        this.multipleTableColumnFooters = multipleTableColumnFooters;
        this.multipleTableColumnFooters_set = true;
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

    // rows
    private int rows = Integer.MIN_VALUE;
    private boolean rows_set = false;

    /**
 * <p>The number of rows per page</span> to be displayed for a paginated table. The 
 * default value is 25 per page.</p>
     */
    public int getRows() {
        if (this.rows_set) {
            return this.rows;
        }
        ValueBinding _vb = getValueBinding("rows");
        if (_vb != null) {
            Object _result = _vb.getValue(getFacesContext());
            if (_result == null) {
                return Integer.MIN_VALUE;
            } else {
                return ((Integer) _result).intValue();
            }
        }
        return 25;
    }

    /**
 * <p>The number of rows per page</span> to be displayed for a paginated table. The 
 * default value is 25 per page.</p>
     * @see #getRows()
     */
    public void setRows(int rows) {
        this.rows = rows;
        this.rows_set = true;
    }

    // selectMultipleToggleButton
    private boolean selectMultipleToggleButton = false;
    private boolean selectMultipleToggleButton_set = false;

    /**
 * <p>Use the <code>selectMultipleToggleButton</code> attribute to display a button 
 * in the group header to allow users to select all rows of the group at once. 
 * The button toggles a column of checkboxes using the id that is given to the 
 * <code>selectId</code> attribute of the <code>ui:tableColumn</code> tag.</p>
     */
    public boolean isSelectMultipleToggleButton() {
        if (this.selectMultipleToggleButton_set) {
            return this.selectMultipleToggleButton;
        }
        ValueBinding _vb = getValueBinding("selectMultipleToggleButton");
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
 * <p>Use the <code>selectMultipleToggleButton</code> attribute to display a button 
 * in the group header to allow users to select all rows of the group at once. 
 * The button toggles a column of checkboxes using the id that is given to the 
 * <code>selectId</code> attribute of the <code>ui:tableColumn</code> tag.</p>
     * @see #isSelectMultipleToggleButton()
     */
    public void setSelectMultipleToggleButton(boolean selectMultipleToggleButton) {
        this.selectMultipleToggleButton = selectMultipleToggleButton;
        this.selectMultipleToggleButton_set = true;
    }

    // selected
    private boolean selected = false;
    private boolean selected_set = false;

    /**
 * <p>Flag indicating that the current row is selected. If the value is set to true, 
 * the row will appear highlighted.</p>
     */
    public boolean isSelected() {
        if (this.selected_set) {
            return this.selected;
        }
        ValueBinding _vb = getValueBinding("selected");
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
 * <p>Flag indicating that the current row is selected. If the value is set to true, 
 * the row will appear highlighted.</p>
     * @see #isSelected()
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
        this.selected_set = true;
    }

    // sourceData
    private Object sourceData = null;

    /**
 * <p>The <code>sourceData</code> attribute is used to specify the data source to 
 * populate the table. The value of the <code>sourceData</code> attribute must be 
 * a JavaServer Faces EL expression that resolves to a backing bean of type 
 * <code>com.sun.data.provider.TableDataProvider</code>.
 * <br><br>
 * The sourceData property is referenced during multiple phases of the JavaServer 
 * Faces life cycle while iterating over the rows. The TableDataProvider object 
 * that is bound to this attribute should be cached so that the object is not 
 * created more often than needed.</p>
     */
    public Object getSourceData() {
        if (this.sourceData != null) {
            return this.sourceData;
        }
        ValueBinding _vb = getValueBinding("sourceData");
        if (_vb != null) {
            return (Object) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>The <code>sourceData</code> attribute is used to specify the data source to 
 * populate the table. The value of the <code>sourceData</code> attribute must be 
 * a JavaServer Faces EL expression that resolves to a backing bean of type 
 * <code>com.sun.data.provider.TableDataProvider</code>.
 * <br><br>
 * The sourceData property is referenced during multiple phases of the JavaServer 
 * Faces life cycle while iterating over the rows. The TableDataProvider object 
 * that is bound to this attribute should be cached so that the object is not 
 * created more often than needed.</p>
     * @see #getSourceData()
     */
    public void setSourceData(Object sourceData) {
        this.sourceData = sourceData;
    }

    // sourceVar
    private String sourceVar = null;

    /**
 * <p>Use the <code>sourceVar</code> attribute to specify the name of the 
 * request-scope attribute under which model data for the current row will be 
 * exposed when iterating. During iterative processing over the rows of data in 
 * the data provider, the TableDataProvider for the current row is exposed as a 
 * request attribute under the key specified by this property. Note: This 
 * value must be unique for each table in the JSP page.</p>
     */
    public String getSourceVar() {
        if (this.sourceVar != null) {
            return this.sourceVar;
        }
        ValueBinding _vb = getValueBinding("sourceVar");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Use the <code>sourceVar</code> attribute to specify the name of the 
 * request-scope attribute under which model data for the current row will be 
 * exposed when iterating. During iterative processing over the rows of data in 
 * the data provider, the TableDataProvider for the current row is exposed as a 
 * request attribute under the key specified by this property. Note: This 
 * value must be unique for each table in the JSP page.</p>
     * @see #getSourceVar()
     */
    public void setSourceVar(String sourceVar) {
        this.sourceVar = sourceVar;
    }

    // styleClasses
    private String styleClasses = null;

    /**
 * <p>Use the <code>styleClasses</code> attribute to specify a list of CSS style 
 * classes to apply to the rows of the group. You can apply all the styles in the 
 * list to each row by separating the class names with commas. Each row looks the 
 * same when commas are used to delimit the styles. You can apply alternating 
 * styles to individual rows by separating the style class names with spaces. You 
 * can create a pattern of shading alternate rows, for example, to improve 
 * readability of the table. For example, if the list has two elements, the first 
 * style class in the list is applied to the first row, the second class to the 
 * second row, the first class to the third row, the second class to the fourth 
 * row, etc. The tableRowGroup component iterates through the list of styles and 
 * repeats from the beginning until a style is applied to each row.</p>
     */
    public String getStyleClasses() {
        if (this.styleClasses != null) {
            return this.styleClasses;
        }
        ValueBinding _vb = getValueBinding("styleClasses");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Use the <code>styleClasses</code> attribute to specify a list of CSS style 
 * classes to apply to the rows of the group. You can apply all the styles in the 
 * list to each row by separating the class names with commas. Each row looks the 
 * same when commas are used to delimit the styles. You can apply alternating 
 * styles to individual rows by separating the style class names with spaces. You 
 * can create a pattern of shading alternate rows, for example, to improve 
 * readability of the table. For example, if the list has two elements, the first 
 * style class in the list is applied to the first row, the second class to the 
 * second row, the first class to the third row, the second class to the fourth 
 * row, etc. The tableRowGroup component iterates through the list of styles and 
 * repeats from the beginning until a style is applied to each row.</p>
     * @see #getStyleClasses()
     */
    public void setStyleClasses(String styleClasses) {
        this.styleClasses = styleClasses;
    }

    // tableDataFilter
    private com.sun.data.provider.TableDataFilter tableDataFilter = null;

    /**
 * <p>The <code>tableDataFilter</code> attribute is used to define filter critera and 
 * mechanism for filtering the contents of a TableDataProvider. The value of the 
 * <code>tableDataFilter</code> attribute must be a JavaServer Faces EL expression 
 * that resolves to a backing bean of type 
 * <code>com.sun.data.provider.TableDataFilter</code>.</p>
     */
    public com.sun.data.provider.TableDataFilter getTableDataFilter() {
        if (this.tableDataFilter != null) {
            return this.tableDataFilter;
        }
        ValueBinding _vb = getValueBinding("tableDataFilter");
        if (_vb != null) {
            return (com.sun.data.provider.TableDataFilter) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>The <code>tableDataFilter</code> attribute is used to define filter critera and 
 * mechanism for filtering the contents of a TableDataProvider. The value of the 
 * <code>tableDataFilter</code> attribute must be a JavaServer Faces EL expression 
 * that resolves to a backing bean of type 
 * <code>com.sun.data.provider.TableDataFilter</code>.</p>
     * @see #getTableDataFilter()
     */
    public void setTableDataFilter(com.sun.data.provider.TableDataFilter tableDataFilter) {
        this.tableDataFilter = tableDataFilter;
    }

    // tableDataSorter
    private com.sun.data.provider.TableDataSorter tableDataSorter = null;

    /**
 * <p>The <code>tableDataSorter</code> attribute is used to define sort critera and 
 * the mechanism for sorting the contents of a TableDataProvider. The value of the 
 * <code>tableDataSorter</code> attribute must be a JavaServer Faces EL expression 
 * that resolves to a backing bean of type 
 * <code>com.sun.data.provider.TableDataSorter</code>.</p>
     */
    public com.sun.data.provider.TableDataSorter getTableDataSorter() {
        if (this.tableDataSorter != null) {
            return this.tableDataSorter;
        }
        ValueBinding _vb = getValueBinding("tableDataSorter");
        if (_vb != null) {
            return (com.sun.data.provider.TableDataSorter) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>The <code>tableDataSorter</code> attribute is used to define sort critera and 
 * the mechanism for sorting the contents of a TableDataProvider. The value of the 
 * <code>tableDataSorter</code> attribute must be a JavaServer Faces EL expression 
 * that resolves to a backing bean of type 
 * <code>com.sun.data.provider.TableDataSorter</code>.</p>
     * @see #getTableDataSorter()
     */
    public void setTableDataSorter(com.sun.data.provider.TableDataSorter tableDataSorter) {
        this.tableDataSorter = tableDataSorter;
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

    /**
     * <p>Restore the state of this component.</p>
     */
    public void restoreState(FacesContext _context,Object _state) {
        Object _values[] = (Object[]) _state;
        super.restoreState(_context, _values[0]);
        this.aboveColumnFooter = ((Boolean) _values[1]).booleanValue();
        this.aboveColumnFooter_set = ((Boolean) _values[2]).booleanValue();
        this.aboveColumnHeader = ((Boolean) _values[3]).booleanValue();
        this.aboveColumnHeader_set = ((Boolean) _values[4]).booleanValue();
        this.align = (String) _values[5];
        this.bgColor = (String) _values[6];
        this._char = (String) _values[7];
        this.charOff = (String) _values[8];
        this.collapsed = ((Boolean) _values[9]).booleanValue();
        this.collapsed_set = ((Boolean) _values[10]).booleanValue();
        this.emptyDataMsg = (String) _values[11];
        this.extraFooterHtml = (String) _values[12];
        this.extraHeaderHtml = (String) _values[13];
        this.first = ((Integer) _values[14]).intValue();
        this.first_set = ((Boolean) _values[15]).booleanValue();
        this.footerText = (String) _values[16];
        this.groupToggleButton = ((Boolean) _values[17]).booleanValue();
        this.groupToggleButton_set = ((Boolean) _values[18]).booleanValue();
        this.headerText = (String) _values[19];
        this.multipleColumnFooters = ((Boolean) _values[20]).booleanValue();
        this.multipleColumnFooters_set = ((Boolean) _values[21]).booleanValue();
        this.multipleTableColumnFooters = ((Boolean) _values[22]).booleanValue();
        this.multipleTableColumnFooters_set = ((Boolean) _values[23]).booleanValue();
        this.onClick = (String) _values[24];
        this.onDblClick = (String) _values[25];
        this.onKeyDown = (String) _values[26];
        this.onKeyPress = (String) _values[27];
        this.onKeyUp = (String) _values[28];
        this.onMouseDown = (String) _values[29];
        this.onMouseMove = (String) _values[30];
        this.onMouseOut = (String) _values[31];
        this.onMouseOver = (String) _values[32];
        this.onMouseUp = (String) _values[33];
        this.rows = ((Integer) _values[34]).intValue();
        this.rows_set = ((Boolean) _values[35]).booleanValue();
        this.selectMultipleToggleButton = ((Boolean) _values[36]).booleanValue();
        this.selectMultipleToggleButton_set = ((Boolean) _values[37]).booleanValue();
        this.selected = ((Boolean) _values[38]).booleanValue();
        this.selected_set = ((Boolean) _values[39]).booleanValue();
        this.sourceData = (Object) _values[40];
        this.sourceVar = (String) _values[41];
        this.styleClasses = (String) _values[42];
        this.tableDataFilter = (com.sun.data.provider.TableDataFilter) _values[43];
        this.tableDataSorter = (com.sun.data.provider.TableDataSorter) _values[44];
        this.toolTip = (String) _values[45];
        this.valign = (String) _values[46];
        this.visible = ((Boolean) _values[47]).booleanValue();
        this.visible_set = ((Boolean) _values[48]).booleanValue();
    }

    /**
     * <p>Save the state of this component.</p>
     */
    public Object saveState(FacesContext _context) {
        Object _values[] = new Object[49];
        _values[0] = super.saveState(_context);
        _values[1] = this.aboveColumnFooter ? Boolean.TRUE : Boolean.FALSE;
        _values[2] = this.aboveColumnFooter_set ? Boolean.TRUE : Boolean.FALSE;
        _values[3] = this.aboveColumnHeader ? Boolean.TRUE : Boolean.FALSE;
        _values[4] = this.aboveColumnHeader_set ? Boolean.TRUE : Boolean.FALSE;
        _values[5] = this.align;
        _values[6] = this.bgColor;
        _values[7] = this._char;
        _values[8] = this.charOff;
        _values[9] = this.collapsed ? Boolean.TRUE : Boolean.FALSE;
        _values[10] = this.collapsed_set ? Boolean.TRUE : Boolean.FALSE;
        _values[11] = this.emptyDataMsg;
        _values[12] = this.extraFooterHtml;
        _values[13] = this.extraHeaderHtml;
        _values[14] = new Integer(this.first);
        _values[15] = this.first_set ? Boolean.TRUE : Boolean.FALSE;
        _values[16] = this.footerText;
        _values[17] = this.groupToggleButton ? Boolean.TRUE : Boolean.FALSE;
        _values[18] = this.groupToggleButton_set ? Boolean.TRUE : Boolean.FALSE;
        _values[19] = this.headerText;
        _values[20] = this.multipleColumnFooters ? Boolean.TRUE : Boolean.FALSE;
        _values[21] = this.multipleColumnFooters_set ? Boolean.TRUE : Boolean.FALSE;
        _values[22] = this.multipleTableColumnFooters ? Boolean.TRUE : Boolean.FALSE;
        _values[23] = this.multipleTableColumnFooters_set ? Boolean.TRUE : Boolean.FALSE;
        _values[24] = this.onClick;
        _values[25] = this.onDblClick;
        _values[26] = this.onKeyDown;
        _values[27] = this.onKeyPress;
        _values[28] = this.onKeyUp;
        _values[29] = this.onMouseDown;
        _values[30] = this.onMouseMove;
        _values[31] = this.onMouseOut;
        _values[32] = this.onMouseOver;
        _values[33] = this.onMouseUp;
        _values[34] = new Integer(this.rows);
        _values[35] = this.rows_set ? Boolean.TRUE : Boolean.FALSE;
        _values[36] = this.selectMultipleToggleButton ? Boolean.TRUE : Boolean.FALSE;
        _values[37] = this.selectMultipleToggleButton_set ? Boolean.TRUE : Boolean.FALSE;
        _values[38] = this.selected ? Boolean.TRUE : Boolean.FALSE;
        _values[39] = this.selected_set ? Boolean.TRUE : Boolean.FALSE;
        _values[40] = this.sourceData;
        _values[41] = this.sourceVar;
        _values[42] = this.styleClasses;
        _values[43] = this.tableDataFilter;
        _values[44] = this.tableDataSorter;
        _values[45] = this.toolTip;
        _values[46] = this.valign;
        _values[47] = this.visible ? Boolean.TRUE : Boolean.FALSE;
        _values[48] = this.visible_set ? Boolean.TRUE : Boolean.FALSE;
        return _values;
    }

}
