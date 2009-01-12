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
package org.netbeans.modules.xslt.tmap.model.api.completion;

import java.util.Collections;
import java.util.List;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.schema.completion.spi.CompletionContext;
import org.netbeans.modules.xml.schema.completion.spi.CompletionModelProvider;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xslt.tmap.TMapConstants;
import org.netbeans.modules.xslt.tmap.model.api.TransformMap;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.schema.completion.spi.CompletionModelProvider.class)
public class TMapCompletionModelProvider extends CompletionModelProvider {

    @Override
    public List<CompletionModel> getModels(CompletionContext context) {
        if (!isTMapFile(context)) {
            return null;
        }

        SchemaModel schemaModel = getTMapSchemaModel();
        if (schemaModel == null) {
            return null;
        }
        
        CompletionModel compModel = new CompletionModelImpl(schemaModel);
        return Collections.singletonList(compModel);
    }

    private boolean isTMapFile(CompletionContext context) {
        FileObject fo = context.getPrimaryFile();
        if (!TMapConstants.TRANSFORMMAP_XML.equals(fo.getNameExt())) {
            return false;
        }
    
//        List<QName> qnames = context.getPathFromRoot();
//        if (qnames != null && qnames.size() > 0) {
//            QName qName = qnames.get(0);
//            if (qName != null) {
//                String root = qName.getLocalPart();
//                String ns = qName.getNamespaceURI();
//                if (TransformMap.TRANSFORM_MAP_NS_URI.equals(ns) 
//                        && TransformMap.TYPE.getTagName().equals(root)) 
//                {
//                    return true;
//                }
//            }
//        }
//
        // TODO m
        if (TransformMap.TRANSFORM_MAP_NS_URI.equals(context.getDefaultNamespace())) {
            return true;
        }
        
        return false;
    }
    
    private SchemaModel getTMapSchemaModel() {
        try {
            
            FileObject tmapSchemaFo = FileUtil.getConfigFile("org-netbeans-xsltpro/transformmap.xsd"); // NOI18N
            if (tmapSchemaFo == null) {
                return null;
            }
            ModelSource ms = Utilities.getModelSource(tmapSchemaFo, false);
            if (ms == null) {
                return null;
            }
            SchemaModel model = SchemaModelFactory.getDefault().createFreshModel(ms);
            model.sync();
            return model;
        }
        catch (Exception ex) {
            return null;
        }
    }
    
}
