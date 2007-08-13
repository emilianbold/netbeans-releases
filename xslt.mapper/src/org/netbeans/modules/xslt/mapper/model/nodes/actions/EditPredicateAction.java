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
