/*
 * CPNameAndTypeInfo.java
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
 * A class representing the CONSTANT_NameAndType constant pool type.
 *
 * @author Thomas Ball
 */
public class CPNameAndTypeInfo extends CPEntry {
    int iName;
    int iDesc;

    CPNameAndTypeInfo(ConstantPool pool,int iName,int iDesc) {
	super(pool);
        this.iName = iName;
        this.iDesc = iDesc;
    }

    protected CPNameAndTypeInfo(ConstantPool pool) {
        super(pool);
        iName = CPName.INVALID_INDEX;
        iDesc = CPName.INVALID_INDEX;
    }

    public final String getName() {
	return ((CPName)pool.cpEntries[iName]).getName();
    }

    void setNameIndex(int index) {
	iName = index;
    }

    public final String getDescriptor() {
	return ((CPName)pool.cpEntries[iDesc]).getName();
    }

    void setDescriptorIndex(int index) {
	iDesc = index;
    }

    public int getTag() {
	return ConstantPool.CONSTANT_NameAndType;
    }

    public String toString() {
        return getClass().getName() + ": name=" + getName() + //NOI18N
            ", descriptor=" + getDescriptor(); //NOI18N
    }
}
