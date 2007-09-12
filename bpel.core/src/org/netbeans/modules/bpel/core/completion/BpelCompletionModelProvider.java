/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
            if ( PROCESS.equals( root )  && 
                    BpelEntity.BUSINESS_PROCESS_NS_URI.equals(ns) ) 
            {
                return true;
            }
        }
        return BPELDataLoader.PRIMARY_EXTENSION.equals( 
                context.getPrimaryFile().getExt() );
    }
}
