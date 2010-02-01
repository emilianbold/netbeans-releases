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

package org.netbeans;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.openide.modules.ModuleInfo;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/** This class contains abstracted calls to OSGi provided by core.netigso
 * module. No other module can implement this class, except core.netigso.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public abstract class NetigsoFramework {

    protected NetigsoFramework() {
        if (!getClass().getName().equals("org.netbeans.core.netigso.Netigso")) { // NOI18N
            throw new IllegalStateException();
        }
    }

    /** Starts the framework.
     */
    protected abstract void prepare(
        Collection<? extends ModuleInfo> preregister
    );

    /** Start the OSGi framework */
    protected abstract void start();

    /** Shutdowns the framework */
    protected abstract void shutdown();

    /** Initializes a classloader for given module.
     * @parma m the module description
     * @param pcl proxy classloader that shall be configured
     * @param jar the module JAR file
     * @return set of covered packages
     */
    protected abstract Set<String> createLoader(
        ModuleInfo m, ProxyClassLoader pcl, File jar
    ) throws IOException;

    /** Deinitializes a classloader for given module */
    protected abstract void stopLoader(ModuleInfo m, ClassLoader loader);

    //
    // Implementation
    //

    private static NetigsoFramework framework;
    private static List<NetigsoModule> toInit = new ArrayList<NetigsoModule>();

    static NetigsoFramework getDefault() {
        if (framework == null) {
            framework = Lookup.getDefault().lookup(NetigsoFramework.class);
            if (framework == null) {
                throw new IllegalStateException("No NetigsoFramework found, is org.netbeans.core.netigso module enabled?"); // NOI18N
            }
        }
        return framework;
    }

    static void willEnable(List<Module> toEnable) {
        boolean found = false;
        if (framework == null) {
            for (Module m : toEnable) {
                if (m instanceof NetigsoModule) {
                    found = true;
                }
            }
        } else {
            found = true;
        }
        if (!found) {
            return;
        }
        getDefault().prepare(toEnable);
    }

    static void turnOn() {
        List<NetigsoModule> init;
        synchronized (NetigsoFramework.class) {
            init = toInit;
            toInit = null;
            if (init == null || init.isEmpty()) {
                return;
            }
        }
        for (NetigsoModule nm : init) {
            try {
                nm.start();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        getDefault().start();
    }


    static synchronized void classLoaderUp(NetigsoModule nm) throws IOException {
        if (toInit != null) {
            toInit.add(nm);
            return;
        }
        nm.start();
    }

    static synchronized void classLoaderDown(NetigsoModule nm) {
        if (toInit != null) {
            toInit.remove(nm);
            return;
        }
    }

    /** Used on shutdown */
    static void shutdownFramework() {
        if (framework != null) {
            framework.shutdown();
        }
        framework = null;
    }
}
