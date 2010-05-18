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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.bpel.nodes;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0 
 */
public enum AnnotationType {
        BREAKPOINT,
        CURRENT_BREAKPOINT,
        DISABLED_BREAKPOINT,
        CURRENT_DISABLED_BREAKPOINT,
        CURRENT_POSITION;
        
        private static Map<String,AnnotationType> NAME_TYPE_MAP 
                = new HashMap<String,AnnotationType>();
        static {
            NAME_TYPE_MAP.put("BpelBreakpoint_normal", AnnotationType.BREAKPOINT); // NOI18N
            NAME_TYPE_MAP.put("CurrentPC", AnnotationType.CURRENT_POSITION); // NOI18N
            NAME_TYPE_MAP.put("BpelBreakpoint_disabled", AnnotationType.DISABLED_BREAKPOINT); // NOI18N
        }
        
        public static AnnotationType getAnnotationType(String type) {
            if (type == null) {
                return null;
            }
            return NAME_TYPE_MAP.get(type);
        }
}
