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

package org.netbeans.modules.xml.validation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.xml.validation.ui.ValidationOutputWindow;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.Model.State;
import org.netbeans.modules.xml.xam.spi.Validation;
import org.netbeans.modules.xml.xam.spi.Validation.ValidationType;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.openide.util.NbBundle;

/**
 *
 * @author Praveen Savur
 */
public class ValidationOutputWindowController {
    
    /** Creates a new instance of ValidationOutputWindowController */
    public ValidationOutputWindowController() {
    }
    
    
    /**
     * Validates the model. Call this on a non-AWT thread. The model
     * will be synchronized (i.e. sync() will be called) before the
     * validation is performed.
     *
     * @param  model  the model to validate.
     */
    public List<ResultItem> validate(Model model) {
        
        // Ensure the model is in sync.
        if (model!=null && !model.inSync()) {
            try {
                model.sync();
            } catch (IOException ioe) {
                // Ignore and let the validator discover the error
                // and report it to the user.
            }
        }
        
        Validation validation = new Validation();
        validation.validate(model, ValidationType.COMPLETE);
        List<ResultItem> validationResult = validation.getValidationResult();
        printGuidanceInformation(validationResult);
        
        return validationResult;
    }
    
    
    
    private void printGuidanceInformation(List<ResultItem> guidanceInformation) {
        ValidationOutputWindow guidanceOutputWindow = new ValidationOutputWindow();
        guidanceOutputWindow.displayValidationInformation(guidanceInformation);
    }
    
}
