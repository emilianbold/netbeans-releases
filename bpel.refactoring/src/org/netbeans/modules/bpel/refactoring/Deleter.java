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
import java.util.List;
import java.util.Set;

import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.SafeDeleteRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;

import org.netbeans.modules.xml.refactoring.ErrorItem;
import org.netbeans.modules.xml.refactoring.spi.RefactoringUtil;
import org.netbeans.modules.xml.refactoring.XMLRefactoringTransaction;

import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.Referenceable;

import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.BPELExtensibilityComponent;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.CorrelationProperty;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PropertyAlias;
import static org.netbeans.modules.xml.ui.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2007.09.17
 */
final class Deleter extends Plugin {
    
  Deleter(SafeDeleteRefactoring refactoring) {
    myRequest = refactoring;
  }
  
  public Problem prepare(RefactoringElementsBag refactoringElements) {
//out();
//out("PREPARE");
//out();
    Referenceable reference = myRequest.getRefactoringSource().lookup(Referenceable.class);

    if (reference == null) {
      return null;
    }
    Set<Component> roots = getRoots(reference);
    myElements = new ArrayList<Element>();

    for (Component root : roots) {
      List<Element> founds = find(reference, root);
//out();
//out("Founds: " + founds);
//out("  root: " + root);

      if (founds != null && founds.size() > 0) {
//out("  size: " + founds.size());
//out("   see: " + founds.get(0).getUserObject() + " " + founds.get(0).getText());
        myElements.addAll(founds);
      }
    }
    if ( !myElements.isEmpty()) {
//out();
      List<Model> models = getModels(myElements);
//out("models: " + models);
      List<ErrorItem> errors = RefactoringUtil.precheckUsageModels(models, true);
//out("errors: " + errors);

      if (errors == null) {
        errors = new ArrayList<ErrorItem>();
      }
      populateErrors(errors);
//out("populate: " + errors);

      if (errors.size() > 0) {
        return processErrors(errors);
      } 
    } 
    XMLRefactoringTransaction transaction = myRequest.getContext().lookup(XMLRefactoringTransaction.class);
    transaction.register(this, myElements);
    refactoringElements.registerTransaction(transaction);
//out();
//out("refactoringElements: " + refactoringElements);

    for (Element element : myElements) {
      element.setTransactionObject(transaction);
      refactoringElements.add(myRequest, element);
//out("    element: " + element);
    }      
    return null;
  }

  private void populateErrors(List<ErrorItem> errors) {
    for (Element element : myElements) {
      Object object = element.getUserObject();

      if (object instanceof BPELExtensibilityComponent) {
        continue;
      }
      ErrorItem error = new ErrorItem(
        object,
        i18n(Deleter.class, "ERR_Cascade_Delete_For_PropertyAlias_Only"), // NOI18N
        ErrorItem.Level.FATAL);

      errors.add(error);
      break;
    }
  }
      
  public Problem fastCheckParameters() {
    return null;
  }

  public Problem checkParameters() {
    return null;
  }

  public void doRefactoring(List<RefactoringElementImplementation> elements) throws IOException {
//out();
//out("DO: " + myRequest.getRefactoringSource());
    Referenceable referenceable = myRequest.getRefactoringSource().lookup(Referenceable.class);

    if ( !(referenceable instanceof Component)) {
      return;
    }
    for (Element element : myElements) {
      Object object = element.getUserObject();
//out("  see: " + object);

      if ( !(object instanceof PropertyAlias)) {
        continue;
      }
      delete((PropertyAlias) object, (Component) referenceable);
    }
//out();
  }

  private void delete(PropertyAlias alias, Component target) {
    Model model = alias.getModel();
    boolean doTransaction = false;

    if (model != null) {
      doTransaction = !model.isIntransaction();
    }
    try {
      if (doTransaction && model != null) {
        model.startTransaction();
      }
      if (target instanceof Part) {
        alias.setPart(null);
      }
      else if (target instanceof Message) {
        alias.setMessageType(null);
        alias.setPart(null);
      }
      else if (target instanceof CorrelationProperty) {
        alias.setPropertyName(null);
      }
//    else {
//out("target: " + target.getClass().getName());
//    }
    }
    finally {
      if (doTransaction && model != null && model.isIntransaction()) {
        model.endTransaction();
      }
    }
  }

  private List<Element> myElements;
  private SafeDeleteRefactoring myRequest;
}
