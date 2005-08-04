/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.startup;

//import junit.framework.TestCase;
import org.netbeans.InvalidException;
import org.netbeans.Module;
import org.netbeans.ModuleInstaller;
import org.netbeans.ModuleManager;
import org.netbeans.junit.NbTestCase;
import java.io.*;
import java.util.*;
import java.beans.*;
import java.net.URL;
import java.util.jar.Manifest;
import org.openide.filesystems.*;
import org.openide.filesystems.FileSystem; // override java.io.FileSystem

/** Some infrastructure for module system tests.
 * @author Jesse Glick
 */
abstract class SetupHid extends NbTestCase {
    
    public SetupHid(String name) {
        super(name);
    }
    
    /** directory full of JAR files to test */
    protected File jars;
    
    protected void setUp() throws Exception {
        java.util.Locale.setDefault (java.util.Locale.US);
        jars = new File(ModuleManagerTest.class.getResource("jars").getFile());
    }
    
    protected static void deleteRec(File f) throws IOException {
        if (f.isDirectory()) {
            File[] kids = f.listFiles();
            if (kids == null) throw new IOException("Could not list: " + f);
            for (int i = 0; i < kids.length; i++) {
                deleteRec(kids[i]);
            }
        }
        if (! f.delete()) throw new IOException("Could not delete: " + f);
    }
    
    protected static void copyStreams(InputStream is, OutputStream os) throws IOException {
        byte[] buf = new byte[4096];
        try {
            int i;
            while ((i = is.read(buf)) != -1) {
                os.write(buf, 0, i);
            }
        } finally {
            is.close();
        }
    }
    
    protected static void copy(File a, File b) throws IOException {
        OutputStream os = new FileOutputStream(b);
        try {
            copyStreams(new FileInputStream(a), os);
        } finally {
            os.close();
        }
    }
    
    protected static void copy(File a, FileObject b) throws IOException {
        FileLock lock = b.lock();
        try {
            OutputStream os = b.getOutputStream(lock);
            try {
                copyStreams(new FileInputStream(a), os);
            } finally {
                os.close();
            }
        } finally {
            lock.releaseLock();
        }
    }
    
    protected static String slurp(String path) throws IOException {
        Main.getModuleSystem(); // #26451
        FileObject fo = Repository.getDefault().getDefaultFileSystem().findResource(path);
        if (fo == null) return null;
        InputStream is = fo.getInputStream();
        StringBuffer text = new StringBuffer((int)fo.getSize());
        byte[] buf = new byte[1024];
        int read;
        while ((read = is.read(buf)) != -1) {
            text.append(new String(buf, 0, read, "US-ASCII"));
        }
        return text.toString();
    }
    
    protected static class FakeModuleInstaller extends ModuleInstaller {
        // For examining results of what happened:
        public final List actions = new ArrayList(); // List<String>
        public final List args = new ArrayList(); // List<Object>
        public void clear() {
            actions.clear();
            args.clear();
        }
        // For adding invalid modules:
        public final Set delinquents = new HashSet(); // Set<Module>
        // For adding modules that don't want to close:
        public final Set wontclose = new HashSet(); // Set<Module>
        public void prepare(Module m) throws InvalidException {
            if (delinquents.contains(m)) throw new InvalidException(m, "not supposed to be installed");
            actions.add("prepare");
            args.add(m);
        }
        public void dispose(Module m) {
            actions.add("dispose");
            args.add(m);
        }
        public void load(List modules) {
            actions.add("load");
            args.add(new ArrayList(modules));
        }
        public void unload(List modules) {
            actions.add("unload");
            args.add(new ArrayList(modules));
        }
        public boolean closing(List modules) {
            actions.add("closing");
            args.add(new ArrayList(modules));
            Iterator it = modules.iterator();
            while (it.hasNext()) {
                if (wontclose.contains(it.next())) return false;
            }
            return true;
        }
        public void close(List modules) {
            actions.add("close");
            args.add(new ArrayList(modules));
        }
        protected Set packageOwners(String pkg) {
            return new HashSet () {
                public boolean contains (Object any) {
                    return true;
                }
            };
        }
    }
    
    protected static final class FakeEvents extends org.netbeans.Events {
        protected void logged(String message, Object[] args) {
            // do nothing
            // XXX is it better to test events or the installer??
        }
    }
    
    protected static final class LoggedPCListener implements PropertyChangeListener {
        private final Set changes = new HashSet(100); // Set<PropertyChangeEvent>
        public synchronized void propertyChange(PropertyChangeEvent evt) {
            changes.add(evt);
            notify();
        }
        public synchronized void waitForChanges() throws InterruptedException {
            wait(5000);
        }
        public synchronized boolean hasChange(Object source, String prop) {
            Iterator it = changes.iterator ();
            while (it.hasNext ()) {
                PropertyChangeEvent ev = (PropertyChangeEvent)it.next ();
                if (source == ev.getSource ()) {
                    if (prop.equals (ev.getPropertyName ())) {
                        return true;
                    }
                }
            }
            return false;
        }
        public synchronized boolean waitForChange(Object source, String prop) throws InterruptedException {
            while (! hasChange(source, prop)) {
                long start = System.currentTimeMillis();
                waitForChanges();
                if (System.currentTimeMillis() - start > 4000) {
                    //System.err.println("changes=" + changes);
                    return false;
                }
            }
            return true;
        }
    }
    
    protected static class LoggedFileListener implements FileChangeListener {
        /** names of files that have changed: */
        private final Set files = new HashSet(100); // Set<String>
        private synchronized void change(FileEvent ev) {
            files.add(ev.getFile().getPath());
            notify();
        }
        public synchronized void waitForChanges() throws InterruptedException {
            wait(5000);
        }
        public synchronized boolean hasChange(String fname) {
            return files.contains(fname);
        }
        public synchronized boolean waitForChange(String fname) throws InterruptedException {
            while (! hasChange(fname)) {
                long start = System.currentTimeMillis();
                waitForChanges();
                if (System.currentTimeMillis() - start > 4000) {
                    //System.err.println("changes=" + changes);
                    return false;
                }
            }
            return true;
        }
        public void fileDeleted(FileEvent fe) {
            change(fe);
        }
        public void fileFolderCreated(FileEvent fe) {
            change(fe);
        }
        public void fileDataCreated(FileEvent fe) {
            change(fe);
        }
        public void fileAttributeChanged(FileAttributeEvent fe) {
            // ignore?
        }
        public void fileRenamed(FileRenameEvent fe) {
            change(fe);
        }
        public void fileChanged(FileEvent fe) {
            change(fe);
        }
    }
    
}
