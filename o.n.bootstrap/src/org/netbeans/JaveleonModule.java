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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Allan Gregersen
 */
public final class JaveleonModule extends AbstractStandardModule {

    public static final boolean isJaveleonPresent;

    static {
        boolean present = false;
        try {
            Thread.currentThread().getContextClassLoader().loadClass("org.javeleon.reload.ReloadFacade");
            present = true;
        } catch (ClassNotFoundException ex) {
            // Javeleon was not present... nothing to do then!
        }
        isJaveleonPresent = present;
    }

    public static Method javeleonReloadMethod;

    static {
        if(JaveleonModule.isJaveleonPresent) {
            try {
                Class javeleonReloadClass = Thread.currentThread().getContextClassLoader().loadClass("org.javeleon.reload.ReloadModule");
                javeleonReloadMethod = javeleonReloadClass.getDeclaredMethod("incrementGlobalId", new Class[]{});
            } catch (Exception ex) {
               // No worries, javeleon is just not enabled
            }
        }
    }

    private static HashMap<String,ClassLoader> currentClassLoaders = new HashMap<String, ClassLoader>();


    public JaveleonModule(ModuleManager mgr, File jar, Object history, Events ev) throws IOException {
        super(mgr, ev, jar, history, true, false, false);
        setEnabled(true);
    }

    @Override
    protected ClassLoader createNewClassLoader(List<File> classp, List<ClassLoader> parents) {
        ClassLoader cl = new BaseModuleClassLoader(classp, parents.toArray(new ClassLoader[parents.size()]));
        currentClassLoaders.put(getCodeNameBase(), cl);
        return cl;
    }

    public @Override void classLoaderUp(Set<Module> parents) throws IOException {
        super.classLoaderUp(parents);
    }

    @Override
    protected ClassLoader getParentLoader(Module parent) {
        if(currentClassLoaders.containsKey(parent.getCodeNameBase()))
            return currentClassLoaders.get(parent.getCodeNameBase());
        else
            return parent.getClassLoader();
    }

    @Override
    public String toString() {
        return "Javeleon module " + getJarFile().toString();
    }

    /** Open the JAR, load its manifest, and do related things. */
    protected void loadManifest() throws IOException {
       setManifest(getManager().loadManifest(getPhysicalJar()));
       parseManifest();
    }

    @Override
    protected void classLoaderDown() {
        // do not touch the class loader... Javeleon system will handle it
    }

    @Override
    public final void reload() throws IOException {
        // Javeleon will do this
    }

    @Override
    protected void cleanup() {
        // do nothing
    }

    @Override
    public void destroy() {
        // do nothing
    }
}
