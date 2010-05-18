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
import java.util.ArrayList;
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
public class ExternalModelRetrieverImpl implements ExternalModelRetriever {

    @SuppressWarnings("unchecked")
    public Collection<WSDLModel> getWSDLModels( BpelModel model, String namespace ) {
//System.out.println();
//System.out.println("///// RETRIEVER: get wsdl models: " + namespace +" " + model);

        if ( namespace == null ) {
            return Collections.EMPTY_LIST;
        }
        List<WSDLModel> list = new ArrayList<WSDLModel>();
        collectWsdlModelsViaImports(model, namespace, list);
//System.out.println("///// list: " + list.size());
        return list;
    }
    
    @SuppressWarnings("unchecked")
    public Collection<SchemaModel> getSchemaModels( BpelModel model, String namespace ) {
        if ( namespace == null ) {
            return Collections.EMPTY_LIST;
        }
        List<SchemaModel> list = new ArrayList<SchemaModel>();
        
        collectShemaModelsViaImports(model, namespace, list);
        return list;
    }

    private void collectWsdlModelsViaImports( BpelModel model, String namespace, List<WSDLModel> list) {
//System.out.println();
//System.out.println("///// collect: " + namespace + " " + model);
        Import[] imports = model.getProcess().getImports();
//System.out.println("///// imports: " + imports.length);

        for (Import imp : imports) {
//System.out.println("/////     see: " + imp + " " + imp.getNamespace());
            if (namespace.equals(imp.getNamespace()) && 
                    Import.WSDL_IMPORT_TYPE.equals( imp.getImportType())) {
                //
                WSDLModel wsdlModel = BpelModelImpl.class.cast(model).
                        getRefCacheSupport().optimizedWsdlResolve(imp);
//System.out.println("/////     model: " + wsdlModel);

                if (wsdlModel != null && wsdlModel.getState() == Model.State.VALID ){
                    list.add( wsdlModel);
                }
            }
        }
    }

    private void collectShemaModelsViaImports( BpelModel model, String namespace, List<SchemaModel> list) {
        Import[] imports = model.getProcess().getImports();

        for( Import imp : imports){
            if ( Import.WSDL_IMPORT_TYPE.equals( imp.getImportType()) ){
                // Fix for #78085
                Collection<SchemaModel> collection = ImportHelper.getInlineSchema( imp, namespace );

                if ( collection!= null ){
                    list.addAll( collection );
                }
            } else {
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

}
