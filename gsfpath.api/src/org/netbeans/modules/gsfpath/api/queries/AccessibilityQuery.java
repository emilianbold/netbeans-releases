/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.gsfpath.api.queries;

import org.netbeans.modules.gsfpath.spi.queries.AccessibilityQueryImplementation;
import org.openide.util.Lookup;
import org.openide.filesystems.FileObject;


/**
 * Indicates whether a Java package should be considered publicly accessible.
 * <div class="nonnormative">
 * <p>Suggested uses:</p>
 * <ol>
 * <li>Visually marking public and private packages as such.</li>
 * <li>Editor code completion could refuse to include private packages from
 * other compilation units.</li>
 * <li>Javadoc editing tools (the suggestions provider and/or AutoComment) could
 * treat missing or incomplete Javadoc in private packages as a minor error, or
 * not an error.</li>
 * </ol>
 * <p>If the Java Project module is enabled, you may register an implementation
 * to the lookup for a project rather than the default lookup.</p>
 * </div>
 * @see AccessibilityQueryImplementation
 * @author Jesse Glick
 * @since org.netbeans.modules.gsfpath.api/1 1.4
 */
public class AccessibilityQuery {
    
    private static final Lookup.Result<? extends AccessibilityQueryImplementation> implementations =
        Lookup.getDefault().lookupResult(AccessibilityQueryImplementation.class);

    private AccessibilityQuery() {}

    /**
     * Check whether a given Java source package should be considered publicly
     * accessible for use by other compilation units.
     * If not, then even public classes in the package should be treated as
     * effectively private by the IDE (though the Java compiler will not forbid
     * you to access them).
     * @param pkg a Java source package (must have a corresponding
     *        {@link org.netbeans.modules.gsfpath.api.classpath.ClassPath#SOURCE} root)
     * @return true if the package is definitely intended for public access from
     *         other compilation units, false if it is definitely not, or null if
     *         this information is not known
     */
    public static Boolean isPubliclyAccessible(FileObject pkg) {
        if (!pkg.isFolder()) {
            throw new IllegalArgumentException("Not a folder: " + pkg); // NOI18N
        }
        for ( AccessibilityQueryImplementation aqi : implementations.allInstances()) {
            Boolean b = aqi.isPubliclyAccessible(pkg);
            if (b != null) {
                return b;
            }
        }
        return null;
    }

}
