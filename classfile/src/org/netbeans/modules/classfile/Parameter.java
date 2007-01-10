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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
        List result = new ArrayList();
        ClassFile classFile = method.getClassFile();
        String signature = method.getDescriptor();
        assert signature.charAt(0) == '(';
        /** the current character in the type signature */
        int isig = 1;  // skip '('
        /** the current local variable array position */
        int ivar = method.isStatic() ? 0 : 1;
        Code code = method.getCode();
        LocalVariableTableEntry[] localVars = code != null ?
            code.getLocalVariableTable() :
            new LocalVariableTableEntry[0];

        
        while (signature.charAt(isig) != ')') {
            String name = "";
            for (int i = 0; i < localVars.length; i++) {
                LocalVariableTableEntry lvte = localVars[i];
                // only parameters have a startPC of zero
                if (lvte.index == ivar && lvte.startPC == 0) {
                    name = localVars[i].getName();
                    break;
                }
            }
            ivar++;
            int sigStart = isig;
            boolean again = true;
            while (again && isig < signature.length()) {
                again = false;
                char ch = signature.charAt(isig);
                switch (ch) {
                    case '[':
                        isig++;
                        again = true;
                        break;
                    case 'B':
                    case 'C':
                    case 'F':
                    case 'I':
                    case 'S':
                    case 'Z':
                    case 'V': {
                        String type = signature.substring(sigStart, ++isig);
                        result.add(Parameter.createParameter(name, type, classFile));
                        break;
                    }
                    case 'D':
                    case 'J': {
                        ivar++;  // longs and doubles take two slots
                        String type = signature.substring(sigStart, ++isig);
                        result.add(Parameter.createParameter(name, type, classFile));
                        break;
                    }
                    case 'L': {
                        int end = signature.indexOf(';', isig) + 1;
                        String type = signature.substring(isig, end);
                        isig = end;
                        result.add(Parameter.createParameter(name, type, classFile));
                        break;
                    }
                    
                }
            }
        }
        
        AttributeMap attrs = method.getAttributes();
        DataInputStream visibleAnnotations = attrs.getStream("RuntimeVisibleParameterAnnotations"); //NOI18N
        DataInputStream invisibleAnnotations = attrs.getStream("RuntimeInvisibleParameterAnnotations"); //NOI18N
        
        try {
            int visibleAnnotationCount = visibleAnnotations != null ? visibleAnnotations.readByte() : -1;
            int invisibleAnnotationCount = invisibleAnnotations != null ? invisibleAnnotations.readByte() : -1;
            
            if (visibleAnnotationCount != (-1) && invisibleAnnotationCount != (-1) && visibleAnnotationCount != invisibleAnnotationCount) {
                throw new InvalidClassFileAttributeException("invalid RuntimeInvisibleParameterAnnotations or RuntimeVisibleParameterAnnotations attribute", null);
            }
            
            int annotationCount = visibleAnnotationCount != (-1) ? visibleAnnotationCount : invisibleAnnotationCount;
            
            if (annotationCount != (-1)) {
                int toAttachIndex = result.size() - annotationCount;
                
                if (toAttachIndex < 0) {
                    throw new InvalidClassFileAttributeException("invalid RuntimeInvisibleParameterAnnotations or RuntimeVisibleParameterAnnotations attribute", null);
                }
                
                if (toAttachIndex > 0) {
                    //may happen for enum constructor and for constructors of inner classes:
                    if (!"<init>".equals(method.getName())) {
                        throw new InvalidClassFileAttributeException("invalid RuntimeInvisibleParameterAnnotations and/or RuntimeVisibleParameterAnnotations attribute", null);
                    }
                }
                
                while (toAttachIndex < result.size()) {
                    ((Parameter) result.get(toAttachIndex)).loadParameterAnnotations(visibleAnnotations, invisibleAnnotations);
                    toAttachIndex++;
                }
            }
        } catch (IOException e) {
            throw new InvalidClassFileAttributeException("invalid RuntimeInvisibleParameterAnnotations or RuntimeVisibleParameterAnnotations attribute", e);
        }
        
        return (Parameter[]) result.toArray(new Parameter[result.size()]);
    }

    private static Parameter createParameter (String name, String type, ClassFile classFile) {
        return new Parameter (name, type, classFile);
    }
    
    /** Creates new Parameter */
    private Parameter(String name, String type, ClassFile classFile) {
        super(name, type, classFile);
    }
    
    private void loadParameterAnnotations(DataInputStream visible, DataInputStream invisible) {
        super.loadAnnotations();
        if (annotations == null && (visible != null || invisible != null))
            annotations = new HashMap(2);
        try {
            if (visible != null)
                Annotation.load(visible, classFile.getConstantPool(), true, annotations);
        } catch (IOException e) {
            throw new InvalidClassFileAttributeException("invalid RuntimeVisibleParameterAnnotations attribute", e);
        }
        try {
            if (invisible != null)
                Annotation.load(invisible, classFile.getConstantPool(), false, annotations);
        } catch (IOException e) {
            throw new InvalidClassFileAttributeException("invalid RuntimeInvisibleParameterAnnotations attribute", e);
        }
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
        StringBuffer sb = new StringBuffer("name=");
	sb.append(getName());
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

}
