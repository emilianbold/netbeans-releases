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
package org.netbeans.modules.iep.editor.wizard;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.iep.editor.model.NameGenerator;
import org.netbeans.modules.iep.model.share.SharedConstants;
import org.netbeans.modules.iep.model.IEPComponentFactory;
import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.OperatorComponent;
import org.netbeans.modules.iep.model.OperatorComponentContainer;
import org.netbeans.modules.iep.model.SchemaAttribute;
import org.netbeans.modules.iep.model.SchemaComponent;
import org.netbeans.modules.iep.model.SchemaComponentContainer;
import org.openide.WizardDescriptor;

/**
 *
 * @author radval
 */
public class IEPWizardHelper implements SharedConstants {

    public static void processUsingExistingSchema(IEPModel model, WizardDescriptor wizard) {
        //if user has provided attribute list
        //use it and create a stream input.
        List<PlaceholderSchemaAttribute> attrList = (List<PlaceholderSchemaAttribute>) wizard.getProperty(WizardConstants.WIZARD_SELECTED_ATTRIBUTE_LIST_KEY);

        if (attrList != null && !attrList.isEmpty()) {
            IEPComponentFactory factory = model.getFactory();

            //stream input
            String ctPath = "/IEP/Input/StreamInput"; //NOT I18N
            OperatorComponent operator = model.getFactory().createOperator(model, ctPath);
            OperatorComponentContainer opContainer = model.getPlanComponent().getOperatorComponentContainer();

            model.startTransaction();
            String name = NameGenerator.generateNewName(opContainer, operator.getComponentType());
            operator.setString(PROP_NAME, name);

            String id = NameGenerator.generateId(opContainer, "o");
            operator.setString(PROP_ID, id);
            operator.setName(id);
            operator.setTitle(id);
            operator.setInt(PROP_X, 50);
            operator.setInt(PROP_Y, 50);

            opContainer.addChildComponent(operator);

            //create schema
            SchemaComponent sComponent = factory.createSchema(model);
            SchemaComponentContainer scContainer = model.getPlanComponent().getSchemaComponentContainer();
            String schemaName = NameGenerator.generateSchemaName(scContainer);
            sComponent.setName(schemaName);
            sComponent.setTitle(schemaName);

            List<SchemaAttribute> attrs = new ArrayList<SchemaAttribute>();

            Iterator<PlaceholderSchemaAttribute> it = attrList.iterator();

            while (it.hasNext()) {
                PlaceholderSchemaAttribute attr = it.next();
                SchemaAttribute sa = factory.createSchemaAttribute(model);
                String attrName = attr.getAttributeName();
                if (attrName == null || attrName.trim().equals("")) {
                    continue;
                }
                sa.setName(attrName);
                sa.setTitle(attrName);
                sa.setAttributeName(attrName);
                sa.setAttributeType(attr.getAttributeType());
                sa.setAttributeSize(attr.getAttributeSize());
                sa.setAttributeScale(attr.getAttributeScale());

                attrs.add(sa);
            }

            sComponent.setSchemaAttributes(attrs);
            scContainer.addSchemaComponent(sComponent);

            operator.setString(PROP_OUTPUT_SCHEMA_ID, sComponent.getName());
            model.endTransaction();
        }
    }
}
