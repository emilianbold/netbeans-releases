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


import java.util.Comparator;
import java.util.Iterator;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfEntry;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.ATTR;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.TAG;


/**
 * Common functions needed for comparison 
 * (and probably not only just for comparison)
 * @author Vladimir Kvashin
 */
public class ComparisonUtils {


    public static class DwarfEntryComparator implements Comparator<DwarfEntry> {
	public int compare(DwarfEntry entry1, DwarfEntry entry2) {
	    int line1 = entry1.getLine();
	    int line2 = entry2.getLine();
	    return line1 - line2;
	}
	public boolean equals(Object obj) {
	    return this == obj;
	}
    }
    
    public static class CsmDeclarationComparator implements Comparator<CsmDeclaration> {
	public int compare(CsmDeclaration decl1, CsmDeclaration decl2) {
	    int line1 = ((CsmOffsetable) decl1).getStartOffset();
	    int line2 = ((CsmOffsetable) decl2).getStartOffset();
	    return line1 - line2;
	}
	public boolean equals(Object obj) {
	    return this == obj;
	}
    }    
    
    public static String getText(CsmType type) {
	CsmClassifier classifier = type.getClassifier();
	String text = CsmTypeGetText(type, classifier); //type.getText();
	if( (classifier == null || classifier.getClass().getName().endsWith("UnresolvedClass")) && ! DMFlags.UNRESOLVED_TOLERANT ) { // NOI18N
	    text = "unresolved::" + text; // NOI18N
	}
// We don't need this workaround since this is solved on Dwarf side	
//        else {
//            // Workaround: dwarf duplicates "const" in the case of const references
//            // so we're adding one more "const" too here
//            if( type.isReference() && type.isConst() ) {
//                text = "const " + text;   // NOI18N
//            }
//        }
	return text;
    }
    
    /** 
     * Almost exact copy of CsmType.getText
     *
     * It's rather strange to have it as a separate function, but in this case
     * it's easier to understand it's the same as CsmType.getText.
     *
     * Why do we need this copy? To use classifier instead of getting text as it's written in code.
     * CsmType doesn't use getClassifier() because it's too expensive
     */
    private static String CsmTypeGetText(CsmType type, CsmClassifier classifier) {
	if( DMFlags.UNRESOLVED_TOLERANT && classifier == null ) {
	    return type.getText().toString();
	}
//        if( text == null ) {
            StringBuffer sb = new StringBuffer();
            if( type.isConst() ) {
                sb.append("const "); // NOI18N
            }
            sb.append(classifier == null ? "unresolved" : classifier.getName()); // NOI18N
            for( int i = 0; i < type.getPointerDepth(); i++ ) {
                sb.append('*');
            }
            if( type.isReference() ) {
                sb.append('&');
            }
            for( int i = 0; i < type.getArrayDepth(); i++ ) {
                sb.append("[]"); // NOI18N
            }
//            text = sb.toString();
//        }
//        return text;
            return sb.toString();
    }    
    
    /**
     * Gets function signature 
     * copy-pasted from FunctionImpl.getSignature, 
     * then modified to show unresolved
     */
    public static String getSignature(CsmFunction function) {
        // TODO: this fake implementation for Deimos only!
        // we should resolve parameter types and provide
        // kind of canonical representation here
        StringBuffer sb = new StringBuffer(function.getName());
        sb.append('(');
        for( Iterator iter = function.getParameters().iterator(); iter.hasNext(); ) {
            CsmParameter param = (CsmParameter) iter.next();
            CsmType type = param.getType();
            if( type != null )  {
                sb.append(getText(type));	// type.getText()
                if( iter.hasNext() ) {
                    sb.append(',');
                }
            }
        }
        sb.append(')');
        if( isConst(function) ) {
            sb.append(" const"); // NOI18N
        }
        return sb.toString();
    }    
    
    private static boolean isConst(CsmFunction function) {
	CsmMethod method = null;
	if( CsmKindUtilities.isMethodDeclaration(function) ) {
	    method = (CsmMethod) function;
	}
	else if( CsmKindUtilities.isFunctionDefinition(function) ) {
	    CsmFunction decl = ((CsmFunctionDefinition) function).getDeclaration();
	    if( CsmKindUtilities.isMethod(decl) ) {
		method = (CsmMethod) decl;
	    }
	}
	return (method == null) ? false : method.isConst();
    }
  
    public static boolean isArtificial(DwarfEntry entry) {
	if (entry.getAttributeValue(ATTR.DW_AT_abstract_origin) != null) {
	    return true;
	}
	if (entry.getAttributeValue(ATTR.DW_AT_artificial) != null) {
	    return true;
	}
	String qname = entry.getQualifiedName();
	if (qname != null && qname.startsWith("_GLOBAL__")) { // NOI18N
	    return true;
	}
	else {
	    return false;
	}
    }    
    
    public static boolean isEmpty(String s) {
	return s == null || s.length() == 0;
    }
    
    public static String getQualifiedName(CsmDeclaration decl) {
	if( CsmKindUtilities.isVariableDefinition(decl) ) {
	    CsmVariable var = ((CsmVariableDefinition) decl).getDeclaration();
	    if( var != null ) {
		return var.getQualifiedName().toString();
	    }
	}
	return decl.getQualifiedName().toString();
    }

    public static boolean isFunction(DwarfEntry entry) {
	TAG kind = entry.getKind();
	switch (kind) {
	    case DW_TAG_member:
	    case DW_TAG_subroutine_type:
	    case DW_TAG_subprogram:
		return true;
	    default:
		return false;
	}
    }
    
    public static boolean isEnum(DwarfEntry entry) {
	TAG kind = entry.getKind();
	switch (kind) {
	    case DW_TAG_enumeration_type:
		return true;
	    default:
		return false;
	}
    }

    public static boolean isVariable(DwarfEntry entry) {
	TAG kind = entry.getKind();
	switch (kind) {
	    case DW_TAG_variable:
	    case DW_TAG_member:
	    case DW_TAG_formal_parameter:
	    case DW_TAG_unspecified_parameters:
	    case DW_TAG_constant:
		return true;
	    default:
		return false;
	}
    }

    public static boolean isClass(DwarfEntry entry) {
	TAG kind = entry.getKind();
	switch (kind) {
	    case DW_TAG_SUN_class_template:
	    case DW_TAG_interface_type:
	    case DW_TAG_structure_type:
	    //case DW_TAG_typedef:
	    case DW_TAG_union_type:
		return true;
	    default:
		return false;
	}
    }
    
    public static boolean isTypedef(DwarfEntry entry) {
	TAG kind = entry.getKind();
	switch (kind) {
	    case DW_TAG_typedef:
		return true;
	    default:
		return false;
	}
    }
    
    public static boolean isParameter(DwarfEntry entry) {
	TAG kind = entry.getKind();
	switch (kind) {
	    case DW_TAG_formal_parameter:
	    case DW_TAG_unspecified_parameters:
		return true;
	    default:
		return false;
	}
    }
	
    public static String getName(DwarfEntry entry) {
	String name = entry.getName();
	if( name.indexOf('<') >= 0 ) {
	    StringBuilder sb = new StringBuilder();
	    int level = 0;
	    for( int i = 0; i < name.length(); i++ ) {
		char c = name.charAt(i);
		if( c == '<' ) {
		    level++;
		}
		else if( c == '<' ) {
		    level--;
		}
		else if(level == 0) {
		    sb.append(c);
		}
	    }
	    name = sb.toString();
	}
	name = name.replaceAll(" ", ""); // NOI18N
	return name;
    }
}
