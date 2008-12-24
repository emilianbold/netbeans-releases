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

package org.netbeans.modules.cnd.api.utils;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.cnd.utils.MIMEExtensions;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

public class AllSourceFileFilter extends SourceFileFilter {

    private static AllSourceFileFilter instance = null;
    private static String[] suffixes = null;

    public static AllSourceFileFilter getInstance() {
        if (instance == null) {
            instance = new AllSourceFileFilter();
        }
        return instance;
    }

    public String getDescription() {
        return NbBundle.getMessage(SourceFileFilter.class, "FILECHOOSER_All_SOURCES_FILEFILTER", getSuffixesAsString()); // NOI18N
    }

    @Override
    public boolean accept(File f) {
        if (FileUtil.getExtension(f.getPath()).length() == 0) {
            // could be header without extension
            return MIMENames.HEADER_MIME_TYPE.equals(MIMENames.getSourceMIMEType(f));
        } else {
            return super.accept(f);
        }
    }
    
    public String[] getSuffixes() {
        if (suffixes == null) {
            suffixes = getAllSuffixes();
        }
        return suffixes;
    }
    
    private String[] getAllSuffixes() {
        Set<String> allSuffixes = new HashSet<String>();
        allSuffixes.addAll(MIMEExtensions.get(MIMENames.CPLUSPLUS_MIME_TYPE).getValues());
        allSuffixes.addAll(MIMEExtensions.get(MIMENames.C_MIME_TYPE).getValues());
        allSuffixes.addAll(MIMEExtensions.get(MIMENames.HEADER_MIME_TYPE).getValues());
        allSuffixes.addAll(MIMEExtensions.get(MIMENames.FORTRAN_MIME_TYPE).getValues());
        return allSuffixes.toArray(new String[allSuffixes.size()]);
    }
}
