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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2008-2009 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.editor.url;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Jan Lahoda
 */
public final class Parser {

    private Parser() {}

    public static Iterable<int[]> recognizeURLs(CharSequence text) {
        List<int[]> result = new LinkedList<int[]>();
        int state = 0;
        int lastURLStart = -1;
        
        OUTER: for (int cntr = 0; cntr < text.length(); cntr++) {
            char ch = text.charAt(cntr);

            if (state == 5) {
                if (Character.isLetterOrDigit(ch)) {
                    continue OUTER;
                }

                switch (ch) {
                    case '/': case '.': case '?': //NOI18N
                    case '%': case '_': case '~': case '=': //NOI18N
                    case '\\':case '&': case '$': case '-': //NOI18N
                    case '\'': //NOI18N
                        continue OUTER;
                }

                assert lastURLStart != (-1);
                result.add(new int[] {lastURLStart, cntr});

                lastURLStart = (-1);
                state = 0;
                continue OUTER;
            }

            switch (ch) {
                case 'h': //NOI18N
                    if (state == 0) {
                        lastURLStart = cntr;
                        state = 1;
                        continue OUTER;
                    }
                    break;
                case 't': //NOI18N
                    if (state == 1) {
                        state = 2;
                        continue OUTER;
                    } else {
                        if (state == 2) {
                            state = 3;
                            continue OUTER;
                        }
                    }
                    break;
                case 'f': //NOI18N
                    if (state == 0) {
                        lastURLStart = cntr;
                        state = 2;
                        continue OUTER;
                    }
                    break;
                case 'p': //NOI18N
                    if (state == 3) {
                        state = 4;
                        continue OUTER;
                    }
                    break;
                case ':': //NOI18N
                    if (state == 4) {
                        state = 5;
                        continue OUTER;
                    }
                    break;
            }

            state = 0;
            lastURLStart = (-1);
        }

        if (lastURLStart != (-1) && state == 5) {
            result.add(new int[] {lastURLStart, text.length()});
        }
        
        return result;
    }
    
    private static final Pattern URL_PATTERN = Pattern.compile("(http|ftp):[0-9a-zA-Z/.?%_~=\\\\&$-]*"); //NOI18N

    public static Iterable<int[]> recognizeURLsREBased(CharSequence text) {
        Matcher m = URL_PATTERN.matcher(text);
        List<int[]> result = new LinkedList<int[]>();

        while (m.find()) {
            result.add(new int[] {m.start(), m.start() + m.group(0).length()});
        }
        
        return result;
    }

    private static final Pattern CHARSET = Pattern.compile("charset=([^;]+)(;|$)", Pattern.MULTILINE);//NOI18N
    public static String decodeContentType(String contentType) {
        if (contentType == null) return null;

        if (contentType != null) {
            Matcher m = CHARSET.matcher(contentType);

            if (m.find()) {
                return m.group(1);
            }
        }

        return null;
    }

}
