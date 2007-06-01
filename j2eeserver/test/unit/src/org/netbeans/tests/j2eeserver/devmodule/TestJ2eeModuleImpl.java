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

package org.netbeans.tests.j2eeserver.devmodule;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import org.netbeans.modules.j2ee.dd.api.web.WebAppMetadata;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleImplementation;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.spi.MetadataModelFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.xml.sax.SAXException;
import org.netbeans.modules.j2ee.metadata.model.api.SimpleMetadataModelImpl;

/**
 *
 * @author  sherold
 */
public class TestJ2eeModuleImpl implements J2eeModuleImplementation {
    
    private final FileObject webAppRoot;
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private final MetadataModel<WebAppMetadata> webAppMetadata;

    /** Creates a new instance of TestJ2eeModule */
    public TestJ2eeModuleImpl(FileObject webAppRoot) throws IOException, SAXException {
        this.webAppRoot = webAppRoot;
        webAppMetadata = MetadataModelFactory.createMetadataModel(new SimpleMetadataModelImpl<WebAppMetadata>());
    }

    public FileObject getArchive() {
        return null;
    }
    
    public Iterator getArchiveContents() {
        return new java.util.Vector(0).iterator();
    }
    
    public FileObject getContentDirectory() {
        return webAppRoot;
    }
    
    public Object getModuleType() {
        return J2eeModule.EJB;
    }
    
    public String getModuleVersion() {
        return J2eeModule.JAVA_EE_5;
    }
    
    public String getUrl() {
        return null;
    }
    
    public void setUrl(String url) {
        //noop
    }

    public File getResourceDirectory() {
        return new File(FileUtil.toFile(webAppRoot), "resources");
    }

    public File getDeploymentConfigurationFile(String name) {
        if (name.equals(J2eeModule.WEB_XML)) {
            return new File(FileUtil.toFile(webAppRoot), name);
        } else {
            return null;
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }
    
    public void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }

    public <T> MetadataModel<T> getDeploymentDescriptor(Class<T> type) {
        if (type == WebAppMetadata.class) {
            return (MetadataModel<T>) webAppMetadata;
        } else {
            return null;
        }
    }
}
