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

package org.netbeans.modules.autoupdate.services;

import org.netbeans.spi.autoupdate.CustomInstaller;

/**
 *
 * @author Jiri Rechtacek
 */
public class UpdateItemDeploymentImpl {
    private Boolean needsRestart;
    private Boolean isGlobal;
    private String targetCluster;
    private CustomInstaller installer;
    
    /** Creates a new instance of UpdateDeploymentImpl */
    public UpdateItemDeploymentImpl (Boolean needsRestart, Boolean isGlobal, String targetCluster, CustomInstaller installer) {
        this.needsRestart = needsRestart;
        this.isGlobal = isGlobal;
        this.targetCluster = targetCluster;
        this.installer = installer;
    }
    
    public String getTargetCluster () {
        return targetCluster;
    }
    
    public Boolean needsRestart () {
        return needsRestart;
    }
    
    public Boolean isGlobal () {
        return isGlobal;
    }
    
    public CustomInstaller getCustomInstaller () {
        return installer;
    }
}
