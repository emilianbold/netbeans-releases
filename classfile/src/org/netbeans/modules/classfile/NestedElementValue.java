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
 * NestedElementValue:  an annotation on a program element that is
 * another annotation.  The value for this annotation is the
 * nested AnnotationComponent.
 *
 * @author  Thomas Ball
 */
public final class NestedElementValue extends ElementValue {
    Annotation value;

    NestedElementValue(ConstantPool pool, Annotation value) {
	this.value = value;
    }

    /**
     * Returns the value of this component, which is an Annotation.
     */
    public final Annotation getNestedValue() {
	return value;
    }

    public String toString() {
	return "nested value=" + value;
    }
}
