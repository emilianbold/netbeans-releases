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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.modelimpl.csm.core;

import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;

/**
 * Creates an instance of appropriate resolver
 * and delegates work to it
 * @author vk155633
 */
public class ResolverFactory {

    //private static final boolean useNewResolver = Boolean.getBoolean("cnd.modelimpl.resolver2");
    public static final int resolver = Integer.getInteger("cnd.modelimpl.resolver", 3).intValue(); // NOI18N

    /** prevents creation */
    private ResolverFactory() {
    }
    
    public static Resolver createResolver(CsmOffsetable context) {
//        if (useNewResolver)
//            return new Resolver2(context);
//        else
//            return new Resolver3(context);
        switch( resolver ) {
            case 1: 
                return new Resolver1(context);
            case 2: 
                return new Resolver2(context);
            case 3: 
                return new Resolver3(context);
            default:
                return new Resolver1(context);
        }
    }

    public static Resolver createResolver(CsmFile file, int offset) {
//        if (useNewResolver)
//            return new Resolver2(file, offset);
//        else
//            return new Resolver3(file, offset);
        switch( resolver ) {
            case 1: 
                return new Resolver1(file, offset);
            case 2: 
                return new Resolver2(file, offset);
            case 3: 
                return new Resolver3(file, offset);
            default:
                return new Resolver1(file, offset);
        }
    }
    
}
