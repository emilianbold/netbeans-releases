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
 * JMSBean.java
 *
 * Created on November 13, 2003, 3:05 PM
 */

package org.netbeans.modules.j2ee.sun.ide.sunresources.beans;

import java.util.Vector;
import org.netbeans.modules.j2ee.sun.ide.editors.NameValuePair;
import org.netbeans.modules.j2ee.sun.share.serverresources.JavaMsgServiceResource;
import org.netbeans.modules.j2ee.sun.dd.api.DDProvider;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.Resources;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.ConnectorResource;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.AdminObjectResource;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.ConnectorConnectionPool;

/**
 *
 * @author  nityad
 */
public class JMSBean extends JavaMsgServiceResource implements java.io.Serializable{

    /** Creates a new instance of JMSBean */
    public JMSBean() {
    }
    
    public String getName() {
        return super.getName();
    }
    
    public static JMSBean createBean(Resources resources) {
        JMSBean bean = new JMSBean();
        //name attribute in bean is for studio display purpose. 
        //It is not part of the resource dtd.
        ConnectorResource connresource = resources.getConnectorResource(0);
        bean.setName(connresource.getJndiName());
        bean.setDescription(connresource.getDescription());
        bean.setIsEnabled(connresource.getEnabled());
        bean.setJndiName(connresource.getJndiName());
        bean.setPoolName(connresource.getPoolName());
        
        ConnectorConnectionPool connpoolresource = resources.getConnectorConnectionPool(0);
        bean.setResAdapter(connpoolresource.getResourceAdapterName());
        bean.setResType(connpoolresource.getConnectionDefinitionName());
         
        PropertyElement[] extraProperties = connpoolresource.getPropertyElement();
        Vector vec = new Vector();       
        for (int i = 0; i < extraProperties.length; i++) {
            NameValuePair pair = new NameValuePair();
            pair.setParamName(extraProperties[i].getName());
            pair.setParamValue(extraProperties[i].getValue());
            vec.add(pair);
        }
        
        if (vec != null && vec.size() > 0) {
            NameValuePair[] props = new NameValuePair[vec.size()];
            bean.setExtraParams((NameValuePair[])vec.toArray(props));
        }    
        
        return bean;
    }
     
    public static JMSBean createBean(AdminObjectResource aoresource) {
        JMSBean bean = new JMSBean();
        
        //name attribute in bean is for studio display purpose. 
        //It is not part of the resource dtd. 
        bean.setName(aoresource.getJndiName());
        bean.setDescription(aoresource.getDescription());
        bean.setIsEnabled(aoresource.getEnabled());
        bean.setJndiName(aoresource.getJndiName());
        bean.setResAdapter(aoresource.getResAdapter());
        bean.setResType(aoresource.getResType());
        
        PropertyElement[] extraProperties = aoresource.getPropertyElement();
        Vector vec = new Vector();       
        for (int i = 0; i < extraProperties.length; i++) {
            NameValuePair pair = new NameValuePair();
            pair.setParamName(extraProperties[i].getName());
            pair.setParamValue(extraProperties[i].getValue());
            vec.add(pair);
        }
        
        if (vec != null && vec.size() > 0) {
            NameValuePair[] props = new NameValuePair[vec.size()];
            bean.setExtraParams((NameValuePair[])vec.toArray(props));
        }    
        
        return bean;
    }
    
    public Resources getConnectorGraph(){
         Resources res = getResourceGraph();
         ConnectorResource connresource = res.newConnectorResource();
         connresource.setDescription(getDescription());
         connresource.setEnabled(getIsEnabled());
         connresource.setJndiName(getJndiName());
         connresource.setPoolName(getJndiName());
                  
         ConnectorConnectionPool connpoolresource = res.newConnectorConnectionPool();
         connpoolresource.setName(getJndiName());
         connpoolresource.setResourceAdapterName(getResAdapter());
         connpoolresource.setConnectionDefinitionName(getResType());
         // set properties
         NameValuePair[] params = getExtraParams();
         if (params != null && params.length > 0) {
             for (int i = 0; i < params.length; i++) {
                 NameValuePair pair = params[i];
                 PropertyElement prop = connpoolresource.newPropertyElement();
                 prop = populatePropertyElement(prop, pair);
                 connpoolresource.addPropertyElement(prop);
             }
         }
		                
         res.addConnectorResource(connresource);
         res.addConnectorConnectionPool(connpoolresource);
         
         return res;
     }
    
     public Resources getAdminObjectGraph(){
         Resources res = getResourceGraph();
         AdminObjectResource aoresource = res.newAdminObjectResource();
         aoresource.setDescription(getDescription());
         aoresource.setEnabled(getIsEnabled());
         aoresource.setJndiName(getJndiName());
         aoresource.setResAdapter(getResAdapter());
         aoresource.setResType(getResType());
         
         // set properties
         NameValuePair[] params = getExtraParams();
         if (params != null && params.length > 0) {
             for (int i = 0; i < params.length; i++) {
                 NameValuePair pair = params[i];
                 PropertyElement prop = aoresource.newPropertyElement();
                 prop = populatePropertyElement(prop, pair);
                 aoresource.addPropertyElement(prop);
             }
         }
         
         res.addAdminObjectResource(aoresource);
         return res;
     }
}
