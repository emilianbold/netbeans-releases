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

package org.netbeans.modules.j2ee.deployment.config;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.ConfigurationFilesListener;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.impl.Server;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
import org.openide.filesystems.FileObject;

/**
 * @author nn136682
 */
public class ConfigFilesListener extends AbstractFilesListener {
    List consumers; //ConfigurationFileListener's
    
    public ConfigFilesListener(J2eeModuleProvider provider, List consumers) {
        super(provider);
        this.consumers = consumers;
    }
    
    protected File[] getTargetFiles() {
        //locate the root to listen to
        Collection servers = ServerRegistry.getInstance().getServers();
        ArrayList result = new ArrayList();
        for (Iterator i=servers.iterator(); i.hasNext();) {
            Server s = (Server) i.next();
            String[] paths = s.getDeploymentPlanFiles(provider.getJ2eeModule().getModuleType());
            if (paths == null)
                continue;
            
            for (int j = 0; j < paths.length; j++) {
                File f = provider.getDeploymentConfigurationFile(paths[j]);
                if (f != null)
                    result.add(f);
            }
        }
        return (File[]) result.toArray(new File[result.size()]);
    }

    protected void targetDeleted(FileObject deleted) {
        fireConfigurationFilesChanged(false, deleted);
    }
    
    protected void targetCreated(FileObject added) {
        fireConfigurationFilesChanged(true, added);
    }
    
    protected void targetChanged(FileObject deleted) {
    }
    
    private void fireConfigurationFilesChanged(boolean added, FileObject fo) {
        for (Iterator i=consumers.iterator(); i.hasNext();) {
            ConfigurationFilesListener cfl = (ConfigurationFilesListener) i.next();
            if (added) {
                cfl.fileCreated(fo);
            } else {
                cfl.fileDeleted(fo);
            }
        }
    }

    protected boolean isTarget(FileObject fo) {
        return isTarget(fo.getNameExt());
    }
    protected boolean isTarget(String fileName) {
        return ServerRegistry.getInstance().isConfigFileName(fileName, provider.getJ2eeModule().getModuleType());
    }
}
