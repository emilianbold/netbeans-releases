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

import org.netbeans.modules.cnd.dwarfdump.CompilationUnit;
import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfEntry;
import org.netbeans.modules.cnd.dwarfdump.reader.DwarfReader;
import org.netbeans.modules.cnd.dwarfdump.section.DwarfDebugInfoSection;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import modelutils.Config;

/**
 *
 * @author ak119685
 */
public class DwarfDump {
    DwarfReader reader = null;
    
    public DwarfDump(String objFileName) throws FileNotFoundException, IOException {
        this.reader = new DwarfReader(objFileName);
    }
    
    public static void main(String[] args) {
        try {
            Config config = new Config("o:", args);
            DwarfDump dwarfDump = new DwarfDump(config.getArgument());
            
            String dumpFile = config.getParameterFor("-o");
            PrintStream dumpStream = null;
            
            if (dumpFile != null) {
                System.err.println("Dumping DWARF to " + dumpFile);
                dumpStream = new PrintStream(dumpFile);
            } else {
                dumpStream = System.out;
            }
   
            dwarfDump.dump(dumpStream);
            
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
        DwarfDebugInfoSection debugInfo = (DwarfDebugInfoSection)reader.getSection(".debug_info");
        List<CompilationUnit> result = null;
        try {
	    if( debugInfo != null ) {
		result = debugInfo.getCompilationUnits();
	    }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        return result;
    }
    
    public void dump(PrintStream out) {
        out.println("Dwarf dump for " + reader.getFileName() + "\n");
//        ElfSection stringsSection = reader.getSection(".shstrtab");
//        stringsSection.dump(out);
        
        for (CompilationUnit cu : getCompilationUnits()) {
            for (DwarfEntry declaration : cu.getDeclarations()) {
                System.out.println(declaration.getDeclaration());
            }
        }
    }
    
}
