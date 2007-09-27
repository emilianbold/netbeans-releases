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


package org.netbeans.modules.iep.editor.model;

import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.netbeans.modules.iep.editor.share.SharedConstants;
import org.netbeans.modules.iep.editor.tcg.model.TcgComponent;
import org.netbeans.modules.iep.editor.tcg.model.TcgComponentType;
import org.netbeans.modules.iep.editor.tcg.model.TcgComponentValidationMsg;
import org.netbeans.modules.iep.editor.tcg.model.TcgComponentValidationReport;
import org.netbeans.modules.iep.editor.tcg.model.TcgComponentValidator;
import org.netbeans.modules.iep.editor.tcg.model.TcgModelConstants;
import org.netbeans.modules.iep.editor.tcg.ps.TcgPsI18n;
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
