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
 * Notifies about class load and class unload events.
 *
 * <br><br>
 * <b>How to use it:</b>
 * <pre style="background-color: rgb(255, 255, 153);">
 *    DebuggerManager.addBreakpoint (ClassLoadUnloadBreakpoint.create (
 *        "org.netbeans.modules.editor.*",
 *        false,
 *        ClassLoadUnloadBreakpoint.TYPE_CLASS_LOADED
 *    ));</pre>
 * This breakpoint stops when some class from org.netbeans.modules.editor 
 * package is loaded.
 *
 * @author Jan Jancura
 */
public final class ClassLoadUnloadBreakpoint extends JPDABreakpoint {

    /** Name of property for class name filter. */
    public static final String          PROP_CLASS_NAME_FILTER = "classNameFilter"; // NOI18N
    /** Name of property for is exclusion property. */
    public static final String          PROP_IS_EXCLUSION_FILTER = "isExclusionFilter"; // NOI18N
    /** Name of property for breakpoint type. */
    public static final String          PROP_BREAKPOINT_TYPE = "breakpointType"; // NOI18N

    /** Catch type property value constant. */
    public static final int             TYPE_CLASS_LOADED = 1;
    /** Catch type property value constant. */
    public static final int             TYPE_CLASS_UNLOADED = 2;
    /** Catch type property value constant. */
    public static final int             TYPE_CLASS_LOADED_UNLOADED = 3;

    /** Property variable. */
    private int                         type = TYPE_CLASS_LOADED;
    private boolean                     exclusionFilter = false;
    private String                      classFilter = "";

    
    private ClassLoadUnloadBreakpoint () {
    }
    
    /**
     * Creates a new breakpoint for given parameters.
     *
     * @param classNameFilter class name filter
     * @param isExclusionFilter if true filter is used as exclusion filter
     * @param breakpointType one of constants: TYPE_CLASS_LOADED, 
     *   TYPE_CLASS_UNLOADED, TYPE_CLASS_LOADED_UNLOADED
     * @return a new breakpoint for given parameters
     */
    public static ClassLoadUnloadBreakpoint create (
        String classNameFilter,
        boolean isExclusionFilter,
        int breakpointType
    ) {
        ClassLoadUnloadBreakpoint b = new ClassLoadUnloadBreakpoint ();
        b.setClassNameFilter (classNameFilter);
        b.setExclusionFilter (isExclusionFilter);
        b.setBreakpointType (breakpointType);
        return b;
    }

    /**
     * Returns type of breakpoint.
     *
     * @return type of breakpoint
     */
    public int getBreakpointType () {
        return type;
    }

    /**
     * Sets type of breakpoint.
     *
     * @param type a new value of breakpoint type property
     */
    public void setBreakpointType (int type) {
        if (type == this.type) return;
        if ((type & (TYPE_CLASS_LOADED | TYPE_CLASS_UNLOADED)) == 0)
            throw new IllegalArgumentException  ();
        int old = this.type;
        this.type = type;
        firePropertyChange (PROP_BREAKPOINT_TYPE, new Integer (old), new Integer (type));
    }

    /**
     * If true filter will be used as exclusion filter.
     *
     * @return exclusion filter property value
     */
    public boolean isExclusionFilter () {
        return exclusionFilter;
    }

    /**
     * Setter of exclusion filter property.
     *
     * @param exclusionFilter a new value of exclusion filter property
     */
    public void setExclusionFilter (boolean exclusionFilter) {
        if (exclusionFilter == this.exclusionFilter) return;
        this.exclusionFilter = exclusionFilter;
        firePropertyChange (
            PROP_IS_EXCLUSION_FILTER, 
            new Boolean (!exclusionFilter), 
            new Boolean (exclusionFilter)
        );
    }

    /**
     * Returns class filter.
     *
     * @return class filter
     */
    public String getClassNameFilter () {
        return classFilter;
    }

    /**
     * Sets class filter.
     *
     * @param classFilter a new value of class filter property
     */
    public void setClassNameFilter (String classFilter) {
        if (classFilter != null) {
            classFilter = classFilter.trim();
        }
        if ( (this.classFilter == classFilter) ||
             ( (this.classFilter != null) && 
               (classFilter != null) && 
               classFilter.equals (this.classFilter)
             )
        ) return;
        String old = this.classFilter;
        this.classFilter = classFilter;
        firePropertyChange (PROP_CLASS_NAME_FILTER, old, classFilter);    }
}
