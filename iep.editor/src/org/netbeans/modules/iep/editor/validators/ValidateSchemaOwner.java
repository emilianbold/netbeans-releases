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
package org.netbeans.modules.iep.editor.validators;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.*;
import org.netbeans.modules.iep.editor.model.DefaultOperatorValidator;
import org.netbeans.modules.iep.editor.tcg.model.TcgModelConstants;
import org.netbeans.modules.iep.editor.tcg.ps.TcgPsI18n;
import org.netbeans.modules.iep.model.lib.I18nException;
import org.netbeans.modules.iep.model.lib.TcgComponent;
import org.netbeans.modules.iep.model.lib.TcgComponentType;
import org.netbeans.modules.iep.model.lib.TcgComponentValidationMsg;
import org.netbeans.modules.iep.model.lib.TcgComponentValidationReport;
import org.openide.util.NbBundle;
/**
 *
 * @author rdwivedi
 */
public class ValidateSchemaOwner extends DefaultOperatorValidator {
    private static Logger mLogger = Logger.getLogger(ValidateSchemaOwner.class.getName());
    private Pattern pattern = Pattern.compile("(\\w*)\\.(\\w*)");
    public ValidateSchemaOwner() {
        
    }
    
    public TcgComponentValidationReport validate(TcgComponent component) {
        
        ArrayList allowedColumnNames = new ArrayList();
        TcgComponentValidationReport report = super.validate(component);
        List messageList = report.getMessageList();
        String type = report.getType();
        TcgComponent parent = component.getParent().getParent();
        TcgComponent schemas = parent.getComponent(SCHEMAS_KEY);
        TcgComponent operators = parent.getComponent(OPERATORS_KEY);
        try {
            boolean isSchemaOwner = component.getProperty(IS_SCHEMA_OWNER_KEY).getBoolValue();
            TcgComponent scb = null;
            List inputIdList = component.getProperty(INPUT_ID_LIST_KEY).getListValue();
            for(int i = 0, I = inputIdList.size(); i < I; i++) {
                String id = (String)inputIdList.get(i);
                TcgComponent input = operators.getComponent(id);
                String outputSchemaId = input.getProperty(OUTPUT_SCHEMA_ID_KEY).getStringValue();
                scb = schemas.getComponent(outputSchemaId);
                appendPossibleColumnNames(allowedColumnNames,scb,input);
            }
            //to do: find way to get all the expression in a loop;
            List expressionList = getListOfExpressions(component);
            Iterator iter = expressionList.iterator();
            boolean founderror = false;
            boolean temp = false;
            while(iter.hasNext()) {
                String expression = (String)iter.next();
                temp = validateSelectExpression(allowedColumnNames,expression,messageList,null);
                if(!founderror && !temp) {
                    founderror = true;
                }
            }
            if(founderror) {
                type = VALIDATION_ERROR_KEY;
            }
            
        } catch(Exception e) {
            mLogger.log(Level.WARNING, "Error ", e);
            
        }
        return new TcgComponentValidationReport(component, type, messageList, report.getChildReportList());
    }
    
    private List getListOfExpressions(TcgComponent component) throws Exception {
        List la = component.getProperty(FROM_COLUMN_LIST_KEY).getListValue();
        return la;
    }
    
    
    private void appendPossibleColumnNames(ArrayList list ,TcgComponent schema,TcgComponent inputComponent) throws I18nException{
        List la = schema.getComponentList();
        String inputComponentName = inputComponent.getProperty(TcgModelConstants.NAME_KEY).getStringValue();
        Iterator columns = la.iterator();
        while(columns.hasNext()){
            TcgComponent cola = (TcgComponent)columns.next();
            String colName = cola.getProperty(TcgModelConstants.NAME_KEY).getStringValue();
            list.add(inputComponentName + "." + colName);
        }
        
    }
    private boolean validateSelectExpression(List allowedColumnsInExpression,
            String expression, List messageList,TcgComponentType compType) {
        boolean validated = true;
        Matcher m = pattern.matcher(expression);
        mLogger.info("The expression is " + expression);
        while(m.find()) {
            String fm = m.group();
            if(!allowedColumnsInExpression.contains(fm)) {
                TcgComponentValidationMsg  msg =
                        new TcgComponentValidationMsg(VALIDATION_ERROR_KEY,
                        ((compType==null)? "":"'" + TcgPsI18n.getDisplayName(compType)
                        + "' ") +
                        NbBundle.getMessage(ValidateSchemaOwner.class,
                        "Column_Not_Found",fm));
                messageList.add(msg);
                validated = false;
            }
        }
        return validated;
    }
    
    
    
    
    
}