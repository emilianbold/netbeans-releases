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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.debugger.jpda.expr;

import com.sun.source.tree.Tree;
import java.util.Collection;

/**
 * Support class to help assertions and error processing when evaluating an expression.
 *
 * @author Maros Sandor
 */
class Assert2 {

    static Object error(Tree node, String param) throws EvaluationException {
        return error(node, param, null);
    }

    static Object error(Tree node, String cause, Object p2) throws EvaluationException {
        return error(node, cause, new Object [] { p2 });
    }

    static Object error(Tree node, String cause, Object p2, Object p3) throws EvaluationException {
        return error(node, cause, new Object [] { p2, p3});
    }

    static Object error(Tree node, String cause, Object p1, Object p2, Object p3) throws EvaluationException {
        return error(node, cause, new Object [] { p1, p2, p3});
    }

    private static Object error (Tree node, String cause, Object [] params) throws EvaluationException {
        throw new EvaluationException2(node, cause, params);
    }

    static void assertAssignable(Object o, Class aClass, Tree s, String p1, Object p2) {
        if (o != null && !aClass.isAssignableFrom(o.getClass())) {
            error(s, p1, p2);
        }
    }

    static void assertAssignable(Object o, Class aClass, Tree s, String p1, Object p2, Object p3) {
        if (o != null && !aClass.isAssignableFrom(o.getClass())) {
            error(s, p1, p2, p3);
        }
    }

    static void assertNotAssignable(Object o, Class aClass, Tree s, String p1, Object p2) {
        if (aClass.isAssignableFrom(o.getClass())) {
            error(s, p1, p2);
        }
    }

    static void assertNotAssignable(Object o, Class aClass, Tree s, String p1, Object p2, Object p3) {
        if (aClass.isAssignableFrom(o.getClass())) {
            error(s, p1, p2, p3);
        }
    }

    static void assertNotAssignable(Object o, Class aClass, Tree node, String s) {
        if (aClass.isAssignableFrom(o.getClass())) {
            error(node, s);
        }
    }

    static void assertLess(int a, int b, Tree s, String p1, Object p2, Object p3) {
        if (a >= b) {
            error(s, p1, p2, p3);
        }
    }

    static void assertNotNull(Object obj, Tree s, String identifier) {
        if (obj == null) {
            error(s, identifier);
        }
    }

    static void assertNonEmpty(Collection collection, Tree s, String p1, Object p2) {
        if (collection == null || collection.size() == 0) {
            error(s, p1, p2);
        }
    }

    static void assertNotNull(Object obj, Tree node, String p1, Object p2) {
        if (obj == null) {
            error(node, p1, p2);
        }
    }

}
