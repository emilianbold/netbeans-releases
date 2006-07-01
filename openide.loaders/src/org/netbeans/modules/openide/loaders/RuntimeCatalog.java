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

package org.netbeans.modules.openide.loaders;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import org.openide.xml.EntityCatalog;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Implements non-persistent catalog functionality as EntityResolver.
 * <p>Registations using this resolver are:
 * <li>transient
 * <li>of the hihgest priority
 * <li>last registration prevails
 */
public final class RuntimeCatalog extends EntityCatalog {

    /** Public constructor for lookup. */
    public RuntimeCatalog() {}
    
    // table mapping public IDs to (local) URIs
    private Map/*<String,String>*/ id2uri;
    
    // tables mapping public IDs to resources and classloaders
    private Map/*<String,String>*/ id2resource;
    private Map/*<String,ClassLoader>*/ id2loader;
    
    /** SAX entity resolver */
    public InputSource resolveEntity(String name, String uri) throws IOException, SAXException {
        
        InputSource retval;
        String mappedURI = name2uri(name);
        InputStream stream = mapResource(name);
        
        // prefer explicit URI mappings, then bundled resources...
        if (mappedURI != null) {
            retval = new InputSource(mappedURI);
            retval.setPublicId(name);
            return retval;
            
        } else if (stream != null) {
            // XXX unused var, what is it for?
            uri = "java:resource:" + (String) id2resource.get(name); // NOI18N
            retval = new InputSource(stream);
            retval.setPublicId(name);
            return retval;
            
        } else {
            return null;
        }
    }
    
    public void registerCatalogEntry(String publicId, String uri) {
        if (id2uri == null) {
            id2uri = new HashMap();
        }
        id2uri.put(publicId, uri);
    }
    
    /** Map publicid to a resource accessible by a classloader. */
    public void registerCatalogEntry(String publicId, String resourceName, ClassLoader loader) {
        if (id2resource == null) {
            id2resource = new HashMap();
        }
        id2resource.put(publicId, resourceName);
        
        if (loader != null) {
            if (id2loader == null) {
                id2loader = new HashMap();
            }
            id2loader.put(publicId, loader);
        }
    }
    
    // maps the public ID to an alternate URI, if one is registered
    private String name2uri(String publicId) {
        
        if (publicId == null || id2uri == null) {
            return null;
        }
        return (String) id2uri.get(publicId);
    }
    
    
    // return the resource as a stream
    private InputStream mapResource(String publicId) {
        if (publicId == null || id2resource == null) {
            return null;
        }
        
        String resourceName = (String) id2resource.get(publicId);
        ClassLoader loader = null;
        
        if (resourceName == null) {
            return null;
        }
        
        if (id2loader != null) {
            loader = (ClassLoader) id2loader.get(publicId);
        }
        
        if (loader == null) {
            return ClassLoader.getSystemResourceAsStream(resourceName);
        }
        return loader.getResourceAsStream(resourceName);
    }
    
}
