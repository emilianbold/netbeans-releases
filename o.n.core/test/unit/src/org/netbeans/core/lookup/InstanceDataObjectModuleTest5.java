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
 * @author Jesse Glick
 * @see InstanceDataObjectModuleTestHid
 */
public class InstanceDataObjectModuleTest5 extends InstanceDataObjectModuleTestHid {

    public InstanceDataObjectModuleTest5(String name) {
        super(name);
    }
    
    public static void main(String[] args) {
        // Turn on verbose logging while developing tests:
        //System.setProperty("org.netbeans.core.modules", "0");
        TestRunner.run(new NbTestSuite(InstanceDataObjectModuleTest5.class));
    }
    
    public void testReloadSettingsSwitchesLookupByNewClass() throws Exception {
        twiddle(m2, TWIDDLE_ENABLE);
        try {
            ClassLoader l1 = m2.getClassLoader();
            Class c1 = l1.loadClass("test2.SomeAction");
            assertEquals("Correct loader", l1, c1.getClassLoader());
            assertTrue("SomeAction<1> instance found after module installation",
                existsSomeAction(c1));
            twiddle(m2, TWIDDLE_RELOAD);
            ClassLoader l2 = m2.getClassLoader();
            assertTrue("ClassLoader really changed", l1 != l2);
            Class c2 = l2.loadClass("test2.SomeAction");
            assertTrue("Class really changed", c1 != c2);
            // Make sure the changes take effect?
            Thread.sleep(2000);
            assertTrue("SomeAction<1> instance not found after module reload",
                !existsSomeAction(c1));
            assertTrue("SomeAction<2> instance found after module reload",
                existsSomeAction(c2));
        } finally {
            twiddle(m2, TWIDDLE_DISABLE);
        }
    }
    
}
