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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.iep.editor.share.SharedConstants;
import org.netbeans.modules.iep.editor.tcg.model.TcgComponent;
import org.netbeans.modules.iep.editor.tcg.model.TcgComponentValidationMsg;
import org.netbeans.modules.iep.editor.tcg.model.TcgComponentValidationReport;
import org.netbeans.modules.iep.editor.tcg.model.TcgComponentValidator;
import org.netbeans.modules.iep.editor.tcg.model.TcgProperty;
import org.netbeans.modules.iep.editor.tcg.ps.TcgPsI18n;
import org.openide.util.NbBundle;
import java.util.logging.Logger;
import org.netbeans.modules.iep.editor.tcg.model.DefaultValidator;
import org.netbeans.modules.iep.editor.tcg.model.TcgComponentType;

/**
 *
 * @author rdwivedi
 */
public class DefaultPlanValidator implements TcgComponentValidator, SharedConstants {
    
    private static Logger mLogger = Logger.getLogger(DefaultPlanValidator.class.getName());
    private DefaultValidator mDefaultValidator = new DefaultValidator();
    /** Creates a new instance of DefaultPlanValidator */
    public DefaultPlanValidator() {
    }
    
    public TcgComponentValidationReport validate(TcgComponent component) {
        TcgComponentValidationReport report = mDefaultValidator.validate(component);
        List messageList = report.getMessageList();
        String type = VALIDATION_OK_KEY;
        TcgComponent inputComp = null;
        TcgComponent outputComp = null;
        TcgComponent operators = null;
        
        List l = component.getComponentList();
        Iterator iter = l.iterator();
        while(iter.hasNext()){
            TcgComponent comp = (TcgComponent)iter.next();
            if(comp.getType().getName().equals(OPERATORS_KEY)) {
                operators = comp;
            }
        }
        
        if(operators == null) {
            addErrorMessage(messageList,null,"DefaultOperatorValidator.atleast_one_operator_required");
            type = VALIDATION_ERROR_KEY;
        } else {
                List ll = operators.getComponentList();
                Iterator iterl = ll.iterator();
                while(iterl.hasNext()){
                    TcgComponent compl = (TcgComponent)iterl.next();
                    
                    if(compl.getType().getName().equals(OP_STREAM_INPUT)||
                       compl.getType().getName().equals(OP_TABLE_INPUT)) {
                        inputComp = compl;
                    }
                    if(compl.getType().getName().equals(OP_STREAM_OUTPUT)||
                       compl.getType().getName().equals(OP_RELATION_OUTPUT)||
                       compl.getType().getName().equals(OP_TABLE_OUTPUT)) {
                       outputComp = compl;
                    }
                }
        }
        
        if(inputComp == null) {
            addErrorMessage(messageList,null,"DefaultOperatorValidator.atleast_one_input_required");
            type = VALIDATION_ERROR_KEY;
        }
        if(outputComp == null) {
            addErrorMessage(messageList,null,"DefaultOperatorValidator.atleast_one_output_required");
            type = VALIDATION_ERROR_KEY;
        }
        
        
        /*TcgComponentValidationReport report = null;
        ArrayList messageList = new ArrayList();
        String type = VALIDATION_OK_KEY;
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
         * */
        
        return new TcgComponentValidationReport(component, type, messageList, report.getChildReportList());
    }
    
    private void addErrorMessage(List mL ,TcgComponentType compType, String messageKey ) {
        TcgComponentValidationMsg  msg = new TcgComponentValidationMsg(VALIDATION_ERROR_KEY,
                ((compType==null)? "":"'" + TcgPsI18n.getDisplayName(compType) + "' ") +
                NbBundle.getMessage(DefaultOperatorValidator.class,
                messageKey));
        mL.add(msg);
    }
    
    private void ensureAtLeatOneOperator() {
        
    }
    
}
