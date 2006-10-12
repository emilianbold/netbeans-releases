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

package org.netbeans.modules.j2ee.api.ejbjar;

/** Constants useful for ejb projects.
 *
 * @author Chris Webster
 *
 *
 */

public class EjbProjectConstants {

    public static final String J2EE_13_LEVEL = "1.3"; //NOI18N
    public static final String J2EE_14_LEVEL = "1.4"; //NOI18N
    public static final String JAVA_EE_5_LEVEL = "1.5"; //NOI18N

    private EjbProjectConstants() {
    }

    /**
     * Constant representing the ejb jar artifact
     */
    public static final String ARTIFACT_TYPE_EJBJAR = "j2ee_archive"; //NOI18N

    /**
     * Standard command for redeploying an ejb module project.
     * @see ActionProvider
     */
    public static final String COMMAND_REDEPLOY = "redeploy" ; //NOI18N
    
    /**
     * Constant representing an j2ee jar artifact to be packaged as a part of an ear archive.
     */
    public static final String ARTIFACT_TYPE_J2EE_MODULE_IN_EAR_ARCHIVE = "j2ee_ear_archive"; //NOI18N
}
