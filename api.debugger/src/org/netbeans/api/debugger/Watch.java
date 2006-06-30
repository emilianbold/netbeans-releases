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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.debugger;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;


/**
 * Abstract definition of watch. Each watch is created for
 * one String which contains the name of variable or some expression.
 *
 * @author   Jan Jancura
 */
public final class Watch {

    /** Name of the property for the watched expression. */
    public static final String PROP_EXPRESSION = "expression"; // NOI18N
    /** Name of the property for the value of the watched expression. This constant is not used at all. */
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
        String old = this.expression;
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

