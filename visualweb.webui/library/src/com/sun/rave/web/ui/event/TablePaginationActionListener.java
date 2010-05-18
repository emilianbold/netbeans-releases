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
package com.sun.rave.web.ui.event;

import com.sun.rave.web.ui.component.TextField;
import com.sun.rave.web.ui.component.Table;
import com.sun.rave.web.ui.component.TableActions;
import com.sun.rave.web.ui.component.TableRowGroup;
import com.sun.rave.web.ui.util.ConversionUtilities;
import com.sun.rave.web.ui.util.LogUtil;

import java.util.Iterator;

import java.util.List;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

/**
 * A listener for receiving pagination toggle events.
 * <p>
 * A class that is interested in receiving such events registers itself with the
 * source {@link Table} of interest, by calling addActionListener().
 * </p><p>
 * Note: To see the messages logged by this class, set the following global
 * defaults in your JDK's "jre/lib/logging.properties" file.
 * </p><p><pre>
 * java.util.logging.ConsoleHandler.level = FINE
 * com.sun.rave.web.ui.event.TablePaginationActionListener.level = FINE
 * </pre></p>
 */
public class TablePaginationActionListener implements ActionListener {
    /**
     * Invoked when the action described by the specified
     * {@link ActionEvent} occurs. The source parent is expected to be a
     * TablePagination object.
     *
     * @param event The {@link ActionEvent} that has occurred
     *
     * @exception AbortProcessingException Signal the JavaServer Faces
     *  implementation that no further processing on the current event
     *  should be performed
     */
    public void processAction(ActionEvent event)
            throws AbortProcessingException {
        UIComponent source = (event != null)
            ? (UIComponent) event.getSource() : null;
        if (source == null) {
            log("processAction", //NOI18N
                "Cannot process action, ActionEvent source is null"); //NOI18N
            return;
        }
        processTable(getTableAncestor(source), source.getId());
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Process Methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    // Helper method to process Table components.
    private void processTable(Table component, String id) 
            throws AbortProcessingException {
        if (component == null) {
            log("processTable", "Cannot process Table action, Table is null"); //NOI18N
            return;
        }

        // Iterate over every TableRowGroup child and set pagination for each.
        Iterator kids = component.getTableRowGroupChildren();
        while (kids.hasNext()) {
            TableRowGroup group = (TableRowGroup) kids.next();

            if (id.equals(TableActions.PAGINATION_FIRST_BUTTON_ID)) {
                setFirst(group);
            } else if (id.equals(TableActions.PAGINATION_LAST_BUTTON_ID)) {
                setLast(group);
            } else if (id.equals(TableActions.PAGINATION_NEXT_BUTTON_ID)) {
                setNext(group);
            } else if (id.equals(TableActions.PAGINATE_BUTTON_ID)) {
                setPaginated(group);
            } else if (id.equals(TableActions.PAGINATION_PREV_BUTTON_ID)) {
                setPrev(group);
            } else if (id.equals(TableActions.PAGINATION_SUBMIT_BUTTON_ID)) {
                try {
                    setPage(group, getPage(component));
                } catch (NumberFormatException e) {
                    log("processTable", "Cannot obtain page field value"); //NOI18N
                    return;    
                }
            }
        }
    }
    
    /**
     * Get the closest Table ancestor that encloses this component.
     *
     * @param component UIComponent for which to extract children.
     */
    private Table getTableAncestor(UIComponent component) {
        if (component == null) {
            log("getTableAncestor", //NOI18N
                "Cannot obtain Table ancestor, UIComponent is null"); //NOI18N
            return null;
        } else if (component instanceof Table) {
            return (Table) component;
        } else {
            return getTableAncestor(component.getParent());
        }
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Pagination Methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    // Get the value of the page field.
    private int getPage(Table component) {
        UIComponent actions = component.getFacet(Table.TABLE_ACTIONS_BOTTOM_ID);
        UIComponent field = (actions != null)
            ? (UIComponent) actions.getFacet(TableActions.PAGINATION_PAGE_FIELD_ID)
            : null;
        String value = null;
        if (field instanceof TextField) {
            value = ConversionUtilities.convertValueToString(field,
                ((TextField) field).getValue());
        } else {
            log("getPage", //NOI18N
                "Cannot obtain page text field value, not TextField instance"); //NOI18N
        }
        return (value != null) ? Integer.parseInt(value) : -1;
    }

    // Set current page.
    private void setPage(TableRowGroup component, int page) {      
        if (component == null) {
            log("setPage", "Cannot set page, TableRowGroup is null"); //NOI18N
            return;
        }      
        // Set the starting row for the current page.
        int row = (page - 1) * component.getRows();
        setRow(component, row);
    }

    // Set first row.
    private void setFirst(TableRowGroup component) {      
        if (component == null) {
            log("setFirst", "Canot set first row, TableRowGroup is null"); //NOI18N
            return;
        }
        // Set the starting row for the first page.
        setRow(component, 0);
    }

    // Set last row.
    private void setLast(TableRowGroup component) {      
        if (component == null) {
            log("setLast", "Cannot set last row, TableRowGroup is null"); //NOI18N
            return;
        }
        // Get the row number of the last page to be displayed.
        setRow(component, component.getLast());
    }

    // Set next row.
    private void setNext(TableRowGroup component) {
        if (component == null) {
            log("setNext", "Cannot set next row, TableRowGroup is null"); //NOI18N
            return;
        }       
        // Get the starting row index for the next page.
        int row = component.getFirst() + component.getRows();
        setRow(component, row);
    }

    // Set paginated.
    private void setPaginated(TableRowGroup component) {      
        if (component == null) {
            log("setPaginated", "Cannot set paginated, TableRowGroup is null"); //NOI18N
            return;
        }
        // Toggle between paginated and scroll mode.
        component.setPaginated(!component.isPaginated());
    }

    // Set previous row.
    private void setPrev(TableRowGroup component) {      
        if (component == null) {
            log("setPrev", "Cannot set previous row, TableRowGroup is null"); //NOI18N
            return;
        }
        // Get the starting row index for the previous page.
        int row = component.getFirst() - component.getRows();
        setRow(component, row);
    }

    // Set row after validating min and max values.
    private void setRow(TableRowGroup component, int row) {
        if (component == null) {
            log("setRow", "Cannot set row, TableRowGroup is null"); //NOI18N
            return;
        }       
        // Result cannot be greater than the row index for the last page.
        int result = Math.min(row, component.getLast());

        // Result cannot be greater than total number of rows or less than zero.
        component.setFirst(Math.min(Math.max(result, 0), component.getRowCount()));
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Misc methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

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
