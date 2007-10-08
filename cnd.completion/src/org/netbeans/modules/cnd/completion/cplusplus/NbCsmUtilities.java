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

package org.netbeans.modules.cnd.completion.cplusplus;

import org.netbeans.editor.BaseDocument;

/**
 *
 * @author vv159170
 */
public class NbCsmUtilities {

    private BaseDocument doc;
//    private FileObject fileObject = null;

    // ..........................................................................

    public static synchronized NbCsmUtilities get(BaseDocument doc) {
        NbCsmUtilities utils = (NbCsmUtilities)doc.getProperty(NbCsmUtilities.class);
        if (utils == null) {
            utils = new NbCsmUtilities(doc);
            doc.putProperty(NbCsmUtilities.class, utils);
        }
        return utils;
    }
    
    /**
     * Creates a new instance of NbCsmUtilities
     */
    private NbCsmUtilities(BaseDocument doc){
        this.doc = doc;
//        DataObject dob = NbEditorUtilities.getDataObject(doc);
//        if (dob != null) {
//            FileObject fo=dob.getPrimaryFile();
//            ClassPath sourceCP = ClassPath.getClassPath(fo, ClassPath.SOURCE);
//            if (sourceCP != null) {
//                this.fileObject = sourceCP.findOwnerRoot(fo);
//            }
//        }
    }

//    private Object getAssociatedObject(Object item) {
//        return item;
//    }
    
//    public Object findItemAtCaretPos(JTextComponent target){
//        Completion completion = ExtUtilities.getCompletion(target);
//        if (completion != null) {
//            if (completion.isPaneVisible()) { // completion pane visible
//                Object item = getAssociatedObject(completion.getSelectedValue());
//                if (item != null) {
//                    return item;
//                }
//            } else { // pane not visible
//                try {
//                    SyntaxSupport sup = Utilities.getSyntaxSupport(target);
//                    CompletionQuery qry = (CompletionQuery)completion.getQuery();
//                    if (!(qry instanceof NbCsmCompletionQuery)) {
//                        return null;
//                    }
//                    NbCsmCompletionQuery query = (NbCsmCompletionQuery)qry;
//
//                    int dotPos = target.getCaret().getDot();
//                    BaseDocument doc = (BaseDocument)target.getDocument();
//                    int[] idFunBlk = NbEditorUtilities.getIdentifierAndMethodBlock(doc, dotPos);
//
//                    if (idFunBlk == null) {
//                        idFunBlk = new int[] { dotPos, dotPos };
//                    }
//
//                    for (int ind = idFunBlk.length - 1; ind >= 1; ind--) {
//                        CompletionQuery.Result result = query.query(target, idFunBlk[ind], sup, true);
//                        if (result != null && result.getData().size() > 0) {
//                            Object itm = getAssociatedObject(result.getData().get(0));
//                            if (result.getData().size() > 1 /*&& (itm instanceof Constructor || itm instanceof Method)*/) {
//                                // It is overloaded method, lets check for the right one
////                                int endOfMethod = JCExtension.findEndOfMethod(target, idFunBlk[ind]);
//                                int endOfMethod = findEndOfMethod(target, idFunBlk[ind]);
//                                if (endOfMethod > -1){
//                                    CompletionQuery.Result resultx = query.query(target, endOfMethod, sup, true);
//                                    if (resultx != null && resultx.getData().size() > 0) {
//                                        return getAssociatedObject(resultx.getData().get(0));
//                                    }
//                                }
//                            }
//                            return itm;
//                        }
//                    }
//                } catch (BadLocationException e) {
//                }
//            }
//        }
//        // Complete the messages
//        return null;
//
//    }    
    
//    static int findEndOfMethod(JTextComponent textComp, int startPos){
//        try{
//            int level = 0;
//            BaseDocument doc = (BaseDocument)textComp.getDocument();
//            for(int i = startPos;  i<textComp.getDocument().getLength(); i++){
//                char ch = doc.getChars(i, 1)[0];
//                if (ch == ';') return -1;
//                if (ch == '(') level++;
//                if (ch == ')'){
//                    if (level == 0){
//                        return i+1;
//                    }else{
//                        level--;
//                    }
//                }
//            }
//            return -1;
//        } catch (BadLocationException e) {
//            return -1;
//        }
//    }
}
