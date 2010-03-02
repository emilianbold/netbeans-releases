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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.netbeans.core.startup.CoreBridge;
import org.openide.modules.Dependency;
import org.openide.modules.ModuleInfo;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.Version;

/**
 * Default lookup when running inside an OSGi container.
 */
public class OSGiMainLookup extends ProxyLookup {

    private static BundleContext context;

    private static OSGiMainLookup get() {
        return (OSGiMainLookup) Lookup.getDefault();
    }

    public static void initialize(BundleContext _context) throws Exception {
        System.setProperty(Lookup.class.getName(), OSGiMainLookup.class.getName());
        context = _context;
        OSGiMainLookup lkp;
        ClassLoader oldCCL = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(OSGiMainLookup.class.getClassLoader());
        try {
            lkp = get();
        } finally {
            Thread.currentThread().setContextClassLoader(oldCCL);
        }
        lkp.postInit();
    }

    static void bundlesAdded(List<Bundle> bundles) {
        // XXX extend existing classLoader
        get().setClassLoader();
        for (Bundle bundle : bundles) {
            get().moduleInfoContent.add(bundle, moduleInfoConvertor);
        }
    }

    static void bundlesRemoved(List<Bundle> bundles) {
        get().setClassLoader();
        for (Bundle bundle : bundles) {
            get().moduleInfoContent.remove(bundle, moduleInfoConvertor);
        }
    }

    static void loadServicesFolder() {
        get().nonClassLoaderDelegates.add(CoreBridge.getDefault().lookupCacheLoad());
        get().setDelegates();
    }

    private ClassLoader classLoader;
    private final List<Lookup> nonClassLoaderDelegates = new ArrayList<Lookup>();
    private final InstanceContent moduleInfoContent = new InstanceContent();
    private static final InstanceContent.Convertor<Bundle,ModuleInfo> moduleInfoConvertor = new InstanceContent.Convertor<Bundle, ModuleInfo>() {
        public @Override ModuleInfo convert(Bundle b) {
            return new BundleModuleInfo(b);
        }
        public @Override Class<? extends ModuleInfo> type(Bundle b) {
            return ModuleInfo.class;
        }
        public @Override String id(Bundle b) {
            return b.getSymbolicName();
        }
        public @Override String displayName(Bundle b) {
            return id(b);
        }
    };

    public OSGiMainLookup() {}

    private void postInit() {
        nonClassLoaderDelegates.add(Lookups.fixed(OSGiRepository.DEFAULT, new OSGiLifecycleManager(context), new OSGiInstalledFileLocator(context)));
        nonClassLoaderDelegates.add(new AbstractLookup(moduleInfoContent));
        // XXX InstalledFileLocator impl for OSGI-INF/files/*
        setClassLoader();
    }

    private void setClassLoader() {
        classLoader = new OSGiClassLoader(context);
        // XXX should it be set as thread CCL? would help some NB APIs, but might break OSGi conventions
        setDelegates();
    }

    private void setDelegates() {
        Lookup[] delegates = new Lookup[nonClassLoaderDelegates.size() + 2];
        nonClassLoaderDelegates.toArray(delegates);
        delegates[delegates.length - 2] = Lookups.metaInfServices(classLoader);
        delegates[delegates.length - 1] = Lookups.singleton(classLoader);
        setLookups(delegates);
    }

    private static final class BundleModuleInfo extends ModuleInfo {
        private final Bundle b;
        public BundleModuleInfo(Bundle b) {
            this.b = b;
        }
        public @Override String getCodeNameBase() {
            return b.getSymbolicName();
        }
        public @Override int getCodeNameRelease() {
            return b.getVersion().getMajor() / 100;
        }
        public @Override String getCodeName() {
            int r = getCodeNameRelease();
            String s = getCodeNameBase();
            return r > 0 ? s + "/" + r : s;
        }
        public @Override SpecificationVersion getSpecificationVersion() {
            Version v = b.getVersion();
            return new SpecificationVersion(v.getMajor() % 100 + "." + v.getMinor() + "." + v.getMicro());
        }
        public @Override boolean isEnabled() {
            switch (b.getState()) {
            case Bundle.RESOLVED:
            case Bundle.ACTIVE:
            case Bundle.STARTING:
            case Bundle.STOPPING:
                return true;
            default:
                return false;
            }
        }
        public @Override Object getAttribute(String attr) {
            return b.getHeaders().get(attr);
        }
        public @Override Object getLocalizedAttribute(String attr) {
            return getAttribute(attr);
        }
        public @Override Set<Dependency> getDependencies() {
            return Collections.emptySet(); // XXX search Require-Bundle's? probably unused anyway
        }
        public @Override boolean owns(Class<?> clazz) {
            return FrameworkUtil.getBundle(clazz) == b;
        }
        private ClassLoader loader;
        public @Override synchronized ClassLoader getClassLoader() throws IllegalArgumentException {
            if (loader == null) {
                loader = new OSGiClassLoader(b);
            }
            return loader;
        }
        public @Override String getImplementationVersion() {
            return b.getVersion().getQualifier();
        }
        public @Override String getDisplayName() {
            return (String) getLocalizedAttribute(Constants.BUNDLE_NAME);
        }
    }

}
