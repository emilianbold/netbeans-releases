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
    public static final String          PROP_ALL_METHODS = "allMethods"; // NOI18N
    /** Property name constant. */
    public static final String          PROP_METHOD_ENTRY = "methodEntry"; // NOI18N
    /** Property name constant. */
    public static final String          PROP_CONDITION = "condition"; // NOI18N
    /** Property name constant. */
    public static final String          PROP_ANONYMOUS_INNER = "applyToAnonymousInnerClasses";    //NOI18N
    /** Property name constant */
    public static final String          PROP_CLASS_NAME = "className"; // NOI18N

    /** Property variable. */
    private boolean                     anonymousInnerClasses = true;
    private String                      className = "";
    private String                      methodName = "";
    private boolean                     allMethods = false;
    private boolean                     methodEntry = true;
    private String                      condition = "";
    
    
    private MethodBreakpoint () {
    }
    
    /**
     * Creates a new breakpoint for given parameters.
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
        b.setClassName (className);
        b.setMethodName (methodName);
        return b;
    }

    /**
     * Does the breakpoint apply also to anonymous inner classes
     * of the specified class?
     *
     * @return <TT>true</TT> if the breakpoint applies also
     *    to the class' anonymous inner classes, <TT>false</TT> otherwise
     */
    public boolean getApplyToAnonymousInnerClasses() {
        return anonymousInnerClasses;
    }
    
    /**
     * Specifies whether the breakpoint should apply to the specified class
     * or also to its anonymous inner classes.
     *
     * @param apply whether the breakpoint should be applied to annonymous 
     *   inner classes
     */
    public void setApplyToAnonymousInnerClasses (boolean apply) {
        if (apply == anonymousInnerClasses) {
            return;
        }
        anonymousInnerClasses = apply;
        firePropertyChange (
            PROP_ANONYMOUS_INNER,
            new Boolean (!apply),
            new Boolean (apply)
        );
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
     * If true this breakpoint stops in all methods for given class.
     *
     * @return If true this breakpoint stops in all methods for given class
     */
    public boolean getAllMethods () {
        return allMethods;
    }

    /**
     * Sets all methods property value.
     *
     * @param all a new value of all methods property
     */
    public void setAllMethods (boolean all) {
        if (allMethods == all) return;
        allMethods = all;
        firePropertyChange (PROP_ALL_METHODS, new Boolean (!allMethods), new Boolean (allMethods));
    }
    
    /**
     * If true debuggeing will be stoped on method entry.
     * 
     * @return If true debuggeing will be stoped on method entry
     */
    public boolean getMethodEntry () {
        return methodEntry;
    }

    /**
     * Sets method entry property value.
     *
     * @param methodEntry a new value of method entry property
     */
    public void setMethodEntry (boolean methodEntry) {
        if (this.methodEntry == methodEntry) return;
        this.methodEntry = methodEntry;
        firePropertyChange (
            PROP_METHOD_ENTRY, 
            new Boolean (!methodEntry), 
            new Boolean (methodEntry)
        );
    }

    /**
     * Get name of class to stop on.
     *
     * @return name of class to stop on
     */
    public String getClassName () {
        return className;
    }

    /**
     * Set name of class to stop on.
     *
     * @param cn a new value of class name property
     */
    public void setClassName (String cn) {
        if (cn != null) {
            cn = cn.trim();
        }
        if ( (cn == className) ||
             ((cn != null) && (className != null) && className.equals (cn))
        ) return;
        Object old = className;
        className = cn;
        firePropertyChange (PROP_CLASS_NAME, old, className);
    }

    /**
     * Returns a string representation of this object.
     *
     * @return  a string representation of the object
     */
    public String toString () {
        return "MethodBreakpoint " + className + "." + methodName;
    }
}
