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
package org.netbeans.modules.bpel.model.impl.references;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Import;
import org.netbeans.modules.bpel.model.api.support.ImportHelper;
import org.netbeans.modules.bpel.model.impl.BpelModelImpl;
import org.netbeans.modules.bpel.model.xam.spi.ExternalModelRetriever;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.Model.State;

/**
 * @author ads
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.bpel.model.xam.spi.ExternalModelRetriever.class)
public class ExternalModelRetrieverImpl implements ExternalModelRetriever {


    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.xam.spi.ExternalModelRetriever#getWSDLModels(org.netbeans.modules.bpel.model.api.BpelModel, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public Collection<WSDLModel> getWSDLModels( BpelModel model,
            String namespace )
    {
        if ( namespace == null ) {
            return Collections.EMPTY_LIST;
        }
        List<WSDLModel> list = new LinkedList<WSDLModel>();
        collectWsdlModelsViaImports(model, namespace, list);

        //collectWsdlModelsViaFS(model, namespace, list);
        
        return list;

    }
    

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.xam.spi.ExternalModelRetriever#getSchemaModels(org.netbeans.modules.bpel.model.api.BpelModel, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public Collection<SchemaModel> getSchemaModels( BpelModel model,
            String namespace )
    {
        if ( namespace == null ) {
            return Collections.EMPTY_LIST;
        }
        List<SchemaModel> list = new LinkedList<SchemaModel>();
        
        collectShemaModelsViaImports(model, namespace, list);
        //collectSchemaModelsViaFS(model, namespace, list);
        return list;

    }

    /*private void collectWsdlModelsViaFS( BpelModel model, String namespace, 
            List<WSDLModel> list ) 
    {
        FileObject[] files = Utils.getFilesByNamespace( model.getModelSource() , 
                namespace , DocumentTypesEnum.wsdl );
        for (FileObject file : files) {
            WSDLModel wsdlModel;
            try {
                ModelSource modelSource = Utilities.getModelSource( file , true );
                wsdlModel = WSDLModelFactory.getDefault().
                    getModel( modelSource );
            }
            catch (IOException e) {
                // The model that we trying to access is possibly broken.
                // SO we skip it in out list.
                wsdlModel = null;
            }
            
            if ( wsdlModel != null ){
                list.add( wsdlModel );
            }
        }
    }*/

    private void collectWsdlModelsViaImports( BpelModel model, String namespace, 
            List<WSDLModel> list ) 
    {
        Import[] imports = model.getProcess().getImports();
        for (Import imp : imports) {
            if ( namespace.equals(imp.getNamespace()) ){
                WSDLModel wsdlModel = BpelModelImpl.class.cast(model).
                        getRefCacheSupport().optimizedWsdlResolve(imp);
                if ( wsdlModel!= null && wsdlModel.getState() == Model.State.VALID ){
                    list.add( wsdlModel );
                }
            }
        }
    }


    /*private void collectSchemaModelsViaFS( BpelModel model, String namespace, 
            List<SchemaModel> list ) 
    {
        FileObject[] files = Utils.getFilesByNamespace( model.getModelSource() ,
                namespace , DocumentTypesEnum.schema );
        for (FileObject file : files) {
            SchemaModel schemaModel ;
            try {
                ModelSource modelSource = Utilities.getModelSource( file , true );
                schemaModel = SchemaModelFactory.getDefault().
                    getModel( modelSource );
            }
            catch (IOException e) {
                // The model that we trying to access is possibly broken.
                // SO we skip it in out list.
                schemaModel = null;
            }
            if ( schemaModel != null && model.getState() == Model.State.VALID){
                list.add( schemaModel );
            }
        }
    }*/

    private void collectShemaModelsViaImports( BpelModel model, String namespace, 
            List<SchemaModel> list ) 
    {
        Import[] imports = model.getProcess().getImports();
        for( Import imp : imports){
            if ( Import.WSDL_IMPORT_TYPE.equals( imp.getImportType()) ){
                // Fix for #78085
                Collection<SchemaModel> collection = ImportHelper.
                    getInlineSchema( imp, namespace );
                if ( collection!= null ){
                    list.addAll( collection );
                }
            }
            if ( !namespace.equals( imp.getNamespace() )){
                continue;
            }
            SchemaModel schemaModel = BpelModelImpl.class.cast(model).
                    getRefCacheSupport().optimizedSchemaResolve(imp);
            if ( schemaModel != null && schemaModel.getState() == State.VALID ){
                list.add( schemaModel );
            }
        }
    }

}
