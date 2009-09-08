/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.debugger.jpda.models;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.ArrayType;
import com.sun.jdi.CharValue;
import com.sun.jdi.ClassObjectReference;
import com.sun.jdi.ClassType;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.InterfaceType;
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
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.Super;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.jdi.ArrayReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ArrayTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ClassNotPreparedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ClassTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.MethodWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ReferenceTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.StringReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.TypeComponentWrapper;
import org.netbeans.modules.debugger.jpda.jdi.TypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ValueWrapper;
import org.netbeans.modules.debugger.jpda.util.JPDAUtils;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;


/**
 * @author   Jan Jancura
 */
class AbstractObjectVariable extends AbstractVariable implements ObjectVariable {
    // Customized for add/removePropertyChangeListener
    // Cloneable for fixed watches
    
    private static final Logger logger = Logger.getLogger("org.netbeans.modules.debugger.jpda.getValue"); // NOI18N

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
        /*if (value instanceof ObjectReference) {
            try {
                // Disable collection of the value so that we do not loose it in the mean time.
                // Enable collection is called as soon as we do not need it.
                System.err.println("DISABLING collection for "+value);
                ObjectReferenceWrapper.disableCollection((ObjectReference) value);
            } catch (Exception ex) {}
        }*/
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
            return ArrayReferenceWrapper.length0((ArrayReference) v);
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
        Value v = getInnerValue();
        if (v == null) return null;
        com.sun.jdi.Field f;
        try {
            f = ReferenceTypeWrapper.fieldByName((ReferenceType) ValueWrapper.type(v), name);
        } catch (ClassNotPreparedExceptionWrapper ex) {
            return null;
        } catch (InternalExceptionWrapper iex) {
            return null;
        } catch (ObjectCollectedExceptionWrapper ocex) {
            return null;
        } catch (VMDisconnectedExceptionWrapper e) {
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
            if (v instanceof ArrayReference && (from > 0 || to < ArrayReferenceWrapper.length((ArrayReference) v))) {
                // compute only requested elements
                Type type = ValueWrapper.type(v);
                ReferenceType rt = (ReferenceType) type;
                if (to == 0) to = ArrayReferenceWrapper.length((ArrayReference) v);
                Field[] elements = getFieldsOfArray (
                        (ArrayReference) v, 
                        ArrayTypeWrapper.componentTypeName((ArrayType) rt),
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
        } catch (InternalExceptionWrapper e) {
            return new Field[] {};
        } catch (ObjectCollectedExceptionWrapper e) {
            return new Field[] {};
        } catch (VMDisconnectedExceptionWrapper e) {
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
            Field[] fv = new Field[to - from];
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
            Field[] fv = new Field[to - from];
            System.arraycopy (inheritedFields, from, fv, 0, to - from);
            return fv;
        }
        return inheritedFields;
    }

    private volatile Super superClass;

    public Super getSuper () {
        if (getInnerValue () == null) 
            return null;
        if (superClass != null) {
            return superClass;
        }
        try {
            Type t = ValueWrapper.type(this.getInnerValue());
            if (!(t instanceof ClassType)) 
                return null;
            ClassType superType;
            superType = ClassTypeWrapper.superclass((ClassType) t);
            if (superType == null) 
                return null;
            Super s = new SuperVariable(
                    getDebugger(), 
                    (ObjectReference) this.getInnerValue(),
                    superType,
                    getID()
                    );
            superClass = s;
            return s;
        } catch (ObjectCollectedExceptionWrapper ocex) {
            return null;
        } catch (InternalExceptionWrapper ex) {
            return null;
        } catch (VMDisconnectedExceptionWrapper e) {
            return null;
        }
    }
    
    public List<JPDAClassType> getAllInterfaces() {
        if (getInnerValue () == null)
            return null;
        try {
            Type t = ValueWrapper.type(this.getInnerValue());
            if (!(t instanceof ClassType))
                return null;
            List<InterfaceType> interfaces;
            interfaces = ClassTypeWrapper.allInterfaces0((ClassType) t);
            if (interfaces == null)
                return null;
            List<JPDAClassType> allInterfaces = new ArrayList<JPDAClassType>();
            for (InterfaceType it : interfaces) {
                allInterfaces.add(new JPDAClassTypeImpl(getDebugger(), it));
            }
            return allInterfaces;
        } catch (ClassNotPreparedExceptionWrapper cnpex) {
            return null;
        } catch (ObjectCollectedExceptionWrapper ocex) {
            return null;
        } catch (InternalExceptionWrapper ex) {
            return null;
        } catch (VMDisconnectedExceptionWrapper e) {
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
        return getToStringValue(v, getDebugger(), 0);
    }
    
    /**
     * Calls {@link java.lang.Object#toString} in debugged JVM and returns
     * its value.
     *
     * @return toString () value of this instance
     */
    public String getToStringValue (int maxLength) throws InvalidExpressionException {
        Value v = getInnerValue ();
        return getToStringValue(v, getDebugger(), maxLength);
    }
    
    static String getToStringValue (Value v, JPDADebuggerImpl debugger, int maxLength) throws InvalidExpressionException {
        if (v == null) return null;
        try {
            if (!(ValueWrapper.type (v) instanceof ClassType))
                return AbstractVariable.getValue (v);
            if (v instanceof CharValue)
                return "\'" + v.toString () + "\'";
            boolean addQuotation = false;
            boolean addDots = false;
            StringReference sr;
            if (maxLength > 0 && maxLength < Integer.MAX_VALUE) {
                Method toStringMethod = ClassTypeWrapper.concreteMethodByName((ClassType) ValueWrapper.type (v),
                     "toString", "()Ljava/lang/String;");  // NOI18N
                sr = (StringReference) debugger.invokeMethod (
                    (ObjectReference) v,
                    toStringMethod,
                    new Value [0],
                    maxLength + 1
                );
            } else if (v instanceof StringReference) {
                sr = (StringReference) v;
                addQuotation = true;
            } else {
                Method toStringMethod = ClassTypeWrapper.concreteMethodByName((ClassType) ValueWrapper.type (v),
                    "toString", "()Ljava/lang/String;");  // NOI18N
                sr = (StringReference) debugger.invokeMethod (
                    (ObjectReference) v,
                    toStringMethod,
                    new Value [0]
                );
            }
            if (sr == null) {
                return null;
            }
            String str = StringReferenceWrapper.value(sr);
            if (maxLength > 0 && maxLength < Integer.MAX_VALUE && str.length() > maxLength) {
                str = str.substring(0, maxLength) + "..."; // NOI18N
            }
            if (addQuotation) {
                str = "\"" + str + "\""; // NOI18N
            }
            return str;
        } catch (InternalExceptionWrapper ex) {
            return ex.getLocalizedMessage();
        } catch (VMDisconnectedExceptionWrapper ex) {
            return NbBundle.getMessage(AbstractVariable.class, "MSG_Disconnected");
        } catch (ObjectCollectedExceptionWrapper ocex) {
            return NbBundle.getMessage(AbstractVariable.class, "MSG_ObjCollected");
        } catch (ClassNotPreparedExceptionWrapper cnpex) {
            return cnpex.getLocalizedMessage();
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
        return invokeMethod(null, methodName, signature, arguments);
    }

    /**
     * Calls given method in debugged JVM on this instance and returns
     * its value.
     *
     * @param thread the thread on which the method invocation is performed.
     * @param methodName a name of method to be called
     * @param signature a signature of method to be called
     * @param arguments a arguments to be used
     *
     * @return value of given method call on this instance
     */
    public Variable invokeMethod (
        JPDAThread thread,
        String methodName,
        String signature,
        Variable[] arguments
    ) throws NoSuchMethodException, InvalidExpressionException {
        try {
             
            // 1) find corrent method
            Value v = this.getInnerValue ();
            if (v == null) return null;
            Method method = null;
            if (signature != null)
                method = ClassTypeWrapper.concreteMethodByName(
                        (ClassType) ValueWrapper.type(v),
                        methodName, signature);
            else {
                List l = ReferenceTypeWrapper.methodsByName(
                        (ClassType) ValueWrapper.type(v),
                        methodName);
                int j, jj = l.size ();
                for (j = 0; j < jj; j++)
                    if ( !MethodWrapper.isAbstract((Method) l.get (j)) &&
                         MethodWrapper.argumentTypeNames((Method) l.get (j)).size () == 0
                    ) {
                        method = (Method) l.get (j);
                        break;
                    }
            }
            
            // 2) method not found => print all method signatures
            if (method == null) {
                List l = ReferenceTypeWrapper.methodsByName(
                        (ClassType) ValueWrapper.type(v), methodName);
                int j, jj = l.size ();
                for (j = 0; j < jj; j++)
                    System.out.println (TypeComponentWrapper.signature((Method) l.get (j)));
                throw new NoSuchMethodException (
                    TypeWrapper.name(ValueWrapper.type(v)) + "." +
                        methodName + " : " + signature
                );
            }
            
            // 3) call this method
            Value[] vs = new Value [arguments.length];
            int i, k = arguments.length;
            for (i = 0; i < k; i++)
                vs [i] = ((AbstractVariable) arguments [i]).getInnerValue ();
            v = getDebugger().invokeMethod (
                (JPDAThreadImpl) thread,
                (ObjectReference) v,
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
        } catch (InternalExceptionWrapper ex) {
            return null;
        } catch (ClassNotPreparedExceptionWrapper ex) {
            return null;
        } catch (VMDisconnectedExceptionWrapper ex) {
            return null;
        } catch (ObjectCollectedExceptionWrapper ocex) {
            return null;
        }
    }

    /**
     * Evaluates the expression in the context of this variable.
     * All methods are invoked on this variable,
     * <code>this</code> can be used to refer to this variable.
     * 
     * @param expression
     * @return Variable containing the result
     */
    public Variable evaluate(String expression) throws InvalidExpressionException {
        return getDebugger().evaluate(expression, this);
    }
    
    /**
     * Declared type of this local.
     *
     * @return declared type of this local
     */
    @Override
    public String getType () {
        if (genericType != null) return genericType;
        Value v = getInnerValue ();
        if (v == null) return "";
        try {
            return TypeWrapper.name(ValueWrapper.type(v));
        } catch (InternalExceptionWrapper ex) {
            return ex.getLocalizedMessage();
        } catch (VMDisconnectedExceptionWrapper vmdex) {
            // The session is gone.
            return NbBundle.getMessage(AbstractVariable.class, "MSG_Disconnected");
        } catch (ObjectCollectedExceptionWrapper ocex) {
            // The object is gone.
            return NbBundle.getMessage(AbstractVariable.class, "MSG_ObjCollected");
        }
    }
    
    @Override
    public JPDAClassType getClassType() {
        Value value = getInnerValue();
        if (value == null) return null;
        try {
            com.sun.jdi.Type type = ValueWrapper.type (value);
            if (type instanceof ReferenceType) {
                return new JPDAClassTypeImpl(getDebugger(), (ReferenceType) type);
            } else {
                return null;
            }
        } catch (ObjectCollectedExceptionWrapper e) {
            return null;
        } catch (InternalExceptionWrapper e) {
            return null;
        } catch (VMDisconnectedExceptionWrapper e) {
            return null;
        }
    }
    
    @Override
    public boolean equals (Object o) {
        return  (o instanceof AbstractObjectVariable) &&
                (getID().equals (((AbstractObjectVariable) o).getID()));
    }
    
    @Override
    public int hashCode() {
        return getID().hashCode();
    }
    
    // other methods............................................................
    
    @Override
    protected void setInnerValue (Value v) {
        super.setInnerValue(v);
        /*Value old = getInnerValue();
        if (old instanceof ObjectReference) {
            try {
                ObjectReferenceWrapper.enableCollection((ObjectReference) old);
            } catch (Exception ex) {}
        }
        if (v instanceof ObjectReference) {
            try {
                ObjectReferenceWrapper.disableCollection((ObjectReference) v);
            } catch (Exception ex) {}
        }*/
        fields = null;
        staticFields = null;
        inheritedFields = null;
        superClass = null;
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
                type = ValueWrapper.type(value);
            } catch (InternalExceptionWrapper ocex) {
                type = null;
            } catch (ObjectCollectedExceptionWrapper ocex) {
                type = null;
            } catch (VMDisconnectedExceptionWrapper e) {
                type = null;
            }
        } else {
            type = null;
        }
        if ( !(value instanceof ObjectReference) ||
             !(type instanceof ReferenceType)
        ) {
            this.fields = new Field [0];
            this.staticFields = new Field [0];
            this.inheritedFields = new Field [0];
        } else {
            try {
                ObjectReference or = (ObjectReference) value;
                ReferenceType rt = (ReferenceType) type;
                if (or instanceof ArrayReference) {
                    this.fields = getFieldsOfArray (
                        (ArrayReference) or, 
                        ArrayTypeWrapper.componentTypeName((ArrayType) rt),
                        this.getID (),
                        0, ArrayReferenceWrapper.length((ArrayReference) or));
                    this.staticFields = new Field[0];
                    this.inheritedFields = new Field[0];
                }
                else {
                    initFieldsOfClass(or, rt, this.getID ());
                }
            } catch (InternalExceptionWrapper iex) {
            } catch (ObjectCollectedExceptionWrapper iex) {
                // The object is gone => no fields
            } catch (VMDisconnectedExceptionWrapper e) {
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
                l = ArrayReferenceWrapper.getValues(ar, from, to - from);
            } catch (InternalExceptionWrapper ex) {
                return new Field[0];
            } catch (VMDisconnectedExceptionWrapper ex) {
                return new Field[0];
            } catch (ObjectCollectedExceptionWrapper ex) {
                Exceptions.printStackTrace(ex);
                return new Field[0];
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
                        this,
                        from + i,
                        to - 1,
                        parentID
                    ) :
                    new ArrayFieldVariable (
                        getDebugger(), 
                        (PrimitiveValue) v, 
                        componentType, 
                        this,
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
        List<Field> classFields = new ArrayList<Field>();
        List<Field> classStaticFields = new ArrayList<Field>();
        List<Field> allInheretedFields = new ArrayList<Field>();
        
        List<com.sun.jdi.Field> l;
        Set<com.sun.jdi.Field> s;
        try {
            l = ReferenceTypeWrapper.allFields0(rt);
            s = new HashSet<com.sun.jdi.Field>(ReferenceTypeWrapper.fields0(rt));

            int i, k = l.size();
            for (i = 0; i < k; i++) {
                com.sun.jdi.Field f = l.get (i);
                Field field = this.getField (f, or, this.getID());
                if (TypeComponentWrapper.isStatic(f))
                    classStaticFields.add(field);
                else {
                    if (s.contains (f))
                        classFields.add(field);
                    else
                        allInheretedFields.add(field);
                }
            }
        } catch (ClassNotPreparedExceptionWrapper e) {
        } catch (InternalExceptionWrapper e) {
        } catch (VMDisconnectedExceptionWrapper e) {
            classFields.clear();
            classStaticFields.clear();
            allInheretedFields.clear();
        }
        this.fields = classFields.toArray (new Field [classFields.size ()]);
        this.inheritedFields = allInheretedFields.toArray (
            new Field [allInheretedFields.size ()]
        );
        this.staticFields = classStaticFields.toArray
                (new Field [classStaticFields.size ()]);
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
            v = ObjectReferenceWrapper.getValue (or, f);
        } catch (ObjectCollectedExceptionWrapper ocex) {
            v = null;
        } catch (InternalExceptionWrapper ocex) {
            v = null;
        } catch (VMDisconnectedExceptionWrapper ocex) {
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
            if (JPDAUtils.IS_JDK_16) {
                final String name = Long.toString(getUniqueID());
                final List<ObjectReference> referrers;
                try {
                    referrers = ObjectReferenceWrapper.referringObjects((ObjectReference) v, maxReferrers);
                } catch (VMDisconnectedExceptionWrapper ex) {
                    return Collections.emptyList();
                } catch (InternalExceptionWrapper ex) {
                    return Collections.emptyList();
                }
                return new AbstractList<ObjectVariable>() {
                    public ObjectVariable get(int i) {
                        ObjectReference obj = referrers.get(i);
                        if (obj instanceof ClassObjectReference) {
                            ClassObjectReference clobj = (ClassObjectReference) obj;
                            return new ClassVariableImpl(getDebugger(), clobj, name+" referrer "+i);
                        } else {
                            return new AbstractObjectVariable(getDebugger(), obj, name+" referrer "+i);
                        }
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
            try {
                return ObjectReferenceWrapper.uniqueID((ObjectReference) value);
            } catch (InternalExceptionWrapper ex) {
                return 0L;
            } catch (VMDisconnectedExceptionWrapper ex) {
                return 0L;
            } catch (ObjectCollectedExceptionWrapper ex) {
                return 0L;
            }
        }
    }
    
    private int cloneNumber = 1;
    
    @Override
    public Variable clone() {
        AbstractObjectVariable clon = new AbstractObjectVariable(getDebugger(), getJDIValue(), getID() + "_clone"+(cloneNumber++));
        clon.genericType = this.genericType;
        return clon;
    }
    
    @Override
    public String toString () {
        return "ObjectVariable ";
    }

    /*@Override
    protected void finalize() throws Throwable {
        Value v = getInnerValue();
        if (v instanceof ObjectReference) {
            try {
                System.err.println("ENABLING collection for "+v);
                ObjectReferenceWrapper.enableCollection((ObjectReference) v);
            } catch (Exception ex) {}
        }
        super.finalize();
    }*/

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
