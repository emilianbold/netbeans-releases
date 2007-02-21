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
package org.netbeans.modules.xslt.mapper.model.nodes;

import java.util.Map;
import javax.swing.JTree;
import org.netbeans.modules.soa.ui.axinodes.AxiomUtils;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIType;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xpath.LocationStep;
import org.netbeans.modules.xml.xpath.XPathLocationPath;
import org.netbeans.modules.xslt.mapper.view.XsltMapper;

/**
 *
 * @author Alexey
 */
public class SourceTypeFinder {
    private XsltMapper mapper;
    
    private LocationStep[] locationSteps;
    private Map<String, String> prefixesMap;
    /** Creates a new instance of SourceTypeFinder */
    public SourceTypeFinder(XsltMapper mapper){
        this.mapper = mapper;
        
        //FIXME! namespace prefixes are taken from root element!
        //should check the whole hierarchy, starting from element, which contains this expression
        prefixesMap = ((AbstractDocumentComponent) mapper
                .getContext()
                .getXSLModel()
                .getStylesheet())
                .getPrefixes();
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
        
        String nameTest = step.getNodeTest().toString();
        
        String name = "";
        String namespace = null;
        
        int pos = nameTest.indexOf(':');
        
        if (pos != -1){
            String prefix = nameTest.substring(0, pos);
            namespace = prefixesMap.get(prefix);
            name = nameTest.substring(pos + 1);
        } else {
            name = nameTest;
        }
        
        AXIComponent type = currentNode.getType();
        
        String typeName = ((AXIType) type).getName();
        String typeNamespace = AxiomUtils.isUnqualified(type) ? 
            "" : type.getTargetNamespace();
        
        
        if (namespace == null){
            namespace = "";
        }
        
        
        if (typeName.equals(name) && namespace.equals(typeNamespace)){
            
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
