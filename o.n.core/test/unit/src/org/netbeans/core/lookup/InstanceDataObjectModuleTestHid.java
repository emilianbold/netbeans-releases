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

package org.netbeans.core.lookup;

import junit.framework.AssertionFailedError;
import org.netbeans.junit.*;
import junit.textui.TestRunner;

import java.io.File;
import org.netbeans.core.modules.Module;
import org.netbeans.core.modules.ModuleManager;
import org.netbeans.core.NbTopManager;
import org.netbeans.core.modules.ModuleHistory;
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
import java.io.IOException;

/** Test InstanceDataObject's behavior in conjunction with module
 * installation and uninstallation.
 * Run each test in its own VM, since otherwise tests can affect
 * their siblings (static vars are evil!).
 * @author Jesse Glick
 * @see issue #16327
 */
public abstract class InstanceDataObjectModuleTestHid extends NbTestCase {
    
    protected InstanceDataObjectModuleTestHid(String name) {
        super(name);
    }
    
    private ModuleManager mgr;
    protected Module m1, m2;

    protected void runTest () throws Throwable {
        try {
            super.runTest();
        } catch (Error err) {
            AssertionFailedError newErr = new AssertionFailedError (err.getMessage () + "\n" + ErrManager.messages);
            newErr.initCause (err);
            throw newErr;
        }
    }
    
    
    protected void setUp() throws Exception {
        NbTopManager nb = NbTopManager.get ();
        nb.register (new ErrManager ());
        mgr = nb.getModuleSystem().getManager();
        final File jar1 = toFile (InstanceDataObjectModuleTestHid.class.getResource("data/test1.jar"));
        final File jar2 = toFile (InstanceDataObjectModuleTestHid.class.getResource("data/test2.jar"));
        try {
            mgr.mutex().writeAccess(new Mutex.ExceptionAction() {
                public Object run() throws Exception {
                    m1 = mgr.create(jar1, new ModuleHistory(jar1.getAbsolutePath()), false, false, false);
                    if (!m1.getProblems().isEmpty()) throw new IllegalStateException("m1 is uninstallable: " + m1.getProblems());
                    m2 = mgr.create(jar2, new ModuleHistory(jar2.getAbsolutePath()), false, false, false);
                    return null;
                }
            });
        } catch (MutexException me) {
            throw me.getException();
        }
        //System.err.println("loaded module: " + idomJar);
    }
    
    protected static File toFile (java.net.URL url) throws java.io.IOException {
        File f = new File (url.getPath ());
        if (f.exists ()) {
            return f;
        }
        
        String n = url.getPath ();
        int indx = n.lastIndexOf ('/');
        if (indx != -1) {
            n = n.substring (indx + 1);
        }
        n = n + url.getPath ().hashCode ();
        
        f = File.createTempFile (n, ".jar");
        java.io.FileOutputStream out = new java.io.FileOutputStream (f);
        org.openide.filesystems.FileUtil.copy (url.openStream (), out);
        out.close ();
        f.deleteOnExit ();
        
        return f;
    }
    
    protected void tearDown() throws Exception {
        try {
            mgr.mutex().writeAccess(new Mutex.ExceptionAction() {
                public Object run() throws Exception {
                    del(m1);
                    del(m2);
                    return null;
                }
                private void del(Module m) throws Exception {
                    if (m.isEnabled()) {
                        // Test presumably failed halfway.
                        if (m.isAutoload() || m.isEager() || m.isFixed()) {
                            // Tough luck, can't get rid of it easily.
                            return;
                        }
                        mgr.disable(m);
                    }
                    mgr.delete(m);
                }
            });
        } catch (MutexException me) {
            Exception e = me.getException();
            throw e/*new Exception(e + " [Messages:" + ErrManager.messages + "]", e)*/;
        } catch (RuntimeException e) {
            // Debugging for #52689:
            throw e/*new Exception(e + " [Messages:" + ErrManager.messages + "]", e)*/;
        }
        m1 = null;
        m2 = null;
        mgr = null;
    }
    
    protected static final int TWIDDLE_ENABLE = 0;
    protected static final int TWIDDLE_DISABLE = 1;
    protected static final int TWIDDLE_RELOAD = 2;
    protected void twiddle(final Module m, final int action) throws Exception {
        try {
            mgr.mutex().writeAccess(new Mutex.ExceptionAction() {
                public Object run() throws Exception {
                    switch (action) {
                    case TWIDDLE_ENABLE:
                        mgr.enable(m);
                        break;
                    case TWIDDLE_DISABLE:
                        mgr.disable(m);
                        break;
                    case TWIDDLE_RELOAD:
                        mgr.disable(m);
                        mgr.enable(m);
                        break;
                    default:
                        throw new IllegalArgumentException("bad action: " + action);
                    }
                    return null;
                }
            });
        } catch (MutexException me) {
            throw me.getException();
        }
    }
    
    protected boolean existsSomeAction(Class c) {
        return existsSomeAction(c, Lookup.getDefault().lookup(new Lookup.Template(c)));
    }
    
    protected boolean existsSomeAction(Class c, Lookup.Result r) {
        assertTrue(Action.class.isAssignableFrom(c));
        boolean found = false;
        Iterator it = r.allInstances().iterator();
        while (it.hasNext()) {
            Action a = (Action)it.next();
            assertTrue("Assignable to template class: c=" + c.getName() + " a.class=" + a.getClass().getName(),
                c.isInstance(a));
            if ("SomeAction".equals(a.getValue(Action.NAME))) {
                found = true;
                break;
            }
        }
        return found;
    }
    
    protected DataObject findIt(String name) throws Exception {
        FileObject fo = Repository.getDefault().getDefaultFileSystem().findResource(name);
        assertNotNull("Found " + name, fo);
        return DataObject.find(fo);
    }

    protected static void assertSameDataObject (String msg, DataObject obj1, DataObject obj2) {
        assertNotNull (msg, obj1);
        assertNotNull (msg, obj2);
        assertEquals ("They have the same primary file: " + msg, obj1.getPrimaryFile (), obj2.getPrimaryFile ());
        assertSame (msg, obj1, obj2);
    }
    
    protected static final class LookupL implements LookupListener {
        public int count = 0;
        public synchronized void resultChanged(LookupEvent ev) {
            count++;
            notifyAll();
        }
        public synchronized boolean gotSomething() throws InterruptedException {
            if (count > 0) return true;
            wait(3000);
            return count > 0;
        }
    }
    
    protected static void deleteRec(File f, boolean thistoo) throws IOException {
        if (f.isDirectory()) {
            File[] kids = f.listFiles();
            if (kids == null) throw new IOException("Could not list: " + f);
            for (int i = 0; i < kids.length; i++) {
                deleteRec(kids[i], true);
            }
        }
        if (thistoo && !f.delete()) throw new IOException("Could not delete: " + f);
    }
    
    private static final class ErrManager extends org.openide.ErrorManager {
        public static final StringBuffer messages = new StringBuffer ();
        
        private String prefix;
        
        public ErrManager () {
            this ("");
        }
        private ErrManager (String s) {
            prefix = s;
        }
        
        public Throwable annotate (Throwable t, int severity, String message, String localizedMessage, Throwable stackTrace, java.util.Date date) {
            return t;
        }
        
        public Throwable attachAnnotations (Throwable t, org.openide.ErrorManager.Annotation[] arr) {
            return t;
        }
        
        public org.openide.ErrorManager.Annotation[] findAnnotations (Throwable t) {
            return null;
        }
        
        public org.openide.ErrorManager getInstance (String name) {
            return new ErrManager (prefix + name);
        }
        
        public void log (int severity, String s) {            
            messages.append ('[');
            messages.append (prefix);
            messages.append (']');
            messages.append (s);
            messages.append ('\n');
        }
        
        public void notify (int severity, Throwable t) {
            messages.append (t.getMessage ());
        }
        
    } 
    
}
