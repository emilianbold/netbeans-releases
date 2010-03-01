/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package org.netbeans.core.osgi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.core.startup.RunLevel;
import org.openide.modules.ModuleInstall;
import org.openide.util.Lookup;
import org.openide.util.NbCollections;
import org.openide.util.SharedClassObject;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.SynchronousBundleListener;
import org.osgi.framework.launch.Framework;
import org.osgi.service.url.AbstractURLStreamHandlerService;
import org.osgi.service.url.URLConstants;
import org.osgi.service.url.URLStreamHandlerService;

/**
 * Initializes critical NetBeans infrastructure inside an OSGi container.
 */
public class Activator implements BundleActivator, SynchronousBundleListener {

    private static final Logger LOG = Logger.getLogger(Activator.class.getName());

    public Activator() {}

    /** Bundles which have been loaded or are in line to be loaded. */
    private final DependencyQueue<String,Bundle> queue = new DependencyQueue<String,Bundle>();

    public @Override void start(final BundleContext context) throws Exception {
        if (System.getProperty("netbeans.home") != null) {
            throw new IllegalStateException("Should not be run from inside regular NetBeans module system");
        }
        String storage = context.getProperty(Constants.FRAMEWORK_STORAGE);
        if (storage != null) {
            System.setProperty("netbeans.user", storage);
        }
        System.setProperty("TopSecurityManager.disable", "true");
        OSGiMainLookup.initialize(context);
        if (((Framework) context.getBundle(0)).getState() == Bundle.STARTING) {
//            System.err.println("framework still starting");
            final AtomicReference<FrameworkListener> frameworkListener = new AtomicReference<FrameworkListener>();
            frameworkListener.set(new FrameworkListener() {
                public @Override void frameworkEvent(FrameworkEvent event) {
                    if (event.getType() == FrameworkEvent.STARTED) {
//                        System.err.println("framework started");
                        context.removeFrameworkListener(frameworkListener.get());
                        context.addBundleListener(Activator.this);
                        processLoadedBundles(context);
                    }
                }
            });
            context.addFrameworkListener(frameworkListener.get());
        } else {
            context.addBundleListener(this);
            processLoadedBundles(context);
        }
    }

    private void processLoadedBundles(BundleContext context) {
        List<Bundle> toLoad = new ArrayList<Bundle>();
        for (Bundle b : context.getBundles()) {
            switch (b.getState()) {
            case Bundle.ACTIVE:
                Dictionary<?,?> headers = b.getHeaders();
                toLoad.addAll(queue.offer(b, provides(headers), requires(headers), needs(headers)));
                break;
            }
        }
//        System.err.println("processing already loaded bundles: " + toLoad);
        load(toLoad);
    }

    public @Override void stop(BundleContext context) throws Exception {}

    public @Override void bundleChanged(BundleEvent event) {
        Bundle bundle = event.getBundle();
        switch (event.getType()) {
        case BundleEvent.STARTED:
//            System.err.println("started " + bundle.getSymbolicName());
            Dictionary<?,?> headers = bundle.getHeaders();
            load(queue.offer(bundle, provides(headers), requires(headers), needs(headers)));
            break;
        case BundleEvent.STOPPED:
//            System.err.println("stopped " + bundle.getSymbolicName());
            unload(queue.retract(bundle));
            break;
        }
    }

    static Set<String> provides(Dictionary<?,?> headers) {
        Set<String> deps = new TreeSet<String>(splitTokens((String) headers.get("OpenIDE-Module-Provides")));
        String name = (String) headers.get(Constants.BUNDLE_SYMBOLICNAME);
        if (name != null) {
            deps.add(name);
        }
        return deps;
    }

    static Set<String> requires(Dictionary<?,?> headers) {
        Set<String> deps = new TreeSet<String>();
        String v = (String) headers.get(Constants.REQUIRE_BUNDLE);
        if (v != null) {
            // PackageAdmin.getRequiredBundles is not suitable for this - it is backwards.
            // XXX try to follow the spec more closely; this will work at least for headers created by MakeOSGi:
            for (String item : v.split(", ")) {
                deps.add(item.replaceFirst(";.+", ""));
            }
        }
        // XXX also check for BUNDLE_SYMBOLICNAME_ATTRIBUTE in IMPORT_PACKAGE (though not currently used by MakeOSGi)
        for (String tok : splitTokens((String) headers.get("OpenIDE-Module-Requires"))) {
            // XXX at least ModuleFormat1/2 should probably be filtered out by MakeOSGi
            if (!tok.matches("org[.]openide[.]modules[.](ModuleFormat\\d+|os[.].+)")) {
                deps.add(tok);
            }
        }
        return deps;
    }

    static Set<String> needs(Dictionary<?,?> headers) {
        return splitTokens((String) headers.get("OpenIDE-Module-Needs"));
    }

    private static Set<String> splitTokens(String tokens) {
        if (tokens == null) {
            return Collections.emptySet();
        }
        Set<String> split = new TreeSet<String>(Arrays.asList(tokens.split("[, ]+")));
        split.remove("");
        return split;
    }

    private void load(List<Bundle> bundles) {
        OSGiMainLookup.bundlesAdded(bundles);
        boolean showWindowSystem = false;
        boolean loadServicesFolder = false;
        for (Bundle bundle : bundles) {
            registerUrlProtocolHandlers(bundle);
            if (bundle.getSymbolicName().equals("org.netbeans.core")) { // NOI18N
                loadServicesFolder = true;
            } else if (bundle.getSymbolicName().equals("org.netbeans.bootstrap")) { // NOI18N
                System.setProperty("netbeans.buildnumber", bundle.getVersion().getQualifier()); // NOI18N
            } else if (bundle.getSymbolicName().equals("org.netbeans.core.windows")) { // NOI18N
                showWindowSystem = true;
            }
        }
        OSGiRepository.DEFAULT.addLayers(layersFor(bundles));
        if (loadServicesFolder) {
            OSGiMainLookup.loadServicesFolder();
        }
        for (Bundle bundle : bundles) {
            ModuleInstall mi = installerFor(bundle);
            if (mi != null) {
                mi.restored();
            }
        }
        if (showWindowSystem) {
            // Trigger for showing main window and setting up related GUI elements.
            // - Main.initUICustomizations()
            // - add "org.netbeans.beaninfo" to Introspector.beanInfoSearchPath
            // - CoreBridge.getDefault().registerPropertyEditors()
            for (RunLevel rl : Lookup.getDefault().lookupAll(RunLevel.class)) {
                rl.run();
            }
        }
    }

    private void unload(List<Bundle> bundles) {
        for (Bundle bundle : bundles) {
            ModuleInstall mi = installerFor(bundle);
            if (mi != null) {
                mi.uninstalled();
            }
        }
        OSGiRepository.DEFAULT.removeLayers(layersFor(bundles));
        OSGiMainLookup.bundlesRemoved(bundles);
    }

    private static URL[] layersFor(List<Bundle> bundles) {
        List<URL> layers = new ArrayList<URL>(2);
        for (Bundle b : bundles) {
            if (b.getSymbolicName().equals("org.netbeans.modules.autoupdate.ui")) { // NOI18N
                // Won't work anyway, so don't even try.
                continue;
            }
            String explicit = (String) b.getHeaders().get("OpenIDE-Module-Layer");
            if (explicit != null) {
                layers.add(b.getResource(explicit));
                // XXX could also add localized/branded variants
            }
            URL generated = b.getResource("META-INF/generated-layer.xml");
            if (generated != null) {
                layers.add(generated);
            }
        }
        return layers.toArray(new URL[layers.size()]);
    }

    private static ModuleInstall installerFor(Bundle b) {
        if (b.getSymbolicName().equals("org.netbeans.modules.autoupdate.ui")) { // NOI18N
            // Won't work anyway, so don't even try.
            return null;
        }
        String respath = (String) b.getHeaders().get("OpenIDE-Module-Install");
        if (respath != null) {
            String fqn = respath.replaceFirst("[.]class$", "").replace('/', '.');
            try {
                return SharedClassObject.findObject(((Class<?>) b.loadClass(fqn)).asSubclass(ModuleInstall.class), true);
            } catch (Exception x) { // CNFE, CCE, ...
                LOG.log(Level.WARNING, "Could not load " + fqn, x);
                return null;
            }
        }
        return null;
    }

    private void registerUrlProtocolHandlers(final Bundle bundle) {
        Enumeration<?> e = bundle.getEntryPaths("META-INF/namedservices/URLStreamHandler/");
        if (e != null) {
            for (String path : NbCollections.iterable(NbCollections.checkedEnumerationByFilter(e, String.class, true))) {
                URL entry = bundle.getEntry(path + "java.net.URLStreamHandler");
                if (entry != null) {
                    String protocol = path.replaceAll("^META-INF/namedservices/URLStreamHandler/|/$", "");
                    try {
                        InputStream is = entry.openStream();
                        try {
                            BufferedReader r = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                            String line;
                            while ((line = r.readLine()) != null) {
                                if (!line.isEmpty() && !line.startsWith("#")) {
                                    final String fqn = line;
                                    Properties props = new Properties();
                                    props.put(URLConstants.URL_HANDLER_PROTOCOL, protocol);
                                    class Svc extends AbstractURLStreamHandlerService {
                                        public @Override URLConnection openConnection(final URL u) throws IOException {
                                            try {
                                                URLStreamHandler handler = (URLStreamHandler) bundle.loadClass(fqn).newInstance();
                                                Method openConnection = URLStreamHandler.class.getDeclaredMethod("openConnection", URL.class);
                                                openConnection.setAccessible(true);
                                                return (URLConnection) openConnection.invoke(handler, u);
                                            } catch (Exception x) {
                                                throw (IOException) new IOException(x.toString()).initCause(x);
                                            }
                                        }
                                    }
                                    BundleContext context = bundle.getBundleContext();
                                    if (context != null) {
                                        context.registerService(URLStreamHandlerService.class.getName(), new Svc(), props);
                                    } else {
                                        LOG.log(Level.WARNING, "no context for {0} in state {1}", new Object[] {bundle.getSymbolicName(), bundle.getState()});
                                    }
                                }
                            }
                        } finally {
                            is.close();
                        }
                    } catch (Exception x) {
                        LOG.log(Level.WARNING, "Could not load protocol handler for " + protocol + " from " + bundle.getSymbolicName(), x);
                    }
                }
            }
        }
    }

}
