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

package org.netbeans.modules.php.api.phpmodule;

import java.io.File;
import java.util.regex.Pattern;
import org.netbeans.modules.php.api.util.StringUtils;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * PHP interpreter as it is specified in Tools > Options > PHP.
 * @author Tomas Mysik
 * @since 1.9
 */
public final class PhpInterpreter extends PhpProgram {
    public static final Pattern[] LINE_PATTERNS = {
        Pattern.compile(".+\\s+in\\s+(.+)\\s+on\\s+line\\s+(\\d+)"), // NOI18N
        Pattern.compile(".+\\(\\)\\s+(.+):(\\d+)"), // NOI18N
    };

    PhpInterpreter(String command) {
        super(command);
    }

    /**
     * Get the {@link PhpOptions#getPhpInterpreter() default}, <b>valid only</b> PHP interpreter.
     * @return the {@link PhpOptions#getPhpInterpreter() default}, <b>valid only</b> PHP interpreter.
     * @throws PhpProgram.InvalidPhpProgramException if PHP interpreter is not valid.
     *         The reason can be found in the {@link PhpProgram.InvalidPhpProgramException#getLocalizedMessage exception's message}.
     */
    public static PhpInterpreter getDefault() throws InvalidPhpProgramException {
        String command = Lookup.getDefault().lookup(PhpOptions.class).getPhpInterpreter();
        String error = validate(command);
        if (error != null) {
            throw new InvalidPhpProgramException(error);
        }
        return new PhpInterpreter(command);
    }

    /**
     * Get the custom, <b>valid only</b> PHP interpreter. This method is used by projects
     * that have their own PHP interpreter specified (using Run Script configuration).
     * @param command command which represents path to PHP interpreter (arguments allowed)
     * @return the custom, <b>valid only</b> PHP interpreter.
     * @throws org.netbeans.modules.php.api.phpmodule.PhpProgram.InvalidPhpProgramException if PHP interpreter is not valid.
     *         The reason can be found in the {@link org.netbeans.modules.php.api.phpmodule.PhpProgram.InvalidPhpProgramException#getLocalizedMessage exception's message}.
     */
    public static PhpInterpreter getCustom(String command) throws InvalidPhpProgramException {
        String error = validate(command);
        if (error != null) {
            throw new InvalidPhpProgramException(error);
        }
        return new PhpInterpreter(command);
    }

    @Override
    public String validate() {
        if (!StringUtils.hasText(getProgram())) {
            return NbBundle.getMessage(PhpInterpreter.class, "MSG_NoPhpInterpreter");
        }

        File file = new File(getProgram());
        if (!file.isAbsolute()) {
            return NbBundle.getMessage(PhpInterpreter.class, "MSG_PhpInterpreterNotAbsolutePath");
        }
        if (!file.isFile()) {
            return NbBundle.getMessage(PhpInterpreter.class, "MSG_PhpInterpreterNotFile");
        }
        if (!file.canRead()) {
            return NbBundle.getMessage(PhpInterpreter.class, "MSG_PhpInterpreterCannotRead");
        }
        return null;
    }

    /**
     * Get the error message if the command is not valid or <code>null</code> if it's valid.
     * @param command a command to validate
     * @return the error message if the command is not valid or <code>null</code> if it's valid.
     * @see #validate()
     */
    public static String validate(String command) {
        return new PhpInterpreter(command).validate();
    }
}
