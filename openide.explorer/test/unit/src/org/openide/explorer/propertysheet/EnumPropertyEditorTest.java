/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.explorer.propertysheet;

import java.beans.PropertyEditor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import org.netbeans.junit.NbTestCase;
import org.openide.nodes.PropertySupport;

/**
 * Check that enumeration types have some kind of minimal proped.
 * @author Jesse Glick
 */
public class EnumPropertyEditorTest extends NbTestCase {

    public EnumPropertyEditorTest(String name) {
        super(name);
    }

    public void testEnumPropEd() throws Exception {
        EProp prop = new EProp();
        PropertyEditor ed = PropUtils.getPropertyEditor(prop);
        assertFalse(ed.supportsCustomEditor());
        assertFalse(ed.isPaintable());
        String[] tags = ed.getTags();
        assertNotNull(tags);
        assertEquals("[CHOCOLATE, VANILLA, STRAWBERRY]", Arrays.toString(tags));
        assertEquals(E.VANILLA, ed.getValue());
        assertEquals("VANILLA", ed.getAsText());
        ed.setAsText("STRAWBERRY");
        assertEquals(E.STRAWBERRY, ed.getValue());
        assertEquals(E.class.getName().replace('$', '.') + ".STRAWBERRY", ed.getJavaInitializationString());
    }

    public void testNulls() throws Exception {
        EProp prop = new EProp();
        PropertyEditor ed = PropUtils.getPropertyEditor(prop);
        ed.setAsText("");
        assertEquals(null, ed.getValue());
        assertEquals("", ed.getAsText());
        assertEquals("null", ed.getJavaInitializationString());
    }

    public enum E {
        CHOCOLATE, VANILLA, STRAWBERRY
    }

    private static class EProp extends PropertySupport.ReadWrite {

        private E e = E.VANILLA;

        public EProp() {
            super("eval", E.class, "E Val", "E value");
        }

        public Object getValue() throws IllegalAccessException, InvocationTargetException {
            return e;
        }

        public void setValue(Object val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            e = (E) val;
        }

    }

}
