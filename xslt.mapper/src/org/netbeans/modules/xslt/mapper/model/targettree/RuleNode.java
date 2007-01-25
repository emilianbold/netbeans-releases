/*
 * RuleNode.java
 *
 * Created on 22 Декабрь 2006 г., 13:58
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xslt.mapper.model.targettree;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.soa.ui.SoaUiUtil;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xslt.mapper.model.nodes.NodeFactory;
import org.netbeans.modules.xslt.mapper.model.nodes.TreeNode;
import org.netbeans.modules.xslt.mapper.model.nodes.actions.ActionConst;
import org.netbeans.modules.xslt.mapper.model.nodes.actions.ActionDescriptor;
import org.netbeans.modules.xslt.mapper.model.nodes.actions.ActionDescriptor.LeafActionDescriptor;
import org.netbeans.modules.xslt.mapper.model.nodes.actions.ActionDescriptor.SubMenuDescriptor;
import org.netbeans.modules.xslt.mapper.model.nodes.actions.ActionType;
import org.netbeans.modules.xslt.mapper.model.nodes.visitor.NodeVisitor;
import org.netbeans.modules.xslt.mapper.view.XsltMapper;
import org.netbeans.modules.xslt.model.XslComponent;

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
        return super.getIcon();
    }
    
    public String getName() {
        if (getType() == null) {
            return SoaUiUtil.getFormattedHtmlString(true,
                    new SoaUiUtil.TextChunk(toString(), SoaUiUtil.MISTAKE_RED));
        } else {
            return toString();
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
