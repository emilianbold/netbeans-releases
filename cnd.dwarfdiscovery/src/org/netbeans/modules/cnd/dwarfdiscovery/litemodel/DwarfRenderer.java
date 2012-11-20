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

package org.netbeans.modules.cnd.dwarfdiscovery.litemodel;

import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.netbeans.modules.cnd.dwarfdump.CompilationUnit;
import org.netbeans.modules.cnd.dwarfdump.CompilationUnitInterface;
import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfEntry;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.TAG;
import org.netbeans.modules.cnd.litemodel.api.Declaration;

/**
 *
 * @author Alexander Simon
 */
public class DwarfRenderer {
    private static final boolean TRACE = true;
    private boolean PROCESS_TOP_LEVEL_DECLARATIONS = true;
    private boolean LIMIT_TO_COMPILATION_UNIT = true;
    private Map<String, String> onePath = new HashMap<String, String>();
    private Set<Declaration> sourceInfoMap = new HashSet<Declaration>();
    private String filePath;
    private String compDir;
    private Set<Long> antiLoop;
    private int fileEntryIdx = 0;

    private DwarfRenderer() {
    }

    public static DwarfRenderer createFullRenderer() {
        DwarfRenderer dwarfRenderer = new DwarfRenderer();
        dwarfRenderer.PROCESS_TOP_LEVEL_DECLARATIONS = false;
        dwarfRenderer.LIMIT_TO_COMPILATION_UNIT = false;
        return dwarfRenderer;
    }

    public static DwarfRenderer createTopLevelDeclarationsRenderer() {
        DwarfRenderer dwarfRenderer = new DwarfRenderer();
        dwarfRenderer.PROCESS_TOP_LEVEL_DECLARATIONS = true;
        dwarfRenderer.LIMIT_TO_COMPILATION_UNIT = false;
        return dwarfRenderer;
    }

    public static DwarfRenderer createTopLevelDeclarationsCompilationUnitsRenderer() {
        DwarfRenderer dwarfRenderer = new DwarfRenderer();
        dwarfRenderer.PROCESS_TOP_LEVEL_DECLARATIONS = true;
        dwarfRenderer.LIMIT_TO_COMPILATION_UNIT = true;
        return dwarfRenderer;
    }

    public void dumpModel(PrintStream out) {
        Map<String, Map<String, Declaration>> res = getLWM();
        for(Map.Entry<String, Map<String, Declaration>> entry : res.entrySet()) {
            out.println(entry.getKey());
            for(Map.Entry<String, Declaration> e : entry.getValue().entrySet()) {
                if (e.getValue().getReferencedType() != null) {
                    out.println("\t"+e.getValue().getQName()+" ("+e.getValue().getKind()+") "+e.getValue().getReferencedType()+" :"+e.getValue().getLine()); // NOI18N
                } else {
                    out.println("\t"+e.getValue().getQName()+" ("+e.getValue().getKind()+") :"+e.getValue().getLine()); // NOI18N
                }
            }
        }
    }

    public Map<String, Map<String, Declaration>> getLWM() {
        Map<String, Map<String, Declaration>> res = new TreeMap<String, Map<String, Declaration>>();
        for (Declaration d : sourceInfoMap) {
            Map<String, Declaration> map = res.get(d.getFileName());
            if (map == null) {
                map = new TreeMap<String, Declaration>();
                res.put(d.getFileName(), map);
            }
            map.put(d.getQName(), d);
        }
        return res;
    }

    public void process(CompilationUnitInterface compilationUnit) throws IOException {
        filePath = compilationUnit.getSourceFileAbsolutePath();
        compDir = compilationUnit.getCompilationDir();
        antiLoop = new HashSet<Long>();
        if (compilationUnit instanceof CompilationUnit) {
            CompilationUnit cu = (CompilationUnit) compilationUnit;
            if (PROCESS_TOP_LEVEL_DECLARATIONS) {
                processEntries(cu, cu.getTopLevelEntries(), null);
            } else {
                processEntries(cu, cu.getDeclarations(false), null);
            }
        }
    }

    private void processEntries(CompilationUnit compilationUnit, List<DwarfEntry> declarations, MyDeclaration parent) throws IOException {
        if (LIMIT_TO_COMPILATION_UNIT) {
            fileEntryIdx = compilationUnit.getStatementList().getFileEntryIdx(compilationUnit.getSourceFileName());
        }

        for (DwarfEntry entry : declarations) {
            prosessEntry(compilationUnit, entry, parent);
        }
    }

    private void prosessEntry(CompilationUnit compilationUnit, DwarfEntry entry, MyDeclaration parent) throws IOException {
        if (antiLoop.contains(entry.getRefference())) {
            return;
        }
        antiLoop.add(entry.getRefference());
        switch (entry.getKind()) {
            case DW_TAG_enumerator:
            case DW_TAG_variable:
            case DW_TAG_member:
            case DW_TAG_inlined_subroutine:
            case DW_TAG_subprogram:
            case DW_TAG_SUN_dtor:
            case DW_TAG_SUN_function_template:
            {
                if (!LIMIT_TO_COMPILATION_UNIT || entry.isEntryDefinedInFile(fileEntryIdx)) {
                    MyDeclaration functionToLine = null;
                    if (entry.getLine() >= 0 && entry.getDeclarationFilePath() != null) {
                        functionToLine = new MyDeclaration(filePath, compDir, entry, onePath);
                        if (functionToLine.getQName() != null) {
                            sourceInfoMap.add(functionToLine);
                        } else {
                            if (TRACE) {
                                System.err.println("Entry has empty qname\n"+entry); // NOI18N
                            }
                        }
                    } else if (parent != null && parent.getLine() >= 0 && parent.getFileName() != null) {
                        functionToLine = new MyDeclaration(entry, parent, onePath);
                        if (functionToLine.getQName() != null) {
                            sourceInfoMap.add(functionToLine);
                        } else {
                            if (TRACE) {
                                System.err.println("Entry has empty qname\n"+entry); // NOI18N
                            }
                        }
                    }
                    if (functionToLine != null) {
                        switch (entry.getKind()) {
                            case DW_TAG_variable:
                            case DW_TAG_member:
                            case DW_TAG_inlined_subroutine:
                            case DW_TAG_subprogram:
                            case DW_TAG_SUN_function_template:
                                DwarfEntry type = compilationUnit.getReferencedType(entry);
                                if (type != null) {
                                    functionToLine.setReferencedType(type, onePath);
                                }
                        }
                    }
                }
                break;
            }
            case DW_TAG_inheritance:
            {
                DwarfEntry inh = compilationUnit.getReferencedType(entry);
                if (inh != null) {
                    if (parent != null && parent.getLine() >= 0 && parent.getFileName() != null) {
                        MyDeclaration functionToLine = new MyDeclaration(entry, parent, inh, onePath);
                        if (functionToLine.getQName() != null) {
                            sourceInfoMap.add(functionToLine);
                        } else {
                            if (TRACE) {
                                System.err.println("Entry has empty qname\n"+inh); // NOI18N
                            }
                        }
                    }
                }
                break;
            }
            case DW_TAG_friend:
            {
                DwarfEntry friend = compilationUnit.getReferencedFriend(entry);
                if (friend != null) {
                    if (parent != null && parent.getLine() >= 0 && parent.getFileName() != null) {
                        MyDeclaration functionToLine = new MyDeclaration(entry, parent, friend, onePath);
                        if (functionToLine.getQName() != null) {
                            sourceInfoMap.add(functionToLine);
                        } else {
                            if (TRACE) {
                                System.err.println("Entry has empty qname\n"+friend); // NOI18N
                            }
                        }
                    }
                }
                break;
            }
            case DW_TAG_namespace:
            {
                if (!LIMIT_TO_COMPILATION_UNIT || entry.isEntryDefinedInFile(fileEntryIdx)) {
                    MyDeclaration aParent = null;
                    if (entry.getLine() >= 0 && entry.getDeclarationFilePath() != null) {
                        aParent = new MyDeclaration(filePath, compDir, entry, onePath);
                        if (aParent.getQName() != null) {
                            sourceInfoMap.add(aParent);
                        } else {
                            if (TRACE) {
                                System.err.println("Entry has empty qname\n"+entry); // NOI18N
                            }
                        }
                    }
                    processEntries(compilationUnit, entry.getChildren(), aParent);
                }
                break;
            }
            case DW_TAG_SUN_class_template:
            case DW_TAG_SUN_struct_template:
            case DW_TAG_SUN_union_template:
            case DW_TAG_class_type:
            case DW_TAG_structure_type:
            case DW_TAG_union_type:
            case DW_TAG_enumeration_type:
            {
                if (!LIMIT_TO_COMPILATION_UNIT || entry.isEntryDefinedInFile(fileEntryIdx)) {
                    MyDeclaration aParent = null;
                    if (entry.getLine() >= 0 && entry.getDeclarationFilePath() != null) {
                        aParent = new MyDeclaration(filePath, compDir, entry, onePath);
                        if (aParent.getQName() != null) {
                            sourceInfoMap.add(aParent);
                        } else {
                            if (TRACE) {
                                System.err.println("Entry has empty qname\n"+entry); // NOI18N
                            }
                        }
                    }
                    processEntries(compilationUnit, entry.getChildren(), aParent);
                }
                break;
            }
            case DW_TAG_typedef:
            case DW_TAG_const_type:
            case DW_TAG_pointer_type:
            case DW_TAG_reference_type:
            case DW_TAG_array_type:
            case DW_TAG_ptr_to_member_type:
            {
                if (!LIMIT_TO_COMPILATION_UNIT || entry.isEntryDefinedInFile(fileEntryIdx)) {
                    MyDeclaration functionToLine = null;
                    if (entry.getLine() >= 0 && entry.getDeclarationFilePath() != null) {
                        functionToLine = new MyDeclaration(filePath, compDir, entry, onePath);
                        if (functionToLine.getQName() != null) {
                            sourceInfoMap.add(functionToLine);
                        } else {
                            if (TRACE) {
                                System.err.println("Entry has empty qname\n"+entry); // NOI18N
                            }
                        }
                    }
                    DwarfEntry type = compilationUnit.getReferencedType(entry);
                    if (functionToLine != null && type != null) {
                        functionToLine.setReferencedType(type, onePath);
                        prosessEntry(compilationUnit, type, null);
                    }
                }
                break;
            }
        }
    }

    private static final class MyDeclaration implements Declaration {
        private final int baseLine;
        private final String filePath;
        private final Kind kind;
        private final String qname;
        private String referencedType;

        public MyDeclaration(String filePath, String compDir, DwarfEntry entry, Map<String, String> onePath) throws IOException {
            kind = kind2kind(entry.getKind());
            baseLine = entry.getLine();
            qname = initName(entry, onePath);
            this.filePath = initPath(filePath, compDir, entry, onePath);
        }

        private Kind kind2kind(org.netbeans.modules.cnd.dwarfdump.dwarfconsts.TAG tag) {
            return Kind.valueOf(tag.toString().substring(7));
        }

        private void setReferencedType(DwarfEntry entry, Map<String, String> onePath) throws IOException {
            referencedType = getString(entry.getType(), onePath);
        }

        @Override
        public String getReferencedType(){
            return referencedType;
        }

        private String initName(DwarfEntry entry, Map<String, String> onePath) throws IOException {
            if (entry.getKind() == TAG.DW_TAG_subprogram) {
                return getString(entry.getQualifiedName() + entry.getParametersString(false), onePath);
            } else {
                return getString(entry.getQualifiedName(), onePath);
            }
        }

        public MyDeclaration(DwarfEntry entry, MyDeclaration parent, Map<String, String> onePath) throws IOException {
            kind = kind2kind(entry.getKind());
            baseLine = parent.getLine();
            qname = initName(entry, onePath);
            this.filePath = parent.getFileName();
        }

        public MyDeclaration(DwarfEntry entry, MyDeclaration parent, DwarfEntry reference, Map<String, String> onePath) throws IOException {
            kind = kind2kind(entry.getKind());
            baseLine = parent.getLine();
            qname = initName(reference, onePath);
            this.filePath = parent.getFileName();
        }

        @Override
        public String getFileName() {
            return filePath;
        }

        @Override
        public int getLine() {
            return baseLine;
        }

        @Override
        public Kind getKind() {
            return kind;
        }

        @Override
        public String getQName() {
            return qname;
        }

        private String normalizeFile(String path) {
            //String aPath = CndFileUtils.normalizeFile(new File(path)).getAbsolutePath();
            path = path.replace("/./", "/"); // NOI18N
            while (true) {
                int i = path.indexOf("/../"); // NOI18N
                if (i < 0) {
                    break;
                }
                int prev = -1;
                for (int j = i - 1; j >= 0; j--) {
                    if (path.charAt(j) == '/') {
                        prev = j;
                        break;
                    }
                }
                if (prev == -1) {
                    break;
                }
                path = path.substring(0, prev)+path.substring(i+3);
            }
            //assert aPath.equals(path);
            return path;
        }

        private String initPath(String filePath, String compDir, DwarfEntry entry, Map<String, String> onePath) throws IOException{
            String res = _initPath(filePath, compDir, entry);
            res = res.replace('\\', '/');
            if (res.indexOf("/../")>=0 || res.indexOf("/./")>=0) { // NOI18N
                res = normalizeFile(res);
            }
            return getString(res, onePath);
        }

        private String getString(String path, Map<String, String> onePath) {
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

        @Override
        public String toString() {
            return qname+"("+kind+") "+filePath+"("+baseLine+")"; // NOI18N
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof MyDeclaration) {
                MyDeclaration other = (MyDeclaration) obj;
                if (baseLine != other.baseLine) {
                    return false;
                }
                if (kind != other.kind) {
                    return false;
                }
                if (!filePath.equals(other.filePath)) {
                    return false;
                }
                if (!qname.equals(other.qname)) {
                    return false;
                }
                return true;
            }
            return false;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 83 * hash + this.baseLine;
            hash = 83 * hash + this.filePath.hashCode();
            hash = 83 * hash + this.kind.ordinal();
            hash = 83 * hash + this.qname.hashCode();
            return hash;
        }
    }
}
