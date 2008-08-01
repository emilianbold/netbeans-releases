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

package org.netbeans.modules.xslt.tmap.util;

import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.soa.ui.SoaUtil;
import org.netbeans.modules.soa.ui.util.ModelUtil;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.modules.xslt.tmap.model.api.ExNamespaceContext;
import org.netbeans.modules.xslt.tmap.model.api.Import;
import org.netbeans.modules.xslt.tmap.model.api.TMapModel;
import org.netbeans.modules.xslt.tmap.model.api.TransformMap;
import org.netbeans.modules.xslt.tmap.model.api.events.VetoException;
import org.netbeans.modules.xslt.tmap.model.impl.InvalidNamespaceException;
import org.netbeans.modules.xslt.tmap.nodes.properties.ResolverUtility;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Alexey
 * @author Vitaly Bychkov
 */
public class ImportRegistrationHelper {
    private final TMapModel myModel;
    private static final Logger LOGGER = Logger.getLogger(ImportRegistrationHelper.class.getName());

    public ImportRegistrationHelper(TMapModel model) {
        myModel = model;
    }
    
    public TMapModel getModel() {
        return myModel;
    }
    
    public void addImport(WSDLModel imp_model){
//System.out.println();
//System.out.println("ADD INPORT");
        if (imp_model == null) {
            return;
        }
        
      addImport(createImport(imp_model));
      Definitions defs = imp_model.getDefinitions();
      
      if (defs == null) {
        return;
      }
      Collection<org.netbeans.modules.xml.wsdl.model.Import> imps = defs.getImports();

      if (imps == null) {
        return;
      }
      for (org.netbeans.modules.xml.wsdl.model.Import i: imps){
        try {
            // check if the imported model is itself #87107
            WSDLModel tmpImpModel = i.getImportedWSDLModel();
       
            if (tmpImpModel == null || tmpImpModel.equals(i.getModel())) {
                continue;
            }
            if ( !ResolverUtility.isModelImported(tmpImpModel, myModel)) {
                addImport(tmpImpModel);
            }
        }
        catch (CatalogModelException e) {
            LOGGER.log(Level.INFO, "occured CatalogModelException" ,e);
          //Just ignore this type of exception
        }
      }
    }
    
    public void addImport(Import new_imp){
//System.out.println();
//System.out.println("add import: " + new_imp);
        if (new_imp == null) {
            return;
        }
        TransformMap tmap = myModel.getTransformMap();
//System.out.println("process: " + process);

        if (tmap == null) {
            return;
        }
        if ( !isImported(new_imp)) {
//System.out.println("ADD !!!");
            tmap.addImport(new_imp);
        }
    }
    
    public Import createImport(WSDLModel model){
        FileObject modelFo = SoaUtil.getFileObjectByModel(model);
        if (modelFo != null) {
            return createImport(modelFo);
        }
        // may be this model is known by global catalog
        // TODO a
        return null;
    }

    public Import createImport(FileObject fo){
        if (fo != null) {
            return createImport(Util.getNewModelNamespace(fo),
                    Util.getNewModelLocation(myModel, fo));
        } 
        return null;
    }
    
    public Import createImport(String namespace, String location){
        TransformMap tMap = myModel.getTransformMap();
        if (tMap != null) {
            try {
                ExNamespaceContext nsContext = tMap.getNamespaceContext();
                nsContext.addNamespace(namespace);
            } catch (InvalidNamespaceException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }
        
        Import imp = myModel.getFactory().createImport();
        if (namespace != null){
            try {
                imp.setNamespace(namespace);
            } catch (VetoException ex) {
                LOGGER.log(Level.INFO, null, ex);
            }
        }
        
        if (location != null) {
            try {
                //Fix for IZ84824
                location = ResolverUtility.encodeLocation(location);
                
                imp.setLocation(location);
            } catch (VetoException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }
        return imp;
    }
    
    private boolean isImported(Import new_imp){
        TransformMap tMap = myModel.getTransformMap();

        if (tMap == null) {
            return false;
        }
        List<Import> imports = tMap.getImports();
        
        if (imports == null) {
            return false ;
        }
        String namespace = new_imp.getNamespace();
        String location = ResolverUtility.decodeLocation(new_imp.getLocation());
        
        for (Import imp : imports) {
            if (namespace != null && !namespace.equals(imp.getNamespace())) {
                continue;
            }
            if (location != null && !location.equals(ResolverUtility.decodeLocation(imp.getLocation()))) {
                continue;
            }
            return true;
        }
        return false;
    }

}
