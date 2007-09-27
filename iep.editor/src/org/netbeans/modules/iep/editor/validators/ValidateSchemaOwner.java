/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.iep.editor.validators;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.*;
import org.netbeans.modules.iep.editor.model.DefaultOperatorValidator;
import org.netbeans.modules.iep.editor.tcg.exception.I18nException;
import org.netbeans.modules.iep.editor.tcg.model.TcgComponent;
import org.netbeans.modules.iep.editor.tcg.model.TcgComponentType;
import org.netbeans.modules.iep.editor.tcg.model.TcgComponentValidationMsg;
import org.netbeans.modules.iep.editor.tcg.model.TcgComponentValidationReport;
import org.netbeans.modules.iep.editor.tcg.model.TcgModelConstants;
import org.netbeans.modules.iep.editor.tcg.ps.TcgPsI18n;
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