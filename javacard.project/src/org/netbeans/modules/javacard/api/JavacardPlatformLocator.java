/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.javacard.api;

import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.InstantiatingIterator;
import org.openide.filesystems.FileObject;

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
}
