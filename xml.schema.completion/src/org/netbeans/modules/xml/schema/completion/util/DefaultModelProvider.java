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

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.schema.completion.spi.CompletionContext;
import org.netbeans.modules.xml.schema.completion.spi.CompletionModelProvider;
import org.netbeans.modules.xml.schema.completion.spi.CompletionModelProvider.CompletionModel;
import org.netbeans.modules.xml.schema.completion.util.CatalogModelProvider;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.locator.CatalogModel;
import org.netbeans.modules.xml.xam.locator.CatalogModelFactory;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * Helps in getting the model for code completion.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class DefaultModelProvider extends CompletionModelProvider {
    
    private CompletionContextImpl context;
    
    public DefaultModelProvider() {        
    }

    /**
     * Returns a list of CompletionModel. Default implementation looks for
     * schemaLocation attribute in the document and if specified creates model
     * for each schema mentioned in there.
     */    
    public List<CompletionModel> getModels(CompletionContext context) {
        this.context = (CompletionContextImpl)context;
        List<URI> uris = context.getSchemas();
        if(uris == null || uris.size() == 0)
            return null;
        List<CompletionModel> models = new ArrayList<CompletionModel>();
        for(URI uri : context.getSchemas()) {
            CompletionModel model = getCompletionModel(uri);
            if(model != null)
                models.add(model);
        }
        
        return models;
    }
    
    private CompletionModel getCompletionModel(URI schemaURI) {
        CompletionModel model = null;
        try {
            ModelSource modelSource = null;
            CatalogModel catalogModel = null;
            CatalogModelProvider catalogModelProvider = getCatalogModelProvider();
            if(catalogModelProvider == null) {
                modelSource = Utilities.getModelSource(context.getPrimaryFile(), true);
                CatalogModelFactory factory = CatalogModelFactory.getDefault();
                catalogModel = factory.getCatalogModel(modelSource);
            } else {
                //purely for unit testing purposes.
                modelSource = catalogModelProvider.getModelSource(context.getPrimaryFile(), true);
                catalogModel = catalogModelProvider.getCatalogModel();
            }
            ModelSource schemaModelSource;
            schemaModelSource = catalogModel.getModelSource(schemaURI, modelSource);
            SchemaModel sm = null;
            if(schemaModelSource.getLookup().lookup(FileObject.class) == null) {
                sm = SchemaModelFactory.getDefault().createFreshModel(schemaModelSource);
            } else {
                sm = SchemaModelFactory.getDefault().getModel(schemaModelSource);
            }
            String tns = sm.getSchema().getTargetNamespace();
            List<String> prefixes = CompletionUtil.getPrefixesAgainstTargetNamespace(
                    context, tns);
            if(prefixes != null && prefixes.size() > 0)
                model = new CompletionModelEx(context, prefixes.get(0), sm);
            else
                model = new CompletionModelEx(context, context.suggestPrefix(tns), sm);
        } catch (Exception ex) {
            //no model for exception
        }
        return model;
    }
    
    /**
     * Uses lookup to find all CatalogModelProvider. If found uses the first one,
     * else returns null. This is purely to solve the problem of not being able to
     * use TestCatalogModel from unit tests.
     *
     * During actual CC from IDE, this will return null.
     */
    private CatalogModelProvider getCatalogModelProvider() {
        Lookup.Template templ = new Lookup.Template(CatalogModelProvider.class);
        Lookup.Result result = Lookup.getDefault().lookup(templ);
        Collection impls = result.allInstances();
        if(impls == null || impls.size() == 0)
            return null;
        
        return (CatalogModelProvider)impls.iterator().next();
    }
    
}
