/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xslt.mapper.xpatheditor;


import java.awt.Dialog;
import java.awt.Window;
import org.netbeans.modules.soa.mapper.common.basicmapper.IBasicMapper;
import org.netbeans.modules.soa.mapper.common.basicmapper.IBasicMapperLiteralUpdateEventInfo;
import org.netbeans.modules.soa.mapper.common.basicmapper.literal.ILiteralEditor;
import org.netbeans.modules.soa.mapper.common.basicmapper.literal.ILiteralUpdater;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IFieldNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IMethoid;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IMethoidNode;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;

/**
 * Implements special case of a XPath expression string literal editor.
 *
 * @author     nk160297
 */
public class XPathLiteralEditor implements ILiteralEditor {

    private Window mOwner;

    private IBasicMapper mBasicMapper;

    private IFieldNode mFieldNode;

    private ILiteralUpdater mUpdateListener;

    private IMethoidNode mMethoidNode;

    private IMethoid mMethoid;

    /**
     * Creates a new instance of XPathLiteralEditor
     */
    public XPathLiteralEditor(Window owner, IBasicMapper basicMapper,
            IFieldNode fieldNode, ILiteralUpdater updateListener) {
        super();
        mOwner = owner;
        mBasicMapper = basicMapper;
        mFieldNode = fieldNode;
        mUpdateListener = updateListener;
        mMethoidNode = (IMethoidNode) mFieldNode.getGroupNode();
        mMethoid = (IMethoid) mMethoidNode.getMethoidObject();
    }

    /**
     * @see org.netbeans.modules.mapper.common.basicmapper.literal
     * *      .ILiteralEditor#show()
     */
    public void show() {
        ExpressionEditor exprEditor = new ExpressionEditor(mBasicMapper);
        String expr = mFieldNode.getLiteralName();
        if (expr != null && expr.length() > 0) {
            exprEditor.setSelectedValue(expr);
        }
        //
        DialogDescriptor descriptor = new DialogDescriptor(exprEditor, "XPath Expression Editor");
        Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.setVisible(true);
        //
        if (!DialogDescriptor.OK_OPTION.equals(descriptor.getValue())) {
            return;
        }
        String literal = exprEditor.getSelectedValue();
        if (null == literal) {
            literal = "";
        }
        fireLiteralUpdated(literal);
    }

    /**
     * @see org.netbeans.modules.mapper.common.basicmapper.literal
     * *      .ILiteralEditor#getUpdateListener()
     */
    public ILiteralUpdater getUpdateListener() {
        return mUpdateListener;
    }

    /**
     * Fires an event for the update of the duration literal.
     * *  @param  newValue    New value for the duration literal.
     */
    private void fireLiteralUpdated( final String newValue ) {
        // set the expression
        mBasicMapper.updateFieldLiteral( new IBasicMapperLiteralUpdateEventInfo(){
            /**
             * @see org.netbeans.modules.mapper.common.basicmapper
             * *  .IBasicMapperLiteralUpdateEventInfo#getLiteralUpdater()
             */
            public ILiteralUpdater getLiteralUpdater()
            {
                return mUpdateListener;
            }

            /**
             * @see org.netbeans.modules.mapper.common.basicmapper
             * *  .IBasicMapperLiteralUpdateEventInfo#getMethoidNode()
             */
            public IMethoidNode getMethoidNode()
            {
                return mMethoidNode;
            }

            /**
             * @see org.netbeans.modules.mapper.common.basicmapper
             * *  .IBasicMapperLiteralUpdateEventInfo#getFieldNode()
             */
            public IFieldNode getFieldNode()
            {
                return mFieldNode;
            }

            /**
             * @see org.netbeans.modules.mapper.common.basicmapper
             * *  .IBasicMapperLiteralUpdateEventInfo#getNewValue()
             */
            public String getNewValue()
            {
                return newValue;
            }

            /**
             * @see org.netbeans.modules.mapper.common.basicmapper
             * *  .IBasicMapperLiteralUpdateEventInfo#isLiteralMethoid()
             */
            public boolean isLiteralMethoid()
            {
                return mMethoid.isLiteral();
            }
        } );
    }
}
