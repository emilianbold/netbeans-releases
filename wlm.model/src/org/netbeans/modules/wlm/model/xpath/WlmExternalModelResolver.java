/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.wlm.model.xpath;

import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.modules.wlm.model.api.TImport;
import org.netbeans.modules.wlm.model.api.TTask;
import org.netbeans.modules.wlm.model.api.WLMModel;
import org.netbeans.modules.wlm.model.utl.ImportHelper;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.xpath.ext.spi.ExternalModelResolver;

/**
 * A default implementation of the ExternalModelResolver for a BPEL process.
 * 
 * @author nk160297
 */
public class WlmExternalModelResolver implements ExternalModelResolver {
    
    private WLMModel mWlmModel;
    
    public WlmExternalModelResolver(WLMModel wlmModel) {
        mWlmModel = wlmModel;
    }
    
    /**
     * Looks for not only imported models! 
     * @param modelNsUri
     * @return
     */
    public Collection<SchemaModel> getModels(String modelNsUri) {
        Collection<SchemaModel> result = new ArrayList<SchemaModel>();
        //
        TTask task = mWlmModel.getTask();
        if (task != null) {
            Collection<TImport> imports = mWlmModel.getTask().getImports();
            for(TImport imp : imports) {
                Collection<SchemaModel> collection = ImportHelper.
                    getInlineSchema( imp, modelNsUri );
                if ( collection!= null ){
                    result.addAll( collection );
                }
                if ( !modelNsUri.equals( imp.getNamespace() )){
                    continue;
                }
            }
        }
        return result;
    }

    public Collection<SchemaModel> getVisibleModels() {
        Collection<SchemaModel> result = new ArrayList<SchemaModel>();
        //
        TTask task = mWlmModel.getTask();
        if (task != null) {
            Collection<TImport> imports = mWlmModel.getTask().getImports();
            for(TImport imp : imports) {
                Collection<SchemaModel> collection = ImportHelper.getAllInlineSchema(imp);
                if ( collection!= null ){
                    result.addAll( collection );
                }
            }
        }
        return result;
    }

    public boolean isSchemaVisible(String schemaNamespaceUri) {
        Collection<SchemaModel> models = getVisibleModels();
        for (SchemaModel model : models) {
            Schema schema = model.getSchema();
            if (schema != null) {
                String nsCondidate = schema.getTargetNamespace();
                if (schemaNamespaceUri.equals(nsCondidate)) {
                    return true;
                }
            }
        }
        return false;
    }
}
