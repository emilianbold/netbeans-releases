/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.qnavigator.navigator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.openide.nodes.Node;

/**
 *
 * @author Alexander Simon
 */
public class CsmFileModel {
    static final Logger logger = Logger.getLogger("org.netbeans.modules.cnd.qnavigator"); // NOI18N
    private List<IndexOffsetNode> lineNumberIndex = Collections.synchronizedList(new ArrayList<IndexOffsetNode>());
    private List<CppDeclarationNode> list = Collections.synchronizedList(new ArrayList<CppDeclarationNode>());
    private CsmFileFilter filter;
    private Action[] actions;

    public CsmFileModel(CsmFileFilter filter, Action[] actions){
        this.filter = filter;
        this.actions = actions;
    }

    public boolean setFile(CsmFile csmFile, boolean force){
        return buildModel(csmFile, force);
    }
    
    public Node[] getNodes() {
        return list.toArray(new Node[0]);
    }
    
    private void clear(){
        lineNumberIndex.clear();
        list.clear();
    }
    
    public CsmFileFilter getFilter(){
        return filter;
    }

    public void addOffset(Node node, CsmOffsetable element, List<IndexOffsetNode> lineNumberIndex) {
        lineNumberIndex.add(new IndexOffsetNode(node,element.getStartOffset(), element.getEndOffset()));
    }
    
    private boolean buildModel(CsmFile csmFile, boolean force) {
        boolean res = true;
        List<CppDeclarationNode> newList = new ArrayList<CppDeclarationNode>();
        List<IndexOffsetNode> newLineNumberIndex = new ArrayList<IndexOffsetNode>();
        if (csmFile != null && csmFile.isValid()) {
            if (filter.isApplicableInclude()) {
                for (CsmInclude element : csmFile.getIncludes()) {
                    CppDeclarationNode node = CppDeclarationNode.nodeFactory((CsmObject) element, this, false, newLineNumberIndex);
                    if (node != null) {
                        newList.add(node);
                    }
                }
            }
            if (filter.isApplicableMacro()) {
                for (CsmMacro element : csmFile.getMacros()) {
                    CppDeclarationNode node = CppDeclarationNode.nodeFactory((CsmObject) element, this, false, newLineNumberIndex);
                    if (node != null) {
                        newList.add(node);
                    }
                }
            }
            for (CsmOffsetableDeclaration element : csmFile.getDeclarations()) {
                if (filter.isApplicable(element)) {
                    CppDeclarationNode node = CppDeclarationNode.nodeFactory((CsmObject) element, this, false, newLineNumberIndex);
                    if (node != null) {
                        newList.add(node);
                    }
                }
            }
        }
        if (csmFile != null &&  csmFile.isValid()) {
            Collections.<CppDeclarationNode>sort(newList);
            Collections.<IndexOffsetNode>sort(newLineNumberIndex);
            resetScope(newLineNumberIndex);
            if (force || isNeedChange(newLineNumberIndex)) {
                clear();
                list.addAll(newList);
                lineNumberIndex.addAll(newLineNumberIndex);
                logger.log(Level.FINE, "Set new navigator model for file {0}", csmFile); // NOI18N
            } else {
                resetScope(lineNumberIndex);
                res = false;
                logger.log(Level.FINE, "Reset navigator model for file {0}", csmFile); // NOI18N
            }
        } else {
            clear();
            logger.log(Level.FINE, "Clear navigator model for file {0}", csmFile); // NOI18N
        }
        newList.clear();
        newLineNumberIndex.clear();
        return res;
    }

    private boolean isNeedChange(List<IndexOffsetNode> newLineNumberIndex){
        if (newLineNumberIndex.size() != lineNumberIndex.size()) {
            return true;
        }
        int i = 0;
        for (IndexOffsetNode n1 : lineNumberIndex) {
            if (newLineNumberIndex.size() <= i) {
                return true;
            }
            IndexOffsetNode n2 = newLineNumberIndex.get(i);
            if (!compareNodeContent(n1, n2)) {
                return true;
            }
            i++;
        }
        i = 0;
        for (IndexOffsetNode n1 : lineNumberIndex) {
            if (newLineNumberIndex.size() <= i) {
                return true;
            }
            IndexOffsetNode n2 = newLineNumberIndex.get(i);
            updateNodeContent(n1, n2);
            i++;
        }
        return false;
    }
    
    private boolean compareNodeContent(IndexOffsetNode n1, IndexOffsetNode n2){
        CppDeclarationNode d1 = (CppDeclarationNode) n1.getNode();
        CppDeclarationNode d2 = (CppDeclarationNode) n2.getNode();
        return d1.compareToWithoutOffset(d2) == 0;
    }

    private void updateNodeContent(IndexOffsetNode n1, IndexOffsetNode n2){
        CppDeclarationNode d1 = (CppDeclarationNode) n1.getNode();
        CppDeclarationNode d2 = (CppDeclarationNode) n2.getNode();
        d1.resetNode(d2);
        n1.resetContent(n2);
    }

    
    private void resetScope(List<IndexOffsetNode> newLineNumberIndex){
        Stack<IndexOffsetNode> stack = new Stack<IndexOffsetNode>();
        for(IndexOffsetNode node : newLineNumberIndex){
            while (!stack.empty()) {
                IndexOffsetNode scope = stack.peek();
                if (node.getStartOffset() >= scope.getStartOffset() &&
                    node.getEndOffset() <= scope.getEndOffset()) {
                    node.setScope(scope);
                    break;
                }
                stack.pop();
            }
            stack.push(node);
        }
    }
    
    public Node setSelection(long caretLineNo) {
        // Find nearest Node
        int index = Collections.<IndexOffsetNode>binarySearch(lineNumberIndex, new IndexOffsetNode(null, caretLineNo, caretLineNo));
        if (index < 0) {
            // exact line not found, but insersion index (-1) returned instead
            index = -index-2;
        }
        if (index > -1 && index < lineNumberIndex.size()) {
            IndexOffsetNode node  = lineNumberIndex.get(index);
            if (node.getStartOffset() <= caretLineNo &&
                node.getEndOffset() >= caretLineNo) {
                // exactly found
                return node.getNode();
            }
            IndexOffsetNode scopedNode = node.getScope();
            while (scopedNode != null){
                node = scopedNode;
                if (scopedNode.getStartOffset() <= caretLineNo &&
                    scopedNode.getEndOffset() >= caretLineNo) {
                    // found in parent
                    return scopedNode.getNode();
                }
                scopedNode = scopedNode.getScope();
            }
            // not found, return last scope
            return node.getNode();
        }
        return null;
    }

    public Action[] getActions() {
        return actions;
    }
}
