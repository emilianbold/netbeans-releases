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

package org.netbeans.modules.bugzilla.issue;

import java.util.Collection;

/**
 * Determines boundaries of text that should be rendered as a hyperlink
 * to an issue attachment (screenshot, full thread-dump, app. output etc.).
 *
 * @author Marian Petras
 */
class AttachmentHyperlinkSupport {

    private static final String PREFIX = "Created an attachment (id=";  //NOI18N
    private static final int EQUAL_SIGN_POSITION = PREFIX.lastIndexOf('=');
    private static final int ATTACHMENT_HYPERLINK_START_FALLBACK
                             = PREFIX.indexOf("attachment");            //NOI18N
    //private static final Pattern pattern = Pattern.compile(
    //                 "([0-9]++)\\)[^\\r\\n]*+(?:[\\r\\n]++(.*+))?+"); //NOI18N

    private static final char CR = '\r';
    private static final char LF = '\n';

    static int[] findBoundaries(String text,
                                Collection<String> knownIds) {
        if ((knownIds != null) && knownIds.isEmpty()) {
            return null;
        }

        final int length = text.length();
        if ((length >= EQUAL_SIGN_POSITION + 3)
                && (text.charAt(EQUAL_SIGN_POSITION) == '=')
                && text.startsWith(PREFIX)) {
            int idStartIndex = EQUAL_SIGN_POSITION + 1;
            if (isValidIdChar(text.charAt(idStartIndex))) {
                int index = idStartIndex + 1;
                while ((index < length) && isValidIdChar(text.charAt(index))) {
                    index++;
                }
                if ((index < length) && (text.charAt(index) == ')')) {
                    int idEndIndex = index;
                    if (isKnownId(text.substring(idStartIndex, idEndIndex),
                                  knownIds)) {
                        do {
                            index++;
                        } while ((index < length) && isNotNewline(text.charAt(index)));
                        if (index < length) {       //at newline

                            /* skip just one newline */
                            if (text.charAt(index) == CR) {
                                index++;
                            }
                            if ((index < length) && (text.charAt(index) == LF)) {
                                index++;
                            }
                            while ((index < length) && isSpace(text.charAt(index))) {
                                index++;
                            }
                            if ((index < length) && isNotNewline(text.charAt(index))) {   //at printable
                                int descriptionStart = index;
                                do {
                                    index++;
                                } while ((index < length) && isNotNewline(text.charAt(index)));
                                return new int[] {descriptionStart, index};
                            }
                        }
                        return new int[] {ATTACHMENT_HYPERLINK_START_FALLBACK,
                                          idEndIndex + 1};
                    }
                }
            }
        }
        return null;
    }

    static String getAttachmentId(String commentText) {
        int closingBracketPos = commentText.indexOf(')', PREFIX.length() + 1);
        assert closingBracketPos != -1;
        return new String(commentText.substring(PREFIX.length(), closingBracketPos));
    }

    private static boolean isKnownId(String id,
                                     Collection<String> knownIds) {
        if (knownIds == null) {
            return true;
        }

        for (String validId : knownIds) {
            if (id.equals(validId)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isValidIdChar(char c) {
        return (c >= '0') && (c <= '9');
    }

    private static boolean isNotNewline(char c) {
        return (c != '\r') && (c != '\n');
    }

    private static boolean isSpace(char c) {
        return (c == ' ') || (c == '\t');
    }

}
