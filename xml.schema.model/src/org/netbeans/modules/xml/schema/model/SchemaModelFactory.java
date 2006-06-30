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

package org.netbeans.modules.xml.schema.model;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.xml.xam.locator.api.ModelSource;
import org.netbeans.modules.xml.schema.model.impl.EmbeddedSchemaModelImpl;
import org.netbeans.modules.xml.schema.model.impl.SchemaModelImpl;
import org.netbeans.modules.xml.text.syntax.XMLKit;
import org.netbeans.modules.xml.xam.AbstractModelFactory;
import org.netbeans.modules.xml.xam.DocumentModel;
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
            d = getResourceAsDocument("primitiveTypesSchema.xsd"); //NOI18N
            m = new SchemaModelImpl(d);
            m.sync();
        } catch (BadLocationException ex) {
            throw new RuntimeException("writing into empty document failed"); //NOI18N
        } catch (IOException ex) {
            throw new RuntimeException("schema should be correct"); //NOI18N
        } 
        return m;
    } 
    private Document getResourceAsDocument(String path) throws IOException, BadLocationException {
        InputStream in = getClass().getResourceAsStream(path);
        Document sd = new BaseDocument(XMLKit.class, false);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        try {
            String line = null;
            while ((line = br.readLine()) != null) {
                sd.insertString(sd.getLength(), line+System.getProperty("line.separator"), null); // NOI18N
            }
        } finally {
            br.close();
        }
        return sd;
    }

    protected SchemaModel createModel(ModelSource modelSource) {
        return new SchemaModelImpl(modelSource);
    }
}
