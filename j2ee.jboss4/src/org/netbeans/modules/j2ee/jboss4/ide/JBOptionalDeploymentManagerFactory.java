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
package org.netbeans.modules.j2ee.jboss4.ide;

import org.netbeans.modules.j2ee.deployment.plugins.spi.MessageDestinationDeployment;
import org.netbeans.modules.j2ee.jboss4.JBDeploymentManager;
import org.netbeans.modules.j2ee.deployment.plugins.spi.DatasourceManager;
import org.netbeans.modules.j2ee.jboss4.config.JBossDatasourceManager;
import org.netbeans.modules.j2ee.jboss4.ide.ui.JBInstantiatingIterator;
import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.modules.j2ee.deployment.plugins.spi.JDBCDriverDeployer;
import org.netbeans.modules.j2ee.deployment.plugins.spi.FindJSPServlet;
import org.netbeans.modules.j2ee.deployment.plugins.spi.IncrementalDeployment;
import org.netbeans.modules.j2ee.deployment.plugins.spi.OptionalDeploymentManagerFactory;
import org.netbeans.modules.j2ee.deployment.plugins.spi.StartServer;
import org.netbeans.modules.j2ee.jboss4.config.mdb.JBossMessageDestinationDeployment;
import org.openide.WizardDescriptor.InstantiatingIterator;

/**
 *
 * @author Martin Adamek
 */
public class JBOptionalDeploymentManagerFactory extends OptionalDeploymentManagerFactory {
    
    public StartServer getStartServer(DeploymentManager dm) {
        return new JBStartServer(dm);
    }

    public IncrementalDeployment getIncrementalDeployment(DeploymentManager dm) {
        return null;
    }

    public FindJSPServlet getFindJSPServlet(DeploymentManager dm) {
        return new JBFindJSPServlet((JBDeploymentManager)dm);
    }

    public InstantiatingIterator getAddInstanceIterator() {
        return new JBInstantiatingIterator();
    }
    
    public DatasourceManager getDatasourceManager(DeploymentManager dm) {
        
        if (!(dm instanceof JBDeploymentManager))
            throw new IllegalArgumentException("");

        String serverUrl = ((JBDeploymentManager)dm).getUrl();
        JBossDatasourceManager dsMgr = new JBossDatasourceManager(serverUrl);
        
        return dsMgr;
    }

    public MessageDestinationDeployment getMessageDestinationDeployment(DeploymentManager dm) {
        if (!(dm instanceof JBDeploymentManager)) {
            throw new IllegalArgumentException("");
        }

        return new JBossMessageDestinationDeployment(((JBDeploymentManager)dm).getUrl());
    }
    
     public JDBCDriverDeployer getJDBCDriverDeployer(DeploymentManager dm) {
         return new JBDriverDeployer((JBDeploymentManager) dm);
     }
    
}
