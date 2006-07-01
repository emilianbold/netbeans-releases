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
