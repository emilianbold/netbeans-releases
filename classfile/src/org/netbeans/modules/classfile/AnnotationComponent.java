/*
 * AnnotationComponent.java
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
 * AnnotationComponent:  a single annotation on a program element.
 *
 * @author  Thomas Ball
 */
public abstract class AnnotationComponent {
    String name;
    int tag;

    static AnnotationComponent load(DataInputStream in, ConstantPool pool) 
	throws IOException {
	int iName = in.readShort();
	char tag = (char)in.readByte();
	switch (tag) {
	  case 'e': {
	      int enumType = in.readShort();
	      int enumConst = in.readShort();
	      return new EnumAnnotation(pool, iName, tag, enumType, enumConst);
	  }
	  case 'c': {
	      int classType = in.readShort();
	      return new ClassAnnotation(pool, iName, tag, classType);
	  }
	  case '@': {
	      AnnotationComponent value = AnnotationComponent.load(in, pool);
	      return new NestedAnnotation(pool, iName, tag, value);
	  }
	  case '[': {
	      AnnotationComponent[] values = 
		  new AnnotationComponent[in.readShort()];
	      return new ArrayAnnotation(pool, iName, tag, values);
	  }
	  default:
	      assert "BCDFIJSZs".indexOf(tag) >= 0 : "invalid annotation tag";
	      return new PrimitiveAnnotation(pool, iName, tag, in.readShort());
	}
    }

    AnnotationComponent(ConstantPool pool, int iName, int tag) {
	this.name = ((CPName)pool.get(iName)).getName();
	this.tag = tag;
    }

    /**
     * Returns the name of this component.
     */
    public final String getName() {
	return name;
    }

    /**
     * Returns the type for this component.  Primitive types are 
     * represented by their classtype letters:  'B', 'C', 'D', 'F',
     * 'I', 'J', and 'Z'.  The other valid types are
     * 'S' for String, 'e' for enum constant, 'c' for class, '@'
     * for annotation type, and '[' for array.
     */
    public final int getTag() {
	return tag;
    }

    public String toString() {
	return name + " tag=" + tag;
    }
}
