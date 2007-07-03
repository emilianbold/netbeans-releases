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

package org.netbeans.modules.ruby;

import junit.framework.TestCase;

/**
 *
 * @author Tor Norbye
 */
public class ArityTest extends TestCase {
    
    public ArityTest(String testName) {
        super(testName);
    }

    public void testArityMatches() {
        assertTrue(Arity.matches(Arity.createTestArity(0, 0), Arity.createTestArity(0,0)));
        assertTrue(Arity.matches(Arity.createTestArity(1, 1), Arity.createTestArity(1,1)));
        assertTrue(Arity.matches(Arity.createTestArity(3, 3), Arity.createTestArity(3,5)));
        assertTrue(Arity.matches(Arity.createTestArity(5, 5), Arity.createTestArity(3,5)));
        assertTrue(Arity.matches(Arity.createTestArity(4, 4), Arity.createTestArity(3,5)));
        assertTrue(Arity.matches(Arity.createTestArity(4, 4), Arity.createTestArity(3,5)));
        assertTrue(Arity.matches(Arity.createTestArity(4, 4), Arity.createTestArity(3,5)));
        assertTrue(Arity.matches(Arity.createTestArity(4, 4), Arity.createTestArity(0,Integer.MAX_VALUE)));

        assertFalse(Arity.matches(Arity.createTestArity(2, 2), Arity.createTestArity(1,1)));
        assertFalse(Arity.matches(Arity.createTestArity(2, 2), Arity.createTestArity(0,1)));
        assertFalse(Arity.matches(Arity.createTestArity(2, 2), Arity.createTestArity(3,4)));
    }
}
