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

package org.netbeans.modules.iep.editor.model;

import java.util.List;
import org.netbeans.modules.iep.editor.share.SharedConstants;
import org.netbeans.modules.iep.editor.tcg.model.DefaultValidator;
import org.netbeans.modules.iep.editor.tcg.ps.TcgPsI18n;
import org.netbeans.modules.iep.model.lib.TcgComponent;
import org.netbeans.modules.iep.model.lib.TcgComponentValidationMsg;
import org.netbeans.modules.iep.model.lib.TcgComponentValidationReport;
import org.netbeans.modules.iep.model.lib.TcgComponentValidator;
import org.netbeans.modules.iep.model.lib.TcgProperty;
import org.openide.util.NbBundle;

/**
 *
 * @author Bing Lu
 */
public class DefaultOperatorValidator implements TcgComponentValidator, SharedConstants {
    private DefaultValidator mDefaultValidator = new DefaultValidator();
    
    /** Creates a new instance of DefaultOperatorValidator */
    public DefaultOperatorValidator() {
    }
    
    public TcgComponentValidationReport validate(TcgComponent component) {
        TcgComponentValidationReport report = mDefaultValidator.validate(component);
        List messageList = report.getMessageList();
        
        String type = report.getType();
        try {
            TcgProperty isGlobal = component.getProperty(IS_GLOBAL_KEY);
            if (!isGlobal.getBoolValue()) {
                return report;
            }
            TcgProperty glbID = component.getProperty(GLOBAL_ID_KEY);
            if (!glbID.hasValue() || glbID.getStringValue().trim().equals("")) {
                messageList.add(
                        new TcgComponentValidationMsg(VALIDATION_ERROR_KEY,
                            "'" + TcgPsI18n.getDisplayName(glbID.getType()) + "' " +
                            NbBundle.getMessage(DefaultOperatorValidator.class,
                                "DefaultOperatorValidator.property_must_be_defined_for_a_global_entity")));
                type = VALIDATION_ERROR_KEY;
            }
        } catch(Exception e) {
            e.printStackTrace();
        } 
        if (report.getType().equals(VALIDATION_ERROR_KEY)) {
            type = VALIDATION_ERROR_KEY;
        }
        return new TcgComponentValidationReport(component, type, messageList, report.getChildReportList());
    }
}
