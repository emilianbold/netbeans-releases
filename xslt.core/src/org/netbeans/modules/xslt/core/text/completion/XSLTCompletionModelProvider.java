/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.xslt.core.text.completion;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import javax.swing.text.Document;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.schema.completion.spi.CompletionContext;
import org.netbeans.modules.xml.schema.completion.spi.CompletionModelProvider;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.netbeans.modules.xslt.core.XSLTDataLoader;
import org.netbeans.modules.xslt.model.XslComponent;
import org.openide.util.lookup.Lookups;

/**
 * @author Alex Petrov (05.05.2008)
 */
public class XSLTCompletionModelProvider extends CompletionModelProvider {
    private static final String 
        STYLESHEET_ELEMENT_NAME = "stylesheet", // NOI18N
        RESOURCES_DIR = "resources",
        FILE_XSLT_1_0_SCHEMA = "xslt_1_0.xsd", // NOI18N
        FILE_XSLT_1_1_SCHEMA = "xslt_1_1.xsd", // NOI18N
        FILE_XSLT_2_0_SCHEMA = "xslt_2_0.xsd"; // NOI18N

    public List<CompletionModel> getModels(CompletionContext context) {
        // Fix for IZ#  93505
        if (! isXsltFile(context)) return null;
        
        CompletionModel complModel = getCompletionModel();
        return  Collections.singletonList(complModel);
    }

    public static CompletionModel getCompletionModel() {
        SchemaModel model = createMetaSchemaModel();
        if (model == null) {
            return null;
        }
        CompletionModel completionModel = new XSLTCompletionModelImpl(model);
        return completionModel;
    }
    
    private boolean isXsltFile(CompletionContext context) {
        List<QName> list = context.getPathFromRoot();
        if ((list != null) && (! list.isEmpty())) {
            QName qName = list.get(0);
            String root = qName.getLocalPart();
            String nameSpace = qName.getNamespaceURI();
            if ((STYLESHEET_ELEMENT_NAME.equals(root)) && 
                (XslComponent.XSL_NAMESPACE.equals(nameSpace))) {
                return true;
            }
        }
        String fileExt = context.getPrimaryFile().getExt();
        return (XSLTDataLoader.PRIMARY_EXTENSION.equals(fileExt)) || 
               (XSLTDataLoader.PRIMARY_EXTENSION2.equals(fileExt));
    }

    private static SchemaModel createMetaSchemaModel() {
        InputStream inputStream = null;
        try {
            String resourcePath = RESOURCES_DIR + "/" + FILE_XSLT_1_0_SCHEMA;
            inputStream = XSLTDataLoader.class.getResourceAsStream(
                resourcePath);

            Document doc = AbstractDocumentModel.getAccessProvider().loadSwingDocument(inputStream);
            ModelSource modelSource = new ModelSource(Lookups.singleton(doc), false);
            SchemaModel schemaModel = SchemaModelFactory.getDefault().createFreshModel(
                modelSource);
            schemaModel.sync();
            return schemaModel;
        }
        catch (Exception ex) {
            return null;
        } finally {
            if (inputStream != null) {
                try { inputStream.close();} catch(Exception e) {}
            }
        }
    }
}