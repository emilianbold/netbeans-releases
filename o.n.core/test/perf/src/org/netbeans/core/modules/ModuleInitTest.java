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

package org.netbeans.core.modules;

import java.io.*;
import java.lang.reflect.Method;
import java.util.*;
import java.util.zip.*;
import java.util.jar.*;

import org.netbeans.performance.Benchmark;
import org.openide.TopManager;

/**
 * Benchmark measuring initialization of the module system.
 * Covers parsing of module config XML files; opening JARs;
 * reading their manifests; computing dependencies; loading XML layers;
 * turning everything on.
 * @author Jesse Glick
 * @see #26786
 */
public class ModuleInitTest extends Benchmark {
    
    public static void main(String[] args) {
        simpleRun(ModuleInitTest.class);
    }
    
    public ModuleInitTest(String name) {
        super(name, new Integer[] {new Integer(10), new Integer(100), new Integer(1000)});
    }
    
    protected File dir;
    
    protected void setUp() throws Exception {
        int size = ((Integer)getArgument()).intValue();
        File tmp = File.createTempFile(getName(), ".tmp");
        tmp.deleteOnExit();
        dir = new File(tmp.getParentFile(), tmp.getName().substring(0, tmp.getName().length() - 4));
        dir.mkdirs();
        //System.out.println("Working dir: " + dir);
        File mods = new File(new File(dir, "home"), "modules");
        File amods = new File(mods, "autoload");
        amods.mkdirs();
        File emods = new File(mods, "eager");
        emods.mkdirs();
        createModules(size, mods, amods, emods);
        new File(new File(dir, "home"), "system").mkdirs();
        // Priming run to create system/Modules directory:
        runNB(dir, false);
    }
    protected void tearDown() throws Exception {
        deleteRec(dir);
    }
    private static void deleteRec(File x) throws IOException {
        File[] kids = x.listFiles();
        if (kids != null) {
            for (int i = 0; i < kids.length; i++) {
                deleteRec(kids[i]);
            }
        }
        if (!x.delete()) throw new IOException("Could not delete: " + x);
    }
    
    private void createModules(int size, File mods, File amods, File emods) throws IOException {
        // XXX be more sophisticated - this only creates dummy modules
        for (int i = 0; i < size; i++) {
            String num = Integer.toString(i);
            while (num.length() < 4) num = "0" + num;
            Manifest mani = new Manifest();
            Attributes attr = mani.getMainAttributes();
            attr.putValue("Manifest-Version", "1.0");
            attr.putValue("OpenIDE-Module", "module" + num);
            // Avoid requiring javahelp:
            attr.putValue("OpenIDE-Module-IDE-Dependencies", "IDE/1 > 2.2");
            attr.putValue("OpenIDE-Module-Specification-Version", "1.0");
            OutputStream os = new FileOutputStream(new File(mods, "module" + num + ".jar"));
            try {
                JarOutputStream jos = new JarOutputStream(os, mani);
                jos.close();
            } finally {
                os.close();
            }
        }
    }
    
    public void testInitModuleSystem() throws Exception {
        int count = getIterationCount();
        for (int i = 0; i < count; i++) {
            runNB(dir, true);
        }
    }
    
    private String cp = refinecp(System.getProperty("java.class.path"));
    //System.err.println("Classpath: " + cp);
    
    private void runNB(File dir, boolean log) throws IOException {
        String[] cmd = {
            "java",
            "-Dorg.openide.TopManager=org.netbeans.core.NonGuiMain",
            "-Dnetbeans.security.nocheck=true",
            "-Dnetbeans.home=" + new File(dir, "home").getAbsolutePath(),
            "-Dnetbeans.user=" + new File(dir, "user").getAbsolutePath(),
            "-Dmodules.dir=" + new File(new File(dir, "home"), "modules").getAbsolutePath(),
            //log ? "-Dorg.netbeans.log.startup=print" : "-Dignore=me",
            "-Dnetbeans.suppress.sysprop.warning=true",
            "-Dlog=" + log,
            "-classpath",
            cp,
            "org.netbeans.core.modules.ModuleInitTest$Main",
        };
        Process p = Runtime.getRuntime().exec(cmd);
        new Copier(p.getInputStream(), System.out).start();
        new Copier(p.getErrorStream(), System.err).start();
        try {
            int stat = p.waitFor();
            if (stat != 0) {
                throw new IOException("Command failed (status " + stat + "): " + Arrays.asList(cmd));
            }
        } catch (InterruptedException ie) {
            throw new IOException(ie.toString());
        }
    }
    
    /** Remove openide/test/perf/src/ if present since it overrides ErrorManager badly.
     */
    private static String refinecp(String cp) {
        StringBuffer b = new StringBuffer(cp.length());
        StringTokenizer t = new StringTokenizer(cp, File.pathSeparator);
        while (t.hasMoreTokens()) {
            File f = new File(t.nextToken());
            if (f.isDirectory()) {
                if (new File(new File(new File(f, "org"), "openide"), "ErrorManagerTest.java").exists() ||
                        new File(new File(new File(f, "org"), "openide"), "ErrorManagerTest.class").exists()) {
                    //System.err.println("Removing " + f + " from classpath");
                    continue;
                }
            }
            if (b.length() != 0) {
                b.append(File.pathSeparatorChar);
            }
            b.append(f);
        }
        return b.toString();
    }
    
    private static final class Copier extends Thread {
        private final InputStream is;
        private final PrintStream ps;
        public Copier(InputStream is, PrintStream ps) {
            this.is = is;
            this.ps = ps;
        }
        public void run() {
            try {
                byte[] b = new byte[4096];
                int i;
                while ((i = is.read(b)) != -1) {
                    ps.write(b, 0, i);
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
    
    public static final class Main {
        public static void main(String[] x) {
            TopManager.getDefault();
            Runtime r = Runtime.getRuntime();
            if (Boolean.getBoolean("log")) {
                System.out.println("Used memory: " + (r.totalMemory() - r.freeMemory()));
            }
        }
    }
    
    // Running NB >1 time in-VM does not work: cannot reset SecurityManager,
    // nor URLStreamHandlerFactory! Must spawn new VM.
            /*
            ClassLoader l = new ReloadNbLoader();
            Class c = Class.forName("org.openide.TopManager", true, l);
            Method m = c.getMethod("getDefault", new Class[] {});
            m.invoke(null, new Object[] {});
             */
    /*
    private static final class ReloadNbLoader extends ClassLoader {
        protected Class loadClass(String n, boolean r) throws ClassNotFoundException {
            Class c = findLoadedClass(n);
            if (c == null) {
                if (n.startsWith("org.netbeans.") || n.startsWith("org.openide.")) {
                    if (n.equals("org.openide.TopManager")) {
                        System.out.println("loading TopManager...");
                    }
                    c = findClass(n);
                } else {
                    c = getParent().loadClass(n);
                }
            }
            if (r) resolveClass(c);
            return c;
        }
        protected Class findClass(String n) throws ClassNotFoundException {
            InputStream is = getResourceAsStream(n.replace('.', '/') + ".class");
            if (is == null) {
                throw new ClassNotFoundException(n);
            }
            try {
                try {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream(100000);
                    byte[] buf = new byte[4096];
                    int i;
                    while ((i = is.read(buf)) != -1) {
                        baos.write(buf, 0, i);
                    }
                    byte[] data = baos.toByteArray();
                    // XXX protection domain?
                    i = n.lastIndexOf('.');
                    if (i != -1) {
                        String p = n.substring(0, i);
                        if (getPackage(p) == null) {
                            definePackage(p, null, null, null, null, null, null, null);
                        }
                    }
                    return defineClass(n, data, 0, data.length);
                } finally {
                    is.close();
                }
            } catch (IOException ioe) {
                throw new ClassNotFoundException(n, ioe);
            }
        }
    }
     */
    
}
