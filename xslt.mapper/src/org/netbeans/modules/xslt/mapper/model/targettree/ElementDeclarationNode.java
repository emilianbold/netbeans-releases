/*
 * ElementDeclarationNodes.java
 *
 * Created on 11 январь 2007 г., 14:27
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xslt.mapper.model.targettree;

import java.awt.ComponentOrientation;
import java.awt.Image;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.soa.ui.SoaUiUtil;
import org.netbeans.modules.soa.ui.axinodes.AxiomUtils;
import org.netbeans.modules.soa.ui.axinodes.NodeType;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xslt.mapper.model.nodes.NodeFactory;
import org.netbeans.modules.xslt.mapper.model.nodes.TreeNode;
import org.netbeans.modules.xslt.mapper.model.nodes.actions.ActionConst;
import org.netbeans.modules.xslt.mapper.model.nodes.actions.ActionDescriptor;
import org.netbeans.modules.xslt.mapper.model.nodes.actions.ActionDescriptor.LeafActionDescriptor;
import org.netbeans.modules.xslt.mapper.model.nodes.actions.ActionDescriptor.SubMenuDescriptor;
import org.netbeans.modules.xslt.mapper.model.nodes.actions.ActionType;
import org.netbeans.modules.xslt.mapper.model.nodes.visitor.NodeVisitor;
import org.netbeans.modules.xslt.mapper.view.GetExpressionVisitor;
import org.netbeans.modules.xslt.mapper.view.XsltMapper;
import org.netbeans.modules.xslt.model.Element;
import org.netbeans.modules.xslt.model.LiteralResultElement;
import org.netbeans.modules.xslt.model.XslComponent;

/**
 *
 * @author Alexey
 */
public class ElementDeclarationNode extends DeclarationNode{
    
    
    public ElementDeclarationNode(XslComponent component, XsltMapper mapper) {
        super(component, mapper);
    }
    protected List<TreeNode> loadChildren() {
        XslComponent myself = (XslComponent) getDataObject();
        
        
        
        
        if (GetExpressionVisitor.isValueOfContainer(myself) != null) {
            //do not show child elements, if value-of element
            //is the the only eleemnt inside this container
            return new ArrayList<TreeNode>();
        }
        
        List<XslComponent> children = myself.getChildren();
        
        final List<TreeNode> result = new ArrayList<TreeNode>(children.size());
        
        
        final Collection<AXIComponent> declaredTypes
                = new ArrayList<AXIComponent>(children.size());
        
        for(XslComponent c: children){
            TreeNode newNode = (TreeNode) NodeFactory.createNode(c, getMapper());
            
            if (newNode != null){
                newNode.setParent(this);
                result.add(newNode);
                declaredTypes.add(newNode.getType());
            }
        }
        
        AXIComponent axic = getType();
        
        if (axic != null) {
            new AXIUtils.ElementVisitor(){
                public void visit(AXIComponent c){
                    if (!declaredTypes.contains(c)){
                        TreeNode newNode = (TreeNode) NodeFactory.createNode(c, getMapper());
                        if (newNode != null){
                            newNode.setParent(ElementDeclarationNode.this);
                            result.add(newNode);
                        }
                        
                    }
                }
            }.visitSubelements((org.netbeans.modules.xml.axi.Element) axic);
            
            
        }
        return result;
    }
    
    public AXIComponent getType() {
        AXIComponent parent_type = getParent().getType();
        XslComponent component = getComponent();
        
        if (parent_type == null){ //no declaration nodes fond downtree
            AXIComponent axi_root =
                    getMapper().getContext().getTargetType();
            if( axi_root == null){
                return null;
            }
            
            if (AXIUtils.isSameSchemaType(component, axi_root)) {
                return axi_root;
            }
            
        } else {
            
            for (AXIComponent c: parent_type.getChildElements()){
                if (AXIUtils.isSameSchemaType(component, c)){
                    return c;
                }
            }
        }
        return null;
    }
    
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
    public String toString(){
        XslComponent comp  = getComponent();
        if (comp instanceof Element) {
            return ((Element) comp).getName().toString();
        } else if (comp instanceof LiteralResultElement) {
            return ((LiteralResultElement) comp).getQName().toString();
        }
        return comp.toString();
    }
    
    public Image getIcon() {
        
        return NodeType.ELEMENT.getImage();
    }
    
    public String getName() {
        AXIComponent axiComponent = getType();
        if (axiComponent instanceof org.netbeans.modules.xml.axi.Element) {
            return AxiomUtils.getElementHtmlDisplayName(
                    (org.netbeans.modules.xml.axi.Element)axiComponent, 
                    ComponentOrientation.RIGHT_TO_LEFT);
        } else {
            return SoaUiUtil.getFormattedHtmlString(true,
                    new SoaUiUtil.TextChunk(toString(), SoaUiUtil.MISTAKE_RED));
        }
    }
    
    public ActionDescriptor<ActionType>[] getActionDescriptorArr() {
        ActionDescriptor<ActionType>[] retValue = new ActionDescriptor[] {
            new LeafActionDescriptor(ActionType.REMOVE),
            new SubMenuDescriptor(ActionConst.ADD_MENU,
                    new SubMenuDescriptor(ActionConst.ADD_RULE),
                    new SubMenuDescriptor(ActionConst.ADD_SCHEMA,
                    new LeafActionDescriptor(ActionType.ADD_NESTED_AXI_OBJECT)
                    )
                    )
        };
        return retValue;
    }
    
}
