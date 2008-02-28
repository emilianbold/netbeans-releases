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
package org.netbeans.modules.bpel.validation.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.xam.spi.Validation;
import org.netbeans.modules.xml.xam.spi.Validation.ValidationType;
import org.netbeans.modules.xml.xam.spi.ValidationResult;
import org.netbeans.modules.xml.xam.spi.Validator;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.netbeans.modules.xml.xam.spi.Validator.ResultType;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.visitor.DeepSchemaVisitor;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.SimpleType;
import org.netbeans.modules.xml.schema.model.TypeContainer;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import java.util.Collection;

import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.model.api.support.SimpleBpelModelVisitorAdaptor;
import org.netbeans.modules.bpel.model.api.support.ValidationVisitor;
import static org.netbeans.modules.soa.ui.util.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2007.05.03
 */
public abstract class CoreValidator extends SimpleBpelModelVisitorAdaptor implements ValidationVisitor, Validator {

  public CoreValidator() {
    myResultItems = new HashSet<ResultItem>();
  }

  public abstract ValidationResult validate(Model model, Validation validation, ValidationType type);

  protected final String getDisplayName() {
    String name = getName();
    StringBuffer spaces = new StringBuffer();

    for (int i=name.length(); i < 57; i++) {
      spaces.append(" "); // NOI18N
    }
    return "Validator " + name + spaces; // NOI18N
  }

  public String getName() {
    return getClass().getName();
  }

  public Set<ResultItem> getResultItems() {
    return myResultItems;
  }

  protected final void setParam(Validation validation, ValidationType type) {
    myValidation = validation;
    myType = type;
  }

  protected final void addWarning(String key, Component component) {
    addMessage(i18n(getClass(), key), ResultType.WARNING, component);
  }

  protected final void addError(String key, Component component) {
//out("add error: " + key + " " + component);
    addMessage(i18n(getClass(), key), ResultType.ERROR, component);
  }

  protected final void addError(String key, Component component, String param) {
    addMessage(i18n(getClass(), key, param), ResultType.ERROR, component);
  }

  protected final void addError(String key, Component component, String param1, String param2) {
    addMessage(i18n(getClass(), key, param1, param2), ResultType.ERROR, component);
  }

  protected final void addQuickFix(Outcome outcome) {
    myResultItems.add(outcome);
  }

  protected final void addErrorMessage(String message, Component component) {
    myResultItems.add(new ResultItem(this, ResultType.ERROR, component, message));
  }

  protected final void addMessage(String message, ResultType type, Component component) {
    myResultItems.add(new ResultItem(this, type, component, message));
  }

  protected final void validate(Model model) {
    myValidation.validate(model, myType);
  }

  protected final boolean isValidationComplete() {
    return myType == ValidationType.COMPLETE;
  }

  protected final String getTypeName(Component component) {
    if (component == null) {
      return "n/a"; // NOI18N
    }
    if (component instanceof Named) {
      return ((Named) component).getName();
    }
    return component.toString();
  }

  protected final String getName(Object component) {
    if (component == null) {
      return null;
    }
    if (component instanceof Named) {
      String name = ((Named) component).getName();

      if (name != null) {
        return name;
      }
    }
    return component.getClass().getName();
  }

  protected final Component getTypeOfElement(Component component) {
//out();
//out("GET TYPE: " + component);
    GlobalType type = null;

    if (component instanceof TypeContainer) {
//out("1");
      NamedComponentReference<? extends GlobalType> ref = ((TypeContainer) component).getType();

      if (ref != null) { 
        type = ref.get();

        if (type != null) {
//out("2");
          return type;
        }
      }
    }
//out("3");
    if (component instanceof DocumentComponent && component instanceof SchemaComponent) {
      DocumentComponent document = (DocumentComponent) component;
      String typeName = document.getPeer().getAttribute("type");
      typeName = removePrefix(typeName);
      type = findType(typeName, (SchemaComponent) component);
    }
    if (type != null) {
      return type;
    }
//out("4");
    return component;
  }

  private GlobalType findType(String typeName, SchemaComponent component) {
//out("= findType: " + typeName);
    if (typeName == null || typeName.equals("")) {
      return null;
    }
    SchemaModel model = component.getModel();
    Collection<Schema> schemas = model.findSchemas("http://www.w3.org/2001/XMLSchema");
    GlobalType type = null;

    for (Schema schema : schemas) {
      type = findType(typeName, schema);

      if (type != null) {
        return type;
      }
    }
    return findType(typeName, model.getSchema());
  }

  private GlobalType findType(final String typeName, Schema schema) {
//out();
//out("= in schema: " + schema.getTargetNamespace());
    myGlobalType = null;

    schema.accept(new DeepSchemaVisitor() {

      @Override
      public void visit(GlobalSimpleType type) {
//out("  see GLOBAL Simple TYPE : " + type.getName());
        if (typeName.equals(type.getName())) {
//out("!!!=== FOUND GLOBAL Simple TYPE ==== : " + type.getName());
          myGlobalType = type;
        }
      }
      @Override
      public void visit(GlobalComplexType type) {
//out(" see GLOBAL Complex TYPE : " + type.getName());
        if (typeName.equals(type.getName())) {
//out("!!!=== FOUND GLOBAL Complex TYPE ==== : " + type.getName());
          myGlobalType = type;
        }
      }
    });
    return myGlobalType;
  }

  private String removePrefix(String value) {
    if (value == null) {
      return null;
    }
    int k = value.indexOf(":");

    if (k == -1) {
      return value;
    }
    return value.substring(k + 1);
  }

  private static void out() {
    System.out.println();
  }

  private void out(Object object) {
    System.out.println("*** " + object); // NOI18N
  }

  private ValidationType myType;
  private Validation myValidation;
  private GlobalType myGlobalType;
  private Set<ResultItem> myResultItems;
}
