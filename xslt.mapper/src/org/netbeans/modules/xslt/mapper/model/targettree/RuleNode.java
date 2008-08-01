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
package org.netbeans.modules.xslt.mapper.model.targettree;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import org.netbeans.modules.soa.ui.SoaUtil;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xslt.mapper.model.nodes.NodeFactory;
import org.netbeans.modules.xslt.mapper.model.nodes.TreeNode;
import org.netbeans.modules.xslt.mapper.model.nodes.actions.ActionConst;
import org.netbeans.modules.xslt.mapper.model.nodes.actions.ActionGroupConstructor;
import org.netbeans.modules.xslt.mapper.model.nodes.actions.AddNestedAxiGroup;
import org.netbeans.modules.xslt.mapper.model.nodes.actions.AddNestedRulesGroup;
import org.netbeans.modules.xslt.mapper.model.nodes.actions.DeleteAction;
import org.netbeans.modules.xslt.mapper.model.nodes.actions.SupportedRuleTypes;
import org.netbeans.modules.xslt.mapper.model.nodes.visitor.NodeVisitor;
import org.netbeans.modules.xslt.mapper.view.XsltMapper;
import org.netbeans.modules.xslt.model.XslComponent;
import org.openide.util.NbBundle;

/**
 *
 * @author Alexey
 */
public class RuleNode extends StylesheetNode {
    
    
    public RuleNode(XslComponent component, XsltMapper mapper) {
        super(component, mapper);
    }
    
    protected List<TreeNode> loadChildren() {
        XslComponent myself = (XslComponent) getDataObject();
        List<XslComponent> children = myself.getChildren();
        
        List<TreeNode> result = new ArrayList<TreeNode>(children.size());
        
        
        for(XslComponent c: children){
            TreeNode newNode = (TreeNode) NodeFactory.createNode(c, getMapper());
            
            if (newNode != null){
                newNode.setParent(this);
                result.add(newNode);
            }
        }
        
        return result;
    }
    
    public AXIComponent getType() {
        return  (getParent() != null) ? getParent().getType():null;
    }
    
    public boolean isMappable() {
        return false;
    }
    
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
    
    public String toString(){
        return getComponent().getComponentType().getSimpleName();
    }
    
    public Image getIcon() {
        return SupportedRuleTypes.getCommonImage();
    }
    
    public String getName() {
        if (getType() == null) {
            return SoaUtil.getFormattedHtmlString(true,
                    new SoaUtil.TextChunk(toString(), SoaUtil.MISTAKE_RED));
        } else {
            return toString();
        }
    }
    
    public JPopupMenu constructPopupMenu() {
        JPopupMenu rootMenu = new JPopupMenu();
        Action newAction;
        //
        String localizedName = NbBundle.getMessage(
                ActionConst.class, ActionConst.ADD_MENU);
        JMenu addMenu = new JMenu(localizedName);
        //
        ActionGroupConstructor nestedAxi = new AddNestedAxiGroup(getMapper(), this);
        Action[] addNestedAxiArr = nestedAxi.getActions();
        //
        AddNestedRulesGroup nestedRules = new AddNestedRulesGroup(getMapper(), this);
        Action[] addNestedRuleArr = nestedRules.getActions();
        //
        if (addNestedAxiArr != null) {
            for (Action action : addNestedAxiArr) {
                addMenu.add(action);
            }
        }
        //
        if (addNestedAxiArr != null && addNestedAxiArr.length > 0 && 
                addNestedRuleArr != null && addNestedRuleArr.length > 0) {
            addMenu.add(new JSeparator());
        }
        //
        if (addNestedRuleArr != null) {
            for (Action action : addNestedRuleArr) {
                addMenu.add(action);
            }
        }
        // Add menu is added only if it's not empty
        if (addMenu.getMenuComponentCount() != 0) {
            rootMenu.add(addMenu);
        }
        //
        newAction = new DeleteAction(getMapper(), this);
        rootMenu.add(newAction);
        //
        return rootMenu;
    }
    
}
