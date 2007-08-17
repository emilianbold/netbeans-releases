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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.websvc.manager.api;


import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlService;

/**
 * Metadata descriptor that contains the information for a single web service.
 * This metadata is associated (one-to-one) with a proxy jar.
 * 
 * @author quynguyen
 */
public class WebServiceDescriptor {
    public static final int JAX_RPC_TYPE = 0;
    public static final int JAX_WS_TYPE = 1;
    
    private String name;
    private String packageName;
    private int wsType;
    private String wsdl;
    private String xmlDescriptor;
    private WsdlService model;
    private List<JarEntry> jars;
    private Map<String, Object> consumerData;
    
    public WebServiceDescriptor() {
    }
    
    public WebServiceDescriptor(String name, String packageName, int wsType, URL wsdl, File xmlDescriptor, WsdlService model) {
        this.name = name;
        this.packageName = packageName;
        this.wsType = wsType;
        this.wsdl = wsdl.toExternalForm();
        this.xmlDescriptor = xmlDescriptor.getAbsolutePath();
        this.model = model;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public int getWsType() {
        return wsType;
    }

    public void setWsType(int wsType) {
        this.wsType = wsType;
    }
    
    public String getWsdl() {
        return wsdl;
    }
    
    public URL getWsdlUrl() {
        try {
            return new java.net.URL(wsdl);
        } catch (MalformedURLException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, ex.getLocalizedMessage(), ex);
            return null;
        }
    }
    
    public void setWsdl(String wsdl) {
        this.wsdl = wsdl;
    }
    
    public String getXmlDescriptor() {
        return xmlDescriptor;
    }

    public File getXmlDescriptorFile() {
        return new File(xmlDescriptor);
    }
    
    public void setXmlDescriptor(String xmlDescriptor) {
        this.xmlDescriptor = xmlDescriptor;
    }

    public Map<String, Object> getConsumerData() {
        if (consumerData == null) {
            consumerData = new HashMap<String, Object>();
        }
        return consumerData;
    }
    
    public void setConsumerData(Map<String, Object> consumerData) {
        this.consumerData = consumerData;
    }
    
    public void addConsumerData(String key, Object data) {
        getConsumerData().put(key, data);
    }
    
    public void removeConsumerData(String key) {
        getConsumerData().remove(key);
    }
    
    public List<JarEntry> getJars() {
        if (jars == null) {
            jars = new LinkedList<JarEntry>();
        }
        return jars;
    }
    
    public void setJars(List<JarEntry> jars) {
        this.jars = jars;
    }

    public WsdlService getModel() {
        return model;
    }

    public void setModel(WsdlService model) {
        this.model = model;
    }
    
    public void addJar(String relativePath, String type) {
        getJars().add(new JarEntry(relativePath, type));
    }
    
    public void removeJar(String relativePath, String type) {
        getJars().remove(new JarEntry(relativePath, type));
    }
    
    public static class JarEntry {
        public static final String PROXY_JAR_TYPE = "proxy";
        public static final String SRC_JAR_TYPE = "source";
        
        private String name;
        private String type;
        
        public JarEntry() {
        }
        
        public JarEntry(String name, String type) {
            this.name = name;
            this.type = type;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getType() {
            return type;
        }
        
        public void setType(String type) {
            this.type = type;
        }
        
        public boolean equals(Object o) { 
            try {
                JarEntry entry = (JarEntry)o;
                return entry.name.equals(name) && entry.type.equals(type);
            }catch (Exception ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.INFO, ex.getLocalizedMessage(), ex);
                return false;
            }
        }
    }
}