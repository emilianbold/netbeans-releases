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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.sun.share.configbean;

import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;


/**
 *  Base class to relate enumerated types of various J2EE/JavaEE versions.
 *
 * @author Peter Williams
 */
public abstract class J2EEBaseVersion implements Comparable {	

    /** -----------------------------------------------------------------------
     *  Implementation
     */
    // This is the module version id, string and numeric form.
    private final String j2eeModuleVersion; // e.g. "2.5" (servlet 2.5), "3.0" (ejb 3.0), etc.
    private final int numericModuleVersion;

    // This is the j2ee/javaee spec version, string and numeric form.
    private final String j2eeSpecVersion; // e.g. "1.4" (j2ee 1.4), "5.0" (javaee 5)
    private final int numericSpecVersion;

    
    /** Creates a new instance of J2EEBaseVersion 
     */
    protected J2EEBaseVersion(String moduleVersion, int nv, String specVersion, int nsv) {
        j2eeModuleVersion = moduleVersion;
        numericModuleVersion = nv;
        j2eeSpecVersion = specVersion;
        numericSpecVersion = nsv;
    }

    /** The string representation of this version.
     *
     * @return String representing the module specification version, e.g. servlet 2.x
     *   ejb-jar 2.x, etc.
     */
    @Override
    public String toString() {
        return j2eeModuleVersion;
    }

    /** Compare the j2ee/javaee spec version of this instance with another (as
     *  opposed to comparing the module type version.
     *
     * @param target Version object to compare with
     * @return -1, 0, 1 if this spec version is less than, equal to, or greater than
     *   the target version.
     */
    public int compareSpecification(J2EEBaseVersion target) {
        if(numericSpecVersion < target.numericSpecVersion) {
            return -1;
        } else if(numericSpecVersion > target.numericSpecVersion) {
            return 1;
        } else {
            return 0;
        }
    }

    /** For use by derived class to compare numeric versions.  Derived class
     *  should ensure target is the appropriate type before invoking this method
     *  to compare the version numbers themselves.
     *
     * @param target Version object to compare with
     * @return -1, 0, 1 if this module version is less than, equal to, or greater than
     *   the target version.
     */
    protected int numericCompare(J2EEBaseVersion target) {
        if(numericModuleVersion < target.numericModuleVersion) {
            return -1;
        } else if(numericModuleVersion > target.numericModuleVersion) {
            return 1;
        } else {
            return 0;
        }
    }

    public static J2EEBaseVersion getVersion(Object/*ModuleType*/ moduleType, String moduleVersion) {
        J2EEBaseVersion version = null;
        if(J2eeModule.WAR.equals(moduleType)) {
            version = ServletVersion.getServletVersion(moduleVersion);
        } else if(J2eeModule.EJB.equals(moduleType)) {
            version = EjbJarVersion.getEjbJarVersion(moduleVersion);
        } else if(J2eeModule.EAR.equals(moduleType)) {
            version = ApplicationVersion.getApplicationVersion(moduleVersion);
        } else if(J2eeModule.CLIENT.equals(moduleType)) {
            version = AppClientVersion.getAppClientVersion(moduleVersion);
        }
        return version;
    }

    /*
    public static J2EEBaseVersion getJ2EEVersion(String version) {
        J2EEBaseVersion result = null;


        if(J2EE_1_3.toString().equals(version)) {
            result = J2EE_1_3;
        } else if(J2EE_1_4.toString().equals(version)) {
            result = J2EE_1_4;
        }

        return result;
    }
    */
}
