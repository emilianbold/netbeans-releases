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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.iep.model.impl;

import javax.xml.namespace.QName;
import org.netbeans.modules.iep.model.Component;
import org.netbeans.modules.iep.model.Documentation;
import org.netbeans.modules.iep.model.IEPComponent;
import org.netbeans.modules.iep.model.IEPComponentFactory;
import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.InputOperatorComponent;
import org.netbeans.modules.iep.model.LinkComponent;
import org.netbeans.modules.iep.model.OperatorComponent;
import org.netbeans.modules.iep.model.OutputOperatorComponent;
import org.netbeans.modules.iep.model.Property;
import org.netbeans.modules.iep.model.SchemaAttribute;
import org.netbeans.modules.iep.model.SchemaComponent;

import org.w3c.dom.Element;

/**
 * 
 * 
 */
public class IEPComponentFactoryImpl implements IEPComponentFactory {

	private IEPModel model;

	/** Creates a new instance of IEPComponentFactoryImpl */
	public IEPComponentFactoryImpl(IEPModel model) {
		this.model = model;
	}

	public IEPComponent create(Element element, IEPComponent context) {

		return context.createChild(element);
	}

	public IEPComponent create(IEPComponent parent, QName qName) {
		String q = qName.getPrefix();
		if (q == null || q.length() == 0) {
			q = qName.getLocalPart();
		} else {
			q = q + ":" + qName.getLocalPart();
		}
		Element element = model.getDocument().createElementNS(
				qName.getNamespaceURI(), q);
		return parent.createChild(element);
	}

    public Component createComponent(IEPModel model) {
        return new ComponentImpl(model);
    }

    public OperatorComponent createOperator(IEPModel model) {
    	return new OperatorComponentImpl(model);
    }
    
    public InputOperatorComponent createInputOperator(IEPModel model) {
    	return new InputOperatorComponentImpl(model);
    }
    
    public OutputOperatorComponent createOutputOperator(IEPModel model) {
    	return new OutputOperatorComponentImpl(model);
    }
    
    public LinkComponent createLink(IEPModel model) {
    	return new LinkComponentImpl(model);
    }
    
    public Property createProperty(IEPModel model) {
        return new PropertyImpl(model);
    }

	public SchemaComponent createSchema(IEPModel model) {
		return new SchemaComponentImpl(model);
	}

	public SchemaAttribute createSchemaAttribute(IEPModel model) {
		return new SchemaAttributeImpl(model);
	}

	public Documentation createDocumentation(IEPModel model) {
		return new DocumentationImpl(model);
	}
	
}
