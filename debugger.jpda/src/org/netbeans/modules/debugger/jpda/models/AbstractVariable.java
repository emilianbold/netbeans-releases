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

import com.sun.jdi.*;

import java.util.List;
import java.io.PushbackReader;
import java.io.StringReader;
import java.io.IOException;

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
    private String genericType;


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

    AbstractVariable (LocalsTreeModel model, Value value, String genericSignature, String id) {
        this.model = model;
        this.value = value;
        try {
            if (genericSignature != null) {
                this.genericType = getTypeDescription(new PushbackReader(new StringReader(genericSignature), 1));
            }
        } catch (IOException e) {
            /// invalid signature
        }
        this.id = id;
        if (this.id == null)
            this.id = "" + super.hashCode ();
    }

    private static String getTypeDescription(PushbackReader signature) throws IOException {
        int c = signature.read();
        switch (c) {
        case 'Z':
            return "boolean";
        case 'B':
            return "byte";
        case 'C':
            return "char";
        case 'S':
            return "short";
        case 'I':
            return "int";
        case 'J':
            return "long";
        case 'F':
            return "float";
        case 'D':
            return "double";
        case '[':
        {
            int arrayCount = 1;
            for (; ;arrayCount++) {
                if ((c = signature.read()) != '[') {
                    signature.unread(c);
                    break;
                }
            }
            return getTypeDescription(signature) + " " + brackets(arrayCount);
        }
        case 'L':
        {
            StringBuffer typeName = new StringBuffer(50);
            for (;;) {
                c = signature.read();
                if (c == ';') {
                    int idx = typeName.lastIndexOf("/");
                    return idx == -1 ? typeName.toString() : typeName.substring(idx + 1);
                }
                else if (c == '<') {
                    int idx = typeName.lastIndexOf("/");
                    if (idx != -1) typeName.delete(0, idx + 1);
                    typeName.append("<");
                    for (;;) {
                        String td = getTypeDescription(signature);
                        typeName.append(td);
                        c = signature.read();
                        if (c == '>') break;
                        signature.unread(c);
                        typeName.append(',');
                    }
                    signature.read();   // should be a semicolon
                    typeName.append(">");
                    return typeName.toString();
                }
                typeName.append((char)c);
            }
        }
        }
        throw new IOException();
    }

    private static String brackets(int arrayCount) {
        StringBuffer sb = new StringBuffer(arrayCount * 2);
        do {
            sb.append("[]");
        } while (--arrayCount > 0);
        return sb.toString();
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
        if (v instanceof VoidValue) return "void";
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
    * Sets string representation of value of this variable.
    *
    * @param value string representation of value of this variable.
    */
    public void setValue (String expression) throws InvalidExpressionException {
        // evaluate expression to Value
        Value value = model.getDebugger ().evaluateIn (expression);
        // set new value to remote veriable
        setValue (value);
        // set new value to this model
        setInnerValue (value);
        // refresh tree
        model.fireNodeChanged (this);
    }
    
    protected void setValue (Value value) throws InvalidExpressionException {
        throw new InternalError ();
    }
    
    /**
    * Returns string representation of type of this variable.
    *
    * @return string representation of type of this variable.
    */
    public int getFieldsCount () {
        Value v = getInnerValue ();
        if (v == null) return 0;
        if (v instanceof ArrayReference)
            return ((ArrayReference) v).length ();
        return ((ReferenceType) v.type ()).fields ().size ();
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
            //((ReferenceType) getInnerValue ().type ()).name (),
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
            this, false, from, to
        );
        Field[] fs = new Field [vs.length];
        System.arraycopy (vs, 0, fs, 0, vs.length);
        return fs;
    }

    /**
     * Return all static fields.
     *
     * @return all static fields
     */
    public Field[] getAllStaticFields (int from, int to) {
        return getModel ().getAllStaticFields (this, from, to);
    }

    /**
     * Return all inherited fields.
     *
     * @return all inherited fields
     */
    public Field[] getInheritedFields (int from, int to) {
        return getModel ().getInheritedFields (this, from, to);
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
    public String getToStringValue () throws InvalidExpressionException {
        if (value == null) return null;
        if (!(value.type () instanceof ClassType)) return getValue ();
        if (value instanceof StringReference)
            return "\"" + ((StringReference) value).value () + "\"";
        Method toStringMethod = ((ClassType) value.type ()).
            concreteMethodByName ("toString", "()Ljava/lang/String;");
        return ((StringReference) model.getDebugger ().invokeMethod (
            (ObjectReference) value,
            toStringMethod,
            new Value [0]
        )).value ();
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
    ) throws NoSuchMethodException, InvalidExpressionException {
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
    }
    
    /**
     * Declared type of this local.
     *
     * @return declared type of this local
     */
    public String getType () {
        if (genericType != null) return genericType;
        if (getInnerValue () == null) return "";
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

