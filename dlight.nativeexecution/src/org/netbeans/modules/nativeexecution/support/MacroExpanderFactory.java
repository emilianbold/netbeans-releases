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
package org.netbeans.modules.nativeexecution.support;

import java.net.ConnectException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;

public final class MacroExpanderFactory {

    private static final List<String> knownMacros = Arrays.asList(new String[]{
                "osname", // NOI18N
                "platform", // NOI18N
                "isa", // NOI18N
                "_isa", // NOI18N
                "soext" // NOI18N
            });

    private MacroExpanderFactory() {
    }

    public static MacroExpander getExpander(ExecutionEnvironment execEnv) {
        return getExpander(execEnv, null);
    }

    public static MacroExpander getExpander(ExecutionEnvironment execEnv, String style) {
        if ("SunStudio".equals(style)) { // NOI18N
            return new SunStudioMacroExpander(execEnv);
        } else {
            return new CommonMacroExpander(execEnv);
        }
    }

    public interface MacroExpander {

        public String expandMacros(String s) throws ParseException;
    }

    private static class SunStudioMacroExpander extends CommonMacroExpander {

        public SunStudioMacroExpander(ExecutionEnvironment execEnv) {
            super(execEnv);
        }

        @Override
        protected String getValueOf(String str) {
            try {
                String result;
                if ("platform".equals(str)) { // NOI18N
                    String platform = HostInfoUtils.getPlatform(execEnv);

                    if ("i386".equals(platform) || // NOI18N
                            "i686".equals(platform) || // NOI18N
                            "x86".equals(platform) || // NOI18N
                            "x86_64".equals(platform) || // NOI18N
                            "athlon".equals(platform)) { // NOI18N
                        result = "intel"; // NOI18N
                    } else {
                        result = platform;
                    }

                    String os = HostInfoUtils.getOS(execEnv);
                    if ("SunOS".equals(os)) { // NOI18N
                        result += "-S2"; // NOI18N
                    } else {
                        result += "-" + os; // NOI18N
                    }

                    return result;
                }

                return super.getValueOf(str);
            } catch (ConnectException ex) {
            }
            return null;
        }
    }

    private static class CommonMacroExpander implements MacroExpander {

        protected final ExecutionEnvironment execEnv;
        private final int[][] ttable = new int[][]{
            {0, 0, 0, 1, 0, 0},
            {2, 3, 3, 10, 4, 3},
            {2, 2, 5, 6, 5, 5},
            {7, 7, 8, 8, 8, 9},
            {7, 3, 3, 3, 8, 8}
        };
        private char c;
        private final StringBuilder res = new StringBuilder();
        private final StringBuilder buf = new StringBuilder();

        public CommonMacroExpander(ExecutionEnvironment execEnv) {
            this.execEnv = execEnv;
        }

        private int getCharClass(char c) {

            if (c == '_' || (c >= 'A' && c <= 'Z') || c >= 'a' && c <= 'z') {
                return 0;
            }

            if (c >= '0' && c <= '9') {
                return 1;
            }

            if (c == '$') {
                return 3;
            }

            if (c == '{') {
                return 4;
            }

            if (c == '}') {
                return 5;
            }

            return 2;
        }

        public final boolean isKnown(String macro) {
            return knownMacros.contains(macro.trim().toLowerCase());
        }

        public final String valueOf(String macro) {
            return getValueOf(macro.toLowerCase().trim());
        }

        protected String getValueOf(String str) {
            try {
                if ("osname".equals(str)) { // NOI18N
                    return HostInfoUtils.getOS(execEnv);
                }

                if ("platform".equals(str)) { // NOI18N
                    String platform = HostInfoUtils.getPlatform(execEnv);

                    if ("i386".equals(platform) || // NOI18N
                            "i686".equals(platform) || // NOI18N
                            "x86_64".equals(platform) || // NOI18N
                            "athlon".equals(platform)) { // NOI18N
                        return "x86"; // NOI18N
                    } else {
                        return platform;
                    }
                }

                if ("isa".equals(str)) { // NOI18N
                    return HostInfoUtils.getIsaBits(execEnv); // NOI18N
                }

                if ("_isa".equals(str)) { // NOI18N
                    return HostInfoUtils.getIsaBits(execEnv).equals("64") ? "_64" : ""; // NOI18N
                }

                if ("soext".equals(str)) { // NOI18N
                    String os = HostInfoUtils.getOS(execEnv);
                    if ("Windows".equals(os)) {
                        return "dll"; // NOI18N
                    }
                    if ("Darwin".equals(os)) {
                        return "dylib"; // NOI18N
                    }
                    return "so"; // NOI18N
                }
            } catch (ConnectException ex) {
            }
            return null;
        }

        public final String expandMacros(final String string) throws ParseException {
            if (string == null || string.length() == 0) {
                return string;
            }

            res.setLength(0);
            buf.setLength(0);

            int state = 0, pos = 0, mpos = -1;
            char[] chars = (string + (char) 0).toCharArray();

            while (pos < chars.length) {
                c = chars[pos];

                switch (ttable[state][getCharClass(c)]) {
                    case 0:
                        if (c != 0) {
                            res.append(c);
                        }
                        break;
                    case 1:
                        mpos = pos;
                        buf.setLength(0);
                        state = 1;
                        break;
                    case 2:
                        buf.append(c);
                        state = 2;
                        break;
                    case 3:
                        res.append(string.substring(mpos, pos + (c == 0 ? 0 : 1)));
                        buf.setLength(0);
                        state = 0;
                        break;
                    case 4:
                        state = 4;
                        break;
                    case 5:
                        res.append(getValueOf(buf.toString()));
                        pos--;
                        buf.setLength(0);
                        state = 0;
                        break;
                    case 6:
                        res.append(getValueOf(buf.toString()));
                        mpos = pos;
                        buf.setLength(0);
                        state = 1;
                        break;
                    case 7:
                        buf.append(c);
                        state = 3;
                        break;
                    case 8:
                        throw new ParseException("Bad substitution", pos); // NOI18N
                    case 9:
                        res.append(getValueOf(buf.toString()));
                        buf.setLength(0);
                        state = 0;
                        break;
                    case 10:
                        res.append(string.substring(mpos, pos));
                        pos--;
                        buf.setLength(0);
                        state = 0;
                        break;
                }
                pos++;
            }

            return res.toString();
        }
    }
}
