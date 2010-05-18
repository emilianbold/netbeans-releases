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

import com.sun.rave.web.ui.component.Table;
import com.sun.rave.web.ui.component.TableActions;
import com.sun.rave.web.ui.component.TableRowGroup;
import com.sun.rave.web.ui.theme.Theme;
import com.sun.rave.web.ui.theme.ThemeStyles;
import com.sun.rave.web.ui.util.LogUtil;
import com.sun.rave.web.ui.util.RenderingUtilities;
import com.sun.rave.web.ui.util.ThemeUtilities;

import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;

/**
 * This class renders TableActions components.
 * <p>
 * Note: To see the messages logged by this class, set the following global
 * defaults in your JDK's "jre/lib/logging.properties" file.
 * </p><p><pre>
 * java.util.logging.ConsoleHandler.level = FINE
 * com.sun.rave.web.ui.renderer.TableActionsRenderer.level = FINE
 * </pre></p>
 */
public class TableActionsRenderer extends Renderer {
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

        TableActions action = (TableActions) component;
        ResponseWriter writer = context.getResponseWriter();
        renderEnclosingTagStart(context, action, writer);
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

        TableActions action = (TableActions) component;
        ResponseWriter writer = context.getResponseWriter();

        // Render actions.
        if (action.isActionsBottom()) {
            renderActionsBottom(context, action, writer);
        } else {
            renderActionsTop(context, action, writer);
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

        TableActions action = (TableActions) component;
        ResponseWriter writer = context.getResponseWriter();
        renderEnclosingTagEnd(context, action, writer);
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
    // Action methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Render the top actions for TableActions components.
     *
     * @param context FacesContext for the current request.
     * @param component TableActions to be rendered.
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    protected void renderActionsTop(FacesContext context,
            TableActions component, ResponseWriter writer) throws IOException {
        Table table = (component != null) ? component.getTableAncestor() : null;
        if (table == null) {
            log("renderActionsTop", "Cannot render actions bar, Table is null"); //NOI18N
            return;
        }

        // We must determine if all TableRowGroup components are empty. Controls
        // are only hidden when all row groups are empty. Likewise, pagination
        // controls are only hidden when all groups fit on a single page.
        int totalRows = table.getRowCount();
        boolean emptyTable = (totalRows == 0);
        boolean singleRow = (totalRows == 1);
        boolean singlePage = (totalRows < table.getRows());

        // Get facets.
        UIComponent actions = table.getFacet(Table.ACTIONS_TOP_FACET);
        UIComponent filter = table.getFacet(Table.FILTER_FACET);
        UIComponent sort = table.getFacet(Table.SORT_PANEL_FACET);
        UIComponent prefs = table.getFacet(Table.PREFERENCES_PANEL_FACET);

        // Flags indicating which facets to render.
        boolean renderActions = actions != null && actions.isRendered();
        boolean renderFilter = filter != null && filter.isRendered();
        boolean renderSort = sort != null && sort.isRendered();
        boolean renderPrefs = prefs != null && prefs.isRendered();

        // Hide sorting and pagination controls for an empty table or when there
        // is only a single row.
        boolean renderSelectMultipleButton = !emptyTable
            && table.isSelectMultipleButton();
        boolean renderDeselectMultipleButton = !emptyTable
            && table.isDeselectMultipleButton();
        boolean renderDeselectSingleButton = !emptyTable
            && table.isDeselectSingleButton();
        boolean renderClearTableSortButton = !emptyTable && !singleRow
            && table.isClearSortButton();
        boolean renderTableSortPanelToggleButton = !emptyTable && !singleRow
            && (table.isSortPanelToggleButton() || renderSort);
        boolean renderPaginateButton = !emptyTable && !singlePage
            && table.isPaginateButton();

        // Return if nothing is rendered.
        if (!(renderActions || renderFilter || renderPrefs
                || renderSelectMultipleButton
                || renderDeselectMultipleButton
                || renderDeselectSingleButton
                || renderClearTableSortButton
                || renderTableSortPanelToggleButton
                || renderPaginateButton)) {
            log("renderActionsTop", //NOI18N
                "Actions bar not rendered, nothing to display"); //NOI18N
            return;
        }

        // Render select multiple button.
        if (renderSelectMultipleButton) {
            writer.writeText("\n", null); //NOI18N
            RenderingUtilities.renderComponent(
                component.getSelectMultipleButton(), context);
        }

        // Render deselect multiple button.
        if (renderDeselectMultipleButton) {
            writer.writeText("\n", null); //NOI18N
            RenderingUtilities.renderComponent(
                component.getDeselectMultipleButton(), context);
        }

        // Render deselect single button.
        if (renderDeselectSingleButton) {
            writer.writeText("\n", null); //NOI18N
            RenderingUtilities.renderComponent(
                component.getDeselectSingleButton(), context);
        }

        // Render actions facet.
        if (renderActions) {
            // Render action separator.
            if (renderSelectMultipleButton 
                    || renderDeselectMultipleButton 
                    || renderDeselectSingleButton) {
                writer.writeText("\n", null); //NOI18N
                RenderingUtilities.renderComponent(
                    component.getActionsSeparatorIcon(), context);
            }
            writer.writeText("\n", null); //NOI18N
            RenderingUtilities.renderComponent(actions, context);
        }

        // Render filter facet.
        if (renderFilter) {
            // Render filter separator.
            if (renderActions
                    || renderSelectMultipleButton
                    || renderDeselectMultipleButton 
                    || renderDeselectSingleButton) {
                writer.writeText("\n", null); //NOI18N
                RenderingUtilities.renderComponent(
                    component.getFilterSeparatorIcon(), context);
            }

            writer.writeText("\n", null); //NOI18N
            RenderingUtilities.renderComponent(component.getFilterLabel(),
                context);
            writer.writeText("\n", null); //NOI18N
            RenderingUtilities.renderComponent(filter, context);
        }

        // Render view action separator.
        if ((renderActions || renderFilter
                || renderSelectMultipleButton
                || renderDeselectMultipleButton 
                || renderDeselectSingleButton)
            && (renderPrefs
                || renderClearTableSortButton
                || renderTableSortPanelToggleButton)){
            writer.writeText("\n", null); //NOI18N
            RenderingUtilities.renderComponent(
                component.getViewActionsSeparatorIcon(), context);
        }

        // Render table sort panel toggle button.
        if (renderTableSortPanelToggleButton){
            writer.writeText("\n", null); //NOI18N
            UIComponent child = component.getSortPanelToggleButton();
            RenderingUtilities.renderComponent(child, context);
        }

        // Render clear sort button.
        if (renderClearTableSortButton) {
            writer.writeText("\n", null); //NOI18N
            RenderingUtilities.renderComponent(
                component.getClearSortButton(), context);
        }

        // Render table preferences panel toggle button.
        if (renderPrefs) {
            writer.writeText("\n", null); //NOI18N
            RenderingUtilities.renderComponent(
                component.getPreferencesPanelToggleButton(), context);
        }

        // Render paginate button.
        if (renderPaginateButton) {
            // Render separator.
            if (renderActions || renderFilter || renderPrefs
                    || renderSelectMultipleButton
                    || renderDeselectMultipleButton
                    || renderDeselectSingleButton
                    || renderClearTableSortButton
                    || renderTableSortPanelToggleButton) {
                writer.writeText("\n", null); //NOI18N
                RenderingUtilities.renderComponent(
                    component.getPaginateSeparatorIcon(), context);
            }
            writer.writeText("\n", null); //NOI18N
            RenderingUtilities.renderComponent(component.getPaginateButton(),
                context);
        }
    }

    /**
     * Render the bottom actions for TableActions components.
     *
     * @param context FacesContext for the current request.
     * @param component TableActions to be rendered.
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    protected void renderActionsBottom(FacesContext context,
            TableActions component, ResponseWriter writer) throws IOException {
        Table table = (component != null) ? component.getTableAncestor() : null;
        if (table == null) {
            log("renderActionsBottom", //NOI18N
                "Cannot render actions bar, Table is null"); //NOI18N
            return;
        }

        // We must determine if all TableRowGroup components are empty. Controls
        // are only hidden when all row groups are empty. Likewise, pagination
        // controls are only hidden when all groups fit on a single page.
        int totalRows = table.getRowCount();
        boolean emptyTable = (totalRows == 0);
        boolean singleRow = (totalRows == 1);
        boolean singlePage = (totalRows < table.getRows());

        // Get facets.
        UIComponent actions = table.getFacet(Table.ACTIONS_BOTTOM_FACET);
        
        // Get flag indicating which facets to render.
        boolean renderActions = !emptyTable && !singleRow
            && actions != null && actions.isRendered();

        // Hide pagination controls when all rows fit on a page.
        boolean renderPaginationControls = !emptyTable && !singlePage
            && table.isPaginationControls();

        // Hide paginate button for a single row.
        boolean renderPaginateButton = !emptyTable && !singlePage
            && table.isPaginateButton();

        // Render table actions facet.
        if (renderActions) {
            writer.writeText("\n", null); //NOI18N
            RenderingUtilities.renderComponent(actions, context);
        }

        // Render actions separator.
        if (renderPaginationControls || renderPaginateButton) {
            // Render actions separator.
            if (renderActions) {
                writer.writeText("\n", null); //NOI18N
                RenderingUtilities.renderComponent(
                    component.getActionsSeparatorIcon(), context);
            }
        }

        // Render pagination controls.
        if (renderPaginationControls) {
            // Get TableRowGroup component.
            TableRowGroup group = table.getTableRowGroupChild();
            boolean paginated = (group != null) ? group.isPaginated() : false;
        
            // Do not display controls while in scroll mode.
            if (paginated) {
                renderPagination(context, component, writer);
            }
        }
        
        // Render paginate button.
        if (renderPaginateButton) {
            writer.writeText("\n", null); //NOI18N
            RenderingUtilities.renderComponent(
                component.getPaginateButton(), context);
        }
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Enclosing tag methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Render enclosing tag for TableActions components.
     *
     * @param context FacesContext for the current request.
     * @param component TableActions to be rendered.
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    protected void renderEnclosingTagStart(FacesContext context,
            TableActions component, ResponseWriter writer) throws IOException {
        if (component == null) {
            log("renderEnclosingTagStart", //NOI18N
                "Cannot render enclosing tag, TableActions is null"); //NOI18N
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
     * Render enclosing tag for TableActions components.
     *
     * @param context FacesContext for the current request.
     * @param component TableActions to be rendered.
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    protected void renderEnclosingTagEnd(FacesContext context,
            TableActions component, ResponseWriter writer) throws IOException {
        if (component == null) {
            log("renderEnclosingTagEnd", //NOI18N
                "Cannot render enclosing tag, TableActions is null"); //NOI18N
            return;
        }
        writer.endElement("td"); //NOI18N
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Private methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Helper method to get style class for TableActions components.
     *
     * @param component TableActions to be rendered
     * @return The style class.
     */
    private String getStyleClass(TableActions component) {
        String styleClass = null;
        if (component == null) {
            log("getStyleClass", //NOI18N
                "Cannot obtain style class, TableActions is null"); //NOI18N
            return styleClass;
        }

        // Get style class.
        if (component.isActionsBottom()) {
            styleClass = ThemeStyles.TABLE_ACTION_TD_LASTROW;
        } else {
            styleClass = ThemeStyles.TABLE_ACTION_TD;
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
     * Render the pagination controls for TableActions components. This does
     * not include the paginate button.
     *
     * @param context FacesContext for the current request.
     * @param component TableActions to be rendered.
     * @param writer ResponseWriter to which the component should be rendered.
     *
     * @exception IOException if an input/output error occurs.
     */
    private void renderPagination(FacesContext context, TableActions component,
            ResponseWriter writer) throws IOException {
        if (component == null) {
            log("renderPagination", //NOI18N
                "Cannot render pagination controls, TableActions is null"); //NOI18N
            return;
        }

        Theme theme = getTheme();

        // Render span for left-side buttons.
        writer.writeText("\n", null); //NOI18N
        writer.startElement("span", component); //NOI18N
        writer.writeAttribute("class", theme.getStyleClass( //NOI18N
            ThemeStyles.TABLE_PAGINATION_LEFT_BUTTON), null);

        // Render first button.
        writer.writeText("\n", null); //NOI18N
        RenderingUtilities.renderComponent(
            component.getPaginationFirstButton(), context);

        // Render prev button.
        writer.writeText("\n", null); //NOI18N
        RenderingUtilities.renderComponent(
            component.getPaginationPrevButton(), context);
        writer.endElement("span"); //NOI18N

        // Render span for label.
        writer.writeText("\n", null); //NOI18N
        writer.startElement("span", component); //NOI18N
        writer.writeAttribute("class", theme.getStyleClass(  //NOI18N
            ThemeStyles.TABLE_PAGINATION_TEXT_BOLD), null);

        // Render page field.
        writer.writeText("\n", null); //NOI18N
        RenderingUtilities.renderComponent(component.getPaginationPageField(),
            context);
        writer.endElement("span"); //NOI18N

        // Render total pages text.
        writer.writeText("\n", null); //NOI18N
        RenderingUtilities.renderComponent(component.getPaginationPagesText(),
            context);

        // Render span for submit button.
        writer.writeText("\n", null); //NOI18N
        writer.startElement("span", component); //NOI18N
        writer.writeAttribute("class", theme.getStyleClass( //NOI18N
            ThemeStyles.TABLE_PAGINATION_SUBMIT_BUTTON), null); //NOI18N

        // Render submit button.
        writer.writeText("\n", null); //NOI18N
        RenderingUtilities.renderComponent(
            component.getPaginationSubmitButton(), context);
        writer.endElement("span"); //NOI18N

        // Render span for right-side buttons.
        writer.writeText("\n", null); //NOI18N
        writer.startElement("span", component); //NOI18N
        writer.writeAttribute("class", theme.getStyleClass( //NOI18N
            ThemeStyles.TABLE_PAGINATION_RIGHT_BUTTON), null);

        // Render next button.
        writer.writeText("\n", null); //NOI18N
        RenderingUtilities.renderComponent(
            component.getPaginationNextButton(), context);

        // Render last button.
        writer.writeText("\n", null); //NOI18N
        RenderingUtilities.renderComponent(
            component.getPaginationLastButton(), context);
        writer.endElement("span"); //NOI18N
    }
}
