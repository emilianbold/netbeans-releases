/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.xml;

import junit.framework.*;
import java.io.IOException;
import java.beans.PropertyChangeListener;
import java.io.BufferedInputStream;
import org.openide.filesystems.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.w3c.dom.DocumentType;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.*;
import org.openide.cookies.InstanceCookie;
import org.openide.util.Lookup;
import org.openide.util.lookup.*;
import org.openide.xml.EntityCatalog;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileAttributeEvent;
import java.net.URL;
import org.openide.ErrorManager;

/** 
 *
 * @author Jaroslav Tulach
 */
public class FileEntityResolverDeadlock54971Test extends TestCase {
    public FileEntityResolverDeadlock54971Test(String testName) {
        super(testName);
    }
    
    static {
        System.setProperty("org.openide.util.Lookup", "org.netbeans.core.xml.FileEntityResolverDeadlock54971Test$Lkp");
    }
    
    protected void setUp() throws Exception {
        Object lookup = Lookup.getDefault().getClass ();
        assertEquals ("Our lookup registered", Lkp.class, lookup);
        
        // register Env as a handler for PublicIDs "-//NetBeans//Test//EN" which
        // is will contain the settings file we create
        FileObject root = Repository.getDefault().getDefaultFileSystem().getRoot();
        FileObject register = FileUtil.createData (root, "/xml/lookups/NetBeans/Test.instance");
        register.setAttribute("instanceCreate", Env.INSTANCE);
        assertTrue (register.getAttribute("instanceCreate") instanceof Environment.Provider);
        
        
        // prepare an object to ask him for cookie
        FileObject fo = createSettings (root, "x.settings");
        ComplexPair.obj = DataObject.find (fo);

        assertNotNull ("correct resolver is registered", Lookup.getDefault().lookup (Environment.Provider.class));
        assertEquals ("correct resolver is registered", FileEntityResolver.class, Lookup.getDefault().lookup (Environment.Provider.class).getClass ());
        
        Lookup l = org.openide.loaders.Environment.find (DataObject.find (fo.getParent()));
        assertNotNull ("Previous call is done just to initialize the Environment.getProviders()", l);
    }

    protected void tearDown() throws Exception {
    }
    
    
    
    public void testAskForEnvironmentFromLookup () throws Exception {
        // this is going to clear the cache in URLMapper
        Lkp.INSTANCE.add (new MyURLMapper ());

        Lkp.query = true;
        
        Lookup.Template t = new Lookup.Template (ComplexPair.class);
        Lookup.Result r = Lookup.getDefault ().lookup (t);
        
        java.util.Collection c = r.allInstances();
        if (c.size () != 1 || ComplexPair.IC != c.iterator().next()) {
            fail ("Wrong instances. Should be just " + ComplexPair.IC + " but was: " + c);
        }
        
        assertEquals ("One request for environment. If this fails" +
                " then the obj.getCookie in ComplexPair failed and was not " +
                " fully executed, wrong it should be", 1, Env.howManyTimesWeHandledRequestForEnvironmentOfOurObject);
    }
    
    private FileObject createSettings (FileObject root, String name) throws IOException {
        FileObject set = FileUtil.createData (root, name);

        FileLock lock = set.lock ();
        java.io.PrintStream os = new java.io.PrintStream (set.getOutputStream (lock));
        
        os.println ("<?xml version=\"1.0\"?>");
        os.println ("<!DOCTYPE settings PUBLIC \"-//NetBeans//Test//EN\" \"http://www.netbeans.org/dtds/sessionsettings-1_0.dtd\">");
        os.println ("<lkp version=\"1.0\">");
        os.println ("</lkp>");
        
        os.close ();
        lock.releaseLock();
        return set;
    }
    
    
    private static class ComplexPair extends org.openide.util.lookup.AbstractLookup.Pair {
        public static ComplexPair IC;
        public static DataObject obj;
        
        public ComplexPair () {
        }

        protected boolean instanceOf(Class c) {
            if (c == getClass ()) {
                if (IC == null) {
                    assertNull ("Just one instance allowed", IC);
                    IC = this;
                }
                assertEquals (IC, this);
                
                // this used to print strange warnings from FileEntityResolver
                Class ask = org.openide.cookies.InstanceCookie.class;
                Object ic = obj.getCookie (ask);
                assertNotNull ("Obj: " + obj + " Must have the InstanceCookie", ic);
            }
            return c.isAssignableFrom(getType ());
        }

        protected boolean creatorOf(Object obj) {
            return obj == this;
        }

        public String getDisplayName() {
            return getId ();
        }

        public String getId() {
            return getType ().getName();
        }

        public Object getInstance() {
            return this;
        }

        public Class getType() {
            return getClass ();
        }
    }

    
    private static final class Env 
    implements InstanceCookie, org.openide.loaders.Environment.Provider {
        public static int howManyTimesWeHandledRequestForEnvironmentOfOurObject;
        private static Env INSTANCE = new Env ();
        
        private Env () {
            assertNull (INSTANCE);
        }

        public String instanceName() {
            return getClass ().getName();
        }

        public Object instanceCreate() throws IOException, ClassNotFoundException {
            return this;
        }

        public Class instanceClass() throws IOException, ClassNotFoundException {
            return getClass ();
        }

        public Lookup getEnvironment(DataObject obj) {
            if (obj == ComplexPair.obj) {
                howManyTimesWeHandledRequestForEnvironmentOfOurObject++;
                return Lookups.singleton(this);
            } else {
                return null;
            }
        }
        
    }

    public static final class Lkp extends org.openide.util.lookup.AbstractLookup {
        public static org.openide.util.lookup.InstanceContent INSTANCE;
        public static boolean query;
        
        public Lkp () {
            this (new org.openide.util.lookup.InstanceContent ());
        }
        
        private Lkp (org.openide.util.lookup.InstanceContent ic) {
            super (ic);
            ic.addPair(new ComplexPair ());
            ic.add (new FileEntityResolver ());
            // add yourself to the lookup as cookie
            INSTANCE = ic;
            
            // add few instnaces to turn this into InheritanceTree storage lookup
            for (int i = 0; i < 20; i++) {
                Lkp.INSTANCE.add (new Integer (i));
            }

        }

        protected void beforeLookup(org.openide.util.Lookup.Template template) {
            if (template.getType() == URLMapper.class) {
                if (query) {
                    query = false;
                    // this will throw exception
                    lookup (MyURLMapper.class);
                    fail ("Never get here");
                }
            }
        }
        
        
    }
    
    public static final class MyURLMapper extends URLMapper {
        public URL getURL(FileObject fo, int type) {
            return null;
        }

        public FileObject[] getFileObjects(URL url) {
            return null;
        }
        
    }
    
}
