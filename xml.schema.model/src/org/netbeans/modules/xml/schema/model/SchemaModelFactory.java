/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.xml.schema.model;
import java.io.IOException;
import java.io.InputStream;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.schema.model.impl.EmbeddedSchemaModelImpl;
import org.netbeans.modules.xml.schema.model.impl.SchemaModelImpl;
import org.netbeans.modules.xml.xam.AbstractModelFactory;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.netbeans.modules.xml.xam.dom.DocumentModel;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.w3c.dom.Element;

/**
 *
 * @author Vidhya Narayanan
 */
public class SchemaModelFactory extends AbstractModelFactory<SchemaModel> {
    
    private static final SchemaModelFactory schemaModelFactory =
        new SchemaModelFactory();
    
    private SchemaModel primitiveTypesSchema;
    
    /**
     * Hidden constructor to create singleton SchemaModelFactory
     */
    private SchemaModelFactory() {
    }
    
    public static SchemaModelFactory getDefault() {
        return schemaModelFactory;
    }
     
    public SchemaModel createEmbeddedSchemaModel(DocumentModel embeddingModel, Element schemaElement){
        return new EmbeddedSchemaModelImpl(embeddingModel, schemaElement);
    }
    
    public synchronized SchemaModel getPrimitiveTypesModel() {
        if (primitiveTypesSchema == null) {
            primitiveTypesSchema = createPrimitiveSchemaModel();
        }
        return primitiveTypesSchema;
    }
    
    private SchemaModel createPrimitiveSchemaModel() {
        javax.swing.text.Document d;
        SchemaModel m;
        try {
            InputStream in = getClass().getResourceAsStream("primitiveTypesSchema.xsd"); //NOI18N
            d = AbstractDocumentModel.getAccessProvider().loadSwingDocument(in);
	    ModelSource ms = 
		new ModelSource(Lookups.singleton(d), false);
            m = new SchemaModelImpl(ms);
            m.sync();
        } catch (BadLocationException ex) {
            throw new RuntimeException("writing into empty document failed",ex); //NOI18N
        } catch (IOException ex) {
            throw new RuntimeException("schema should be correct",ex); //NOI18N
        } 
        return m;
    } 

    /**
     * Get model from given model source.  Model source should at very least 
     * provide lookup for:
     * 1. FileObject of the model source
     * 2. DataObject represent the model
     * 3. Swing Document buffer for in-memory text of the model source
     */
    public SchemaModel getModel(ModelSource modelSource) {
        Lookup lookup = modelSource.getLookup();
        assert lookup.lookup(Document.class) != null;
        return super.getModel(modelSource);
    }
    
    protected SchemaModel createModel(ModelSource modelSource) {
        return new SchemaModelImpl(modelSource);
    }
}
