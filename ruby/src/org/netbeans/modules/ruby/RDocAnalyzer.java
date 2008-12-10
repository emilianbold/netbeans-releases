/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.ruby;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class RDocAnalyzer {

    private static final Map<String, String> COMMENT_TYPE_TO_REAL_TYPE = new HashMap<String, String>();

    static {
        COMMENT_TYPE_TO_REAL_TYPE.put("anArray", "Array"); // NOI18N
        COMMENT_TYPE_TO_REAL_TYPE.put("anEnumerat", "Enumeration"); // NOI18N
        COMMENT_TYPE_TO_REAL_TYPE.put("array", "Array"); // NOI18N
        COMMENT_TYPE_TO_REAL_TYPE.put("false", "FalseClass"); // NOI18N
        COMMENT_TYPE_TO_REAL_TYPE.put("fixnum", "Fixnum"); // NOI18N
        COMMENT_TYPE_TO_REAL_TYPE.put("float", "Float"); // NOI18N
        COMMENT_TYPE_TO_REAL_TYPE.put("integer", "Integer"); // NOI18N
        COMMENT_TYPE_TO_REAL_TYPE.put("mod", "Module"); // NOI18N
        COMMENT_TYPE_TO_REAL_TYPE.put("new_method", "UnboundMethod"); // NOI18N
        COMMENT_TYPE_TO_REAL_TYPE.put("new_str", "String"); // NOI18N
        COMMENT_TYPE_TO_REAL_TYPE.put("nil", "NilClass"); // NOI18N
        COMMENT_TYPE_TO_REAL_TYPE.put("numeric_result", "Numeric"); // NOI18N
        COMMENT_TYPE_TO_REAL_TYPE.put("obj", "Object"); // NOI18N
        COMMENT_TYPE_TO_REAL_TYPE.put("proc", "Proc"); // NOI18N
        COMMENT_TYPE_TO_REAL_TYPE.put("string", "String"); // NOI18N
        COMMENT_TYPE_TO_REAL_TYPE.put("str", "String"); // NOI18N
        COMMENT_TYPE_TO_REAL_TYPE.put("symbol", "Symbol"); // NOI18N
        COMMENT_TYPE_TO_REAL_TYPE.put("true", "TrueClass"); // NOI18N
        COMMENT_TYPE_TO_REAL_TYPE.put("unbound_method", "UnboundMethod"); // NOI18N
    }

    private final Set<String> types;

    private RDocAnalyzer() {
        this.types = new HashSet<String>(2);
    }

    static Set<? extends String> collectTypesFromComment(final List<? extends String> comment) {
        RDocAnalyzer rda = new RDocAnalyzer();
        for (String line : comment) {
            line = line.trim();
            if (!line.startsWith("#  ")) { // NOI18N
                break; // ignore other then the header
            }
            rda.parseTypeFromLine(line);
        }
        return rda.types;
    }

    private void parseTypeFromLine(String line) {
        int typeIndex = line.indexOf(" -> "); // NOI18N
        if (typeIndex == -1) {
            typeIndex = line.indexOf(" => "); // NOI18N
        }
        if (typeIndex == -1) {
            return;
        }
        String rawCommentTypes = line.substring(typeIndex + 4).trim();
        if (rawCommentTypes.length() == 0) {
            return;
        }
        String[] rawCommentTypes2 = rawCommentTypes.split("or"); // NOI18N
        for (String rawCommentType : rawCommentTypes2) {
            String[] commentTypes = rawCommentType.split(","); // NOI18N
            for (String commentType : commentTypes) {
                commentType = commentType.trim();
                if (commentType.length() > 0) {
                    addRealTypeForCommentType(commentType);
                }
            }
        }
    }

    private void addRealTypeForCommentType(final String commentType) {
        String realType = COMMENT_TYPE_TO_REAL_TYPE.get(commentType);
        if (realType == null && Character.isUpperCase(commentType.charAt(0))) {
            // not in the map, but might be right type, e.g. String, Fixnum, ...
            // TODO: check whether such type exist
            realType = commentType;
        }
        if (realType != null) {
            types.add(realType);
        } /* TODO: uncomment else block and run the RDocAnalyzerTest, try to
         handle the unknown types like literals:
           * 1, -1, ...
           * "str"
           * [1, 2, 3]
         as well */
//         else {
//            System.err.println("Unknown real Ruby type for comment type: " + commentType);
//        }
    }
}
