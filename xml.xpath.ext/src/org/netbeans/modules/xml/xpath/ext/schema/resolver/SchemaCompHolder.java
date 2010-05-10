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

package org.netbeans.modules.xml.xpath.ext.schema.resolver;

import org.netbeans.modules.xml.schema.model.Attribute;
import org.netbeans.modules.xml.schema.model.Element;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.xpath.ext.XPathUtils;
import org.netbeans.modules.xml.xpath.ext.schema.SchemaModelsStack;
import org.netbeans.modules.xml.xpath.ext.spi.XPathPseudoComp;

/**
 * Represents a wrapper for objects, which can be a part of Schema Context.
 * It's implied that such objects represents XPath location step. 
 * There are the following possible cases:
 *  - Type
 *  - Element
 *  - Attribute 
 *  - Pseudo Element
 *  - Pseudo Attribute
 * 
 * @author nk160297
 */
public interface SchemaCompHolder<T> {
    T getHeldComponent();
    ComponentType getComponentType();
    String getName();
    String getNamespace(SchemaModelsStack sms);
    SchemaComponent getSchemaComponent();
    boolean isPseudoComp();
    boolean isPrefixRequired();
    boolean isAttribute();

    final class Factory {
        public static SchemaCompHolder construct(SchemaComponent sc) {
            if (sc == null) {
                return null;
            } else if (sc instanceof Element) {
                return new ElementHolder((Element)sc);
            } else if (sc instanceof Attribute) {
                return new AttributeHolder((Attribute)sc);
            } else if (sc instanceof GlobalType) {
                return new GTypeHolder((GlobalType)sc);
            } else {
                return null;
            }
        }
        
        public static SchemaCompHolder construct(XPathPseudoComp pseudoComp) {
            if (pseudoComp == null) {
                return null;
            } else if (pseudoComp.isAttribute()) {
                return new PseudoAttributeHolder(pseudoComp);
            } else {
                return new PseudoElementHolder(pseudoComp);
            }
        } 
    }
    
    enum ComponentType {
        TYPE(GlobalType.class),
        ELEMENT(Element.class), 
        ATTRIBUTE(Attribute.class), 
        PSEUDO_ELEMENT(PseudoElementHolder.class), 
        PSEUDO_ATTRIBUTE(PseudoAttributeHolder.class);
        
        private Class mClass;
        
        private ComponentType(Class aClass) {
            mClass = aClass;
        }
        
        public Class getType() {
            return mClass;
        }
    }
    
    final class GTypeHolder implements SchemaCompHolder<GlobalType> {
        public GlobalType mGType; 
        
        public GTypeHolder(GlobalType aType) {
            assert aType != null;
            mGType = aType;
        }
        
        public GlobalType getHeldComponent() {
            return mGType;
        }
        
        public ComponentType getComponentType() {
            return ComponentType.ELEMENT;
        }
        
        public String getName() {
            return mGType.getName();
        }
        
        public String getNamespace(SchemaModelsStack sms) {
            if (sms == null) {
                return mGType.getModel().getEffectiveNamespace(mGType);
            } else {
                return SchemaModelsStack.getEffectiveNamespace(mGType, sms);
            }
        }
        
        public SchemaComponent getSchemaComponent() {
            return mGType;
        }
        
        public boolean isPseudoComp() {
            return false;
        }

        public boolean isPrefixRequired() {
            return true;
        }
        
        public boolean isAttribute() {
            return false;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            String name = this.getName();
            hash = 67 * hash + (name != null ? name.hashCode() : 0);
            return hash;
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof GTypeHolder) {
                GTypeHolder holder = (GTypeHolder)other;
                return holder.mGType.equals(this.mGType);
            }
            return false;
        }
        
        @Override
        public String toString() {
            return getName();
        }
    }
    
    final class ElementHolder implements SchemaCompHolder<Element> {
        public Element mElement; 
        
        public ElementHolder(Element anElement) {
            assert anElement instanceof Named;
            mElement = anElement;
        }
        
        public Element getHeldComponent() {
            return mElement;
        }
        
        public ComponentType getComponentType() {
            return ComponentType.ELEMENT;
        }

        public String getName() {
            return ((Named)mElement).getName();
        }
        
        public String getNamespace(SchemaModelsStack sms) {
            if (sms == null) {
                return mElement.getModel().getEffectiveNamespace(mElement);
            } else {
                return SchemaModelsStack.getEffectiveNamespace(mElement, sms);
            }
        }
        
        public SchemaComponent getSchemaComponent() {
            return mElement;
        }
        
        public boolean isPseudoComp() {
            return false;
        }
        
        public boolean isPrefixRequired() {
            return XPathUtils.isPrefixRequired(mElement);
        }

        public boolean isAttribute() {
            return false;
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof ElementHolder) {
                ElementHolder holder = (ElementHolder)other;
                return holder.mElement.equals(this.mElement);
            }
            return false;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            String name = this.getName();
            hash = 47 * hash + (name != null ? name.hashCode() : 0);
            return hash;
        }
        
        @Override
        public String toString() {
            return getName();
        }
    }
    
    final class AttributeHolder implements SchemaCompHolder<Attribute> {
        public Attribute mAttribute; 
        
        public AttributeHolder(Attribute anAttribute) {
            assert anAttribute instanceof Named;
            mAttribute = anAttribute;
        }
        
        public Attribute getHeldComponent() {
            return mAttribute;
        }
        
        public ComponentType getComponentType() {
            return ComponentType.ATTRIBUTE;
        }

        public String getName() {
            return ((Named)mAttribute).getName();
        }

        public String getNamespace(SchemaModelsStack sms) {
            if (sms == null) {
                return mAttribute.getModel().getEffectiveNamespace(mAttribute);
            } else {
                return SchemaModelsStack.getEffectiveNamespace(mAttribute, sms);
            }
        }
        
        public SchemaComponent getSchemaComponent() {
            return mAttribute;
        }
        
        public boolean isPseudoComp() {
            return false;
        }
        
        public boolean isPrefixRequired() {
            return XPathUtils.isPrefixRequired(mAttribute);
        }

        public boolean isAttribute() {
            return true;
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof AttributeHolder) {
                AttributeHolder holder = (AttributeHolder)other;
                return holder.mAttribute.equals(this.mAttribute);
            }
            return false;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            String name = this.getName();
            hash = 43 * hash + (name != null ? name.hashCode() : 0);
            return hash;
        }
        
        @Override
        public String toString() {
            return "@" + getName();
        }
    }
    
    final class PseudoElementHolder implements SchemaCompHolder<XPathPseudoComp> {
        public XPathPseudoComp mPseudoComp; 
        
        public PseudoElementHolder(XPathPseudoComp pseudoComp) {
            mPseudoComp = pseudoComp;
        }
        
        public XPathPseudoComp getHeldComponent() {
            return mPseudoComp;
        }
        
        public ComponentType getComponentType() {
            return ComponentType.PSEUDO_ELEMENT;
        }

        public String getName() {
            return mPseudoComp.getName();
        }

        public String getNamespace(SchemaModelsStack sms) {
            return mPseudoComp.getNamespace();
        }
        
        public SchemaComponent getSchemaComponent() {
            return mPseudoComp.getType();
        }
        
        public boolean isPseudoComp() {
            return true;
        }
        
        public boolean isPrefixRequired() {
            return true;
        }

        public boolean isAttribute() {
            return false;
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof PseudoElementHolder) {
                PseudoElementHolder holder = (PseudoElementHolder)other;
                return holder.mPseudoComp.equals(this.mPseudoComp);
            }
            return false;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            String name = this.getName();
            hash = 67 * hash + (name != null ? name.hashCode() : 0);
            return hash;
        }
        
        @Override
        public String toString() {
            return "#" + getName();
        }
    }
    
    final class PseudoAttributeHolder implements SchemaCompHolder<XPathPseudoComp> {
        public XPathPseudoComp mPseudoComp; 
        
        public PseudoAttributeHolder(XPathPseudoComp pseudoComp) {
            mPseudoComp = pseudoComp;
        }
        
        public XPathPseudoComp getHeldComponent() {
            return mPseudoComp;
        }
        
        public ComponentType getComponentType() {
            return ComponentType.PSEUDO_ATTRIBUTE;
        }
        
        public String getName() {
            return mPseudoComp.getName();
        }

        public String getNamespace(SchemaModelsStack sms) {
            return mPseudoComp.getNamespace();
        }
        
        public SchemaComponent getSchemaComponent() {
            return mPseudoComp.getType();
        }
        
        public boolean isPseudoComp() {
            return true;
        }

        public boolean isPrefixRequired() {
            return true;
        }

        public boolean isAttribute() {
            return true;
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof PseudoAttributeHolder) {
                PseudoAttributeHolder holder = (PseudoAttributeHolder)other;
                return holder.mPseudoComp.equals(this.mPseudoComp);
            }
            return false;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            String name = this.getName();
            hash = 29 * hash + (name != null ? name.hashCode() : 0);
            return hash;
        }
        
        @Override
        public String toString() {
            return "@#" + getName();
        }
    }
    
}
