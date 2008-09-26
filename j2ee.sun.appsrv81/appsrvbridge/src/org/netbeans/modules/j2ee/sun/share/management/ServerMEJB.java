/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

/*
 * ServerMEJB.java
 *
 * Created on August 13, 2003, 12:14 PM
 */

package org.netbeans.modules.j2ee.sun.share.management;

import java.io.IOException;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.management.IntrospectionException;
import javax.management.InstanceNotFoundException;
import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.AttributeList;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.util.ResourceBundle;

import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.modules.j2ee.sun.api.SunDeploymentManagerInterface;

import org.netbeans.modules.j2ee.sun.api.ServerInterface;
import org.netbeans.modules.j2ee.sun.bridge.apis.AppserverMgmtController;
import org.netbeans.modules.j2ee.sun.ide.controllers.ControllerUtil;



/**
 *
 * @author  nityad
 */
public class ServerMEJB implements ServerInterface {
    
    /** Creates a new instance of ServerMEJB */
    private UtilMEJB ut = null;

    private DeploymentManager currentDM = null; 
    static final ResourceBundle bundle = ResourceBundle.getBundle("org.netbeans.modules.j2ee.sun.share.management.Bundle");// NOI18N
    
    public ServerMEJB( ) {

    }
    public ServerMEJB(DeploymentManager dm) {
        setDeploymentManager (dm);
    }
    public void setDeploymentManager(DeploymentManager dm){
        this.currentDM = dm;
        
    }
    public Object getAttribute(ObjectName name, String attribute) throws MBeanException, 
      AttributeNotFoundException, InstanceNotFoundException, ReflectionException, RemoteException{
         return this.getUT().updateGetAttribute(name, attribute);
    }
    
    public AttributeList getAttributes(ObjectName name, String[] attributes) throws
      ReflectionException, InstanceNotFoundException, RemoteException{
        return this.getUT().updateGetAttributes(name, attributes);
    }
   
    public javax.management.MBeanInfo getMBeanInfo(ObjectName name) throws IntrospectionException, InstanceNotFoundException,
      ReflectionException, RemoteException{
        return this.getUT().updateMBeanInfo(name);
    }
    
    public Object invoke(ObjectName name, String operationName, Object[] params, String[] signature) throws InstanceNotFoundException,
     MBeanException, ReflectionException, RemoteException{
        return this.getUT().updateInvoke(name, operationName, params, signature);
    }
    
    public void setAttribute(ObjectName name, javax.management.Attribute attribute) throws InstanceNotFoundException,
      AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException, RemoteException{
        this.getUT().updateSetAttribute(name, attribute);
   }
    
    public DeploymentManager getDeploymentManager(){
        return this.currentDM;
    }
    
    public MBeanServerConnection getMBeanServerConnection() throws RemoteException, ServerException {
        return this.getUT().getConnection();
    }
    
    public /*ServerMEJB*/Object getManagement(){
        return this;
    }
    
    private UtilMEJB getUT() throws RemoteException, ServerException{
        SunDeploymentManagerInterface sdmi = (SunDeploymentManagerInterface)currentDM;
            if (sdmi.isSuspended()){
                //System.out.println("CANNOT DO A getUT WHILE STOP IN A BREAK POINT IN DEBUG MODE...");
                throw new RemoteException(bundle.getString("MSG_ServerInDebug")) ;
            }
            if (this.ut!=null){

                return this.ut;
            }
        SunDeploymentManagerInterface dm=(SunDeploymentManagerInterface)currentDM;
        try{
            
                ut = new UtilMEJB(dm);
                ut.getConnection().getDefaultDomain();//test the connection. Might throw IOE for Uname/password


        }
        catch (IOException e){
            if(e.getMessage().contains ("500")){//internal error
                this.ut =null;
            }
        }
        catch (java.lang.NoClassDefFoundError ncf){//wrong dynamic classpath
                throw new RemoteException(bundle.getString("MSG_WrongInstallDir")) ;
        }
        return this.ut;
    }

    public String getWebModuleName(String contextRoot) {
        String name = null;
        SunDeploymentManagerInterface sunDm = (SunDeploymentManagerInterface)currentDM;
        if (sunDm.isSuspended()) {
            return null;
        }
        if (sunDm.isRunning(false)) {
            Thread holder = Thread.currentThread();
            if (sunDm.grabInnerDM(holder,true)) {
                try {
                    AppserverMgmtController controller = getController();
                    if(controller != null) {
                        name = controller.getWebModuleName(contextRoot);
                    }
                } finally {
                    sunDm.releaseInnerDM(holder);
                }
            }
        }
        return name;
    }

    private AppserverMgmtController getController(){
        AppserverMgmtController controller = null;       
        SunDeploymentManagerInterface sunDm = (SunDeploymentManagerInterface)currentDM;
        controller = ControllerUtil.getAppserverMgmtControllerFromDeployMgr(currentDM);
        return controller;
    }
    
    /* check if the dm is ok in term of user name and password,
     * throws an IOexception if this is incorrect
     * oterwise, returns normally
     **/
    public void checkCredentials() throws  IOException{
        SunDeploymentManagerInterface sdmi = (SunDeploymentManagerInterface)currentDM;
        if (sdmi.isSuspended()){
            throw new RemoteException(bundle.getString("MSG_ServerInDebug")) ;
        }        
        SunDeploymentManagerInterface dm=(SunDeploymentManagerInterface)currentDM;
        getUT().getConnection().getDefaultDomain();//test the connection. Might throw IOE for Uname/password
        
        return;
    }
    
}
