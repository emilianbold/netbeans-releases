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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.qnavigator.navigator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
        lineNumberIndex.add(new IndexOffsetNode(node,element.getStartOffset()));
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
        } else {
            list.clear();
            lineNumberIndex.clear();
        }
    }

    public Node setSelection(long caretLineNo) {
        // Find nearest Node
        int index = Collections.<IndexOffsetNode>binarySearch(lineNumberIndex, new IndexOffsetNode(null, caretLineNo));
        if (index < 0) {
            // exact line not found, but insersion index (-1) returned instead
            index = -index-2;
        }
        if (index > -1 && index < lineNumberIndex.size()) {
            IndexOffsetNode node  = lineNumberIndex.get(index);
            return node.getNode();
        }
        return null;
    }

    public Action[] getActions() {
        return actions;
    }
}
