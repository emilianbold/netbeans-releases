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

import java.util.*;
import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfEntry;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.TAG;

/**
 * Misc. dwarf-related utility functions
 * @author vk155633
 */
public class DwarfTree {
    
    // We need a tree-like structure
    // that we'll use for comparison of declarations within bodies
    
    /** Creates a node */
    public static Node<DwarfEntry> createDwarfNode(DwarfEntry parent) {
	Comparator<DwarfEntry> comparator = new ComparisonUtils.DwarfEntryComparator();
	Node<DwarfEntry> root = _createDwarfNode(parent);
	root.flatten();
	root.sort(comparator);
//	for( Node<DwarfEntry> child : root.getSubnodes() ) {
//	    child.flatten();
//	    child.sort(comparator);
//	}
	return root;
    }
    
    /** Creates a node */
    private static Node<DwarfEntry> _createDwarfNode(DwarfEntry parent) {
	Iterable<DwarfEntry> dwarfEntries = parent.getChildren();
	Node<DwarfEntry> node = new Node<DwarfEntry>();
	for( DwarfEntry entry : dwarfEntries ) {
	    if( ComparisonUtils.isArtificial(entry) ) {
		continue;
	    }
	    TAG kind = entry.getKind();
	    switch (kind) {
		case DW_TAG_variable:
		case DW_TAG_class_type:
		case DW_TAG_structure_type:
		case DW_TAG_union_type:
		    node.addDeclaration(entry);
		    break;
		case DW_TAG_lexical_block:
		case DW_TAG_try_block:
		case DW_TAG_catch_block:
		    node.addSubnode(_createDwarfNode(entry));
		    break;
		case DW_TAG_array_type:
		case DW_TAG_entry_point:
		case DW_TAG_enumeration_type:
		case DW_TAG_imported_declaration:
		case DW_TAG_member:
		case DW_TAG_pointer_type:
		case DW_TAG_reference_type:
		case DW_TAG_compile_unit:
		case DW_TAG_string_type:
		case DW_TAG_subroutine_type:
		case DW_TAG_typedef:
		case DW_TAG_variant:
		case DW_TAG_common_block:   // fortran only
		case DW_TAG_common_inclusion:
		case DW_TAG_inheritance:
		case DW_TAG_module:
		case DW_TAG_ptr_to_member_type:
		case DW_TAG_set_type:
		case DW_TAG_subrange_type:
		case DW_TAG_with_stmt:
		case DW_TAG_access_declaration:
		case DW_TAG_base_type:
		case DW_TAG_const_type:
		case DW_TAG_constant:
		case DW_TAG_enumerator:
		case DW_TAG_file_type:
		case DW_TAG_friend:
		case DW_TAG_namelist:
		case DW_TAG_namelist_item:
		case DW_TAG_packed_type:
		case DW_TAG_subprogram:
		case DW_TAG_template_type_param:
		case DW_TAG_template_value_param:
		case DW_TAG_thrown_type:
		case DW_TAG_variant_part:
		case DW_TAG_volatile_type:
		case DW_TAG_dwarf_procedure:
		case DW_TAG_restrict_type:
		case DW_TAG_interface_type:
		case DW_TAG_namespace:
		case DW_TAG_imported_module:
		case DW_TAG_unspecified_type:
		case DW_TAG_partial_unit:
		case DW_TAG_imported_unit:
		case DW_TAG_condition:
		case DW_TAG_shared_type:
		case DW_TAG_lo_user:
		case DW_TAG_SUN_class_template:
		case DW_TAG_SUN_rtti_descriptor:
		case DW_TAG_hi_user:
		    System.err.println("### node kind: " + kind + " entry: " + entry + " in " + entry.getDeclarationFilePath());
		    break;
		case DW_TAG_label:
		case DW_TAG_inlined_subroutine:
		case DW_TAG_formal_parameter:
		case DW_TAG_unspecified_parameters:
		    // ignore
		    break;
		default:
		    System.err.println("!!! unknown node kind: " + kind);
		    break;
		    
	    }
	}
	return node;
    }    
}
