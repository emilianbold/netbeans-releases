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

package org.netbeans.modules.ruby.spi.project.support.rake;

import java.io.IOException;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.CacheDirectoryProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.w3c.dom.Element;

/**
 * Manages extensible (freeform) metadata in an Ant-based project.
 * @author Jesse Glick
 */
final class ExtensibleMetadataProviderImpl implements AuxiliaryConfiguration, CacheDirectoryProvider {

    /**
     * Relative path from project directory to the required private cache directory.
     */
    private static final String CACHE_PATH = "nbproject/private/cache"; // NOI18N
    
    private final RakeProjectHelper helper;
    
    ExtensibleMetadataProviderImpl(RakeProjectHelper helper) {
        this.helper = helper;
    }
    
    public FileObject getCacheDirectory() throws IOException {
        return FileUtil.createFolder(helper.getProjectDirectory(), CACHE_PATH);
    }
    
    public Element getConfigurationFragment(String elementName, String namespace, boolean shared) {
        if (elementName == null || elementName.indexOf(':') != -1 || namespace == null) {
            throw new IllegalArgumentException("Illegal elementName and/or namespace"); // NOI18N
        }
        return helper.getConfigurationFragment(elementName, namespace, shared);
    }
    
    public void putConfigurationFragment(Element fragment, boolean shared) throws IllegalArgumentException {
        if (fragment.getNamespaceURI() == null || fragment.getNamespaceURI().length() == 0) {
            throw new IllegalArgumentException("Illegal elementName and/or namespace"); // NOI18N
        }
        if (fragment.getLocalName().equals(helper.getType().getPrimaryConfigurationDataElementName(shared)) &&
                fragment.getNamespaceURI().equals(helper.getType().getPrimaryConfigurationDataElementNamespace(shared))) {
            throw new IllegalArgumentException("elementName + namespace reserved for project's primary configuration data"); // NOI18N
        }
        helper.putConfigurationFragment(fragment, shared);
    }
    
    public boolean removeConfigurationFragment(String elementName, String namespace, boolean shared) throws IllegalArgumentException {
        if (elementName == null || elementName.indexOf(':') != -1 || namespace == null) {
            throw new IllegalArgumentException("Illegal elementName and/or namespace"); // NOI18N
        }
        if (elementName.equals(helper.getType().getPrimaryConfigurationDataElementName(shared)) &&
                namespace.equals(helper.getType().getPrimaryConfigurationDataElementNamespace(shared))) {
            throw new IllegalArgumentException("elementName + namespace reserved for project's primary configuration data"); // NOI18N
        }
        return helper.removeConfigurationFragment(elementName, namespace, shared);
    }
    
}
