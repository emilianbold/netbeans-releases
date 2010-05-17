/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.wlm.model.utl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.ArrayList;

import org.netbeans.modules.wlm.model.api.TImport;
import org.netbeans.modules.wlm.model.api.WLMModel;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.wsdl.model.Types;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModelFactory;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.modules.xml.xam.locator.CatalogModelFactory;

/**
 * Helper class for accessing to WSDL or XSD models by import statement.
 * @author ads
 */
public final class ImportHelper {

    /**
     * This is helper class, we don't want its instance.  
     */
    private ImportHelper() {
    }
    
    /**
     * Returns wsdl model respectively given import <code>imp</code>.
     * @param model BPEL OM
     * @param location location uri
     * @param importType type of import
     * 
     */
    public static WSDLModel getWsdlModel( WLMModel model, String location,
           String importType  ) 
    {
        return getWsdlModel(model, location, importType, true );
    }
        

    /**
     * Returns wsdl model respectively given import <code>imp</code>.
     * @param model BPEL OM
     * @param location location uri
     * @param importType type of import
     * @param checkWellFormed if true method will return null if model is not valid
     */
    public static WSDLModel getWsdlModel( WLMModel model, String location,
            String importType  , boolean checkWellFormed ) 

    {
        if (!TImport.WSDL_IMPORT_TYPE.equals( importType)) {
            return null;
        }
        WSDLModel wsdlModel;
        if (location == null) {
            return null;
        }
        try {
            URI uri = new URI(location);
            ModelSource source = CatalogModelFactory.getDefault()
                    .getCatalogModel( model.getModelSource())
                    .getModelSource(uri, model.getModelSource());
            wsdlModel = WSDLModelFactory.getDefault().getModel(source);
        }
        catch (URISyntaxException e) {
            wsdlModel = null;
        }
        catch (CatalogModelException e) {
            wsdlModel = null;
        }
        if (wsdlModel != null && wsdlModel.getState() == 
            Model.State.NOT_WELL_FORMED && checkWellFormed ) 
        {
            return null;
        }
        return wsdlModel;
    }
    
    /**
     * Returns wsdl model respectively given import <code>imp</code>.
     * @param imp import statement in BPEL OM
     */
    public static WSDLModel getWsdlModel( TImport imp ) {
        return getWsdlModel( imp , true );
    }
    
    /**
     * Returns wsdl model respectively given import <code>imp</code>.
     * @param imp import statement in BPEL OM
     * @param checkWellFormed if true method will return null if model is not valid 
     */
    public static WSDLModel getWsdlModel( TImport imp , boolean checkWellFormed ) {
        return getWsdlModel( imp.getModel() , imp.getLocation(), 
                imp.getImportType() , checkWellFormed );
    }
    
    /**
     * Returns schema model respectively <code>imp</code> import
     * statement in BPEL OM and <code>namespace</code>.
     * @param bpelModel BPEL OM
     * @param location location uri
     * @param importType type of import
     * @param namespace schema namespace
     */
    public static Collection<SchemaModel> getInlineSchema( WLMModel bpelModel,
            String namespace , String location , String importType ) 
    {
        WSDLModel model = getWsdlModel( bpelModel, location, importType );
        if ( model == null ){
            return null;
        }
        Types types =  model.getDefinitions().getTypes();
        if ( types == null ){
            return null;
        }
        Collection<Schema> collection = types.getSchemas();
        Collection<SchemaModel> models = null;
        for (Schema schema : collection) {
            if ( namespace.equals( schema.getTargetNamespace() )){
                if ( models == null ){
                    models = new ArrayList<SchemaModel>();
                }
                models.add( schema.getModel() );
            }
        }
        return models;
    }

    /**
     * Returns schema model respectively <code>imp</code> import
     * statement in BPEL OM and <code>namespace</code>.
     * @param imp import statement in BPEL OM
     * @param namespace Namespace for desired inline schema model
     */
    public static Collection<SchemaModel> getInlineSchema( TImport imp,
            String namespace ) 
    {
        return getInlineSchema(imp.getModel(), namespace, imp.getLocation(),
                imp.getImportType() );
    }
    
    /**
     * Returns all schema models accesible throuhg the imported WSDL
     * @param bpelModel BPEL OM
     * @param location location uri
     * @param importType type of import
     */
    public static Collection<SchemaModel> getAllInlineSchema(WLMModel bpelModel,
            String location , String importType)
    {
        WSDLModel model = getWsdlModel( bpelModel, location, importType );
        if ( model == null ){
            return null;
        }
        Types types =  model.getDefinitions().getTypes();
        if ( types == null ){
            return null;
        }
        Collection<Schema> collection = types.getSchemas();
        Collection<SchemaModel> models = null;
        for (Schema schema : collection) {
            if (models == null) {
                models = new ArrayList<SchemaModel>();
            }
            models.add( schema.getModel() );
        }
        return models;
    }

    /**
     * Returns all schema models accesible throuhg the imported WSDL
     * @param imp import statement in BPEL OM
     * @param namespace Namespace for desired inline schema model
     */
    public static Collection<SchemaModel> getAllInlineSchema(TImport imp)
    {
        return getAllInlineSchema(imp.getModel(), imp.getLocation(),
                imp.getImportType());
    }

}
