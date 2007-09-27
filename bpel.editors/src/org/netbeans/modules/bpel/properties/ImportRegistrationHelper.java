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
package org.netbeans.modules.bpel.properties;

import java.util.Collection;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Import;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.model.api.events.VetoException;
import org.netbeans.modules.bpel.model.api.support.ImportHelper;
import org.netbeans.modules.bpel.properties.Constants.StandardImportType;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;

import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.ErrorManager;

import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 *
 * @author Alexey
 */
public class ImportRegistrationHelper {
    public ImportRegistrationHelper(BpelModel model) {
        this.model = model;
    }
    
    public void addImport(Model imp_model){
//System.out.println();
//System.out.println("ADD INPORT");
      addImport(createImport(imp_model));
      
      if ( !(imp_model instanceof WSDLModel)) {
        return;
      }
      Definitions defs = ((WSDLModel) imp_model).getDefinitions();
      
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
            Model tmpImpModel = i.getImportedWSDLModel();
       
            if (tmpImpModel == null || tmpImpModel.equals(i.getModel())) {
                continue;
            }
            if ( !ResolverUtility.isModelImported(tmpImpModel, model)) {
                addImport(tmpImpModel);
            }
        }
        catch (CatalogModelException e) {
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
        Process process = model.getProcess();
//System.out.println("process: " + process);

        if (process == null) {
            return;
        }
        if ( !isImported(new_imp)) {
//System.out.println("ADD !!!");
            process.addImport(new_imp);
        }
    }
    
    public Import createImport(Model model){
        return createImport(Util.getFileObjectByModel(model));
    }
    
    public Import createImport(FileObject fo){
        if (fo != null) {
            StandardImportType importType = StandardImportType.forExt(fo.getExt());
            
            return createImport(Util.getNewModelNamespace(fo, importType),
                    Util.getNewModelLocation(model, fo),
                    importType.getImportType());
        }
        return null;
    }
    
    public Import createImport(String namespace, String location, String type){
        Import imp = model.getBuilder().createImport();
        if (namespace != null){
            try {
                imp.setNamespace(namespace);
            } catch (VetoException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
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
        
        if (type != null){
            try {
                imp.setImportType(type);
            } catch (VetoException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }
        return imp;
    }
    
    private boolean isImported(Import new_imp){
        Process process = model.getProcess();

        if (process == null) {
            return false;
        }
        Import[] imports = process.getImports();
        
        if (imports == null) {
            return false ;
        }
        String namespace = new_imp.getNamespace();
        String location = ResolverUtility.decodeLocation(new_imp.getLocation());
        String type = new_imp.getImportType();
        
        for (Import imp : imports) {
            if (namespace != null && !namespace.equals(imp.getNamespace())) {
                continue;
            }
            if (location != null && !location.equals(ResolverUtility.decodeLocation(imp.getLocation()))) {
                continue;
            }
            if (type != null && !type.equals(imp.getImportType())) {
                continue;
            }
            return true;
        }
        return false;
    }

    private BpelModel model;
}
