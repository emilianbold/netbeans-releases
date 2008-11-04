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
package org.netbeans.modules.maven.model.profile.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.xml.namespace.QName;
import org.netbeans.modules.maven.model.profile.ProfilesComponent;
import org.netbeans.modules.maven.model.profile.ProfilesComponentFactory;
import org.netbeans.modules.maven.model.profile.ProfilesExtensibilityElement;
import org.netbeans.modules.maven.model.profile.ProfilesModel;
import org.netbeans.modules.maven.model.profile.ProfilesQName;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author mkleint
 */
public abstract class ProfilesComponentImpl extends AbstractDocumentComponent<ProfilesComponent>
        implements ProfilesComponent {

   public ProfilesComponentImpl(ProfilesModel model, Element e) {
        super(model, e);
    }
    
    @Override
    protected String getNamespaceURI() {
        return ProfilesQName.NS_URI;
    }
        
    @Override
    public ProfilesModel getModel() {
        return (ProfilesModel) super.getModel();
    }
    
    protected void populateChildren(List<ProfilesComponent> children) {
        //System.out.println("populateChildren: " + getPeer().getNodeName());
        NodeList nl = getPeer().getChildNodes();
        if (nl != null){
            ProfilesModel model = getModel();
            ProfilesComponentFactory componentFactory = model.getFactory();
            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                if (n instanceof Element) {
                    ProfilesComponent comp = componentFactory.create((Element)n, this);
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
    
    public static Element createElementNS(ProfilesModel model, ProfilesQName rq) {
        return createElementNS(model, rq.getQName());
    }

    public static Element createElementNS(ProfilesModel model, QName rq) {
        String qualified = rq.getPrefix() + ":" + rq.getLocalPart();
        return model.getDocument().createElementNS(
                rq.getNamespaceURI(), qualified);
    }
        
    public void removeExtensibilityElement(ProfilesExtensibilityElement ee) {
        removeChild(EXTENSIBILITY_ELEMENT_PROPERTY, ee);
    }
    
    public void addExtensibilityElement(ProfilesExtensibilityElement ee) {
        appendChild(EXTENSIBILITY_ELEMENT_PROPERTY, ee);
    }
    
    public List<ProfilesExtensibilityElement> getExtensibilityElements() {
        return getChildren(ProfilesExtensibilityElement.class);
    }
    
    public <T extends ProfilesExtensibilityElement> List<T> getExtensibilityElements(Class<T> type) {
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

    @Override
    public String getChildElementText(QName qname) {
        return super.getChildElementText(qname);
    }


    @Override
    public void setChildElementText(String propertyName, String text, QName qname) {
        super.setChildElementText(propertyName, text, qname);
    }

    protected final Collection<Class<? extends ProfilesComponent>> getClassesBefore(Class<? extends ProfilesComponent>[] ordering, Class current) {
        ArrayList<Class<? extends ProfilesComponent>> toRet = new ArrayList<Class<? extends ProfilesComponent>>();
        for (Class<? extends ProfilesComponent> ord : ordering) {
            if (ord.equals(current)) break;
            toRet.add(ord);
        }
        return toRet;
    }
    
}

