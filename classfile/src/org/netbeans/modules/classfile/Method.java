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

/**
 * A Java method object.
 *
 * @author  Thomas Ball
 */
public class Method extends Field {
    
    private Code code;
    private CPClassInfo[] exceptions;

    static Method[] loadMethods(DataInputStream in, ConstantPool pool,
				boolean includeCode) 
      throws IOException {
	int count = in.readUnsignedShort();
	Method[] methods = new Method[count];
	for (int i = 0; i < count; i++)
	    methods[i] = new Method(in, pool, includeCode);
	return methods;
    }
    
    /** Creates new Method */
    Method(DataInputStream in, ConstantPool pool, boolean includeCode) throws IOException {
        super(in, pool, includeCode);
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
        else if (includeCode && name.equals("Exceptions")) { //NOI18N
            exceptions = ClassFile.getCPClassList(in, pool);
            return true;
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
    
    public String toString() {
        String s = super.toString();
        CPClassInfo[] ec = getExceptionClasses();
        if (ec.length > 0) {
            StringBuffer sb = new StringBuffer(s);
            sb.append(", throws"); //NOI18N
            for (int i = 0; i < ec.length; i++) {
                sb.append(' '); //NOI18N
                sb.append(ec[i].getName());
            }
        }
        return s;
    }

    public final String getDeclaration() {
        return CPMethodInfo.getFullMethodName(getName(), getDescriptor());
    }
}
