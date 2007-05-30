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
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import org.netbeans.modules.j2ee.deployment.common.api.SourceFileMap;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.sun.dd.api.DDProvider;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.*;
import org.netbeans.modules.j2ee.sun.api.ServerInterface;
import org.netbeans.modules.j2ee.sun.api.SunDeploymentManagerInterface;
import org.netbeans.modules.j2ee.sun.ide.sunresources.beans.ResourceUtils;
import org.openide.ErrorManager;

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
    private Utils() {
    }
    
    /*
     * This create a temporary file, deleted at exit, that contains
     * the necessary password infos for starting or creating a domain
     * bot admu and master password are there.
     * @returns the temporary file
     * or null if for some reason, this file cannot be created.
     */
    public static File createTempPasswordFile(String password, String masterPassword){
        OutputStream output;
        PrintWriter p =null;
        File retVal = null;
        try {
            retVal = File.createTempFile("admin",null);//NOI18N
            retVal.deleteOnExit();
            output = new FileOutputStream(retVal);
            p = new PrintWriter(output);
            p.println("AS_ADMIN_ADMINPASSWORD="+ password);//NOI18N for create domains
            p.println("AS_ADMIN_PASSWORD="+ password);//NOI18N for start domains
            p.println("AS_ADMIN_MASTERPASSWORD="+ masterPassword);//NOI18N
            //retVal = file;
        } catch(IOException e) {
            // this should not happen... If it does we should at least log it
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,e);
        } finally {
            if (p!=null) {
                p.close();
            }
        }
        return retVal;
    }
    
    
    public static Object getResource(java.io.File primaryFile) {
        Resources resources = getResourceGraph(primaryFile);
        Object retVal = null;
        // identify JDBC Connection Pool xml
        JdbcConnectionPool[] pools = resources.getJdbcConnectionPool();
        if(pools.length != 0){
            retVal = pools[0];
        }
        // identify JDBC Resources xml
        JdbcResource[] dataSources = resources.getJdbcResource();
        if(null == retVal && dataSources.length != 0){
            retVal = dataSources[0];
        }
        return retVal;
    }
    
    public static String getResourceType(Resources resources) {
        // import Persistence Manager Factory Resources
        String retVal = null;
        PersistenceManagerFactoryResource[] pmfResources = resources.getPersistenceManagerFactoryResource();
        if (pmfResources.length != 0){
            retVal = "persistence-manager-factory-resource";
        }
        // import Mail Resources
        MailResource[] mailResources = resources.getMailResource();
        if (null == retVal && mailResources.length != 0){
            retVal = "mail-resource";
        }
        // import Connector/ Connector Connection Pools/ Admin Object Resources
        ConnectorResource[] connResources = resources.getConnectorResource();
        ConnectorConnectionPool[] connPoolResources = resources.getConnectorConnectionPool();
        AdminObjectResource[] admObjResources = resources.getAdminObjectResource();
        if (null == retVal &&
                (admObjResources.length != 0 ||
                (connPoolResources.length != 0 && connResources.length != 0))){
            retVal = "jms-resource";
        }
        return retVal;
    }
    
    public static void setTopManagerStatus(String msg){
        org.openide.awt.StatusDisplayer.getDefault().setStatusText(msg);
    }
    
    
    public static File[] getResourceDirs(J2eeModule module) {
        File retVal[] = EMPTY_FILE_LIST;
        SourceFileMap sourceFileMap = SourceFileMap.findSourceMap(module); //deployableObject);
        if (sourceFileMap != null) {
            retVal = sourceFileMap.getEnterpriseResourceDirs();
        }
        return retVal;
    }
    
    
    public static File[] getResourceDirs(File file){
        File retVal[] = EMPTY_FILE_LIST;
        FileObject fo = FileUtil.toFileObject(file);
        SourceFileMap sourceFileMap = SourceFileMap.findSourceMap(fo);
        if (sourceFileMap != null) {
            retVal = sourceFileMap.getEnterpriseResourceDirs();
        }
        return retVal;
    }
    
    
    public static void registerResources(java.io.File[] resourceDirs, ServerInterface mejb){
        System.out.println(bundle.getString("Msg_ProjResRegisterStart")); //NOI18N
        try{
            SunDeploymentManagerInterface sunDm = (SunDeploymentManagerInterface)mejb.getDeploymentManager();
            for (int j=0; j<resourceDirs.length; j++){
                File resourceDir = resourceDirs[j];
                File[] resources = null;
                //Temporary flag while moving to single sun-resource file
                //TODO -- remove/clean up
                if (System.getProperties().getProperty("resource-redesign") != null){
                    if(resourceDir != null){
                        resources = resourceDir.listFiles(new newResourceFileFilter());
                    }
                    if(resources != null){
                        registerSunResources(mejb, resources);
                    }
                }else{
                    if(resourceDir != null){
                        resources = resourceDir.listFiles(new ResourceFileFilter());
                    }
                    
                    if(resources != null){
                        registerConnectionPools(mejb, resources);
                        registerDatasources(mejb, resources);
                        registerOtherResources(resources, sunDm);//Remaining Resources
                    }
                }
            }
        }catch(Exception ex){
            String errorMsg = MessageFormat.format(bundle.getString( "Msg_RegFailure"), new Object[]{ex.getLocalizedMessage()}); //NOI18N
            System.out.println(errorMsg);
        }
        System.out.println(bundle.getString("Msg_ProjResRegisterFinish")); //NOI18N
    }
    
    private static void registerSunResources(final ServerInterface mejb, final File[] resources) throws Exception {
        for(int i=0; i<resources.length; i++ ){
            File resource = resources[i];
            if((resource != null) && (!resource.isDirectory())){
                Utils.registerIndvResources(mejb, resource);
            }
        }
    }
    
    public static void registerIndvResources(ServerInterface mejb, java.io.File primaryFile) {
        Resources resources = getResourceGraph(primaryFile);
        Object retVal = null;
        // identify JDBC Connection Pools
        JdbcConnectionPool[] pools = resources.getJdbcConnectionPool();
        for(int i=0; i<pools.length; i++){           
            JdbcConnectionPool connectionPoolBean = pools[i];
            ResourceUtils.register(connectionPoolBean, mejb, true);
        }
        // identify JDBC Resources
        JdbcResource[] dataSources = resources.getJdbcResource();
        for(int i=0; i<dataSources.length; i++){           
            JdbcResource datasourceBean = dataSources[i];
            try{
                ResourceUtils.register(datasourceBean, mejb, true);
            }catch(Exception ex){
                String errorMsg = MessageFormat.format(bundle.getString( "Msg_RegFailure"), new Object[]{ex.getLocalizedMessage()}); //NOI18N
                System.out.println(errorMsg);
            }    
        }
        
        // identify Mail Resources
        MailResource[] mailResources = resources.getMailResource();
        for(int i=0; i<mailResources.length; i++){           
            MailResource mailBean = mailResources[i];
            ResourceUtils.register(mailBean, mejb, true);
        }
        
        // identify JMS Resources
        JmsResource[] jmsResources = resources.getJmsResource();
        for(int i=0; i<jmsResources.length; i++){           
            JmsResource jmsBean = jmsResources[i];
            ResourceUtils.register(jmsBean, mejb, true);
        }
    }
    
    private static void registerConnectionPools(final ServerInterface mejb, final File[] resources) throws Exception {
        //Register All Connection Pools First
        for(int i=0; i<resources.length; i++ ){
            File resource = resources[i];
            if((resource != null) && (!resource.isDirectory())){
                Object sunResource = Utils.getResource(resource);
                if(sunResource != null && sunResource instanceof JdbcConnectionPool){
                    JdbcConnectionPool connectionPoolBean =(JdbcConnectionPool)sunResource;
                    ResourceUtils.register(connectionPoolBean, mejb, true);
                }
            }
        }
    }
    
    private static void registerDatasources(final ServerInterface mejb, final File[] resources) throws Exception {
        //Register All DataSources
        for(int i=0; i<resources.length; i++ ){
            File resource = resources[i];
            if((resource != null) && (!resource.isDirectory())){
                Object sunResource = Utils.getResource(resource);
                if(sunResource != null && sunResource instanceof JdbcResource){
                    JdbcResource datasourceBean = (JdbcResource)sunResource;
                    ResourceUtils.register(datasourceBean, mejb, true);
                }
            }
        }
    }
        
    private static void registerOtherResources(final File[] resources, final SunDeploymentManagerInterface sunDm) throws Exception {
        //Register All Remaining Resources
        for(int i=0; i<resources.length; i++ ){
            File resource = resources[i];
            if((resource != null) && (!resource.isDirectory())){
                Resources res = getResourceGraph(resource);
                String resourceType = Utils.getResourceType(res);
                if(resourceType != null){
                    ResourceUtils.register(res, sunDm, true, resourceType);
                }
            }
        }
    }
    
    public static class ResourceFileFilter implements FileFilter {
        public boolean accept(File f) {
            return f.isDirectory() ||
                    f.getName().toLowerCase(Locale.getDefault()).endsWith(".sun-resource"); //NOI18N
        }
    }
    
    public static class newResourceFileFilter implements FileFilter {
        public boolean accept(File f) {
            return f.isDirectory() ||
                    f.getName().toLowerCase(Locale.getDefault()).endsWith(".xml"); //NOI18N
        }
    }
    
    static Resources getResourceGraph(java.io.File primaryFile) {
        Resources resources = null;
        try {
            if (primaryFile.exists() && !primaryFile.isDirectory()) {
                FileInputStream in = new FileInputStream(primaryFile);
                
                resources = DDProvider.getDefault().getResourcesGraph(in);
            }
        } catch (IOException ex) {
            // the primary file should not be null
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,ex);
        }
        return resources;
    }
    
    
}
