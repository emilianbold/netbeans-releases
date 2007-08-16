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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.debugger.jpda.models;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.CharValue;
import com.sun.jdi.ClassObjectReference;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.PrimitiveValue;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StringReference;
import com.sun.jdi.Value;
import com.sun.jdi.VoidValue;
import com.sun.jdi.VMDisconnectedException;
import java.beans.Customizer;
import java.beans.PropertyChangeEvent;

import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Set;

import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.expr.JDIVariable;
import org.openide.util.NbBundle;


/**
 * @author   Jan Jancura
 */
class AbstractVariable implements JDIVariable, Customizer, Cloneable {
    // Customized for add/removePropertyChangeListener
    // Cloneable for fixed watches
    
    private Value   value;
    private JPDADebuggerImpl debugger;
    private String          id;
    
    private Set<PropertyChangeListener> listeners = new HashSet<PropertyChangeListener>();

    
    AbstractVariable (
        JPDADebuggerImpl debugger,
        Value value,
        String id
    ) {
        this.debugger = debugger;
        this.value = value;
        this.id = id;
        if (this.id == null)
            this.id = Integer.toString(super.hashCode());
    }

    
    // public interface ........................................................
    
    /**
    * Returns string representation of type of this variable.
    *
    * @return string representation of type of this variable.
    */
    public String getValue () {
        Value v = getInnerValue ();
        return getValue(v);
    }
    
    static String getValue (Value v) {
        if (v == null) return "null";
        if (v instanceof VoidValue) return "void";
        if (v instanceof CharValue)
            return "\'" + v.toString () + "\'";
        if (v instanceof PrimitiveValue)
            return v.toString ();
        if (v instanceof StringReference)
            return "\"" +
                ((StringReference) v).value ()
                + "\"";
        if (v instanceof ClassObjectReference)
            return "class " + ((ClassObjectReference) v).reflectedType ().name ();
        if (v instanceof ArrayReference)
            return "#" + ((ArrayReference) v).uniqueID () + 
                "(length=" + ((ArrayReference) v).length () + ")";
        return "#" + ((ObjectReference) v).uniqueID ();
    }

    /**
    * Sets string representation of value of this variable.
    *
    * @param value string representation of value of this variable.
    */
    public void setValue (String expression) throws InvalidExpressionException {
        String oldValue = getValue();
        if (expression.equals(oldValue)) {
            return ; // Do nothing, since the values are identical
        }
        Value value;
        Value oldV = getInnerValue();
        if (oldV instanceof CharValue && expression.startsWith("'") && expression.endsWith("'") && expression.length() > 1) {
            value = oldV.virtualMachine().mirrorOf(expression.charAt(1));
        } else if (oldV instanceof StringReference && expression.startsWith("\"") && expression.endsWith("\"") && expression.length() > 1) {
            value = oldV.virtualMachine().mirrorOf(expression.substring(1, expression.length() - 1));
        } else {
            // evaluate expression to Value
            Value evaluatedValue = debugger.evaluateIn (expression);
            if (!(evaluatedValue instanceof PrimitiveValue)) {
                throw new InvalidExpressionException(expression);
            }
            value = (PrimitiveValue) evaluatedValue;
        }
        // set new value to remote veriable
        setValue (value);
        // set new value to this model
        setInnerValue (value);
        // refresh tree
        PropertyChangeEvent evt = new PropertyChangeEvent(this, "value", null, value);
        Object[] ls;
        synchronized (listeners) {
            ls = listeners.toArray();
        }
        for (int i = 0; i < ls.length; i++) {
            ((PropertyChangeListener) ls[i]).propertyChange(evt);
        }
        //pchs.firePropertyChange("value", null, value);
        //getModel ().fireTableValueChangedChanged (this, null);
    }
    
    /**
     * Override, but do not call directly!
     */
    protected void setValue (Value value) throws InvalidExpressionException {
        throw new InternalError ();
    }
    
    public void setObject(Object bean) {
        try {
            if (bean instanceof String) {
                setValue((String) bean);
            //} else if (bean instanceof Value) {
            //    setValue((Value) bean); -- do not call directly
            } else {
                throw new IllegalArgumentException(""+bean);
            }
        } catch (InvalidExpressionException ieex) {
            IllegalArgumentException iaex = new IllegalArgumentException(ieex.getLocalizedMessage());
            iaex.initCause(ieex);
            throw iaex;
        }
    }

    /**
     * Declared type of this local.
     *
     * @return declared type of this local
     */
    public String getType () {
        if (getInnerValue () == null) return "";
        try {
            return this.getInnerValue().type().name ();
        } catch (VMDisconnectedException vmdex) {
            // The session is gone.
            return NbBundle.getMessage(AbstractVariable.class, "MSG_Disconnected");
        } catch (ObjectCollectedException ocex) {
            // The object is gone.
            return NbBundle.getMessage(AbstractVariable.class, "MSG_ObjCollected");
        }
    }
    
    public JPDAClassType getClassType() {
        Value value = getInnerValue();
        if (value == null) return null;
        com.sun.jdi.Type type = value.type();
        if (type instanceof ReferenceType) {
            return new JPDAClassTypeImpl(debugger, (ReferenceType) type);
        } else {
            return null;
        }
    }
    
    public boolean equals (Object o) {
        return  (o instanceof AbstractVariable) &&
                (id.equals (((AbstractVariable) o).id));
    }
    
    public int hashCode () {
        return id.hashCode ();
    }

    
    // other methods............................................................
    
    protected Value getInnerValue () {
        return value;
    }
    
    protected void setInnerValue (Value v) {
        value = v;
    }
    
    public Value getJDIValue() {
        return value;
    }
    
    protected final JPDADebuggerImpl getDebugger() {
        return debugger;
    }
    
    protected final String getID () {
        return id;
    }
    
    private int cloneNumber = 1;
    
    public Variable clone() {
        AbstractVariable clon = new AbstractVariable(debugger, value, id + "_clone"+(cloneNumber++));
        return clon;
    }
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
        listeners.add(l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
        listeners.remove(l);
    }
    
    public String toString () {
        return "Variable ";
    }
    
    /* Uncomment when needed. Was used to create "readable" String and Char values.
    private static String convertToStringInitializer (String s) {
        StringBuffer sb = new StringBuffer ();
        int i, k = s.length ();
        for (i = 0; i < k; i++)
            switch (s.charAt (i)) {
                case '\b':
                    sb.append ("\\b");
                    break;
                case '\f':
                    sb.append ("\\f");
                    break;
                case '\\':
                    sb.append ("\\\\");
                    break;
                case '\t':
                    sb.append ("\\t");
                    break;
                case '\r':
                    sb.append ("\\r");
                    break;
                case '\n':
                    sb.append ("\\n");
                    break;
                case '\"':
                    sb.append ("\\\"");
                    break;
                default:
                    sb.append (s.charAt (i));
            }
        return sb.toString();
    }
    
    private static String convertToCharInitializer (String s) {
        StringBuffer sb = new StringBuffer ();
        int i, k = s.length ();
        for (i = 0; i < k; i++)
            switch (s.charAt (i)) {
                case '\b':
                    sb.append ("\\b");
                    break;
                case '\f':
                    sb.append ("\\f");
                    break;
                case '\\':
                    sb.append ("\\\\");
                    break;
                case '\t':
                    sb.append ("\\t");
                    break;
                case '\r':
                    sb.append ("\\r");
                    break;
                case '\n':
                    sb.append ("\\n");
                    break;
                case '\'':
                    sb.append ("\\\'");
                    break;
                default:
                    sb.append (s.charAt (i));
            }
        return sb.toString();
    }
     */
    
}

