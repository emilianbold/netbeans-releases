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

package org.netbeans.modules.cnd.api.utils;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import org.netbeans.modules.cnd.loaders.CCDataLoader;
import org.netbeans.modules.cnd.loaders.CDataLoader;
import org.netbeans.modules.cnd.loaders.FortranDataLoader;
import org.netbeans.modules.cnd.loaders.HDataLoader;
import org.openide.loaders.ExtensionList;
import org.openide.util.NbBundle;

public class AllSourceFileFilter extends SourceFileFilter {

    private static AllSourceFileFilter instance = null;
    private static String[] suffixes = null;

    public static AllSourceFileFilter getInstance() {
        if (instance == null)
            instance = new AllSourceFileFilter();
        return instance;
    }

    public String getDescription() {
        return NbBundle.getMessage(SourceFileFilter.class, "FILECHOOSER_All_SOURCES_FILEFILTER", getSuffixesAsString()); // NOI18N
    }
    
    
    public String[] getSuffixes() {
        if (suffixes == null)
            suffixes = getAllSuffixes();
        return suffixes;
    }
    
    public static String[] getAllSuffixes() {
        List suffixes = new ArrayList();
        addSuffices(suffixes, CCDataLoader.getInstance().getExtensions());
        addSuffices(suffixes, CDataLoader.getInstance().getExtensions());
        addSuffices(suffixes, HDataLoader.getInstance().getExtensions());
        addSuffices(suffixes, FortranDataLoader.getInstance().getExtensions());
        return (String[])suffixes.toArray(new String[suffixes.size()]);
    }
    
    private static void addSuffices(List suffixes, ExtensionList list) {
        for (Enumeration e = list.extensions(); e != null &&  e.hasMoreElements();) {
            String ex = (String) e.nextElement();
            suffixes.add(ex);
        }
    }

}
