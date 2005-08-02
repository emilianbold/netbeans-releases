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

package org.netbeans.api.debugger.jpda;


/**
 * Notifies about method entry events.
 *
 * <br><br>
 * <b>How to use it:</b>
 * <pre style="background-color: rgb(255, 255, 153);">
 *    DebuggerManager.addBreakpoint (MethodBreakpoint.create (
 *        "examples.texteditor.Ted*",
 *        "<init>
 *    ));</pre>
 * This breakpoint stops when some initializer of class Ted or innercalsses is
 * called.
 *
 * @author Jan Jancura
 */
public final class MethodBreakpoint extends JPDABreakpoint {

    /** Property name constant */
    public static final String          PROP_METHOD_NAME = "methodName"; // NOI18N
    /** Property name constant. */
    public static final String          PROP_BREAKPOINT_TYPE = "breakpointtType"; // NOI18N
    /** Property name constant. */
    public static final String          PROP_CONDITION = "condition"; // NOI18N
    /** Property name constant */
    public static final String          PROP_CLASS_FILTERS = "classFilters"; // NOI18N
    /** Property name constant */
    public static final String          PROP_CLASS_EXCLUSION_FILTERS = "classExclusionFilters"; // NOI18N

    /** Breakpoint type property value constant. */
    public static final int             TYPE_METHOD_ENTRY = 1;
    /** Breakpoint type property value constant. */
    public static final int             TYPE_METHOD_EXIT = 2;

    /** Property variable. */
    private String[]                    classFilters = new String [0];
    private String[]                    classExclusionFilters = new String [0];
    private String                      methodName = "";
    private int                         breakpointType = TYPE_METHOD_ENTRY;
    private String                      condition = "";
    
    
    private MethodBreakpoint () {
    }
    
    /**
     * Creates a new method breakpoint for given parameters.
     *
     * @param className a class name filter
     * @param methodName a name of method
     * @return a new breakpoint for given parameters
     */
    public static MethodBreakpoint create (
        String className,
        String methodName
    ) {
        MethodBreakpoint b = new MethodBreakpoint ();
        b.setClassFilters (new String[] {className});
        b.setMethodName (methodName);
        return b;
    }
    
    /**
     * Creates a new method breakpoint.
     *
     * @return a new method breakpoint
     */
    public static MethodBreakpoint create (
    ) {
        MethodBreakpoint b = new MethodBreakpoint ();
        return b;
    }

    /**
     * Get name of method to stop on.
     *
     * @return name of method to stop on
     */
    public String getMethodName () {
        return methodName;
    }

    /**
     * Set name of method to stop on.
     *
     * @param mn a name of method to stop on
     */
    public void setMethodName (String mn) {
        if (mn != null) {
            mn = mn.trim();
        }
        if ( (mn == methodName) ||
             ((mn != null) && (methodName != null) && methodName.equals (mn))
        ) return;
        String old = methodName;
        methodName = mn;
        firePropertyChange (PROP_METHOD_NAME, old, mn);
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
     * Returns type of this breakpoint.
     *
     * @return type of this breakpoint
     */
    public int getBreakpointType () {
        return breakpointType;
    }

    /**
     * Sets type of this breakpoint (TYPE_METHOD_ENTRY or TYPE_METHOD_EXIT).
     *
     * @param breakpointType a new value of breakpoint type property
     */
    public void setBreakpointType (int breakpointType) {
        if (breakpointType == this.breakpointType) return;
        if ((breakpointType & (TYPE_METHOD_ENTRY | TYPE_METHOD_EXIT)) == 0)
            throw new IllegalArgumentException  ();
        int old = this.breakpointType;
        this.breakpointType = breakpointType;
        firePropertyChange (PROP_BREAKPOINT_TYPE, new Integer (old), new Integer (breakpointType));
    }

    /**
     * Get list of class filters to stop on.
     *
     * @return list of class filters to stop on
     */
    public String[] getClassFilters () {
        return classFilters;
    }

    /**
     * Set list of class filters to stop on.
     *
     * @param classFilters a new value of class filters property
     */
    public void setClassFilters (String[] classFilters) {
        if (classFilters == this.classFilters) return;
        Object old = this.classFilters;
        this.classFilters = classFilters;
        firePropertyChange (PROP_CLASS_FILTERS, old, classFilters);
    }

    /**
     * Get list of class exclusion filters to stop on.
     *
     * @return list of class exclusion filters to stop on
     */
    public String[] getClassExclusionFilters () {
        return classExclusionFilters;
    }

    /**
     * Set list of class exclusion filters to stop on.
     *
     * @param classExclusionFilters a new value of class exclusion filters property
     */
    public void setClassExclusionFilters (String[] classExclusionFilters) {
        if (classExclusionFilters == this.classExclusionFilters) return;
        Object old = this.classExclusionFilters;
        this.classExclusionFilters = classExclusionFilters;
        firePropertyChange (PROP_CLASS_EXCLUSION_FILTERS, old, classExclusionFilters);
    }

    /**
     * Returns a string representation of this object.
     *
     * @return  a string representation of the object
     */
    public String toString () {
        return "MethodBreakpoint " + classFilters + "." + methodName;
    }
}
