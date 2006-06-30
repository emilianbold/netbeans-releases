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
 * PersistenceManagerBean.java
 *
 * Created on September 17, 2003, 1:29 PM
 */

package org.netbeans.modules.j2ee.sun.ide.sunresources.beans;

import java.util.Vector;

import org.netbeans.modules.j2ee.sun.ide.editors.NameValuePair;
import org.netbeans.modules.j2ee.sun.share.serverresources.PersistenceManagerResource;
import org.netbeans.modules.j2ee.sun.dd.api.DDProvider;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.*;

/**
 *
 * @author  nityad
 */
public class PersistenceManagerBean extends PersistenceManagerResource implements java.io.Serializable {
    
    /** Creates a new instance of PersistenceManagerBean */
    public PersistenceManagerBean() {
    }
    
    public String getName() {
        return super.getName();
    }
    
    public String getJndiName(){
        return super.getJndiName();
    }
    
    public static PersistenceManagerBean createBean(PersistenceManagerFactoryResource pmfresource) {
        PersistenceManagerBean bean = new PersistenceManagerBean();
        //name attribute in bean is for studio display purpose. 
        //It is not part of the persistence-manager-factory-resource dtd.
        bean.setName(pmfresource.getJndiName());
        bean.setDescription(pmfresource.getDescription());
        bean.setJndiName(pmfresource.getJndiName());
        bean.setFactoryClass(pmfresource.getFactoryClass());
        bean.setDatasourceJndiName(pmfresource.getJdbcResourceJndiName());
        bean.setIsEnabled(pmfresource.getEnabled());
        
        PropertyElement[] extraProperties = pmfresource.getPropertyElement();
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
        PersistenceManagerFactoryResource pmfresource = res.newPersistenceManagerFactoryResource();
        pmfresource.setDescription(getDescription());
        pmfresource.setJndiName(getJndiName());
        pmfresource.setFactoryClass(getFactoryClass());
        pmfresource.setJdbcResourceJndiName(getDatasourceJndiName());
        pmfresource.setEnabled(getIsEnabled());
        
        // set properties
        NameValuePair[] params = getExtraParams();
        if (params != null && params.length > 0) {
            for (int i = 0; i < params.length; i++) {
                NameValuePair pair = params[i];
                PropertyElement prop = pmfresource.newPropertyElement();
                prop = populatePropertyElement(prop, pair);
                pmfresource.addPropertyElement(prop);
            }
        }
        
        res.addPersistenceManagerFactoryResource(pmfresource);
        return res;
    }
    
}
