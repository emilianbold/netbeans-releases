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
import org.netbeans.modules.iep.editor.share.SharedConstants;
import org.netbeans.modules.iep.editor.tcg.model.DefaultValidator;
import org.netbeans.modules.iep.editor.tcg.model.TcgComponent;
import org.netbeans.modules.iep.editor.tcg.model.TcgComponentValidationMsg;
import org.netbeans.modules.iep.editor.tcg.model.TcgComponentValidationReport;
import org.netbeans.modules.iep.editor.tcg.model.TcgComponentValidator;
import org.netbeans.modules.iep.editor.tcg.model.TcgProperty;
import org.netbeans.modules.iep.editor.tcg.ps.TcgPsI18n;
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
