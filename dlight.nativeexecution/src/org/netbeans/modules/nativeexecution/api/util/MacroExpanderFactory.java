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
package org.netbeans.modules.nativeexecution.api.util;

import java.io.IOException;
import java.text.ParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CancellationException;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.HostInfo.OS;

public final class MacroExpanderFactory {

    private MacroExpanderFactory() {
    }

    public static MacroExpander getExpander(ExecutionEnvironment execEnv) {
        return getExpander(execEnv, null);
    }

    public static MacroExpander getExpander(
            ExecutionEnvironment execEnv, String style) {
        MacroExpander result;

        if ("SunStudio".equals(style)) { // NOI18N
            result = new SunStudioMacroExpander(execEnv);
        } else {
            result = new CommonMacroExpander(execEnv);
        }

        return result;
    }

    public interface MacroExpander {

        public String expandPredefinedMacros(String string) throws ParseException;

        public String expandMacros(
                String string,
                Map<String, String> envVariables) throws ParseException;
    }

    private static class CommonMacroExpander implements MacroExpander {

        protected static final Map<String, String> predefinedMacros =
                Collections.synchronizedMap(new HashMap<String, String>());
        protected final ExecutionEnvironment execEnv;
        private final int[][] ttable = new int[][]{
            {0, 0, 0, 1, 0, 0},
            {2, 3, 3, 10, 4, 3},
            {2, 2, 5, 6, 5, 5},
            {7, 7, 8, 8, 8, 9},
            {7, 3, 3, 3, 8, 8}
        };
        private final StringBuilder res = new StringBuilder();
        private final StringBuilder buf = new StringBuilder();
        private boolean initialized;

        public CommonMacroExpander(ExecutionEnvironment execEnv) {
            this.execEnv = execEnv;
            initialized = false;
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

        private String valueOf(String macro, Map<String, String> map) {
            String result = map.get(macro);
            return result == null ? "${" + macro + "}" : result; // NOI18N
        }

        public final String expandPredefinedMacros(
                final String string) throws ParseException {
            return expandMacros(string, predefinedMacros);
        }

        public final String expandMacros(
                final String string,
                final Map<String, String> map) throws ParseException {

            if (string == null || string.length() == 0) {
                return string;
            }

            synchronized (this) {
                if (!initialized) {
                    try {
                        HostInfo hi = HostInfoUtils.getHostInfo(execEnv);
                        if (hi != null) {
                            setupPredefined(hi);
                            initialized = true;
                        }
                    } catch (IOException ex) {
                    } catch (CancellationException ex) {
                    }
                // Should we try to get host info next time?
                // (in case of failure)
                }
            }

            res.setLength(0);
            buf.setLength(0);

            int state = 0, pos = 0, mpos = -1;
            char[] chars = (string + (char) 0).toCharArray();
            char c;

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
                        res.append(valueOf(buf.toString().trim(), map));
                        pos--;
                        buf.setLength(0);
                        state = 0;
                        break;
                    case 6:
                        res.append(valueOf(buf.toString().trim(), map));
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
                        res.append(valueOf(buf.toString().trim(), map));
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

        protected void setupPredefined(final HostInfo hi) {
            String soext;
            String osname;
            switch (hi.getOSFamily()) {
                case WINDOWS:
                    soext = "dll"; // NOI18N
                    osname = "Windows"; // NOI18N
                    break;
                case MACOSX:
                    soext = "dylib"; // NOI18N
                    osname = "MacOSX"; // NOI18N
                    break;
                case SUNOS:
                    soext = "so"; // NOI18N
                    osname = "SunOS"; // NOI18N
                    break;
                case LINUX:
                    soext = "so"; // NOI18N
                    osname = "Linux"; // NOI18N
                    break;
                default:
                    osname = hi.getOSFamily().name();
                    soext = "so"; // NOI18N
            }

            OS os = hi.getOS();

            predefinedMacros.put("soext", soext); // NOI18N
            predefinedMacros.put("platform", hi.getCpuFamily().name().toLowerCase()); // NOI18N
            predefinedMacros.put("osname", osname); // NOI18N
            predefinedMacros.put("isa", os.getBitness().toString()); // NOI18N
            predefinedMacros.put("_isa", os.getBitness() == HostInfo.Bitness._64 ? "_64" : ""); // NOI18N
        }
    }

    private static class SunStudioMacroExpander extends CommonMacroExpander {

        public SunStudioMacroExpander(ExecutionEnvironment execEnv) {
            super(execEnv);
        }

        @Override
        protected void setupPredefined(HostInfo hi) {
            super.setupPredefined(hi);

            // Rewrite "platform"
            String platform = predefinedMacros.get("platform"); // NOI18N

            if ("x86".equals(platform)) { // NOI18N
                platform = "intel"; // NOI18N
            }

            if (hi.getOSFamily() == HostInfo.OSFamily.SUNOS) { // NOI18N
                platform += "-S2"; // NOI18N
            } else {
                platform += "-" + hi.getOSFamily().name(); // NOI18N
            }

            predefinedMacros.put("platform", platform); // NOI18N
        }
    }
}
