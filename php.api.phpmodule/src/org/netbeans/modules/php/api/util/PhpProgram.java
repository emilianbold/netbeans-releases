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

package org.netbeans.modules.php.api.util;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Utilities;

/**
 * Base class for all PHP based programs (scripts).
 * @author Tomas Mysik
 */
public class PhpProgram {
    protected static final Logger LOGGER = Logger.getLogger(PhpProgram.class.getName());
    private static final String[] NO_PARAMETERS = new String[0];

    private final String program;
    private final String[] parameters;
    private final String fullCommand;

    /**
     * Parse command which can be just binary or binary with parameters.
     * As a parameter separator, "-" or "/" is used.
     * @param command command to parse, can be <code>null</code>.
     */
    public PhpProgram(String command) {
        if (command == null) {
            // avoid NPE
            command = ""; // NOI18N
        }

        // try to find program (search for " -" or " /" after space)
        String[] tokens = command.split(" * (?=\\-|/)", 2); // NOI18N
        switch (tokens.length) {
            case 1:
                LOGGER.fine("Only program given (no parameters)");

                program = tokens[0].trim();
                parameters = NO_PARAMETERS;
                fullCommand = program;
                break;

            default:
                assert tokens.length > 1;
                program = tokens[0].trim();
                parameters = Utilities.parseParameters(tokens[1].trim());
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.fine(String.format("Parameters parsed: %s => %s", tokens[1].trim(), Arrays.asList(parameters)));
                }
                fullCommand = command.trim();
                break;
        }
    }

    /**
     * @return PHP program, never <code>null</code>.
     */
    public String getProgram() {
        return program;
    }

    /**
     * @return parameters, can be an empty array but never <code>null</code>.
     */
    public String[] getParameters() {
        return parameters;
    }

    /**
     * @return the full command, in the original form (just without leading and trailing whitespaces).
     */
    public String getFullCommand() {
        return fullCommand;
    }

    /**
     * @return <code>true</code> if program is set, <code>false</code> otherwise.
     */
    public boolean isValid() {
        return StringUtils.hasText(program);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(200);
        sb.append(getClass().getName());
        sb.append(" [program: "); // NOI18N
        sb.append(program);
        sb.append(", parameters: "); // NOI18N
        sb.append(Arrays.asList(parameters));
        sb.append("]"); // NOI18N
        return sb.toString();
    }
}
