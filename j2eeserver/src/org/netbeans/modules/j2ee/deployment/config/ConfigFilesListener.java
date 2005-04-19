/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
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
