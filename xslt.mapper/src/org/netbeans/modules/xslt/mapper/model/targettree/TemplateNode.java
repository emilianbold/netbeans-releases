/*
 * TemplateNode.java
 *
 * Created on 15 январь 2007 г., 16:48
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xslt.mapper.model.targettree;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xslt.mapper.model.nodes.NodeFactory;
import org.netbeans.modules.xslt.mapper.model.nodes.TreeNode;
import org.netbeans.modules.xslt.mapper.view.XsltMapper;
import org.netbeans.modules.xslt.model.XslComponent;

/**
 *
 * @author Alexey
 */
public class TemplateNode extends RuleNode{
    
    /** Creates a new instance of TemplateNode **/
    public TemplateNode(XslComponent component, XsltMapper mapper) {
        super(component, mapper);
    }
    /** Root template is combining behaviors of schema placeholder node and rule node:
     * it adds schema placeholdeers to the list of XSLT children
     **/
    protected List<TreeNode> loadChildren() {
        XslComponent myself = (XslComponent) getDataObject();
        List<TreeNode> result = super.loadChildren();
        
        AXIComponent rootType = getMapper().getContext().getTargetType();
        
        if (rootType == null){
            return result;
        }
        
        boolean hasRootType = false;
        
        for(TreeNode t: result){
            AXIComponent type = t.getType();
            if (type != null && type.equals(rootType)){
                hasRootType = true;
            }
        }
        
        if (!hasRootType){
            TreeNode newNode = (TreeNode) NodeFactory.
                    createNode(rootType, getMapper());
            newNode.setParent(this);
            result.add(newNode);
        }
        
        return result;
    }
}
