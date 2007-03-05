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

import com.sun.data.provider.SortCriteria;
import com.sun.rave.web.ui.theme.Theme;
import com.sun.rave.web.ui.util.LogUtil;
import com.sun.rave.web.ui.util.ThemeUtilities;

import java.io.IOException;

import javax.faces.context.FacesContext;
import javax.faces.component.UIComponent;
import javax.faces.component.NamingContainer;

/**
 * Component that represents a table footer.
 * <p>
 * Note: Column headers and footers are rendered by TableRowGroupRenderer. Table
 * column footers are rendered by TableRenderer.
 * </p><p>
 * Note: To see the messages logged by this class, set the following global
 * defaults in your JDK's "jre/lib/logging.properties" file.
 * </p><p><pre>
 * java.util.logging.ConsoleHandler.level = FINE
 * com.sun.rave.web.ui.component.TableFooter.level = FINE
 * </pre></p>
 */
public class TableFooter extends TableFooterBase implements NamingContainer {
    // The Table ancestor enclosing this component.
    private Table table = null;

    // The TableColumn ancestor enclosing this component.
    private TableColumn tableColumn = null;

    // The TableRowGroup ancestor enclosing this component.
    private TableRowGroup tableRowGroup = null;

    // Sort level for this component.
    private int sortLevel = -1;

    /** Default constructor */
    public TableFooter() {
        super();
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Child methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Helper method to get sort level for this component.
     *
     * @return The sort level or 0 if sort does not apply.
     */
    public int getSortLevel() {
        if (sortLevel == -1) {
            TableColumn col = getTableColumnAncestor();
            TableRowGroup group = getTableRowGroupAncestor();
            if (col != null && group != null) {
                sortLevel = group.getSortLevel(col.getSortCriteria());
            } else {
                log("getSortLevel", //NOI18N
                    "Cannot obtain sort level, TableColumn or TableRowGroup is null"); //NOI18N
            }
        }
        return sortLevel;
    }

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

    /**
     * Get the closest TableColumn ancestor that encloses this component.
     *
     * @return The TableColumn ancestor.
     */
    public TableColumn getTableColumnAncestor() {
        if (tableColumn == null) {
            UIComponent component = this;
            while (component != null) {
                component = component.getParent();
                if (component instanceof TableColumn) {
                    tableColumn = (TableColumn) component;
                    break;
                }
            }
        }
        return tableColumn;
    }

    /**
     * Get the closest TableRowGroup ancestor that encloses this component.
     *
     * @return The TableRowGroup ancestor.
     */
    public TableRowGroup getTableRowGroupAncestor() {
        if (tableRowGroup == null) {
            UIComponent component = this;
            while (component != null) {
                component = component.getParent();
                if (component instanceof TableRowGroup) {
                    tableRowGroup = (TableRowGroup) component;
                    break;
                }
            }
        }
        return tableRowGroup;
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
        tableColumn = null;
        tableRowGroup = null;
        sortLevel = -1;
        super.encodeBegin(context);
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Private methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Helper method to get Theme objects.
     *
     * @return The current theme.
     */
    private Theme getTheme() {
	return ThemeUtilities.getTheme(getFacesContext());
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
