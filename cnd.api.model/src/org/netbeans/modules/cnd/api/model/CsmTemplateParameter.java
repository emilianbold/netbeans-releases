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
 * Represent one template parameter
 * @author Vladimir Kvashin
 */
public interface CsmTemplateParameter extends CsmObject {

    class Kind extends TypeSafeEnum {

        private Kind(String id) {
            super(id);
        }

        public static final Kind DECLARATION = new Kind("DECLARATION"); // NOI18N
        public static final Kind CLASS = new Kind("CLASS"); // NOI18N
        public static final Kind TEMPLATE = new Kind("TEMPLATE"); // NOI18N
    }
    
    /** Gets this template parameter kin */
    Kind getKind();
    
    
    /** Gets this parameter text  */
    // TODO: perhaps we'd  better move this to some common interface
    String getText();
    
    
}

