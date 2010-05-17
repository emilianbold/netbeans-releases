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

import com.sun.rave.web.ui.event.TablePaginationActionListener;
import com.sun.rave.web.ui.event.TableSortActionListener;
import com.sun.rave.web.ui.theme.Theme;
import com.sun.rave.web.ui.theme.ThemeImages;
import com.sun.rave.web.ui.theme.ThemeStyles;
import com.sun.rave.web.ui.util.ClientSniffer;
import com.sun.rave.web.ui.util.LogUtil;
import com.sun.rave.web.ui.util.RenderingUtilities;
import com.sun.rave.web.ui.util.ThemeUtilities;

import java.io.IOException;

import javax.faces.context.FacesContext;
import javax.faces.component.NamingContainer;
import javax.faces.component.UIComponent;

/**
 * Component that represents a table action bar.
 * <p>
 * Note: Column headers and footers are rendered by TableRowGroupRenderer. Table
 * column footers are rendered by TableRenderer.
 * </p><p>
 * Note: To see the messages logged by this class, set the following global
 * defaults in your JDK's "jre/lib/logging.properties" file.
 * </p><p><pre>
 * java.util.logging.ConsoleHandler.level = FINE
 * com.sun.rave.web.ui.component.TableActions.level = FINE
 * </pre></p>
 */
public class TableActions extends TableActionsBase implements NamingContainer {
    /** The component id for the actions separator icon. */
    public static final String ACTIONS_SEPARATOR_ICON_ID = "_actionsSeparatorIcon"; //NOI18N

    /** The facet name for the actions separator icon. */
    public static final String ACTIONS_SEPARATOR_ICON_FACET = "actionsSeparatorIcon"; //NOI18N

    /** The component id for the clear sort button. */
    public static final String CLEAR_SORT_BUTTON_ID = "_clearSortButton"; //NOI18N

    /** The facet name for the clear sort button. */
    public static final String CLEAR_SORT_BUTTON_FACET = "clearSortButton"; //NOI18N

    /** The component id for the deselect multiple button. */
    public static final String DESELECT_MULTIPLE_BUTTON_ID = "_deselectMultipleButton"; //NOI18N

    /** The facet name for the deselect multiple button. */
    public static final String DESELECT_MULTIPLE_BUTTON_FACET = "deselectMultipleButton"; //NOI18N

    /** The component id for the deselect single button. */
    public static final String DESELECT_SINGLE_BUTTON_ID = "_deselectSingleButton"; //NOI18N

    /** The facet name for the deselect single button. */
    public static final String DESELECT_SINGLE_BUTTON_FACET = "deselectSingleButton"; //NOI18N

    /** The component id for the filter label. */
    public static final String FILTER_LABEL_ID = "_filterLabel"; //NOI18N

    /** The facet name for the filter label. */
    public static final String FILTER_LABEL_FACET = "filterLabel"; //NOI18N

    /** The component id for the filter separator icon. */
    public static final String FILTER_SEPARATOR_ICON_ID = "_filterSeparatorIcon"; //NOI18N

    /** The facet name for the filter separator icon. */
    public static final String FILTER_SEPARATOR_ICON_FACET = "filterSeparatorIcon"; //NOI18N

    /** The component id for the paginate button. */
    public static final String PAGINATE_BUTTON_ID = "_paginateButton"; //NOI18N

    /** The facet name for the paginate button. */
    public static final String PAGINATE_BUTTON_FACET = "paginateButton"; //NOI18N

    /** The component id for the paginate separator icon. */
    public static final String PAGINATE_SEPARATOR_ICON_ID = "_paginateSeparatorIcon"; //NOI18N

    /** The facet name for the paginate separator icon. */
    public static final String PAGINATE_SEPARATOR_ICON_FACET = "paginateSeparatorIcon"; //NOI18N

    /** The component id for the pagination first button. */
    public static final String PAGINATION_FIRST_BUTTON_ID = "_paginationFirstButton"; //NOI18N

    /** The facet name for the pagination first button. */
    public static final String PAGINATION_FIRST_BUTTON_FACET = "paginationFirstButton"; //NOI18N

    /** The component id for the pagination last button. */
    public static final String PAGINATION_LAST_BUTTON_ID = "_paginationLastButton"; //NOI18N

    /** The facet name for the pagination last button. */
    public static final String PAGINATION_LAST_BUTTON_FACET = "paginationLastButton"; //NOI18N

    /** The component id for the pagination next button. */
    public static final String PAGINATION_NEXT_BUTTON_ID = "_paginationNextButton"; //NOI18N

    /** The facet name for the pagination next button. */
    public static final String PAGINATION_NEXT_BUTTON_FACET = "paginationNextButton"; //NOI18N

    /** The component id for the pagination page field. */
    public static final String PAGINATION_PAGE_FIELD_ID = "_paginationPageField"; //NOI18N

    /** The facet name for the pagination page field. */
    public static final String PAGINATION_PAGE_FIELD_FACET = "paginationPageField"; //NOI18N

    /** The component id for the pagination pages text. */
    public static final String PAGINATION_PAGES_TEXT_ID = "_paginationPagesText"; //NOI18N

    /** The facet name for the pagination pages text. */
    public static final String PAGINATION_PAGES_TEXT_FACET = "paginationPagesText"; //NOI18N

    /** The component id for the pagination previous button. */
    public static final String PAGINATION_PREV_BUTTON_ID = "_paginationPrevButton"; //NOI18N

    /** The facet name for the pagination previous button. */
    public static final String PAGINATION_PREV_BUTTON_FACET = "paginationPrevButton"; //NOI18N

    /** The component id for the pagination submit button. */
    public static final String PAGINATION_SUBMIT_BUTTON_ID = "_paginationSubmitButton"; //NOI18N

    /** The facet name for the pagination submit button. */
    public static final String PAGINATION_SUBMIT_BUTTON_FACET = "paginationSubmitButton"; //NOI18N

    /** The component id for the preferences panel button. */
    public static final String PREFERENCES_PANEL_TOGGLE_BUTTON_ID = "_preferencesPanelToggleButton"; //NOI18N

    /** The facet name for the preferences panel button. */
    public static final String PREFERENCES_PANEL_TOGGLE_BUTTON_FACET = "preferencesPanelToggleButton"; //NOI18N

    /** The component id for the select multiple button. */
    public static final String SELECT_MULTIPLE_BUTTON_ID = "_selectMultipleButton"; //NOI18N

    /** The facet name for the select multiple button. */
    public static final String SELECT_MULTIPLE_BUTTON_FACET = "selectMultipleButton"; //NOI18N

    /** The component id for the sort panel toggle button. */
    public static final String SORT_PANEL_TOGGLE_BUTTON_ID = "_sortPanelToggleButton"; //NOI18N

    /** The facet name for the sort panel toggle button. */
    public static final String SORT_PANEL_TOGGLE_BUTTON_FACET = "sortPanelToggleButton"; //NOI18N

    /** The component id for the view actions separator icon. */
    public static final String VIEW_ACTIONS_SEPARATOR_ICON_ID = "_viewActionsSeparatorIcon"; //NOI18N

    /** The facet name for the view actions separator icon. */
    public static final String VIEW_ACTIONS_SEPARATOR_ICON_FACET = "viewActionsSeparatorIcon"; //NOI18N

    // The Table ancestor enclosing this component.
    private Table table = null;

    /** Default constructor */
    public TableActions() {
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

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Pagination methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Get first page button for pagination controls.
     *
     * @return The first page button.
     */
    public UIComponent getPaginationFirstButton() {
        UIComponent facet = getFacet(PAGINATION_FIRST_BUTTON_FACET);
        if (facet != null) {
            return facet;
        }

        // Get disabled state.
        Table table = getTableAncestor();
        TableRowGroup group = (table != null)
            ? table.getTableRowGroupChild() : null;
        boolean disabled = (group != null) ? group.getFirst() <= 0 : false;

        // Get child.
        IconHyperlink child = new IconHyperlink();
	child.setId(PAGINATION_FIRST_BUTTON_ID);
        child.setIcon(disabled
            ? ThemeImages.TABLE_PAGINATION_FIRST_DISABLED
            : ThemeImages.TABLE_PAGINATION_FIRST);
        child.setBorder(0);
        child.setAlign("top"); //NOI18N
        child.setDisabled(disabled);
        child.addActionListener(new TablePaginationActionListener());

        // Set tool tip.
        String toolTip = getTheme().getMessage("table.pagination.first"); //NOI18N
        child.setAlt(toolTip);
        child.setToolTip(toolTip);

        // Set tab index.
        if (table != null) {
            child.setTabIndex(table.getTabIndex());
        } else {
            log("getPaginationFirstButton", "Tab index not set, Table is null"); //NOI18N
        }

        // Set focus when paginaton buttons are disabled -- bugtraq #6316565.
        setPaginationFocus(child);

        // Save facet and return child.
        getFacets().put(child.getId(), child);
        return child;
    }

    /**
     * Get pagination submit button for pagination controls.
     *
     * @return The pagination submit button.
     */
    public UIComponent getPaginationSubmitButton() {
        UIComponent facet = getFacet(PAGINATION_SUBMIT_BUTTON_FACET);
        if (facet != null) {
            return facet;
        }

        // Get child.
        Button child = new Button();
        child.setId(PAGINATION_SUBMIT_BUTTON_ID);
        child.setText(getTheme().getMessage("table.pagination.submit")); //NOI18N
        child.setToolTip(getTheme().getMessage("table.pagination.submitPage")); //NOI18N
        child.addActionListener(new TablePaginationActionListener());
        
        // Set tab index.
        Table table = getTableAncestor();
        if (table != null) {
            child.setTabIndex(table.getTabIndex());
        } else {
            log("getPaginationSubmitButton", "Tab index not set, Table is null"); //NOI18N
        }

        // Save facet and return child.
        getFacets().put(child.getId(), child);        
        return child;
    }

    /**
     * Get last page button for pagination controls.
     *
     * @return The last page button.
     */
    public UIComponent getPaginationLastButton() {
        UIComponent facet = getFacet(PAGINATION_LAST_BUTTON_FACET);
        if (facet != null) {
            return facet;
        }

        // Get disabled state.
        Table table = getTableAncestor();
        TableRowGroup group = (table != null)
            ? table.getTableRowGroupChild() : null;
        boolean disabled = (group != null) 
            ? group.getFirst() >= group.getLast() : false;

        // Get child.
        IconHyperlink child = new IconHyperlink();
        child.setId(PAGINATION_LAST_BUTTON_ID);
        child.setIcon(disabled
            ? ThemeImages.TABLE_PAGINATION_LAST_DISABLED
            : ThemeImages.TABLE_PAGINATION_LAST);
        child.setBorder(0);
        child.setAlign("top"); //NOI18N
        child.setDisabled(disabled);
        child.addActionListener(new TablePaginationActionListener());

        // Set tool tip.
        String toolTip = getTheme().getMessage("table.pagination.last"); //NOI18N
        child.setAlt(toolTip);
        child.setToolTip(toolTip);

        // Set tab index.
        if (table != null) {
            child.setTabIndex(table.getTabIndex());
        } else {
            log("getPaginationLastButton", "Tab index not set, Table is null"); //NOI18N
        }

        // Set focus when paginaton buttons are disabled -- bugtraq #6316565.
        setPaginationFocus(child);

        // Save facet and return child.
        getFacets().put(child.getId(), child); 
        return child;
    }

    /**
     * Get next page button for pagination controls.
     *
     * @return The next page button.
     */
    public UIComponent getPaginationNextButton() {
        UIComponent facet = getFacet(PAGINATION_NEXT_BUTTON_FACET);
        if (facet != null) {
            return facet;
        }

        // Get disabled state.
        Table table = getTableAncestor();
        TableRowGroup group = (table != null)
            ? table.getTableRowGroupChild() : null;
        boolean disabled = (group != null) 
            ? group.getFirst() >= group.getLast() : false;

        // Get child.
        IconHyperlink child = new IconHyperlink();
        child.setId(PAGINATION_NEXT_BUTTON_ID);
        child.setIcon(disabled
            ? ThemeImages.TABLE_PAGINATION_NEXT_DISABLED
            : ThemeImages.TABLE_PAGINATION_NEXT);
        child.setBorder(0);
        child.setAlign("top"); //NOI18N
        child.setDisabled(disabled);
        child.addActionListener(new TablePaginationActionListener());

        // Set tool tip.
        String toolTip = getTheme().getMessage("table.pagination.next"); //NOI18N
        child.setAlt(toolTip);
        child.setToolTip(toolTip);

        // Set tab index.
        if (table != null) {
            child.setTabIndex(table.getTabIndex());
        } else {
            log("getPaginationNextButton", "Tab index not set, Table is null"); //NOI18N
        }

        // Set focus when paginaton buttons are disabled -- bugtraq #6316565.
        setPaginationFocus(child);

        // Save facet and return child.
        getFacets().put(child.getId(), child); 
        return child;
    }

    /**
     * Get page field for pagination controls.
     *
     * @return The page field.
     */
    public UIComponent getPaginationPageField() {
        UIComponent facet = getFacet(PAGINATION_PAGE_FIELD_FACET);
        if (facet != null) {
            return facet;
        }

        // Get current page.
        Table table = getTableAncestor();
        TableRowGroup group = (table != null)
            ? table.getTableRowGroupChild() : null;
        int page = (group != null) ? group.getPage() : 1;

        // Get child.
        TextField child = new TextField();
        child.setId(PAGINATION_PAGE_FIELD_ID);
        child.setText(Integer.toString(page));
        child.setOnKeyPress(getPaginationJavascript());
        child.setColumns(3); //NOI18N
        child.setLabelLevel(2);
        child.setLabel(getTheme().getMessage("table.pagination.page")); //NOI18N       

        // Set tab index.
        if (table != null) {
            child.setTabIndex(table.getTabIndex());
        } else {
            log("getPaginationPageField", "Tab index not set, Table is null"); //NOI18N
        }

        // Save facet and return child.
        getFacets().put(child.getId(), child); 
        return child;
    }

    /**
     * Get pages text for pagination controls.
     *
     * @return The pages text.
     */
    public UIComponent getPaginationPagesText() {
        UIComponent facet = getFacet(PAGINATION_PAGES_TEXT_FACET);
        if (facet != null) {
            return facet;
        }       

        Theme theme = getTheme();

        // Get child.
        StaticText child = new StaticText();
        child.setId(PAGINATION_PAGES_TEXT_ID);
        child.setStyleClass(theme.getStyleClass(
            ThemeStyles.TABLE_PAGINATION_TEXT));

        // Set page text.
        Table table = getTableAncestor();
        if (table != null) {
            child.setText(theme.getMessage("table.pagination.pages", //NOI18N
                new String[] {Integer.toString(table.getPageCount())}));
        } else {
            log("getPaginationPagesText", "Pages text not set, Table is null"); //NOI18N
        }

        // Save facet and return child.
        getFacets().put(child.getId(), child);
        return child;
    }

    /**
     * Get paginate button of pagination controls.
     *
     * @return The paginate button.
     */
    public UIComponent getPaginateButton() {
        UIComponent facet = getFacet(PAGINATE_BUTTON_FACET);
        if (facet != null) {
            return facet;
        }

        // Get paginated state.
        Table table = getTableAncestor();
        TableRowGroup group = (table != null) 
            ? table.getTableRowGroupChild() : null;
        boolean paginated = (group != null) ? group.isPaginated() : false;

        // Get child.
        IconHyperlink child = new IconHyperlink();
        child.setId(PAGINATE_BUTTON_ID);
        child.setIcon(paginated
            ? ThemeImages.TABLE_SCROLL_PAGE : ThemeImages.TABLE_PAGINATE);
        child.setBorder(0);
        child.setAlign("top"); //NOI18N
        child.addActionListener(new TablePaginationActionListener());

        // Set i18n tool tip.
        String toolTip = paginated
            ? getTheme().getMessage("table.pagination.scroll") //NOI18N
            : getTheme().getMessage("table.pagination.paginated"); //NOI18N
        child.setAlt(toolTip);
        child.setToolTip(toolTip);

        // Set tab index.
        if (table != null) {
            child.setTabIndex(table.getTabIndex());
        } else {
            log("getPaginateButton", "Tab index not set, Table is null"); //NOI18N
        }

        // Save facet and return child.
        getFacets().put(child.getId(), child); 
        return child;
    }

    /**
     * Get previous page button for pagination controls.
     *
     * @return The previous page button.
     */
    public UIComponent getPaginationPrevButton() {
        UIComponent facet = getFacet(PAGINATION_PREV_BUTTON_FACET);
        if (facet != null) {
            return facet;
        }

        // Get disabled state.
        Table table = getTableAncestor();
        TableRowGroup group = (table != null)
            ? table.getTableRowGroupChild() : null;
        boolean disabled = (group != null) ? group.getFirst() <= 0 : false;

        // Get child.
        IconHyperlink child = new IconHyperlink();
        child.setId(PAGINATION_PREV_BUTTON_ID);
        child.setIcon(disabled
            ? ThemeImages.TABLE_PAGINATION_PREV_DISABLED
            : ThemeImages.TABLE_PAGINATION_PREV);
        child.setBorder(0);
        child.setAlign("top"); //NOI18N
        child.setDisabled(disabled);
        child.addActionListener(new TablePaginationActionListener());

        // Set tool tip.
        String toolTip = getTheme().getMessage("table.pagination.previous"); //NOI18N
        child.setAlt(toolTip);
        child.setToolTip(toolTip);

        // Set tab index.
        if (table != null) {
            child.setTabIndex(table.getTabIndex());
        } else {
            log("getPaginationPrevButton", "Tab index not set, Table is null"); //NOI18N
        }

        // Set focus when paginaton buttons are disabled -- bugtraq #6316565.
        setPaginationFocus(child);

        // Save facet and return child.
        getFacets().put(child.getId(), child); 
        return child;
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Select methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Get deselect multiple button.
     *
     * @return The deselect multiple button.
     */
    public UIComponent getDeselectMultipleButton() {
        UIComponent facet = getFacet(DESELECT_MULTIPLE_BUTTON_FACET);
        if (facet != null) {
            return facet;
        }

        // Get paginated state.
        Table table = getTableAncestor();
        TableRowGroup group = (table != null)
            ? table.getTableRowGroupChild() : null;
        boolean paginated = (group != null) ? group.isPaginated() : false;

        // Get child.
        IconHyperlink child = new IconHyperlink();
	child.setId(DESELECT_MULTIPLE_BUTTON_ID);
        child.setIcon(ThemeImages.TABLE_DESELECT_MULTIPLE);
        child.setBorder(0);
        child.setAlign("top"); //NOI18N

        // Set onClick and tab index.
        if (table != null) {
            child.setOnClick(getSelectJavascript(
                table.getDeselectMultipleButtonOnClick(), false));
            child.setTabIndex(table.getTabIndex());
        } else {
            log("getDeselectMultipleButton", //NOI18N
                "Tab index & onClick not set, Table is null"); //NOI18N
        }

        // Get tool tip.
        String toolTip = getTheme().getMessage(paginated
            ? "table.select.deselectMultiplePaginated" //NOI18N
            : "table.select.deselectMultiple"); //NOI18N
        child.setAlt(toolTip);
        child.setToolTip(toolTip);

        // Save facet and return child.
        getFacets().put(child.getId(), child);
        return child;
    }

    /**
     * Get deselect single button.
     *
     * @return The deselect single button.
     */
    public UIComponent getDeselectSingleButton() {
        UIComponent facet = getFacet(DESELECT_SINGLE_BUTTON_FACET);
        if (facet != null) {
            return facet;
        }

        // Get paginated state.
        Table table = getTableAncestor();
        TableRowGroup group = (table != null)
            ? table.getTableRowGroupChild() : null;
        boolean paginated = (group != null) ? group.isPaginated() : false;

        // Get child.
        IconHyperlink child = new IconHyperlink();
	child.setId(DESELECT_SINGLE_BUTTON_ID);
        child.setIcon(ThemeImages.TABLE_DESELECT_SINGLE);
        child.setBorder(0);
        child.setAlign("top"); //NOI18N

        // Set onClick and tab index.
        if (table != null) {
            child.setOnClick(getSelectJavascript(
                table.getDeselectSingleButtonOnClick(), false));
            child.setTabIndex(table.getTabIndex());
        } else {
            log("getDeselectSingleButton", //NOI18N
                "Tab index & onClick not set, Table is null"); //NOI18N
        }

        // Set tool tip.
        String toolTip = getTheme().getMessage(paginated
            ? "table.select.deselectSinglePaginated" //NOI18N
            : "table.select.deselectSingle"); //NOI18N
        child.setAlt(toolTip);
        child.setToolTip(toolTip);

        // Save facet and return child.
        getFacets().put(child.getId(), child);
        return child;
    }

    /**
     * Get select multiple button.
     *
     * @return The select multiple button.
     */
    public UIComponent getSelectMultipleButton() {
        UIComponent facet = getFacet(SELECT_MULTIPLE_BUTTON_FACET);
        if (facet != null) {
            return facet;
        }

        // Get paginated state.
        Table table = getTableAncestor();
        TableRowGroup group = (table != null)
            ? table.getTableRowGroupChild() : null;
        boolean paginated = (group != null) ? group.isPaginated() : false;

        // Get child.
        IconHyperlink child = new IconHyperlink();
	child.setId(SELECT_MULTIPLE_BUTTON_ID);
        child.setIcon(ThemeImages.TABLE_SELECT_MULTIPLE);
        child.setBorder(0);
        child.setAlign("top"); //NOI18N

        // Set onClick and tab index.
        if (table != null) {
            child.setOnClick(getSelectJavascript(
                table.getDeselectMultipleButtonOnClick(), true));
            child.setTabIndex(table.getTabIndex());
        } else {
            log("getSelectMultipleButton", //NOI18N
                "Tab index & onClick not set, Table is null"); //NOI18N
        }

        // Set tool tip.
        String toolTip = getTheme().getMessage(paginated
            ? "table.select.selectMultiplePaginated" //NOI18N
            : "table.select.selectMultiple"); //NOI18N
        child.setAlt(toolTip);
        child.setToolTip(toolTip);

        // Save facet and return child.
        getFacets().put(child.getId(), child);
        return child;
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Separator methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Get the actions separator icon.
     *
     * @return The top actions separator icon.
     */
    public UIComponent getActionsSeparatorIcon() {
        return getSeparatorIcon(ACTIONS_SEPARATOR_ICON_ID,
            ACTIONS_SEPARATOR_ICON_FACET);
    }

    /**
     * Get the filter separator icon.
     *
     * @return The filter separator icon.
     */
    public UIComponent getFilterSeparatorIcon() {
        return getSeparatorIcon(FILTER_SEPARATOR_ICON_ID, 
            FILTER_SEPARATOR_ICON_FACET);
    }

    /**
     * Get the paginate separator icon.
     *
     * @return The paginate separator icon.
     */
    public UIComponent getPaginateSeparatorIcon() {
        return getSeparatorIcon(PAGINATE_SEPARATOR_ICON_ID, 
            PAGINATE_SEPARATOR_ICON_FACET);
    }

    /**
     * Get the view actions separator icon.
     *
     * @return The view actions separator icon.
     */
    public UIComponent getViewActionsSeparatorIcon() {
        return getSeparatorIcon(VIEW_ACTIONS_SEPARATOR_ICON_ID, 
            VIEW_ACTIONS_SEPARATOR_ICON_FACET);
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // View-changing action methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Get clear sort button.
     *
     * @return The clear sort button.
     */
    public UIComponent getClearSortButton() {
        UIComponent facet = getFacet(CLEAR_SORT_BUTTON_FACET);
        if (facet != null) {
            return facet;
        }

        // Get child.
        IconHyperlink child = new IconHyperlink();
	child.setId(CLEAR_SORT_BUTTON_ID);
        child.setIcon(ThemeImages.TABLE_SORT_CLEAR);
        child.setBorder(0);
        child.setAlign("top"); //NOI18N
        child.addActionListener(new TableSortActionListener());

        // Set tool tip.
        String toolTip = getTheme().getMessage(
            "table.viewActions.clearSort"); //NOI18N
        child.setAlt(toolTip);
        child.setToolTip(toolTip);

        // Set tab index.
        if (table != null) {
            child.setTabIndex(table.getTabIndex());
        } else {
            log("getClearSortButton", "Tab index not set, Table is null"); //NOI18N
        }

        // Save facet and return child.
        getFacets().put(child.getId(), child); 
        return child;
    }

    /**
     * Get filter label.
     *
     * @return The filter label.
     */
    public UIComponent getFilterLabel() {
        UIComponent facet = getFacet(FILTER_LABEL_FACET);
        if (facet != null) {
            return facet;
        }

        // Get child.
        Label child = new Label();
	child.setId(FILTER_LABEL_ID);
        child.setText(getTheme().getMessage("table.viewActions.filter")); //NOI18N        
        child.setLabelLevel(2);

        Table table = getTableAncestor();
        if (table != null) {
            child.setLabeledComponent(table.getFacet(Table.FILTER_FACET));
        } else {
            log("getFilterLabel", "Labeled component not set, Table is null"); //NOI18N
        }

        // Save facet and return child.
        getFacets().put(child.getId(), child); 
        return child;
    }

    /**
     * Get preferences panel toggle button.
     *
     * @return The preferences panel toggle button.
     */
    public UIComponent getPreferencesPanelToggleButton() {
        UIComponent facet = getFacet(PREFERENCES_PANEL_TOGGLE_BUTTON_FACET);
        if (facet != null) {
            return facet;
        }

        // Get child.
        IconHyperlink child = new IconHyperlink();
        child.setId(PREFERENCES_PANEL_TOGGLE_BUTTON_ID);
        child.setIcon(ThemeImages.TABLE_PREFERENCES_PANEL);
        child.setBorder(0);
        child.setAlign("top"); //NOI18N

        // Set JS to display table preferences panel.
        Table table = getTableAncestor();
        if (table != null) {
            StringBuffer buff = new StringBuffer(128)
                .append("document.getElementById('") //NOI18N
                .append(table.getClientId(getFacesContext()))
                .append("').togglePreferencesPanel(); return false"); //NOI18N
            child.setOnClick(buff.toString());
            child.setTabIndex(table.getTabIndex());
        } else {
            log("getPreferencesPanelToggleButton", //NOI18N
                "Tab index & onClick not set, Table is null"); //NOI18N
        }

        // Get tool tip.
        String toolTip = getTheme().getMessage("table.viewActions.preferences"); //NOI18N
        child.setAlt(toolTip);
        child.setToolTip(toolTip);

        // Save facet and return child.
        getFacets().put(child.getId(), child); 
        return child;
    }

    /**
     * Get sort panel toggle button.
     *
     * @return The sort panel toggle button.
     */
    public UIComponent getSortPanelToggleButton() {
        UIComponent facet = getFacet(SORT_PANEL_TOGGLE_BUTTON_FACET);
        if (facet != null) {
            return facet;
        }

        // Get child.
        IconHyperlink child = new IconHyperlink();
	child.setId(SORT_PANEL_TOGGLE_BUTTON_ID);
        child.setIcon(ThemeImages.TABLE_SORT_PANEL);
        child.setBorder(0);
        child.setAlign("top"); //NOI18N

        // Set JS to display table preferences panel.
        Table table = getTableAncestor();
        if (table != null) {
            StringBuffer buff = new StringBuffer(128)
                .append("document.getElementById('") //NOI18N
                .append(table.getClientId(getFacesContext()))
                .append("').toggleSortPanel(); return false"); //NOI18N
            child.setOnClick(buff.toString());
            child.setTabIndex(table.getTabIndex());
        } else {
            log("getSortPanelToggleButton", //NOI18N
                "Tab index & onClick not set, Table is null"); //NOI18N
        }

        // Set tool tip.
        Theme theme = getTheme();
        String toolTip = theme.getMessage("table.viewActions.sort"); //NOI18N
        child.setAlt(toolTip);
        child.setToolTip(toolTip);

        // Save facet and return child.
        getFacets().put(child.getId(), child); 
        return child;
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // UIComponent methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * If the rendered property is true, render the begining of the current
     * state of this UIComponent to the response contained in the specified
     * FacesContext.
     *
     * If a Renderer is associated with this UIComponent, the actual encoding 
     * will be delegated to Renderer.encodeBegin(FacesContext, UIComponent).
     *
     * @param context FacesContext for the current request.
     *
     * @exception IOException if an input/output error occurs while rendering.
     * @exception NullPointerException if FacesContext is null.
     */
    public void encodeBegin(FacesContext context) throws IOException {
        // Clear cached variables -- bugtraq #6300020.
        table = null;
        super.encodeBegin(context);
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Private methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Helper method to get Javascript for the de/select all buttons.
     *
     * @param script The Javascript to be prepended, if any.
     * @param checked true if components used for row selection should be 
     * checked; otherwise, false.
     *
     * @return The Javascript for the de/select buttons.
     */
    private String getSelectJavascript(String script, boolean checked) {
        // Get JS to de/select all components in table.
        StringBuffer buff = new StringBuffer(1024);

        // Developer may have added onClick Javascript for de/select all button.
        if (script != null) {
            buff.append(script).append(";"); //NOI18N
        } 

        // Append Javascript to de/select all select components.
        Table table = getTableAncestor();
        if (table != null) {
            buff.append("document.getElementById('") //NOI18N
                .append(table.getClientId(getFacesContext()))
                .append("').selectAllRows(") //NOI18N
                .append(checked)
                .append("); return false"); //NOI18N
        } else {
            log("getSelectJavascript", //NOI18N
                "Cannot obtain select Javascript, Table is null"); //NOI18N
        }
        return buff.toString();
    }

    /**
     * Helper method to get separator icons used for top and bottom actions, 
     * filter, view actions, and paginate button.
     *
     * @param id The identifier for the component.
     * @param name The facet name used to override the component.
     *
     * @return The separator icon.
     */
    private UIComponent getSeparatorIcon(String id, String name) {
        UIComponent facet = getFacet(name);
        if (facet != null) {
            return facet;
        }

        // Get child.
        Icon child = getTheme().getIcon(ThemeImages.TABLE_ACTIONS_SEPARATOR);
	child.setId(id);
        child.setBorder(0);
        child.setAlign("top"); //NOI18N
        
        // Save facet and return child.
        getFacets().put(child.getId(), child); 
        return child;
    }

    /**
     * Helper method to get Javascript to submit the "go" button when the user
     * clicks enter in the page field.
     *
     * @return The Javascript used to submit the "go" button.
     */
    private String getPaginationJavascript() {
        ClientSniffer cs = ClientSniffer.getInstance(getFacesContext());

        // Get key code.
        String keyCode = cs.isNav() ? "event.which" : "event.keyCode"; //NOI18N

        // Append JS to capture the event.
        StringBuffer buff = new StringBuffer(128)
            .append("if (") //NOI18N
            .append(keyCode)
            .append("==13) {"); //NOI18N

        // To prevent an auto-submit, Netscape 6.x and netscape 7.0 require 
        // setting the cancelBubble property. However, Netscape 7.1, 
        // Mozilla 1.x, IE 5.x for SunOS/Windows do not use this property.
        if (cs.isNav6() || cs.isNav70()) {
            buff.append("event.cancelBubble = true;"); //NOI18N
        }
        
        // Append JS to submit the button.
        buff.append("var e=document.getElementById('") //NOI18N
            .append(getClientId(getFacesContext()) + 
                NamingContainer.SEPARATOR_CHAR + 
                TableActions.PAGINATION_SUBMIT_BUTTON_ID)
            .append("'); if (e != null) e.click(); return false}"); //NOI18N
        return buff.toString();
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
     * Helper method to determine if table is empty.
     *
     * @return true if table contains no rows.
     */
    private boolean isEmptyTable() {
        int totalRows = table.getRowCount();
        return (totalRows == 0);
    }

    /**
     * Helper method to determine if all rows fit on a single page.
     * <p>
     * Note: Pagination controls are only hidden when all groups fit on a single
     * page.
     * </p>
     * @return true if all rows fit on a single page.
     */
    private boolean isSinglePage() {
        int totalRows = table.getRowCount();
        return (totalRows < table.getRows());
    }

    /**
     * Helper method to determine if table contains a single row.
     *
     * @return true if all rows fit on a single page.
     */
    private boolean isSingleRow() {
        int totalRows = table.getRowCount();
        return (totalRows == 1);
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
     * Set focus when paginaton buttons are disabled.
     */
    private void setPaginationFocus(IconHyperlink component) {
        if (component == null || !component.isDisabled()) {
            return;
        }

        // Get prefix for all IDs.
        FacesContext context = getFacesContext();
        String prefix = getClientId(context) + NamingContainer.SEPARATOR_CHAR;

        // Get the client ID of the last component to have focus.
        String id = RenderingUtilities.getLastClientID(context);
        if (id == null) {
            return;
        }

        // Set focus on the page field component.
        if (id.equals(prefix + PAGINATION_FIRST_BUTTON_ID)
                || id.equals(prefix + PAGINATION_LAST_BUTTON_ID)
                || id.equals(prefix + PAGINATION_NEXT_BUTTON_ID)
                || id.equals(prefix + PAGINATION_PREV_BUTTON_ID)) {
            RenderingUtilities.setLastClientID(context, 
                prefix + PAGINATION_PAGE_FIELD_ID + Field.INPUT_ID);
        }
    }
}
