/*
 * Variable.java
 *
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
 *
 * Contributor(s): Thomas Ball
 *
 * Version: $Revision$
 */

package org.netbeans.modules.classfile;

import java.io.*;

/**
 * A Java field.  Unfortunately, the word "field" is generally used in
 * the Java documentation to mean either a variable, or both variables
 * and methods.  This class only describes variables.
 *
 * @author  Thomas Ball
 */
public final class Variable extends Field {

    private Object constValue = notLoadedConstValue;
    
    private static final Object notLoadedConstValue = new Object();

    static Variable[] loadFields(DataInputStream in, ConstantPool pool,
                                 ClassFile cls) 
      throws IOException {
          int count = in.readUnsignedShort();
          Variable[] variables = new Variable[count];
          for (int i = 0; i < count; i++)
              variables[i] = new Variable(in, pool, cls);
          return variables;
    }
    
    /** Creates new Variable */
    Variable(DataInputStream in, ConstantPool pool, ClassFile cls) 
	throws IOException {
        super(in, pool, cls, false);
    }

    /**
     * Returns true if the variable is a constant; that is, a final
     * static variable.
     * @see #getConstantValue
     */
    public final boolean isConstant() {
        return attributes.get("ConstantValue") != null;//NOI18N
    }

    /**
     * Returns the value object of this variable if it is a constant,
     * otherwise null.
     * @deprecated replaced by <code>Object getConstantValue()</code>.
     */
    @Deprecated
    public final Object getValue() {
	return getConstantValue();
    }
    
    /**
     * Returns the value object of this variable if it is a constant,
     * otherwise null.
     * @see #isConstant
     */
    public final Object getConstantValue() {
	if (constValue == notLoadedConstValue) {
	    DataInputStream in = attributes.getStream("ConstantValue"); // NOI18N
	    if (in != null) {
		try {
		    int index = in.readUnsignedShort();
		    CPEntry cpe = classFile.constantPool.get(index);
		    constValue = cpe.getValue();
		} catch (IOException e) {
		    throw new InvalidClassFileAttributeException("invalid ConstantValue attribute", e);
		}
	    }
	}
        return constValue;
    }
    
    /**
     * Return a string in the form "<type> <name>".  Class types
     * are shown in a "short" form; i.e. "Object" instead of
     * "java.lang.Object"j.
     *
     * @return string describing the variable and its type.
     */
    public final String getDeclaration() {
	StringBuffer sb = new StringBuffer();
	sb.append(CPFieldMethodInfo.getSignature(getDescriptor(), false));
	sb.append(' ');
	sb.append(getName());
	return sb.toString();
    }

    /**
     * Returns true if this field defines an enum constant.
     */
    public final boolean isEnumConstant() {
	return (access & Access.ENUM) == Access.ENUM;
    }
            
    public String toString() {
        StringBuffer sb = new StringBuffer(super.toString());
        if (isConstant()) {
	    sb.append(", const value="); //NOI18N
	    sb.append(getValue());
	}
        return sb.toString();
    }
}
