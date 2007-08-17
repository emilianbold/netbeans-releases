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
import java.net.URL;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;

import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;

import org.netbeans.modules.xml.refactoring.ErrorItem;
import org.netbeans.modules.xml.refactoring.spi.RefactoringUtil;
import org.netbeans.modules.xml.refactoring.spi.SharedUtils;
import org.netbeans.modules.xml.refactoring.XMLRefactoringTransaction;

import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.Referenceable;

import org.netbeans.modules.bpel.model.api.Import;
import org.netbeans.modules.bpel.model.api.events.VetoException;

import static org.netbeans.modules.print.ui.PrintUI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2007.03.16
 */
final class Mover extends Plugin {
    
  Mover(MoveRefactoring refactoring) {
    myRequest = refactoring;
  }
  
  public Problem fastCheckParameters() {
    URL url = ((MoveRefactoring) myRequest).getTarget().lookup(URL.class);

    if (url == null) {
      return null;
    }
    FileObject target = URLMapper.findFileObject(url);

    if (target != null && !target.canWrite()) {
      return new Problem(true, i18n(Mover.class,"ERR_PackageIsReadOnly")); // NOI18N
    }
    return null;
  }
    
  public Problem checkParameters() {
    return null;
  }
    
  public Problem prepare(RefactoringElementsBag refactoringElements) {
    Referenceable reference =
      myRequest.getRefactoringSource().lookup(Referenceable.class);

    if (reference == null) {
      return null;
    }
    if ( !(reference instanceof Model) ) {
      return null;
    }
    Set<Component> roots = getRoots(reference);
    List<Element> elements = new ArrayList<Element>();

    for (Component root : roots) {
      List<Element> founds = find(reference, root);

      if (founds != null) {
        elements.addAll(founds);
      }
    }
    if (elements.size() > 0) {
      List<Model> models = getModels(elements);
      List<ErrorItem> errors = RefactoringUtil.precheckUsageModels(models, true);

      if (errors != null && errors.size() > 0) {
        return processErrors(errors);
      } 
    } 
    XMLRefactoringTransaction transaction =
      myRequest.getContext().lookup(XMLRefactoringTransaction.class);
    transaction.register(this, elements);
    refactoringElements.registerTransaction(transaction);

    for (Element element : elements) {
      element.setTransactionObject(transaction);
      refactoringElements.add(myRequest, element);
    }      
    return null;
  }
      
  private Problem processErrors(List<ErrorItem> items) {
    if (items == null || items.size()== 0) {
      return null;
    }
    Problem parent = null;
    Problem child = null;
    Problem head = null;
    Iterator<ErrorItem> iterator = items.iterator();
            
    while(iterator.hasNext()) {
      ErrorItem error = iterator.next();

      if (parent == null) {
        parent = new Problem(isFatal(error), error.getMessage());
        child = parent;
        head = parent;
        continue;
      }
      child = new Problem(isFatal(error), error.getMessage());
      parent.setNext(child);
      parent = child;
    }
    return head;
  }
   
  public void doRefactoring(
    List<RefactoringElementImplementation> elements) throws IOException
  {
    Map<Model, Set<RefactoringElementImplementation>> map = getModelMap(elements);
    Set<Model> models = map.keySet();
    Referenceable reference =
      myRequest.getRefactoringSource().lookup(Referenceable.class);

    for (Model model : models) {
      if (reference instanceof Model) {
        rename(model, getComponents( map.get(model)));
      }
    }    
  }
    
  public String getModelReference(Component component) {
    if (component instanceof Import) {
      return ((Import) component).getLocation();
    }
    return null;
  }

  private Map<Model, Set<RefactoringElementImplementation>> getModelMap(
    List<RefactoringElementImplementation> elements)
  {
    Map<Model, Set<RefactoringElementImplementation>> results =
      new HashMap<Model, Set<RefactoringElementImplementation>>();
  
    for(RefactoringElementImplementation element : elements) {
      Model model = (element.getLookup().lookup(Component.class)).getModel();
      Set<RefactoringElementImplementation> components = results.get(model);

      if (components == null) {
        components = new HashSet<RefactoringElementImplementation>();
        components.add(element);
        results.put(model, components);
      }
      else {
        components.add(element);
      }
    }
    return results;
  }

  private List<Component> getComponents(
    Set<RefactoringElementImplementation> elements)
  {
    List<Component> component = new ArrayList<Component>(elements.size());
  
    for (RefactoringElementImplementation element : elements) {
      component.add(element.getLookup().lookup(Component.class));
    }
    return component;
  }
       
  private List<Model> getModels(List<Element> elements) {
    List<Model> models = new ArrayList<Model>();

    for (Element element : elements) {
      models.add((element.getLookup().lookup(Component.class)).getModel());
    }
    return models;
  }

  private boolean isFatal(ErrorItem error) {
    return error.getLevel() == ErrorItem.Level.FATAL;
  }  
  
  private void rename(Model model, List<Component> components) throws IOException {
    if (components == null) {
      return;
    }
    for (Component component : components) {
      renameFile(model, component);
    }
  }

  private void renameFile(Model model, Component component) throws IOException {
//out();
//out("FILE RENAME: " + Util.getName(component));
    if ( !(component instanceof Import)) {
      return;
    }
    try {
      Import _import = (Import) component;
      String location = _import.getLocation();

      try {
        location = SharedUtils.calculateNewLocationString(model, myRequest);
      }
      catch (URISyntaxException e) {
        return;
      }
      _import.setLocation(location);
    }
    catch(VetoException e) {
      throw new IOException(e.getMessage());
    }
  }
  
  private MoveRefactoring myRequest;
}
