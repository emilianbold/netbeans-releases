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

package org.netbeans.modules.cnd.execution.impl;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.filesystems.FileObject;

public final class SUNErrorParser extends ErrorParser {

    private static final Pattern SUN_ERROR_SCANNER_CPP_ERROR = Pattern.compile("^\"(.*)\", line ([0-9]+): Error:"); // NOI18N
    private static final Pattern SUN_ERROR_SCANNER_CPP_WARNING = Pattern.compile("^\"(.*)\", line ([0-9]+): Warning:"); // NOI18N
    private static final Pattern SUN_ERROR_SCANNER_C_ERROR = Pattern.compile("^\"(.*)\", line ([0-9]+):"); // NOI18N
    private static final Pattern SUN_ERROR_SCANNER_C_WARNING = Pattern.compile("^\"(.*)\", line ([0-9]+): warning:"); // NOI18N
    private static final Pattern SUN_ERROR_SCANNER_FORTRAN_ERROR = Pattern.compile("^\"(.*)\", Line = ([0-9]+),"); // NOI18N
    private static final Pattern SUN_ERROR_SCANNER_FORTRAN_WARNING = Pattern.compile("^\"(.*)\", Line = ([0-9]+), Column = ([0-9]+): WARNING:"); // NOI18N
    private static final Pattern SUN_DIRECTORY_ENTER = Pattern.compile("\\(([^)]*)\\)[^:]*:"); // NOI18N
    private static final Pattern[] patterns = new Pattern[]{SUN_ERROR_SCANNER_CPP_ERROR, SUN_ERROR_SCANNER_CPP_WARNING, SUN_ERROR_SCANNER_FORTRAN_WARNING,
                                                            SUN_ERROR_SCANNER_FORTRAN_ERROR, SUN_ERROR_SCANNER_C_WARNING, SUN_ERROR_SCANNER_C_ERROR, SUN_DIRECTORY_ENTER};
    private static final Pattern SS_OF_1 = Pattern.compile("::\\(.*\\)");// NOI18N
    private static final Pattern SS_OF_2 = Pattern.compile(":\\(.*\\).*");// NOI18N
    private static final Pattern SS_OF_3 = Pattern.compile("\\(.*\\).*:");// NOI18N
    private static final Pattern[] SunStudioOutputFilters = new Pattern[] {SS_OF_1, SS_OF_2, SS_OF_3};

    public SUNErrorParser(ExecutionEnvironment execEnv, FileObject relativeTo) {
        super(execEnv, relativeTo);
    }

    public Result handleLine(String line) throws IOException {
        Result res = handleLineImpl(line);
        if (res == null || res == NO_RESULT) {
            // Remove lines extra lines from Sun Compiler output
            for (int i = 0; i < SunStudioOutputFilters.length; i++) {
                Matcher skipper = SunStudioOutputFilters[i].matcher(line);
                boolean found = skipper.find();
                if (found && skipper.start() == 0) {
                    return REMOVE_LINE;
                }
            }
        }
        return res;
    }

    private Result handleLineImpl(String line) throws IOException {
        for (int pi = 0; pi < patterns.length; pi++) {
            Pattern p = patterns[pi];
            Matcher m = p.matcher(line);
            boolean found = m.find();
            if (found && m.start() == 0) {
                return handleLine(line, m);
            }
        }
        return null;
    }

    private Result handleLine(String line, Matcher m) throws IOException {
        if (m.pattern() == SUN_DIRECTORY_ENTER) {
            FileObject myObj = resolveFile(m.group(1));
            if (myObj != null) {
                relativeTo = myObj;
            }
            return NO_RESULT;
        }
        if (m.pattern() == SUN_ERROR_SCANNER_CPP_ERROR || m.pattern() == SUN_ERROR_SCANNER_CPP_WARNING || m.pattern() == SUN_ERROR_SCANNER_C_ERROR ||
            m.pattern() == SUN_ERROR_SCANNER_C_WARNING || m.pattern() == SUN_ERROR_SCANNER_FORTRAN_ERROR || m.pattern() == SUN_ERROR_SCANNER_FORTRAN_WARNING) {
            try {
                String file = m.group(1);
                Integer lineNumber = Integer.valueOf(m.group(2));
                //FileObject fo = relativeTo.getFileObject(file);
                FileObject fo = resolveRelativePath(relativeTo, file);
                boolean important = m.pattern() == SUN_ERROR_SCANNER_CPP_ERROR || m.pattern() == SUN_ERROR_SCANNER_C_ERROR ||
                                    m.pattern() == SUN_ERROR_SCANNER_FORTRAN_ERROR;
                if (fo != null) {
                    return new Results(line, new OutputListenerImpl(fo, lineNumber.intValue() - 1), important);
                }
            } catch (NumberFormatException e) {
            }
            return NO_RESULT;
        }
        throw new IllegalArgumentException("Unknown pattern: " + m.pattern().pattern()); // NOI18N
    }
}
