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
/*
 * JavaMailSessionBean.java
 *
 * Created on September 17, 2003, 2:50 PM
 */

package org.netbeans.modules.j2ee.sun.ide.sunresources.beans;

import java.util.Vector;
import org.netbeans.modules.j2ee.sun.ide.editors.NameValuePair;
import org.netbeans.modules.j2ee.sun.share.serverresources.MailSessionResource;
import org.netbeans.modules.j2ee.sun.dd.api.DDProvider;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.*;

/**
 *
 * @author  nityad
 */
public class JavaMailSessionBean extends MailSessionResource implements java.io.Serializable {
    
    /** Creates a new instance of JavaMailSessionBean */
    public JavaMailSessionBean() {
    }
    
    public String getName() {
        return super.getName();
    }
    
    public String getJndiName(){
        return super.getJndiName();
    }
    
    public static JavaMailSessionBean createBean(MailResource mailresource) {
        JavaMailSessionBean bean = new JavaMailSessionBean();
        //name attribute in bean is for studio display purpose. 
        //It is not part of the mail-resource dtd.
        bean.setName(mailresource.getJndiName());
        bean.setDescription(mailresource.getDescription());
        bean.setJndiName(mailresource.getJndiName());
        bean.setStoreProt(mailresource.getStoreProtocol());
        bean.setStoreProtClass(mailresource.getStoreProtocolClass());
        bean.setTransProt(mailresource.getTransportProtocol());
        bean.setTransProtClass(mailresource.getTransportProtocolClass());
        bean.setHostName(mailresource.getHost());
        bean.setUserName(mailresource.getUser());
        bean.setFromAddr(mailresource.getFrom());
        bean.setIsDebug(mailresource.getDebug());
        bean.setIsEnabled(mailresource.getEnabled());
           
        PropertyElement[] extraProperties = mailresource.getPropertyElement();
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
    
    public Resources getGraph(){
        Resources res = getResourceGraph();
        MailResource mlresource = res.newMailResource();
        mlresource.setDescription(getDescription());
        mlresource.setJndiName(getJndiName());
        mlresource.setStoreProtocol(getStoreProt());
        mlresource.setStoreProtocolClass(getStoreProtClass());
        mlresource.setTransportProtocol(getTransProt());
        mlresource.setTransportProtocolClass(getTransProtClass());
        mlresource.setHost(getHostName());
        mlresource.setUser(getUserName());
        mlresource.setFrom(getFromAddr());
        mlresource.setDebug(getIsDebug());
        mlresource.setEnabled(getIsEnabled());
        
        // set properties
        NameValuePair[] params = getExtraParams();
        if (params != null && params.length > 0) {
            for (int i = 0; i < params.length; i++) {
                NameValuePair pair = params[i];
                PropertyElement prop = mlresource.newPropertyElement();
                prop = populatePropertyElement(prop, pair);
                mlresource.addPropertyElement(prop);
            }
        }
        
        res.addMailResource(mlresource);
        return res;
    }
        
}
