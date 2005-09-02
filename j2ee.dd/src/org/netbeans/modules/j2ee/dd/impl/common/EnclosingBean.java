/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/**
 * Superclass for most s2b beans that provides useful methods for creating and finding beans
 * in bean graph.
 *
 * @author  Milan Kuchtiak
 */

package org.netbeans.modules.j2ee.dd.impl.common;

import org.netbeans.modules.schema2beans.Version;
import org.netbeans.modules.schema2beans.BaseBean;

//import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
//import org.netbeans.modules.j2ee.dd.impl.common.CommonDDAccess;
import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.netbeans.modules.j2ee.dd.api.common.NameAlreadyUsedException;
import org.netbeans.modules.j2ee.dd.api.common.CreateCapability;
import org.netbeans.modules.j2ee.dd.api.common.FindCapability;
import org.netbeans.modules.j2ee.dd.api.common.RootInterface;

public abstract class EnclosingBean extends BaseBean implements CommonDDBean, CreateCapability, FindCapability {

    private Object original = this;

    /** Creates a new instance of EnclosingBean */
    public EnclosingBean(java.util.Vector comps, Version version) {
	super(comps, version);
    }
    
    /**
    * Method is looking for the nested bean according to the specified property and value 
    *
    * @param beanName e.g. "Servlet" or "ResourceRef"
    * @param propertyName e.g. "ServletName" or ResourceRefName"
    * @param value specific propertyName value e.g. "ControllerServlet" or "jdbc/EmployeeAppDb"
    * @return Bean satisfying the parameter values or null if not found
    */ 
    public CommonDDBean findBeanByName(String beanName, String propertyName, String value) {
        return (CommonDDBean)CommonDDAccess.findBeanByName(this, beanName, propertyName, value);
    }
    
    /**
    * An empty (not bound to schema2beans graph) bean is created corresponding to beanName 
    * regardless the Servlet Spec. version 
    * @param beanName bean name e.g. Servlet
    * @return CommonDDBean corresponding to beanName value
    */
    public CommonDDBean createBean(String beanName) throws ClassNotFoundException {
        return (CommonDDBean)CommonDDAccess.newBean(this, beanName, getPackagePostfix ());
    }
    
    private String getPackagePostfix () {
        String pack = getClass().getPackage().getName();
//        if (pack.endsWith(CommonDDAccess.SERVLET_2_4)) return CommonDDAccess.SERVLET_2_4;
//        else if (pack.endsWith(CommonDDAccess.SERVLET_2_3)) return CommonDDAccess.SERVLET_2_3;
        return pack;
    }
    
    public void write (org.openide.filesystems.FileObject fo) throws java.io.IOException {
        // PENDING
        // need to be implemented with Dialog opened when the file object is locked
    }
    
    public CommonDDBean addBean(String beanName, String[] propertyNames, Object[] propertyValues, String keyProperty) throws ClassNotFoundException, NameAlreadyUsedException {
        if (keyProperty!=null) {
            Object keyValue = null;
            if (propertyNames!=null)
                for (int i=0;i<propertyNames.length;i++) {
                    if (keyProperty.equals(propertyNames[i])) {
                        keyValue=propertyValues[i];
                        break;
                    }
                }
            if (keyValue!=null && keyValue instanceof String) {
                if (findBeanByName(beanName, keyProperty,(String)keyValue)!=null) {
                    throw new NameAlreadyUsedException(beanName,  keyProperty, (String)keyValue);
                }   
            }
        }
        CommonDDBean newBean = createBean(beanName);
        if (propertyNames!=null)
            for (int i=0;i<propertyNames.length;i++) {
                try {
                    ((BaseBean)newBean).setValue(propertyNames[i],propertyValues[i]);
                } catch (IndexOutOfBoundsException ex) {
                    ((BaseBean)newBean).setValue(propertyNames[i],new Object[]{propertyValues[i]});
                }
            }
        CommonDDAccess.addBean(this, newBean, beanName, getPackagePostfix ());
        return newBean;
    }
    
    public CommonDDBean addBean(String beanName) throws ClassNotFoundException {
        try {
            return addBean(beanName,null,null,null);
        } catch (NameAlreadyUsedException ex){}
        return null;
    }

    public Object getOriginal() {
        return original;
    }

    public Object clone() {
        EnclosingBean enclosingBean = (EnclosingBean) super.clone();
        enclosingBean.original = original;
        return enclosingBean;
    }

    public void merge(RootInterface root, int mode) {
        this.merge((BaseBean)root,mode);
    }

}