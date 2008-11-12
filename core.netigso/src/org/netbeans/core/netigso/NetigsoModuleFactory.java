/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.core.netigso;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.Manifest;
import org.apache.felix.framework.Felix;
import org.apache.felix.framework.cache.BundleCache;
import org.apache.felix.framework.util.FelixConstants;
import org.netbeans.Events;
import org.netbeans.InvalidException;
import org.netbeans.Module;
import org.netbeans.ModuleFactory;
import org.netbeans.ModuleManager;
import org.netbeans.ProxyClassLoader;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Exceptions;
import org.openide.util.NbCollections;
import org.openide.util.lookup.ServiceProvider;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@ServiceProvider(service=ModuleFactory.class)
public class NetigsoModuleFactory extends ModuleFactory {
    private static NetigsoActivator activator;
    private static Felix felix;

    @Override
    public Module create(
        File jar, Object history,
        boolean reloadable, boolean autoload, boolean eager,
        ModuleManager mgr, Events ev
    ) throws IOException {
        try {
            return super.create(jar, history, reloadable, autoload, eager, mgr, ev);
        } catch (InvalidException ex) {
            Manifest mani = ex.getManifest();
            if (mani != null) {
                String name = mani.getMainAttributes().getValue("Bundle-SymbolicName"); // NOI18N
                if (name == null) {
                    throw ex;
                }
                return new BundleModule(jar, mgr, ev, history, reloadable, autoload, eager);
            }
            throw ex;
        }
    }

    private synchronized static Felix getContainer() throws BundleException {
        if (activator == null) {
            Map<String,String> configMap = new HashMap<String, String>();
            // Configure the Felix instance to be embedded.
            configMap.put(FelixConstants.EMBEDDED_EXECUTION_PROP, "true");
            // Add core OSGi packages to be exported from the class path
            // via the system bundle.
            configMap.put(Constants.FRAMEWORK_SYSTEMPACKAGES,
                "org.osgi.framework; version=1.3.0," +
                "org.osgi.service.packageadmin; version=1.2.0," +
                "org.osgi.service.startlevel; version=1.0.0," +
                "org.osgi.service.url; version=1.0.0");
            // Explicitly specify the directory to use for caching bundles.
            String ud = System.getProperty("netbeans.user");
            if (ud == null) {
                throw new IllegalStateException();
            }
            configMap.put(BundleCache.CACHE_PROFILE_DIR_PROP, ud + File.separator + "var" + File.separator + "cache" + File.separator + "felix");
            activator = new NetigsoActivator();
            List<BundleActivator> activators = new ArrayList<BundleActivator>();
            activators.add(activator);
            felix = new Felix(configMap, activators);
            felix.start();
        }
        return felix;
    }


    private static final class BundleModule extends Module {
        final Bundle bundle;
        private BundleLoader loader;
        private Manifest manifest;

        public BundleModule(File jar, ModuleManager mgr, Events ev, Object history, boolean reloadable, boolean autoload, boolean eager) throws IOException {
            super(mgr, ev, history, reloadable, autoload, eager);
            try {
                BundleContext bc = getContainer().getBundleContext();
                bundle = bc.installBundle(jar.toURI().toURL().toExternalForm());
                manifest = new Manifest();
            } catch (BundleException ex) {
                throw (IOException)new IOException(ex.getMessage()).initCause(ex);
            }
        }

        @Override
        public String[] getProvides() {
            return new String[0];
        }

        @Override
        public String getCodeName() {
            return bundle.getSymbolicName();
        }

        @Override
        public String getCodeNameBase() {
            return bundle.getSymbolicName();
        }

        @Override
        public int getCodeNameRelease() {
            String version = (String)bundle.getHeaders().get("Bundle-SymbolicName"); // NOI18N
            int slash = version.lastIndexOf('/');
            if (slash != -1) {
                return Integer.parseInt(version.substring(slash + 1));
            }
            return -1;
        }

        @Override
        public SpecificationVersion getSpecificationVersion() {
            String version = (String)bundle.getHeaders().get("Bundle-Version"); // NOI18N
            return new SpecificationVersion(version);
        }

        @Override
        protected void parseManifest() throws InvalidException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public List<File> getAllJars() {
            return Collections.emptyList();
        }

        @Override
        public void setReloadable(boolean r) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void reload() throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected void classLoaderUp(Set<Module> parents) throws IOException {
            if (bundle.getState() != Bundle.INSTALLED) {
                return;
            }
            try {
                bundle.start();
            } catch (BundleException ex) {
                throw (IOException)new IOException(ex.getMessage()).initCause(ex);
            }
            loader = new BundleLoader(bundle);
            assert bundle.getState() == Bundle.ACTIVE;
        }

        @Override
        protected void classLoaderDown() {
            assert bundle.getState() == Bundle.ACTIVE;
            try {
                bundle.stop();
            } catch (BundleException ex) {
                throw new IllegalStateException(ex);
            }
            loader = null;
        }

        @Override
        public ClassLoader getClassLoader() throws IllegalArgumentException {
            return loader;
        }

        @Override
        protected void cleanup() {
        }

        @Override
        protected void destroy() {
        }

        @Override
        public boolean isFixed() {
            return false;
        }

        @Override
        public Manifest getManifest() {
            return manifest;
        }

        @Override
        public Object getLocalizedAttribute(String attr) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String toString() {
            return "Netigso: " + getCodeName(); // NOI18N
        }
    } // end of BundleModule
    private static final class BundleLoader extends ProxyClassLoader {
        private final Bundle bundle;

        public BundleLoader(Bundle bundle) {
            super(new ClassLoader[0], true);
            this.bundle = bundle;
        }

        @Override
        public URL findResource(String name) {
            return bundle.getResource(name);
        }

        @Override
        public Enumeration<URL> findResources(String name) {
            try {
                return bundle.getResources(name);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                return null;
            }
        }

        @Override
        protected Class<?> doLoadClass(String pkg, String name) {
            try {
                return bundle.loadClass(name);
            } catch (ClassNotFoundException ex) {
                Exceptions.printStackTrace(ex);
                return null;
            }
        }
    }
}
