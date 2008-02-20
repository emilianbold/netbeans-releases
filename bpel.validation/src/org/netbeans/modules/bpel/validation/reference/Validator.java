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
package org.netbeans.modules.bpel.validation.reference;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.netbeans.modules.bpel.model.api.BpelContainer;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.ContentElement;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.model.api.Variable;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.api.VariableDeclarationScope;
import org.netbeans.modules.bpel.model.api.references.BpelReference;
import org.netbeans.modules.bpel.model.api.references.ReferenceCollection;
import org.netbeans.modules.bpel.model.api.references.SchemaReference;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.netbeans.modules.bpel.model.impl.references.MappedReference;
import org.netbeans.modules.bpel.model.impl.services.ExpressionUpdater;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.Reference;
import org.netbeans.modules.xml.xam.Referenceable;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.netbeans.modules.xml.xam.spi.ValidationResult;
import org.netbeans.modules.xml.xam.spi.Validation;
import org.netbeans.modules.xml.xam.spi.Validation.ValidationType;
import org.netbeans.modules.bpel.validation.core.Outcome;
import org.netbeans.modules.bpel.validation.core.BpelValidator;
import static org.netbeans.modules.soa.ui.util.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2007.05.03
 */
public final class Validator extends BpelValidator {

  @Override
  public void visit(Process process) {
    processEntity(process);
  }

  private void processEntity(BpelEntity entity) {
    checkReferenceCollection(entity);
    checkExpressions(entity);
    List<BpelEntity> children = entity.getChildren();

    for (BpelEntity child : children) {
      processEntity(child);
    }
  }

  private void checkExpressions(BpelEntity entity) {
    if ( !(entity instanceof ContentElement)) {
      return;
    }
    String expression = ((ContentElement) entity).getContent();
    Collection<String> collection = ExpressionUpdater.getInstance().getUsedVariables(expression);
    Set<String> set = new HashSet<String>(collection);
    findDeclarationsAscendant(entity, set);

    if (set.size() == 0) {
      return;
    }
    StringBuilder builder = new StringBuilder();

    for (String string : set) {
      builder.append( string );
      builder.append(", "); // NOI18N
    }
    String str;

    if (set.size() > 1) {
      str = i18n(Validator.class, FIX_VARIABLES);
      str = MessageFormat.format(str, builder.substring(0, builder.length()-2), expression.trim());
    }
    else {
      str = i18n(Validator.class, FIX_VARIABLE);
      str = MessageFormat.format(str, builder.substring(0, builder.length()-2), expression.trim());
    }
    addErrorMessage(str, entity);
  }
  
  private void findDeclarationsAscendant(BpelEntity entity, Set<String> set) {
      if (set.size() == 0) {
          return;
      }
      if (entity instanceof VariableDeclarationScope) {
          findDeclarationsDescendant(entity, set);
      }
      // # 81027
      BpelContainer parent = entity.getParent();

      if (parent != null) {
        findDeclarationsAscendant(parent, set);
      }
  }
  
  private void findDeclarationsDescendant(BpelEntity entity, Set<String> set) {
      if (entity instanceof VariableDeclaration) {
          String name = ((VariableDeclaration)entity).getVariableName();
          set.remove(name);
      }
      List<Variable> list = entity.getChildren(Variable.class);

      if (list != null) {
          for (Variable variable : list) {
              String name = variable.getVariableName();
              set.remove( name );
          }
      }
      if (entity instanceof VariableDeclarationScope) {
          List<VariableDeclarationScope> scopes = entity.getChildren(VariableDeclarationScope.class);

          if (scopes == null) {
              return;
          }
          for (VariableDeclarationScope scope : scopes) {
              findDeclarationsDescendant(scope, set);
          }
      }
  }

  @SuppressWarnings("unchecked")
  private void checkReferenceCollection(BpelEntity entity) {
    if ( !(entity instanceof ReferenceCollection)) {
      return;
    }
    ReferenceCollection collection = (ReferenceCollection) entity;
    Reference[] refs = collection.getReferences();

    for (Reference reference : refs) {
      if (reference == null) {
        continue;
      }
      if (reference.isBroken()) {
        addQuickFix(new Outcome(this, ResultType.ERROR, (Component) entity,
          getMessage(entity, reference), QuickFix.get(entity, (Reference<Referenceable>) reference)));
      }
    }
  }

  private String getMessage(BpelEntity entity, Reference ref) {
      String str = null;
      String tagName = entity.getPeer().getLocalName();
      Attribute attr = null;

      if (ref instanceof MappedReference) {
        attr = ((MappedReference) ref).getAttribute();
      }
      if (ref instanceof BpelReference) {
        str = i18n(getClass(), FIX_REFERENCE);
        str = MessageFormat.format(str, tagName, attr.getName()) + " " + i18n(getClass(), FIX_CORRECTION); // NOI18N
      }
      else if (ref instanceof WSDLReference) {
        str = i18n(getClass(), FIX_REFERENCE_EXTERNAL);
        str = MessageFormat.format(str, tagName, attr.getName(), WSDL) + " " + i18n(getClass(), FIX_CORRECTION_EXTERNAL); // NOI18N
      }
      else if (ref instanceof SchemaReference) {
        str = i18n(getClass(), FIX_REFERENCE_EXTERNAL);
        str = MessageFormat.format(str, tagName, attr.getName(), XSD) + " " + i18n(getClass(),FIX_CORRECTION_EXTERNAL); // NOI18N
      }
      return str;
  }

  private static final String FIX_VARIABLES = "FIX_Variables";    // NOI18N
  private static final String FIX_VARIABLE = "FIX_Variable";      // NOI18N
  private static final String FIX_CORRECTION_EXTERNAL = "FIX_Correction_External"; // NOI18N
  private static final String XSD = "xsd";                        // NOI18N
  private static final String WSDL = "WSDL";                      // NOI18N
  private static final String FIX_REFERENCE_EXTERNAL = "FIX_Reference_External"; // NOI18N
  private static final String FIX_CORRECTION = "FIX_Correction";  // NOI18N
  private static final String FIX_UNKNOWN = "FIX_Unknown";        // NOI18N
  private static final String FIX_REFERENCE = "FIX_Reference";    // NOI18N
}
