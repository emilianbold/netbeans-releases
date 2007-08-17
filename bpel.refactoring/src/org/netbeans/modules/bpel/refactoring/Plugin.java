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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.bpel.model.api.events.VetoException;

import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Lookup;

import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;

import org.netbeans.modules.xml.refactoring.spi.SharedUtils;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.refactoring.WSDLRefactoringEngine;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.Referenceable;
import org.netbeans.modules.xml.refactoring.XMLRefactoringPlugin;

import org.netbeans.modules.bpel.core.BPELDataLoader;
import org.netbeans.modules.bpel.core.BPELDataObject;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.spi.BpelModelFactory;
import org.netbeans.modules.bpel.model.api.Import;
import org.netbeans.modules.bpel.model.api.Process;

import static org.netbeans.modules.print.ui.PrintUI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2007.03.16
 */
abstract class Plugin implements RefactoringPlugin, XMLRefactoringPlugin {
    
  protected final Set<Component> getRoots(Referenceable target) {
    Set<FileObject> files = SharedUtils.getSearchFiles(target);
    Set<Component> roots = new HashSet<Component>();

    for (FileObject file : files) {
      try {
        Component root = getFileRoot(file);
        roots.add(root);
      }
      catch (IOException e) {
        ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, e.getMessage());
      }  
    }
    return roots;
  }

  protected final List<Element> find(Referenceable target, Component root) {
    List<Component> componets = new ArrayList<Component>();
    List<Element> elements = new ArrayList<Element>();

    if (root == null) {
      return elements;
    }
    if ( !(target instanceof Referenceable)) {
      return elements;
    }
    if (root instanceof Process) {
      BpelVisitor visitor = new BpelVisitor(componets, target);
      ((Process) root).accept(visitor);
    }
    else if (root instanceof Definitions) {
      WsdlVisitor visitor = new WsdlVisitor(componets, target);
      ((Definitions) root).accept(visitor);
    }
    for (Component componet : componets) {
      elements.add(new Element(componet));
    }
    return elements;
  }

  private Component getFileRoot(FileObject file) throws IOException {
    Component root = WSDLRefactoringEngine.getWSDLDefinitions(file);
//out();
//out("getRoot: " + file);
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

  public Problem preCheck() {
    return null;
  }

  public void cancelRequest() {}

  public String getModelReference(Component component) {
    if (component instanceof Import) {
      return ((Import) component).getLocation();
    }
    return null;
  }

  public void setModelReference(Component component, String location) {
    if (component instanceof Import) {
      try {
        ((Import) component).setLocation(location);
      }
      catch (VetoException ex) {
        return;
      }
    }
  }

  public Collection<Component> getExternalReferences(Model model) {
    return new ArrayList<Component>();
  }

  public Model getModel(ModelSource source) {
    FileObject file = source.getLookup().lookup(FileObject.class);
    
    if (BPEL_MIME_TYPE.equals(FileUtil.getMIMEType(file))) {
      BpelModelFactory factory = (BpelModelFactory) Lookup.getDefault().
        lookup(BpelModelFactory.class);
      return factory.getModel(source);
     }
     return null;
  }

  private static final String BPEL_MIME_TYPE = "x-bpel+xml"; // NOI18N
}
