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
// 0    - 0.92Mb, 2.75s
// 10   - 1.44Mb, 3.36s
// 100  - 1.71Mb, 4.5s
// 1000 - 7.21Mb, 9-12s

// another run (04-sep-02) with module dependencies:
// 0    - 0.92Mb, 2.77s
// 10   - 1.41Mb, 3.37s
// 100  - 1.69Mb, 4.3s
// 1000 - 7.72Mb, 10-12s
// So dependencies only seem to have a small startup time impact
// for a lot of modules (hundreds, a second or so).

// 7ms/module to read manifest & .xml file, plus .25ms/module to get file list from Modules/ (mostly file access?)
// .35ms/module to check dependency & orderings
// 2.4ms/module to open JAR & create classloader
// .2ms/m to look for nonexistent sections [already improving]
// .14ms/m to look for nonexistent module installs [how to improve?]

// after some tweaks (05-sep-02), measured inside NB w/ term:
// 0    - 0.87Mb, 3.0s
// 10   - 1.32Mb, 3.7s
// 100  - 1.73Mb, 4.6s
// 1000 - 6.29Mb, 11.3s
// measured outside from a shell:
// 0    - 1.15Mb, 2.9s
// 10   - 0.82Mb, 3.1s
// 100  - 1.66Mb, around 4s
// 1000 - 5.74Mb, 10.5s

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
    
    // Sorry, I can't easily draw this dependency graph.
    // It is complicated - that is the whole point.
    private static final int cyclesize = 8;
    // Base names of cyclic pattern of modules:
    private static final String[] names = {"aut1", "prv1", "reg1", "aut2", "reg2", "reg3", "dis1", "eag1"};
    // Types: 0 = regular, 1 = autoload, 2 = eager
    private static final int[] types = {1, 1, 0, 1, 0, 0, 0, 2};
    // Intra-set dependencies (list of indices of other modules):
    private static final int[][] intradeps = {{}, {}, {0}, {}, {0, 3}, {3}, {3}, {4, 5}};
    // Inter-set dependencies (list of indices of analogous modules in the previous cycle):
    private static final int[][] interdeps = {{0}, {}, {2}, {}, {}, {}, {6}, {5}};
    // Whether the module should be permitted to be enabled or not:
    private static final boolean[] enabled = {true, true, true, true, true, true, false, true};
    // Provided tokens from the module; freeform, but if ends in '#' that will get subst'd w/ cycle
    private static final String[][] provides = {{}, {"tok#"}, {}, {}, {}, {}, {}, {}};
    // Required tokens; same syntax as above.
    private static final String[][] requires = {{}, {}, {"tok#"}, {}, {}, {}, {}, {}};
    // E.g.: module #16 (0-indexed) is in the third cycle and is a reg1, thus named reg1_002.
    // It depends on module #14, aut1_002 (from intradeps) and module #9, reg1_001 (from interdeps).
    // It also depends on tok_002, provided only by module #15, prv1_002.
    // All of these will be enabled, along with some other modules (indirectly).
    static {
        if (names.length != cyclesize ||
                types.length != cyclesize ||
                intradeps.length != cyclesize ||
                interdeps.length != cyclesize ||
                enabled.length != cyclesize ||
                provides.length != cyclesize ||
                requires.length != cyclesize) {
            throw new Error();
        }
    }
    
    private void createModules(int size, File mods, File amods, File emods) throws IOException {
        // XXX be more sophisticated
        // - create layers - some overlap, some fresh files, some fresh top-level dirs, some empty, some w/ contents
        // - add random contents to JARs so they are bigger
        File[] moddirs = {mods, amods, emods}; // indexed by types[n]
        for (int i = 0; i < size; i++) {
            int which = i % cyclesize;
            int cycle = i / cyclesize;
            String cycleS = Integer.toString(cycle);
            // Enough for 1000*cyclesize modules to sort nicely:
            while (cycleS.length() < 3) cycleS = "0" + cycleS;
            Manifest mani = new Manifest();
            Attributes attr = mani.getMainAttributes();
            attr.putValue("Manifest-Version", "1.0");
            // XXX should create some package prefix to be more representative
            String name = names[which] + "_" + cycleS;
            attr.putValue("OpenIDE-Module", name + "/1");
            // Avoid requiring javahelp:
            attr.putValue("OpenIDE-Module-IDE-Dependencies", "IDE/1 > 2.2");
            attr.putValue("OpenIDE-Module-Specification-Version", "1.0");
            StringBuffer deps = null;
            for (int j = 0; j < intradeps[which].length; j++) {
                if (deps == null) {
                    deps = new StringBuffer(1000);
                } else {
                    deps.append(", ");
                }
                deps.append(names[intradeps[which][j]]);
                deps.append('_');
                deps.append(cycleS);
                deps.append("/1 > 1.0");
            }
            if (cycle > 0) {
                String oldCycleS = Integer.toString(cycle - 1);
                while (oldCycleS.length() < 3) oldCycleS = "0" + oldCycleS;
                for (int j = 0; j < interdeps[which].length; j++) {
                    if (deps == null) {
                        deps = new StringBuffer(1000);
                    } else {
                        deps.append(", ");
                    }
                    deps.append(names[interdeps[which][j]]);
                    deps.append('_');
                    deps.append(oldCycleS);
                    deps.append("/1 > 1.0");
                }
            }
            if (!enabled[which]) {
                if (deps == null) {
                    deps = new StringBuffer(1000);
                } else {
                    deps.append(", ");
                }
                // An impossible dependency.
                deps.append("honest.man.in.washington");
            }
            if (deps != null) {
                attr.putValue("OpenIDE-Module-Module-Dependencies", deps.toString());
            }
            if (provides[which].length > 0) {
                StringBuffer buf = new StringBuffer(100);
                for (int j = 0; j < provides[which].length; j++) {
                    if (j > 0) {
                        buf.append(", ");
                    }
                    String tok = provides[which][j];
                    if (tok.endsWith("#")) {
                        tok = tok.substring(0, tok.length() - 1) + "_" + cycleS;
                    }
                    buf.append(tok);
                }
                attr.putValue("OpenIDE-Module-Provides", buf.toString());
            }
            if (requires[which].length > 0) {
                StringBuffer buf = new StringBuffer(100);
                for (int j = 0; j < requires[which].length; j++) {
                    if (j > 0) {
                        buf.append(", ");
                    }
                    String tok = requires[which][j];
                    if (tok.endsWith("#")) {
                        tok = tok.substring(0, tok.length() - 1) + "_" + cycleS;
                    }
                    buf.append(tok);
                }
                attr.putValue("OpenIDE-Module-Requires", buf.toString());
            }
            OutputStream os = new FileOutputStream(new File(moddirs[types[which]], name + ".jar"));
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
            if (Boolean.getBoolean("log")) {
                // XXX should GC first to be more accurate!
                Runtime r = Runtime.getRuntime();
                double megs = (r.totalMemory() - r.freeMemory()) / 1024.0 / 1024.0;
                System.out.println("Used memory: " + new DecimalFormat("0.00 Mb").format(megs));
            }
        }
    }
    
}
