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
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Enumeration;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.felix.moduleloader.IContent;
import org.apache.felix.moduleloader.IContentLoader;
import org.apache.felix.moduleloader.IModule;
import org.apache.felix.moduleloader.ISearchPolicy;
import org.apache.felix.moduleloader.IURLPolicy;
import org.apache.felix.moduleloader.ResourceNotFoundException;
import org.netbeans.Module;
import org.netbeans.ProxyClassLoader;
import org.netbeans.core.startup.MainLookup;
import org.openide.modules.ModuleInfo;
import org.openide.util.Enumerations;
import org.openide.util.Exceptions;
import org.openide.util.lookup.InstanceContent;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.SynchronousBundleListener;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class NetigsoActivator
implements BundleActivator, SynchronousBundleListener, ServiceListener,
InstanceContent.Convertor<ServiceReference, Object> {
    static final Logger LOG = Logger.getLogger(NetigsoActivator.class.getName());

    private Set<Module> all = new CopyOnWriteArraySet<Module>();

    public NetigsoActivator() {
    }
    private BundleContext m_context = null;

    public void start(BundleContext context) {
        m_context = context;
        context.addBundleListener(this);
        context.addServiceListener(this);
    }

    public void stop(BundleContext context) {
        context.removeBundleListener(this);
        context.removeServiceListener(this);
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
        LOG.log(Level.FINER, "bundleChanged {0}", ev);
        final String pref = "netigso://"; // NOI18N
        if (ev.getType() == BundleEvent.RESOLVED && loc != null && loc.startsWith(pref)) {
            String cnb = loc.substring(pref.length());
            for (ModuleInfo mi : all) {
                if (cnb.equals(mi.getCodeNameBase())) {
                join(ev.getBundle(), mi);
                    return;
            }
        }
            LOG.warning("No join for " + cnb);
    }
    }

    void register(Module m) {
        LOG.log(Level.FINER, "register module {0}", m.getCodeNameBase());
        all.add(m);
    }


    /** Injects classloader of mi to Felix's bundle.
     */
    private void join(Bundle bundle, ModuleInfo mi) {
        try {
            Method m = findMethod(bundle, "getInfo");
            Object info = m.invoke(bundle);
            Method m2 = findMethod(info, "getCurrentModule");
            IModule imodule = (IModule)m2.invoke(info);
            Method m3 = findMethod(imodule, "setContentLoader", IContentLoader.class);
            m3.invoke(imodule, new ModuleContentLoader(mi));
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static Method findMethod(Object obj, String name, Class... args) throws Exception {
        Exception first = null;
        Class<?> c = obj.getClass();
        while (c != null) {
            try {
                Method m = c.getDeclaredMethod(name, args);
                m.setAccessible(true);
                return m;
            } catch (Exception e) {
                first = e;
            }
            c = c.getSuperclass();
        }
        throw first;
    }

    public void serviceChanged(ServiceEvent ev) {
        final ServiceReference ref = ev.getServiceReference();
        if (ev.getType() == ServiceEvent.REGISTERED) {
            MainLookup.register(ref, this);
        }
        if (ev.getType() == ServiceEvent.UNREGISTERING) {
            MainLookup.unregister(ref, this);
        }
    }

    public Object convert(ServiceReference obj) {
        return obj.getBundle().getBundleContext().getService(obj);
    }

    public Class<? extends Object> type(ServiceReference obj) {
        String[] arr = (String[])obj.getProperty(Constants.OBJECTCLASS);
        if (arr.length > 0) {
            try {
                return (Class<?>)obj.getBundle().loadClass(arr[0]);
            } catch (ClassNotFoundException ex) {
                LOG.log(Level.INFO, "Cannot load service class", arr[0]); // NOI18N
            }
        }
        return Object.class;
    }

    public String id(ServiceReference obj) {
        return (String) obj.getProperty(Constants.SERVICE_ID);
    }

    public String displayName(ServiceReference obj) {
        return (String) obj.getProperty(Constants.SERVICE_DESCRIPTION);
    }

    private static final class ModuleContentLoader implements IContentLoader,
    ISearchPolicy {
        private final ModuleInfo mi;

        public ModuleContentLoader(ModuleInfo mi) {
            this.mi = mi;
        }

        public void close() {
        }

        public IContent getContent() {
            return null;
        }

        ISearchPolicy sp;
        public void setSearchPolicy(ISearchPolicy arg0) {
            sp = arg0;
        }

        public ISearchPolicy getSearchPolicy() {
            return sp == null ? this : sp;
        }

        IURLPolicy up;
        public void setURLPolicy(IURLPolicy arg0) {
            up = arg0;
        }

        public IURLPolicy getURLPolicy() {
            return up;
        }

        Object sc;
        public void setSecurityContext(Object arg0) {
            sc = arg0;
        }

        public Object getSecurityContext() {
            return sc;
        }

        public Class getClass(String name) {
            try {
                return mi.getClassLoader().loadClass(name);
            } catch (ClassNotFoundException ex) {
                return null;
            }
        }

        public URL getResource(String name) {
            if (mi.getClassLoader() instanceof ProxyClassLoader) {
                return ((ProxyClassLoader)mi.getClassLoader()).findResource(name);
            }
            return mi.getClassLoader().getResource(name);
        }

        public Enumeration getResources(String name) {
            try {
                if (mi.getClassLoader() instanceof ProxyClassLoader) {
                    return ((ProxyClassLoader) mi.getClassLoader()).findResources(name);
                }
                return mi.getClassLoader().getResources(name);
            } catch (IOException iOException) {
                Exceptions.printStackTrace(iOException);
                return Enumerations.empty();
            }
        }

        public URL getResourceFromContent(String arg0) {
            return null;
        }

        public boolean hasInputStream(int arg0, String arg1) throws IOException {
            return getResource(arg1) != null;
        }

        public InputStream getInputStream(int arg0, String name) throws IOException {
            return getResource(name).openStream();
        }

        public Object[] definePackage(String arg0) {
            return null;
        }

        public Class findClass(String name) throws ClassNotFoundException {
            return getClass(name);
        }

        public URL findResource(String name) throws ResourceNotFoundException {
            return findResource(name);
        }

        public Enumeration findResources(String name) throws ResourceNotFoundException {
            return getResources(name);
        }

        public String findLibrary(String arg0) {
            return null;
        }

    } // end of ModuleContentLoader

}
