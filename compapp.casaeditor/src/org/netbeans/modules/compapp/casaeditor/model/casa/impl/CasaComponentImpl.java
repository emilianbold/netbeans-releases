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
package org.netbeans.modules.compapp.casaeditor.model.casa.impl;

import java.util.List;
import org.netbeans.modules.compapp.casaeditor.model.*;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponent;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponentFactory;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaModel;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaQName;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaExtensibilityElement;
import org.netbeans.modules.compapp.casaeditor.model.casa.ReferenceableCasaComponent;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author jqian
 */
public abstract class CasaComponentImpl extends AbstractDocumentComponent<CasaComponent>
        implements CasaComponent {

   public CasaComponentImpl(CasaModel model, Element e) {
        super(model, e);
    }
    
    @Override
    protected String getNamespaceURI() {
        return CasaQName.CASA_NS_URI;
    }
        
    @Override
    public CasaModel getModel() {
        return (CasaModel) super.getModel();
    }
    
    protected void populateChildren(List<CasaComponent> children) {
        //System.out.println("populateChildren: " + getPeer().getNodeName());
        NodeList nl = getPeer().getChildNodes();
        if (nl != null){
            CasaModel model = getModel();
            CasaComponentFactory componentFactory = model.getFactory();
            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                if (n instanceof Element) {
                    CasaComponent comp = componentFactory.create((Element)n, this);
                    if (comp != null) {
                        children.add(comp);
                    }
                }
            }
        }
    }

    protected Object getAttributeValueOf(Attribute attribute, String stringValue) {
        return stringValue;
    }  
    
    public static Element createElementNS(CasaModel model, CasaQName rq) {
        return model.getDocument().createElementNS(
                rq.getQName().getNamespaceURI(), rq.getQualifiedName());
    }
        
    protected <T extends ReferenceableCasaComponent> NamedComponentReference<T> 
            resolveGlobalReference(            
            Class<T> c, Attribute attrName) {
        
        String v = getAttribute(attrName);
        return v == null ? null : new GlobalReferenceImpl<T>(c, this, v);
    }
    
    public <T extends ReferenceableCasaComponent> NamedComponentReference<T> 
            createReferenceTo(T target, Class<T> type) {
        
        return new GlobalReferenceImpl<T>(target, type, this);
    }
        
    public void removeExtensibilityElement(CasaExtensibilityElement ee) {
        removeChild(EXTENSIBILITY_ELEMENT_PROPERTY, ee);
    }
    
    public void addExtensibilityElement(CasaExtensibilityElement ee) {
        appendChild(EXTENSIBILITY_ELEMENT_PROPERTY, ee);
    }
    
    public List<CasaExtensibilityElement> getExtensibilityElements() {
        return getChildren(CasaExtensibilityElement.class);
    }
    
    public <T extends CasaExtensibilityElement> List<T> getExtensibilityElements(Class<T> type) {
        return getChildren(type);
    }
}

