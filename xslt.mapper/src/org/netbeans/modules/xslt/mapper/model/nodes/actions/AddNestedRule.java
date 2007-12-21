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

import java.awt.event.ActionEvent;
import java.lang.reflect.Method;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.modules.xslt.mapper.model.BranchConstructor;
import org.netbeans.modules.xslt.mapper.model.nodes.TreeNode;
import org.netbeans.modules.xslt.mapper.model.targettree.SchemaNode;
import org.netbeans.modules.xslt.mapper.model.targettree.StylesheetNode;
import org.netbeans.modules.xslt.mapper.view.XsltMapper;
import org.netbeans.modules.xslt.model.Instruction;
import org.netbeans.modules.xslt.model.SequenceConstructor;
import org.netbeans.modules.xslt.model.XslComponent;
import org.netbeans.modules.xslt.model.XslComponentFactory;
import org.netbeans.modules.xslt.model.XslModel;
import org.openide.ErrorManager;

/**
 *
 * @author nk160297
 */
public class AddNestedRule extends XsltNodeAction {
    
    private static final long serialVersionUID = 1L;
    
    protected SupportedRuleTypes myType;
    
    public AddNestedRule(XsltMapper xsltMapper, TreeNode node,
            Class<? extends Instruction> ruleClass) {
        this(xsltMapper, node, SupportedRuleTypes.getRuleType(ruleClass));
    }
    
    public AddNestedRule(XsltMapper xsltMapper, TreeNode node,
            SupportedRuleTypes type) {
        super(xsltMapper, node);
        myType = type;
        postInit();
    }
    
    public String getDisplayName() {
        return myType.getDisplayName();
    }
    
    public Icon getIcon() {
        Icon icon = new ImageIcon(myType.getImage());
        return icon;
    }
    
    public void actionPerformed(ActionEvent e) {
        XslModel model = myXsltMapper.getContext().getXSLModel();
        if (model == null) {
            return;
        }
        //
        try {
            XslComponent parentComp = null;
            if (myTreeNode instanceof SchemaNode) {
                BranchConstructor xbc = 
                        new BranchConstructor((SchemaNode)myTreeNode, getMapper());
                xbc.exitTranactionOnFinish(false);
                parentComp = xbc.construct();
            } else if (myTreeNode instanceof StylesheetNode) {
                Object dataObject = myTreeNode.getDataObject();
                if (dataObject != null && dataObject instanceof XslComponent) {
                    parentComp = (XslComponent)dataObject;
                }
            }
            //
            if (parentComp != null && parentComp instanceof SequenceConstructor) {
                XslComponentFactory factory = model.getFactory();
                //
                Instruction newXslInstruction = null;
                Method[] methodArr = XslComponentFactory.class.getMethods();
                for (Method method : methodArr) {
                    Class returnType = method.getReturnType();
                    if (returnType.equals(myType.getInterface())) {
                        try {
                            newXslInstruction = (Instruction)method.invoke(factory);
                            break;
                        } catch (Exception ex) {
                            ErrorManager.getDefault().notify(ex);
                        }
                    }
                }
                //
                assert newXslInstruction != null : "Can't create the rule object"; // NOI18N
                //
                // newXslAttribute.setName();
                //
                if (!model.isIntransaction()) {
                    model.startTransaction();
                }
                //
                ((SequenceConstructor)parentComp).appendSequenceChild(
                        newXslInstruction);
            }
        } finally {
            if (model.isIntransaction()) {
                model.endTransaction();
            }
        }
    }
    
}
