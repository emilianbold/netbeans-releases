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

package org.netbeans.modules.tbls.model;
import java.util.ArrayList;

import org.netbeans.modules.tbls.model.TcgComponent;

/**
 * OkValidator.java
 * 
 * Created on September 9, 2005, 2:30 PM
 * 
 * @author Bing Lu
 */
public class OkValidator implements org.netbeans.modules.tbls.model.TcgComponentValidator {
    
    /**
     * Creates a new instance of OkValidator 
     */
    public OkValidator() {
    }
    
    public org.netbeans.modules.tbls.model.TcgComponentValidationReport validate(TcgComponent component) {
        return new org.netbeans.modules.tbls.model.TcgComponentValidationReport(component, VALIDATION_OK_KEY, new ArrayList(), new ArrayList());
    }
}
