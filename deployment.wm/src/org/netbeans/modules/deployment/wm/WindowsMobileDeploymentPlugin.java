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

package org.netbeans.modules.deployment.wm;

import java.awt.Component;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.spi.mobility.deployment.DeploymentPlugin;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Ryzl
 */
public class WindowsMobileDeploymentPlugin implements DeploymentPlugin {
    
    /* should be set as default property in deploy-wm-impl.xml */
    public static final String DEFAULT_APP_LOCATION = "\\My Documents\\NetBeans Applications";
    public static final String PROP_APP_LOCATION = "wm.app.location";
    
    /** Creates a new instance of WindowsMobileDeploymentPlugin */
    public WindowsMobileDeploymentPlugin() {
    }

    public String getDeploymentMethodName() {
        return "WindowsMobile"; // NOI18N
    }

    public String getDeploymentMethodDisplayName() {
        return NbBundle.getMessage(WindowsMobileDeploymentPlugin.class, "LBL_WindowsMobileTypeName"); //NOI18N
    }

    public String getAntScriptLocation() {
        return "modules/scr/deploy-wm-impl.xml"; // NOI18N
    }


    public Map<String, Object> getProjectPropertyDefaultValues() {
        return Collections.emptyMap();
    }

    public Map<String, Object> getGlobalPropertyDefaultValues() {
        HashMap<String, Object> map = new HashMap<String, Object>(5);
        map.put(PROP_APP_LOCATION, DEFAULT_APP_LOCATION);
        return Collections.unmodifiableMap(map);
    }

    public Component createProjectCustomizerPanel() {
        return new WindowsMobileCustomizerPanel();
    }

    public Component createGlobalCustomizerPanel() {
        return new WindowsMobileCustomizerPanel();

    }
    
}
