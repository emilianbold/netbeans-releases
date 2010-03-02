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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.cnd.api.toolchain.CompilerFlavor;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager.ScannerDescriptor;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager.ScannerPattern;
import org.netbeans.modules.cnd.toolchain.compilerset.ToolUtils;
import org.netbeans.modules.cnd.spi.toolchain.ErrorParserProvider;
import org.netbeans.modules.cnd.spi.toolchain.ErrorParserProvider.Result;
import org.netbeans.modules.cnd.spi.toolchain.ErrorParserProvider.Results;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

public final class GCCErrorParser extends ErrorParser {

    private final List<Pattern> GCC_ERROR_SCANNER = new ArrayList<Pattern>();
    private final List<Pattern> patterns = new ArrayList<Pattern>();
    private Pattern GCC_DIRECTORY_ENTER;
    private Pattern GCC_DIRECTORY_LEAVE;
    private Pattern GCC_DIRECTORY_CD;
    private Pattern GCC_DIRECTORY_MAKE_ALL;
    private Pattern GCC_STACK_HEADER;
    private Pattern GCC_STACK_NEXT;

    private Stack<FileObject> relativesTo = new Stack<FileObject>();
    private Stack<Integer> relativesLevel = new Stack<Integer>();
    private ArrayList<StackIncludeItem> errorInludes = new ArrayList<StackIncludeItem>();
    private boolean isEntered;
    private final OutputListenerFactory listenerFactory = new OutputListenerFactory();

    public GCCErrorParser(CompilerFlavor flavor, ExecutionEnvironment execEnv, FileObject relativeTo) {
        super(execEnv, relativeTo);
        this.relativesTo.push(relativeTo);
        this.relativesLevel.push(0);
        this.isEntered = false;
	init(flavor);
    }

    private void init(CompilerFlavor flavor) {
	ScannerDescriptor scanner = flavor.getToolchainDescriptor().getScanner();
	if (scanner.getEnterDirectoryPattern() != null) {
	    GCC_DIRECTORY_ENTER = Pattern.compile(scanner.getEnterDirectoryPattern());
	    patterns.add(GCC_DIRECTORY_ENTER);
	}
	if (scanner.getLeaveDirectoryPattern() != null) {
	    GCC_DIRECTORY_LEAVE = Pattern.compile(scanner.getLeaveDirectoryPattern());
	    patterns.add(GCC_DIRECTORY_LEAVE);
	}
	if (scanner.getChangeDirectoryPattern() != null) {
	    GCC_DIRECTORY_CD = Pattern.compile(scanner.getChangeDirectoryPattern());
	    patterns.add(GCC_DIRECTORY_CD);
	}
	if (scanner.getChangeDirectoryPattern() != null) {
	    GCC_DIRECTORY_MAKE_ALL = Pattern.compile(scanner.getMakeAllInDirectoryPattern());
	    patterns.add(GCC_DIRECTORY_MAKE_ALL);
	}
	if (scanner.getStackHeaderPattern() != null && scanner.getStackHeaderPattern() != null) {
	    GCC_STACK_HEADER = Pattern.compile(scanner.getStackHeaderPattern());
	    patterns.add(GCC_STACK_HEADER);
	    GCC_STACK_NEXT = Pattern.compile(scanner.getStackHeaderPattern());
	    patterns.add(GCC_STACK_NEXT);
	}
	for(ScannerPattern s : scanner.getPatterns()){
	    Pattern pattern = Pattern.compile(s.getPattern());
	    GCC_ERROR_SCANNER.add(pattern);
	    patterns.add(pattern);
	}
    }

    // FIXUP IZ#115960 and all other about EmptyStackException
    // - make Stack.pop() and peek() safe.
    private void popPath() {
        if (relativesTo.size() > 1) {
            relativesTo.pop();
        }
    }

    private void popLevel() {
        if (relativesLevel.size() > 1) {
            relativesLevel.pop();
        }
    }

    @Override
    public Result handleLine(String line) throws IOException {
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
        if (m.pattern() == GCC_DIRECTORY_ENTER || m.pattern() == GCC_DIRECTORY_LEAVE) {
            String levelString = m.group(1);
            int level = levelString == null ? 0 : Integer.valueOf(levelString);
            int baseLavel = relativesLevel.peek().intValue();
            String directory = m.group(2);
            if (level > baseLavel) {
                isEntered = true;
                relativesLevel.push(level);
                isEntered = true;
            } else if (level == baseLavel) {
                isEntered = !this.isEntered;
            } else {
                isEntered = false;
                popLevel();
            }
            if (isEntered) {
                if (!ToolUtils.isPathAbsolute(directory)) {
                    if (relativeTo != null) {
                        if (relativeTo.isFolder()) {
                            directory = relativeTo.getURL().getPath() + File.separator + directory;
                        }
                    }
                }
                FileObject relativeDir = resolveFile(directory);
                if (relativeDir != null) {
                    relativesTo.push(relativeDir);
                }
                return ErrorParserProvider.NO_RESULT;
            } else {
                popPath();
                return ErrorParserProvider.NO_RESULT;
            }
        }
        if (m.pattern() == GCC_DIRECTORY_CD) {
            String directory = m.group(1);
            if (!ToolUtils.isPathAbsolute(directory)) {
                if (relativeTo != null) {
                    if (relativeTo.isFolder()) {
                        directory = relativeTo.getURL().getPath() + File.separator + directory;
                    }
                }
            }
            FileObject relativeDir = resolveFile(directory);
            if (relativeDir != null) {
                relativesTo.push(relativeDir);
            }
            return ErrorParserProvider.NO_RESULT;
        }
        if (m.pattern() == GCC_DIRECTORY_MAKE_ALL) {
            FileObject relativeDir = relativesTo.peek();
            String directory = m.group(1);
            if (!ToolUtils.isPathAbsolute(directory)) {
                if (relativeDir != null) {
                    if (relativeDir.isFolder()) {
                        directory = relativeDir.getURL().getPath() + File.separator + directory;
                    }
                }
            }
            relativeDir = resolveFile(directory);
            if (relativeDir != null) {
                relativesTo.push(relativeDir);
            }
            return ErrorParserProvider.NO_RESULT;
        }
        if (m.pattern() == GCC_STACK_HEADER) {
            Results res = new Results();
            for (Iterator<StackIncludeItem> it = errorInludes.iterator(); it.hasNext();) {
                StackIncludeItem item = it.next();
                res.add(item.line, null);
            }
            errorInludes.clear();
            try {
                String file = m.group(1);
                Integer lineNumber = Integer.valueOf(m.group(2));
                FileObject relativeDir = relativesTo.peek();
                if (relativeDir != null) {
                    FileObject fo = resolveRelativePath(relativeDir, file);
                    if (fo != null) {
                        errorInludes.add(new StackIncludeItem(fo, line, lineNumber.intValue() - 1));
                        return res;
                    }
                }
            } catch (NumberFormatException e) {
            }
            errorInludes.add(new StackIncludeItem(null, line, 0));
            return res;
        }
        if (m.pattern() == GCC_STACK_NEXT) {
            try {
                String file = m.group(1);
                Integer lineNumber = Integer.valueOf(m.group(2));
                FileObject relativeDir = relativesTo.peek();
                if (relativeDir != null) {
                    FileObject fo = resolveRelativePath(relativeDir, file);
                    if (fo != null) {
                        errorInludes.add(new StackIncludeItem(fo, line, lineNumber.intValue() - 1));
                        return new Results();
                    }
                }
            } catch (NumberFormatException e) {
            }
            errorInludes.add(new StackIncludeItem(null, line, 0));
            return new Results();
        }
        if (GCC_ERROR_SCANNER.contains(m.pattern())) {
            Results res = new Results();
            try {
                String file = m.group(1);
                Integer lineNumber = Integer.valueOf(m.group(2));
                FileObject relativeDir = relativesTo.peek();
                if (relativeDir != null) {
                    //FileObject fo = relativeDir.getFileObject(file);
                    FileObject fo = resolveRelativePath(relativeDir, file);
                    boolean important = m.group(3).indexOf("error") != (-1); // NOI18N
                    if (fo != null) {
                        for (Iterator<StackIncludeItem> it = errorInludes.iterator(); it.hasNext();) {
                            StackIncludeItem item = it.next();
                            if (item.fo != null) {
                                res.add(item.line, listenerFactory.register(item.fo, item.lineNumber, important,
                                        NbBundle.getMessage(GCCErrorParser.class, "HINT_IncludedFrom"))); // NOI18N
                            } else {
                                res.add(item.line, null);
                            }
                        }
                        errorInludes.clear();
                        String description = null;
                        if (m.groupCount()<= 4) {
                            description = m.group(4);
                        }
                        res.add(line, listenerFactory.register(fo, lineNumber.intValue() - 1, important, description));
                        return res;
                    }
                }
            } catch (NumberFormatException e) {
            }
            for (Iterator<StackIncludeItem> it = errorInludes.iterator(); it.hasNext();) {
                StackIncludeItem item = it.next();
                res.add(item.line, null);
            }
            errorInludes.clear();
            res.add(line, null);
            return res;
        }
        throw new IllegalArgumentException("Unknown pattern: " + m.pattern().pattern()); // NOI18N
    }

    private static class StackIncludeItem {

        private FileObject fo;
        private String line;
        private int lineNumber;

        private StackIncludeItem(FileObject fo, String line, int lineNumber) {
            super();
            this.fo = fo;
            this.line = line;
            this.lineNumber = lineNumber;
        }
    }
}
