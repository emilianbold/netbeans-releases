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
package org.netbeans.modules.xml.schema.completion.util;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.xml.schema.completion.spi.CompletionContext;
import org.netbeans.modules.xml.schema.completion.spi.CompletionModelProvider;
import org.netbeans.modules.xml.schema.completion.spi.CompletionModelProvider.CompletionModel;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.openide.util.lookup.Lookups;

/**
 * Helps in getting the model for code completion.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class MetaSchemaModelProvider extends CompletionModelProvider {
    
    private CompletionContextImpl context;
    
    public MetaSchemaModelProvider() {
    }

    /**
     * Returns a list of CompletionModel. Default implementation looks for
     * schemaLocation attribute in the document and if specified creates model
     * for each schema mentioned in there.
     */    
    public List<CompletionModel> getModels(CompletionContext context) {
        if(context == null ||
           context.getPrimaryFile() == null ||
           context.getPrimaryFile().getExt() == null ||
           !"xsd".equals(context.getPrimaryFile().getExt())) //NOI18N
            return null;
        SchemaModel sm = createMetaSchemaModel();
        if(sm == null)
            return null;        
        CompletionModel cm = new CompletionModelEx((CompletionContextImpl)context, "xsd", sm); //NOI18N
        List<CompletionModel> models = new ArrayList<CompletionModel>();
        models.add(cm);
        return models;
    }
    
    private SchemaModel createMetaSchemaModel() {
        try {
            InputStream in = getClass().getResourceAsStream("XMLSchema.xsd"); //NOI18N
            javax.swing.text.Document d = AbstractDocumentModel.
                    getAccessProvider().loadSwingDocument(in);
	    ModelSource ms = new ModelSource(Lookups.singleton(d), false);
            SchemaModel m = SchemaModelFactory.getDefault().createFreshModel(ms);
            m.sync();
            return m;
        } catch (Exception ex) {
            //just catch
        } 
        return null;
    }
        
}
