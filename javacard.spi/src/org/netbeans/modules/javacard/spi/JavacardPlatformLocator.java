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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.javacard.spi;

import java.io.IOException;
import java.util.Collection;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.InstantiatingIterator;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * Class which allows extension modules to identify a valid emulator/deployer
 * install on the user's disk.  Register an instance of this in the default
 * lookup to allow it to be used when searching for Java Card installs on
 * disk in Tools > Java Platforms.
 *
 * @author Tim Boudreau
 */
public abstract class JavacardPlatformLocator {
    /**
     * Create a wizard which is responsible for additonal steps of discovering,
     * validating and configuring this platform.
     * @param dir The directory, which presumably a call to accept() has already
     * returned true for.
     * @return A wizard iterator whose instantiate() will create a file on
     * disk whose Node contains an instance of JavaPlatform, and whose format
     * is that of a properties file containing all properties a build script
     * will need to build and deploy using this platform.
     */
    public abstract InstantiatingIterator<WizardDescriptor> createIterator(FileObject dir);
    /**
     * Return true if the directory passed is identified as a Javacard Platform,
     * by whatever definition this implementation uses to determine that.
     * This method should simply check for a well known file pattern and return
     * quickly.  The wizard iterator create if true is returned can display
     * any problems or do deeper validation.
     * @param f A directory visible in a file chooser
     * @return true if this looks like a valid emulator or platform install
     */
    public abstract boolean accept(FileObject f);

    /**
     * Set up a Java Card Platform in the IDE, using the default settings
     * and no GUI input.
     * <p/>
     * This method is for use by modules which wish to bundle a runtime and
     * have it automatically configured in the IDE without user intervention.
     *
     * @param platformFolder A folder, which this JavacardPlatformLocator has
     * returned true from accept() for, which should be set up as a platform
     * @param displayName The display name the resulting platform should have
     * in NetBeans' UI
     * @return a FileObject for the created platform file in the platforms
     * folder in the system FS
     */
    public abstract FileObject install (FileObject platformFolder, String displayName) throws IOException;

    /**
     * Find the wizard steps for a given directory, if any.  Simply iterates
     * all instances in the default lookup, returning the result of createIterator()
     * for the first instance that returns true from accept().
     * @param dir A directory which might be a Java Card platform install
     * @return A wizard iterator that provides the rest of the wizard steps
     * for setting up the platform
     */
    public static InstantiatingIterator<WizardDescriptor> find(FileObject dir) {
        Collection<? extends JavacardPlatformLocator> all =
                Lookup.getDefault().lookupAll(JavacardPlatformLocator.class);
        for (JavacardPlatformLocator l : all) {
            if (l.accept(dir)) {
                return l.createIterator(dir);
            }
        }
        return null;
    }

    /** Determine if any installed JavacardPlatformLocator identifies the
     * passed folder as a Java Card platform.
     * @param dir A folder on disk
     * @return true if some installed locator identified the passed dir as
     * a Java Card platform installation.
     */
    public static boolean isPlatform(FileObject dir) {
        Collection<? extends JavacardPlatformLocator> all =
                Lookup.getDefault().lookupAll(JavacardPlatformLocator.class);
        for (JavacardPlatformLocator l : all) {
            if (l.accept(dir)) {
                return true;
            }
        }
        return false;
    }
}
