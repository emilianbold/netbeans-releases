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

import java.util.Vector;
import org.netbeans.modules.cnd.loaders.CCFSrcLoader;
import org.netbeans.modules.cnd.loaders.HDataLoader;
import org.openide.util.NbBundle;

public class AllFileFilter extends SourceFileFilter {

    private static AllFileFilter instance = null;
    private static String[] suffixes = null;

    public static AllFileFilter getInstance() {
        if (instance == null)
            instance = new AllFileFilter();
        return instance;
    }

    public String getDescription() {
        return NbBundle.getMessage(SourceFileFilter.class, "FILECHOOSER_All_FILEFILTER", getSuffixesAsString());
    }
    
    
    public String[] getSuffixes() {
        if (suffixes == null)
            suffixes = getAllSuffixes();
        return suffixes;
    }
    
    public String getSuffixesAsString() {
        String suf = AllSourceFileFilter.getInstance().getSuffixesAsString();
        suf += ResourceFileFilter.getInstance().getSuffixesAsString();
        return suf;
    }
    
    public static String[] getAllSuffixes() {
        Vector suffixes = new Vector();
        addSuffices(suffixes, AllSourceFileFilter.getInstance().getSuffixes());
        addSuffices(suffixes, ResourceFileFilter.getInstance().getSuffixes());
        return (String[])suffixes.toArray(new String[suffixes.size()]);
    }
    
    private static void addSuffices(Vector suffixes, String[] suffixes2) {
        for (int i = 0; i < suffixes2.length; i++)
            suffixes.add(suffixes2[i]);
    }
}
