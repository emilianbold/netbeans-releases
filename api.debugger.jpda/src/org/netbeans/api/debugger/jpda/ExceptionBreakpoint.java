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

package org.netbeans.api.debugger.jpda;


/**
 * Notifies about exceptions throw in debugged JVM.
 *
 * <br><br>
 * <b>How to use it:</b>
 * <pre style="background-color: rgb(255, 255, 153);">
 *    DebuggerManager.addBreakpoint (ExceptionBreakpoint.create (
 *        "java.lang.NullPointerException",
 *        ExceptionBreakpoint.TYPE_EXCEPTION_UNCATCHED
 *    ));</pre>
 * This breakpoint stops when NullPointerException is throw and uncatched.
 *
 * @author Jan Jancura
 */
public final class ExceptionBreakpoint extends JPDABreakpoint {

    /** Property name constant */
    public static final String          PROP_EXCEPTION_CLASS_NAME = "exceptionClassName"; // NOI18N
    /** Property name constant. */
    public static final String          PROP_CATCH_TYPE = "catchType"; // NOI18N
    /** Property name constant. */
    public static final String          PROP_CONDITION = "condition"; // NOI18N

    /** Catch type constant. */
    public static final int             TYPE_EXCEPTION_CATCHED = 1;
    /** Catch type constant. */
    public static final int             TYPE_EXCEPTION_UNCATCHED = 2;
    /** Catch type constant. */
    public static final int             TYPE_EXCEPTION_CATCHED_UNCATCHED = 3;

    private String                      exceptionClassName = "";
    private int                         catchType = TYPE_EXCEPTION_UNCATCHED;
    private String                      condition = ""; // NOI18N

    
    private ExceptionBreakpoint () {
    }
    
    /**
     * Creates a new breakpoint for given parameters.
     *
     * @param exceptionClassName class name filter
     * @param catchType one of constants: TYPE_EXCEPTION_CATCHED, 
     *   TYPE_EXCEPTION_UNCATCHED, TYPE_EXCEPTION_CATCHED_UNCATCHED
     * @return a new breakpoint for given parameters
     */
    public static ExceptionBreakpoint create (
        String exceptionClassName,
        int catchType
    ) {
        ExceptionBreakpoint b = new ExceptionBreakpoint ();
        b.setExceptionClassName (exceptionClassName);
        b.setCatchType (catchType);
        return b;
    }
    
    /**
     * Get name of exception class to stop on.
     *
     * @return name of exception class to stop on
     */
    public String getExceptionClassName () {
        return exceptionClassName;
    }

    /**
     * Set name of exception class to stop on.
     *
     * @param cn a new name of exception class to stop on.
     */
    public void setExceptionClassName (String cn) {
        if (cn != null) {
            cn = cn.trim();
        }
        if ( (cn == exceptionClassName) ||
             ((cn != null) && (exceptionClassName != null) && exceptionClassName.equals (cn))
        ) return;
        Object old = exceptionClassName;
        exceptionClassName = cn;
        firePropertyChange (PROP_EXCEPTION_CLASS_NAME, old, exceptionClassName);
    }
    
    /**
     * Returns condition.
     *
     * @return cond a condition
     */
    public String getCondition () {
        return condition;
    }

    /**
     * Sets condition.
     *
     * @param cond a c new condition
     */
    public void setCondition (String cond) {
        if (cond != null) {
            cond = cond.trim();
        }
        String old = condition;
        condition = cond;
        firePropertyChange (PROP_CONDITION, old, cond);
    }

    /**
     * Returns breakpoint type property value.
     *
     * @return breakpoint type property value.
     */
    public int getCatchType () {
        return catchType;
    }

    /**
     * Sets breakpoint type property value.
     *
     * @param catchType a new value of breakpoint type property value
     */
    public void setCatchType (int catchType) {
        if (catchType == this.catchType) return;
        if ( (catchType & (TYPE_EXCEPTION_CATCHED | TYPE_EXCEPTION_UNCATCHED)) == 0
           ) throw new IllegalArgumentException  ();
        int old = this.catchType;
        this.catchType = catchType;
        firePropertyChange (
            PROP_CATCH_TYPE, 
            new Integer (old), 
            new Integer (catchType)
        );
    }

    /**
     * Returns a string representation of this object.
     *
     * @return  a string representation of the object
     */
    public String toString () {
        return "ExceptionBreakpoint" + exceptionClassName;
    }
}
