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

package org.netbeans.core.lookup;

import org.netbeans.core.LoaderPoolNode;
import org.openide.util.Lookup;

/** A test.
 * @author Jesse Glick
 * @see InstanceDataObjectModuleTestHid
 */
public class InstanceDataObjectModuleTest6 extends InstanceDataObjectModuleTestHid {

    public InstanceDataObjectModuleTest6(String name) {
        super(name);
    }

    public void testReloadSettingsCausesLookupResultChange() throws Exception {
        ERR.log("before twidle enabled");
        twiddle(m2, TWIDDLE_ENABLE);
        ERR.log("Ok twidle enable");
        try {
            ClassLoader l1 = m2.getClassLoader();
            Class c1 = l1.loadClass("test2.SomeAction");
            assertEquals("Correct loader", l1, c1.getClassLoader());
            Lookup.Result r = Lookup.getDefault().lookupResult(c1);
            assertTrue("SomeAction<1> instance found after module installation",
                existsSomeAction(c1, r));
            ERR.log("Action successfully checked, reload"); 
            
            
            LookupL l = new LookupL();
            r.addLookupListener(l);
            ERR.log("Listener attached"); 
            twiddle(m2, TWIDDLE_RELOAD);
            ERR.log("Reload done");
            assertTrue("Got a result change after module reload", l.gotSomething());

            ERR.log("wait for loader pool");
            LoaderPoolNode.waitFinished();
            ERR.log("Pool refreshed");
            
            assertTrue("SomeAction<1> instance not found after module reload",
                !existsSomeAction(c1, r));
        } finally {
            ERR.log("finally disable");
            twiddle(m2, TWIDDLE_DISABLE);
            ERR.log("finally disable done");
        }
    }
    
}
