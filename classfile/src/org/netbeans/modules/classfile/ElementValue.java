/*
 * ElementValue.java
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
 * ElementValue:  the value portion of the name-value pair of a
 * single annotation element.
 *
 * @author  Thomas Ball
 */
public abstract class ElementValue {
    static ElementValue load(DataInputStream in, ConstantPool pool) 
	throws IOException {
	char tag = (char)in.readByte();
	switch (tag) {
	  case 'e': {
	      int enumType = in.readShort();
	      int enumConst = in.readShort();
	      return new EnumElementValue(pool, enumType, enumConst);
	  }
	  case 'c': {
	      int classType = in.readShort();
	      return new ClassElementValue(pool, classType);
	  }
	  case '@': {
	      AnnotationComponent value = AnnotationComponent.load(in, pool);
	      return new NestedElementValue(pool, value);
	  }
	  case '[': {
	      ElementValue[] values = new ElementValue[in.readShort()];
	      for (int i = 0; i < values.length; i++)
		  values[i] = load(in, pool);
	      return new ArrayElementValue(pool, values);
	  }
	  default:
	      assert "BCDFIJSZs".indexOf(tag) >= 0 : "invalid annotation tag";
	      return new PrimitiveElementValue(pool, in.readShort());
	}
    }

    /* Package-private constructor so that only classes in this
     * package can subclass.
     */
    ElementValue() {
    }
}
