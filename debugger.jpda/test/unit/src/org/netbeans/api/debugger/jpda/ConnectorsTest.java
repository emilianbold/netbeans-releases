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

package org.netbeans.api.debugger.jpda;

/**
 * Tests attaching cookie and connector. Launches a VM in server mode and tries to attach to it.
 * After successfuly attaching to the VM and stopping in main, this test finished debugging sessiona and terminates.
 *
 * @author Maros Sandor
 */
public class ConnectorsTest extends DebuggerJPDAApiTestBase {

    public ConnectorsTest(String s) {
        super(s);
    }

    public void testAttach() throws Exception {

        JPDASupport support = JPDASupport.attach("org.netbeans.api.debugger.jpda.testapps.EmptyApp");
        support.doFinish();
    }

    public void testListen() throws Exception {

        JPDASupport support = JPDASupport.listen("org.netbeans.api.debugger.jpda.testapps.EmptyApp");
        support.doFinish();
    }
}
