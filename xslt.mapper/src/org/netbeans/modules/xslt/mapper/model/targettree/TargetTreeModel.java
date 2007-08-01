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
    

    
    public TargetTreeModel(XsltMapper mapper) {
        super(mapper);

    }

    public TreeNode loadRoot() {
        if (getMapper().getContext().getXSLModel() != null &&
            getMapper().getContext().getXSLModel().getStylesheet() != null){
            Stylesheet stylesheet = getMapper().getContext().getXSLModel().getStylesheet();
            
            List<Template> templates = stylesheet.getChildren(Template.class);
            for (Template t: templates){
                if (t.getMatch().equals("/")){
                    return (TreeNode) NodeFactory.createNode(t, getMapper());
                }
            }
        }  
         return null;
        
    }
    
}
