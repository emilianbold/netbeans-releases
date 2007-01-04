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

package org.netbeans.modules.cnd.dwarfdump;

import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfAbbriviationTable;
import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfAbbriviationTableEntry;
import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfEntry;
import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfMacinfoTable;
import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfNameLookupTable;
import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfStatementList;
import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfMacinfoEntry;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.ATTR;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.TAG;
import org.netbeans.modules.cnd.dwarfdump.reader.DwarfReader;
import org.netbeans.modules.cnd.dwarfdump.section.DwarfAbbriviationTableSection;
import org.netbeans.modules.cnd.dwarfdump.section.DwarfAttribute;
import org.netbeans.modules.cnd.dwarfdump.section.DwarfLineInfoSection;
import org.netbeans.modules.cnd.dwarfdump.section.DwarfMacroInfoSection;
import org.netbeans.modules.cnd.dwarfdump.section.DwarfNameLookupTableSection;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

/**
 *
 * @author ak119685
 */
public class CompilationUnit {
    private DwarfReader reader;
    
    public long debugInfoSectionOffset;
    public long unit_offset;
    public long unit_length;
    public int  version;
    public long debug_abbrev_offset;
    public long info_offset;
    public byte address_size;
    public DwarfEntry root = null;
    
    private DwarfAbbriviationTable abbr_table = null;
    private DwarfStatementList statement_list = null;
    private DwarfMacinfoTable macrosTable = null;
    private DwarfNameLookupTable pubnamesTable = null;
    private long debugInfoOffset;

    /** Creates a new instance of CompilationUnit */
    public CompilationUnit(DwarfReader reader, long sectionOffset, long unitOffset) throws IOException {
        this.reader = reader;
        this.debugInfoSectionOffset = sectionOffset;
        this.unit_offset = unitOffset;
        readCompilationUnitHeader();
        root = getDebugInfo(false);
    }
    
    public String getProducer() {
        return (String)root.getAttributeValue(ATTR.DW_AT_producer);
    }
    
    public String getCompilationDir() {
        return (String)root.getAttributeValue(ATTR.DW_AT_comp_dir);
    }
    
    public String getSourceFileName() {
        return (String)root.getAttributeValue(ATTR.DW_AT_name);
    }
    
    public String getSourceFileFullName() {
        String result = null;
        
        try {
            result = new File(getCompilationDir() + File.separator + getSourceFileName()).getCanonicalPath();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        return result;
    }
    
    public String getSourceLanguage() {
        return root.getAttributeValue(ATTR.DW_AT_language).toString();
    }
    
    public String getType(DwarfEntry entry) {
        TAG entryKind = entry.getKind();
        
        if (entryKind.equals(TAG.DW_TAG_unspecified_parameters)) {
            return "null";
        }
        
        Integer typeRef = (Integer)entry.getAttributeValue(ATTR.DW_AT_type);
        
        if (typeRef == null) {
            return "void";
        }
        
        DwarfEntry typeEntry = getEntry(typeRef);
        TAG kind = typeEntry.getKind();
        
        if (kind.equals(TAG.DW_TAG_base_type)) {
            String name = typeEntry.getName();
            
            // TODO: Is it OK?
            if (name.equals("long unsigned int")) {
                name = "unsigned long";
            } else if (name.equals("long int")) {
                name = "long";
            }
            
            return name;
        }
        
        if (kind.equals(TAG.DW_TAG_structure_type) ||
                kind.equals(TAG.DW_TAG_enumeration_type) ||
                kind.equals(TAG.DW_TAG_typedef)) {
            return typeEntry.getName();
        }
        
        if (kind.equals(TAG.DW_TAG_const_type)) {
            // TODO: Check algorithm!
            Integer constTypeRef = (Integer)typeEntry.getAttributeValue(ATTR.DW_AT_type);
            DwarfEntry refTypeEntry = getEntry(constTypeRef);
            typeEntry.getKind();
            if (refTypeEntry.getKind().equals(TAG.DW_TAG_reference_type) ||
                    refTypeEntry.getKind().equals(TAG.DW_TAG_array_type)) {
                return getType(typeEntry);
            } else {
                return "const " + getType(typeEntry);
            }
            
//            return "const " + getType(typeEntry);
            
        }
        
        if (kind.equals(TAG.DW_TAG_reference_type)) {
            return getType(typeEntry) + "&";
        }
        
        if (kind.equals(TAG.DW_TAG_array_type)) {
            return getType(typeEntry) + "[]";
        }
        
        if (kind.equals(TAG.DW_TAG_pointer_type)) {
            return getType(getEntry(typeEntry.getRefference())) + "*";
        }
        
        if (kind.equals(TAG.DW_TAG_subroutine_type)) {
            return getType(typeEntry);
        }
        
        if (kind.equals(TAG.DW_TAG_volatile_type)) {
            return getType(typeEntry);
        }
        
        return "<" + kind + ">";
    }
    
    public DwarfEntry getEntry(long sectionOffset) {
        return entryLookup(getDebugInfo(true), sectionOffset);
    }
    
    private DwarfEntry entryLookup(DwarfEntry entry, long refference) {
        if (entry == null) {
            return null;
        }
        
        if (entry.getRefference() == refference) {
            return entry;
        }
        
        for (DwarfEntry child : entry.getChildren()) {
            DwarfEntry res = entryLookup(child, refference);
            if (res != null) {
                return res;
            }
        }
        
        return null;
    }
    
    public DwarfEntry getRoot() {
        return root;
    }
    
    public DwarfEntry getTypedefFor(Integer typeRef) {
        // TODO: Rewrite not to iterate every time.
        
        for (DwarfEntry entry : getDebugInfo(true).getChildren()) {
            if (entry.getKind().equals(TAG.DW_TAG_typedef)) {
                Object entryTypeRef = entry.getAttributeValue(ATTR.DW_AT_type);
                if (entryTypeRef != null && ((Integer)entryTypeRef).equals(typeRef)) {
                    return entry;
                }
            }
        }
        
        return null;
    }

    private void readCompilationUnitHeader() throws IOException {
        reader.seek(debugInfoSectionOffset + unit_offset);
        
        unit_length = reader.readInt();
        version = reader.readShort();
        debug_abbrev_offset = reader.readInt();
        address_size = (byte)(0xff & reader.readByte());
        
        debugInfoOffset = reader.getFilePointer();
        
        reader.setAddressSize(address_size);

        DwarfAbbriviationTableSection abbrSection = (DwarfAbbriviationTableSection)reader.getSection(".debug_abbrev");
        abbr_table = abbrSection.getAbbriviationTable(debug_abbrev_offset);
        
    }
    
    public DwarfStatementList getStatementList() {
        if (statement_list == null) {
            initStatementList();
        }
        
        return statement_list;
    }
    
    public DwarfMacinfoTable getMacrosTable() {
        if (macrosTable == null) {
            initMacrosTable();
        }
        
        return macrosTable;
    }

    private DwarfNameLookupTable getPubnamesTable() {
        if (pubnamesTable == null) {
            initPubnamesTable();
        }
        
        return pubnamesTable;
    }
    

    private DwarfEntry getDebugInfo(boolean readChildren) {
        if (root == null || (readChildren && root.getChildren().size() == 0)) {
            try {
                long currPos = reader.getFilePointer();
                reader.seek(debugInfoOffset);
                root = readEntry(0, readChildren);
                reader.seek(currPos);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        
        return root;
    }
    
    private DwarfEntry readEntry(int level, boolean readChildren) throws IOException {
        long refference = reader.getFilePointer() - debugInfoSectionOffset - unit_offset;
        long idx = reader.readUnsignedLEB128();
        
        if (idx == 0) {
            return null;
        }
        
        DwarfAbbriviationTableEntry abbreviationEntry = abbr_table.getEntry(idx);
        
        DwarfEntry entry = new DwarfEntry(this, abbreviationEntry, refference, level);
        
        for (int i = 0; i < abbreviationEntry.getAttributesCount(); i++) {
            DwarfAttribute attr = abbreviationEntry.getAttribute(i);
            entry.addValue(reader.readAttrValue(attr));
        }
        
        if (readChildren == true && entry.hasChildren()) {
            DwarfEntry child;
            while ((child = readEntry(level + 1, true)) != null) {
                entry.addChild(child);
            }
        }

        return entry;
    }

    private void initStatementList() {
        DwarfLineInfoSection lineInfoSection = (DwarfLineInfoSection)reader.getSection(".debug_line");
        Integer statementListOffset = (Integer)root.getAttributeValue(ATTR.DW_AT_stmt_list);
        if (statementListOffset != null) {
            statement_list = lineInfoSection.getStatementList(statementListOffset.longValue());
        }
    }

    private void initMacrosTable() {
        DwarfMacroInfoSection macroInfoSection = (DwarfMacroInfoSection)reader.getSection(".debug_macinfo");
        Integer macroInfoOffset = (Integer)root.getAttributeValue(ATTR.DW_AT_macro_info);
        
        if (macroInfoOffset != null) {
            macrosTable = macroInfoSection.getMacinfoTable(macroInfoOffset);
        }
    }

    private void initPubnamesTable() {
        DwarfNameLookupTableSection dwarfNameLookupTableSection = (DwarfNameLookupTableSection)reader.getSection(".debug_pubnames");
        pubnamesTable = dwarfNameLookupTableSection.getNameLookupTableFor(unit_offset);
    }
    
    public ArrayList<DwarfEntry> getDeclarations() {

        // make sure that pubnames table has been read ...
        getPubnamesTable();

        ArrayList<DwarfEntry> result = new ArrayList<DwarfEntry>();
        ArrayList<DwarfEntry> excludedDeclarations = new ArrayList<DwarfEntry>();
        
        int fileEntryIdx = getStatementList().getFileEntryIdx(getSourceFileName());
        
        for (DwarfEntry child : getDebugInfo(true).getChildren()) {
            if (child.isEntryDefinedInFile(fileEntryIdx)) {
                // TODO: Check algorythm
                // Do not add definitions that have DW_AT_abstract_origin attribute.
                // Do not add entries that's names start with _GLOBAL__F | _GLOBAL__I | _GLOBAL__D

                boolean entryAdded = false;

                if (child.getAttributeValue(ATTR.DW_AT_abstract_origin) == null) {
                    String qname = child.getQualifiedName();
                    if (qname != null && !qname.startsWith("_GLOBAL__")) {
                        result.add(child);
                        entryAdded = true;
                    }
                }

                if (!entryAdded) {
                    excludedDeclarations.add(child);
                }
            }
        }

        if (excludedDeclarations.size() > 0) {
            System.out.println("Excluding following abstract declarations from DWARF:");
            for (DwarfEntry declaration : excludedDeclarations) {
                System.out.println("\t" + declaration.getDeclaration());
            }
        }
        
        return result;
    }
    
    public void dump(PrintStream out) {
        out.println("*** " + getSourceFileFullName() + " ***");
        out.println("  Compilation Unit @ offset " + Long.toHexString(unit_offset) + ":");
        out.println("    Length: " + unit_length);
        out.println("    Version: " + version);
        out.println("    Abbrev Offset: " + debug_abbrev_offset);
        out.println("    Pointer Size: " + address_size);
        
        getPubnamesTable();
        
        getDebugInfo(true).dump(out);
        getStatementList().dump(out);
        pubnamesTable.dump(out);
        
        out.println();
    }    

}
