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

package org.netbeans.modules.j2ee.sun.share.config;

import javax.swing.SwingUtilities;
import javax.enterprise.deploy.model.DDBean;

/**
 * Helper class.
 *
 * @author sherold
 */
public final class Utils {

    // !PW These are deliberately not public for now, I don't see any use of them
    // outside this package.  I suppose this premise could change though.
    static final String SERVER_ID_AS81 = "J2EE"; // NOI18N
    static final String SERVER_ID_AS90 = "JavaEE5"; // NOI18N
    static final String SERVER_ID_WS70 = "SUNWebserver7"; // NOI18N
    
    /**
     * Check that current target server is Sun AppServer.
     */
    public static boolean isSunServer(String serverId) {
        boolean result = false;
        if(SERVER_ID_AS81.equals(serverId) || SERVER_ID_AS90.equals(serverId)
            || SERVER_ID_WS70.equals(serverId)) {  
            result = true;
        }
        return result;
    }
    
    /**
     *  Quick method to return the value of an expected singular child field of
     *  a DDBean or null if not found.
     */
    public static String getField(DDBean bean, String fieldId) {
        String result = null;
        DDBean[] childFields = bean.getChildBean(fieldId);
        if(childFields.length > 0) {
           result = childFields[0].getText();
        }
        return result;
    }
}
