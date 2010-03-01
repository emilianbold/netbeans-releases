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

package org.netbeans.modules.cnd.toolchain.execution;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.cnd.api.toolchain.CompilerFlavor;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager.ScannerDescriptor;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager.ScannerPattern;
import org.netbeans.modules.cnd.spi.toolchain.ErrorParserProvider;
import org.netbeans.modules.cnd.spi.toolchain.ErrorParserProvider.Result;
import org.netbeans.modules.cnd.spi.toolchain.ErrorParserProvider.Results;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.filesystems.FileObject;

public final class SUNErrorParser extends ErrorParser {

    private final List<Pattern> errorScuners = new ArrayList<Pattern>();
    private final List<Pattern> patterns = new ArrayList<Pattern>();
    private final List<String> severity = new ArrayList<String>();
    private final List<Pattern> SunStudioOutputFilters = new ArrayList<Pattern>();
    private Pattern SUN_DIRECTORY_ENTER;
    private final OutputListenerFactory listenerFactory = new OutputListenerFactory();


    public SUNErrorParser(CompilerFlavor flavor, ExecutionEnvironment execEnv, FileObject relativeTo) {
        super(execEnv, relativeTo);
	init(flavor);
    }

    private void init(CompilerFlavor flavor) {
	ScannerDescriptor scanner = flavor.getToolchainDescriptor().getScanner();
	for(ScannerPattern s : scanner.getPatterns()){
	    Pattern pattern = Pattern.compile(s.getPattern());
	    patterns.add(pattern);
	    severity.add(s.getSeverity());
	    errorScuners.add(pattern);
	}
	if (scanner.getEnterDirectoryPattern() != null) {
	    SUN_DIRECTORY_ENTER = Pattern.compile(scanner.getEnterDirectoryPattern());
	    patterns.add(SUN_DIRECTORY_ENTER);
	}
	for(String s : scanner.getFilterOutPatterns()){
	    SunStudioOutputFilters.add(Pattern.compile(s));
	}
    }

    @Override
    public Result handleLine(String line) throws IOException {
        Result res = handleLineImpl(line);
        if (res == null || res == ErrorParserProvider.NO_RESULT) {
            // Remove lines extra lines from Sun Compiler output
            for (Pattern pattern : SunStudioOutputFilters) {
                Matcher skipper = pattern.matcher(line);
                boolean found = skipper.find();
                if (found && skipper.start() == 0) {
                    return ErrorParserProvider.REMOVE_LINE;
                }
            }
        }
        return res;
    }

    private Result handleLineImpl(String line) throws IOException {
        for (Pattern p : patterns) {
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
            return ErrorParserProvider.NO_RESULT;
        }
	int i = errorScuners.indexOf(m.pattern());
        if (i >= 0) {
            try {
                String file = m.group(1);
                Integer lineNumber = Integer.valueOf(m.group(2));
                //FileObject fo = relativeTo.getFileObject(file);
                FileObject fo = resolveRelativePath(relativeTo, file);
                boolean important = severity.get(i).equals("error"); // NOI18N
                if (fo != null) {
                    String description = null;
                    if (m.groupCount()<= 3) {
                        description = m.group(3);
                    }
                    return new Results(line, listenerFactory.register(fo, lineNumber.intValue() - 1, important, description));
                }
            } catch (NumberFormatException e) {
            }
            return ErrorParserProvider.NO_RESULT;
        }
        throw new IllegalArgumentException("Unknown pattern: " + m.pattern().pattern()); // NOI18N
    }
}
