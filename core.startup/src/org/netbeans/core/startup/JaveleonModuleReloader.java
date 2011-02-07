/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.core.startup;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import org.netbeans.Events;
import org.netbeans.InvalidException;
import org.netbeans.JaveleonModule;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author Allan Gregersen
 */
class JaveleonModuleReloader {

    private static JaveleonModuleReloader  reloader = new JaveleonModuleReloader();

    static JaveleonModuleReloader getDefault() {
        return reloader;
    }

    // Use JaveleonModuleReloader.getDefault() to get the singleton instance
    private JaveleonModuleReloader() {
    }

    boolean reloadJaveleonModule(File jar, ModuleManager mgr, NbInstaller installer, Events ev) throws IOException {
        if(!JaveleonModule.isJaveleonPresent) return false;

        try {
            JaveleonModule.javeleonReloadMethod.invoke(null);
        } catch (Exception ex) {
            // oops, we shouldn't end up in here, since Javeleon was
            // supposed to be present given the above test succeeeded!
            // Oh well, just fall back to normal reload operation then
            return false;
        }
        System.err.println("Start Javeleon module update...");

        // the existing module if any
        Module m = null;
        // the new updated module
        Module tm = null;
        // Anything that needs to have class loaders refreshed
        List<Module> dependents;
        // First see if this refers to an existing module.
        for (Module module : mgr.getModules()) {
            if (module.getJarFile() != null) {
                if (jar.equals(module.getJarFile())) {
                    // Hah, found it.
                    m = module;
                    tm = mgr.createJaveleonModule(jar, new ModuleHistory(jar.getAbsolutePath()));
                    break;
                }
            }
        }
        if(m == null)
            return false;

        // now find dependent modules which need to be class loader migrated
        dependents = mgr.simulateJaveleonReload(m);

        // setup the class loader for the new Javeleon module
        // That's all we need to do to update the module with Javeleon!
        mgr.setupClassLoaderForJaveleonModule(tm);
        refreshLayer(m, tm, installer, mgr);

        // OK so far, then create new Javeleon modules for the
        // dependent modules and create new classloaders for
        // them as well
        for (Module m3 : dependents) {
            File moduleJar = m3.getJarFile();
            Module toRefresh = mgr.createJaveleonModule(moduleJar, new ModuleHistory(moduleJar.getAbsolutePath()));
            mgr.setupClassLoaderForJaveleonModule(toRefresh);
            refreshLayer(m3, toRefresh, installer, mgr);
        }
        // done...
        System.err.println("Javeleon finished module update...");
        ev.log(Events.FINISH_DEPLOY_TEST_MODULE, jar);
        return true;
    }

    private static void refreshLayer(Module original, Module newModule, NbInstaller installer, ModuleManager mgr) {
        try {
            // Always refresh the layer. Exsitng instances created from the
            // layer will be retained and their identity preserved in the updated
            // module.
            installer.unload(Collections.singletonList(original));
            installer.dispose(original);
            mgr.replaceJaveleonModule(original, newModule);
            systemClassLoaderChangedForJaveleon(mgr.getClassLoader());
            installer.prepare(newModule);
            installer.load(Collections.singletonList(newModule));
        } catch (InvalidException ex) {
            // shouldn't happen ever
        }
    }

    /**
     * Persisting all previously looked up
     * service instances from META-INF/services.
     * All new lookups will ask the new system class loader.
     */
    static void systemClassLoaderChangedForJaveleon(ClassLoader nue) {

        // We need to change the class loader of the main lookup instance.
        // For now there's no API to do this so use reflection.
        try {
            // get the default main loookup instance
            MainLookup l = (MainLookup) Lookup.getDefault();

            // obtain a reference to the current lookups and clone them
            Method getLookups = ProxyLookup.class.getDeclaredMethod("getLookups");
            getLookups.setAccessible(true);
            Lookup[] newDelegates = ((Lookup[])getLookups.invoke(l)).clone();

            // now actually change the current class loader of the default main lookup
            Field mainLookupClassLoader = MainLookup.class.getDeclaredField("classLoader");
            mainLookupClassLoader.setAccessible(true);
            mainLookupClassLoader.set(l, nue);

            // The MetaInfservices lookup instance is always placed at index 0.
            Lookup metaServicesLookup = newDelegates[0];

            // replace the class loader of the MetaInfservices instance in-place.
            Field field = metaServicesLookup.getClass().getDeclaredField("loader");
            field.setAccessible(true);
            field.set(metaServicesLookup, nue);

            // now set the changed lookups
            newDelegates[1] = Lookups.singleton(nue);
            Method setLookups = ProxyLookup.class.getDeclaredMethod("setLookups", Lookup[].class);
            setLookups.setAccessible(true);
            setLookups.invoke(l, (Object)newDelegates);
        } catch (Exception x) {
            // shouldn't happen as long as the classLoader
            // and loader field names don't change.
            Exceptions.printStackTrace(x);
        }
    }
}
