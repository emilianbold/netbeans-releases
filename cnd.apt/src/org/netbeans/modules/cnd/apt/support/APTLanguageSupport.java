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

package org.netbeans.modules.cnd.apt.support;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.cnd.apt.impl.support.lang.APTGnuCFilter;
import org.netbeans.modules.cnd.apt.impl.support.lang.APTGnuCppFilter;
import org.netbeans.modules.cnd.apt.impl.support.lang.APTLanguageSupportImpl;
import org.netbeans.modules.cnd.apt.impl.support.lang.APTStdCFilter;

/**
 * support for languages:
 *  - filters collection
 * @author Vladimir Voskresensky
 */
public class APTLanguageSupport {
    private static APTLanguageSupport singleton = new APTLanguageSupport();

    public static final String STD_C    = "Std C Language"; // NOI18N
    public static final String GNU_C    = "Gnu C Language"; // NOI18N
    public static final String GNU_CPP  = "Gnu C++ Language"; // NOI18N
    public static final String STD_CPP  = "Std C++ Language"; // NOI18N
    
    private APTLanguageSupport() {
    }
    
    public static APTLanguageSupport getInstance() {
        return singleton;
    }
    
    public APTLanguageFilter getFilter(String lang) {
        return APTLanguageSupportImpl.getFilter(lang);
    }
    
    public void addFilter(String lang, final APTLanguageFilter filter) {
        APTLanguageSupportImpl.addFilter(lang, filter);
    }
}
