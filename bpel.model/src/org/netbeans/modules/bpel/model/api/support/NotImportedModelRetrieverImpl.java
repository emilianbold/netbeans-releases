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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.netbeans.api.project.Project;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.xam.spi.NotImportedModelRetriever;
import org.netbeans.modules.xml.reference.ReferenceUtil;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.filesystems.FileObject;

/**
 * Looks for all schema models in current project and all related projects 
 * 
 * @author nk160297
 */
public class NotImportedModelRetrieverImpl implements NotImportedModelRetriever {

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.xam.spi.ExternalModelRetriever#getWSDLModels(org.netbeans.modules.bpel.model.api.BpelModel, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public Collection<WSDLModel> getWSDLModels(BpelModel model, String namespace) {
        return Collections.EMPTY_LIST;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.xam.spi.ExternalModelRetriever#getSchemaModels(org.netbeans.modules.bpel.model.api.BpelModel, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public Collection<SchemaModel> getSchemaModels(BpelModel model, String namespace) {
        if (namespace == null) {
            return Collections.EMPTY_LIST;
        }
        //
        //
        // TODO: Only schema models from this and related projects are returned. 
        // It is necessary to add schema models from the "BPEL Global Catalog"
        // 
        Project project = Utils.safeGetProject(model);
        List<FileObject> schemaFoList = ReferenceUtil.getXSDFilesRecursively(project, false);
        Collection<SchemaModel> resultList = new ArrayList<SchemaModel>();

        for (FileObject fo : schemaFoList) {
            SchemaModel sModel = Utils.getSchemaModel(fo);
            if (sModel != null) {
                Schema schema = sModel.getSchema();
                if (schema != null) {
                    String modelNs = sModel.getEffectiveNamespace(schema);
                    if (namespace.equals(modelNs)) {
                        resultList.add(sModel);
                    }
                }
            }
        }
        return resultList;
    }
}
