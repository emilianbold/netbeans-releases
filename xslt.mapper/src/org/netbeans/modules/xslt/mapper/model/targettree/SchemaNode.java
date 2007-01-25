/*
 * PlaceholderNode.java
 *
 * Created on 22 Декабрь 2006 г., 13:57
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
import org.netbeans.modules.xml.axi.AXIType;
import org.netbeans.modules.xml.axi.Attribute;
import org.netbeans.modules.xml.axi.Element;
import org.netbeans.modules.xslt.mapper.model.nodes.NodeFactory;
import org.netbeans.modules.xslt.mapper.model.nodes.TreeNode;
import org.netbeans.modules.xslt.mapper.model.nodes.visitor.NodeVisitor;
import org.netbeans.modules.xslt.mapper.view.XsltMapper;

/**
 *
 * @author Alexey
 */
public class SchemaNode extends TreeNode {
    
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
            return NodeType.ELEMENT.getImage();
        } else if (dataObject instanceof Attribute) {
            return NodeType.ATTRIBUTE.getImage();
        } else {
            return null;
        }
    }
    
    public String getName() {
        Object dataObject = getDataObject();
        boolean isSourceViewNode = isSourceViewNode();
        //
        if (isSourceViewNode) {
            if (dataObject instanceof Element) {
                return AxiomUtils.getElementHtmlDisplayName(
                        (Element)dataObject, ComponentOrientation.LEFT_TO_RIGHT);
            } else if (dataObject instanceof Attribute) {
                return AxiomUtils.getAttributeHtmlDisplayName(
                        (Attribute)dataObject, ComponentOrientation.LEFT_TO_RIGHT);
            }
        } else {
            if (dataObject instanceof Element) {
                return AxiomUtils.getElementHtmlDisplayName((Element)dataObject, 
                        ComponentOrientation.RIGHT_TO_LEFT, 
                        SoaUiUtil.INACTIVE_BLUE);
            } else if (dataObject instanceof Attribute) {
                return AxiomUtils.getAttributeHtmlDisplayName(
                        (Attribute)dataObject, 
                        ComponentOrientation.RIGHT_TO_LEFT, 
                        SoaUiUtil.INACTIVE_BLUE);
            }
        }
        //
        return null;
    }
    
//    public boolean isSourceViewNode() {
//        if (isSourceViewNode == null) {
//            isSourceViewNode = calculateIsSourceViewNode() ?
//                Boolean.TRUE : Boolean.FALSE;
//        }
//        return isSourceViewNode;
//    }
//    
//    public boolean calculateIsSourceViewNode() {
//        TreeNode tempNode = this;
//        while (tempNode.getParent() != null) {
//            tempNode = tempNode.getParent();
//        }
//        //
//        if (tempNode instanceof SchemaNode) {
//            AXIComponent sourceType = getMapper().getContext().getSourceType();
//            AXIComponent rootNodeType = tempNode.getType();
//            if (rootNodeType.equals(sourceType)) {
//                return true;
//            }
//        }
//        //
//        return false;
//    }
    
}
