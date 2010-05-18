/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.websvc.wsdl.validator;

import java.io.IOException;
import java.util.List;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.spi.Validation;
import org.netbeans.modules.xml.xam.spi.Validation.ValidationType;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;

/**
 * @author Praveen Savur
 * @author rico
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
