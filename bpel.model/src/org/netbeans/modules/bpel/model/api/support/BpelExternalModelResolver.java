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

package org.netbeans.modules.bpel.model.api.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.references.SchemaReferenceBuilder;
import org.netbeans.modules.xml.catalogsupport.util.ProjectUtilities;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.xpath.ext.spi.ExternalModelResolver;
import org.openide.filesystems.FileObject;

/**
 * A default implementation of the ExternalModelResolver for a BPEL process.
 * 
 * @author nk160297
 */
public class BpelExternalModelResolver implements ExternalModelResolver {
    
    private BpelModel mBpelModel;
    
    public BpelExternalModelResolver(BpelModel bpelModel) {
        mBpelModel = bpelModel;
    }

    /**
     * Looks for not only imported models! 
     * @param modelNsUri
     * @return
     */
    public Collection<SchemaModel> getModels(String modelNsUri) {
        return SchemaReferenceBuilder.getSchemaModels(mBpelModel, modelNsUri, true);
    }

    public Collection<SchemaModel> getVisibleModels() {
        //
        // TODO: Only schema models from this and related projects are returned. 
        // It is necessary to add schema models from the "BPEL Global Catalog"
        // 
        Project project = Utils.safeGetProject(mBpelModel);
        List<FileObject> schemaFoList = ProjectUtilities.
                getXSDFilesRecursively(project, false);
        Collection<SchemaModel> resultList = new ArrayList<SchemaModel>();
        for (FileObject fo : schemaFoList) {
            SchemaModel sModel = Utils.getSchemaModel(fo);
            if (sModel != null) {
                resultList.add(sModel);
            }
        }
        //
        return resultList;
    }

    public boolean isSchemaVisible(String schemaNamespaceUri) {
        Collection<SchemaModel> models = getVisibleModels();
        for (SchemaModel model : models) {
            String nsCondidate = model.getSchema().getTargetNamespace();
            if (schemaNamespaceUri.equals(nsCondidate)) {
                return true;
            }
        }
        return false;
    }

}
