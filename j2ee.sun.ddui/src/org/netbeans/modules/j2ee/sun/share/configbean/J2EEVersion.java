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
 *  Enumerated types for various J2EE versions.
 *
 *  Be careful with the compareTo method of this class.  It is there for comparing
 *  like versions (e.g. servlet 2.3 versus 2.4) only, but there is no type safety
 *  to prevent doing dumb things like comparing J2EE 1.4 with servlet 2.3.
 *
 *  Perhaps I can think of a better design in the next version.
 *
 * @author Peter Williams
 */
public final class J2EEVersion extends J2EEBaseVersion {	

    /** Represents J2EE version 1.3
     */
    public static final J2EEVersion J2EE_1_3 = new J2EEVersion(
            "1.3", 1300,    // NOI18N
            "1.3", 1300);   // NOI18N

    /** Represents J2EE version 1.4
     */
    public static final J2EEVersion J2EE_1_4 = new J2EEVersion(
            "1.4", 1400,    // NOI18N
            "1.4", 1400);   // NOI18N	

    /** Represents JavaEE version 5.0
     */
    public static final J2EEVersion JAVAEE_5_0 = new J2EEVersion(
            "5.0", 5000,    // NOI18N
            "5.0", 5000);   // NOI18N	

    /** -----------------------------------------------------------------------
     *  Implementation
     */

    /** Creates a new instance of J2EEVersion 
     */
    private J2EEVersion(String version, int nv, String specVersion, int nsv) {
        super(version, nv, specVersion, nsv);
    }

    /** Comparator implementation that works only on J2EEVersion objects
     *
     *  @param obj J2EEVersion to compare with.
     *  @return -1, 0, or 1 if this version is less than, equal to, or greater
     *     than the version passed in as an argument.
     *  @throws ClassCastException if obj is not a J2EEVersion object.
     */
    public int compareTo(Object obj) {
        J2EEVersion target = (J2EEVersion) obj;
        return numericCompare(target);
    }

    public static J2EEVersion getJ2EEVersion(String version) {
        J2EEVersion result = null;

        if(J2EE_1_3.toString().equals(version)) {
            result = J2EE_1_3;
        } else if(J2EE_1_4.toString().equals(version)) {
            result = J2EE_1_4;
        } else if(JAVAEE_5_0.toString().equals(version)) {
            result = JAVAEE_5_0;
        }

        return result;
    }	
}
