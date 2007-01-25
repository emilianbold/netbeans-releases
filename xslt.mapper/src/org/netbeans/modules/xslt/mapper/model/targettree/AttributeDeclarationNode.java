/*
 * AttributeDeclarationNode.java
 *
 * Created on 11 январь 2007 г., 14:19
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xslt.mapper.model.targettree;

import java.awt.ComponentOrientation;
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.soa.ui.SoaUiUtil;
import org.netbeans.modules.soa.ui.axinodes.AxiomUtils;
import org.netbeans.modules.soa.ui.axinodes.NodeType;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.Element;
import org.netbeans.modules.xslt.mapper.model.nodes.NodeFactory;
import org.netbeans.modules.xslt.mapper.model.nodes.TreeNode;
import org.netbeans.modules.xslt.mapper.model.nodes.actions.ActionDescriptor;
import org.netbeans.modules.xslt.mapper.model.nodes.actions.ActionDescriptor.LeafActionDescriptor;
import org.netbeans.modules.xslt.mapper.model.nodes.actions.ActionType;
import org.netbeans.modules.xslt.mapper.model.nodes.visitor.NodeVisitor;
import org.netbeans.modules.xslt.mapper.view.GetExpressionVisitor;
import org.netbeans.modules.xslt.mapper.view.XsltMapper;
import org.netbeans.modules.xslt.model.Attribute;
import org.netbeans.modules.xslt.model.XslComponent;

/**
 *
 * @author Alexey
 */
public class AttributeDeclarationNode extends DeclarationNode {
    
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
        
        if (parent_type != null){
            Attribute attr = (Attribute) getComponent();
            for (org.netbeans.modules.xml.axi.AbstractAttribute a: parent_type.getAttributes()){
                if (a.getName().equals(attr.getName())){
                    return a;
                }
            }
        }
        return null;
    }
    public Image getIcon() {
        
        return NodeType.ATTRIBUTE.getImage();
    }
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
    
    public String getName(){
        AXIComponent axiComponent = getType();
        if (axiComponent instanceof org.netbeans.modules.xml.axi.Attribute) {
            return AxiomUtils.getAttributeHtmlDisplayName(
                    (org.netbeans.modules.xml.axi.Attribute) axiComponent,
                    ComponentOrientation.RIGHT_TO_LEFT);
        } else {
            return SoaUiUtil.getFormattedHtmlString(true,
                    new SoaUiUtil.TextChunk(toString(), SoaUiUtil.MISTAKE_RED));
        }
    }
    
    public ActionDescriptor<ActionType>[] getActionDescriptorArr() {
        ActionDescriptor<ActionType>[] retValue = new ActionDescriptor[] {
            new LeafActionDescriptor(ActionType.REMOVE)
        };
        return retValue;
    }
    
}
