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

import java.awt.ComponentOrientation;
import java.awt.Image;
import java.util.ArrayList;
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
import org.netbeans.modules.xml.axi.AXIType;
import org.netbeans.modules.xml.axi.Attribute;
import org.netbeans.modules.xml.axi.Element;
import org.netbeans.modules.xml.schema.model.Attribute.Use;
import org.netbeans.modules.xslt.mapper.model.nodes.NodeFactory;
import org.netbeans.modules.xslt.mapper.model.nodes.TreeNode;
import org.netbeans.modules.xslt.mapper.model.nodes.actions.ActionConst;
import org.netbeans.modules.xslt.mapper.model.nodes.actions.AddNestedRulesGroup;
import org.netbeans.modules.xslt.mapper.model.nodes.actions.AddThisAxiComponentAction;
import org.netbeans.modules.xslt.mapper.model.nodes.visitor.NodeVisitor;
import org.netbeans.modules.xslt.mapper.view.XsltMapper;
import org.openide.util.NbBundle;

/**
 *
 * @author Alexey
 */
public class SchemaNode extends TreeNode implements TooltipTextProvider {
    
//    private transient Boolean isSourceViewNode = null;
    
    /** Creates a new instance of PlaceholderNode */
    public SchemaNode(AXIComponent component,  XsltMapper mapper) {
        super(component, mapper);
    }
    
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
    
    public AXIComponent getType() {
        return (AXIComponent) getDataObject();
    }
    
    public boolean isMappable() {
        return true;
    }
    public String toString(){
        String name = ((AXIType) getType()).getName();
        return ((getDataObject() instanceof Attribute) ? "[@" : "[")+ name +"]";
    }
    protected List<TreeNode> loadChildren() {
        final ArrayList<TreeNode> result = new ArrayList<TreeNode>();
        
        AXIComponent axic = (AXIComponent) getDataObject();
        
        if(axic instanceof Element){
            new AXIUtils.ElementVisitor(){
                public void visit(AXIComponent c){
                    TreeNode newNode = (TreeNode) NodeFactory.createNode(c, getMapper());
                    if (newNode != null){
                        newNode.setParent(SchemaNode.this);
                        result.add(newNode);
                    }
                }
            }.visitSubelements((Element) axic);
        }
        return result;
        
    }
    
    public Image getIcon() {
        Object dataObject = getDataObject();
        if (dataObject instanceof Element) {
            BadgeModificator mult = AxiomUtils.getElementBadge((Element)dataObject);
            return NodeType.ELEMENT.getImage(mult);
        } else if (dataObject instanceof Attribute) {
            Use attrUse = ((Attribute)dataObject).getUse();
            if (attrUse == Use.OPTIONAL) {
                return NodeType.ATTRIBUTE.getImage(BadgeModificator.OPTIONAL);
            }
            return NodeType.ATTRIBUTE.getImage(BadgeModificator.SINGLE);
        } else {
            return null;
        }
    }
    
    public String getName() {
        Object dataObject = getDataObject();
        //
        String result = null;
        if (dataObject instanceof Element) {
            result = ((Element)dataObject).getName();
        } else if (dataObject instanceof Attribute) {
            result = ((Attribute)dataObject).getName();
        }
        //
        return result;
    }
    
    public String getName(boolean selected) {
        String result = getName();
        //
        if (!selected && !isSourceViewNode()) {
            result = SoaUiUtil.getFormattedHtmlString(true,
                    new SoaUiUtil.TextChunk(result, SoaUiUtil.INACTIVE_BLUE));
        }
        //
        return result;
    }
    
    public String getTooltipText() {
        Object dataObject = getDataObject();
        //
        if (dataObject instanceof Element) {
            return AxiomUtils.getElementTooltip((Element)dataObject);
        } else if (dataObject instanceof Attribute) {
            return AxiomUtils.getAttributeTooltip((Attribute)dataObject);
        }
        //
        return null;
    }
    
    public JPopupMenu constructPopupMenu() {
        JPopupMenu rootMenu = new JPopupMenu();
        Action newAction;
        //
        String localizedName = NbBundle.getMessage(
                ActionConst.class, ActionConst.ADD_MENU);
        JMenu addMenu = new JMenu(localizedName);
        //
        newAction = new AddThisAxiComponentAction(getMapper(), this);
        addMenu.add(newAction);
        //
        AddNestedRulesGroup nestedRules = new AddNestedRulesGroup(getMapper(), this);
        Action[] addNestedRuleArr = nestedRules.getActions();
        //
        if (addNestedRuleArr != null && addNestedRuleArr.length > 0) {
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
        return rootMenu;
    }
    
    
}
