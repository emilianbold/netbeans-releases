/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.keyring.jps;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;

/**
 * Reflectively accesses {@code oracle.security.jps.service.credstore.CredentialStore}.
 * @see <a href="http://download.oracle.com/docs/cd/E12839_01/doc.1111/e14650/index.html">Security Services API Reference</a>
 */
final class CredentialStoreProxy {

    private final /*CredentialStore*/Object store;
    private final Method getCredential, resetCredential, deleteCredential, newPasswordCredential, getPassword;
    private final String mapName;
    private final ClassLoader l;

    CredentialStoreProxy(String configFile, String impl, String contextName, String mapName) throws Exception {
        if (configFile == null || configFile.isEmpty() || !new File(configFile).isFile()) {
            throw new IllegalArgumentException("bad configFile: " + configFile);
        }
        if (impl == null || impl.isEmpty() || !new File(impl).isFile()) {
            throw new IllegalArgumentException("bad impl: " + impl);
        }
        if (contextName == null || contextName.isEmpty()) {
            throw new IllegalArgumentException("bad contextName: " + contextName);
        }
        if (mapName == null || mapName.isEmpty()) {
            throw new IllegalArgumentException("bad mapName: " + mapName);
        }
        this.mapName = mapName;
        System.setProperty("oracle.security.jps.config", configFile);
        l = new URLClassLoader(new URL[] {new File(impl).toURI().toURL()});
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(l);
        try {
            Class<?> JpsContextFactory = l.loadClass("oracle.security.jps.JpsContextFactory");
            Class<?> JpsContext = l.loadClass("oracle.security.jps.JpsContext");
            Class<?> CredentialStore = l.loadClass("oracle.security.jps.service.credstore.CredentialStore");
            Class<?> Credential = l.loadClass("oracle.security.jps.service.credstore.Credential");
            Class<?> PasswordCredential = l.loadClass("oracle.security.jps.service.credstore.PasswordCredential");
            Class<?> CredentialFactory = l.loadClass("oracle.security.jps.service.credstore.CredentialFactory");
            getCredential = CredentialStore.getMethod("getCredential", String.class, String.class);
            resetCredential = CredentialStore.getMethod("resetCredential", String.class, String.class, Credential);
            deleteCredential = CredentialStore.getMethod("deleteCredential", String.class, String.class);
            newPasswordCredential = CredentialFactory.getMethod("newPasswordCredential", String.class, char[].class, String.class);
            getPassword = PasswordCredential.getMethod("getPassword");
            /*JpsContextFactory*/Object contextFactory = JpsContextFactory.getMethod("getContextFactory").invoke(null);
            /*JpsContext*/Object context = JpsContextFactory.getMethod("getContext", String.class).invoke(contextFactory, contextName);
            if (context == null) {
                throw new IllegalArgumentException("no such context: " + contextName);
            }
            store = JpsContext.getMethod("getServiceInstance", Class.class).invoke(context, CredentialStore);
            if (store == null) {
                throw new IllegalStateException("no CredentialStore");
            }
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
        read("nonexistent"); // sanity check
    }

    private <T> T run(PrivilegedExceptionAction<T> action) throws Exception {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(l); // e.g. CredentialFactory.newPasswordCredential uses CCL
        try {
            return AccessController.doPrivileged(action);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    char[] read(final String key) throws Exception {
        return run(new PrivilegedExceptionAction<char[]>() {
            @Override public char[] run() throws Exception {
                /*Credential*/Object credential = getCredential.invoke(store, mapName, key);
                if (credential == null) {
                    return null;
                }
                return (char[]) getPassword.invoke(credential);
            }
        });
    }

    void save(final String key, final char[] password, final String description) throws Exception {
        run(new PrivilegedExceptionAction<Void>() {
            @Override public Void run() throws Exception {
                resetCredential.invoke(store, mapName, key, newPasswordCredential.invoke(null, /*name*/"_", password, description));
                return null;
            }
        });
    }

    void delete(final String key) throws Exception {
        run(new PrivilegedExceptionAction<Void>() {
            @Override public Void run() throws Exception {
                if (getCredential.invoke(store, mapName, key) != null) { // otherwise throws CredentialNotFoundException
                    deleteCredential.invoke(store, mapName, key);
                }
                return null;
            }
        });
    }

}
