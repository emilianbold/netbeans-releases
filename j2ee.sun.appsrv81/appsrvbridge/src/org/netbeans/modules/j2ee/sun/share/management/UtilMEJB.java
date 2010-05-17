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

/*
 * UtilMEJB.java
 *
 * Created on August 13, 2003, 12:31 PM
 */

package org.netbeans.modules.j2ee.sun.share.management;



import javax.management.MBeanServerConnection;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.management.InstanceNotFoundException;
import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import java.util.Set;
import java.util.Arrays;
import java.util.HashSet;
import java.rmi.RemoteException;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanInfo;
import javax.management.ObjectName;
import org.netbeans.modules.j2ee.sun.api.SunDeploymentManagerInterface;
import org.netbeans.modules.j2ee.sun.ide.controllers.ControllerUtil;

import org.netbeans.modules.j2ee.sun.ide.j2ee.mbmapping.Constants;
import org.netbeans.modules.j2ee.sun.util.AppserverConnectionFactory;

/**
 *
 * @author  nityad
 */
public class UtilMEJB implements Constants{
    
    private MBeanServerConnection conn = null;
    
    
    /** Creates a new instance of UtilMEJB */
    
    
    public UtilMEJB(SunDeploymentManagerInterface dm)  throws RemoteException{
        
        
        ClassLoader origClassLoader=Thread.currentThread().getContextClassLoader();
        try{
            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
            conn =  ControllerUtil.getMBeanServerConnWithInterceptor(
                        (SunDeploymentManagerInterface)dm,
                        AppserverConnectionFactory.getHTTPAppserverConnection(
                            dm.getHost(), dm.getPort(), dm.getUserName(),
                            dm.getPassword(), dm.isSecure()));
        }catch(Exception ex){
            throw new RemoteException(ex.getMessage(), ex.getCause());
        }
        
        finally{
            Thread.currentThread().setContextClassLoader(origClassLoader);
        }
    }
    
    
    protected MBeanServerConnection getConnection(){
        return this.conn;
    }
    
    protected AttributeList updateGetAttributes(ObjectName objName, String[] attributes){
        AttributeList attList = null;
        try{
            String isResource = objName.getKeyProperty("type"); //NOI18N
            Set configMods = new HashSet(Arrays.asList(CONFIG_MODULE));
            if((isResource != null) && (! configMods.contains(isResource))){
                attList = this.conn.getAttributes(objName, attributes);
            }
        }catch(Exception ex){
            //Empty list is returned.
        }
        return attList;
    }
    
    protected Object updateGetAttribute(ObjectName objName, String attribute){
        Object attr = null;
        try{
            String isResource = objName.getKeyProperty("type"); //NOI18N
            Set configMods = new HashSet(Arrays.asList(CONFIG_MODULE));
            if((isResource != null) && (! configMods.contains(isResource))){
                attr = this.conn.getAttribute(objName, attribute);
            }
            String type = objName.getKeyProperty("j2eeType"); //NOI18N
            if(type.equals("J2EEServer")) { //NOI18N
                attr = this.conn.getAttribute(objName, attribute);
            }
        }catch(Exception ex){
            //Null value is returned
        }
        return attr;
    }
    
    protected Object updateInvoke(ObjectName objName, String operationName, Object[] params, String[] signature) throws RemoteException{
        //If condition is satisfied only for createResource
        if(objName.toString().equals(MAP_RESOURCES))
            return invokeServerForResource(objName, operationName, params, signature);
        else
            return invokeServer(objName, operationName, params, signature);
    }
    
    private Object invokeServer(ObjectName objName, String operationName, Object[] params, String[] signature)  throws RemoteException{
        Object retVal = null;
        try{
            retVal = this.conn.invoke(objName, operationName, params, signature);
        }catch(Exception ex){
            //            if (!objName.toString().startsWith("ias:type=domain,category=config")){
            //                System.out.println(" Error in invokeServer " + ex.getMessage());
            //            }
            throw new RemoteException(ex.getMessage(), ex.getCause());
            
        }
        return retVal;
    }
    
    private Object invokeServerForResource(ObjectName objName, String operationName, Object[] params, String[] signature) throws RemoteException{
        Object retVal = null;
        try{
            if(operationName.equals("setProperty")){ //NOI18N
                this.conn.invoke(objName, operationName, params, signature);
            }else{
                retVal = this.conn.invoke(objName, operationName, params, signature);
            }
        }catch(Exception ex){
            throw new RemoteException(ex.getMessage(),ex.getCause());
        }
        return retVal;
    }
    
    protected MBeanInfo updateMBeanInfo(ObjectName objName){
        MBeanInfo bnInfo = null;
        try{
            String isResource = objName.getKeyProperty("type"); //NOI18N
            Set configMods = new HashSet(Arrays.asList(CONFIG_MODULE));
            if((isResource != null) && (! configMods.contains(isResource))){
                bnInfo = this.conn.getMBeanInfo(objName);
            }
        }catch(Exception ex){
            //Property sheet is not created
        }
        return bnInfo;
    }
    
    protected void updateSetAttribute(ObjectName objName, Attribute attribute) throws RemoteException, InstanceNotFoundException, AttributeNotFoundException,
            InvalidAttributeValueException, MBeanException, ReflectionException {
        try{
            String isResource = objName.getKeyProperty("type"); //NOI18N
            Set configMods = new HashSet(Arrays.asList(CONFIG_MODULE));
            if((isResource != null) && (! configMods.contains(isResource))){
                this.conn.setAttribute(objName, attribute);
            }
        }catch(java.io.IOException ex){
            throw new RemoteException(ex.getLocalizedMessage(), ex.getCause());
        }
    }
    
}


