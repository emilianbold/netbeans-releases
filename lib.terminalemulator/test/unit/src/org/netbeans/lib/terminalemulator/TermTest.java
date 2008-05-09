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

package org.netbeans.lib.terminalemulator;

import java.lang.reflect.Modifier;
import junit.framework.*;
import org.netbeans.junit.*;
import org.netbeans.lib.terminalemulator.Coord;
import org.netbeans.lib.terminalemulator.Term;

/**
 *
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 */
public class TermTest extends NbTestCase {

    public TermTest(java.lang.String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new NbTestSuite(TermTest.class));
    }
    
    /** This test assures compatibility with Jelly library.
     * Please contact QA or any JellyTools developer in case of failure.
     */    
    public void testJellyCompatibility() {
        try {
            assertTrue("Term class is public", Modifier.isPublic(Term.class.getModifiers()));
            assertTrue("Coord class is public", Modifier.isPublic(Coord.class.getModifiers()));
            Term term = new Term();
            term.getRowText(0);
            term.textWithin(Coord.make(0,0),Coord.make(0,0));
            term.getCursorRow();
            term.flush();
            Class.forName("org.netbeans.lib.terminalemulator.Screen");
        } catch (Exception e) {
            throw new AssertionFailedErrorException("JellyTools compatibility conflict, please contact QA or any JellyTools developer.", e);
        }
    }
    
    
}
