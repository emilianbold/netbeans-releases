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
import javax.swing.JPopupMenu;
import org.netbeans.modules.soa.ui.SoaUiUtil;
import org.netbeans.modules.soa.ui.TooltipTextProvider;
import org.netbeans.modules.soa.ui.axinodes.AxiomUtils;
import org.netbeans.modules.soa.ui.axinodes.NodeType;
import org.netbeans.modules.soa.ui.axinodes.NodeType.BadgeModificator;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.Element;
import org.netbeans.modules.xml.schema.model.Attribute.Use;
import org.netbeans.modules.xslt.mapper.model.nodes.NodeFactory;
import org.netbeans.modules.xslt.mapper.model.nodes.TreeNode;
import org.netbeans.modules.xslt.mapper.model.nodes.actions.DeleteAction;
import org.netbeans.modules.xslt.mapper.model.nodes.visitor.NodeVisitor;
import org.netbeans.modules.xslt.mapper.view.GetExpressionVisitor;
import org.netbeans.modules.xslt.mapper.view.XsltMapper;
import org.netbeans.modules.xslt.model.Attribute;
import org.netbeans.modules.xslt.model.AttributeValueTemplate;
import org.netbeans.modules.xslt.model.XslComponent;

/**
 *
 * @author Alexey
 */
public class AttributeDeclarationNode extends DeclarationNode
        implements TooltipTextProvider {
    
    /** Creates a new instance of AttributeDeclarationNode */
    public AttributeDeclarationNode(XslComponent component, XsltMapper mapper) {
        super(component, mapper);
    }
    /**
     * attribute element has no children
     * @returns empty list
     **/
    protected List<TreeNode> loadChildren() {
        Attribute myself = (Attribute) getDataObject();
        
        if (GetExpressionVisitor.isValueOfContainer(myself) != null) {
            //do not show child elements, if value-of element
            //is the the only eleemnt inside this container
            return new ArrayList<TreeNode>();
        }
        
        List<XslComponent> children = myself.getChildren();
        final List<TreeNode> result = new ArrayList<TreeNode>(children.size());
        
        
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
        Element parent_type = (Element) getParent().getType();
        XslComponent component = getComponent();        
        
        if (parent_type != null){
            for (AXIComponent c: parent_type.getAttributes()){
                if (AXIUtils.isSameSchemaType(component, c)){
                    return c;
                }
            }
        }
        return null;
    }
    
    public Image getIcon() {
        AXIComponent axiComponent = getType();
        if (axiComponent instanceof org.netbeans.modules.xml.axi.Attribute) {
            Use attrUse = ((org.netbeans.modules.xml.axi.Attribute)axiComponent).getUse();
            if (attrUse == Use.OPTIONAL) {
                return NodeType.ATTRIBUTE.getImage(BadgeModificator.OPTIONAL);
            } else {
                return NodeType.ATTRIBUTE.getImage(BadgeModificator.SINGLE);
            }
        }
        //
        return NodeType.ATTRIBUTE.getImage(BadgeModificator.SINGLE);
    }
    
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
    
    public String getName(){
        AXIComponent axiComponent = getType();
        if (axiComponent instanceof org.netbeans.modules.xml.axi.Attribute) {
            return ((org.netbeans.modules.xml.axi.Attribute)axiComponent).getName();
        } else {
            Attribute myself = (Attribute) getDataObject();
            return myself.getName().toString();
        }
    }
    
    public String getName(boolean selected){
        AXIComponent axiComponent = getType();
        if (selected) {
            return getName();
        } else if (axiComponent instanceof org.netbeans.modules.xml.axi.Attribute) {
            return getName();
        } else {
            String name = getName();
            return SoaUiUtil.getFormattedHtmlString(true,
                    new SoaUiUtil.TextChunk(name, SoaUiUtil.MISTAKE_RED));
        }
    }
    
        public String toString() {



        Attribute component = (Attribute) getComponent();

        AttributeValueTemplate atv = component.getName();

        if (atv != null && atv.getQName() != null) {
            return atv.getQName().getLocalPart();
        }

        org.netbeans.modules.xml.axi.Attribute attribute = (org.netbeans.modules.xml.axi.Attribute) getType();
        if (attribute != null && attribute.getName() != null) {
            return attribute.getName();
        }

        return super.toString();
    }

    
    public String getTooltipText() {
        AXIComponent axiComponent = getType();
        if (axiComponent instanceof org.netbeans.modules.xml.axi.Attribute) {
            return AxiomUtils.getAttributeTooltip(
                    (org.netbeans.modules.xml.axi.Attribute) axiComponent);
        } else {
            Attribute myself = (Attribute) getDataObject();
            String name = myself.getName().toString();
            return SoaUiUtil.getFormattedHtmlString(true,
                    new SoaUiUtil.TextChunk(name, SoaUiUtil.MISTAKE_RED));
        }
    }
    
    public JPopupMenu constructPopupMenu() {
        JPopupMenu rootMenu = new JPopupMenu();
        Action newAction = new DeleteAction(getMapper(), this);
        rootMenu.add(newAction);
        //
        return rootMenu;
    }
    
}
