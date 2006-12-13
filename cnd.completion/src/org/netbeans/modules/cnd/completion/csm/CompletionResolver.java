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

package org.netbeans.modules.cnd.completion.csm;
import java.util.List;

/**
 * completion resolver for the file
 * file should be passed somehow in constructor
 * using of resolver:
 *  resolver = createResolver(file);
 * if reusing => resolver.refresh();
 *  if (resolver.resolve(...)) {
 *   result = resolver.getResult();
 *  }
 *
 * @author vv159170
 */
public interface CompletionResolver {
    // flags indicating what we plan to resolve using this resolver
    public static final int RESOLVE_NONE                   = 0;
    
    public static final int RESOLVE_CONTEXT                = 1 << 0;

    public static final int RESOLVE_CLASSES                = 1 << 1;
    
    public static final int RESOLVE_GLOB_VARIABLES         = 1 << 2;

    public static final int RESOLVE_GLOB_FUNCTIONS         = 1 << 3;

    public static final int RESOLVE_CLASS_FIELDS           = 1 << 4;

    public static final int RESOLVE_CLASS_METHODS          = 1 << 5;
    
    public static final int RESOLVE_LOCAL_VARIABLES        = 1 << 6;

    public static final int RESOLVE_FILE_LOCAL_VARIABLES   = 1 << 7;

    public static final int RESOLVE_LIB_CLASSES            = 1 << 8;
    
    public static final int RESOLVE_LIB_VARIABLES          = 1 << 9;

    public static final int RESOLVE_LIB_FUNCTIONS          = 1 << 10;
    
    public static final int RESOLVE_LIB_ENUMERATORS        = 1 << 11;
    
    public static final int RESOLVE_GLOB_ENUMERATORS       = 1 << 12;

    public static final int RESOLVE_FILE_MACROS            = 1 << 13;

    public static final int RESOLVE_GLOB_MACROS            = 1 << 14;
    
    public static final int RESOLVE_FUNCTIONS              = RESOLVE_GLOB_FUNCTIONS | RESOLVE_LIB_FUNCTIONS | RESOLVE_CLASS_METHODS;
    /**
     * specify what to resolve by this resolver
     */
    public void setResolveTypes(int resolveTypes);
    
    /**
     * init resolver before using
     * or reinit
     */
    public boolean refresh();

    /**
     * resolve code completion on specified position
     * items should start with specified prefix
     * or must exactly correspond to input prefix string
     */
    public boolean resolve(int offset, String strPrefix, boolean match);

    /**
     * get result of resolving
     */
    public List/*<CsmObject>*/ getResult();
}
