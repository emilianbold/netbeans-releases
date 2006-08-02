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

package org.openide.loaders;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingEnumeration;
import junit.textui.TestRunner;
import org.netbeans.junit.*;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;

/**
 *
 * @author  Jan Pokorsky
 */
public final class DefaultSettingsContextTest extends NbTestCase {

    private FileSystem lfs;
    private DataObject dobj;
    
    /** Creates a new instance of DefaultSettingsContextTest */
    public DefaultSettingsContextTest(String name) {
        super(name);
    }
    
    protected void setUp() throws java.lang.Exception {
        super.setUp();
        clearWorkDir();
        
        String fsstruct [] = new String [] {
            "AA/a.test"
        };
        
        TestUtilHid.destroyLocalFileSystem(getName());
        lfs = TestUtilHid.createLocalFileSystem(getWorkDir(), fsstruct);
        
        FileObject fo = lfs.findResource("AA/a.test");
        assertNotNull("file not found", fo);
        dobj = DataObject.find(fo);
        
    }
    
    public void testOperations() throws Exception {
        Context ctx = new DefaultSettingsContext(dobj);
        String val = "attrVal";
        String name = "attrName";
        
        // bind
        ctx.bind(name, val);
        assertEquals("lookup bound", val, ctx.lookup(name));
        assertEquals("lookupLink", val, ctx.lookupLink(name));
        
        NameAlreadyBoundException nabex = null;
        try {
            ctx.bind(name, val);
        } catch (NameAlreadyBoundException ex) {
            nabex = ex;
        }
        assertNotNull("bind with the same name", nabex);
        
        // rebind
        String newVal = "attrNewVal";
        String newName = "attrNewName";
        ctx.rebind(name, val);
        assertEquals("lookup orig", val, ctx.lookup(name));
        ctx.rebind(name, newVal);
        assertEquals("lookup rebound", newVal, ctx.lookup(name));
        ctx.rebind(name, val);
        assertEquals("lookup rebound", val, ctx.lookup(name));
        
        // rename
        ctx.rename(name, newName);
        assertEquals("lookup old name", null, ctx.lookup(name));
        assertEquals("lookup renamed", val, ctx.lookup(newName));
        
        ctx.bind(name, val);
        assertEquals("lookup bound", val, ctx.lookup(name));
        nabex = null;
        try {
            ctx.rename(newName, name);
        } catch (NameAlreadyBoundException ex) {
            nabex = ex;
        }
        assertNotNull("rename to existing name", nabex);
        
        // unbind
        ctx.unbind(newName);
        assertEquals("lookup unbound", null, ctx.lookup(newName));
    }
    
    public void testListing() throws Exception {
        Context ctx = new DefaultSettingsContext(dobj);
        String[] names = new String[] {"attrName1", "attrName2", "attrName3"};
        String[] vals = new String[] {"attrVal1", "attrVal2", "attrVal3"};
        
        NamingEnumeration en = ctx.listBindings(".");
        assertNotNull(en);
        assertTrue("listBindings is not empty", !en.hasMore());
        
        en = ctx.list(".");
        assertNotNull(en);
        assertTrue("list is not empty", !en.hasMore());
        
        for (int i = 0; i < names.length; i++) {
            ctx.bind(names[i], vals[i]);
        }
        
        en = ctx.listBindings(".");
        assertNotNull(en);
        java.util.List namesLst = java.util.Arrays.asList(names);
        java.util.Set binds = new java.util.HashSet();
        for (int i = 0; i < names.length; i++) {
            assertTrue("expected attr: " + names[i], en.hasMore());
            Binding bi = (Binding) en.next();
            int index = namesLst.indexOf(bi.getName());
            assertTrue("wrong name: " + bi.getName(), index >= 0);
            assertEquals("wrong value", vals[index], bi.getObject());
            binds.add(bi.getName());
        }
        assertEquals("wrong set of bindings: " + binds, names.length, binds.size());
        
        InvalidNameException iex = null;
        try {
            ctx.listBindings("../name");
        } catch (InvalidNameException ex) {
            iex = ex;
        }
        assertNotNull("name was not invalidated: ../name", iex);
        
    }
    
    public void testParse() throws Exception {
        DefaultSettingsContext ctx = new DefaultSettingsContext(dobj);
        
        javax.naming.Name n = ctx.parse("attrName");
        assertNotNull(n);
        assertEquals("size", 1, n.size());
        String name = getRelativeName(ctx, n);
        assertEquals("size", "attrName", name);
        
        n = ctx.parse("././attrName");
        assertNotNull(n);
        assertEquals("size", 3, n.size());
        name = getRelativeName(ctx, n);
        assertEquals("size", "attrName", name);
        
        n = ctx.parse("../attrName");
        assertNotNull(n);
        assertEquals("size", 2, n.size());
        InvalidNameException iex = null;
        try {
            name = getRelativeName(ctx, n);
        } catch (InvalidNameException ex) {
            iex = ex;
        }
        assertNotNull("name was not invalidated: ../attrName", iex);
    }
    
    public void testEnvironmentFind() {
        Context ctx = Environment.findSettingsContext(dobj);
        assertNotNull("missing default impl", ctx);
        assertEquals("unknown impl", DefaultSettingsContext.class, ctx.getClass());
    }
    
    private String getRelativeName(DefaultSettingsContext ctx, Name name) throws Exception {
        java.lang.reflect.Method m = ctx.getClass().getDeclaredMethod(
            "getRelativeName", new Class[] {Name.class});
        try {
            m.setAccessible(true);
            return (String) m.invoke(ctx, new Object[] {name});
        } catch (java.lang.reflect.InvocationTargetException ite) {
            throw (Exception) ite.getTargetException();
        }
    }
    
}
