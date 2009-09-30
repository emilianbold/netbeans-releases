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
import org.netbeans.modules.bpel.model.ext.editor.api.Cast;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.xpath.ext.XPathException;
import org.netbeans.modules.xml.xpath.ext.XPathExpression;
import org.netbeans.modules.xml.xpath.ext.XPathExpressionPath;
import org.netbeans.modules.xml.xpath.ext.XPathModel;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.XPathSchemaContext;
import org.netbeans.modules.xml.xpath.ext.XPathSchemaContextHolder;
import org.netbeans.modules.xml.xpath.ext.XPathVariableReference;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.CastSchemaContext;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.VariableSchemaContext;
import org.netbeans.modules.xml.xpath.ext.spi.XPathCast;
import org.netbeans.modules.xml.xpath.ext.spi.XPathVariable;
import org.openide.ErrorManager;

/**
 * The Type Cast objects based on the XPath expression.
 * It is usualy used when the mapper initiated from the sources. 
 * 
 * @author nk160297
 */
public class TypeCast extends AbstractTypeCast {

    private XPathExpression mXPathExpression;
    
    public static XPathExpression getExpression(Cast cast) {
        String pathText = cast.getPath();
        XPathModel xPathModel = BpelXPathModelFactory.create(cast);
        XPathExpression xPathExpr = null;
        try {
            xPathExpr = xPathModel.parseExpression(pathText);
        } catch (XPathException ex) {
            ErrorManager.getDefault().log(ErrorManager.WARNING, 
                    "Unresolved XPath: " + pathText); //NOI18N
        }
        return xPathExpr;
    }
    
    public static TypeCast convert(Cast cast) {
        GlobalType castTo = null;
        //
        SchemaReference<? extends GlobalType> castToRef = cast.getType();
        XPathExpression xPathExpr = getExpression(cast);
        //
        if (castToRef == null) {
            ErrorManager.getDefault().log(ErrorManager.WARNING, 
                    "Cast To has to be specified");
            return null;
        } else {
            castTo = castToRef.get();
            if (castTo == null) {
                ErrorManager.getDefault().log(ErrorManager.WARNING, 
                        "Unresolved global type: " + castToRef.getQName());
                return null;
            }
        }
        //
        return new TypeCast(xPathExpr, castTo);
    }
    
    public TypeCast(XPathCast xPathCast) {
        this(xPathCast.getPathExpression(), xPathCast.getType());
    }
    
    public TypeCast(XPathExpression path, GlobalType type) {
        super(type);
        assert path != null;
        assert path instanceof XPathSchemaContextHolder;
        mXPathExpression = path;
    }
    
    public XPathSchemaContext getSchemaContext() {
        return ((XPathSchemaContextHolder)mXPathExpression).getSchemaContext();
    }

    public XPathExpression getPathExpression() {
        return mXPathExpression;
    }
    
    public AbstractVariableDeclaration getBaseVariable() {
        XPathVariableReference varRef = null;
        if (mXPathExpression instanceof XPathVariableReference) {
            varRef = (XPathVariableReference)mXPathExpression;
        } else if (mXPathExpression instanceof XPathExpressionPath) {
            XPathExpression rootExpr = 
                    ((XPathExpressionPath)mXPathExpression).getRootExpression();
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
    
    public boolean populateCast(Cast target, 
            BpelEntity destination, boolean inLeftMapperTree) {
        String pathText = mXPathExpression.getExpressionString();
        try {
            target.setPath(pathText);
        } catch (VetoException ex) {
            ErrorManager.getDefault().notify(ex);
            return false;
        }
        //
        return populateCastImpl(target, destination, inLeftMapperTree);
    }
    
    public Object getCastedObject() {
        assert mXPathExpression instanceof XPathSchemaContextHolder;
        XPathSchemaContext sContext = 
                ((XPathSchemaContextHolder)mXPathExpression).getSchemaContext();
        if (sContext != null) {
            return getSchemaContextSubject(sContext);
        }
        //
        return null;
    }
    
    private Object getSchemaContextSubject(XPathSchemaContext sContext) {
        if (sContext == null) {
            return null;
        }
        //
        Object result = null;
        if (sContext instanceof VariableSchemaContext) {
            XPathVariable var = ((VariableSchemaContext)sContext).getVariable();
            assert var instanceof XPathBpelVariable;
            XPathBpelVariable bpelVar = (XPathBpelVariable)var;
            Part part = bpelVar.getPart();
            if (part == null) {
                result = bpelVar.getVarDecl();
            } else {
                result = part;
            }
        } else if (sContext instanceof CastSchemaContext) {
            XPathSchemaContext baseContext = ((CastSchemaContext)sContext).getBaseContext();
            result = getSchemaContextSubject(baseContext);
        } else {
            SchemaComponent sComp = XPathSchemaContext.Utilities.
                    getSchemaComp(sContext);
            return sComp;
        }
        //
        return result;
    }

} 
