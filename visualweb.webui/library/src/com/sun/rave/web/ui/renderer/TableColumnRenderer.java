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
import com.sun.rave.web.ui.component.Alarm;
import com.sun.rave.web.ui.component.TableColumn;
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
 * This class renders TableColumn components.
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
 * com.sun.rave.web.ui.renderer.TableColumnRenderer.level = FINE
 * </pre></p><p>
 * See TLD docs for more information.
 * </p>
 */
public class TableColumnRenderer extends Renderer {
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

        // Don't render TableColumn parents.
        TableColumn col = (TableColumn) component;
        Iterator kids = col.getTableColumnChildren();
        if (!kids.hasNext()) {
            ResponseWriter writer = context.getResponseWriter();
            renderEnclosingTagStart(context, col, writer);
        }
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

        // Don't render TableColumn parents.
        TableColumn col = (TableColumn) component;
        Iterator kids = col.getTableColumnChildren();
        if (kids.hasNext()) {
            while (kids.hasNext()) {
                TableColumn kid = (TableColumn) kids.next();
                if (!kid.isRendered()) {
                    log("encodeChildren", //NOI18N
                        "TableColumn not rendered, nothing to display"); //NOI18N
                    continue;
                }
                RenderingUtilities.renderComponent(kid, context);
            }
        } else {
            // Render empty cell image. Don't do this automatically because we
            // cannot reconize truly null values, such as an empty alarm cell or a
            // comment field which is blank, neither of which should have the dash
            // image. Further, the dash image is not used for cells that contain
            // user interface elements such as checkboxes or drop-down lists when
            // these elements are not applicable. Instead, the elements are simply
            // not displayed.
            if (col.isEmptyCell()) {
                RenderingUtilities.renderComponent(col.getEmptyCellIcon(), context);
            } else {
                // Render children.
                kids = col.getChildren().iterator();
                while (kids.hasNext()) {
                    UIComponent kid = (UIComponent) kids.next();
                    if (!kid.isRendered()) {
                        log("encodeChildren", //NOI18N
                            "TableColumn child not rendered, nothing to display"); //NOI18N
                        continue;
                    }
                    RenderingUtilities.renderComponent(kid, context);

                    // Render a separator bar between each child component.
                    if (kids.hasNext() && col.isEmbeddedActions()) {
                        RenderingUtilities.renderComponent(
                            col.getEmbeddedActionSeparatorIcon(), context);
                        ResponseWriter writer = context.getResponseWriter();
                        writer.writeText(" ", null); //NOI18N
                    }
                }
            }
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
            log("encodeEnd", //NOI18N
                "Cannot render, FacesContext or UIComponent is null"); //NOI18N
            throw new NullPointerException();
        }
        if (!component.isRendered()) {
            log("encodeEnd", "Component not rendered, nothing to display"); //NOI18N
            return;
        }

        // Don't render TableColumn parents.
        TableColumn col = (TableColumn) component;
        Iterator kids = col.getTableColumnChildren();
        if (!kids.hasNext()) {
            ResponseWriter writer = context.getResponseWriter();
            renderEnclosingTagEnd(context, col, writer);
        }
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
    // Enclosing tag methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Render enclosing tag for TableColumn components.
     *
     * @param context FacesContext for the current request.
     * @param component TableColumn to be rendered.
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    protected void renderEnclosingTagStart(FacesContext context,
            TableColumn component, ResponseWriter writer) throws IOException {
        if (component == null) {
            log("renderEnclosingTagStart", //NOI18N
                "Cannot render enclosing tag, UIComponent is null"); //NOI18N
            return;
        }
        
        // Note: Row header is not valid for select column.
        boolean isRowHeader = component.isRowHeader()
            && component.getSelectId() == null;

        writer.writeText("\n", null); //NOI18N
        writer.startElement(isRowHeader ? "th" : "td", component); //NOI18N

        // Render client id.
        writer.writeAttribute("id", component.getClientId(context), null); //NOI18N
           
        // Render style class.
        RenderingUtilities.renderStyleClass(context, writer, component,
            getStyleClass(component));

        // Render align.
        if (component.getAlign() != null) {
            writer.writeAttribute("align", component.getAlign(), null); //NOI18N
        }

        // Render scope.
        if (isRowHeader) {
            writer.writeAttribute("scope", "row", null); //NOI18N
        } else if (component.getScope() != null) {
            writer.writeAttribute("scope", component.getScope(), null); //NOI18N
        }

        // Render colspan.
        if (component.getColSpan() > -1) {
            writer.writeAttribute("colspan", //NOI18N
                Integer.toString(component.getColSpan()), null); //NOI18N
        }

        // Render rowspan.
        if (component.getRowSpan() > -1) {
            writer.writeAttribute("rowspan", //NOI18N
                Integer.toString(component.getRowSpan()), null); //NOI18N
        }

        // Render nowrap.
        if (component.isNoWrap()) {
            writer.writeAttribute("nowrap", "nowrap", null); //NOI18N
        }

        // Render tooltip.
        if (component.getToolTip() != null) {
            writer.writeAttribute("title", component.getToolTip(), "toolTip"); //NOI18N
        }

        // Render pass through attributes.
        RenderingUtilities.writeStringAttributes(component, writer, 
            stringAttributes);
    }

    /**
     * Render enclosing tag for TableColumn components.
     *
     * @param context FacesContext for the current request.
     * @param component TableColumn to be rendered.
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    protected void renderEnclosingTagEnd(FacesContext context,
            TableColumn component, ResponseWriter writer) throws IOException {
        if (component == null) {
            log("renderEnclosingTagEnd", //NOI18N
                "Cannot render enclosing tag, UIComponent is null"); //NOI18N
            return;
        }

        // Note: Row header is not valid for select column.
        boolean isRowHeader = component.isRowHeader()
            && component.getSelectId() == null;

        writer.endElement(isRowHeader ? "th" : "td"); //NOI18N
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Private methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Helper method to get column style class for TableColumn components.
     *
     * @param component TableColumn to be rendered
     *
     * @return The style class.
     */
    private String getStyleClass(TableColumn component) {
        String styleClass = null;
        if (component == null) {
            log("getStyleClass", //NOI18N
                "Cannot obtain style class, TableColumn is null"); //NOI18N
            return styleClass;
        }
        
        // Get sort level.
        TableRowGroup group = component.getTableRowGroupAncestor();
        SortCriteria criteria = component.getSortCriteria();
        int level = (group != null) ? group.getSortLevel(criteria) : -1;

        // Get style class.
        if (component.isSpacerColumn()) {
            styleClass = ThemeStyles.TABLE_TD_SPACER;
        } else if (component.getSeverity() != null
                && !component.getSeverity().equals(Alarm.SEVERITY_OK)) {
            styleClass = ThemeStyles.TABLE_TD_ALARM;
        } else if (level == 1) {
            if (component.getSelectId() != null) {
                styleClass = ThemeStyles.TABLE_TD_SELECTCOL_SORT;
            } else {
                styleClass = ThemeStyles.TABLE_TD_SORT;
            }
        } else {
            if (component.getSelectId() != null) {
                styleClass = ThemeStyles.TABLE_TD_SELECTCOL;
            } else {
                styleClass = ThemeStyles.TABLE_TD_LAYOUT;
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
