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

package org.netbeans.modules.cnd.api.model;

import org.netbeans.modules.cnd.api.model.util.TypeSafeEnum;

/**
 * Common ancestor for both fundamental (built-in) and compound types
 * @author Vladimir Kvashin
 */
public interface CsmClassifier<T> extends CsmDeclaration<T> {

//    class Kind extends TypeSafeEnum {
//
//        private Kind(String id) {
//            super(id);
//        }
//
//        public static final Kind BUILT_IN = new Kind("Built-in");
//        public static final Kind CLASS = new Kind("Class");
//        public static final Kind ENUM = new Kind("Enum");
//    }
//    
//    Kind getKind();
}
