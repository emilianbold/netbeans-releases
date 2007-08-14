/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.earproject.util;

import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.earproject.EarProject;

/**
 * Common utilities for Enterprise project.
 * This is a helper class; all methods are static.
 * @author Tomas Mysik
 */
public final class EarProjectUtil {

    private EarProjectUtil() {}

    /**
     * Return <code>true</code> if deployment descriptor is compulsory for given EAR project.
     * @param earProject EAR project instance.
     * @return <code>true</code> if deployment descriptor is compulsory for given EAR project.
     * @see #isDDCompulsory(String)
     */
    public static boolean isDDCompulsory(EarProject earProject) {
        assert earProject != null;
        return isDDCompulsory(earProject.getJ2eePlatformVersion());
    }

    /**
     * Return <code>true</code> if deployment descriptor is compulsory for enterprise application
     * with given Java EE (or J2EE) version (typically applies for J2EE 1.3 or 1.4).
     * <p>
     * For possible JAVA EE versions see {@link J2eeModule J2eeModule constants}.
     * @param j2eeVersion Java EE (or J2EE) version.
     * @return <code>true</code> if deployment descriptor is compulsory.
     * @see J2eeModule
     */
    public static boolean isDDCompulsory(String j2eeVersion) {
        // #103298
        if (j2eeVersion == null) {
            // what should we return?
            return false;
        }
        if (J2eeModule.J2EE_13.equals(j2eeVersion)
                || J2eeModule.J2EE_14.equals(j2eeVersion)) {
            return true;
        } else if (J2eeModule.JAVA_EE_5.equals(j2eeVersion)) {
            return false;
        }
        assert false : "Unknown j2eeVersion: " + j2eeVersion;
        return true;
    }
    
    /**
     * Return <code>true</code> if deployment descriptor exists on the filesystem.
     * <p>
     * This method is useful if we want to write changes to the <i>application.xml</i> file.
     * @param earProject EAR project instance.
     * @return <code>true</code> if deployment descriptor exists on the filesystem for given EAR project.
     * @see org.netbeans.modules.j2ee.earproject.ProjectEar#getDeploymentDescriptor()
     */
    public static boolean isDDWritable(EarProject earProject) {
        return (earProject.getAppModule().getDeploymentDescriptor() != null);
    }
    
    /**
     * Check that the given String is neither <code>null</code> nor of length 0.
     * @param str input String.
     * @return <code>true</code> if input string contains any characters.
     */
    public static boolean hasLength(String str) {
        return str != null && str.length() > 0;
    }
}
