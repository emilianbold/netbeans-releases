/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.dwarfdump;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfEntry;
import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfMacinfoEntry;
import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfMacinfoTable;
import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfStatementList;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.LANG;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.MACINFO;
import org.netbeans.modules.cnd.dwarfdump.exception.WrongFileFormatException;

/**
 * Light weight service that gets command line from binary file in case Sun Studio compiler
 *
 * @author Alexander Simon
 */
public class CompileLineService {
    private static final String COMPILE_DIRECTORY = "\"directory\": "; //NOI18N
    private static final String SOURCE_FILE = "\"file\": "; //NOI18N
    private static final String COMMAND_LINE = "\"command\": "; //NOI18N
    private static final String SOURCE_PATH = "\"path\": "; //NOI18N
    private static final String LANGUAGE = "\"language\": "; //NOI18N
    private static final String MAIN = "\"main\": "; //NOI18N
    private static final String MAIN_LINE = "\"line\": "; //NOI18N
    private static final String DWARF_DUMP = "\"dwarf\": "; //NOI18N

    private CompileLineService() {
    }

    public static void main(String[] args){
        if (args.length < 2) {
            System.err.println("Not enough parameters."); // NOI18N
            System.err.println("Usage:"); // NOI18N
            System.err.println("java -cp org-netbeans-modules-cnd-dwarfdump.jar org.netbeans.modules.cnd.dwarfdump.CompileLineService -file binaryFileName [-dwarf]"); // NOI18N
            System.err.println("java -cp org-netbeans-modules-cnd-dwarfdump.jar org.netbeans.modules.cnd.dwarfdump.CompileLineService -folder folderName [-dwarf]"); // NOI18N
            return;
        }
        try {
            if (args.length == 3 &&  "-dwarf".equals(args[2])) { // NOI18N
                dump(args[0], args[1], true, System.out);
            } else {
                dump(args[0], args[1], false, System.out);
            }
        } catch (Throwable ex) {
            Dwarf.LOG.log(Level.INFO, "File "+args[1], ex); // NOI18N
        }
    }

    private static void dump(String kind, String objFileName, boolean dwarf, PrintStream out) throws IOException, Exception {
        List<SourceFile> res = null;
        if ("-file".equals(kind)){ // NOI18N
            res = getSourceFileProperties(objFileName, dwarf);
        } else if ("-folder".equals(kind)){ // NOI18N
            res = getSourceFolderProperties(objFileName, dwarf);
        } else {
            throw new Exception("Wrong arguments: "+kind+" "+objFileName); // NOI18N
        }
        out.println("["); // NOI18N
        boolean first = true;
        for(SourceFile entry : res) {
            if (!first) {
                out.println(","); // NOI18N
                
            }
            out.println("{"); // NOI18N
            boolean finished = true;
            finished = printLine(out, COMPILE_DIRECTORY, entry.compileDir, finished);
            finished = printLine(out, SOURCE_FILE, entry.sourceFile, finished);
            finished = printLine(out, COMMAND_LINE, entry.compileLine, finished);
            finished = printLine(out, SOURCE_PATH, entry.absolutePath, finished);
            finished = printLine(out, LANGUAGE, entry.sourceLanguage, finished);
            finished = printLine(out, MAIN, entry.hasMain, finished);
            finished = printLine(out, MAIN_LINE, entry.mainLine, finished);
            finished = printLine(out, DWARF_DUMP, entry.dwarfDump, finished);
            out.println(""); // NOI18N
            out.print("}"); // NOI18N
            first = false;
        }
        if (!first) {
            out.println(""); // NOI18N
        }
        out.println("]"); // NOI18N
    }

    private static boolean printLine(PrintStream out, String key, String value, boolean finished) {
        if (value != null && value.length() > 0) {
            if (!finished) {
                out.println(","); // NOI18N
            }
            out.print(" "); // NOI18N
            out.print(key);
            out.print("\""); // NOI18N
            out.print(value);
            out.print("\""); // NOI18N
            finished = false;
        }
        return finished;
    }

    private static boolean printLine(PrintStream out, String key, boolean value, boolean finished) {
        if (value) {
            if (!finished) {
                out.println(","); // NOI18N
                finished = false;
            }
            out.print(" "); // NOI18N
            out.print(key);
            out.print("true"); // NOI18N
        }
        return finished;
    }

    private static boolean printLine(PrintStream out, String key, int value, boolean finished) {
        if (value != 0) {
            if (!finished) {
                out.println(","); // NOI18N
                finished = false;
            }
            out.print(" "); // NOI18N
            out.print(key);
            out.print(""+value); // NOI18N
        }
        return finished;
    }
    
    public static List<SourceFile> getSourceProperties(BufferedReader out) throws IOException {
        return readSourceProperties(out);
    }

    private static List<SourceFile> readSourceProperties(BufferedReader out) throws IOException {
        List<SourceFile> list = new ArrayList<SourceFile>();
        String line;
        String compileDir = null;
        String sourceFile = null;
        String compileLine = null;
        String absolutePath = null;
        String sourceLanguage = null;
        boolean hasMain = false;
        int lineNumber = 0;
        String dwarf = null;
        while ((line=out.readLine())!= null){
            line = line.trim();
            if (line.startsWith("[")) { // NOI18N
                // start output
                continue;
            }
            if (line.startsWith("]")) { // NOI18N
                // end output
                continue;
            }
            if (line.startsWith("{")) { // NOI18N
                // start item
                compileDir = null;
                sourceFile = null;
                compileLine = null;
                absolutePath = null;
                sourceLanguage = null;
                hasMain = false;
                lineNumber = 0;
                dwarf = null;
                continue;
            }
            if (line.startsWith("}")) { // NOI18N
                final SourceFile src = new SourceFile(compileDir, sourceFile, compileLine, absolutePath, sourceLanguage, hasMain, lineNumber);
                if (dwarf != null) {
                    src.dwarfDump = dwarf;
                }
                list.add(src);
                continue;
            }
            if (line.startsWith(COMPILE_DIRECTORY)) {
                compileDir = removeQuotesAndComma(line.substring(COMPILE_DIRECTORY.length()));
                continue;
            }
            if (line.startsWith(SOURCE_FILE)) {
                sourceFile = removeQuotesAndComma(line.substring(SOURCE_FILE.length()));
                continue;
            }
            if (line.startsWith(COMMAND_LINE)) {
                compileLine = removeQuotesAndComma(line.substring(COMMAND_LINE.length()));
                continue;
            }
            if (line.startsWith(SOURCE_PATH)) {
                absolutePath = removeQuotesAndComma(line.substring(SOURCE_PATH.length()));
                continue;
            }
            if (line.startsWith(LANGUAGE)) {
                sourceLanguage = removeQuotesAndComma(line.substring(LANGUAGE.length()));
                continue;
            }
            if (line.startsWith(MAIN)) {
                hasMain = "true".equals(removeQuotesAndComma(line.substring(MAIN.length()))); // NOI18N
                continue;
            }
            if (line.startsWith(MAIN_LINE)) {
                lineNumber = Integer.parseInt(removeQuotesAndComma(line.substring(MAIN_LINE.length())));
                continue;
            }
            if (line.startsWith(DWARF_DUMP)) {
                dwarf = removeQuotesAndComma(line.substring(DWARF_DUMP.length()));
                continue;
            }
        }
        return list;
    }
    
    private static String removeQuotesAndComma(String str) {
        str = str.trim();
        if (str.endsWith(",")) { // NOI18N
            str = str.substring(0, str.length() - 1);
        }
        if (str.length() >= 2 && (str.charAt(0) == '\'' && str.charAt(str.length() - 1) == '\'' || // NOI18N
            str.charAt(0) == '"' && str.charAt(str.length() - 1) == '"')) {// NOI18N
            str = str.substring(1, str.length() - 1); // NOI18N
        }
        return str;
    }

    private static String removeQuotes(String str) {
        if (str.length() >= 2 && (str.charAt(0) == '\'' && str.charAt(str.length() - 1) == '\'' || // NOI18N
            str.charAt(0) == '"' && str.charAt(str.length() - 1) == '"')) {// NOI18N
            str = str.substring(1, str.length() - 1); // NOI18N
        }
        return str;
    }

    // valid on Solaris or Linux
    public static List<SourceFile> getSourceFolderProperties(String objFolderName, boolean dwarf) {
        List<SourceFile> list = new ArrayList<SourceFile>();
        for(String objFileName : getObjectFiles(objFolderName)) {
            list.addAll(getSourceFileProperties(objFileName, dwarf));
        }
        return list;
    }

    public static List<SourceFile> getSourceFileProperties(String objFileName, boolean dwarf) {
        List<SourceFile> list = new ArrayList<SourceFile>();
        Dwarf dump = null;
        try {
            dump = new Dwarf(objFileName);
            Dwarf.CompilationUnitIterator iterator = dump.iteratorCompilationUnits();
            while (iterator.hasNext()) {
                CompilationUnitInterface cu = iterator.next();
                if (cu != null) {
                    if (cu.getRoot() == null || cu.getSourceFileName() == null) {
                        if (Dwarf.LOG.isLoggable(Level.FINE)) {
                            Dwarf.LOG.log(Level.FINE, "Compilation unit has broken name in file {0}", objFileName);  // NOI18N
                        }
                        continue;
                    }
                    String lang = cu.getSourceLanguage();
                    if (lang == null) {
                        if (Dwarf.LOG.isLoggable(Level.FINE)) {
                            Dwarf.LOG.log(Level.FINE, "Compilation unit has unresolved language in file {0}for {1}", new Object[]{objFileName, cu.getSourceFileName()});  // NOI18N
                        }
                        continue;
                    }
                    if (LANG.DW_LANG_C.toString().equals(lang)
                            || LANG.DW_LANG_C89.toString().equals(lang)
                            || LANG.DW_LANG_C99.toString().equals(lang)
                            || LANG.DW_LANG_C_plus_plus.toString().equals(lang)) {
                        try {
                            list.add(createSourceFile(cu, dwarf));
                        } catch (IOException ex){
                            throw ex;
                        } catch (Exception ex){
                            if (Dwarf.LOG.isLoggable(Level.FINE)) {
                                Dwarf.LOG.log(Level.FINE, "Compilation unit {0} {1}", new Object[]{cu.getSourceFileName(), ex.getMessage()});  // NOI18N
                            }
                            continue;
                        }
                    } else {
                        if (Dwarf.LOG.isLoggable(Level.FINE)) {
                            Dwarf.LOG.log(Level.FINE, "Unknown language: {0}", lang);  // NOI18N
                        }
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            // Skip Exception
            if (Dwarf.LOG.isLoggable(Level.FINE)) {
                Dwarf.LOG.log(Level.FINE, "File not found {0}: {1}", new Object[]{objFileName, ex.getMessage()});  // NOI18N
            }
        } catch (WrongFileFormatException ex) {
            if (Dwarf.LOG.isLoggable(Level.FINE)) {
                Dwarf.LOG.log(Level.FINE, "Unsuported format of file {0}: {1}", new Object[]{objFileName, ex.getMessage()});  // NOI18N
            }
        } catch (IOException ex) {
            Dwarf.LOG.log(Level.INFO, "Exception in file " + objFileName, ex);  // NOI18N
        } catch (Exception ex) {
            Dwarf.LOG.log(Level.INFO, "Exception in file " + objFileName, ex);  // NOI18N
        } finally {
            if (dump != null) {
                dump.dispose();
            }
        }
        return list;
    }

    public static SourceFile createSourceFile(String compileDir, String sourceFile, String compileLine) {
        return new SourceFile(compileDir, sourceFile, compileLine, null, null, false, -1);
    }

    public static SourceFile createSourceFile(CompilationUnitInterface cu, boolean dwarf) throws IOException, Exception {
        SourceFile res = new SourceFile(cu);
        if (res.compileLine.length() == 0 && (cu instanceof CompilationUnit) && dwarf) {
            CompilationUnit dcu = (CompilationUnit)cu;
            StringBuilder buf = new StringBuilder();
            DwarfStatementList dwarfStatementTable = dcu.getStatementList();
            List<String> paths = new ArrayList<String>();
            if (dwarfStatementTable != null) {
                for (Iterator<String> it = dwarfStatementTable.getIncludeDirectories().iterator(); it.hasNext();) {
                    addpath(paths, it.next(), false);
                }
                for(String file : dwarfStatementTable.getFilePaths()) {
                    addpath(paths, file, true);
                }
            }
            for(String path : paths) {
                buf.append(" -I").append("'").append(path).append("'"); // NOI18N
            }
            DwarfMacinfoTable dwarfMacroTable = dcu.getMacrosTable();
            if (dwarfMacroTable != null) {
                List<DwarfMacinfoEntry> table = dwarfMacroTable.getCommandLineMarcos();
                for (Iterator<DwarfMacinfoEntry> it = table.iterator(); it.hasNext();) {
                    DwarfMacinfoEntry entry = it.next();
                    if ((entry.type == MACINFO.DW_MACINFO_define ||
                         entry.type == MACINFO.DW_MACRO_define_indirect) &&
                         entry.definition != null) {
                        String def = entry.definition;
                        int i = def.indexOf(' ');
                        if (i>0){
                            buf.append(" -D").append(def.substring(0,i)).append("='").append(def.substring(i+1).trim()).append("'"); // NOI18N
                        } else {
                            buf.append(" -D").append(def.substring(0,i)); // NOI18N
                        }
                    } else if ((entry.type == MACINFO.DW_MACINFO_undef ||
                         entry.type == MACINFO.DW_MACRO_undef_indirect) &&
                         entry.definition != null) {
                        buf.append(" -U").append(entry.definition); // NOI18N
                    }
                }
                if (dwarfStatementTable != null) {
                    List<Integer> commandLineIncludedFiles = dwarfMacroTable.getCommandLineIncludedFiles();
                    for(int i : commandLineIncludedFiles) {
                        String includedSource = dwarfStatementTable.getFilePath(i);
                        if (includedSource.startsWith("./")) { // NOI18N
                            includedSource = res.compileDir+includedSource.substring(1);
                        }
                        if (!res.absolutePath.equals(includedSource)) {
                            buf.append(" -include ").append("'").append(includedSource).append("'"); // NOI18N
                        }
                    }
                }
            }
            res.dwarfDump = buf.toString().trim();
        }
        return res;
    }

    private static void addpath(List<String> userIncludes, String path, boolean isFile){
         if (isFile) {
            int i = path.lastIndexOf('/'); // NOI18N
            if (i > 0) {
                path = path.substring(0, i);
            }
         }
         if (!userIncludes.contains(path)) {
             userIncludes.add(path);
        }
    }
    
    private static Set<String> getObjectFiles(String root){
        HashSet<String> map = new HashSet<String>();
        gatherSubFolders(new File(root), map, new HashSet<String>());
        return map;
    }

    private static boolean isExecutable(File file){
        String name = file.getName();
        return name.indexOf('.') < 0;
    }

    private static void gatherSubFolders(File d, HashSet<String> map, HashSet<String> antiLoop){
        if (d.exists() && d.isDirectory() && d.canRead()){
            if (ignoreFolder(d)){
                return;
            }
            String canPath;
            try {
                canPath = d.getCanonicalPath();
            } catch (IOException ex) {
                Dwarf.LOG.log(Level.INFO, "File "+d.getAbsolutePath(), ex);
                return;
            }
            if (!antiLoop.contains(canPath)){
                antiLoop.add(canPath);
                File[] ff = d.listFiles();
                if (ff != null) {
                    for (int i = 0; i < ff.length; i++) {
                        if (ff[i].isDirectory()) {
                            gatherSubFolders(ff[i], map, antiLoop);
                        } else if (ff[i].isFile()) {
                            String name = ff[i].getName();
                            if (name.endsWith(".o") ||  // NOI18N
                                name.endsWith(".so") || // NOI18N
                                name.endsWith(".a") ||  // NOI18N
                                isExecutable(ff[i])){
                                String path = ff[i].getAbsolutePath();
                                map.add(path);
                            }
                        }
                    }
                }
            }
        }
    }

    private static boolean ignoreFolder(File file){
        if (file.isDirectory()) {
            String name = file.getName();
            return name.equals("SCCS") || name.equals("CVS") || name.equals(".hg") || name.equals("SunWS_cache") || name.equals(".svn"); // NOI18N
        }
        return false;
    }

    public static final class SourceFile implements CompilationUnitInterface {

        private final String compileLine;
        private final String compileDir;
        private final String sourceFile;
        private String dwarfDump;
        private Map<String,String> userMacros;
        private List<String> userUndefs;
        private List<String> userPaths;
        private List<String> userIncludes;
        private final String absolutePath;
        private final String sourceLanguage;
        private final boolean hasMain;
        private final int mainLine;

        private SourceFile(CompilationUnitInterface cu) throws IOException, Exception {
            String s = cu.getCommandLine();
            if (s == null) {
                // client may be interested in compilation units also
                s = "";  // NOI18N
                //throw new Exception("Dwarf information does not contain compile line");  // NOI18N
            } 
            compileLine = s.trim();
            compileDir = cu.getCompilationDir();
            sourceFile = cu.getSourceFileName();
            if (sourceFile == null) {
                throw new Exception("Dwarf information does not contain source file name");  // NOI18N
            }
            absolutePath = cu.getSourceFileAbsolutePath();
            sourceLanguage = cu.getSourceLanguage();
            hasMain = cu.hasMain();
            mainLine = cu.getMainLine();
        }

        private SourceFile( String compileDir, String sourceFile, String compileLine, String absolutePath, String sourceLanguage, boolean hasMain, int mainLine) {
            this.compileLine = compileLine == null ? "" : compileLine;
            this.compileDir = compileDir;
            this.sourceFile = sourceFile;
            this.absolutePath = absolutePath;
            this.sourceLanguage = sourceLanguage;
            this.hasMain = hasMain;
            this.mainLine = mainLine;
        }

        public final String getCompilationDir() {
            return compileDir;
        }

        public final String getSourceFileName() {
            return sourceFile;
        }

        public final String getCommandLine() {
            return compileLine;
        }

        public DwarfEntry getRoot() {
            return null;
        }

        public final Map<String,String> getUserMacros() {
            if (userMacros == null) {
                initMacrosAndPaths();
            }
            return userMacros;
        }

        public final List<String> getUndefs() {
            if (userUndefs == null) {
                initMacrosAndPaths();
            }
            return userUndefs;
        }

        public final List<String> getUserPaths() {
            if (userPaths == null) {
                initMacrosAndPaths();
            }
            return userPaths;
        }

        public final List<String> getIncludeFiles() {
            if (userIncludes == null) {
                initMacrosAndPaths();
            }
            return userIncludes;
        }
        
        public String getSourceFileAbsolutePath() {
            return absolutePath;
        }

        public String getSourceLanguage() {
            return sourceLanguage;
        }

        public boolean hasMain() {
            return hasMain;
        }

        public int getMainLine() {
            return mainLine;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final SourceFile other = (SourceFile) obj;
            if ((this.compileLine == null) ? (other.compileLine != null) : !this.compileLine.equals(other.compileLine)) {
                return false;
            }
            if ((this.compileDir == null) ? (other.compileDir != null) : !this.compileDir.equals(other.compileDir)) {
                return false;
            }
            if ((this.sourceFile == null) ? (other.sourceFile != null) : !this.sourceFile.equals(other.sourceFile)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 97 * hash + (this.compileLine != null ? this.compileLine.hashCode() : 0);
            hash = 97 * hash + (this.compileDir != null ? this.compileDir.hashCode() : 0);
            hash = 97 * hash + (this.sourceFile != null ? this.sourceFile.hashCode() : 0);
            return hash;
        }

        @Override
        public String toString() {
            return "SourceFile{" + "compileLine=" + compileLine + ", compileDir=" + compileDir + ", sourceFile=" + sourceFile + '}'; // NOI18N
        }
        
        private void initMacrosAndPaths(){
            userPaths = new ArrayList<String>();
            userIncludes = new ArrayList<String>();
            userMacros = new LinkedHashMap<String, String>();
            userUndefs = new ArrayList<String>();
            Iterator<String> st = null;
            if (compileLine.length() > 0) {
                st = splitCommandLine(compileLine).iterator();
            } else if(dwarfDump != null && dwarfDump.length() > 0) {
                st = splitCommandLine(dwarfDump).iterator();
            } else {
                return;
            }
            while(st.hasNext()){
                String option = st.next();
                if (option.startsWith("--")) { // NOI18N
                    option = option.substring(1);
                }
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
                    userPaths.add(removeQuotes(path));
                } else if (option.startsWith("-U")){ // NOI18N
                    String macro = option.substring(2);
                    if (macro.length()==0 && st.hasNext()){
                        macro = st.next();
                    }
                    userUndefs.add(removeQuotes(macro));
                } else if (option.startsWith("-Y")){ // NOI18N
                    String defaultSearchPath = option.substring(2);
                    if (defaultSearchPath.length()==0 && st.hasNext()){
                        defaultSearchPath = st.next();
                    }
                    if (defaultSearchPath.startsWith("I,")){ // NOI18N
                        defaultSearchPath = defaultSearchPath.substring(2);
                        userPaths.add(removeQuotes(defaultSearchPath));
                    }
                } else if (option.startsWith("-isystem")){ // NOI18N
                    String path = option.substring(8);
                    if (path.length()==0 && st.hasNext()){
                        path = st.next();
                    }
                    userPaths.add(removeQuotes(path));
                } else if (option.startsWith("-include")){ // NOI18N
                    String path = option.substring(8);
                    if (path.length()==0 && st.hasNext()){
                        path = st.next();
                    }
                    userIncludes.add(removeQuotes(path));
                } else if (option.startsWith("-imacros")){ // NOI18N
                    String path = option.substring(8);
                    if (path.length()==0 && st.hasNext()){
                        path = st.next();
                    }
                    userIncludes.add(removeQuotes(path));
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
