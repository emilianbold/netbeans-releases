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

package org.netbeans.modules.j2ee.sun.bridge;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;


import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.status.ProgressObject;

// necessary imports from App Server Jars: they need to be there at runtime:
import com.sun.enterprise.deployment.client.DeploymentFacilityFactory;
import com.sun.enterprise.deployment.client.DeploymentFacility;
import com.sun.enterprise.deployment.client.ServerConnectionIdentifier;

import com.sun.enterprise.deployment.deploy.shared.Archive;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;




/**
 *
 * @author  Ludo Champenois
 * small class to invoke App Server APIS to really do the directory based deployment.
 * It has to be a separate class because of class loader closure issues if the app server jars
 * are not around wihtin the  IDE.
 */
public class DirectoryDeploymentFacility {
    
    private String host,  user,  passwd;
    private int port;
    private boolean secure;
    // class name that might differ between AS 8 and AS 9...
    final static private String FILEARCHIVEA81= "com.sun.enterprise.deployment.archivist.FileArchive"; //NOI18N
    final static private String FILEARCHIVEA9=  "com.sun.enterprise.deployment.deploy.shared.FileArchive";//NOI18N
    
    
    public DirectoryDeploymentFacility(String host, int port, String user, String passwd,boolean secure) {
        this.host =host;
        this.port =port;
        this.user =user;
        this.passwd =passwd;
        this.secure = secure;
    }
    
    
    /**
     * @param targetModuleID
     * @return a progress object representing the incrmental dpeloy action.
     */
    final public ProgressObject  incrementalDeploy(  final TargetModuleID tmid) {
        ProgressObject progressObject = null;
        File dirLocation = AppServerBridge.getDirLocation( tmid);        
      //      long tt = System.currentTimeMillis();
     //   ClassLoader origClassLoader = origClassLoader=Thread.currentThread().getContextClassLoader();
        try {
     //       Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
            
            Archive fa= getFileArchive(dirLocation);
            if (fa==null){
                IllegalStateException ise = new IllegalStateException("cannot find FileArchive class...");
                throw ise;
            }
            DeploymentFacility df = DeploymentFacilityFactory.getDeploymentFacility();
            ServerConnectionIdentifier sci = new ServerConnectionIdentifier();
            sci.setHostName(host);
            sci.setHostPort(port);
            sci.setUserName(user);
            sci.setPassword(passwd);
            sci.setSecure(secure);
            df.connect(sci);
            java.util.Properties deploymentOptions = new java.util.Properties();
            
            deploymentOptions.put("force","true");
            deploymentOptions.put("name", tmid.getModuleID());
            setContextRoot(deploymentOptions, tmid.getModuleID(), fa);
            System.out.println("moduleID="+tmid.getModuleID());
            deploymentOptions.put("archiveName", dirLocation.getAbsolutePath());
            Target[] targets =new Target[1];
            targets[0] =  (Target)tmid;
            
            
            progressObject = df.deploy( targets, fa,  null, deploymentOptions );
//            System.out.println("redeploy in="+(System.currentTimeMillis()-tt));
            
        } catch(Exception e) {
            e.printStackTrace();
            IllegalStateException ise = new IllegalStateException(e.getMessage());
            ise.initCause(e);
            throw ise;
            
        } //finally {
       //     Thread.currentThread().setContextClassLoader(origClassLoader);
            
      //  }
        return progressObject;
    }
    
    
    
    
    
    
    
    /**
     * First time deployment file distribution.
     * Before this method is called the files are copied into the target
     * folder provided by plugin.
     * @param target target of deployment
     * @param file the destination directory for the given deploy app
     * @return the object for feedback on progress of deployment
     */
    public ProgressObject initialDeploy(Target target,  File file,  String moduleID) {
        
        
        ProgressObject progressObject = null;
        try{
            Archive fa= getFileArchive(file);
            if (fa==null){
                IllegalStateException ise = new IllegalStateException("cannot find FileArchive class...");
                throw ise;
            }
            DeploymentFacility df = DeploymentFacilityFactory.getDeploymentFacility();
            ServerConnectionIdentifier sci = new ServerConnectionIdentifier();
            sci.setHostName(host);
            sci.setHostPort(port);
            sci.setUserName(user);
            sci.setPassword(passwd);
            sci.setSecure(secure);
            df.connect(sci);
            java.util.Properties deploymentOptions = new java.util.Properties();
            
            deploymentOptions.put("force","true");
            setContextRoot(deploymentOptions, moduleID, fa);
            System.out.println("moduleID="+moduleID);
            deploymentOptions.put("archiveName", file.getAbsolutePath());
            Target[] targets =new Target[1];
            targets[0] = target;
            
            progressObject = df.deploy( targets, fa,  null, deploymentOptions );
            
        } catch(Exception e) {
            e.printStackTrace();
            IllegalStateException ise = new IllegalStateException(e.getMessage());
            ise.initCause(e);
            throw ise;
        }
        return progressObject;
    }
    
    private static void setContextRoot(final java.util.Properties deploymentOptions, final String moduleID, final Archive fa) { // throws IOException {
        InputStream webXml = null;
        InputStream sunWebXml = null;
        try {
            webXml = fa.getEntry("WEB-INF/web.xml");
            sunWebXml = fa.getEntry("WEB-INF/sun-web.xml");
            
            deploymentOptions.put("name", moduleID);
            if (null != webXml) {
                if (null != sunWebXml) {
                    // parse sun-web for the context-root value
                    Document swx = loadSunWeb(sunWebXml);
                    if (null == swx) {
                        System.out.println("this should not happen here");
                        deploymentOptions.put("contextRoot", moduleID);
                    } else {
                        NodeList contextRootNodeList = swx.getElementsByTagName("context-root");
                        if (null == contextRootNodeList || contextRootNodeList.getLength() < 1) {
                            deploymentOptions.put("contextRoot", moduleID);
                        } else {
                            deploymentOptions.put("contextRoot",contextRootNodeList.item(0).getTextContent());
                        }
                    }
                } else {
                    deploymentOptions.put("contextRoot", moduleID);
                }
            }
        } catch (IOException ioe) {
            // do nothing here...
        } finally {
            if (null != webXml) {
                try {
                    webXml.close();
                } catch (IOException ioe) {
                }
            }
            if (null != sunWebXml) {
                try {
                    sunWebXml.close();
                } catch (IOException ioe) {
                }
            }
            
        }
    }
    
    private Archive getFileArchive(File file){
        try{
            Class fileArchiveClass;
            try {
                fileArchiveClass = this.getClass().getClassLoader().loadClass(FILEARCHIVEA81);
                java.util.logging.Logger.getLogger("javax.enterprise.system.tools.deployment").setLevel(java.util.logging.Level.SEVERE);
            } catch (ClassNotFoundException ex) {
                try {
                    fileArchiveClass = this.getClass().getClassLoader().loadClass(FILEARCHIVEA9);
                } catch (ClassNotFoundException ex2) {
                    ex2.printStackTrace();
                    return null;
                }
            }
            Object fa =fileArchiveClass.newInstance();
            
            java.lang.reflect.Method method =fileArchiveClass.getMethod("open", new Class[]{  String.class});//NOI18N
            method.invoke(fa, new Object[] {file.getAbsolutePath() });
            return (Archive)fa;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    
    static private Document loadSunWeb(InputStream is) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setValidating(false);
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            
            dBuilder.setEntityResolver(new EntityResolver() {
                public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                    StringReader reader = new StringReader("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"); // NOI18N
                    InputSource source = new InputSource(reader);
                    source.setPublicId(publicId);
                    source.setSystemId(systemId);
                    return source;
                }
            });
            
            return dBuilder.parse(is);
        } catch (Exception e) {
            return null;
        }
    }
}


