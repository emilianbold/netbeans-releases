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

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;


/**
 * Misc. (static) utility functions
 * @author Vladimir Kvashin
 */
public class Utils {
    
    public static final Logger LOG = Logger.getLogger("org.netbeans.modules.cnd.modelimpl"); // NOI18N
    private static final int LOG_LEVEL = Integer.getInteger("org.netbeans.modules.cnd.modelimpl.level", -1).intValue(); // NOI18N
    
    static {
        // command line param has priority for logging
        // do not change it
        if (LOG_LEVEL == -1) {
            // command line param has priority for logging
            if (TraceFlags.DEBUG) {
                LOG.setLevel(Level.ALL);
            } else {
                LOG.setLevel(Level.SEVERE);
            }
        }
    }
    
    public static String getQualifiedName(String name, CsmNamespace parent) {
	StringBuffer sb = new StringBuffer(name);
	while(parent != null ) {
	    if( ! parent.isGlobal() ) {
		sb.insert(0, "::"); // NOI18N
		sb.insert(0, parent.getName());
	    }
	    parent = parent.getParent();
	}
	return sb.toString();
    }
    
    public static String toString(String[] a) {
	StringBuffer sb = new StringBuffer("["); // NOI18N
	for (int i = 0; i < a.length; i++) {
	    if( i > 0 ) {
		sb.append(',');
	    }
	    sb.append(a[i]);
	}
	sb.append(']');
	return sb.toString();
    }
    
    public static String[] splitQualifiedName(String qualified) {
        List v = new ArrayList();
        for (StringTokenizer t = new StringTokenizer(qualified, ": \t\n\r\f", false); t.hasMoreTokens(); ) {// NOI18N 
            v.add(t.nextToken());
        }
        return (String[]) v.toArray(new String[v.size()]);
    }    
}
