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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.io.PushbackReader;
import java.io.StringReader;
import java.io.IOException;

import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.Field;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.Super;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;


/**
 * @author   Jan Jancura
 */
public class AbstractVariable implements ObjectVariable {

    private Value           value;
    private LocalsTreeModel model;
    private String          id;
    private String          genericType;
    private Field[]         fields;
    private Field[]         staticFields;
    private Field[]         inheritedFields;

    
    AbstractVariable (
        LocalsTreeModel model,
        Value value,
        String id
    ) {
        this.model = model;
        this.value = value;
        this.id = id;
        if (this.id == null)
            this.id = Integer.toString(super.hashCode());
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
            this.id = Integer.toString (super.hashCode());
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
            return "\'" + convertToStringInitializer (v.toString ()) + "\'";
        if (v instanceof PrimitiveValue)
            return v.toString ();
        if (v instanceof StringReference)
            return "\"" + convertToStringInitializer (
                ((StringReference) v).value ()
            ) + "\"";
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
        Value value = getModel().getDebugger ().evaluateIn (expression);
        // set new value to remote veriable
        setValue (value);
        // set new value to this model
        setInnerValue (value);
        // refresh tree
        getModel().fireNodeChanged (this);
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
        if (fields == null) initFields ();
        Value v = getInnerValue ();
        if (v == null) return 0;
        if (v instanceof ArrayReference)
            return ((ArrayReference) v).length ();
        return fields.length;
    }

    /**
     * Returns field defined in this object.
     *
     * @param name a name of field to be returned
     *
     * @return field defined in this object
     */
    public Field getField (String name) {
        if (getInnerValue() == null) return null;
        com.sun.jdi.Field f = 
            ((ReferenceType) this.getInnerValue().type()).fieldByName (name);
        if (f == null) return null;
        return this.getField (
            f, 
            (ObjectReference) getInnerValue (),
            getID()
        );
    }
    
    /**
     * Returns all fields declared in this type. Or max 50 fields 
     * of an array.
     */
    public Field[] getFields (int from, int to) {
        //either the fields are cached or we have to init them
        if (fields == null) initFields ();
        if (to != 0) {
            Field[] fv = new Field [to - from];
            System.arraycopy (fields, from, fv, 0, to - from);
            return fv;
        }
        return fields;
    }
        
    /**
     * Return all static fields.
     *
     * @return all static fields
     */
    public Field[] getAllStaticFields (int from, int to) {
        if (fields == null) initFields ();
        if (to != 0) {
            FieldVariable[] fv = new FieldVariable [to - from];
            System.arraycopy (staticFields, from, fv, 0, to - from);
            return fv;
        }
        return staticFields;
    }

    /**
     * Return all inherited fields.
     * 
     * @return all inherited fields
     */
    public Field[] getInheritedFields (int from, int to) {
        if (fields == null) initFields ();
        if (to != 0) {
            FieldVariable[] fv = new FieldVariable [to - from];
            System.arraycopy (inheritedFields, from, fv, 0, to - from);
            return fv;
        }
        return inheritedFields;
    }

    public Super getSuper () {
        if (getInnerValue () == null) 
            return null;
        Type t = this.getInnerValue().type();
        if (!(t instanceof ClassType)) 
            return null;
        ClassType superType = ((ClassType) t).superclass ();
        if (superType == null) 
            return null;
        return new SuperVariable(
                this.getModel(), 
                (ObjectReference) this.getInnerValue(),
                superType,
                this.id
                );
    }
    
    /**
     * Calls {@link java.lang.Object#toString} in debugged JVM and returns
     * its value.
     *
     * @return toString () value of this instance
     */
    public String getToStringValue () throws InvalidExpressionException {
        try {
            if (this.getInnerValue() == null) return null;
            if (!(this.getInnerValue().type() instanceof ClassType)) return getValue ();
            if (this.getInnerValue() instanceof StringReference)
                return "\"" + ((StringReference) this.getInnerValue()).value () + "\"";
            Method toStringMethod = ((ClassType) this.getInnerValue().type()).
                concreteMethodByName ("toString", "()Ljava/lang/String;");
            return ((StringReference) getModel().getDebugger ().invokeMethod (
                (ObjectReference) this.getInnerValue(),
                toStringMethod,
                new Value [0]
            )).value ();
        } catch (VMDisconnectedException ex) {
            return "";
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
    ) throws NoSuchMethodException, InvalidExpressionException {
        try {
             
            // 1) find corrent method
            if (this.getInnerValue () == null) return null;
            Method method = null;
            if (signature != null)
                method = ((ClassType) this.getInnerValue ().type ()).
                    concreteMethodByName (methodName, signature);
            else {
                List l = ((ClassType) this.getInnerValue ().type ()).
                    methodsByName (methodName);
                int j, jj = l.size ();
                for (j = 0; j < jj; j++)
                    if ( ((Method) l.get (j)).isAbstract () == false &&
                         ((Method) l.get (j)).argumentTypeNames ().size () == 0
                    ) {
                        method = (Method) l.get (j);
                        break;
                    }
            }
            
            // 2) method not found => print all method signatures
            if (method == null) {
                List l = ((ClassType) this.getInnerValue ().type ()).
                    methodsByName (methodName);
                int j, jj = l.size ();
                for (j = 0; j < jj; j++)
                    System.out.println (((Method) l.get (j)).signature ());
                throw new NoSuchMethodException (
                    this.getInnerValue ().type ().name () + "." + 
                        methodName + " : " + signature
                );
            }
            
            // 3) call this method
            Value[] vs = new Value [arguments.length];
            int i, k = arguments.length;
            for (i = 0; i < k; i++)
                vs [i] = ((AbstractVariable) arguments [i]).getInnerValue ();
            Value v = getModel().getDebugger ().invokeMethod (
                (ObjectReference) this.getInnerValue(),
                method,
                vs
            );
            
            // 4) encapsulate result
            if (v instanceof ObjectReference)
                return new org.netbeans.modules.debugger.jpda.models.
                    ObjectVariable (
                        getModel (),
                        (ObjectReference) v,
                        id + method + "^"
                    );
            return new AbstractVariable (getModel (), v, id + method);
        } catch (VMDisconnectedException ex) {
            return null;
        }
    }
    
    /**
     * Declared type of this local.
     *
     * @return declared type of this local
     */
    public String getType () {
        if (genericType != null) return genericType;
        if (getInnerValue () == null) return "";
        return this.getInnerValue().type().name ();
    }
    
    public boolean equals (Object o) {
        return  (o instanceof AbstractVariable) &&
                (id.equals (((AbstractVariable) o).id));
    }
    
    public int hashCode () {
        return id.hashCode ();
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
    
    private static String getTypeDescription (PushbackReader signature) 
    throws IOException {
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
                    return idx == -1 ? 
                        typeName.toString() : typeName.substring(idx + 1);
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

    private static String brackets (int arrayCount) {
        StringBuffer sb = new StringBuffer (arrayCount * 2);
        do {
            sb.append ("[]");
        } while (--arrayCount > 0);
        return sb.toString ();
    }

    private void initFields () {
        Type type = getInnerValue ().type ();
        if ( !(getInnerValue() instanceof ObjectReference) || 
             !(type instanceof ReferenceType)
        ) {
            this.fields = new Field [0];
            this.staticFields = new Field [0];
            this.inheritedFields = new Field [0];
        } else {
            ObjectReference or = (ObjectReference) this.getInnerValue();
            ReferenceType rt = (ReferenceType) type;
            if (or instanceof ArrayReference) {
                this.initFieldsOfArray (
                    (ArrayReference) or, 
                    ((ArrayType) rt).componentTypeName (),
                    this.getID ());
            }
            else {
                this.initFieldsOfClass(or, rt, this.getID ());
            }
        }
    }

    private void initFieldsOfArray (
            ArrayReference ar, 
            String componentType,
            String parentID
        ) {
            List l = ar.getValues();
            int i, k = l.size ();
            Field[] ch = new Field [k];
            for (i = 0; i < k; i++) {
                Value v = (Value) l.get (i);
                ch [i] = (v instanceof ObjectReference) ?
                    new ObjectArrayFieldVariable (
                        this.getModel (), 
                        (ObjectReference) v, 
                        componentType, 
                        ar, 
                        i, 
                        parentID
                    ) :
                    new ArrayFieldVariable (
                        this.getModel (), 
                        v, 
                        componentType, 
                        ar, 
                        i, 
                        parentID
                    );
            }
            this.fields = ch;
            this.staticFields = new Field[0];
            this.inheritedFields = new Field[0];
        }

    private void initFieldsOfClass (
        ObjectReference or, 
        ReferenceType rt,
        String parentID)
    {
        List fields = new ArrayList();
        List staticFields = new ArrayList();
        List allInheretedFields = new ArrayList();
        
        List l = rt.allFields ();
        Set s = new HashSet (rt.fields ());

        int i, k = l.size();
        for (i = 0; i < k; i++) {
            com.sun.jdi.Field f = (com.sun.jdi.Field) l.get (i);
            Field field = this.getField (f, or, this.getID());
            if (f.isStatic ())
                staticFields.add(field);
            else {
                if (s.contains (f))
                    fields.add(field);
                else
                    allInheretedFields.add(field);
            }
        }
        this.fields = (Field[]) fields.toArray (new Field [fields.size ()]);
        this.inheritedFields = (Field[]) allInheretedFields.toArray (
            new Field [allInheretedFields.size ()]
        );
        this.staticFields = (Field[]) staticFields.toArray 
            (new Field [staticFields.size ()]);
    }
    
    FieldVariable getField (
        com.sun.jdi.Field f, 
        ObjectReference or, 
        String parentID
    ) {
        Value v = or.getValue (f);
        if ( (v == null) || (v instanceof ObjectReference))
            return new ObjectFieldVariable (
                this.getModel(),
                (ObjectReference) v,
                f,
                parentID,
                JPDADebuggerImpl.getGenericSignature(f),
                or
            );
        return new FieldVariable (this.getModel(), v, f, parentID, or);
    }
    
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
        return new String (sb);
    }
}

