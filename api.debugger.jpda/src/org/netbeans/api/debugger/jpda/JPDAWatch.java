/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.debugger.jpda;



/**
 * Represents watch in JPDA debugger.
 *
 * <pre style="background-color: rgb(255, 255, 102);">
 * It's strongly recommended
 * not to implement this interface in client code. New methods can be added to
 * this interface at any time to keep up with the JDI functionality.</pre>
 *
 * @author   Jan Jancura
 */

public interface JPDAWatch extends Variable {

    /**
     * Watched expression.
     *
     * @return watched expression
     */
    public abstract String getExpression ();

    /**
     * Sets watched expression.
     *
     * @param expression a expression to be watched
     */
    public abstract void setExpression (String expression);
    
    /**
     * Remove the watch from the list of all watches in the system.
     */
    public abstract void remove ();

    /**
     * Declared type of this local.
     *
     * @return declared type of this local
     */
    public abstract String getType ();

    /**
     * Text representation of current value of this local.
     *
     * @return text representation of current value of this local
     */
    public abstract String getValue ();

    /**
     * Returns description of problem is this watch can not be evaluated
     * in current context.
     *
     * @return description of problem
     */
    public abstract String getExceptionDescription ();
    
    /**
     * Sets value of this local represented as text.
     *
     * @param value a new value of this variable represented as text
     */
    public abstract void setValue (String value) throws InvalidExpressionException;

    /**
     * Calls {@link java.lang.Object#toString} in debugged JVM and returns
     * its value.
     *
     * @return toString () value of this instance
     */
    public abstract String getToStringValue () throws InvalidExpressionException;
}

