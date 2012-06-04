/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.jira.issue;

import org.netbeans.modules.bugtracking.spi.IssueFinder;
import org.openide.ErrorManager;

/**
 *
 * @author Marian Petras
 */
public class JiraIssueFinder extends IssueFinder {

    private static JiraIssueFinder instance;
    private static final int[] EMPTY_INT_ARR = new int[0];

    private JiraIssueFinder() {}


    public synchronized static IssueFinder getInstance() {
        if(instance == null) {
            instance = new JiraIssueFinder();
        }
        return instance;
    }

    @Override
    public int[] getIssueSpans(CharSequence text) {
        int[] result = findBoundaries(text);
        return (result != null) ? result : EMPTY_INT_ARR;
    }

    @Override
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
        assert Impl.isPrjKeyChar(issueHyperlinkText.charAt(pos));
        do {
            pos--;          //skip the project key
        } while ((pos >= 0) && Impl.isPrjKeyChar(issueHyperlinkText.charAt(pos)));

        pos++;  //jump back to the last matching character
        if (issueHyperlinkText.charAt(pos) == '#') {
            pos++;
        }

        return issueHyperlinkText.substring(pos);
    }

    private static int[] findBoundaries(CharSequence str) {
        try {
            return getImpl().findBoundaries(str);
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
            return null;
        }
    }

    private static Impl getImpl() {
        return new Impl();
    }

    static JiraIssueFinder getTestInstance() {
        return new JiraIssueFinder();
    }

    //--------------------------------------------------------------------------

    private static final class Impl {

        /*
         * This implementation is quite simple because of the following
         * precondition:
         *
         * #1 - all defined bug-words ("bug", "issue") consist of lowercase
         *      characters from the basic latin alphabet (a-z), no spaces
         */

        private static final String[] BUGWORDS = new String[] {"bug", "issue"}; //NOI18N

        private static final String PUNCT_CHARS = ",:;()[]{}";          //NOI18N

        private static final int LOWER_A = 'a';     //automatic conversion to int
        private static final int LOWER_Z = 'z';     //automatic conversion to int

        private static final int INIT        = 0;
        private static final int BUGWORD_OR_PRJKEY = 1;
        private static final int PRJKEY      = 2;
        private static final int HASH        = 3;
        private static final int HASH_SPC    = 4;
        private static final int BUGWORD_SPC = 5;
        private static final int BUGWORD_NL  = 6;
        private static final int BUGWORD_NL_STAR = 7;
        private static final int DASH        = 8;
        private static final int NUM         = 9;
        private static final int GARBAGE     = 10;

        private CharSequence str;
        private int pos;
        private int state;

        static {
            boolean asserts = false;
            assert asserts = true;
            if (asserts) {
                /*
                 * Checks that precondition #1 is met
                 * - all bugwords the bug number prefix are lowercase:
                 */
                for (int i = 0; i < BUGWORDS.length; i++) {
                    assert BUGWORDS[i].equals(BUGWORDS[i].toLowerCase());
                }
            }
        }

        int start;
        int end;
        int startOfBugwordOrPrjkey;
        int[] result;

        private Impl() { }

        private int[] findBoundaries(CharSequence str) {
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
            startOfBugwordOrPrjkey = -1;

            result = null;
        }

        private void handleChar(int c) {
            int newState;
            switch (state) {
                case INIT:
                    if (c == '#') {
                        rememberIsStart();
                        newState = HASH;
                    } else if (isAsciiLetter(c)) {
                        rememberIsStart();
                        rememberIsBugwordOrPrjkeyStart();
                        newState = BUGWORD_OR_PRJKEY;
                    } else if (isPrjKeyChar(c)) {
                        rememberIsStart();
                        newState = PRJKEY;
                    } else {
                        newState = getInitialState(c);
                    }
                    break;
                case BUGWORD_OR_PRJKEY:
                    if (isAsciiLetter(c)) {
                        newState = BUGWORD_OR_PRJKEY;
                    } else if (c == '-') {
                        newState = DASH;
                    } else if (isPrjKeyChar(c)) {
                        newState = PRJKEY;
                    } else if ((       (c == ' ')  || (c == '\t')
                                    || (c == '\r') || (c == '\n')   )
                               && isBugword(startOfBugwordOrPrjkey)) {
                        rememberIsStart(startOfBugwordOrPrjkey);
                        newState = ((c == ' ') || (c == '\t')) ? BUGWORD_SPC
                                                               : BUGWORD_NL;
                    } else {
                        newState = getInitialState(c);
                    }
                    break;
                case PRJKEY:
                    if (c == '-') {
                        newState = DASH;
                    } else if (isPrjKeyChar(c)) {
                        newState = PRJKEY;
                    } else {
                        newState = getInitialState(c);
                    }
                    break;
                case HASH:
                    if ((c == ' ') || (c == '\t')) {
                        newState = HASH_SPC;
                    } else if (isPrjKeyChar(c)) {
                        //bugword immediately after '#' is not allowed
                        newState = PRJKEY;
                    } else {
                        newState = getInitialState(c);
                    }
                    break;
                case HASH_SPC:
                    if ((c == ' ') || (c == '\t')) {
                        newState = HASH_SPC;
                    } else if (isAsciiLetter(c)) {
                        rememberIsBugwordOrPrjkeyStart();
                        newState = BUGWORD_OR_PRJKEY;
                    } else if (isPrjKeyChar(c)) {
                        newState = PRJKEY;
                    } else {
                        newState = getInitialState(c);
                    }
                    break;
                case BUGWORD_SPC:
                    if ((c == ' ') || (c == '\t')) {
                        newState = BUGWORD_SPC;
                    } else if ((c == '\r') || (c == '\n')) {
                        newState = BUGWORD_NL;
                    } else if (c == '#') {
                        newState = HASH;
                    } else if (isAsciiLetter(c)) {
                        rememberIsBugwordOrPrjkeyStart();
                        newState = BUGWORD_OR_PRJKEY;
                    } else if (isPrjKeyChar(c)) {
                        newState = PRJKEY;
                    } else {
                        newState = getInitialState(c);
                    }
                    break;
                case BUGWORD_NL:
                    if ((c == ' ') || (c == '\t') || (c == '\r') || (c == '\n')) {
                        newState = BUGWORD_NL;
                    } else if (c == '*') {
                        newState = BUGWORD_NL_STAR;
                    } else if (c == '#') {
                        newState = HASH;
                    } else if (isAsciiLetter(c)) {
                        rememberIsBugwordOrPrjkeyStart();
                        newState = BUGWORD_OR_PRJKEY;
                    } else if (isPrjKeyChar(c)) {
                        newState = PRJKEY;
                    } else {
                        newState = getInitialState(c);
                    }
                    break;
                case BUGWORD_NL_STAR:
                    if ((c == ' ') || (c == '\t')) {
                        newState = BUGWORD_SPC;
                    } else if ((c == '\r') || (c == '\n')) {
                        newState = BUGWORD_NL;
                    } else {
                        newState = getInitialState(c);
                    }
                    break;
                case DASH:
                    if (c == '#') {
                        newState = HASH;
                    } else if (isDigit(c)) {
                        newState = NUM;
                    } else if (isPrjKeyChar(c)) {
                        newState = PRJKEY;
                    } else {
                        newState = INIT;
                    }
                    break;
                case NUM:
                    if (c == '-') {
                        newState = DASH;
                    } else if (isDigit(c)) {
                        newState = NUM;
                    } else if (isPrjKeyChar(c)) {
                        newState = PRJKEY;
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
            if ((state == NUM) && (newState == INIT)) {
                storeResult(start, pos);
            }
            if ((newState == INIT) || (newState == GARBAGE)) {
                start = -1;
                startOfBugwordOrPrjkey = -1;
            }
            state = newState;
        }

        private int getInitialState(int c) {
            return isSpaceOrPunct(c) ? INIT : GARBAGE;
        }

        private void rememberIsStart() {
            rememberIsStart(pos);
        }

        private void rememberIsStart(int pos) {
            start = pos;
        }

        private void rememberIsBugwordOrPrjkeyStart() {
            startOfBugwordOrPrjkey = pos;
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

        private static boolean isPrjKeyChar(int c) {
            return (c > 0x20) && (c <= 0xff) && 
                   (isPunctuationAllowed() ? (c != ',') : !isPunct(c)); 
        }

        private static boolean isDigit(int c) {
            return ((c >= '0') && (c <= '9'));
        }

        private static boolean isAsciiLetter(int c) {
            /* relies on precondition #1 (see the top of the class) */
            c |= 0x20;
            return ((c >= LOWER_A) && (c <= LOWER_Z));
        }

        private static boolean isSpaceOrPunct(int c) {
            return (c == '\r') || (c == '\n')
                   || Character.isSpaceChar(c) || isPunct(c);
        }

        private static boolean isPunct(int c) {
            return PUNCT_CHARS.indexOf(c) != -1;
        }

        private boolean isBugword() {
            return isBugword(start);
        }

        private boolean isBugword(int startPos) {
            /* relies on precondition #1 (see the top of the class) */
            CharSequence word = str.subSequence(startPos, pos);
            for (int i = 0; i < BUGWORDS.length; i++) {
                if (equalsIgnoreCase(BUGWORDS[i], word)) {
                    return true;
                }
            }
            return false;
        }

        private static boolean equalsIgnoreCase(CharSequence pattern, CharSequence str) {
            final int patternLength = pattern.length();

            if (str.length() != patternLength) {
                return false;
            }

            /* relies on precondition #1 (see the top of the class) */
            for (int i = 0; i < patternLength; i++) {
                if ((str.charAt(i) | 0x20) != pattern.charAt(i)) {
                    return false;
                }
            }

            return true;
        }

        private static boolean isPunctuationAllowed() {
            return !"true".equals(System.getProperty("org.netbeans.modules.jira.noPunctuationInIssueKey", "false"));
        }


    }

}
