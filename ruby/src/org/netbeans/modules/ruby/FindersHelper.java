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
import org.jrubyparser.ast.CallNode;
import org.jrubyparser.ast.Node;
import org.jrubyparser.ast.NodeType;
import org.jrubyparser.ast.SymbolNode;

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
     * The standard find method in AR:Base.
     */
    private static final String FIND ="find"; //NOI18N
    /**
     * Alias for find :all in AR:Base.
     */
    private static final String ALL ="all"; //NOI18N
    /**
     * The names of the default find methods.
     */
    private static final String[] STANDARD_FINDERS = {ALL, FIND}; //NOI18N
    /**
     * The max amount of dynamic finders to compute.
     */
    private static final int MAX_ITEMS = 2000;
    /**
     * The threshold for the column count after which we will compute only one
     * level of finders.
     */
    private static final int THRESHOLD = 100;

    private enum FinderType {

        FIND_BY("find_by_") {
            boolean isMultiple() { return false; }
        },
        FIND_ALL_BY("find_all_by_") {
            boolean isMultiple() { return true;}
        },
        FIND_LAST_BY("find_last_by_") {
            boolean isMultiple() { return false; }
        },
        SCOPED_BY("scoped_by_") {
            boolean isMultiple() {return true;}
            @Override boolean hasOptions() { return false;}
        };

        private final String prefix;

        FinderType(String prefix) {this.prefix = prefix; }

        abstract boolean isMultiple();
        String getPrefix() { return prefix; }
        boolean hasOptions() { return true; }
    }
    /**
     * Common finder method prefixes.
     */
    private final Collection<FinderType> prefixes;
    private final Collection<String> columns;
    /**
     * The max depth of finders to compute, i.e. the how many columns to combine.
     */
    private final int maxDepth;

    private FindersHelper(Collection<FinderType> prefixes, Collection<String> columns) {
        this.prefixes = prefixes;
        this.columns = columns;
        int size = columns.size();
        // compute the depth -- needs to be limited since the number of 
        // possible combinations grows exponentially (hence the use oflogarithm here).
        this.maxDepth = size > THRESHOLD ? 0 : (int) (Math.log(MAX_ITEMS) / Math.log(size));
    }

    // package private for unit tests
    static List<String> extractColumns(String method) {
        for (FinderType finder : FinderType.values()) {
            String prefix = finder.getPrefix();
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

    private static List<FinderType> matchingFinderPrefixes(String methodPrefix) {
        List<FinderType> result = new ArrayList<FinderType>(5);
        for (FinderType finder : FinderType.values()) {
            String finderPrefix = finder.getPrefix();
            if (methodPrefix.length() >= finderPrefix.length()) {
                if (finderPrefix.startsWith(methodPrefix.substring(0, finderPrefix.length()))) {
                    result.add(finder);
                }
            } else if (finderPrefix.startsWith(methodPrefix)) {
                result.add(finder);
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
        for (FinderType prefix : prefixes) {
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
            String base = root + ATTRIBUTE_SEPARATOR + o;
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

    static boolean isFinderMethod(String name) {
        return isFinderMethod(name, true);
    }

    static boolean isFinderMethod(String name, boolean includeStandardFinders) {
        for (FinderType each : FinderType.values()) {
            if (name.startsWith(each.getPrefix())) {
                return true;
            }
        }
        if (includeStandardFinders) {
            for (String each : STANDARD_FINDERS) {
                if (name.equals(each)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Look up the right return type for the given finder call.
     */
    static RubyType pickFinderType(final Node call, final String method, final RubyType model) {

        boolean multiple = false;
        boolean foundMatching = false;

        for (FinderType finder : FinderType.values()) {
            if (method.startsWith(finder.getPrefix())) {
                foundMatching = true;
                multiple = finder.isMultiple();
                break;
            }
        }
        // regular "find" (which is not a dynamic finder)
        if (!foundMatching && method.equals(FIND)) { // NOI18N
            // Finder method that does both - gotta inspect it
            List<Node> nodes = new ArrayList<Node>();
            AstUtilities.addNodesByType(call, new NodeType[]{NodeType.SYMBOLNODE}, nodes);
            boolean foundAll = false;
            for (Node n : nodes) {
                SymbolNode symbol = (SymbolNode) n;
                if ("all".equals(symbol.getName())) { // NOI18N
                    foundAll = true;
                    break;
                }
            }
            multiple = foundAll;
            foundMatching = true;
        } else if (!foundMatching && method.equals(ALL)) {
            multiple = true;
            foundMatching = true;
        } else if (!foundMatching) {
            // Not sure - probably some other locally defined finder method;
            // just default to the model name
            multiple = false;
        }

        if (multiple) {
            return RubyType.ARRAY;
        } else {
            return model;
        }
    }

    static class FinderMethod implements Comparable<FinderMethod> {

        private final FinderType finder;
        private final String attributes;

        public FinderMethod(FinderType prefix, String attributes) {
            this.finder = prefix;
            this.attributes = attributes;
        }

        public String getName() {
            return finder.getPrefix() + attributes;
        }

        public String getSignature() {
            StringBuilder result = new StringBuilder(finder.getPrefix() + attributes + "(");
            String[] params = attributes.split(ATTRIBUTE_SEPARATOR);
            for (int i = 0; i < params.length; i++) {
                String param = params[i];
                result.append(param);
                if (i < params.length - 1) {
                    result.append(", ");
                }
            }
            if (finder.hasOptions()) {
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
