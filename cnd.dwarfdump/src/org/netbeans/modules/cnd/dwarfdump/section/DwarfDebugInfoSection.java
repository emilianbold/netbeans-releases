/*
 * DwarfDebugInfoSection.java
 *
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.cnd.dwarfdump.section;

import org.netbeans.modules.cnd.dwarfdump.CompilationUnit;
import org.netbeans.modules.cnd.dwarfdump.reader.DwarfReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ak119685
 */
public class DwarfDebugInfoSection extends ElfSection {
    List<CompilationUnit> compilationUnits = new ArrayList<CompilationUnit>();
    
    public DwarfDebugInfoSection(DwarfReader reader, int sectionIdx) {
        super(reader, sectionIdx);
    }
    
    public int getCompilationUnitsNumber() {
        List<CompilationUnit> compilationUnits = null;
        
        try {
            compilationUnits = getCompilationUnits();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        return (compilationUnits == null) ? 0 : compilationUnits.size();
    }
    
    public CompilationUnit getCompilationUnit(long unit_offset) {
        for (CompilationUnit unit : compilationUnits) {
            if (unit.unit_offset == unit_offset) {
                return unit;
            }
        }
        
        return null;
    }
    
    public List<CompilationUnit> getCompilationUnits() throws IOException {
        if (compilationUnits.size() == 0) {
	    ElfSection arangesSection = reader.getSection(".debug_aranges");
	    if( arangesSection != null ) {
		DwarfArangesSection aranges = (DwarfArangesSection) arangesSection.read();
		for (AddressRange arange : aranges.getRanges()) {
		    CompilationUnit unit = new CompilationUnit((DwarfReader)reader, header.getSectionOffset(), arange.info_offset);
		    compilationUnits.add(unit);
		}
	    }
        }        
        return compilationUnits;
    }
    
    public void dump(PrintStream out) {
        try {
            for (CompilationUnit unit : getCompilationUnits()) {
                unit.dump(out);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
