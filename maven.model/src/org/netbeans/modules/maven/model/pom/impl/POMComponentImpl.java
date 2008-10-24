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
package org.netbeans.modules.maven.model.pom.impl;

import java.util.List;
import javax.xml.namespace.QName;
import org.netbeans.modules.maven.model.pom.POMComponent;
import org.netbeans.modules.maven.model.pom.POMComponentFactory;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.POMQName;
import org.netbeans.modules.maven.model.pom.POMExtensibilityElement;
import org.netbeans.modules.maven.model.pom.ReferenceablePOMComponent;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author mkleint
 */
public abstract class POMComponentImpl extends AbstractDocumentComponent<POMComponent>
        implements POMComponent {

   public POMComponentImpl(POMModel model, Element e) {
        super(model, e);
    }
    
    @Override
    protected String getNamespaceURI() {
        return POMQName.NS_URI;
    }
        
    @Override
    public POMModel getModel() {
        return (POMModel) super.getModel();
    }
    
    protected void populateChildren(List<POMComponent> children) {
        //System.out.println("populateChildren: " + getPeer().getNodeName());
        NodeList nl = getPeer().getChildNodes();
        if (nl != null){
            POMModel model = getModel();
            POMComponentFactory componentFactory = model.getFactory();
            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                if (n instanceof Element) {
                    POMComponent comp = componentFactory.create((Element)n, this);
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
    
    public static Element createElementNS(POMModel model, POMQName rq) {
        return model.getDocument().createElementNS(
                rq.getQName().getNamespaceURI(), rq.getQualifiedName());
    }
        
    protected <T extends ReferenceablePOMComponent> NamedComponentReference<T> 
            resolveGlobalReference(            
            Class<T> c, Attribute attrName) {        
        String v = getAttribute(attrName);
        return v == null ? null : new GlobalReferenceImpl<T>(c, this, v);
    }
    
    public <T extends ReferenceablePOMComponent> NamedComponentReference<T> 
            createReferenceTo(T target, Class<T> type) {        
        return new GlobalReferenceImpl<T>(target, type, this);
    }
        
    public void removeExtensibilityElement(POMExtensibilityElement ee) {
        removeChild(EXTENSIBILITY_ELEMENT_PROPERTY, ee);
    }
    
    public void addExtensibilityElement(POMExtensibilityElement ee) {
        appendChild(EXTENSIBILITY_ELEMENT_PROPERTY, ee);
    }
    
    public List<POMExtensibilityElement> getExtensibilityElements() {
        return getChildren(POMExtensibilityElement.class);
    }
    
    public <T extends POMExtensibilityElement> List<T> getExtensibilityElements(Class<T> type) {
        return getChildren(type);
    }

    /**
     * Utility method to get the QName of a QName-type attribute value.
     * @param qNameTypeAttributeValue
     * @return
     */
    protected QName getQName(String qNameTypeAttributeValue) {
        QName ret = null;
        if (qNameTypeAttributeValue != null) {
            int colonIndex = qNameTypeAttributeValue.indexOf(":");  // NOI18N
            if (colonIndex != -1) {
                String prefix = qNameTypeAttributeValue.substring(0, colonIndex);
                String localPart = qNameTypeAttributeValue.substring(colonIndex + 1);
                String namespaceURI = lookupNamespaceURI(prefix);
                ret = new QName(namespaceURI, localPart, prefix);
            } else {
                String localPart = qNameTypeAttributeValue;
                String namespaceURI = lookupNamespaceURI(null);
                ret = new QName(namespaceURI, localPart);
            }
        }
        return ret;
    }   
    
}

