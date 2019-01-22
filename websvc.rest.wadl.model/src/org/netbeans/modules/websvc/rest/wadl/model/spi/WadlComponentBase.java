/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.websvc.rest.wadl.model.spi;

import org.netbeans.modules.websvc.rest.wadl.model.impl.*;
import java.util.List;
import java.util.Vector;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent;
import org.netbeans.modules.websvc.rest.wadl.model.*;
import org.netbeans.modules.websvc.rest.wadl.model.extensions.xsd.impl.SchemaReferenceImpl;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * 
 */
public abstract class WadlComponentBase extends AbstractDocumentComponent<WadlComponent> implements WadlComponent {

    /** Creates a new instance of WadlComponentImpl */
    public WadlComponentBase(WadlModel model, org.w3c.dom.Element e) {
        super((WadlModelImpl) model, e);
    }
    
    @Override
    public WadlModel getModel() {
        return (WadlModel) super.getModel();
    }
    
    protected void populateChildren(List<WadlComponent> children) {
        NodeList nl = getPeer().getChildNodes();
        if (nl != null){
            for (int i = 0; i < nl.getLength(); i++) {
                org.w3c.dom.Node n = nl.item(i);
                if (n instanceof Element) {
                    WadlModel wmodel = getModel();
                    WadlComponentBase comp = (WadlComponentBase) wmodel.getFactory().create((Element)n,this);
                    if (comp != null) {
                        children.add(comp);
                    }
                }
            }
        }
    }
    
    protected static org.w3c.dom.Element createNewElement(QName qName, WadlModel model){
        return model.getDocument().createElementNS(qName.getNamespaceURI(), qName.getLocalPart());
    }
    
    protected static org.w3c.dom.Element createPrefixedElement(QName qName, WadlModel model){
        String qualified = qName.getPrefix() == null ? qName.getLocalPart() : 
            qName.getPrefix() + ":" + qName.getLocalPart();
        return model.getDocument().createElementNS(qName.getNamespaceURI(), qualified);
    }
    
    public void setDoc(Doc doc) {
        setChildBefore(Doc.class, DOC_PROPERTY, doc, TypeCollection.ALL.types());
    }
    
    public Doc getDoc() {
        return getChild(Doc.class);
    }
    
    protected Object getAttributeValueOf(Attribute attr, String stringValue) {
        return stringValue;
    }
    
    protected <T extends ReferenceableWadlComponent> NamedComponentReference<T> resolveGlobalReference(
            Class<T> c, Attribute attrName) {
        String v = getAttribute(attrName);
        return v == null ? null : new GlobalReferenceImpl<T>(c, this, v);
    }
    
    public WadlModel getWadlModel() {
        return getModel();
    }
    
    public <T extends ReferenceableWadlComponent> NamedComponentReference<T> createReferenceTo(T target, Class<T> type) {
        return new GlobalReferenceImpl<T>(target, type, this);
    }
    
    protected <T extends ReferenceableSchemaComponent>
            NamedComponentReference<T> resolveSchemaReference(Class<T> c, Attribute attrName) {
        String v = getAttribute(attrName);
        return v == null ? null : new SchemaReferenceImpl<T>(c, this, v);
    }
    
    public <T extends ReferenceableSchemaComponent> 
            NamedComponentReference<T> createSchemaReference(T target, Class<T> type) {
        return new SchemaReferenceImpl<T>( target, type, this);
    }
    
    public String toString(QName qname) {
        return getPrefixedName(qname.getNamespaceURI(), qname.getLocalPart());
    }

    public boolean canPaste(Component child) {
        if (child instanceof DocumentComponent) {
            return new ChildComponentUpdateVisitor().canAdd(this, (DocumentComponent) child);
        } else {
            return false;
        }
    }

    public ParamStyle[] getValidParamStyles() {
        Vector<ParamStyle> v = new Vector<ParamStyle>();
        return (ParamStyle[]) v.toArray(new ParamStyle[0]);
    }

    public String[] getValidParamStyles(boolean toUpper) {
        Vector<String> v = new Vector<String>();
        return (String[]) v.toArray(new String[0]);
    }
}
