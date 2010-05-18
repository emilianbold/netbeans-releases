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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.xml.xpath.ext.schema;

import java.util.LinkedList;
import java.util.List;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;

/**
 * Collects an ordered chain on Schema Models. 
 * Such chain is required to properly calculate the effective namespace of 
 * a global element, when it is located inside of a schema, which doesn't have
 * a target namespace attribute. 
 * 
 * @author nk160297
 */
public class SchemaModelsStack {

    // The last added item is the first in the list
    private LinkedList<SchemaModel> mSModelsList = new LinkedList<SchemaModel>();
    
    public void appendSchemaModel(SchemaModel sModel) {
        if (sModel != null) {
            if (mSModelsList.isEmpty()) {
                mSModelsList.addFirst(sModel);
            } else {
                SchemaModel lastAdded = mSModelsList.getFirst();
                if (lastAdded == null || !sModel.equals(lastAdded)) {
                    mSModelsList.addFirst(sModel);
                }
            }
        }
    }
    
    public void appendSchemaComponent(SchemaComponent sComponent) {
        if (sComponent != null) {
            SchemaModel sModel = sComponent.getModel();
            appendSchemaModel(sModel);
        }
    }
    
    public void discard() {
        mSModelsList.clear();
    }
    
    public List<SchemaModel> getSchemaModelsList() {
        return mSModelsList;
    }
    
    public static String getEffectiveNamespace(SchemaComponent sComp, SchemaModelsStack stack) {
        SchemaModel ownerModel = sComp.getModel();
        String namespaceUri = ownerModel.getEffectiveNamespace(sComp);
        //
        if (namespaceUri != null && namespaceUri.length() != 0) {
            return namespaceUri;
        }
        //
        // The target namespace isn't defined
        List<SchemaModel> modelsList = stack.getSchemaModelsList();
        for (SchemaModel model : modelsList) {
            //
            // Try asking the effective namespace from the parent schema
            namespaceUri = model.getEffectiveNamespace(sComp);
            if (namespaceUri != null && namespaceUri.length() != 0) {
                return namespaceUri;
            }
        }
        //
        return null;
    }
    
    @Override
    public String toString() {
        return mSModelsList.toString();
    }
}
