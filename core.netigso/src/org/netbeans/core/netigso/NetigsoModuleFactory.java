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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.logging.Level;
import org.apache.felix.framework.Felix;
import org.netbeans.Events;
import org.netbeans.InvalidException;
import org.netbeans.Module;
import org.netbeans.ModuleFactory;
import org.netbeans.ModuleManager;
import org.netbeans.Stamps;
import org.openide.util.lookup.ServiceProvider;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@ServiceProvider(service=ModuleFactory.class)
public class NetigsoModuleFactory extends ModuleFactory
implements Stamps.Updater {
    private static NetigsoActivator activator;
    private static Felix felix;
    private static Set<String> registered;

    static void clear() {
        activator = null;
        felix = null;
        readBundles();
    }

    public NetigsoModuleFactory() {
        readBundles();
    }

    private static void readBundles() {
        registered = new HashSet<String>();
        try {
            InputStream is = Stamps.getModulesJARs().asStream("felix-bundles");
            if (is == null) {
                return;
            }
            BufferedReader r = new BufferedReader(new InputStreamReader(is, "UTF-8")); // NOI18N
            for (;;) {
                String line = r.readLine();
                if (line == null) {
                    break;
                }
                registered.add(line);
            }
        } catch (IOException ex) {
            NetigsoActivator.LOG.log(Level.WARNING, "Cannot read cache", ex);
        }
    }

    @Override
    public Module createFixed(Manifest mani, Object history, ClassLoader loader, boolean autoload, boolean eager, ModuleManager mgr, Events ev) throws InvalidException {
        Module m = super.createFixed(mani, history, loader, autoload, eager, mgr, ev);
        try {
            registerBundle(m);
        } catch (IOException ex) {
            throw (InvalidException)new InvalidException(m, ex.getMessage()).initCause(ex);
        }
        return m;
    }

    @Override
    public Module create(
        File jar, Object history,
        boolean reloadable, boolean autoload, boolean eager,
        ModuleManager mgr, Events ev
    ) throws IOException {
        try {
            Module m = super.create(jar, history, reloadable, autoload, eager, mgr, ev);
            registerBundle(m);
            return m;
        } catch (InvalidException ex) {
            Manifest mani = ex.getManifest();
            if (mani != null) {
                String name = mani.getMainAttributes().getValue("Bundle-SymbolicName"); // NOI18N
                if (name == null) {
                    throw ex;
                }
                return new NetigsoModule(jar, mgr, ev, history, reloadable, autoload, eager);
            }
            throw ex;
        }
    }

    synchronized static Felix getContainer() throws BundleException {
        if (activator == null) {
            Map<String,Object> configMap = new HashMap<String,Object>();
            // Configure the Felix instance to be embedded.
            //configMap.put(FelixConstants.EMBEDDED_EXECUTION_PROP, "true");
            // Add core OSGi packages to be exported from the class path
            // via the system bundle.
/*            configMap.put(Constants.FRAMEWORK_SYSTEMPACKAGES,
                "org.osgi.framework; version=1.4.0," +
                "org.osgi.service.packageadmin; version=1.2.0," +
                "org.osgi.service.startlevel; version=1.1.0," +
                "org.osgi.util.tracker; version=1.3.3,"+
                "org.osgi.service.url; version=1.0.0");
 */
            // Explicitly specify the directory to use for caching bundles.
            String ud = System.getProperty("netbeans.user");
            if (ud == null) {
                throw new IllegalStateException();
            }
            String cache = ud + File.separator + "var" + File.separator + "cache" + File.separator + "felix";
            configMap.put("felix.cache.profiledir", cache);
            configMap.put("felix.cache.dir", cache);
            configMap.put(Constants.FRAMEWORK_STORAGE, cache);
            activator = new NetigsoActivator();
            List<BundleActivator> activators = new ArrayList<BundleActivator>();
            activators.add(activator);
            configMap.put("felix.systembundle.activators", activators);
            felix = new Felix(configMap);
            felix.init();
            NetigsoActivator.LOG.finer("Felix initialized"); // NOI18N
        }
        return felix;
    }

    static void startContainer() throws BundleException {
        if (getContainer().getState() == Bundle.STARTING) {
            NetigsoActivator.LOG.finer("Felix start:"); // NOI18N
            getContainer().start();
            NetigsoActivator.LOG.finer("Felix started"); // NOI18N
        }
    }


    /** Creates a fake bundle definition that represents one NetBeans module
     *
     * @param m the module
     * @return the stream to read the definition from or null, if it does not
     *   make sense to represent this module as bundle
     */
    private static final InputStream fakeBundle(Module m) throws IOException {
        String exp = (String) m.getAttribute("OpenIDE-Module-Public-Packages"); // NOI18N
        if ("-".equals(exp)) { // NOI18N
            return null;
        }
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Manifest man = new Manifest();
        man.getMainAttributes().putValue("Manifest-Version", "1.0"); // workaround for JDK bug
        man.getMainAttributes().putValue("Bundle-ManifestVersion", "2"); // NOI18N
        man.getMainAttributes().putValue("Bundle-SymbolicName", m.getCodeName()); // NOI18N

        if (m.getSpecificationVersion() != null) {
            String spec = just3Dots(m.getSpecificationVersion().toString());
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

    private static String just3Dots(String version) {
        int first = version.indexOf('.');
        int second = first >= 0 ? version.indexOf('.', first + 1) : -1;
        int third = second >= 0 ? version.indexOf('.', second + 1) : -1;
        return (third >= 0) ? version.substring(0, third) : version;
    }

    private void registerBundle(Module m) throws IOException {
        if (!registered.add(m.getCodeName())) {
            return;
        }

        InputStream is = fakeBundle(m);
        if (is != null) {
            try {
                Bundle bundle;
                bundle = getContainer().getBundleContext().installBundle("netigso://" + m.getCodeNameBase(), is);
                activator.register(m);
                is.close();
            } catch (BundleException ex) {
                throw new IOException(ex.getMessage());
            }
            Stamps.getModulesJARs().scheduleSave(this, "felix-bundles", false); // NOI18N
        }
    }

    static Bundle findBundle(String cnb) {
        for (Bundle b : activator.getBundles()) {
            if (cnb.equals(b.getSymbolicName())) {
                return b;
            }
        }
        return null;
    }

    public void flushCaches(DataOutputStream os) throws IOException {
        Writer w = new OutputStreamWriter(os);
        for (String s : registered) {
            w.write(s);
            w.write('\n');
        }
        w.close();
    }

    public void cacheReady() {
    }
}
