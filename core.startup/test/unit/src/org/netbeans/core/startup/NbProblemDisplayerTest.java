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
package org.netbeans.core.startup;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.jar.Manifest;
import org.netbeans.InvalidException;
import org.netbeans.Module;
import org.netbeans.ModuleInstaller;
import org.netbeans.ModuleManager;
import org.netbeans.junit.NbTestCase;
import org.openide.modules.Dependency;

/**
 *
 * @author Jaroslav Tulach
 */
public class NbProblemDisplayerTest extends NbTestCase {
    
    public NbProblemDisplayerTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public void testSimpleDepOnJava() throws Exception {
        StringBuilder writeTo = new StringBuilder();
        Set<ProblemModule> modules = new HashSet<ProblemModule>();

        {
            Manifest mf = new Manifest();
            mf.getMainAttributes().putValue("OpenIDE-Module", "root.module");
            ProblemModule pm = new ProblemModule(mf);
            pm.addProblem(Dependency.create(Dependency.TYPE_JAVA, "Java > 1.30"));
            pm.addAttr("OpenIDE-Module-Name", "RootModule");
            modules.add(pm);
        }
        
        NbProblemDisplayer.problemMessagesForModules(writeTo, modules, true);

        String msg = writeTo.toString();
        if (msg.indexOf("RootModule") == -1) {
            fail("There should be noted the root module: " + msg);
        }
    }
    public void testFindTheRootCause() throws Exception {
        StringBuilder writeTo = new StringBuilder();
        Set<ProblemModule> modules = new HashSet<ProblemModule>();

        {
            Manifest mf = new Manifest();
            mf.getMainAttributes().putValue("OpenIDE-Module", "root.module");
            ProblemModule pm = new ProblemModule(mf);
            pm.addProblem(Dependency.create(Dependency.TYPE_JAVA, "Java > 1.30"));
            pm.addAttr("OpenIDE-Module-Name", "RootModule");
            modules.add(pm);
        }

        {
            Manifest mf = new Manifest();
            mf.getMainAttributes().putValue("OpenIDE-Module", "dep.module");
            ProblemModule pm = new ProblemModule(mf);
            pm.addProblem(Dependency.create(Dependency.TYPE_MODULE, "root.module"));
            pm.addAttr("OpenIDE-Module-Name", "DepModule");
            modules.add(pm);
        }
        
        NbProblemDisplayer.problemMessagesForModules(writeTo, modules, true);

        String msg = writeTo.toString();
        if (msg.indexOf("DepModule") >= 0) {
            fail("There should not be be name of dep.module: " + msg);
        }

        Locale.setDefault(Locale.US);

        if (msg.toUpperCase().indexOf("ANOTHER MODULE") == -1) {
            fail("There should be note about one missing module: " + msg);
        }
    }

    private static class ProblemModule extends Module {
        private static final Inst INST = new Inst();
        private static final ModuleManager MGR = new ModuleManager(INST, new NbEvents());

        private Map attrs = new HashMap();
        private Set<Dependency> problems = new HashSet<Dependency>();

        public ProblemModule(Manifest m) throws IOException {
            super(MGR, null, m, null, ProblemModule.class.getClassLoader());
            parseManifest();
        }

        public void addProblem(Set<Dependency> d) {
            problems.addAll(d);
        }

        public void addAttr(String key, String value) {
            attrs.put(key, value);
        }


        public List<File> getAllJars() {
            return Collections.emptyList();
        }

        public void setReloadable(boolean r) {
        }

        public void reload() throws IOException {
        }

        protected void classLoaderUp(Set<Module> parents) throws IOException {
        }

        protected void classLoaderDown() {
        }

        protected void cleanup() {
        }

        protected void destroy() {
        }

        public boolean isValid() {
            return true;
        }

        public boolean isFixed() {
            return false;
        }

        public Object getLocalizedAttribute(String attr) {
            return attrs.get(attr);
        }

        public Set getProblems() {
            return problems;
        }

    } // end of ProblemModule

    private static final class Inst extends ModuleInstaller {
        public void prepare(Module m) throws InvalidException {
        }

        public void dispose(Module m) {
        }

        public void load(List<Module> modules) {
        }

        public void unload(List<Module> modules) {
        }

        public boolean closing(List<Module> modules) {
            return true;
        }

        public void close(List<Module> modules) {
        }
    } // end of ModuleInstaller
}
