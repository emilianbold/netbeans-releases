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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.core.netigso;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.ArchiveResources;
import org.netbeans.Module;
import org.netbeans.NetigsoFramework;
import org.netbeans.ProxyClassLoader;
import org.netbeans.Stamps;
import org.netbeans.core.startup.Main;
import org.openide.modules.ModuleInfo;
import org.openide.modules.Places;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.service.packageadmin.RequiredBundle;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@ServiceProviders({
    @ServiceProvider(service = NetigsoFramework.class),
    @ServiceProvider(service = Netigso.class)
})
public final class Netigso extends NetigsoFramework implements Stamps.Updater {
    static final Logger LOG = Logger.getLogger(Netigso.class.getName());
    private static final ThreadLocal<Boolean> SELF_QUERY = new ThreadLocal<Boolean>();
    private static final String[] EMPTY = {};

    private Framework framework;
    private ClassLoader frameworkLoader;
    private NetigsoActivator activator;

    Framework getFramework() {
        return framework;
    }
    @Override
    protected ClassLoader findFrameworkClassLoader() {
        ClassLoader l = frameworkLoader;
        if (l != null) {
            return l;
        }
        Framework f = framework;
        if (f != null) {
            return frameworkLoader = f.getClass().getClassLoader();
        }
        return getClass().getClassLoader();
    }

    @Override
    protected void prepare(Lookup lkp, Collection<? extends Module> preregister) {
        if (framework == null) {
            readBundles();
            
            Map<String, Object> configMap = new HashMap<String, Object>();
            final String cache = getNetigsoCache().getPath();
            configMap.put(Constants.FRAMEWORK_STORAGE, cache);
            activator = new NetigsoActivator();
            configMap.put("netigso.archive", NetigsoArchiveFactory.DEFAULT.create(this)); // NOI18N
            configMap.put("felix.log.level", "4"); // NOI18N
            configMap.put("felix.bootdelegation.classloaders", activator); // NOI18N
            FrameworkFactory frameworkFactory = lkp.lookup(FrameworkFactory.class);
            if (frameworkFactory == null) {
                throw new IllegalStateException(
                        "Cannot find OSGi framework implementation." + // NOI18N
                        " Is org.netbeans.libs.felix module or similar enabled?" // NOI18N
                        );
            }
            framework = frameworkFactory.newFramework(configMap);
            try {
                framework.init();
            } catch (BundleException ex) {
                LOG.log(Level.SEVERE, "Cannot start OSGi framework", ex); // NOI18N
            }
            NetigsoServices ns = new NetigsoServices(this, framework);
            LOG.finer("OSGi Container initialized"); // NOI18N
        }
        activator.register(preregister);
        for (Module mi : preregister) {
            try {
                fakeOneModule(mi, null);
            } catch (IOException ex) {
                LOG.log(Level.WARNING, "Cannot fake " + mi.getCodeName(), ex);
            }
        }
    }

    @Override
    protected Set<String> start(Collection<? extends Module> allModules) {
        return toActivate(framework, allModules);
    }

    @Override
    protected void start() {
        try {
            framework.start();
        } catch (BundleException ex) {
            LOG.log(Level.WARNING, "Cannot start Container" + framework, ex);
        }
    }

    private static Set<String> toActivate(Framework f, Collection<? extends Module> allModules) {
        ServiceReference sr = f.getBundleContext().getServiceReference("org.osgi.service.packageadmin.PackageAdmin"); // NOI18N
        if (sr == null) {
            return null;
        }
        PackageAdmin pkgAdm = (PackageAdmin)f.getBundleContext().getService(sr);
        if (pkgAdm == null) {
            return null;
        }
        Set<String> allCnbs = new HashSet<String>(allModules.size() * 2);
        for (ModuleInfo m : allModules) {
            allCnbs.add(m.getCodeNameBase());
        }
        
        Set<String> needEnablement = new HashSet<String>();
        for (Bundle b : f.getBundleContext().getBundles()) {
            String loc = b.getLocation();
            if (loc.startsWith("netigso://")) {
                loc = loc.substring("netigso://".length());
            } else {
                continue;
            }
            RequiredBundle[] arr = pkgAdm.getRequiredBundles(loc);
            if (arr != null) for (RequiredBundle rb : arr) {
                for (Bundle n : rb.getRequiringBundles()) {
                    if (allCnbs.contains(n.getSymbolicName().replace('-', '_'))) {
                        needEnablement.add(loc);
                    }
                }
            }
        }
        return needEnablement;
    }

    @Override
    protected void shutdown() {
        try {
            framework.stop();
            framework.waitForStop(10000);
            framework = null;
            frameworkLoader = null;
        } catch (InterruptedException ex) {
            LOG.log(Level.WARNING, "Wait for shutdown failed" + framework, ex);
        } catch (BundleException ex) {
            LOG.log(Level.WARNING, "Cannot start Container" + framework, ex);
        }
    }

    @Override
    protected Set<String> createLoader(ModuleInfo m, ProxyClassLoader pcl, File jar) throws IOException {
        try {
            assert registered.containsKey(m.getCodeNameBase()) : m.getCodeNameBase();
            Bundle b = findBundle(m.getCodeNameBase());
            if (b == null) {
                for (Bundle bb : framework.getBundleContext().getBundles()) {
                    LOG.log(Level.FINE, "Bundle {0}: {1}", new Object[] { bb.getBundleId(), bb.getSymbolicName() });
                }
                throw new IOException("Not found bundle:" + m.getCodeNameBase());
            }
            ClassLoader l = new NetigsoLoader(b, m, jar);
            Set<String> pkgs = new HashSet<String>();
            String[] knownPkgs = registered.get(m.getCodeNameBase());
            if (knownPkgs == EMPTY) {
                try {
                    SELF_QUERY.set(true);
                    Enumeration en = b.findEntries("", null, true);
                    if (en == null) {
                        LOG.log(Level.INFO, "Bundle {0}: {1} is empty", new Object[] { b.getBundleId(), b.getSymbolicName() });
                    } else {
                        while (en.hasMoreElements()) {
                            URL url = (URL) en.nextElement();
                            if (url.getFile().startsWith("/META-INF")) {
                                pkgs.add(url.getFile().substring(9));
                                continue;
                            }
                            pkgs.add(url.getFile().substring(1).replaceFirst("/[^/]*$", "").replace('/', '.'));
                        }
                    }
                    Object exported = b.getHeaders("").get("Export-Package");
                    if (exported instanceof String) {
                        for (String p : exported.toString().split(",")) { // NOI18N
                            int semic = p.indexOf(';');
                            if (semic >= 0) {
                                p = p.substring(0, semic);
                            }
                            pkgs.add(p);
                        }
                    }
                } finally {
                    SELF_QUERY.set(false);
                }
                registered.put(m.getCodeNameBase(), pkgs.toArray(new String[0]));
                Stamps.getModulesJARs().scheduleSave(this, "netigso-bundles", false); // NOI18N
            } else {
                pkgs.addAll(Arrays.asList(knownPkgs));
            }
            pcl.append(new ClassLoader[]{ l });
            try {
                LOG.log(Level.FINE, "Starting bundle {0}", m.getCodeNameBase());
                b.start();
            } catch (BundleException possible) {
                if (isRealBundle(b)) {
                    throw possible;
                }
                LOG.log(Level.FINE, "Not starting fragment {0}", m.getCodeNameBase());
            }
            return pkgs;
        } catch (BundleException ex) {
            throw new IOException("Cannot start " + jar, ex);
        }
    }

    private static boolean isRealBundle(Bundle b) {
        return b.getHeaders("").get("Fragment-Host") == null; // NOI18N
    }

    @Override
    protected void stopLoader(ModuleInfo m, ClassLoader loader) {
        NetigsoLoader nl = (NetigsoLoader)loader;
        Bundle b = nl.bundle;
        try {
            assert b != null;
            try {
                LOG.log(Level.FINE, "Stopping bundle {0}", m.getCodeNameBase());
                b.stop();
            } catch (BundleException possible) {
                if (isRealBundle(b)) {
                    throw possible;
                }
                LOG.log(Level.FINE, "Not stopping fragment {0}", m.getCodeNameBase());
            }
        } catch (BundleException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    protected void reload(Module m) throws IOException {
        try {
            Bundle b = findBundle(m.getCodeNameBase());
            b.stop();
            fakeOneModule(m, b);
        } catch (BundleException ex) {
            throw new IOException(ex);
        }
    }

    //
    // take care about the registered bundles
    //
    private final Map<String,String[]> registered = new HashMap<String,String[]>();
    
    final void notifyBundleChange(final String symbolicName, final Version version, final int action) {
        Main.getModuleSystem().getManager().mutex().postReadRequest(new Runnable() {
            @Override
            public void run() {
                if (activator.isUnderOurControl(symbolicName)) {
                    return;
                }
                String type = "" + action;
                switch (action) {
                    case BundleEvent.INSTALLED: return; // no message for installed
                    case BundleEvent.RESOLVED: type = "resolved"; break;
                    case BundleEvent.STARTED: type = "started"; break;
                    case BundleEvent.STOPPED: type = "stopped"; break;
                    case BundleEvent.UNINSTALLED: return; // nothing for uninstalled
                }
                Netigso.LOG.log(Level.INFO, "bundle {0}@{2} {1}", new Object[]{
                    symbolicName, type, version
                });
            }
        });
    }

    private File getNetigsoCache() throws IllegalStateException {
        // Explicitly specify the directory to use for caching bundles.
        return Places.getCacheSubdirectory("netigso");
    }

    private void deleteRec(File dir) {
        File[] arr = dir.listFiles();
        if (arr != null) {
            for (File f : arr) {
                deleteRec(f);
            }
        }
        dir.delete();
    }

    private void fakeOneModule(Module m, Bundle original) throws IOException {
        if (registered.get(m.getCodeNameBase()) != null && original == null) {
            return;
        }
        registered.put(m.getCodeNameBase(), EMPTY);
        Bundle b;
        try {
            String symbolicName = (String) m.getAttribute("Bundle-SymbolicName");
            if ("org.netbeans.core.osgi".equals(symbolicName)) { // NOI18N
                // Always ignore.
            } else if (symbolicName != null) { // NOI18N
                if (original != null) {
                    LOG.log(Level.FINE, "Updating bundle {0}", original.getLocation());
                    original.update();
                    b = original;
                } else {
                    BundleContext bc = framework.getBundleContext();
                    File jar = m.getJarFile();
                    String loc;
                    if (m.isReloadable()) {
                        loc = toURI(jar);
                    } else {
                        loc = "reference:" + toURI(jar); // NOI18N
                    }
                    LOG.log(Level.FINE, "Installing bundle {0}", loc);
                    b = bc.installBundle(loc);
                }
            } else {
                InputStream is = fakeBundle(m);
                if (is != null) {
                    if (original != null) {
                        original.update(is);
                        b = original;
                    } else {
                        b = framework.getBundleContext().installBundle(
                            "netigso://" + m.getCodeNameBase(), is
                        );
                    }
                    is.close();
                }
            }
            Stamps.getModulesJARs().scheduleSave(this, "netigso-bundles", false); // NOI18N
        } catch (BundleException ex) {
            throw new IOException(ex);
        }
    }
    
    private static String threeDotsWithMajor(String version, String withMajor) {
        int indx = withMajor.indexOf('/');
        int major = 0;
        if (indx > 0) {
            major = Integer.parseInt(withMajor.substring(indx + 1));
        }
        String[] segments = (version + ".0.0.0").split("\\.");
        assert segments.length >= 3 && segments[0].length() > 0;

        return (Integer.parseInt(segments[0]) + major * 100) + "."  + segments[1] + "." + segments[2];
    }

    /** Creates a fake bundle definition that represents one NetBeans module
     *
     * @param m the module
     * @return the stream to read the definition from or null, if it does not
     *   make sense to represent this module as bundle
     */
    private static InputStream fakeBundle(ModuleInfo m) throws IOException {
        String exp = (String) m.getAttribute("OpenIDE-Module-Public-Packages"); // NOI18N
        if ("-".equals(exp)) { // NOI18N
            return null;
        }
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Manifest man = new Manifest();
        man.getMainAttributes().putValue("Manifest-Version", "1.0"); // workaround for JDK bug
        man.getMainAttributes().putValue("Bundle-ManifestVersion", "2"); // NOI18N
        man.getMainAttributes().putValue("Bundle-SymbolicName", m.getCodeNameBase()); // NOI18N

        if (m.getSpecificationVersion() != null) {
            String spec = threeDotsWithMajor(m.getSpecificationVersion().toString(), m.getCodeName());
            man.getMainAttributes().putValue("Bundle-Version", spec.toString()); // NOI18N
        }
        if (exp != null) {
            man.getMainAttributes().putValue("Export-Package", exp.replaceAll("\\.\\*", "")); // NOI18N
        } else {
            man.getMainAttributes().putValue("Export-Package", m.getCodeNameBase()); // NOI18N
        }
        JarOutputStream jos = new JarOutputStream(os, man);
        jos.close();
        return new ByteArrayInputStream(os.toByteArray());
    }

    private void readBundles() {
        assert registered.isEmpty();
        try {
            InputStream is = Stamps.getModulesJARs().asStream("netigso-bundles");
            if (is == null) {
                File f;
                try {
                    f = getNetigsoCache();
                } catch (IllegalStateException ex) {
                    return;
                }
                deleteRec(f);
                return;
            }
            Properties p = new Properties();
            p.load(is);
            is.close();
            for (Map.Entry<Object, Object> entry : p.entrySet()) {
                String k = (String)entry.getKey();
                String v = (String)entry.getValue();
                registered.put(k, v.split(","));
                LOG.log(Level.FINE, "readBundle: {0}", k);
            }
        } catch (IOException ex) {
            LOG.log(Level.WARNING, "Cannot read cache", ex);
        }
    }

    @Override
    public void flushCaches(DataOutputStream os) throws IOException {
        Properties p = new Properties();
        for (Map.Entry<String, String[]> entry : registered.entrySet()) {
            StringBuilder sb = new StringBuilder();
            String sep = "";
            for (String s : entry.getValue()) {
                sb.append(sep);
                sb.append(s);
                sep = ",";
            }
            p.setProperty(entry.getKey(), sb.toString());
        }

        p.store(os, null);
    }

    @Override
    public void cacheReady() {
    }

    private Bundle findBundle(String codeNameBase) {
        for (Bundle bb : framework.getBundleContext().getBundles()) {
            final String bbName = bb.getSymbolicName().replace('-', '_');
            if (bbName.equals(codeNameBase)) {
                return bb;
            }
        }
        return null;
    }

    public byte[] fromArchive(long bundleId, String resource, ArchiveResources ar) throws IOException {
        if (Boolean.TRUE.equals(SELF_QUERY.get())) {
            return ar.resource(resource);
        }
        return fromArchive(ar, resource);
    }

    private static String toURI(final File file) {
        class VFile extends File {

            public VFile() {
                super(file.getPath());
            }

            @Override
            public boolean isDirectory() {
                return false;
            }

            @Override
            public File getAbsoluteFile() {
                return this;
            }
        }
        return new VFile().toURI().toString();
    }

}
