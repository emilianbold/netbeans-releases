/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ruby;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Collects hash names for parameters from rdocs.
 *
 * @author Erno Mononen
 */
final class HashNameAnalyzer {

    private static final Pattern CONFIGURATION_OPTIONS = Pattern.compile(".*(Configuration options|Supported options|=== Options|Options are:).*");
    // matches "<tt>:opt</tt>" and "[:opt]"
    private static final Pattern HASH_NAME = Pattern.compile(".*(?:<tt>|\\[):(\\w+)(?:</tt>|\\]).*");
    private final List<String> rdocs;
    private final String paramName;

    private HashNameAnalyzer(String paramName, List<String> rdocs) {
        this.paramName = paramName;
        this.rdocs = rdocs;
    }

    /**
     * Attempts to collect the names of the hash keys defined in the given {@code rdocs}.
     * <p/>
     * <i>Note: The format recognized by this is very Rails specific, in particular specific
     * to validate methods in {@code ActiveModel::Validations}.</i>.
     *
     * @param paramName the name param for which the collected hash names 
     *  get assigned.
     * 
     * @param rdocs RDocs of a method.
     * @return the hash names in the format expected by {@code RubyIndexerHelper}.
     */
    static String collect(String paramName, List<String> rdocs) {
        if (rdocs.isEmpty()) {
            return "";
        }
        HashNameAnalyzer analyzer = new HashNameAnalyzer(paramName, rdocs);
        return analyzer.collect();
    }

    private String collect() {
        List<String> result = new ArrayList<String>();
        boolean confOptsFound = false;
        for (String line : rdocs) {
            if (CONFIGURATION_OPTIONS.matcher(line).matches()) {
                confOptsFound = true;
                continue;
            }
            if (confOptsFound) {
                Matcher matcher = HASH_NAME.matcher(line);
                if (matcher.matches()) {
                    String name = matcher.group(1);
                    if (!result.contains(name)) {
                        result.add(matcher.group(1));
                    }
                }
            }
        }
        return toIndexerFormat(result);
    }

    private String toIndexerFormat(List<String> hashNames) {
        if (hashNames.isEmpty()) {
            return "";
        }
        StringBuilder result = new StringBuilder(paramName + "(=>");
        for (Iterator<String> it = hashNames.iterator(); it.hasNext();) {
            String hashName = it.next();
            result.append(hashName);
            if (it.hasNext()) {
                result.append("|");
            }
        }
        result.append(")");
        return result.toString();
    }
}
