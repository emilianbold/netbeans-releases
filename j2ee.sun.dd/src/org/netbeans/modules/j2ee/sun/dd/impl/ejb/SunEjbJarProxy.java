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
 * SunEjbJarProxy.java
 *
 * Created on February 7, 2005, 12:13 PM
 */

package org.netbeans.modules.j2ee.sun.dd.impl.ejb;

import org.netbeans.modules.j2ee.sun.dd.api.DDException;
import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.SunEjbJar;

/**
 *
 * @author Nitya Doraisamy
 */
public class SunEjbJarProxy implements SunEjbJar {
    
    private SunEjbJar ejbJarRoot;
    private String version;
    private OutputProvider outputProvider;
    private int ddStatus;
    private org.xml.sax.SAXParseException error;    
    private java.util.List listeners; 
        
    /** Creates a new instance of SunEjbJarProxy */
    public SunEjbJarProxy(SunEjbJar ejbJarRoot, String version) {
        this.ejbJarRoot = ejbJarRoot;
        this.version = version;
    }

    public int addSecurityRoleMapping(org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping securityRoleMapping) {
        return ejbJarRoot==null?-1:ejbJarRoot.addSecurityRoleMapping(securityRoleMapping);
    }

    public org.netbeans.modules.j2ee.sun.dd.api.ejb.EnterpriseBeans getEnterpriseBeans() {
        return ejbJarRoot==null?null:ejbJarRoot.getEnterpriseBeans();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping[] getSecurityRoleMapping() {
        return ejbJarRoot==null?null:ejbJarRoot.getSecurityRoleMapping();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping getSecurityRoleMapping(int param) {
        return ejbJarRoot==null?null:ejbJarRoot.getSecurityRoleMapping(param);
    }

    public org.netbeans.modules.j2ee.sun.dd.api.ejb.EnterpriseBeans newEnterpriseBeans() {
        if(ejbJarRoot == null)
            return null;
        else
            return ejbJarRoot.newEnterpriseBeans();
    }

    public int removeSecurityRoleMapping(org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping securityRoleMapping) {
        return ejbJarRoot==null?-1:ejbJarRoot.removeSecurityRoleMapping(securityRoleMapping);
    }

    public void setEnterpriseBeans(org.netbeans.modules.j2ee.sun.dd.api.ejb.EnterpriseBeans enterpriseBeans) {
        if (ejbJarRoot!=null) ejbJarRoot.setEnterpriseBeans(enterpriseBeans);
    }

    public void setSecurityRoleMapping(org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping[] securityRoleMapping) {
        if (ejbJarRoot!=null) ejbJarRoot.setSecurityRoleMapping(securityRoleMapping);
    }

    public void setSecurityRoleMapping(int param, org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping securityRoleMapping) {
        if (ejbJarRoot!=null) ejbJarRoot.setSecurityRoleMapping(param, securityRoleMapping);
    }

    public int sizeSecurityRoleMapping() {
         return ejbJarRoot==null?-1:ejbJarRoot.sizeSecurityRoleMapping();
    }

    public void removePropertyChangeListener(java.beans.PropertyChangeListener pcl) {
        if (ejbJarRoot != null) 
            ejbJarRoot.removePropertyChangeListener(pcl);
        listeners.remove(pcl);
    }

    public void addPropertyChangeListener(java.beans.PropertyChangeListener pcl) {
         if (ejbJarRoot != null) 
            ejbJarRoot.addPropertyChangeListener(pcl);
        listeners.add(pcl);
    }

    public void setVersion(java.math.BigDecimal version) {
        String newVersion = version.toString();
        if (this.version.equals(newVersion)) return;
        if (ejbJarRoot!=null) {
            org.w3c.dom.Document document = null;
            if (ejbJarRoot instanceof org.netbeans.modules.j2ee.sun.dd.impl.ejb.model_2_1_0.SunEjbJar) {
                document = 
                    ((org.netbeans.modules.j2ee.sun.dd.impl.ejb.model_2_1_0.SunEjbJar)ejbJarRoot).graphManager().getXmlDocument();
            }
            if (document!=null) {
                org.w3c.dom.Element docElement = document.getDocumentElement();
                if (docElement!=null) {
                    org.w3c.dom.DocumentType docType = document.getDoctype();
                    if (docType!=null) {
                        document.removeChild(docType); //NOI18N
                    }
                    docElement.setAttribute("version","2.1"); //NOI18N
                    //Do required set Attributes
                }
            }
        }
    }

    public java.math.BigDecimal getVersion() {
        return new java.math.BigDecimal(version);
    }
    
     public void setOriginal(SunEjbJar ejbJarRoot) {
        if (this.ejbJarRoot != ejbJarRoot) {
            for (int i=0;i<listeners.size();i++) {
                java.beans.PropertyChangeListener pcl = 
                    (java.beans.PropertyChangeListener)listeners.get(i);
                if (this.ejbJarRoot != null) 
                    this.ejbJarRoot.removePropertyChangeListener(pcl);
                if (ejbJarRoot != null) 
                    ejbJarRoot.addPropertyChangeListener(pcl);
                
            }
            this.ejbJarRoot = ejbJarRoot;
            if (ejbJarRoot != null) 
                setProxyVersion(ejbJarRoot.getVersion().toString());
        }
    }
    
    public SunEjbJar getOriginal() {
        return ejbJarRoot;
    }
    
    public org.xml.sax.SAXParseException getError() {
        return error;
    }
    public void setError(org.xml.sax.SAXParseException error) {
        this.error=error;
    }    
    
    public void setProxyVersion(java.lang.String value) {
        if ((version==null && value!=null) || !version.equals(value)) {
            java.beans.PropertyChangeEvent evt = 
                new java.beans.PropertyChangeEvent(this, PROPERTY_VERSION, version, value); 
            version=value;
            for (int i=0;i<listeners.size();i++) {
                ((java.beans.PropertyChangeListener)listeners.get(i)).propertyChange(evt);
            }
        }
    }
    
    public Object getValue(String name) {
        return ejbJarRoot==null?null:ejbJarRoot.getValue(name);
    }
    
    public void write(java.io.OutputStream os) throws java.io.IOException {
        if (ejbJarRoot!=null) {
            ejbJarRoot.write(os);
        }
    }

    public String dumpBeanNode() {
        if (ejbJarRoot!=null) 
            return ejbJarRoot.dumpBeanNode();
        else
            return null;
    }

    public void setValue(String name, Object[] value) {
        if (ejbJarRoot!=null) ejbJarRoot.setValue(name, value);
    }

    public Object[] getValues(String name) {
        return ejbJarRoot==null?null:ejbJarRoot.getValues(name);
    }

    public void setValue(String name, int index, Object value) {
        if (ejbJarRoot!=null) ejbJarRoot.setValue(name, index, value);
    }

    public void setValue(String name, Object value) {
        if (ejbJarRoot!=null) ejbJarRoot.setValue(name, value);
    }

    public Object getValue(String name, int index) {
        return ejbJarRoot==null?null:ejbJarRoot.getValue(name, index);
    }

    public String getAttributeValue(String name) {
        return ejbJarRoot==null?null:ejbJarRoot.getAttributeValue(name);
    }

    public int size(String name) {
        return ejbJarRoot==null?-1:ejbJarRoot.size(name);
    }

    public int addValue(String name, Object value) {
        return ejbJarRoot==null?-1:ejbJarRoot.addValue(name, value);
    }

    public String[] findPropertyValue(String propName, Object value) {
        return ejbJarRoot==null?null:ejbJarRoot.findPropertyValue(propName, value);
    }

    public int removeValue(String name, Object value) {
        return ejbJarRoot==null?-1:ejbJarRoot.removeValue(name, value);
    }

    public void write(java.io.Writer w) throws java.io.IOException, DDException {
        if (ejbJarRoot!=null) ejbJarRoot.write(w);
    }

    public void removeValue(String name, int index) {
        if (ejbJarRoot!=null) ejbJarRoot.removeValue(name, index);
    }

   public Object clone() {
        SunEjbJarProxy proxy = null;
        if (ejbJarRoot==null)
            proxy = new SunEjbJarProxy(null, version);
        else {
            SunEjbJar clonedSunEjb=(SunEjbJar)ejbJarRoot.clone();
            proxy = new SunEjbJarProxy(clonedSunEjb, version);
        }
        proxy.setError(error);
        return proxy;
    }

    public String getAttributeValue(String propName, String name) {
        return ejbJarRoot==null?null:ejbJarRoot.getAttributeValue(propName, name);
    }

    public String getAttributeValue(String propName, int index, String name) {
        return ejbJarRoot==null?null:ejbJarRoot.getAttributeValue(propName, index, name);
    }

    public void setAttributeValue(String name, String value) {
         if (ejbJarRoot!=null) ejbJarRoot.setAttributeValue(name, value);
    }

    public void setAttributeValue(String propName, String name, String value) {
        if (ejbJarRoot!=null) ejbJarRoot.setAttributeValue(propName, name, value);
    }

    public void setAttributeValue(String propName, int index, String name, String value) {
        if (ejbJarRoot!=null) ejbJarRoot.setAttributeValue(propName, index, name, value);
    }

    public CommonDDBean getPropertyParent(String name) {
        return ejbJarRoot.getPropertyParent(name);
    }

    public void merge(CommonDDBean root, int mode) {
        if (root != null) {
            if (root instanceof SunEjbJarProxy)
                ejbJarRoot.merge(((SunEjbJarProxy)root).getOriginal(), mode);
            else ejbJarRoot.merge(root, mode);
        }
    }
        
    /** Contract between friend modules that enables 
    * a specific handling of write(FileObject) method for targeted FileObject
    */
    public static interface OutputProvider {
        public void write(SunEjbJar ejbJarRoot) throws java.io.IOException;
    }
}
