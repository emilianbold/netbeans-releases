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

package org.netbeans.modules.bpel.mapper.cast;

import org.netbeans.modules.bpel.model.api.AbstractVariableDeclaration;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.events.VetoException;
import org.netbeans.modules.bpel.model.api.references.SchemaReference;
import org.netbeans.modules.bpel.model.api.support.BpelXPathModelFactory;
import org.netbeans.modules.bpel.model.api.support.XPathBpelVariable;
import org.netbeans.modules.bpel.model.ext.editor.api.PseudoComp;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.xpath.ext.XPathException;
import org.netbeans.modules.xml.xpath.ext.XPathExpression;
import org.netbeans.modules.xml.xpath.ext.XPathExpressionPath;
import org.netbeans.modules.xml.xpath.ext.XPathModel;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.XPathSchemaContext;
import org.netbeans.modules.xml.xpath.ext.XPathSchemaContextHolder;
import org.netbeans.modules.xml.xpath.ext.XPathVariableReference;
import org.netbeans.modules.xml.xpath.ext.spi.XPathPseudoComp;
import org.netbeans.modules.xml.xpath.ext.spi.XPathVariable;
import org.openide.ErrorManager;

/**
 * The PseudoComp objects based on the XPath expression.
 * It is usualy used when the mapper initiated from the sources. 
 * 
 * @author nk160297
 */
public class MapperPseudoComp extends AbstractPseudoComp {

    private XPathExpression mParentXPathExpression;
    
    public static XPathExpression getExpression(PseudoComp pseudoComp) {
        String pathText = pseudoComp.getParentPath();
        if (pathText == null || pathText.length() == 0) {
            return null;
        }
        //
        XPathModel xPathModel = BpelXPathModelFactory.create(pseudoComp);
        XPathExpression xPathExpr = null;
        try {
            xPathExpr = xPathModel.parseExpression(pathText);
        } catch (XPathException ex) {
            ErrorManager.getDefault().log(ErrorManager.WARNING, 
                    "Unresolved XPath: " + pathText); //NOI18N
        }
        return xPathExpr;
    }
    
    public static MapperPseudoComp convert(PseudoComp pseudoComp) {
        GlobalType type = null;
        //
        SchemaReference<? extends GlobalType> typeRef = pseudoComp.getType();
        XPathExpression xPathExpr = getExpression(pseudoComp);
        if (xPathExpr == null) {
            return null;
        }
        //
        if (typeRef == null) {
            ErrorManager.getDefault().log(ErrorManager.WARNING, 
                    "The type attribute has to be specified");
            return null;
        } else {
            type = typeRef.get();
            if (type == null) {
                ErrorManager.getDefault().log(ErrorManager.WARNING, 
                        "Unresolved global type: " + typeRef.getQName());
                return null;
            }
        }
        DetachedPseudoComp dpc = new DetachedPseudoComp(
                type, pseudoComp.getName(), pseudoComp.getNamespace(), 
                pseudoComp.isAttribute());
        //
        return new MapperPseudoComp(xPathExpr, dpc);
    }
    
    public MapperPseudoComp(XPathPseudoComp xPathPseudoComp) {
        this(xPathPseudoComp.getParentPathExpression(), xPathPseudoComp);
    }
    
    public MapperPseudoComp(XPathExpression parentPath, XPathPseudoComp xpc) {
        super(xpc.getType(), xpc.getName(), xpc.getNamespace(), xpc.isAttribute());
        assert parentPath != null;
        assert parentPath instanceof XPathSchemaContextHolder;
        mParentXPathExpression = parentPath;
    }
    
    public XPathSchemaContext getSchemaContext() {
        return ((XPathSchemaContextHolder)mParentXPathExpression).getSchemaContext();
    }

    public XPathExpression getParentPathExpression() {
        return mParentXPathExpression;
    }
    
    public AbstractVariableDeclaration getBaseVariable() {
        XPathVariableReference varRef = null;
        if (mParentXPathExpression instanceof XPathVariableReference) {
            varRef = (XPathVariableReference)mParentXPathExpression;
        } else if (mParentXPathExpression instanceof XPathExpressionPath) {
            XPathExpression rootExpr = 
                    ((XPathExpressionPath)mParentXPathExpression).getRootExpression();
            if (rootExpr != null && rootExpr instanceof XPathVariableReference) {
                varRef = (XPathVariableReference)rootExpr;
            }
        }
        //
        if (varRef != null) {
            XPathVariable xPathVar = varRef.getVariable();
            if (xPathVar != null && xPathVar instanceof XPathBpelVariable) {
                AbstractVariableDeclaration varDecl = 
                        ((XPathBpelVariable)xPathVar).getVarDecl();
                return varDecl;
            }
        }
        //
        return null;
    }
    
    public boolean populatePseudoComp(PseudoComp target, 
            BpelEntity destination, boolean inLeftMapperTree) 
            throws ExtRegistrationException {
        String pathText = mParentXPathExpression.getExpressionString();
        try {
            target.setParentPath(pathText);
        } catch (VetoException ex) {
            ErrorManager.getDefault().notify(ex);
            return false;
        }
        //
        return populatePseudoCompImpl(target, destination, inLeftMapperTree);
    }
    
} 