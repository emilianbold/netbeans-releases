/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.cnd.apt.impl.support.lang;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.cnd.apt.support.APTLanguageFilter;
import org.netbeans.modules.cnd.apt.support.APTLanguageSupport;
import org.netbeans.modules.cnd.apt.utils.APTUtils;

/**
 * implementation of language filters collection
 * @author Vladimir Voskresensky
 */
public class APTLanguageSupportImpl {
    private static Map<String, APTLanguageFilter> langFilters = new HashMap<String, APTLanguageFilter>();
    
    private APTLanguageSupportImpl() {
    }
    
    public static APTLanguageFilter getFilter(String lang) {
        // no sync is needed here
        APTLanguageFilter filter = langFilters.get(lang);
        if (filter == null) {
            filter = createFilter(lang);
            if (filter != null) {
                addFilter(lang, filter);
            }
        }
        return filter;
    }
    
    public static void addFilter(String lang, final APTLanguageFilter filter) {
        langFilters.put(lang, filter);
    }

    private static APTLanguageFilter createFilter(String lang) {
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
        } else if (lang.equalsIgnoreCase(APTLanguageSupport.FORTRAN)) {
            filter = new APTFortranFilter();
        } else {
            APTUtils.LOG.warning("unsupported language " + lang); // NOI18N
        }
        return filter;
    }
}
