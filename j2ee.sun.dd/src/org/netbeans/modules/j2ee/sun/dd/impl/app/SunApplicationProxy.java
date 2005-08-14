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
 * SunApplicationProxy.java
 *
 * Created on February 7, 2005, 9:14 PM
 */

package org.netbeans.modules.j2ee.sun.dd.impl.app;

import org.netbeans.modules.j2ee.sun.dd.api.DDException;
import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.app.SunApplication;
import org.netbeans.modules.j2ee.sun.dd.impl.DTDRegistry;

import org.w3c.dom.Document;
/**
 *
 * @author Nitya Doraisamy
 */
public class SunApplicationProxy implements SunApplication {
    
    private SunApplication appRoot;
    private String version;
    private OutputProvider outputProvider;
    private int ddStatus;
    private org.xml.sax.SAXParseException error;    
    private java.util.List listeners; 
    
    /** Creates a new instance of SunApplicationProxy */
    public SunApplicationProxy(SunApplication appRoot, String version) {
        this.appRoot = appRoot;
        this.version = version;
    }

    public void addPropertyChangeListener(java.beans.PropertyChangeListener pcl) {
        if (appRoot != null) 
            appRoot.addPropertyChangeListener(pcl);
        listeners.add(pcl);
    }

    public int addSecurityRoleMapping(org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping value) {
        return appRoot==null?-1:appRoot.addSecurityRoleMapping(value);
    }

    public int addWeb(org.netbeans.modules.j2ee.sun.dd.api.app.Web value) {
        return appRoot==null?-1:appRoot.addWeb(value);
    }

    public String dumpBeanNode() {
        return appRoot==null?null:appRoot.dumpBeanNode();
    }

    public String getPassByReference() {
        return appRoot==null?null:appRoot.getPassByReference();
    }

    public String getRealm() {
        return appRoot==null?null:appRoot.getRealm();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping[] getSecurityRoleMapping() {
        return appRoot==null?null:appRoot.getSecurityRoleMapping();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping getSecurityRoleMapping(int index) {
        return appRoot==null?null:appRoot.getSecurityRoleMapping(index);
    }

    public String getUniqueId() {
        return appRoot==null?null:appRoot.getUniqueId();
    }

    public Object getValue(String propertyName) {
        return appRoot==null?null:appRoot.getValue(propertyName);
    }

    public java.math.BigDecimal getVersion() {
        return new java.math.BigDecimal(version);
    }

    public org.netbeans.modules.j2ee.sun.dd.api.app.Web[] getWeb() {
        return appRoot==null?null:appRoot.getWeb();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.app.Web getWeb(int index) {
        return appRoot==null?null:appRoot.getWeb(index);
    }

    public org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping newSecurityRoleMapping() {
        return appRoot==null?null:appRoot.newSecurityRoleMapping();
    }

    public org.netbeans.modules.j2ee.sun.dd.api.app.Web newWeb() {
        return appRoot==null?null:appRoot.newWeb();
    }

    public void removePropertyChangeListener(java.beans.PropertyChangeListener pcl) {
        if (appRoot != null) 
            appRoot.removePropertyChangeListener(pcl);
        listeners.remove(pcl);
    }

    public int removeSecurityRoleMapping(org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping value) {
        return appRoot==null?-1:appRoot.removeSecurityRoleMapping(value);
    }

    public int removeWeb(org.netbeans.modules.j2ee.sun.dd.api.app.Web value) {
        return appRoot==null?-1:appRoot.removeWeb(value);
    }

    public void setPassByReference(String value) {
        if (appRoot!=null) appRoot.setPassByReference(value);
    }

    public void setRealm(String value) {
        if (appRoot!=null) appRoot.setRealm(value);
    }

    public void setSecurityRoleMapping(org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping[] value) {
        if (appRoot!=null) appRoot.setSecurityRoleMapping(value);
    }

    public void setSecurityRoleMapping(int index, org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping value) {
        if (appRoot!=null) appRoot.setSecurityRoleMapping(index, value);
    }

    public void setUniqueId(String value) {
        if (appRoot!=null) appRoot.setUniqueId(value);
    }

    public void setVersion(java.math.BigDecimal version) {
        String newVersion = version.toString();
        
        if (this.version.equals(newVersion))
            return;
        if (appRoot != null) {
            Document document = null;
            if(newVersion.equals(SunApplication.VERSION_5_0_0)){
                //This will always be an upgrade
                document = getDocument();
                org.netbeans.modules.j2ee.sun.dd.impl.app.model_5_0_0.SunApplication appGraph =
                        org.netbeans.modules.j2ee.sun.dd.impl.app.model_5_0_0.SunApplication.createGraph(document);
                appGraph.changeDocType(DTDRegistry.SUN_APPLICATION_50_DTD_PUBLIC_ID, DTDRegistry.SUN_APPLICATION_50_DTD_SYSTEM_ID);
                this.appRoot = appGraph;
            }
            if(newVersion.equals(SunApplication.VERSION_1_4_0)){
                document = getDocument();
                org.netbeans.modules.j2ee.sun.dd.impl.app.model_1_4_0.SunApplication appGraph =
                        org.netbeans.modules.j2ee.sun.dd.impl.app.model_1_4_0.SunApplication.createGraph(document);
                appGraph.changeDocType(DTDRegistry.SUN_APPLICATION_140_DTD_PUBLIC_ID, DTDRegistry.SUN_APPLICATION_140_DTD_SYSTEM_ID);
                this.appRoot = appGraph;
            }
            if(newVersion.equals(SunApplication.VERSION_1_3_0)){
                appRoot.setRealm(null);
                document = getDocument();
                org.netbeans.modules.j2ee.sun.dd.impl.app.model_1_3_0.SunApplication appGraph =
                        org.netbeans.modules.j2ee.sun.dd.impl.app.model_1_3_0.SunApplication.createGraph(document);
                appGraph.changeDocType(DTDRegistry.SUN_APPCLIENT_130_DTD_PUBLIC_ID, DTDRegistry.SUN_APPLICATION_130_DTD_SYSTEM_ID);
                this.appRoot = appGraph;
            }
        }
    }

    private Document getDocument(){
        Document document = null;
        if (appRoot instanceof org.netbeans.modules.j2ee.sun.dd.impl.app.model_1_3_0.SunApplication) {
            document =
                    ((org.netbeans.modules.j2ee.sun.dd.impl.app.model_1_3_0.SunApplication)appRoot).graphManager().getXmlDocument();
        }else if (appRoot instanceof org.netbeans.modules.j2ee.sun.dd.impl.app.model_1_4_0.SunApplication) {
            document =
                    ((org.netbeans.modules.j2ee.sun.dd.impl.app.model_1_4_0.SunApplication)appRoot).graphManager().getXmlDocument();
        }else if (appRoot instanceof org.netbeans.modules.j2ee.sun.dd.impl.app.model_5_0_0.SunApplication) {
            document =
                    ((org.netbeans.modules.j2ee.sun.dd.impl.app.model_5_0_0.SunApplication)appRoot).graphManager().getXmlDocument();
        }
        return document;
    }
    
    public void setWeb(org.netbeans.modules.j2ee.sun.dd.api.app.Web[] value) {
        if (appRoot!=null) appRoot.setWeb(value);
    }

    public void setWeb(int index, org.netbeans.modules.j2ee.sun.dd.api.app.Web value) {
        if (appRoot!=null) appRoot.setWeb(index, value);
    }

    public int sizeSecurityRoleMapping() {
        return appRoot==null?-1:appRoot.sizeSecurityRoleMapping();
    }

    public int sizeWeb() {
        return appRoot==null?-1:appRoot.sizeWeb();
    }

    public void write(java.io.OutputStream os) throws java.io.IOException {
        if (appRoot!=null) {
            appRoot.write(os);
        }
    }
 
    public SunApplication getOriginal() {
        return appRoot;
    }
    
    public org.xml.sax.SAXParseException getError() {
        return error;
    }
    public void setError(org.xml.sax.SAXParseException error) {
        this.error = error;
    }  

    public void setValue(String name, Object[] value) {
        if (appRoot!=null) appRoot.setValue(name, value);
    }

    public Object[] getValues(String name) {
        return appRoot==null?null:appRoot.getValues(name);
    }

    public void setValue(String name, int index, Object value) {
        if (appRoot!=null) appRoot.setValue(name, index, value);
    }

    public void setValue(String name, Object value) {
        if (appRoot!=null) appRoot.setValue(name, value);
    }

    public Object getValue(String name, int index) {
        return appRoot==null?null:appRoot.getValue(name, index);
    }

    public String getAttributeValue(String name) {
        return appRoot==null?null:appRoot.getAttributeValue(name);
    }

    public int size(String name) {
        return appRoot==null?-1:appRoot.size(name);
    }

    public int addValue(String name, Object value) {
        return appRoot==null?-1:appRoot.addValue(name, value);
    }

    public String[] findPropertyValue(String propName, Object value) {
        return appRoot==null?null:appRoot.findPropertyValue(propName, value);
    }

    public int removeValue(String name, Object value) {
        return appRoot==null?-1:appRoot.removeValue(name, value);
    }

    public void write(java.io.Writer w) throws java.io.IOException, org.netbeans.modules.j2ee.sun.dd.api.DDException {
        if (appRoot!=null) appRoot.write(w);
    }
    
    public void removeValue(String name, int index) {
        if (appRoot!=null) appRoot.removeValue(name, index);
    }

    public Object clone() {
        SunApplicationProxy proxy = null;
        if (appRoot==null)
            proxy = new SunApplicationProxy(null, version);
        else {
            SunApplication clonedSunApp=(SunApplication)appRoot.clone();
            proxy = new SunApplicationProxy(clonedSunApp, version);
        }
        proxy.setError(error);
        return proxy;
    }

    public String getAttributeValue(String propName, String name) {
        return appRoot==null?null:appRoot.getAttributeValue(propName, name);
    }

    public String getAttributeValue(String propName, int index, String name) {
        return appRoot==null?null:appRoot.getAttributeValue(propName, index, name);
    }

    public void setAttributeValue(String name, String value) {
        if (appRoot!=null) appRoot.setAttributeValue(name, value);
    }

    public void setAttributeValue(String propName, String name, String value) {
        if (appRoot!=null) appRoot.setAttributeValue(propName, name, value);
    }

    public void setAttributeValue(String propName, int index, String name, String value) {
        if (appRoot!=null) appRoot.setAttributeValue(propName, index, name, value);
    }

    public CommonDDBean getPropertyParent(String name) {
        return appRoot.getPropertyParent(name);
    }
    
    public void merge(CommonDDBean root, int mode) {
        if (root != null) {
            if (root instanceof SunApplicationProxy)
                appRoot.merge(((SunApplicationProxy)root).getOriginal(), mode);
            else appRoot.merge(root, mode);
        }
    }
   
    public static interface OutputProvider {
        public void write(SunApplication appRoot) throws java.io.IOException;
    }
}
