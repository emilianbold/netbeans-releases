/*
 * Method.java
 *
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
 *
 * Contributor(s): Thomas Ball
 *
 * Version: $Revision$
 */

package org.netbeans.modules.classfile;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Arrays;

/**
 * A Java method object.
 *
 * @author  Thomas Ball
 */
public final class Method extends Field {
    
    private boolean includeCode;
    private Code code;
    private CPClassInfo[] exceptions;
    private Parameter[] parameters;

    static Method[] loadMethods(DataInputStream in, ConstantPool pool,
				ClassFile cls, boolean includeCode) 
      throws IOException {
	int count = in.readUnsignedShort();
	Method[] methods = new Method[count];
	for (int i = 0; i < count; i++)
	    methods[i] = new Method(in, pool, cls, includeCode);
	return methods;
    }
    
    /** Creates new Method */
    Method(DataInputStream in, ConstantPool pool, ClassFile cls, boolean includeCode) throws IOException {
        super(in, pool, cls);
        this.includeCode = includeCode;
        super.loadAttributes(in, pool);
    }

    /**
     * (Private implementation method)
     */
    final boolean loadAttribute(String name, int len, 
      DataInputStream in, ConstantPool pool) throws IOException {
        if (includeCode && name.equals("Code")) { //NOI18N
	    code = new Code(in, pool);
            return true;
	}
        else if (name.equals("Exceptions")) { //NOI18N
            exceptions = ClassFile.getCPClassList(in, pool);
            return true;
        }
	else if (name.equals("Signature")) { //NOI18N
            CPUTF8Info entry;
	    try {
		entry = (CPUTF8Info)pool.get(in.readUnsignedShort());
	    } catch (ClassCastException e) {
		throw new IOException("invalid constant pool entry");
	    }
	    setTypeSignature(entry.getName());
	    return true;
	}
	else if (name.equals("RuntimeVisibleParameterAnnotations")) { //NOI18N
	    return false; //FIXME
	}
	else if (name.equals("RuntimeInvisibleParameterAnnotations")) { //NOI18N
	    return false; //FIXME
	}
	else if (name.equals("AnnotationDefault")) { //NOI18N
	    return false; //FIXME
	}
        return false;
    }

    /** 
     * Get the bytecodes of this method.
     *
     * @return the Code object, or null if method is abstract.
     */    
    public final Code getCode() {
        return code;  // will be null for abstract methods
    }
    
    public final CPClassInfo[] getExceptionClasses() {
        if (exceptions == null)
            exceptions = new CPClassInfo[0];
        return exceptions;
    }
    
    /**
     * Returns true if this method is a generics bridge method defined
     * by the compiler.
     */
    public final boolean isBridge() {
	return (access & Access.BRIDGE) == Access.BRIDGE;
    }
            
    /**
     * Returns true if this method is declared with a variable number
     * of arguments.
     */
    public final boolean isVarArgs() {
	return (access & Access.VARARGS) == Access.VARARGS;
    }

    /**
     * Returns the parameters for this method as a declaration-ordered list.
     */
    public final List getParameters() {
	if (parameters == null)
	    parameters = Parameter.makeParams(this);
	return Arrays.asList(parameters);
    }
            
    public String toString() {
        StringBuffer sb = new StringBuffer(super.toString());
        CPClassInfo[] ec = getExceptionClasses();
        if (ec.length > 0) {
            sb.append(", throws"); //NOI18N
            for (int i = 0; i < ec.length; i++) {
                sb.append(' '); //NOI18N
                sb.append(ec[i].getName());
            }
	}
        return sb.toString();
    }

    public final String getDeclaration() {
        return CPMethodInfo.getFullMethodName(getName(), getDescriptor());
    }
}
