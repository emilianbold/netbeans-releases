/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.debugger;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;


/**
 * Abstract definition of watch. Each watch is created for (defined by) 
 * one String which contains the name of variable or some expression.
 * <br><br>
 * <b><u>List of properties:</b></u>
 * <ul>
 *   <li><b>variableName (rw):</b> contains the name of variable or expression
 *      which should be evaluated</li> 
 *   <li><b>asText (rw):</b> returns the current value of variable / 
 *      expression</li> 
 *   <li><b>type (ro):</b> returns the type of current value of variable</li> 
 *   <li><b>errorMessage (ro):</b> returns some description of problems 
 *      if the value can not be evaluated</li> 
 *   <li><b>hidden (rw):</b> returns true if this watch should be visible to 
 *      user</li> 
 * </ul>
 *
 * @author   Jan Jancura
 */
public final class Watch {
    
    /** Name of the property for the watched expression. */
    public static final String PROP_EXPRESSION = "expression"; // NOI18N
    /** Name of the property for the value of the watched expression. */
    public static final String PROP_VALUE = "value"; // NOI18N

    private String          expression;
    private PropertyChangeSupport pcs;
    
    
    Watch (String expr) {
        this.expression = expr;
        pcs = new PropertyChangeSupport (this);
    }
    
    /**
     * Return expression this watch is created for.
     *
     * @return expression this watch is created for
     */
    public String getExpression () {
        return expression;
    }

    /** 
     * Set the expression to watch.
     *
     * @param expression expression to watch
     */
    public void setExpression (String expression) {
        String old = expression;
        this.expression = expression;
        pcs.firePropertyChange (PROP_EXPRESSION, old, expression);
    }
    
    /**
     * Remove the watch from the list of all watches in the system.
     */
    public void remove () {
        DebuggerManager dm = DebuggerManager.getDebuggerManager ();
        dm.removeWatch (this);
    }

    /**
     * Add a property change listener.
     *
     * @param l the listener to add
     */
    public void addPropertyChangeListener (PropertyChangeListener l) {
        pcs.addPropertyChangeListener (l);
    }

    /**
     * Remove a property change listener.
     *
     * @param l the listener to remove
     */
    public void removePropertyChangeListener (PropertyChangeListener l) {
        pcs.removePropertyChangeListener (l);
    }
}

