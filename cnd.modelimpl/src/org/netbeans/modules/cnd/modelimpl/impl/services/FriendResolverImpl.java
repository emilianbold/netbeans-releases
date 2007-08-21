/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.modelimpl.impl.services;

import java.util.Collection;
import java.util.Collections;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmFriend;
import org.netbeans.modules.cnd.api.model.CsmFriendClass;
import org.netbeans.modules.cnd.api.model.CsmFriendFunction;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.services.CsmFriendResolver;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;

/**
 *
 * @author Alexander Simon
 */
public class FriendResolverImpl extends CsmFriendResolver {
    
    /** Creates a new instance of FriendResolverImpl */
    public FriendResolverImpl() {
    }
    
    /**
     * checks if target has declared 'friendDecl' as friend declaration, i.e.
     *  class target {
     *      friend class friendDecl;
     *      friend void friendDecl();
     *  };
     *
     *  void friendDecl() {
     *  }
     *
     *  void friendDecl::foo() {
     *  }
     * @param friendDecl declaration to check (not null)
     * @param target class to check
     * @return true if 'friendDecl' is the declarated friend declaration of 'target'
     * @throws IllegalArgumentException if friendDecl is null
     */
    public boolean isFriend(CsmOffsetableDeclaration friendDecl, CsmClass target) {
        if (friendDecl == null) {
            throw new IllegalArgumentException("friendDecl must not be null");
        }
        CsmClass containingClass = null;
        if (CsmKindUtilities.isMethodDefinition(friendDecl)){
            CsmFunction decl = CsmBaseUtilities.getFunctionDeclaration((CsmFunction)friendDecl);
            containingClass = ((CsmMember)decl).getContainingClass();
        } else if (CsmKindUtilities.isMethodDeclaration(friendDecl)) {
            containingClass = ((CsmMember)friendDecl).getContainingClass();
        }
        for (CsmFriend friend : target.getFriends()){
            if (CsmKindUtilities.isFriendClass(friend)){
                CsmFriendClass cls = (CsmFriendClass) friend;
                CsmClass reference = cls.getReferencedClass() ;
                if (friendDecl.equals(reference)){
                    return true;
                }
                if (containingClass != null && containingClass.equals(reference)) {
                    return true;
                }
            } else if (CsmKindUtilities.isFriendMethod(friend)){
                if (friendDecl.equals(friend)) {
                    return true;
                }
                CsmFriendFunction fun = (CsmFriendFunction) friend;
                CsmFunction ref = fun.getReferencedFunction();
                if (friendDecl.equals(ref)) {
                    return true;
                }
                if (ref != null && CsmKindUtilities.isFunctionDefinition(ref)){
                    if (friendDecl.equals(((CsmFunctionDefinition)ref).getDeclaration())){
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    /**
     * return all friend declarations for declaration, i.e.
     *  class target {
     *      friend class friendClass;
     *      friend void friendMethod();
     *  };
     *  class friendClass{
     *  }
     *  void friendMethod(){
     *  }
     *
     * @return friend class declaration "friendClass" for class declaration "friendClass" or
     *         friend method declaration "friendMethod" for method definition "friendMethod"
     */
    public Collection<CsmFriend> findFriends(CsmOffsetableDeclaration decl) {
        CsmProject prj = decl.getContainingFile().getProject();
        if (prj instanceof ProjectBase) {
            return ((ProjectBase)prj).findFriendDeclarations(decl);
        }
        return Collections.<CsmFriend>emptyList();
    }
}
