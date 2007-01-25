/*
 * DiagramBuilder.java
 *
 * Created on 19 январь 2007 г., 17:34
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xslt.mapper.view;

import org.netbeans.modules.soa.mapper.common.IMapperLink;
import org.netbeans.modules.xml.xpath.XPathExpression;
import org.netbeans.modules.xslt.mapper.model.nodes.Node;
import org.netbeans.modules.xslt.mapper.model.nodes.TreeNode;
import org.netbeans.modules.xslt.model.SelectSpec;
import org.netbeans.modules.xslt.model.XslComponent;
import org.netbeans.modules.xslt.model.XslVisitor;

/**
 *
 * @author Alexey
 */
public class DiagramBuilder {
    
    private XsltMapper mapper;
    
    public DiagramBuilder(XsltMapper mapper) {
        this.mapper = mapper;
    }
    public void updateDiagram(){
        TreeNode root = (TreeNode) mapper.getMapperViewManager()
        .getDestView()
        .getTree()
        .getModel()
        .getRoot();

        if (root != null){
            updateDiagramRecursive(root);
            mapper.getMapperViewManager().getCanvasView().getAutoLayout().autoLayout();
        }
    }
    public void updateDiagram(TreeNode tree_node){
        Object data = tree_node.getDataObject();
        
        
        if (data instanceof XslComponent){
            
            XslComponent xslc = (XslComponent) data;
            
            GetExpressionVisitor expression_visitor =
                    new GetExpressionVisitor();
            
            xslc.accept(expression_visitor );
            if (expression_visitor.getResult() != null){
                NodeCreatorVisitor node_visitor = new NodeCreatorVisitor(mapper);
                
                expression_visitor.getResult().accept(node_visitor);
                
                if (node_visitor.getResult() != null){
                    mapper.addLink(node_visitor.getResult(), tree_node);
                }
            }
            
        }
        
    }
    
    private void updateDiagramRecursive(TreeNode node){
        updateDiagram(node);
        for( TreeNode n: node.getChildren()){
            updateDiagramRecursive(n);
        }
    }
    
    
}
