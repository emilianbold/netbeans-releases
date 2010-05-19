/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.wlm.model.impl;

import java.util.List;

import javax.xml.namespace.QName;

import org.netbeans.modules.wlm.model.api.WLMComponent;
import org.netbeans.modules.wlm.model.api.WLMModel;
import org.netbeans.modules.wlm.model.utl.Util;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.w3c.dom.Comment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public abstract class WLMComponentBase extends
		AbstractDocumentComponent<WLMComponent> implements WLMComponent {

	/** Creates a new instance of WSDLComponentImpl */
	public WLMComponentBase(WLMModel model, org.w3c.dom.Element e) {
		super((WLMModelImpl) model, e);
	}

	public WLMModelImpl getModel() {
		return (WLMModelImpl) super.getModel();
	}

    public Class<? extends WLMComponent> getElementType() {
        return WLMComponent.class;
    }

	protected void populateChildren(List<WLMComponent> children) {
		NodeList nl = getPeer().getChildNodes();
		if (nl != null) {
			for (int i = 0; i < nl.getLength(); i++) {
				org.w3c.dom.Node n = nl.item(i);
				if (n instanceof Element) {
					WLMModel wmodel = getModel();
					WLMComponentBase comp = (WLMComponentBase) wmodel
							.getFactory().create((Element) n, this);
					if (comp != null) {
						children.add(comp);
					}
				}
			}
		}
	}

	protected static org.w3c.dom.Element createNewElement(QName qName,
			WLMModel model) {
		return model.getDocument().createElementNS(qName.getNamespaceURI(),
				qName.getLocalPart());
	}

	protected static org.w3c.dom.Element createPrefixedElement(QName qName,
			WLMModel model) {
		String qualified = qName.getPrefix() == null ? qName.getLocalPart()
				: qName.getPrefix() + ":" + qName.getLocalPart();
		return model.getDocument().createElementNS(qName.getNamespaceURI(),
				qualified);
	}

	protected Object getAttributeValueOf(Attribute attr, String stringValue) {
		return stringValue;
	}

	/**
	 * This method is return corrected Text content without XML comments. See
	 * the problem appeared in getText() method.
	 * 
	 */
	protected String getCorrectedText() {
		try {
			StringBuilder text = new StringBuilder();
			NodeList nodeList = getPeer().getChildNodes();
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				if (node instanceof Text && !(node instanceof Comment)) {
					text.append(node.getNodeValue());
				}
			}
			/*
			 * TODO : there is bug in XAM/XDM. XML entities such as &gt;,
			 * &apos;, &quot; is not recognized. The method below perform
			 * replacement in string pointed entities to corresponding values.
			 * Usage of this method possibly should be removed when bug in
			 * XAM/XDM will be fixed. Fix for #84651
			 */
			return Util.hackXmlEntities(text.toString());
		} finally {
		}
	}

	// public <T extends ReferenceableWLMComponent> NamedComponentReference<T>
	// createReferenceTo(T target, Class<T> type) {
	// // TODO Auto-generated method stub
	// return null;
	// }
        
//        public void removeChild(WLMComponent child) {
//            super.removeChild("", child);
//        }
}
