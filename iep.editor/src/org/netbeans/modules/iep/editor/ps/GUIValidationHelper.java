/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.iep.editor.ps;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.text.ParseException;
import org.netbeans.modules.iep.model.share.SharedConstants;
import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.OperatorComponent;
import org.netbeans.modules.iep.model.OperatorComponentContainer;
import org.netbeans.modules.iep.model.Property;
import org.netbeans.modules.iep.model.SchemaComponent;
import org.netbeans.modules.iep.model.SchemaComponentContainer;
import org.netbeans.modules.tbls.model.TcgPropertyType;
import org.netbeans.modules.tbls.model.TcgType;
import org.openide.util.NbBundle;

/**
 *
 * @author radval
 */
public class GUIValidationHelper {

    public static void validateForUniqueOperatorName(IEPModel model, 
                                            OperatorComponent component,
                                            String newName,
                                            PropertyChangeEvent evt) throws PropertyVetoException {
        OperatorComponentContainer ocContainer = model.getPlanComponent().getOperatorComponentContainer();
        
            
            // name
            String name = component.getString(SharedConstants.PROP_NAME);
            if (!newName.equals(name) && ocContainer.findOperator(newName) != null) {
                String msg = NbBundle.getMessage(DefaultCustomEditor.class,
                        "CustomEditor.NAME_IS_ALREADY_TAKEN_BY_ANOTHER_OPERATOR",
                        newName);
                throw new PropertyVetoException(msg, evt);
            }
    }
    
    public static void validateForUniqueSchemaName(IEPModel model, 
                                                   OperatorComponent component,
                                                   String newSchemaName,
                                                   PropertyChangeEvent evt) throws PropertyVetoException {
        
        SchemaComponent outputSchema = component.getOutputSchema();
        SchemaComponentContainer scContainer = model.getPlanComponent().getSchemaComponentContainer();
        
        String schemaName = null;
        if(outputSchema != null) {
            schemaName = outputSchema.getName();
        }
                
        if (!newSchemaName.equals(schemaName) && scContainer.findSchema(newSchemaName) != null) {
            String msg = NbBundle.getMessage(DefaultCustomEditor.class,
                    "CustomEditor.OUTPUT_SCHEMA_NAME_IS_ALREADY_TAKENBY_ANOTHER_SCHEMA",
                    newSchemaName);
            throw new PropertyVetoException(msg, evt);
        }
    }
    
    public static void validateProperty(String propertyLabel,
                                 String propertyValue,
                                 Property property,
                                 PropertyChangeEvent evt) throws PropertyVetoException {
        TcgPropertyType pt = property.getPropertyType();
        String value =  propertyValue;
        // if value is required, it must be specified
        if (pt.isRequired()) {
            if ((value == null) || value.trim().equals("")) {
                String msg = NbBundle.getMessage(PropertyPanel.class,
                        "PropertyPanel.PROPERTY_CANNOT_BE_EMPTY",
                        propertyLabel);
                throw new PropertyVetoException(msg, evt);
            }
        }
        if ((value == null) || value.trim().equals("")) {
            return;
        }
        // if value is specified, then it must be valid
        if (pt.getType() == TcgType.INTEGER) {
            try {
                Integer.parseInt(value);
            } catch (NumberFormatException e) {
                String msg = NbBundle.getMessage(PropertyPanel.class,
                        "PropertyPanel.PROPERTY_IS_NOT_A_VALID_INTEGER",
                        propertyLabel);
                throw new PropertyVetoException(msg, evt);
            }
        }
        if (pt.getType() == TcgType.LONG) {
            try {
                Long.parseLong(value);
            } catch (NumberFormatException e) {
                String msg = NbBundle.getMessage(PropertyPanel.class,
                        "PropertyPanel.PROPERTY_IS_NOT_A_VALID_LONG_INTEGER",
                        propertyLabel);
                throw new PropertyVetoException(msg, evt);
            }
        }
        if (pt.getType() == TcgType.DOUBLE) {
            try {
                Double.parseDouble(value);
            } catch (NumberFormatException e) {
                String msg = NbBundle.getMessage(PropertyPanel.class,
                        "PropertyPanel.PROPERTY_IS_NOT_A_VALID_NUMBER",
                        propertyLabel);
                throw new PropertyVetoException(msg, evt);
            }
        }
        if (pt.getType() == TcgType.DATE) {
            try {
                //caller should pass write value
                //value = value + mTemp.toString();
                SharedConstants.DATE_FORMAT.parse(value);
            } catch (ParseException e) {
                String msg = NbBundle.getMessage(PropertyPanel.class,
                        "PropertyPanel.PROPERTY_IS_NOT_A_VALID_TIME",
                        propertyLabel);
                throw new PropertyVetoException(msg, evt);
            }
        }
    }
}
