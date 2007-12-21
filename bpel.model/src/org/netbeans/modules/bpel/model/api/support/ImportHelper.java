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
package org.netbeans.modules.bpel.model.api.support;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.LinkedList;

import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Import;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.wsdl.model.Types;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModelFactory;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.modules.xml.xam.locator.CatalogModelFactory;

/**
 * Helper class for accessing to WSDL or XSD models by import statement.
 * @author ads
 */
public final class ImportHelper {

    /**
     * This is helper class, we don't want its instance.  
     */
    private ImportHelper() {
    }
    
    /**
     * Returns wsdl model respectively given import <code>imp</code>.
     * @param model BPEL OM
     * @param location location uri
     * @param importType type of import
     * 
     */
    public static WSDLModel getWsdlModel( BpelModel model, String location,
           String importType  ) 
    {
        return getWsdlModel(model, location, importType, true );
    }
        

    /**
     * Returns wsdl model respectively given import <code>imp</code>.
     * @param model BPEL OM
     * @param location location uri
     * @param importType type of import
     * @param checkWellFormed if true method will return null if model is not valid
     */
    public static WSDLModel getWsdlModel( BpelModel model, String location, 
            String importType  , boolean checkWellFormed ) 

    {
        if (!Import.WSDL_IMPORT_TYPE.equals( importType)) {
            return null;
        }
        WSDLModel wsdlModel;
        if (location == null) {
            return null;
        }
        try {
            URI uri = new URI(location);
            ModelSource source = CatalogModelFactory.getDefault()
                    .getCatalogModel( model.getModelSource())
                    .getModelSource(uri, model.getModelSource());
            wsdlModel = WSDLModelFactory.getDefault().getModel(source);
        }
        catch (URISyntaxException e) {
            wsdlModel = null;
        }
        catch (CatalogModelException e) {
            wsdlModel = null;
        }
        if (wsdlModel != null && wsdlModel.getState() == 
            Model.State.NOT_WELL_FORMED && checkWellFormed ) 
        {
            return null;
        }
        return wsdlModel;
    }
    
    /**
     * Returns wsdl model respectively given import <code>imp</code>.
     * @param imp import statement in BPEL OM
     */
    public static WSDLModel getWsdlModel( Import imp ) {
        return getWsdlModel( imp , true );
    }
    
    /**
     * Returns wsdl model respectively given import <code>imp</code>.
     * @param imp import statement in BPEL OM
     * @param checkWellFormed if true method will return null if model is not valid 
     */
    public static WSDLModel getWsdlModel( Import imp , boolean checkWellFormed ) {
        return getWsdlModel( imp.getBpelModel() , imp.getLocation(), 
                imp.getImportType() , checkWellFormed );
    }
    
    /**
     * Returns schema model respectively <code>imp</code> import
     * statement in BPEL OM and <code>namespace</code>.
     * @param bpelModel BPEL OM
     * @param location location uri
     * @param importType type of import
     * @param namespace schema namespace
     */
    public static Collection<SchemaModel> getInlineSchema( BpelModel bpelModel, 
            String namespace , String location , String importType ) 
    {
        WSDLModel model = getWsdlModel( bpelModel, location, importType );
        if ( model == null ){
            return null;
        }
        Types types =  model.getDefinitions().getTypes();
        if ( types == null ){
            return null;
        }
        Collection<Schema> collection = types.getSchemas();
        Collection<SchemaModel> models = null;
        for (Schema schema : collection) {
            if ( namespace.equals( schema.getTargetNamespace() )){
                if ( models == null ){
                    models = new LinkedList<SchemaModel>();
                }
                models.add( schema.getModel() );
            }
        }
        return models;
    }

    /**
     * Returns schema model respectively <code>imp</code> import
     * statement in BPEL OM and <code>namespace</code>.
     * @param imp import statement in BPEL OM
     * @param namespace Namespace for desired inline schema model
     */
    public static Collection<SchemaModel> getInlineSchema( Import imp, 
            String namespace ) 
    {
        return getInlineSchema(imp.getBpelModel(), namespace, imp.getLocation(),
                imp.getImportType() );
    }
    
    /**
     * Returns schema model respectively given import <code>imp</code>.
     * @param model BPEL OM
     * @param location location uri
     * @param importType type of import
     */
    public static SchemaModel getSchemaModel( BpelModel model, String location,
            String importType ) 
    {
        return getSchemaModel( model, location, importType , true );
    }
    
    /**
     * Returns schema model respectively given import <code>imp</code>.
     * @param model BPEL OM
     * @param location location uri
     * @param importType type of import
     */
    public static SchemaModel getSchemaModel( BpelModel model, String location,
            String importType , boolean checkWellFormed ) 
    {
        if ( !Import.SCHEMA_IMPORT_TYPE.equals( importType)){
            return null;
        }
        SchemaModel schemaModel ;
        if (location == null) {
            return null;
        }
        try {
            URI uri = new URI( location );
            ModelSource modelSource = CatalogModelFactory.getDefault().
                        getCatalogModel(model.getModelSource())
                        .getModelSource(uri, model.getModelSource());
            
            schemaModel = SchemaModelFactory.getDefault().
                getModel( modelSource );
        }
        catch (URISyntaxException e) {
            schemaModel = null;
        }
        catch (CatalogModelException e) {
            schemaModel = null;
        }
        if (schemaModel != null && schemaModel.getState() == 
            Model.State.NOT_WELL_FORMED && checkWellFormed ) 
        {
            schemaModel = null;    
        }
        return schemaModel;
    }
    
    /**
     * Returns schema model respectively given import <code>imp</code>.
     * @param imp import statement in BPEL OM
     */
    public static SchemaModel getSchemaModel( Import imp , 
            boolean checkWellFormed ) 
    {
        return getSchemaModel(imp.getBpelModel(), imp.getLocation() , 
                imp.getImportType() , checkWellFormed );
    }

    /**
     * Returns schema model respectively given import <code>imp</code>.
     * @param imp import statement in BPEL OM
     */
    public static SchemaModel getSchemaModel( Import imp ) {
        return getSchemaModel(imp.getBpelModel(), imp.getLocation() , 
                imp.getImportType());
    }
}
