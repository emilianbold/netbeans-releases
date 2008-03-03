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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.editor.ext.html.parser;

import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class AstNode {

    public enum NodeType {COMMENT, DECLARATION, ERROR,
        TEXT, TAG, UNMATCHED_TAG, OPEN_TAG, ENDTAG, ENTITY_REFERENCE};
    
    private String name;
    private NodeType nodeType;    
    private int startOffset;
    private int endOffset;
    private boolean closed;
    private Collection<AstNode> children = new ArrayList<AstNode>();
    private AstNode parent = null;

    public AstNode(String name, NodeType nodeType, int startOffset, int endOffset) {
        this.name = name;
        this.nodeType = nodeType;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.closed = false;
    }
    
    public String name() {
        return name;
    }

    public NodeType type() {
        return nodeType;
    }

    public int startOffset() {
        return startOffset;
    }

    public int endOffset() {
        return endOffset;
    }

    public Collection<AstNode> children() {
        return children;
    }
    
    void addChild(AstNode child) {
        children.add(child);
        child.setParent(this);
    }
    
    void markUnmatched(){
        nodeType = NodeType.UNMATCHED_TAG;
    }

    @Override
    public String toString() {
        StringBuilder childrenText = new StringBuilder();
        
        for (AstNode child : children()){
            String childTxt = child.toString();
            
            for (String line : childTxt.split("\n")){
                childrenText.append("-");
                childrenText.append(line);
                childrenText.append('\n');
            }
        }
        
        return name() + ":" + type() + "<" + startOffset() + ","
                + endOffset() + ">\n" + childrenText.toString(); 
    }
    
    public AstNode parent() {
        return parent;
    }
    
    void removeAllChildren(){
        children.clear();
    }
    
    private void setParent(AstNode parent) {
        this.parent = parent;
    }
    
    void setEndOffset(int endOffset){
        this.endOffset = endOffset;
    }
}
