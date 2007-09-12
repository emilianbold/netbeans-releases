/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
import org.netbeans.modules.soa.ui.form.valid.DefaultDialogDescriptor;
import org.netbeans.modules.soa.ui.form.valid.SoaDialogDisplayer;
import org.openide.DialogDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

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
     * @see org.netbeans.modules.soa.mapper.common.basicmapper.literal
     * *      .ILiteralEditor#show()
     */
    public void show() {
        ExpressionEditor exprEditor = new ExpressionEditor(mBasicMapper);
        String expr = mFieldNode.getLiteralName();
        if (expr != null && expr.length() > 0) {
            exprEditor.setSelectedValue(expr);
        }
        //
        String title = NbBundle.getMessage(
                ExpressionEditor.class, "TITLE_ExpressionBuilder");
        DefaultDialogDescriptor descriptor = 
                new DefaultDialogDescriptor(exprEditor, title);
        descriptor.setHelpCtx(new HelpCtx("xslt_editor_xpath")); // NOI18N

        Dialog dialog = SoaDialogDisplayer.getDefault().createDialog(descriptor);
        dialog.setVisible(true);
        //
        if (!descriptor.isOkHasPressed()) {
            return;
        }
        String literal = exprEditor.getSelectedValue();
        if (null == literal) {
            literal = "";
        }
        fireLiteralUpdated(literal);
    }

    /**
     * @see org.netbeans.modules.soa.mapper.common.basicmapper.literal
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
             * @see org.netbeans.modules.soa.mapper.common.basicmapper
             * *  .IBasicMapperLiteralUpdateEventInfo#getLiteralUpdater()
             */
            public ILiteralUpdater getLiteralUpdater()
            {
                return mUpdateListener;
            }

            /**
             * @see org.netbeans.modules.soa.mapper.common.basicmapper
             * *  .IBasicMapperLiteralUpdateEventInfo#getMethoidNode()
             */
            public IMethoidNode getMethoidNode()
            {
                return mMethoidNode;
            }

            /**
             * @see org.netbeans.modules.soa.mapper.common.basicmapper
             * *  .IBasicMapperLiteralUpdateEventInfo#getFieldNode()
             */
            public IFieldNode getFieldNode()
            {
                return mFieldNode;
            }

            /**
             * @see org.netbeans.modules.soa.mapper.common.basicmapper
             * *  .IBasicMapperLiteralUpdateEventInfo#getNewValue()
             */
            public String getNewValue()
            {
                return newValue;
            }

            /**
             * @see org.netbeans.modules.soa.mapper.common.basicmapper
             * *  .IBasicMapperLiteralUpdateEventInfo#isLiteralMethoid()
             */
            public boolean isLiteralMethoid()
            {
                return mMethoid.isLiteral();
            }
        } );
    }
}
