/*
 * SourceTreeModel.java
 *
 * Created on 19 Декабрь 2006 г., 19:50
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xslt.mapper.model.targettree;

import java.util.List;
import org.netbeans.modules.xslt.mapper.model.XsltNodesTreeModel;

import org.netbeans.modules.xslt.mapper.model.nodes.NodeFactory;
import org.netbeans.modules.xslt.mapper.model.nodes.TreeNode;
import org.netbeans.modules.xslt.mapper.view.XsltMapper;
import org.netbeans.modules.xslt.model.Stylesheet;
import org.netbeans.modules.xslt.model.Template;

/**
 *
 * @author Alexey
 */
public class TargetTreeModel extends XsltNodesTreeModel {
    
    private XsltMapper mapper;
    
    public TargetTreeModel(XsltMapper mapper) {
        this.mapper = mapper;
        if (mapper.getContext().getXSLModel() != null &&
            mapper.getContext().getXSLModel().getStylesheet() != null){
            Stylesheet stylesheet = mapper.getContext().getXSLModel().getStylesheet();
            
            
            List<Template> templates = stylesheet.getChildren(Template.class);
            for (Template t: templates){
                if (t.getMatch().equals("/")){
                    TreeNode rootNode = (TreeNode) NodeFactory.createNode(t, mapper);
                    setRootNode(rootNode);
                    break;
                }
            }
        }  else {
            //rootNode = new textNode("XSLT Model is not available");
        }
    }
    
}
