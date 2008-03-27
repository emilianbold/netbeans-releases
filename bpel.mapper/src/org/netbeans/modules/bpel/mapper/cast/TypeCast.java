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

import org.netbeans.modules.bpel.model.api.references.SchemaReference;
import org.netbeans.modules.bpel.model.api.support.BpelXPathModelFactory;
import org.netbeans.modules.bpel.model.ext.editor.api.Cast;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.xpath.ext.XPathException;
import org.netbeans.modules.xml.xpath.ext.XPathExpression;
import org.netbeans.modules.xml.xpath.ext.XPathExpressionPath;
import org.netbeans.modules.xml.xpath.ext.XPathModel;
import org.netbeans.modules.xml.xpath.ext.XPathSchemaContext;
import org.openide.ErrorManager;

/**
 * The Type Cast objects based on the XPath expression.
 * It is usualy used when the mapper initiated from the sources. 
 * 
 * @author nk160297
 */
public class TypeCast extends AbstractTypeCast {

    private XPathExpressionPath mXPathExpressionPath;
    
    public static TypeCast convert(Cast cast) {
        XPathExpressionPath exprPath = null;
        GlobalType castTo = null;
        //
        String pathText = cast.getPath();
        XPathModel xPathModel = BpelXPathModelFactory.create(cast);
        XPathExpression xPathExpr = null;
        try {
            xPathExpr = xPathModel.parseExpression(pathText);
        } catch (XPathException ex) {
            ErrorManager.getDefault().log(ErrorManager.WARNING, 
                    "Unresolved XPath: " + pathText); //NOI18N
        }
        //
        assert xPathExpr instanceof XPathExpressionPath;
        exprPath = (XPathExpressionPath)xPathExpr;
        //
        SchemaReference<GlobalType> gTypeRef = cast.getType();
        if (gTypeRef == null) {
            ErrorManager.getDefault().log(ErrorManager.WARNING, 
                    "Cast To has to be specified");
        } else {
            castTo = gTypeRef.get();
            if (castTo == null) {
                ErrorManager.getDefault().log(ErrorManager.WARNING, 
                        "Unresolved global type: " + gTypeRef.getQName());
            }
        }
        //
        return new TypeCast(exprPath, castTo);
    }
    
    public TypeCast(XPathExpressionPath path, GlobalType castTo) {
        super(castTo);
        assert path != null;
        mXPathExpressionPath = path;
    }
    
    public XPathSchemaContext getSchemaContext() {
        return mXPathExpressionPath.getSchemaContext();
    }

    public XPathExpressionPath getXPathExpressionPath() {
        return mXPathExpressionPath;
    }
} 