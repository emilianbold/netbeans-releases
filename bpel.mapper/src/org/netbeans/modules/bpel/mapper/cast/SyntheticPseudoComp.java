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

import org.netbeans.modules.bpel.mapper.model.BpelMapperUtils;
import org.netbeans.modules.bpel.mapper.predicates.editor.PathConverter;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.api.events.VetoException;
import org.netbeans.modules.bpel.model.ext.editor.api.PseudoComp;
import org.netbeans.modules.xml.xpath.ext.XPathExpression;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.XPathSchemaContext;
import org.openide.ErrorManager;

/**
 * The special kind of Pseudo Schema component based on the schema context.
 * It is usualy used when the user create a new Pseudo component. 
 * @author nk160297
 */
public class SyntheticPseudoComp extends AbstractPseudoComp {

    /**
     * The iterator points to the xsd:any or xsd:anyAttribute (in the BPEL mapper tree), 
     * which was used for create the new Pseudo component. The first element of the 
     * iterator should be skipped while constructing the schema context or expression. 
     */
    private Iterable<Object> mItrb;
    private XPathSchemaContext mSContext;
    
    public SyntheticPseudoComp(Iterable<Object> itrb, DetachedPseudoComp dpc) {
        super(dpc);
        assert itrb != null;
        mItrb = itrb;
    }
    
    public Iterable<Object> getLocationIterable() {
        return mItrb;
    }
    
    public XPathSchemaContext getSchemaContext() {
        if (mSContext == null) {
            mSContext = PathConverter.constructContext(mItrb, true);
        }
        return mSContext;
    }

    public VariableDeclaration getBaseVariable() {
        return BpelMapperUtils.getBaseVariable(mItrb);
    }
    
    public boolean populatePseudoComp(PseudoComp target, 
            BpelEntity destination, boolean inLeftMapperTree) 
            throws ExtRegistrationException {
        //
        Iterable<Object> itrb = getLocationIterable();
        XPathExpression pathObj = PathConverter.constructXPath(
                destination, itrb, true);
        if (pathObj == null) {
            return false;
        }
        //
        String pathText = pathObj.getExpressionString();
        try {
            target.setParentPath(pathText);
        } catch (VetoException ex) {
            ErrorManager.getDefault().notify(ex);
            return false;
        }
        //
        return populatePseudoCompImpl(target, destination, inLeftMapperTree);
    }

    public Object getBaseAnyObject() {
        Object result = mItrb.iterator().next();
        return result;
    }

    /**
     * This XPath expression is build relative to the variable returned by 
     * the getBaseVariable() method. It's not absolutely honest way. 
     * But it can work. 
     * 
     * @return
     */
    public XPathExpression getParentPathExpression() {
        return PathConverter.constructXPath(
                (BpelEntity)getBaseVariable(), mItrb, true);
    }

} 