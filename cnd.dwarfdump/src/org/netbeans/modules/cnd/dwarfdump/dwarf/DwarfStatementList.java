/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.cnd.dwarfdump.dwarf;

import java.io.ByteArrayOutputStream;
import org.netbeans.modules.cnd.dwarfdump.section.FileEntry;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public class DwarfStatementList {
    public long total_length;
    public int version;
    public long prologue_length;
    public int minimum_instruction_length;
    public int default_is_stmt;
    public int line_base;
    public int line_range;
    public int opcode_base;
    public long[] standard_opcode_lengths;
    public List<String> includeDirs = new ArrayList<String>();
    public List<FileEntry> fileEntries = new ArrayList<FileEntry>();
    public Map<String, Set<FileEntry>> name2entry;

    private long offset;


    /** Creates a new instance of DwarfStatementList */
    public DwarfStatementList(long offset) {
        this.offset = offset;
    }

    public List<String> getIncludeDirectories() {
        return includeDirs;
    }

    public List<String> getFilePaths() {
        List<String> result = new ArrayList<String>();

        for (int idx = 1; idx <= fileEntries.size(); idx++) {
            String filepath = getFilePath(idx);
            if (filepath != null) {
                result.add(filepath);
            }
        }

        return result;
    }

    public List<FileEntry> getFileEntries() {
        return fileEntries;
    }

    public void dump(PrintStream out) {
        out.printf("%nPrologue Statement List (offset = %d [0x%08x]):%n%n", offset, offset); // NOI18N
        out.printf("  %-28s %s%n", "Length:", total_length); // NOI18N
        out.printf("  %-28s %s%n", "DWARF Version:", version); // NOI18N
        out.printf("  %-28s %s%n", "Prologue Length:", prologue_length); // NOI18N
        out.printf("  %-28s %s%n", "Minimum Instruction Length:", minimum_instruction_length); // NOI18N
        out.printf("  %-28s %s%n", "Initial value of 'is_stmt':", default_is_stmt); // NOI18N
        out.printf("  %-28s %s%n", "Line Base:", line_base); // NOI18N
        out.printf("  %-28s %s%n", "Line Range:", line_range); // NOI18N
        out.printf("  %-28s %s%n", "Opcode Base:", opcode_base); // NOI18N

        out.println("%n Opcodes:"); // NOI18N

        for (int i = 0; i < standard_opcode_lengths.length; i++) {
            out.printf("  Opcode %d has %d args%n", i + 1, standard_opcode_lengths[i]); // NOI18N
        }

        out.println("%n The Directory Table:%n"); // NOI18N

        int idx = 0;

        out.println(" Entry Path"); // NOI18N
        for (String includeDir : includeDirs) {
            out.printf(" %-6d%s%n", ++idx, includeDir); // NOI18N
        }

        out.println("%n The File Name Table:%n"); // NOI18N
        out.println(" Entry Dir   Time  Size  Name"); // NOI18N

        idx = 0;
        for (FileEntry fileEntry : fileEntries) {
            out.printf(" %-6d%-6d%-6d%-6d%s%n", ++idx, fileEntry.dirIndex, fileEntry.modifiedTime, fileEntry.fileSize, fileEntry.fileName); // NOI18N
        }
    }

    @Override
    public String toString() {
        try {
            ByteArrayOutputStream st = new ByteArrayOutputStream();
            PrintStream out = new PrintStream(st, false, "UTF-8"); // NOI18N
            dump(out);
            return st.toString("UTF-8"); //NOI18N
        } catch (IOException ex) {
            return ""; // NOI18N
        }
    }

    public int getDirectoryIndex(String dirname) {
        int idx = 0;

        if (dirname == null) {
            return 0;
        }

        for (String dir : includeDirs) {
            idx++;
            if (dir.equals(dirname)) {
                return idx;
            }
        }

        return 0;
    }

    public int getFileEntryIdx(String fileName) {
        File file = new File(fileName);
        String dirname = file.getParent();
        String fname = file.getName();
        return getFileEntryIdx(getDirectoryIndex(dirname), fname);
    }

    private int getFileEntryIdx(int dirIdx, String fname) {
        int idx = 0;

        for (FileEntry fileEntry : fileEntries) {
            idx++;
            if (fileEntry.dirIndex == dirIdx && fileEntry.fileName.equals(fname)) {
                return idx;
            }
        }

        return 0;
    }

    public String getFilePath(int idx) {
        FileEntry fileEntry = fileEntries.get(idx - 1);

        if (fileEntry == null || fileEntry.fileName.equals("<internal>") || fileEntry.fileName.equals("<built-in>")) { // NOI18N
            return null;
        }

        String result;

        if (fileEntry.dirIndex == 0) {
            result = "." + File.separator + fileEntry.fileName; // NOI18N
        } else {
            String dir = includeDirs.get(fileEntry.dirIndex - 1);
            if (dir.endsWith("\\") || dir.endsWith("/")) { // NOI18N
                result = dir + fileEntry.fileName;
            } else {
                result = dir + File.separator + fileEntry.fileName;
            }
        }

        return result;
    }

    public List<String> getPathsForFile(String fname) {
        Map<String, Set<FileEntry>> map = getName2Entry();
        Set<FileEntry> set = map.get(fname);
        if (set == null){
            return Collections.<String>emptyList();
        }
        List<String> result = new ArrayList<String>();
        String suffix = File.separator + fname;
        StringBuilder buf = new StringBuilder(100);
        for (FileEntry fileEntry : set) {
            if (fileEntry.fileName.equals(fname)){
                String dir = (fileEntry.dirIndex == 0) ? "." : includeDirs.get(fileEntry.dirIndex - 1); // NOI18N
                result.add(dir);
            } else if(fileEntry.fileName.endsWith(suffix)) {
                buf.setLength(0);
                buf.append( (fileEntry.dirIndex == 0) ? "." : includeDirs.get(fileEntry.dirIndex - 1) ); // NOI18N
                buf.append(File.separator);
                buf.append( fileEntry.fileName.substring(0,fileEntry.fileName.length()-fname.length()-1) );
                result.add(buf.toString());
            }
        }
        return result;
    }

    private Map<String, Set<FileEntry>> getName2Entry(){
        if (name2entry == null) {
            Map<String, Set<FileEntry>> res = new HashMap<String, Set<FileEntry>>();
            for (FileEntry fileEntry : fileEntries) {
                int i = fileEntry.fileName.lastIndexOf(File.separator);
                String key;
                if (i >= 0) {
                    key = fileEntry.fileName.substring(i+1);
                } else {
                    key = fileEntry.fileName;
                }
                Set<FileEntry> set = res.get(key);
                if (set == null) {
                    set = new HashSet<FileEntry>();
                    res.put(key, set);
                }
                set.add(fileEntry);
            }
            name2entry = res;
        }
        return name2entry;
    }

}
