/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.project.runconfigs.validation;

import java.io.File;
import org.netbeans.modules.php.api.util.StringUtils;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Validator for {@link org.netbeans.modules.php.project.runconfigs.RunConfig}.
 */
public final class RunConfigValidator {

    private RunConfigValidator() {
    }

    @NbBundle.Messages({
        "RunConfigValidator.error.index.missing=Index File must be specified in order to run or debug project in command line.",
        "RunConfigValidator.error.index.invalid=Index File must be a valid relative URL."
    })
    public static String validateIndexFile(File rootDirectory, String indexFile) {
        assert rootDirectory != null;
        if (!StringUtils.hasText(indexFile)) {
            return Bundle.RunConfigValidator_error_index_missing();
        }
        boolean error = false;
        if (indexFile.startsWith("/") // NOI18N
                || indexFile.startsWith("\\")) { // NOI18N
            error = true;
        } else if (Utilities.isWindows() && indexFile.contains(File.separator)) {
            error = true;
        } else {
            File index = new File(rootDirectory, indexFile.replace('/', File.separatorChar)); // NOI18N
            if (!index.isFile()
                    || !index.equals(FileUtil.normalizeFile(index))) {
                error = true;
            }
        }
        if (error) {
            return Bundle.RunConfigValidator_error_index_invalid();
        }
        return null;
    }

}
