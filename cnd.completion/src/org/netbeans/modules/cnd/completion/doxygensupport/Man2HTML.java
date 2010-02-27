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
package org.netbeans.modules.cnd.completion.doxygensupport;

import java.io.BufferedReader;
import java.io.IOException;

/**
 *
 * @author thp
 * Simple man output to HTML formatter. 
 */
public class Man2HTML {

    public static int MAX_WIDTH = 58;

    private enum MODE {

        NORMAL, BOLD, ITALIC;
    };
    private BufferedReader br;
    private MODE mode = MODE.NORMAL;

    /**
     * Simple man output to HTML formatter. Takes the output of the man command as input.
     * @param br Charater input stream
     */
    public Man2HTML(BufferedReader br) {
        this.br = br;
    }

    private void startNormal(StringBuffer buf) {
        if (mode != MODE.NORMAL) {
            if (mode == MODE.BOLD) {
                buf.append("</B>"); // NOI18N
            } else if (mode == MODE.ITALIC) {
                buf.append("</I>"); // NOI18N
            }
            mode = MODE.NORMAL;
        }
    }

    private void startBold(StringBuffer buf) {
        buf.append("<B>"); // NOI18N
        mode = MODE.BOLD;
    }

    private void startItalic(StringBuffer buf) {
        buf.append("<I>"); // NOI18N
        mode = MODE.ITALIC;
    }

    private int countIndent(String line) {
        int indent = 0;
        while (indent < line.length() && line.charAt(indent) == ' ') {
            indent++;
        }
        return indent;
    }

    private int breakAtColumn(String line) {
        int breakAt = MAX_WIDTH;
        int column = 0;
        int i = 0;
        while (i < line.length() && column < MAX_WIDTH) {
            char ch = line.charAt(i);
            if (ch == '\b') {
                column--;
            } else if (ch == ' ') {
                breakAt = column - 1;
                column++;
            } else {
                column++;
            }
            i++;
        }
        if (column < MAX_WIDTH) {
            breakAt = MAX_WIDTH;
        }
        return breakAt;
    }

    /**
     * Run the formatter.
     * @return the formattet html document as a String
     */
    public String getHTML() {
        StringBuffer buf = new StringBuffer();
        buf.append("<HTML>\n"); // NOI18N
        buf.append("<BODY>\n"); // NOI18N
        buf.append("<PRE>\n"); // NOI18N
//        buf.append("<small>\n"); // NOI18N

        char prevCh = 0;
        char curCh = 0;
        char nextCh = 0;

        int curColumn = 0;

        try {
            String line = null;
            while ((line = br.readLine()) != null) {
                for (int i = 0; i < line.length(); i++) {
                    prevCh = curCh;
                    curCh = nextCh;
                    nextCh = line.charAt(i);

                    if (nextCh == '\b') {
                        if (mode == MODE.NORMAL) {
                            if (curCh == '_') {
                                startItalic(buf);
                            } else {
                                startBold(buf);
                            }
                        }
                    } else {
                        if (curCh != 0 && curCh != '\b') {
                            if (prevCh != 0 && prevCh != '\b') {
                                startNormal(buf);
                            }
                            // Just append the char to line. Escape if necessary.
                            if (curCh == '<') {
                                buf.append("&lt;"); // NOI18N
                            } else if (curCh == '>') {
                                buf.append("&gt;"); // NOI18N
                            } else if (curCh == '\"') {
                                buf.append("&rdquo;"); // NOI18N
                            } else if (curCh == '\'') {
                                buf.append("&rsquo;"); // NOI18N
                            } else if (curCh == '`') {
                                buf.append("&lsquo;"); // NOI18N
                            } else if (curCh == '&') {
                                buf.append("&amp;"); // NOI18N
                            } else {
                                buf.append(curCh);
                            }
                            curColumn++;
//                            if (curColumn >= MAX_WIDTH) {
//                                break;
//                            }
                        }
                    }
                }


                if (nextCh != 0) {
                    buf.append(nextCh);
                }
                startNormal(buf);
                prevCh = 0;
                curCh = 0;
                nextCh = 0;
                curColumn = 0;
                buf.append("\n"); // NOI18N
            }
        } catch (IOException ioe) {
        }

//        buf.append("</small>\n"); // NOI18N
        buf.append("</PRE>\n"); // NOI18N
        buf.append("</BODY>\n"); // NOI18N
        buf.append("</HTML>\n"); // NOI18N
        return buf.toString();
    }
}
