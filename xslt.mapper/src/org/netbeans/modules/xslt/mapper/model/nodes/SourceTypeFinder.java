/*
 * SourceTypeFinder.java
 *
 * Created on 19 январь 2007 г., 12:23
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xslt.mapper.model.nodes;

import javax.swing.JTree;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIType;
import org.netbeans.modules.xml.xpath.LocationStep;
import org.netbeans.modules.xml.xpath.XPathExpressionPath;
import org.netbeans.modules.xml.xpath.XPathLocationPath;
import org.netbeans.modules.xslt.mapper.view.XsltMapper;

/**
 *
 * @author Alexey
 */
public class SourceTypeFinder {
    private XsltMapper mapper;
    
    private LocationStep[] locationSteps;
    /** Creates a new instance of SourceTypeFinder */
    public SourceTypeFinder(XsltMapper mapper){
        this.mapper = mapper;
        
    }
    
    public TreeNode findNode(XPathLocationPath path){
        JTree sourceTree = mapper.
                getMapperViewManager().
                getSourceView().
                getTree();
        
        
        
        this.locationSteps = path.getSteps();
        
        return findImpl((TreeNode) sourceTree.getModel().getRoot(), 0);
        
    }
    
    private TreeNode findImpl(TreeNode currentNode, int depth){
        LocationStep step = locationSteps[depth];
        
        String typeName = ((AXIType) currentNode.getType()).getName();
        
        String testName = step.getNodeTest().toString();
        
        if (typeName.equals(testName)){
            
            if (depth == (locationSteps.length - 1)) {
                //last step in path
                return currentNode;
            }
            
            if (!currentNode.getChildren().isEmpty()){
                //perform recursion
                for (TreeNode tn: currentNode.getChildren()){
                    TreeNode result = findImpl(tn, depth + 1);
                    if (result != null){
                        return result;
                    }
                }
            }
        }
        
        return null;
    }
    
    
}
