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

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles pluralizing/singularizing words Rails style
 * -- see <tt>activesupport/lib/active_support/inflections.rb</tt>. This
 * is basically a poor man's simplified Java port of the Rails equivalent.
 * <p/>
 * See <a href="http://api.rubyonrails.org/classes/Inflector.html">Rails Inflector</a>
 * for the original implementation that this class tries to mimic.
 *
 * @todo move also all the others related methods from RubyUtils to here (tableize etc..)
 *
 * @author Erno Mononen
 */
final class Inflector {

    private final Set<String> uncountables = new HashSet<String>();
    private final Map<String, String> irregulars = new LinkedHashMap<String, String>();
    private final Map<Pattern, String> plurals = new LinkedHashMap<Pattern, String>();
    private final Map<Pattern, String> singulars = new LinkedHashMap<Pattern, String>();

    private static final Inflector INSTANCE = new Inflector();
    
    public static Inflector getDefault() {
        return INSTANCE;
    }
    
    private Inflector() {
        init();
    }

    /**
     * Gets the plural form of the given word.
     *
     * @param word
     * @return
     */
    public String pluralize(final String word) {
        if (isEmpty(word)) {
            return word;
        }

        String lowerCaseWord = word.toLowerCase(Locale.ENGLISH);
        if (uncountables.contains(lowerCaseWord)) {
            return word;
        }
        if (irregulars.containsKey(lowerCaseWord)) {
            return irregulars.get(lowerCaseWord);
        }
        for (Pattern p : plurals.keySet()) {
            Matcher m = p.matcher(word);
            if (m.find()) {
                return m.replaceAll(plurals.get(p));
            }
        }
        return word;
    }

    /**
     * Gets the singular form of the given word.
     * 
     * @param word
     * @return
     */
    public String singularize(final String word) {
        if (isEmpty(word)) {
            return word;
        }

        String lowerCaseWord = word.toLowerCase(Locale.ENGLISH);
        if (uncountables.contains(lowerCaseWord)) {
            return word;
        }
        if (irregulars.containsValue(lowerCaseWord)) {
            for (Map.Entry<String, String> entry : irregulars.entrySet()){
                if (entry.getValue().equals(lowerCaseWord)) {
                    return entry.getKey();
                }
            }
        }
        for (Pattern p : singulars.keySet()) {
            Matcher m = p.matcher(word);
            if (m.find()) {
                return m.replaceAll(singulars.get(p));
            }
        }
        return word;
    }

    private boolean isEmpty(String str) {
        return str == null || "".equals(str.trim());
    }
    private void plural(String regex, String replacement) {
        plurals.put(compile(regex), replacement);
    }

    private void singular(String regex, String replacement) {
        singulars.put(compile(regex), replacement);
    }

    private void irregular(String singular, String plural) {
        irregulars.put(singular, plural);
    }

    private Pattern compile(String regex) {
        // case insensitive
        return Pattern.compile("(?i)" + regex);
    }

    void uncountable(String... uncountable) {
        for (String each : uncountable) {
            uncountables.add(each);
        }
    }

    private void init() {
        plural("(ax|test)is$", "$1es");
        plural("(octop|vir)us$", "$1i");
        plural("(alias|status)$", "$1es");
        plural("(bu)s$", "$1ses");
        plural("(buffal|tomat)o$", "$1oes");
        plural("([ti])um$", "$1a");
        plural("sis$", "ses");
        plural("(?:([^f])fe|([lr])f)$", "$1$2ves");
        plural("(hive)$", "$1s");
        plural("([^aeiouy]|qu)y$", "$1ies");
        plural("(matr|vert|ind)ix|ex$", "$1ices");
        plural("(x|ch|ss|sh)$", "$1es");
        plural("([m|l])ouse$", "$1ice");
        plural("^(ox)$", "$1en");
        plural("(quiz)$", "$1zes");
        plural("(.*p)erson$", "$1eople");
        plural("(.*c)riterion$", "$1riteria");
        plural("(.*m)an$", "$1en");
        plural("s$", "s");
        plural("$", "s");

        singular("(ax|test)es$", "$1is");
        singular("(n)ews$", "$1ews");
        singular("([ti])a$", "$1um");
        singular("((a)naly|(b)a|(d)iagno|(p)arenthe|(p)rogno|(s)ynop|(t)he)ses$", "$1$2sis");
        singular("(^analy)ses$", "$1sis");
        singular("([^f])ves$", "$1fe");
        singular("(hive)s$", "$1");
        singular("(tive)s$", "$1");
        singular("([lr])ves$", "$1f");
        singular("([^aeiouy]|qu)ies$", "$1y");
        singular("(s)eries$", "$1eries");
        singular("(m)ovies$", "$1ovie");
        singular("(x|ch|ss|sh)es$", "$1");
        singular("([m|l])ice$", "$1ouse");
        singular("(bus)es$", "$1");
        singular("(o)es$", "$1");
        singular("(shoe)s$", "$1");
        singular("(cris|ax|test)es$", "$1is");
        singular("(octop|vir)i$", "$1us");
        singular("(octop|vir)us$", "$1us");
        singular("(alias|status)es$", "$1");
        singular("^(ox)en", "$1");
        singular("(vert|ind)ices$", "$1ex");
        singular("(matr)ices$", "$1ix");
        singular("(quiz)zes$", "$1");
        singular("(database)s$", "$1");
        singular("(.*p)eople$", "$1erson");
        plural("(.*c)riteria$", "$1riterion");
        singular("(.*m)en$", "$1an");
        singular("s$", "");

        irregular("person", "people");
        irregular("man", "men");
        irregular("child", "children");
        irregular("sex", "sexes");
        irregular("move", "moves");
        irregular("cow", "kine");
        irregular("criterion", "criteria");

        uncountable("equipment", "information", "rice", "money", "species", "series", "fish", "sheep");
    }
}

