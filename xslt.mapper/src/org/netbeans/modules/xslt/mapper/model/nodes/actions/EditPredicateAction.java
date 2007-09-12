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

package org.netbeans.modules.xslt.mapper.model.nodes.actions;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import org.netbeans.modules.soa.ui.form.valid.DefaultDialogDescriptor;
import org.netbeans.modules.soa.ui.form.valid.SoaDialogDisplayer;
import org.netbeans.modules.xml.xpath.AbstractXPathModelHelper;
import org.netbeans.modules.xml.xpath.XPathException;
import org.netbeans.modules.xml.xpath.XPathExpression;
import org.netbeans.modules.xml.xpath.XPathModel;
import org.netbeans.modules.xml.xpath.XPathPredicateExpression;
import org.netbeans.modules.xslt.mapper.model.targettree.PredicatedSchemaNode;
import org.netbeans.modules.xslt.mapper.view.PredicateManager;
import org.netbeans.modules.xslt.mapper.view.XsltMapper;
import org.netbeans.modules.xslt.mapper.xpatheditor.ExpressionEditor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Shows the Expression editor dialog in order to edit a predicate.
 *
 * @author nk160297
 */
public class EditPredicateAction extends XsltNodeAction {
    
    private static final long serialVersionUID = 1L;
    
    public EditPredicateAction(XsltMapper xsltMapper, PredicatedSchemaNode node) {
        super(xsltMapper, node);
        postInit();
        // putValue(DeleteAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0));
    }
    
    public String getDisplayName() {
        return NbBundle.getMessage(ActionConst.class, "EDIT_PREDICATE"); // NOI18N
    }
    
    public void actionPerformed(ActionEvent e) {
        //        XslModel model = myXsltMapper.getContext().getXSLModel();
        //        Object dataObject = myTreeNode.getDataObject();
        
        ExpressionEditor exprEditor = new ExpressionEditor(myXsltMapper);
        //
        XPathPredicateExpression[] predicateArr = ((PredicatedSchemaNode)myTreeNode).
                getPredicatedAxiComp().getPredicates();
        if (predicateArr != null && predicateArr.length > 0) {
            String firstPredicateText = predicateArr[0].getExpressionString();
            exprEditor.setSelectedValue(firstPredicateText);
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
        //
        XPathModel xpImpl = AbstractXPathModelHelper.getInstance().newXPathModel();
        try {
            XPathExpression expression = xpImpl.parseExpression(literal);
            XPathPredicateExpression predicate =
                    AbstractXPathModelHelper.getInstance().
                    newXPathPredicateExpression(expression);
            //
            if (predicateArr != null && predicateArr.length > 0) {
                // If there was previous prediates, then modify first element only
                XPathPredicateExpression[] newPredicateArr = 
                        new XPathPredicateExpression[predicateArr.length];
                newPredicateArr[0] = predicate;
                // copy remaining elements as is
                for (int index = 1; index < predicateArr.length; index++) {
                    newPredicateArr[index] = predicateArr[index];
                }
                predicateArr = newPredicateArr;
            } else {
                // If there was not - create a new one
                predicateArr = new XPathPredicateExpression[]{predicate};
            }
            //
            PredicateManager pm = myXsltMapper.getPredicateManager();
            pm.modifyPredicate((PredicatedSchemaNode)myTreeNode, predicateArr);
        } catch (XPathException ex) {
            // Error.Incorrect expression
        };
        
    }
    
}
