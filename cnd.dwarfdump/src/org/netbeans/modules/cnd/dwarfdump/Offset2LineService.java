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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfEntry;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.TAG;
import org.netbeans.modules.cnd.dwarfdump.section.DwarfLineInfoSection.LineNumber;

/**
 *
 * @author Alexander Simon
 */
public class Offset2LineService {
    private static final boolean TRACE = false;
    private Map<String, String> onePath;

    private Offset2LineService() {
    }

    public static void main(String[] args){
        if (args.length < 1) {
            System.err.println("Not enough parameters."); // NOI18N
            System.err.println("Usage:"); // NOI18N
            System.err.println("java -cp org-netbeans-modules-cnd-dwarfdump.jar org.netbeans.modules.cnd.dwarfdump.Offset2LineService binaryFileName"); // NOI18N
            return;
        }
        try {
            dump(args[0], System.out);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    private static void dump(String executable, PrintStream out) throws IOException {
        Map<String, AbstractFunctionToLine> res = getOffset2Line(executable);
        for(Map.Entry<String, AbstractFunctionToLine> entry : res.entrySet()) {
            out.println(entry.getKey());
            entry.getValue().dump(out);
        }
    }

    public static Map<String, AbstractFunctionToLine> getOffset2Line(BufferedReader out) throws IOException {
        return new Offset2LineService().readOffset2Line(out);
    }

    public static Map<String, AbstractFunctionToLine> getOffset2Line(String executable) throws IOException {
        return new Offset2LineService().getSourceInfo(executable);
    }


    private Map<String, AbstractFunctionToLine> readOffset2Line(BufferedReader out) throws IOException {
        onePath = new HashMap<String, String>();
        Map<String, AbstractFunctionToLine> sourceInfoMap = new HashMap<String, AbstractFunctionToLine>();
        String line;
        int state = 0;
        String functionName = null;
        String fileName = null;
        int baseLine = 0;
        List<Integer> lines = new ArrayList<Integer>();
        List<Integer> offsets = new ArrayList<Integer>();
        while ((line=out.readLine())!= null){
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }
            switch (state) {
                case 0:
                    // read function name
                    functionName = line;
                    state++;
                    break;
                case 1:
                    // read file name
                    fileName = line;
                    state++;
                    break;
                case 2:
                    // read base name
                    try {
                        baseLine = Integer.parseInt(line);
                        lines.clear();
                        offsets.clear();
                        state++;
                    } catch (NumberFormatException ex) {
                        state = 0;
                    }
                    break;
                case 3:
                    char c = line.charAt(0);
                    if (c >= '0' && c <= '9') {
                        // line-offset table
                        int i = line.indexOf(',');
                        if (i > 0) {
                            try {
                                Integer lineNumber = Integer.valueOf(line.substring(0,i));
                                Integer offset = Integer.valueOf(line.substring(i+1));
                                lines.add(lineNumber);
                                offsets.add(offset);
                            } catch (NumberFormatException ex) {
                                state = 0;
                            }
                        }
                    } else {
                        // end of table
                        sourceInfoMap.put(functionName, createAbstractFunctionToLine(fileName, baseLine, lines, offsets));
                        functionName = line;
                        state = 1;
                    }
                    break;
            }
        }
        if (state > 1) {
            sourceInfoMap.put(functionName, createAbstractFunctionToLine(fileName, baseLine, lines, offsets));
        }
        onePath = null;
        return sourceInfoMap;
    }

    private AbstractFunctionToLine createAbstractFunctionToLine(String fileName, int baseLine,
            List<Integer> lines, List<Integer> offsets) {
        if (lines.isEmpty()) {
            return new DeclarationToLine(fileName, baseLine, onePath);
        } else {
            return new FunctionToLine(fileName, baseLine, lines, offsets, onePath);
        }
    }

    private Map<String, AbstractFunctionToLine> getSourceInfo(String executable) throws IOException {
        onePath = new HashMap<String, String>();
        Map<String, AbstractFunctionToLine> sourceInfoMap = new HashMap<String, AbstractFunctionToLine>();
        if (TRACE) {
            System.err.println("Process file: "+executable); // NOI18N
        }
        Dwarf dwarf = new Dwarf(executable);
        try {
            Iterator<CompilationUnit> iterator = dwarf.iteratorCompilationUnits();
            while(iterator.hasNext()) {
                CompilationUnit compilationUnit = iterator.next();
                TreeSet<LineNumber> lineNumbers = getCompilationUnitLines(compilationUnit);
                String filePath = compilationUnit.getSourceFileAbsolutePath();
                String compDir = compilationUnit.getCompilationDir();
                Set<Long> antiLoop = new HashSet<Long>();
                processEntries(compilationUnit, compilationUnit.getDeclarations(false), filePath, compDir, lineNumbers, sourceInfoMap, antiLoop);
            }
        } finally {
            dwarf.dispose();
        }
        onePath = null;
        return sourceInfoMap;
    }

    private void processEntries(CompilationUnit compilationUnit, List<DwarfEntry> declarations, String filePath, String compDir,
            TreeSet<LineNumber> lineNumbers, Map<String, AbstractFunctionToLine> sourceInfoMap, Set<Long> antiLoop) throws IOException {
        for (DwarfEntry entry : declarations) {
            prosessEntry(compilationUnit, entry, filePath, compDir, lineNumbers, sourceInfoMap, antiLoop);
        }
    }

    private void prosessEntry(CompilationUnit compilationUnit, DwarfEntry entry, String filePath, String compDir,
            TreeSet<LineNumber> lineNumbers, Map<String, AbstractFunctionToLine> sourceInfoMap, Set<Long> antiLoop) throws IOException {
        if (antiLoop.contains(entry.getRefference())) {
            return;
        }
        antiLoop.add(entry.getRefference());
        switch (entry.getKind()) {
            case DW_TAG_subprogram:
            {
                if (entry.getLine() < 0 || entry.getDeclarationFilePath() == null) {
                    return;
                }
                if (entry.getLowAddress() == 0) {
                    DeclarationToLine functionToLine = new DeclarationToLine(filePath, compDir, entry, onePath);
                    sourceInfoMap.put(entry.getQualifiedName(), functionToLine);
                    if (TRACE) {
                        System.err.println("Function: "+entry.getQualifiedName()); // NOI18N
                        System.err.println(functionToLine);
                    }
                } else {
                    FunctionToLine functionToLine = new FunctionToLine(filePath, compDir, entry, lineNumbers, onePath);
                    sourceInfoMap.put(entry.getQualifiedName(), functionToLine);
                    if (TRACE) {
                        System.err.println("Function: "+entry.getQualifiedName()); // NOI18N
                        System.err.println(functionToLine);
                    }
                }
                break;
            }
            case DW_TAG_structure_type:
            case DW_TAG_class_type:
                processEntries(compilationUnit, entry.getChildren(), filePath, compDir, lineNumbers, sourceInfoMap, antiLoop);
                break;
            case DW_TAG_typedef:
            case DW_TAG_const_type:
            case DW_TAG_pointer_type:
            case DW_TAG_reference_type:
            case DW_TAG_array_type:
            case DW_TAG_ptr_to_member_type:
            {
                DwarfEntry type = compilationUnit.getReferencedType(entry);
                if (type != null) {
                    prosessEntry(compilationUnit, type, filePath, compDir, lineNumbers, sourceInfoMap, antiLoop);
                }
                break;
            }
        }
    }

    private static TreeSet<LineNumber> getCompilationUnitLines(CompilationUnit unit) throws IOException{
        Set<LineNumber> numbers = unit.getLineNumbers();
        return new TreeSet<LineNumber>(numbers);
    }

    public static final class SourceLineInfo {
        private final CharSequence fileName;
        private final int lineNumber;
        public SourceLineInfo(CharSequence fileName, int lineNumber) {
            this.fileName = fileName;
            this.lineNumber = lineNumber;
        }

        public String getFileName() {
            return fileName.toString();
        }

        public int getLine() {
            return lineNumber;
        }

        @Override
        public String toString() {
            return fileName.toString() + ':' + lineNumber;
        }
    }

    public static abstract class AbstractFunctionToLine {
        public abstract SourceLineInfo getLine(int offset);

        protected abstract void dump(PrintStream out);

        protected String initPath(String filePath, String compDir, DwarfEntry entry, Map<String, String> onePath) throws IOException{
            String res = _initPath(filePath, compDir, entry);
            return getPath(res, onePath);
        }

        protected String getPath(String path, Map<String, String> onePath) {
            String cached = onePath.get(path);
            if (cached == null) {
                onePath.put(path, path);
                cached = path;
            }
            return cached;
        }

        private String _initPath(String filePath, String compDir, DwarfEntry entry) throws IOException{
            String entyFilePath = entry.getDeclarationFilePath();
            if (entyFilePath != null && filePath.endsWith(entyFilePath)) {
                return filePath;
            } else {
                if (entyFilePath != null &&
                        (entyFilePath.startsWith("/") || // NOI18N
                         entyFilePath.length()>2 && entyFilePath.charAt(1) == ':')){ // NOI18N
                    return entyFilePath;
                } else {
                    if (compDir.endsWith("/") || compDir.endsWith("\\")) { // NOI18N
                        return compDir+entyFilePath;
                    } else {
                        return compDir+"/"+entyFilePath; // NOI18N
                    }
                }
            }
        }
    }

    private static final class DeclarationToLine extends AbstractFunctionToLine {
        private final int baseLine;
        private final String filePath;

        public DeclarationToLine(String filePath, String compDir, DwarfEntry entry, Map<String, String> onePath) throws IOException {
            assert entry.getKind() == TAG.DW_TAG_subprogram;
            baseLine = entry.getLine();
            this.filePath = initPath(filePath, compDir, entry, onePath);
        }

        public DeclarationToLine(String filePath, int baseLine, Map<String, String> onePath) {
            this.baseLine = baseLine;
            this.filePath = getPath(filePath, onePath);
        }

        @Override
        public SourceLineInfo getLine(int offset){
            return new SourceLineInfo(filePath, baseLine);
        }

        @Override
        public String toString() {
            StringBuilder buf = new StringBuilder("File: "+filePath); // NOI18N
            buf.append("\n\tBase Line:  ").append(baseLine); // NOI18N
            return buf.toString();
        }

        @Override
        protected void dump(PrintStream out) {
            out.println(filePath);
            out.println(""+baseLine); // NOI18N
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof DeclarationToLine) {
                DeclarationToLine other = (DeclarationToLine) obj;
                if (!filePath.equals(other.filePath)) {
                    return false;
                }
                if (baseLine != other.baseLine) {
                    return false;
                }
                return true;
            }
            return false;
        }
    }

    private static final class FunctionToLine extends AbstractFunctionToLine {
        private final int[] lineStorage;
        private final int[] offsetStorage;
        private final int baseLine;
        private final String filePath;

        public FunctionToLine(String filePath, String compDir, DwarfEntry entry, TreeSet<LineNumber> numbers, Map<String, String> onePath) throws IOException {
            assert entry.getKind() == TAG.DW_TAG_subprogram;
            assert entry.getLowAddress() != 0;
            baseLine = entry.getLine();
            this.filePath = initPath(filePath, compDir, entry, onePath);
            long base =entry.getLowAddress();
            long baseHihg =entry.getHighAddress();
            //System.err.println(""+entry);
            List<Integer> lineStorageList = new ArrayList<Integer>();
            List<Integer> offsetStorageList = new ArrayList<Integer>();
            for(LineNumber l : numbers) {
                if (l.endOffset>=base && l.endOffset <= baseHihg) {
                    //System.err.println(""+l);
                    lineStorageList.add(l.line);
                    offsetStorageList.add((int)(l.endOffset - base));
                }
            }
            lineStorage = new int[lineStorageList.size()];
            offsetStorage = new int[offsetStorageList.size()];
            for (int i = 0; i < lineStorageList.size(); i++){
                lineStorage[i] = lineStorageList.get(i);
                offsetStorage[i] = offsetStorageList.get(i);
            }
        }

        public FunctionToLine(String filePath, int baseLine, List<Integer> lines, List<Integer> offsets, Map<String, String> onePath) {
            assert lines.size() == offsets.size();
            this.baseLine = baseLine;
            this.filePath = getPath(filePath, onePath);
            lineStorage = new int[lines.size()];
            offsetStorage = new int[offsets.size()];
            for(int i = 0; i < lines.size(); i++) {
                lineStorage[i] = lines.get(i);
                offsetStorage[i] = offsets.get(i);
            }
        }

        @Override
        public SourceLineInfo getLine(int offset){
            if (offset <= 0) {
                if (baseLine > 0) {
                    return new SourceLineInfo(filePath, baseLine);
                } else {
                    if (lineStorage.length > 0){
                        return new SourceLineInfo(filePath, lineStorage[0]);
                    }
                }
                return null;
            }
            int res = -1;
            int delta = Integer.MAX_VALUE;
            for (int i = 0; i < offsetStorage.length; i++) {
                if (offsetStorage[i] > offset) {
                    int d = offsetStorage[i] - offset;
                    if (d < delta) {
                        res = i;
                        delta = d;
                    }
                }
            }
            if (res < 0) {
                return new SourceLineInfo(filePath, baseLine);
            }
            return new SourceLineInfo(filePath, lineStorage[res]);
        }

        @Override
        public String toString() {
            StringBuilder buf = new StringBuilder("File: "+filePath); // NOI18N
            buf.append("\n\tBase Line:  ").append(baseLine); // NOI18N
            if (lineStorage.length>0) {
                for(int i = 0; i < lineStorage.length; i++) {
                    buf.append("\n\tLine: ").append(lineStorage[i]).append("\t (").append(offsetStorage[i]).append(")"); // NOI18N
                }
                //buf.append("\n\tStart Line: ").append(lineStorage[0]).append("\t (").append(offsetStorage[0]).append(")"); // NOI18N
                //buf.append("\n\tEnd Line:   ").append(lineStorage[lineStorage.length - 1]).append("\t (").append(offsetStorage[lineStorage.length - 1]).append(")"); // NOI18N
            }
            return buf.toString();
        }

        @Override
        protected void dump(PrintStream out) {
            out.println(filePath);
            out.println(""+baseLine); // NOI18N
            for(int i = 0; i < lineStorage.length; i++) {
                out.println(""+lineStorage[i]+","+offsetStorage[i]); // NOI18N
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof FunctionToLine) {
                FunctionToLine other = (FunctionToLine) obj;
                if (!filePath.equals(other.filePath)) {
                    return false;
                }
                if (baseLine != other.baseLine) {
                    return false;
                }
                if (lineStorage.length != other.lineStorage.length) {
                    return false;
                }
                for (int i = 0; i < lineStorage.length; i++) {
                    if (lineStorage[i] != other.lineStorage[i]) {
                        return false;
                    }
                    if (offsetStorage[i] != other.offsetStorage[i]) {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }
    }
}
