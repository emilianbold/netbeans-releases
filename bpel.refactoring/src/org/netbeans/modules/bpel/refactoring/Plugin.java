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
 * License. When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP. Sun designates this
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
package org.netbeans.modules.bpel.refactoring;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
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

import org.netbeans.modules.xml.refactoring.ErrorItem;
import org.netbeans.modules.xml.refactoring.spi.SharedUtils;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.refactoring.WSDLRefactoringEngine;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.Referenceable;
import org.netbeans.modules.xml.refactoring.XMLRefactoringPlugin;

import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.spi.BpelModelFactory;
import org.netbeans.modules.bpel.model.api.Import;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.editors.api.EditorUtil;
import static org.netbeans.modules.xml.ui.UI.*;

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
    if ( !"text/x-bpel+xml".equals(FileUtil.getMIMEType(file))) { // NOI18N
      return null;
    }
//out();
//out("Find usages");
//out("   FileObject: " + file);
    DataObject data = null;

    try {
      data = DataObject.find(file);
    }
    catch (DataObjectNotFoundException e) {
//out("   DataObject is NULL");
      return null;
    }
//out("   DataObject: " + data);
    BpelModel model = EditorUtil.getBpelModel(data);

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

  protected List<Model> getModels(List<Element> elements) {
    List<Model> models = new ArrayList<Model>();

    for (Element element : elements) {
      models.add((element.getLookup().lookup(Component.class)).getModel());
    }
    return models;
  }

  protected Problem processErrors(List<ErrorItem> items) {
    if (items == null || items.size()== 0) {
      return null;
    }
    Problem parent = null;
    Problem child = null;
    Problem head = null;
    Iterator<ErrorItem> iterator = items.iterator();
            
    while (iterator.hasNext()) {
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

  protected boolean isFatal(ErrorItem error) {
    return error.getLevel() == ErrorItem.Level.FATAL;
  }  

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
      BpelModelFactory factory = (BpelModelFactory) Lookup.getDefault().lookup(BpelModelFactory.class);
      return factory.getModel(source);
     }
     return null;
  }

  private static final String BPEL_MIME_TYPE = "x-bpel+xml"; // NOI18N
}
