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

package org.netbeans.modules.j2ee.deployment.devmodules.api;

import java.util.Comparator;
import org.openide.util.NbBundle;

/**
 * Represents the defined Java EE profiles.
 *
 * @author Petr Hejl
 */
public final class Profile implements Comparable<Profile> {

    public static final Comparator<Profile> REVERSE_COMPARATOR = new Comparator<Profile>() {

        public int compare(Profile o1, Profile o2) {
            return -(o1.compareTo(o2));
        }
    };

    public static final Profile J2EE_13  = new Profile(1, J2eeModule.J2EE_13, "J2EE13.displayName");

    public static final Profile J2EE_14  = new Profile(2, J2eeModule.J2EE_14, "J2EE14.displayName");

    public static final Profile JAVA_EE_5  = new Profile(3, J2eeModule.JAVA_EE_5, "JavaEE5.displayName");

    public static final Profile JAVA_EE_6_FULL  = new Profile(4, "EE_6_FULL", "JavaEE6Full.displayName");

    public static final Profile JAVA_EE_6_WEB  = new Profile(5, "EE_6_WEB", "JavaEE6Web.displayName");

    private final int order;

    private final String name;

    private final String bundleKey;

    private Profile(int order, String name, String bundleKey) {
        this.order = order;
        this.name = name;
        this.bundleKey = bundleKey;
    }

    /**
     * Returns the UI visible description of the profile.
     *
     * @return the UI visible description of the profile
     */
    public String getDisplayName() {
        return NbBundle.getMessage(Profile.class, bundleKey);
    }

    public String toPropertiesString() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    public int compareTo(Profile o) {
        return this.order - o.order;
    }

    public static Profile fromPropertiesString(String value) {
        if (J2EE_13.toPropertiesString().equals(value)) {
            return J2EE_13;
        } else if (J2EE_14.toPropertiesString().equals(value)) {
            return J2EE_14;
        } else if (JAVA_EE_5.toPropertiesString().equals(value)) {
            return JAVA_EE_5;
        } else if (JAVA_EE_6_FULL.toPropertiesString().equals(value)) {
            return JAVA_EE_6_FULL;
        } else if (JAVA_EE_6_WEB.toPropertiesString().equals(value)) {
            return JAVA_EE_6_WEB;
        } else {
            return null;
        }
    }
}
