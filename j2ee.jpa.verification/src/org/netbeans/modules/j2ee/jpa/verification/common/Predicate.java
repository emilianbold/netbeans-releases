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

package org.netbeans.modules.j2ee.jpa.verification.common;

/**
 * This term is borrowed from propositional logic.
 * It represents predicate part of "S is P" kind of preposition.
 * In other words, it represents something that is affirmed or denied of the
 * subject in a proposition in logic (taken from http://m-w.com).
 * e.g. In the proposition <i>Foo.class is an entity class</i>
 * Foo.class is the subject, the property of being an entity class is the predicate.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public interface Predicate {
    /**
     * Evaluate the predicate for this subject.
     * @param subject for which this predicate will be evaluated
     * @return true if the subject has this property, else false.
     * It also returns false if the subject is not applicable to this predicate.
     */
    boolean evaluate(Object subject);
}
