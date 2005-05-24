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
 * ConnPoolBean.java
 *
 * Created on September 12, 2003, 4:18 PM
 */

package org.netbeans.modules.j2ee.sun.ide.sunresources.beans;

import java.util.Vector;

import org.netbeans.modules.j2ee.sun.ide.editors.NameValuePair;
import org.netbeans.modules.j2ee.sun.share.serverresources.JdbcCP;
import org.netbeans.modules.j2ee.sun.share.dd.resources.JdbcConnectionPool;
import org.netbeans.modules.j2ee.sun.share.dd.resources.ExtraProperty;

/**
 *
 * @author  nityad
 */
public class ConnPoolBean extends JdbcCP implements java.io.Serializable{
    
    /** Creates new ConnPoolBean */
    public ConnPoolBean() {

    }
    
    public String getName() {
        return super.getName();
    }
    
    public static ConnPoolBean createBean(JdbcConnectionPool pool) {
        ConnPoolBean bean = new ConnPoolBean();
                
        bean.setName(pool.getName());
        bean.setDescription(pool.getDescription());
        bean.setDsClass(pool.getDatasourceClassname());
        bean.setResType(pool.getResType());
        bean.setSteadyPoolSize(pool.getSteadyPoolSize());
        bean.setMaxPoolSize(pool.getMaxPoolSize());
        bean.setMaxWaitTimeMilli(pool.getMaxWaitTimeInMillis());
        bean.setPoolResizeQty(pool.getPoolResizeQuantity());
        bean.setIdleIimeoutSecond(pool.getIdleTimeoutInSeconds());
        bean.setTranxIsoLevel(pool.getTransactionIsolationLevel());
        bean.setIsIsoLevGuaranteed(pool.getIsIsolationLevelGuaranteed());
        bean.setIsConnValidReq(pool.getIsConnectionValidationRequired());
        bean.setConnValidMethod(pool.getConnectionValidationMethod());
        bean.setValidationTableName(pool.getValidationTableName());
        bean.setFailAllConns(pool.getFailAllConnections());
                
        ExtraProperty[] extraProperties = pool.getExtraProperty();
        Vector vec = new Vector();       
        for (int i = 0; i < extraProperties.length; i++) {
            NameValuePair pair = new NameValuePair();
            pair.setParamName(extraProperties[i].getAttributeValue("name"));  //NOI18N
            pair.setParamValue(extraProperties[i].getAttributeValue("value"));  //NOI18N
            //pair.setParamDescription(extraProperties[i].getDescription());
            vec.add(pair);
        }
        
        if (vec != null && vec.size() > 0) {
            NameValuePair[] props = new NameValuePair[vec.size()];
            bean.setExtraParams((NameValuePair[])vec.toArray(props));
        } 
        return bean;
    }
    
}
