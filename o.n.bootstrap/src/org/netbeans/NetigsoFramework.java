/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import org.openide.modules.ModuleInfo;
import org.openide.util.Enumerations;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/** This class contains abstracted calls to OSGi provided by core.netigso
 * module. No other module can implement this class, except core.netigso.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public abstract class NetigsoFramework {
    private ModuleManager mgr;
    
    protected NetigsoFramework() {
        if (!getClass().getName().equals("org.netbeans.core.netigso.Netigso")) { // NOI18N
            throw new IllegalStateException();
        }
    }

    /** Starts the framework.
     */
    protected abstract void prepare(
        Lookup loadFrameworkFrom,
        Collection<? extends Module> preregister
    );

    /** Get's ready for start of the OSGi framework.
     * @param allModules the modules that are in the system
     * @return returns set of additional modules (usually autoloads that need to be turned on)
     * @since 2.31 it returns set of module code name bases
     */
    protected abstract Set<String> start(Collection<? extends Module> allModules);
    
    /** Starts the OSGi framework by activating all bundles that shall be activated.
     * @since 2.35
     */
    protected abstract void start();

    /** Shutdowns the framework */
    protected abstract void shutdown();

    /** Initializes a classloader for given module.
     * @parma m the module description
     * @param pcl proxy classloader that shall be configured
     * @param jar the module JAR file
     * @return set of covered packages
     */
    protected abstract Set<String> createLoader(
        ModuleInfo m, ProxyClassLoader pcl, File jar
    ) throws IOException;

    /**
     * Find given resource inside provide module representing an OSGi bundle.
     * The search should happen without resolving the bundle, if possible. E.g.
     * by using <code>Bundle.getEntry</code>.
     * @param resName  name of the resource to seek for
     * @return empty enumeration or enumeration with one element.
     * @since 2.49
     */
    protected Enumeration<URL> findResources(Module module, String resName) {
        return Enumerations.empty();
    }

    
    /** Reloads one module
     * @since 2.27
     */
    protected abstract void reload(Module m) throws IOException;

    /** Deinitializes a classloader for given module */
    protected abstract void stopLoader(ModuleInfo m, ClassLoader loader);

    /** Allows the OSGi support to identify the classloader that loads
     * all OSGi framework classes.
     * 
     * @since 2.37
     */
    protected ClassLoader findFrameworkClassLoader() {
        return getClass().getClassLoader();
    }

    /** Default start level for all bundles that don't specify any own.
     * 
     * @since 2.44.2
     * @return 
     */
    protected int defaultStartLevel() {
        return 0;
    }
    
    //
    // Access to Archive
    //
    
    /** Get an array of bytes from archive. If not found, it remembers the
     * request and later calls {@link #toArchive(java.lang.String, java.lang.String)}
     * method to store it for next time.
     *
     * @param name name of the resource inside the JAR
     * @parma resources the provider of the real resources
     * @return either cached value or the one returned by resources (or null)
     * @throws IOException if something goes wrong
     * @since 2.29
     */
    protected final byte[] fromArchive(ArchiveResources resources, String name) throws IOException {
        return JarClassLoader.archive.getData(resources, name);
    }

    /** Creates a delegating loader for given module. The loader is suitable
     * for use as a delegating loader for a fake OSGi bundle.
     * @param cnb name of the bundle/module
     * @return classloader or null if the module does not exist
     * @since 2.50
     */
    protected final ClassLoader createClassLoader(String cnb) {
        Module m = findModule(cnb);
        return m == null ? null : new NetigsoLoader(m);
    }
    
    /** Finds module for given name.
     * @param cnb code name base of the module
     * @return the module or null
     * @since 2.50
     */
    protected final Module findModule(String cnb) {
        return mgr.get(cnb);
    }

    //
    // Implementation
    //

    private static NetigsoFramework framework;
    private static List<NetigsoModule> toInit = new ArrayList<NetigsoModule>();
    private static ArrayList<Module> toEnable = new ArrayList<Module>();

    static NetigsoFramework getDefault() {
        return framework;
    }

    static void willEnable(List<Module> newlyEnabling) {
        toEnable.addAll(newlyEnabling);
    }

    static Set<Module> turnOn(ModuleManager mgr, ClassLoader findNetigsoFrameworkIn, Collection<Module> allModules) throws InvalidException {
        boolean found = false;
        if (framework == null) {
            for (Module m : toEnable) {
                if (m instanceof NetigsoModule) {
                    found = true;
                    break;
                }
            }
        } else {
            found = true;
        }
        if (!found) {
            return Collections.emptySet();
        }
        final Lookup lkp = Lookups.metaInfServices(findNetigsoFrameworkIn);
        framework = lkp.lookup(NetigsoFramework.class);
        if (framework == null) {
            throw new IllegalStateException("No NetigsoFramework found, is org.netbeans.core.netigso module enabled?"); // NOI18N
        }
        framework.mgr = mgr;
        getDefault().prepare(lkp, allModules);
        toEnable.clear();
        toEnable.trimToSize();
        delayedInit(mgr);
        Set<String> cnbs = framework.start(allModules);
        if (cnbs == null) {
            return Collections.emptySet();
        }

        Set<Module> additional = new HashSet<Module>();
        for (Module m : allModules) {
            if (!m.isEnabled() && cnbs.contains(m.getCodeNameBase())) {
                additional.add(m);
            }
        }
        return additional;
    }

    private static boolean delayedInit(ModuleManager mgr) throws InvalidException {
        List<NetigsoModule> init;
        synchronized (NetigsoFramework.class) {
            init = toInit;
            toInit = null;
            if (init == null || init.isEmpty()) {
                return true;
            }
        }
        Set<NetigsoModule> problematic = new HashSet<NetigsoModule>();
        for (NetigsoModule nm : init) {
            try {
                nm.start();
            } catch (IOException ex) {
                nm.setEnabled(false);
                InvalidException invalid = new InvalidException(nm, ex.getMessage());
                nm.setProblem(invalid);
                problematic.add(nm);
            }
        }
        if (!problematic.isEmpty()) {
            mgr.getEvents().log(Events.FAILED_INSTALL_NEW, problematic);
        }
        
        return problematic.isEmpty();
    }

    static synchronized void classLoaderUp(NetigsoModule nm) throws IOException {
        if (toInit != null) {
            toInit.add(nm);
            return;
        }
        if (!toEnable.isEmpty()) {
            getDefault().prepare(Lookup.getDefault(), toEnable);
            toEnable.clear();
        }
        nm.start();
    }

    static synchronized void classLoaderDown(NetigsoModule nm) {
        if (toInit != null) {
            toInit.remove(nm);
            return;
        }
    }

    static void startFramework() {
        if (framework != null) {
            framework.start();
        }
    }


    /** Used on shutdown */
    static void shutdownFramework() {
        if (framework != null) {
            framework.shutdown();
        }
        framework = null;
        toInit = new ArrayList<NetigsoModule>();
        toEnable.clear();
    }
    
    static ClassLoader findFallbackLoader() {
        NetigsoFramework f = framework;
        if (f == null) {
            return null;
        }
        
        ClassLoader frameworkLoader = f.findFrameworkClassLoader();
        
        Class[] stack = TopSecurityManager.getStack();
        for (int i = 0; i < stack.length; i++) {
            ClassLoader sl = stack[i].getClassLoader();
            if (sl == null) {
                continue;
            }
            if (sl.getClass().getClassLoader() == frameworkLoader) {
                return stack[i].getClassLoader();
            }
        }
        return null;
    }
}
