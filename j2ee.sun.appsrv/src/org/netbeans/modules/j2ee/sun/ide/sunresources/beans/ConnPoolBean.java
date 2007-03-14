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
 * ConnPoolBean.java
 *
 * Created on September 12, 2003, 4:18 PM
 */

package org.netbeans.modules.j2ee.sun.ide.sunresources.beans;

import java.util.Vector;

import org.netbeans.modules.j2ee.sun.ide.editors.NameValuePair;
import org.netbeans.modules.j2ee.sun.share.serverresources.JdbcCP;
import org.netbeans.modules.j2ee.sun.dd.api.DDProvider;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.*;

import org.netbeans.modules.j2ee.sun.ide.editors.IsolationLevelEditor;
import org.netbeans.modules.j2ee.sun.sunresources.beans.WizardConstants;
import org.openide.util.NbBundle;
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
        String tranxIsolation = pool.getTransactionIsolationLevel();
        if(tranxIsolation == null){
            tranxIsolation = WizardConstants.__IsolationLevelDefault;
        }
        bean.setTranxIsoLevel(tranxIsolation);
        bean.setIsIsoLevGuaranteed(pool.getIsIsolationLevelGuaranteed());
        bean.setIsConnValidReq(pool.getIsConnectionValidationRequired());
        bean.setConnValidMethod(pool.getConnectionValidationMethod());
        bean.setValidationTableName(pool.getValidationTableName());
        bean.setFailAllConns(pool.getFailAllConnections());
        bean.setNontranxconns(pool.getNonTransactionalConnections());
        bean.setAllowNonComponentCallers(pool.getAllowNonComponentCallers());
                
        PropertyElement[] extraProperties = pool.getPropertyElement();
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
        JdbcConnectionPool connPool = res.newJdbcConnectionPool();
        connPool.setDescription(getDescription());
        connPool.setName(getName());
        connPool.setDatasourceClassname(getDsClass());
        connPool.setResType(getResType());
        connPool.setSteadyPoolSize(getSteadyPoolSize());
        connPool.setMaxPoolSize(getMaxPoolSize());
        connPool.setMaxWaitTimeInMillis(getMaxWaitTimeMilli());
        connPool.setPoolResizeQuantity(getPoolResizeQty());
        connPool.setIdleTimeoutInSeconds(getIdleIimeoutSecond());
        String isolation = getTranxIsoLevel();
        if (isolation != null && (isolation.length() == 0 || isolation.equals(WizardConstants.__IsolationLevelDefault))) {  
            isolation = null;
        }
        connPool.setTransactionIsolationLevel(isolation);
        connPool.setIsIsolationLevelGuaranteed(getIsIsoLevGuaranteed());
        connPool.setIsConnectionValidationRequired(getIsConnValidReq());
        connPool.setConnectionValidationMethod(getConnValidMethod());
        connPool.setValidationTableName(getValidationTableName());
        connPool.setFailAllConnections(getFailAllConns());
        connPool.setNonTransactionalConnections(getNontranxconns());
        connPool.setAllowNonComponentCallers(getAllowNonComponentCallers());
        NameValuePair[] params = getExtraParams();
        if (params != null && params.length > 0) {
            for (int i = 0; i < params.length; i++) {
                NameValuePair pair = params[i];
                PropertyElement prop = connPool.newPropertyElement();
                prop = populatePropertyElement(prop, pair); 
                connPool.addPropertyElement(prop);
            }
        }
        res.addJdbcConnectionPool(connPool);
        return res;
    }    
}
