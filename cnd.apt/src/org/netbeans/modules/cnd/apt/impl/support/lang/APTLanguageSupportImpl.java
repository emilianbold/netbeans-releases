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

package org.netbeans.modules.cnd.apt.impl.support.lang;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.cnd.apt.support.APTLanguageFilter;
import org.netbeans.modules.cnd.apt.support.APTLanguageSupport;

/**
 * implementation of language filters collection
 * @author Vladimir Voskresensky
 */
public class APTLanguageSupportImpl {
    private static Map/*<String, APTLanguageFilter>*/ langFilters = new HashMap();
    
    private APTLanguageSupportImpl() {
    }
    
    public static APTLanguageFilter getFilter(String lang) {
        APTLanguageFilter filter = (APTLanguageFilter) langFilters.get(lang);
        if (filter == null) {
            filter = createFilter(lang);
        }
        return filter;
    }
    
    public static void addFilter(String lang, final APTLanguageFilter filter) {
        langFilters.put(lang, filter);
    }
    
    private static Map/*<String, APTLanguageFilter>*/ getFilters() {
        return langFilters;
    }

    private static APTLanguageFilter createFilter(String lang) {
        assert (getFilters().get(lang) == null);
        APTLanguageFilter filter = null;
        // Now support only few filters
        if (lang.equalsIgnoreCase(APTLanguageSupport.STD_C)) {
            filter = new APTStdCFilter();
        } else if (lang.equalsIgnoreCase(APTLanguageSupport.STD_CPP)) {
            filter = new APTStdCppFilter();
        } else if (lang.equalsIgnoreCase(APTLanguageSupport.GNU_C)) {
            filter = new APTGnuCFilter();
        } else if (lang.equalsIgnoreCase(APTLanguageSupport.GNU_CPP)) {
            filter = new APTGnuCppFilter();
        }
        if (filter != null) {
            addFilter(lang, filter);
        }
        return filter;
    }
}
