/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
