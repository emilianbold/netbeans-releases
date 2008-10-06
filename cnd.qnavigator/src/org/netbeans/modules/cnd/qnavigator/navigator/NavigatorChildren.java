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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmCompoundClassifier;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmEnum;
import org.netbeans.modules.cnd.api.model.CsmEnumerator;
import org.netbeans.modules.cnd.api.model.CsmFriend;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmNamespaceDefinition;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.openide.nodes.Children;

/**
 *
 * @author Alexander Simon
 */
public class NavigatorChildren extends Children.SortedArray {

    private CsmOffsetableDeclaration element;
    private CsmCompoundClassifier container;
    private CsmFileModel model;
    private List<IndexOffsetNode> lineNumberIndex;

    public NavigatorChildren(CsmOffsetableDeclaration element, CsmFileModel model, List<IndexOffsetNode> lineNumberIndex) {
        this(element, model, null, lineNumberIndex);
    }

    public NavigatorChildren(CsmOffsetableDeclaration element, CsmFileModel model, CsmCompoundClassifier container, List<IndexOffsetNode> lineNumberIndex) {
        this.element = element;
        this.container = container;
        this.model = model;
        this.lineNumberIndex = lineNumberIndex;
        this.getNodes();
    }

    @Override
    protected Collection initCollection() {
        List<CppDeclarationNode> retValue = new ArrayList<CppDeclarationNode>();
        if (container != null) {
            if (CsmKindUtilities.isClass(container)) {
                initClassifier((CsmClass) container, retValue);
            } else {
                initEnum((CsmEnum) container, retValue);
            }
        } else if (CsmKindUtilities.isClass(element)) {
            initClassifier((CsmClass) element, retValue);
        } else if (CsmKindUtilities.isEnum(element)) {
            initEnum((CsmEnum) element, retValue);
        } else if (CsmKindUtilities.isNamespaceDefinition(element)) {
            CsmNamespaceDefinition ns = (CsmNamespaceDefinition) element;
            for (CsmDeclaration decl : ns.getDeclarations()) {
                CppDeclarationNode node = CppDeclarationNode.nodeFactory(decl, model, false, lineNumberIndex);
                if (node != null) {
                    retValue.add(node);
                }
            }
        }
        Collections.<CppDeclarationNode>sort(retValue);
        return retValue;
    }

    private void initClassifier(CsmClass cls, List<CppDeclarationNode> retValue) {
        for (CsmMember member : cls.getMembers()) {
            CppDeclarationNode node = CppDeclarationNode.nodeFactory(member, model, false, lineNumberIndex);
            if (node != null) {
                retValue.add(node);
            }
        }
        for (CsmFriend friend : cls.getFriends()) {
            CppDeclarationNode node = CppDeclarationNode.nodeFactory(friend, model, true, lineNumberIndex);
            if (node != null) {
                retValue.add(node);
            }
        }
    }

    private void initEnum(CsmEnum cls, List<CppDeclarationNode> retValue) {
        for (CsmEnumerator en : cls.getEnumerators()) {
            CppDeclarationNode node = CppDeclarationNode.nodeFactory(en, model, false, lineNumberIndex);
            if (node != null) {
                retValue.add(node);
            }
        }
    }
}