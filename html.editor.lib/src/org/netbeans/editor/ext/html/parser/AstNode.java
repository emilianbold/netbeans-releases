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

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
    private List<AstNode> children = null;
    private AstNode parent = null;
    private Map<String, Object> attributes = null;

    AstNode(String name, NodeType nodeType, int startOffset, int endOffset) {
        this.name = name;
        this.nodeType = nodeType;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
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

    public List<AstNode> children() {
        return children == null ? Collections.EMPTY_LIST : children;
    }
    
    void addChild(AstNode child) {
        if (children == null) {
            children = new LinkedList<AstNode>();
        }
        children.add(child);
        child.setParent(this);
    }
    
    void markUnmatched(){
        nodeType = NodeType.UNMATCHED_TAG;
    }

    void setAttribute(String key, Object value) {
        if(attributes == null) {
            attributes = new HashMap<String, Object>();
        }
        attributes.put(key, value);
    }
    
    public Object getAttribute(String key) {
        return attributes == null ? null : attributes.get(key);
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
    
    String signature() {
        return name() + "[" + type() + "]";
    }
    
    public AstNode parent() {
        return parent;
    }
    
    /** returns the AST path from the root element */
    public AstPath path() {
        return new AstPath(null, this);
    }
    
    void removeTagChildren(){
        for (Iterator<AstNode> it = children().iterator(); it.hasNext();){
            if (it.next().isTagNode()){
                it.remove();
            }
        }
    }
    
    boolean isTagNode(){
        return type() == NodeType.TAG || type() == NodeType.UNMATCHED_TAG;
    }
    
    private void setParent(AstNode parent) {
        this.parent = parent;
    }
    
    void setEndOffset(int endOffset){
        this.endOffset = endOffset;
    }
}
