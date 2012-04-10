/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.completion.impl.xref;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.cnd.api.lexer.TokenItem;
import org.netbeans.modules.cnd.api.model.services.CsmReferenceContext;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;

/**
 * Implementation of <code>CsmReferenceContext</code>.
 *
 * @author Alexey Vladykin
 * @author Vladimir Voskresensky
 */
public class ReferenceContextImpl implements CsmReferenceContext {

    private final ReferenceContextImpl parent;
    private final int parentSize; // cached value of parent.size()
    // 0 - tokenItem, 1 - reference
    private final List<Object> stack;
    private int popCount;

    public ReferenceContextImpl() {
        this(null, false);
    }
    
    @SuppressWarnings("unchecked")
    public ReferenceContextImpl(ReferenceContextImpl parent, boolean fullcopy) {
        if (fullcopy && parent != null) {
            this.parent = null;
            this.parentSize = 0;
            int size = parent.size();
            this.stack = new ArrayList<Object>(size*2);
            for (int i = 0; i < size; ++i) {
                stack.add(parent.getTokenItem(i));
                stack.add(parent.getReference(i));
            }
        } else {
            this.parent = parent;
            this.parentSize = parent == null ? 0 : parent.size();
            this.stack = new ArrayList<Object>(0);
        }
    }

    @SuppressWarnings("unchecked")
    public ReferenceContextImpl(ReferenceContextImpl c) {
        this.parentSize = c.parentSize;
        if(c.parent != null) {
            this.parent = new ReferenceContextImpl(c.parent);
        } else {
            parent =  null;
        }
        this.stack = new ArrayList<Object>(c.stack);
        this.popCount = c.popCount;
    }

    @Override
    public int size() {
        return parentSize - popCount + stack.size() / 2;
    }

    @Override
    public CsmReference getReference() {
        return getReference(size() - 1);
    }

    @Override
    public CsmReference getReference(int i) {
        if (0 <= i && i < parentSize - popCount) {
            return parent.getReference(i);
        } else {
            return (CsmReference) stack.get(2 * (i - parentSize + popCount) + 1);
        }
    }

    @Override
    public CppTokenId getToken() {
        return getToken(size() - 1);
    }

    @Override
    public CppTokenId getToken(int i) {
        TokenItem<CppTokenId> token = getTokenItem(i);
        return token == null ? null : token.id();
    }

    @Override
    public int getTokenOffset() {
        return getTokenOffset(size() - 1);
    }

    @Override
    public int getTokenOffset(int i) {
        TokenItem<CppTokenId> token = getTokenItem(i);
        return token == null ? -1 : token.offset();
    }

    @SuppressWarnings("unchecked")
    private TokenItem<CppTokenId> getTokenItem(int i) {
        if (0 <= i && i < parentSize - popCount) {
            return parent.getTokenItem(i);
        } else {
            return (TokenItem<CppTokenId>) stack.get(2 * (i - parentSize + popCount));
        }
    }

    /*package*/
    @SuppressWarnings("unchecked")
    void push(TokenItem<? extends TokenId> token, CsmReference ref) {
        stack.add(token);
        stack.add(ref);
    }

    /*package*/ void pop() {
        if (stack.isEmpty()) {
            if (popCount < parentSize) {
                ++popCount;
            } else {
                throw new IllegalStateException("Stack underflow"); // NOI18N
            }
        } else {
            stack.remove(stack.size() - 1);
            stack.remove(stack.size() - 1);
        }
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < size(); ++i) {
            if (0 < i) {
                buf.append(' '); // NOI18N
            }
            buf.append('('); // NOI18N
            buf.append(getTokenItem(i));
            buf.append(','); // NOI18N
            CsmReference ref = getReference(i);
            buf.append(ref == null ? null : ref.getText());
            buf.append(')'); // NOI18N
        }
        return buf.toString();
    }
}
