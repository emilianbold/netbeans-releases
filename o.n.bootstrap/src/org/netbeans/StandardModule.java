/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans;

// THIS CLASS OUGHT NOT USE NbBundle NOR org.openide CLASSES
// OUTSIDE OF openide-util.jar! UI AND FILESYSTEM/DATASYSTEM
// INTERACTIONS SHOULD GO ELSEWHERE.
// (NbBundle.getLocalizedValue is OK here.)

import java.io.File;
import java.io.IOException;
import java.util.List;

/** Object representing one module, possibly installed.
 * Responsible for opening of module JAR file; reading
 * manifest; parsing basic information such as dependencies;
 * and creating a classloader for use by the installer.
 * Methods not defined in ModuleInfo must be called from within
 * the module manager's read mutex as a rule.
 * @author Jesse Glick, Allan Gregersen
 */
final class StandardModule extends AbstractStandardModule {

    /** Use ModuleManager.create as a factory. */
    public StandardModule(ModuleManager mgr, Events ev, File jar, Object history, boolean reloadable, boolean autoload, boolean eager) throws IOException {
        super(mgr, ev, jar, history, reloadable, autoload, eager);
    }

    /** Creates a new OneModuleClassLoader used for all modules being
     * initially installed into the system.
     */
    @Override
    protected ClassLoader createNewClassLoader(List<File> classp, List<ClassLoader> parents) {
        return new OneModuleClassLoader(classp, parents.toArray(new ClassLoader[parents.size()]));
    }

    /** String representation for debugging. */
    public @Override String toString() {
        String s = "StandardModule:" + getCodeNameBase() + " jarFile: " + getJarFile().getAbsolutePath(); // NOI18N
        if (!isValid()) s += "[invalid]"; // NOI18N
        return s;
    }

    /** Class loader to load a single module.
     * Auto-localizing, multi-parented, permission-granting, the works.
     */
    class OneModuleClassLoader extends AbstractOneModuleClassLoader {
        private int rc;
        /** Create a new loader for a module.
         * @param classp the List of all module jars of code directories;
         *      includes the module itself, its locale variants,
         *      variants of extensions and Class-Path items from Manifest.
         *      The items are JarFiles for jars and Files for directories
         * @param parents a set of parent classloaders (from other modules)
         */
        public OneModuleClassLoader(List<File> classp, ClassLoader[] parents) throws IllegalArgumentException {
            super(classp, parents);
            rc = releaseCount++;
        }

        protected @Override void finalize() throws Throwable {
            super.finalize();
            Util.err.fine("Finalize for " + this + ": rc=" + rc + " releaseCount=" + releaseCount + " released=" + released); // NOI18N
            if (rc == releaseCount) {
                // Hurrah! release() worked.
                released = true;
            } else {
                Util.err.fine("Now resources for " + getCodeNameBase() + " have been released."); // NOI18N
            }
        }
    }

}
