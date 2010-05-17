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

package dwarfvsmodel;

import java.io.PrintStream;
import java.util.*;
import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfEntry;
import org.netbeans.modules.cnd.dwarfdump.CompilationUnit;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.ATTR;
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
	
	// for templates, dwarf places an instance for each template instantiation
	// skip them
	for( DwarfEntry overload : overloads ) {
	    if( overload.getLine() == entry.getLine() ) {
		//System.err.println("\Excluding \n\t" + entry + " \n\tbecause found \n\t" + overload + "\n");
		return;
	    }
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
			    return ComparisonUtils.getName(entry);
			case Ignore:
			default:
			    return "";
		    }
		}
		else {
		    return parentQName + "::" + ComparisonUtils.getName(entry); // NOI18N
		}
	    }
	}	
	else {
	    return fqn;
	}
    }
    
    public String qualifiedNameFromMangled(DwarfEntry entry) {
	String ln = (String) entry.getAttributeValue(ATTR.DW_AT_MIPS_linkage_name);
	if( ln != null ) {
	    if( ln.startsWith("_ZN") ) { // NOI18N
		int pos = 3;
		StringBuilder qn = new StringBuilder();
		if( ln.startsWith("_ZNSt") ) { // NOI18N
		    pos += 2;
		    qn.append("std::"); // NOI18N
		}		
		while( pos < ln.length() && Character.isDigit(ln.charAt(pos)) ) {
		    int len = 0;
		    int mul = 1;
		    for( char c = ln.charAt(pos); pos < ln.length() && Character.isDigit(c); c = ln.charAt(++pos) ) {
			len *= 10;
			len += (int) c - (int) '0';
		    }
		    if( len <= 0 ) {
			return null;
		    }
		    if( pos+len > ln.length() ) {
			return null;
		    }
		    if( qn.length() > 0 ) {
			qn.append("::"); // NOI18N
		    }
		    qn.append(ln.substring(pos, pos+len));
		    pos += len;
		}
		if( qn.length() > 0 ) {
		    String name = entry.getName();
		    if( name.startsWith("operator") && ! Character.isJavaIdentifierPart(name.charAt(8)) ) { // NOI18N
			qn.append("::"); // NOI18N
			qn.append(name);
		    }
		    return qn.toString();
		}
	    }
	}
	return null;
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
