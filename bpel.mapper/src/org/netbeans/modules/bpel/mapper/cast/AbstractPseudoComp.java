/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.bpel.mapper.cast;

import javax.xml.namespace.QName;
import org.netbeans.modules.bpel.model.api.AbstractVariableDeclaration;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.events.VetoException;
import org.netbeans.modules.bpel.model.api.references.SchemaReference;
import org.netbeans.modules.bpel.model.ext.editor.api.PseudoComp;
import org.netbeans.modules.bpel.model.ext.editor.api.Source;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.xpath.ext.schema.ExNamespaceContext;
import org.netbeans.modules.xml.xpath.ext.schema.InvalidNamespaceException;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.XPathSchemaContext;
import org.netbeans.modules.xml.xpath.ext.spi.XPathPseudoComp;
import org.openide.util.NbBundle;

/**
 * The base class for different kind of Pseudo Schema components.
 * @author nk160297
 */
public abstract class AbstractPseudoComp implements XPathPseudoComp {

    public static final String ANY_ELEMENT = 
            NbBundle.getMessage(AbstractPseudoComp.class, "ANY_ELEMENT"); // NOI18N
    
    public static final String ANY_ATTRIBUTE = 
            NbBundle.getMessage(AbstractPseudoComp.class, "ANY_ATTRIBUTE"); // NOI18N
    
    private DetachedPseudoComp mDPC;

    public AbstractPseudoComp(DetachedPseudoComp dpc) {
        mDPC = dpc;
    }
    
    public AbstractPseudoComp(GlobalType type, String name, 
            String namespace, boolean isAttribute) {
        mDPC = new DetachedPseudoComp(type, name, namespace, isAttribute);
    }
    
    public GlobalType getType() {
        return mDPC.getType();
    }

    public String getName() {
        return mDPC.getName();
    }

    public String getNamespace() {
        return mDPC.getNamespace();
    }
    
    public boolean isAttribute() {
        return mDPC.isAttribute();
    }

    public String getParentPathText() {
        return getParentPathExpression().getExpressionString();
    }

    public void setSchemaContext(XPathSchemaContext newContext) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public boolean equals(Object other) {
        if (other instanceof XPathPseudoComp) {
            XPathPseudoComp otherPC = (XPathPseudoComp)other;
            if (!equalTypeName(otherPC, this)) {
                return false;
            }
            //
            // Compare context
            if (!(otherPC.getSchemaContext().equalsChain(this.getSchemaContext()))) {
                return false;
            }
            //
            return true;
        }
        return false;
    }
    
    /**
     * Compare everything except the location.
     * @param other
     * @return
     */
    public static boolean equalTypeName(
            XPathPseudoComp onePC, XPathPseudoComp otherPC) {
        //
        if (otherPC.isAttribute() != onePC.isAttribute()) {
            return false;
        }
        if (!(otherPC.getName().equals(onePC.getName()))) {
            // different name
            return false;
        } 
        if (!(otherPC.getNamespace().equals(onePC.getNamespace()))) {
            // different namespace
            return false;
        } 
        if (!(otherPC.getType().equals(onePC.getType()))) {
            // different type
            return false;
        }
        //
        return true;
    }
    
    @Override
    public String toString() {
        if (isAttribute()) {
            return "/@" + getName(); 
        } else {
            return "/" + getName(); 
        }
    }

    /**
     * Returns the base variable if available. 
     * Base variable is once where the casted object is located. 
     * @return
     */
    public abstract AbstractVariableDeclaration getBaseVariable();
    
    public abstract boolean populatePseudoComp(PseudoComp target, 
            BpelEntity destination, boolean inLeftMapperTree) 
            throws ExtRegistrationException;
    
    protected boolean populatePseudoCompImpl(PseudoComp target, 
            BpelEntity destination, boolean inLeftMapperTree) 
            throws ExtRegistrationException {
        //
        GlobalType type = getType();
        SchemaReference<GlobalType> typeRef = target.createSchemaReference(
                (GlobalType)type, GlobalType.class);
        target.setType(typeRef);
        //
        try {
            String localName = getName();
            //
            String namespace = getNamespace();
            if (namespace != null && namespace.length() != 0) {
                // Register prefix for the new namespace
                ExNamespaceContext nsContext = destination.getNamespaceContext();
                nsContext.addNamespace(namespace);
            }
            //        
            QName qName = new QName(namespace, localName);
            target.setName(qName);
            target.setIsAttribute(isAttribute());
        } catch (VetoException ex) {
            throw new ExtRegistrationException(ex);
        } catch (InvalidNamespaceException ex) {
            throw new ExtRegistrationException(ex);
        }
        //
        if (inLeftMapperTree) {
            target.setSource(Source.FROM);
        } else {
            target.setSource(Source.TO);
        }
        //
        return true;
    }

    //------------------------------------------------------------------
    
    public static boolean populatePseudoComp(
            XPathPseudoComp source, PseudoComp target, 
            BpelEntity destination, boolean inLeftMapperTree) 
            throws ExtRegistrationException {
        //
        if (!(source instanceof AbstractPseudoComp)) {
            return ((AbstractPseudoComp)source).populatePseudoComp(
                    target, destination, inLeftMapperTree);
        } else {
            MapperPseudoComp newSource = new MapperPseudoComp(source);
            if (newSource != null) {
                return newSource.populatePseudoComp(
                    target, destination, inLeftMapperTree);
            }
        }
        //
        return false;
    }
    
    public static String getDisplayName(XPathPseudoComp pseudo)  {
        if (pseudo.isAttribute()) {
            return "(" + pseudo.getName() + ")" + ANY_ATTRIBUTE;
        } else {
            return "(" + pseudo.getName() + ")" + ANY_ELEMENT;
        }
    }
    
}
