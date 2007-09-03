/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
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

package org.netbeans.modules.cnd.navigation.hierarchy;

import java.util.Iterator;
import javax.swing.JEditorPane;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmNamespaceDefinition;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
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
            }
        } else {
            decl = ContextUtils.findDeclaration(activatedNodes[0]);
        }
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
        if (innerDecl == null) {
            for (Iterator it = file.getDeclarations().iterator(); it.hasNext();) {
                CsmDeclaration decl = (CsmDeclaration) it.next();
                if (isInObject(decl, offset)) {
                    innerDecl = findInnerDeclaration(decl, offset);
                    innerDecl = innerDecl != null ? innerDecl : decl;
                    break;
                }
            }
        }
        return innerDecl;
    }

    private static CsmDeclaration findInnerDeclaration(CsmDeclaration outDecl, int offset) {
        Iterator it = null;
        CsmDeclaration innerDecl = null;
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
                    innerDecl = findInnerDeclaration(decl, offset);
                    innerDecl = innerDecl != null ? innerDecl : decl;
                    break;
                }
            }
        }
        return innerDecl;
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
