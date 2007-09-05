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

package org.netbeans.modules.cnd.api.model.services;

import java.util.Collection;
import java.util.Collections;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmFriend;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.openide.util.Lookup;

/**
 * entry point to resolve friends of declarations
 * @author Vladimir Voskresensky
 */
public abstract class CsmFriendResolver {
    /** A dummy resolver that never returns any results.
     */
    private static final CsmFriendResolver EMPTY = new Empty();
    
    /** default instance */
    private static CsmFriendResolver defaultResolver;
    
    protected CsmFriendResolver() {
    }
    
    /** Static method to obtain the resolver.
     * @return the resolver
     */
    public static synchronized CsmFriendResolver getDefault() {
        if (defaultResolver != null) {
            return defaultResolver;
        }
        defaultResolver = Lookup.getDefault().lookup(CsmFriendResolver.class);
        return defaultResolver == null ? EMPTY : defaultResolver;
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
     *
     * @return true if 'friendDecl' is the declarated friend declaration of 'target'
     */
    public abstract boolean isFriend(CsmOffsetableDeclaration friendDecl, CsmClass target);
    
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
    public abstract Collection<CsmFriend> findFriends(CsmOffsetableDeclaration decl);

    //
    // Implementation of the default resolver
    //
    private static final class Empty extends CsmFriendResolver {
        Empty() {
        }
        public boolean isFriend(CsmOffsetableDeclaration friendDecl, CsmClass target) {
            return false;
        }

        public Collection<CsmFriend> findFriends(CsmOffsetableDeclaration decl) {
            return Collections.<CsmFriend>emptyList();
        }
    }    
}
