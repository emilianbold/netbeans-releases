/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.debugger.jpda.expr;

import java.util.Collection;

/**
 * Support class to help assertions and error processing when evaluating an expression.
 *
 * @author Maros Sandor
 */
class Assert {

    static Object error(SimpleNode node, String param) throws EvaluationException {
        return error(node, param, null);
    }

    static Object error(SimpleNode node, String cause, Object p2) throws EvaluationException {
        return error(node, cause, new Object [] { p2 });
    }

    static Object error(SimpleNode node, String cause, Object p2, Object p3) throws EvaluationException {
        return error(node, cause, new Object [] { p2, p3});
    }

    static Object error(SimpleNode node, String cause, Object p1, Object p2, Object p3) throws EvaluationException {
        return error(node, cause, new Object [] { p1, p2, p3});
    }

    private static Object error (SimpleNode node, String cause, Object [] params) throws EvaluationException {
        throw new EvaluationException(node, cause, params);
    }

    static void assertAssignable(Object o, Class aClass, SimpleNode s, String p1, Object p2) {
        if (o != null && !aClass.isAssignableFrom(o.getClass())) {
            error(s, p1, p2);
        }
    }

    static void assertAssignable(Object o, Class aClass, SimpleNode s, String p1, Object p2, Object p3) {
        if (o != null && !aClass.isAssignableFrom(o.getClass())) {
            error(s, p1, p2, p3);
        }
    }

    static void assertNotAssignable(Object o, Class aClass, SimpleNode s, String p1, Object p2) {
        if (aClass.isAssignableFrom(o.getClass())) {
            error(s, p1, p2);
        }
    }

    static void assertNotAssignable(Object o, Class aClass, SimpleNode s, String p1, Object p2, Object p3) {
        if (aClass.isAssignableFrom(o.getClass())) {
            error(s, p1, p2, p3);
        }
    }

    static void assertNotAssignable(Object o, Class aClass, SimpleNode node, String s) {
        if (aClass.isAssignableFrom(o.getClass())) {
            error(node, s);
        }
    }

    static void assertLess(int a, int b, SimpleNode s, String p1, Object p2, Object p3) {
        if (a >= b) {
            error(s, p1, p2, p3);
        }
    }

    static void assertNotNull(Object obj, SimpleNode s, String identifier) {
        if (obj == null) {
            error(s, identifier);
        }
    }

    static void assertNonEmpty(Collection collection, SimpleNode s, String p1, Object p2) {
        if (collection == null || collection.size() == 0) {
            error(s, p1, p2);
        }
    }

    static void assertNotNull(Object obj, SimpleNode node, String p1, Object p2) {
        if (obj == null) {
            error(node, p1, p2);
        }
    }

}
