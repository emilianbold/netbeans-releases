/*
 * ClassElementValue.java
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

import java.io.DataInputStream;
import java.io.IOException;

/**
 * ClassElementValue:  the value part of a single element for
 * those annotations that are a class type.  The value for these 
 * annotations is a CPClassInfo constant pool entry, which describes 
 * the class.
 *
 * @author  Thomas Ball
 */
public final class ClassElementValue extends ElementValue {
    CPClassInfo value;

    ClassElementValue(ConstantPool pool, int iValue) {
	this.value = (CPClassInfo)pool.get(iValue);
    }

    /**
     * Returns the value of this component, as a class constant pool entry.
     */
    public final CPClassInfo getClassValue() {
	return value;
    }

    public String toString() {
	return "class=" + value;
    }
}
