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

package org.netbeans.modules.cnd.navigation.hierarchy;

import java.util.Iterator;
import javax.swing.JEditorPane;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmNamespaceDefinition;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceResolver;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.openide.cookies.EditorCookie;
import org.openide.nodes.Node;

public class ContextUtils {
    public static final boolean USE_REFERENCE_RESOLVER = getBoolean("hierarchy.use.reference", true); // NOI18N
    
    private ContextUtils() {
    }

    public static boolean getBoolean(String name, boolean result) {
        String text = System.getProperty(name);
        if( text != null ) {
            result = Boolean.parseBoolean(text);
        }
        return result;
    }
    
    public static CsmFile findFile(Node[] activatedNodes) {
        if (activatedNodes != null && activatedNodes.length > 0) {
            if (ContextUtils.USE_REFERENCE_RESOLVER) {
                CsmReference ref = ContextUtils.findReference(activatedNodes[0]);
                if (ref != null && CsmKindUtilities.isInclude(ref.getOwner())) {
                    CsmInclude incl = (CsmInclude) ref.getOwner();
                    CsmFile file = incl.getIncludeFile();
                    if (file != null) {
                        return file;
                    }
                }
            }
            return ContextUtils.findFile(activatedNodes[0]);
        }
        return null;
    }

    public static CsmFile findFile(Node activatedNode) {
        EditorCookie c = activatedNode.getCookie(EditorCookie.class);
        if (c != null) {
            JEditorPane[] panes = c.getOpenedPanes();
            if (panes != null && panes.length>0) {
                return CsmUtilities.getCsmFile(activatedNode,false);
            }
        }
        return null;
    }

    public static CsmClass getContextClass(Node[] activatedNodes){
        CsmObject decl = null;
        if (ContextUtils.USE_REFERENCE_RESOLVER) {
            CsmReference ref = ContextUtils.findReference(activatedNodes[0]);            
            if (isSupportedReference(ref)) {
                decl = ref.getReferencedObject();
                if (CsmKindUtilities.isClass(decl)){
                    return (CsmClass)decl;
                } else if (CsmKindUtilities.isVariable(decl)){
                    CsmVariable v = (CsmVariable)decl;
                    CsmType type = v.getType();
                    // could be null type for parameter with vararg "..." type
                    CsmClassifier cls = type == null ? null : type.getClassifier();
                    if (CsmKindUtilities.isClass(cls)){
                        return (CsmClass)cls;
                    }
                }
            }
        }
        decl = ContextUtils.findDeclaration(activatedNodes[0]);
        if (CsmKindUtilities.isClass(decl)){
            return (CsmClass)decl;
        }
        return null;
    }

    public static boolean isSupportedReference(CsmReference ref) {
        return ref != null && 
                !CsmKindUtilities.isMacro(ref.getOwner()) &&
                !CsmKindUtilities.isInclude(ref.getOwner());        
    }
    
    public static CsmReference findReference(Node activatedNode) {
        return CsmReferenceResolver.getDefault().findReference(activatedNode);
    }
    
    public static CsmDeclaration findDeclaration(Node activatedNode) {
        EditorCookie c = activatedNode.getCookie(EditorCookie.class);
        if (c != null) {
            JEditorPane[] panes = c.getOpenedPanes();
            if (panes != null && panes.length>0) {
                int offset = panes[0].getCaret().getDot();
                CsmFile file = CsmUtilities.getCsmFile(activatedNode,false);
                if (file != null){
                    return findInnerFileDeclaration(file, offset);
                }
            }
        }
        return null;
    }
    
    private static CsmDeclaration findInnerFileDeclaration(CsmFile file, int offset) {
        CsmDeclaration innerDecl = null;
        for (Iterator it = file.getDeclarations().iterator(); it.hasNext();) {
            CsmDeclaration decl = (CsmDeclaration) it.next();
            if (isInObject(decl, offset)) {
                innerDecl = findInnerDeclaration(decl, offset);
                innerDecl = innerDecl != null ? innerDecl : decl;
                break;
            }
        }
        return innerDecl;
    }

    private static CsmDeclaration findInnerDeclaration(CsmDeclaration outDecl, int offset) {
        Iterator it = null;
        if (CsmKindUtilities.isNamespaceDefinition(outDecl)) {
            it = ((CsmNamespaceDefinition) outDecl).getDeclarations().iterator();
        } else if (CsmKindUtilities.isClass(outDecl)) {
            CsmClass cl  = (CsmClass)outDecl;
            it = cl.getMembers().iterator();
        }
        if (it != null) {
            while (it.hasNext()) {
                CsmDeclaration decl = (CsmDeclaration) it.next();
                if (isInObject(decl, offset)) {
                    CsmDeclaration innerDecl = findInnerDeclaration(decl, offset);
                    if (CsmKindUtilities.isClass(innerDecl)){
                        return innerDecl;
                    } else if (CsmKindUtilities.isClass(decl)){
                        return decl;
                    }
                    break;
                }
            }
        }
        return null;
    }    

    private static boolean isInObject(CsmObject obj, int offset) {
        if (!CsmKindUtilities.isOffsetable(obj)) {
            return false;
        }
        CsmOffsetable offs = (CsmOffsetable)obj;
        if ((offs.getStartOffset() <= offset) &&
                (offset <= offs.getEndOffset())) {
            return true;
        }
        return false;
    }

}
