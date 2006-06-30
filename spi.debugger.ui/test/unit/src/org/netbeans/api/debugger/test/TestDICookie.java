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

package org.netbeans.api.debugger.test;


import java.io.IOException;
import java.util.*;

/**
 * A test DebuggerInfo cookie.
 *
 * @author Maros Sandor
 */
public class TestDICookie {

    /**
     * Public ID used for registration in Meta-inf/debugger.
     */
    public static final String ID = "netbeans-test-TestDICookie";

    private Map args;

    private TestDICookie(Map args) {
        this.args = args;
    }

    /**
     * Creates a new instance of ListeningDICookie for given parameters.
     *
     * @param args arguments to be used
     * @return a new instance of ListeningDICookie for given parameters
     */
    public static TestDICookie create(Map args) {
        return new TestDICookie (args);
    }

    /**
     * Returns map of arguments to be used.
     *
     * @return map of arguments to be used
     */
    public Map getArgs () {
        return args;
    }

    private Set infos = new HashSet();

    public void addInfo(Object s) {
        infos.add(s);
    }

    public boolean hasInfo(Object s) {
        return infos.contains(s);
    }
}
