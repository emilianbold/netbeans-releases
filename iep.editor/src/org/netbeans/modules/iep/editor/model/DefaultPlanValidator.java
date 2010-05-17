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

import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.iep.model.share.SharedConstants;
import org.netbeans.modules.tbls.editor.ps.TcgPsI18n;
import org.openide.util.NbBundle;
import java.util.logging.Logger;
import org.netbeans.modules.tbls.model.DefaultValidator;
import org.netbeans.modules.tbls.model.TcgComponent;
import org.netbeans.modules.tbls.model.TcgComponentType;
import org.netbeans.modules.tbls.model.TcgComponentValidationMsg;
import org.netbeans.modules.tbls.model.TcgComponentValidationReport;
import org.netbeans.modules.tbls.model.TcgComponentValidator;

/**
 *
 * @author rahul dwivedi
 */
public class DefaultPlanValidator implements TcgComponentValidator, SharedConstants {
    
    private static Logger mLogger = Logger.getLogger(DefaultPlanValidator.class.getName());
    private DefaultValidator mDefaultValidator = new DefaultValidator();
    /** Creates a new instance of DefaultPlanValidator */
    public DefaultPlanValidator() {
    }
    
    public TcgComponentValidationReport validate(TcgComponent component) {
        TcgComponentValidationReport report = mDefaultValidator.validate(component);
        List<TcgComponentValidationMsg> messageList = report.getMessageList();
        String type = VALIDATION_OK_KEY;
        TcgComponent operators = null;
        
        List l = component.getComponentList();
        Iterator iter = l.iterator();
        while(iter.hasNext()){
            TcgComponent comp = (TcgComponent)iter.next();
            if(comp.getType().getName().equals(COMP_OPERATORS)) {
                operators = comp;
                break;
            }
        }
        
        if(operators == null) {
            addErrorMessage(messageList,null,"DefaultOperatorValidator.at_least_one_operator_required");
            type = VALIDATION_ERROR_KEY;
        } 
        
        return new TcgComponentValidationReport(component, type, messageList, report.getChildReportList());
    }
    
    private void addErrorMessage(List<TcgComponentValidationMsg> mL ,TcgComponentType compType, String messageKey ) {
        TcgComponentValidationMsg  msg = new TcgComponentValidationMsg(VALIDATION_ERROR_KEY,
                ((compType==null)? "":"'" + TcgPsI18n.getDisplayName(compType) + "' ") +
                NbBundle.getMessage(DefaultOperatorValidator.class,
                messageKey));
        mL.add(msg);
    }
    
    private void ensureAtLeatOneOperator() {
        
    }
    
    private void validateForSpecificRules() {
        
    }
    
    
}
