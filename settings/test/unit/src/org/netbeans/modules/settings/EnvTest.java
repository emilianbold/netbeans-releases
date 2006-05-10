/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.settings;

import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;

import org.openide.filesystems.FileSystem;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;

/**
 *
 * @author  Jan Pokorsky
 */
public class EnvTest extends NbTestCase {
    FileSystem fs;
    
    /** Creates a new instance of EnvTest */
    public EnvTest(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        
        Lookup.getDefault().lookup(ModuleInfo.class);
        fs = org.openide.filesystems.Repository.getDefault().getDefaultFileSystem();
    }
    
    public void testFindEntityRegistration() throws Exception {
        String provider = "xml/lookups/NetBeans_org_netbeans_modules_settings_xtest/DTD_XML_FooSetting_1_0.instance";
        FileObject fo = fs.findResource(provider);
        assertNotNull("provider registration not found: " + provider, fo);
        assertNotNull("entity registration not found for " + provider, Env.findEntityRegistration(fo));
    }
    
    public void testFindProvider() throws Exception {
        Class clazz = org.netbeans.modules.settings.convertors.FooSetting.class;
        FileObject fo = Env.findProvider(clazz);
        assertNotNull("xml/memory registration not found: " + clazz.getName(), fo);
    }
    
    public void testFindProviderFromSuperClass() throws Exception {
        Class clazz = org.netbeans.modules.settings.convertors.Bar3Setting.class;
        FileObject fo = Env.findProvider(clazz);
        assertNotNull("xml/memory registration not found: " + clazz.getName(), fo);
    }
    
    public void testFindProviderFromSuperClass2() throws Exception {
        Class clazz = org.netbeans.modules.settings.convertors.Bar4Setting.class;
        FileObject fo = Env.findProvider(clazz);
        assertNull("xml/memory registration found: " + clazz.getName(), fo);
    }
}
