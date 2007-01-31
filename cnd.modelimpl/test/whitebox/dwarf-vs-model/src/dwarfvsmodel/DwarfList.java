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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package dwarfvsmodel;

import java.io.PrintStream;
import java.util.*;
import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfEntry;
import org.netbeans.modules.cnd.dwarfdump.CompilationUnit;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.TAG;

/**
 * A list of high-level dwarf declarations - variables and functions
 * @author vk155633
 */
public class DwarfList {
    
    private CompilationUnit compilationUnit;
    private List<DwarfEntry> entries = new ArrayList<DwarfEntry>();
    private Map<DwarfEntry, String> entry2fqn = new HashMap<DwarfEntry, String>();
    private Map<String, List<DwarfEntry>> fqn2entry = new HashMap<String, List<DwarfEntry>>();

    
    /** Creates a new instance of DwarfList */
    public DwarfList(CompilationUnit compilationUnit) {
	this.compilationUnit = compilationUnit;
	add(compilationUnit.getDeclarations());
    }
    
    public DwarfList(CompilationUnit compilationUnit, DwarfEntry entry) {
	this.compilationUnit = compilationUnit;
	add(entry.getChildren());
    }
    
    private static enum Rule {
	Ignore,
	Add,
	AddChildren
    }
    	    
    private void add(Iterable<DwarfEntry> currEntries) {
	for( DwarfEntry entry : currEntries ) {
	    Rule rule = getRule(entry);
	    switch( rule ) {
		case Add:
		    add(entry);
		    break;
		case AddChildren:
		    add(entry.getChildren());
		    break;
		case Ignore:
		default:
		    //nothing
	    } 
	}
    }
    
    private void add(DwarfEntry entry) {
	
	String fqn = getQualifiedName(entry);
	
	List<DwarfEntry> overloads = fqn2entry.get(fqn);
	if( overloads == null ) {
	    overloads = new ArrayList<DwarfEntry>();
	    fqn2entry.put(fqn, overloads);
	}

	if( overloads.size() > 0 ) {
	    // specification and definition shouldn't be both in a list
	    DwarfEntry spec = entry.getSpecification();
	    if( spec != null ) {
		// this is a definition
		if( spec != entry && Collections.replaceAll(overloads, spec, entry) ) {
		    Collections.replaceAll(entries, spec, entry);
		}
		else {
		    overloads.add(entry);
		    entries.add(entry);
		}
	    }
	    else { 
		// spec == null, let's find a definition
		DwarfEntry def = entry.getDefinition();
		if( ! overloads.contains(def) ) {
		    overloads.add(entry);
		    entries.add(entry);
		}
	    }
	}
	else {
	    overloads.add(entry);
	    entries.add(entry);
	}
    }
    
    public Iterable<DwarfEntry> getDeclarations(String qualifiedName) {
	List<DwarfEntry> entries = fqn2entry.get(qualifiedName);
	return entries == null ? (Iterable<DwarfEntry>) Collections.EMPTY_LIST : entries;
    }
    
    private Rule getRule(DwarfEntry entry) {
	if( ComparisonUtils.isArtificial(entry) ) {
	    return Rule.Ignore;
	}
	TAG kind = entry.getKind();
	switch (kind) {
	    case DW_TAG_array_type:
	    case DW_TAG_class_type:
	    case DW_TAG_structure_type:
	    case DW_TAG_enumeration_type:
	    case DW_TAG_member:
	    case DW_TAG_string_type:
	    case DW_TAG_subroutine_type:
	    case DW_TAG_union_type:
	    case DW_TAG_interface_type:
	    case DW_TAG_namespace:
	    case DW_TAG_SUN_class_template:
		return Rule.AddChildren;
	    case DW_TAG_variable:
	    case DW_TAG_typedef:
	    case DW_TAG_constant:
	    case DW_TAG_enumerator:
	    case DW_TAG_subprogram:
		return Rule.Add;
	    case DW_TAG_inheritance:
	    case DW_TAG_access_declaration:
		// description seems very interesting, but I never seen such entries in dump :(
		return Rule.Ignore;
		// TODO: divide the below list into 2 parts:
		// 1) just to ignore
		// 2) to report warning (entries that should never occur here)
	    case DW_TAG_inlined_subroutine:
	    case DW_TAG_lexical_block:
	    case DW_TAG_try_block:
	    case DW_TAG_imported_declaration:
	    case DW_TAG_label:
	    case DW_TAG_entry_point:
	    case DW_TAG_common_block:
	    case DW_TAG_common_inclusion:
	    case DW_TAG_pointer_type:
	    case DW_TAG_reference_type:
	    case DW_TAG_compile_unit:
	    case DW_TAG_unspecified_parameters:
	    case DW_TAG_variant:
	    case DW_TAG_module:
	    case DW_TAG_ptr_to_member_type:
	    case DW_TAG_set_type:
	    case DW_TAG_subrange_type:
	    case DW_TAG_with_stmt:
	    case DW_TAG_base_type:
	    case DW_TAG_catch_block:
	    case DW_TAG_const_type:
	    case DW_TAG_file_type:
	    case DW_TAG_friend:
	    case DW_TAG_namelist:
	    case DW_TAG_namelist_item:
	    case DW_TAG_packed_type:
	    case DW_TAG_template_type_param:
	    case DW_TAG_template_value_param:
	    case DW_TAG_thrown_type:
	    case DW_TAG_variant_part:
	    case DW_TAG_volatile_type:
	    case DW_TAG_dwarf_procedure:
	    case DW_TAG_restrict_type:
	    case DW_TAG_imported_module:
	    case DW_TAG_unspecified_type:
	    case DW_TAG_partial_unit:
	    case DW_TAG_imported_unit:
	    case DW_TAG_condition:
	    case DW_TAG_shared_type:
	    case DW_TAG_lo_user:
	    case DW_TAG_SUN_rtti_descriptor:
	    case DW_TAG_hi_user:
	    case DW_TAG_formal_parameter:
		return Rule.Ignore;
	    default:
		System.err.println("!!! unknown node kind: " + kind);
		return Rule.Ignore;
	}
    }
    
    public Iterable<DwarfEntry> getDeclarations() {
	return entries;
    }
    
    public String getQualifiedName(DwarfEntry entry) {
	String fqn = entry2fqn.get(entry);
	if( fqn == null ) {
	    fqn = findQualifiedName(entry);
	    entry2fqn.put(entry, fqn);
	}
	return fqn;
    }
    
    private String findQualifiedName(DwarfEntry entry) {
	if( entry == null ) {
	    return "";
	}
	String fqn = null; // unfortunately we can't rely on entry.getQualifiedName();
	if( ComparisonUtils.isEmpty(fqn) ) {
	    DwarfEntry spec = entry.getSpecification();
	    if( spec != null ) {
		return getQualifiedName(spec);
	    }
	    else {
		DwarfEntry parent = entry.getParent();
		String parentQName = getQualifiedName(parent);
		if( ComparisonUtils.isEmpty(parentQName)  ) {
		    Rule rule = getRule(entry);
		    switch( rule ) {
			case Add:
			case AddChildren:
			    return entry.getName();
			case Ignore:
			default:
			    return "";
		    }
		}
		else {
		    return parentQName + "::" + entry.getName(); // NOI18N
		}
	    }
	}	
	else {
	    return fqn;
	}
    }
    
    public void dump(PrintStream ps, boolean bodies) {
	ps.println("==== Dwarf comparison list for " + compilationUnit.getSourceFileName() + "  " + compilationUnit.getSourceFileFullName()); // NOI18N
	List<DwarfEntry> sorted = new ArrayList<DwarfEntry>(entries);
	Collections.sort(sorted, new ComparisonUtils.DwarfEntryComparator());
	for( DwarfEntry entry : sorted ) {
	    dump(ps, entry, bodies);
	} 
	ps.println("\n"); // NOI18N
    }
    
    private void dump(PrintStream ps, DwarfEntry entry, boolean bodies) {
	ps.println(toString(entry));
	if( bodies && ComparisonUtils.isFunction(entry) ) {
	    Node<DwarfEntry> node = DwarfTree.createDwarfNode(entry);
	    Tracer tr = new Tracer(ps);
	    tr.indent();
	    tr.traceDwarf(node);
	}
    }
    
    public String toString(DwarfEntry entry) {
	return getQualifiedName(entry) + "    " + entry.getDeclaration(); // NOI18N
    }
}
