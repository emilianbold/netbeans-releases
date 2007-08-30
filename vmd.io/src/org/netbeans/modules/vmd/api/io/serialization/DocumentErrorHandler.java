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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.vmd.api.io.serialization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Karol Harezlak
 */
public final class DocumentErrorHandler {
    
    private List<String> errors;
    private List<String> warnings;
   
    public DocumentErrorHandler() {
        errors = new ArrayList<String>();
        warnings = new ArrayList<String>();
    }
     /**
     * This method adds critical issues to the deserializing document rapport and
     * stops loading of document. All reported issues are shown in report
     * dialog window right after loading of document is stooped.  
     * @param description of issue, non null String 
     */ 
    public DocumentErrorHandler addError(String error) {
        if (error == null)
            throw new IllegalArgumentException();
        errors.add(error);
        return this;
    }
    /**
     * This method adds NON critical issues to the deserializing document rapport.
     * All reported issues are shown in report dialog window when document is loaded.
     * @param description of issue, non null String 
     */ 
    public DocumentErrorHandler addWaring(String warning) {
         if (warning == null)
            throw new IllegalArgumentException();
        warnings.add(warning);
        return this;
    }
    /**
     * Returns reported critical issues;
     * @return critical issues
     */ 
    public List<String> getErrors() {
        return Collections.unmodifiableList(errors);
    }
    /**
     * Returns reported non criticla issues. 
     */
    public List<String> getWarnings() {
        return Collections.unmodifiableList(warnings);
    }
    
}
