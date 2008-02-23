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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.languages.php;

import java.io.IOException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.gsf.api.Index;
import org.netbeans.modules.gsf.api.Index.SearchResult;
import org.netbeans.modules.gsf.api.Index.SearchScope;
import org.netbeans.modules.gsf.api.NameKind;
import org.openide.util.Exceptions;

/**
 *
 * @author tor
 */
public class PhpQuery {
    static final Set<SearchScope> ALL_SCOPE = EnumSet.allOf(SearchScope.class);
    private Index index;
    
    public static PhpQuery get(Index index) {
        return new PhpQuery(index);
    }

    
    private PhpQuery(Index index) {
        this.index = index;
    }
    
    private boolean search(String key, String name, NameKind kind, Set<SearchResult> result) {
        try {
            index.gsfSearch(key, name, kind, ALL_SCOPE, result);

            return true;
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);

            return false;
        }
    }
    
    public Set<String> getFunctions(String name, NameKind kind) {
        final Set<SearchResult> result = new HashSet<SearchResult>();

        String field = "func";

        search(field, name, kind, result);

        Set<String> functions = new HashSet<String>();
        for (SearchResult map : result) {
            String[] signatures = map.getValues(field);

            if (signatures != null) {
                for (String signature : signatures) {
                    // Lucene returns some inexact matches, TODO investigate why this is necessary
                    if (kind == NameKind.PREFIX && !signature.startsWith(name)) {
                        continue;
                    } else if (kind == NameKind.CASE_INSENSITIVE_PREFIX && !signature.regionMatches(true, 0, name, 0, name.length())) {
                        continue;
                    }
                    assert map != null;
                    
                    functions.add(signature);
                }
            }
        }

        return functions;
    }
}
