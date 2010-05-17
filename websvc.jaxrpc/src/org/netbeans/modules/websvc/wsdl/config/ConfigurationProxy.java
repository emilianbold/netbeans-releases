/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.websvc.wsdl.config;

import org.netbeans.modules.websvc.wsdl.config.api.Configuration;

/**
 *
 * @author Peter Williams
 */
public class ConfigurationProxy implements Configuration {
    private Configuration configuration;
    private String version;
    private java.util.List listeners;
    public boolean writing=false;
    private OutputProvider outputProvider;
    private org.xml.sax.SAXParseException error;
    private int ddStatus;

    public ConfigurationProxy(Configuration configuration) {
        init(configuration, "1.1");
    }

    private void init(Configuration configuration, String version) {
        this.configuration=configuration;
        this.version = version;
        listeners = new java.util.ArrayList();
    }

    public void setOriginal(Configuration configuration) {
        if (this.configuration!=configuration) {
            for (int i=0;i<listeners.size();i++) {
                java.beans.PropertyChangeListener pcl =
                    (java.beans.PropertyChangeListener)listeners.get(i);
                if (this.configuration!=null) {
                    this.configuration.removePropertyChangeListener(pcl);
                }
                if (configuration!=null) {
                    configuration.addPropertyChangeListener(pcl);
                }

            }
            this.configuration=configuration;
            // !PW Configuration does not store version in JAX-RPC 1.1
//            if (configuration!=null) {
//                setProxyVersion(configuration.getVersion());
//            }
        }
    }

    public Configuration getOriginal() {
        return configuration;
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
    /*
    public void setVersion(java.lang.String value) {
    }
    */
    public java.lang.String getVersion() {
        return version;
    }
    public org.xml.sax.SAXParseException getError() {
        return error;
    }
    public void setError(org.xml.sax.SAXParseException error) {
        this.error=error;
    }
    public int getStatus() {
        return ddStatus;
    }
    public void setStatus(int value) {
        if (ddStatus!=value) {
            java.beans.PropertyChangeEvent evt =
                new java.beans.PropertyChangeEvent(this, PROPERTY_STATUS, new Integer(ddStatus), new Integer(value));
            ddStatus=value;
            for (int i=0;i<listeners.size();i++) {
                ((java.beans.PropertyChangeListener)listeners.get(i)).propertyChange(evt);
            }
        }
    }

    public void addPropertyChangeListener(java.beans.PropertyChangeListener pcl) {
        if (configuration!=null) configuration.addPropertyChangeListener(pcl);
        listeners.add(pcl);
    }

    public void removePropertyChangeListener(java.beans.PropertyChangeListener pcl) {
        if (configuration!=null) configuration.removePropertyChangeListener(pcl);
        listeners.remove(pcl);
    }

//    public org.netbeans.modules.j2ee.dd.api.common.CommonDDBean createBean(String beanName) throws ClassNotFoundException {
//        return configuration==null?null:configuration.createBean(beanName);
//    }
//
//    public org.netbeans.modules.j2ee.dd.api.common.CommonDDBean addBean(String beanName, String[] propertyNames, Object[] propertyValues, String keyProperty) throws ClassNotFoundException, org.netbeans.modules.j2ee.dd.api.common.NameAlreadyUsedException {
//        return configuration==null?null:configuration.addBean(beanName, propertyNames, propertyValues, keyProperty);
//    }
//
//    public org.netbeans.modules.j2ee.dd.api.common.CommonDDBean addBean(String beanName) throws ClassNotFoundException {
//        return configuration==null?null:configuration.addBean(beanName);
//    }
//
//    public org.netbeans.modules.j2ee.dd.api.common.CommonDDBean findBeanByName(String beanName, String propertyName, String value) {
//        return configuration==null?null:configuration.findBeanByName(beanName, propertyName, value);
//    }

    public org.netbeans.modules.websvc.wsdl.config.api.J2eeMappingFile getJ2eeMappingFile() {
        return configuration == null ? null : configuration.getJ2eeMappingFile();
    }

    public org.netbeans.modules.websvc.wsdl.config.api.Modelfile getModelfile() {
        return configuration == null ? null : configuration.getModelfile();
    }

    public org.netbeans.modules.websvc.wsdl.config.api.Service getService() {
        return configuration == null ? null : configuration.getService();
    }

    public org.netbeans.modules.websvc.wsdl.config.api.Wsdl getWsdl() {
        return configuration == null ? null : configuration.getWsdl();
    }

    public org.netbeans.modules.websvc.wsdl.config.api.J2eeMappingFile newJ2eeMappingFile() {
        return configuration == null ? null : configuration.newJ2eeMappingFile();
    }

    public org.netbeans.modules.websvc.wsdl.config.api.Modelfile newModelfile() {
        return configuration == null ? null : configuration.newModelfile();
    }

    public org.netbeans.modules.websvc.wsdl.config.api.Service newService() {
        return configuration == null ? null : configuration.newService();
    }

    public org.netbeans.modules.websvc.wsdl.config.api.Wsdl newWsdl() {
        return configuration == null ? null : configuration.newWsdl();
    }

    public void setJ2eeMappingFile(org.netbeans.modules.websvc.wsdl.config.api.J2eeMappingFile value) {
        if(configuration != null) {
            configuration.setJ2eeMappingFile(value);
        }
    }

    public void setModelfile(org.netbeans.modules.websvc.wsdl.config.api.Modelfile value) {
        if(configuration != null) {
            configuration.setModelfile(value);
        }
    }

    public void setService(org.netbeans.modules.websvc.wsdl.config.api.Service value) {
        if(configuration != null) {
            configuration.setService(value);
        }
    }

    public void setWsdl(org.netbeans.modules.websvc.wsdl.config.api.Wsdl value) {
        if(configuration != null) {
            configuration.setWsdl(value);
        }
    }

    public Object getValue(String name) {
        return configuration==null?null:configuration.getValue(name);
    }

    public void merge(org.netbeans.modules.websvc.wsdl.config.api.RootInterface bean, int mode) {
        if (configuration!=null) {
            if (bean instanceof ConfigurationProxy) {
                configuration.merge(((ConfigurationProxy)bean).getOriginal(), mode);
            } else {
                configuration.merge(bean, mode);
            }
        }
    }

    public void write(java.io.OutputStream os) throws java.io.IOException {
        if (configuration!=null) {
            writing=true;
            configuration.write(os);
        }
    }

    public void write(org.openide.filesystems.FileObject fo) throws java.io.IOException {
        if (configuration!=null) {
            try {
                org.openide.filesystems.FileLock lock = fo.lock();
                try {
                    java.io.OutputStream os = fo.getOutputStream(lock);
                    try {
                        writing=true;
                        write(os);
                    } finally {
                        os.close();
                    }
                }
                finally {
                    lock.releaseLock();
                }
            } catch (org.openide.filesystems.FileAlreadyLockedException ex) {
                // trying to use OutputProvider for writing changes
                org.openide.loaders.DataObject dobj = org.openide.loaders.DataObject.find(fo);
                if (dobj!=null && dobj instanceof ConfigurationProxy.OutputProvider)
                    ((ConfigurationProxy.OutputProvider)dobj).write(this);
                else throw ex;
            }
        }
    }

    public Object clone() {
        ConfigurationProxy proxy = null;
        if (configuration==null) {
            proxy = new ConfigurationProxy(null);
        } else {
            Configuration clonedConfiguration=(Configuration)configuration.clone();
            proxy = new ConfigurationProxy(clonedConfiguration);
            ((org.netbeans.modules.websvc.wsdl.config.impl.Configuration)clonedConfiguration)._setSchemaLocation
                ("http://java.sun.com/xml/ns/jax-rpc/ri/config");
        }
        proxy.setError(error);
        proxy.setStatus(ddStatus);
        return proxy;
    }

    public boolean isWriting() {
        return writing;
    }

    public void setWriting(boolean writing) {
        this.writing=writing;
    }

    public void setOutputProvider(OutputProvider iop) {
        this.outputProvider=iop;
    }

    /** Contract between friend modules that enables
    * a specific handling of write(FileObject) method for targeted FileObject
    */
    public static interface OutputProvider {
        public void write(Configuration configuration) throws java.io.IOException;
        public org.openide.filesystems.FileObject getTarget();
    }
}
