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

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.modules.javacard.platform.BrokenJavacardPlatform;

import java.io.File;
import java.util.Properties;

/**
 *
 * @author Tim Boudreau
 */
public abstract class JavacardPlatform extends JavaPlatform {
    private final String systemName;

    /**
     * Create a new JavacardPlatform
     * @param systemName The name of the DataObject which defines this
     * platform in the system filesystem.  May or may not be the same
     * as the display name.
     */
    protected JavacardPlatform(String systemName) {
        this.systemName = systemName;
    }

    /**
     * Get the system name of this JavacardPlatform.  This should be the
     * name of the DataObject that represents this platform in the
     * system filesystem
     * @return the system name
     */
    public final String getSystemName() {
        return systemName;
    }

    /**
     * Get the bootstrap libraries appropriate to this kind of project.
     * Classic applets and libraries may have a different boot class path
     * than JavaCard v3 ones
     * @param kind The kind of the project
     * @return A classpath
     */
    public abstract ClassPath getBootstrapLibraries(ProjectKind kind);

    /**
     * Determine if this platform is a valid platform.  Will be untrue
     * in cases that this platform is a dummy placeholder for an undefined
     * platform, in the case of opening a project that uses something other
     * than the default name when no similarly named platform has been
     * set up in the IDE.
     * @return true if this is a functioning platform that code can be
     * run agains, false if this is a placeholder for a real platform.
     */
    public abstract boolean isValid();

    /**
     * Get the home directory for this platform
     * @return
     */
    public abstract File getHome();

    /**
     * Return true if this platform points to a copy of the Java Card
     * Reference implementation
     * @return
     */
    public abstract boolean isRI();

    /**
     * Create a dummy invalid platform as a placeholder for a real one,
     * if no platform by that name exists
     * @param name
     * @return
     */
    public static JavacardPlatform createBrokenJavacardPlatform(String name) {
        return new BrokenJavacardPlatform(name);
    }

    /**
     * Get the kind of this platform.  If defined by a properties file,
     * this should return the value of the key javacard.platform.kind, or
     * JavacardPlatformKeyNames.PLATFORM_KIND.
     * <p>
     * This value is used to match factories for Card instances with the
     * right platform.
     * @return The "kind" of this platform
     */
    public abstract String getPlatformKind();

    public Properties toProperties() {
        return new Properties();
    }
}
