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

package org.netbeans.modules.cnd.api.utils;

import org.openide.util.NbBundle;

public class ResourceFileFilter extends SourceFileFilter {
    private static String suffixes[] = {"gif", "jpg", "png", "htm", "html", "xml", "txt", "mk", "Makefile", "makefile"}; // NOI18N
    private static ResourceFileFilter instance = null;

    public static ResourceFileFilter getInstance() {
        if (instance == null)
            instance = new ResourceFileFilter();
        return instance;
    }

    public String getDescription() {
        return NbBundle.getMessage(SourceFileFilter.class, "FILECHOOSER_RESOURCE_FILEFILTER", getSuffixesAsString()); // NOI18N
    }
    
    @Override
    public String getSuffixesAsString() {
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < getSuffixes().length; i++) {
            if (i > 0)
                ret.append(" "); // NOI18N
            if (!getSuffixes()[i].equals("Makefile") && !getSuffixes()[i].equals("makefile")) // NOI18N
                ret.append(".");  // NOI18N
            ret.append(getSuffixes()[i]); // NOI18N
        }
        return ret.toString();
    }
    
    public String[] getSuffixes() {
        return suffixes;
    }
}
