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
package org.netbeans.modules.bpel.project.anttasks;

import java.io.File;
import java.net.URI;
import java.util.Collection;
import java.util.Iterator;

import org.netbeans.modules.xml.xam.spi.ValidationResult;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.xml.xam.spi.Validator;
import org.netbeans.modules.bpel.model.spi.BpelModelFactory;
import org.netbeans.modules.xml.xam.spi.Validation;
import org.netbeans.modules.xml.xam.spi.ValidationResult;
import org.netbeans.modules.xml.xam.spi.Validator;
import org.netbeans.modules.xml.xam.spi.Validation;
import org.netbeans.modules.xml.xam.spi.Validation.ValidationType;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.openide.util.lookup.Lookups;

/**
 * Class used for validating the BPEL files
 * @author Sreenivasan Genipudi
 */
public class ValidateBPEL {
    public ValidateBPEL() {}
    
    public Collection validate(URI bpelFileUri) {
        BpelModel model = null;
        try {
            model = BPELCatalogModel.getDefault().getBPELModel(bpelFileUri);
        }catch (Exception ex) {
            throw new RuntimeException("Error while trying to create BPEL Model ",ex);
        }
        Validation validation = new Validation();
        validation.validate((org.netbeans.modules.xml.xam.Model)model,  ValidationType.COMPLETE);
        Collection col  =validation.getValidationResult();

        for (Iterator itr = col.iterator(); itr.hasNext();) {
           ResultItem resultItem = (ResultItem) itr.next();
        }
        return col;
    }
}
