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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import javax.swing.SwingUtilities;
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

    private static JaveleonModuleReloader reloader = new JaveleonModuleReloader();

    static JaveleonModuleReloader getDefault() {
        return reloader;
    }

    /** This map ensures that the layer handling done by
     * NBInstaller.loadLayer()is consistent with the modules
     * registered with the currently installed layers.
     */
    private HashMap<String, Module> registeredModules = new HashMap<String, Module>();

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

    /**
     * Persisting all previously looked up
     * service instances from META-INF/services.
     * All new lookups will ask the new system class loader.
     */
    private void systemClassLoaderChangedForJaveleon(ClassLoader nue) {

        // We need to change the class loader of the main lookup instance.
        // For now there's no API to do this so use reflection.
        try {
            // get the default main loookup instance
            MainLookup mainLookup = (MainLookup) Lookup.getDefault();

            // obtain a reference to the current lookups and clone them
            Method getLookups = ProxyLookup.class.getDeclaredMethod("getLookups"); // NOI18N
            getLookups.setAccessible(true);
            Lookup[] newDelegates = ((Lookup[])getLookups.invoke(mainLookup)).clone();

            // now actually change the current class loader of the default main lookup
            Field mainLookupClassLoader = MainLookup.class.getDeclaredField("classLoader"); // NOI18N
            mainLookupClassLoader.setAccessible(true);
            mainLookupClassLoader.set(mainLookup, nue);

            // The MetaInfservices lookup instance is always placed at index 0.
            Lookup metaServicesLookup = newDelegates[0];

            // replace the class loader of the MetaInfservices instance in-place.
            Field field = metaServicesLookup.getClass().getDeclaredField("loader"); // NOI18N
            field.setAccessible(true);
            field.set(metaServicesLookup, nue);

            // now set the changed lookups
            newDelegates[1] = Lookups.singleton(nue);
            Method setLookups = ProxyLookup.class.getDeclaredMethod("setLookups", Lookup[].class); // NOI18N
            setLookups.setAccessible(true);
            setLookups.invoke(mainLookup, (Object)newDelegates);
        } catch (Exception x) {
            // shouldn't happen as long as the classLoader
            // and loader field names don't change.
            Exceptions.printStackTrace(x);
        }
    }

    private Map<Object, Object[]> retainOpenTopComponents(ClassLoader loader) {

            /*
            WindowManager manager = WindowManager.getDefault();
            HashMap<Mode, TopComponent[]> map = new HashMap<Mode, TopComponent[]>();
            for (Mode mode: WindowManager.getDefault().getModes())
            map.put(mode, manager.getOpenedTopComponents(mode));
            return map;
             */
         try {
            Class classWindowManager = loader.loadClass("org.openide.windows.WindowManager");
            Class classMode = loader.loadClass("org.openide.windows.Mode");
            Object manager = classWindowManager.getDeclaredMethod("getDefault").invoke(null);
            Set modes = (Set) classWindowManager.getDeclaredMethod("getModes").invoke(manager);
            HashMap<Object, Object[]> map = new HashMap<Object, Object[]>();
            for (Object mode : modes) {
                map.put(mode, (Object[]) classWindowManager.getDeclaredMethod("getOpenedTopComponents", classMode).invoke(manager, mode));
            }
            return map;
        } catch (Exception ex) {
            //Exceptions.printStackTrace(ex);
            return Collections.emptyMap();
        }
    }

    private void restoreOpenTopComponents(final ClassLoader loader, final Map<Object, Object[]> map) {
        /*
         for (Map.Entry<Mode, TopComponent[]> entry: map.entrySet())
            for (TopComponent topComponent: entry.getValue())
                topComponent.open();
         */

        if (map == null || map.isEmpty())
            return;

        // TopComponent.open must be called from the AWT thread
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    Class classTopComponent = loader.loadClass("org.openide.windows.TopComponent");
                    for (Map.Entry<Object, Object[]> entry : map.entrySet()) {
                        for (Object topComponent : entry.getValue()) {
                            classTopComponent.getDeclaredMethod("open").invoke(topComponent);
                        }
                    }
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }

            }
        });
    }

    private void refreshLayer(Module original, Module newModule, NbInstaller installer, ModuleManager mgr) {
        try {            
            boolean changed = layersChanged(original, newModule);
            Map<Object, Object[]> map = null;
            // Always refresh the layer. Exsitng instances created from the
            // layer will be retained and their identity preserved in the updated
            // module.

            if (changed) {
                map = retainOpenTopComponents(mgr.getClassLoader());
                Module registeredModule = getAndClearRegisteredModule(original);
                System.err.println("Change detected, unloading layer for " + original + " (but actually for " + registeredModule + ")");
                loadLayers(installer, registeredModule, false);
                //installer.unload(Collections.singletonList(registeredModule));
                installer.dispose(registeredModule);
            }
            mgr.replaceJaveleonModule(original, newModule);
            systemClassLoaderChangedForJaveleon(mgr.getClassLoader());
            if (changed) {
                System.err.println("Change detected, loading layer for " + newModule);
                installer.prepare(newModule);
                loadLayers(installer, newModule, true);
                //installer.load(Collections.singletonList(newModule));
                registerModule(newModule);
                
                restoreOpenTopComponents(mgr.getClassLoader(), map);
            }
            if (!changed && !(original instanceof JaveleonModule)) {
                // make sure to register the original module for later unloading
                // of the original module that installed the layer.
                registerModule(original);
            }
        }
        catch (InvalidException ex) {
            // shouldn't happen ever
        }
        catch (Throwable ex) {
            ex.printStackTrace(System.err);
        }
    }

    private void loadLayers(NbInstaller installer, Module m, boolean load) {
        try {
            Method loadLayers = NbInstaller.class.getDeclaredMethod("loadLayers", List.class, boolean.class);
            loadLayers.setAccessible(true);
            loadLayers.invoke(installer, Collections.singletonList(m), load);
        } catch (NoSuchMethodException ex) {
            Exceptions.printStackTrace(ex);
        } catch (SecurityException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalAccessException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalArgumentException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        }

    }

    private boolean layersChanged(Module m1, Module m2) {
        return ((CRC32Layer(m1) != CRC32Layer(m2)) || (CRC32GeneratedLayer(m1) != CRC32GeneratedLayer(m2)));
    }

    private long calculateChecksum(URL layer) {
        if (layer == null) {
            return -1;
        }
        try {
            CheckedInputStream cis = new CheckedInputStream(layer.openStream(), new CRC32());
            // Compute the CRC32 checksum

            byte[] buf = new byte[1024];
            while (cis.read(buf) >= 0) {
            }
            return cis.getChecksum().getValue();
        } catch (IOException e) {
            return -1;
        }
    }

    private long CRC32Layer(Module m) {
        String layerResource = m.getManifest().getMainAttributes().getValue("OpenIDE-Module-Layer"); // NOI18N
        String osgi = m.getManifest().getMainAttributes().getValue("Bundle-SymbolicName"); // NOI18N
        if (layerResource != null && osgi == null) {
            URL layer = m.getClassLoader().getResource(layerResource);
            return calculateChecksum(layer);
        }
        return -1;
    }

    private long CRC32GeneratedLayer(Module m) {
        String layerRessource = "META-INF/generated-layer.xml"; // NOI18N
        URL layer = m.getClassLoader().getResource(layerRessource);
        return calculateChecksum(layer);
    }

    private Module getAndClearRegisteredModule(Module original) {
        return (registeredModules.containsKey(original.getCodeNameBase())) ?
            registeredModules.remove(original.getCodeNameBase()) :
            original;
    }

    private void registerModule(Module newModule) {
        registeredModules.put(newModule.getCodeNameBase(), newModule);
    }

}
