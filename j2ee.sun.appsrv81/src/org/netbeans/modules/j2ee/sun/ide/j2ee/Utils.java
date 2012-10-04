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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.j2ee.sun.ide.j2ee;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.modules.j2ee.deployment.common.api.SourceFileMap;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.sun.dd.api.DDProvider;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.*;
import org.netbeans.modules.j2ee.sun.api.ServerInterface;
import org.netbeans.modules.j2ee.sun.api.SunDeploymentManagerInterface;
import org.netbeans.modules.j2ee.sun.api.restricted.RegistrationUtils;
import org.netbeans.modules.j2ee.sun.api.restricted.ResourceUtils;
import org.openide.ErrorManager;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.xml.sax.SAXException;

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
        SunDeploymentManagerInterface sunDm = (SunDeploymentManagerInterface) mejb.getDeploymentManager();
// TODO : re-enable this when server team resolves 3317
//        if (sunDm.getAppserverVersion() != ServerLocationManager.GF_V2) {
            System.out.println(bundle.getString("Msg_ProjResRegisterStart")); //NOI18N
            try {
                for (int j = 0; j < resourceDirs.length; j++) {
                    File resourceDir = resourceDirs[j];
                    File[] resources = null;
                    if (resourceDir != null) {
                        resources = resourceDir.listFiles(new SunResourceFileFilter());
                    }
                    if (resources != null) {
                        registerSunResources(mejb, resources);
                    }
                }
            } catch (Exception ex) {
                String errorMsg = MessageFormat.format(bundle.getString("Msg_RegFailure"), new Object[]{ex.getLocalizedMessage()}); //NOI18N
                System.out.println(errorMsg);
            }
            System.out.println(bundle.getString("Msg_ProjResRegisterFinish")); //NOI18N
//        }else {
//            System.out.println(bundle.getString("Msg_PrepResourcesForDeploy")); //NOI18N
//            for (int j = 0; j < resourceDirs.length; j++) {
//                File resourceDir = resourceDirs[j];
//                File[] resources = null;
//                if (resourceDir != null) {
//                    resources = resourceDir.listFiles(new SunResourceFileFilter());
//                }
//                if (resources != null) {
//                    prepareSunResources(mejb, resources);
//                }
//            }
//        }
    }
    
    private static void registerSunResources(final ServerInterface mejb, final File[] resources) throws Exception {
        for(int i=0; i<resources.length; i++ ){
            File resource = resources[i];
            if((resource != null) && (!resource.isDirectory())){
                Utils.registerIndvResources(mejb, resource);
            }
        }
    }
    
    private static void prepareSunResources(final ServerInterface mejb, final File[] resources) {
        for(int i=0; i<resources.length; i++ ){
            File resource = resources[i];
            if((resource != null) && (!resource.isDirectory())){
                RegistrationUtils.checkUpdateServerResources(mejb, resource);
            }
        }
    }
    
    public static void registerIndvResources(ServerInterface mejb, java.io.File primaryFile) throws Exception {
        Resources resources = getResourceGraph(primaryFile);
        Object retVal = null;
        
        JdbcConnectionPool[] pools = resources.getJdbcConnectionPool();
        for(int i=0; i<pools.length; i++){           
            JdbcConnectionPool connectionPoolBean = pools[i];
            ResourceUtils.register(connectionPoolBean, mejb, true);
        }
        
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
        
        MailResource[] mailResources = resources.getMailResource();
        for(int i=0; i<mailResources.length; i++){           
            MailResource mailBean = mailResources[i];
            ResourceUtils.register(mailBean, mejb, true);
        }
        
        AdminObjectResource[] aoResources = resources.getAdminObjectResource();
        for(int i=0; i<aoResources.length; i++){           
            AdminObjectResource aoBean = aoResources[i];
            ResourceUtils.register(aoBean, mejb, true);
        }
        
        ConnectorConnectionPool[] connPoolResources = resources.getConnectorConnectionPool();
        for(int i=0; i<connPoolResources.length; i++){           
            ConnectorConnectionPool connPoolBean = connPoolResources[i];
            ResourceUtils.register(connPoolBean, mejb, true);
        }
        
        ConnectorResource[] connResources = resources.getConnectorResource();
        for(int i=0; i<connResources.length; i++){           
            ConnectorResource connBean = connResources[i];
            ResourceUtils.register(connBean, mejb, true);
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
    
    public static class SunResourceFileFilter implements FileFilter {
        @Override
        public boolean accept(File f) {
            return ((! f.isDirectory()) && (f.getName().equals("sun-resources.xml") || f.getName().equals("glassfish-resources.xml"))); //NOI18N
        }
    }
    
    static Resources getResourceGraph(java.io.File primaryFile) {
        Resources resources = null;
        try {
            if (primaryFile.exists() && !primaryFile.isDirectory()) {
                FileInputStream in = new FileInputStream(primaryFile);
                try {
                    resources = DDProvider.getDefault().getResourcesGraph(in);
                } finally {
                    in.close();
                }
            }
        } catch (SAXException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,ex);
        } catch (IOException ex) {
            // the primary file should not be null
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,ex);
        }
        return resources;
    }
    
    /**
     * A canWrite test that may tell the truth on Windows.
     * 
     * This is a work around for http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4420020
     * @param f the file or directory to test
     * @return true when the file is writable...
     */
    public static boolean canWrite(File f) {
        if (org.openide.util.Utilities.isWindows()) {
            // File.canWrite() has lots of bogus return cases associated with 
            // read-only directories and files... 
            boolean retVal = true;
            java.io.File tmp = null;
            if (!f.exists()) {
                retVal = false;
            } else if (f.isDirectory()) {
                try             {
                    tmp = java.io.File.createTempFile("foo", ".tmp", f);
                }
                catch (IOException ex) {
                    // I hate using exceptions for flow of control
                    retVal = false;
                } finally {
                    if (null != tmp) {
                        tmp.delete();
                    }
                }
            } else {
                java.io.FileOutputStream fos = null;
                try {
                    fos = new java.io.FileOutputStream(f, true);
                }
                catch (FileNotFoundException ex) {
                    // I hate using exceptions for flow of control
                    retVal = false;
                } finally {
                    if (null != fos) {
                        try {
                            fos.close();
                        } catch (java.io.IOException ioe) {
                            Logger.getLogger(Utils.class.getName()).log(Level.FINEST,
                                    null, ioe);
                        }
                    }
                }                
            }
            return retVal;
        } else {
            // we can actually trust the canWrite() implementation...
            return f.canWrite();
        }
    }
}
