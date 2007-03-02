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

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.debugger.Breakpoint;

/**
 * Notifies about variable change or access events.
 *
 * <br><br>
 * <b>How to use it:</b>
 * <pre style="background-color: rgb(255, 255, 153);">
 *    DebuggerManager.addBreakpoint (FieldBreakpoint.create (
 *        "org.netbeans.modules.editor.EditorPanel",
 *        "state",
 *        FieldBreakpoint.TYPE_MODIFICATION
 *    ));</pre>
 * This breakpoint stops when state field of EditorPanel class is modified.
 *
 * @author Jan Jancura
 */
public class FieldBreakpoint extends JPDABreakpoint {

    /** Property name constant. */
    public static final String      PROP_FIELD_NAME = "fieldName"; // NOI18N
    /** Property name constant. */
    public static final String      PROP_CLASS_NAME = "className"; // NOI18N
    /** Property name constant. */
    public static final String      PROP_CONDITION = "condition"; // NOI18N
    /** Property name constant. */
    public static final String      PROP_BREAKPOINT_TYPE = "breakpointType"; // NOI18N
    
    /** Property type value constant. */
    public static final int         TYPE_ACCESS = 1;
    /** Property type value constant. */
    public static final int         TYPE_MODIFICATION = 2;

    private String                  className = "";
    private String                  fieldName = "";
    private int                     type = TYPE_MODIFICATION;
    private String                  condition = ""; // NOI18N

    
    private FieldBreakpoint () {
    }
    
    /**
     * Creates a new breakpoint for given parameters.
     *
     * @param className class name
     * @param fieldName name of field
     * @param breakpointType one of constants: TYPE_ACCESS, 
     *   TYPE_MODIFICATION
     * @return a new breakpoint for given parameters
     */
    public static FieldBreakpoint create (
        String className,
        String fieldName,
        int breakpointType
    ) {
        FieldBreakpoint b = new FieldBreakpointImpl ();
        b.setClassName (className);
        b.setFieldName (fieldName);
        b.setBreakpointType (breakpointType);
        return b;
    }

    /**
     * Get name of class the field is defined in.
     *
     * @return the name of class the field is defined in
     */
    public String getClassName () {
        return className;
    }

    /**
     * Set name of class the field is defined in.
     *
     * @param className a new name of class the field is defined in
     */
    public void setClassName (String className) {
        if ( (className == this.className) ||
             ( (className != null) && 
               (this.className != null) && 
               this.className.equals (className)
             )
        ) return;
        Object old = this.className;
        this.className = className;
        firePropertyChange (PROP_CLASS_NAME, old, className);
    }

    /**
     * Returns name of field.
     *
     * @return a name of field
     */
    public String getFieldName () {
        return fieldName;
    }

    /**
     * Sets name of field.
     *
     * @param name a name of field
     */
    public void setFieldName (String name) {
        if (name != null) {
            name = name.trim();
        }
        if ( (name == fieldName) ||
             ((name != null) && (fieldName != null) && fieldName.equals (name))
        ) return;
        String old = fieldName;
        fieldName = name;
        firePropertyChange (PROP_FIELD_NAME, old, fieldName);
    }

    /**
     * Returns type of breakpoint (one of TYPE_ACCESS and TYPE_MODIFICATION).
     *
     * @return type of breakpoint
     */
    public int getBreakpointType () {
        return type;
    }

    /**
     * Sets type of breakpoint.
     *
     * @param type a new type of breakpoint
     */
    public void setBreakpointType (int type) {
        if (this.type == type) return;
        if ( (type != TYPE_MODIFICATION) &&
                (type != TYPE_ACCESS)
           ) throw new IllegalArgumentException  ();
        int old = this.type;
        this.type = type;
        firePropertyChange (PROP_BREAKPOINT_TYPE, new Integer (old), new Integer (type));
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
     * Returns a string representation of this object.
     *
     * @return  a string representation of the object
     */
    public String toString () {
        return "FieldBreakpoint " + className + "." + fieldName;
    }
    
    private static final class FieldBreakpointImpl extends FieldBreakpoint implements ChangeListener {
        
        public void stateChanged(ChangeEvent chev) {
            Object source = chev.getSource();
            if (source instanceof Breakpoint.VALIDITY) {
                setValidity((Breakpoint.VALIDITY) source, chev.toString());
            } else {
                throw new UnsupportedOperationException(chev.toString());
            }
        }
    }
}
