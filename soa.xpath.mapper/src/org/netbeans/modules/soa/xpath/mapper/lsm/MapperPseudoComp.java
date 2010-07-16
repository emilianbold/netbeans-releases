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
package org.netbeans.modules.soa.xpath.mapper.lsm;

import org.netbeans.modules.soa.xpath.mapper.utils.XPathMapperUtils;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.xpath.ext.XPathExpression;
import org.netbeans.modules.xml.xpath.ext.XPathPredicateExpression;
import org.netbeans.modules.xml.xpath.ext.XPathSchemaContextHolder;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.PredicatedSchemaContext;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.XPathSchemaContext;
import org.netbeans.modules.xml.xpath.ext.spi.XPathPseudoComp;
import org.netbeans.modules.xml.xpath.ext.spi.XPathVariable;
import org.openide.util.NbBundle;

/**
 * Represents XPath Pseudo Component in BPEL Mapper
 *
 * @author Nikita Krjukov
 */
public class MapperPseudoComp implements XPathPseudoComp,
    MapperLsm {

    public static final String ANY_ELEMENT = 
            NbBundle.getMessage(MapperPseudoComp.class, "ANY_ELEMENT"); // NOI18N
    
    public static final String ANY_ATTRIBUTE = 
            NbBundle.getMessage(MapperPseudoComp.class, "ANY_ATTRIBUTE"); // NOI18N

    public static String getDisplayName(XPathPseudoComp pseudo)  {
        if (pseudo.isAttribute()) {
            return "(" + pseudo.getName() + ")" + ANY_ATTRIBUTE;
        } else {
            return "(" + pseudo.getName() + ")" + ANY_ELEMENT;
        }
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

    //------------------------------------------------------------------

    private DetachedPseudoComp mDPC;
    protected XPathSchemaContext mParentSContext;

    /**
     * Constructor is protected because the object has to be created either by
     * a derived class or by PseudoCompManager.
     *
     * @param xPathPseudoComp
     */
    protected MapperPseudoComp(XPathPseudoComp xPathPseudoComp) {
        this(xPathPseudoComp.getSchemaContext(), xPathPseudoComp);
    }

    protected MapperPseudoComp(XPathSchemaContext parentSContext, XPathPseudoComp xpc) {
        this(xpc.getType(), xpc.getName(), xpc.getNamespace(), xpc.isAttribute());
        assert parentSContext != null;
        mParentSContext = parentSContext;
    }

    protected MapperPseudoComp(XPathExpression parentPath, XPathPseudoComp xpc) {
        this(xpc.getType(), xpc.getName(), xpc.getNamespace(), xpc.isAttribute());
        assert parentPath != null;
        assert parentPath instanceof XPathSchemaContextHolder;
        mParentSContext = XPathSchemaContextHolder.class.cast(parentPath).getSchemaContext();
    }

    protected MapperPseudoComp(GlobalType type, String name,
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

    public void setSchemaContext(XPathSchemaContext newContext) {
        // It has to be specified in a constructor
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
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
            XPathSchemaContext mySContext = this.getSchemaContext();
            XPathSchemaContext otherSContext = otherPC.getSchemaContext();
            if (mySContext != otherSContext) {
                if (mySContext == null || otherSContext == null) {
                    return false;
                } else if (!(otherSContext.equalsChain(mySContext))) {
                    return false;
                }
            }
            //
            return true;
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        XPathSchemaContext mySContext = this.getSchemaContext();
        if (mySContext != null) {
            return getSchemaContext().toString().hashCode();
        } else {
            return super.hashCode();
        }
    }

    @Override
    public String toString() {
        if (isAttribute()) {
            return "/@" + getName(); 
        } else {
            return "/" + getName(); 
        }
    }

    public XPathVariable getBaseVariable() {
        return XPathMapperUtils.getBaseVariable(mParentSContext);
    }

    public XPathSchemaContext getSchemaContext() {
        return mParentSContext;
    }

    public void modifyPredicate(PredicatedSchemaContext template, 
            XPathPredicateExpression[] newExprArr) {
        //
        PredicateManager.modifyPredicateInSContext(
                mParentSContext, template, newExprArr);
    }

}
