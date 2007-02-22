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
 * ArrayElementValue:  the value portion of an annotation element that
 * is an array of ElementValue instances.
 *
 * @author  Thomas Ball
 */
public final class ArrayElementValue extends ElementValue {
    ElementValue[] values;

    ArrayElementValue(ConstantPool pool, ElementValue[] values) {
	this.values = values;
    }

    /**
     * Returns the set of ElementValue instances for this component.
     */
    public ElementValue[] getValues() {
	return values.clone();
    }

    public String toString() {
	StringBuffer sb = new StringBuffer("[");
	int n = values.length;
	for (int i = 0; i < n; i++) {
	    sb.append(values[i]);
	    if ((i + 1) < n)
		sb.append(',');
	}
	sb.append(']');
	return sb.toString();
    }
}
