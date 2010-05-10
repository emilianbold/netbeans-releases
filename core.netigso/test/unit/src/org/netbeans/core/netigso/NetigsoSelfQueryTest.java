/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.core.netigso;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.jar.Manifest;
import org.netbeans.ArchiveResources;
import org.netbeans.Module;
import org.netbeans.ProxyClassLoader;
import org.netbeans.core.netigso.spi.BundleContent;
import org.netbeans.core.netigso.spi.NetigsoArchive;
import org.netbeans.junit.MockServices;
import org.openide.modules.Dependency;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Lookup;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.Version;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

public class NetigsoSelfQueryTest extends NetigsoHid {

    public NetigsoSelfQueryTest(String name) {
        super(name);
    }

    protected @Override void setUp() throws Exception {
        Locale.setDefault(Locale.US);
        clearWorkDir();
        System.setProperty("netbeans.user", getWorkDirPath());


        MockServices.setServices(MockFramework.class);
    }

    public void testSelfInspectionIsNotArchived() throws Exception {
        Netigso nf = Lookup.getDefault().lookup(Netigso.class);
        assertNotNull("Framework found", nf);

        class MI extends Module {

            public MI() throws IOException {
                super(null, null, null, false, false, false);
            }

            @Override
            public String getCodeNameBase() {
                return "org.test";
            }

            @Override
            public int getCodeNameRelease() {
                return 0;
            }

            @Override
            public String getCodeName() {
                return "org.test";
            }

            @Override
            public SpecificationVersion getSpecificationVersion() {
                return new SpecificationVersion("1.2");
            }

            @Override
            public boolean isEnabled() {
                return true;
            }

            @Override
            public Object getAttribute(String attr) {
                return null;
            }

            @Override
            public Object getLocalizedAttribute(String attr) {
                return null;
            }

            @Override
            public Set<Dependency> getDependencies() {
                return Collections.emptySet();
            }

            @Override
            public List<File> getAllJars() {
                return Collections.singletonList(getJarFile());
            }

            @Override
            public void setReloadable(boolean r) {
            }

            @Override
            public void reload() throws IOException {
            }

            @Override
            protected void classLoaderUp(Set<Module> parents) throws IOException {
            }

            @Override
            protected void classLoaderDown() {
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
                return new Manifest();
            }

        }
        MI mi = new MI();
        ProxyClassLoader pcl = new ProxyClassLoader(new ClassLoader[0], false);
        nf.prepare(Lookup.getDefault(), Collections.singleton(mi));
        Set<String> set = nf.createLoader(mi, pcl, jars);

        assertTrue("org.test.pkg: " + set, set.contains("org.test.pkg"));
    }

    public static final class MockFramework implements Framework, FrameworkFactory, BundleContext {
        private final List<MockBundle> bundles = new ArrayList<MockBundle>();
        NetigsoArchive archive;

        @Override
        public Framework newFramework(Map map) {
            archive = (NetigsoArchive) map.get("netigso.archive");
            assertNotNull("archive provided", archive);
            return this;
        }

        @Override
        public void init() throws BundleException {
        }

        @Override
        public FrameworkEvent waitForStop(long l) throws InterruptedException {
            return null;
        }

        @Override
        public void start() throws BundleException {
        }

        @Override
        public void start(int i) throws BundleException {
        }

        @Override
        public void stop() throws BundleException {
        }

        @Override
        public void stop(int i) throws BundleException {
        }

        @Override
        public void uninstall() throws BundleException {
        }

        @Override
        public void update() throws BundleException {
        }

        @Override
        public void update(InputStream in) throws BundleException {
        }

        @Override
        public long getBundleId() {
            return 0;
        }

        @Override
        public String getLocation() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getSymbolicName() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int getState() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Dictionary getHeaders() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public ServiceReference[] getRegisteredServices() {
            return new ServiceReference[0];
        }

        @Override
        public ServiceReference[] getServicesInUse() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean hasPermission(Object o) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public URL getResource(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Dictionary getHeaders(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Class loadClass(String string) throws ClassNotFoundException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Enumeration getResources(String string) throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Enumeration getEntryPaths(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public URL getEntry(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public long getLastModified() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Enumeration findEntries(String string, String string1, boolean bln) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public BundleContext getBundleContext() {
            return this;
        }

        @Override
        public Map getSignerCertificates(int i) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Version getVersion() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getProperty(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Bundle getBundle() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Bundle installBundle(String url, InputStream in) throws BundleException {
            final MockBundle b = new MockBundle(url, this);
            bundles.add(b);
            return b;
        }

        @Override
        public Bundle installBundle(String string) throws BundleException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Bundle getBundle(long l) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Bundle[] getBundles() {
            return bundles.toArray(new Bundle[0]);
        }

        @Override
        public void addServiceListener(ServiceListener sl, String string) throws InvalidSyntaxException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void addServiceListener(ServiceListener sl) {
        }

        @Override
        public void removeServiceListener(ServiceListener sl) {
        }

        @Override
        public void addBundleListener(BundleListener bl) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void removeBundleListener(BundleListener bl) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void addFrameworkListener(FrameworkListener fl) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void removeFrameworkListener(FrameworkListener fl) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public ServiceRegistration registerService(String[] strings, Object o, Dictionary dctnr) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public ServiceRegistration registerService(String string, Object o, Dictionary dctnr) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public ServiceReference[] getServiceReferences(String string, String string1) throws InvalidSyntaxException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public ServiceReference[] getAllServiceReferences(String string, String string1) throws InvalidSyntaxException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public ServiceReference getServiceReference(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Object getService(ServiceReference sr) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean ungetService(ServiceReference sr) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public File getDataFile(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Filter createFilter(String string) throws InvalidSyntaxException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }

    private static final class MockBundle implements Bundle, BundleContent {
        private final String url;
        private final MockFramework f;
        private final NetigsoArchive archive;

        public MockBundle(String url, MockFramework f) {
            this.url = url;
            this.f = f;
            this.archive = f.archive.forBundle(10, this);
        }



        @Override
        public int getState() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void start(int i) throws BundleException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void start() throws BundleException {
        }

        @Override
        public void stop(int i) throws BundleException {
        }

        @Override
        public void stop() throws BundleException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void update(InputStream in) throws BundleException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void update() throws BundleException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void uninstall() throws BundleException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Dictionary getHeaders() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public long getBundleId() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getLocation() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public ServiceReference[] getRegisteredServices() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public ServiceReference[] getServicesInUse() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean hasPermission(Object o) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public URL getResource(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Dictionary getHeaders(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getSymbolicName() {
            return "org.test";
        }

        @Override
        public Class loadClass(String string) throws ClassNotFoundException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Enumeration getResources(String string) throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Enumeration getEntryPaths(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public URL getEntry(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public long getLastModified() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Enumeration findEntries(String string, String string1, boolean bln) {
            Set<URL> set = new HashSet<URL>();
            try {
                if (archive.fromArchive("org/test/pkg/MyClass.class") != null) {
                    set.add(new URL("file:/org/test/pkg/MyClass.class"));
                }
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
            return Collections.enumeration(set);
        }

        @Override
        public byte[] resource(String name) throws IOException {
            for (StackTraceElement e : new Exception().getStackTrace()) {
                if (e.getClassName().equals("org.netbeans.Archive")) {
                    fail("Cannot be called from archive!");
                }
            }

            if (name.equals("org/test/pkg/MyClass.class")) {
                return new byte[1];
            }
            return null;
        }

        @Override
        public BundleContext getBundleContext() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Map getSignerCertificates(int i) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Version getVersion() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }
}
