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

package org.netbeans.spi.project.support.ant;

import java.io.IOException;
import org.netbeans.spi.project.ExtensibleMetadataProvider;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.w3c.dom.Element;

/**
 * Manages extensible (freeform) metadata in an Ant-based project.
 * @author Jesse Glick
 */
final class ExtensibleMetadataProviderImpl implements ExtensibleMetadataProvider {
    
    /**
     * Relative path from project directory to the required private cache directory.
     */
    private static final String CACHE_PATH = "nbproject/private/cache"; // NOI18N
    
    private final AntProjectHelper helper;
    
    ExtensibleMetadataProviderImpl(AntProjectHelper helper) {
        this.helper = helper;
    }
    
    public FileObject getCacheDirectory() {
        try {
            return FileUtil.createFolder(helper.getProjectDirectory(), CACHE_PATH);
        } catch (IOException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            return null;
        }
    }
    
    public boolean supportsConfigurationFragments() {
        return true;
    }
    
    public Element getConfigurationFragment(String elementName, String namespace, boolean shared) throws UnsupportedOperationException {
        if (elementName == null || elementName.indexOf(':') != -1 || namespace == null) {
            throw new IllegalArgumentException("Illegal elementName and/or namespace"); // NOI18N
        }
        return helper.getConfigurationFragment(elementName, namespace, shared);
    }
    
    public void putConfigurationFragment(Element fragment, boolean shared) throws UnsupportedOperationException, IllegalArgumentException {
        if (fragment.getNamespaceURI() == null || fragment.getNamespaceURI().length() == 0) {
            throw new IllegalArgumentException("Illegal elementName and/or namespace"); // NOI18N
        }
        if (fragment.getLocalName().equals(helper.getType().getPrimaryConfigurationDataElementName(shared)) &&
                fragment.getNamespaceURI().equals(helper.getType().getPrimaryConfigurationDataElementNamespace(shared))) {
            throw new IllegalArgumentException("elementName + namespace reserved for project's primary configuration data"); // NOI18N
        }
        helper.putConfigurationFragment(fragment, shared);
    }
    
    public boolean removeConfigurationFragment(String elementName, String namespace, boolean shared) throws UnsupportedOperationException, IllegalArgumentException {
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
