/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.bpel.refactoring;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.spi.ProgressProviderAdapter;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.xml.refactoring.ErrorItem;
import org.netbeans.modules.xml.refactoring.XMLRefactoringPlugin;
import org.netbeans.modules.xml.refactoring.spi.SharedUtils;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.Referenceable;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;

import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.bpel.model.api.Process;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Import;
import org.netbeans.modules.bpel.model.api.Process;

import org.netbeans.modules.bpel.core.BPELDataLoader;
import org.netbeans.modules.bpel.core.BPELDataObject;

import org.netbeans.modules.xml.refactoring.spi.RefactoringEngine;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.refactoring.WSDLRefactoringEngine;

import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.Referenceable;
import static org.netbeans.modules.print.api.PrintUI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2007.03.16
 */
abstract class Plugin extends ProgressProviderAdapter implements RefactoringPlugin {
    
    public List<Element> find(Referenceable target, Component searchRoot){
        List<Element> usages = new ArrayList<Element>();
        if (searchRoot == null) {
            return usages;
        }
        if ( !(target instanceof Referenceable)) {
           return usages;
        }
       
       if (searchRoot instanceof Process) {
           BpelVisitor visitor = new BpelVisitor(usages, target);
           ((Process) searchRoot).accept(visitor);
       } else if (searchRoot instanceof Definitions) {
           WsdlVisitor visitor = new WsdlVisitor(usages, target);
           ((Definitions) searchRoot).accept(visitor);
       }
     
       return usages;
    }
                    
         
     public List<Model> getModels(List<Element> elements){
         List<Model> models = new ArrayList<Model>();
         for(Element element : elements) {
             models.add((element.getLookup().lookup(Component.class)).getModel());
         }
         return models;
     }
     
     public Map<Model, Set<RefactoringElementImplementation>> getModelMap(List<RefactoringElementImplementation> elements){
        Map<Model, Set<RefactoringElementImplementation>> results = new HashMap<Model, Set<RefactoringElementImplementation>>();
        for(RefactoringElementImplementation element:elements){
           Model model = (element.getLookup().lookup(Component.class)).getModel();
           Set<RefactoringElementImplementation> elementsInModel = results.get(model);
           if(elementsInModel == null){
               elementsInModel = new HashSet<RefactoringElementImplementation>();
               elementsInModel.add(element);
               results.put(model, elementsInModel);
           } else
               elementsInModel.add(element);
        }
        return results;
    }
     
     public Set<Component> getSearchRoots(Referenceable target) {
       Set<Component>  searchRoots = new HashSet<Component>();
       Set<FileObject> files = SharedUtils.getSearchFiles(target);

        for (FileObject file : files) {
                      
            try {
                Component root = getFileRoot(file);
                searchRoots.add(root);
            } catch (IOException ex) {
                ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, ex.getMessage());
            }  
       }
        
        return searchRoots;
     }

      public boolean isFatal(ErrorItem error){
        if(error.getLevel() == ErrorItem.Level.FATAL)
            return true;
        else
            return false;
   } 

  private Component getFileRoot(FileObject file) throws IOException {
    Component root = WSDLRefactoringEngine.getWSDLDefinitions(file);
//out();
//out("getSearchRoot: " + file);
//out("         root: " + Util.getName(root));

    if (root != null) {
      return root;
    }
    if ( !BPELDataLoader.MIME_TYPE.equals(FileUtil.getMIMEType(file))) {
      return null;
    }
//out();
//out("Find usages");
//out("   FileObject: " + file);
    DataObject dataObject = null;

    try {
      dataObject = DataObject.find(file);
    }
    catch (DataObjectNotFoundException e) {
//out("   DataObject is NULL");
      return null;
    }
//out("   DataObject: " + dataObject);

    if ( !(dataObject instanceof BPELDataObject)) {
//out("   DataObject is not BPELDataObject");
      return null;
    }
    BpelModel model =
      (BpelModel) ((BPELDataObject)dataObject).getLookup().lookup(BpelModel.class);

    if (model == null) {
//out("   Bpel model is null");
      return null;
    }
    return model.getProcess();
  }
}
