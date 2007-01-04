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

package org.netbeans.modules.cnd.debugger.gdb.models;

//import com.sun.jdi.*;
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

import org.netbeans.modules.cnd.debugger.gdb.InvalidExpressionException;
import org.netbeans.modules.cnd.debugger.gdb.Field;
//import org.netbeans.modules.cnd.debugger.gdb.ObjectVariable;
//import org.netbeans.modules.cnd.debugger.gdb.Super;
import org.netbeans.modules.cnd.debugger.gdb.Variable;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebuggerImpl;

/*
 * AbstractVariable.java
 *
 * @author Nik Molchanov (copied from Jan Jancura's JPDA implementation)
 */
public class AbstractVariable implements //NM ObjectVariable, 
        Variable,  //NM
        Customizer { // Customized for add/removePropertyChangeListener

    private String          name; //NM added
    private String          value; //NM Value          value;
    private GdbDebuggerImpl debugger;
    private String          id;
    private String          genericType;
    private Field[]         fields;
    private Field[]         staticFields;
    private Field[]         inheritedFields;
    
    private Set listeners = new HashSet();
    
    public //NM made public
    AbstractVariable (
        GdbDebuggerImpl debugger,
        String name, //NM added name
        String type, //NM added type
        String value, //NM Value value,
        String id
    ) {
        this.debugger = debugger;
        this.name = name;
        this.genericType = type;
        this.value = value;
        this.id = id;
        if (this.id == null)
            this.id = Integer.toString(super.hashCode());
    }

    AbstractVariable (GdbDebuggerImpl debugger,
                      String name, //NM added name
                      String type, //NM added type
                      String value,  //NM Value value,
                      String genericSignature, 
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
    }
    
    // public interface ........................................................
    
    /**
    * Returns string representation of type of this variable.
    *
    * @return string representation of type of this variable.
    */
    public String getValue () {
        String v = getInnerValue (); return v; //NM
        /*NM TEMPORARY COMMENTED OUT
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
        NM*/
    }

    /**
    * Sets string representation of value of this variable.
    *
    * @param value string representation of value of this variable.
    */
    public void setValue (String expression) throws InvalidExpressionException {
        // evaluate expression to Value
        //NM Value value = debugger.evaluateIn (expression);
        String value = expression;
        // set new value to remote veriable
        //NM setValue (value);
        // Adjust value according to the type
        if(genericType.equals("char *")) { // NOI18N
            // There are 2 values: pointer and string
            // First check if pointer is changed
            // Second check if string value is changed
            // Only one of them can be changed at once.
            String strAddr = null;
            String strValue = null;
            int i = expression.indexOf(' ');
            if (i >= 0) {
                strAddr = expression.substring(0, i);
                strValue = expression.substring(i+1);
            } else {
                if (expression.startsWith("0x")) { // NOI18N
                    strAddr = expression;
                } else {
                    strValue = expression;
                }
            }
            String oldValue = getValue();
            if (strAddr != null) {
                if (!oldValue.startsWith(strAddr)) {
                    // Pointer is changed
                    strAddr = expression;
                    debugger.setVariableValue(name, strAddr);
                }
            }
            // Compare string value
            i = oldValue.indexOf(' ');
            if (i >= 0) {
                String oldstrValue = oldValue.substring(i+1);
                if (!oldstrValue.equals(strValue)) {
                    // String value is changed
                    // Now let's update it in memory byte-by-byte
                    for (int n = 0; n < strValue.length(); n++) {
                        char c = strValue.charAt(n);
                        if (c != oldstrValue.charAt(n)) {
                            if (n < 2) break; // First 2 characters must match (\")
                            int k = n - 2;
                            debugger.setVariableValue(name+"["+k+"]", "'"+c+"'"); // NOI18N
                        }
                    }
                }
            } else {
                // Something wrong
            }
            return;
        }
        debugger.setVariableValue(name, value);
        // set new value to this model
        // setInnerValue (value);
        // refresh tree
/*NM TEMPORARY COMMENTED OUT
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
NM*/
    }

    /**
     * Override, but do not call directly!
     */
/*NM TEMPORARY COMMENTED OUT
    protected void setValue (Value value) throws InvalidExpressionException {
        throw new InternalError ();
    }
NM*/

    public void setObject(Object bean) {
        try {
            if (bean instanceof String) {
                setValue((String) bean);
            //} else if (bean instanceof Value) {
            //    setValue((Value) bean); -- do not call directly
            } else {
                throw new IllegalArgumentException(""+bean); // NOI18N
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
/*NM TEMPORARY COMMENTED OUT
        //NM Value v = getInnerValue ();
        //NM if (v == null) return 0;
        //NM if (v instanceof ArrayReference) {
        //NM     return ((ArrayReference) v).length ();
        //NM } else {
NM*/
        if (fields == null) initFields ();
        return fields.length;
        //NM }
    }
    /**
     * Returns field defined in this object.
     *
     * @param name a name of field to be returned
     *
     * @return field defined in this object
     */
    public Field getField (String name) {
        if (fields == null) return null;
        int i, k = fields.length;
        for (i=0; i < k; i++) {
            Field f = fields[i];
            if (name.equals(f.getName())) {
                return f;
            }
        }        
        return null; // Not found
/*NM TEMPORARY COMMENTED OUT
        com.sun.jdi.Field f = 
            ((ReferenceType) this.getInnerValue().type()).fieldByName (name);
        if (f == null) return null;
        return this.getField (
            f, 
            (ObjectReference) getInnerValue (),
            getID()
        );
 NM*/   
    }
    /**
     * Returns all fields declared in this type that are in interval
     * &lt;<code>from</code>, <code>to</code>).
     */
    public Field[] getFields (int from, int to) {
/*NM TEMPORARY COMMENTED OUT
        Value v = getInnerValue ();
        if (v == null) return new Field[] {};
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
NM*/        
            //either the fields are cached or we have to init them
            if (fields == null) initFields ();
            if (to != 0) {
                to = Math.min(fields.length, to);
                from = Math.min(fields.length, from);
                Field[] fv = new Field [to - from];
                System.arraycopy (fields, from, fv, 0, to - from);
                return fv;
            }
            return fields;
        //NM }
    }
    /**
     * Return all static fields.
     *
     * @return all static fields
     */
/*NM TEMPORARY COMMENTED OUT
    public Field[] getAllStaticFields (int from, int to) {
        Value v = getInnerValue ();
        if (v == null || v instanceof ArrayReference) {
            return new Field[] {};
        }
        if (fields == null) initFields ();
        if (to != 0) {
            to = Math.min(staticFields.length, to);
            from = Math.min(staticFields.length, from);
            FieldVariable[] fv = new FieldVariable [to - from];
            System.arraycopy (staticFields, from, fv, 0, to - from);
            return fv;
        }
        return staticFields;
    }
*/
    /**
     * Return all inherited fields.
     * 
     * @return all inherited fields
     */
/*NM TEMPORARY COMMENTED OUT
    public Field[] getInheritedFields (int from, int to) {
        Value v = getInnerValue ();
        if (v == null || v instanceof ArrayReference) {
            return new Field[] {};
        }
        if (fields == null) initFields ();
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
    }
*/    
    /**
     * Calls {@link java.lang.Object#toString} in debugged JVM and returns
     * its value.
     *
     * @return toString () value of this instance
     */
    public String getToStringValue () throws InvalidExpressionException {
        String v = getInnerValue (); return v; //NM
        /*NM TEMPORARY COMMENTED OUT
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
            return "";
        }
        NM*/
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
    /*NM 
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
        }
    }
*/    
    /**
     * Declared type of this local.
     *
     * @return declared type of this local
     */
    public String getType () {
        if (genericType != null) return genericType;
        return ""; // NOI18N
        /*NM TEMPORARY COMMENTED OUT 
        if (getInnerValue () == null) return "";
        try {
            return this.getInnerValue().type().name ();
        } catch (VMDisconnectedException vmdex) {
            return ""; // The session is gone.
        }
        NM*/
    }
    
   //NM TEMPORARY ADDED CODE
   /**
    * Sets string representation of type of this variable.
    */
    public void setDeclaredType (String type) { //NM
        genericType = type; //NM
    } //NM
    
    public boolean equals (Object o) {
        return  (o instanceof AbstractVariable) &&
                (id.equals (((AbstractVariable) o).id));
    }
    
    public int hashCode () {
        return id.hashCode ();
    }

    public String name () {
        return id;
    }
    public String getName () {
        return id;
    }    
    // other methods............................................................
    
    protected /* Value */ String getInnerValue () {
        return value;
    }
    
    protected void setInnerValue (/* Value */ String v) {
        value = v;
        //NM fields = null;
        //NM staticFields = null;
        //NM inheritedFields = null;
    }
    
    protected final GdbDebuggerImpl getDebugger() {
        return debugger;
    }
    
    String getID () {
        return id;
    }
    
    private static String getTypeDescription (PushbackReader signature) 
    throws IOException {
        int c = signature.read();
        switch (c) {
        case 'Z':
            return "boolean"; // NOI18N
        case 'B':
            return "byte"; // NOI18N
        case 'C':
            return "char"; // NOI18N
        case 'S':
            return "short"; // NOI18N
        case 'I':
            return "int"; // NOI18N
        case 'J':
            return "long"; // NOI18N
        case 'F':
            return "float"; // NOI18N
        case 'D':
            return "double"; // NOI18N
        case '[':
        {
            int arrayCount = 1;
            for (; ;arrayCount++) {
                if ((c = signature.read()) != '[') {
                    signature.unread(c);
                    break;
                }
            }
            return getTypeDescription(signature) + ' ' + brackets(arrayCount);
        }
        case 'L':
        {
            StringBuffer typeName = new StringBuffer(50);
            for (;;) {
                c = signature.read();
                if (c == ';') {
                    int idx = typeName.lastIndexOf("/"); // NOI18N
                    return idx == -1 ? 
                        typeName.toString() : typeName.substring(idx + 1);
                }
                else if (c == '<') {
                    int idx = typeName.lastIndexOf("/"); // NOI18N
                    if (idx != -1) typeName.delete(0, idx + 1);
                    typeName.append("<"); // NOI18N
                    for (;;) {
                        String td = getTypeDescription(signature);
                        typeName.append(td);
                        c = signature.read();
                        if (c == '>') break;
                        signature.unread(c);
                        typeName.append(',');
                    }
                    signature.read();   // should be a semicolon
                    typeName.append(">"); // NOI18N
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
            sb.append ("[]"); // NOI18N
        } while (--arrayCount > 0);
        return sb.toString ();
    }
    private void initFields () {
/*NM TEMPORARY COMMENTED OUT
        Value value = getInnerValue();
        Type type;
        if (value != null) {
            type = getInnerValue ().type ();
        } else {
            type = null;
        }
        if ( !(getInnerValue() instanceof ObjectReference) || 
             !(type instanceof ReferenceType)
        ) {
 NM*/
            this.fields = new Field [0];
            this.staticFields = new Field [0];
            this.inheritedFields = new Field [0];
/*NM TEMPORARY COMMENTED OUT
        } else {
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
        }
NM*/
    }
    
/*NM TEMPORARY COMMENTED OUT
    private Field[] getFieldsOfArray (
            ArrayReference ar, 
            String componentType,
            String parentID,
            int from,
            int to
        ) {
            List l = ar.getValues(from, to - from);
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
        Value v = or.getValue (f);
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
 NM*/
    
    
    /**
    * Adds a field.
    *
    * @parameter field A field to add.
    */
    public void addField (Field field) {
        if (fields == null) {
            initFields();
        }
        int n = fields.length;
        Field[] fv = new Field [n + 1];
        System.arraycopy (fields, 0, fv, 0, n);
        fields = fv;
        fields[n] = field;
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        listeners.add(l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
        listeners.remove(l);
    }
    
    public String toString () {
        return "ObjectVariable "; // NOI18N
    }

    /*
     * Commented out private method convertToStringInitializer(String s)
     * because it is not used anywhere. Looks like we don't need it.
     *
    private static String convertToStringInitializer (String s) {
        StringBuffer sb = new StringBuffer ();
        int i, k = s.length ();
        for (i = 0; i < k; i++)
            switch (s.charAt (i)) {
                case '\b':
                    sb.append ("\\b"); // NOI18N
                    break;
                case '\f':
                    sb.append ("\\f"); // NOI18N
                    break;
                case '\\':
                    sb.append ("\\\\"); // NOI18N
                    break;
                case '\t':
                    sb.append ("\\t"); // NOI18N
                    break;
                case '\r':
                    sb.append ("\\r"); // NOI18N
                    break;
                case '\n':
                    sb.append ("\\n"); // NOI18N
                    break;
                case '\"':
                    sb.append ("\\\""); // NOI18N
                    break;
                default:
                    sb.append (s.charAt (i));
            }
        return sb.toString();
    }
    */
    /*
     * Commented out private method convertToCharInitializer(String s)
     * because it is not used anywhere. Looks like we don't need it.
     *
    private static String convertToCharInitializer (String s) {
        StringBuffer sb = new StringBuffer ();
        int i, k = s.length ();
        for (i = 0; i < k; i++)
            switch (s.charAt (i)) {
                case '\b':
                    sb.append ("\\b"); // NOI18N
                    break;
                case '\f':
                    sb.append ("\\f"); // NOI18N
                    break;
                case '\\':
                    sb.append ("\\\\"); // NOI18N
                    break;
                case '\t':
                    sb.append ("\\t"); // NOI18N
                    break;
                case '\r':
                    sb.append ("\\r"); // NOI18N
                    break;
                case '\n':
                    sb.append ("\\n"); // NOI18N
                    break;
                case '\'':
                    sb.append ("\\\'"); // NOI18N
                    break;
                default:
                    sb.append (s.charAt (i));
            }
        return sb.toString();
    }
    */
}

