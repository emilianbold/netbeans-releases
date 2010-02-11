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
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.modules.ModuleInstall;
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
import org.osgi.service.url.AbstractURLStreamHandlerService;
import org.osgi.service.url.URLConstants;
import org.osgi.service.url.URLStreamHandlerService;

/**
 * Initializes critical NetBeans infrastructure inside an OSGi container.
 */
public class Activator implements BundleActivator, SynchronousBundleListener, FrameworkListener {

    private static final Logger LOG = Logger.getLogger(Activator.class.getName());

    public Activator() {}

    public @Override void start(BundleContext context) throws Exception {
        if (System.getProperty("netbeans.home") != null) {
            throw new IllegalStateException("Should not be run from inside regular NetBeans module system");
        }
        String storage = context.getProperty(Constants.FRAMEWORK_STORAGE);
        if (storage != null) {
            System.setProperty("netbeans.user", storage);
        }
        // XXX set netbeans.buildnumber to OpenIDE-Module-Implementation-Version of org.netbeans.bootstrap
        OSGiMainLookup.initialize(context);
        context.addFrameworkListener(this);
        context.addBundleListener(this);
//        System.err.println("processing already loaded bundles...");
        for (Bundle b : context.getBundles()) {
            switch (b.getState()) {
            case Bundle.ACTIVE:
                // XXX coalesce these layer events
                bundleChanged(new BundleEvent(BundleEvent.RESOLVED, b));
                // XXX should resolve all first, then start all:
                bundleChanged(new BundleEvent(BundleEvent.STARTED, b));
                break;
            case Bundle.RESOLVED:
            case Bundle.STARTING:
            case Bundle.STOPPING:
                bundleChanged(new BundleEvent(BundleEvent.RESOLVED, b));
                break;
            }
        }
//        System.err.println("done processing already loaded bundles.");
        // XXX if GUI mode: Main.initUICustomizations(); add "org.netbeans.beaninfo" to Introspector.beanInfoSearchPath; GuiRunLevel.run
    }

    public @Override void stop(BundleContext context) throws Exception {}

    public @Override void bundleChanged(BundleEvent event) {
        Bundle bundle = event.getBundle();
        switch (event.getType()) {
        case BundleEvent.RESOLVED:
//            System.err.println("resolved " + bundle.getSymbolicName());
            OSGiMainLookup.bundleResolved(bundle);
            OSGiRepository.DEFAULT.addLayers(layersFor(bundle));
            break;
        case BundleEvent.UNRESOLVED:
//            System.err.println("unresolved " + bundle.getSymbolicName());
            OSGiMainLookup.bundleUnresolved(bundle);
            OSGiRepository.DEFAULT.removeLayers(layersFor(bundle));
            break;
        case BundleEvent.STARTED:
//            System.err.println("started " + bundle.getSymbolicName());
            registerUrlProtocolHandlers(bundle); // must be active
            ModuleInstall mi = installerFor(bundle);
            if (mi != null) {
//                System.err.println("running " + mi.getClass().getName() + ".restored()");
                mi.restored();
            }
            // XXX if o.n.core (or o.n.m.settings?) tell OSGiMainLookup to use CoreBridge.getDefault().lookupCacheLoad()
            break;
        case BundleEvent.STOPPED:
//            System.err.println("stopped " + bundle.getSymbolicName());
            mi = installerFor(bundle);
            if (mi != null) {
//                System.err.println("running " + mi.getClass().getName() + ".uninstalled()");
                mi.uninstalled();
            }
            break;
        }
    }

    public @Override void frameworkEvent(FrameworkEvent event) {
//        System.err.println("XXX framework event " + event.getType() + " on " + event.getBundle().getSymbolicName());
        // XXX perhaps defer processing various things until the framework is started
    }

    private static URL[] layersFor(Bundle b) {
        List<URL> layers = new ArrayList<URL>(2);
        String explicit = (String) b.getHeaders().get("OpenIDE-Module-Layer");
        if (explicit != null) {
            layers.add(b.getResource(explicit));
            // XXX could also add localized/branded variants
        }
        URL generated = b.getResource("META-INF/generated-layer.xml");
        if (generated != null) {
            layers.add(generated);
        }
        return layers.toArray(new URL[layers.size()]);
    }

    private static ModuleInstall installerFor(Bundle b) {
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
        Enumeration e = bundle.getEntryPaths("META-INF/namedservices/URLStreamHandler/");
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
