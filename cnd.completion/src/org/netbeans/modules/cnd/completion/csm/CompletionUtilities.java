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

package org.netbeans.modules.cnd.completion.csm;

import java.util.List;
import org.netbeans.modules.cnd.completion.cplusplus.ext.CsmCompletionQuery;
import org.netbeans.modules.cnd.completion.cplusplus.ext.CsmResultItem;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.editor.BaseDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.SyntaxSupport;
import org.netbeans.editor.ext.CompletionQuery;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.completion.cplusplus.CsmCompletionProvider;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.editor.NbEditorUtilities;

/**
 *
 * @author Vladimir Voskresensky
 */
public class CompletionUtilities {


    public static List/*<CsmDeclaration*/ findFunctionLocalVariables(BaseDocument doc, int offset) {
        CsmFile file = CsmUtilities.getCsmFile(doc, true);
        CsmContext context = CsmOffsetResolver.findContext(file, offset);
        return CsmContextUtilities.findFunctionLocalVariables(context);
    }
    
    public static List/*<CsmDeclaration*/ findClassFields(BaseDocument doc, int offset) {
        CsmClass clazz = findClassOnPosition(doc, offset);
        List res = null;
        if (clazz != null) {
            res = new CsmProjectContentResolver().getFields(clazz, false);
        }
        return res;
    }
    
    public static List/*<CsmDeclaration*/ findFileVariables(BaseDocument doc, int offset) {
        CsmFile file = CsmUtilities.getCsmFile(doc, true);
        CsmContext context = CsmOffsetResolver.findContext(file, offset);
        return CsmContextUtilities.findFileLocalVariables(context);
    }
    
    public static List/*<CsmDeclaration*/ findGlobalVariables(BaseDocument doc, int offset) {
        CsmProject prj = CsmUtilities.getCsmProject(doc);
        if (prj == null) {
            return null;
        }
        return CsmContextUtilities.findGlobalVariables(prj);
    }

    // TODO: think if we need it?
    public static CsmClass findClassOnPosition(BaseDocument doc, int offset) {
        CsmFile file = CsmUtilities.getCsmFile(doc, true);
        CsmContext context = CsmOffsetResolver.findContext(file, offset);
        CsmClass clazz = CsmContextUtilities.getClass(context, true);
        return clazz;
    }

    public static CsmOffsetableDeclaration findFunDefinitionOrClassOnPosition(BaseDocument doc, int offset) {
        CsmFile file = CsmUtilities.getCsmFile(doc, true);
        CsmContext context = CsmOffsetResolver.findContext(file, offset);
        CsmOffsetableDeclaration out = null;
        out = CsmContextUtilities.getFunctionDefinition(context);
        if (out == null || !CsmContextUtilities.isInFunctionBody(context, offset)) {
            out = CsmContextUtilities.getClass(context, false);
        }
        return out;
    }
    
    public static CsmObject findItemAtCaretPos(JTextComponent target, BaseDocument doc, CsmCompletionQuery query, int dotPos){
        try {
            doc = doc != null ? doc : (BaseDocument)target.getDocument();
            SyntaxSupport sup = doc.getSyntaxSupport();
            int[] idFunBlk = NbEditorUtilities.getIdentifierAndMethodBlock(doc, dotPos);
            
            if (idFunBlk == null) {
                idFunBlk = new int[] { dotPos, dotPos };
            }
            
            for (int ind = idFunBlk.length - 1; ind >= 1; ind--) {
                CompletionQuery.Result result = query.query(target, doc, idFunBlk[ind], sup, true, false);
                if (result != null && result.getData().size() > 0) {
                    CsmObject itm = getAssociatedObject(result.getData().get(0));
                    if (result.getData().size() > 1 && (CsmKindUtilities.isFunction(itm))) {
                        // It is overloaded method, lets check for the right one
                        int endOfMethod = findEndOfMethod(doc, idFunBlk[ind]-1);
                        if (endOfMethod > -1){
                            CompletionQuery.Result resultx = query.query(target, doc, endOfMethod, sup, true, false);
                            if (resultx != null && resultx.getData().size() > 0) {
                                return getAssociatedObject(resultx.getData().get(0));
                            }
                        }
                    }
                    return itm;
                }
            }
        } catch (BadLocationException e) {
        }
        return null;
    }
    
    public static CsmObject findItemAtCaretPos(JTextComponent target, int dotPos){
        return findItemAtCaretPos(target, null, CsmCompletionProvider.getCompletionQuery(), dotPos);
    }

    private static CsmObject getAssociatedObject(Object item) {
        if (item instanceof CsmResultItem){
            CsmObject ret = (CsmObject) ((CsmResultItem)item).getAssociatedObject();
//            // for constructors return class
//            if (CsmKindUtilities.isConstructor(ret)) {
//                ret = ((CsmConstructor)ret).getContainingClass();
//            }
            if (ret != null) {
                return ret;
            }
        }
        return null;
    }

    static int findEndOfMethod(BaseDocument doc, int startPos){
        try{
            int level = 0;
            for(int i = startPos;  i<doc.getLength(); i++){
                char ch = doc.getChars(i, 1)[0];
                if (ch == ';') return -1;
                if (ch == '(') level++;
                if (ch == ')'){
                    level--;
                    if (level == 0){
                        return i+1;
                    }
                }
            }
            return -1;
        } catch (BadLocationException e) {
            return -1;
        }
    }    
   
}
