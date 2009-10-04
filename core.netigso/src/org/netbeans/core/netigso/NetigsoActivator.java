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

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Enumeration;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import org.apache.felix.framework.ModuleImpl;
import org.apache.felix.framework.ModuleImpl.ModuleClassLoader;
import org.netbeans.Module;
import org.openide.modules.ModuleInfo;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.SynchronousBundleListener;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class NetigsoActivator
implements BundleActivator, SynchronousBundleListener {
    private Set<Module> all = new CopyOnWriteArraySet<Module>();

    public NetigsoActivator() {
    }
    private BundleContext m_context = null;

    public void start(BundleContext context) {
        m_context = context;
        context.addBundleListener(this);
    }

    public void stop(BundleContext context) {
        context.removeBundleListener(this);
        m_context = null;
    }

    public Bundle[] getBundles() {
        if (m_context != null) {
            return m_context.getBundles();
        }
        return null;
    }

    public void bundleChanged(BundleEvent ev) {
        String loc = ev.getBundle().getLocation();
        NetigsoModule.LOG.log(Level.FINER, "bundleChanged {0}", ev);
        final String pref = "netigso://"; // NOI18N
        if (ev.getType() == BundleEvent.RESOLVED && loc != null && loc.startsWith(pref)) {
            String cnb = loc.substring(pref.length());
            for (ModuleInfo mi : all) {
                if (cnb.equals(mi.getCodeNameBase())) {
                join(ev.getBundle(), mi);
                    return;
            }
        }
            NetigsoModule.LOG.warning("No join for " + cnb);
    }
    }

    void register(Module m) {
        NetigsoModule.LOG.log(Level.FINER, "register module {0}", m.getCodeNameBase());
        all.add(m);
    }


    /** Injects classloader of mi to Felix's bundle.
     */
    private void join(Bundle bundle, ModuleInfo mi) {
        try {
            Field modules = findField(bundle, "m_modules");
            Object[] arr = (Object[])modules.get(bundle);
            Field loader = null;
            for (int i = 0; i < arr.length; i++) {
                ModuleImpl impl = (ModuleImpl)arr[i];
                if (loader == null) {
                    loader = findField(impl, "m_classLoader");
                }
                loader.set(impl, new DelegateLoader(impl, mi));
            }
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
    private static Field findField(Object obj, String name) throws Exception {
        Exception first = null;
        Class<?> c = obj.getClass();
        while (c != null) {
            try {
                Field m = c.getDeclaredField(name);
                m.setAccessible(true);
                return m;
            } catch (Exception e) {
                first = e;
            }
            c = c.getSuperclass();
        }
        throw first;
    }

    private static final class DelegateLoader extends ModuleClassLoader {
        private final ModuleInfo mi;
        public DelegateLoader(ModuleImpl impl, ModuleInfo mi) {
            impl.super(null);
            this.mi = mi;
        }

        @Override
        public Class<?> loadClass(String string) throws ClassNotFoundException {
            return getDelegate().loadClass(string);
        }

        @Override
        public Enumeration<URL> getResources(String string) throws IOException {
            return getDelegate().getResources(string);
        }

        @Override
        public InputStream getResourceAsStream(String string) {
            return getDelegate().getResourceAsStream(string);
        }

        @Override
        public URL getResource(String string) {
            return getDelegate().getResource(string);
        }

        private ClassLoader getDelegate() {
            return mi.getClassLoader();
        }

    } // end of DelegateLoader
}
