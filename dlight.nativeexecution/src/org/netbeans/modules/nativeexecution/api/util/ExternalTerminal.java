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

import java.util.List;
import org.netbeans.modules.nativeexecution.ExternalTerminalAccessor;
import org.netbeans.modules.nativeexecution.support.TerminalProfile;

/**
 * NativeProcessBuilder can create a process that is executed in an external
 * terminal identified by ExternalTerminal object. The object can be obtained
 * with {@link ExternalTerminalProvider#getTerminal(java.lang.String)}.
 *
 * @see ExternalTerminalProvider
 */
public final class ExternalTerminal {

    private final TerminalProfile profile;
    private String title = null;
    private String prompt = "Press [Enter] to close the terminal ..."; // NOI18N


    static {
        ExternalTerminalAccessor.setDefault(new ExternalTerminalAccessorImpl());
    }

    ExternalTerminal(TerminalProfile info) throws IllegalArgumentException {
        this.profile = info;
    }

    private ExternalTerminal(ExternalTerminal terminal) {
        profile = terminal.profile;
        title = terminal.title;
        prompt = terminal.prompt;
    }

    /**
     * Returnes an ExternalTerminal with configured prompt message that
     * appears in terminal after command execution is finished.
     *
     * @param prompt prompt to be used in external terminal
     * @return ExternalTerminal with configured prompt message
     */
    public ExternalTerminal setPrompt(String prompt) {
        ExternalTerminal result = new ExternalTerminal(this);
        result.prompt = prompt;
        return result;
    }

    /**
     * Returnes an ExternalTerminal with configured title that
     * appears in terminal that executes a native process.
     *
     * @param title String to be displayed in a title of a terminal (if a
     *        terminal has capabilities to set a title)
     * 
     * @return ExternalTerminal with configured title
     */
    public ExternalTerminal setTitle(String title) {
        ExternalTerminal result = new ExternalTerminal(this);
        result.title = title;
        return result;
    }

    private static class ExternalTerminalAccessorImpl
            extends ExternalTerminalAccessor {

        @Override
        public TerminalProfile getTerminalProfile(ExternalTerminal terminal) {
            return terminal.profile;
        }

        @Override
        public List<String> wrapCommand(
                ExternalTerminal terminal, String... args) {
            String t = terminal.title;

            if (t == null) {
                StringBuilder sb = new StringBuilder();
                for (String arg : args) {
                    sb.append(arg).append(' ');
                }

                t = sb.toString().trim();
            }


            return terminal.profile.wrapCommand(t, args);
        }

        @Override
        public String getPrompt(ExternalTerminal terminal) {
            return terminal.prompt;
        }

        @Override
        public String getTitle(ExternalTerminal terminal) {
            return terminal.title;
        }
    }
}
