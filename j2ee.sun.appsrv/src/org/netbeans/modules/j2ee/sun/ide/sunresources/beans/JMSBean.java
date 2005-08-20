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
 * JMSBean.java
 *
 * Created on November 13, 2003, 3:05 PM
 */

package org.netbeans.modules.j2ee.sun.ide.sunresources.beans;

import java.util.Vector;

import org.netbeans.modules.j2ee.sun.ide.editors.NameValuePair;
import org.netbeans.modules.j2ee.sun.share.serverresources.JavaMsgServiceResource;
import org.netbeans.modules.j2ee.sun.dd.api.DDProvider;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.*;

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
    
     public static JMSBean createBean(JmsResource jmsresource) {
        JMSBean bean = new JMSBean();
        //name attribute in bean is for studio display purpose. 
        //It is not part of the jms-resource dtd.
        bean.setName(jmsresource.getJndiName());
        bean.setDescription(jmsresource.getDescription());
        bean.setJndiName(jmsresource.getJndiName());
        bean.setResType(jmsresource.getResType());
        bean.setIsEnabled(jmsresource.getEnabled());
        
        PropertyElement[] extraProperties = jmsresource.getPropertyElement();
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
         JmsResource jmsresource = res.newJmsResource();
         jmsresource.setDescription(getDescription());
         jmsresource.setJndiName(getJndiName());
         jmsresource.setResType(getResType());
         jmsresource.setEnabled(getIsEnabled());
         
         // set properties
         NameValuePair[] params = getExtraParams();
         if (params != null && params.length > 0) {
             for (int i = 0; i < params.length; i++) {
                 NameValuePair pair = params[i];
                 PropertyElement prop = jmsresource.newPropertyElement();
                 prop = populatePropertyElement(prop, pair);
                 jmsresource.addPropertyElement(prop);
             }
         }
         
         res.addJmsResource(jmsresource);
         return res;
     }
    
}
