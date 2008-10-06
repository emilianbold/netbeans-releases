/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmIdentifiable;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.modelimpl.csm.NamespaceImpl;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import static org.netbeans.modules.cnd.api.model.CsmDeclaration.Kind.*;


/**
 * Misc. (static) utility functions
 * @author Vladimir Kvashin
 */
public class Utils {
    
    public static final Logger LOG = Logger.getLogger("org.netbeans.modules.cnd.modelimpl"); // NOI18N
    private static final int LOG_LEVEL = Integer.getInteger("org.netbeans.modules.cnd.modelimpl.level", -1).intValue(); // NOI18N
    
    static {
        // command line param has priority for logging
        // do not change it
        if (LOG_LEVEL == -1) {
            // command line param has priority for logging
            if (TraceFlags.DEBUG) {
                LOG.setLevel(Level.ALL);
            } else {
                LOG.setLevel(Level.SEVERE);
            }
        }
    }
    
    public static CsmOffsetable createOffsetable(CsmFile file, int startOffset, int endOffset) {
        return new OffsetableBase(file, startOffset, endOffset);
    }
    
    public static String getQualifiedName(String name, CsmNamespace parent) {
	StringBuilder sb = new StringBuilder(name);
        if (parent != null) {
            if (!parent.isGlobal()) {
		sb.insert(0, "::"); // NOI18N
		sb.insert(0, parent.getQualifiedName());
            }
        }
        return sb.toString();
    }
      
    public static String getNestedNamespaceQualifiedName(CharSequence name, NamespaceImpl parent, boolean createForEmptyNames) {
	StringBuilder sb = new StringBuilder(name);
        if (parent != null) {
            if (name.length() == 0 && createForEmptyNames) {
                sb.append(parent.getNameForUnnamedElement());
            }
            if (!parent.isGlobal()) {
		sb.insert(0, "::"); // NOI18N
		sb.insert(0, parent.getQualifiedName());
            }
        }
        return sb.toString();
    }
    
    public static String toString(String[] a) {
	StringBuilder sb = new StringBuilder("["); // NOI18N
	for (int i = 0; i < a.length; i++) {
	    if( i > 0 ) {
		sb.append(',');
	    }
	    sb.append(a[i]);
	}
	sb.append(']');
	return sb.toString();
    }
    
    public static String[] splitQualifiedName(String qualified) {
        List v = new ArrayList();
        for (StringTokenizer t = new StringTokenizer(qualified, ": \t\n\r\f", false); t.hasMoreTokens(); ) {// NOI18N 
            v.add(t.nextToken());
        }
        return (String[]) v.toArray(new String[v.size()]);
    }   
    
    public static void disposeAll(Collection<? extends CsmObject> coll) {
        for (CsmObject elem : coll) {
            if( elem  instanceof Disposable ) {
                Disposable decl = (Disposable) elem;
                if (TraceFlags.TRACE_DISPOSE) {
                    System.err.println("disposing with UID " + ((CsmIdentifiable)elem).getUID());
                }
                decl.dispose();
            } else {
                if (TraceFlags.TRACE_DISPOSE) {
                    System.err.println("non disposable with UID " + ((CsmIdentifiable)elem).getUID());
                }
            }            
        }
    }
    
    public static String getCsmIncludeKindkey() {
        // Returned string should be differed from getCsmDeclarationKindkey()
        return "I"; // NOI18N
    }

    public static CharSequence[] getAllClassifiersUniqueNames(CharSequence uniqueName) {
        CharSequence namePostfix = uniqueName.subSequence(1, uniqueName.length());
        CharSequence out[] = new CharSequence[]
                                {
                                getCsmDeclarationKindkey(CsmDeclaration.Kind.CLASS) + namePostfix,
                                getCsmDeclarationKindkey(CsmDeclaration.Kind.STRUCT) + namePostfix,
                                getCsmDeclarationKindkey(CsmDeclaration.Kind.UNION) + namePostfix,
                                getCsmDeclarationKindkey(CsmDeclaration.Kind.ENUM) + namePostfix,
                                getCsmDeclarationKindkey(CsmDeclaration.Kind.TYPEDEF) + namePostfix,
                                getCsmDeclarationKindkey(CsmDeclaration.Kind.CLASS_FORWARD_DECLARATION) + namePostfix
                                };
        return out;
    }

    public static String getCsmDeclarationKindkey(CsmDeclaration.Kind kind) {
        // Returned string should be differed from getCsmIncludeKindkey()
	switch( kind ) {
	    case ASM:			    return "A"; // NOI18N
	    case BUILT_IN:		    return "B"; // NOI18N
	    case CLASS:			    return "C"; // NOI18N
	    case ENUM:			    return "E"; // NOI18N
	    case FUNCTION:		    return "F"; // NOI18N
	    case MACRO:			    return "M"; // NOI18N
	    case NAMESPACE_DEFINITION:	    return "N"; // NOI18N
	    case STRUCT:		    return "S"; // NOI18N
	    case TEMPLATE_DECLARATION:	    return "T"; // NOI18N
	    case UNION:			    return "U"; // NOI18N
	    case VARIABLE:		    return "V"; // NOI18N
	    case NAMESPACE_ALIAS:	    return "a"; // NOI18N
	    case ENUMERATOR:		    return "e"; // NOI18N
	    case FUNCTION_DEFINITION:	    return "f"; // NOI18N
	    case USING_DIRECTIVE:	    return "g"; // NOI18N
            case TEMPLATE_PARAMETER:        return "p"; // NOI18N
	    case CLASS_FRIEND_DECLARATION:  return "r"; // NOI18N
	    case TEMPLATE_SPECIALIZATION:   return "s"; // NOI18N
	    case TYPEDEF:		    return "t"; // NOI18N
	    case USING_DECLARATION:	    return "u"; // NOI18N
	    case VARIABLE_DEFINITION:	    return "v"; // NOI18N
	    case CLASS_FORWARD_DECLARATION: return "w"; // NOI18N
            default:	throw new IllegalArgumentException("Unexpected value of CsmDeclaration.Kind:" + kind); //NOI18N
	}
    }

    public static CsmDeclaration.Kind getCsmDeclarationKind(char kind) {
	switch( kind ) {
	    case 'A': return ASM; // NOI18N
            case 'B': return BUILT_IN; // NOI18N
            case 'C': return CLASS; // NOI18N
	    case 'E': return ENUM; // NOI18N
	    case 'F': return FUNCTION; // NOI18N
	    case 'M': return MACRO; // NOI18N
	    case 'N': return NAMESPACE_DEFINITION; // NOI18N
	    case 'S': return STRUCT; // NOI18N
	    case 'T': return TEMPLATE_DECLARATION; // NOI18N
            case 'U': return UNION; // NOI18N
	    case 'V': return VARIABLE; // NOI18N
	    case 'a': return NAMESPACE_ALIAS; // NOI18N
	    case 'e': return ENUMERATOR; // NOI18N
	    case 'f': return FUNCTION_DEFINITION; // NOI18N
	    case 'g': return USING_DIRECTIVE; // NOI18N
            case 'p': return TEMPLATE_PARAMETER; // NOI18N
	    case 'r': return CLASS_FRIEND_DECLARATION; // NOI18N
	    case 's': return TEMPLATE_SPECIALIZATION; // NOI18N
	    case 't': return TYPEDEF; // NOI18N
	    case 'u': return USING_DECLARATION; // NOI18N
	    case 'v': return VARIABLE_DEFINITION; // NOI18N
	    case 'w': return CLASS_FORWARD_DECLARATION; // NOI18N
            default:	throw new IllegalArgumentException("Unexpected char for CsmDeclaration.Kind: " + kind); //NOI18N
	}
        
    }
}
