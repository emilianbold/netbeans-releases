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
package org.netbeans;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 * A stream handler factory that delegates to others in lookup.
 */
public class ProxyURLStreamHandlerFactory implements URLStreamHandlerFactory, LookupListener {

    private static final Logger LOG = Logger.getLogger(ProxyURLStreamHandlerFactory.class.getName());
    private static boolean proxyFactoryInitialized;

    public static synchronized void register() {
        if (!proxyFactoryInitialized) {
            URLStreamHandler originalJarHandler = null;
            try {
                Method m = URL.class.getDeclaredMethod("getURLStreamHandler", String.class);
                m.setAccessible(true);
                originalJarHandler = (URLStreamHandler) m.invoke(null, "jar");
            } catch (Throwable t) {
                LOG.log(Level.SEVERE, "No way to find original stream handler for jar protocol", t); // NOI18N
            }
            try {
                URL.setURLStreamHandlerFactory(new ProxyURLStreamHandlerFactory(null, originalJarHandler));
            } catch (Error e) {
                LOG.log(Level.CONFIG, "Problems registering URLStreamHandlerFactory, trying reflection", e); // NOI18N
                try {
                    URLStreamHandlerFactory prev = null;
                    for (Field f : URL.class.getDeclaredFields()) {
                        LOG.log(Level.FINEST, "Found field {0}", f);
                        if (f.getType() == URLStreamHandlerFactory.class) {
                            LOG.log(Level.FINEST, "Clearing field {0}");
                            f.setAccessible(true);
                            prev = (URLStreamHandlerFactory) f.get(null);
                            LOG.log(Level.CONFIG, "Previous value was {0}", prev);
                            f.set(null, null);
                            LOG.config("Field is supposed to be empty");
                            break;
                        }
                    }
                    if (prev != null && prev.getClass().getName().equals(ProxyURLStreamHandlerFactory.class.getName())) {
                        prev = null;
                    }
                    URL.setURLStreamHandlerFactory(new ProxyURLStreamHandlerFactory(prev, originalJarHandler));
                } catch (Throwable t) {
                    LOG.log(Level.SEVERE, "No way to register URLStreamHandlerFactory; NetBeans is unlikely to work", t); // NOI18N
                }
            }
            proxyFactoryInitialized = true;
        }
    }

    private final URLStreamHandlerFactory delegate;
    private final URLStreamHandler originalJarHandler;
    private Lookup.Result<URLStreamHandlerFactory> r;
    private URLStreamHandlerFactory[] handlers;

    private ProxyURLStreamHandlerFactory(URLStreamHandlerFactory delegate, URLStreamHandler originalJarHandler) {
        this.delegate = delegate;
        this.originalJarHandler = originalJarHandler;
    }

    public URLStreamHandler createURLStreamHandler(String protocol) {
        if (protocol.equals("jar")) {
            return new JarClassLoader.JarURLStreamHandler(originalJarHandler);
        } else if (protocol.equals("file") || protocol.equals("http") || protocol.equals("https") || protocol.equals("resource")) { // NOI18N
            // Well-known handlers in JRE. Do not try to initialize lookup, etc.
            return null;
        } else {
            if (delegate != null) {
                URLStreamHandler h = delegate.createURLStreamHandler(protocol);
                if (h != null) {
                    return h;
                }
            }
            URLStreamHandlerFactory[] _handlers;
            synchronized (this) {
                if (handlers == null) {
                    r = Lookup.getDefault().lookupResult(URLStreamHandlerFactory.class);
                    r.addLookupListener(this);
                    resultChanged(null);
                }
                _handlers = handlers;
            }
            for (URLStreamHandlerFactory f : _handlers) {
                URLStreamHandler h = f.createURLStreamHandler(protocol);
                if (h != null) {
                    return h;
                }
            }
            return null;
        }
    }

    public void resultChanged(LookupEvent ev) {
        Collection<? extends URLStreamHandlerFactory> c = r.allInstances();
        synchronized (this) {
            handlers = c.toArray(new URLStreamHandlerFactory[0]);
        }
    }
}
