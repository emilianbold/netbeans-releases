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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.filesystems.FileObject;

public final class GCCErrorParser extends ErrorParser {

    private static final Pattern GCC_ERROR_SCANNER = Pattern.compile("^([a-zA-Z]:[^:\n]*|[^:\n]*):([0-9]+)[\\.:]([^:\n]*):([^\n]*)"); // NOI18N
    private static final Pattern GCC_ERROR_SCANNER_ANOTHER = Pattern.compile("^([^:\n]*):([0-9]+): ([a-zA-Z]*):*.*"); // NOI18N
    private static final Pattern GCC_ERROR_SCANNER_INTEL = Pattern.compile("^([^\\(\n]*)\\(([0-9]+)\\): ([^:\n]*): ([^\n]*)"); // NOI18N
    private static final Pattern GCC_DIRECTORY_ENTER = Pattern.compile("[gd]?make(?:\\.exe)?(?:\\[([0-9]+)\\])?: Entering[\\w+\\s+]+`([^']*)'"); // NOI18N
    private static final Pattern GCC_DIRECTORY_LEAVE = Pattern.compile("[gd]?make(?:\\.exe)?(?:\\[([0-9]+)\\])?: Leaving[\\w+\\s+]+`([^']*)'"); // NOI18N
    private static final Pattern GCC_DIRECTORY_CD    = Pattern.compile("cd\\s+([\\S]+)[\\s;]");// NOI18N
    private static final Pattern GCC_STACK_HEADER = Pattern.compile("In file included from ([A-Z]:[^:\n]*|[^:\n]*):([^:^,]*)"); // NOI18N
    private static final Pattern GCC_STACK_NEXT =   Pattern.compile("                 from ([A-Z]:[^:\n]*|[^:\n]*):([^:^,]*)"); // NOI18N
    private static final Pattern[] patterns = new Pattern[]{GCC_DIRECTORY_ENTER, GCC_DIRECTORY_LEAVE, GCC_DIRECTORY_CD,
                                                            GCC_STACK_HEADER, GCC_STACK_NEXT, GCC_ERROR_SCANNER, GCC_ERROR_SCANNER_ANOTHER, GCC_ERROR_SCANNER_INTEL,
                                                            MSVCErrorParser.MSVC_WARNING_SCANNER, MSVCErrorParser.MSVC_ERROR_SCANNER};

    private Stack<FileObject> relativesTo = new Stack<FileObject>();
    private Stack<Integer> relativesLevel = new Stack<Integer>();
    private ArrayList<StackIncludeItem> errorInludes = new ArrayList<StackIncludeItem>();
    private boolean isEntered;

    public GCCErrorParser(ExecutionEnvironment execEnv, FileObject relativeTo) {
        super(execEnv, relativeTo);
        this.relativesTo.push(relativeTo);
        this.relativesLevel.push(0);
        this.isEntered = false;
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

    public Result handleLine(String line) throws IOException {
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
                if (!IpeUtils.isPathAbsolute(directory)) {
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
                return NO_RESULT;
            } else {
                popPath();
                return NO_RESULT;
            }
        }
        if (m.pattern() == GCC_DIRECTORY_CD) {
            String directory = m.group(1);
            if (!IpeUtils.isPathAbsolute(directory)) {
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
            return NO_RESULT;
        }
        if (m.pattern() == GCC_STACK_HEADER) {
            Results res = new Results();
            for (Iterator<StackIncludeItem> it = errorInludes.iterator(); it.hasNext();) {
                StackIncludeItem item = it.next();
                res.add(item.line, null, false);
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
        if ((m.pattern() == GCC_ERROR_SCANNER) || (m.pattern() == GCC_ERROR_SCANNER_ANOTHER) || (m.pattern() == GCC_ERROR_SCANNER_INTEL) ||
            (m.pattern() == MSVCErrorParser.MSVC_WARNING_SCANNER) || (m.pattern() == MSVCErrorParser.MSVC_ERROR_SCANNER)) {
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
                                res.add(item.line, new OutputListenerImpl(item.fo, item.lineNumber), important);
                            } else {
                                res.add(item.line, null, false);
                            }
                        }
                        errorInludes.clear();
                        res.add(line, new OutputListenerImpl(fo, lineNumber.intValue() - 1), important);
                        return res;
                    }
                }
            } catch (NumberFormatException e) {
            }
            for (Iterator<StackIncludeItem> it = errorInludes.iterator(); it.hasNext();) {
                StackIncludeItem item = it.next();
                res.add(item.line, null, false);
            }
            errorInludes.clear();
            res.add(line, null, false);
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
