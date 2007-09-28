/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
import org.netbeans.modules.xml.xpath.XPathPredicateExpression;
import org.netbeans.modules.xslt.mapper.model.targettree.PredicatedSchemaNode;
import org.netbeans.modules.xslt.mapper.view.PredicateManager;
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
        if (locationSteps == null || depth >= locationSteps.length) {
            return null;
        }
        LocationStep step = locationSteps[depth];
        
        XPathPredicateExpression[] predicates = step.getPredicates();
        boolean predicatedNodeRequired =
                predicates != null && predicates.length != 0;
        if (predicatedNodeRequired && currentNode instanceof PredicatedSchemaNode) {
            // A not predicated node has to be found at first
            // The corresponding predicated node will be looked for later.
            return null;
        }
        //
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
            
            // Try find the same predicated node
            if (predicatedNodeRequired) {
                PredicateManager pManager = mapper.getPredicateManager();
                TreeNode predicatedNode = pManager.getPredicatedNode(
                        currentNode, predicates);
                //
                // The predicated node has to be already created here
                assert predicatedNode != null;
//                if (predicatedNode == null) {
//                    // Creates a new predicated node and register a new predicate
//                    // in the predicates' manager.
//                    predicatedNode = pManager.createPredicatedNode(
//                            currentNode, predicates);
//                    assert predicatedNode != null;
//                }
                currentNode = predicatedNode;
            }
            
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
