/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.dwarfdump;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.LANG;
import org.netbeans.modules.cnd.dwarfdump.exception.WrongFileFormatException;

/**
 * Lite weight service that gets command line from binary file in case Sun Studio compiler
 *
 * @author Alexander Simon
 */
public class CompileLineService {

    private static final boolean TRACE_READ_EXCEPTIONS = false;

    private CompileLineService() {
    }

    public static List<SourceFile> getSourceFileProperties(String objFileName) {
        List<SourceFile> list = new ArrayList<SourceFile>();
        Dwarf dump = null;
        try {
            dump = new Dwarf(objFileName);
            Iterator<CompilationUnit> iterator = dump.iteratorCompilationUnits();
            while (iterator.hasNext()) {
                CompilationUnit cu = iterator.next();
                if (cu != null) {
                    if (cu.getRoot() == null || cu.getSourceFileName() == null) {
                        if (TRACE_READ_EXCEPTIONS) {
                            System.out.println("Compilation unit has broken name in file " + objFileName);  // NOI18N
                        }
                        continue;
                    }
                    String lang = cu.getSourceLanguage();
                    if (lang == null) {
                        if (TRACE_READ_EXCEPTIONS) {
                            System.out.println("Compilation unit has unresolved language in file " + objFileName + "for " + cu.getSourceFileName());  // NOI18N
                        }
                        continue;
                    }
                    if (LANG.DW_LANG_C.toString().equals(lang)
                            || LANG.DW_LANG_C89.toString().equals(lang)
                            || LANG.DW_LANG_C99.toString().equals(lang)
                            || LANG.DW_LANG_C_plus_plus.toString().equals(lang)) {
                        list.add(new SourceFile(cu));
                    } else {
                        if (TRACE_READ_EXCEPTIONS) {
                            System.out.println("Unknown language: " + lang);  // NOI18N
                        }
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            // Skip Exception
            if (TRACE_READ_EXCEPTIONS) {
                System.out.println("File not found " + objFileName + ": " + ex.getMessage());  // NOI18N
            }
        } catch (WrongFileFormatException ex) {
            if (TRACE_READ_EXCEPTIONS) {
                System.out.println("Unsuported format of file " + objFileName + ": " + ex.getMessage());  // NOI18N
            }
        } catch (IOException ex) {
            if (TRACE_READ_EXCEPTIONS) {
                System.err.println("Exception in file " + objFileName);  // NOI18N
                ex.printStackTrace();
            }
        } catch (Exception ex) {
            if (TRACE_READ_EXCEPTIONS) {
                System.err.println("Exception in file " + objFileName);  // NOI18N
                ex.printStackTrace();
            }
        } finally {
            if (dump != null) {
                dump.dispose();
            }
        }
        return list;
    }

    public static final class SourceFile {

        private final String compileLine;
        private final String compileDir;
        private final String sourceFile;
        private Map<String,String> userMacros;
        private List<String> userPaths;

        private SourceFile(CompilationUnit cu) throws IOException, Exception {
            compileLine = cu.getCommandLine();
            if (compileLine == null) {
                throw new Exception("Dwarf information dies not contain compile line");  // NOI18N
            }
            compileDir = cu.getCompilationDir();
            if (compileDir == null) {
                throw new Exception("Dwarf information dies not contain compile dir");  // NOI18N
            }
            sourceFile = cu.getSourceFileName();
            if (sourceFile == null) {
                throw new Exception("Dwarf information dies not contain source file name");  // NOI18N
            }
        }

        public final String getCompileDir() {
            return compileDir;
        }

        public final String getSource() {
            return sourceFile;
        }

        public final String getCompileLine() {
            return compileLine;
        }

        public final Map<String,String> getUserMacros() {
            if (userMacros == null) {
                initMacrosAndPaths();
            }
            return userMacros;
        }

        public final List<String> getUserPaths() {
            if (userPaths == null) {
                initMacrosAndPaths();
            }
            return userPaths;
        }

        private void initMacrosAndPaths(){
            userPaths = new ArrayList<String>();
            userMacros = new LinkedHashMap<String, String>();
            Iterator<String> st = splitCommandLine(compileLine).iterator();
            while(st.hasNext()){
                String option = st.next();
                if (option.startsWith("-D")){ // NOI18N
                    String macro = option.substring(2);
                    int i = macro.indexOf('=');
                    if (i>0){
                        String value = macro.substring(i+1).trim();
                        if (value.length() >= 2 &&
                           (value.charAt(0) == '\'' && value.charAt(value.length()-1) == '\'' || // NOI18N
                            value.charAt(0) == '"' && value.charAt(value.length()-1) == '"' )) { // NOI18N
                            value = value.substring(1,value.length()-1);
                        }
                        userMacros.put(macro.substring(0,i), value);
                    } else {
                        userMacros.put(macro, null);
                    }
                } else if (option.startsWith("-I")){ // NOI18N
                    String path = option.substring(2);
                    if (path.length()==0 && st.hasNext()){
                        path = st.next();
                    }
                    userPaths.add(path);
                } else if (option.startsWith("-Y")){ // NOI18N
                    String defaultSearchPath = option.substring(2);
                    if (defaultSearchPath.length()==0 && st.hasNext()){
                        defaultSearchPath = st.next();
                    }
                    if (defaultSearchPath.startsWith("I,")){ // NOI18N
                        defaultSearchPath = defaultSearchPath.substring(2);
                        userPaths.add(defaultSearchPath);
                    }
                } else if (option.startsWith("-isystem")){ // NOI18N
                    String path = option.substring(8);
                    if (path.length()==0 && st.hasNext()){
                        path = st.next();
                    }
                    userPaths.add(path);
                } else if (option.startsWith("-include")){ // NOI18N
                    String path = option.substring(8);
                    if (path.length()==0 && st.hasNext()){
                        path = st.next();
                    }
                    userPaths.add(path);
                } else if (option.startsWith("-imacros")){ // NOI18N
                    String path = option.substring(8);
                    if (path.length()==0 && st.hasNext()){
                        path = st.next();
                    }
                    userPaths.add(path);
                }
            }
        }

        private List<String> splitCommandLine(String line) {
            List<String> res = new ArrayList<String>();
            int i = 0;
            StringBuilder current = new StringBuilder();
            boolean isSingleQuoteMode = false;
            boolean isDoubleQuoteMode = false;
            while (i < line.length()) {
                char c = line.charAt(i);
                i++;
                switch (c) {
                    case '\'': // NOI18N
                        if (isSingleQuoteMode) {
                            isSingleQuoteMode = false;
                        } else if (!isDoubleQuoteMode) {
                            isSingleQuoteMode = true;
                        }
                        current.append(c);
                        break;
                    case '\"': // NOI18N
                        if (isDoubleQuoteMode) {
                            isDoubleQuoteMode = false;
                        } else if (!isSingleQuoteMode) {
                            isDoubleQuoteMode = true;
                        }
                        current.append(c);
                        break;
                    case ' ': // NOI18N
                    case '\t': // NOI18N
                    case '\n': // NOI18N
                    case '\r': // NOI18N
                        if (isSingleQuoteMode || isDoubleQuoteMode) {
                            current.append(c);
                            break;
                        } else {
                            if (current.length() > 0) {
                                res.add(current.toString());
                                current.setLength(0);
                            }
                        }
                        break;
                    default:
                        current.append(c);
                        break;
                }
            }
            if (current.length() > 0) {
                res.add(current.toString());
            }
            return res;
        }
    }
}
