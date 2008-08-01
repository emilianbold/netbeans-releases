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

import org.netbeans.modules.xml.schema.completion.spi.CompletionModelProvider.CompletionModel;
import org.netbeans.modules.xml.schema.model.SchemaModel;

/**
 * @author Alex Petrov (05.05.2008)
 */
public class XSLTCompletionModelImpl extends CompletionModel {
    private static final String SCHEMA_ELEMENT_PREFIX = "xsl"; // NOI18N

    private SchemaModel xslSchemaModel;
    
    XSLTCompletionModelImpl(SchemaModel schemaModel) {
        xslSchemaModel = schemaModel;
    }
    
    public String getSuggestedPrefix() {
        return SCHEMA_ELEMENT_PREFIX; // NOI18N
    }
    
    public SchemaModel getSchemaModel() {
        return xslSchemaModel;
    }
    
    public String getTargetNamespace() {
        return (xslSchemaModel == null ? null :
                xslSchemaModel.getSchema().getTargetNamespace());
    }
}