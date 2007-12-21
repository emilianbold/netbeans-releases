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
package org.netbeans.modules.bpel.model.api.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.model.api.support.SimpleBpelModelVisitor;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.spi.Validation;
import org.netbeans.modules.xml.xam.spi.ValidationResult;
import org.netbeans.modules.xml.xam.spi.Validator;
import org.netbeans.modules.xml.xam.spi.Validation.ValidationType;

public class BpelXpathValidator implements Validator {

    public String getName() {
        return getClass().getName();
    }

    @SuppressWarnings("unchecked")
    public ValidationResult validate(Model model, Validation validation, ValidationType validationType) {
        if( !(model instanceof BpelModel)) {
            return null;
        }
        final BpelModel bpelModel = (BpelModel) model;
        
        if (bpelModel.getState() == Model.State.NOT_WELL_FORMED) {
            return null;
        }
        final ArrayList<Set<ResultItem>> collection =
                new ArrayList<Set<ResultItem>>(1);
        Set<Model> models = Collections.singleton((Model)bpelModel);
        Runnable run = new Runnable() {
            
            public void run() {
                Set<ResultItem> results ;
                ValidationVisitor visitor = new BpelXpathValidatorVisitor(BpelXpathValidator.this);
                Process process = bpelModel.getProcess();
                if (process != null) {
                    if (visitor instanceof SimpleBpelModelVisitor) {
                        process.accept((SimpleBpelModelVisitor)visitor);
                    } else {
                        process.accept(visitor);
                    }
                }
                
                results = visitor.getResultItems();
                collection.add( results );
            }
        };
        bpelModel.invoke( run );
        Set<ResultItem> results = collection.get(0);

        if (results == null) {
            results = null;
        }
        return new ValidationResult(results, models);
    }
}
