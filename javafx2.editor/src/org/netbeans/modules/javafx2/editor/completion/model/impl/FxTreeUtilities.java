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
package org.netbeans.modules.javafx2.editor.completion.model.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.modules.javafx2.editor.completion.model.FxModel;
import org.netbeans.modules.javafx2.editor.completion.model.FxNode;
import org.netbeans.modules.javafx2.editor.completion.model.FxNodeVisitor;

/**
 *
 * @author sdedic
 */
public final class FxTreeUtilities {
    private FxModel model;
    private ModelAccessor accessor = ModelAccessor.INSTANCE;

    public FxTreeUtilities(FxModel model) {
        this.model = model;
    }
    
    public boolean containsPos(FxNode n, int position, boolean caret) {
        return accessor.i(n).contains(position, caret);
    }
    
    public boolean contentContainsPos(FxNode n, int position, boolean caret) {
        return accessor.i(n).contentContains(position, caret);
    }
    
    public boolean isDefined(int pos) {
        return pos != -1;
    }
    
    public boolean isAccurate(int pos) {
        return pos >= 0;
    }
    
    public int getOffset(int pos) {
        return pos >= 0  ? pos : (-pos) - 1;
    }
    
    public int getStart(FxNode n, int position) {
        return accessor.i(n).getStart();
    }

    /**
     * Finds path of Nodes leading to the position position. If 'ignoreTag' is true,
     * and the position is within element's tag (incl. attributes), that element is
     * excluded.
     * If 'caret' is set, the position is interepreted as caret pos, that is between characters.
     * The caret must be after 1st offset of the element, or 
     * 
     * @param position
     * @param ignoreTag
     * @param caret
     * @return 
     */
    public List<? extends FxNode> findEnclosingElements(final int position, boolean ignoreTag,
            final boolean caret) {
        class T extends FxNodeVisitor.ModelTreeTraversal {
            Deque<FxNode>    nodeStack = new LinkedList<FxNode>();
            
            @Override
            protected void scan(FxNode node) {
                super.scan(node);
            }

            @Override
            public void visitNode(FxNode node) {
                NodeInfo ni = accessor.i(node);
                if (ni.isAttribute()) {
                    return;
                }
                if (ni.contains(position, caret)) {
                    nodeStack.push(node);
                    super.visitNode(node);
                }
                if (ni.getStart() > position) {
                    throw new Error();
                }
            }
            
        }
        
        T visitor = new T();
        try {
            model.accept(visitor);
        } catch (Error e) {
            // expected
        }
        if (!visitor.nodeStack.isEmpty() && ignoreTag) {
            FxNode n = visitor.nodeStack.peekFirst();
            if (!accessor.i(n).contentContains(position, caret)) {
                visitor.nodeStack.removeFirst();
            }
        }
        return Collections.unmodifiableList(
                new ArrayList<FxNode>(visitor.nodeStack)
        );
    }
}
