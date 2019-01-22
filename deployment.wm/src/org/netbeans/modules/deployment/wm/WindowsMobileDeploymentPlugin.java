/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
 * 
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.mobility.deployment.DeploymentPlugin.class, position=105)
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
        HashMap<String, Object> map = new HashMap<String, Object>(5);
        map.put(PROP_APP_LOCATION, DEFAULT_APP_LOCATION);
        return Collections.unmodifiableMap(map);
    }

    public Map<String, Object> getGlobalPropertyDefaultValues() {
        return Collections.emptyMap();
    }

    public Component createProjectCustomizerPanel() {
        return new WindowsMobileCustomizerPanel();
    }

    public Component createGlobalCustomizerPanel() {
        return null;
    }
    
}
