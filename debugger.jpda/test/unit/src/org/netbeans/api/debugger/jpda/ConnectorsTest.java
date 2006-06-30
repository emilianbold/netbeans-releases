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

package org.netbeans.api.debugger.jpda;

import org.netbeans.junit.NbTestCase;

/**
 * Tests attaching cookie and connector. Launches a VM in server mode and tries to attach to it.
 * After successfuly attaching to the VM and stopping in main, this test finished debugging sessiona and terminates.
 *
 * @author Maros Sandor, Jan Jancura
 */
public class ConnectorsTest extends NbTestCase {

    public ConnectorsTest (String s) {
        super (s);
    }

    public void testAttach () throws Exception {

        JPDASupport support = JPDASupport.attach (
            "org.netbeans.api.debugger.jpda.testapps.EmptyApp"
        );
        support.doFinish ();
    }

    public void testListen () throws Exception {

        JPDASupport support = JPDASupport.attach (
            "org.netbeans.api.debugger.jpda.testapps.EmptyApp"
        );
        support.doFinish ();
    }
}
