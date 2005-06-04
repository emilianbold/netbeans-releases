/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.lookup;

import org.netbeans.junit.*;
import junit.textui.TestRunner;

import java.io.File;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.core.NbTopManager;
import org.netbeans.core.startup.ModuleHistory;
import org.openide.util.Lookup;
import javax.swing.Action;
import java.util.Iterator;
import org.openide.loaders.DataObject;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.util.Mutex;
import org.openide.cookies.InstanceCookie;
import org.openide.util.MutexException;
import org.openide.util.LookupListener;
import org.openide.util.LookupEvent;

/** A test.
 */
public class InstanceDataObjectModule38420Test extends InstanceDataObjectModuleTestHid {

    public InstanceDataObjectModule38420Test (String name) {
        super(name);
    }
    
    public static void main(String[] args) {
        TestRunner.run(new NbTestSuite(InstanceDataObjectModule38420Test.class));
    }
    
    public void testEnableDisableOfModulePreservesExistingInstances () throws Exception {
        Lookup.Result res = Lookup.getDefault ().lookup (new Lookup.Template (Action.class));
        Action found = null;
        twiddle(m1, TWIDDLE_ENABLE);
        try {
            twiddle(m2, TWIDDLE_ENABLE);
            StringBuffer foundLog = new StringBuffer ();
            try {
                java.util.Iterator it = res.allInstances ().iterator ();
                while (it.hasNext ()) {
                    Action a = (Action)it.next ();
                    if ("test1.SomeAction".equals (a.getClass ().getName ())) {
                        found = a;
                    } else {
                        foundLog.append ("Found: ");
                        foundLog.append (a.getClass ().getName ());
                        foundLog.append ("\n");
                    }
                }
                assertNotNull ("Action from module m1 has been found. Only found:\n" + foundLog, found);

            } finally {
                twiddle (m2, TWIDDLE_DISABLE);
            }

            Action again = (Action)Lookup.getDefault ().lookup (found.getClass ());
            assertSame ("The instance remains the same", found, again);
            
            java.lang.ref.WeakReference ref = new java.lang.ref.WeakReference (found);
            found = null;
            again = null;
            res = null;
            assertGC ("Content of lookup is hold by a weak reference", ref);

        } finally {
            twiddle(m1, TWIDDLE_DISABLE);
        }
    }
    
}
