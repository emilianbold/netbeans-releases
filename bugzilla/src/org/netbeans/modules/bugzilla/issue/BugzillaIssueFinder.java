/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugzilla.issue;

import org.netbeans.modules.bugtracking.spi.IssueFinder;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 *
 * @author Tomas Stupka
 * @author Marian Petras
 */
@ServiceProviders({@ServiceProvider(service=IssueFinder.class),
                   @ServiceProvider(service=BugzillaIssueFinder.class)})
public class BugzillaIssueFinder extends IssueFinder {

    private static final int[] EMPTY_INT_ARR = new int[0];

    public int[] getIssueSpans(String text) {
        int[] result = findBoundaries(text);
        return (result != null) ? result : EMPTY_INT_ARR;
    }

    public String getIssueId(String issueHyperlinkText) {
        int pos = issueHyperlinkText.length() - 1;
        while ((pos >= 0) && Impl.isDigit(issueHyperlinkText.charAt(pos))) {
            pos--;
        }
        return issueHyperlinkText.substring(pos + 1);
    }

    private static int[] findBoundaries(String str) {
        return getImpl().findBoundaries(str);
    }

    private static Impl getImpl() {
        return new Impl();
    }

    static BugzillaIssueFinder getTestInstance() {
        return new BugzillaIssueFinder();
    }

    //--------------------------------------------------------------------------

    private static final class Impl {

        private static final String[] BUGWORDS = new String[] {"bug", "issue",  //NOI18N
                                                               "Bug", "Issue",  //NOI18N
                                                               "BUG", "ISSUE"}; //NOI18N

        private static final String PUNCT_CHARS = ",:;()[]{}";          //NOI18N

        private static final int INIT       = 0;
        private static final int CHARS      = 1;
        private static final int HASH       = 2;
        private static final int HASH_SPC   = 3;
        private static final int NUM        = 4;
        private static final int BUGWORD    = 5;
        private static final int BUGWORD_NL = 6;
        private static final int STAR       = 7;
        private static final int GARBAGE    = 8;

        private String str;
        private int pos;
        private int state;

        int start;
        int end;
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

            result = null;
        }

        private void handleChar(int c) {
            int newState;
            switch (state) {
                case INIT:
                    if (c == '#') {
                        rememberIsStart();
                        newState = HASH;
                    } else if (Character.isLetter(c)) {
                        rememberIsStart();
                        newState = CHARS;
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
                    } else if (isDigit(c)) {
                        newState = NUM;
                    } else {
                        newState = getInitialState(c);
                    }
                    break;
                case HASH_SPC:
                    if ((c == ' ') || (c == '\t')) {
                        newState = HASH_SPC;
                    } else if (isDigit(c)) {
                        newState = NUM;
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
                    } else if (isDigit(c)) {
                        newState = NUM;
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
                    } else if (isDigit(c)) {
                        newState = NUM;
                    } else {
                        newState = getInitialState(c);
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
            state = newState;
        }

        private int getInitialState(int c) {
            return isSpaceOrPunct(c) ? INIT : GARBAGE;
        }

        private void rememberIsStart() {
            assert start == -1;
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

        private static boolean isSpaceOrPunct(int c) {
            return (c == '\r') || (c == '\n')
                   || Character.isSpaceChar(c) || isPunct(c);
        }

        private static boolean isPunct(int c) {
            return PUNCT_CHARS.indexOf(c) != -1;
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