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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bpel.model;

import java.util.Collection;
import org.netbeans.modules.bpel.model.api.Import;
import org.netbeans.modules.bpel.model.api.references.RefCacheSupport;
import org.netbeans.modules.bpel.model.impl.BpelModelImpl;
import org.netbeans.modules.xml.schema.model.Include;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.impl.SchemaModelImpl;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Types;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.util.Exceptions;
import static org.junit.Assert.*;

/**
 * BPEL related utility methods
 *
 * WARNING: Be careful while moving the class!
 * It is used to access tests' resources.
 *
 * @author Nikita Krjukov
 */
public final class BpelTestUtils {

    /**
     * Recursively checks imports.
     *
     * @param model
     * @throws java.lang.Exception
     */
    public static void checkImports(Model model) {
        if (model instanceof BpelModelImpl) {
            BpelModelImpl bpelModel = BpelModelImpl.class.cast(model);
            Import[] importArr = bpelModel.getProcess().getImports();
            RefCacheSupport refCache = bpelModel.getRefCacheSupport();
            //
            for (int index = 0; index < importArr.length; index++) {
                Import imp = importArr[index];
                if (Import.SCHEMA_IMPORT_TYPE.equals(imp.getImportType())) {
                    SchemaModel schemaModel = refCache.optimizedSchemaResolve(imp);
                    // SchemaModel schemaModel = ImportHelper.getSchemaModel(imp);
                    assertNotNull(schemaModel);
                    checkImports(schemaModel);
                } else if (Import.WSDL_IMPORT_TYPE.equals(imp.getImportType())) {
                    WSDLModel wsdlModel = refCache.optimizedWsdlResolve(imp);
                    // WSDLModel wsdlModel = ImportHelper.getWsdlModel(imp);
                    assertNotNull(wsdlModel);
                    checkImports(wsdlModel);
                }
            }
        } else if (model instanceof SchemaModelImpl) {
            SchemaModelImpl sModel = SchemaModelImpl.class.cast(model);
            Schema schema = sModel.getSchema();
            //
            Collection<org.netbeans.modules.xml.schema.model.Import> imports =
                    schema.getImports();
            for (org.netbeans.modules.xml.schema.model.Import imp : imports) {
                SchemaModel resolved = sModel.resolve(imp);
                // SchemaModel resolved = imp.resolveReferencedModel();
                assertNotNull(resolved);
                checkImports(resolved);
            }
            //
            Collection<Include> includes = schema.getIncludes();
            for (Include incl : includes) {
                SchemaModel resolved = sModel.resolve(incl);
                // SchemaModel resolved = incl.resolveReferencedModel();
                assertNotNull(resolved);
                checkImports(resolved);
            }
        } else if (model instanceof WSDLModel) {
            Definitions def = WSDLModel.class.cast(model).getDefinitions();
            //
            Collection<org.netbeans.modules.xml.wsdl.model.Import> imports =
                    def.getImports();
            for (org.netbeans.modules.xml.wsdl.model.Import imp : imports) {
                try {
                    WSDLModel resolved = imp.getImportedWSDLModel();
                    assertNotNull(resolved);
                    checkImports(resolved);
                } catch (CatalogModelException ex) {
                    Exceptions.printStackTrace(ex);
                    assertTrue("Exception while resolving a WSDL import", false);
                }
            }
            //
            Types types = def.getTypes();
            if (types != null) {
                Collection<Schema> schemas = types.getSchemas();
                for (Schema schema : schemas) {
                    SchemaModel sModel = schema.getModel();
                    assertNotNull(sModel);
                    checkImports(sModel);
                }
            }
        } else {
            throw new RuntimeException(model.getClass().getSimpleName() +
                    " is unsupported model's class!"); // NOI18N
        }
    }

}
