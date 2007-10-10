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
 * DigisoftDeploymentPlugin.java
 *
 * Created on 13 June 2007, 16:33
 *
 */

package org.netbeans.modules.mobility.deployment.ricoh;

import java.awt.Component;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.spi.mobility.deployment.DeploymentPlugin;

/**
 *
 * @author Lukas Waldmann
 */
public class RicohDeploymentPlugin implements DeploymentPlugin
{
    final static String RicohDeployment = "RicohDeployment";
    
    public RicohDeploymentPlugin() {
    }
    
    public String getDeploymentMethodName()
    {
        return RicohDeployment;
    }

    public String getDeploymentMethodDisplayName()
    {
        return "Ricoh Deployment";
    }

    public String getAntScriptLocation()
    {
        return "modules/scr/deploy-ricoh-impl.xml"; // NOI18N
    }

    public Map<String, Object> getProjectPropertyDefaultValues()
    {
        return RicohDeploymentProperties.getDefaultProjectProperties();
    }

    public Map<String, Object> getGlobalPropertyDefaultValues()
    {
        return Collections.EMPTY_MAP;
    }

    public Component createProjectCustomizerPanel()
    {
        return new RicohCustomizerPanel();
    }

    public Component createGlobalCustomizerPanel()
    {
        
        return null;
    }    
}
