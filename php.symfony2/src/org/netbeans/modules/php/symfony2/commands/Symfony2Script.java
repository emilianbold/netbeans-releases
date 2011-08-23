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
package org.netbeans.modules.php.symfony2.commands;

import java.io.File;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.phpmodule.PhpProgram;
import org.netbeans.modules.php.api.util.FileUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle.Messages;

/**
 * Represents Symfony2 command line tool.
 */
public final class Symfony2Script extends PhpProgram {

    public static final String SCRIPT_PATH = "app/console"; // NOI18N

    Symfony2Script(String command) {
        super(command);
    }

    /**
     * Get the project specific, <b>valid only</b> Symfony2 script. If not found, {@code null} is returned.
     * @param phpModule PHP module for which Symfony2 script is taken
     * @param warn <code>true</code> if user is warned when the Symfony2 script is not valid
     * @return Symfony2 console script or {@code null} if the script is not valid
     */
    @CheckForNull
    @Messages("MSG_InvalidSymfony2Script=<html>Project''s Symfony2 console script is not valid.<br>({0})")
    public static Symfony2Script forPhpModule(PhpModule phpModule, boolean warn) {
        String console = new File(FileUtil.toFile(phpModule.getSourceDirectory()), SCRIPT_PATH.replace('/', File.separatorChar)).getAbsolutePath(); // NOI18N
        String error = validate(console);
        if (error == null) {
            return new Symfony2Script(console);
        }
        if (warn) {
            NotifyDescriptor.Message message = new NotifyDescriptor.Message(
                    Bundle.MSG_InvalidSymfony2Script(error),
                    NotifyDescriptor.WARNING_MESSAGE);
            DialogDisplayer.getDefault().notify(message);
        }
        // in fact should not happen since the console script is used for framework detection
        return null;
    }

    public static String validate(String command) {
        return new Symfony2Script(command).validate();
    }

    @Messages("LBL_Symfony2Script=Symfony2 console")
    @Override
    public String validate() {
        return FileUtils.validateScript(getProgram(), Bundle.LBL_Symfony2Script());
    }

}
