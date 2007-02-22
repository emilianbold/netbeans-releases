/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package test.dwarfdump;

import java.util.TreeSet;
import org.netbeans.modules.cnd.dwarfdump.CompilationUnit;
import org.netbeans.modules.cnd.dwarfdump.Dwarf;
import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfMacinfoTable;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.SECTIONS;
import org.netbeans.modules.cnd.dwarfdump.reader.DwarfReader;
import org.netbeans.modules.cnd.dwarfdump.section.DwarfArangesSection;
import org.netbeans.modules.cnd.dwarfdump.section.DwarfDebugInfoSection;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import modelutils.Config;

/**
 *
 * @author ak119685
 */
public class DwarfDump {

    Dwarf dwarf = null;
    
    public DwarfDump(String objFileName) throws FileNotFoundException, IOException {
	this.dwarf = new Dwarf(objFileName);
    }
    
    public static void main(String[] args) {
        try {
            Config config = new Config("faido:", args); // NOI18N
            DwarfDump dwarfDump = new DwarfDump(config.getArgument());
            
            String dumpFile = config.getParameterFor("-o"); // NOI18N
            PrintStream dumpStream = null;
            
            if (dumpFile != null) {
                System.err.println("Dumping DWARF to " + dumpFile);
                dumpStream = new PrintStream(dumpFile);
            } else {
                dumpStream = System.out;
            }
   
            if (config.flagSet("-i")) { // NOI18N
                dwarfDump.dumpIncludes(dumpStream);
            }
            
            if (config.flagSet("-d")) { // NOI18N
                dwarfDump.dumpDefines(dumpStream);
            }
            
            if (config.flagSet("-a")) { // NOI18N
                dwarfDump.dump(dumpStream);
            }
            
            if (config.flagSet("-f")) { // NOI18N
                dwarfDump.dumpFiles(dumpStream);
            }
            
        } catch (Exception ex) {
            System.err.println("Fatal error: " + ex.getMessage());
            ex.printStackTrace(System.err);
        }
    }
    
    public CompilationUnit getCompilationUnit(String srcFileFullName) {
        for (CompilationUnit unit : getCompilationUnits()) {
            // TODO: remove hack
            
            String srcFileName = srcFileFullName.substring(srcFileFullName.lastIndexOf(File.separatorChar));
            String unitFileName = unit.getSourceFileFullName();
            unitFileName = unitFileName.substring(unitFileName.lastIndexOf(File.separatorChar));
            
            if (unitFileName.equals(srcFileName)) {
            //if (unit.getSourceFileFullName().equals(srcFileFullName)) {
                return unit;
            }
        }
        
        return null;
    }

    public List<CompilationUnit> getCompilationUnits() {
        DwarfDebugInfoSection debugInfo = (DwarfDebugInfoSection)dwarf.getSection(SECTIONS.DEBUG_INFO);
        List<CompilationUnit> result = null;
        
        if (debugInfo != null) {
            try {
                result = debugInfo.getCompilationUnits();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else {
            return new ArrayList<CompilationUnit>();
        }
        
        return result;
    }
    
    public void dump(PrintStream out) {
        out.println("Dwarf dump for " + dwarf.getFileName() + "\n"); // NOI18N

//        ElfSection stringsSection = reader.getSection(".shstrtab");
//        stringsSection.dump(out);
    
        DwarfArangesSection aranges = (DwarfArangesSection) dwarf.getSection(SECTIONS.DEBUG_ARANGES);
        
        if (aranges != null) {
            aranges.dump(out);
        }
        
        List<CompilationUnit> compUnits = getCompilationUnits();
        if (compUnits != null) {
            for (CompilationUnit cu : compUnits) {
                cu.dump(out);
            }
        }
    }

    private void dumpIncludes(PrintStream out) {
        List<CompilationUnit> CUs = getCompilationUnits();
        
        if (CUs == null) {
	out.println("No Compilation Units found for" + this.dwarf.getFileName()); // NOI18N
            return;
        }
        
        for (CompilationUnit cu : CUs) {
            out.println("Include directories for " + cu.getSourceFileFullName()); // NOI18N
            List<String> dirs = cu.getStatementList().getIncludeDirectories();
            String basedir = cu.getCompilationDir();
            
            for (String dir : dirs) {
                if (!(dir.startsWith("/") || dir.startsWith("\\"))) { // NOI18N
                    dir = basedir + File.separator + dir;
                }
                
                try {
                    out.println(new File(dir).getCanonicalPath());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            out.println();
        }
    }
    
    private void dumpDefines(PrintStream out) {
        for (CompilationUnit cu : getCompilationUnits()) {
            out.println("Defines for " + cu.getSourceFileFullName()); // NOI18N
            DwarfMacinfoTable macroTable = cu.getMacrosTable();
            
            if (macroTable == null) {
                out.println("No defines found for this CU. Be sure to compile with -g3 -gdwarf-2 flags"); // NOI18N
            } else {
                List<String> dirs = macroTable.getCommandLineDefines();
                for (String dir : dirs) {
                    out.println(dir);
                }
            }
            out.println();
        }
    } 

    private void dumpFiles(PrintStream out) {
        out.println("Files used for building " + dwarf.getFileName()); // NOI18N

        TreeSet<String> paths = new TreeSet();
        
        for (CompilationUnit cu : getCompilationUnits()) {
            for (String path : cu.getStatementList().getFilePaths()) {
                path = path.replaceAll("\\\\", "/"); // NOI18N

                if (!paths.contains(path)) {
                    paths.add(path);
                }
            }
        }
        
        for (String path : paths) {
            out.println(path);
        }
    }
}
