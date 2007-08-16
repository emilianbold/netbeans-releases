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
import com.sun.jdi.ArrayType;
import com.sun.jdi.CharValue;
import com.sun.jdi.ClassType;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.PrimitiveValue;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StringReference;
import com.sun.jdi.Type;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.Value;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.io.PushbackReader;
import java.io.StringReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.api.debugger.jpda.Field;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.Super;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.Java6Methods;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;


/**
 * @author   Jan Jancura
 */
class AbstractObjectVariable extends AbstractVariable implements ObjectVariable {
    // Customized for add/removePropertyChangeListener
    // Cloneable for fixed watches
    
    private static final Logger logger = Logger.getLogger("org.netbeans.modules.debugger.jpda.getValue"); // NOI8N

    private String          genericType;
    private Field[]         fields;
    private Field[]         staticFields;
    private Field[]         inheritedFields;
    private volatile boolean refreshFields;
    
    private DebuggetStateListener stateChangeListener = new DebuggetStateListener();

    
    AbstractObjectVariable (
        JPDADebuggerImpl debugger,
        Value value,
        String id
    ) {
        super(debugger, value, id);
        debugger.addPropertyChangeListener(
                WeakListeners.propertyChange(stateChangeListener, debugger));
    }

    AbstractObjectVariable (JPDADebuggerImpl debugger, Value value, String genericSignature,
                      String id) {
        this(debugger, value, id);
        try {
            if (genericSignature != null) {
                this.genericType = getTypeDescription(new PushbackReader(new StringReader(genericSignature), 1));
            }
        } catch (IOException e) {
            /// invalid signature
        }
    }

    
    // public interface ........................................................
    
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
                    getDebugger(), 
                    (ObjectReference) this.getInnerValue(),
                    superType,
                    getID()
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
        Value v = getInnerValue ();
        return getToStringValue(v, getDebugger());
    }
    
    static String getToStringValue (Value v, JPDADebuggerImpl debugger) throws InvalidExpressionException {
        if (v == null) return null;
        try {
            if (!(v.type () instanceof ClassType)) 
                return AbstractVariable.getValue (v);
            if (v instanceof CharValue)
                return "\'" + v.toString () + "\'";
            if (v instanceof StringReference)
                return "\"" +
                    ((StringReference) v).value ()
                    + "\"";
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
            Value v = getDebugger().invokeMethod (
                (ObjectReference) this.getInnerValue(),
                method,
                vs
            );
            
            // 4) encapsulate result
            if (v instanceof ObjectReference)
                return new AbstractObjectVariable ( // It's also ObjectVariable
                        getDebugger(),
                        (ObjectReference) v,
                        getID() + method + "^"
                    );
            return new AbstractVariable (getDebugger(), v, getID() + method);
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
    
    public JPDAClassType getClassType() {
        Value value = getInnerValue();
        if (value == null) return null;
        com.sun.jdi.Type type = value.type();
        if (type instanceof ReferenceType) {
            return new JPDAClassTypeImpl(getDebugger(), (ReferenceType) type);
        } else {
            return null;
        }
    }
    
    public boolean equals (Object o) {
        return  (o instanceof AbstractObjectVariable) &&
                (getID().equals (((AbstractObjectVariable) o).getID()));
    }
    
    
    // other methods............................................................
    
    protected void setInnerValue (Value v) {
        super.setInnerValue(v);
        fields = null;
        staticFields = null;
        inheritedFields = null;
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
                        getDebugger(), 
                        (ObjectReference) v, 
                        componentType, 
                        ar, 
                        from + i,
                        to - 1,
                        parentID
                    ) :
                    new ArrayFieldVariable (
                        getDebugger(), 
                        (PrimitiveValue) v, 
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
        List<Field> fields = new ArrayList<Field>();
        List<Field> staticFields = new ArrayList<Field>();
        List<Field> allInheretedFields = new ArrayList<Field>();
        
        List<com.sun.jdi.Field> l = rt.allFields ();
        Set<com.sun.jdi.Field> s = new HashSet<com.sun.jdi.Field>(rt.fields ());

        int i, k = l.size();
        for (i = 0; i < k; i++) {
            com.sun.jdi.Field f = l.get (i);
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
        this.fields = fields.toArray (new Field [fields.size ()]);
        this.inheritedFields = allInheretedFields.toArray (
            new Field [allInheretedFields.size ()]
        );
        this.staticFields = staticFields.toArray
                (new Field [staticFields.size ()]);
    }
    
    org.netbeans.api.debugger.jpda.Field getField (
        com.sun.jdi.Field f, 
        ObjectReference or, 
        String parentID
    ) {
        Value v;
        try {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("STARTED : "+or+".getValue("+f+")");
            }
            v = or.getValue (f);
        } catch (ObjectCollectedException ocex) {
            v = null;
        }
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("FINISHED: "+or+".getValue("+f+") = "+v);
        }
        if ( (v == null) || (v instanceof ObjectReference))
            return new ObjectFieldVariable (
                getDebugger(),
                (ObjectReference) v,
                f,
                parentID,
                JPDADebuggerImpl.getGenericSignature(f),
                or
            );
        return new FieldVariable (getDebugger(), (PrimitiveValue) v, f, parentID, or);
    }
    
    public List<ObjectVariable> getReferringObjects(long maxReferrers) {
        Value v = getJDIValue();
        if (v instanceof ObjectReference) {
            if (Java6Methods.isJDK6()) {
                final String name = Long.toString(getUniqueID());
                final List<ObjectReference> referrers = Java6Methods.referringObjects((ObjectReference) v, maxReferrers);
                return new AbstractList<ObjectVariable>() {
                    public ObjectVariable get(int i) {
                        ObjectReference obj = referrers.get(i);
                        return new AbstractObjectVariable(getDebugger(), obj, name+" referrer "+i);
                    }

                    public int size() {
                        return referrers.size();
                    }
                };
            } else {
                return Collections.emptyList();
            }
        } else {
            return Collections.emptyList();
        }
    }
    
    public long getUniqueID() {
        Value value = getJDIValue();
        if (!(value instanceof ObjectReference)) { // null or anything else than Object
            return 0L;
        } else {
            return ((ObjectReference) value).uniqueID();
        }
    }
    
    private int cloneNumber = 1;
    
    public Variable clone() {
        AbstractObjectVariable clon = new AbstractObjectVariable(getDebugger(), getJDIValue(), getID() + "_clone"+(cloneNumber++));
        clon.genericType = this.genericType;
        return clon;
    }
    
    public String toString () {
        return "ObjectVariable ";
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
    
    private class DebuggetStateListener extends Object implements PropertyChangeListener {
        
        public void propertyChange(PropertyChangeEvent evt) {
            if (JPDADebugger.PROP_STATE.equals(evt.getPropertyName())) {
                Object newValue = evt.getNewValue();
                if (newValue instanceof Integer &&
                    JPDADebugger.STATE_RUNNING == ((Integer) newValue).intValue()) {
                    AbstractObjectVariable.this.refreshFields = true;
                }
            }
        }
        
    }
    
}
