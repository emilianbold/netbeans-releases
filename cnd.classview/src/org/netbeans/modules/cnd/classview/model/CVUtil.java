/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.cnd.classview.model;

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
    
    public static CharSequence getSignature(CsmFunction fun) {
	return NameCache.getManager().getString(CsmUtilities.getSignature(fun, showParamNames));
    }
        
    public static CharSequence getNamespaceDisplayName(CsmNamespace ns){
        String displayName = ns.getName().toString();
        if (displayName.length() == 0) {
            displayName = ns.getQualifiedName().toString();
            int scope = displayName.lastIndexOf("::"); // NOI18N
            if (scope != -1) {
                displayName = displayName.substring(scope + 2);
            }
            displayName = displayName.replace('<', ' ').replace('>', ' '); // NOI18N
        }
        return  NameCache.getManager().getString(displayName);
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
