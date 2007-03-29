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

/**
 *
 * AutoUndeploySupport.java
 *
 * Created on February 12, 2004, 3:57 PM
 * @author  nn136682
 */

package org.netbeans.modules.j2ee.deployment.plugins.spi;

import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.Target;

/**
 * Service provided by plugin for lookup TargetModuleID.
 * This service basically help J2EE framework identify the target modules
 * needs to be undeployed before a safe full deployment can happen.
 */
public abstract class TargetModuleIDResolver {

    public static final String KEY_CONTEXT_ROOT = "contextRoot";
    public static final String KEY_CONTENT_DIR = "contentDirs";
    public static final TargetModuleID[] EMPTY_TMID_ARRAY = new TargetModuleID[0];
    private static String[] lookupKeys = null;

    public final String[] getLookupKeys() {
        if (lookupKeys == null) {
            lookupKeys = new String[] {
                KEY_CONTEXT_ROOT, KEY_CONTENT_DIR
            };
        }
        return lookupKeys;
    }

    /**
     * Return the list of TargetModuleIDs that could match the given lookup info.
     * @param targetModuleInfo lookup info, keyed by list returned by #getLookupKeys
     * @return array of root TargetModuleIDs. 
     */
    public abstract TargetModuleID[] lookupTargetModuleID(java.util.Map targetModuleInfo, Target[] targetList);
}
