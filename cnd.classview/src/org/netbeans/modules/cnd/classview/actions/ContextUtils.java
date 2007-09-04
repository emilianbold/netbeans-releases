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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.classview.actions;

import java.util.Iterator;
import javax.swing.JEditorPane;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmNamespaceDefinition;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.openide.cookies.EditorCookie;
import org.openide.nodes.Node;

public class ContextUtils {
    private ContextUtils() {
    }

    public static CsmOffsetableDeclaration getContext(Node[] activatedNodes){
        if (activatedNodes != null && activatedNodes.length > 0){
            return ContextUtils.findDeclaration(activatedNodes[0]);
        }
        return null;
    }

    private static CsmOffsetableDeclaration findDeclaration(Node activatedNode) {
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
    
    private static CsmOffsetableDeclaration findInnerFileDeclaration(CsmFile file, int offset) {
        CsmOffsetableDeclaration innerDecl = null;
        if (innerDecl == null) {
            for (CsmOffsetableDeclaration decl : file.getDeclarations()) {
                if (isInObject(decl, offset)) {
                    innerDecl = findInnerDeclaration(decl, offset);
                    innerDecl = innerDecl != null ? innerDecl : decl;
                    break;
                }
            }
        }
        return innerDecl;
    }

    private static CsmOffsetableDeclaration findInnerDeclaration(CsmOffsetableDeclaration outDecl, int offset) {
        Iterator it = null;
        CsmOffsetableDeclaration innerDecl = null;
        if (CsmKindUtilities.isNamespaceDefinition(outDecl)) {
            it = ((CsmNamespaceDefinition) outDecl).getDeclarations().iterator();
        } else if (CsmKindUtilities.isClass(outDecl)) {
            CsmClass cl  = (CsmClass)outDecl;
            it = cl.getMembers().iterator();
        }
        if (it != null) {
            while (it.hasNext()) {
                CsmOffsetableDeclaration decl = (CsmOffsetableDeclaration) it.next();
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
