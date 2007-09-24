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

    public NavigatorChildren(CsmOffsetableDeclaration element, CsmFileModel model) {
        this.element = element;
        this.model = model;
        this.getNodes();
    }

    public NavigatorChildren(CsmOffsetableDeclaration element, CsmFileModel model, CsmCompoundClassifier container) {
        this.element = element;
        this.container = container;
        this.model = model;
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
                CppDeclarationNode node = CppDeclarationNode.nodeFactory(decl, model, false);
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
            CppDeclarationNode node = CppDeclarationNode.nodeFactory(member, model, false);
            if (node != null) {
                retValue.add(node);
            }
        }
        for (CsmFriend friend : cls.getFriends()) {
            CppDeclarationNode node = CppDeclarationNode.nodeFactory(friend, model, true);
            if (node != null) {
                retValue.add(node);
            }
        }
    }

    private void initEnum(CsmEnum cls, List<CppDeclarationNode> retValue) {
        for (CsmEnumerator en : cls.getEnumerators()) {
            CppDeclarationNode node = CppDeclarationNode.nodeFactory(en, model, false);
            if (node != null) {
                retValue.add(node);
            }
        }
    }
}