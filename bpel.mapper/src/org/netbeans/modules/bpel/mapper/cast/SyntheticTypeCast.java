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

import org.netbeans.modules.bpel.mapper.predicates.editor.PathConverter;
import org.netbeans.modules.bpel.mapper.tree.spi.RestartableIterator;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Variable;
import org.netbeans.modules.bpel.model.api.events.VetoException;
import org.netbeans.modules.bpel.model.api.references.SchemaReference;
import org.netbeans.modules.bpel.model.ext.editor.api.Cast;
import org.netbeans.modules.bpel.model.ext.editor.api.Source;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.xpath.ext.XPathExpression;
import org.netbeans.modules.xml.xpath.ext.XPathSchemaContext;
import org.openide.ErrorManager;

/**
 * The special kind of Type Cast based on the schema context.
 * It is usualy used when the user create a new Type Cast. 
 * @author nk160297
 */
public class SyntheticTypeCast extends AbstractTypeCast {

    private RestartableIterator<Object> mItr;
    private XPathSchemaContext mSContext;
    
    public static boolean populateCast(SyntheticTypeCast from, Cast to, 
            BpelEntity destination, boolean inLeftMapperTree) {
        RestartableIterator<Object> itr = from.getLocationIterator();
        XPathExpression pathObj = PathConverter.constructXPath(destination, itr);
        if (pathObj == null) {
            return false;
        }
        //
        String pathText = pathObj.getExpressionString();
        try {
            to.setPath(pathText);
        } catch (VetoException ex) {
            ErrorManager.getDefault().notify(ex);
            return false;
        }
        //
        GlobalType castTo = from.getCastTo();
        SchemaReference<GlobalType> typeRef =
                to.createSchemaReference(castTo, GlobalType.class);
        to.setType(typeRef);
        //
        if (inLeftMapperTree) {
            to.setSource(Source.FROM);
        } else {
            to.setSource(Source.TO);
        }
        //
        return true;
    }
    
    public SyntheticTypeCast(RestartableIterator<Object> itr, GlobalType castTo) {
        super(castTo);
        assert itr != null;
        mItr = itr;
    }
    
    public SyntheticTypeCast(XPathSchemaContext sContext, GlobalType castTo) {
        super(castTo);
        assert sContext != null;
        mSContext = sContext;
    }
    
    public RestartableIterator<Object> getLocationIterator() {
        return mItr;
    }
    
    public XPathSchemaContext getSchemaContext() {
        if (mSContext == null) {
            mSContext = PathConverter.constructContext(mItr);
        }
        return mSContext;
    }

    public Variable getBaseVariable() {
        mItr.restart();
        while (mItr.hasNext()) {
            Object obj = mItr.next();
            if (obj instanceof Variable) {
                return (Variable)obj;
            }
        }
        //
        return null;
    }
    
} 