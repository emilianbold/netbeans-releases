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
import java.util.*;

/**
 * A representation of a parameter to a method declaration.  A parameter 
 * will not have a name, unless the classfile is compiled with local
 * variable tables (if not, then the name is an empty string).  
 * A final modifier on a parameter is never reported, 
 * since that modifier is not stored in a classfile.
 *
 * @author  Thomas Ball
 */
public final class Parameter extends Field {

    static Parameter[] makeParams(Method method) {
	List paramList = new ArrayList();
        for (Iterator it = new ParamIterator(method); it.hasNext();)
            paramList.add(it.next());
        return (Parameter[])paramList.toArray(new Parameter[0]);
    }

    /** Creates new Parameter */
    Parameter(String name, String type, ClassFile classFile) {
        super(name, type, classFile);
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
    
    public String toString() {
        StringBuffer sb = new StringBuffer(getName());
        sb.append(" type="); //NOI18N
        sb.append(getDescriptor());
	if (getTypeSignature() != null) {
	    sb.append(", signature="); //NOI18N
	    sb.append(typeSignature);
	}
        loadAnnotations();
	if (annotations.size() > 0) {
	    Iterator iter = annotations.values().iterator();
	    sb.append(", annotations={ ");
	    while (iter.hasNext()) {
		sb.append(iter.next().toString());
		if (iter.hasNext())
		    sb.append(", ");
	    }
	    sb.append(" }");
	}
        return sb.toString();
    }

    private static class ParamIterator implements Iterator {
        ClassFile classFile;
        String signature;
        LocalVariableTableEntry[] localVars;
        int ivar;
        int isig;
        
        /** 
         * @param method 
         */
        ParamIterator(Method method) {
            classFile = method.getClassFile();
            signature = method.getDescriptor();
            assert signature.charAt(0) == '(';
            isig = 1;  // skip '('
            ivar = 0;
	    Code code = method.getCode();
            localVars = code != null ? 
		code.getLocalVariableTable() : 
		new LocalVariableTableEntry[0];
        }
        
        public boolean hasNext() {
            return signature.charAt(isig) != ')';
        }
        
        public Object next() {
            if (hasNext()) {
                String name = ivar < localVars.length ? 
                    localVars[ivar].getName() : "";
                ivar++;
                int sigStart = isig;
                while (isig < signature.length()) {
                    char ch = signature.charAt(isig);
                    switch (ch) {
                        case '[':
                            isig++;
                            break;
                        case 'B':
                        case 'C':
                        case 'D':
                        case 'F':
                        case 'I':
                        case 'J':
                        case 'S':
                        case 'Z':
                        case 'V': {
                            String type = signature.substring(sigStart, ++isig);
                            return new Parameter(name, type, classFile);
                        }
                        case 'L': {
                            int end = signature.indexOf(';', isig);
                            String type = signature.substring(isig + 1, end).replace('/', '.');
                            isig = end + 1;
                            return new Parameter(name, type, classFile);
                        }

                    }
                }
            }
            throw new NoSuchElementException();
        }
        
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
