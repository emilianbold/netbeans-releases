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

package org.netbeans.modules.debugger.jpda.models;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.CharValue;
import com.sun.jdi.ClassObjectReference;
import com.sun.jdi.ClassType;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.PrimitiveValue;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StringReference;
import com.sun.jdi.Type;
import com.sun.jdi.Value;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.Field;
import org.netbeans.api.debugger.jpda.Super;
import org.netbeans.api.debugger.jpda.Variable;


/**
 * @author   Jan Jancura
 */
public class AbstractVariable implements Variable {

    private Value value;
    private LocalsTreeModel model;
    private String id;
    

    AbstractVariable (
        LocalsTreeModel model,
        Value value,
        String id
    ) {
        this.model = model;
        this.value = value;
        this.id = id;
        if (this.id == null)
            this.id = "" + super.hashCode ();
    }
    
    
    // public interface ........................................................
    
    /**
    * Returns string representation of type of this variable.
    *
    * @return string representation of type of this variable.
    */
    public String getValue () {
        Value v = getInnerValue ();
        if (v == null) return "null";
        if (v instanceof CharValue)
            return "\'" + v.toString () + "\'";
        if (v instanceof PrimitiveValue)
            return v.toString ();
        if (v instanceof StringReference)
            return "\"" + ((StringReference) v).value () + "\"";
        if (v instanceof ClassObjectReference)
            return "class " + ((ClassObjectReference) v).reflectedType ().name ();
        if (v instanceof ArrayReference)
            return "#" + ((ArrayReference) v).uniqueID () + 
                "(length=" + ((ArrayReference) v).length () + ")";
        return "#" + ((ObjectReference) v).uniqueID ();
    }

    /**
    * Returns string representation of type of this variable.
    *
    * @return string representation of type of this variable.
    */
    public void setValue (String value) throws InvalidExpressionException {
        setInnerValue (
            model.getDebugger ().evaluateIn (value)
        );
    }
    
    /**
    * Returns string representation of type of this variable.
    *
    * @return string representation of type of this variable.
    */
    public int getFieldsCount () {
        if (getInnerValue () == null) return 0;
        return ((ReferenceType) getInnerValue ().type ()).fields ().size ();
    }

    /**
     * Returns field defined in this object.
     *
     * @param name a name of field to be returned
     *
     * @return field defined in this object
     */
    public Field getField (String name) {
        if (getInnerValue () == null) return null;
        com.sun.jdi.Field f = ((ReferenceType) getInnerValue ().type ()).
            fieldByName (name);
        if (f == null) return null;
        return getModel ().getField (
            f, 
            (ObjectReference) getInnerValue (),
            ((ReferenceType) getInnerValue ().type ()).name (),
            id
        );
    }

    /**
     * Return field of given name.
     *
     * @param fieldName name of field
     * @return field of given name
     */
    public Field[] getFields (int from, int to) {
        if (getInnerValue () == null) return new Field [0];
        AbstractVariable[] vs = getModel ().getFields (
            this, false
        );
        Field[] fs = new Field [vs.length];
        System.arraycopy (vs, 0, fs, 0, vs.length);
        return fs;
    }
    
    public Super getSuper () {
        if (getInnerValue () == null) return null;
        Type t = getInnerValue ().type ();
        if (!(t instanceof ClassType)) return null;
        ClassType s = ((ClassType) getInnerValue ().type ()).superclass ();
        if (s == null) return null;
        return getModel ().getSuper (
            s, 
            (ObjectReference) getInnerValue (),
            id
        );
    }
    
    /**
     * Calls {@link java.lang.Object#toString} in debugged JVM and returns
     * its value.
     *
     * @return toString () value of this instance
     */
    public String getToStringValue () {
        if (value == null) return null;
        if (!(value.type () instanceof ClassType)) return getValue ();
        Method toStringMethod = ((ClassType) value.type ()).
            concreteMethodByName ("toString", "()Ljava/lang/String;");
        try {
            return ((StringReference) model.getDebugger ().invokeMethod (
                (ObjectReference) value,
                toStringMethod,
                new Value [0]
            )).value ();
        } catch (InvalidExpressionException e) {
            e.printStackTrace( );
            return null;
        }
    }
    
    /**
     * Calls given method in debugged JVM on this instance and returns
     * its value.
     *
     * @param methodName a name of method to be called
     * @param signature a signature of method to be called
     * @param arguments a arguments to be used
     *
     * @return value of given method call on this instance
     */
    public Variable invokeMethod (
        String methodName,
        String signature,
        Variable[] arguments
    ) throws NoSuchMethodException {
        if (value == null) return null;
        Method method = ((ClassType) value.type ()).
            concreteMethodByName (methodName, signature);
        if (method == null) {
            List l = ((ClassType) value.type ()).
                methodsByName (methodName);
            int j, jj = l.size ();
            for (j = 0; j < jj; j++)
                System.out.println( ((Method) l.get (j)).signature ());
            throw new NoSuchMethodException (
                value.type ().name () + "." + methodName + " : " + signature
            );
        }
        Value[] vs = new Value [arguments.length];
        int i, k = arguments.length;
        for (i = 0; i < k; i++)
            vs [i] = ((AbstractVariable) arguments [i]).getInnerValue ();
        try {
            Value v = model.getDebugger ().invokeMethod (
                (ObjectReference) value,
                method,
                vs
            );
            if (v instanceof ObjectReference)
                return new ObjectVariable (
                    model,
                    (ObjectReference) v,
                    id + method + "^"
                );
            else
                return new AbstractVariable (
                    model,
                    v,
                    id + method
                );
        } catch (InvalidExpressionException e) {
            e.printStackTrace( );
            return null;
        }
    }
    
    /**
     * Declared type of this local.
     *
     * @return declared type of this local
     */
    public String getType () {
        if (getInnerValue () == null) return null;
        return getInnerValue ().type ().name ();
    }

    
    // other methods............................................................
    
    Value getInnerValue () {
        return value;
    }
    
    void setInnerValue (Value v) {
        value = v;
    }
    
    LocalsTreeModel getModel () {
        return model;
    }
    
    String getID () {
        return id;
    }
    
    public boolean equals (Object o) {
        return  (o instanceof AbstractVariable) &&
                (id.equals (((AbstractVariable) o).id));
    }
    
    public int hashCode () {
        return id.hashCode ();
    }
}

