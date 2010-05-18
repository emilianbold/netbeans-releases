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

import com.sun.data.provider.RowKey;
import com.sun.rave.web.ui.util.LogUtil;

import java.util.HashMap;

import javax.faces.FactoryFinder;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

/**
 * A utility class for radiobutton and checkbox components used to select rows
 * of a table.
 * <p>
 * Note: UI guidelines recomend that rows should be unselected when no longer in
 * view. For example, when a user selects rows of the table and navigates to
 * another page. Or, when a user applies a filter or sort that may hide
 * previously selected rows from view. If a user invokes an action to delete the
 * currently selected rows, they may inadvertently remove rows not displayed on
 * the current page. Using TableSelectPhaseListener ensures that invalid row
 * selections are not rendered by clearing selected state after the render
 * response phase. That said, there are cases when maintaining state across
 * table pages is necessary. In this scenario, use the keepSelected method to
 * prevent state from being cleared by this instance.
 * </p><p>
 * Note: To see the messages logged by this class, set the following global
 * defaults in your JDK's "jre/lib/logging.properties" file.
 * </p><p><pre>
 * java.util.logging.ConsoleHandler.level = FINE
 * com.sun.rave.web.ui.event.TableSelectPhaseListener.level = FINE
 * </pre></p>
 */
public class TableSelectPhaseListener implements PhaseListener {
    private Object unselected = null; // Unselected object for primitve values.
    private HashMap selected = new HashMap(); // Selected values map.
    private boolean keepSelected = false; // Do not clear selected flag.

    /** Default constructor */
    public TableSelectPhaseListener() {
        // Add phase listener.
	LifecycleFactory factory = (LifecycleFactory)
        FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
	Lifecycle lifecycle = factory.getLifecycle(
            LifecycleFactory.DEFAULT_LIFECYCLE);
        lifecycle.addPhaseListener(this);
    }

    /** 
     * Construct an instance with the given flag indicating that selected
     * objects should not be cleared after the render response phase.
     *
     * @param keepSelected If true, ojects are not cleared.
     */
    public TableSelectPhaseListener(boolean keepSelected) {
        this();
        keepSelected(keepSelected);
    }

    /** Construct an instance with an unselected parameter.
     * <p>
     * The unselected parameter is only required if a primitve value is being 
     * used for the selecteValue attribute of the checkbox or radiobutton. If 
     * the selectedValue property is an Object value then unselected can be 
     * null. If however it is a primitive type then it should be the MIN_VALUE 
     * constant instance of the wrapper Object type. For example if the 
     * application is assigning int values to selectedValue then unselected 
     * should be new Integer(Integer.MIN_VALUE).
     * </p>
     * @param unselected the object to return for an unselected checkbox.
     */
    public TableSelectPhaseListener(Object unselected) {
        this();
        this.unselected = unselected;
    }

    /**
     * Called during the JSF Lifecycle after the RENDER_RESPONSE phase.
     *
     * @param event The PhaseEvent object.
     */
    public void afterPhase(PhaseEvent event) {
        if (!keepSelected) {
            selected.clear();
        } else {
            log("afterPhase", //NOI18N
                "Selected values not cleared, keepSelected is false");
        }
    }

    /**
     * Called during the JSF Lifecycle before the RENDER_RESPONSE phase.
     *
     * @param event The PhaseEvent object.
     */
    public void beforePhase(PhaseEvent event) {
        // Not needed
    }

    /** Get the phase id. */
    public PhaseId getPhaseId() {
        return PhaseId.RENDER_RESPONSE;
    }

    /**
     * Clear all selected objects.
     */
    public void clear() {
        selected.clear();
    }

    /**
     * Get the selected object from this instance.
     * <p>
     * Note: Call this method from the get method that that is bound to the 
     * selected attribute.
     * </p>
     * @param rowKey The current RowKey.
     * @return The selected object.
     */
    public Object getSelected(RowKey rowKey) {
        Object object = (rowKey != null) 
            ? selected.get(rowKey.getRowId()) : null;

        // If null, return the unselected value.
	return (object != null) ? object : unselected;
    }

    /**
     * Test if the flag indicating that selected objects should be cleared
     * after the render response phase.
     *
     * @return true if ojects are not to be cleared.
     */
    public boolean isKeepSelected() {
        return keepSelected;
    }

    /**
     * Test if the object associated with the given RowKey is selected.
     *
     * @param rowKey The current RowKey.
     * @return A true or false value.
     */
    public boolean isSelected(RowKey rowKey) {
        Object object = getSelected(rowKey);
	return (object != null && object != unselected);
    }

    /**
     * Set the flag indicating that selected objects should be cleared after
     * the render response phase.
     *
     * @param keepSelected Selected objects are kept true, cleared if false.
     */
    public void keepSelected(boolean keepSelected) {
        this.keepSelected = keepSelected;
    }

    /**
     * Set the selected object for this instance.
     * <p>
     * Note: Call this method from the set method that that is bound to the 
     * selected attribute.
     * </p>
     * @param rowKey The current RowKey.
     * @param object The selected object.
     */
    public void setSelected(RowKey rowKey, Object object) {
        if (rowKey != null) {
            selected.put(rowKey.getRowId(), object);
        }
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Private methods
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
