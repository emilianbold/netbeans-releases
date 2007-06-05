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

package org.netbeans.modules.xml.wsdl.bindingsupport.spi;

/**
 *
 * @author radval
 */
public class ValidationInfo {
     
    
    public enum ValidationType {
        ADVICE, WARNING, ERROR
    }
    
    
    private ValidationType mType;
    private String mDescription;
    
    public ValidationInfo(ValidationType type, String description) {
        this.mType = type;
        this.mDescription = description;
    }
    /**
     * Returns type of validation result.
     * @return Type of message. Advice/Warning or Error.
     */
    public ValidationType getType() {
        return mType;
    }
    
    
    /**
     * Returns description of the validation result item.
     * @return Message describing advice/warning or error.
     */
    public String getDescription() {
        return mDescription;
    }
    
}
    

