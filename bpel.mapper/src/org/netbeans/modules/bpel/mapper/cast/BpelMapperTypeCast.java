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

import javax.xml.namespace.NamespaceContext;
import org.netbeans.modules.bpel.mapper.model.BpelMapperUtils;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.api.events.VetoException;
import org.netbeans.modules.bpel.model.api.references.SchemaReference;
import org.netbeans.modules.bpel.model.api.support.BpelXPathModelFactory;
import org.netbeans.modules.bpel.model.ext.editor.api.Cast;
import org.netbeans.modules.bpel.model.ext.editor.api.LocationStepModifier;
import org.netbeans.modules.bpel.model.ext.editor.api.Source;
import org.netbeans.modules.soa.ui.SoaUtil;
import org.netbeans.modules.soa.xpath.mapper.lsm.MapperTypeCast;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.xpath.ext.XPathException;
import org.netbeans.modules.xml.xpath.ext.XPathExpression;
import org.netbeans.modules.xml.xpath.ext.XPathModel;
import org.netbeans.modules.xml.xpath.ext.XPathSchemaContextHolder;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.XPathSchemaContext;
import org.netbeans.modules.xml.xpath.ext.spi.XPathCast;
import org.netbeans.modules.xml.xpath.ext.spi.XPathCastResolver;
import org.openide.ErrorManager;

/**
 * Represents XPath Type Cast in BPEL Mapper
 *
 * @author Nikita Krjukov
 */
public class BpelMapperTypeCast extends MapperTypeCast implements BpelMapperLsm {

    public static BpelMapperTypeCast convert(Cast cast) {
        GlobalType castTo = null;
        //
        SchemaReference<? extends GlobalType> castToRef = cast.getType();
        XPathExpression xPathExpr = getExpression(cast, null);
        if (xPathExpr == null) {
            return null;
        }
        //
        if (castToRef == null) {
            ErrorManager.getDefault().log(ErrorManager.WARNING, 
                    "Cast To has to be specified"); // NOI18N
            return null;
        } else {
            castTo = castToRef.get();
            if (castTo == null) {
                ErrorManager.getDefault().log(ErrorManager.WARNING, 
                        "Unresolved global type: " + castToRef.getQName()); // NOI18N
                return null;
            }
        }
        //
        XPathSchemaContext sContext = convert(xPathExpr);
        if (sContext == null) {
            return null;
        }
        //
        return new BpelMapperTypeCast(sContext, castTo);
    }

    public static BpelMapperTypeCast convert(Cast cast, XPathCastResolver castResolver) {
        GlobalType castTo = null;
        //
        SchemaReference<? extends GlobalType> castToRef = cast.getType();
        //
        if (castToRef == null) {
            ErrorManager.getDefault().log(ErrorManager.WARNING, 
                    "Cast To has to be specified"); // NOI18N
            return null;
        } else {
            castTo = castToRef.get();
            if (castTo == null) {
                ErrorManager.getDefault().log(ErrorManager.WARNING, 
                        "Unresolved global type: " + castToRef.getQName()); // NOI18N
                return null;
            }
        }
        //
        XPathExpression xPathExpr = getExpression(cast, castResolver);
        XPathSchemaContext sContext = convert(xPathExpr);
        if (sContext == null) {
            return null;
        }
        //
        return new BpelMapperTypeCast(sContext, castTo);
    }

    protected static XPathSchemaContext convert(XPathExpression xPathExpr) {
        if (xPathExpr instanceof XPathSchemaContextHolder) {
            XPathSchemaContext sContext = XPathSchemaContextHolder.class.
                    cast(xPathExpr).getSchemaContext();
            //
            if (sContext == null) {
                ErrorManager.getDefault().log(ErrorManager.WARNING,
                        "Cast path \"" + xPathExpr.getExpressionString() +
                        "\". " + "It has to be a valid path expression!"); // NOI18N
            }
            //
            return sContext;
        }
        //
        return null;
    }

    protected static XPathExpression getExpression(
            Cast cast, XPathCastResolver castResolver) {
        String pathText = cast.getPath();
        XPathModel xPathModel = BpelXPathModelFactory.create(cast, castResolver);
        XPathExpression xPathExpr = null;
        try {
            xPathExpr = xPathModel.parseExpression(pathText);
        } catch (XPathException ex) {
            ErrorManager.getDefault().log(ErrorManager.WARNING,
                    "Unresolved XPath: " + pathText); // NOI18N
        }
        return xPathExpr;
    }

    //==========================================================================

    public BpelMapperTypeCast(XPathCast xPathCast) {
        super(xPathCast);
    }

    public BpelMapperTypeCast(XPathSchemaContext exprContext, GlobalType castTo) {
        super(exprContext, castTo);
    }

    public boolean populateCast(Cast target,
            BpelEntity destination, boolean inLeftMapperTree) {
        NamespaceContext nsContext = destination.getNamespaceContext();
        String pathText = getSchemaContext().getExpressionString(nsContext, null);
        try {
            target.setPath(pathText);
        } catch (VetoException ex) {
            ErrorManager.getDefault().notify(ex);
            return false;
        }
        //
        return populateCastImpl(target, destination, inLeftMapperTree);
    }

    protected boolean populateCastImpl(Cast target, 
            BpelEntity destination, boolean inLeftMapperTree) {
        //
        GlobalType type = getType();
        SchemaReference<GlobalType> typeRef = target.createSchemaReference(
                (GlobalType)type, GlobalType.class);
        target.setType(typeRef);
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
        if (lsm instanceof Cast) {
            return equalsIgnoreLocation((Cast)lsm);
        }
        return false;
    }

    protected boolean equalsIgnoreLocation(Cast cast) {
        if (cast == null) {
            return false;
        }
        //
        boolean theyEquals;
        //
        SchemaReference<? extends GlobalType> otherTypeRef = cast.getType();
        if (otherTypeRef != null) {
            GlobalType otherType = otherTypeRef.get();
            GlobalType myType = this.getType();
            theyEquals = SoaUtil.equal(myType, otherType);
            if (!theyEquals) {
                return false;
            }
        }
        //
        String myPath = this.getSchemaContext().
                getExpressionString(cast.getNamespaceContext(), null);
        String otherPath = cast.getPath();
        theyEquals = SoaUtil.equal(myPath, otherPath);
        if (!theyEquals) {
            return false;
        }
        //
        return true;
    }

    public VariableDeclaration getBaseBpelVariable() {
        return BpelMapperUtils.getBaseVariable(getSchemaContext());
    }


    @Override
    public Object getCastedObject() {
        return BpelMapperUtils.getSchemaContextSubject(mCastedSContext);
    }

} 