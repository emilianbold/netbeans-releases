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
package org.netbeans.modules.cnd.gizmo;

import org.netbeans.modules.cnd.gizmo.support.GizmoServiceInfo;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.WeakHashMap;
import java.util.logging.Level;
import org.netbeans.modules.cnd.dwarfdump.CompilationUnit;
import org.netbeans.modules.cnd.dwarfdump.Dwarf;
import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfEntry;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.TAG;
import org.netbeans.modules.cnd.dwarfdump.section.DwarfLineInfoSection.LineNumber;
import org.netbeans.modules.dlight.spi.SourceFileInfoProvider;
import org.netbeans.modules.dlight.util.DLightLogger;
import org.openide.util.lookup.ServiceProvider;

/**
 * @author Alexey Vladykin
 */
@ServiceProvider(service = SourceFileInfoProvider.class, position = 5000)
public class DwarfSourceInfoProvider implements SourceFileInfoProvider {
    private static final boolean TRACE = false;
    private WeakHashMap<String, Map<String, AbstractFunctionToLine>> cache;
    private Map<String, String> onePath;

    public DwarfSourceInfoProvider() {
        cache = new WeakHashMap<String, Map<String, AbstractFunctionToLine>>();
    }

    public SourceFileInfo fileName(String functionSignature, int lineNumber, long offset, Map<String, String> serviceInfo) {
        if (serviceInfo == null){
            return null;
        }
        String executable = serviceInfo.get(GizmoServiceInfo.GIZMO_PROJECT_EXECUTABLE);
        if (executable != null) {
            String functionName = functionSignature;
            int parenIdx = functionSignature.indexOf('(');
            if (0 <= parenIdx) {
                functionName = functionSignature.substring(0, parenIdx);
            }
            Map<String, AbstractFunctionToLine> sourceInfoMap = getSourceInfo(executable);
            if (TRACE) {
                System.err.println("Search for:"+functionName+"+"+offset); // NOI18N
            }
            AbstractFunctionToLine fl = sourceInfoMap.get(functionName);
            int space = functionName.indexOf(' ');
            if (fl == null && space > 0) {
                fl = sourceInfoMap.get(functionName.substring(space+1));
            }
            int star = functionName.indexOf('*');
            if (fl == null && star > 0) {
                fl = sourceInfoMap.get(functionName.substring(star+1));
            }
            if (fl != null) {
                if (TRACE) {
                    System.err.println("Found:"+fl); // NOI18N
                }
                SourceFileInfo sourceInfo = fl.getLine((int)offset, serviceInfo);
                if (TRACE) {
                    System.err.println("Line:"+sourceInfo); // NOI18N
                }
                if (lineNumber > 0 && sourceInfo != null) {
                    return new SourceFileInfo(sourceInfo.getFileName(), lineNumber, 0);
                }
                return sourceInfo;
            }
        }
        return null;
    }

    private synchronized Map<String, AbstractFunctionToLine> getSourceInfo(String executable) {
        onePath = new HashMap<String, String>();
        Map<String, AbstractFunctionToLine> sourceInfoMap = cache.get(executable);
        Set<Long> antiLoop = new HashSet<Long>();
        if (sourceInfoMap == null) {
            sourceInfoMap = new HashMap<String, AbstractFunctionToLine>();
            try {
                Dwarf dwarf = new Dwarf(executable);
                try {
                    for (CompilationUnit compilationUnit : dwarf.getCompilationUnits()) {
                        TreeSet<LineNumber> lineNumbers = getCompilationUnitLines(compilationUnit);
                        String filePath = compilationUnit.getSourceFileAbsolutePath();
                        String compDir = compilationUnit.getCompilationDir();
                        processEntries(compilationUnit, compilationUnit.getDeclarations(false), filePath, compDir, lineNumbers, sourceInfoMap, antiLoop);
                    }
                } finally {
                    dwarf.dispose();
                }
            } catch (IOException ex) {
                DLightLogger.instance.log(Level.INFO, ex.getMessage());
            } catch (Throwable ex) {
                DLightLogger.instance.log(Level.INFO, ex.getMessage(), ex);
            }
            cache.put(executable, sourceInfoMap.isEmpty()?
                Collections.<String, AbstractFunctionToLine>emptyMap() : sourceInfoMap);
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
                        System.err.println(functionToLine);
                    }
                } else {
                    FunctionToLine functionToLine = new FunctionToLine(filePath, compDir, entry, lineNumbers, onePath);
                    sourceInfoMap.put(entry.getQualifiedName(), functionToLine);
                    if (TRACE) {
                        System.err.println(functionToLine);
                    }
                }
                break;
            }
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

//    private static String toAbsolutePath(Map<String, String> serviceInfo, String path) {
//        String projectPath = serviceInfo.get(GizmoServiceInfo.GIZMO_PROJECT_FOLDER);
//        if (projectPath != null) {
//            path = IpeUtils.toAbsolutePath(projectPath, path);
//        }
//        return path;
//    }

    private static TreeSet<LineNumber> getCompilationUnitLines(CompilationUnit unit) throws IOException{
        Set<LineNumber> numbers = unit.getLineNumbers();
        return new TreeSet<LineNumber>(numbers);
    }

    private static abstract class AbstractFunctionToLine {
        public abstract SourceFileInfo getLine(int offset, Map<String, String> serviceInfo);

        protected String initPath(String filePath, String compDir, DwarfEntry entry, Map<String, String> onePath) throws IOException{
            String res = _initPath(filePath, compDir, entry);
            String cached = onePath.get(res);
            if (cached == null) {
                onePath.put(res, res);
                cached = res;
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
        private final String functionName;
        private final int baseLine;
        private final String filePath;

        public DeclarationToLine(String filePath, String compDir, DwarfEntry entry, Map<String, String> onePath) throws IOException {
            assert entry.getKind() == TAG.DW_TAG_subprogram;
            functionName = entry.getQualifiedName();
            baseLine = entry.getLine();
            this.filePath = initPath(filePath, compDir, entry, onePath);
        }

        public SourceFileInfo getLine(int offset, Map<String, String> serviceInfo){
            //return new SourceFileInfo(toAbsolutePath(serviceInfo, filePath), baseLine, 0);
            return new SourceFileInfo(filePath, baseLine, 0);
        }

        @Override
        public String toString() {
            StringBuilder buf = new StringBuilder("File: "+filePath); // NOI18N
            buf.append("\n\tFunction:   "+functionName); // NOI18N
            buf.append("\n\tBase Line:  "+baseLine); // NOI18N
            return buf.toString();
        }
    }

    private static final class FunctionToLine extends AbstractFunctionToLine {
        private final String functionName;
        private final int[] lineStorage;
        private final int[] offsetStorage;
        private final int baseLine;
        private final String filePath;
        
        public FunctionToLine(String filePath, String compDir, DwarfEntry entry, TreeSet<LineNumber> numbers, Map<String, String> onePath) throws IOException {
            assert entry.getKind() == TAG.DW_TAG_subprogram;
            assert entry.getLowAddress() != 0;
            functionName = entry.getQualifiedName();
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

        public SourceFileInfo getLine(int offset, Map<String, String> serviceInfo){
            if (offset == 0) {
                //return new SourceFileInfo(toAbsolutePath(serviceInfo, filePath), baseLine, 0);
                if (baseLine > 0) {
                    return new SourceFileInfo(filePath, baseLine, 0);
                } else {
                    if (lineStorage.length > 0){
                        return new SourceFileInfo(filePath, lineStorage[0], 0);
                    }
                }
                return null;
            }
            int res = -1;
            for (int i = 0; i < offsetStorage.length; i++) {
                if (offsetStorage[i] > offset) {
                    res = i;
                    break;
                }
            }
            if (res < 0) {
                return new SourceFileInfo(filePath, baseLine, 0);
            }
            //return new SourceFileInfo(toAbsolutePath(serviceInfo, filePath), lineStorage[res], 0);
            return new SourceFileInfo(filePath, lineStorage[res], 0);
        }

        @Override
        public String toString() {
            StringBuilder buf = new StringBuilder("File: "+filePath); // NOI18N
            buf.append("\n\tFunction:   "+functionName); // NOI18N
            buf.append("\n\tBase Line:  "+baseLine); // NOI18N
            if (lineStorage.length>0) {
                buf.append("\n\tStart Line: "+lineStorage[0]+"\t ("+offsetStorage[0]+")"); // NOI18N
                buf.append("\n\tEnd Line:   "+lineStorage[lineStorage.length-1]+"\t ("+offsetStorage[lineStorage.length-1]+")"); // NOI18N
            }
            return buf.toString();
        }
    }

}
