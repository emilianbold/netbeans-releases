/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.iep.model.impl;

import java.util.List;

import javax.xml.namespace.QName;
import org.netbeans.modules.iep.model.IEPComponent;
import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.util.Util;

import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.w3c.dom.Comment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public abstract class IEPComponentBase extends
		AbstractDocumentComponent<IEPComponent> implements IEPComponent {

	/** Creates a new instance of WSDLComponentImpl */
	public IEPComponentBase(IEPModel model, org.w3c.dom.Element e) {
		super(model, e);
	}

	public IEPModelImpl getModel() {
		return (IEPModelImpl) super.getModel();
	}

	protected void populateChildren(List<IEPComponent> children) {
		NodeList nl = getPeer().getChildNodes();
		if (nl != null) {
			for (int i = 0; i < nl.getLength(); i++) {
				org.w3c.dom.Node n = nl.item(i);
				if (n instanceof Element) {
					IEPModel wmodel = getModel();
					IEPComponentBase comp = (IEPComponentBase) wmodel
							.getFactory().create((Element) n, this);
					if (comp != null) {
						children.add(comp);
					}
				}
			}
		}
	}

	protected static org.w3c.dom.Element createNewElement(QName qName,
			IEPModel model) {
		return model.getDocument().createElementNS(qName.getNamespaceURI(),
				qName.getLocalPart());
	}

	protected static org.w3c.dom.Element createPrefixedElement(QName qName,
			IEPModel model) {
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

	// public <T extends ReferenceableIEPComponent> NamedComponentReference<T>
	// createReferenceTo(T target, Class<T> type) {
	// // TODO Auto-generated method stub
	// return null;
	// }
        
        public void removeChild(IEPComponent child) {
            super.removeChild("", child);
        }
}
