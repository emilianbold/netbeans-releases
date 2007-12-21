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
import java.util.HashMap;
import java.util.List;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import org.netbeans.modules.soa.ui.SoaUiUtil;
import org.netbeans.modules.soa.ui.TooltipTextProvider;
import org.netbeans.modules.soa.ui.axinodes.AxiomUtils;
import org.netbeans.modules.soa.ui.axinodes.NodeType;
import org.netbeans.modules.soa.ui.axinodes.NodeType.BadgeModificator;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xslt.mapper.model.nodes.NodeFactory;
import org.netbeans.modules.xslt.mapper.model.nodes.TreeNode;
import org.netbeans.modules.xslt.mapper.model.nodes.actions.ActionConst;
import org.netbeans.modules.xslt.mapper.model.nodes.actions.ActionGroupConstructor;
import org.netbeans.modules.xslt.mapper.model.nodes.actions.AddNestedAxiGroup;
import org.netbeans.modules.xslt.mapper.model.nodes.actions.AddNestedRulesGroup;
import org.netbeans.modules.xslt.mapper.model.nodes.actions.DeleteAction;
import org.netbeans.modules.xslt.mapper.model.nodes.visitor.NodeVisitor;
import org.netbeans.modules.xslt.mapper.view.GetExpressionVisitor;
import org.netbeans.modules.xslt.mapper.view.XsltMapper;
import org.netbeans.modules.xslt.model.Element;
import org.netbeans.modules.xslt.model.LiteralResultElement;
import org.netbeans.modules.xslt.model.XslComponent;
import org.openide.util.NbBundle;

/**
 *
 * @author Alexey
 */
public class ElementDeclarationNode extends DeclarationNode implements TooltipTextProvider {

    public ElementDeclarationNode(XslComponent component, XsltMapper mapper) {
        super(component, mapper);
    }

    protected List<TreeNode> loadChildren() {
        XslComponent myself = (XslComponent) getDataObject();


        List<AXIComponent> childTypes = AXIUtils.getChildTypes(getType());
        List<AXIComponent> usedTypes = new ArrayList<AXIComponent>();
        List<TreeNode> xslNodes = new ArrayList<TreeNode>();

        //dont show child XSL components if the only child of current eleemnt is "value-of" element
        if (GetExpressionVisitor.isValueOfContainer(myself) == null) {

            List<XslComponent> children = myself.getChildren();
            for (XslComponent c : children) {
                TreeNode newNode = (TreeNode) NodeFactory.createNode(c, getMapper());

                if (newNode == null) {
                    continue;
                }

                newNode.setParent(this);
                xslNodes.add(newNode);
                usedTypes.add(newNode.getType());
            }
        }
        TreeNode current = null;
        int lastPos = 0;
        List<TreeNode> results = new ArrayList<TreeNode>();

        for (TreeNode xsl_tn : xslNodes) {
            AXIComponent type = xsl_tn.getType();
            int pos0 = childTypes.indexOf(type);
            if (pos0 > 0) {
                for (int n = lastPos; n < pos0; n++) {
                    AXIComponent t = childTypes.get(n);
                    if (!usedTypes.contains(t)) {
                        results.add(createSchemaNode(t));
                    }
                }
                lastPos = pos0 + 1;
            }
            results.add(xsl_tn);
        }

        //add the remaining schema nodes
        for (int n = lastPos; n < childTypes.size(); n++) {
            AXIComponent t = childTypes.get(n);

            if (!usedTypes.contains(t)) {
                results.add(createSchemaNode(t));
            }
        }
        return results;
    }

    public AXIComponent getType() {
        AXIComponent type = AXIUtils.getType(getComponent(), getMapper());
        if (type == null || type.getModel() == null){
            return null;
        }
        return type;
    }

    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    public String toString() {
        XslComponent comp = getComponent();
        if (comp instanceof Element) {
            return ((Element) comp).getName().getQName().getLocalPart();
        } else if (comp instanceof LiteralResultElement) {
            return ((LiteralResultElement) comp).getQName().getLocalPart();
        }
        return comp.toString();
    }

    public Image getIcon() {
        AXIComponent axiComponent = getType();
        if (axiComponent instanceof org.netbeans.modules.xml.axi.Element) {
            BadgeModificator bm = AxiomUtils.getElementBadge((org.netbeans.modules.xml.axi.Element) axiComponent);
            return NodeType.ELEMENT.getImage(bm);
        }
        //
        return NodeType.ELEMENT.getImage(BadgeModificator.SINGLE);
    }

    public String getName() {
        AXIComponent axiComponent = getType();
        if (axiComponent instanceof org.netbeans.modules.xml.axi.Element) {
            return ((org.netbeans.modules.xml.axi.Element) axiComponent).getName();
        } 
        return toString();
    }

    public String getName(boolean selected) {
        AXIComponent axiComponent = getType();



        if (selected) {
            return getName();
        } else if (axiComponent instanceof org.netbeans.modules.xml.axi.Element) {
            return getName();
        } else {
            return SoaUiUtil.getFormattedHtmlString(true, new SoaUiUtil.TextChunk(getName(), SoaUiUtil.MISTAKE_RED));
        }
    }

    public String getTooltipText() {
        AXIComponent axiComponent = getType();
        if (axiComponent instanceof org.netbeans.modules.xml.axi.Element) {
            return AxiomUtils.getElementTooltip((org.netbeans.modules.xml.axi.Element) axiComponent);
        } else {
            return SoaUiUtil.getFormattedHtmlString(true, new SoaUiUtil.TextChunk(toString(), SoaUiUtil.MISTAKE_RED));
        }
    }

    public JPopupMenu constructPopupMenu() {
        JPopupMenu rootMenu = new JPopupMenu();
        //
        String localizedName = NbBundle.getMessage(ActionConst.class, ActionConst.ADD_MENU);
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
        if (addNestedAxiArr != null && addNestedAxiArr.length > 0 && addNestedRuleArr != null && addNestedRuleArr.length > 0) {
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
        Action newAction = new DeleteAction(getMapper(), this);
        rootMenu.add(newAction);
        //
        return rootMenu;
    }

    private TreeNode createSchemaNode(AXIComponent c) {
        TreeNode newNode = (TreeNode) NodeFactory.createNode(c, getMapper());
        if (newNode != null) {
            newNode.setParent(ElementDeclarationNode.this);
        }
        return newNode;
    }
}
