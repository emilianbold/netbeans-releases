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
package org.netbeans.modules.bpel.mapper.cast;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import org.netbeans.modules.bpel.mapper.model.BpelMapperUtils;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.api.events.VetoException;
import org.netbeans.modules.bpel.model.api.references.SchemaReference;
import org.netbeans.modules.bpel.model.api.support.BpelXPathModelFactory;
import org.netbeans.modules.bpel.model.ext.editor.api.LocationStepModifier;
import org.netbeans.modules.bpel.model.ext.editor.api.PseudoComp;
import org.netbeans.modules.bpel.model.ext.editor.api.Source;
import org.netbeans.modules.soa.ui.SoaUtil;
import org.netbeans.modules.soa.xpath.mapper.lsm.DetachedPseudoComp;
import org.netbeans.modules.soa.xpath.mapper.lsm.ExtRegistrationException;
import org.netbeans.modules.soa.xpath.mapper.lsm.MapperPseudoComp;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.xpath.ext.XPathException;
import org.netbeans.modules.xml.xpath.ext.XPathExpression;
import org.netbeans.modules.xml.xpath.ext.XPathModel;
import org.netbeans.modules.xml.xpath.ext.schema.ExNamespaceContext;
import org.netbeans.modules.xml.xpath.ext.schema.InvalidNamespaceException;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.XPathSchemaContext;
import org.netbeans.modules.xml.xpath.ext.spi.XPathCastResolver;
import org.netbeans.modules.xml.xpath.ext.spi.XPathPseudoComp;
import org.openide.ErrorManager;

/**
 * Represents XPath Pseudo Component in BPEL Mapper
 *
 * @author Nikita Krjukov
 */
public class BpelMapperPseudoComp extends MapperPseudoComp implements BpelMapperLsm {

    public static BpelMapperPseudoComp convert(PseudoComp pseudoComp) {
        GlobalType type = null;
        //
        XPathExpression xPathExpr = getExpression(pseudoComp, null);
        if (xPathExpr == null) {
            return null;
        }
        //
        SchemaReference<? extends GlobalType> typeRef = pseudoComp.getType();
        if (typeRef == null) {
            ErrorManager.getDefault().log(ErrorManager.WARNING, 
                    "The type attribute has to be specified"); // NOI18N
            return null;
        } else {
            type = typeRef.get();
            if (type == null) {
                ErrorManager.getDefault().log(ErrorManager.WARNING, 
                        "Unresolved global type: " + typeRef.getQName()); // NOI18N
                return null;
            }
        }
        QName qName = pseudoComp.getName();
        if (qName == null) {
            ErrorManager.getDefault().log(ErrorManager.WARNING, 
                    "The qName attribute is absent or corrupted: " + pseudoComp); // NOI18N
            return null;
        }
        DetachedPseudoComp dpc = new DetachedPseudoComp(
                type, qName.getLocalPart(),
                qName.getNamespaceURI(), pseudoComp.isAttribute());
        //
        return new BpelMapperPseudoComp(xPathExpr, dpc);
    }

    public static BpelMapperPseudoComp convert(PseudoComp pseudoComp, XPathCastResolver castResolver) {
        //
        if (pseudoComp == null) {
            return null;
        }
        //
        GlobalType type = null;
        //
        XPathExpression xPathExpr = getExpression(pseudoComp, castResolver);
        if (xPathExpr == null) {
            return null;
        }
        //
        SchemaReference<? extends GlobalType> typeRef = pseudoComp.getType();
        if (typeRef == null) {
            ErrorManager.getDefault().log(ErrorManager.WARNING, 
                    "The type attribute has to be specified"); // NOI18N
            return null;
        } else {
            type = typeRef.get();
            if (type == null) {
                ErrorManager.getDefault().log(ErrorManager.WARNING, 
                        "Unresolved global type: " + typeRef.getQName()); // NOI18N
                return null;
            }
        }
        QName qName = pseudoComp.getName();
        if (qName == null) {
            ErrorManager.getDefault().log(ErrorManager.WARNING, 
                    "The qName attribute is absent or corrupted: " + pseudoComp); // NOI18N
            return null;
        }
        DetachedPseudoComp dpc = new DetachedPseudoComp(
                type, qName.getLocalPart(),
                qName.getNamespaceURI(), pseudoComp.isAttribute());
        //
        return new BpelMapperPseudoComp(xPathExpr, dpc);
    }

    public static XPathExpression getExpression(PseudoComp pseudoComp, 
            XPathCastResolver castResolver) {
        //
        String pathText = pseudoComp.getParentPath();
        if (pathText == null || pathText.length() == 0) {
            return null;
        }
        //
        XPathModel xPathModel = BpelXPathModelFactory.create(pseudoComp, castResolver);
        XPathExpression xPathExpr = null;
        try {
            xPathExpr = xPathModel.parseExpression(pathText);
        } catch (XPathException ex) {
            ErrorManager.getDefault().log(ErrorManager.WARNING, 
                    "Unresolved XPath: " + pathText); // NOI18N
        }
        return xPathExpr;
    }

    public static boolean populatePseudoComp(
            XPathPseudoComp source, PseudoComp target,
            BpelEntity destination, boolean inLeftMapperTree)
            throws ExtRegistrationException {
        //
        if (!(source instanceof BpelMapperPseudoComp)) {
            return ((BpelMapperPseudoComp)source).populatePseudoComp(
                    target, destination, inLeftMapperTree);
        } else {
            BpelMapperPseudoComp newSource = new BpelMapperPseudoComp(source);
            if (newSource != null) {
                return newSource.populatePseudoComp(
                    target, destination, inLeftMapperTree);
            }
        }
        //
        return false;
    }

    //==========================================================================

    public BpelMapperPseudoComp(XPathPseudoComp xPathPseudoComp) {
        super(xPathPseudoComp);
    }

    public BpelMapperPseudoComp(XPathSchemaContext parentSContext, XPathPseudoComp xpc) {
        super(parentSContext, xpc);
    }

    private BpelMapperPseudoComp(XPathExpression parentPath, XPathPseudoComp xpc) {
        super(parentPath, xpc);
    }

    private BpelMapperPseudoComp(GlobalType type, String name,
            String namespace, boolean isAttribute) {
        super(type, name, namespace, isAttribute);
    }

    //==========================================================================

    public boolean populatePseudoComp(PseudoComp target,
            BpelEntity destination, boolean inLeftMapperTree)
            throws ExtRegistrationException {
        NamespaceContext nsContext = destination.getNamespaceContext();
        String pathText = getSchemaContext().getExpressionString(nsContext, null);
        // String pathText = mParentXPathExpression.getExpressionString();
        try {
            target.setParentPath(pathText);
        } catch (VetoException ex) {
            ErrorManager.getDefault().notify(ex);
            return false;
        }
        //
        return populatePseudoCompImpl(target, destination, inLeftMapperTree);
    }

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

    public boolean equalsIgnoreLocation(LocationStepModifier lsm) {
        if (lsm instanceof PseudoComp) {
            return equalsIgnoreLocation((PseudoComp)lsm);
        }
        return false;
    }

    protected boolean equalsIgnoreLocation(PseudoComp pseudo) {
        if (pseudo == null) {
            return false;
        }
        boolean theyEquals;
        //
        QName otherQName = pseudo.getName();
        if (otherQName != null) {
            String otherName = otherQName.getLocalPart();
            theyEquals = SoaUtil.equal(this.getName(), otherName);
            if (!theyEquals) {
                return false;
            }
            //
            String otherNs = otherQName.getNamespaceURI();
            theyEquals = SoaUtil.equal(this.getNamespace(), otherNs);
            if (!theyEquals) {
                return false;
            }
        }
        //
//        String myPath = this.getParentPathText();
//        String otherPath = pseudo.getParentPath();
//        //
//        theyEquals = SoaUtil.equal(myPath, otherPath);
//        if (!theyEquals) {
//            return false;
//        }
        //
        SchemaReference<? extends GlobalType> otherTypeRef = pseudo.getType();
        if (otherTypeRef != null) {
            GlobalType otherType = otherTypeRef.get();
            GlobalType myType = this.getType();
            theyEquals = SoaUtil.equal(myType, otherType);
            if (!theyEquals) {
                return false;
            }
        }
        //
        return true;
    }

    public VariableDeclaration getBaseBpelVariable() {
        return BpelMapperUtils.getBaseVariable(mParentSContext);
    }

}
