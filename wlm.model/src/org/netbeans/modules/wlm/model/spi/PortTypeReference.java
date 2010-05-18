/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.wlm.model.spi;

import java.util.Collection;
import java.util.List;

import org.netbeans.modules.wlm.model.api.WLMComponent;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;

public class PortTypeReference extends WSDLReferenceImpl<PortType> {

	public PortTypeReference(AbstractDocumentComponent parent, String ref) {
		super(PortType.class, parent, ref);
	}

	public PortTypeReference(PortType referenced,
						AbstractDocumentComponent parent) {
		super(referenced, PortType.class, parent);
		// TODO Auto-generated constructor stub
	}

	public PortType get() {
		// TODO Auto-generated method stub
		String namespace = getEffectiveNamespace();
		WLMComponent wlmComp = WLMComponent.class.cast(getParent());
		Collection<WSDLModel> wsdlmodels = wlmComp.getModel().findWSDLModel(
				namespace);
		boolean found = false;
		for (WSDLModel model : wsdlmodels) {
			List<PortType> list = model.getDefinitions().getChildren(PortType.class);
			for (PortType portType : list) {
				if (getLocalName().equals(portType.getName())) {
					setReferenced(PortType.class.cast(portType)) ;
					found = true;
					break;
				}
			}
			if (found) {
				break;
            }
		}

		return getReferenced();
	}

}
