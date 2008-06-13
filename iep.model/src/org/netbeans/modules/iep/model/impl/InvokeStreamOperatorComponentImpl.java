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

package org.netbeans.modules.iep.model.impl;

import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.IEPReference;
import org.netbeans.modules.iep.model.InvokeStreamOperatorComponent;
import org.netbeans.modules.iep.model.Property;
import org.w3c.dom.Element;

/**
 *
 * @author radval
 */
public class InvokeStreamOperatorComponentImpl extends OperatorComponentImpl implements InvokeStreamOperatorComponent {

    public InvokeStreamOperatorComponentImpl(IEPModel model,  Element e) {
            super(model, e);
        }
        
    public InvokeStreamOperatorComponentImpl(IEPModel model) {
        super(model);
    }
    
    public void setExternalIEPProcessQualifiedName(String name) {
        Property p = super.getProperty(PROP_EXTERNAL_IEP_PROCESS_QUALIFIED_NAME);
        if(p == null) {
                p = getModel().getFactory().createProperty(getModel());
                p.setName(PROP_EXTERNAL_IEP_PROCESS_QUALIFIED_NAME);
                addProperty(p);

        }

        p.setValue(name);
    }

    public String getExternalIEPProcessQualifiedName() {
        String name = null;
        
        Property p = super.getProperty(PROP_EXTERNAL_IEP_PROCESS_QUALIFIED_NAME);
        if(p != null) {
            name = p.getValue();
        }

        return name;
    }

    public void setExternalOperatorName(String name) {
        Property p = super.getProperty(PROP_EXTERNAL_OPERATOR_NAME);
        if(p == null) {
                p = getModel().getFactory().createProperty(getModel());
                p.setName(PROP_EXTERNAL_OPERATOR_NAME);
                addProperty(p);

        }

        p.setValue(name);
    }

    public String getExternalOperatorName() {
        String name = null;
        
        Property p = super.getProperty(PROP_EXTERNAL_OPERATOR_NAME);
        if(p != null) {
            name = p.getValue();
        }

        return name;
    }

    public void setExternalOperator(IEPReference operator) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
