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
import org.netbeans.modules.xml.xam.spi.Validation;
import org.netbeans.modules.xml.xam.spi.Validation.ValidationType;
import org.netbeans.modules.xml.xam.spi.ValidationResult;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.netbeans.modules.xml.xam.spi.Validator.ResultType;

import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.model.api.support.SimpleBpelModelVisitorAdaptor;
import org.netbeans.modules.bpel.model.api.support.ValidationVisitor;
import static org.netbeans.modules.soa.ui.util.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2007.05.03
 */
public abstract class Validator extends SimpleBpelModelVisitorAdaptor implements ValidationVisitor, org.netbeans.modules.xml.xam.spi.Validator {

  public ValidationResult validate(Model model, Validation validation, ValidationType type) {
    myResultItems = new HashSet<ResultItem>();
    myValidation = validation;
    myType = type;

    if ( !(model instanceof BpelModel)) {
      return null;
    }
    final BpelModel bpelModel = (BpelModel) model;
    
    if (bpelModel.getState() == Model.State.NOT_WELL_FORMED) {
      return null;
    }
    final List<Set<ResultItem>> collection = new ArrayList<Set<ResultItem>>(1);

    Runnable run = new Runnable() {
      public void run() {
startTime();
        Process process = bpelModel.getProcess();

        if (process != null) {
          process.accept(Validator.this);
        }
        collection.add(getResultItems());
endTime();
      }
    };
    bpelModel.invoke(run);

    return new ValidationResult(collection.get(0), Collections.singleton(model));
  }

  public String getName() {
    return getClass().getName();
  }

  public Set<ResultItem> getResultItems() {
    return myResultItems;
  }

  private void startTime() {
    myTime = System.currentTimeMillis ();
  }

  private void endTime() {
    long currentTime = System.currentTimeMillis ();
    out("Validator " + getName() + " takes " + (currentTime - myTime) + " ms.");
  }

  private long myTime;

  protected final void addWarning(String key, Component component) {
    addMessage(i18n(getClass(), key), ResultType.WARNING, component);
  }

  protected final void addError(String key, Component component) {
    addMessage(i18n(getClass(), key), ResultType.ERROR, component);
  }

  protected final void addError(String key, Component component, String param) {
    addMessage(i18n(getClass(), key, param), ResultType.ERROR, component);
  }

  protected final void addError(String key, Component component, String param1, String param2) {
    addMessage(i18n(getClass(), key, param1, param2), ResultType.ERROR, component);
  }

  protected final void addQuickFix(Outcome outcome) {
    getResultItems().add(outcome);
  }

  protected final void addErrorMessage(String message, Component component) {
    getResultItems().add(new ResultItem(this, ResultType.ERROR, component, message));
  }

  protected final void addMessage(String message, ResultType type, Component component) {
    getResultItems().add(new ResultItem(this, type, component, message));
  }

  protected final void validate(Model model) {
    myValidation.validate(model, myType);
  }

  private ValidationType myType;
  private Validation myValidation;
  private Set<ResultItem> myResultItems;
}
