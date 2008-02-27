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
import static org.netbeans.modules.soa.ui.util.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2008.02.15
 */
public abstract class BpelValidator extends CoreValidator {

  public ValidationResult validate(Model model, Validation validation, ValidationType type) {
    setParam(validation, type);

    if ( !(model instanceof BpelModel)) {
      return null;
    }
    final BpelModel bpelModel = (BpelModel) model;
    
    if (bpelModel.getState() == Model.State.NOT_WELL_FORMED) {
      return null;
    }
    Runnable run = new Runnable() {
      public void run() {
        startTime();
        Process process = bpelModel.getProcess();

        if (process != null) {
          process.accept(BpelValidator.this);
        }
        endTime(getDisplayName());
      }
    };
    bpelModel.invoke(run);
//out();
//out();
//out("!!! ERRORS: " + getResultItems().size());
//out();
//out();
    return new ValidationResult(getResultItems(), Collections.singleton(model));
  }
}
