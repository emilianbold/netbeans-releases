/*
 * CPFieldMethodInfo.java
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

/**
 * The base class for field, method, and interface method constant pool types.
 *
 * @author Thomas Ball
 */
abstract class CPFieldMethodInfo extends CPNameAndTypeInfo {
    int iClass;
    int iNameAndType;

    CPFieldMethodInfo(ConstantPool pool,int iClass,int iNameAndType) {
        super(pool);
        this.iClass = iClass;
        this.iNameAndType = iNameAndType;
    }

    public final int getClassID() {
        return iClass;
    }

    public final int getFieldID() {
        return iNameAndType;
    }

    public final ClassName getClassName() {
        return ClassName.getClassName(
            ((CPName)pool.cpEntries[iClass]).getName());
    }

    void setClassNameIndex(int index) {
	iClass = index;
    }

    public final String getFieldName() {
	return ((CPNameAndTypeInfo)pool.cpEntries[iNameAndType]).getName();
    }

    public String toString() {
        return getClass().getName() + ": class=" + getClassName() +     //NOI18N
            ", name=" + getName() + ", descriptor=" + getDescriptor();  //NOI18N
    }
    
    public final String getSignature() {
        return getSignature(getDescriptor(), true);
    }
    
    static String getSignature(String s, boolean fullName) {
        StringBuffer sb = new StringBuffer();
        int arrays = 0;
        int i = 0;
        while (i < s.length()) {
            char ch = s.charAt(i++);
            switch (ch) {
                case 'B': sb.append("byte"); continue; //NOI18N
                case 'C': sb.append("char"); continue; //NOI18N
                case 'D': sb.append("double"); continue; //NOI18N
                case 'F': sb.append("float"); continue; //NOI18N
                case 'I': sb.append("int"); continue; //NOI18N
                case 'J': sb.append("long"); continue; //NOI18N
                case 'S': sb.append("short"); continue; //NOI18N
                case 'Z': sb.append("boolean"); continue; //NOI18N
                case 'V': sb.append("void"); continue; //NOI18N
                
                case 'L':
                    int l = s.indexOf(';');
                    String cls = s.substring(1, l).replace('/', '.');
                    if (!fullName) {
                        int idx = cls.lastIndexOf('.');
                        if (idx >= 0)
                            cls = cls.substring(idx+1);
                    }
                    sb.append(cls);
                    i = l + 1;
                    continue;
                
                case '[':
                    arrays++;
                    continue;
                default:
                    break; // invalid character
            }
        }
        while (arrays-- > 0)
            sb.append("[]");
        return sb.toString();
    }
    
    void resolve(CPEntry[] cpEntries) {
        // Read in NameAndTypeInfo values.
        CPNameAndTypeInfo nati = (CPNameAndTypeInfo)cpEntries[iNameAndType];
        setNameIndex(nati.iName);
        setDescriptorIndex(nati.iDesc);
    }
}
