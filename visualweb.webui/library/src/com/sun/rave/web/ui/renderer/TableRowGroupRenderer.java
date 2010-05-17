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
package com.sun.rave.web.ui.renderer;

import com.sun.data.provider.RowKey;
import com.sun.rave.web.ui.component.Table;
import com.sun.rave.web.ui.component.TableColumn;
import com.sun.rave.web.ui.component.TableHeader;
import com.sun.rave.web.ui.component.TableRowGroup;
import com.sun.rave.web.ui.theme.Theme;
import com.sun.rave.web.ui.theme.ThemeStyles;
import com.sun.rave.web.ui.util.LogUtil;
import com.sun.rave.web.ui.util.RenderingUtilities;
import com.sun.rave.web.ui.util.ThemeUtilities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.component.NamingContainer;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;

/**
 * This class renders TableRowGroup components.
 * <p>
 * The TableRowGroup component provides a layout mechanism for displaying rows 
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
 * com.sun.rave.web.ui.renderer.TableRowGroupRenderer.level = FINE
 * </pre></p><p>
 * See TLD docs for more information.
 * </p>
 */
public class TableRowGroupRenderer extends Renderer {
    /**
     * The set of String pass-through attributes to be rendered.
     * <p>
     * Note: The BGCOLOR attribute is deprecated (in the HTML 4.0 spec) in favor
     * of style sheets. In addition, the DIR and LANG attributes are not
     * cuurently supported.
     * </p>
     */
    private static final String stringAttributes[] = {
        "align", //NOI18N
        "bgColor", //NOI18N
        "char", //NOI18N
        "charOff", //NOI18N
        "dir", //NOI18N
        "lang", //NOI18N
        "onClick", //NOI18N
        "onDblClick", //NOI18N
        "onKeyDown", //NOI18N
        "onKeyPress", //NOI18N
        "onKeyUp", //NOI18N
        "onMouseDown", //NOI18N
        "onMouseUp", //NOI18N
        "onMouseMove", //NOI18N
        "onMouseOut", //NOI18N
        "onMouseOver", //NOI18N
        "style", //NOI18N
        "valign"}; //NOI18N

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Renderer methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Render the beginning of the specified UIComponent to the output stream or 
     * writer associated with the response we are creating.
     *
     * @param context FacesContext for the current request.
     * @param component UIComponent to be rendered.
     *
     * @exception IOException if an input/output error occurs.
     * @exception NullPointerException if context or component is null.
     */
    public void encodeBegin(FacesContext context, UIComponent component)
            throws IOException {
        if (context == null || component == null) {
            log("encodeBegin", //NOI18N
                "Cannot render, FacesContext or UIComponent is null"); //NOI18N
            throw new NullPointerException();
        }
        if (!component.isRendered()) {
            log("encodeBegin", "Component not rendered, nothing to display"); //NOI18N
            return;
        }

        TableRowGroup group = (TableRowGroup) component;
        ResponseWriter writer = context.getResponseWriter();

        // Render group and column headers.
        if (group.isAboveColumnHeader()) {
            renderGroupHeader(context, group, writer);
            renderColumnHeaders(context, group, writer);
        } else {
            renderColumnHeaders(context, group, writer);
            renderGroupHeader(context, group, writer);
        }
        group.setRowKey(null); // Clean up.
    }

    /**
     * Render the children of the specified UIComponent to the output stream or
     * writer associated with the response we are creating.
     *
     * @param context FacesContext for the current request.
     * @param component UIComponent to be decoded.
     *
     * @exception IOException if an input/output error occurs.
     * @exception NullPointerException if context or component is null.
     */
    public void encodeChildren(FacesContext context, UIComponent component)
            throws IOException {
        if (context == null || component == null) {
            log("encodeChildren", //NOI18N
                "Cannot render, FacesContext or UIComponent is null"); //NOI18N
            throw new NullPointerException();
        }
        if (!component.isRendered()) {
            log("encodeChildren", "Component not rendered, nothing to display"); //NOI18N
            return;
        }

        TableRowGroup group = (TableRowGroup) component;
        ResponseWriter writer = context.getResponseWriter();

        // Render empty data message.
        if (group.getRowCount() == 0) {
            log("encodeChildren", "Cannot render data, row count is zero"); //NOI18N
            renderEmptyDataColumn(context, group, writer);
            return;
        }

        // Get rendered row keys.
        RowKey[] rowKeys = group.getRenderedRowKeys();
        if (rowKeys == null) {
            log("encodeChildren", "Cannot render data, RowKey array is null"); //NOI18N
            return;
        }

        // Iterate over the rendered RowKey objects.
        for (int i = 0; i < rowKeys.length; i++) {
            group.setRowKey(rowKeys[i]);
            if (!group.isRowAvailable()) {
                log("encodeChildren", "Cannot render data, row not available"); //NOI18N
                break;
            }

            // Render row.
            renderEnclosingTagStart(context, group, writer, i);

            // Render children.
            Iterator kids = group.getTableColumnChildren();
            while (kids.hasNext()) {
                TableColumn col = (TableColumn) kids.next();
                if (!col.isRendered()) {
                    log("encodeChildren", //NOI18N
                        "TableColumn not rendered, nothing to display"); //NOI18N
                    continue;
                }
                // Render column.
                RenderingUtilities.renderComponent(col, context);
            }
            renderEnclosingTagEnd(writer);
        }
        group.setRowKey(null); // Clean up.
    }

    /**
     * Render the ending of the specified UIComponent to the output stream or 
     * writer associated with the response we are creating.
     *
     * @param context FacesContext for the current request.
     * @param component UIComponent to be rendered.
     *
     * @exception IOException if an input/output error occurs.
     * @exception NullPointerException if context or component is null.
     */
    public void encodeEnd(FacesContext context, UIComponent component)
            throws IOException {
        if (context == null || component == null) {
            log("encodeEnd", //NOI18N
                "Cannot render, FacesContext or UIComponent is null"); //NOI18N
            throw new NullPointerException();
        }
        if (!component.isRendered()) {
            log("encodeEnd", "Component not rendered, nothing to display"); //NOI18N
            return;
        }

        TableRowGroup group = (TableRowGroup) component;
        ResponseWriter writer = context.getResponseWriter();

        // Do not render footers for an empty table.
        if (group.getRowCount() == 0) {
            log("encodeEnd", //NOI18N
                "Column, group, and table footers not rendered, row count is zero"); //NOI18N
            return;
        }

        // Render group and column footers.
        if (group.isAboveColumnFooter()) {
            renderGroupFooter(context, group, writer);
            renderColumnFooters(context, group, writer);
        } else {
            renderColumnFooters(context, group, writer);
            renderGroupFooter(context, group, writer);
        }

        // Do not render table footers for an empty table.
        Table table = group.getTableAncestor();
        if (table.getRowCount() > 0) {
            renderTableColumnFooters(context, group, writer);
        } else {
            log("encodeEnd", //NOI18N
                "Table column footers not rendered, row count is zero"); //NOI18N
        }

        group.setRowKey(null); // Clean up.
    }

    /**
     * Return a flag indicating whether this Renderer is responsible
     * for rendering the children the component it is asked to render.
     * The default implementation returns false.
     */
    public boolean getRendersChildren() {
        return true;
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Empty data methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Render empty data message for TableRowGroup components.
     *
     * @param context FacesContext for the current request.
     * @param component TableRowGroup to be rendered.
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    protected void renderEmptyDataColumn(FacesContext context,
            TableRowGroup component, ResponseWriter writer) throws IOException {
        if (component == null) {
            log("renderEmptyDataColumn", //NOI18N
                "Cannot render empty data column, TableRowGroup is null"); //NOI18N
            return;
        }
        // Render row start.
        renderEnclosingTagStart(context, component, writer, -1);

        // Render empty data column.
        writer.writeText("\n", null); //NOI18N       
        RenderingUtilities.renderComponent(component.getEmptyDataColumn(),
            context);
        renderEnclosingTagEnd(writer);
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Column methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Render column footers for TableRowGroup components.
     * <p>
     * Note: Although not currently a requirement, nested TableColumn children
     * could render column footers that look like:
     * </p><pre>
     *
     * |   | 1 | 2 |   | 3 | 4 | 5 | 6 |   |
     * | A |   B   | C |       D       | E |
     *
     * </pre><p>
     * In this case, components would be rendered on separate rows. For example,
     * the HTML would look like:
     * </p><pre>
     *
     * <table border="1">
     * <tr>
     * <th rowspan="2">A</th>
     * <th>1</th>
     * <th>2</th>
     * <th rowspan="2">C</th>
     * <th>3</th>
     * <th>4</th>
     * <th>5</th>
     * <th>6</th>
     * <th rowspan="2">E</th>
     * </tr>
     * <tr>
     * <th colspan="2">B</th>
     * <th colspan="2">D</th>
     * </tr>
     * </table>
     *
     * </pre><p>
     * However, the current implementation will render only the first row, which
     * would look like:
     * </p><pre>
     *
     * | A | 1 | 2 | C | 3 | 4 | 5 | 6 | E |
     *
     * </pre>
     * @param context FacesContext for the current request.
     * @param component TableRowGroup to be rendered.
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    protected void renderColumnFooters(FacesContext context,
            TableRowGroup component, ResponseWriter writer) throws IOException {
        if (component == null) {
            log("renderColumnFooters", //NOI18N
                "Cannot render column footers, TableRowGroup is null"); //NOI18N
            return;
        }

        // Get Map of List objects containing nested TableColumn children.
        Map map = getColumnFooterMap(component);

        // Render nested TableColumn children on separate rows.
        Theme theme = getTheme();
        Table table = component.getTableAncestor();
        for (int c = 0; c < map.size(); c++) {
            // The default is to show one level only.
            if (c > 0 && !component.isMultipleColumnFooters()) {
                log("renderColumnFooters", //NOI18N
                    "Multiple column footers not rendered, nothing to display"); //NOI18N
                break;
            }

            // Flag to keep from rendering empty tag when no headers are displayed.
            boolean renderStartElement = true;

            // Get List of nested TableColumn children.
            List list = (List) map.get(new Integer(c));
            for (int i = 0; i < list.size(); i++) {
                TableColumn col = (TableColumn) list.get(i);
                if (!col.isRendered()) {
                    log("renderColumnFooters", //NOI18N
                        "TableColumn not rendered, nothing to display"); //NOI18N
                    continue;
                }

                // Get group footer.
                UIComponent footer = col.getColumnFooter();
                if (!(footer != null && footer.isRendered())) {
                    log("renderColumnFooters", //NOI18N
                        "Column footer not rendered, nothing to display"); //NOI18N
                    continue;
                }

                // Render start element.
                if (renderStartElement) {
                    renderStartElement = false;
                    writer.writeText("\n", null); //NOI18N
                    writer.startElement("tr", component); //NOI18N
                    writer.writeAttribute("id", getId(component, //NOI18N
                        TableRowGroup.COLUMN_FOOTER_BAR_ID +
                        NamingContainer.SEPARATOR_CHAR + c), null);

                    // Render style class.
                    if (component.isCollapsed()) {
                        writer.writeAttribute("class", //NOI18N
                            theme.getStyleClass(ThemeStyles.HIDDEN), null); //NOI18N
                    }
                }
                // Render footer.
                RenderingUtilities.renderComponent(footer, context);
            }

            // If start element was rendered, this value will be false.
            if (!renderStartElement) {
                writer.endElement("tr"); //NOI18N
            }
        }
    }

    /**
     * Render column headers for TableRowGroup components.
     * <p>
     * Note: Although not typical, nested TableColumn children may render column
     * headers that look like:
     * </p><pre>
     *
     * | A |   B   | C |       D       | E |
     * |   | 1 | 2 |   | 3 | 4 | 5 | 6 |   |
     *
     * </pre><p>
     * In this case, components would be rendered on separate rows. For example,
     * the HTML would look like:
     * </p><pre>
     *
     * <table border="1">
     * <tr>
     * <th rowspan="2">A</th>
     * <th colspan="2">B</th>
     * <th rowspan="2">C</th>
     * <th colspan="4">D</th>
     * <th rowspan="2">E</th>
     * </tr>
     * <tr>
     * <th>1</th>
     * <th>2</th>
     * <th>3</th>
     * <th>4</th>
     * <th>5</th>
     * <th>6</th>
     * </tr>
     * </table>
     *
     * </pre>
     * @param context FacesContext for the current request.
     * @param component TableRowGroup to be rendered.
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    protected void renderColumnHeaders(FacesContext context,
            TableRowGroup component, ResponseWriter writer) throws IOException {
        if (component == null) {
            log("renderColumnHeaders", //NOI18N
                "Cannot render column headers, TableRowGroup is null"); //NOI18N
            return;
        }

        // Get Map of List objects containing nested TableColumn children.
        Map map = getColumnHeaderMap(component);

        // Render nested TableColumn children on separate rows.
        Theme theme = getTheme();
        Table table = component.getTableAncestor();
        for (int c = 0; c < map.size(); c++) {
            // Flag to keep from rendering empty tag when no headers are displayed.
            boolean renderStartElement = true;

            // Get List of nested TableColumn children.
            List list = (List) map.get(new Integer(c));
            for (int i = 0; i < list.size(); i++) {
                TableColumn col = (TableColumn) list.get(i);
                if (!col.isRendered()) {
                    log("renderColumnHeaders", //NOI18N
                        "TableColumn not rendered, nothing to display"); //NOI18N
                    continue;
                }

                // Get group header.
                UIComponent header = col.getColumnHeader();
                if (!(header != null && header.isRendered())) {
                    log("renderColumnHeaders", //NOI18N
                        "Column header not rendered, nothing to display"); //NOI18N
                    continue;
                }

                // Render start element.
                if (renderStartElement) {
                    renderStartElement = false;
                    writer.writeText("\n", null); //NOI18N
                    writer.startElement("tr", component); //NOI18N
                    writer.writeAttribute("id", getId(component, //NOI18N
                        TableRowGroup.COLUMN_HEADER_BAR_ID +
                        NamingContainer.SEPARATOR_CHAR + c), null);

                    // Render style class.
                    //
                    // Note: We must determine if column headers are available for 
                    // all or individual TableRowGroup components. That is, there
                    // could be a single column header for all row groups or one for
                    // each group. Thus, headers may only be hidden when there is
                    // more than one column header.
                    if (component.isCollapsed() && table != null
                            && table.getColumnHeadersCount() > 1) {
                        writer.writeAttribute("class", //NOI18N
                            theme.getStyleClass(ThemeStyles.HIDDEN), null); //NOI18N
                    }
                }
                //<RAVE>
                // Set the visiblilty of the column header to that of the column
                 ((TableHeader)header).setVisible(col.isVisible());
                //</RAVE> 
                 
                // Render header.
                RenderingUtilities.renderComponent(header, context);
            }
            
            // If start element was rendered, this value will be false.
            if (!renderStartElement) {
                writer.endElement("tr"); //NOI18N
            }
        }
    }

    /**
     * Render table column footers for TableRowGroup components.
     * <p>
     * Note: Although not currently a requirement, nested TableColumn children
     * could render column footers that look like:
     * </p><pre>
     *
     * |   | 1 | 2 |   | 3 | 4 | 5 | 6 |   |
     * | A |   B   | C |       D       | E |
     *
     * </pre><p>
     * In this case, components would be rendered on separate rows. For example,
     * the HTML would look like:
     * </p><pre>
     *
     * <table border="1">
     * <tr>
     * <th rowspan="2">A</th>
     * <th>1</th>
     * <th>2</th>
     * <th rowspan="2">C</th>
     * <th>3</th>
     * <th>4</th>
     * <th>5</th>
     * <th>6</th>
     * <th rowspan="2">E</th>
     * </tr>
     * <tr>
     * <th colspan="2">B</th>
     * <th colspan="2">D</th>
     * </tr>
     * </table>
     *
     * </pre><p>
     * However, the current implementation will render only the first row, which
     * would look like:
     * </p><pre>
     *
     * | A | 1 | 2 | C | 3 | 4 | 5 | 6 | E |
     *
     * </pre>
     * @param context FacesContext for the current request.
     * @param component TableRowGroup to be rendered.
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    protected void renderTableColumnFooters(FacesContext context,
            TableRowGroup component, ResponseWriter writer) throws IOException {
        if (component == null) {
            log("renderTableColumnFooters", //NOI18N
                "Cannot render table column footers, TableRowGroup is null"); //NOI18N
            return;
        }

        // Get Map of List objects containing nested TableColumn children.
        Map map = getColumnFooterMap(component);

        // Render nested TableColumn children on separate rows.
        Theme theme = getTheme();
        Table table = component.getTableAncestor();
        for (int c = 0; c < map.size(); c++) {
            // The default is to show one level only.
            if (c > 0 && table != null
                    && !component.isMultipleTableColumnFooters()) {
                log("renderTableColumnFooters", //NOI18N
                    "Multiple table column footers not rendered, nothing to display"); //NOI18N
                break;
            }

            // Flag to keep from rendering empty tag when no headers are displayed.
            boolean renderStartElement = true;

            // Get List of nested TableColumn children.
            List list = (List) map.get(new Integer(c));
            for (int i = 0; i < list.size(); i++) {
                TableColumn col = (TableColumn) list.get(i);
                if (!col.isRendered()) {
                    log("renderTableColumnFooters", //NOI18N
                        "TableColumn not rendered, nothing to display"); //NOI18N
                    continue;
                }

                // Get group footer.
                UIComponent footer = col.getTableColumnFooter();
                if (!(footer != null && footer.isRendered())) {
                    log("renderTableColumnFooters", //NOI18N
                        "Table column footer not rendered, nothing to display"); //NOI18N
                    continue;
                }

                // Render start element.
                if (renderStartElement) {
                    renderStartElement = false;
                    writer.writeText("\n", null); //NOI18N
                    writer.startElement("tr", component); //NOI18N
                    writer.writeAttribute("id", getId(component, //NOI18N
                        TableRowGroup.TABLE_COLUMN_FOOTER_BAR_ID +
                        NamingContainer.SEPARATOR_CHAR + c), null);

                    // Render style class.
                    //
                    // Note: We must determine if column footers are available 
                    // for all or individual TableRowGroup components. That is, 
                    // there could be a single column footer for all row groups 
                    // or one for each group. Thus, footers may only be hidden 
                    // when there is more than one column footer.
                    if (component.isCollapsed() 
                            && table.getColumnHeadersCount() > 1) {
                        writer.writeAttribute("class", //NOI18N
                            theme.getStyleClass(ThemeStyles.HIDDEN), null); //NOI18N
                    }
                }
                // Render header.
                RenderingUtilities.renderComponent(footer, context);
            }

            // If start element was rendered, this value will be false.
            if (!renderStartElement) {
                writer.endElement("tr"); //NOI18N
            }
        }
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Group methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Render group footer for TableRowGroup components.
     *
     * @param context FacesContext for the current request.
     * @param component TableRowGroup to be rendered.
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    protected void renderGroupFooter(FacesContext context,
            TableRowGroup component, ResponseWriter writer) throws IOException {
        if (component == null) {
            log("renderGroupFooter", //NOI18N
                "Cannot render group footer, TableRowGroup is null"); //NOI18N
            return;
        }

        // Get group footer.
        UIComponent footer = component.getGroupFooter();
        if (!(footer != null && footer.isRendered())) {
            log("renderGroupFooter", //NOI18N
                "Group footer not rendered, nothing to display"); //NOI18N
            return;
        }

        Theme theme = getTheme();
        writer.writeText("\n", null); //NOI18N
        writer.startElement("tr", component); //NOI18N
        writer.writeAttribute("id", getId(component, //NOI18N
            TableRowGroup.GROUP_FOOTER_BAR_ID), null);

        // Render style class.
        if (component.isCollapsed()) {
            writer.writeAttribute("class", //NOI18N
                theme.getStyleClass(ThemeStyles.HIDDEN), null); //NOI18N
        }

        // Render footer.
        RenderingUtilities.renderComponent(footer, context);
        writer.endElement("tr"); //NOI18N
    }

    /**
     * Render group header for TableRowGroup components.
     *
     * @param context FacesContext for the current request.
     * @param component TableRowGroup to be rendered.
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    protected void renderGroupHeader(FacesContext context,
            TableRowGroup component, ResponseWriter writer) throws IOException {
        if (component == null) {
            log("renderGroupHeader", //NOI18N
                "Cannot render group header, TableRowGroup is null"); //NOI18N
            return;
        }

        // Get group header.
        UIComponent header = component.getGroupHeader();
        if (!(header != null && header.isRendered())) {
            log("renderGroupHeader", //NOI18N
                "Group header not rendered, nothing to display"); //NOI18N
            return;
        }

        Theme theme = getTheme();
        writer.writeText("\n", null); //NOI18N
        writer.startElement("tr", component); //NOI18N
        writer.writeAttribute("id", getId(component, //NOI18N
            TableRowGroup.GROUP_HEADER_BAR_ID), null);

        // Render header.
        RenderingUtilities.renderComponent(header, context);
        writer.endElement("tr"); //NOI18N
    }
   
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Enclosing tag methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Render enclosing tag for TableRowGroup components.
     *
     * @param context FacesContext for the current request.
     * @param component TableRowGroup to be rendered.
     * @param writer ResponseWriter to which the component should be rendered.
     * @param index The current row index.
     *
     * @exception IOException if an input/output error occurs.
     */
    protected void renderEnclosingTagStart(FacesContext context,
        TableRowGroup component, ResponseWriter writer, int index)
            throws IOException {
        if (component == null) {
            log("renderEnclosingTagStart", //NOI18N
                "Cannot render enclosing tag, TableRowGroup is null"); //NOI18N
            return;
        }

        Theme theme = getTheme();
        writer.writeText("\n", null); //NOI18N
        writer.startElement("tr", component); //NOI18N
        writer.writeAttribute("id", component.getClientId(context), null); //NOI18N

        // Get style class for nonempty table.
        String styleClasses[] = getRowStyleClasses(component);
        String styleClass = (index > -1 && styleClasses.length > 0)
            ? styleClasses[index % styleClasses.length] : null;

        // Get selected style class.
        if (component.isSelected()) {
            String s = theme.getStyleClass(ThemeStyles.TABLE_SELECT_ROW);
            styleClass = (styleClass != null) ? styleClass + " " + s : s; //NOI18N
        }

        // Get collapsed style class.
        if (component.isCollapsed()) {
            String s = theme.getStyleClass(ThemeStyles.HIDDEN);
            styleClass = (styleClass != null) ? styleClass + " " + s : s; //NOI18N
        }

        // Render style class.
        RenderingUtilities.renderStyleClass(context, writer, component,
            styleClass);

        // Render tooltip.
        if (component.getToolTip() != null) {
            writer.writeAttribute("title", component.getToolTip(), "toolTip"); //NOI18N
        }

        // Render pass through attributes.
        RenderingUtilities.writeStringAttributes(component, writer, 
            stringAttributes);
    }

    /**
     * Render enclosing tag for TableRowGroup components.
     *
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    protected void renderEnclosingTagEnd(ResponseWriter writer)
            throws IOException {
        writer.endElement("tr"); //NOI18N
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Private methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Helper method to get Map of List objects containing nested TableColumn
     * children.
     * <p>
     * Note: Although not currently a requirement, nested TableColumn children
     * could render column footers that look like:
     * </p><pre>
     *
     * |   | 1 | 2 |   | 3 | 4 | 5 | 6 |   |
     * | A |   B   | C |       D       | E |
     *
     * </pre><p>
     * In this case, components would be rendered on separate rows. Thus, we
     * need a map that contains List objects like so:
     * </p><pre>
     * 
     * Key (row) 0: A, 1, 2, C, 3, 4, 5, 6, E
     * Key (row) 1: B, D
     * 
     * </pre><p>
     * Obtaining the List for key 0 tells the renderer that A, 1, 2, C, 3, 4, 5,
     * 6, and E should be rendered for the first row. Obtaining the List for key
     * 1, tells the renderer that B and D should be rendered for the next row.
     * And so on...
     * </p>
     * @param component TableRowGroup to be rendered.
     * @return A Map of nested TableColumn children.
     */
    private Map getColumnFooterMap(TableRowGroup component) {
        Map map = getColumnHeaderMap(component); // Start with header map.
        if (map.size() == 0) {
            log("getColumnFooterMap", "Cannot obtain column footer map"); //NOI18N
            return map;
        }

        // Invert map.
        HashMap newMap = new HashMap();
        for (int i = 0; i < map.size(); i++) {
            newMap.put(new Integer(i), map.get(new Integer(map.size() - i - 1)));
        }

        // Move all non-nested components to the top row.
        List newList = (List) newMap.get(new Integer(0));
        for (int c = 1; c < newMap.size(); c++) { // Top row is set already.
            List list = (List) newMap.get(new Integer(c));
            for (int i = list.size() - 1; i >= 0; i--) { // Start with last component.
                TableColumn col = (TableColumn) list.get(i);
                if (col.getTableColumnChildren().hasNext()) {
                    // Do not move TableColumn components with nested children.
                    continue;
                }

                // Get colspan of all previous components.
                int colspan = 0;
                for (int k = i - 1; k >= 0; k--) {
                    TableColumn prevCol = (TableColumn) list.get(k);
                    if (prevCol.getTableColumnChildren().hasNext()) {
                        // Count only nested TableColumn components. Other
                        // components have not been moved to the to row, yet.
                        colspan += prevCol.getColumnCount();
                    }
                }

                // Set new position in the top row.
                newList.add(colspan, col);
                list.remove(i);
            }
        }
        return newMap;
    }

    /**
     * Helper method to get Map of List objects containing nested TableColumn
     * children.
     * <p>
     * Note: Nested TableColumn children may be rendered on separate rows. For
     * example, to render column footers that look like the following:
     * </p><pre>
     *
     * | A |   B   | C |       D       | E |
     * |   | 1 | 2 |   | 3 | 4 | 5 | 6 |   |
     *
     * </pre><p>
     * In this case, components would be rendered on separate rows. Thus, we
     * need a map that contains List objects like so:
     * </p><pre>
     * 
     * Key (row) 0: A, B, C, D, E
     * Key (row) 1: 1, 2, 3, 4, 5, 6 
     * 
     * </pre><p>
     * Obtaining the List for key 0 tells the renderer that A, B, C, D, and E
     * should be rendered for the first row. Obtaining the List for key 1, tells
     * the renderer that 1, 2, 3, 4, 5, and 6 should be rendered for the next
     * row. And so on...
     * </p>
     * @param component TableRowGroup to be rendered.
     * @return A Map of nested TableColumn children.
     */
    private Map getColumnHeaderMap(TableRowGroup component) {
        HashMap map = new HashMap();
        if (component == null) {
            log("getColumnHeaderMap", //NOI18N
                "Cannot obtain column header map, TableRowGroup is null"); //NOI18N
            return map;
        }
        Iterator kids = component.getTableColumnChildren();
        while (kids.hasNext()) {
            TableColumn col = (TableColumn) kids.next();
            initColumnHeaderMap(col, map, 0);
        }
        return map;
    }

    /**
     * Get component id.
     *
     * @param component The parent UIComponent component.
     * @param id The id of the the component to be rendered.
     */
    private String getId(UIComponent component, String id) {
        String clientId = component.getClientId(FacesContext.getCurrentInstance());
        return clientId + NamingContainer.SEPARATOR_CHAR + id;
    }

    /** Helper method to get Theme objects. */
    private Theme getTheme() {
	return ThemeUtilities.getTheme(FacesContext.getCurrentInstance());
    }

    /**
     * Helper method to get an array of stylesheet classes to be applied to each
     * row, in the order specified.
     * <p>
     * Note: This is a comma-delimited list of CSS style classes that will be
     * applied to the rows of this table. A space separated list of classes may 
     * also be specified for any individual row. These styles are applied, in 
     * turn, to each row in the table. For example, if the list has two 
     * elements, the first style class in the list is applied to the first row, 
     * the second to the second row, the first to the third row, the second to 
     * the fourth row, etc. In other words, we keep iterating through the list
     * until we reach the end, and then we start at the beginning again.
     * </p>
     * @param component TableRowGroup component being rendered.
     * @return An array of stylesheet classes.
     */
    private String[] getRowStyleClasses(TableRowGroup component) {
        String values = (component != null)
            ? (String) component.getStyleClasses() : null;

        if (values == null) {
            return new String[0];
        }

        values = values.trim();
        ArrayList list = new ArrayList();

        while (values.length() > 0) {
            int comma = values.indexOf(","); //NOI18N
            if (comma >= 0) {
                list.add(values.substring(0, comma).trim());
                values = values.substring(comma + 1);
            } else {
                list.add(values.trim());
                values = ""; //NOI18N
            }
        }

        String results[] = new String[list.size()];
        return ((String[]) list.toArray(results));
    }

    /**
     * Helper method to initialize Map of List objects containing nested
     * TableColumn children.
     *
     * @param component TableColumn to be rendered.
     * @param map Map to save component List.
     * @param level The current level of the component tree.
     */
    private void initColumnHeaderMap(TableColumn component, Map map,
            int level) {
        if (component == null) {
            log("initColumnHeaderMap", //NOI18N
                "Cannot initialize column header map, TableColumn is null"); //NOI18N
            return;
        }
    
        // Get new List for nested TableColumn children.
        Iterator kids = component.getTableColumnChildren();
        if (kids.hasNext()) {
            ArrayList newList = new ArrayList();
            while (kids.hasNext()) {
                TableColumn col = (TableColumn) kids.next();
                if (!col.isRendered()) {
                    continue;
                }
                initColumnHeaderMap(col, map, level + 1);
            }
        }

        // Create a new List if needed.
        Integer key = new Integer(level);
        List list = (List) map.get(key);
        if (list == null) {
            list = new ArrayList();
        }
        list.add(component); // Save component in List.
        map.put(key, list); // Save List in map.
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
