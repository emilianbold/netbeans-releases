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
 * PersistenceManagerBean.java
 *
 * Created on September 17, 2003, 1:29 PM
 */

package org.netbeans.modules.j2ee.sun.ide.sunresources.beans;

import java.util.Vector;

import org.netbeans.modules.j2ee.sun.ide.editors.NameValuePair;
import org.netbeans.modules.j2ee.sun.share.serverresources.PersistenceManagerResource;
import org.netbeans.modules.j2ee.sun.share.dd.resources.PersistenceManagerFactoryResource;
import org.netbeans.modules.j2ee.sun.share.dd.resources.ExtraProperty;

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
        
        ExtraProperty[] extraProperties = pmfresource.getExtraProperty();
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
