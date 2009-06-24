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

package org.netbeans.modules.jira.issue;

import org.netbeans.modules.bugtracking.spi.IssueFinder;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 *
 * @author Marian Petras
 */
@ServiceProviders({@ServiceProvider(service=IssueFinder.class),
                   @ServiceProvider(service=JiraIssueFinder.class)})
public class JiraIssueFinder extends IssueFinder {

    private static final int[] EMPTY_INT_ARR = new int[0];

    public int[] getIssueSpans(String text) {
        int[] result = findBoundaries(text);
        return (result != null) ? result : EMPTY_INT_ARR;
    }

    public String getIssueId(String issueHyperlinkText) {
        int pos = issueHyperlinkText.length() - 1;

        assert Impl.isDigit(issueHyperlinkText.charAt(pos));
        do {
            assert pos >= 3;
            pos--;
        } while (Impl.isDigit(issueHyperlinkText.charAt(pos)));

        assert pos >= 2;                        //position of the dash character
        assert issueHyperlinkText.charAt(pos) == '-';
        pos--;              //skip the hyphen
        assert pos >= 1;
        assert Impl.isUppercaseAscii(issueHyperlinkText.charAt(pos));
        pos--;              //skip the last charcter left of hyphen
        assert pos >= 0;
        assert Impl.isUppercaseAscii(issueHyperlinkText.charAt(pos));
        do {
            pos--;          //skip remaining part of sequence of uppercase chars
        } while ((pos >= 0) && Impl.isUppercaseAscii(issueHyperlinkText.charAt(pos)));

        return issueHyperlinkText.substring(pos + 1);
    }

    private static int[] findBoundaries(String str) {
        return getImpl().findBoundaries(str);
    }

    private static Impl getImpl() {
        return new Impl();
    }

    static JiraIssueFinder getTestInstance() {
        return new JiraIssueFinder();
    }

    //--------------------------------------------------------------------------

    private static final class Impl {

        private static final String[] BUGWORDS = new String[] {"bug", "issue",  //NOI18N
                                                               "Bug", "Issue",  //NOI18N
                                                               "BUG", "ISSUE"}; //NOI18N

        private static final String PUNCT_CHARS = ",:;()[]{}";          //NOI18N

        private static final int INIT       = 0;
        private static final int ID_CHARS   = 1;
        private static final int CHARS      = 2;
        private static final int HASH       = 3;
        private static final int HASH_SPC   = 4;
        private static final int NUM        = 5;
        private static final int BUGWORD    = 6;
        private static final int BUGWORD_NL = 7;
        private static final int DASH       = 8;
        private static final int STAR       = 9;
        private static final int GARBAGE    = 10;

        private String str;
        private int pos;
        private int state;

        int start;
        int end;
        int bugIdStart;
        int[] result;

        private Impl() { }

        private int[] findBoundaries(String str) {
            reset();

            this.str = str;

            for (pos = 0; pos < str.length(); pos++) {
                handleChar(str.charAt(pos));
            }
            if (state == NUM) {
                storeResult(start, pos);
            }
            return result;
        }

        private void reset() {
            str = null;
            pos = 0;
            state = INIT;

            start = -1;
            end = -1;
            bugIdStart = -1;

            result = null;
        }

        private void handleChar(int c) {
            int newState;
            switch (state) {
                case INIT:
                    if (c == '#') {
                        rememberIsStart();
                        newState = HASH;
                    } else if (isUppercaseAscii(c)) {
                        rememberIsStart();
                        newState = ID_CHARS;
                    } else if (Character.isLetter(c)) {
                        rememberIsStart();
                        newState = CHARS;
                    } else {
                        newState = getInitialState(c);
                    }
                    break;
                case ID_CHARS:
                    if (isUppercaseAscii(c)) {
                        newState = ID_CHARS;
                    } else if (Character.isLetter(c)) {
                        newState = CHARS;
                    } else if (c == '-') {
                        if (checkIdIsAtLeastTwoCharsLong()) {
                            newState = DASH;
                        } else {
                            newState = getInitialState(c);
                        }
                    } else if ((c == ' ') || (c == '\t') || (c == '\r') || (c == '\n')) {
                        if (isBugword()) {
                            newState = ((c == ' ') || (c == '\t')) ? BUGWORD
                                                                   : BUGWORD_NL;
                        } else {
                            newState = getInitialState(c);
                        }
                    } else {
                        newState = getInitialState(c);
                    }
                    break;
                case CHARS:
                    if (Character.isLetter(c)) {
                        newState = CHARS;
                    } else if ((c == ' ') || (c == '\t') || (c == '\r') || (c == '\n')) {
                        if (isBugword()) {
                            newState = ((c == ' ') || (c == '\t')) ? BUGWORD
                                                                   : BUGWORD_NL;
                        } else {
                            newState = getInitialState(c);
                        }
                    } else {
                        newState = getInitialState(c);
                    }
                    break;
                case HASH:
                    if ((c == ' ') || (c == '\t')) {
                        newState = HASH_SPC;
                    } else if (isUppercaseAscii(c)) {
                        newState = ID_CHARS;
                    } else {
                        newState = getInitialState(c);
                    }
                    break;
                case HASH_SPC:
                    if ((c == ' ') || (c == '\t')) {
                        newState = HASH_SPC;
                    } else if (isUppercaseAscii(c)) {
                        newState = ID_CHARS;
                    } else if (Character.isLetter(c)) {
                        newState = CHARS;
                    } else {
                        newState = getInitialState(c);
                    }
                    break;
                case NUM:
                    if (isDigit(c)) {
                        newState = NUM;
                    } else {
                        newState = getInitialState(c);
                    }
                    break;
                case BUGWORD:
                    if ((c == ' ') || (c == '\t')) {
                        newState = BUGWORD;
                    } else if ((c == '\r') || (c == '\n')) {
                        newState = BUGWORD_NL;
                    } else if (c == '#') {
                        newState = HASH;
                    } else if (isUppercaseAscii(c)) {
                        newState = ID_CHARS;
                    } else {
                        newState = getInitialState(c);
                    }
                    break;
                case BUGWORD_NL:
                    if ((c == '\r') || (c == '\n') || (c == ' ') || (c == '\t')) {
                        newState = BUGWORD_NL;
                    } else if (c == '*') {
                        newState = STAR;
                    } else if (c == '#') {
                        newState = HASH;
                    } else if (isUppercaseAscii(c)) {
                        newState = ID_CHARS;
                    } else {
                        newState = getInitialState(c);
                    }
                    break;
                case DASH:
                    if (c == '#') {
                        rememberIsStart();
                        newState = HASH;
                    } else if (isDigit(c)) {
                        newState = NUM;
                    } else if (isUppercaseAscii(c)) {
                        rememberIsStart();
                        newState = ID_CHARS;
                    } else if (Character.isLetter(c)) {
                        rememberIsStart();
                        newState = CHARS;
                    } else {
                        newState = INIT;
                    }
                    break;
                case STAR:
                    if ((c == ' ') || (c == '\t')) {
                        newState = BUGWORD;
                    } else if ((c == '\r') || (c == '\n')) {
                        newState = BUGWORD_NL;
                    } else {
                        newState = getInitialState(c);
                    }
                    break;
                case GARBAGE:
                    newState = getInitialState(c);
                    break;
                default:
                    assert false;
                    newState = getInitialState(c);
                    break;
            }
            if ((state == NUM) && (newState != NUM)) {
                if (isSpaceOrPunct(c)) {
                    storeResult(start, pos);
                }
            }
            if ((newState == INIT) || (newState == GARBAGE)) {
                start = -1;
            }
            if (newState != ID_CHARS) {
                bugIdStart = -1;
            }
            if ((newState == ID_CHARS) && (state != ID_CHARS)) {
                bugIdStart = pos;
            }
            state = newState;
        }

        private int getInitialState(int c) {
            return isSpaceOrPunct(c) ? INIT : GARBAGE;
        }

        private void rememberIsStart() {
            start = pos;
        }

        private void storeResult(int start, int end) {
            assert (start != -1);
            if (result == null) {
                result = new int[] {start, end};
            } else {
                int[] newResult = new int[result.length + 2];
                System.arraycopy(result, 0, newResult, 0, result.length);
                newResult[result.length    ] = start;
                newResult[result.length + 1] = end;
                result = newResult;
            }
        }

        private static boolean isDigit(int c) {
            return ((c >= '0') && (c <= '9'));
        }

        private static boolean isUppercaseAscii(int c) {
            return (c >= 'A') && (c <= 'Z');
        }

        private static boolean isSpaceOrPunct(int c) {
            return (c == '\r') || (c == '\n')
                   || Character.isSpaceChar(c) || isPunct(c);
        }

        private static boolean isPunct(int c) {
            return PUNCT_CHARS.indexOf(c) != -1;
        }

        private boolean checkIdIsAtLeastTwoCharsLong() {
            return (pos - bugIdStart) >= 2;
        }

        private boolean isBugword() {
            String word = str.substring(start, pos);
            for (int i = 0; i < BUGWORDS.length; i++) {
                if (word.equals(BUGWORDS[i])) {
                    return true;
                }
            }
            return false;
        }

    }

}
