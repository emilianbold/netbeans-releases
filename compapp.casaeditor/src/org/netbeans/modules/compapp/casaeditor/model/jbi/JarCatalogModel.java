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

package org.netbeans.modules.compapp.casaeditor.model.jbi;

import java.io.IOException;
import org.netbeans.modules.xml.xam.locator.CatalogModel;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.modules.xml.xam.ModelSource;
import java.net.URI;
import java.util.HashMap;
import java.io.File;
import org.netbeans.modules.compapp.casaeditor.Constants;
import org.w3c.dom.ls.LSInput;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Jar Catalgo Model
 *
 * @author tli
 */
public class JarCatalogModel implements CatalogModel {

    private HashMap srcs = new HashMap();
    private HashMap nss = new HashMap();

    /**
     * Add model source with key
     *
     * @param loc source uri key
     * @param ms model source
     */
    public void addModelSource(URI loc, ModelSource ms) {
        srcs.put(loc, ms);
    }

    public void addNSModelSource(String ns, ModelSource ms) {
        nss.put(ns, ms);
    }

    private Object lookup(URI uri) {
        Object ms = srcs.get(uri);
        if (ms == null) {
            ms = nss.get(uri.toString());
        }
        return ms;
    }

    private URI normalizeURI(URI uri, ModelSource ms) {
        if (!uri.isAbsolute()) {
            // if a relative uri, try to normalize using referencing model's file path
            try {
                File f = (File) ms.getLookup().lookup(File.class);
                String path = f.getParentFile().getPath().replace('\\', '/') + Constants.FORWARD_SLASH + uri.toString();
                int idx = path.indexOf("build/SEDeployment.jar"); // NOI18N
                if (idx > 0) { // OK, needs to strip off Chris W. fixes..
                    path = f.getParentFile().getParentFile().getPath().replace('\\', '/') + Constants.FORWARD_SLASH + uri.toString(); // NOI18N
                    path = path.substring(idx+23);
                }
                return new URI(path);
            } catch (Exception ex) {
                // failed to generate normalized uri..
            }
        }
        return uri;
    }

    public ModelSource getModelSource(URI locationURI, ModelSource modelSourceOfSourceDocument) throws CatalogModelException {
        // try lookup using the uri as is..
        Object ms = lookup(locationURI);

        // if not found, try normalize the uri.
        if (ms == null) {
            ms = lookup(normalizeURI(locationURI, modelSourceOfSourceDocument));
        }
        return ((ms != null) ? ((ModelSource) ms) : null);
    }

    public ModelSource getModelSource(URI locationURI) throws CatalogModelException {
        return getModelSource(locationURI, null);
    }

    
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        //TODO FIXME: this is to fix a build break. Please implement this method.
        return null;
    }

    public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
        //TODO FIXME: this is to fix a build break. Please implement this method.
        return null;
    }

}
