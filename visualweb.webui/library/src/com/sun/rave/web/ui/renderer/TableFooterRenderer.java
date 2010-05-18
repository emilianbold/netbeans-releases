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

import com.sun.data.provider.SortCriteria;
import com.sun.rave.web.ui.component.Table;
import com.sun.rave.web.ui.component.TableColumn;
import com.sun.rave.web.ui.component.TableFooter;
import com.sun.rave.web.ui.component.TableRowGroup;
import com.sun.rave.web.ui.theme.Theme;
import com.sun.rave.web.ui.theme.ThemeStyles;
import com.sun.rave.web.ui.util.LogUtil;
import com.sun.rave.web.ui.util.RenderingUtilities;
import com.sun.rave.web.ui.util.ThemeUtilities;

import java.io.IOException;
import java.util.Iterator;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;

/**
 * This class renders TableFooter components.
 * <p>
 * Note: To see the messages logged by this class, set the following global
 * defaults in your JDK's "jre/lib/logging.properties" file.
 * </p><p><pre>
 * java.util.logging.ConsoleHandler.level = FINE
 * com.sun.rave.web.ui.renderer.TableFooterRenderer.level = FINE
 * </pre></p>
 */
public class TableFooterRenderer extends Renderer {
    /**
     * The set of String pass-through attributes to be rendered.
     * <p>
     * Note: The WIDTH, HEIGHT, and BGCOLOR attributes are all deprecated (in
     * the HTML 4.0 spec) in favor of style sheets. In addition, the DIR and 
     * LANG attributes are not cuurently supported.
     * </p>
     */
    private static final String stringAttributes[] = {
        "abbr", //NOI18N
        "align", //NOI18N
        "axis", //NOI18N
        "bgColor", //NOI18N
        "char", //NOI18N
        "charOff", //NOI18N
        "dir", //NOI18N
        "headers", //NOI18N
        "height", //NOI18N
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
        "valign", //NOI18N
        "width"}; //NOI18N

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

        TableFooter footer = (TableFooter) component;
        ResponseWriter writer = context.getResponseWriter();
        renderEnclosingTagStart(context, footer, writer);
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

        TableFooter footer = (TableFooter) component;
        ResponseWriter writer = context.getResponseWriter();

        // Render footers.
        if (footer.isGroupFooter()) {
            renderGroupFooter(context, footer, writer);
        } else if (footer.isTableColumnFooter()) {
            renderTableColumnFooter(context, footer, writer);
        } else if (footer.isTableFooter()) {
            renderTableFooter(context, footer, writer);
        } else {
            renderColumnFooter(context, footer, writer);
        }
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
            log("encodeChildren", //NOI18N
                "Cannot render, FacesContext or UIComponent is null"); //NOI18N
            throw new NullPointerException();
        }
        if (!component.isRendered()) {
            log("encodeChildren", "Component not rendered, nothing to display"); //NOI18N
            return;
        }

        TableFooter footer = (TableFooter) component;
        ResponseWriter writer = context.getResponseWriter();
        renderEnclosingTagEnd(context, footer, writer);
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
    // Footer methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Render column footer for TableFooter components.
     *
     * @param context FacesContext for the current request.
     * @param component TableFooter to be rendered.
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    protected void renderColumnFooter(FacesContext context,
            TableFooter component, ResponseWriter writer) throws IOException {
        if (component == null) {
            log("renderColumnFooter", //NOI18N
                "Cannot render column footer, TableFooter is null"); //NOI18N
            return;
        }

        // Render facet.
        TableColumn col = component.getTableColumnAncestor();
        UIComponent facet = (col != null)
            ? component.getFacet(TableColumn.FOOTER_FACET) : null;
        if (facet != null && facet.isRendered()) {
            RenderingUtilities.renderComponent(facet, context);
        } else {
            writer.startElement("span", component); //NOI18N
            writer.writeAttribute("class", //NOI18N
                getTheme().getStyleClass(ThemeStyles.TABLE_GROUP_COL_FOOTER_TEXT),
                null);

            // Render footer text.
            if (col != null && col.getFooterText() != null) {
                writer.writeText(col.getFooterText(), null);
            }
            writer.endElement("span"); //NOI18N
        }
    }

    /**
     * Render group footer for TableFooter components.
     *
     * @param context FacesContext for the current request.
     * @param component TableFooter to be rendered.
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    protected void renderGroupFooter(FacesContext context,
            TableFooter component, ResponseWriter writer) throws IOException {
        if (component == null) {
            log("renderGroupFooter", //NOI18N
                "Cannot render group footer, TableFooter is null"); //NOI18N
            return;
        }

        // Render facet.
        TableRowGroup group = component.getTableRowGroupAncestor();
        UIComponent facet = (group != null)
            ? group.getFacet(TableRowGroup.FOOTER_FACET) : null;
        if (facet != null && facet.isRendered()) {
            RenderingUtilities.renderComponent(facet, context);
        } else {
            writer.startElement("span", component); //NOI18N
            writer.writeAttribute("class", //NOI18N
                getTheme().getStyleClass(ThemeStyles.TABLE_GROUP_FOOTER_TEXT),
                null);

            // Render text.
            if (group.getFooterText() != null) {
                writer.writeText(group.getFooterText(), null);
            }
            writer.endElement("span"); //NOI18N
        }
    }

    /**
     * Render table column footer for TableFooter components.
     *
     * @param context FacesContext for the current request.
     * @param component TableFooter to be rendered.
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    protected void renderTableColumnFooter(FacesContext context,
            TableFooter component, ResponseWriter writer) throws IOException {
        if (component == null) {
            log("renderTableColumnFooter", //NOI18N
                "Cannot render table column footer, TableFooter is null"); //NOI18N
            return;
        }

        // Render facet.
        TableColumn col = component.getTableColumnAncestor();
        UIComponent facet = (col != null)
            ? col.getFacet(TableColumn.TABLE_FOOTER_FACET) : null;
        if (facet != null && facet.isRendered()) {
            RenderingUtilities.renderComponent(facet, context);
        } else {
            writer.startElement("span", component); //NOI18N
            writer.writeAttribute("class", //NOI18N
                getTheme().getStyleClass(ThemeStyles.TABLE_COL_FOOTER_TEXT),
                null);

            // Get TableColumn component.
            if (col != null && col.getTableFooterText() != null) {
                writer.writeText(col.getTableFooterText(), null);
            }
            writer.endElement("span"); //NOI18N
        }
    }

    /**
     * Render table footer for TableFooter components.
     *
     * @param context FacesContext for the current request.
     * @param component TableFooter to be rendered.
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    protected void renderTableFooter(FacesContext context,
            TableFooter component, ResponseWriter writer) throws IOException {
        if (component == null) {
            log("renderTableFooter", //NOI18N
                "Cannot render table footer, TableFooter is null"); //NOI18N
            return;
        }

        Table table = component.getTableAncestor();
        if (table == null) {
            log("renderTableFooter", //NOI18N
                "Cannot render table footer, Table is null"); //NOI18N
            return;
        }

        // Render facet.
        UIComponent facet = table.getFacet(Table.TABLE_FOOTER_FACET);
        if (facet != null && facet.isRendered()) {
            RenderingUtilities.renderComponent(facet, context);
        } else {
            Theme theme = getTheme();

            // Get hidden selected rows text.
            String hiddenSelectionsText = table.isHiddenSelectedRows()
                ? theme.getMessage("table.hiddenSelections", //NOI18N
                    new String[] {Integer.toString(table.getHiddenSelectedRowsCount())})
                : null;

            // If both footer and hidden selected rows are not null, the
            // table footer is left aigned and hidden selected rows is right
            // aligned. Otherwise, text should appear centered.
            if (hiddenSelectionsText != null && table.getFooterText() != null) {
                writer.startElement("span", component); //NOI18N
                writer.writeAttribute("class", //NOI18N
                    theme.getStyleClass(ThemeStyles.TABLE_FOOTER_LEFT), null);
                writer.writeText(table.getFooterText(), null);
                writer.endElement("span"); //NOI18N
                writer.startElement("span", component); //NOI18N
                writer.writeAttribute("class", //NOI18N
                    theme.getStyleClass(ThemeStyles.TABLE_FOOTER_MESSAGE_SPAN),
                    null);
                writer.writeText(hiddenSelectionsText, null);
                writer.endElement("span"); //NOI18N
            } else {
                writer.startElement("span", component); //NOI18N
                writer.writeAttribute("class", //NOI18N
                    theme.getStyleClass(ThemeStyles.TABLE_FOOTER_TEXT), null);
                writer.writeText((table.getFooterText() != null)
                    ? table.getFooterText() : hiddenSelectionsText, null);
                writer.endElement("span"); //NOI18N
            }
        }
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Enclosing tag methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Render enclosing tag for TableFooter components.
     *
     * @param context FacesContext for the current request.
     * @param component TableFooter to be rendered.
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    protected void renderEnclosingTagStart(FacesContext context,
            TableFooter component, ResponseWriter writer) throws IOException {
        if (component == null) {
            log("renderEnclosingTagStart", //NOI18N
                "Cannot render enclosing tag, TableFooter is null"); //NOI18N
            return;
        }

        writer.writeText("\n", null); //NOI18N
        writer.startElement("td", component); //NOI18N
        writer.writeAttribute("id", component.getClientId(context), null); //NOI18N

        // Render style class.
        String extraHtml = RenderingUtilities.renderStyleClass(context, writer,
            component, getStyleClass(component), component.getExtraHtml());

        // Render colspan.
        if (component.getColSpan() > -1
                && (extraHtml == null || extraHtml.indexOf("colspan=") == -1)) { //NOI18N
            writer.writeAttribute("colspan", //NOI18N
                Integer.toString(component.getColSpan()), null); //NOI18N
        }

        // Render rowspan.
        if (component.getRowSpan() > -1
                && (extraHtml == null || extraHtml.indexOf("rowspan=") == -1)) { //NOI18N
            writer.writeAttribute("rowspan", //NOI18N
                Integer.toString(component.getRowSpan()), null); //NOI18N
        }

        // Render nowrap.
        if (component.isNoWrap()
                && (extraHtml == null || extraHtml.indexOf("nowrap=") == -1)) { //NOI18N
            writer.writeAttribute("nowrap", "nowrap", null); //NOI18N
        }

        // Render tooltip.
        if (component.getToolTip() != null
                && (extraHtml == null || extraHtml.indexOf("title=") == -1)) { //NOI18N
            writer.writeAttribute("title", component.getToolTip(), "toolTip"); //NOI18N
        }

        // Render pass through attributes.
        RenderingUtilities.writeStringAttributes(component, writer, 
            stringAttributes, extraHtml);
    }

    /**
     * Render enclosing tag for TableFooter components.
     *
     * @param context FacesContext for the current request.
     * @param component TableFooter to be rendered.
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    protected void renderEnclosingTagEnd(FacesContext context,
            TableFooter component, ResponseWriter writer) throws IOException {
        if (component == null) {
            log("renderEnclosingTagEnd", //NOI18N
                "Cannot render enclosing tag, TableFooter is null"); //NOI18N
            return;
        }
        writer.endElement("td"); //NOI18N
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Private methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Helper method to get style class for TableFooter components.
     *
     * @param component TableFooter to be rendered
     * @return The style class.
     */
    private String getStyleClass(TableFooter component) {
        String styleClass = null;
        if (component == null) {
            log("getStyleClass", //NOI18N
                "Cannot obtain style class, TableFooter is null"); //NOI18N
            return styleClass;
        }

        // Get style class.
        if (component.isTableFooter()) {
            styleClass = ThemeStyles.TABLE_FOOTER;
        } else if (component.isGroupFooter()) {
            styleClass = ThemeStyles.TABLE_GROUP_FOOTER;
        } else {
            TableColumn col = component.getTableColumnAncestor();
            if (col != null && col.isSpacerColumn()) {
                styleClass = (component.isTableColumnFooter())
                    ? ThemeStyles.TABLE_COL_FOOTER_SPACER
                    : ThemeStyles.TABLE_TD_SPACER;
            } else if (component.getSortLevel() == 1) {
                styleClass = (component.isTableColumnFooter())
                    ? ThemeStyles.TABLE_COL_FOOTER_SORT
                    : ThemeStyles.TABLE_GROUP_COL_FOOTER_SORT;
            } else {
                styleClass = (component.isTableColumnFooter())
                    ? ThemeStyles.TABLE_COL_FOOTER
                    : ThemeStyles.TABLE_GROUP_COL_FOOTER;
            }
        }
        return getTheme().getStyleClass(styleClass);
    }

    /** Helper method to get Theme objects. */
    private Theme getTheme() {
	return ThemeUtilities.getTheme(FacesContext.getCurrentInstance());
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
