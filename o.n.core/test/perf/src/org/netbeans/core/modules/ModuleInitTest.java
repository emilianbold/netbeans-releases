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
import java.text.DecimalFormat;
import java.util.*;
import java.util.zip.*;
import java.util.jar.*;

import org.netbeans.performance.Benchmark;
import org.openide.TopManager;

// initial run (04-sep-02) with empty dummy modules under JDK 1.4.1rc:
// 0 -    0.92Mb, 2.75s
// 10 -   1.44Mb, 3.36s
// 100 -  1.71Mb, 4.5s
// 1000 - 7.21Mb, 9-12s

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
        super(name, new Integer[] {new Integer(0), new Integer(10), new Integer(100), new Integer(1000)});
    }
    
    private File topdir = new File(new File(System.getProperty("java.io.tmpdir")), "ModuleInitTest");
    private File homedir = new File(topdir, "home");
    private File skeldir = new File(topdir, "skeluser");
    private File userdir = new File(topdir, "user");
    private int lastSize = -1;
    
    protected void setUp() throws Exception {
        int size = ((Integer)getArgument()).intValue();
        if (size != lastSize) {
            if (homedir.exists()) {
                deleteRec(homedir);
            }
            File mods = new File(homedir, "modules");
            File amods = new File(mods, "autoload");
            amods.mkdirs();
            File emods = new File(mods, "eager");
            emods.mkdirs();
            createModules(size, mods, amods, emods);
            new File(homedir, "system").mkdirs();
            // Priming run to create system/Modules directory:
            if (skeldir.exists()) {
                deleteRec(skeldir);
            }
            runNB(homedir, skeldir, false);
            lastSize = size;
        }
        // On every run, copy the primed skeleton user dir to the real location,
        // then start NB with the copied user dir.
        if (userdir.exists()) {
            deleteRec(userdir);
        }
        copyRec(skeldir, userdir);
    }
    protected void tearDown() throws Exception {
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
    private static void copyStream(InputStream is, OutputStream os) throws IOException {
        try {
            byte[] b = new byte[4096];
            int i;
            while ((i = is.read(b)) != -1) {
                os.write(b, 0, i);
            }
        } finally {
            is.close();
        }
    }
    private static void copyRec(File x, File y) throws IOException {
        if (x.isDirectory()) {
            if (!y.mkdirs()) throw new IOException("Could not mkdir: " + y);
            String[] kids = x.list();
            if (kids == null) throw new IOException("Could not list: " + x);
            for (int i = 0; i < kids.length; i++) {
                copyRec(new File(x, kids[i]), new File(y, kids[i]));
            }
        } else {
            y.getParentFile().mkdirs();
            OutputStream os = new FileOutputStream(y);
            try {
                copyStream(new FileInputStream(x), os);
            } finally {
                os.close();
            }
        }
    }
    
    private void createModules(int size, File mods, File amods, File emods) throws IOException {
        // XXX be more sophisticated - this only creates dummy modules
        // 1. Add inter-module dependencies of varying depths
        // 2. Make some modules eager, some autoload, a few disabled
        // 3. Create layers - some overlap, some fresh files, some fresh top-level dirs, some empty, some w/ contents
        // 4. Add random contents to JARs so they are bigger
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
            runNB(homedir, userdir, true);
        }
    }
    
    private String cp = refinecp(System.getProperty("java.class.path"));
    //System.err.println("Classpath: " + cp);
    
    private void runNB(File homedir, File userdir, boolean log) throws IOException {
        String[] cmd = {
            "java",
            "-Dorg.openide.TopManager=org.netbeans.core.NonGuiMain",
            "-Dnetbeans.security.nocheck=true",
            "-Dnetbeans.home=" + homedir.getAbsolutePath(),
            "-Dnetbeans.user=" + userdir.getAbsolutePath(),
            //"-Dmodules.dir=" + new File(homedir, "modules").getAbsolutePath(),
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
                copyStream(is, ps);
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
                double megs = (r.totalMemory() - r.freeMemory()) / 1024.0 / 1024.0;
                System.out.println("Used memory: " + new DecimalFormat("0.00 Mb").format(megs));
            }
        }
    }
    
}
