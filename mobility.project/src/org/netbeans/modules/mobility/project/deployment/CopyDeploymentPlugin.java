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

/*
 * CopyDeploymentPlugin.java
 *
 * Created on 9. prosinec 2004, 17:10
 */
package org.netbeans.modules.mobility.project.deployment;

import java.awt.Component;
import java.io.File;
import java.util.Collections;
import java.util.Map;
import org.openide.util.NbBundle;
import org.netbeans.spi.mobility.deployment.DeploymentPlugin;

/**
 *
 * @author Adam Sotona
 */
public class CopyDeploymentPlugin implements DeploymentPlugin {
    
    public static final String PROP_TARGET = "deployment.copy.target"; //NOI18N
    static final Map<String, Object> propDefValues = Collections.singletonMap(PROP_TARGET, (Object)new File("")); //NOI18N
        
    public String getAntScriptLocation() {
        return "modules/scr/deploy-copy-impl.xml"; // NOI18N
    }
    
    public Component createCustomizerPanel() {
        return new CopyCustomizerPanel();
    }
    
    public String getDeploymentMethodName() {
        return "Copy"; //NOI18N
    }
    
    public String getDeploymentMethodDisplayName() {
        return NbBundle.getMessage(CopyDeploymentPlugin.class, "LBL_CopyTypeName"); //NOI18N
    }
    
    public Map<String, Object> getPropertyDefaultValues() {
        return propDefValues;
    }
    
}
