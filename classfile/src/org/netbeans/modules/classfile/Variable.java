/*
 * Variable.java
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

import java.io.*;

/**
 * A Java field.  Unfortunately, the word "field" is generally used in 
 * the Java documentation to mean either a variable, or both variables
 * and methods.  This class only describes variables.
 *
 * @author  Thomas Ball
 */
public final class Variable extends Field {

    private boolean constant;    // don't initialize to false, as loadAttribute is called by super.<init>
    private Object value;
    
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
    Variable(DataInputStream in, ConstantPool pool, ClassFile cls) throws IOException {
        super(in, pool, cls);
        loadAttributes(in, pool);
    }

    /**
     * (Private implementation method)
     */
    final boolean loadAttribute(String name, int len, 
      DataInputStream in, ConstantPool pool) throws IOException {
        if (name.equals("ConstantValue")) { //NOI18N
            constant = true;
            int index = in.readUnsignedShort();
            CPEntry cpe = pool.get(index);
            value = cpe.getValue();
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
        return false;
    }

    /**
     * Returns true if the variable is a constant; that is, a final
     * static variable.
     * @see #getConstantValue
     */
    public final boolean isConstant() {
        return constant;
    }

    /**
     * Returns the value object of this variable if it is a constant,
     * otherwise null.
     * @deprecated replaced by <code>Object getConstantValue()</code>.
     */
    public final Object getValue() {
        return value;
    }
    
    /**
     * Returns the value object of this variable if it is a constant,
     * otherwise null.
     * @see #isConstant
     */
    public final Object getConstantValue() {
        return value;
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
