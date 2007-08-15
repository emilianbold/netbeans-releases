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

package org.netbeans.modules.tomcat5.progress;

import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;

/**
 *
 * @author Petr Hejl
 */
public class TargetModuleIDImpl implements TargetModuleID {

    private final String targetName;
    
    private final String targetDescription;
    
    private final String moduleID;
    
    private final String url;
    
    public TargetModuleIDImpl(String targetName, String targetDescription,
            String moduleID, String url) {
        
        this.targetName = targetName;
        this.targetDescription = targetDescription;
        this.moduleID = moduleID;
        this.url = url;
    }

    public TargetModuleID[] getChildTargetModuleID() {
        return new TargetModuleID[0];
    }

    public String getModuleID() {
        return moduleID;
    }

    public TargetModuleID getParentTargetModuleID() {
        return null;
    }

    public Target getTarget() {
        return new Target() {

            public String getDescription() {
                return targetDescription;
            }

            public String getName() {
                return targetName;
            }
        };
    }

    public String getWebURL() {
        return url;
    }
}
