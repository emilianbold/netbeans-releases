/*
 * AnnotationComponent.java
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

import java.io.DataInputStream;
import java.io.IOException;

/**
 * AnnotationComponent:  a single annotation on a program element.
 *
 * @author  Thomas Ball
 */
public class AnnotationComponent {
    String name;
    ElementValue value;

    static AnnotationComponent load(DataInputStream in, ConstantPool pool,
				    boolean runtimeVisible)
	throws IOException {
	int iName = in.readUnsignedShort();
	String name = ((CPName)pool.get(iName)).getName();
	ElementValue value = ElementValue.load(in, pool, runtimeVisible);
	return new AnnotationComponent(name, value);
    }

    AnnotationComponent(String name, ElementValue value) {
	this.name = name;
	this.value = value;
    }

    /**
     * Returns the name of this component.
     */
    public final String getName() {
	return name;
    }

    /**
     * Returns the value for this component.
     */
    public final ElementValue getValue() {
	return value;
    }

    public String toString() {
	return "name=" + name + ", value=" + value;
    }
}
