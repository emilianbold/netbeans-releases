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

import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.iep.editor.share.SharedConstants;
import org.netbeans.modules.iep.editor.tcg.model.TcgComponent;
import org.netbeans.modules.iep.editor.tcg.model.TcgComponentValidationMsg;
import org.netbeans.modules.iep.editor.tcg.model.TcgComponentValidationReport;
import org.netbeans.modules.iep.editor.tcg.model.TcgComponentValidator;
import org.netbeans.modules.iep.editor.tcg.ps.TcgPsI18n;
import org.openide.util.NbBundle;
import java.util.logging.Logger;
import org.netbeans.modules.iep.editor.tcg.model.DefaultValidator;
import org.netbeans.modules.iep.editor.tcg.model.TcgComponentType;

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
    
    private void validateForSpecificRules() {
        
    }
    
    
}
