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

package org.netbeans.modules.j2ee.sun.ide.j2ee;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.plugins.spi.AntDeploymentProvider;
import org.netbeans.modules.j2ee.sun.api.ServerLocationManager;
import org.netbeans.modules.j2ee.sun.api.SunDeploymentManagerInterface;
import org.netbeans.modules.j2ee.sun.ide.dm.SunDeploymentManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * @thief vkraemer
 * @author sherold
 */
public class AntDeploymentProviderImpl implements AntDeploymentProvider {
    
    private SunDeploymentManager dm;
    
    public AntDeploymentProviderImpl(DeploymentManager dm) {
        this.dm = (SunDeploymentManager)dm;
    }

    public void writeDeploymentScript(OutputStream os, Object moduleType) throws IOException {
        InputStream is;
        boolean addExtension = false;
        SunDeploymentManagerInterface sdmi = 
                (SunDeploymentManagerInterface) dm;
        is = AntDeploymentProviderImpl.class.getResourceAsStream("ant-deploy.xml"); // NOI18N
        if (ServerLocationManager.getAppServerPlatformVersion(sdmi.getPlatformRoot()) >= ServerLocationManager.GF_V2) {
            addExtension = true;
        }
            
        try {
            FileUtil.copy(is, os);
        } finally {
            is.close();
        }
        
        try {
            Project proj = getProjectFromOutputStream(os);
            // more great naming conventions...
            String target = ModuleType.EAR.equals(moduleType) ? "pre-dist" : "-pre-dist";
            if (addExtension) {
                BuildExtension.copyTemplate(proj);
                BuildExtension.extendBuildXml(proj,target);
            } else {
                BuildExtension.abbreviateBuildXml(proj,target);
            }
        } catch (IOException ioe) {
                Logger.getLogger(AntDeploymentProviderImpl.class.getName()).log(Level.INFO, null,ioe);      //NOI18N
            throw ioe;
                
            }
        }
    

    public File getDeploymentPropertiesFile() {
        File file = dm.getAntDeploymentPropertiesFile();
        if (!file.exists()) {
            // generate the deployment properties file only if it does not exist
            try {
                dm.storeAntDeploymentProperties(file, true);
            } catch (IOException ioe) {
                Logger.getLogger(AntDeploymentProviderImpl.class.getName()).log(Level.INFO, null,ioe);      //NOI18N
            }
        }
        return file;
    }
    
    private Project getProjectFromOutputStream(OutputStream os) {        
        Project p = null;
        try {
            Class osClass = os.getClass();
            Field field = osClass.getDeclaredField("val$f");
            System.out.println("field = " + field);
            if(field != null) {
                field.setAccessible(true);
                Object obj = field.get(os);
                if(obj instanceof File) {
                    File f = (File) obj;
                    System.out.println("F = " + f.getPath());
                    FileObject fo = FileUtil.toFileObject(f);
                    if(fo != null) {
                        p = FileOwnerQuery.getOwner(fo);
                    }
                }
            }
            System.out.println("Done.");
        } catch(Exception ex) {
                Logger.getLogger(AntDeploymentProviderImpl.class.getName()).log(Level.INFO, null,ex);      //NOI18N
        }
        return p;
    }
    

}
