/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.project.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Tomas Mysik
 */
public final class PhpInterpreter {
    private static final Logger LOGGER = Logger.getLogger(PhpInterpreter.class.getName());

    private final String interpreter;
    private final String[] parameters;
    private final String fullCommand;

    /**
     * Parse command which can be just binary or binary with parameters.
     * As a parameter separator, "-" or "/" is used.
     * @param command command to parse, can be <code>null</code>.
     * @see #isValid()
     */
    public PhpInterpreter(String command) {
        if (command == null) {
            // avoid NPE
            command = ""; // NOI18N
        }

        // try to find parameters (search for " -" or " /")
        String[] tokens = command.split(" * (?=\\-|/)"); // NOI18N
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine(String.format("%s => %s", command, Arrays.asList(tokens)));
        }

        interpreter = tokens[0].trim();
        if (tokens.length == 1) {
            parameters = new String[0];
        } else {
            // we have some parameters
            List<String> params = new ArrayList<String>(tokens.length - 1);
            for (int i = 1; i < tokens.length; ++i) {
                params.add(tokens[i].trim());
            }
            parameters = params.toArray(new String[params.size()]);
        }
        fullCommand = command.trim();
    }

    /**
     * @return PHP interpreter, never <code>null</code>.
     */
    public String getInterpreter() {
        return interpreter;
    }

    /**
     * @return parameters, can be an empty array but never <code>null</code>.
     */
    public String[] getParameters() {
        return parameters;
    }

    /**
     * @return the full command, in the original form (just without leading and trailing whitespace).
     */
    public String getFullCommand() {
        return fullCommand;
    }

    /**
     * @return <code>true</code> if interpreter is set, <code>false</code> otherwise.
     */
    public boolean isValid() {
        return interpreter.length() > 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(200);
        sb.append(getClass().getName());
        sb.append(" [interpreter: "); // NOI18N
        sb.append(interpreter);
        sb.append(", parameters: "); // NOI18N
        sb.append(Arrays.asList(parameters));
        sb.append("]"); // NOI18N
        return sb.toString();
    }
}
