/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.xml.schema.completion.spi;

import java.util.List;
import org.netbeans.modules.xml.schema.model.SchemaModel;

/**
 * Schema aware code completion feature works, based on the "schemaLocation"
 * or "noNamespaceSchemaLocation" attribute of the root element of the XML
 * document. If a matching schema found, it uses the models for the schemas
 * to query completion items.
 *
 * CompletionModelProvider is a hook for XML documents that do not directly
 * conform to a schema either through "schemaLocation" or "noNamespaceSchemaLocation".
 * attribute but still want to use the schema aware code completion feature.
 * For example, if you want code completion in a WSDL document, all you need
 * to do is, implement a CompletionModelProvider and return the set of models
 * for WSDL's schema(s).
 *
 * @author Samaresh (Samaresh@Netbeans.Org)
 */
public abstract class CompletionModelProvider {
    
    /**
     * Returns a list of CompletionModels at a given context. Context may be
     * used in determining the list of CompletionModels. For example, it does
     * not make sense to return WSDL models while working on a non-WSDL file.
     */
    public abstract List<CompletionModel> getModels(CompletionContext context);
    
    /**
     * Class CompletionModel.
     */
    public static abstract class CompletionModel {
        
        /**
         * Returns the suggested prefix to be used for completion.
         */
        public abstract String getSuggestedPrefix();
        
        /**
         * Returns the target namespace for this schema model.
         */
        public abstract String getTargetNamespace();
        
        /**
         * Returns the schema model.
         */
        public abstract SchemaModel getSchemaModel();
    }
    
}
