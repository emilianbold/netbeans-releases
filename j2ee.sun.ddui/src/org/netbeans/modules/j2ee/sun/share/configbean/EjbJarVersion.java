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


/**
 *  Enumerated types for EjbJar Version
 *
 * @author Peter Williams
 */
public final class EjbJarVersion extends J2EEBaseVersion {

    /** Represents ejbjar version 2.0
     */
    public static final EjbJarVersion EJBJAR_2_0 = new EjbJarVersion(
        "2.0", 2000,	// NOI18N
        "1.3", 1300    // NOI18N
        );

    /** Represents ejbjar version 2.1
     */
    public static final EjbJarVersion EJBJAR_2_1 = new EjbJarVersion(
        "2.1", 2101,	// NOI18N
        "1.4", 1400    // NOI18N
        );

    /** Represents ejbjar version 3.0
     */
    public static final EjbJarVersion EJBJAR_3_0 = new EjbJarVersion(
        "3.0", 3000,	// NOI18N
        "5.0", 5000    // NOI18N
        );

    /** -----------------------------------------------------------------------
     *  Implementation
     */

    /** Creates a new instance of EjbJarVersion 
     */
    private EjbJarVersion(String moduleVersion, int nv, String specVersion, int nsv) {
        super(moduleVersion, nv, specVersion, nsv);
    }

    /** Comparator implementation that works only on EjbJarVersion objects
     *
     *  @param obj EjbJarVersion to compare with.
     *  @return -1, 0, or 1 if this version is less than, equal to, or greater
     *     than the version passed in as an argument.
     *  @throws ClassCastException if obj is not a EjbJarVersion object.
     */
    public int compareTo(Object obj) {
        EjbJarVersion target = (EjbJarVersion) obj;
        return numericCompare(target);
    }

    public static EjbJarVersion getEjbJarVersion(String version) {
        EjbJarVersion result = null;

        if(EJBJAR_2_0.toString().equals(version)) {
            result = EJBJAR_2_0;
        } else if(EJBJAR_2_1.toString().equals(version)) {
            result = EJBJAR_2_1;
        } else if(EJBJAR_3_0.toString().equals(version)) {
            result = EJBJAR_3_0;
        }

        return result;
    }
}
