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
 * A representation of a parameter to a method declaration.  A parameter 
 * will not have a name, unless the classfile is compiled with local
 * variable tables.  A final modifier on a parameter is not reported, 
 * since that modifier is not stored in a classfile.
 *
 * @author  Thomas Ball
 */
public final class Parameter extends Field {

    static Parameter[] makeParams(Method method) {
	return new Parameter[0]; //FIXME
    }

    /** Creates new Parameter */
    Parameter(String name, String type) {
        super(name, type);
    }

    boolean loadAttribute(String type, int len, DataInputStream in, 
			  ConstantPool pool) throws IOException {
	assert false : "parameters are never loaded directly from classfile";
	return false;
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
	String name = getName();
	if (name != null) {
	    sb.append(' ');
	    sb.append(name);
	}
	return sb.toString();
    }
}
