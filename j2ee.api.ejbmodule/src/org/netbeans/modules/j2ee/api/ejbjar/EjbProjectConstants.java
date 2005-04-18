/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.api.ejbjar;

/** Constants useful for ejb projects. This class is not used in ejb project anymore and should be removed.
 * It was replaced by org.netbeans.modules.j2ee.common.J2eeProjectConstants.
 *
 * @author Chris Webster
 *  
 *
 */
public class EjbProjectConstants {
    
    private EjbProjectConstants() {
    }
    
    /**
     * Constant representing the ejb jar artifact
     */
    public static final String ARTIFACT_TYPE_EJBJAR = "j2ee_archive"; //NOI18N

    /**
     * Constant representing an ejb jar artifact to be packaged as a part of an ear archive.
     */
    public static final String ARTIFACT_TYPE_EJBJAR_EAR_ARCHIVE = "j2ee_ear_archive"; //NOI18N
    
    /**
     * Standard command for redeploying an ejb module project.
     * @see ActionProvider
     */
    public static final String COMMAND_REDEPLOY = "redeploy" ; //NOI18N
    
}
