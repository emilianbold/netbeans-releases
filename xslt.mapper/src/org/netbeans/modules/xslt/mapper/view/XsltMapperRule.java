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
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
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
        return !(new CheckCyclicLinks(link).isCycle());
    }
    
    
    private class CheckCyclicLinks{
        private IMapperGroupNode stopAt;
        private Set<IMapperNode> visited = new HashSet<IMapperNode>();
        
        private boolean cycleFound = false;
        public CheckCyclicLinks(IMapperLink link){
            
            if (link.getStartNode() instanceof IFieldNode &&
                    link.getEndNode() instanceof IFieldNode){
                this.stopAt = link.getStartNode().getGroupNode();
                checkRecursive(link.getEndNode());
            }
            
            
        }
        public boolean isCycle(){
            return this.cycleFound;
        }
        
        private void checkRecursive(IMapperNode node){
            
            
            if (node instanceof IFieldNode){
                IMapperGroupNode group = node.getGroupNode();
                if (group == stopAt){
                    cycleFound = true;
                    return;
                }
                if (group != null){
                    IMapperNode n = group.getFirstNode();
                    while (n != null){
                        
                        for (Object obj: n.getLinks()){
                            IMapperLink l = (IMapperLink) obj;
                            if(l.getStartNode() == n){
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
