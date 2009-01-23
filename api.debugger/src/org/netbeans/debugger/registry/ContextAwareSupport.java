/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.debugger.registry;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.spi.debugger.ContextProvider;
import org.openide.util.Exceptions;

/**
 *
 * @author Martin Entlicher
 */
public final class ContextAwareSupport {

    private ContextAwareSupport() {}

    public static Object createInstance(String service, ContextProvider context) {
        try {
            ClassLoader cl = org.openide.util.Lookup.getDefault().lookup(ClassLoader.class);
            String method = null;
            if (service.endsWith("()")) {
                int lastdot = service.lastIndexOf('.');
                if (lastdot < 0) {
                    Exceptions.printStackTrace(
                            new IllegalStateException("Bad service - dot before method name is missing: " +
                            "'" + service + "'."));
                    return null;
                }
                method = service.substring(lastdot + 1, service.length() - 2).trim();
                service = service.substring(0, lastdot);
            }
            Class cls = cl.loadClass (service);

            Object o = null;
            if (method != null) {
                Method m = null;
                if (context != null) {
                    try {
                        m = cls.getDeclaredMethod(method, new Class[] { ContextProvider.class });
                    } catch (NoSuchMethodException nsmex) {}
                }
                if (m == null) {
                    try {
                        m = cls.getDeclaredMethod(method, new Class[] { });
                    } catch (NoSuchMethodException nsmex) {}
                }
                if (m != null) {
                    o = m.invoke(null, (m.getParameterTypes().length == 0)
                                 ? new Object[] {} : new Object[] { context });
                }
            }
            if (o == null && context != null) {
                Constructor[] cs = cls.getConstructors ();
                int i, k = cs.length;
                for (i = 0; i < k; i++) {
                    Constructor c = cs [i];
                    if (c.getParameterTypes ().length != 1 ||
                        !ContextProvider.class.isAssignableFrom(c.getParameterTypes()[0])) {
                        continue;
                    }
                    try {
                        o = c.newInstance (new Object[] {context});
                    } catch (IllegalAccessException e) {
                        Exceptions.printStackTrace(Exceptions.attachMessage(e, "service: " + service));
                    } catch (IllegalArgumentException e) {
                        Exceptions.printStackTrace(Exceptions.attachMessage(e, "service: " + service));
                    }
                }
            }
            if (o == null)
                o = cls.newInstance ();
            if (Logger.getLogger(ContextAwareSupport.class.getName()).isLoggable(Level.FINE)) {
                Logger.getLogger(ContextAwareSupport.class.getName()).fine("instance "+o+" created.");
            }
            return o;
        } catch (ClassNotFoundException e) {
            Exceptions.printStackTrace(
                    Exceptions.attachMessage(
                        e,
                        "The service "+service+" not found."));
        } catch (InstantiationException e) {
            Exceptions.printStackTrace(
                    Exceptions.attachMessage(
                    e,
                    "The service "+service+" can not be instantiated."));
        } catch (IllegalAccessException e) {
            Exceptions.printStackTrace(
                    Exceptions.attachMessage(
                    e,
                    "The service "+service+" can not be accessed."));
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(
                    Exceptions.attachMessage(
                    ex,
                    "The service "+service+" can not be created."));
        } catch (ExceptionInInitializerError ex) {
            Exceptions.printStackTrace(
                    Exceptions.attachMessage(
                    ex,
                    "The service "+service+" can not be initialized."));
        }
        return null;
    }

}
