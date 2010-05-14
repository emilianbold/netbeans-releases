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
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.tbls.editor.ps.TcgPsI18n;
import org.netbeans.modules.tbls.model.TcgComponent;
import org.netbeans.modules.tbls.model.TcgComponentValidationMsg;
import org.openide.util.NbBundle;
//import java.util.logging.Logger;

/**
 * DefaultValidator.java
 * 
 * Created on September 9, 2005, 2:30 PM
 * 
 * @author Bing Lu
 */
public class DefaultValidator implements org.netbeans.modules.tbls.model.TcgComponentValidator {
    
    //private static Logger mLogger = Logger.getLogger(DefaultValidator.class.getName());
    /**
     * Creates a new instance of DefaultValidator 
     */
    public DefaultValidator() {
    }
    
    public org.netbeans.modules.tbls.model.TcgComponentValidationReport validate(TcgComponent component) {
        List<TcgComponentValidationMsg> messageList = new ArrayList<TcgComponentValidationMsg>();
        List<TcgComponentValidationReport> childReportList = new ArrayList<TcgComponentValidationReport>();
        String type = VALIDATION_OK_KEY;
        for (Iterator it = component.getPropertyList().iterator(); it.hasNext();) {
            org.netbeans.modules.tbls.model.TcgProperty property = (org.netbeans.modules.tbls.model.TcgProperty) it.next();
            org.netbeans.modules.tbls.model.TcgPropertyType propertyType = property.getType();
            if (propertyType.isRequired() && 
                (property.getStringValue() == null || property.getStringValue().equals(""))) 
            {
                messageList.add(new TcgComponentValidationMsg(VALIDATION_ERROR_KEY, "'" + TcgPsI18n.getDisplayName(propertyType) + "' " +
                        NbBundle.getMessage(DefaultValidator.class,"DefaultValidator.property_is_required_but_undefined")));
                type = VALIDATION_ERROR_KEY;
                continue;
            }

            if (propertyType.isRequired() && property.getValue().equals(propertyType.getDefaultValue())) {
                Object defVal = propertyType.getDefaultValue();
                String strVal = propertyType.getType().format(defVal);
                messageList.add(
                    new TcgComponentValidationMsg(
                        VALIDATION_WARNING_KEY, 
                        "'" + TcgPsI18n.getDisplayName(propertyType) + "' " + 
                        NbBundle.getMessage(DefaultValidator.class,"DefaultValidator.property_uses_default_value") + " (" + strVal + ")"));
                if (type.equals(VALIDATION_OK_KEY)) {
                    type = VALIDATION_WARNING_KEY;
                }
                continue;
            }
            
            
        }
        for (Iterator it = component.getComponentList().iterator(); it.hasNext();) {
            TcgComponent child = (TcgComponent)it.next();
            org.netbeans.modules.tbls.model.TcgComponentValidationReport r = child.validate();
            childReportList.add(r);
            if ((r.getType().equals(VALIDATION_WARNING_KEY) && type.equals(VALIDATION_OK_KEY)) ||
                (r.getType().equals(VALIDATION_ERROR_KEY) && type.equals(VALIDATION_WARNING_KEY)))
            {
               type = r.getType();
            }
        }
        return new org.netbeans.modules.tbls.model.TcgComponentValidationReport(component, type, messageList, childReportList);
    }
    
}