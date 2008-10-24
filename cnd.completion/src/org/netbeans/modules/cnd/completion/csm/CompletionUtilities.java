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

package org.netbeans.modules.cnd.completion.csm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.cnd.completion.cplusplus.ext.CsmCompletionQuery;
import org.netbeans.modules.cnd.completion.cplusplus.ext.CsmResultItem;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.editor.BaseDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.SyntaxSupport;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.completion.cplusplus.CsmCompletionProvider;
import org.netbeans.modules.cnd.completion.cplusplus.ext.CsmCompletionQuery.CsmCompletionResult;
import org.netbeans.modules.cnd.completion.impl.xref.FileReferencesContext;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.editor.NbEditorUtilities;

/**
 *
 * @author Vladimir Voskresensky
 */
public class CompletionUtilities {


    public static List/*<CsmDeclaration*/ findFunctionLocalVariables(BaseDocument doc, int offset, FileReferencesContext fileReferncesContext) {
        CsmFile file = CsmUtilities.getCsmFile(doc, true);
        if (file == null || !file.isValid()) {
            return Collections.<CsmDeclaration>emptyList();
        }
        CsmContext context = CsmOffsetResolver.findContext(file, offset, fileReferncesContext);
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
        if (file == null || !file.isValid()) {
            return Collections.<CsmDeclaration>emptyList();
        }        
        CsmContext context = CsmOffsetResolver.findContext(file, offset, null);
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
        if (file == null || !file.isValid()) {
            return null;
        }        
        CsmContext context = CsmOffsetResolver.findContext(file, offset, null);
        CsmClass clazz = CsmContextUtilities.getClass(context, true, false);
        return clazz;
    }
    public static CsmOffsetableDeclaration findFunDefinitionOrClassOnPosition(BaseDocument doc, int offset) {
        return findFunDefinitionOrClassOnPosition(doc, offset, null);
    }

    public static CsmOffsetableDeclaration findFunDefinitionOrClassOnPosition(BaseDocument doc, int offset, FileReferencesContext fileReferncesContext) {
        CsmOffsetableDeclaration out = null;
        CsmFile file = CsmUtilities.getCsmFile(doc, true);
        if (file != null) {
            CsmContext context = CsmOffsetResolver.findContext(file, offset, fileReferncesContext);
            out = CsmContextUtilities.getFunctionDefinition(context);
            if (out == null || !CsmContextUtilities.isInFunctionBodyOrInitializerList(context, offset)) {
                out = CsmContextUtilities.getClass(context, false, false);
            }
        }
        return out;
    }
    
    public static CsmObject findItemAtCaretPos(JTextComponent target, int dotPos){
        return findItemAtCaretPos(target, null, CsmCompletionProvider.getCompletionQuery(), dotPos);
    }

    public static CsmObject findItemAtCaretPos(JTextComponent target, Document doc, CsmCompletionQuery query, int dotPos){
        try {
            BaseDocument baseDoc = null;
            if (doc instanceof BaseDocument) {
                baseDoc = (BaseDocument)doc;
            }
            baseDoc = baseDoc != null ? baseDoc : (BaseDocument)target.getDocument();
            SyntaxSupport sup = baseDoc.getSyntaxSupport();
            int[] idFunBlk = NbEditorUtilities.getIdentifierAndMethodBlock(baseDoc, dotPos);
            
            if (idFunBlk == null) {
                idFunBlk = new int[] { dotPos, dotPos };
            }
            
            boolean searchFuncsOnly = (idFunBlk.length == 3);
            for (int ind = idFunBlk.length - 1; ind >= 1; ind--) {
                CsmCompletionResult result = query.query(target, baseDoc, idFunBlk[ind], sup, true, false);
                if (result != null && result.getItems().size() > 0) {
                    List<CsmObject> filter = getAssociatedObjects(result.getItems(), searchFuncsOnly);
                    CsmObject itm = filter.size() > 0 ? filter.get(0) : getAssociatedObject(result.getItems().get(0));
                    if (filter.size() > 1 && searchFuncsOnly) {
                        // It is overloaded method, lets check for the right one
                        int endOfMethod = findEndOfMethod(baseDoc, idFunBlk[ind]-1);
                        if (endOfMethod > -1){
                            CsmCompletionResult resultx = query.query(target, baseDoc, endOfMethod, sup, true, false);
                            if (resultx != null && resultx.getItems().size() > 0) {
                                return getAssociatedObject(resultx.getItems().get(0));
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
    
    private static List<CsmObject> getAssociatedObjects(List items, boolean wantFuncsOnly) {
        List<CsmObject> out = new ArrayList();
        List<CsmObject> funcs = new ArrayList();
        
        for (Object item : items) {
            if (item instanceof CsmResultItem){
                CsmObject ret = getAssociatedObject(item);
                boolean isFunc = CsmKindUtilities.isFunction(ret);
                if (isFunc) {
                    funcs.add(ret);
                } else {
                    out.add(ret);
                }
            }
        }
        if (!wantFuncsOnly) {
            out.addAll(funcs);
        } else {
            out = funcs;
        }
        return out;        
    }    
    
    private static CsmObject getAssociatedObject(Object item) {
        if (item instanceof CsmResultItem){
            CsmObject ret = (CsmObject) ((CsmResultItem)item).getAssociatedObject();
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
