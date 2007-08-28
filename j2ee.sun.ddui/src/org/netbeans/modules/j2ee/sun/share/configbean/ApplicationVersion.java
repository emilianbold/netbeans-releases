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
 *  Enumerated types for Application Version
 *
 * @author Peter Williams
 */
public final class ApplicationVersion extends J2EEBaseVersion {

    /** Represents application version 1.3
     */
    public static final ApplicationVersion APPLICATION_1_3 = new ApplicationVersion(
        "1.3", 1300,	// NOI18N
        "1.3", 1300	// NOI18N
        );

    /** Represents application version 1.4
     */
    public static final ApplicationVersion APPLICATION_1_4 = new ApplicationVersion(
        "1.4", 1400,	// NOI18N
        "1.4", 1400	// NOI18N
        );

    /** Represents application version 5.0
     */
    public static final ApplicationVersion APPLICATION_5_0 = new ApplicationVersion(
        "5.0", 5000,	// NOI18N
        "5.0", 5000	// NOI18N
        );

    /** -----------------------------------------------------------------------
     *  Implementation
     */

    /** Creates a new instance of ApplicationVersion 
     */
    private ApplicationVersion(String version, int nv, String specVersion, int nsv) {
        super(version, nv, specVersion, nsv);
    }

    /** Comparator implementation that works only on ApplicationVersion objects
     *
     *  @param obj ApplicationVersion to compare with.
     *  @return -1, 0, or 1 if this version is less than, equal to, or greater
     *     than the version passed in as an argument.
     *  @throws ClassCastException if obj is not a ApplicationVersion object.
     */
    public int compareTo(Object obj) {
        ApplicationVersion target = (ApplicationVersion) obj;
        return numericCompare(target);
    }

    public static ApplicationVersion getApplicationVersion(String version) {
        ApplicationVersion result = null;

        if(APPLICATION_1_3.toString().equals(version)) {
            result = APPLICATION_1_3;
        } else if(APPLICATION_1_4.toString().equals(version)) {
            result = APPLICATION_1_4;
        } else if(APPLICATION_5_0.toString().equals(version)) {
            result = APPLICATION_5_0;
        }

        return result;
    }
}
