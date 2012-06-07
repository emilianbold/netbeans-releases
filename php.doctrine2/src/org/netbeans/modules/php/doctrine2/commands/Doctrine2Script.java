/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.doctrine2.commands;

import org.netbeans.api.extexecution.ExternalProcessBuilder;
import org.netbeans.modules.php.api.phpmodule.PhpInterpreter;
import org.netbeans.modules.php.api.phpmodule.PhpProgram;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.doctrine2.options.Doctrine2Options;
import org.openide.util.NbBundle;

/**
 * Represents <a href="http://doctrine-project.org/">doctrine</a> command line tool.
 */
public final class Doctrine2Script extends PhpProgram {

    public static final String SCRIPT_NAME = "doctrine"; // NOI18N
    public static final String SCRIPT_NAME_LONG = SCRIPT_NAME + FileUtils.getScriptExtension(true);
    public static final String LIST_COMMAND = "list"; // NOI18N
    public static final String XML_PARAM = "--xml"; // NOI18N


    private Doctrine2Script(String command) {
        super(command);
    }

    /**
     * Get the default, <b>valid only</b> Doctrine2 script.
     * @return the default, <b>valid only</b> Doctrine2 script.
     * @throws InvalidPhpProgramException if Doctrine2 script is not valid.
     */
    public static Doctrine2Script getDefault() throws InvalidPhpProgramException {
        String script = Doctrine2Options.getInstance().getScript();
        String error = validate(script);
        if (error != null) {
            throw new InvalidPhpProgramException(error);
        }
        return new Doctrine2Script(script);
    }

    public static String validate(String command) {
        return new Doctrine2Script(command).validate();
    }

    @NbBundle.Messages("Doctrine2Script.script.label=Doctrine2 script")
    @Override
    public String validate() {
        return FileUtils.validateFile(Bundle.Doctrine2Script_script_label(), getProgram(), false);
    }

    @Override
    public ExternalProcessBuilder getProcessBuilder() {
        // XXX
        if (getProgram().endsWith(".bat")) { // NOI18N
            return super.getProcessBuilder();
        }
        // run file via php interpreter
        try {
            return PhpInterpreter.getDefault().getProcessBuilder()
                    .addArgument(getProgram());
        } catch (InvalidPhpProgramException ex) {
            // ignored
        }
        return super.getProcessBuilder();
    }

}
