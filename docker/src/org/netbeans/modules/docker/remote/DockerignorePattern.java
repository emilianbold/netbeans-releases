/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.docker.remote;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.PatternSyntaxException;
import org.openide.util.Pair;

/**
 *
 * @author Petr Hejl
 */
public class DockerignorePattern {

    private final List<Rule> rules;

    public DockerignorePattern(List<Rule> rules) {
        this.rules = rules;
    }

    public static DockerignorePattern compile(String goPattern, char separator) {
        List<Rule> ret = new ArrayList<>();
        char[] patternChars = goPattern.toCharArray();
        for (int i = 0; i < patternChars.length; i++) {
            char c = patternChars[i];
            switch (c) {
                case '*':
                    if (ret.isEmpty() || !(ret.get(ret.size() - 1) instanceof StarRule)) {
                        ret.add(new StarRule(separator));
                    }
                    break;
                case '?':
                    ret.add(new QuestionRule(separator));
                    break;
                case '[':
                    Pair<? extends Rule, Integer> p = createRange(patternChars, i, separator);
                    ret.add(p.first());
                    if (p.second() < 0) {
                        return new DockerignorePattern(ret);
                    }
                    i = p.second();
                    break;
                case '\\':
                    if (separator == '\\') {
                        ret.add(new CharacterRule(patternChars[i]));
                    } else {
                        if (i < patternChars.length - 1) {
                            ret.add(new CharacterRule(patternChars[++i]));
                        } else {
                            ret.add(new ErrorRule(goPattern, i));
                            return new DockerignorePattern(ret);
                        }
                    }
                    break;
                default:
                    ret.add(new CharacterRule(patternChars[i]));
                    break;
            }
        }
        return new DockerignorePattern(ret);
    }

    public boolean matches(String input) {
        return matches(rules, input);
    }

    boolean isError() {
        for (Rule r : rules) {
            if (r instanceof ErrorRule) {
                return true;
            }
        }
        return false;
    }

    private static boolean matches(List<Rule> rules, String input) {
        char[] inputChars = input.toCharArray();
        int i = 0;
        int listIndex = 0;
        for (Iterator<Rule> it = rules.iterator(); it.hasNext();) {
            Rule r = it.next();
            try {
                if (inputChars.length == 0) {
                    // star matches even empty string
                    return rules.size() == 1 && r.matchesEmpty();
                }
                int[] test = r.consume(inputChars, i);
                if (test == null) {
                    return false;
                }

                if (test.length == 1) {
                    i = test[0];
                } else if (listIndex == rules.size() - 1
                        && test[test.length - 1] >= input.length()) {
                    // last rule - take the longest one
                    i = test[test.length - 1];
                } else {
                    for (int j = test.length - 1; j >= 0; j--) {
                        if (matches(rules.subList(listIndex + 1, rules.size()), input.substring(test[j]))) {
                            return true;
                        }
                    }
                    return false;
                }
            } catch (PatternSyntaxException ex) {
                return false;
            }
            listIndex++;
            if (i >= inputChars.length) {
                if (!it.hasNext()) {
                    return true;
                } else {
                    return it.next().matchesEmpty();
                }
            }
        }
        return i >= inputChars.length;
    }

    private static Pair<? extends Rule, Integer> createRange(char[] chars, int offset, char separator) throws PatternSyntaxException {
        if (chars[offset] != '[' || offset >= chars.length - 1) {
            return Pair.of(new ErrorRule(new String(chars), offset), -1);
            //throw new PatternSyntaxException("Malformed range", new String(chars), offset);
        }

        boolean negated = false;
        int start = offset + 1;
        char first = chars[offset + 1];
        if (first == '^') {
            negated = true;
            start++;
        }

        if (start >= chars.length - 1) {
            return Pair.of(new ErrorRule(new String(chars), start), -1);
            //throw new PatternSyntaxException("Malformed range", new String(chars), start);
        }

        Character last = null;
        LinkedList<Character> singles = new LinkedList<>();
        List<Pair<Character, Character>> ranges = new LinkedList<>();
        boolean inRange = false;
        for (int i = start; i < chars.length; i++) {
            char c = chars[i];
            switch (c) {
                case '\\':
                    if (separator == '\\') {
                        char l = chars[i];
                        if (inRange) {
                            ranges.add(Pair.of(last, l));
                            inRange = false;
                            last = null;
                        } else {
                            last = l;
                            singles.add(l);
                        }
                    } else {
                        if (i < chars.length - 1) {
                            char l = chars[++i];
                            // XXX is backslash allowed in range ?
                            if (inRange) {
                                ranges.add(Pair.of(last, l));
                                inRange = false;
                                last = null;
                            } else {
                                last = l;
                                singles.add(l);
                            }
                        } else {
                            return Pair.of(new ErrorRule(new String(chars), i), -1);
                            //throw new PatternSyntaxException("Malformed range", new String(chars), i);
                        }
                    }
                    break;
                case ']':
                    if (inRange || i == start) {
                        return Pair.of(new ErrorRule(new String(chars), i), -1);
                        //throw new PatternSyntaxException("Malformed range", new String(chars), i);
                    }
                    return Pair.of(new RangeRule(negated, ranges, singles), i);
                case '-':
                    if (last == null) {
                        return Pair.of(new ErrorRule(new String(chars), i), -1);
                        //throw new PatternSyntaxException("Malformed range", new String(chars), i);
                    }
                    singles.removeLast();
                    inRange = true;
                    break;
                default:
                    char l = chars[i];
                    if (inRange) {
                        ranges.add(Pair.of(last, l));
                        inRange = false;
                        last = null;
                    } else {
                        last = l;
                        singles.add(l);
                    }
                    break;
            }
        }
        return Pair.of(new ErrorRule(new String(chars), chars.length - 1), -1);
        //throw new PatternSyntaxException("Malformed range", new String(chars), chars.length - 1);
    }

    private static interface Rule {

        int[] consume(char[] chars, int offset);

        boolean matchesEmpty();

    }

    private static class StarRule implements Rule {

        private final char separator;

        public StarRule(char separator) {
            this.separator = separator;
        }

        @Override
        public int[] consume(char[] chars, int offset) {
            if (offset >= chars.length) {
                throw new IllegalArgumentException();
            }

            int limit = -1;
            for (int i = offset; i < chars.length; i++) {
                if (chars[i] == separator) {
                    limit = i;
                    break;
                }
            }
            if (limit < 0) {
                limit = chars.length;
            }
            int[] ret = new int[limit - offset + 1];
            for (int i = 0; i < ret.length; i++) {
                ret[i] = offset + i;
            }
            return ret;
        }

        @Override
        public boolean matchesEmpty() {
            return true;
        }
    }

    private static class QuestionRule implements Rule {

        private final char separator;

        public QuestionRule(char separator) {
            this.separator = separator;
        }

        @Override
        public int[] consume(char[] chars, int offset) {
            if (offset >= chars.length) {
                throw new IllegalArgumentException();
            }
            if (chars[offset] == separator) {
                return null;
            }
            return new int[]{offset + 1};
        }

        @Override
        public boolean matchesEmpty() {
            return false;
        }
    }

    private static class RangeRule implements Rule {

        private final boolean negated;

        private final List<Pair<Character, Character>> ranges;

        private final List<Character> singles;

        public RangeRule(boolean negated, List<Pair<Character, Character>> ranges, List<Character> singles) {
            this.negated = negated;
            this.ranges = ranges;
            this.singles = singles;
        }

        @Override
        public int[] consume(char[] chars, int offset) {
            if (offset >= chars.length) {
                throw new IllegalArgumentException();
            }
            boolean ok = check(chars[offset]);
            if (negated) {
                ok = !ok;
            }
            if (!ok) {
                return null;
            }
            return new int[]{offset + 1};
        }

        @Override
        public boolean matchesEmpty() {
            return false;
        }

        private boolean check(char c) {
            for (Character s : singles) {
                if (s == c) {
                    return true;
                }
            }
            for (Pair<Character, Character> r : ranges) {
                if (r.first() <= c && c <= r.second()) {
                    return true;
                }
            }
            return false;
        }
    }

    private static class CharacterRule implements Rule {

        private final char c;

        public CharacterRule(char c) {
            this.c = c;
        }

        @Override
        public int[] consume(char[] chars, int offset) throws IllegalStateException {
            if (offset >= chars.length) {
                throw new IllegalArgumentException();
            }

            if (chars[offset] != c) {
                return null;
            }
            return new int[]{offset + 1};
        }

        @Override
        public boolean matchesEmpty() {
            return false;
        }
    }

    private static class ErrorRule implements Rule {

        private final String regex;

        private final int index;

        public ErrorRule(String regex, int index) {
            this.regex = regex;
            this.index = index;
        }

        @Override
        public int[] consume(char[] chars, int offset) {
            throw new PatternSyntaxException("Malformed pattern", regex, index);
        }

        @Override
        public boolean matchesEmpty() {
            return false;
        }
    }
}
