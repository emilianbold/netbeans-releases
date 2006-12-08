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

package org.openide.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import junit.framework.TestCase;

/**
 *
 * @author Andrei Badea
 */
public class ParametersTest extends TestCase {

    public ParametersTest(String testName) {
        super(testName);
    }

    public void testNotNull() throws Exception {
        assertNPEOnNull(Parameters.class.getMethod("notNull", CharSequence.class, Object.class));
        Parameters.notNull("param", "");
    }

    public void testNotEmpty() throws Exception {
        assertNPEOnNull(Parameters.class.getMethod("notEmpty", CharSequence.class, CharSequence.class));
        try {
            Parameters.notEmpty("param", "");
            fail("Should have thrown IAE");
        } catch (IllegalArgumentException e) {}
        Parameters.notEmpty("param", "foo");
    }

    public void testNotWhitespace() throws Exception {
        assertNPEOnNull(Parameters.class.getMethod("notWhitespace", CharSequence.class, CharSequence.class));
        try {
            Parameters.notWhitespace("param", "");
            fail("Should have thrown IAE");
        } catch (IllegalArgumentException e) {}
        try {
            Parameters.notWhitespace("param", " ");
            fail("Should have thrown IAE");
        } catch (IllegalArgumentException e) {}
        Parameters.notWhitespace("param", " foo ");
    }

    public void testJavaIdentifier() throws Exception {
        assertNPEOnNull(Parameters.class.getMethod("javaIdentifier", CharSequence.class, CharSequence.class));
        try {
            Parameters.javaIdentifier("param", "");
            fail("Should have thrown IAE");
        } catch (IllegalArgumentException e) {}
        try {
            Parameters.javaIdentifier("param", "foo#Method");
            fail("Should have thrown IAE");
        } catch (IllegalArgumentException e) {}
        Parameters.javaIdentifier("param", "fooMethod");
    }

    public void testJavaIdentifierOrNull() throws Exception {
        try {
            Parameters.javaIdentifierOrNull("param", "");
            fail("Should have thrown IAE");
        } catch (IllegalArgumentException e) {}
        try {
            Parameters.javaIdentifierOrNull("param", "foo#Method");
            fail("Should have thrown IAE");
        } catch (IllegalArgumentException e) {}
        Parameters.javaIdentifierOrNull("param", null);
        Parameters.javaIdentifierOrNull("param", "fooMethod");
    }

    private void assertNPEOnNull(Method method) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException  {
        try {
            method.invoke(null, "param", null);
            fail("Should have thrown NPE");
        } catch (InvocationTargetException e) {
            Throwable target = e.getTargetException();
            assertEquals(NullPointerException.class, target.getClass());
            // ensure the NPE was thrown by us, not by the VM
            assertEquals("The param parameter cannot be null", target.getMessage());
        }
    }
}
