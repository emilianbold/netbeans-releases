/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javafx2.editor.completion.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Encapsulates non-public data 
 *
 * @author sdedic
 */
class NodeInfo {
    private int    start;
    private int    end = -1;
    
    private int    contentStart = -1;
    private int    contentEnd = -1;
    
    private List<FxNode>    children = Collections.EMPTY_LIST;
    
    static NodeInfo newNode() {
        return new NodeInfo(-1);
    }
    
    static NodeInfo syntheticNode() {
        return new NodeInfo(-1);
    }

    public NodeInfo(int start) {
        this.start = start;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public int getContentStart() {
        return contentStart;
    }

    public int getContentEnd() {
        return contentEnd;
    }
    
    public NodeInfo endsAt(int e) {
        this.end = e;
        return this;
    }
    
    public NodeInfo endContent(int pos) {
        this.contentEnd = pos;
        return this;
    }
    
    public NodeInfo startContent(int pos) {
        this.contentStart = pos;
        return this;
    }
    
    public void addChild(FxNode n) {
        if (children.isEmpty()) {
            children = new ArrayList<FxNode>();
        }
        children.add(n);
    }

    public List<FxNode> getChildren() {
        return children.isEmpty() ? 
                Collections.<FxNode>emptyList() : 
                Collections.unmodifiableList(children);
    }
    
    int offset(int o) {
        return o >= 0 ? o : (- o) - 1;
    }
    
    boolean contentContains(int position) {
        if (contentStart == -1) {
            return false;
        }
        if (contentEnd == -1) {
            if (end == -1) {
                return false;
            }
            return offset(contentStart) <= position &&
                   offset(end) > position;
        } else {
            return offset(contentStart) <= position &&
                   offset(contentEnd) > position;
        }
    }

    boolean contains(int position) {
        if (position < start) {
            return false;
        }
        int e = end;
        if (end == -1) {
            return false;
        } else if (end < 0) {
            e = (-end) - 1;
        }
        return position <= e;
    }
}
