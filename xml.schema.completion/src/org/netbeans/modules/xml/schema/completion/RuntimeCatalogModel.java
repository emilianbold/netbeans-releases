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

/*
 * RuntimeCatalogModel.java
 *
 * Created on January 18, 2007, 2:59 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.completion;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.xml.services.UserCatalog;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.netbeans.modules.xml.xam.locator.CatalogModel;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.util.lookup.Lookups;
import org.w3c.dom.ls.LSInput;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author girix
 */
public class RuntimeCatalogModel implements CatalogModel{
    
    /** Creates a new instance of RuntimeCatalogModel */
    public RuntimeCatalogModel() {
    }
    
    public ModelSource getModelSource(URI locationURI) throws CatalogModelException {
        throw new RuntimeException("Method not implemented"); //NOI18N
    }
    
    public ModelSource getModelSource(URI locationURI,
            ModelSource modelSourceOfSourceDocument) throws CatalogModelException {
        InputSource isrc;
        try {
            isrc = UserCatalog.getDefault().getEntityResolver().
                    resolveEntity(null, locationURI.toString());
            InputStream is = new URL(isrc.getSystemId()).openStream();
            if(is != null)
                return createModelSource(is);
        } catch (Exception ex) {
            throw new CatalogModelException(ex);
        }
        
        return null;
    }
    
    private ModelSource createModelSource(InputStream is) throws CatalogModelException{
        try {
            Document d = AbstractDocumentModel.getAccessProvider().loadSwingDocument(is);
            if(d != null)
                return new ModelSource(Lookups.fixed(new Object[]{this,d}), false);
        } catch (Exception ex) {
            throw new CatalogModelException(ex);
        }
                
        return null;
    }
    
    public InputSource resolveEntity(String publicId,
            String systemId) throws SAXException, IOException {
        throw new RuntimeException("Method not implemented"); //NOI18N
    }
    
    public LSInput resolveResource(String type, String namespaceURI,
            String publicId, String systemId, String baseURI) {
        throw new RuntimeException("Method not implemented"); //NOI18N
    }
    
}
