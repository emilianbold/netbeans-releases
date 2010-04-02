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

package org.netbeans.api.j2ee.core;

import java.util.Comparator;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.openide.util.NbBundle;

/**
 * Represents the defined Java EE profiles.
 *
 * @author Petr Hejl
 */
public final class Profile {

    public static final Comparator<Profile> UI_COMPARATOR = new Comparator<Profile>() {

        @Override
        public int compare(Profile o1, Profile o2) {
            return -(o1.order - o2.order);
        }
    };

    // Do not ever change name of this constant - it is copied from j2eeserver
    public static final Profile J2EE_13  = new Profile(1, "1.3", null, "J2EE13.displayName");

    // Do not ever change name of this constant - it is copied from j2eeserver
    public static final Profile J2EE_14  = new Profile(2, "1.4", null, "J2EE14.displayName");

    // Do not ever change name of this constant - it is copied from j2eeserver
    public static final Profile JAVA_EE_5  = new Profile(3, "1.5", null, "JavaEE5.displayName");

    public static final Profile JAVA_EE_6_FULL  = new Profile(4, "1.6", null, "JavaEE6Full.displayName");

    public static final Profile JAVA_EE_6_WEB  = new Profile(5, "1.6", "web", "JavaEE6Web.displayName");

    private final int order;

    // cache
    private final String propertiesString;

    private final String bundleKey;

    private Profile(int order, String canonicalName, String profile, String bundleKey) {
        this.order = order;
        this.bundleKey = bundleKey;

        StringBuilder builder = new StringBuilder(canonicalName);
        if (profile != null) {
            builder.append("-").append(profile); // NOI18N
        }
        this.propertiesString = builder.toString();
    }

    /**
     * Returns the UI visible description of the profile.
     *
     * @return the UI visible description of the profile
     */
    @NonNull
    public String getDisplayName() {
        return NbBundle.getMessage(Profile.class, bundleKey);
    }

    @NonNull
    public String toPropertiesString() {
        return propertiesString;
    }

    @Override
    public String toString() {
        return toPropertiesString();
    }

    @CheckForNull
    public static Profile fromPropertiesString(@NullAllowed String value) {
        if (J2EE_13.toPropertiesString().equals(value)) {
            return J2EE_13;
        } else if (J2EE_14.toPropertiesString().equals(value)) {
            return J2EE_14;
        } else if (JAVA_EE_5.toPropertiesString().equals(value)) {
            return JAVA_EE_5;
        } else if (JAVA_EE_6_FULL.toPropertiesString().equals(value)
                || "EE_6_FULL".equals(value)) { // NOI18N
            return JAVA_EE_6_FULL;
        } else if (JAVA_EE_6_WEB.toPropertiesString().equals(value)
                || "EE_6_WEB".equals(value)) {
            return JAVA_EE_6_WEB;
        } else {
            return null;
        }
    }
}
