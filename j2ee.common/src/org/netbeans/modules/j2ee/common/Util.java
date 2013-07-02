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

package org.netbeans.modules.j2ee.common;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;

public class Util {

    private static final Logger LOGGER = Logger.getLogger(Util.class.getName());
    
    /**
     * Is J2EE version of a given project JavaEE 5 or higher?
     *
     * @param project J2EE project
     * @return true if J2EE version is JavaEE 5 or higher; otherwise false
     */
    public static boolean isJavaEE5orHigher(Project project) {
        if (project == null) {
            return false;
        }
        J2eeModuleProvider j2eeModuleProvider = project.getLookup().lookup(J2eeModuleProvider.class);
        if (j2eeModuleProvider != null) {
            J2eeModule j2eeModule = j2eeModuleProvider.getJ2eeModule();
            if (j2eeModule != null) {
                J2eeModule.Type type = j2eeModule.getType();
                String strVersion = j2eeModule.getModuleVersion();
                assert strVersion != null : "Module type " + j2eeModule.getType() + " returned null module version"; // NOI18N
                try {    
                    double version = Double.parseDouble(strVersion);
                    if (J2eeModule.Type.EJB.equals(type) && (version > 2.1)) {
                        return true;
                    }
                    if (J2eeModule.Type.WAR.equals(type) && (version > 2.4)) {
                        return true;
                    }
                    if (J2eeModule.Type.CAR.equals(type) && (version > 1.4)) {
                        return true;
                    }
                } catch (NumberFormatException ex) {
                    LOGGER.log(Level.INFO, "Module version invalid " + strVersion, ex);
                }                
            }
        }
        return false;
    }

    /**
     * Find out if the version of the given profile is at least Java EE 5 or higher.
     *
     * @param profile profile that we want to compare
     * @return true if the version of the given profile is Java EE 5 or higher,
     *         false otherwise
     * @since 1.75
     */
    public static boolean isAtLeastJavaEE5(@NonNull Profile profile) {
        return isVersionEqualOrHigher(profile, Profile.JAVA_EE_5);
    }

    /**
     * Find out if the version of the given profile is at least Java EE 6 Web or
     * higher. Please be aware that Java EE 6 Web is considered as lower than Java
     * EE 6 Full.
     *
     * @param profile profile that we want to compare
     * @return true if the version of the given profile is Java EE 6 Web or
     *         higher, false otherwise
     * @since 1.75
     */
    public static boolean isAtLeastJavaEE6Web(@NonNull Profile profile) {
        return isVersionEqualOrHigher(profile, Profile.JAVA_EE_6_WEB);
    }

    /**
     * Find out if the version of the given profile is at least Java EE 7 Web or
     * higher. Please be aware that Java EE 7 Web is considered as lower than Java
     * EE 7 Full.
     *
     * @param profile profile that we want to compare
     * @return true if the version of the given profile is Java EE 7 Web or
     *         higher, false otherwise
     * @since 1.79
     */
    public static boolean isAtLeastJavaEE7Web(@NonNull Profile profile) {
        return isVersionEqualOrHigher(profile, Profile.JAVA_EE_7_WEB);
    }

    /**
     * Compares if the first given profile has equal or higher Java EE version
     * in comparison to the second profile.
     *
     * Please be aware of the following rules:
     * <br/><br/>
     *
     * 1) Each Java EE X version is considered as lower than Java EE X+1 version
     * (this applies regardless on Web/Full specification and in reality it means
     * that even Java EE 6 Full version is considered as lower than Java EE 7 Web)
     * <br/><br/>
     *
     * 2) Each Java EE X Web version is considered as lower than Java EE X Full
     * <br/>
     *
     * @param profileToCompare profile that we want to compare
     * @param comparingVersion version which we are comparing with
     * @return <code>true</code> if the profile version is equal or higher in
     *         comparison with the second one, <code>false</code> otherwise
     * @since 1.75
     */
    private static boolean isVersionEqualOrHigher(
            @NonNull Profile profileToCompare,
            @NonNull Profile comparingVersion) {

        int comparisonResult = Profile.UI_COMPARATOR.compare(profileToCompare, comparingVersion);
        if (comparisonResult == 0) {
            // The same version for both
            return true;

        } else {
            String profileToCompareVersion = getProfileVersion(profileToCompare);
            String comparingProfileVersion = getProfileVersion(comparingVersion);

            // If the canonicalName is the same value we have to differ between Web and Full profile
            if (profileToCompareVersion.equals(comparingProfileVersion)) {
                return compareWebAndFull(profileToCompare, comparingVersion);
            } else {
                if (comparisonResult > 0) {
                    // profileToCompare has lower version than comparingVersion
                    return false;
                } else {
                    return true;
                }
            }
        }
    }

    private static boolean compareWebAndFull(
            @NonNull Profile profileToCompare,
            @NonNull Profile comparingVersion) {

        boolean isThisFullProfile = isFullProfile(profileToCompare);
        boolean isParamFullProfile = isFullProfile(comparingVersion);

        if (isThisFullProfile && isParamFullProfile) {
            // Both profiles are Java EE Full
            return true;
        }
        if (!isThisFullProfile && !isParamFullProfile) {
            // Both profiles are Java EE Web
            return true;
        }
        if (isThisFullProfile && !isParamFullProfile) {
            // profileToCompare is Java EE Full profile and comparingVersion is only Java EEWeb profile
            return true;
        }
        return false;
    }

    private static String getProfileVersion(@NonNull Profile profile) {
        String profileDetails = profile.toPropertiesString();
        int indexOfDash = profileDetails.indexOf("-");
        if (indexOfDash != -1) {
            return profileDetails.substring(0, indexOfDash);
        }
        return profileDetails;
    }

    private static boolean isFullProfile(@NonNull Profile profile) {
        final String profileDetails = profile.toPropertiesString();
        if (profileDetails.indexOf("-") == -1) {
            return true;
        }
        return false;
    }
    
}
