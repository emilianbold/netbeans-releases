/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ruby;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * A helper class for generating signatures for various ActiveRecord
 * dynamic finder methods.
 *
 * @author Erno Mononen
 */
final class FindersHelper {

    private static final String ATTRIBUTE_SEPARATOR_BASE = "_and"; //NOI18N
    private static final String ATTRIBUTE_SEPARATOR = ATTRIBUTE_SEPARATOR_BASE + "_"; //NOI18N

    /**
     * The max amount of dynamic finders to compute.
     */
    private static final int MAX_ITEMS = 2000;
    /**
     * The threshold for the column count after which we will compute only one
     * level of finders.
     */
    private static final int THRESHOLD = 100;

    /**
     * The prefix for dynamic scope methods.
     */
    static final String SCOPED_BY = "scoped_by_"; //NOI18N
    /**
     * Common finder method prefixes.
     */
    static final List<String> FINDER_PREFIXES = Arrays.asList("find_by_", "find_all_by_", "find_last_by_", SCOPED_BY); //NOI18N

    private final Collection<String> prefixes;
    private final Collection<String> columns;
    /**
     * The max depth of finders to compute, i.e. the how many columns to combine.
     */
    private final int maxDepth;


    private FindersHelper(Collection<String> prefixes, Collection<String> columns) {
        this.prefixes = prefixes;
        this.columns = columns;
        int size = columns.size();
        // compute the depth -- needs to be limited since the number of 
        // possible combinations grows exponentially (hence the use oflogarithm here).
        this.maxDepth = size > THRESHOLD ? 0 : (int) (Math.log(MAX_ITEMS)/Math.log(size));
    }

    // package private for unit tests
    static List<String> extractColumns(String method) {
        for (String prefix : FINDER_PREFIXES) {
            int prefixIdx = method.indexOf(prefix);
            if (prefixIdx != -1) {
                String woPrefix = method.substring(prefix.length());
                if (woPrefix.endsWith(ATTRIBUTE_SEPARATOR_BASE)) {
                    woPrefix = woPrefix.substring(0, woPrefix.length() - ATTRIBUTE_SEPARATOR_BASE.length());
                }
                return Arrays.asList(woPrefix.split(ATTRIBUTE_SEPARATOR));
            }
        }
        return Collections.<String>emptyList();
    }

    /**
     * Gets all the possible finder attribute combinations for the given 
     * <code>prefix</code> and <code>columns</code>,
     * e.g. for columns 'name' and 'price' this would return "name",
     * "name_and_price", "price", "price_and_name".
     *
     * @param columns
     * @param prefix the prefix of the method.
     * @return
     */
    static List<FinderMethod> getFinderSignatures(String prefix, Collection<String> columns) {
        Set<String> columnsCopy = new HashSet<String>(columns);
        Collection<String> existingColumns = extractColumns(prefix);
        columnsCopy.removeAll(existingColumns);
        FindersHelper helper = new FindersHelper(matchingFinderPrefixes(prefix), columns);
        return helper.computeSignatures();
    }

    private static List<String> matchingFinderPrefixes(String methodPrefix) {
        List<String> result = new ArrayList<String>(4);
        for (String finderPrefix : FINDER_PREFIXES) {
            if (methodPrefix.length() >= finderPrefix.length()) {
                if (finderPrefix.startsWith(methodPrefix.substring(0, finderPrefix.length()))) {
                    result.add(finderPrefix);
                }
            } else if (finderPrefix.startsWith(methodPrefix)) {
                result.add(finderPrefix);
            }
        }
        return result;
    }

    private List<FinderMethod> computeSignatures() {
        Set<String> combinations = new HashSet<String>();
        for (String baseColumn : columns) {
            combinations.add(baseColumn);
            Set<String> copy = new HashSet<String>(columns);
            copy.remove(baseColumn);
            addCombinations(baseColumn, copy, combinations, 0);
        }

        List<FinderMethod> result = new ArrayList<FinderMethod>(combinations.size());
        for (String prefix : prefixes) {
            for (Iterator<String> it = combinations.iterator(); it.hasNext();) {
                result.add(new FinderMethod(prefix, it.next()));
            }
        }
        Collections.sort(result);
        return result;
    }

    private void addCombinations(String root, Set<String> others, Set<String> result, int depth) {
        if (depth >= maxDepth) {
            return;
        }
        for (String o : others) {
            String base = root + ATTRIBUTE_SEPARATOR + o; //NOI18N
            result.add(base);
            Set<String> rest = new HashSet<String>(others);
            rest.remove(o);
            addCombinations(base, rest, result, depth + 1);
        }
    }

    static int nextAttributeLocation(String finderMethodName, int fromIndex) {
        return finderMethodName.indexOf(ATTRIBUTE_SEPARATOR, fromIndex);
    }

    static String subToNextAttribute(String finderMethodName, int attributeSeparatorIndex) {
        return finderMethodName.substring(0, attributeSeparatorIndex + ATTRIBUTE_SEPARATOR.length() - 1);
    }

    static class FinderMethod implements Comparable<FinderMethod> {

        private final String prefix;
        private final String attributes;

        public FinderMethod(String prefix, String attributes) {
            this.prefix = prefix;
            this.attributes = attributes;
        }

        public String getName() {
            return prefix + attributes;
        }

        public String getSignature() {
            StringBuilder result = new StringBuilder(prefix + attributes + "(");
            String[] params = attributes.split(ATTRIBUTE_SEPARATOR);
            for (int i = 0; i < params.length; i++) {
                String param = params[i];
                result.append(param);
                if (i < params.length - 1) {
                    result.append(", ");
                }
            }
            if (!SCOPED_BY.equals(prefix)) {
                // no options for dynamic scopes
                result.append(", *options");
            }
            result.append(")");
            return result.toString();

        }

        public String getColumn() {
            // the primary column
            int andIndex = attributes.indexOf(ATTRIBUTE_SEPARATOR);
            if (andIndex == -1) {
                return attributes;
            }
            return attributes.substring(0, andIndex);
        }

        public int compareTo(FinderMethod o) {
            return getName().compareTo(o.getName());
        }
    }
}
