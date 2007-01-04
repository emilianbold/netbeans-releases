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

package org.netbeans.modules.cnd.dwarfdump.dwarf;

import org.netbeans.modules.cnd.dwarfdump.CompilationUnit;
import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfDeclaration;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.ATTR;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.TAG;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author ak119685
 */
public class DwarfEntry {
    private CompilationUnit compilationUnit;
    private DwarfAbbriviationTableEntry abbriviationTableEntry;
    private ArrayList<Object> values = new ArrayList<Object>();
    private ArrayList<DwarfEntry> children = new ArrayList<DwarfEntry>();
    private long refference;
    private int hierarchyLevel;
    private String qualifiedName = null;
    private String name = null;
    
    /** Creates a new instance of DwarfEntry */

    public DwarfEntry(CompilationUnit compilationUnit, DwarfAbbriviationTableEntry abbrEntry, long refference, int hierarchyLevel) {
        this.abbriviationTableEntry = abbrEntry;
        this.compilationUnit = compilationUnit;
        this.refference = refference;
        this.hierarchyLevel = hierarchyLevel;
    }
    
    public TAG getKind() {
        return abbriviationTableEntry.getKind();
    }
    
    public String getName() {
        if (name == null) {
            name = (String)getAttributeValue(ATTR.DW_AT_name);
        }
        
        return name;
    }
    
    public String getQualifiedName() {
        return (qualifiedName) == null ? getName() : qualifiedName;
    }
    
    public void setQualifiedName(String qualifiedName) {
        this.qualifiedName = qualifiedName;
        
        Integer originOffset = (Integer)getAttributeValue(ATTR.DW_AT_abstract_origin);
        if (originOffset != null) {
            DwarfEntry origin = compilationUnit.getEntry(originOffset);
            origin.setQualifiedName(qualifiedName);
        }

        Integer specificationOffset = (Integer)getAttributeValue(ATTR.DW_AT_specification);
        if (specificationOffset != null) {
            DwarfEntry specification = compilationUnit.getEntry(specificationOffset);
            specification.setQualifiedName(qualifiedName);
        }
    }
    
    public String getType() {
        return compilationUnit.getType(this);
    }
    
    public int getUintAttributeValue(ATTR attr) {
        Object value = getAttributeValue(attr);
        
        if (value == null) {
            return -1;
        }
        int result = ((Number)value).intValue();
        
        if (result < 0) {
            result &= 0xFF;
        }
        
        return result;
    }
    
    public Object getAttributeValue(ATTR attr) {
        Object attrValue = null;
        
        // Get the index of this attribute from the abbriviation table entry 
        // associated with this one.
        
        int attrIdx = abbriviationTableEntry.getAttribute(attr);
        
        // If there is no such attribute in this entry - try to get this 
        // attribute from "abstract origin" or "specification" entry (if any)
        
        if (attrIdx == -1) {
            Integer offset = -1;
            
            if (abbriviationTableEntry.getAttribute(ATTR.DW_AT_abstract_origin) >= 0) {
                offset = (Integer)getAttributeValue(ATTR.DW_AT_abstract_origin);
            } else if (abbriviationTableEntry.getAttribute(ATTR.DW_AT_specification) >= 0) {
                offset = (Integer)getAttributeValue(ATTR.DW_AT_specification);
            }
            
            attrValue = (offset == -1) ? null : compilationUnit.getEntry(offset).getAttributeValue(attr);
        } else {
            // Attribute has been found
            attrValue = values.get(attrIdx);
        }
        
        return attrValue;
    }

    public void addValue(Object value) {
        values.add(value);
    }
    
    public ArrayList<DwarfEntry> getChildren() {
        return children;
    }
    
    public boolean hasChildren() {
        return abbriviationTableEntry.hasChildren();
    }
    
    public void addChild(DwarfEntry child) {
        children.add(child);
    }
    
    public long getRefference() {
        return refference;
    }
    
    public DwarfDeclaration getDeclaration() {
        TAG kind = getKind();
        String name = getQualifiedName();
        String type = getType();
        String paramStr = "";
        
        if (kind.equals(TAG.DW_TAG_subprogram)) {
            ArrayList<DwarfEntry> params = getParameters();
            paramStr = "(";
            DwarfEntry param = null;
            for (Iterator<DwarfEntry> i = params.iterator(); i.hasNext(); ) {
                param = i.next();
                
                if (param.getKind().equals(TAG.DW_TAG_unspecified_parameters)) {
                    paramStr += "...";
                } else {
                    paramStr += param.getType() + " " + param.getName();
                }
                
                if (i.hasNext()) {
                    paramStr += ", ";
                }
            }
            paramStr += ")";
        }
        
        String declarationString = type + " " + name + paramStr;
        
        int declarationLine = getUintAttributeValue(ATTR.DW_AT_decl_line);
        int declarationColumn = getUintAttributeValue(ATTR.DW_AT_decl_column);
        
        String declarationPosition = ((declarationLine == -1) ? "" : declarationLine) +
                ((declarationColumn == -1) ? "" : ":" + declarationColumn);
        
        declarationPosition += " <" + refference + " (0x" + Long.toHexString(refference) + ")>";
        
        return new DwarfDeclaration(kind.toString(), declarationString, declarationPosition);
    }
    
    public ArrayList<DwarfEntry> getParameters() {
        ArrayList<DwarfEntry> result = new ArrayList<DwarfEntry>();
        ArrayList<DwarfEntry> children = getChildren();
        
        for (DwarfEntry child: children) {
            if (child.isParameter() && !child.isArtifitial()) {
                result.add(child);
            }
        }
        
        return result;
    }
    
    public ArrayList<DwarfEntry> getMembers() {
        ArrayList<DwarfEntry> result = new ArrayList<DwarfEntry>();
        ArrayList<DwarfEntry> children = getChildren();
        
        for (DwarfEntry child: children) {
            if (child.isMember() && !child.isArtifitial()) {
                result.add(child);
            }
        }
        
        return result;
    }
    
    
    public String toString() {
        return getDeclaration().toString();
    }
    
    public TAG getTag() {
        return abbriviationTableEntry.getKind();
    }
    
    public void dump(PrintStream out) {
        out.print("<" + hierarchyLevel + "><" + Long.toHexString(refference) + ">: ");
        abbriviationTableEntry.dump(out, this);
        
        for (int i = 0; i < children.size(); i++) {
            children.get(i).dump(out);
        }
    }
    
    private boolean isArtifitial() {
        Object isArt = getAttributeValue(ATTR.DW_AT_artificial);
        return ((isArt != null) && ((Boolean)isArt).booleanValue());
    }
    
    private boolean isParameter() {
        TAG kind = getKind();
        return kind.equals(TAG.DW_TAG_formal_parameter) || kind.equals(TAG.DW_TAG_unspecified_parameters);
    }
    
    private boolean isMember() {
        TAG kind = getKind();
        //return kind.equals(TAG.DW_TAG_member);
        return !kind.equals(TAG.DW_TAG_inheritance);
    }

    public boolean isEntryDefinedInFile(int fileEntryIdx) {
        int fileIdx = getUintAttributeValue(ATTR.DW_AT_decl_file);
        return (fileIdx == fileEntryIdx);
    }

//    public String getFile() {
//        int fileIdx = (Integer)getUintAttributeValue(ATTR.DW_AT_decl_file);
//        DwarfLineInfoSection lineSection = compilationUnit.getDwarfDebugInfoSection().getLineInfoSection();
//        return lineSection.getFilePath(fileIdx);
//    }

    public String getTypeDef() {
        if (getKind().equals(TAG.DW_TAG_typedef)) {
            return getType();
        }
        
        Integer typeRefIdx = (Integer)getAttributeValue(ATTR.DW_AT_type);
        DwarfEntry typeRef = compilationUnit.getTypedefFor(typeRefIdx);
        
        return (typeRef == null) ? getType() : typeRef.getType();
    }

    ArrayList<Object> getValues() {
        return values;
    }

}
