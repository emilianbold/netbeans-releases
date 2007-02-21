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
