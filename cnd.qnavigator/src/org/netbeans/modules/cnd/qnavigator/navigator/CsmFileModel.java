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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.qnavigator.navigator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
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
    private List<IndexOffsetNode> lineNumberIndex = Collections.synchronizedList(new ArrayList<IndexOffsetNode>());
    private List<CppDeclarationNode> list = Collections.synchronizedList(new ArrayList<CppDeclarationNode>());
    private CsmFileFilter filter;
    private Action[] actions;

    public CsmFileModel(CsmFileFilter filter, Action[] actions){
        this.filter = filter;
        this.actions = actions;
    }

    public void setFile(CsmFile csmFile){
        buildModel(csmFile);
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

    public void addOffset(Node node, CsmOffsetable element) {
        lineNumberIndex.add(new IndexOffsetNode(node,element.getStartOffset(), element.getEndOffset()));
    }
    
    private void buildModel(CsmFile csmFile) {
        clear();
        if (csmFile != null && csmFile.isValid()) {
            if (filter.isApplicableInclude()) {
                for (CsmInclude element : csmFile.getIncludes()) {
                    CppDeclarationNode node = CppDeclarationNode.nodeFactory((CsmObject) element, this, false);
                    if (node != null) {
                        list.add(node);
                    }
                }
            }
            if (filter.isApplicableMacro()) {
                for (CsmMacro element : csmFile.getMacros()) {
                    CppDeclarationNode node = CppDeclarationNode.nodeFactory((CsmObject) element, this, false);
                    if (node != null) {
                        list.add(node);
                    }
                }
            }
            for (CsmOffsetableDeclaration element : csmFile.getDeclarations()) {
                if (filter.isApplicable(element)) {
                    CppDeclarationNode node = CppDeclarationNode.nodeFactory((CsmObject) element, this, false);
                    if (node != null) {
                        list.add(node);
                    }
                }
            }
        }
        if (csmFile != null &&  csmFile.isValid()) {
            Collections.<CppDeclarationNode>sort(list);
            Collections.<IndexOffsetNode>sort(lineNumberIndex);
            resetScope();
        } else {
            list.clear();
            lineNumberIndex.clear();
        }
    }

    private void resetScope(){
        Stack<IndexOffsetNode> stack = new Stack<IndexOffsetNode>();
        for(IndexOffsetNode node : lineNumberIndex){
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
