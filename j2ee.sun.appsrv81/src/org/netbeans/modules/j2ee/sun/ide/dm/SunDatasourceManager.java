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

package org.netbeans.modules.j2ee.sun.ide.dm;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.DatasourceAlreadyExistsException;
import org.netbeans.modules.j2ee.deployment.plugins.spi.DatasourceManager;
import org.netbeans.modules.j2ee.sun.ide.j2ee.Utils;
import org.netbeans.modules.j2ee.sun.share.serverresources.SunDatasource;

/**
 *
 * @author Nitya Doraisamy
 */
public class SunDatasourceManager implements DatasourceManager {
    
    private DeploymentManager dm;
    private SunDeploymentManager sunDm;
    
    /**
     * Creates a new instance of SunDatasourceManager
     */
    public SunDatasourceManager(DeploymentManager dm) {
        this.dm = dm;
        this.sunDm = (SunDeploymentManager)this.dm;
    }
    
    public Set getDatasources() {
        return this.sunDm.getResourceConfigurator().getServerDataSources();
    }
    
    public void deployDatasources(Set datasources) throws ConfigurationException,
            DatasourceAlreadyExistsException {
        Object[] dsources = (Object[])datasources.toArray();
        List dirs = new ArrayList();
        for(int i=0; i<dsources.length; i++){
            SunDatasource ds = (SunDatasource)dsources[i];
            dirs.add(ds.getResourceDir());
        }
        
        if(! dirs.isEmpty()){
            File[] resourceDirs = (File[])dirs.toArray(new File[dirs.size()]);
            if(resourceDirs != null){
                Utils.registerResources(resourceDirs, this.sunDm.getManagement());
            }
        }
    }
    
}
