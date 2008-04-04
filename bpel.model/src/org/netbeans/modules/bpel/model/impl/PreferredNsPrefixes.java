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

package org.netbeans.modules.bpel.model.impl;

import java.util.HashMap;
import org.netbeans.modules.bpel.model.api.support.BpelXPathExtFunctionMetadata;
import org.netbeans.modules.bpel.model.ext.Extensions;

/**
 * Knows which prefixes are preferred for namespaces. 
 * 
 * @author nk160297
 */
public final class PreferredNsPrefixes {

    private static HashMap<String, String> uriToPrefixMap;

    /**
     * Returns either the preferred prefix or null.
     * @param nsUri
     * @return
     */
    public static synchronized String getPreferredPrefix(String nsUri) {
        if (uriToPrefixMap == null) {
            uriToPrefixMap = new HashMap<String, String>();
            //
            // Initialize the map
            uriToPrefixMap.put(BpelXPathExtFunctionMetadata.SUN_EXT_FUNC_NS, 
                    "sxxf"); // NOI18N
            
            uriToPrefixMap.put(Extensions.TRACE_EXT_URI, "sxt"); // NOI18N
            uriToPrefixMap.put(Extensions.ERROR_EXT_URI, "sxeh"); // NOI18N
            uriToPrefixMap.put(Extensions.TRANSACTION_EXT_URI, "sxtx"); // NOI18N
            uriToPrefixMap.put(Extensions.EDITOR_EXT_URI, "sxed"); // NOI18N
        }
        //
        String prefix = uriToPrefixMap.get(nsUri);
        return prefix;
    }
    
}

