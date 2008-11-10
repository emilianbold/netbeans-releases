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
package org.netbeans.modules.bpel.core.completion;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import javax.xml.namespace.QName;

import org.netbeans.modules.bpel.core.BPELDataLoader;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.resources.ResourcePackageMarker;
import org.netbeans.modules.xml.schema.completion.spi.CompletionContext;
import org.netbeans.modules.xml.schema.completion.spi.CompletionModelProvider;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.openide.util.lookup.Lookups;

/**
 * 
 * @author ads
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.schema.completion.spi.CompletionModelProvider.class)
public class BpelCompletionModelProvider extends CompletionModelProvider {

    private static final String PROCESS = "process";            // NOI18N

    public List<CompletionModel> getModels(CompletionContext context) {
        // Fix for IZ#  93505
        if ( !isBpelFile( context) ) {
            return null;
        }
        SchemaModel model = createMetaSchemaModel();

        if(model == null) {
            return null;
        }
        CompletionModel complModel = new CompletionModelImpl( model );
        return  Collections.singletonList( complModel );
    }

    private SchemaModel createMetaSchemaModel() {
        try {
            InputStream in = ResourcePackageMarker.class
                    .getResourceAsStream(ResourcePackageMarker.WS_BPEL_SCHEMA);
            javax.swing.text.Document d = AbstractDocumentModel
                    .getAccessProvider().loadSwingDocument(in);
            ModelSource ms = new ModelSource(Lookups.singleton(d), false);
            SchemaModel m = SchemaModelFactory.getDefault()
                    .createFreshModel(ms);
            m.sync();
            return m;
        }
        catch (Exception ex) {
            return null;
        }
    }

    private boolean isBpelFile( CompletionContext context ) {
        List<QName> list = context.getPathFromRoot();
        if ( list!= null && list.size() >0 ) {
            QName qName = list.get( 0 );
            String root = qName.getLocalPart();
            String ns = qName.getNamespaceURI();
            if ( PROCESS.equals( root )  && BpelEntity.BUSINESS_PROCESS_NS_URI.equals(ns) ) {
                return true;
            }
        }
        return BPELDataLoader.PRIMARY_EXTENSION.equals( 
                context.getPrimaryFile().getExt() );
    }
}
