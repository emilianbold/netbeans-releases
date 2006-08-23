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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.completion.cplusplus;

import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.SyntaxSupport;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.Completion;
import org.netbeans.editor.ext.CompletionQuery;
import org.netbeans.editor.ext.ExtUtilities;
import org.netbeans.modules.editor.NbEditorUtilities;

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
