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

package org.openide.text;

import org.netbeans.junit.NbTestCase;

/**
 * Regression tests.
 *
 * How to run from IDE:
 *   1. Mount jar: junit.jar
 *   2. Mount dir: openide/src
 *   3. Mount dir: openide/test/regr/src
 *   4. Run class TextTest from dir openide/test/regr/src in internal execution
 *   (inside IDE VM - set execution type in Properties window)
 *   It will open new window in Editor. When deadlock is there IDE hangs.
 * How to run from command line:
 *   In directory: <NetBeans>/openide/test/
 *   Command: ant -Dxtest.attributes=regr
 *
 * @author  Marek Slama, Yarda Tulach
 */
public class TextTest extends NbTestCase {

    /** Creates new TextTest */
    public TextTest(String s) {
        super(s);
    }
    
    /** Regression test to reproduce deadlock from bug #10449. */
    public void testDeadlock() throws Exception {
        System.out.println(System.currentTimeMillis() + " testDeadlock START");

        CloneableEditorSupport.Env env = new EmptyCESHidden.Env ();
        CloneableEditorSupport tst = new EmptyCESHidden(env);
        
        ((EmptyCESHidden.Env)env).setInstance(tst);

        Object doc = tst.openDocument();

        tst.open();

        System.out.println(System.currentTimeMillis() + " testDeadlock END");
    }
    
}
