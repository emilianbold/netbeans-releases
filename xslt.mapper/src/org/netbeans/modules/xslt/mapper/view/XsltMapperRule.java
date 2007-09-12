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
package org.netbeans.modules.xslt.mapper.view;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import org.netbeans.modules.soa.mapper.basicmapper.BasicMapperRule;
import org.netbeans.modules.soa.mapper.common.IMapperGroupNode;
import org.netbeans.modules.soa.mapper.common.IMapperLink;
import org.netbeans.modules.soa.mapper.common.IMapperNode;
import org.netbeans.modules.soa.mapper.common.IMapperNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IFieldNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.tree.IMapperTreeNode;

/**
 *
 * @author Alexey
 */
public class XsltMapperRule extends BasicMapperRule {

    /**
     *
     * @param mapper
     */
    public XsltMapperRule(XsltMapper mapper) {
        super(mapper);
    }

    @Override
    public boolean isAllowToCreate(IMapperLink link) {
        IMapperNode startNode = link.getStartNode();
        IMapperNode endNode = link.getEndNode();
        //
        // Check if the start and end nodes are specified
        if (startNode == null || endNode == null) {
            return false;
        }
        //
        // In case of the start and end nodes are the field nodes
        // then they should be of different types: input and output
        if (startNode instanceof IFieldNode && endNode instanceof IFieldNode) {
            boolean isStartInput = ((IFieldNode) startNode).isInput();
            boolean isEndInput = ((IFieldNode) endNode).isInput();
            //
            if (isStartInput == isEndInput) {
                return false;
            }
            //
            boolean isStartOutput = ((IFieldNode) startNode).isOutput();
            boolean isEndOutput = ((IFieldNode) endNode).isOutput();
            //
            if (isStartOutput == isEndOutput) {
                return false;
            }
            if (startNode.getLinkCount() > 0) {
                return false;
            }
            if (endNode.getLinkCount() > 0) {
                return false;
            }
        }
        if (startNode instanceof IMapperTreeNode &&
            ((IMapperTreeNode) startNode).isDestTreeNode() && 
            startNode.getLinkCount() > 0){
                return false;
            
                
                    
        }
        if (endNode instanceof IMapperTreeNode &&
            ((IMapperTreeNode) endNode).isDestTreeNode() && 
            endNode.getLinkCount() > 0){
                return false;
                    
        }


        //
        return !(new CheckCyclicLinks(link).isCycle());
    }


    private class CheckCyclicLinks {

        private IMapperGroupNode stopAt;
        private Set<IMapperNode> visited = new HashSet<IMapperNode>();

        private boolean cycleFound = false;

        public CheckCyclicLinks(IMapperLink link) {

            if (link.getStartNode() instanceof IFieldNode && link.getEndNode() instanceof IFieldNode) {
                this.stopAt = link.getStartNode().getGroupNode();
                checkRecursive(link.getEndNode());
            }
        }

        public boolean isCycle() {
            return this.cycleFound;
        }

        private void checkRecursive(IMapperNode node) {


            if (node instanceof IFieldNode) {
                IMapperGroupNode group = node.getGroupNode();
                if (group == stopAt) {
                    cycleFound = true;
                    return;
                }
                if (group != null) {
                    IMapperNode n = group.getFirstNode();
                    while (n != null) {

                        for (Object obj : n.getLinks()) {
                            IMapperLink l = (IMapperLink) obj;
                            if (l.getStartNode() == n) {
                                checkRecursive(l.getEndNode());
                            }
                        }
                        n = group.getNextNode(n);
                    }
                }
            }
        }
    }
}
