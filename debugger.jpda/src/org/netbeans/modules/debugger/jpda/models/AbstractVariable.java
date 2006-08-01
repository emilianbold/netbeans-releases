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

package org.netbeans.modules.debugger.jpda.models;

import com.sun.jdi.*;
import java.beans.Customizer;
import java.beans.PropertyChangeEvent;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.io.PushbackReader;
import java.io.StringReader;
import java.io.IOException;

import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.Field;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.Super;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.expr.JDIObjectVariable;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;


/**
 * @author   Jan Jancura
 */
class AbstractVariable implements JDIObjectVariable, Customizer, Cloneable {
    // Customized for add/removePropertyChangeListener
    // Cloneable for fixed watches

    private Value           value;
    private JPDADebuggerImpl debugger;
    private String          id;
    private String          genericType;
    private Field[]         fields;
    private Field[]         staticFields;
    private Field[]         inheritedFields;
    private volatile boolean refreshFields;
    
    private Set listeners = new HashSet();
    private DebuggetStateListener stateChangeListener = new DebuggetStateListener();

    
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
        debugger.addPropertyChangeListener(JPDADebugger.PROP_STATE,
                WeakListeners.propertyChange(stateChangeListener, debugger));
    }

    AbstractVariable (JPDADebuggerImpl debugger, Value value, String genericSignature,
                      String id) {
        this.debugger = debugger;
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
        debugger.addPropertyChangeListener(JPDADebugger.PROP_STATE,
                WeakListeners.propertyChange(stateChangeListener, debugger));
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
            return "\'" + convertToCharInitializer (v.toString ()) + "\'";
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
        Value value = debugger.evaluateIn (expression);
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
    * Returns string representation of type of this variable.
    *
    * @return string representation of type of this variable.
    */
    public int getFieldsCount () {
        Value v = getInnerValue ();
        if (v == null) return 0;
        if (v instanceof ArrayReference) {
            try {
                return ((ArrayReference) v).length ();
            } catch (ObjectCollectedException ocex) {
                return 0;
            }
        } else {
            if (fields == null || refreshFields) {
                initFields ();
            }
            return fields.length;
        }
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
        com.sun.jdi.Field f;
        try {
            f = ((ReferenceType) this.getInnerValue().type()).fieldByName(name);
        } catch (ObjectCollectedException ocex) {
            return null;
        }
        if (f == null) return null;
        return this.getField (
            f, 
            (ObjectReference) getInnerValue (),
            getID()
        );
    }
    
    /**
     * Returns all fields declared in this type that are in interval
     * &lt;<code>from</code>, <code>to</code>).
     */
    public Field[] getFields (int from, int to) {
        Value v = getInnerValue ();
        if (v == null) return new Field[] {};
        try {
            if (v instanceof ArrayReference && (from > 0 || to < ((ArrayReference) v).length())) {
                // compute only requested elements
                Type type = v.type ();
                ReferenceType rt = (ReferenceType) type;
                if (to == 0) to = ((ArrayReference) v).length();
                Field[] elements = getFieldsOfArray (
                        (ArrayReference) v, 
                        ((ArrayType) rt).componentTypeName (),
                        this.getID (),
                        from, to);
                return elements;
            } else {
                //either the fields are cached or we have to init them
                if (fields == null || refreshFields) {
                    initFields ();
                }
                if (to != 0) {
                    to = Math.min(fields.length, to);
                    from = Math.min(fields.length, from);
                    Field[] fv = new Field [to - from];
                    System.arraycopy (fields, from, fv, 0, to - from);
                    return fv;
                }
                return fields;
            }
        } catch (ObjectCollectedException ocex) {
            return new Field[] {};
        }
    }
        
    /**
     * Return all static fields.
     *
     * @return all static fields
     */
    public Field[] getAllStaticFields (int from, int to) {
        Value v = getInnerValue ();
        if (v == null || v instanceof ArrayReference) {
            return new Field[] {};
        }
        if (fields == null || refreshFields) {
            initFields ();
        }
        if (to != 0) {
            to = Math.min(staticFields.length, to);
            from = Math.min(staticFields.length, from);
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
        Value v = getInnerValue ();
        if (v == null || v instanceof ArrayReference) {
            return new Field[] {};
        }
        if (fields == null || refreshFields) {
            initFields ();
        }
        if (to != 0) {
            to = Math.min(inheritedFields.length, to);
            from = Math.min(inheritedFields.length, from);
            FieldVariable[] fv = new FieldVariable [to - from];
            System.arraycopy (inheritedFields, from, fv, 0, to - from);
            return fv;
        }
        return inheritedFields;
    }

    public Super getSuper () {
        if (getInnerValue () == null) 
            return null;
        try {
            Type t = this.getInnerValue().type();
            if (!(t instanceof ClassType)) 
                return null;
            ClassType superType = ((ClassType) t).superclass ();
            if (superType == null) 
                return null;
            return new SuperVariable(
                    debugger, 
                    (ObjectReference) this.getInnerValue(),
                    superType,
                    this.id
                    );
        } catch (ObjectCollectedException ocex) {
            return null;
        }
    }
    
    /**
     * Calls {@link java.lang.Object#toString} in debugged JVM and returns
     * its value.
     *
     * @return toString () value of this instance
     */
    public String getToStringValue () throws InvalidExpressionException {
        try {
            Value v = getInnerValue ();
            if (v == null) return null;
            
            if (!(v.type () instanceof ClassType)) 
                return getValue ();
            if (v instanceof CharValue)
                return "\'" + convertToCharInitializer (v.toString ()) + "\'";
            if (v instanceof StringReference)
                return "\"" + convertToStringInitializer (
                    ((StringReference) v).value ()
                ) + "\"";
            Method toStringMethod = ((ClassType) v.type ()).
                concreteMethodByName ("toString", "()Ljava/lang/String;");
            StringReference sr = (StringReference) debugger.invokeMethod (
                (ObjectReference) v,
                toStringMethod,
                new Value [0]
            );
            if (sr == null) {
                return null;
            } else {
                return sr.value ();
            }
        } catch (VMDisconnectedException ex) {
            return NbBundle.getMessage(AbstractVariable.class, "MSG_Disconnected");
        } catch (ObjectCollectedException ocex) {
            return NbBundle.getMessage(AbstractVariable.class, "MSG_ObjCollected");
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
                    if ( !((Method) l.get (j)).isAbstract () &&
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
            Value v = debugger.invokeMethod (
                (ObjectReference) this.getInnerValue(),
                method,
                vs
            );
            
            // 4) encapsulate result
            if (v instanceof ObjectReference)
                return new AbstractVariable ( // It's also ObjectVariable
                        debugger,
                        (ObjectReference) v,
                        id + method + "^"
                    );
            return new AbstractVariable (debugger, v, id + method);
        } catch (VMDisconnectedException ex) {
            return null;
        } catch (ObjectCollectedException ocex) {
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
        fields = null;
        staticFields = null;
        inheritedFields = null;
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
        refreshFields = false;
        Value value = getInnerValue();
        Type type;
        if (value != null) {
            try {
                type = getInnerValue ().type ();
            } catch (ObjectCollectedException ocex) {
                type = null;
            }
        } else {
            type = null;
        }
        if ( !(getInnerValue() instanceof ObjectReference) || 
             !(type instanceof ReferenceType)
        ) {
            this.fields = new Field [0];
            this.staticFields = new Field [0];
            this.inheritedFields = new Field [0];
        } else {
            try {
                ObjectReference or = (ObjectReference) this.getInnerValue();
                ReferenceType rt = (ReferenceType) type;
                if (or instanceof ArrayReference) {
                    this.fields = getFieldsOfArray (
                        (ArrayReference) or, 
                        ((ArrayType) rt).componentTypeName (),
                        this.getID (),
                        0, ((ArrayReference) or).length());
                    this.staticFields = new Field[0];
                    this.inheritedFields = new Field[0];
                }
                else {
                    initFieldsOfClass(or, rt, this.getID ());
                }
            } catch (ObjectCollectedException ocex) {
                // The object is gone => no fields
            }
        }
    }

    private Field[] getFieldsOfArray (
            ArrayReference ar, 
            String componentType,
            String parentID,
            int from,
            int to
        ) {
            List l;
            try {
                l = ar.getValues(from, to - from);
            } catch (ObjectCollectedException ocex) {
                l = java.util.Collections.EMPTY_LIST;
            }
            int i, k = l.size ();
            Field[] ch = new Field [k];
            for (i = 0; i < k; i++) {
                Value v = (Value) l.get (i);
                ch [i] = (v instanceof ObjectReference) ?
                    new ObjectArrayFieldVariable (
                        debugger, 
                        (ObjectReference) v, 
                        componentType, 
                        ar, 
                        from + i,
                        to - 1,
                        parentID
                    ) :
                    new ArrayFieldVariable (
                        debugger, 
                        v, 
                        componentType, 
                        ar, 
                        from + i,
                        to - 1,
                        parentID
                    );
            }
            return ch;
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
        Value v;
        try {
            v = or.getValue (f);
        } catch (ObjectCollectedException ocex) {
            v = null;
        }
        if ( (v == null) || (v instanceof ObjectReference))
            return new ObjectFieldVariable (
                debugger,
                (ObjectReference) v,
                f,
                parentID,
                JPDADebuggerImpl.getGenericSignature(f),
                or
            );
        return new FieldVariable (debugger, v, f, parentID, or);
    }
    
    public Variable clone() {
        AbstractVariable clon = new AbstractVariable(debugger, value, id);
        clon.genericType = this.genericType;
        return clon;
    }
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
        listeners.add(l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
        listeners.remove(l);
    }
    
    public String toString () {
        return "ObjectVariable ";
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
    
    private class DebuggetStateListener extends Object implements PropertyChangeListener {
        
        public void propertyChange(PropertyChangeEvent evt) {
            if (JPDADebugger.PROP_STATE.equals(evt.getPropertyName())) {
                Object newValue = evt.getNewValue();
                if (newValue instanceof Integer &&
                    JPDADebugger.STATE_RUNNING == ((Integer) newValue).intValue()) {
                    AbstractVariable.this.refreshFields = true;
                }
            }
        }
        
    }
    
}

