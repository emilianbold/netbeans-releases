/*
 * CPUTFInfo.java
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
 * A class representing the CONSTANT_Utf8 constant pool type.
 *
 * @author Thomas Ball
 */
public final class CPUTF8Info extends CPName {
    String name;
    byte[] utf;

    CPUTF8Info(ConstantPool pool, String name) {
	super(pool);
	this.name = name;
    }

    CPUTF8Info(ConstantPool pool, byte[] utf) {
	super(pool);
	this.utf = utf;
    }

    public final String getName() {
	if (name == null) {
	    name = ConstantPoolReader.readUTF(utf, utf.length);
	    utf = null;
	}
	return name;
    }

    public final Object getValue() {
        return getName();
    }

    public final int getTag() {
	return ConstantPool.CONSTANT_Utf8;
    }

    public String toString() {
	return getClass().getName() + ": name=" + getName(); //NOI18N
    }
}
