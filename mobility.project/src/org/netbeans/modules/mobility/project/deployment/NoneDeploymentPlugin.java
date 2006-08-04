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
 * NoneDeploymentPlugin.java
 *
 * Created on 24. rijen 2004, 17:10
 */
package org.netbeans.modules.mobility.project.deployment;

import java.awt.Component;
import java.util.Collections;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import org.openide.util.NbBundle;
import org.netbeans.spi.mobility.deployment.DeploymentPlugin;

/**
 *
 * @author Adam Sotona
 */
public class NoneDeploymentPlugin implements DeploymentPlugin {
        
    public String getAntScriptLocation() {
        return null;
    }
    
    public Component createCustomizerPanel() {
        final JLabel l = new JLabel(NbBundle.getMessage(NoneDeploymentPlugin.class, "LBL_NoneTypeDesciption")); //NOI18N
        l.setHorizontalAlignment(SwingConstants.CENTER);
        return l;
    }
    
    public String getDeploymentMethodName() {
        return "NONE"; //NOI18N
    }
    
    public String getDeploymentMethodDisplayName() {
        return NbBundle.getMessage(NoneDeploymentPlugin.class, "LBL_NoneTypeName"); //NOI18N
    }
    
    public Map getPropertyDefaultValues() {
        return Collections.EMPTY_MAP;
    }
    
}
