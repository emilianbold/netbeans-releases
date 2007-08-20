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

package org.netbeans.modules.cnd.classview.model;

import java.util.*;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.classview.NameCache;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.openide.nodes.*;

/**
 * Misc static utilitiy functions
 * @author Vladimir Kvasihn
 */
public class CVUtil {
    private static final boolean showParamNames = getBoolean("cnd.classview.show-param-names", true); // NOI18N
    
    public static String getSignature(CsmFunction fun) {
	return NameCache.getString(CsmUtilities.getSignature(fun, showParamNames));
    }
        
    public static String getNamesapceDisplayName(CsmNamespace ns){
        String displayName = ns.getName();
        if (displayName.length() == 0) {
            displayName = ns.getQualifiedName();
            int scope = displayName.lastIndexOf("::"); // NOI18N
            if (scope != -1) {
                displayName = displayName.substring(scope + 2);
            }
            displayName = displayName.replace('<', ' ').replace('>', ' '); // NOI18N
        }
        return  NameCache.getString(displayName);
    }

    public static Node createLoadingRoot() {
        Children.Array children = new Children.SortedArray();
        children.add(new Node[] { createLoadingNode() });
        AbstractNode root = new AbstractNode(children);
        return root;
    }
    
    public static Node createLoadingNode() {
        BaseNode node = new LoadingNode();
        return node;
    }
    
    private static boolean getBoolean(String name, boolean result) {
        String text = System.getProperty(name);
        if( text != null ) {
            result = Boolean.parseBoolean(text);
        }
        return result;
    }
}
