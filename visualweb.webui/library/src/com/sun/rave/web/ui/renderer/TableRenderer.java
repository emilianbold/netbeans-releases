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
import com.sun.data.provider.SortCriteria;
import com.sun.rave.web.ui.component.DropDown;
import com.sun.rave.web.ui.component.Label;
import com.sun.rave.web.ui.component.Table;
import com.sun.rave.web.ui.component.TableActions;
import com.sun.rave.web.ui.component.TableColumn;
import com.sun.rave.web.ui.component.TableHeader;
import com.sun.rave.web.ui.component.TablePanels;
import com.sun.rave.web.ui.component.TableRowGroup;
import com.sun.rave.web.ui.component.util.Util;
import com.sun.rave.web.ui.model.Option;
import com.sun.rave.web.ui.theme.Theme;
import com.sun.rave.web.ui.theme.ThemeStyles;
import com.sun.rave.web.ui.util.LogUtil;
import com.sun.rave.web.ui.util.RenderingUtilities;
import com.sun.rave.web.ui.util.ThemeUtilities;
import com.sun.rave.web.ui.theme.ThemeImages;
import com.sun.rave.web.ui.theme.ThemeJavascript;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.component.NamingContainer;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;

/**
 * This class renders Table components.
 * <p>
 * The table component provides a layout mechanism for displaying table actions.
 * UI guidelines describe specific behavior that can applied to the rows and 
 * columns of data such as sorting, filtering, pagination, selection, and custom 
 * user actions. In addition, UI guidelines also define sections of the table 
 * that can be used for titles, row group headers, and placement of pre-defined
 * and user defined actions.
 * </p><p>
 * Note: Column headers and footers are rendered by TableRowGroupRenderer. Table
 * column footers are rendered by TableRenderer.
 * </p><p>
 * Note: To see the messages logged by this class, set the following global
 * defaults in your JDK's "jre/lib/logging.properties" file.
 * </p><p><pre>
 * java.util.logging.ConsoleHandler.level = FINE
 * com.sun.rave.web.ui.renderer.TableRenderer.level = FINE
 * </pre></p><p>
 * See TLD docs for more information.
 * </p>
 */
public class TableRenderer extends Renderer {
    // Javascript object name.
    private static final String JAVASCRIPT_OBJECT_CLASS = "Table"; //NOI18N

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
        "dir", //NOI18N
        "frame", //NOI18N
        "lang", //NOI18N
        "onClick", //NOI18N
        "onDblClick", //NOI18N
        "onKeyDown", //NOI18N
        "onKeyPress", //NOI18N
        "onKeyUp", //NOI18N
        "onMouseDown", //NOI18N
        "onMouseMove", //NOI18N
        "onMouseOut", //NOI18N
        "onMouseOver", //NOI18N
        "onMouseUp", //NOI18N
        "rules", //NOI18N
        "summary"}; //NOI18N

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

        Table table = (Table) component;
        ResponseWriter writer = context.getResponseWriter();
        renderEnclosingTagStart(context, table, writer);
        renderTitle(context, table, writer);
        renderActionsTop(context, table, writer);
        renderEmbeddedPanels(context, table, writer);
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

        Table table = (Table) component;
        ResponseWriter writer = context.getResponseWriter();

        // Render TableRowGroup children.
        Iterator kids = table.getTableRowGroupChildren();
        while (kids.hasNext()) {
            TableRowGroup group = (TableRowGroup) kids.next();
            RenderingUtilities.renderComponent(group, context);
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

        Table table = (Table) component;
        ResponseWriter writer = context.getResponseWriter();
        renderActionsBottom(context, table, writer);
        renderTableFooter(context, table, writer);
        renderEnclosingTagEnd(writer);
        renderJavascript(context, table, writer);
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
    // Action bar methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Render the bottom actions for Table components.
     *
     * @param context FacesContext for the current request.
     * @param component Table to be rendered.
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    protected void renderActionsBottom(FacesContext context,
            Table component, ResponseWriter writer) throws IOException {
        if (component == null) {
            log("renderActionsBottom", //NOI18N
                "Cannot render actions bar, Table is null"); //NOI18N
            return;
        }

        // Get panel component.
        UIComponent actions = component.getTableActionsBottom();
        if (!(actions != null && actions.isRendered())) {
            log("renderActionsBottom", //NOI18N
                "Actions bar not rendered, nothing to display"); //NOI18N
            return;
        }

        writer.writeText("\n", null); //NOI18N
        writer.startElement("tr", component); //NOI18N
        writer.writeAttribute("id", getId(component, //NOI18N
            Table.TABLE_ACTIONS_BOTTOM_BAR_ID), null);

        // Render embedded panels.
        RenderingUtilities.renderComponent(actions, context);
        writer.endElement("tr"); //NOI18N
    }

    /**
     * Render the top actions for Table components.
     *
     * @param context FacesContext for the current request.
     * @param component Table to be rendered.
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    protected void renderActionsTop(FacesContext context,
            Table component, ResponseWriter writer) throws IOException {
        if (component == null) {
            log("renderActionsTop", //NOI18N
                "Cannot render actions bar, Table is null"); //NOI18N
            return;
        }

        // Get panel component.
        UIComponent actions = component.getTableActionsTop();
        if (!(actions != null && actions.isRendered())) {
            log("renderActionsTop", //NOI18N
                "Actions bar not rendered, nothing to display"); //NOI18N
            return;
        }

        writer.writeText("\n", null); //NOI18N
        writer.startElement("tr", component); //NOI18N
        writer.writeAttribute("id", getId(component, //NOI18N
            Table.TABLE_ACTIONS_TOP_BAR_ID), null);

        // Render embedded panels.
        RenderingUtilities.renderComponent(actions, context);
        writer.endElement("tr"); //NOI18N
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Embedded panel methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Render embedded panels for Table components.
     *
     * @param context FacesContext for the current request.
     * @param component Table to be rendered.
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    protected void renderEmbeddedPanels(FacesContext context,
            Table component, ResponseWriter writer) throws IOException {
        if (component == null) {
            log("renderEmbeddedPanels", //NOI18N
                "Cannot render embedded panels, Table is null"); //NOI18N
            return;
        }

        // Get panel component.
        UIComponent panels = component.getEmbeddedPanels();
        if (!(panels != null && panels.isRendered())) {
            log("renderEmbeddedPanels", //NOI18N
                "Embedded panels not rendered, nothing to display"); //NOI18N
            return;
        }

        writer.writeText("\n", null); //NOI18N
        writer.startElement("tr", component); //NOI18N
        writer.writeAttribute("id", getId(component, //NOI18N
            Table.EMBEDDED_PANELS_BAR_ID), null);

        // Render embedded panels.
        RenderingUtilities.renderComponent(panels, context);
        writer.endElement("tr"); //NOI18N
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Footer methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Render table footer for Table components.
     *
     * @param context FacesContext for the current request.
     * @param component Table to be rendered.
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    protected void renderTableFooter(FacesContext context, Table component,
            ResponseWriter writer) throws IOException {
        if (component == null) {
            log("renderTableFooter", //NOI18N
                "Cannot render table foter, Table is null"); //NOI18N
            return;
        }

        // Get footer.
        UIComponent footer = component.getTableFooter();
        if (!(footer != null && footer.isRendered())) {
            log("renderTableFooter", //NOI18N
                "Table footer not rendered, nothing to display"); //NOI18N
            return;
        }

        Theme theme = getTheme();
        writer.writeText("\n", null); //NOI18N
        writer.startElement("tr", component); //NOI18N
        writer.writeAttribute("id", getId(component, Table.TABLE_FOOTER_BAR_ID), //NOI18N
            null);

        // Render footer.
        RenderingUtilities.renderComponent(footer, context);
        writer.endElement("tr"); //NOI18N
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Title methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Render title for Table components.
     *
     * @param context FacesContext for the current request.
     * @param component Table to be rendered.
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    protected void renderTitle(FacesContext context, Table component,
            ResponseWriter writer) throws IOException {
        if (component == null) {
            log("renderTitle", "Cannot render title, Table is null"); //NOI18N
            return;
        }

        // Render facet.
        UIComponent facet = component.getFacet(Table.TITLE_FACET);
        if (facet != null) {
            renderTitleStart(context, component, writer);
            RenderingUtilities.renderComponent(facet, context);
            renderTitleEnd(context, writer);
            return;
        }

        // Render default title.
        if (component.getTitle() == null) {
            log("renderTitle", "Title is null, nothing to display"); //NOI18N
            return;
        }

        // Get filter augment.
        Theme theme = getTheme();
        String filter = (component.getFilterText() != null)
            ? theme.getMessage("table.title.filterApplied", //NOI18N
                new String[] {component.getFilterText()})
            : ""; //NOI18N

        // Get TableRowGroup component.
        TableRowGroup group = component.getTableRowGroupChild();
        boolean paginated = (group != null) ? group.isPaginated() : false;

        // Initialize values.
        int totalRows = component.getRowCount();
        boolean emptyTable = (totalRows == 0);
        
        // Render title (e.g., "Title (25 - 50 of 1000) [Filter]").
        String title = component.getTitle();
        if (component.isAugmentTitle()) {
            if (!emptyTable && paginated) {
                // Get max values for paginated group table.
                int maxRows = component.getRows();
                int maxFirst = component.getFirst();

                // Get first and last rows augment.
                String first = Integer.toString(maxFirst + 1);
                String last = Integer.toString(Math.min(maxFirst + maxRows,
                    totalRows));

                if (component.getItemsText() != null) {
                    title = theme.getMessage("table.title.paginatedItems", //NOI18N
                        new String[] {component.getTitle(), first, last,
                            Integer.toString(totalRows), component.getItemsText(), filter});
                } else {
                    title = theme.getMessage("table.title.paginated", //NOI18N
                        new String[] {component.getTitle(), first, last,
                            Integer.toString(totalRows), filter});
                }
            } else {
                if (component.getItemsText() != null) {
                    title = theme.getMessage("table.title.scrollItems", //NOI18N
                        new String[] {component.getTitle(), Integer.toString(totalRows),
                            component.getItemsText(), filter});
                } else {
                    title = theme.getMessage("table.title.scroll", //NOI18N
                        new String[] {component.getTitle(), Integer.toString(totalRows),
                            filter});
                }
            }
        } else {
            log("renderTitle", //NOI18N
                "Title not augmented, itemsText & filterText not displayed"); //NOI18N
        }

        renderTitleStart(context, component, writer);

        // Render title and hidden rows text.
        if (component.isHiddenSelectedRows()) {
            writer.startElement("span", component); //NOI18N
            writer.writeAttribute("class", //NOI18N
                theme.getStyleClass(ThemeStyles.TABLE_TITLE_TEXT_SPAN), null);
            writer.writeText(title, null);
            writer.endElement("span"); //NOI18N
            writer.startElement("span", component); //NOI18N
            writer.writeAttribute("class", //NOI18N
                theme.getStyleClass(ThemeStyles.TABLE_TITLE_MESSAGE_SPAN), null);
            writer.writeText(theme.getMessage("table.hiddenSelections", //NOI18N
                new String[] {Integer.toString(
                    component.getHiddenSelectedRowsCount())}), null);
            writer.endElement("span"); //NOI18N
        } else {
            // Render default title text.
            writer.writeText(title, null);
        }
        renderTitleEnd(context, writer);
    }

    /**
     * Render title for Table components.
     *
     * @param context FacesContext for the current request.
     * @param component The table component being rendered.
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    private void renderTitleStart(FacesContext context, Table component,
            ResponseWriter writer)throws IOException {
        writer.writeText("\n", null); //NOI18N
        writer.startElement("caption", component); //NOI18N
        writer.writeAttribute("id", getId(component, Table.TITLE_BAR_ID), //NOI18N
            null);
        writer.writeAttribute("class", //NOI18N
            getTheme().getStyleClass(ThemeStyles.TABLE_TITLE_TEXT), null);

        // Render extra HTML.
        if (component.getExtraTitleHtml() != null) {
            RenderingUtilities.renderExtraHtmlAttributes(writer, 
                component.getExtraTitleHtml());
        }
    }

    /**
     * Render title for Table components.
     *
     * @param context FacesContext for the current request.
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    private void renderTitleEnd(FacesContext context, ResponseWriter writer)
            throws IOException {
        writer.endElement("caption"); //NOI18N
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Enclosing tag methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Render enclosing tag for Table components.
     *
     * @param context FacesContext for the current request.
     * @param component Table to be rendered.
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    protected void renderEnclosingTagStart(FacesContext context,
            Table component, ResponseWriter writer) throws IOException {
        if (component == null) {
            log("renderEnclosingTagStart", //NOI18N
                "Cannot render enclosing tag, Table is null"); //NOI18N
            return;
        }

        Theme theme = getTheme();

        // Render div used to set style and class properties -- bugtraq #6316179.
        writer.writeText("\n", null); //NOI18N
        
        //<RAVE> - Removing the outer div put around table to meet SWADE guidelines        
        //writer.startElement("div", component); //NOI18N
        //writer.writeAttribute("id", component.getClientId(context), null); //NOI18N

        // Render style.
        //if (component.getStyle() != null) {
        //    writer.writeAttribute("style", component.getStyle(), null); //NOI18N
        //}

        // Render style class.
        //RenderingUtilities.renderStyleClass(context, writer, component, null);

        // Render div used to set width.
        //writer.writeText("\n", null); //NOI18N
        //writer.startElement("div", component); //NOI18N

        // Render width.
        //if (component.getWidth() != null) {
        //    String width = component.getWidth();

        //    // If not a percentage, units are in pixels.
        //    if (width.indexOf("%") == -1) { //NOI18N
        //        width += "px"; //NOI18N
        //    }
        //    writer.writeAttribute("style", "width:" + width, null); //NOI18N
        //} else {
        //    writer.writeAttribute("style", "width:100%", null); //NOI18N
        //}
        //</RAVE>

        // Render table.
        writer.writeText("\n", null); //NOI18N
        writer.startElement("table", component); //NOI18N
        //<RAVE>
        //writer.writeAttribute("id", getId(component, Table.TABLE_ID), null); //NOI18N
        writer.writeAttribute("id", component.getClientId(context), null); //NOI18N
        //</RAVE>

        //<RAVE> - Move the style and style class from outer div to table component
        // Render style.
        if (component.getStyle() != null) {
            String style = component.getStyle();
            
            if (component.getWidth() != null){ 
                String width = component.getWidth();

                // If not a percentage, units are in pixels.
                if (width.indexOf("%") == -1) { //NOI18N
                  width += "px"; //NOI18N
                }
                style = style + ";width: " + width;
            }else{
               style = style + ";width: 100%"; 
            }
            writer.writeAttribute("style", style, null); //NOI18N
        }

        
        // Get style class.
        String styleClass = theme.getStyleClass(ThemeStyles.TABLE);

        if(!component.isVisible()) {
            String hiddenStyle = theme.getStyleClass(ThemeStyles.HIDDEN); 
            if(styleClass == null) {
                styleClass = hiddenStyle;
            } else {
                styleClass = styleClass + " " + hiddenStyle; //NOI18N
            }
        }
        // </RAVE>
        if (component.isLite()) {
            styleClass += " " + theme.getStyleClass(ThemeStyles.TABLE_LITE); //NOI18N
        }
        
        if (component.getStyleClass() != null){
            styleClass += " "  + component.getStyleClass();
        }  
        //</RAVE> 

        // Render style class.
        writer.writeAttribute("class", styleClass, null); //NOI18N

         //<RAVE>
        // Render width. Note: 100 percent ensures consistent right margins.
        //writer.writeAttribute("width", "100%", null); //NOI18N
         //</RAVE>
        
        // Render border.
        if (component.getBorder() > -1) {
            writer.writeAttribute("border", //NOI18N
                Integer.toString(component.getBorder()), null); //NOI18N
        } else {
            writer.writeAttribute("border", "0", null); //NOI18N
        }

        // Render cellpadding.
        if (component.getCellPadding() != null) {
            writer.writeAttribute("cellpadding", component.getCellPadding(), null); //NOI18N
        } else {
            writer.writeAttribute("cellpadding", "0", null); //NOI18N
        }

        // Render cellspacing.
        if (component.getCellSpacing() != null) {
            writer.writeAttribute("cellspacing", component.getCellSpacing(), null); //NOI18N
        } else {
            writer.writeAttribute("cellspacing", "0", null); //NOI18N
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
     * Render enclosing tag for Table components.
     *
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    protected void renderEnclosingTagEnd(ResponseWriter writer)
            throws IOException {
        writer.endElement("table"); //NOI18N
        //<RAVE>
        //writer.endElement("div"); //NOI18N
        //writer.endElement("div"); //NOI18N
        //</RAVE>
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Private methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

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

    /**
     * Helper method to get the column ID and selectId from nested TableColumn 
     * components, used in Javascript functions (e.g., de/select all button
     * functionality).
     *
     * @param context FacesContext for the current request.
     * @param component TableColumn to be rendered.
     * @return The first selectId property found.
     */
    private String getSelectId(FacesContext context, TableColumn component) {
        String selectId = null;
        if (component == null) {
            log("getSelectId", "Cannot obtain select Id, TableColumn is null"); //NOI18N
            return selectId;
        }
    
        // Render nested TableColumn children.
        Iterator kids = component.getTableColumnChildren();
        if (kids.hasNext()) {
            while (kids.hasNext()) {
                TableColumn col = (TableColumn) kids.next();
                if (!col.isRendered()) {
                    continue;
                }
                selectId = getSelectId(context, col);
                if (selectId != null) {
                    break;
                }
            }
        } else {
            // Get selectId for possible nested TableColumn components.
            if (component.getSelectId() != null) {
                // Get TableRowGroup ancestor.
                TableRowGroup group = component.getTableRowGroupAncestor();
                if (group != null) {
                    // Get column and group id.
                    String colId = component.getClientId(context);
                    String groupId = group.getClientId(context) + 
                        NamingContainer.SEPARATOR_CHAR;
                    try {
                        selectId = colId.substring(groupId.length(), 
                            colId.length()) + NamingContainer.SEPARATOR_CHAR +
                            component.getSelectId();
                    } catch (IndexOutOfBoundsException e) {
                        // Do nothing.
                    }
                }
            }
        }
        return selectId;
    }

    /**
     * Helper method to get the sort menu option value for the select column.
     *
     * @param component Table to be rendered.
     *
     * @return The select option value.
     */
    private String getSelectSortMenuOptionValue(Table component) {
        TableRowGroup group = component.getTableRowGroupChild();

        // Get first select column found.
        if (group != null) {
            Iterator kids = group.getTableColumnChildren();
            while (kids.hasNext()) {
                TableColumn col = (TableColumn) kids.next();
                if (!col.isRendered() || col.getSelectId() == null) {
                    continue;
                }
                String value = getSelectSortMenuOptionValue(col);
                if (value != null) {
                    return value;
                }
            }
        } else {
            log("getSelectSortMenuOptionValue", //NOI18N
                "Cannot obtain select sort menu option value, TableRowGroup is null"); //NOI18N
        }
        return null;
    }

    /**
     * Helper method to get the sort menu option value for the select column.
     *
     * @param component TableColumn to be rendered.
     *
     * @return The select option value.
     */
    private String getSelectSortMenuOptionValue(TableColumn component) {
        Iterator kids = component.getTableColumnChildren();
        if (kids.hasNext()) {
            while (kids.hasNext()) {
                TableColumn col = (TableColumn) kids.next();
                if (!col.isRendered() || col.getSelectId() == null) {
                    continue;
                }
                String value = getSelectSortMenuOptionValue(col);
                if (value != null) {
                    return value;
                }
            }
        }

        // Return sort criteria key.
        SortCriteria criteria = component.getSortCriteria();
        return (criteria != null) ? criteria.getCriteriaKey() : null;
    }

    /**
     * Helper method to get Javascript array containing tool tips used for sort 
     * order menus.
     *
     * @param component Table to be rendered.
     * @param boolean Flag indicating descending tooltips.
     * @return A Javascript array containing tool tips.
     */
    private String getSortToolTipJavascript(Table component,
            boolean descending) {
        // Get undetermined tooltip.
        String tooltip = (descending)
            ? "table.sort.augment.undeterminedDescending" //NOI18N
            : "table.sort.augment.undeterminedAscending"; //NOI18N

        // Append array of ascending sort order tooltips.
        StringBuffer buff = new StringBuffer(1024);
        buff.append("new Array('") //NOI18N
            .append(getTheme().getMessage(tooltip))
            .append("'"); //NOI18N

        // Use the first TableRowGroup child to obtain sort tool tip.
        TableRowGroup group = component.getTableRowGroupChild();
        if (group != null) {
            // For each TableColumn component, get the sort tool tip augment
            // based on the value for the align property of TableColumn.
            Iterator kids = group.getTableColumnChildren();
            while (kids.hasNext()) {
                TableColumn col = (TableColumn) kids.next();
                if (!col.isRendered()) {
                    continue;
                }
                // Get tool tip augment.
                buff.append(",'") //NOI18N
                    .append(col.getSortToolTipAugment(descending))
                    .append("'"); //NOI18N
            }
        } else {
            log("getSortToolTipJavascript", //NOI18N
                "Cannot obtain Javascript array of sort tool tips, TableRowGroup is null"); //NOI18N
        }
        buff.append(")"); //NOI18N
        return buff.toString();
    }

    /**
     * Helper method to get table column footer style class for TableColumn 
     * components.
     *
     * @param component TableColumn to be rendered.
     * @param level The current sort level.
     * @return The style class for the table column footer.
     */
    private String getTableColumnFooterStyleClass(TableColumn component,
            int level) {
        String styleClass = null;

        // Get appropriate style class.
        if (component.isSpacerColumn()) {
            styleClass = ThemeStyles.TABLE_COL_FOOTER_SPACER;
        } else if (level == 1) {
            styleClass = ThemeStyles.TABLE_COL_FOOTER_SORT;
        } else {
            styleClass = ThemeStyles.TABLE_COL_FOOTER;
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

    /**
     * Helper method to render Javascript to Table components.
     *
     * @param context FacesContext for the current request.
     * @param component Table to be rendered.
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    private void renderJavascript(FacesContext context, Table component,
            ResponseWriter writer) throws IOException {
        if (component == null) {
            log("renderJavascript", "Cannot render Javascript, Table is null"); //NOI18N
            return;
        }

        // Include table Javascript file.
        writer.writeText("\n", null); //NOI18N
	RenderingUtilities.renderJsInclude(context, component, getTheme(), 
            writer, ThemeJavascript.TABLE);

        renderAssignFunctions(context, component, writer);
        renderAssignPanelProperties(context, component, writer);
        renderAssignFilterProperties(context, component, writer);
        renderAssignSortPanelProperties(context, component, writer);
        renderAssignGroupProperties(context, component, writer);
        renderAssignGroupPanelProperties(context, component, writer);
    }

    /**
     * Helper method to assign Javascript to Table components.
     *
     * @param context FacesContext for the current request.
     * @param component Table to be rendered.
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    private void renderAssignFunctions(FacesContext context, Table component,
            ResponseWriter writer) throws IOException {
        if (component == null) {
            log("renderAssignFunctions", //NOI18N
                "Cannot render assignFunctions Javascript function, Table is null"); //NOI18N
            return;
        }

        // Assign Javascript functions and properties.
        StringBuffer buff = new StringBuffer(1024);
        buff.append("sjwuic_table_assignFunctions('") //NOI18N
            .append(component.getClientId(context))
            .append("')"); //NOI18N

        writer.writeText("\n", null); //NOI18N
	writer.startElement("script", component); //NOI18N
	writer.writeAttribute("type", "text/javascript", null); //NOI18N
	writer.writeText(buff.toString(), null);
	writer.endElement("script"); //NOI18N
    }

    /**
     * Helper method to assign Javascript to Table components.
     *
     * @param context FacesContext for the current request.
     * @param component Table to be rendered.
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    private void renderAssignPanelProperties(FacesContext context, 
            Table component, ResponseWriter writer) throws IOException {
        if (component == null) {
            log("renderAssignPanelProperties", //NOI18N
                "Cannot render assignProperties Javascript function, Table is null"); //NOI18N
            return;
        }

        // Don't invoke component.getEmbeddedPanels() here because it will
        // create new component instances which do not work with the action
        // listeners assigned to the rendered components.
        UIComponent panels = component.getFacet(Table.EMBEDDED_PANELS_ID);
        if (panels == null) {
            log("renderAssignPanelProperties", //NOI18N
                "Cannot render assignProperties Javascript function, embedded panels facet is null"); //NOI18N
            return;
        }

        Theme theme = getTheme();
        String prefix = panels.getClientId(context) + NamingContainer.SEPARATOR_CHAR;

        // Assign Javascript functions and properties.
        StringBuffer buff = new StringBuffer(1024);
        buff.append("sjwuic_table_assignPanelProperties('") //NOI18N
            .append(component.getClientId(context))
            .append("'"); //NOI18N

        // Append array of panel Ids.
        buff.append(",new Array('") //NOI18N
            .append(prefix + TablePanels.SORT_PANEL_ID)
            .append("','") //NOI18N
            .append(prefix + TablePanels.PREFERENCES_PANEL_ID)
            .append("','") //NOI18N
            .append(prefix + TablePanels.FILTER_PANEL_ID)
            .append("')"); //NOI18N

        // Don't invoke component.getTableActionsTop() here because it will
        // create new component instances which do not work with the action
        // listeners assigned to the rendered components.
        UIComponent actions = component.getFacet(Table.TABLE_ACTIONS_TOP_ID);
        if (actions == null) {
            log("renderAssignPanelProperties", //NOI18N
                "Cannot render assignProperties Javascript function, actions top facet is null"); //NOI18N
            return;
        }

        // Append array of focus Ids.
        buff.append(",new Array('") //NOI18N
            .append((component.getSortPanelFocusId() != null)
                ? component.getSortPanelFocusId()
                : prefix + TablePanels.PRIMARY_SORT_COLUMN_MENU_ID)
            .append("',") //NOI18N
            .append((component.getPreferencesPanelFocusId() != null)
                ? "'" + component.getPreferencesPanelFocusId() + "'" //NOI18N
                : "null") //NOI18N
            .append(",") //NOI18N                
            .append((component.getFilterPanelFocusId() != null)
                ? "'" + component.getFilterPanelFocusId() + "'" //NOI18N
                : "null") //NOI18N
            .append(")"); //NOI18N

        prefix = actions.getClientId(context) + NamingContainer.SEPARATOR_CHAR;

        // Append array of panel toggle Ids.
        buff.append(",new Array('") //NOI18N
            .append(prefix + TableActions.SORT_PANEL_TOGGLE_BUTTON_ID)
            .append("','") //NOI18N
            .append(prefix + TableActions.PREFERENCES_PANEL_TOGGLE_BUTTON_ID)
            .append("','") //NOI18N
            .append((component.getFilterId() != null)
                ? component.getFilterId() : "") //NOI18N
            .append("')"); //NOI18N

        // Append array of toggle icons for open panels.
        buff.append(",new Array('") //NOI18N
            .append(theme.getIcon(ThemeImages.TABLE_SORT_PANEL_FLIP).getUrl())
            .append("','") //NOI18N
            .append(theme.getIcon(ThemeImages.TABLE_PREFERENCES_PANEL_FLIP).getUrl())
            .append("', null)"); //NOI18N

        // Append array of toggle icons for closed panels.
        buff.append(",new Array('") //NOI18N
            .append(theme.getIcon(ThemeImages.TABLE_SORT_PANEL).getUrl())
            .append("','") //NOI18N
            .append(theme.getIcon(ThemeImages.TABLE_PREFERENCES_PANEL).getUrl())
            .append("', null))"); //NOI18N

        writer.writeText("\n", null); //NOI18N
	writer.startElement("script", component); //NOI18N
	writer.writeAttribute("type", "text/javascript", null); //NOI18N
	writer.writeText(buff.toString(), null);
	writer.endElement("script"); //NOI18N
    }

    /**
     * Helper method to assign Javascript to Table components.
     *
     * @param context FacesContext for the current request.
     * @param component Table to be rendered.
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    private void renderAssignFilterProperties(FacesContext context, 
            Table component, ResponseWriter writer) throws IOException {
        if (component == null) {
            log("renderAssignFilterProperties", //NOI18N
                "Cannot render assignFilterProperties Javascript function, Table is null"); //NOI18N
            return;
        }

        Theme theme = getTheme();

        // Assign Javascript functions and properties.
        StringBuffer buff = new StringBuffer(1024);
        buff.append("sjwuic_table_assignFilterProperties('") //NOI18N
            .append(component.getClientId(context))
            .append("'"); //NOI18N

        // Append basic and custom filter menu style classes.
        buff.append(",'") //NOI18N
            .append(theme.getStyleClass(ThemeStyles.MENU_JUMP))
            .append("','") //NOI18N
            .append(theme.getStyleClass(ThemeStyles.TABLE_CUSTOM_FILTER_MENU))
            .append("'"); //NOI18N

        // Custom filter options.
        buff.append(",'") //NOI18N
            .append(Table.CUSTOM_FILTER)
            .append("','") //NOI18N
            .append(Table.CUSTOM_FILTER_APPLIED)
            .append("')"); //NOI18N

        writer.writeText("\n", null); //NOI18N
	writer.startElement("script", component); //NOI18N
	writer.writeAttribute("type", "text/javascript", null); //NOI18N
	writer.writeText(buff.toString(), null);
	writer.endElement("script"); //NOI18N
    }

    /**
     * Helper method to assign Javascript to Table components.
     *
     * @param context FacesContext for the current request.
     * @param component Table to be rendered.
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    private void renderAssignGroupProperties(FacesContext context, 
            Table component, ResponseWriter writer) throws IOException {
        if (component == null) {
            log("renderAssignGroupProperties", //NOI18N
                "Cannot render assignGroupProperties Javascript function, Table is null"); //NOI18N
            return;
        }

        Theme theme = getTheme();

        // Assign Javascript functions and properties.
        StringBuffer buff = new StringBuffer(1024);
        buff.append("sjwuic_table_assignGroupProperties('") //NOI18N
            .append(component.getClientId(context))
            .append("'"); //NOI18N

        // Append select row style class.
        buff.append(",'") //NOI18N
            .append(theme.getStyleClass(ThemeStyles.TABLE_SELECT_ROW))
            .append("'"); //NOI18N

        // Append array of select IDs.
        buff.append(",new Array("); //NOI18N
        Iterator kids = component.getTableRowGroupChildren();
        while (kids.hasNext()) {
            TableRowGroup group = (TableRowGroup) kids.next();
            
            // Iterate over each TableColumn chlid to find selectId.
            String selectId = null;
            Iterator grandkids = group.getTableColumnChildren();
            while (grandkids.hasNext()) {
                TableColumn col = (TableColumn) grandkids.next();
                if (!col.isRendered()) {
                    continue;
                }
                selectId = getSelectId(context, col);
                if (selectId != null) {
                    break;
                }
            }

            // Append selectId, if applicable.
            buff.append("'") //NOI18N
                .append((selectId != null) ? selectId : "") //NOI18N
                .append("'"); //NOI18N

            // Append separator for next id.
            if (kids.hasNext()) {
                buff.append(","); //NOI18N
            }
        }
        buff.append(")"); //NOI18N

        // Append array of TableRowGroup IDs.
        buff.append(",new Array("); //NOI18N
        kids = component.getTableRowGroupChildren();
        while (kids.hasNext()) {
            TableRowGroup group = (TableRowGroup) kids.next();
            buff.append("'") //NOI18N
                .append(group.getClientId(context))
                .append("'"); //NOI18N

            // Append separator for next id.
            if (kids.hasNext()) {
                buff.append(","); //NOI18N
            }
        }
        buff.append(")"); //NOI18N

        // Append array of row IDs.
        buff.append(",new Array("); //NOI18N
        kids = component.getTableRowGroupChildren();
        while (kids.hasNext()) {
            TableRowGroup group = (TableRowGroup) kids.next();
            RowKey[] rowKeys = group.getRenderedRowKeys(); // Only rendered rows.

            // Append an array of row ids for each TableRowGroup child.
            if (rowKeys != null) {
                buff.append("new Array("); //NOI18N
                for (int i = 0; i < rowKeys.length; i++) {
                    buff.append((i > 0) ? ",'" : "'") //NOI18N
                        .append(rowKeys[i].getRowId())
                        .append("'"); //NOI18N
                }
                buff.append(")"); //NOI18N
            } else {
                buff.append("null"); // TableRowGroup may have been empty. //NOI18N
            }
    
            // Append separator for next array.
            if (kids.hasNext()) {
                buff.append(","); //NOI18N
            }
        }
        buff.append(")"); //NOI18N

        // Append array of hidden selected row counts.
        buff.append(",new Array("); //NOI18N
        kids = component.getTableRowGroupChildren();
        while (kids.hasNext()) {
            TableRowGroup group = (TableRowGroup) kids.next();

            // Don't bother with calculations if this property is not set.
            if (component.isHiddenSelectedRows()) {
                buff.append("'") //NOI18N
                    .append(Integer.toString(group.getHiddenSelectedRowsCount()))
                    .append("'"); //NOI18N
            } else {
                // Note: Use chars; otherwise, a zero length array is created.
                buff.append("'0'"); //NOI18N
            }

            // Append separator for next array.
            if (kids.hasNext()) {
                buff.append(","); //NOI18N
            }
        }
        buff.append(")"); //NOI18N

        // Append confirm delete messages.
        buff.append(",'") //NOI18N
            .append(theme.getMessage("table.confirm.hiddenSelections")) //NOI18N
            .append("','") //NOI18N
            .append(theme.getMessage("table.confirm.totalSelections")) //NOI18N
            .append("','") //NOI18N
            .append(theme.getMessage("table.confirm.deleteSelections")) //NOI18N
            .append("')"); //NOI18N

        writer.writeText("\n", null); //NOI18N
	writer.startElement("script", component); //NOI18N
	writer.writeAttribute("type", "text/javascript", null); //NOI18N
	writer.writeText(buff.toString(), null);
	writer.endElement("script"); //NOI18N
    }

    /**
     * Helper method to assign Javascript to Table components.
     *
     * @param context FacesContext for the current request.
     * @param component Table to be rendered.
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    private void renderAssignGroupPanelProperties(FacesContext context, 
            Table component, ResponseWriter writer) throws IOException {
        if (component == null) {
            log("renderAssignGroupPanelProperties", //NOI18N
                "Cannot render assignGroupPanelProperties Javascript function, Table is null"); //NOI18N
            return;
        }

        Theme theme = getTheme();

        // Assign Javascript functions and properties.
        StringBuffer buff = new StringBuffer(1024);
        buff.append("sjwuic_table_assignGroupPanelProperties('") //NOI18N
            .append(component.getClientId(context))
            .append("'"); //NOI18N

        // Append bar IDs.
        buff.append(",'") //NOI18N
            .append(TableRowGroup.COLUMN_FOOTER_BAR_ID)
            .append("','") //NOI18N
            .append(TableRowGroup.COLUMN_HEADER_BAR_ID)
            .append("','") //NOI18N
            .append(TableRowGroup.TABLE_COLUMN_FOOTER_BAR_ID)
            .append("','") //NOI18N
            .append(TableRowGroup.GROUP_FOOTER_BAR_ID)
            .append("'"); //NOI18N

        // Get ID prefix for TableHeader components.
        String prefix = TableRowGroup.GROUP_HEADER_ID +
            NamingContainer.SEPARATOR_CHAR;               

        // Append row group toggle button properties.
        buff.append(",'") //NOI18N
            .append(prefix + TableHeader.GROUP_PANEL_TOGGLE_BUTTON_ID)
            .append("','") //NOI18N
            .append(theme.getMessage("table.group.collapse")) //NOI18N
            .append("','") //NOI18N
            .append(theme.getMessage("table.group.expand")) //NOI18N
            .append("','") //NOI18N
            .append(theme.getIcon(ThemeImages.TABLE_GROUP_PANEL_FLIP).getUrl())
            .append("','") //NOI18N
            .append(theme.getIcon(ThemeImages.TABLE_GROUP_PANEL).getUrl())
            .append("'"); //NOI18N

        // Append warning icon properties.
        buff.append(",'") //NOI18N
            .append(prefix + TableHeader.WARNING_ICON_ID)
            .append("','") //NOI18N
            .append(getTheme().getIcon(ThemeImages.DOT).getUrl())
            .append("','") //NOI18N
            .append(getTheme().getIcon(ThemeImages.ALERT_WARNING_SMALL).getUrl())
            .append("',") //NOI18N
            .append("null") //NOI18N -- No tooltip for place holder icon.
            .append(",'") //NOI18N
            .append(theme.getMessage("table.group.warning")) //NOI18N
            .append("'"); //NOI18N

        // Append collapsed hidden field properties.
        buff.append(",'") //NOI18N
            .append(prefix + TableHeader.COLLAPSED_HIDDEN_FIELD_ID)
            .append("'"); //NOI18N

        // Append select multiple toggle button properties.
        buff.append(",'") //NOI18N
            .append(prefix + TableHeader.SELECT_MULTIPLE_TOGGLE_BUTTON_ID)
            .append("','") //NOI18N
            .append(theme.getMessage("table.group.selectMultiple")) //NOI18N
            .append("','") //NOI18N
            .append(theme.getMessage("table.group.deselectMultiple")) //NOI18N
            .append("')"); //NOI18N

        writer.writeText("\n", null); //NOI18N
	writer.startElement("script", component); //NOI18N
	writer.writeAttribute("type", "text/javascript", null); //NOI18N
	writer.writeText(buff.toString(), null);
	writer.endElement("script"); //NOI18N
    }

    /**
     * Helper method to assign Javascript to Table components.
     *
     * @param context FacesContext for the current request.
     * @param component Table to be rendered.
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    private void renderAssignSortPanelProperties(FacesContext context, 
            Table component, ResponseWriter writer) throws IOException {
        if (component == null) {
            log("renderAssignSortPanelProperties", //NOI18N
                "Cannot render assignSortPanelProperties Javascript function, Table is null"); //NOI18N
            return;
        }

        // Don't invoke component.getEmbeddedPanels() here because it will
        // create new component instances which do not work with the action
        // listeners assigned to the rendered components.
        UIComponent panels = component.getFacet(Table.EMBEDDED_PANELS_ID);
        if (panels == null) {
            log("renderAssignSortPanelProperties", //NOI18N
                "Cannot render assignSortPanelProperties Javascript function, Embedded panels facet is null"); //NOI18N
            return;
        }

        Theme theme = getTheme();
        String prefix = panels.getClientId(context) + NamingContainer.SEPARATOR_CHAR;

        // Assign Javascript functions and properties.
        StringBuffer buff = new StringBuffer(1024);
        buff.append("sjwuic_table_assignSortPanelProperties('") //NOI18N
            .append(component.getClientId(context))
            .append("'"); //NOI18N

        // Append array of sort column menu Ids.
        buff.append(",new Array('") //NOI18N
            .append(prefix + TablePanels.PRIMARY_SORT_COLUMN_MENU_ID)
            .append("','") //NOI18N
            .append(prefix + TablePanels.SECONDARY_SORT_COLUMN_MENU_ID)
            .append("','") //NOI18N
            .append(prefix + TablePanels.TERTIARY_SORT_COLUMN_MENU_ID)
            .append("')"); //NOI18N

        // Append array of sort order menu Ids.
        buff.append(",new Array('") //NOI18N
            .append(prefix + TablePanels.PRIMARY_SORT_ORDER_MENU_ID)
            .append("','") //NOI18N
            .append(prefix + TablePanels.SECONDARY_SORT_ORDER_MENU_ID)
            .append("','") //NOI18N
            .append(prefix + TablePanels.TERTIARY_SORT_ORDER_MENU_ID)
            .append("')"); //NOI18N

        // Append array of ascending sort order tooltips.
        buff.append(",") //NOI18N
            .append(getSortToolTipJavascript(component, false));

        // Append array of descending sort order tooltips.
        buff.append(",") //NOI18N
            .append(getSortToolTipJavascript(component, true));

        // Append error messages.
        buff.append(",'") //NOI18N
            .append(theme.getMessage(
                "table.panel.duplicateSelectionError")) //NOI18N
            .append("','") //NOI18N
            .append(theme.getMessage(
                "table.panel.missingSelectionError")) //NOI18N
            .append("'"); //NOI18N

        // Append sort menu option value for select column and paginated flag.
        String value = getSelectSortMenuOptionValue(component);
        TableRowGroup group = component.getTableRowGroupChild();
        buff.append(",'") //NOI18N
            .append(value != null ? value : "null") //NOI18N
            .append("',") //NOI18N
            .append(component.isHiddenSelectedRows())
            .append(",") //NOI18N
            .append(group != null ? Boolean.toString(group.isPaginated()) : "false") //NOI18N
            .append(")"); //NOI18N

        writer.writeText("\n", null); //NOI18N
	writer.startElement("script", component); //NOI18N
	writer.writeAttribute("type", "text/javascript", null); //NOI18N
	writer.writeText(buff.toString(), null);
	writer.endElement("script"); //NOI18N
    }
}
