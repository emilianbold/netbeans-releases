package org.netbeans.modules.j2ee.sun.ide.j2ee;
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
 * Utils.java
 *
 * Created on December 5, 2004, 6:33 PM
 */

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.text.MessageFormat;
import java.util.Properties;
import java.util.ResourceBundle;

import java.util.Map;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.ObjectName;

import org.netbeans.modules.j2ee.deployment.common.api.SourceFileMap;
import org.netbeans.modules.j2ee.sun.dd.api.DDProvider;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.*;
import org.netbeans.modules.j2ee.sun.api.ServerInterface;
import org.netbeans.modules.j2ee.sun.ide.sunresources.beans.ResourceUtils;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author  Rajeshwar Patil
 */
public class Utils {

    static final ResourceBundle bundle = ResourceBundle.getBundle("org.netbeans.modules.j2ee.sun.ide.j2ee.Bundle");// NOI18N
    static final File[] EMPTY_FILE_LIST = new File[0];

    /** Creates a new instance of Utils */
    public Utils() {
    }


    public static Object getResource(java.io.File primaryFile) {
       try {
            if((! primaryFile.isDirectory())/* && primaryFile.isValid()*/){
                FileInputStream in = new FileInputStream(primaryFile);
                
                Resources resources = DDProvider.getDefault().getResourcesGraph(in);
                
                // identify JDBC Connection Pool xml
                JdbcConnectionPool[] pools = resources.getJdbcConnectionPool();
                if(pools.length != 0){
                    return pools[0];
                }  
                // identify JDBC Resources xml
                JdbcResource[] dataSources = resources.getJdbcResource();
                if(dataSources.length != 0){
                    return dataSources[0];
                }
                
                // import Persistence Manager Factory Resources
                PersistenceManagerFactoryResource[] pmfResources = resources.getPersistenceManagerFactoryResource();
                if(pmfResources.length != 0){
                    return pmfResources[0];
                }
                // import Mail Resources
                MailResource[] mailResources = resources.getMailResource();
                if(mailResources.length != 0){
                    return mailResources[0];
                }
                // import Java Message Service Resources
                JmsResource[] jmsResources = resources.getJmsResource();
                if(jmsResources.length != 0){
                    return jmsResources[0];
                }
            }
        }catch(Exception exception){
            System.out.println("Error while resource creation  ");
            return null;
        }
         return null;
    }

    public static void setTopManagerStatus(String msg){
        org.openide.awt.StatusDisplayer.getDefault().setStatusText(msg);
    }


    public static File[] getResourceDirs(javax.enterprise.deploy.model.DeployableObject deployableObject){
        try{
            SourceFileMap sourceFileMap = SourceFileMap.findSourceMap(deployableObject);
            if (sourceFileMap != null)
                return sourceFileMap.getEnterpriseResourceDirs();
            }catch(Exception exception){
                System.out.println(exception.getMessage());
        }
        return EMPTY_FILE_LIST;
    }


    public static File[] getResourceDirs(File file){
      try{
             FileObject fo = FileUtil.toFileObject(file);
             SourceFileMap sourceFileMap = SourceFileMap.findSourceMap(fo);
             if (sourceFileMap != null)
                 return sourceFileMap.getEnterpriseResourceDirs();
             }catch(Exception exception){
                 System.out.println(exception.getMessage());
         }
         return EMPTY_FILE_LIST;
    }     


    public static void registerResources(java.io.File[] resourceDirs, ServerInterface mejb){
        System.out.println(bundle.getString("Msg_ProjResRegisterStart")); //NOI18N
        try{
            for (int j=0; j<resourceDirs.length; j++){
                File resourceDir = resourceDirs[j];
                File[] resources = null;
                
                if(resourceDir != null){
                    resources = resourceDir.listFiles(new ResourceFileFilter());
                }
                
                File resource = null;
                Object sunResource = null;
                if(resources != null){
                    //Register All Connection Pools First
                    for(int i=0; i<resources.length; i++ ){
                        resource = resources[i];
                        if((resource != null) && (!resource.isDirectory())){
                            sunResource = Utils.getResource(resource);
                            if(sunResource != null && sunResource instanceof JdbcConnectionPool){
                                JdbcConnectionPool connectionPoolBean =(JdbcConnectionPool)sunResource;
                                ResourceUtils.register(connectionPoolBean, mejb, true);
                            }
                        }
                    }//Connection Pools
                    
                    //Register All DataSources
                    for(int i=0; i<resources.length; i++ ){
                        resource = resources[i];
                        if((resource != null) && (!resource.isDirectory())){
                            sunResource = Utils.getResource(resource);
                            if(sunResource != null && sunResource instanceof JdbcResource){
                                JdbcResource datasourceBean = (JdbcResource)sunResource;
                                ResourceUtils.register(datasourceBean, mejb, true);
                            }
                        }
                    }//DataSources
                    
                    //Register All Remaining Resources
                    for(int i=0; i<resources.length; i++ ){
                        resource = resources[i];
                        if((resource != null) && (!resource.isDirectory())){
                            sunResource = Utils.getResource(resource);
                            if(sunResource != null){
                                if(sunResource instanceof PersistenceManagerFactoryResource){
                                    PersistenceManagerFactoryResource pmBean = (PersistenceManagerFactoryResource)sunResource;
                                    ResourceUtils.register(pmBean, mejb, true); 
                                } else if(sunResource instanceof MailResource){
                                    MailResource jmBean = (MailResource)sunResource;
                                    ResourceUtils.register(jmBean, mejb, true); 
                                } else if(sunResource instanceof JmsResource){
                                    JmsResource jmsBean = (JmsResource)sunResource;
                                    ResourceUtils.register(jmsBean, mejb, true);
                                }
                            }
                        }
                    }//Remaining Resources
                }
            }
        }catch(Exception ex){
            String errorMsg = MessageFormat.format(bundle.getString( "Msg_RegFailure"), new Object[]{ex.getLocalizedMessage()}); //NOI18N
            System.out.println(errorMsg);
        }
        System.out.println(bundle.getString("Msg_ProjResRegisterFinish")); //NOI18N
    }
  

    public static class ResourceFileFilter implements FileFilter {
        public boolean accept(File f) {
            return f.isDirectory() || f.getName().toLowerCase().endsWith(".sun-resource"); //NOI18N
        }
    }

}
