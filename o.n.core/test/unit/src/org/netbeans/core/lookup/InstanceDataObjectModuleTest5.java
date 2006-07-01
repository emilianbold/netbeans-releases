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
 * @author Jesse Glick
 * @see InstanceDataObjectModuleTestHid
 */
public class InstanceDataObjectModuleTest5 extends InstanceDataObjectModuleTestHid {

    public InstanceDataObjectModuleTest5(String name) {
        super(name);
    }
    
    public void testReloadSettingsSwitchesLookupByNewClass() throws Exception {
        ERR.log("before twidle enabled");
        twiddle(m2, TWIDDLE_ENABLE);
        ERR.log("Ok twidle enable");
        ClassLoader l1 = null;
        ClassLoader l2 = null;
        try {
            l1 = m2.getClassLoader();
            Class c1 = l1.loadClass("test2.SomeAction");
            assertEquals("Correct loader", l1, c1.getClassLoader());
            assertTrue("SomeAction<1> instance found after module installation",
                existsSomeAction(c1));
            
            ERR.log("Action successfully checked, reload"); 
            twiddle(m2, TWIDDLE_RELOAD);
            ERR.log("Twidle reload");
            l2 = m2.getClassLoader();
            assertTrue("ClassLoader really changed", l1 != l2);
            Class c2 = l2.loadClass("test2.SomeAction");
            assertTrue("Class really changed", c1 != c2);
            
            ERR.log("After successful checks that there was a reload changes");
            
            LoaderPoolNode.waitFinished();
            
            ERR.log("Waiting for pool node to update itself");

            assertTrue("SomeAction<1> instance not found after module reload",
                !existsSomeAction(c1));
            assertTrue("SomeAction<2> instance found after module reload",
                existsSomeAction(c2));
        } finally {
            ERR.log("Verify why it failed");
            FileObject fo = Repository.getDefault().getDefaultFileSystem().findResource("Services/Misc/inst-2.settings");
            ERR.log("File object found: " + fo);
            if (fo != null) {
                DataObject obj = DataObject.find(fo);
                ERR.log("data object found: " + obj);
                InstanceCookie ic = (InstanceCookie)obj.getCookie(InstanceCookie.class);
                ERR.log("InstanceCookie: " + ic);
                if (ic != null) {
                    ERR.log("value: " + ic.instanceCreate());
                    ERR.log(" cl  : " + ic.instanceCreate().getClass().getClassLoader());
                    ERR.log(" l1  : " + l1);
                    ERR.log(" l2  : " + l2);
                }
            }
            ERR.log("Disabling");
            twiddle(m2, TWIDDLE_DISABLE);
            ERR.log("Disabled");
        }
    }
    
}
