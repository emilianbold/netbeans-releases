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

import java.util.Iterator;
import org.netbeans.modules.bpel.mapper.model.PathConverter;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Variable;
import org.netbeans.modules.bpel.model.api.events.VetoException;
import org.netbeans.modules.bpel.model.ext.editor.api.Cast;
import org.netbeans.modules.soa.ui.tree.TreeItem;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.xpath.ext.XPathExpression;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.XPathSchemaContext;
import org.openide.ErrorManager;

/**
 * The special kind of Type Cast based on the schema context.
 * It is usualy used when the user create a new Type Cast. 
 * @author nk160297
 */
public class SyntheticTypeCast extends AbstractTypeCast {

    private TreeItem mCastedTreeItem;
    private XPathSchemaContext mSContext;
    
    public SyntheticTypeCast(TreeItem castedTreeItem, GlobalType castTo) {
        super(castTo);
        assert castedTreeItem != null;
        mCastedTreeItem = castedTreeItem;
    }
    
    public TreeItem getCastedTreeItem() {
        return mCastedTreeItem;
    }
    
    public XPathSchemaContext getSchemaContext() {
        if (mSContext == null) {
            mSContext = PathConverter.constructContext(mCastedTreeItem, false);
        }
        return mSContext;
    }

    public Variable getBaseVariable() {
        Iterator itr = mCastedTreeItem.iterator();
        while (itr.hasNext()) {
            Object obj = itr.next();
            if (obj instanceof Variable) {
                return (Variable)obj;
            }
        }
        //
        return null;
    }
    
    public boolean populateCast(Cast target, 
            BpelEntity destination, boolean inLeftMapperTree) {
        TreeItem treeItem = getCastedTreeItem();
        XPathExpression pathObj = PathConverter.constructXPath(
                destination, treeItem, false);
        if (pathObj == null) {
            return false;
        }
        //
        String pathText = pathObj.getExpressionString();
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
        Object result = mCastedTreeItem.getDataObject();
        return result;
    }

    /**
     * This XPath expression is build relative to the variable returned by 
     * the getBaseVariable() method. It's not absolutely honest way. 
     * But it can work. 
     * 
     * @return
     */
    public XPathExpression getPathExpression() {
        return PathConverter.constructXPath(getBaseVariable(), mCastedTreeItem, false);
    }
    
} 
