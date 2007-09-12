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
