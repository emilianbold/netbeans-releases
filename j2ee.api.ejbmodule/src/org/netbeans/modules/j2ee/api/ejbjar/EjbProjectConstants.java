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

import org.netbeans.modules.j2ee.api.common.J2eeProjectConstants;
/** Constants useful for ejb projects.
 *
 * @author Chris Webster
 */
public class EjbProjectConstants {
    
    private EjbProjectConstants() {
    }
    
    /**
     * Constant representing the ejb jar artifact
     */
    public static final String ARTIFACT_TYPE_EJBJAR = J2eeProjectConstants.ARTIFACT_TYPE_J2EE_ARCHIVE; //NOI18N

    /**
     * Constant representing an ejb jar artifact to be packaged as a part of an ear archive.
     */
    public static final String ARTIFACT_TYPE_EJBJAR_EAR_ARCHIVE = J2eeProjectConstants.ARTIFACT_TYPE_J2EE_EAR_ARCHIVE;
    
    /**
     * Standard command for redeploying an ejb module project.
     * @see ActionProvider
     */
    public static final String COMMAND_REDEPLOY = "redeploy" ; //NOI18N
    
}
