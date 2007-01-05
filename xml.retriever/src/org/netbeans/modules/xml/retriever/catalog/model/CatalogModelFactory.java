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
package org.netbeans.modules.xml.retriever.catalog.model;

import java.io.IOException;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.modules.xml.xam.AbstractModelFactory;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.retriever.catalog.model.impl.CatalogModelImpl;

public class CatalogModelFactory extends AbstractModelFactory<CatalogModel> {
    /**
     * Creates a new instance of CatalogModelFactory
     */
    private CatalogModelFactory() {
    }
    
    private static CatalogModelFactory instance = new CatalogModelFactory();
    
    public static CatalogModelFactory getInstance() {
        return instance;
    }
    
    protected CatalogModel createModel(ModelSource source) {
        return new CatalogModelImpl(source);
    }
    
    public static final String CATALOG_TEMPLATE = "<?xml version=\"1.0\" encoding=\"UTF-8\""
            +" standalone=\"no\"?>"+"\n"+
            "<catalog xmlns=\"urn:oasis:names:tc:entity:xmlns:xml:catalog\" prefer=\"system\"/>";
    
    public CatalogModel getModel(ModelSource source) {
        Document doc = (Document) source.getLookup().lookup(Document.class);
        if( (doc != null) && doc.getLength() <= 5){
            //means the catalog file is empty now
            try {
                doc.remove(0, doc.getLength());
                doc.insertString(0, CATALOG_TEMPLATE, null);
            } catch (BadLocationException ex) {
                return null;
            }
        }
        
        CatalogModel cm =(CatalogModel) super.getModel(source);
        try {
            cm.sync();
        } catch (IOException ex) {
        }
        return cm;
    }
}
