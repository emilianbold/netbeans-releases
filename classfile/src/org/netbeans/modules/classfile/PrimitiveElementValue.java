/*
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
 */

package org.netbeans.modules.classfile;

/**
 * A PrimitiveElementValue is the value portion of an annotation component
 * that has a primitive type or String constant.  Its value
 * is a constant pool entry of the same type as the primitive;
 * for example, an int constant would have a value of type CPIntegerInfo.
 *
 * @author  Thomas Ball
 */
public final class PrimitiveElementValue extends ElementValue {
    CPEntry value;

    PrimitiveElementValue(ConstantPool pool, int iValue) {
	this.value = pool.get(iValue);
    }

    /**
     * Returns the value of this component, as a constant pool entry.
     */
    public final CPEntry getValue() {
	return value;
    }

    public String toString() {
	return "const=" + value.getValue();
    }
}
