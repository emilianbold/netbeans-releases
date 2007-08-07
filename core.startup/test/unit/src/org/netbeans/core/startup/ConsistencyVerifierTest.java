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

package org.netbeans.core.startup;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.Manifest;
import org.netbeans.junit.NbTestCase;

/**
 * @author Jesse Glick
 */
public class ConsistencyVerifierTest extends NbTestCase {

    public ConsistencyVerifierTest(String n) {
        super(n);
    }

    private Set<Manifest> modules(String... descs) {
        Set<Manifest> modules = new HashSet<Manifest>();
        for (String desc : descs) {
            Manifest m = new Manifest();
            for (String piece : desc.split("; ")) {
                String[] lhsRhs = piece.split("=");
                assert lhsRhs.length == 2 : "'" + piece + "' in '" + desc + "'";
                m.getMainAttributes().putValue(
                        lhsRhs[0].matches("autoload|eager") ? lhsRhs[0] : lhsRhs[0].length() == 0 ? "OpenIDE-Module" : "OpenIDE-Module-" + lhsRhs[0],
                        lhsRhs[1]);
            }
            modules.add(m);
        }
        return modules;
    }

    private void assertProblems(String problems, String... descs) {
        assertEquals("for " + Arrays.toString(descs),
                problems, ConsistencyVerifier.findInconsistencies(modules(descs), Collections.singleton("placeholder"), false).toString());
    }

    public void testBasicFunctionality() throws Exception {
        assertProblems("{}",
                "=foo",
                "=bar; Module-Dependencies=foo");
        assertProblems("{foo=[module bar]}",
                "=foo; Module-Dependencies=bar");
        assertProblems("{foo=[requires svc]}",
                "=foo; Requires=svc");
        assertProblems("{}",
                "=foo; Requires=svc",
                "=bar; Provides=svc");
        assertProblems("{bar=[requires svc], foo=[requires svc]}",
                "=foo; Requires=svc",
                "=bar; Requires=svc");
        assertProblems("{foo=[module bar > 2.0, requires svc]}",
                "=foo; Requires=svc; Module-Dependencies=bar > 2.0",
                "=bar; Specification-Version=1.5");
    }

    public void testStandardProvides() throws Exception {
        assertProblems("{}",
                "=foo; Requires=org.openide.modules.ModuleFormat1");
        assertProblems("{foo=[requires org.openide.modules.ModuleFormat2]}",
                "=foo; Requires=org.openide.modules.ModuleFormat2");
        assertProblems("{}",
                "=foo; Requires=org.openide.modules.os.Unix");
        assertProblems("{}",
                "=foo; Requires=org.openide.modules.os.PlainUnix");
        assertProblems("{}",
                "=foo; Requires=org.openide.modules.os.Windows");
        assertProblems("{}",
                "=foo; Requires=org.openide.modules.os.MacOSX");
        assertProblems("{}",
                "=foo; Requires=org.openide.modules.os.OS2");
        assertProblems("{}",
                "=foo; Requires=org.openide.modules.os.Linux");
        assertProblems("{}",
                "=foo; Requires=org.openide.modules.os.Solaris");
        assertProblems("{foo=[requires org.openide.modules.os.Windoze]}",
                "=foo; Requires=org.openide.modules.os.Windoze");
    }

    public void testIAE() throws Exception {
        try {
            ConsistencyVerifier.findInconsistencies(modules("=foo; Whatever=1", "=foo; Whatever=2"), null, false);
            fail();
        } catch (IllegalArgumentException x) {}
        try {
            ConsistencyVerifier.findInconsistencies(modules("Whatever=17"), null, false);
            fail();
        } catch (IllegalArgumentException x) {}
        try {
            ConsistencyVerifier.findInconsistencies(modules("=11"), null, false);
            fail();
        } catch (IllegalArgumentException x) {}
    }

    public void testCheckAutoloadsEnabled() throws Exception {
        assertProblems("{}", "=foo; Module-Dependencies=bar", "=bar; autoload=true");
        assertProblems("{baz=[module is autoload but would not be enabled]}", "=foo; Module-Dependencies=bar", "=bar; autoload=true", "=baz; autoload=true");
        assertProblems("{}", "=compat; autoload=true; Deprecated=true");
        assertProblems("{}", "=placeholder; autoload=true");
    }

}
