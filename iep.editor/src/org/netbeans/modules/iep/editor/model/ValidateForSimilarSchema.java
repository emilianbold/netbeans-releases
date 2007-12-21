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
import java.util.logging.Logger;
import java.util.logging.Level;
import org.netbeans.modules.iep.editor.share.SharedConstants;
import org.netbeans.modules.iep.editor.tcg.model.TcgModelConstants;
import org.netbeans.modules.iep.editor.tcg.ps.TcgPsI18n;
import org.netbeans.modules.iep.model.lib.TcgComponent;
import org.netbeans.modules.iep.model.lib.TcgComponentType;
import org.netbeans.modules.iep.model.lib.TcgComponentValidationMsg;
import org.netbeans.modules.iep.model.lib.TcgComponentValidationReport;
import org.netbeans.modules.iep.model.lib.TcgComponentValidator;
import org.openide.util.NbBundle;

/**
 *
 * @author rahul dwivedi
 */
public class ValidateForSimilarSchema implements TcgComponentValidator, SharedConstants {
    
    private static Logger mLogger = Logger.getLogger(ValidateForSimilarSchema.class.getName());
    private DefaultOperatorValidator mDefaultValidator = new DefaultOperatorValidator();
    
    /** Creates a new instance of ValidateForSimilarSchema */
    public ValidateForSimilarSchema() {
    }
    
    
    
    public TcgComponentValidationReport validate(TcgComponent component) {
        TcgComponentValidationReport report = mDefaultValidator.validate(component);
        List messageList = report.getMessageList();
        String type = VALIDATION_OK_KEY;
        TcgComponent parent = component.getParent().getParent();
        TcgComponent schemas = parent.getComponent(SCHEMAS_KEY);
        TcgComponent operators = parent.getComponent(OPERATORS_KEY);
        try {
            TcgComponent sca = null;
            TcgComponent scb = null;
            List inputIdList = component.getProperty(INPUT_ID_LIST_KEY).getListValue();
            if(inputIdList.size() > 1) {
                String id = (String)inputIdList.get(0);
                TcgComponent input = operators.getComponent(id);
                String outputSchemaId = input.getProperty(OUTPUT_SCHEMA_ID_KEY).getStringValue();
                sca = schemas.getComponent(outputSchemaId);
                
            }
            for(int i = 1, I = inputIdList.size(); i < I; i++) {
                String id = (String)inputIdList.get(i);
                TcgComponent input = operators.getComponent(id);
                String outputSchemaId = input.getProperty(OUTPUT_SCHEMA_ID_KEY).getStringValue();
                scb = schemas.getComponent(outputSchemaId);
                if(!ensureSchemasAreSame(sca,scb)) {
                    addErrorMessage(messageList,null,"ValidateForSimilarSchema.input_schemas_are_not_same");
                    type = VALIDATION_ERROR_KEY;
                }
            }
        } catch(Exception e) {
            mLogger.log(Level.WARNING, "Error ", e);
            addErrorMessage(messageList,null,"ValidateForSimilarSchema.input_schemas_are_not_same");
            type = VALIDATION_ERROR_KEY;
        }
        return new TcgComponentValidationReport(component, type, messageList, report.getChildReportList());
    }
    
    private void addErrorMessage(List mL ,TcgComponentType compType, String messageKey ) {
        TcgComponentValidationMsg  msg = new TcgComponentValidationMsg(VALIDATION_ERROR_KEY,
                ((compType==null)? "":"'" + TcgPsI18n.getDisplayName(compType) + "' ") +
                NbBundle.getMessage(ValidateForSimilarSchema.class,
                messageKey));
        mL.add(msg);
    }
    
    private boolean ensureSchemasAreSame(TcgComponent schemaA, TcgComponent schemaB)  {
        int cntA = schemaA.getComponentCount();// these are columns.
        int cntB = schemaB.getComponentCount();
        boolean isSame = true;
        if(cntA != cntB) {
            isSame = false;
        }
        List la = schemaA.getComponentList();
        List lb = schemaB.getComponentList();
        int i = 0;
        while(i < cntA && isSame) {
            isSame = ensureSameColumn((TcgComponent)la.get(i),(TcgComponent)lb.get(i));
            i++;
        }
        return isSame;
    }
    
    private boolean ensureSameColumn(TcgComponent cola, TcgComponent colb)  {
        int propCountA =  cola.getPropertyCount();
        int propCountB =  colb.getPropertyCount();
        if(propCountA != propCountB) {
            return  false;
        }
        try{
            if (!cola.getProperty(TcgModelConstants.NAME_KEY).getStringValue().equals(colb.getProperty(TcgModelConstants.NAME_KEY).getStringValue())) {
                return false;
            }
            if (!cola.getProperty(TYPE_KEY).getStringValue().equals(colb.getProperty(TYPE_KEY).getStringValue())) {
                return false;
            }
            if (!cola.getProperty(SIZE_KEY).getStringValue().equals(colb.getProperty(SIZE_KEY).getStringValue())) {
                return false;
            }
            if (!cola.getProperty(SCALE_KEY).getStringValue().equals(colb.getProperty(SCALE_KEY).getStringValue())) {
                return false;
            }
        } catch(Exception e) {
            mLogger.log(Level.WARNING, "Error ", e);
            return false;
        }
        return true;
    }
    
    
    
}
